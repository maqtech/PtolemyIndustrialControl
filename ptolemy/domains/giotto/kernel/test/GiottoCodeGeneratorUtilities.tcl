# Tests for the GiottoCodeGeneratorUtilties
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Ptolemy test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[string compare test [info procs jdkCapture]] == 1} then {
    source $PTII/util/testsuite/jdktools.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


# Generate giotto for the Ptolemy model named by the model arg
proc generateGiotto { model } {
    set args [java::new {String[]} 1 \
            [list $model]]

    jdkCapture {
        java::call ptolemy.domains.giotto.kernel.GiottoCodeGeneratorUtilities \
            main $args
    } output
    regsub {@version \$Id.*$} $output {@version ...} output2
    return $output2
}


######################################################################
####
#
test GiottoCodeGeneratorUtilities-1.1 {Generate code for the Simple demo} {
    set args [java::new {String[]} 0 \
            [list {}]]

    jdkCaptureErr {
       catch {
           java::call \
               ptolemy.domains.giotto.kernel.GiottoCodeGeneratorUtilities \
               main $args
       } errMsg1
    } errMsg2
    list $errMsg1
} {{java.lang.IllegalArgumentException: Usage: java -classpath $PTII ptolemy.domains.giotto.kernel.GiottoCodeGeneratorUtilities ptolemyModel.xml
The model is read in and Giotto code is generated on stdout.}}


######################################################################
####
#
test GiottoCodeGeneratorUtilities-1.2 {Generate code for the Simple demo} {
    generateGiotto "../../demo/Simple/Simple.xml"
} {/* Giotto code for Simple
   Generated by Ptolemy II Giotto Code Generator.
 */

//////////////////////////////////////////////////////
//// Simple
/**
Simple
@author
@version ...
*/

//////////////////////////////////////////////////////
////                    sensors                   ////

sensor

//////////////////////////////////////////////////////
////                    actuators                 ////

actuator

//////////////////////////////////////////////////////
////                    output ports              ////

output
  Token_port Ramp_output := CGinit_Ramp_output;

//////////////////////////////////////////////////////
////                    tasks                     ////

/** Ramp
 */
task Ramp ()
        output (Ramp_output)
        state ()
{
        schedule CGRamp_Task(Ramp_output)
}

/** outputs
 */
task outputs (Token_port outputs_input)
        output ()
        state ()
{
        schedule CGoutputs_Task(outputs_input)
}

/** outputs2
 */
task outputs2 (Token_port outputs2_input)
        output ()
        state ()
{
        schedule CGoutputs2_Task(outputs2_input)
}

//////////////////////////////////////////////////////
////                    drivers for common actors ////

driver outputs_driver (Ramp_output)
        output (Token_port outputs_input)
{
          if constant_true() then outputs_inputdriver( Ramp_output, outputs_input)
}

driver outputs2_driver (Ramp_output)
        output (Token_port outputs2_input)
{
          if constant_true() then outputs2_inputdriver( Ramp_output, outputs2_input)
}

//////////////////////////////////////////////////////
////                    output drivers            ////


//////////////////////////////////////////////////////
////                    modes                     ////

start Simple {

    //////////////////////////////////////////////////////
    ////                   mode Simple
    mode Simple () period 100 {
        taskfreq 1 do Ramp();
        taskfreq 1 do outputs(outputs_driver);
        taskfreq 2 do outputs2(outputs2_driver);
    }
}

}


######################################################################
####
#
test GiottoCodeGeneratorUtilities-2.1 {Generate code for the Hierarchy demo} {
    generateGiotto "../../demo/Hierarchy/HierarchyCG.xml"
} {/* Giotto code for HierarchyCG
   Generated by Ptolemy II Giotto Code Generator.
 */

//////////////////////////////////////////////////////
//// HierarchyCG
/**
HierarchyCG
@author
@version ...
*/

//////////////////////////////////////////////////////
////                    sensors                   ////

sensor

//////////////////////////////////////////////////////
////                    actuators                 ////

actuator

//////////////////////////////////////////////////////
////                    output ports              ////

output
  Token_port Source_output := CGinit_Source_output;
  Token_port Upper_Composite_output := CGinit_Upper_Composite_output;
  Token_port Lower_Composite_output := CGinit_Lower_Composite_output;

//////////////////////////////////////////////////////
////                    tasks                     ////

/** Source
 */
task Source ()
        output (Source_output)
        state ()
{
        schedule CGSource_Task(Source_output)
}

/** Upper_Composite
 */
task Upper_Composite (Token_port Upper_Composite_input)
        output (Upper_Composite_output)
        state ()
{
        schedule CGUpper_Composite_Task(Upper_Composite_input, Upper_Composite_output)
}

/** Lower_Composite
 */
task Lower_Composite (Token_port Lower_Composite_input)
        output (Lower_Composite_output)
        state ()
{
        schedule CGLower_Composite_Task(Lower_Composite_input, Lower_Composite_output)
}

/** Plotter
 */
task Plotter (Token_port Plotter_input)
        output ()
        state ()
{
        schedule CGPlotter_Task(Plotter_input)
}

/** Plotter2
 */
task Plotter2 (Token_port Plotter2_input)
        output ()
        state ()
{
        schedule CGPlotter2_Task(Plotter2_input)
}

//////////////////////////////////////////////////////
////                    drivers for common actors ////

driver Upper_Composite_driver (Source_output)
        output (Token_port Upper_Composite_input)
{
          if constant_true() then Upper_Composite_inputdriver( Source_output, Upper_Composite_input)
}

driver Lower_Composite_driver (Source_output)
        output (Token_port Lower_Composite_input)
{
          if constant_true() then Lower_Composite_inputdriver( Source_output, Lower_Composite_input)
}

driver Plotter_driver (Upper_Composite_output)
        output (Token_port Plotter_input)
{
          if constant_true() then Plotter_inputdriver( Upper_Composite_output, Plotter_input)
}

driver Plotter2_driver (Lower_Composite_output)
        output (Token_port Plotter2_input)
{
          if constant_true() then Plotter2_inputdriver( Lower_Composite_output, Plotter2_input)
}

//////////////////////////////////////////////////////
////                    output drivers            ////


//////////////////////////////////////////////////////
////                    modes                     ////

start HierarchyCG {

    //////////////////////////////////////////////////////
    ////                   mode HierarchyCG
    mode HierarchyCG () period 1000 {
        taskfreq 1 do Source();
        taskfreq 1 do Upper_Composite(Upper_Composite_driver);
        taskfreq 2 do Lower_Composite(Lower_Composite_driver);
        taskfreq 2 do Plotter(Plotter_driver);
        taskfreq 2 do Plotter2(Plotter2_driver);
    }
}

}

######################################################################
####
#
test GiottoCodeGeneratorUtilities-2.2 {Generate code for the Hierarchy demo, which has multiports, so Giotto codegen fails} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser setMoMLFilters \
        [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]
    $parser addMoMLFilter \
        [java::new ptolemy.moml.filter.RemoveGraphicalClasses]
    set file [java::new java.io.File "../../demo/Hierarchy/Hierarchy.xml"]
    set url [$file toURL]

    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser {parse java.net.URL java.net.URL} [java::null] $url]]
    puts "# Ignore the next message about cannot recieve data from multiple sources"
    catch {
        java::call ptolemy.domains.giotto.kernel.GiottoCodeGeneratorUtilities \
            generateGiottoCode $toplevel
    } errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Failed to generate Giotto code.
  in .Hierarchy
