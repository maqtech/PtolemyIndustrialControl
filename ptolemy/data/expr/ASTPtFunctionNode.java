/* ASTPtFunctionNode represent function nodes in the parse tree

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.*;
import java.lang.reflect.*;

//////////////////////////////////////////////////////////////////////////
//// ASTPtFunctionNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents function nodes in
the parse tree.
<p>
Currently the functions supported are precisely those
in the java.lang.Math package. However, it is relatively straightforward
to extend this node to allow references to other functions. This
provides a strightforward mechanism to extend the functionality of
the parser by adding extra functions that the parser can call. One
example might be tcl(...) which would pass the string to a tcl
interpreter to evaluate and return the result. This is also the mechanism
by which files can be read into a parameter, probably via a readFile(...)
method.
<p>
FIXME: need to define a basic set of functions, and inplement them.
<p>
@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtFunctionNode extends ASTPtRootNode {
    protected String funcName;

    protected ptolemy.data.Token _resolveNode() 
            throws IllegalArgumentException {
        int args = jjtGetNumChildren();
        Class[] argTypes = new Class[args];
        Object[] argValues = new Object[args];
        // Note: Java makes a dintinction between the class objects
        // for double & Double...
        try {
            for (int i = 0; i<args; i++) {
                if (childTokens[i] instanceof DoubleToken) {
                    argValues[i] = new Double(((ScalarToken)childTokens[i]).doubleValue());
                    argTypes[i] = Double.TYPE;
                } else if (childTokens[i] instanceof IntToken) {
                    argValues[i] = new Integer(((ScalarToken)childTokens[i]).intValue());
                    argTypes[i] = Integer.TYPE;
                } else if (childTokens[i] instanceof LongToken) {
                    argValues[i] = new Long(((ScalarToken)childTokens[i]).longValue());
                    argTypes[i] = Long.TYPE;
                } else {
                    String str = "invalid argument type, valid types are: ";
                    str = str + "int, long, double, complex and String";
                    throw new IllegalArgumentException(str);
                }
            }
            // Currently this method only looks in java.lang.Math for
            // the invoked function, but this will be exttended
            Class destClass = Class.forName("java.lang.Math");
            Method m = destClass.getMethod(funcName, argTypes);
            Object result = m.invoke(destClass, argValues);
            if (result instanceof Double) {
                return new DoubleToken(((Double)result).doubleValue());
            } else if (result instanceof Integer) {
                return new IntToken(((Integer)result).intValue());
                /*  } else if (result instanceof Float) {
                    return new ptolemy.data.FloatToken(((Float)result).floatValue()); */
            } else if (result instanceof Long) {
                return new LongToken(((Long)result).longValue());
            } else  {
                String str = "result of  function not of a supported type, ";
                str = str + "ie float, int, double, or long";
                throw new IllegalArgumentException(str);
            }
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i<args; i++) {
                if (i == 0) {
                    sb.append(argValues[i].toString());
                } else {
                    sb.append(", " + argValues[i].toString());
                }
            }
            String str = "Function " + funcName + "(" + sb;
            str = str + ") cannot be executed with given arguments";
            throw new IllegalArgumentException(str + ": " + ex.getMessage());
        }
    }


    public ASTPtFunctionNode(int id) {
        super(id);
    }

    public ASTPtFunctionNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtFunctionNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtFunctionNode(p, id);
    }
}
