/* An adapter that allows a body transformer to be used as a scene transformer.

 Copyright (c) 2001 The Regents of the University of California.
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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.kernel;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.data.*;
import ptolemy.data.type.Typeable;
import ptolemy.data.expr.Variable;
import ptolemy.copernicus.kernel.SootUtilities;


/**
An adapter that turns a body transformer into a scene transformer.
This applies the transformer specified in the constructor to
all of the bodies in the scene.
*/
public class TransformerAdapter extends SceneTransformer {
    /** Construct a new transformer
     */
    public TransformerAdapter(BodyTransformer transformer) {
        _transformer = transformer;
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("TransformerAdapter.internalTransform("
                + phaseName + ", " + options + ")");

        Iterator classes = Scene.v().getApplicationClasses().iterator();
        while(classes.hasNext()) {
            SootClass theClass = (SootClass)classes.next();
            Iterator methods = theClass.getMethods().iterator();
            while(methods.hasNext()) {
                SootMethod m = (SootMethod) methods.next();
                if(!m.isConcrete())
                    continue;

                JimpleBody body = (JimpleBody) m.retrieveActiveBody();

                // FIXME: pass in the options.
                // Currently this is not possible because the
                // internalTransform method is protected.
                _transformer.transform(body, phaseName, "");
            }
        }
    }

    private BodyTransformer _transformer;
}














