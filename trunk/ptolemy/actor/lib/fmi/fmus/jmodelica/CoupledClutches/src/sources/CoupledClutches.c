#include "stdio.h"
#include "stdlib.h"
#include "math.h"
#include "jmi.h"
#include "jmi_block_residual.h"
#include "jmi_log.h"
#include "ModelicaUtilities.h"
#include "ModelicaStandardTables.h"

#include "fmi2_me.h"
#include "fmi2_cs.h"
#include "fmiFunctions.h"
#include "fmiFunctionTypes.h"
#include "fmiTypesPlatform.h"

#define FMI_FUNCTION_PREFIX CoupledClutches



extern void dgemm_(char* TRANSA, char* TRANSB, int* M, int* N, int* K, double* ALPHA, double* A, int* LDA, double* B, int* LDB, double* BETA, double* C, int* LDC);

const char *C_GUID = "ea7aec0861f0b63ed1a2bab0365537ae";

static int model_ode_guards_init(jmi_t* jmi);
static int model_init_R0(jmi_t* jmi, jmi_real_t** res);
static int model_ode_initialize(jmi_t* jmi);

static const int N_real_ci = 9;
static const int N_real_cd = 0;
static const int N_real_pi = 52;
static const int N_real_pd = 9;

static const int N_integer_ci = 15 + 0;
static const int N_integer_cd = 0 + 0;
static const int N_integer_pi = 6 + 7;
static const int N_integer_pd = 0 + 0;

static const int N_boolean_ci = 0;
static const int N_boolean_cd = 0;
static const int N_boolean_pi = 12;
static const int N_boolean_pd = 0;

static const int N_string_ci = 0;
static const int N_string_cd = 0;
static const int N_string_pi = 0;
static const int N_string_pd = 0;

static const int N_real_dx = 8;
static const int N_real_x = 8;
static const int N_real_u = 0;
static const int N_real_w = 48;

static const int N_real_d = 0;

static const int N_integer_d = 3 + 0;
static const int N_integer_u = 0 + 0;

static const int N_boolean_d = 12;
static const int N_boolean_u = 0;

static const int N_string_d = 0;
static const int N_string_u = 0;

static const int N_ext_objs = 0;

static const int N_sw = 37;
static const int N_eq_F = 71;
static const int N_eq_R = 37;

static const int N_dae_blocks = 1;
static const int N_dae_init_blocks = 1;
static const int N_guards = 0;

static const int N_eq_F0 = 71 + 23;
static const int N_eq_F1 = 56;
static const int N_eq_Fp = 0;
static const int N_eq_R0 = 37 + 0;
static const int N_sw_init = 0;
static const int N_guards_init = 0;

static const int Scaling_method = JMI_SCALING_NONE;

#define sf(i) (jmi->variable_scaling_factors[i])

static const int N_outputs = 0;
static const int Output_vrefs[1] = {-1};

static int CAD_dae_n_nz = 1;
static const int CAD_dae_nz_rows[1] = {-1};
static const int CAD_dae_nz_cols[1] = {-1};


static const int CAD_ODE_A_n_nz = 0;
static const int CAD_ODE_B_n_nz = 0;
static const int CAD_ODE_C_n_nz = 0;
static const int CAD_ODE_D_n_nz = 0;
static const int CAD_ODE_A_nz_rows[1] = {-1};
static const int CAD_ODE_A_nz_cols[1] = {-1};
static const int CAD_ODE_B_nz_rows[1] = {-1};
static const int CAD_ODE_B_nz_cols[1] = {-1};
static const int CAD_ODE_C_nz_rows[1] = {-1};
static const int CAD_ODE_C_nz_cols[1] = {-1};
static const int CAD_ODE_D_nz_rows[1] = {-1};
static const int CAD_ODE_D_nz_cols[1] = {-1};


static const int N_initial_relations = 0;
static const int DAE_initial_relations[] = { -1 };

static const int N_relations = 37;
static const int DAE_relations[] = { JMI_REL_LEQ, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_GT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LEQ, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_GT, JMI_REL_LT, JMI_REL_LEQ, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_GT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT };

static const int N_nominals = 8;
static const jmi_real_t DAE_nominals[] = { 1.0, 1.0E-4, 1.0, 1.0, 1.0E-4, 1.0, 1.0E-4, 1.0 };

#define _clutch1_unitAngularAcceleration_39 ((*(jmi->z))[jmi->offs_real_ci+0])
#define _clutch1_unitTorque_40 ((*(jmi->z))[jmi->offs_real_ci+1])
#define _sin1_pi_48 ((*(jmi->z))[jmi->offs_real_ci+2])
#define _clutch2_unitAngularAcceleration_85 ((*(jmi->z))[jmi->offs_real_ci+3])
#define _clutch2_unitTorque_86 ((*(jmi->z))[jmi->offs_real_ci+4])
#define _clutch3_unitAngularAcceleration_122 ((*(jmi->z))[jmi->offs_real_ci+5])
#define _clutch3_unitTorque_123 ((*(jmi->z))[jmi->offs_real_ci+6])
#define _J4_flange_b_tau_126 ((*(jmi->z))[jmi->offs_real_ci+7])
#define _sin2_pi_137 ((*(jmi->z))[jmi->offs_real_ci+8])
#define _freqHz_0 ((*(jmi->z))[jmi->offs_real_pi+0])
#define _T2_1 ((*(jmi->z))[jmi->offs_real_pi+1])
#define _T3_2 ((*(jmi->z))[jmi->offs_real_pi+2])
#define _J1_J_3 ((*(jmi->z))[jmi->offs_real_pi+3])
#define _clutch1_mue_pos_1_1_11 ((*(jmi->z))[jmi->offs_real_pi+4])
#define _clutch1_mue_pos_1_2_12 ((*(jmi->z))[jmi->offs_real_pi+5])
#define _clutch1_peak_13 ((*(jmi->z))[jmi->offs_real_pi+6])
#define _clutch1_cgeo_14 ((*(jmi->z))[jmi->offs_real_pi+7])
#define _clutch1_fn_max_15 ((*(jmi->z))[jmi->offs_real_pi+8])
#define _clutch1_phi_nominal_23 ((*(jmi->z))[jmi->offs_real_pi+9])
#define _clutch1_w_small_25 ((*(jmi->z))[jmi->offs_real_pi+10])
#define _sin1_amplitude_43 ((*(jmi->z))[jmi->offs_real_pi+11])
#define _sin1_freqHz_44 ((*(jmi->z))[jmi->offs_real_pi+12])
#define _sin1_phase_45 ((*(jmi->z))[jmi->offs_real_pi+13])
#define _sin1_offset_46 ((*(jmi->z))[jmi->offs_real_pi+14])
#define _sin1_startTime_47 ((*(jmi->z))[jmi->offs_real_pi+15])
#define _step1_height_49 ((*(jmi->z))[jmi->offs_real_pi+16])
#define _step1_offset_50 ((*(jmi->z))[jmi->offs_real_pi+17])
#define _J2_J_52 ((*(jmi->z))[jmi->offs_real_pi+18])
#define _clutch2_mue_pos_1_1_57 ((*(jmi->z))[jmi->offs_real_pi+19])
#define _clutch2_mue_pos_1_2_58 ((*(jmi->z))[jmi->offs_real_pi+20])
#define _clutch2_peak_59 ((*(jmi->z))[jmi->offs_real_pi+21])
#define _clutch2_cgeo_60 ((*(jmi->z))[jmi->offs_real_pi+22])
#define _clutch2_fn_max_61 ((*(jmi->z))[jmi->offs_real_pi+23])
#define _clutch2_phi_nominal_69 ((*(jmi->z))[jmi->offs_real_pi+24])
#define _clutch2_w_small_71 ((*(jmi->z))[jmi->offs_real_pi+25])
#define _J3_J_89 ((*(jmi->z))[jmi->offs_real_pi+26])
#define _clutch3_mue_pos_1_1_94 ((*(jmi->z))[jmi->offs_real_pi+27])
#define _clutch3_mue_pos_1_2_95 ((*(jmi->z))[jmi->offs_real_pi+28])
#define _clutch3_peak_96 ((*(jmi->z))[jmi->offs_real_pi+29])
#define _clutch3_cgeo_97 ((*(jmi->z))[jmi->offs_real_pi+30])
#define _clutch3_fn_max_98 ((*(jmi->z))[jmi->offs_real_pi+31])
#define _clutch3_phi_nominal_106 ((*(jmi->z))[jmi->offs_real_pi+32])
#define _clutch3_w_small_108 ((*(jmi->z))[jmi->offs_real_pi+33])
#define _J4_J_127 ((*(jmi->z))[jmi->offs_real_pi+34])
#define _sin2_amplitude_132 ((*(jmi->z))[jmi->offs_real_pi+35])
#define _sin2_phase_134 ((*(jmi->z))[jmi->offs_real_pi+36])
#define _sin2_offset_135 ((*(jmi->z))[jmi->offs_real_pi+37])
#define _sin2_startTime_136 ((*(jmi->z))[jmi->offs_real_pi+38])
#define _step2_height_138 ((*(jmi->z))[jmi->offs_real_pi+39])
#define _step2_offset_139 ((*(jmi->z))[jmi->offs_real_pi+40])
#define _fixed_phi0_141 ((*(jmi->z))[jmi->offs_real_pi+41])
#define __block_jacobian_check_tol_166 ((*(jmi->z))[jmi->offs_real_pi+42])
#define __cs_rel_tol_168 ((*(jmi->z))[jmi->offs_real_pi+43])
#define __cs_step_size_170 ((*(jmi->z))[jmi->offs_real_pi+44])
#define __events_default_tol_172 ((*(jmi->z))[jmi->offs_real_pi+45])
#define __events_tol_factor_173 ((*(jmi->z))[jmi->offs_real_pi+46])
#define __nle_solver_default_tol_177 ((*(jmi->z))[jmi->offs_real_pi+47])
#define __nle_solver_min_tol_179 ((*(jmi->z))[jmi->offs_real_pi+48])
#define __nle_solver_regularization_tolerance_180 ((*(jmi->z))[jmi->offs_real_pi+49])
#define __nle_solver_step_limit_factor_181 ((*(jmi->z))[jmi->offs_real_pi+50])
#define __nle_solver_tol_factor_182 ((*(jmi->z))[jmi->offs_real_pi+51])
#define _step1_startTime_10 ((*(jmi->z))[jmi->offs_real_pd+0])
#define _sin2_freqHz_16 ((*(jmi->z))[jmi->offs_real_pd+1])
#define _step2_startTime_51 ((*(jmi->z))[jmi->offs_real_pd+2])
#define _clutch1_mue0_62 ((*(jmi->z))[jmi->offs_real_pd+3])
#define _clutch2_mue0_99 ((*(jmi->z))[jmi->offs_real_pd+4])
#define _clutch3_mue0_133 ((*(jmi->z))[jmi->offs_real_pd+5])
#define _torque_phi_support_140 ((*(jmi->z))[jmi->offs_real_pd+6])
#define _fixed_flange_phi_157 ((*(jmi->z))[jmi->offs_real_pd+7])
#define _torque_support_phi_158 ((*(jmi->z))[jmi->offs_real_pd+8])
#define _clutch1_Unknown_33 ((*(jmi->z))[jmi->offs_integer_ci+0])
#define _clutch1_Free_34 ((*(jmi->z))[jmi->offs_integer_ci+1])
#define _clutch1_Forward_35 ((*(jmi->z))[jmi->offs_integer_ci+2])
#define _clutch1_Stuck_36 ((*(jmi->z))[jmi->offs_integer_ci+3])
#define _clutch1_Backward_37 ((*(jmi->z))[jmi->offs_integer_ci+4])
#define _clutch2_Unknown_79 ((*(jmi->z))[jmi->offs_integer_ci+5])
#define _clutch2_Free_80 ((*(jmi->z))[jmi->offs_integer_ci+6])
#define _clutch2_Forward_81 ((*(jmi->z))[jmi->offs_integer_ci+7])
#define _clutch2_Stuck_82 ((*(jmi->z))[jmi->offs_integer_ci+8])
#define _clutch2_Backward_83 ((*(jmi->z))[jmi->offs_integer_ci+9])
#define _clutch3_Unknown_116 ((*(jmi->z))[jmi->offs_integer_ci+10])
#define _clutch3_Free_117 ((*(jmi->z))[jmi->offs_integer_ci+11])
#define _clutch3_Forward_118 ((*(jmi->z))[jmi->offs_integer_ci+12])
#define _clutch3_Stuck_119 ((*(jmi->z))[jmi->offs_integer_ci+13])
#define _clutch3_Backward_120 ((*(jmi->z))[jmi->offs_integer_ci+14])
#define __block_solver_experimental_mode_167 ((*(jmi->z))[jmi->offs_integer_pi+0])
#define __cs_solver_169 ((*(jmi->z))[jmi->offs_integer_pi+1])
#define __iteration_variable_scaling_174 ((*(jmi->z))[jmi->offs_integer_pi+2])
#define __log_level_175 ((*(jmi->z))[jmi->offs_integer_pi+3])
#define __nle_solver_max_iter_178 ((*(jmi->z))[jmi->offs_integer_pi+4])
#define __residual_equation_scaling_185 ((*(jmi->z))[jmi->offs_integer_pi+5])
#define _J1_stateSelect_4 ((*(jmi->z))[jmi->offs_integer_pi+6])
#define _clutch1_stateSelect_24 ((*(jmi->z))[jmi->offs_integer_pi+7])
#define _J2_stateSelect_53 ((*(jmi->z))[jmi->offs_integer_pi+8])
#define _clutch2_stateSelect_70 ((*(jmi->z))[jmi->offs_integer_pi+9])
#define _J3_stateSelect_90 ((*(jmi->z))[jmi->offs_integer_pi+10])
#define _clutch3_stateSelect_107 ((*(jmi->z))[jmi->offs_integer_pi+11])
#define _J4_stateSelect_128 ((*(jmi->z))[jmi->offs_integer_pi+12])
#define _torque_useSupport_9 ((*(jmi->z))[jmi->offs_boolean_pi+0])
#define _clutch1_useHeatPort_41 ((*(jmi->z))[jmi->offs_boolean_pi+1])
#define _clutch2_useHeatPort_87 ((*(jmi->z))[jmi->offs_boolean_pi+2])
#define _clutch3_useHeatPort_124 ((*(jmi->z))[jmi->offs_boolean_pi+3])
#define __block_jacobian_check_165 ((*(jmi->z))[jmi->offs_boolean_pi+4])
#define __enforce_bounds_171 ((*(jmi->z))[jmi->offs_boolean_pi+5])
#define __nle_solver_check_jac_cond_176 ((*(jmi->z))[jmi->offs_boolean_pi+6])
#define __rescale_after_singular_jac_183 ((*(jmi->z))[jmi->offs_boolean_pi+7])
#define __rescale_each_step_184 ((*(jmi->z))[jmi->offs_boolean_pi+8])
#define __runtime_log_to_file_186 ((*(jmi->z))[jmi->offs_boolean_pi+9])
#define __use_Brent_in_1d_187 ((*(jmi->z))[jmi->offs_boolean_pi+10])
#define __use_jacobian_equilibration_188 ((*(jmi->z))[jmi->offs_boolean_pi+11])
#define _der_J1_w_196 ((*(jmi->z))[jmi->offs_real_dx+0])
#define _der_clutch1_phi_rel_197 ((*(jmi->z))[jmi->offs_real_dx+1])
#define _der_clutch1_w_rel_198 ((*(jmi->z))[jmi->offs_real_dx+2])
#define _der_J2_phi_199 ((*(jmi->z))[jmi->offs_real_dx+3])
#define _der_clutch2_phi_rel_200 ((*(jmi->z))[jmi->offs_real_dx+4])
#define _der_clutch2_w_rel_201 ((*(jmi->z))[jmi->offs_real_dx+5])
#define _der_clutch3_phi_rel_202 ((*(jmi->z))[jmi->offs_real_dx+6])
#define _der_clutch3_w_rel_203 ((*(jmi->z))[jmi->offs_real_dx+7])
#define _J1_w_6 ((*(jmi->z))[jmi->offs_real_x+0])
#define _clutch1_phi_rel_19 ((*(jmi->z))[jmi->offs_real_x+1])
#define _clutch1_w_rel_20 ((*(jmi->z))[jmi->offs_real_x+2])
#define _J2_phi_54 ((*(jmi->z))[jmi->offs_real_x+3])
#define _clutch2_phi_rel_65 ((*(jmi->z))[jmi->offs_real_x+4])
#define _clutch2_w_rel_66 ((*(jmi->z))[jmi->offs_real_x+5])
#define _clutch3_phi_rel_102 ((*(jmi->z))[jmi->offs_real_x+6])
#define _clutch3_w_rel_103 ((*(jmi->z))[jmi->offs_real_x+7])
#define _J1_phi_5 ((*(jmi->z))[jmi->offs_real_w+0])
#define _J1_a_7 ((*(jmi->z))[jmi->offs_real_w+1])
#define _torque_tau_8 ((*(jmi->z))[jmi->offs_real_w+2])
#define _clutch1_fn_17 ((*(jmi->z))[jmi->offs_real_w+3])
#define _clutch1_f_normalized_18 ((*(jmi->z))[jmi->offs_real_w+4])
#define _clutch1_a_rel_21 ((*(jmi->z))[jmi->offs_real_w+5])
#define _clutch1_tau_22 ((*(jmi->z))[jmi->offs_real_w+6])
#define _clutch1_tau0_26 ((*(jmi->z))[jmi->offs_real_w+7])
#define _clutch1_tau0_max_27 ((*(jmi->z))[jmi->offs_real_w+8])
#define _clutch1_sa_29 ((*(jmi->z))[jmi->offs_real_w+9])
#define _clutch1_lossPower_42 ((*(jmi->z))[jmi->offs_real_w+10])
#define _J2_w_55 ((*(jmi->z))[jmi->offs_real_w+11])
#define _J2_a_56 ((*(jmi->z))[jmi->offs_real_w+12])
#define _clutch2_fn_63 ((*(jmi->z))[jmi->offs_real_w+13])
#define _clutch2_f_normalized_64 ((*(jmi->z))[jmi->offs_real_w+14])
#define _clutch2_a_rel_67 ((*(jmi->z))[jmi->offs_real_w+15])
#define _clutch2_tau_68 ((*(jmi->z))[jmi->offs_real_w+16])
#define _clutch2_tau0_72 ((*(jmi->z))[jmi->offs_real_w+17])
#define _clutch2_tau0_max_73 ((*(jmi->z))[jmi->offs_real_w+18])
#define _clutch2_sa_75 ((*(jmi->z))[jmi->offs_real_w+19])
#define _clutch2_lossPower_88 ((*(jmi->z))[jmi->offs_real_w+20])
#define _J3_phi_91 ((*(jmi->z))[jmi->offs_real_w+21])
#define _J3_w_92 ((*(jmi->z))[jmi->offs_real_w+22])
#define _J3_a_93 ((*(jmi->z))[jmi->offs_real_w+23])
#define _clutch3_fn_100 ((*(jmi->z))[jmi->offs_real_w+24])
#define _clutch3_f_normalized_101 ((*(jmi->z))[jmi->offs_real_w+25])
#define _clutch3_a_rel_104 ((*(jmi->z))[jmi->offs_real_w+26])
#define _clutch3_tau_105 ((*(jmi->z))[jmi->offs_real_w+27])
#define _clutch3_tau0_109 ((*(jmi->z))[jmi->offs_real_w+28])
#define _clutch3_tau0_max_110 ((*(jmi->z))[jmi->offs_real_w+29])
#define _clutch3_sa_112 ((*(jmi->z))[jmi->offs_real_w+30])
#define _clutch3_lossPower_125 ((*(jmi->z))[jmi->offs_real_w+31])
#define _J4_phi_129 ((*(jmi->z))[jmi->offs_real_w+32])
#define _J4_w_130 ((*(jmi->z))[jmi->offs_real_w+33])
#define _J4_a_131 ((*(jmi->z))[jmi->offs_real_w+34])
#define _der_J1_phi_159 ((*(jmi->z))[jmi->offs_real_w+35])
#define _der_J2_w_160 ((*(jmi->z))[jmi->offs_real_w+36])
#define _der_J3_phi_161 ((*(jmi->z))[jmi->offs_real_w+37])
#define _der_J3_w_162 ((*(jmi->z))[jmi->offs_real_w+38])
#define _der_J4_phi_163 ((*(jmi->z))[jmi->offs_real_w+39])
#define _der_J4_w_164 ((*(jmi->z))[jmi->offs_real_w+40])
#define _der_2_J1_phi_189 ((*(jmi->z))[jmi->offs_real_w+41])
#define _der_2_clutch1_phi_rel_190 ((*(jmi->z))[jmi->offs_real_w+42])
#define _der_2_J2_phi_191 ((*(jmi->z))[jmi->offs_real_w+43])
#define _der_2_clutch2_phi_rel_192 ((*(jmi->z))[jmi->offs_real_w+44])
#define _der_2_J3_phi_193 ((*(jmi->z))[jmi->offs_real_w+45])
#define _der_2_clutch3_phi_rel_194 ((*(jmi->z))[jmi->offs_real_w+46])
#define _der_2_J4_phi_195 ((*(jmi->z))[jmi->offs_real_w+47])
#define _time ((*(jmi->z))[jmi->offs_t])
#define _clutch1_mode_38 ((*(jmi->z))[jmi->offs_integer_d+0])
#define _clutch2_mode_84 ((*(jmi->z))[jmi->offs_integer_d+1])
#define _clutch3_mode_121 ((*(jmi->z))[jmi->offs_integer_d+2])
#define _clutch1_free_28 ((*(jmi->z))[jmi->offs_boolean_d+0])
#define _clutch1_startForward_30 ((*(jmi->z))[jmi->offs_boolean_d+1])
#define _clutch1_startBackward_31 ((*(jmi->z))[jmi->offs_boolean_d+2])
#define _clutch1_locked_32 ((*(jmi->z))[jmi->offs_boolean_d+3])
#define _clutch2_free_74 ((*(jmi->z))[jmi->offs_boolean_d+4])
#define _clutch2_startForward_76 ((*(jmi->z))[jmi->offs_boolean_d+5])
#define _clutch2_startBackward_77 ((*(jmi->z))[jmi->offs_boolean_d+6])
#define _clutch2_locked_78 ((*(jmi->z))[jmi->offs_boolean_d+7])
#define _clutch3_free_111 ((*(jmi->z))[jmi->offs_boolean_d+8])
#define _clutch3_startForward_113 ((*(jmi->z))[jmi->offs_boolean_d+9])
#define _clutch3_startBackward_114 ((*(jmi->z))[jmi->offs_boolean_d+10])
#define _clutch3_locked_115 ((*(jmi->z))[jmi->offs_boolean_d+11])
#define pre_der_J1_w_196 ((*(jmi->z))[jmi->offs_pre_real_dx+0])
#define pre_der_clutch1_phi_rel_197 ((*(jmi->z))[jmi->offs_pre_real_dx+1])
#define pre_der_clutch1_w_rel_198 ((*(jmi->z))[jmi->offs_pre_real_dx+2])
#define pre_der_J2_phi_199 ((*(jmi->z))[jmi->offs_pre_real_dx+3])
#define pre_der_clutch2_phi_rel_200 ((*(jmi->z))[jmi->offs_pre_real_dx+4])
#define pre_der_clutch2_w_rel_201 ((*(jmi->z))[jmi->offs_pre_real_dx+5])
#define pre_der_clutch3_phi_rel_202 ((*(jmi->z))[jmi->offs_pre_real_dx+6])
#define pre_der_clutch3_w_rel_203 ((*(jmi->z))[jmi->offs_pre_real_dx+7])
#define pre_J1_w_6 ((*(jmi->z))[jmi->offs_pre_real_x+0])
#define pre_clutch1_phi_rel_19 ((*(jmi->z))[jmi->offs_pre_real_x+1])
#define pre_clutch1_w_rel_20 ((*(jmi->z))[jmi->offs_pre_real_x+2])
#define pre_J2_phi_54 ((*(jmi->z))[jmi->offs_pre_real_x+3])
#define pre_clutch2_phi_rel_65 ((*(jmi->z))[jmi->offs_pre_real_x+4])
#define pre_clutch2_w_rel_66 ((*(jmi->z))[jmi->offs_pre_real_x+5])
#define pre_clutch3_phi_rel_102 ((*(jmi->z))[jmi->offs_pre_real_x+6])
#define pre_clutch3_w_rel_103 ((*(jmi->z))[jmi->offs_pre_real_x+7])
#define pre_J1_phi_5 ((*(jmi->z))[jmi->offs_pre_real_w+0])
#define pre_J1_a_7 ((*(jmi->z))[jmi->offs_pre_real_w+1])
#define pre_torque_tau_8 ((*(jmi->z))[jmi->offs_pre_real_w+2])
#define pre_clutch1_fn_17 ((*(jmi->z))[jmi->offs_pre_real_w+3])
#define pre_clutch1_f_normalized_18 ((*(jmi->z))[jmi->offs_pre_real_w+4])
#define pre_clutch1_a_rel_21 ((*(jmi->z))[jmi->offs_pre_real_w+5])
#define pre_clutch1_tau_22 ((*(jmi->z))[jmi->offs_pre_real_w+6])
#define pre_clutch1_tau0_26 ((*(jmi->z))[jmi->offs_pre_real_w+7])
#define pre_clutch1_tau0_max_27 ((*(jmi->z))[jmi->offs_pre_real_w+8])
#define pre_clutch1_sa_29 ((*(jmi->z))[jmi->offs_pre_real_w+9])
#define pre_clutch1_lossPower_42 ((*(jmi->z))[jmi->offs_pre_real_w+10])
#define pre_J2_w_55 ((*(jmi->z))[jmi->offs_pre_real_w+11])
#define pre_J2_a_56 ((*(jmi->z))[jmi->offs_pre_real_w+12])
#define pre_clutch2_fn_63 ((*(jmi->z))[jmi->offs_pre_real_w+13])
#define pre_clutch2_f_normalized_64 ((*(jmi->z))[jmi->offs_pre_real_w+14])
#define pre_clutch2_a_rel_67 ((*(jmi->z))[jmi->offs_pre_real_w+15])
#define pre_clutch2_tau_68 ((*(jmi->z))[jmi->offs_pre_real_w+16])
#define pre_clutch2_tau0_72 ((*(jmi->z))[jmi->offs_pre_real_w+17])
#define pre_clutch2_tau0_max_73 ((*(jmi->z))[jmi->offs_pre_real_w+18])
#define pre_clutch2_sa_75 ((*(jmi->z))[jmi->offs_pre_real_w+19])
#define pre_clutch2_lossPower_88 ((*(jmi->z))[jmi->offs_pre_real_w+20])
#define pre_J3_phi_91 ((*(jmi->z))[jmi->offs_pre_real_w+21])
#define pre_J3_w_92 ((*(jmi->z))[jmi->offs_pre_real_w+22])
#define pre_J3_a_93 ((*(jmi->z))[jmi->offs_pre_real_w+23])
#define pre_clutch3_fn_100 ((*(jmi->z))[jmi->offs_pre_real_w+24])
#define pre_clutch3_f_normalized_101 ((*(jmi->z))[jmi->offs_pre_real_w+25])
#define pre_clutch3_a_rel_104 ((*(jmi->z))[jmi->offs_pre_real_w+26])
#define pre_clutch3_tau_105 ((*(jmi->z))[jmi->offs_pre_real_w+27])
#define pre_clutch3_tau0_109 ((*(jmi->z))[jmi->offs_pre_real_w+28])
#define pre_clutch3_tau0_max_110 ((*(jmi->z))[jmi->offs_pre_real_w+29])
#define pre_clutch3_sa_112 ((*(jmi->z))[jmi->offs_pre_real_w+30])
#define pre_clutch3_lossPower_125 ((*(jmi->z))[jmi->offs_pre_real_w+31])
#define pre_J4_phi_129 ((*(jmi->z))[jmi->offs_pre_real_w+32])
#define pre_J4_w_130 ((*(jmi->z))[jmi->offs_pre_real_w+33])
#define pre_J4_a_131 ((*(jmi->z))[jmi->offs_pre_real_w+34])
#define pre_der_J1_phi_159 ((*(jmi->z))[jmi->offs_pre_real_w+35])
#define pre_der_J2_w_160 ((*(jmi->z))[jmi->offs_pre_real_w+36])
#define pre_der_J3_phi_161 ((*(jmi->z))[jmi->offs_pre_real_w+37])
#define pre_der_J3_w_162 ((*(jmi->z))[jmi->offs_pre_real_w+38])
#define pre_der_J4_phi_163 ((*(jmi->z))[jmi->offs_pre_real_w+39])
#define pre_der_J4_w_164 ((*(jmi->z))[jmi->offs_pre_real_w+40])
#define pre_der_2_J1_phi_189 ((*(jmi->z))[jmi->offs_pre_real_w+41])
#define pre_der_2_clutch1_phi_rel_190 ((*(jmi->z))[jmi->offs_pre_real_w+42])
#define pre_der_2_J2_phi_191 ((*(jmi->z))[jmi->offs_pre_real_w+43])
#define pre_der_2_clutch2_phi_rel_192 ((*(jmi->z))[jmi->offs_pre_real_w+44])
#define pre_der_2_J3_phi_193 ((*(jmi->z))[jmi->offs_pre_real_w+45])
#define pre_der_2_clutch3_phi_rel_194 ((*(jmi->z))[jmi->offs_pre_real_w+46])
#define pre_der_2_J4_phi_195 ((*(jmi->z))[jmi->offs_pre_real_w+47])
#define pre_clutch1_mode_38 ((*(jmi->z))[jmi->offs_pre_integer_d+0])
#define pre_clutch2_mode_84 ((*(jmi->z))[jmi->offs_pre_integer_d+1])
#define pre_clutch3_mode_121 ((*(jmi->z))[jmi->offs_pre_integer_d+2])
#define pre_clutch1_free_28 ((*(jmi->z))[jmi->offs_pre_boolean_d+0])
#define pre_clutch1_startForward_30 ((*(jmi->z))[jmi->offs_pre_boolean_d+1])
#define pre_clutch1_startBackward_31 ((*(jmi->z))[jmi->offs_pre_boolean_d+2])
#define pre_clutch1_locked_32 ((*(jmi->z))[jmi->offs_pre_boolean_d+3])
#define pre_clutch2_free_74 ((*(jmi->z))[jmi->offs_pre_boolean_d+4])
#define pre_clutch2_startForward_76 ((*(jmi->z))[jmi->offs_pre_boolean_d+5])
#define pre_clutch2_startBackward_77 ((*(jmi->z))[jmi->offs_pre_boolean_d+6])
#define pre_clutch2_locked_78 ((*(jmi->z))[jmi->offs_pre_boolean_d+7])
#define pre_clutch3_free_111 ((*(jmi->z))[jmi->offs_pre_boolean_d+8])
#define pre_clutch3_startForward_113 ((*(jmi->z))[jmi->offs_pre_boolean_d+9])
#define pre_clutch3_startBackward_114 ((*(jmi->z))[jmi->offs_pre_boolean_d+10])
#define pre_clutch3_locked_115 ((*(jmi->z))[jmi->offs_pre_boolean_d+11])


