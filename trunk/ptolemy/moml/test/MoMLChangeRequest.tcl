# Tests for the MoMLChangeRequest class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="2"/>
    </property>
</model>
}

#----------------------------------------------------------------------
test MoMLChangeRequest-1.1 {Test adding an entity} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <model name=".top">
            <entity name="const" class="ptolemy.actor.lib.Const"/>
        </model>
    }]
    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </property>
    <entity name="const" class="ptolemy.actor.lib.Const">
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
</model>
}

#----------------------------------------------------------------------
test MoMLChangeRequest-1.2 {Test adding another entity} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <model name=".top">
            <entity name="rec" class="ptolemy.actor.lib.Recorder"/>
        </model>
    }]
    $manager requestChange $change
    set recorder [java::cast ptolemy.actor.lib.Recorder \
            [$toplevel getEntity "rec"]]
    $recorder getFullName
} {.top.rec}

#----------------------------------------------------------------------
test MoMLChangeRequest-1.3 {Test adding a relation} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <model name=".top">
            <relation name="r" class="ptolemy.actor.TypedIORelation"/>
        </model>
    }]
    $manager requestChange $change
    set r [$toplevel getRelation "r"]
    $r getFullName
} {.top.r}

#----------------------------------------------------------------------
test MoMLChangeRequest-1.4 {Test adding a pair of links} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <model name=".top">
            <link relation="r" port="const.output"/>
            <link relation="r" port="rec.input"/>
        </model>
    }]
    $manager requestChange $change
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {1 1}

#----------------------------------------------------------------------
test MoMLChangeRequest-1.5 {Test changing a parameter} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <model name=".top">
            <entity name="const">
                <property name="value" value="2"/>
            </entity>
        </model>
    }]
    $manager initialize
    $manager iterate
    $manager requestChange $change
    $manager iterate
    $manager wrapup
    enumToTokenValues [$recorder getRecord 0]
} {1 2}

#----------------------------------------------------------------------
test MoMLChangeRequest-1.5 {Test deleting an entity} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <model name=".top">
            <deleteEntity name="const"/>
        </model>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </property>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
        <property name="capacity" class="ptolemy.data.expr.Parameter" value="-1">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="rec.input" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLChangeRequest-1.6 {Test deleting a relation} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <model name=".top">
            <deleteRelation name="r"/>
        </model>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </property>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
        <property name="capacity" class="ptolemy.data.expr.Parameter" value="-1">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
</model>
}

#----------------------------------------------------------------------
test MoMLChangeRequest-1.6 {Test deleting a port, using a new parser and context} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <deletePort name="rec.input"/>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </property>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
        <property name="capacity" class="ptolemy.data.expr.Parameter" value="-1">
        </property>
    </entity>
</model>
}

#----------------------------------------------------------------------
test MoMLChangeRequest-1.7 {Test deleting a property using a lower context} {
    set rec [$toplevel getEntity "rec"]
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $rec {
        <deleteProperty name="capacity"/>
    }]
    $manager requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </property>
    <entity name="rec" class="ptolemy.actor.lib.Recorder">
    </entity>
</model>
}

# FIXME:  delete links

#----------------------------------------------------------------------
#----------------------------------------------------------------------
#----------------------------------------------------------------------

# Test propagation of changes from a class to instances.

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
   <class name="gen" extends="ptolemy.kernel.CompositeEntity">
   </class>
   <entity name="der" class=".top.gen"/>
</model>
}

test MoMLChangeRequest-2.1 {Setup} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $baseModel]]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="gen" extends="ptolemy.kernel.CompositeEntity">
    </class>
    <entity name="der" class=".top.gen">
    </entity>
</model>
}

