# Tests for the BackwardCompatibility class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}


set constMoml  "$header 
<entity name=\"BackwardCompatibilityConst\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">
  </entity>
</entity>"

######################################################################
####
#
test BackwardCompatibility-1.1 {Const: added an _icon} { 
    # This test is sort of pointless, since we add the Const _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build
    
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]

    set toplevel [$parser parse $constMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityConst" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
</entity>
}}

set mathFunctionMoml  "$header 
<entity name=\"BackwardCompatibilityMathFunction\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"MathFunction\" class=\"ptolemy.actor.lib.MathFunction\">
  </entity>
</entity>"

test BackwardCompatibility-3.1 {MathFunction} { 
    # This test is sort of pointless, since we add the _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $mathFunctionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityMathFunction" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="MathFunction" class="ptolemy.actor.lib.MathFunction">
        <property name="function" class="ptolemy.kernel.util.StringAttribute" value="exp">
        </property>
        <port name="firstOperand" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
</entity>
}}


set scaleMoml  "$header 
<entity name=\"BackwardCompatibilityScale\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"Scale\" class=\"ptolemy.actor.lib.Scale\">
  </entity>
</entity>"

test BackwardCompatibility-4.1 {Scale} { 
    # This test is sort of pointless, since we add the _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $scaleMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityScale" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="Scale" class="ptolemy.actor.lib.Scale">
        <property name="factor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="scaleOnLeft" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
</entity>
}}

set trigFunctionMoml  "$header 
<entity name=\"BackwardCompatibilityTrigFunction\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"TrigFunction\" class=\"ptolemy.actor.lib.TrigFunction\">
  </entity>
</entity>"

test BackwardCompatibility-5.1 {TrigFunction} { 
    # This test is sort of pointless, since we add the _icon
    # and then remove it.  If we don't remove, this test will not run under
    # the nightly build

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $trigFunctionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityTrigFunction" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="TrigFunction" class="ptolemy.actor.lib.TrigFunction">
        <property name="function" class="ptolemy.kernel.util.StringAttribute" value="sin">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
</entity>
}}


######################################################################
####
#

set complexToCartesianMoml  "$header 
<entity name=\"BackwardCompatibilityComplextToCartesian\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"ComplexToCartesian1\" class=\"ptolemy.actor.lib.conversions.ComplexToCartesian\">
        <port name=\"input\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"real\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
        <port name=\"imag\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
    </entity>
    <entity name=\"CartesianToComplex2\" class=\"ptolemy.actor.lib.conversions.CartesianToComplex\">
        <port name=\"real\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"imag\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"output\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
    </entity>
</entity>"

test BackwardCompatibility-6.1 {ComplexToCartesian: port name change} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $complexToCartesianMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityComplextToCartesian" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="ComplexToCartesian1" class="ptolemy.actor.lib.conversions.ComplexToCartesian">
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="x" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="y" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
    <entity name="CartesianToComplex2" class="ptolemy.actor.lib.conversions.CartesianToComplex">
        <port name="x" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="y" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
</entity>
}}


######################################################################
####
#

set htvqEncodeMoml  "$header 
<entity name=\"BackwardCompatibilityComplextToCartesian\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"HTVQEncode1\" class=\"ptolemy.domains.sdf.lib.vq.HTVQEncode\">
        <property name=\"codeBook\" class=\"ptolemy.data.expr.Parameter\" value=\"&quot;/ptolemy/domains/sdf/lib/vq/data/usc_hvq_s5.dat&quot;\">
        </property>
        <property name=\"blockCount\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
        </property>
        <property name=\"blockWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"4\">
        </property>
        <property name=\"blockHeight\" class=\"ptolemy.data.expr.Parameter\" value=\"2\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"244.0, 124.0\">
        </property>
        <port name=\"input\" class=\"ptolemy.domains.sdf.kernel.SDFIOPort\">
            <property name=\"input\"/>
            <property name=\"tokenConsumptionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
            </property>
            <property name=\"tokenInitProduction\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenProductionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
        </port>
        <port name=\"output\" class=\"ptolemy.domains.sdf.kernel.SDFIOPort\">
            <property name=\"output\"/>
            <property name=\"tokenConsumptionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenInitProduction\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenProductionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
            </property>
        </port>
    </entity>
</entity>"