const char *fmi_runtime_options_map_names[] = {
    "_block_jacobian_check",
    "_block_jacobian_check_tol",
    "_block_solver_experimental_mode",
    "_cs_rel_tol",
    "_cs_solver",
    "_cs_step_size",
    "_enforce_bounds",
    "_events_default_tol",
    "_events_tol_factor",
    "_iteration_variable_scaling",
    "_log_level",
    "_nle_solver_check_jac_cond",
    "_nle_solver_default_tol",
    "_nle_solver_max_iter",
    "_nle_solver_min_tol",
    "_nle_solver_regularization_tolerance",
    "_nle_solver_step_limit_factor",
    "_nle_solver_tol_factor",
    "_rescale_after_singular_jac",
    "_rescale_each_step",
    "_residual_equation_scaling",
    "_runtime_log_to_file",
    "_use_Brent_in_1d",
    "_use_jacobian_equilibration",
    NULL
};

const int fmi_runtime_options_map_vrefs[] = {
    536871014, 51, 268435541, 52, 268435542, 53, 536871015, 54, 55, 268435543,
    268435544, 536871016, 56, 268435545, 57, 58, 59, 60, 536871017, 536871018,
    268435546, 536871019, 536871020, 536871021, 0
};

const int fmi_runtime_options_map_length = 24;

#define _real_ci(i) ((*(jmi->z))[jmi->offs_real_ci+i])
#define _real_cd(i) ((*(jmi->z))[jmi->offs_real_cd+i])
#define _real_pi(i) ((*(jmi->z))[jmi->offs_real_pi+i])
#define _real_pd(i) ((*(jmi->z))[jmi->offs_real_pd+i])
#define _real_dx(i) ((*(jmi->z))[jmi->offs_real_dx+i])
#define _real_x(i) ((*(jmi->z))[jmi->offs_real_x+i])
#define _real_u(i) ((*(jmi->z))[jmi->offs_real_u+i])
#define _real_w(i) ((*(jmi->z))[jmi->offs_real_w+i])
#define _t ((*(jmi->z))[jmi->offs_t])

#define _real_d(i) ((*(jmi->z))[jmi->offs_real_d+i])
#define _integer_d(i) ((*(jmi->z))[jmi->offs_integer_d+i])
#define _integer_u(i) ((*(jmi->z))[jmi->offs_integer_u+i])
#define _boolean_d(i) ((*(jmi->z))[jmi->offs_boolean_d+i])
#define _boolean_u(i) ((*(jmi->z))[jmi->offs_boolean_u+i])

#define _pre_real_dx(i) ((*(jmi->z))[jmi->offs_pre_real_dx+i])
#define _pre_real_x(i) ((*(jmi->z))[jmi->offs_pre_real_x+i])
#define _pre_real_u(i) ((*(jmi->z))[jmi->offs_pre_real_u+i])
#define _pre_real_w(i) ((*(jmi->z))[jmi->offs_pre_real_w+i])

#define _pre_real_d(i) ((*(jmi->z))[jmi->offs_pre_real_d+i])
#define _pre_integer_d(i) ((*(jmi->z))[jmi->offs_pre_integer_d+i])
#define _pre_integer_u(i) ((*(jmi->z))[jmi->offs_pre_integer_u+i])
#define _pre_boolean_d(i) ((*(jmi->z))[jmi->offs_pre_boolean_d+i])
#define _pre_boolean_u(i) ((*(jmi->z))[jmi->offs_pre_boolean_u+i])

#define _sw(i) ((*(jmi->z))[jmi->offs_sw + i])
#define _sw_init(i) ((*(jmi->z))[jmi->offs_sw_init + i])
#define _pre_sw(i) ((*(jmi->z))[jmi->offs_pre_sw + i])
#define _pre_sw_init(i) ((*(jmi->z))[jmi->offs_pre_sw_init + i])
#define _guards(i) ((*(jmi->z))[jmi->offs_guards + i])
#define _guards_init(i) ((*(jmi->z))[jmi->offs_guards_init + i])
#define _pre_guards(i) ((*(jmi->z))[jmi->offs_pre_guards + i])
#define _pre_guards_init(i) ((*(jmi->z))[jmi->offs_pre_guards_init + i])

#define _atInitial (jmi->atInitial)



char* StateSelect_0_e[] = { "", "never", "avoid", "default", "prefer", "always" };


void func_Modelica_Math_tempInterpol1_def(jmi_ad_var_t u_v, jmi_array_t* table_a, jmi_ad_var_t icol_v, jmi_ad_var_t* y_o);
jmi_ad_var_t func_Modelica_Math_tempInterpol1_exp(jmi_ad_var_t u_v, jmi_array_t* table_a, jmi_ad_var_t icol_v);




