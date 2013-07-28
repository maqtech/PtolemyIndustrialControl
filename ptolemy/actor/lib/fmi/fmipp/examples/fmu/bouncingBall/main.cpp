using namespace std;

#include <cstdio>
#include <cstdlib>
#include <iostream>

#include "FMU.h"


int main( int argc, char** argv )
{
	string MODELNAME( "bouncingBallME1" );
	FMU fmu( MODELNAME );

	fmiStatus status = fmu.instantiate( "bouncingBallME1", fmiFalse );
	fmu.logger( status, "instantiation" );
	if ( status != fmiOK ) cout << "instantiation : " << status << endl;

	//status = fmu.setValue("p", 0.1);
	//status = fmu.setValue("x", 0.5);

	status = fmu.initialize();
	fmu.logger( status, "initialization" );
	if ( status != fmiOK )  cout << "initialization : " << status << endl;

	fmiReal h_;
	fmiReal v_;
	double t = 0;
	double commStepSize = 0.1;
	double tstop = 5.0;

	status = fmu.getValue( "h", h_ );
	status = fmu.getValue( "v", v_ );

	printf( "  time      h        v    \n" );
	printf( "%6.3f %8.4f  %8.4f\n",t,h_,v_ );

	while ( t < tstop )
	{
		fmu.integrate(t+commStepSize);

		status = fmu.getValue( "h", h_ );
		status = fmu.getValue( "v", v_ );

		t += commStepSize;
		printf( "%6.3f %8.4f  %8.4f\n",t,h_,v_ );
	}

	cout << "time " << t << endl;

	return 0;
}