test BackwardCompatibility-7.1 {HTVQEncode: Property Class Change} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $htvqEncodeMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityComplextToCartesian" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="HTVQEncode1" class="ptolemy.domains.sdf.lib.vq.HTVQEncode">
        <property name="codeBook" class="ptolemy.data.expr.Parameter" value="&quot;/ptolemy/domains/sdf/lib/vq/data/usc_hvq_s5.dat&quot;">
        </property>
        <property name="blockCount" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="blockWidth" class="ptolemy.data.expr.Parameter" value="4">
        </property>
        <property name="blockHeight" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="244.0, 124.0">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="1">
            </property>
            <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
            <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="1">
            </property>
            <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </port>
    </entity>
</entity>
}}



######################################################################
####
#

set expressionMoml  "$header 
<entity name=\"expressionProperty\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"SDFDirector\" class=\"ptolemy.domains.sdf.kernel.SDFDirector\">
        <property name=\"iterations\" class=\"ptolemy.data.expr.Parameter\" value=\"5\">
        </property>
        <property name=\"vectorizationFactor\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"73.0, 25.0\">
        </property>
    </property>
    <entity name=\"slow\" class=\"ptolemy.actor.lib.Ramp\">
        <property name=\"firingCountLimit\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
        </property>
        <property name=\"init\" class=\"ptolemy.data.expr.Parameter\" value=\"0.0\">
        </property>
        <property name=\"step\" class=\"ptolemy.data.expr.Parameter\" value=\"PI/100.0\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"63.0, 113.0\">
        </property>
        <port name=\"output\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
        <port name=\"trigger\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
            <property name=\"multiport\"/>
        </port>
        <port name=\"step\" class=\"ptolemy.actor.parameters.ParameterPort\">
            <property name=\"input\"/>
        </port>
    </entity>
    <entity name=\"fast\" class=\"ptolemy.actor.lib.Ramp\">
        <property name=\"firingCountLimit\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
        </property>
        <property name=\"init\" class=\"ptolemy.data.expr.Parameter\" value=\"0.0\">
        </property>
        <property name=\"step\" class=\"ptolemy.data.expr.Parameter\" value=\"PI/10.0\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"63.0, 200.0\">
        </property>
        <port name=\"output\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
        <port name=\"trigger\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
            <property name=\"multiport\"/>
        </port>
        <port name=\"step\" class=\"ptolemy.actor.parameters.ParameterPort\">
            <property name=\"input\"/>
        </port>
    </entity>
    <entity name=\"Expression\" class=\"ptolemy.actor.lib.Expression\">
        <property name=\"expression\" class=\"ptolemy.data.expr.Parameter\" value=\"cos(slow) + cos(fast)\">
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"202.0, 191.0\">
        </property>
        <port name=\"output\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"output\"/>
        </port>
        <port name=\"slow\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
        <port name=\"fast\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
        </port>
    </entity>
    <entity name=\"FileWriter\" class=\"ptolemy.actor.lib.FileWriter\">
        <property name=\"filename\" class=\"ptolemy.data.expr.Parameter\" value=\"&quot;&quot;\">
        </property>
        <doc>Write to a file</doc>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"428.0, 205.0\">
        </property>
        <port name=\"input\" class=\"ptolemy.actor.TypedIOPort\">
            <property name=\"input\"/>
            <property name=\"multiport\"/>
        </port>
    </entity>
    <relation name=\"_R0\" class=\"ptolemy.actor.TypedIORelation\">
    </relation>
    <relation name=\"_R1\" class=\"ptolemy.actor.TypedIORelation\">
    </relation>
    <relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">
    </relation>
    <link port=\"slow.output\" relation=\"_R0\"/>
    <link port=\"fast.output\" relation=\"_R1\"/>
    <link port=\"Expression.output\" relation=\"relation\"/>
    <link port=\"Expression.slow\" relation=\"_R0\"/>
    <link port=\"Expression.fast\" relation=\"_R1\"/>
    <link port=\"FileWriter.input\" relation=\"relation\"/>
</entity>"