static int dae_block_0(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    jmi_real_t** res = &residual;
    int ef = 0;
    JMI_ARRAY_STATIC(tmp_1, 2, 2)
    JMI_ARRAY_STATIC(tmp_2, 2, 2)
    JMI_ARRAY_STATIC(tmp_3, 2, 2)
    JMI_ARRAY_STATIC(tmp_4, 2, 2)
    JMI_ARRAY_STATIC(tmp_5, 2, 2)
    JMI_ARRAY_STATIC(tmp_6, 2, 2)
    JMI_ARRAY_STATIC(tmp_7, 2, 2)
    JMI_ARRAY_STATIC(tmp_8, 2, 2)
    JMI_ARRAY_STATIC(tmp_9, 2, 2)
    JMI_ARRAY_STATIC(tmp_10, 2, 2)
    JMI_ARRAY_STATIC(tmp_11, 2, 2)
    JMI_ARRAY_STATIC(tmp_12, 2, 2)
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 135;
        x[1] = 127;
        x[2] = 145;
        x[3] = 156;
    } else if (evaluation_mode == JMI_BLOCK_NON_REAL_VALUE_REFERENCE) {
        x[0] = 536871092;
        x[1] = 536871096;
        x[2] = 536871100;
        x[3] = 536871099;
        x[4] = 536871101;
        x[5] = 536871095;
        x[6] = 536871097;
        x[7] = 536871091;
        x[8] = 536871093;
    } else if (evaluation_mode == JMI_BLOCK_ACTIVE_SWITCH_INDEX) {
        x[0] = jmi->offs_sw + 5;
        x[1] = jmi->offs_sw + 6;
        x[2] = jmi->offs_sw + 18;
        x[3] = jmi->offs_sw + 19;
        x[4] = jmi->offs_sw + 29;
        x[5] = jmi->offs_sw + 30;
        x[6] = jmi->offs_sw + 25;
        x[7] = jmi->offs_sw + 26;
        x[8] = jmi->offs_sw + 14;
        x[9] = jmi->offs_sw + 15;
        x[10] = jmi->offs_sw + 1;
        x[11] = jmi->offs_sw + 2;
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
        (*res)[0] = 1;
        (*res)[1] = 1;
        (*res)[2] = 1;
        (*res)[3] = 1;
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _clutch1_sa_29;
        x[1] = _J1_a_7;
        x[2] = _clutch2_sa_75;
        x[3] = _clutch3_sa_112;
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        jmi_real_t Q1[92] = {0};
        jmi_real_t Q2[92] = {0};
        jmi_real_t* Q3 = residual;
        int i;
        char trans = 'N';
        double alpha = -1;
        double beta = 1;
        int n1 = 23;
        int n2 = 4;
        Q1[8] = - COND_EXP_EQ(_clutch1_locked_32, JMI_TRUE, AD_WRAP_LITERAL(0.0), COND_EXP_EQ(_clutch1_free_28, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch1_startForward_30, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch1_startBackward_31, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(1.0))))));
        Q1[22] = - COND_EXP_EQ(_clutch1_locked_32, JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(0.0));
        Q1[23] = 1.0;
        Q1[51] = - COND_EXP_EQ(_clutch2_locked_78, JMI_TRUE, AD_WRAP_LITERAL(0.0), COND_EXP_EQ(_clutch2_free_74, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch2_startForward_76, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch2_startBackward_77, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(1.0))))));
        Q1[65] = - COND_EXP_EQ(_clutch2_locked_78, JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(0.0));
        Q1[71] = - COND_EXP_EQ(_clutch3_locked_115, JMI_TRUE, AD_WRAP_LITERAL(0.0), COND_EXP_EQ(_clutch3_free_111, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch3_startForward_113, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch3_startBackward_114, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(1.0))))));
        Q1[85] = - COND_EXP_EQ(_clutch3_locked_115, JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(0.0));
        for (i = 0; i < 92; i += 23) {
            Q1[i + 0] = (Q1[i + 0]) / (- 1.0);
            Q1[i + 1] = (Q1[i + 1] - (1.0) * Q1[i + 0]) / (- 1.0);
            Q1[i + 2] = (Q1[i + 2]) / (1.0);
            Q1[i + 3] = (Q1[i + 3] - (1.0) * Q1[i + 2]) / (- 1.0);
            Q1[i + 4] = (Q1[i + 4] - (1.0) * Q1[i + 3]) / (- 1.0);
            Q1[i + 5] = (Q1[i + 5]) / (1.0);
            Q1[i + 6] = (Q1[i + 6] - (1.0) * Q1[i + 5]) / (- 1.0);
            Q1[i + 7] = (Q1[i + 7] - (1.0) * Q1[i + 6]) / (- 1.0);
            Q1[i + 8] = (Q1[i + 8]) / (1.0);
            Q1[i + 9] = (Q1[i + 9] - (1.0) * Q1[i + 8]) / (- 1.0);
            Q1[i + 10] = (Q1[i + 10] - (1.0) * Q1[i + 9]) / (- 1.0);
            Q1[i + 11] = (Q1[i + 11] - (1.0) * Q1[i + 1] - (1.0) * Q1[i + 10]) / (- 1.0);
            Q1[i + 12] = (Q1[i + 12] - (1.0) * Q1[i + 7] - (1.0) * Q1[i + 11]) / (- 1.0);
            Q1[i + 13] = (Q1[i + 13] - (1.0) * Q1[i + 4] - (1.0) * Q1[i + 12]) / (- 1.0);
            Q1[i + 14] = (Q1[i + 14] - (- 1.0) * Q1[i + 13]) / (1.0);
            Q1[i + 15] = (Q1[i + 15] - (- 1.0) * Q1[i + 14]) / (1.0);
            Q1[i + 16] = (Q1[i + 16]) / (1.0);
            Q1[i + 17] = (Q1[i + 17] - (- 1.0) * Q1[i + 12]) / (1.0);
            Q1[i + 18] = (Q1[i + 18] - (- 1.0) * Q1[i + 17]) / (1.0);
            Q1[i + 19] = (Q1[i + 19]) / (1.0);
            Q1[i + 20] = (Q1[i + 20] - (- 1.0) * Q1[i + 11]) / (1.0);
            Q1[i + 21] = (Q1[i + 21] - (- 1.0) * Q1[i + 20]) / (1.0);
            Q1[i + 22] = (Q1[i + 22]) / (1.0);
        }
        Q2[60] = _J4_J_127;
        Q2[64] = 1.0;
        Q2[65] = - 1.0;
        Q2[73] = _J3_J_89;
        Q2[77] = 1.0;
        Q2[78] = - 1.0;
        Q2[86] = _J2_J_52;
        Q2[90] = 1.0;
        Q2[91] = - 1.0;
        memset(Q3, 0, 16 * sizeof(jmi_real_t));
        Q3[7] = _J1_J_3;
        dgemm_(&trans, &trans, &n2, &n2, &n1, &alpha, Q2, &n2, Q1, &n1, &beta, Q3, &n2);
    } else if (evaluation_mode & JMI_BLOCK_EVALUATE || evaluation_mode & JMI_BLOCK_WRITE_BACK) {
        if ((evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) == 0) {
            _clutch1_sa_29 = x[0];
            _J1_a_7 = x[1];
            _clutch2_sa_75 = x[2];
            _clutch3_sa_112 = x[3];
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(5) = jmi_turn_switch(_clutch1_sa_29 - (- _clutch1_tau0_max_27), _sw(5), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(6) = jmi_turn_switch(_clutch1_sa_29 - (- _clutch1_tau0_26), _sw(6), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(7) = jmi_turn_switch(_clutch1_w_rel_20 - (- _clutch1_w_small_25), _sw(7), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(8) = jmi_turn_switch(_clutch1_w_rel_20 - (0), _sw(8), jmi->events_epsilon, JMI_REL_LT);
            }
            _clutch1_startBackward_31 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch1_mode_38, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(5), LOG_EXP_AND(pre_clutch1_startBackward_31, _sw(6)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch1_mode_38, 1, JMI_TRUE, JMI_FALSE), _sw(7))), LOG_EXP_AND(_atInitial, _sw(8)));
        }
        _der_J1_w_196 = jmi_divide_equation(jmi, (- _J1_a_7),(- 1.0),"(- J1.a) / (- 1.0)");
        _der_2_J1_phi_189 = jmi_divide_equation(jmi, (- _der_J1_w_196),(- 1.0),"(- J1.der(w)) / (- 1.0)");
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(18) = jmi_turn_switch(_clutch2_sa_75 - (- _clutch2_tau0_max_73), _sw(18), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(19) = jmi_turn_switch(_clutch2_sa_75 - (- _clutch2_tau0_72), _sw(19), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(20) = jmi_turn_switch(_clutch2_w_rel_66 - (- _clutch2_w_small_71), _sw(20), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(21) = jmi_turn_switch(_clutch2_w_rel_66 - (0), _sw(21), jmi->events_epsilon, JMI_REL_LT);
            }
            _clutch2_startBackward_77 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch2_mode_84, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(18), LOG_EXP_AND(pre_clutch2_startBackward_77, _sw(19)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch2_mode_84, 1, JMI_TRUE, JMI_FALSE), _sw(20))), LOG_EXP_AND(_atInitial, _sw(21)));
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(29) = jmi_turn_switch(_clutch3_sa_112 - (- _clutch3_tau0_max_110), _sw(29), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(30) = jmi_turn_switch(_clutch3_sa_112 - (- _clutch3_tau0_109), _sw(30), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(31) = jmi_turn_switch(_clutch3_w_rel_103 - (- _clutch3_w_small_108), _sw(31), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(32) = jmi_turn_switch(_clutch3_w_rel_103 - (0), _sw(32), jmi->events_epsilon, JMI_REL_LT);
            }
            _clutch3_startBackward_114 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch3_mode_121, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(29), LOG_EXP_AND(pre_clutch3_startBackward_114, _sw(30)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch3_mode_121, 1, JMI_TRUE, JMI_FALSE), _sw(31))), LOG_EXP_AND(_atInitial, _sw(32)));
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(25) = jmi_turn_switch(_clutch3_sa_112 - (_clutch3_tau0_max_110), _sw(25), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(26) = jmi_turn_switch(_clutch3_sa_112 - (_clutch3_tau0_109), _sw(26), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(27) = jmi_turn_switch(_clutch3_w_rel_103 - (_clutch3_w_small_108), _sw(27), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(28) = jmi_turn_switch(_clutch3_w_rel_103 - (0), _sw(28), jmi->events_epsilon, JMI_REL_GT);
            }
            _clutch3_startForward_113 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch3_mode_121, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(25), LOG_EXP_AND(pre_clutch3_startForward_113, _sw(26)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch3_mode_121, -1, JMI_TRUE, JMI_FALSE), _sw(27))), LOG_EXP_AND(_atInitial, _sw(28)));
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            _clutch3_locked_115 = LOG_EXP_AND(LOG_EXP_NOT(_clutch3_free_111), LOG_EXP_NOT(LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch3_mode_121, 1, JMI_TRUE, JMI_FALSE), _clutch3_startForward_113), COND_EXP_EQ(pre_clutch3_mode_121, -1, JMI_TRUE, JMI_FALSE)), _clutch3_startBackward_114)));
        }
        _clutch3_a_rel_104 = COND_EXP_EQ(_clutch3_locked_115, JMI_TRUE, AD_WRAP_LITERAL(0), COND_EXP_EQ(_clutch3_free_111, JMI_TRUE, _clutch3_sa_112, COND_EXP_EQ(_clutch3_startForward_113, JMI_TRUE, _clutch3_sa_112 - _clutch3_tau0_max_110, COND_EXP_EQ(_clutch3_startBackward_114, JMI_TRUE, _clutch3_sa_112 + _clutch3_tau0_max_110, COND_EXP_EQ(COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, _clutch3_sa_112 - _clutch3_tau0_max_110, _clutch3_sa_112 + _clutch3_tau0_max_110)))));
        _der_clutch3_w_rel_203 = jmi_divide_equation(jmi, (- _clutch3_a_rel_104),(- 1.0),"(- clutch3.a_rel) / (- 1.0)");
        _der_2_clutch3_phi_rel_194 = jmi_divide_equation(jmi, (- _der_clutch3_w_rel_203),(- 1.0),"(- clutch3.der(w_rel)) / (- 1.0)");
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(14) = jmi_turn_switch(_clutch2_sa_75 - (_clutch2_tau0_max_73), _sw(14), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(15) = jmi_turn_switch(_clutch2_sa_75 - (_clutch2_tau0_72), _sw(15), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(16) = jmi_turn_switch(_clutch2_w_rel_66 - (_clutch2_w_small_71), _sw(16), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(17) = jmi_turn_switch(_clutch2_w_rel_66 - (0), _sw(17), jmi->events_epsilon, JMI_REL_GT);
            }
            _clutch2_startForward_76 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch2_mode_84, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(14), LOG_EXP_AND(pre_clutch2_startForward_76, _sw(15)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch2_mode_84, -1, JMI_TRUE, JMI_FALSE), _sw(16))), LOG_EXP_AND(_atInitial, _sw(17)));
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            _clutch2_locked_78 = LOG_EXP_AND(LOG_EXP_NOT(_clutch2_free_74), LOG_EXP_NOT(LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch2_mode_84, 1, JMI_TRUE, JMI_FALSE), _clutch2_startForward_76), COND_EXP_EQ(pre_clutch2_mode_84, -1, JMI_TRUE, JMI_FALSE)), _clutch2_startBackward_77)));
        }
        _clutch2_a_rel_67 = COND_EXP_EQ(_clutch2_locked_78, JMI_TRUE, AD_WRAP_LITERAL(0), COND_EXP_EQ(_clutch2_free_74, JMI_TRUE, _clutch2_sa_75, COND_EXP_EQ(_clutch2_startForward_76, JMI_TRUE, _clutch2_sa_75 - _clutch2_tau0_max_73, COND_EXP_EQ(_clutch2_startBackward_77, JMI_TRUE, _clutch2_sa_75 + _clutch2_tau0_max_73, COND_EXP_EQ(COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, _clutch2_sa_75 - _clutch2_tau0_max_73, _clutch2_sa_75 + _clutch2_tau0_max_73)))));
        _der_clutch2_w_rel_201 = jmi_divide_equation(jmi, (- _clutch2_a_rel_67),(- 1.0),"(- clutch2.a_rel) / (- 1.0)");
        _der_2_clutch2_phi_rel_192 = jmi_divide_equation(jmi, (- _der_clutch2_w_rel_201),(- 1.0),"(- clutch2.der(w_rel)) / (- 1.0)");
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(1) = jmi_turn_switch(_clutch1_sa_29 - (_clutch1_tau0_max_27), _sw(1), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(2) = jmi_turn_switch(_clutch1_sa_29 - (_clutch1_tau0_26), _sw(2), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(3) = jmi_turn_switch(_clutch1_w_rel_20 - (_clutch1_w_small_25), _sw(3), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(4) = jmi_turn_switch(_clutch1_w_rel_20 - (0), _sw(4), jmi->events_epsilon, JMI_REL_GT);
            }
            _clutch1_startForward_30 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch1_mode_38, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(1), LOG_EXP_AND(pre_clutch1_startForward_30, _sw(2)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch1_mode_38, -1, JMI_TRUE, JMI_FALSE), _sw(3))), LOG_EXP_AND(_atInitial, _sw(4)));
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            _clutch1_locked_32 = LOG_EXP_AND(LOG_EXP_NOT(_clutch1_free_28), LOG_EXP_NOT(LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch1_mode_38, 1, JMI_TRUE, JMI_FALSE), _clutch1_startForward_30), COND_EXP_EQ(pre_clutch1_mode_38, -1, JMI_TRUE, JMI_FALSE)), _clutch1_startBackward_31)));
        }
        _clutch1_a_rel_21 = COND_EXP_EQ(_clutch1_locked_32, JMI_TRUE, AD_WRAP_LITERAL(0), COND_EXP_EQ(_clutch1_free_28, JMI_TRUE, _clutch1_sa_29, COND_EXP_EQ(_clutch1_startForward_30, JMI_TRUE, _clutch1_sa_29 - _clutch1_tau0_max_27, COND_EXP_EQ(_clutch1_startBackward_31, JMI_TRUE, _clutch1_sa_29 + _clutch1_tau0_max_27, COND_EXP_EQ(COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, _clutch1_sa_29 - _clutch1_tau0_max_27, _clutch1_sa_29 + _clutch1_tau0_max_27)))));
        _der_clutch1_w_rel_198 = jmi_divide_equation(jmi, (- _clutch1_a_rel_21),(- 1.0),"(- clutch1.a_rel) / (- 1.0)");
        _der_2_clutch1_phi_rel_190 = jmi_divide_equation(jmi, (- _der_clutch1_w_rel_198),(- 1.0),"(- clutch1.der(w_rel)) / (- 1.0)");
        _der_2_J2_phi_191 = jmi_divide_equation(jmi, (- _der_2_clutch1_phi_rel_190 + (- _der_2_J1_phi_189)),(- 1.0),"(- clutch1._der_der_phi_rel + (- J1._der_der_phi)) / (- 1.0)");
        _der_2_J3_phi_193 = jmi_divide_equation(jmi, (- _der_2_clutch2_phi_rel_192 + (- _der_2_J2_phi_191)),(- 1.0),"(- clutch2._der_der_phi_rel + (- J2._der_der_phi)) / (- 1.0)");
        _der_2_J4_phi_195 = jmi_divide_equation(jmi, (- _der_2_clutch3_phi_rel_194 + (- _der_2_J3_phi_193)),(- 1.0),"(- clutch3._der_der_phi_rel + (- J3._der_der_phi)) / (- 1.0)");
        _der_J4_w_164 = _der_2_J4_phi_195;
        _J4_a_131 = _der_J4_w_164;
        JMI_ARRAY_STATIC_INIT_2(tmp_1, 1, 2)
        jmi_array_ref_2(tmp_1, 1,1) = _clutch3_mue_pos_1_1_94;
        jmi_array_ref_2(tmp_1, 1,2) = _clutch3_mue_pos_1_2_95;
        JMI_ARRAY_STATIC_INIT_2(tmp_2, 1, 2)
        jmi_array_ref_2(tmp_2, 1,1) = _clutch3_mue_pos_1_1_94;
        jmi_array_ref_2(tmp_2, 1,2) = _clutch3_mue_pos_1_2_95;
        JMI_ARRAY_STATIC_INIT_2(tmp_3, 1, 2)
        jmi_array_ref_2(tmp_3, 1,1) = _clutch3_mue_pos_1_1_94;
        jmi_array_ref_2(tmp_3, 1,2) = _clutch3_mue_pos_1_2_95;
        JMI_ARRAY_STATIC_INIT_2(tmp_4, 1, 2)
        jmi_array_ref_2(tmp_4, 1,1) = _clutch3_mue_pos_1_1_94;
        jmi_array_ref_2(tmp_4, 1,2) = _clutch3_mue_pos_1_2_95;
        _clutch3_tau_105 = COND_EXP_EQ(_clutch3_locked_115, JMI_TRUE, _clutch3_sa_112, COND_EXP_EQ(_clutch3_free_111, JMI_TRUE, AD_WRAP_LITERAL(0), _clutch3_cgeo_97 * _clutch3_fn_100 * COND_EXP_EQ(_clutch3_startForward_113, JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch3_w_rel_103, tmp_1, AD_WRAP_LITERAL(2)), COND_EXP_EQ(_clutch3_startBackward_114, JMI_TRUE, - func_Modelica_Math_tempInterpol1_exp(- _clutch3_w_rel_103, tmp_2, AD_WRAP_LITERAL(2)), COND_EXP_EQ(COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch3_w_rel_103, tmp_3, AD_WRAP_LITERAL(2)), - func_Modelica_Math_tempInterpol1_exp(- _clutch3_w_rel_103, tmp_4, AD_WRAP_LITERAL(2)))))));
        _der_J3_w_162 = _der_2_J3_phi_193;
        _J3_a_93 = _der_J3_w_162;
        JMI_ARRAY_STATIC_INIT_2(tmp_5, 1, 2)
        jmi_array_ref_2(tmp_5, 1,1) = _clutch2_mue_pos_1_1_57;
        jmi_array_ref_2(tmp_5, 1,2) = _clutch2_mue_pos_1_2_58;
        JMI_ARRAY_STATIC_INIT_2(tmp_6, 1, 2)
        jmi_array_ref_2(tmp_6, 1,1) = _clutch2_mue_pos_1_1_57;
        jmi_array_ref_2(tmp_6, 1,2) = _clutch2_mue_pos_1_2_58;
        JMI_ARRAY_STATIC_INIT_2(tmp_7, 1, 2)
        jmi_array_ref_2(tmp_7, 1,1) = _clutch2_mue_pos_1_1_57;
        jmi_array_ref_2(tmp_7, 1,2) = _clutch2_mue_pos_1_2_58;
        JMI_ARRAY_STATIC_INIT_2(tmp_8, 1, 2)
        jmi_array_ref_2(tmp_8, 1,1) = _clutch2_mue_pos_1_1_57;
        jmi_array_ref_2(tmp_8, 1,2) = _clutch2_mue_pos_1_2_58;
        _clutch2_tau_68 = COND_EXP_EQ(_clutch2_locked_78, JMI_TRUE, _clutch2_sa_75, COND_EXP_EQ(_clutch2_free_74, JMI_TRUE, AD_WRAP_LITERAL(0), _clutch2_cgeo_60 * _clutch2_fn_63 * COND_EXP_EQ(_clutch2_startForward_76, JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch2_w_rel_66, tmp_5, AD_WRAP_LITERAL(2)), COND_EXP_EQ(_clutch2_startBackward_77, JMI_TRUE, - func_Modelica_Math_tempInterpol1_exp(- _clutch2_w_rel_66, tmp_6, AD_WRAP_LITERAL(2)), COND_EXP_EQ(COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch2_w_rel_66, tmp_7, AD_WRAP_LITERAL(2)), - func_Modelica_Math_tempInterpol1_exp(- _clutch2_w_rel_66, tmp_8, AD_WRAP_LITERAL(2)))))));
        _der_J2_w_160 = _der_2_J2_phi_191;
        _J2_a_56 = _der_J2_w_160;
        JMI_ARRAY_STATIC_INIT_2(tmp_9, 1, 2)
        jmi_array_ref_2(tmp_9, 1,1) = _clutch1_mue_pos_1_1_11;
        jmi_array_ref_2(tmp_9, 1,2) = _clutch1_mue_pos_1_2_12;
        JMI_ARRAY_STATIC_INIT_2(tmp_10, 1, 2)
        jmi_array_ref_2(tmp_10, 1,1) = _clutch1_mue_pos_1_1_11;
        jmi_array_ref_2(tmp_10, 1,2) = _clutch1_mue_pos_1_2_12;
        JMI_ARRAY_STATIC_INIT_2(tmp_11, 1, 2)
        jmi_array_ref_2(tmp_11, 1,1) = _clutch1_mue_pos_1_1_11;
        jmi_array_ref_2(tmp_11, 1,2) = _clutch1_mue_pos_1_2_12;
        JMI_ARRAY_STATIC_INIT_2(tmp_12, 1, 2)
        jmi_array_ref_2(tmp_12, 1,1) = _clutch1_mue_pos_1_1_11;
        jmi_array_ref_2(tmp_12, 1,2) = _clutch1_mue_pos_1_2_12;
        _clutch1_tau_22 = COND_EXP_EQ(_clutch1_locked_32, JMI_TRUE, _clutch1_sa_29, COND_EXP_EQ(_clutch1_free_28, JMI_TRUE, AD_WRAP_LITERAL(0), _clutch1_cgeo_14 * _clutch1_fn_17 * COND_EXP_EQ(_clutch1_startForward_30, JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch1_w_rel_20, tmp_9, AD_WRAP_LITERAL(2)), COND_EXP_EQ(_clutch1_startBackward_31, JMI_TRUE, - func_Modelica_Math_tempInterpol1_exp(- _clutch1_w_rel_20, tmp_10, AD_WRAP_LITERAL(2)), COND_EXP_EQ(COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch1_w_rel_20, tmp_11, AD_WRAP_LITERAL(2)), - func_Modelica_Math_tempInterpol1_exp(- _clutch1_w_rel_20, tmp_12, AD_WRAP_LITERAL(2)))))));
        if (evaluation_mode & JMI_BLOCK_EVALUATE) {
            (*res)[0] = - _clutch3_tau_105 - (_J4_J_127 * _J4_a_131);
            (*res)[1] = - _clutch2_tau_68 + _clutch3_tau_105 - (_J3_J_89 * _J3_a_93);
            (*res)[2] = - _clutch1_tau_22 + _clutch2_tau_68 - (_J2_J_52 * _J2_a_56);
            (*res)[3] = _torque_tau_8 + _clutch1_tau_22 - (_J1_J_3 * _J1_a_7);
        }
    }
    return ef;
}



