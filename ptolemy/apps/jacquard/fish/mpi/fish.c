#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <math.h>

#include <mpi.h>

#include "config.h"
#include "fish.h"
#include "mpi-fish.h"

FILE *output_fp; /* Pointer to output file */
int outputp;

#ifdef TRACE_WITH_VAMPIR
  int tracingp;
#endif

int n_fish = 10; /* Other defaults in fish-lib.c. */
fish_t* fish; /* _All_ fish */
int n_local_fish;
fish_t* local_fish;

#define start_mpi_timer(t) do { *(t) -= MPI_Wtime(); } while (0)
#define stop_mpi_timer(t) do { *(t) += MPI_Wtime(); } while (0)

void make_fishtype (MPI_Datatype* fishtype);
#define MIN_FISH_PER_PROC 2
void split_fish (const int n_proc, int* fish_off, int* n_fish_split);

void init_fish (const int rank,
		const int* fish_off, const int* n_fish_split);
void interact_fish(fish_t* local_fish_, const int n_local_fish,
		   const fish_t* fish_, const int n_fish);
void bounce_fish (fish_t* fish);
void move_fish (fish_t* fish, const int n_fish, const double dt);
double compute_norm (const fish_t* fish, const int n_fish);
void output_fish(FILE* output_fp, const double t, const double dt,
		 const fish_t* fish, const int n_fish);

int
main(int argc, char **argv)
{
  const MPI_Comm comm = MPI_COMM_WORLD;
  int n_proc, rank;
  MPI_Datatype fishtype;

  double sum_total_timer, total_timer = 0.0;
  double sum_gather_timer, gather_timer = 0.0;
  double sum_mpi_timer, mpi_timer = 0.0;

  double curr_time;
  double output_time;
  double dt = 0.0;
  double local_max_norm, max_norm;
  int steps;

  int* fish_off;
  int* n_fish_split;

  MPI_Init (&argc, &argv);

#ifdef TRACE_WITH_VAMPIR
  VT_symdef(TRACE_LOCAL_COMP, "Local computation", "Computation");
  VT_symdef(TRACE_FISH_GATHER, "Gathering to 0", "Communication");
  VT_symdef(TRACE_MAX_NORM, "Collecting max norm", "Communication");
  VT_symdef(TRACE_OUTPUT, "Output", "Output");
#endif

  MPI_Comm_size (comm, &n_proc);
  MPI_Comm_rank (comm, &rank);
  make_fishtype (&fishtype);

  get_options(argc, argv);
  srand48(clock());

#ifdef TRACE_WITH_VAMPIR
    VT_traceoff();
#endif

  if (output_filename) {
    outputp = 1;
    if (0 == rank) {
      output_fp = fopen(output_filename, "w");
      if (output_fp == NULL) {
	printf("Could not open %s for output\n", output_filename);
	exit(1);
      }
      fprintf(output_fp, "n_fish: %d\n", n_fish);
    }
  }

  fish_off = malloc ( (n_proc+1) * sizeof(int) );
  n_fish_split = malloc ( (n_proc) * sizeof(int) );
  split_fish (n_proc, fish_off, n_fish_split);
  n_local_fish = n_fish_split[rank];

  /*
    All fish are generated on proc 0 to ensure same random numbers.
    (Yes, the circle case could be parallelized.  Feel free to
    do it.)
  */
  init_fish (rank, fish_off, n_fish_split);

  MPI_Scatterv (fish, n_fish_split, fish_off, fishtype,
		local_fish, n_local_fish, fishtype,
		0, comm);

#ifdef TRACE_WITH_VAMPIR
    tracingp = 1;
    VT_traceon();
#endif

  start_mpi_timer(&total_timer);

  for (output_time = 0.0, curr_time = 0.0, steps = 0;
       curr_time <= end_time && steps < max_steps;
       curr_time += dt, ++steps) {

#ifdef TRACE_WITH_VAMPIR
    if (steps >= STEPS_TO_TRACE) {
      tracingp = 0; VT_traceoff();
    }
#endif

    trace_begin(TRACE_FISH_GATHER);
    start_mpi_timer (&gather_timer);
    start_mpi_timer (&mpi_timer);
    /* 
       Pull in all the fish.  Obviously, this is not a good idea.
       You will be greatly expanding this one line...

       However, feel free to waste memory when producing output.
       If you're dumping fish to a file, go ahead and do an
       Allgatherv _in the output steps_ if you want.  Or you could
       pipeline dumping the fish.
    */
    MPI_Allgatherv (local_fish, n_local_fish, fishtype,
		    fish, n_fish_split, fish_off, fishtype, comm);
    stop_mpi_timer (&gather_timer);
    stop_mpi_timer (&mpi_timer);
    trace_end(TRACE_FISH_GATHER);

    /*
      We only output once every output_interval time unit, at most.
      Without that restriction, we can easily create a huge output
      file.  Printing a record for ten fish takes about 300 bytes, so
      for every 1000 steps, we could dump 300K of info.  Now scale the
      number of fish by 1000...
     */
    trace_begin(TRACE_OUTPUT);
    if (outputp && curr_time >= output_time) {
      if (0 == rank)
	output_fish (output_fp, curr_time, dt, fish, n_fish);
      output_time = curr_time + output_interval;
    }
    trace_end(TRACE_OUTPUT);

    trace_begin (TRACE_LOCAL_COMP);
    interact_fish (local_fish, n_local_fish, fish, n_fish);
    local_max_norm = compute_norm (local_fish, n_local_fish);
    trace_end (TRACE_LOCAL_COMP);

    trace_begin (TRACE_MAX_NORM);
    start_mpi_timer (&mpi_timer);
    MPI_Allreduce (&local_max_norm, &max_norm, 1, MPI_DOUBLE, MPI_MAX, comm);
    stop_mpi_timer (&mpi_timer);
    trace_end (TRACE_MAX_NORM);

    trace_begin (TRACE_LOCAL_COMP);
    dt = max_norm_change / max_norm;
    dt = f_max(dt, min_dt);
    dt = f_min(dt, max_dt);

    move_fish(local_fish, n_local_fish, dt);
    trace_end (TRACE_LOCAL_COMP);
  }

  stop_mpi_timer(&total_timer);

#ifdef TRACE_WITH_VAMPIR
    VT_traceoff();
#endif

  if (outputp) {
    MPI_Allgatherv (local_fish, n_local_fish, fishtype,
		    fish, n_fish_split, fish_off, fishtype, comm);
    if (0 == rank) {
      output_fish (output_fp, curr_time, dt, fish, n_fish);
      printf("\tEnded at %g (%g), %d (%d) steps\n",
	     curr_time, end_time, steps, max_steps);
    }
  }

  MPI_Reduce (&total_timer, &sum_total_timer, 1, MPI_DOUBLE,
	      MPI_SUM, 0, comm);
  MPI_Reduce (&gather_timer, &sum_gather_timer, 1, MPI_DOUBLE,
	      MPI_SUM, 0, comm);
  MPI_Reduce (&mpi_timer, &sum_mpi_timer, 1, MPI_DOUBLE,
	      MPI_SUM, 0, comm);

  if (0 == rank) {
    printf("Number of PEs: %d\n"
	   "Time taken on 0: %g (avg. %g)\n"
	   "Time in gathers on 0: %g (avg %g)\n"
	   "Time in MPI on 0: %g (avg %g)\n",
	   n_proc,
	   total_timer, sum_total_timer / n_proc,
	   gather_timer, sum_gather_timer / n_proc,
	   mpi_timer, sum_mpi_timer / n_proc);
  }

  MPI_Barrier (comm);
  MPI_Finalize ();
  return 0;
}