test MoMLChangeRequest-2.2 {Test propagation} {
    set gen [$toplevel getEntity "gen"]
    # NOTE: Have to give the context as "gen" for the changes to
    # propogate to its clones.
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $gen {
        <entity name="new" class="ptolemy.kernel.ComponentEntity"/>
    }]
    # NOTE: Request is filled immediately in the toplevel context.
    $toplevel requestChange $change
    # NOTE: exportMoML won't give a full description.
    $toplevel description
} {ptolemy.kernel.CompositeEntity {.top} attributes {
    {ptolemy.kernel.util.NonpersistentProcessedString {.top.iconDescription} attributes {
    }}
    {ptolemy.moml.ParserAttribute {.top._parser} attributes {
    }}
} ports {
} entities {
    {ptolemy.kernel.CompositeEntity {.top.gen} attributes {
        {ptolemy.kernel.util.NonpersistentProcessedString {.top.gen.iconDescription} attributes {
        }}
    } ports {
    } entities {
        {ptolemy.kernel.ComponentEntity {.top.gen.new} attributes {
            {ptolemy.kernel.util.NonpersistentProcessedString {.top.gen.new.iconDescription} attributes {
            }}
        } ports {
        }}
    } relations {
    }}
    {ptolemy.kernel.CompositeEntity {.top.der} attributes {
        {ptolemy.kernel.util.NonpersistentProcessedString {.top.der.iconDescription} attributes {
        }}
    } ports {
    } entities {
        {ptolemy.kernel.ComponentEntity {.top.der.new} attributes {
            {ptolemy.kernel.util.NonpersistentProcessedString {.top.der.new.iconDescription} attributes {
            }}
        } ports {
        }}
    } relations {
    }}
} relations {
}}

#----------------------------------------------------------------------
#----------------------------------------------------------------------
#----------------------------------------------------------------------

# Test propagation of changes from a class to class to instances.

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
   <class name="gen" extends="ptolemy.kernel.CompositeEntity">
   </class>
   <class name="intClass" extends=".top.gen"/>
   <entity name="der" class=".top.intClass"/>
</model>
}

test MoMLChangeRequest-3.1 {Setup} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $baseModel]]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="gen" extends="ptolemy.kernel.CompositeEntity">
    </class>
    <class name="intClass" extends=".top.gen">
    </class>
    <entity name="der" class=".top.intClass">
    </entity>
</model>
}

test MoMLChangeRequest-3.2 {Test propagation} {
    set gen [$toplevel getEntity "gen"]
    # NOTE: Have to give the context as "gen" for the changes to
    # propogate to its clones.
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $gen {
        <entity name="new" class="ptolemy.kernel.ComponentEntity"/>
    }]
    # NOTE: Request is filled immediately in the toplevel context.
    $toplevel requestChange $change
    # NOTE: exportMoML won't give a full description.
    $toplevel description
} {ptolemy.kernel.CompositeEntity {.top} attributes {
    {ptolemy.kernel.util.NonpersistentProcessedString {.top.iconDescription} attributes {
    }}
    {ptolemy.moml.ParserAttribute {.top._parser} attributes {
    }}
} ports {
} entities {
    {ptolemy.kernel.CompositeEntity {.top.gen} attributes {
        {ptolemy.kernel.util.NonpersistentProcessedString {.top.gen.iconDescription} attributes {
        }}
    } ports {
    } entities {
        {ptolemy.kernel.ComponentEntity {.top.gen.new} attributes {
            {ptolemy.kernel.util.NonpersistentProcessedString {.top.gen.new.iconDescription} attributes {
            }}
        } ports {
        }}
    } relations {
    }}
    {ptolemy.kernel.CompositeEntity {.top.intClass} attributes {
        {ptolemy.kernel.util.NonpersistentProcessedString {.top.intClass.iconDescription} attributes {
        }}
    } ports {
    } entities {
        {ptolemy.kernel.ComponentEntity {.top.intClass.new} attributes {
            {ptolemy.kernel.util.NonpersistentProcessedString {.top.intClass.new.iconDescription} attributes {
            }}
        } ports {
        }}
    } relations {
    }}
    {ptolemy.kernel.CompositeEntity {.top.der} attributes {
        {ptolemy.kernel.util.NonpersistentProcessedString {.top.der.iconDescription} attributes {
        }}
    } ports {
    } entities {
        {ptolemy.kernel.ComponentEntity {.top.der.new} attributes {
            {ptolemy.kernel.util.NonpersistentProcessedString {.top.der.new.iconDescription} attributes {
            }}
        } ports {
        }}
    } relations {
    }}
} relations {
}}