static int dae_init_block_0(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    jmi_real_t** res = &residual;
    int ef = 0;
    JMI_ARRAY_STATIC(tmp_1, 2, 2)
    JMI_ARRAY_STATIC(tmp_2, 2, 2)
    JMI_ARRAY_STATIC(tmp_3, 2, 2)
    JMI_ARRAY_STATIC(tmp_4, 2, 2)
    JMI_ARRAY_STATIC(tmp_5, 2, 2)
    JMI_ARRAY_STATIC(tmp_6, 2, 2)
    JMI_ARRAY_STATIC(tmp_7, 2, 2)
    JMI_ARRAY_STATIC(tmp_8, 2, 2)
    JMI_ARRAY_STATIC(tmp_9, 2, 2)
    JMI_ARRAY_STATIC(tmp_10, 2, 2)
    JMI_ARRAY_STATIC(tmp_11, 2, 2)
    JMI_ARRAY_STATIC(tmp_12, 2, 2)
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 135;
        x[1] = 127;
        x[2] = 145;
        x[3] = 156;
    } else if (evaluation_mode == JMI_BLOCK_NON_REAL_VALUE_REFERENCE) {
        x[0] = 536871092;
        x[1] = 536871096;
        x[2] = 536871100;
        x[3] = 536871099;
        x[4] = 536871101;
        x[5] = 536871095;
        x[6] = 536871097;
        x[7] = 536871091;
        x[8] = 536871093;
    } else if (evaluation_mode == JMI_BLOCK_ACTIVE_SWITCH_INDEX) {
        x[0] = jmi->offs_sw + 5;
        x[1] = jmi->offs_sw + 6;
        x[2] = jmi->offs_sw + 18;
        x[3] = jmi->offs_sw + 19;
        x[4] = jmi->offs_sw + 29;
        x[5] = jmi->offs_sw + 30;
        x[6] = jmi->offs_sw + 25;
        x[7] = jmi->offs_sw + 26;
        x[8] = jmi->offs_sw + 14;
        x[9] = jmi->offs_sw + 15;
        x[10] = jmi->offs_sw + 1;
        x[11] = jmi->offs_sw + 2;
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
        (*res)[0] = 1;
        (*res)[1] = 1;
        (*res)[2] = 1;
        (*res)[3] = 1;
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _clutch1_sa_29;
        x[1] = _J1_a_7;
        x[2] = _clutch2_sa_75;
        x[3] = _clutch3_sa_112;
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        jmi_real_t Q1[92] = {0};
        jmi_real_t Q2[92] = {0};
        jmi_real_t* Q3 = residual;
        int i;
        char trans = 'N';
        double alpha = -1;
        double beta = 1;
        int n1 = 23;
        int n2 = 4;
        Q1[8] = - COND_EXP_EQ(_clutch1_locked_32, JMI_TRUE, AD_WRAP_LITERAL(0.0), COND_EXP_EQ(_clutch1_free_28, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch1_startForward_30, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch1_startBackward_31, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(1.0))))));
        Q1[22] = - COND_EXP_EQ(_clutch1_locked_32, JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(0.0));
        Q1[23] = 1.0;
        Q1[51] = - COND_EXP_EQ(_clutch2_locked_78, JMI_TRUE, AD_WRAP_LITERAL(0.0), COND_EXP_EQ(_clutch2_free_74, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch2_startForward_76, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch2_startBackward_77, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(1.0))))));
        Q1[65] = - COND_EXP_EQ(_clutch2_locked_78, JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(0.0));
        Q1[71] = - COND_EXP_EQ(_clutch3_locked_115, JMI_TRUE, AD_WRAP_LITERAL(0.0), COND_EXP_EQ(_clutch3_free_111, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch3_startForward_113, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(_clutch3_startBackward_114, JMI_TRUE, AD_WRAP_LITERAL(1.0), COND_EXP_EQ(COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(1.0))))));
        Q1[85] = - COND_EXP_EQ(_clutch3_locked_115, JMI_TRUE, AD_WRAP_LITERAL(1.0), AD_WRAP_LITERAL(0.0));
        for (i = 0; i < 92; i += 23) {
            Q1[i + 0] = (Q1[i + 0]) / (- 1.0);
            Q1[i + 1] = (Q1[i + 1] - (1.0) * Q1[i + 0]) / (- 1.0);
            Q1[i + 2] = (Q1[i + 2]) / (1.0);
            Q1[i + 3] = (Q1[i + 3] - (1.0) * Q1[i + 2]) / (- 1.0);
            Q1[i + 4] = (Q1[i + 4] - (1.0) * Q1[i + 3]) / (- 1.0);
            Q1[i + 5] = (Q1[i + 5]) / (1.0);
            Q1[i + 6] = (Q1[i + 6] - (1.0) * Q1[i + 5]) / (- 1.0);
            Q1[i + 7] = (Q1[i + 7] - (1.0) * Q1[i + 6]) / (- 1.0);
            Q1[i + 8] = (Q1[i + 8]) / (1.0);
            Q1[i + 9] = (Q1[i + 9] - (1.0) * Q1[i + 8]) / (- 1.0);
            Q1[i + 10] = (Q1[i + 10] - (1.0) * Q1[i + 9]) / (- 1.0);
            Q1[i + 11] = (Q1[i + 11] - (1.0) * Q1[i + 1] - (1.0) * Q1[i + 10]) / (- 1.0);
            Q1[i + 12] = (Q1[i + 12] - (1.0) * Q1[i + 7] - (1.0) * Q1[i + 11]) / (- 1.0);
            Q1[i + 13] = (Q1[i + 13] - (1.0) * Q1[i + 4] - (1.0) * Q1[i + 12]) / (- 1.0);
            Q1[i + 14] = (Q1[i + 14] - (- 1.0) * Q1[i + 13]) / (1.0);
            Q1[i + 15] = (Q1[i + 15] - (- 1.0) * Q1[i + 14]) / (1.0);
            Q1[i + 16] = (Q1[i + 16]) / (1.0);
            Q1[i + 17] = (Q1[i + 17] - (- 1.0) * Q1[i + 12]) / (1.0);
            Q1[i + 18] = (Q1[i + 18] - (- 1.0) * Q1[i + 17]) / (1.0);
            Q1[i + 19] = (Q1[i + 19]) / (1.0);
            Q1[i + 20] = (Q1[i + 20] - (- 1.0) * Q1[i + 11]) / (1.0);
            Q1[i + 21] = (Q1[i + 21] - (- 1.0) * Q1[i + 20]) / (1.0);
            Q1[i + 22] = (Q1[i + 22]) / (1.0);
        }
        Q2[60] = _J4_J_127;
        Q2[64] = 1.0;
        Q2[65] = - 1.0;
        Q2[73] = _J3_J_89;
        Q2[77] = 1.0;
        Q2[78] = - 1.0;
        Q2[86] = _J2_J_52;
        Q2[90] = 1.0;
        Q2[91] = - 1.0;
        memset(Q3, 0, 16 * sizeof(jmi_real_t));
        Q3[7] = _J1_J_3;
        dgemm_(&trans, &trans, &n2, &n2, &n1, &alpha, Q2, &n2, Q1, &n1, &beta, Q3, &n2);
    } else if (evaluation_mode & JMI_BLOCK_EVALUATE || evaluation_mode & JMI_BLOCK_WRITE_BACK) {
        if ((evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) == 0) {
            _clutch1_sa_29 = x[0];
            _J1_a_7 = x[1];
            _clutch2_sa_75 = x[2];
            _clutch3_sa_112 = x[3];
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(5) = jmi_turn_switch(_clutch1_sa_29 - (- _clutch1_tau0_max_27), _sw(5), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(6) = jmi_turn_switch(_clutch1_sa_29 - (- _clutch1_tau0_26), _sw(6), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(7) = jmi_turn_switch(_clutch1_w_rel_20 - (- _clutch1_w_small_25), _sw(7), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(8) = jmi_turn_switch(_clutch1_w_rel_20 - (0), _sw(8), jmi->events_epsilon, JMI_REL_LT);
            }
            _clutch1_startBackward_31 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch1_mode_38, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(5), LOG_EXP_AND(pre_clutch1_startBackward_31, _sw(6)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch1_mode_38, 1, JMI_TRUE, JMI_FALSE), _sw(7))), LOG_EXP_AND(_atInitial, _sw(8)));
        }
        _der_J1_w_196 = jmi_divide_equation(jmi, (- _J1_a_7),(- 1.0),"(- J1.a) / (- 1.0)");
        _der_2_J1_phi_189 = jmi_divide_equation(jmi, (- _der_J1_w_196),(- 1.0),"(- J1.der(w)) / (- 1.0)");
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(18) = jmi_turn_switch(_clutch2_sa_75 - (- _clutch2_tau0_max_73), _sw(18), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(19) = jmi_turn_switch(_clutch2_sa_75 - (- _clutch2_tau0_72), _sw(19), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(20) = jmi_turn_switch(_clutch2_w_rel_66 - (- _clutch2_w_small_71), _sw(20), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(21) = jmi_turn_switch(_clutch2_w_rel_66 - (0), _sw(21), jmi->events_epsilon, JMI_REL_LT);
            }
            _clutch2_startBackward_77 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch2_mode_84, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(18), LOG_EXP_AND(pre_clutch2_startBackward_77, _sw(19)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch2_mode_84, 1, JMI_TRUE, JMI_FALSE), _sw(20))), LOG_EXP_AND(_atInitial, _sw(21)));
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(29) = jmi_turn_switch(_clutch3_sa_112 - (- _clutch3_tau0_max_110), _sw(29), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(30) = jmi_turn_switch(_clutch3_sa_112 - (- _clutch3_tau0_109), _sw(30), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(31) = jmi_turn_switch(_clutch3_w_rel_103 - (- _clutch3_w_small_108), _sw(31), jmi->events_epsilon, JMI_REL_LT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(32) = jmi_turn_switch(_clutch3_w_rel_103 - (0), _sw(32), jmi->events_epsilon, JMI_REL_LT);
            }
            _clutch3_startBackward_114 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch3_mode_121, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(29), LOG_EXP_AND(pre_clutch3_startBackward_114, _sw(30)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch3_mode_121, 1, JMI_TRUE, JMI_FALSE), _sw(31))), LOG_EXP_AND(_atInitial, _sw(32)));
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(25) = jmi_turn_switch(_clutch3_sa_112 - (_clutch3_tau0_max_110), _sw(25), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(26) = jmi_turn_switch(_clutch3_sa_112 - (_clutch3_tau0_109), _sw(26), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(27) = jmi_turn_switch(_clutch3_w_rel_103 - (_clutch3_w_small_108), _sw(27), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(28) = jmi_turn_switch(_clutch3_w_rel_103 - (0), _sw(28), jmi->events_epsilon, JMI_REL_GT);
            }
            _clutch3_startForward_113 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch3_mode_121, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(25), LOG_EXP_AND(pre_clutch3_startForward_113, _sw(26)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch3_mode_121, -1, JMI_TRUE, JMI_FALSE), _sw(27))), LOG_EXP_AND(_atInitial, _sw(28)));
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            _clutch3_locked_115 = LOG_EXP_AND(LOG_EXP_NOT(_clutch3_free_111), LOG_EXP_NOT(LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch3_mode_121, 1, JMI_TRUE, JMI_FALSE), _clutch3_startForward_113), COND_EXP_EQ(pre_clutch3_mode_121, -1, JMI_TRUE, JMI_FALSE)), _clutch3_startBackward_114)));
        }
        _clutch3_a_rel_104 = COND_EXP_EQ(_clutch3_locked_115, JMI_TRUE, AD_WRAP_LITERAL(0), COND_EXP_EQ(_clutch3_free_111, JMI_TRUE, _clutch3_sa_112, COND_EXP_EQ(_clutch3_startForward_113, JMI_TRUE, _clutch3_sa_112 - _clutch3_tau0_max_110, COND_EXP_EQ(_clutch3_startBackward_114, JMI_TRUE, _clutch3_sa_112 + _clutch3_tau0_max_110, COND_EXP_EQ(COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, _clutch3_sa_112 - _clutch3_tau0_max_110, _clutch3_sa_112 + _clutch3_tau0_max_110)))));
        _der_clutch3_w_rel_203 = jmi_divide_equation(jmi, (- _clutch3_a_rel_104),(- 1.0),"(- clutch3.a_rel) / (- 1.0)");
        _der_2_clutch3_phi_rel_194 = jmi_divide_equation(jmi, (- _der_clutch3_w_rel_203),(- 1.0),"(- clutch3.der(w_rel)) / (- 1.0)");
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(14) = jmi_turn_switch(_clutch2_sa_75 - (_clutch2_tau0_max_73), _sw(14), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(15) = jmi_turn_switch(_clutch2_sa_75 - (_clutch2_tau0_72), _sw(15), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(16) = jmi_turn_switch(_clutch2_w_rel_66 - (_clutch2_w_small_71), _sw(16), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(17) = jmi_turn_switch(_clutch2_w_rel_66 - (0), _sw(17), jmi->events_epsilon, JMI_REL_GT);
            }
            _clutch2_startForward_76 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch2_mode_84, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(14), LOG_EXP_AND(pre_clutch2_startForward_76, _sw(15)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch2_mode_84, -1, JMI_TRUE, JMI_FALSE), _sw(16))), LOG_EXP_AND(_atInitial, _sw(17)));
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            _clutch2_locked_78 = LOG_EXP_AND(LOG_EXP_NOT(_clutch2_free_74), LOG_EXP_NOT(LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch2_mode_84, 1, JMI_TRUE, JMI_FALSE), _clutch2_startForward_76), COND_EXP_EQ(pre_clutch2_mode_84, -1, JMI_TRUE, JMI_FALSE)), _clutch2_startBackward_77)));
        }
        _clutch2_a_rel_67 = COND_EXP_EQ(_clutch2_locked_78, JMI_TRUE, AD_WRAP_LITERAL(0), COND_EXP_EQ(_clutch2_free_74, JMI_TRUE, _clutch2_sa_75, COND_EXP_EQ(_clutch2_startForward_76, JMI_TRUE, _clutch2_sa_75 - _clutch2_tau0_max_73, COND_EXP_EQ(_clutch2_startBackward_77, JMI_TRUE, _clutch2_sa_75 + _clutch2_tau0_max_73, COND_EXP_EQ(COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, _clutch2_sa_75 - _clutch2_tau0_max_73, _clutch2_sa_75 + _clutch2_tau0_max_73)))));
        _der_clutch2_w_rel_201 = jmi_divide_equation(jmi, (- _clutch2_a_rel_67),(- 1.0),"(- clutch2.a_rel) / (- 1.0)");
        _der_2_clutch2_phi_rel_192 = jmi_divide_equation(jmi, (- _der_clutch2_w_rel_201),(- 1.0),"(- clutch2.der(w_rel)) / (- 1.0)");
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(1) = jmi_turn_switch(_clutch1_sa_29 - (_clutch1_tau0_max_27), _sw(1), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(2) = jmi_turn_switch(_clutch1_sa_29 - (_clutch1_tau0_26), _sw(2), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(3) = jmi_turn_switch(_clutch1_w_rel_20 - (_clutch1_w_small_25), _sw(3), jmi->events_epsilon, JMI_REL_GT);
            }
            if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
                _sw(4) = jmi_turn_switch(_clutch1_w_rel_20 - (0), _sw(4), jmi->events_epsilon, JMI_REL_GT);
            }
            _clutch1_startForward_30 = LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_AND(COND_EXP_EQ(pre_clutch1_mode_38, 0, JMI_TRUE, JMI_FALSE), LOG_EXP_OR(_sw(1), LOG_EXP_AND(pre_clutch1_startForward_30, _sw(2)))), LOG_EXP_AND(COND_EXP_EQ(pre_clutch1_mode_38, -1, JMI_TRUE, JMI_FALSE), _sw(3))), LOG_EXP_AND(_atInitial, _sw(4)));
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) {
            _clutch1_locked_32 = LOG_EXP_AND(LOG_EXP_NOT(_clutch1_free_28), LOG_EXP_NOT(LOG_EXP_OR(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch1_mode_38, 1, JMI_TRUE, JMI_FALSE), _clutch1_startForward_30), COND_EXP_EQ(pre_clutch1_mode_38, -1, JMI_TRUE, JMI_FALSE)), _clutch1_startBackward_31)));
        }
        _clutch1_a_rel_21 = COND_EXP_EQ(_clutch1_locked_32, JMI_TRUE, AD_WRAP_LITERAL(0), COND_EXP_EQ(_clutch1_free_28, JMI_TRUE, _clutch1_sa_29, COND_EXP_EQ(_clutch1_startForward_30, JMI_TRUE, _clutch1_sa_29 - _clutch1_tau0_max_27, COND_EXP_EQ(_clutch1_startBackward_31, JMI_TRUE, _clutch1_sa_29 + _clutch1_tau0_max_27, COND_EXP_EQ(COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, _clutch1_sa_29 - _clutch1_tau0_max_27, _clutch1_sa_29 + _clutch1_tau0_max_27)))));
        _der_clutch1_w_rel_198 = jmi_divide_equation(jmi, (- _clutch1_a_rel_21),(- 1.0),"(- clutch1.a_rel) / (- 1.0)");
        _der_2_clutch1_phi_rel_190 = jmi_divide_equation(jmi, (- _der_clutch1_w_rel_198),(- 1.0),"(- clutch1.der(w_rel)) / (- 1.0)");
        _der_2_J2_phi_191 = jmi_divide_equation(jmi, (- _der_2_clutch1_phi_rel_190 + (- _der_2_J1_phi_189)),(- 1.0),"(- clutch1._der_der_phi_rel + (- J1._der_der_phi)) / (- 1.0)");
        _der_2_J3_phi_193 = jmi_divide_equation(jmi, (- _der_2_clutch2_phi_rel_192 + (- _der_2_J2_phi_191)),(- 1.0),"(- clutch2._der_der_phi_rel + (- J2._der_der_phi)) / (- 1.0)");
        _der_2_J4_phi_195 = jmi_divide_equation(jmi, (- _der_2_clutch3_phi_rel_194 + (- _der_2_J3_phi_193)),(- 1.0),"(- clutch3._der_der_phi_rel + (- J3._der_der_phi)) / (- 1.0)");
        _der_J4_w_164 = _der_2_J4_phi_195;
        _J4_a_131 = _der_J4_w_164;
        JMI_ARRAY_STATIC_INIT_2(tmp_1, 1, 2)
        jmi_array_ref_2(tmp_1, 1,1) = _clutch3_mue_pos_1_1_94;
        jmi_array_ref_2(tmp_1, 1,2) = _clutch3_mue_pos_1_2_95;
        JMI_ARRAY_STATIC_INIT_2(tmp_2, 1, 2)
        jmi_array_ref_2(tmp_2, 1,1) = _clutch3_mue_pos_1_1_94;
        jmi_array_ref_2(tmp_2, 1,2) = _clutch3_mue_pos_1_2_95;
        JMI_ARRAY_STATIC_INIT_2(tmp_3, 1, 2)
        jmi_array_ref_2(tmp_3, 1,1) = _clutch3_mue_pos_1_1_94;
        jmi_array_ref_2(tmp_3, 1,2) = _clutch3_mue_pos_1_2_95;
        JMI_ARRAY_STATIC_INIT_2(tmp_4, 1, 2)
        jmi_array_ref_2(tmp_4, 1,1) = _clutch3_mue_pos_1_1_94;
        jmi_array_ref_2(tmp_4, 1,2) = _clutch3_mue_pos_1_2_95;
        _clutch3_tau_105 = COND_EXP_EQ(_clutch3_locked_115, JMI_TRUE, _clutch3_sa_112, COND_EXP_EQ(_clutch3_free_111, JMI_TRUE, AD_WRAP_LITERAL(0), _clutch3_cgeo_97 * _clutch3_fn_100 * COND_EXP_EQ(_clutch3_startForward_113, JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch3_w_rel_103, tmp_1, AD_WRAP_LITERAL(2)), COND_EXP_EQ(_clutch3_startBackward_114, JMI_TRUE, - func_Modelica_Math_tempInterpol1_exp(- _clutch3_w_rel_103, tmp_2, AD_WRAP_LITERAL(2)), COND_EXP_EQ(COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch3_w_rel_103, tmp_3, AD_WRAP_LITERAL(2)), - func_Modelica_Math_tempInterpol1_exp(- _clutch3_w_rel_103, tmp_4, AD_WRAP_LITERAL(2)))))));
        _der_J3_w_162 = _der_2_J3_phi_193;
        _J3_a_93 = _der_J3_w_162;
        JMI_ARRAY_STATIC_INIT_2(tmp_5, 1, 2)
        jmi_array_ref_2(tmp_5, 1,1) = _clutch2_mue_pos_1_1_57;
        jmi_array_ref_2(tmp_5, 1,2) = _clutch2_mue_pos_1_2_58;
        JMI_ARRAY_STATIC_INIT_2(tmp_6, 1, 2)
        jmi_array_ref_2(tmp_6, 1,1) = _clutch2_mue_pos_1_1_57;
        jmi_array_ref_2(tmp_6, 1,2) = _clutch2_mue_pos_1_2_58;
        JMI_ARRAY_STATIC_INIT_2(tmp_7, 1, 2)
        jmi_array_ref_2(tmp_7, 1,1) = _clutch2_mue_pos_1_1_57;
        jmi_array_ref_2(tmp_7, 1,2) = _clutch2_mue_pos_1_2_58;
        JMI_ARRAY_STATIC_INIT_2(tmp_8, 1, 2)
        jmi_array_ref_2(tmp_8, 1,1) = _clutch2_mue_pos_1_1_57;
        jmi_array_ref_2(tmp_8, 1,2) = _clutch2_mue_pos_1_2_58;
        _clutch2_tau_68 = COND_EXP_EQ(_clutch2_locked_78, JMI_TRUE, _clutch2_sa_75, COND_EXP_EQ(_clutch2_free_74, JMI_TRUE, AD_WRAP_LITERAL(0), _clutch2_cgeo_60 * _clutch2_fn_63 * COND_EXP_EQ(_clutch2_startForward_76, JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch2_w_rel_66, tmp_5, AD_WRAP_LITERAL(2)), COND_EXP_EQ(_clutch2_startBackward_77, JMI_TRUE, - func_Modelica_Math_tempInterpol1_exp(- _clutch2_w_rel_66, tmp_6, AD_WRAP_LITERAL(2)), COND_EXP_EQ(COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch2_w_rel_66, tmp_7, AD_WRAP_LITERAL(2)), - func_Modelica_Math_tempInterpol1_exp(- _clutch2_w_rel_66, tmp_8, AD_WRAP_LITERAL(2)))))));
        _der_J2_w_160 = _der_2_J2_phi_191;
        _J2_a_56 = _der_J2_w_160;
        JMI_ARRAY_STATIC_INIT_2(tmp_9, 1, 2)
        jmi_array_ref_2(tmp_9, 1,1) = _clutch1_mue_pos_1_1_11;
        jmi_array_ref_2(tmp_9, 1,2) = _clutch1_mue_pos_1_2_12;
        JMI_ARRAY_STATIC_INIT_2(tmp_10, 1, 2)
        jmi_array_ref_2(tmp_10, 1,1) = _clutch1_mue_pos_1_1_11;
        jmi_array_ref_2(tmp_10, 1,2) = _clutch1_mue_pos_1_2_12;
        JMI_ARRAY_STATIC_INIT_2(tmp_11, 1, 2)
        jmi_array_ref_2(tmp_11, 1,1) = _clutch1_mue_pos_1_1_11;
        jmi_array_ref_2(tmp_11, 1,2) = _clutch1_mue_pos_1_2_12;
        JMI_ARRAY_STATIC_INIT_2(tmp_12, 1, 2)
        jmi_array_ref_2(tmp_12, 1,1) = _clutch1_mue_pos_1_1_11;
        jmi_array_ref_2(tmp_12, 1,2) = _clutch1_mue_pos_1_2_12;
        _clutch1_tau_22 = COND_EXP_EQ(_clutch1_locked_32, JMI_TRUE, _clutch1_sa_29, COND_EXP_EQ(_clutch1_free_28, JMI_TRUE, AD_WRAP_LITERAL(0), _clutch1_cgeo_14 * _clutch1_fn_17 * COND_EXP_EQ(_clutch1_startForward_30, JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch1_w_rel_20, tmp_9, AD_WRAP_LITERAL(2)), COND_EXP_EQ(_clutch1_startBackward_31, JMI_TRUE, - func_Modelica_Math_tempInterpol1_exp(- _clutch1_w_rel_20, tmp_10, AD_WRAP_LITERAL(2)), COND_EXP_EQ(COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), JMI_TRUE, func_Modelica_Math_tempInterpol1_exp(_clutch1_w_rel_20, tmp_11, AD_WRAP_LITERAL(2)), - func_Modelica_Math_tempInterpol1_exp(- _clutch1_w_rel_20, tmp_12, AD_WRAP_LITERAL(2)))))));
        if (evaluation_mode & JMI_BLOCK_EVALUATE) {
            (*res)[0] = - _clutch3_tau_105 - (_J4_J_127 * _J4_a_131);
            (*res)[1] = - _clutch2_tau_68 + _clutch3_tau_105 - (_J3_J_89 * _J3_a_93);
            (*res)[2] = - _clutch1_tau_22 + _clutch2_tau_68 - (_J2_J_52 * _J2_a_56);
            (*res)[3] = _torque_tau_8 + _clutch1_tau_22 - (_J1_J_3 * _J1_a_7);
        }
    }
    return ef;
}