/* Compute the global maximum of all velocities / accelerations.  */
double
compute_norm (const fish_t* fish, const int n_fish)
{
  int i;
  double max_norm = 0.0;

  for (i = 0; i < n_fish; ++i) {
    max_norm = f_max (max_norm, fabs(fish[i].vx));
    max_norm = f_max (max_norm, fabs(fish[i].vy));
    max_norm = f_max (max_norm, fabs(fish[i].ax));
    max_norm = f_max (max_norm, fabs(fish[i].ay));
  }

  return max_norm;
}


/* Compute the accelerations (force/mass) for each fish */
void
interact_fish(fish_t* local_fish_, const int n_local_fish,
	      const fish_t* fish_, const int n_fish)
{
  const fish_t* restrict fish = fish_;
  fish_t* restrict local_fish = local_fish_;
  int i, j;

  for (i = 0; i < n_local_fish; ++i) {

    local_fish[i].ax = 0.0;
    local_fish[i].ay = 0.0;

    for (j = 0; j < n_fish; ++j) {
      double dx, dy;

      dx = fish[j].x - local_fish[i].x;
      dy = fish[j].y - local_fish[i].y;

      if (dx != 0 || dy != 0) {
	double r2, r, a;

	r2 = f_max(dx * dx + dy * dy, min_r2);
	r = sqrt(r2);
	a = G * FISH_MASS / r2;

	local_fish[i].ax += a * dx / r;
	local_fish[i].ay += a * dy / r;
      }
    }
  }
}


/* Allocate and initialize the fish positions / velocities / accelerations. */
void
init_fish (const int rank,
	   const int* fish_off, const int* n_fish_split)
{
  int i, li;
  fish = malloc(n_fish * sizeof(fish_t));
  local_fish = malloc(n_local_fish * sizeof(fish_t));
  if (0 == rank) {
    for (i = 0; i < n_fish; ++i) {
      if (uniformp) {
	fish[i].x = unscale_coord(drand48());
	fish[i].y = unscale_coord(drand48());
      } else {
	const double angle = i * (2.0 * M_PI / n_fish);
	fish[i].x = unscale_coord(0.5 * cos(angle) + 0.5);
	fish[i].y = unscale_coord(0.5 * sin(angle) + 0.5);
      }
      fish[i].vx = fish[i].vy = 0.0;
      fish[i].ax = fish[i].ay = 0.0;
    }
  }
}


