/* A transformer that tried to statically instanceof token expressions.

 Copyright (c) 2001-2002 The Regents of the University of California.
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


package ptolemy.copernicus.java;

import soot.*;
import soot.jimple.*;
import soot.toolkits.scalar.*;
import soot.util.*;
import soot.toolkits.graph.*;
import java.util.*;

import ptolemy.copernicus.kernel.CastAndInstanceofEliminator;
import ptolemy.copernicus.kernel.PtolemyUtilities;

//////////////////////////////////////////////////////////////////////////
//// FieldsForAttributesTransformer
/**
A transformer that removes unnecessary instanceof checks for tokens.
This is similar to CastAndInstanceofEliminator, except here
we use a stronger type inference algorithm that is aware of
Ptolemy token types.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/

public class TokenInstanceofEliminator extends BodyTransformer
{
    private static TokenInstanceofEliminator instance =
    new TokenInstanceofEliminator();
    private TokenInstanceofEliminator() {}

    public static TokenInstanceofEliminator v() { return instance; }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " debug";
    }

    protected void internalTransform(Body b, String phaseName, Map options)
    {
        JimpleBody body = (JimpleBody)b;

        System.out.println("TokenInstanceofEliminator.internalTransform(" +
                body.getMethod() + ", " + phaseName + ")");

        boolean debug = Options.getBoolean(options, "debug");

        eliminateCastsAndInstanceOf(body, phaseName, new HashSet(), debug);
    }

    public static void eliminateCastsAndInstanceOf(Body body,
            String phaseName, Set unsafeLocalSet, boolean debug) {

        // Analyze the types of variables which refer to tokens.
        TokenTypeAnalysis tokenTypes =
            new TokenTypeAnalysis(body.getMethod(),
                    new CompleteUnitGraph(body));

        for (Iterator units = body.getUnits().iterator();
             units.hasNext();) {
            Unit unit = (Unit)units.next();
            for (Iterator boxes = unit.getUseBoxes().iterator();
                 boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();

                if (value instanceof InstanceOfExpr) {
                    // If the operand of the expression is
                    // declared to be of a type that implies
                    // the instanceof is true, then replace
                    // with true.
                    InstanceOfExpr expr = (InstanceOfExpr)value;
                    Type checkType = expr.getCheckType();
                    Value op = expr.getOp();
                    if (!PtolemyUtilities.isTokenType(op.getType())) {
                        continue;
                    }

                    // Use the token type inference to get the actual
                    // type of the argument.
                    ptolemy.data.type.Type type =
                        tokenTypes.getTypeOfBefore((Local)op, unit);

                    Type opType =
                        PtolemyUtilities.getSootTypeForTokenType(type);

                    if(debug) System.out.println("Checking instanceof check: " + expr);
                    CastAndInstanceofEliminator.replaceInstanceofCheck(
                            box, Scene.v().getActiveHierarchy(),
                            checkType, opType, debug);
                }
            }
        }
    }
}