void func_Modelica_Math_tempInterpol1_def(jmi_ad_var_t u_v, jmi_array_t* table_a, jmi_ad_var_t icol_v, jmi_ad_var_t* y_o) {
    JMI_DYNAMIC_INIT()
    jmi_ad_var_t y_v;
    jmi_ad_var_t i_v;
    jmi_ad_var_t n_v;
    jmi_ad_var_t u1_v;
    jmi_ad_var_t u2_v;
    jmi_ad_var_t y1_v;
    jmi_ad_var_t y2_v;
    n_v = jmi_array_size(table_a, 0);
    if (COND_EXP_LE(n_v, 1, JMI_TRUE, JMI_FALSE)) {
        y_v = jmi_array_val_2(table_a, 1, icol_v);
    } else {
        if (COND_EXP_LE(u_v, jmi_array_val_2(table_a, 1, 1), JMI_TRUE, JMI_FALSE)) {
            i_v = 1;
        } else {
            i_v = 2;
            while (LOG_EXP_AND(COND_EXP_LT(i_v, n_v, JMI_TRUE, JMI_FALSE), COND_EXP_GE(u_v, jmi_array_val_2(table_a, i_v, 1), JMI_TRUE, JMI_FALSE))) {
                i_v = i_v + 1;
            }
            i_v = i_v - 1;
        }
        u1_v = jmi_array_val_2(table_a, i_v, 1);
        u2_v = jmi_array_val_2(table_a, i_v + 1, 1);
        y1_v = jmi_array_val_2(table_a, i_v, icol_v);
        y2_v = jmi_array_val_2(table_a, i_v + 1, icol_v);
        if (COND_EXP_GT(u2_v, u1_v, JMI_TRUE, JMI_FALSE) == JMI_FALSE) {
            jmi_assert_failed("Table index must be increasing", JMI_ASSERT_ERROR);
        }
        y_v = y1_v + jmi_divide_function("Modelica.Math.tempInterpol1", (y2_v - y1_v) * (u_v - u1_v),(u2_v - u1_v),"(y2 - y1) * (u - u1) / (u2 - u1)");
    }
    if (y_o != NULL) *y_o = y_v;
    JMI_DYNAMIC_FREE()
    return;
}

jmi_ad_var_t func_Modelica_Math_tempInterpol1_exp(jmi_ad_var_t u_v, jmi_array_t* table_a, jmi_ad_var_t icol_v) {
    jmi_ad_var_t y_v;
    func_Modelica_Math_tempInterpol1_def(u_v, table_a, icol_v, &y_v);
    return y_v;
}








static int model_ode_guards(jmi_t* jmi) {

    return 0;
}

static int model_ode_next_time_event(jmi_t* jmi, jmi_real_t* nextTime) {
  jmi_real_t nextTimeEvent;
  jmi_real_t nextTimeEventTmp;
  jmi_real_t nSamp;
  nextTimeEvent = JMI_INF;
  *nextTime = nextTimeEvent;

    return 0;
}