test BackwardCompatibility-7.2 {Expression: Property Class Change} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $expressionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="expressionProperty" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="SDFDirector" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="5">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="73.0, 25.0">
        </property>
    </property>
    <entity name="slow" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="step" class="ptolemy.actor.parameters.PortParameter" value="PI/100.0">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="63.0, 113.0">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
        <port name="step" class="ptolemy.actor.parameters.ParameterPort">
            <property name="input"/>
        </port>
    </entity>
    <entity name="fast" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="step" class="ptolemy.actor.parameters.PortParameter" value="PI/10.0">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="63.0, 200.0">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
        <port name="step" class="ptolemy.actor.parameters.ParameterPort">
            <property name="input"/>
        </port>
    </entity>
    <entity name="Expression" class="ptolemy.actor.lib.Expression">
        <property name="expression" class="ptolemy.kernel.util.StringAttribute" value="cos(slow) + cos(fast)">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="202.0, 191.0">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="slow" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="fast" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
    </entity>
    <entity name="FileWriter" class="ptolemy.actor.lib.FileWriter">
        <property name="filename" class="ptolemy.data.expr.Parameter" value="&quot;&quot;">
        </property>
        <doc>Write to a file</doc>
        <property name="_location" class="ptolemy.kernel.util.Location" value="428.0, 205.0">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
    <relation name="_R0" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="_R1" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="slow.output" relation="_R0"/>
    <link port="fast.output" relation="_R1"/>
    <link port="Expression.output" relation="relation"/>
    <link port="Expression.slow" relation="_R0"/>
    <link port="Expression.fast" relation="_R1"/>
    <link port="FileWriter.input" relation="relation"/>
</entity>
}}

######################################################################
####
#

set editorFactoryMoml  "$header 
<entity name=\"BackwardCompatibilityEditorFactor\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"lambda\" class=\"ptolemy.data.expr.Parameter\" value=\"25.0\">
        <property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">
        </property>
        <property name=\"lambda\" class=\"ptolemy.vergil.icon.ValueIcon\">
        </property>
        <property name=\"_smallIconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure>
      <svg>
        <text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\">-P-</text>
      </svg>
    </configure>
        </property>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"355.0, 200.0\">
        </property>
    </property>
</entity>"

test BackwardCompatibility-8.1 {Is a parameter, does not have _editorFactory} { 

    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $editorFactoryMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityEditorFactor" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="lambda" class="ptolemy.data.expr.Parameter" value="25.0">
        <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
        </property>
        <property name="lambda" class="ptolemy.kernel.util.Attribute">
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-P-</text>
      </svg>
    </configure>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="355.0, 200.0">
        </property>
        <property name="_editorFactory" class="ptolemy.kernel.util.Attribute">
        </property>
    </property>
</entity>
}}

set annotationMoml  "$header 
<entity name=\"BackwardCompatibilityEditorFactor\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"0:annotation1\" class=\"ptolemy.kernel.util.Attribute\">
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"426.0, 80.0\">
        </property>
        <property name=\"_iconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure>
           <svg>
           <text x=\"20\" style=\"font-size:14; font-family:sanserif; fill:blue\" y=\"20\">This model shows a nonlinear feedback
system that exhibits chaotic behavior.
It is modeled in continuous time. The
CT director uses a sophisticated
ordinary differential equation solver
to execute the model. This particular
model is known as a Lorenz attractor.</text>
           </svg>
           </configure>
        </property>
    </property>
</entity>
"

test BackwardCompatibility-9.1 {annotation named annotation1 without a _hideName} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $annotationMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="BackwardCompatibilityEditorFactor" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="0:annotation1" class="ptolemy.kernel.util.Attribute">
        <property name="_location" class="ptolemy.kernel.util.Location" value="426.0, 80.0">
        </property>
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
           <svg>
           <text x="20" style="font-size:14; font-family:sanserif; fill:blue" y="20">This model shows a nonlinear feedback
system that exhibits chaotic behavior.
It is modeled in continuous time. The
CT director uses a sophisticated
ordinary differential equation solver
to execute the model. This particular
model is known as a Lorenz attractor.</text>
           </svg>
           </configure>
        </property>
        <property name="_hideName" class="ptolemy.data.expr.Parameter">
        </property>
    </property>
</entity>
}}