Because:
Input port cannot receive data from multiple sources in Giotto.
  in .Hierarchy.Plotter.input}}


######################################################################
####
#
test GiottoCodeGeneratorUtilities-3.1 {Generate code for the Multimode demo} {
    generateGiotto "../../demo/Multimode/Multimode.xml"
} {/* Giotto code for Multimode
   Generated by Ptolemy II Giotto Code Generator.
 */

//////////////////////////////////////////////////////
//// Multimode
/**
Multimode
@author
@version ...
*/

//////////////////////////////////////////////////////
////                    sensors                   ////

sensor

//////////////////////////////////////////////////////
////                    actuators                 ////

actuator

//////////////////////////////////////////////////////
////                    output ports              ////

output
  Token_port Ramp_output := CGinit_Ramp_output;
  Token_port modal_model_out := CGinit_modal_model_out;

//////////////////////////////////////////////////////
////                    tasks                     ////

/** Ramp
 */
task Ramp ()
        output (Ramp_output)
        state ()
{
        schedule CGRamp_Task(Ramp_output)
}

/** modal_model
 */
task modal_model (Token_port modal_model_in)
        output (modal_model_out)
        state ()
{
        schedule CGmodal_model_Task(modal_model_in, modal_model_out)
}

/** outputs
 */
task outputs (Token_port outputs_input)
        output ()
        state ()
{
        schedule CGoutputs_Task(outputs_input)
}

//////////////////////////////////////////////////////
////                    drivers for common actors ////

driver modal_model_driver (Ramp_output)
        output (Token_port modal_model_in)
{
          if constant_true() then modal_model_inputdriver( Ramp_output, modal_model_in)
}

driver outputs_driver (modal_model_out)
        output (Token_port outputs_input)
{
          if constant_true() then outputs_inputdriver( modal_model_out, outputs_input)
}

//////////////////////////////////////////////////////
////                    output drivers            ////


//////////////////////////////////////////////////////
////                    modes                     ////

start Multimode {

    //////////////////////////////////////////////////////
    ////                   mode Multimode
    mode Multimode () period 100 {
        taskfreq 2 do Ramp();
        taskfreq 1 do modal_model(modal_model_driver);
        taskfreq 1 do outputs(outputs_driver);
    }
}

}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