static int model_ode_derivatives(jmi_t* jmi) {
    int ef = 0;
    model_ode_guards(jmi);
/************* ODE section *********/
    if (jmi->atInitial || jmi->atEvent) {
        _sw(36) = jmi_turn_switch(_time - (_step2_startTime_51), _sw(36), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch3_f_normalized_101 = _step2_offset_139 + COND_EXP_EQ(_sw(36), JMI_TRUE, AD_WRAP_LITERAL(0), _step2_height_138);
    _clutch3_fn_100 = _clutch3_fn_max_98 * _clutch3_f_normalized_101;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(24) = jmi_turn_switch(_clutch3_fn_100 - (0), _sw(24), jmi->events_epsilon, JMI_REL_LEQ);
    }
    _clutch3_free_111 = _sw(24);
    _clutch3_tau0_109 = _clutch3_mue0_133 * _clutch3_cgeo_97 * _clutch3_fn_100;
    _clutch3_tau0_max_110 = _clutch3_peak_96 * _clutch3_tau0_109;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(12) = jmi_turn_switch(_time - (_step1_startTime_10), _sw(12), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch2_f_normalized_64 = _step1_offset_50 + COND_EXP_EQ(_sw(12), JMI_TRUE, AD_WRAP_LITERAL(0), _step1_height_49);
    _clutch2_fn_63 = _clutch2_fn_max_61 * _clutch2_f_normalized_64;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(13) = jmi_turn_switch(_clutch2_fn_63 - (0), _sw(13), jmi->events_epsilon, JMI_REL_LEQ);
    }
    _clutch2_free_74 = _sw(13);
    _clutch2_tau0_72 = _clutch2_mue0_99 * _clutch2_cgeo_60 * _clutch2_fn_63;
    _clutch2_tau0_max_73 = _clutch2_peak_59 * _clutch2_tau0_72;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(35) = jmi_turn_switch(_time - (_sin2_startTime_136), _sw(35), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch1_f_normalized_18 = _sin2_offset_135 + COND_EXP_EQ(_sw(35), JMI_TRUE, AD_WRAP_LITERAL(0), _sin2_amplitude_132 * sin(AD_WRAP_LITERAL(6.283185307179586) * _sin2_freqHz_16 * (_time - _sin2_startTime_136) + _sin2_phase_134));
    _clutch1_fn_17 = _clutch1_fn_max_15 * _clutch1_f_normalized_18;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(0) = jmi_turn_switch(_clutch1_fn_17 - (0), _sw(0), jmi->events_epsilon, JMI_REL_LEQ);
    }
    _clutch1_free_28 = _sw(0);
    _clutch1_tau0_26 = _clutch1_mue0_62 * _clutch1_cgeo_14 * _clutch1_fn_17;
    _clutch1_tau0_max_27 = _clutch1_peak_13 * _clutch1_tau0_26;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(11) = jmi_turn_switch(_time - (_sin1_startTime_47), _sw(11), jmi->events_epsilon, JMI_REL_LT);
    }
    _torque_tau_8 = _sin1_offset_46 + COND_EXP_EQ(_sw(11), JMI_TRUE, AD_WRAP_LITERAL(0), _sin1_amplitude_43 * sin(AD_WRAP_LITERAL(6.283185307179586) * _sin1_freqHz_44 * (_time - _sin1_startTime_47) + _sin1_phase_45));
    ef |= jmi_solve_block_residual(jmi->dae_block_residuals[0]);
    _der_clutch1_phi_rel_197 = jmi_divide_equation(jmi, (- _clutch1_w_rel_20),(- 1.0),"(- clutch1.w_rel) / (- 1.0)");
    _der_J1_phi_159 = jmi_divide_equation(jmi, (- _J1_w_6),(- 1.0),"(- J1.w) / (- 1.0)");
    _der_J2_phi_199 = jmi_divide_equation(jmi, (- _der_clutch1_phi_rel_197 + (- _der_J1_phi_159)),(- 1.0),"(- clutch1.der(phi_rel) + (- J1._der_phi)) / (- 1.0)");
    _der_clutch2_phi_rel_200 = jmi_divide_equation(jmi, (- _clutch2_w_rel_66),(- 1.0),"(- clutch2.w_rel) / (- 1.0)");
    _der_clutch3_phi_rel_202 = jmi_divide_equation(jmi, (- _clutch3_w_rel_103),(- 1.0),"(- clutch3.w_rel) / (- 1.0)");
/************ Real outputs *********/
/****Integer and boolean outputs ***/
/**** Other variables ***/
    _J1_phi_5 = - _clutch1_phi_rel_19 + _J2_phi_54;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(9) = jmi_turn_switch(_clutch1_w_rel_20 - (AD_WRAP_LITERAL(0)), _sw(9), jmi->events_epsilon, JMI_REL_GT);
    }
    if (jmi->atInitial || jmi->atEvent) {
        _sw(10) = jmi_turn_switch(_clutch1_w_rel_20 - (AD_WRAP_LITERAL(0)), _sw(10), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch1_mode_38 = COND_EXP_EQ(_clutch1_free_28, JMI_TRUE, AD_WRAP_LITERAL(2), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch1_startForward_30), _sw(9)), JMI_TRUE, AD_WRAP_LITERAL(1), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(-1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch1_startBackward_31), _sw(10)), JMI_TRUE, AD_WRAP_LITERAL(-1), AD_WRAP_LITERAL(0))));
    _clutch1_lossPower_42 = _clutch1_tau_22 * _clutch1_w_rel_20;
    _J2_w_55 = _der_J2_phi_199;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(22) = jmi_turn_switch(_clutch2_w_rel_66 - (AD_WRAP_LITERAL(0)), _sw(22), jmi->events_epsilon, JMI_REL_GT);
    }
    if (jmi->atInitial || jmi->atEvent) {
        _sw(23) = jmi_turn_switch(_clutch2_w_rel_66 - (AD_WRAP_LITERAL(0)), _sw(23), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch2_mode_84 = COND_EXP_EQ(_clutch2_free_74, JMI_TRUE, AD_WRAP_LITERAL(2), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch2_startForward_76), _sw(22)), JMI_TRUE, AD_WRAP_LITERAL(1), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(-1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch2_startBackward_77), _sw(23)), JMI_TRUE, AD_WRAP_LITERAL(-1), AD_WRAP_LITERAL(0))));
    _clutch2_lossPower_88 = _clutch2_tau_68 * _clutch2_w_rel_66;
    _J3_phi_91 = jmi_divide_equation(jmi, (- _clutch2_phi_rel_65 + (- _J2_phi_54)),(- 1.0),"(- clutch2.phi_rel + (- J2.phi)) / (- 1.0)");
    _der_J3_phi_161 = jmi_divide_equation(jmi, (- _der_clutch2_phi_rel_200 + (- _der_J2_phi_199)),(- 1.0),"(- clutch2.der(phi_rel) + (- J2.der(phi))) / (- 1.0)");
    _J3_w_92 = _der_J3_phi_161;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(33) = jmi_turn_switch(_clutch3_w_rel_103 - (AD_WRAP_LITERAL(0)), _sw(33), jmi->events_epsilon, JMI_REL_GT);
    }
    if (jmi->atInitial || jmi->atEvent) {
        _sw(34) = jmi_turn_switch(_clutch3_w_rel_103 - (AD_WRAP_LITERAL(0)), _sw(34), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch3_mode_121 = COND_EXP_EQ(_clutch3_free_111, JMI_TRUE, AD_WRAP_LITERAL(2), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch3_startForward_113), _sw(33)), JMI_TRUE, AD_WRAP_LITERAL(1), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(-1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch3_startBackward_114), _sw(34)), JMI_TRUE, AD_WRAP_LITERAL(-1), AD_WRAP_LITERAL(0))));
    _clutch3_lossPower_125 = _clutch3_tau_105 * _clutch3_w_rel_103;
    _J4_phi_129 = jmi_divide_equation(jmi, (- _clutch3_phi_rel_102 + (- _J3_phi_91)),(- 1.0),"(- clutch3.phi_rel + (- J3.phi)) / (- 1.0)");
    _der_J4_phi_163 = jmi_divide_equation(jmi, (- _der_clutch3_phi_rel_202 + (- _der_J3_phi_161)),(- 1.0),"(- clutch3.der(phi_rel) + (- J3._der_phi)) / (- 1.0)");
    _J4_w_130 = _der_J4_phi_163;
/********* Write back reinits *******/

    return ef;
}

static int model_ode_derivatives_dir_der(jmi_t* jmi) {
    int ef = 0;

    return ef;
}

static int model_ode_outputs(jmi_t* jmi) {
    int ef = 0;

    return ef;
}

static int model_ode_guards_init(jmi_t* jmi) {

    return 0;
}

static int model_ode_initialize(jmi_t* jmi) {
    int ef = 0;
    model_ode_guards(jmi);
    _J1_w_6 = 10;
    _der_J1_phi_159 = jmi_divide_equation(jmi, (- _J1_w_6),(- 1.0),"(- J1.w) / (- 1.0)");
    if (jmi->atInitial || jmi->atEvent) {
        _sw(11) = jmi_turn_switch(_time - (_sin1_startTime_47), _sw(11), jmi->events_epsilon, JMI_REL_LT);
    }
    _torque_tau_8 = _sin1_offset_46 + COND_EXP_EQ(_sw(11), JMI_TRUE, AD_WRAP_LITERAL(0), _sin1_amplitude_43 * sin(AD_WRAP_LITERAL(6.283185307179586) * _sin1_freqHz_44 * (_time - _sin1_startTime_47) + _sin1_phase_45));
    if (jmi->atInitial || jmi->atEvent) {
        _sw(35) = jmi_turn_switch(_time - (_sin2_startTime_136), _sw(35), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch1_f_normalized_18 = _sin2_offset_135 + COND_EXP_EQ(_sw(35), JMI_TRUE, AD_WRAP_LITERAL(0), _sin2_amplitude_132 * sin(AD_WRAP_LITERAL(6.283185307179586) * _sin2_freqHz_16 * (_time - _sin2_startTime_136) + _sin2_phase_134));
    _clutch1_fn_17 = _clutch1_fn_max_15 * _clutch1_f_normalized_18;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(0) = jmi_turn_switch(_clutch1_fn_17 - (0), _sw(0), jmi->events_epsilon, JMI_REL_LEQ);
    }
    _clutch1_free_28 = _sw(0);
    if (jmi->atInitial || jmi->atEvent) {
        _sw(12) = jmi_turn_switch(_time - (_step1_startTime_10), _sw(12), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch2_f_normalized_64 = _step1_offset_50 + COND_EXP_EQ(_sw(12), JMI_TRUE, AD_WRAP_LITERAL(0), _step1_height_49);
    _clutch2_fn_63 = _clutch2_fn_max_61 * _clutch2_f_normalized_64;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(13) = jmi_turn_switch(_clutch2_fn_63 - (0), _sw(13), jmi->events_epsilon, JMI_REL_LEQ);
    }
    _clutch2_free_74 = _sw(13);
    if (jmi->atInitial || jmi->atEvent) {
        _sw(36) = jmi_turn_switch(_time - (_step2_startTime_51), _sw(36), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch3_f_normalized_101 = _step2_offset_139 + COND_EXP_EQ(_sw(36), JMI_TRUE, AD_WRAP_LITERAL(0), _step2_height_138);
    _clutch3_fn_100 = _clutch3_fn_max_98 * _clutch3_f_normalized_101;
    if (jmi->atInitial || jmi->atEvent) {
        _sw(24) = jmi_turn_switch(_clutch3_fn_100 - (0), _sw(24), jmi->events_epsilon, JMI_REL_LEQ);
    }
    _clutch3_free_111 = _sw(24);
    _clutch3_tau0_109 = _clutch3_mue0_133 * _clutch3_cgeo_97 * _clutch3_fn_100;
    _clutch3_tau0_max_110 = _clutch3_peak_96 * _clutch3_tau0_109;
    pre_clutch3_mode_121 = 3;
    pre_clutch3_startBackward_114 = JMI_FALSE;
    _J4_w_130 = 0;
    _der_J4_phi_163 = jmi_divide_equation(jmi, (- _J4_w_130),(- 1.0),"(- J4.w) / (- 1.0)");
    _J3_w_92 = 0;
    _der_J3_phi_161 = jmi_divide_equation(jmi, (- _J3_w_92),(- 1.0),"(- J3.w) / (- 1.0)");
    _der_clutch3_phi_rel_202 = _der_J4_phi_163 + (- _der_J3_phi_161);
    _clutch3_w_rel_103 = _der_clutch3_phi_rel_202;
    pre_clutch3_startForward_113 = JMI_FALSE;
    _clutch2_tau0_72 = _clutch2_mue0_99 * _clutch2_cgeo_60 * _clutch2_fn_63;
    _clutch2_tau0_max_73 = _clutch2_peak_59 * _clutch2_tau0_72;
    pre_clutch2_mode_84 = 3;
    pre_clutch2_startBackward_77 = JMI_FALSE;
    _J2_w_55 = 0;
    _der_J2_phi_199 = jmi_divide_equation(jmi, (- _J2_w_55),(- 1.0),"(- J2.w) / (- 1.0)");
    _der_clutch2_phi_rel_200 = _der_J3_phi_161 + (- _der_J2_phi_199);
    _clutch2_w_rel_66 = _der_clutch2_phi_rel_200;
    pre_clutch2_startForward_76 = JMI_FALSE;
    _clutch1_tau0_26 = _clutch1_mue0_62 * _clutch1_cgeo_14 * _clutch1_fn_17;
    _clutch1_tau0_max_27 = _clutch1_peak_13 * _clutch1_tau0_26;
    pre_clutch1_mode_38 = 3;
    pre_clutch1_startBackward_31 = JMI_FALSE;
    _der_clutch1_phi_rel_197 = _der_J2_phi_199 + (- _der_J1_phi_159);
    _clutch1_w_rel_20 = _der_clutch1_phi_rel_197;
    pre_clutch1_startForward_30 = JMI_FALSE;
    ef |= jmi_solve_block_residual(jmi->dae_init_block_residuals[0]);
    _clutch1_lossPower_42 = _clutch1_tau_22 * _clutch1_w_rel_20;
    _J1_phi_5 = 0;
    _J2_phi_54 = 0;
    _clutch1_phi_rel_19 = _J2_phi_54 + (- _J1_phi_5);
    if (jmi->atInitial || jmi->atEvent) {
        _sw(9) = jmi_turn_switch(_clutch1_w_rel_20 - (AD_WRAP_LITERAL(0)), _sw(9), jmi->events_epsilon, JMI_REL_GT);
    }
    if (jmi->atInitial || jmi->atEvent) {
        _sw(10) = jmi_turn_switch(_clutch1_w_rel_20 - (AD_WRAP_LITERAL(0)), _sw(10), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch1_mode_38 = COND_EXP_EQ(_clutch1_free_28, JMI_TRUE, AD_WRAP_LITERAL(2), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch1_startForward_30), _sw(9)), JMI_TRUE, AD_WRAP_LITERAL(1), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(-1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch1_mode_38, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch1_startBackward_31), _sw(10)), JMI_TRUE, AD_WRAP_LITERAL(-1), AD_WRAP_LITERAL(0))));
    _clutch2_lossPower_88 = _clutch2_tau_68 * _clutch2_w_rel_66;
    _J3_phi_91 = 0;
    _clutch2_phi_rel_65 = _J3_phi_91 + (- _J2_phi_54);
    if (jmi->atInitial || jmi->atEvent) {
        _sw(22) = jmi_turn_switch(_clutch2_w_rel_66 - (AD_WRAP_LITERAL(0)), _sw(22), jmi->events_epsilon, JMI_REL_GT);
    }
    if (jmi->atInitial || jmi->atEvent) {
        _sw(23) = jmi_turn_switch(_clutch2_w_rel_66 - (AD_WRAP_LITERAL(0)), _sw(23), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch2_mode_84 = COND_EXP_EQ(_clutch2_free_74, JMI_TRUE, AD_WRAP_LITERAL(2), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch2_startForward_76), _sw(22)), JMI_TRUE, AD_WRAP_LITERAL(1), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(-1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch2_mode_84, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch2_startBackward_77), _sw(23)), JMI_TRUE, AD_WRAP_LITERAL(-1), AD_WRAP_LITERAL(0))));
    _clutch3_lossPower_125 = _clutch3_tau_105 * _clutch3_w_rel_103;
    _J4_phi_129 = 0;
    _clutch3_phi_rel_102 = _J4_phi_129 + (- _J3_phi_91);
    if (jmi->atInitial || jmi->atEvent) {
        _sw(33) = jmi_turn_switch(_clutch3_w_rel_103 - (AD_WRAP_LITERAL(0)), _sw(33), jmi->events_epsilon, JMI_REL_GT);
    }
    if (jmi->atInitial || jmi->atEvent) {
        _sw(34) = jmi_turn_switch(_clutch3_w_rel_103 - (AD_WRAP_LITERAL(0)), _sw(34), jmi->events_epsilon, JMI_REL_LT);
    }
    _clutch3_mode_121 = COND_EXP_EQ(_clutch3_free_111, JMI_TRUE, AD_WRAP_LITERAL(2), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch3_startForward_113), _sw(33)), JMI_TRUE, AD_WRAP_LITERAL(1), COND_EXP_EQ(LOG_EXP_AND(LOG_EXP_OR(LOG_EXP_OR(COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(-1), JMI_TRUE, JMI_FALSE), COND_EXP_EQ(pre_clutch3_mode_121, AD_WRAP_LITERAL(2), JMI_TRUE, JMI_FALSE)), _clutch3_startBackward_114), _sw(34)), JMI_TRUE, AD_WRAP_LITERAL(-1), AD_WRAP_LITERAL(0))));
    pre_clutch1_free_28 = JMI_FALSE;
    pre_clutch1_locked_32 = JMI_FALSE;
    pre_clutch2_free_74 = JMI_FALSE;
    pre_clutch2_locked_78 = JMI_FALSE;
    pre_clutch3_free_111 = JMI_FALSE;
    pre_clutch3_locked_115 = JMI_FALSE;

    return ef;
}


