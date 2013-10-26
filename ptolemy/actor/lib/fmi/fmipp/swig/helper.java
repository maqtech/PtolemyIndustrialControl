/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package ptolemy.actor.lib.fmi.fmipp.swig;

/**
 * helper class.
 *
 * @author cxh
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class helper {
  public static SWIGTYPE_p_double new_double_array(int nelements) {
    long cPtr = helperJNI.new_double_array(nelements);
    return (cPtr == 0) ? null : new SWIGTYPE_p_double(cPtr, false);
  }

  public static void delete_double_array(SWIGTYPE_p_double ary) {
    helperJNI.delete_double_array(SWIGTYPE_p_double.getCPtr(ary));
  }

  public static double double_array_getitem(SWIGTYPE_p_double ary, int index) {
    return helperJNI.double_array_getitem(SWIGTYPE_p_double.getCPtr(ary), index);
  }

  public static void double_array_setitem(SWIGTYPE_p_double ary, int index, double value) {
    helperJNI.double_array_setitem(SWIGTYPE_p_double.getCPtr(ary), index, value);
  }

  public static SWIGTYPE_p_std__string new_string_array(int nelements) {
    long cPtr = helperJNI.new_string_array(nelements);
    return (cPtr == 0) ? null : new SWIGTYPE_p_std__string(cPtr, false);
  }

  public static void delete_string_array(SWIGTYPE_p_std__string ary) {
    helperJNI.delete_string_array(SWIGTYPE_p_std__string.getCPtr(ary));
  }

  public static String string_array_getitem(SWIGTYPE_p_std__string ary, int index) {
    return helperJNI.string_array_getitem(SWIGTYPE_p_std__string.getCPtr(ary), index);
  }

  public static void string_array_setitem(SWIGTYPE_p_std__string ary, int index, String value) {
    helperJNI.string_array_setitem(SWIGTYPE_p_std__string.getCPtr(ary), index, value);
  }

}