test BackwardCompatiblity-11.1 {Call toString on all the filters} {
    set filters [$parser getMoMLFilters]
    # listToStrings is defined in  util/testsuite/enums.tcl
    listToStrings $filters
} {{ptolemy.moml.filter.AddEditorFactory: If a parameter has a _location, then
add a VisibleParameterEditorFactory named _editorFactory.
} {ptolemy.moml.filter.AddIcon: Add specialized icons that display the value
of one of the parameters.
The affected actors are:
	ptolemy.actor.lib.Scale
	ptolemy.actor.lib.Const
	ptolemy.actor.lib.TrigFunction
	ptolemy.actor.lib.MathFunction
	ptolemy.actor.lib.Expression
} {ptolemy.moml.filter.ClassChanges: change any class names that have been renamed
Below are original class names followed by the new class names.
	ptolemy.moml.Location	 -> ptolemy.kernel.util.Location} {ptolemy.moml.filter.HideAnnotationNames: If an annotation name ends with
'annotation1', then add _hideName if necessary.
} {ptolemy.moml.filter.PortNameChanges: Update any actor port names that have been
renamed.
Below are the actors that are affected, along
with the old port name and the new port name:	ptolemy.actor.lib.Sleep
		delay	 -> sleepTime
	ptolemy.actor.lib.conversions.ComplexToCartesian
		imag	 -> y
		real	 -> x
	ptolemy.actor.lib.conversions.CartesianToComplex
		imag	 -> y
		real	 -> x
} {ptolemy.moml.filter.PropertyClassChanges: Update any actor port class names
that have been renamed.
Below are the actors that are affected, alongwith the port name
and the new classname:	ptolemy.domains.sdf.lib.vq.ImageUnpartition
		output	 -> ptolemy.actor.TypedIOPort
		input	 -> ptolemy.actor.TypedIOPort
	ptolemy.actor.lib.javasound.AudioReader
		sourceURL	 -> ptolemy.data.expr.Parameter
	ptolemy.actor.lib.Expression
		expression	 -> ptolemy.kernel.util.StringAttribute
	ptolemy.domains.sdf.lib.vq.VQDecode
		output	 -> ptolemy.actor.TypedIOPort
		input	 -> ptolemy.actor.TypedIOPort
	ptolemy.domains.sdf.lib.vq.HTVQEncode
		output	 -> ptolemy.actor.TypedIOPort
		input	 -> ptolemy.actor.TypedIOPort
	ptolemy.domains.sdf.lib.vq.ImagePartition
		output	 -> ptolemy.actor.TypedIOPort
		input	 -> ptolemy.actor.TypedIOPort
} {ptolemy.moml.filter.RemoveGraphicalClasses: Remove or replace classes that are graphical.
This filter is used by the nightly build, and
can be used to run applets so that files like
diva.jar do not need to be downloaded.
The following actors are affected:
ptolemy.vergil.toolbox.AnnotationEditorFactorywill be removed
ptolemy.vergil.basic.NodeControllerFactorywill be removed
ptolemy.vergil.icon.UpdatedValueIconwill be removed
ptolemy.vergil.toolbox.VisibleParameterEditorFactorywill be replaced by ptolemy.kernel.util.Attribute
ptolemy.vergil.icon.AttributeValueIconwill be removed
ptolemy.vergil.icon.BoxedValueIconwill be removed
ptolemy.vergil.icon.ValueIconwill be replaced by ptolemy.kernel.util.Attribute
}}

test BackwardCompatiblity-11.2 {Call BackwardCompatibility.toString} {
    # This is a little strange because when we call 
    # BackwardCompatibility.allFilters(), we add the individual filters 
    # so when we call toString in the test above, we never actually
    # call BackwardCompatibility.toString()

    # Ideally, we would like to make toString() static, but we
    # can't do that because Object.toString() is not static
    set bc [java::new ptolemy.moml.filter.BackwardCompatibility]
    $bc toString
} {This filter contains the following filters:
ptolemy.moml.filter.AddEditorFactory: If a parameter has a _location, then
add a VisibleParameterEditorFactory named _editorFactory.

ptolemy.moml.filter.AddIcon: Add specialized icons that display the value
of one of the parameters.
The affected actors are:
	ptolemy.actor.lib.Scale
	ptolemy.actor.lib.Const
	ptolemy.actor.lib.TrigFunction
	ptolemy.actor.lib.MathFunction
	ptolemy.actor.lib.Expression

ptolemy.moml.filter.ClassChanges: change any class names that have been renamed
Below are original class names followed by the new class names.
	ptolemy.moml.Location	 -> ptolemy.kernel.util.Location
ptolemy.moml.filter.HideAnnotationNames: If an annotation name ends with
'annotation1', then add _hideName if necessary.

ptolemy.moml.filter.PortNameChanges: Update any actor port names that have been
renamed.
Below are the actors that are affected, along
with the old port name and the new port name:	ptolemy.actor.lib.Sleep
		delay	 -> sleepTime
	ptolemy.actor.lib.conversions.ComplexToCartesian
		imag	 -> y
		real	 -> x
	ptolemy.actor.lib.conversions.CartesianToComplex
		imag	 -> y
		real	 -> x

ptolemy.moml.filter.PropertyClassChanges: Update any actor port class names
that have been renamed.
Below are the actors that are affected, alongwith the port name
and the new classname:	ptolemy.domains.sdf.lib.vq.ImageUnpartition
		output	 -> ptolemy.actor.TypedIOPort
		input	 -> ptolemy.actor.TypedIOPort
	ptolemy.actor.lib.javasound.AudioReader
		sourceURL	 -> ptolemy.data.expr.Parameter
	ptolemy.actor.lib.Expression
		expression	 -> ptolemy.kernel.util.StringAttribute
	ptolemy.domains.sdf.lib.vq.VQDecode
		output	 -> ptolemy.actor.TypedIOPort
		input	 -> ptolemy.actor.TypedIOPort
	ptolemy.domains.sdf.lib.vq.HTVQEncode
		output	 -> ptolemy.actor.TypedIOPort
		input	 -> ptolemy.actor.TypedIOPort
	ptolemy.domains.sdf.lib.vq.ImagePartition
		output	 -> ptolemy.actor.TypedIOPort
		input	 -> ptolemy.actor.TypedIOPort

}