static int model_ode_initialize_dir_der(jmi_t* jmi) {
    int ef = 0;
    /* This function is not needed - no derivatives of the initialization system is exposed.*/
    return ef;
}

static int model_dae_F(jmi_t* jmi, jmi_real_t** res) {

    return 0;
}

static int model_dae_dir_dF(jmi_t* jmi, jmi_real_t** res, jmi_real_t** dF, jmi_real_t** dz) {

    return 0;
}

static int model_dae_R(jmi_t* jmi, jmi_real_t** res) {
    (*res)[0] = _clutch1_fn_17 - (0);
    (*res)[1] = _clutch1_sa_29 - (_clutch1_tau0_max_27);
    (*res)[2] = _clutch1_sa_29 - (_clutch1_tau0_26);
    (*res)[3] = _clutch1_w_rel_20 - (_clutch1_w_small_25);
    (*res)[4] = _clutch1_w_rel_20 - (0);
    (*res)[5] = _clutch1_sa_29 - (- _clutch1_tau0_max_27);
    (*res)[6] = _clutch1_sa_29 - (- _clutch1_tau0_26);
    (*res)[7] = _clutch1_w_rel_20 - (- _clutch1_w_small_25);
    (*res)[8] = _clutch1_w_rel_20 - (0);
    (*res)[9] = _clutch1_w_rel_20 - (AD_WRAP_LITERAL(0));
    (*res)[10] = _clutch1_w_rel_20 - (AD_WRAP_LITERAL(0));
    (*res)[11] = _time - (_sin1_startTime_47);
    (*res)[12] = _time - (_step1_startTime_10);
    (*res)[13] = _clutch2_fn_63 - (0);
    (*res)[14] = _clutch2_sa_75 - (_clutch2_tau0_max_73);
    (*res)[15] = _clutch2_sa_75 - (_clutch2_tau0_72);
    (*res)[16] = _clutch2_w_rel_66 - (_clutch2_w_small_71);
    (*res)[17] = _clutch2_w_rel_66 - (0);
    (*res)[18] = _clutch2_sa_75 - (- _clutch2_tau0_max_73);
    (*res)[19] = _clutch2_sa_75 - (- _clutch2_tau0_72);
    (*res)[20] = _clutch2_w_rel_66 - (- _clutch2_w_small_71);
    (*res)[21] = _clutch2_w_rel_66 - (0);
    (*res)[22] = _clutch2_w_rel_66 - (AD_WRAP_LITERAL(0));
    (*res)[23] = _clutch2_w_rel_66 - (AD_WRAP_LITERAL(0));
    (*res)[24] = _clutch3_fn_100 - (0);
    (*res)[25] = _clutch3_sa_112 - (_clutch3_tau0_max_110);
    (*res)[26] = _clutch3_sa_112 - (_clutch3_tau0_109);
    (*res)[27] = _clutch3_w_rel_103 - (_clutch3_w_small_108);
    (*res)[28] = _clutch3_w_rel_103 - (0);
    (*res)[29] = _clutch3_sa_112 - (- _clutch3_tau0_max_110);
    (*res)[30] = _clutch3_sa_112 - (- _clutch3_tau0_109);
    (*res)[31] = _clutch3_w_rel_103 - (- _clutch3_w_small_108);
    (*res)[32] = _clutch3_w_rel_103 - (0);
    (*res)[33] = _clutch3_w_rel_103 - (AD_WRAP_LITERAL(0));
    (*res)[34] = _clutch3_w_rel_103 - (AD_WRAP_LITERAL(0));
    (*res)[35] = _time - (_sin2_startTime_136);
    (*res)[36] = _time - (_step2_startTime_51);

    return 0;
}

static int model_init_F0(jmi_t* jmi, jmi_real_t** res) {

    return 0;
}

static int model_init_F1(jmi_t* jmi, jmi_real_t** res) {
    (*res)[0] = 0.0 - _J1_a_7;
    (*res)[1] = 0.0 - _torque_tau_8;
    (*res)[2] = 0.0 - _clutch1_fn_17;
    (*res)[3] = 0.0 - _clutch1_f_normalized_18;
    (*res)[4] = 0 - _clutch1_phi_rel_19;
    (*res)[5] = 0 - _clutch1_w_rel_20;
    (*res)[6] = 0 - _clutch1_a_rel_21;
    (*res)[7] = 0.0 - _clutch1_tau_22;
    (*res)[8] = 0.0 - _clutch1_tau0_26;
    (*res)[9] = 0.0 - _clutch1_tau0_max_27;
    (*res)[10] = 0.0 - _clutch1_sa_29;
    (*res)[11] = 0.0 - _clutch1_lossPower_42;
    (*res)[12] = 0.0 - _J2_a_56;
    (*res)[13] = 0.0 - _clutch2_fn_63;
    (*res)[14] = 0.0 - _clutch2_f_normalized_64;
    (*res)[15] = 0 - _clutch2_phi_rel_65;
    (*res)[16] = 0 - _clutch2_w_rel_66;
    (*res)[17] = 0 - _clutch2_a_rel_67;
    (*res)[18] = 0.0 - _clutch2_tau_68;
    (*res)[19] = 0.0 - _clutch2_tau0_72;
    (*res)[20] = 0.0 - _clutch2_tau0_max_73;
    (*res)[21] = 0.0 - _clutch2_sa_75;
    (*res)[22] = 0.0 - _clutch2_lossPower_88;
    (*res)[23] = 0.0 - _J3_a_93;
    (*res)[24] = 0.0 - _clutch3_fn_100;
    (*res)[25] = 0.0 - _clutch3_f_normalized_101;
    (*res)[26] = 0 - _clutch3_phi_rel_102;
    (*res)[27] = 0 - _clutch3_w_rel_103;
    (*res)[28] = 0 - _clutch3_a_rel_104;
    (*res)[29] = 0.0 - _clutch3_tau_105;
    (*res)[30] = 0.0 - _clutch3_tau0_109;
    (*res)[31] = 0.0 - _clutch3_tau0_max_110;
    (*res)[32] = 0.0 - _clutch3_sa_112;
    (*res)[33] = 0.0 - _clutch3_lossPower_125;
    (*res)[34] = 0.0 - _J4_a_131;
    (*res)[35] = 0.0 - _der_J1_phi_159;
    (*res)[36] = 0.0 - _der_J2_w_160;
    (*res)[37] = 0.0 - _der_J3_phi_161;
    (*res)[38] = 0.0 - _der_J3_w_162;
    (*res)[39] = 0.0 - _der_J4_phi_163;
    (*res)[40] = 0.0 - _der_J4_w_164;
    (*res)[41] = 0.0 - _der_2_J1_phi_189;
    (*res)[42] = 0.0 - _der_2_clutch1_phi_rel_190;
    (*res)[43] = 0.0 - _der_2_J2_phi_191;
    (*res)[44] = 0.0 - _der_2_clutch2_phi_rel_192;
    (*res)[45] = 0.0 - _der_2_J3_phi_193;
    (*res)[46] = 0.0 - _der_2_clutch3_phi_rel_194;
    (*res)[47] = 0.0 - _der_2_J4_phi_195;

    return 0;
}

static int model_init_Fp(jmi_t* jmi, jmi_real_t** res) {
    /* C_DAE_initial_dependent_parameter_residuals */
    return -1;
}

static int model_init_eval_parameters(jmi_t* jmi) {
    JMI_ARRAY_STATIC(tmp_13, 2, 2)
    JMI_ARRAY_STATIC(tmp_14, 2, 2)
    JMI_ARRAY_STATIC(tmp_15, 2, 2)
    _step1_startTime_10 = (_T2_1);
    _sin2_freqHz_16 = (_freqHz_0);
    _step2_startTime_51 = (_T3_2);
    JMI_ARRAY_STATIC_INIT_2(tmp_13, 1, 2)
    jmi_array_ref_2(tmp_13, 1,1) = _clutch1_mue_pos_1_1_11;
    jmi_array_ref_2(tmp_13, 1,2) = _clutch1_mue_pos_1_2_12;
    _clutch1_mue0_62 = (func_Modelica_Math_tempInterpol1_exp(AD_WRAP_LITERAL(0), tmp_13, AD_WRAP_LITERAL(2)));
    JMI_ARRAY_STATIC_INIT_2(tmp_14, 1, 2)
    jmi_array_ref_2(tmp_14, 1,1) = _clutch2_mue_pos_1_1_57;
    jmi_array_ref_2(tmp_14, 1,2) = _clutch2_mue_pos_1_2_58;
    _clutch2_mue0_99 = (func_Modelica_Math_tempInterpol1_exp(AD_WRAP_LITERAL(0), tmp_14, AD_WRAP_LITERAL(2)));
    JMI_ARRAY_STATIC_INIT_2(tmp_15, 1, 2)
    jmi_array_ref_2(tmp_15, 1,1) = _clutch3_mue_pos_1_1_94;
    jmi_array_ref_2(tmp_15, 1,2) = _clutch3_mue_pos_1_2_95;
    _clutch3_mue0_133 = (func_Modelica_Math_tempInterpol1_exp(AD_WRAP_LITERAL(0), tmp_15, AD_WRAP_LITERAL(2)));
    _torque_phi_support_140 = (_fixed_phi0_141);
    _fixed_flange_phi_157 = (_torque_phi_support_140);
    _torque_support_phi_158 = (_torque_phi_support_140);

    return 0;
}

static int model_init_R0(jmi_t* jmi, jmi_real_t** res) {
    (*res)[0] = _clutch1_fn_17 - (0);
    (*res)[1] = _clutch1_sa_29 - (_clutch1_tau0_max_27);
    (*res)[2] = _clutch1_sa_29 - (_clutch1_tau0_26);
    (*res)[3] = _clutch1_w_rel_20 - (_clutch1_w_small_25);
    (*res)[4] = _clutch1_w_rel_20 - (0);
    (*res)[5] = _clutch1_sa_29 - (- _clutch1_tau0_max_27);
    (*res)[6] = _clutch1_sa_29 - (- _clutch1_tau0_26);
    (*res)[7] = _clutch1_w_rel_20 - (- _clutch1_w_small_25);
    (*res)[8] = _clutch1_w_rel_20 - (0);
    (*res)[9] = _clutch1_w_rel_20 - (AD_WRAP_LITERAL(0));
    (*res)[10] = _clutch1_w_rel_20 - (AD_WRAP_LITERAL(0));
    (*res)[11] = _time - (_sin1_startTime_47);
    (*res)[12] = _time - (_step1_startTime_10);
    (*res)[13] = _clutch2_fn_63 - (0);
    (*res)[14] = _clutch2_sa_75 - (_clutch2_tau0_max_73);
    (*res)[15] = _clutch2_sa_75 - (_clutch2_tau0_72);
    (*res)[16] = _clutch2_w_rel_66 - (_clutch2_w_small_71);
    (*res)[17] = _clutch2_w_rel_66 - (0);
    (*res)[18] = _clutch2_sa_75 - (- _clutch2_tau0_max_73);
    (*res)[19] = _clutch2_sa_75 - (- _clutch2_tau0_72);
    (*res)[20] = _clutch2_w_rel_66 - (- _clutch2_w_small_71);
    (*res)[21] = _clutch2_w_rel_66 - (0);
    (*res)[22] = _clutch2_w_rel_66 - (AD_WRAP_LITERAL(0));
    (*res)[23] = _clutch2_w_rel_66 - (AD_WRAP_LITERAL(0));
    (*res)[24] = _clutch3_fn_100 - (0);
    (*res)[25] = _clutch3_sa_112 - (_clutch3_tau0_max_110);
    (*res)[26] = _clutch3_sa_112 - (_clutch3_tau0_109);
    (*res)[27] = _clutch3_w_rel_103 - (_clutch3_w_small_108);
    (*res)[28] = _clutch3_w_rel_103 - (0);
    (*res)[29] = _clutch3_sa_112 - (- _clutch3_tau0_max_110);
    (*res)[30] = _clutch3_sa_112 - (- _clutch3_tau0_109);
    (*res)[31] = _clutch3_w_rel_103 - (- _clutch3_w_small_108);
    (*res)[32] = _clutch3_w_rel_103 - (0);
    (*res)[33] = _clutch3_w_rel_103 - (AD_WRAP_LITERAL(0));
    (*res)[34] = _clutch3_w_rel_103 - (AD_WRAP_LITERAL(0));
    (*res)[35] = _time - (_sin2_startTime_136);
    (*res)[36] = _time - (_step2_startTime_51);

    return 0;
}

int jmi_new(jmi_t** jmi, jmi_callbacks_t* jmi_callbacks) {

    jmi_init(jmi, N_real_ci, N_real_cd, N_real_pi, N_real_pd,
             N_integer_ci, N_integer_cd, N_integer_pi, N_integer_pd,
             N_boolean_ci, N_boolean_cd, N_boolean_pi, N_boolean_pd,
             N_string_ci, N_string_cd, N_string_pi, N_string_pd,
             N_real_dx, N_real_x, N_real_u, N_real_w,
             N_real_d, N_integer_d, N_integer_u, N_boolean_d, N_boolean_u,
             N_string_d, N_string_u, N_outputs, (int (*))Output_vrefs,
             N_sw, N_sw_init, N_guards, N_guards_init,
             N_dae_blocks, N_dae_init_blocks,
             N_initial_relations, (int (*))DAE_initial_relations,
             N_relations, (int (*))DAE_relations,
             (jmi_real_t *) DAE_nominals,
             Scaling_method, N_ext_objs, jmi_callbacks);

    jmi_dae_add_equation_block(*jmi, dae_block_0, NULL, 4, 9, 12, JMI_DISCRETE_VARIABILITY, JMI_LINEAR_SOLVER, 0);


    jmi_dae_init_add_equation_block(*jmi, dae_init_block_0, NULL, 4, 9, 12, JMI_DISCRETE_VARIABILITY, JMI_LINEAR_SOLVER, 0);






    /* Initialize the DAE interface */
    jmi_dae_init(*jmi, *model_dae_F, N_eq_F, NULL, 0, NULL, NULL,
                 *model_dae_dir_dF,
                 CAD_dae_n_nz,(int (*))CAD_dae_nz_rows,(int (*))CAD_dae_nz_cols,
                 CAD_ODE_A_n_nz, (int (*))CAD_ODE_A_nz_rows, (int(*))CAD_ODE_A_nz_cols,
                 CAD_ODE_B_n_nz, (int (*))CAD_ODE_B_nz_rows, (int(*))CAD_ODE_B_nz_cols,
                 CAD_ODE_C_n_nz, (int (*))CAD_ODE_C_nz_rows, (int(*))CAD_ODE_C_nz_cols,
                 CAD_ODE_D_n_nz, (int (*))CAD_ODE_D_nz_rows, (int(*))CAD_ODE_D_nz_cols,
                 *model_dae_R, N_eq_R, NULL, 0, NULL, NULL,*model_ode_derivatives,
                 *model_ode_derivatives_dir_der,
                 *model_ode_outputs,*model_ode_initialize,*model_ode_guards,
                 *model_ode_guards_init,*model_ode_next_time_event);

    /* Initialize the Init interface */
    jmi_init_init(*jmi, *model_init_F0, N_eq_F0, NULL,
                  0, NULL, NULL,
                  *model_init_F1, N_eq_F1, NULL,
                  0, NULL, NULL,
                  *model_init_Fp, N_eq_Fp, NULL,
                  0, NULL, NULL,
                  *model_init_eval_parameters,
                  *model_init_R0, N_eq_R0, NULL,
                  0, NULL, NULL);

    return 0;
}

int jmi_terminate(jmi_t* jmi) {

    return 0;
}