/* Apply reflective boundary conditions (fish bounce off walls). */
void
bounce_fish (fish_t* fish)
{
  while (fish->x < LEFT_WALL || fish->x > RIGHT_WALL) {
    if (fish->x < LEFT_WALL) {
      fish->x = 2.0 * LEFT_WALL - fish->x;
      fish->vx = -fish->vx;
    }
    if (fish->x > RIGHT_WALL) {
      fish->x = 2.0 * RIGHT_WALL - fish->x;
      fish->vx = -fish->vx;
    }
  }
  while (fish->y < LEFT_WALL || fish->y > RIGHT_WALL) {
    if (fish->y < LEFT_WALL) {
      fish->y = 2.0 * LEFT_WALL - fish->y;
      fish->vy = -fish->vy;
    }
    if (fish->y > RIGHT_WALL) {
      fish->y = 2.0 * RIGHT_WALL - fish->y;
      fish->vy = -fish->vy;
    }
  }
}


/* Actually move the fish. */
void
move_fish(fish_t* fish, const int n_fish, const double dt)
{
  int i;
  for (i = 0; i < n_fish; ++i) {
    fish[i].x += dt * fish[i].vx;
    fish[i].y += dt * fish[i].vy;
    fish[i].vx += dt * fish[i].ax;
    fish[i].vy += dt * fish[i].ay;
    bounce_fish(&fish[i]);

    assert(scale_coord(fish[i].x) >= 0.0);
    assert(scale_coord(fish[i].y) >= 0.0);
  }
}


/*
  Dump out all the fishies (and their center of gravity)
  in a format that the viewer understands.
*/
void
output_fish(FILE* output_fp, const double t, const double dt,
	    const fish_t* fish, const int n_fish)
{
  int i;
  double cg_x = 0.0;
  double cg_y = 0.0;

  fprintf(output_fp, "%.5g (%.5g):\n", t, dt);
  for (i = 0; i < n_fish; ++i) {
    cg_x += fish[i].x;
    cg_y += fish[i].y;
    fprintf(output_fp, "  %d: (%g, %g)\n", i,
	    scale_coord(fish[i].x), scale_coord(fish[i].y));
  }
  cg_x /= n_fish;
  cg_y /= n_fish;
  fprintf(output_fp, "  cg: (%g, %g)\n", scale_coord(cg_x),
	  scale_coord(cg_y));
}


void
make_fishtype (MPI_Datatype* fishtype)
{
  int err, i;

  /* QQQ: How does the data type affect performance? */
#if 1
  MPI_Aint disp[8];
  MPI_Datatype types[8] = { MPI_LB, MPI_DOUBLE, MPI_DOUBLE, MPI_DOUBLE,
			    MPI_DOUBLE, MPI_DOUBLE, MPI_DOUBLE, MPI_UB };
  int blocklen[8] = { 1, 1, 1, 1, 1, 1, 1, 1 };
  fish_t example[2];

  MPI_Address (&example[0], &disp[0]);
  MPI_Address (&example[0].x, &disp[1]);
  MPI_Address (&example[0].y, &disp[2]);
  MPI_Address (&example[0].vx, &disp[3]);
  MPI_Address (&example[0].vy, &disp[4]);
  MPI_Address (&example[0].ax, &disp[5]);
  MPI_Address (&example[0].ay, &disp[6]);
  MPI_Address (&example[1], &disp[7]);
  for (i = 7; i >= 0; --i) disp[i] -= disp[0];

  err = MPI_Type_struct (8, &blocklen[0], &disp[0], &types[0], fishtype);
#elif 0
  MPI_Aint disp[2];
  MPI_Aint base;
  MPI_Datatype types[2] = { MPI_DOUBLE, MPI_UB };
  int blocklen[2] = { 6, 1 };
  fish_t example[2];

  MPI_Address (&example[0], &base);
  MPI_Address (&example[0].x, &disp[0]);
  MPI_Address (&example[1], &disp[1]);
  disp[0] -= base;
  disp[1] -= base;
  err = MPI_Type_struct (2, blocklen, disp, types, fishtype);
#else
  err = MPI_Type_contiguous (6, MPI_DOUBLE, fishtype);
#endif

  if (err) {
    fprintf (stderr, "Error creating type: %d\n", err);
    MPI_Abort (MPI_COMM_WORLD, -29);
  }
  MPI_Type_commit (fishtype);
}


void
split_fish (const int n_proc, int* off, int* n)
{
  int n_per;
  int idx, num, p;

  n_per = (n_fish + n_proc - 1) / n_proc;
  if (n_per < MIN_FISH_PER_PROC) n_per = MIN_FISH_PER_PROC;

  idx = 0;
  num = n_per;
  for (p = 0; p < n_proc; ++p) {
    if (idx + num > n_fish)
      num = n_fish - idx;

    n[p] = num;
    off[p] = idx;
    idx += num;
  }
  off[p] = idx;
}