test BackwardCompatiblity-20.1 {Try running old models, first check that the makefile created the compat/ directory} { 
    if {! [file exists compat]} {
	error "compat directory does not exist.  This could happen\
		If you do not have access to old Ptolemy II tests"
    } else {
	list 1
    }
} {1} KNOWN_FAILURE

if {[info procs jdkStackTrace] == 1} then {
    source [file join $PTII util testsuite jdkTools.tcl]
}
# createAndExecute a file with a MoMLFilter
proc createAndExecute {file} {
    global KNOWN_FAILED
    if { "$file" == "compat/testAudioReaderAudioPlayer.xml" \
	    || "$file" == "compat/testAudioReader.xml" \
	    || "$file" == "compat/testAudioPlayer.xml" \
	    || "$file" == "compat/testAudioCapture_AudioPlayer.xml" \
	    || "$file" == "compat/testAudioCapture.xml" \
	    || "$file" == "compat/vqtest1.xml" \
	    || "$file" == "compat/DifferentialSystem.xml" \
	    || "$file" == "compat/MaximumEntropySpectrum.xml" \
	    || "$file" == "compat/ArrayAppend.xml" } {
	puts "$file: Skipping Known Failure"
	incr KNOWN_FAILED
	return
    }

    
    #java::new ptolemy.actor.gui.MoMLSimpleApplication $file
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]
    $parser addMoMLFilter \
	    [java::new ptolemy.moml.filter.RemoveGraphicalClasses]

    set namedObj [$parser parseFile $file]
    set toplevel [java::cast ptolemy.actor.CompositeActor $namedObj]

    # DT is a mess, don't bother testing it
    set compositeActor [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$compositeActor getDirector]
    if [java::instanceof \
	    $director ptolemy.domains.dt.kernel.DTDirector] {
	puts "$file: Skipping DT tests, marking as Known Failure"
	incr KNOWN_FAILED
	return
    }

    # Look for comp
    set deepEntityList [$compositeActor deepEntityList]
    for {set i 0} {$i < [$deepEntityList size]} {incr i} {
	set containedActor [$deepEntityList get $i]
	if [java::instanceof $containedActor \
		ptolemy.actor.TypedCompositeActor] {
	    set compositeActor [java::cast ptolemy.actor.CompositeActor \
		    $containedActor]
	    set director [$compositeActor getDirector]
	    if [java::instanceof \
		    $director ptolemy.domains.dt.kernel.DTDirector] {
		puts "$file: Skipping tests with DT inside, marking as Known Failure"
		incr KNOWN_FAILED
		return
	    }
	}
    }


    #set newMoML [$toplevel exportMoML]
    #puts $newMoML

    set workspace [$toplevel workspace]
    set manager [java::new ptolemy.actor.Manager \
	    $workspace "compatibilityChecking"]
    
    $toplevel setManager $manager
    $manager execute

}


# Find all the files in the compat directory


#foreach file [list compat/ComplexToCartesianAndBack.xml compat/testAudioReaderAudioPlayer.xml compat/test1.xml compat/FIR1.xml] {
foreach file [lsort [glob compat/*.xml]] {
    puts "------------------ testing $file"
    test "Auto" "Automatic test in file $file" {
        set application [createAndExecute $file]
        list {}
    } {{}}
    #test "Auto-rerun" "Automatic test rerun in file $file" {
    #	$application rerun
    #	list {}
    #} {{}}
}
#doneTests

