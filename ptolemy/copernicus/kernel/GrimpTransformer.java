/* A transformer that creates a grimp body.

 Copyright (c) 2003-2004 The Regents of the University of California.
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

import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.grimp.*;
import soot.jimple.FieldRef;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


//////////////////////////////////////////////////////////////////////////
//// GrimpTransformer
/**
A Transformer that creates a GrimpBody from the active body.
GrimpBodies are better to create class files from because they have
constructor bytecode that look like avac's constructors

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 4.0

*/
public class GrimpTransformer extends SceneTransformer {

    /** Return an instance of this transformer that will operate on
     *  the given model.
     */
    public static GrimpTransformer v() {
        return _instance;
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("GrimpTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        // Loop through all the methods and kill all the used fields.
        for (Iterator i = Scene.v().getApplicationClasses().iterator();
             i.hasNext();) {
            SootClass entityClass = (SootClass)i.next();
            for (Iterator methods = entityClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                if (method.isConcrete()) {
                    method.setActiveBody(
                            Grimp.v().newBody(method.retrieveActiveBody(), "gb"));
                }
            }
        }
    }
    private static GrimpTransformer _instance = new GrimpTransformer();
}