int jmi_set_start_values(jmi_t* jmi) {
    _clutch1_Unknown_33 = (3);
    _clutch1_Free_34 = (2);
    _clutch1_Forward_35 = (1);
    _clutch1_Stuck_36 = (0);
    _clutch1_Backward_37 = (- 1);
    _clutch1_unitAngularAcceleration_39 = (1);
    _clutch1_unitTorque_40 = (1);
    _sin1_pi_48 = (3.141592653589793);
    _clutch2_Unknown_79 = (3);
    _clutch2_Free_80 = (2);
    _clutch2_Forward_81 = (1);
    _clutch2_Stuck_82 = (0);
    _clutch2_Backward_83 = (- 1);
    _clutch2_unitAngularAcceleration_85 = (1);
    _clutch2_unitTorque_86 = (1);
    _clutch3_Unknown_116 = (3);
    _clutch3_Free_117 = (2);
    _clutch3_Forward_118 = (1);
    _clutch3_Stuck_119 = (0);
    _clutch3_Backward_120 = (- 1);
    _clutch3_unitAngularAcceleration_122 = (1);
    _clutch3_unitTorque_123 = (1);
    _J4_flange_b_tau_126 = (0);
    _sin2_pi_137 = (3.141592653589793);
    _freqHz_0 = (0.2);
    _T2_1 = (0.4);
    _T3_2 = (0.9);
    _J1_J_3 = (1);
    _J1_stateSelect_4 = (3);
    _torque_useSupport_9 = (JMI_TRUE);
    _clutch1_mue_pos_1_1_11 = (0);
    _clutch1_mue_pos_1_2_12 = (0.5);
    _clutch1_peak_13 = (1.1);
    _clutch1_cgeo_14 = (1);
    _clutch1_fn_max_15 = (20);
    _clutch1_phi_nominal_23 = (1.0E-4);
    _clutch1_stateSelect_24 = (4);
    _clutch1_w_small_25 = (1.0E10);
    _clutch1_useHeatPort_41 = (JMI_FALSE);
    _sin1_amplitude_43 = (10);
    _sin1_freqHz_44 = (5);
    _sin1_phase_45 = (0);
    _sin1_offset_46 = (0);
    _sin1_startTime_47 = (0);
    _step1_height_49 = (1);
    _step1_offset_50 = (0);
    _J2_J_52 = (1);
    _J2_stateSelect_53 = (3);
    _clutch2_mue_pos_1_1_57 = (0);
    _clutch2_mue_pos_1_2_58 = (0.5);
    _clutch2_peak_59 = (1.1);
    _clutch2_cgeo_60 = (1);
    _clutch2_fn_max_61 = (20);
    _clutch2_phi_nominal_69 = (1.0E-4);
    _clutch2_stateSelect_70 = (4);
    _clutch2_w_small_71 = (1.0E10);
    _clutch2_useHeatPort_87 = (JMI_FALSE);
    _J3_J_89 = (1);
    _J3_stateSelect_90 = (3);
    _clutch3_mue_pos_1_1_94 = (0);
    _clutch3_mue_pos_1_2_95 = (0.5);
    _clutch3_peak_96 = (1.1);
    _clutch3_cgeo_97 = (1);
    _clutch3_fn_max_98 = (20);
    _clutch3_phi_nominal_106 = (1.0E-4);
    _clutch3_stateSelect_107 = (4);
    _clutch3_w_small_108 = (1.0E10);
    _clutch3_useHeatPort_124 = (JMI_FALSE);
    _J4_J_127 = (1);
    _J4_stateSelect_128 = (3);
    _sin2_amplitude_132 = (1);
    _sin2_phase_134 = (1.57);
    _sin2_offset_135 = (0);
    _sin2_startTime_136 = (0);
    _step2_height_138 = (1);
    _step2_offset_139 = (0);
    _fixed_phi0_141 = (0);
    __block_jacobian_check_165 = (JMI_FALSE);
    __block_jacobian_check_tol_166 = (1.0E-6);
    __block_solver_experimental_mode_167 = (0);
    __cs_rel_tol_168 = (1.0E-6);
    __cs_solver_169 = (0);
    __cs_step_size_170 = (0.0010);
    __enforce_bounds_171 = (JMI_FALSE);
    __events_default_tol_172 = (1.0E-10);
    __events_tol_factor_173 = (1.0E-4);
    __iteration_variable_scaling_174 = (1);
    __log_level_175 = (3);
    __nle_solver_check_jac_cond_176 = (JMI_FALSE);
    __nle_solver_default_tol_177 = (1.0E-10);
    __nle_solver_max_iter_178 = (100);
    __nle_solver_min_tol_179 = (1.0E-12);
    __nle_solver_regularization_tolerance_180 = (-1.0);
    __nle_solver_step_limit_factor_181 = (10.0);
    __nle_solver_tol_factor_182 = (1.0E-4);
    __rescale_after_singular_jac_183 = (JMI_TRUE);
    __rescale_each_step_184 = (JMI_FALSE);
    __residual_equation_scaling_185 = (1);
    __runtime_log_to_file_186 = (JMI_FALSE);
    __use_Brent_in_1d_187 = (JMI_FALSE);
    __use_jacobian_equilibration_188 = (JMI_FALSE);
    model_init_eval_parameters(jmi);
    _J1_phi_5 = (0);
    _J1_w_6 = (10);
    _J1_a_7 = (0.0);
    _torque_tau_8 = (0.0);
    _clutch1_fn_17 = (0.0);
    _clutch1_f_normalized_18 = (0.0);
    _clutch1_phi_rel_19 = (0);
    _clutch1_w_rel_20 = (0);
    _clutch1_a_rel_21 = (0);
    _clutch1_tau_22 = (0.0);
    _clutch1_tau0_26 = (0.0);
    _clutch1_tau0_max_27 = (0.0);
    _clutch1_free_28 = (JMI_FALSE);
    _clutch1_sa_29 = (0.0);
    _clutch1_startForward_30 = (JMI_FALSE);
    _clutch1_startBackward_31 = (JMI_FALSE);
    _clutch1_locked_32 = (JMI_FALSE);
    _clutch1_mode_38 = (3);
    _clutch1_lossPower_42 = (0.0);
    _J2_phi_54 = (0);
    _J2_w_55 = (0);
    _J2_a_56 = (0.0);
    _clutch2_fn_63 = (0.0);
    _clutch2_f_normalized_64 = (0.0);
    _clutch2_phi_rel_65 = (0);
    _clutch2_w_rel_66 = (0);
    _clutch2_a_rel_67 = (0);
    _clutch2_tau_68 = (0.0);
    _clutch2_tau0_72 = (0.0);
    _clutch2_tau0_max_73 = (0.0);
    _clutch2_free_74 = (JMI_FALSE);
    _clutch2_sa_75 = (0.0);
    _clutch2_startForward_76 = (JMI_FALSE);
    _clutch2_startBackward_77 = (JMI_FALSE);
    _clutch2_locked_78 = (JMI_FALSE);
    _clutch2_mode_84 = (3);
    _clutch2_lossPower_88 = (0.0);
    _J3_phi_91 = (0);
    _J3_w_92 = (0);
    _J3_a_93 = (0.0);
    _clutch3_fn_100 = (0.0);
    _clutch3_f_normalized_101 = (0.0);
    _clutch3_phi_rel_102 = (0);
    _clutch3_w_rel_103 = (0);
    _clutch3_a_rel_104 = (0);
    _clutch3_tau_105 = (0.0);
    _clutch3_tau0_109 = (0.0);
    _clutch3_tau0_max_110 = (0.0);
    _clutch3_free_111 = (JMI_FALSE);
    _clutch3_sa_112 = (0.0);
    _clutch3_startForward_113 = (JMI_FALSE);
    _clutch3_startBackward_114 = (JMI_FALSE);
    _clutch3_locked_115 = (JMI_FALSE);
    _clutch3_mode_121 = (3);
    _clutch3_lossPower_125 = (0.0);
    _J4_phi_129 = (0);
    _J4_w_130 = (0);
    _J4_a_131 = (0.0);
    _der_J1_phi_159 = (0.0);
    _der_J2_w_160 = (0.0);
    _der_J3_phi_161 = (0.0);
    _der_J3_w_162 = (0.0);
    _der_J4_phi_163 = (0.0);
    _der_J4_w_164 = (0.0);
    _der_2_J1_phi_189 = (0.0);
    _der_2_clutch1_phi_rel_190 = (0.0);
    _der_2_J2_phi_191 = (0.0);
    _der_2_clutch2_phi_rel_192 = (0.0);
    _der_2_J3_phi_193 = (0.0);
    _der_2_clutch3_phi_rel_194 = (0.0);
    _der_2_J4_phi_195 = (0.0);
    _der_J1_w_196 = (0.0);
    _der_clutch1_phi_rel_197 = (0.0);
    _der_clutch1_w_rel_198 = (0.0);
    _der_J2_phi_199 = (0.0);
    _der_clutch2_phi_rel_200 = (0.0);
    _der_clutch2_w_rel_201 = (0.0);
    _der_clutch3_phi_rel_202 = (0.0);
    _der_clutch3_w_rel_203 = (0.0);
    pre_clutch1_free_28 = (JMI_FALSE);
    pre_clutch1_startForward_30 = (JMI_FALSE);
    pre_clutch1_startBackward_31 = (JMI_FALSE);
    pre_clutch1_locked_32 = (JMI_FALSE);
    pre_clutch1_mode_38 = (3);
    pre_clutch2_free_74 = (JMI_FALSE);
    pre_clutch2_startForward_76 = (JMI_FALSE);
    pre_clutch2_startBackward_77 = (JMI_FALSE);
    pre_clutch2_locked_78 = (JMI_FALSE);
    pre_clutch2_mode_84 = (3);
    pre_clutch3_free_111 = (JMI_FALSE);
    pre_clutch3_startForward_113 = (JMI_FALSE);
    pre_clutch3_startBackward_114 = (JMI_FALSE);
    pre_clutch3_locked_115 = (JMI_FALSE);
    pre_clutch3_mode_121 = (3);

    return 0;
}

const char *jmi_get_model_identifier() {
    return "CoupledClutches";
}

/*
void _emit(log_t *log, char* message) { }
void create_log_file_if_needed(log_t *log) { }
BOOL emitted_category(log_t *log, category_t category) { 0; }
*/
/* FMI 2.0 functions common for both ME and CS.*/

FMI_Export const char* fmiGetTypesPlatform() {
    return fmi2_get_types_platform();
}

FMI_Export const char* fmiGetVersion() {
    return fmi2_get_version();
}

FMI_Export fmiStatus fmiSetDebugLogging(fmiComponent    c,
                                        fmiBoolean      loggingOn, 
                                        size_t          nCategories, 
                                        const fmiString categories[]) {
    return fmi2_set_debug_logging(c, loggingOn, nCategories, categories);
}

FMI_Export fmiComponent fmiInstantiate(fmiString instanceName,
                                       fmiType   fmuType, 
                                       fmiString fmuGUID, 
                                       fmiString fmuResourceLocation, 
                                       const fmiCallbackFunctions* functions, 
                                       fmiBoolean                  visible,
                                       fmiBoolean                  loggingOn) {
                                           
    
    if (fmuType == fmiCoSimulation) {
#ifndef FMUCS20
        functions->logger(0, instanceName, fmiError, "ERROR", "The model is not compiled as a Co-Simulation FMU.");
        return NULL;
#endif
    } else if (fmuType == fmiModelExchange) {
#ifndef FMUME20
        functions->logger(0, instanceName, fmiError, "ERROR", "The model is not compiled as a Model Exchange FMU.");
        return NULL;
#endif
    }
    return fmi2_instantiate(instanceName, fmuType, fmuGUID, fmuResourceLocation,
                            functions, visible, loggingOn);
}

FMI_Export void fmiFreeInstance(fmiComponent c) {
    fmi2_free_instance(c);
}

FMI_Export fmiStatus fmiSetupExperiment(fmiComponent c, 
                                        fmiBoolean   toleranceDefined, 
                                        fmiReal      tolerance, 
                                        fmiReal      startTime, 
                                        fmiBoolean   stopTimeDefined, 
                                        fmiReal      stopTime) {
    return fmi2_setup_experiment(c, toleranceDefined, tolerance, startTime,
                                 stopTimeDefined, stopTime);
}

FMI_Export fmiStatus fmiEnterInitializationMode(fmiComponent c) {
    return fmi2_enter_initialization_mode(c);
}

FMI_Export fmiStatus fmiExitInitializationMode(fmiComponent c) {
    return fmi2_exit_initialization_mode(c);
}

FMI_Export fmiStatus fmiTerminate(fmiComponent c) {
    return fmi2_terminate(c);
}

FMI_Export fmiStatus fmiReset(fmiComponent c) {
    return fmi2_reset(c);
}

FMI_Export fmiStatus fmiGetReal(fmiComponent c, const fmiValueReference vr[],
                                size_t nvr, fmiReal value[]) {
    return fmi2_get_real(c, vr, nvr, value);
}

FMI_Export fmiStatus fmiGetInteger(fmiComponent c, const fmiValueReference vr[],
                                   size_t nvr, fmiInteger value[]) {
    return fmi2_get_integer(c, vr, nvr, value);
}

FMI_Export fmiStatus fmiGetBoolean(fmiComponent c, const fmiValueReference vr[],
                                   size_t nvr, fmiBoolean value[]) {
    return fmi2_get_boolean(c, vr, nvr, value);
}

FMI_Export fmiStatus fmiGetString(fmiComponent c, const fmiValueReference vr[],
                                  size_t nvr, fmiString value[]) {
    return fmi2_get_string(c, vr, nvr, value);
}

FMI_Export fmiStatus fmiSetReal(fmiComponent c, const fmiValueReference vr[],
                                size_t nvr, const fmiReal value[]) {
    return fmi2_set_real(c, vr, nvr, value);
}

FMI_Export fmiStatus fmiSetInteger(fmiComponent c, const fmiValueReference vr[],
                                  size_t nvr, const fmiInteger value[]) {
    return fmi2_set_integer(c, vr, nvr, value);
}

FMI_Export fmiStatus fmiSetBoolean(fmiComponent c, const fmiValueReference vr[],
                                   size_t nvr, const fmiBoolean value[]) {
    return fmi2_set_boolean(c, vr, nvr, value);
}

FMI_Export fmiStatus fmiSetString(fmiComponent c, const fmiValueReference vr[],
                                  size_t nvr, const fmiString value[]) {
    return fmi2_set_string(c, vr, nvr, value);
}

FMI_Export fmiStatus fmiGetFMUstate(fmiComponent c, fmiFMUstate* FMUstate) {
    return fmi2_get_fmu_state(c, FMUstate);
}

FMI_Export fmiStatus fmiSetFMUstate(fmiComponent c, fmiFMUstate FMUstate) {
    return fmi2_set_fmu_state(c, FMUstate);
}

FMI_Export fmiStatus fmiFreeFMUstate(fmiComponent c, fmiFMUstate* FMUstate) {
    return fmi2_free_fmu_state(c, FMUstate);
}

FMI_Export fmiStatus fmiSerializedFMUstateSize(fmiComponent c, fmiFMUstate FMUstate,
                                               size_t* size) {
    return fmi2_serialized_fmu_state_size(c, FMUstate, size);
}

FMI_Export fmiStatus fmiSerializedFMUstate(fmiComponent c, fmiFMUstate FMUstate,
                                  fmiByte serializedState[], size_t size) {
    return fmi2_serialized_fmu_state(c, FMUstate, serializedState, size);
}

FMI_Export fmiStatus fmiDeSerializedFMUstate(fmiComponent c,
                                  const fmiByte serializedState[],
                                  size_t size, fmiFMUstate* FMUstate) {
    return fmi2_de_serialized_fmu_state(c, serializedState, size, FMUstate);
}

FMI_Export fmiStatus fmiGetDirectionalDerivative(fmiComponent c,
                const fmiValueReference vUnknown_ref[], size_t nUnknown,
                const fmiValueReference vKnown_ref[],   size_t nKnown,
                const fmiReal dvKnown[], fmiReal dvUnknown[]) {
        return fmi2_get_directional_derivative(c, vUnknown_ref, nUnknown,
                                           vKnown_ref, nKnown, dvKnown, dvUnknown);
}


#ifdef FMUME20
/* FMI 2.0 functions specific for ME.*/

FMI_Export fmiStatus fmiEnterEventMode(fmiComponent c) {
        return fmi2_enter_event_mode(c);
}

FMI_Export fmiStatus fmiNewDiscreteStates(fmiComponent  c,
                                          fmiEventInfo* fmiEventInfo) {
        return fmi2_new_discrete_state(c, fmiEventInfo);
}

FMI_Export fmiStatus fmiEnterContinuousTimeMode(fmiComponent c) {
        return fmi2_enter_continuous_time_mode(c);
}

FMI_Export fmiStatus fmiCompletedIntegratorStep(fmiComponent c,
                                                fmiBoolean   noSetFMUStatePriorToCurrentPoint, 
                                                fmiBoolean*  enterEventMode, 
                                                fmiBoolean*   terminateSimulation) {
        return fmi2_completed_integrator_step(c, noSetFMUStatePriorToCurrentPoint,
                                          enterEventMode, terminateSimulation);
}

FMI_Export fmiStatus fmiSetTime(fmiComponent c, fmiReal time) {
        return fmi2_set_time(c, time);
}

FMI_Export fmiStatus fmiSetContinuousStates(fmiComponent c, const fmiReal x[],
                                            size_t nx) {
        return fmi2_set_continuous_states(c, x, nx);
}

FMI_Export fmiStatus fmiGetDerivatives(fmiComponent c, fmiReal derivatives[],
                                       size_t nx) {
        return fmi2_get_derivatives(c, derivatives, nx);
}

FMI_Export fmiStatus fmiGetEventIndicators(fmiComponent c, 
                                           fmiReal eventIndicators[], size_t ni) {
        return fmi2_get_event_indicators(c, eventIndicators, ni);
}

FMI_Export fmiStatus fmiGetContinuousStates(fmiComponent c, fmiReal x[],
                                            size_t nx) {
        return fmi2_get_continuous_states(c, x, nx);
}

FMI_Export fmiStatus fmiGetNominalsOfContinuousStates(fmiComponent c, 
                                                      fmiReal x_nominal[], 
                                                      size_t nx) {
        return fmi2_get_nominals_of_continuous_states(c, x_nominal, nx);
}

#endif

#ifdef FMUCS20
/* FMI 2.0 functions specific for CS.*/

FMI_Export fmiStatus fmiSetRealInputDerivatives(fmiComponent c, 
                                                const fmiValueReference vr[],
                                                size_t nvr, const fmiInteger order[],
                                                const fmiReal value[]) {
        return fmi2_set_real_input_derivatives(c, vr, nvr, order, value);
}

FMI_Export fmiStatus fmiGetRealOutputDerivatives(fmiComponent c,
                                                 const fmiValueReference vr[],
                                                 size_t nvr, const fmiInteger order[],
                                                 fmiReal value[]) {
        return fmi2_get_real_output_derivatives(c, vr, nvr, order, value);
}

FMI_Export fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
                               fmiReal    communicationStepSize,
                               fmiBoolean noSetFMUStatePriorToCurrentPoint) {
        return fmi2_do_step(c, currentCommunicationPoint, communicationStepSize,
                        noSetFMUStatePriorToCurrentPoint);
}

FMI_Export fmiStatus fmiCancelStep(fmiComponent c) {
        return fmi2_cancel_step(c);
}

FMI_Export fmiStatus fmiGetStatus(fmiComponent c, const fmiStatusKind s,
                                  fmiStatus* value) {
        return fmi2_get_status(c, s, value);
}

FMI_Export fmiStatus fmiGetRealStatus(fmiComponent c, const fmiStatusKind s,
                                      fmiReal* value) {
        return fmi2_get_real_status(c, s, value);
}

FMI_Export fmiStatus fmiGetIntegerStatus(fmiComponent c, const fmiStatusKind s,
                                         fmiInteger* values) {
        return fmi2_get_integer_status(c, s, values);
}

FMI_Export fmiStatus fmiGetBooleanStatus(fmiComponent c, const fmiStatusKind s,
                                         fmiBoolean* value) {
        return fmi2_get_boolean_status(c, s, value);
}

FMI_Export fmiStatus fmiGetStringStatus(fmiComponent c, const fmiStatusKind s,
                                        fmiString* value) {
        return fmi2_get_string_status(c, s, value);
        
}

#endif
