/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package ptolemy.actor.lib.fmi.fmipp.swig;

public class helperJNI {
  public final static native long new_double_array(int jarg1);
  public final static native void delete_double_array(long jarg1);
  public final static native double double_array_getitem(long jarg1, int jarg2);
  public final static native void double_array_setitem(long jarg1, int jarg2, double jarg3);
  public final static native long new_string_array(int jarg1);
  public final static native void delete_string_array(long jarg1);
  public final static native String string_array_getitem(long jarg1, int jarg2);
  public final static native void string_array_setitem(long jarg1, int jarg2, String jarg3);
  public final static native long new_IncrementalFMU__SWIG_0(String jarg1);
  public final static native long new_IncrementalFMU__SWIG_1(String jarg1, String jarg2);
  public final static native long new_IncrementalFMU__SWIG_2(String jarg1, String jarg2, String jarg3);
  public final static native long new_IncrementalFMU__SWIG_3(String jarg1, long jarg2, long jarg3, long jarg4, long jarg5);
  public final static native long new_IncrementalFMU__SWIG_4(String jarg1, long jarg2, long jarg3);
  public final static native long new_IncrementalFMU__SWIG_5(long jarg1, IncrementalFMU jarg1_);
  public final static native void delete_IncrementalFMU(long jarg1);
  public final static native int IncrementalFMU_init(long jarg1, IncrementalFMU jarg1_, String jarg2, long jarg3, long jarg4, long jarg5, double jarg6, double jarg7, double jarg8, double jarg9);
  public final static native void IncrementalFMU_defineInputs(long jarg1, IncrementalFMU jarg1_, long jarg2, long jarg3);
  public final static native void IncrementalFMU_defineOutputs(long jarg1, IncrementalFMU jarg1_, long jarg2, long jarg3);
  public final static native long IncrementalFMU_getCurrentOutputs(long jarg1, IncrementalFMU jarg1_);
  public final static native double IncrementalFMU_sync__SWIG_0(long jarg1, IncrementalFMU jarg1_, double jarg2, double jarg3);
  public final static native double IncrementalFMU_sync__SWIG_1(long jarg1, IncrementalFMU jarg1_, double jarg2, double jarg3, long jarg4);
}
