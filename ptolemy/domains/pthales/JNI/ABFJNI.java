/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009-2010 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.
*/
package ptolemy.domains.pthales.JNI;

/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

class ABFJNI {
    public final static native double C_get();

    public final static native double Pi_get();

    public final static native int ATL_nb_ant_get();

    public final static native int ATL_nb_pls_get();

    public final static native int ATL_nb_beams_get();

    public final static native int ATL_nb_rg_cov_get();

    public final static native int ATL_org_rg_cov_get();

    public final static native int ATL_lgth_chirp_get();

    public final static native double ATL_K_chirp_get();

    public final static native int ATL_rg_min_get();

    public final static native double ATL_lambda_get();

    public final static native double ATL_Tpulse_get();

    public final static native double ATL_SubarraySpacing_get();

    public final static native int ATL_Targ1_CIR_get();

    public final static native int ATL_Targ1_V_get();

    public final static native int ATL_Targ1_Dist_get();

    public final static native int ATL_Targ1_RCS_get();

    public final static native int ATL_rg_size_get();

    public final static native int ATL_Beamwidth_get();

    public final static native int ATL_noise_power_get();

    public final static native int ATL_jam_CIR_get();

    public final static native int ATL_jam_power_get();

    public final static native int ATL_jam_freq_MHz_get();

    public final static native void Calc_Chirp(int jarg1, float[] jarg2_,
            int jarg3, float jarg4);

    public final static native void AddNoise(int nx, int ny, float[] sig_in,
            float Sigma2, float[] noisy);

    public final static native void Calc_Echo(int nb_samp_chirpX4,
            float[] ChirpX4, int nb_ant, int nb_rg, int nb_pul,
            float[] echo_out, int rg_min, float rg_size, float SubArraySpacing,
            float lambda, float Tpulse, float Targ_angle, float Targ_V,
            float Targ_dist, float Targ_RCS);

    public final static native void Calc_SteerVect(int jarg1, int jarg2,
            float[] jarg3, float jarg4, float jarg5, float jarg6);

    public final static native void DecimBy4(int jarg1, float[] jarg2,
            int jarg3, float[] jarg4);

    public final static native void AddJam(int jarg1, int jarg2, int jarg3,
            float[] jarg4, int jarg5, int jarg6, int jarg7, float[] jarg8,
            float jarg9, float jarg10, float jarg11, float jarg12,
            float jarg13, float jarg14);

    public final static native void Slid_Filter(int jarg1, float[] jarg2,
            int jarg3, float[] jarg4, int jarg5, float[] jarg6);

    public final static native void Apply_Filter(int jarg1, int jarg2,
            float[] jarg3, int jarg4, float[] jarg5, int jarg6, float[] jarg7);

    public final static native void lazy_FFT(int jarg1, float[] jarg2,
            int jarg3, float[] jarg4);

    public final static native void CovAvCov(int jarg1, int jarg2, int jarg3,
            float[] jarg4, int jarg5, int jarg6, float[] jarg7);

    public final static native void Mat_Invert(int jarg1, int jarg2,
            float[] jarg3, int jarg4, int jarg5, float[] jarg6);

    public final static native void Matmat(int jarg1, int jarg2, float[] jarg3,
            int jarg4, int jarg5, float[] jarg6, int jarg7, int jarg8,
            float[] jarg9);

    public final static native void CalcWeights(int jarg1, int jarg2,
            float[] jarg3, int jarg4, int jarg5, float[] jarg6, int jarg7,
            int jarg8, float[] jarg9);
}
