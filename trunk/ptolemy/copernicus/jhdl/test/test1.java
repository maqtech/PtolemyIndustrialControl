/* A type polymorphic FIR filter.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl.test;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.FixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.domains.sdf.kernel.SDFIOPort;

public class test1 extends TypedAtomicActor {

    public test1(CompositeEntity container, String name)
		throws NameDuplicationException, IllegalActionException  {
        super(container, name);
		
        input = new SDFIOPort(this, "input", true, false);
        output = new SDFIOPort(this, "output", false, true);
        output.setTypeAtLeast(input);
		
    }

//      ///////////////////////////////////////////////////////////////////
//      ////                         public variables                  ////

    public SDFIOPort input;

    /** The output port. By default, the type of this output is constrained
     *  to be at least that of the input.
     */
    public SDFIOPort output;

    public int e;

    public int f() { return 3; }
    public int g(int a) { return a+4; }

    /**
     * ControlFlow
     * - none
     * DataFlow
     * - UnopExpr 
     **/
    public void method1(int b) {
	int a = -b;
    }
    /**
     * ControlFlow
     * - none
     * DataFlow
     * - UnopExpr 
     **/
    public void method2(int b) {
	int a = (b + 5) * 4 + 3;
	a = -a;	    
    }
    /** fieldref **/
    public void method3() {
	test1 b=null;
	int a = b.e + 4;
	this.e = a;
    }
    /** invoke statement * expr **/
    public void method4(int b) {
	g(b+3);
	int a=g(b+2);
    }

    /** If-Else Control Flow (no Boolean expressions) **/
    public void method10(int b) {
	int d=0;
	if (b < 4) {
	    d = 2;
	    if (b==3) {
		d += b;
	    }
	} else {
	    d = b;
	}
    }

    /** Boolean expression Control Flow **/
    public void method11(int b, int c) {
	int d=0;
	if ((b < 4 || b > 10) && c == 10 || c < 3) {
	    d += 2;
	} else {
	    if (c > 10)
		d = b+c;
	    d += 4;
	}
    }

    /** Boolean expression Control Flow **/
    public void method12(int b, int c) {
	int d=0;
	boolean z = b<4 || b > 10 && c == 10;
	boolean y = b<4 && b > 10 || c == 10;
    }
    public int method13(int b) {
	if ((b>2) || b++ < 5)
	    return b++;
	return 4;
    }

    /** Boolean expression Control Flow **/
    public void method14(int b, int c) {
	int d=0;
	if ((b < 4 || b > c) && c == 10) {
	    d = 2;
	} else {
	    d = 4;	    
	}
	d = 2;
	if ((b < 4 || b > 10) && c == 10 || (c < 3 && b > 5)) {
	    d = 2;
	} else {
	    d = 4;	    
	}
	d = 3;
	if ((b < 4 || b > 10) && c == 10 || (c < 3 && b > 5) ||
	    (b > 4 || b < 6)) {
	    d = 2;
	} else {
	    d = 4;	    
	}
    }
    public void method15(int b, int c) {
	int d=0;
	if (b < 4 || b > 10) {
	    d = 2;
	} else {
	    d = 4;	    
	}
    }
    public void method16(int b, int c) {
	int d=0;
	if (b < 4 || b > 10) {
	    d = 2;
	    if (b == 9)
		d = 5;
	} else {
	    d = 4;	    
	}
    }

    /** If-Else Control Flow (no Boolean expressions) **/
    public void method17(int b) {
	int d=0;
	if (b < 4) {
	    if (b==3) {
		d += b;
	    } else 
		d -= b;
	} else {
	    if (b > 7)
		d -= b;
	    else
		d += b;
	}
    }

    /** tableswitch expression Control Flow **/
    public void method20(int b) {
	int d=0;
	switch(b) {
	case 0:
	    d += 1;
	    break;
	case 1:
	    d += 2;
	    break;
	case 2:
	    d += 3;
	    break;
	default:
	    d += d;
	    break;
	}
    }

    /** tableswitch expression Control Flow (non-breaks) **/
    public void method21(int b) {
	int d=0;
	switch(b) {
	case 0:
	    d += 1;
	case 1:
	    d += 2;
	case 2:
	    d += 3;
	default:
	    d += d;
	    break;
	}
    }

    /** look-up switch expression Control Flow (breaks) **/
    public void method22(int b) {
	int d=0;
	switch(b) {
	case 2:
	    d += 1;
	    break;
	case 4:
	    d += 2;
	    break;
	case 1:
	    d += 3;
	    break;
	default:
	    d += d;
	    break;
	}
    }

    /** tableswitch expression Control Flow (non-breaks) **/
    public void method23(int b) {
	int d=0;
	switch(b) {
	case 0:
	    d += 1;
	case 1:
	    d += 2;
	    break;
	case 2:
	    d += 3;
	    break;
	default:
	    d += d;
	    break;
	}
    }

    /** For loop **/
    public void method33(int b) {
	int d=0;
	for (int i=0;i<b;i++)
	    d++;
    }

    /** Used to test serial combining (two forks) **/
    public int method34(int a) {
	int d=0;
	if (a > 5) {
	    d = a * 2 + 3 + d;
	    d += a;
	} else {
	    d += a;
	    d = d * 2 + a;
	}
	d = d * 2 + a;
	d += a + 5;
	return d;
    }

    /** Used to test serial combining (one fork) **/
    public int method35(int a) {
	int d=0;
	if (a > 5) {
	    d = a * 2 + 3 + d;
	    d += a;
	}
	d = d * 2 + a;
	d += a + 5;
	return d;
    }

    /** Used to test serial combining (one fork) **/
    public int method36(int a) {
	int b=1;
	int c=2;
	int d=3;
	// b defined by both branches
	// c defined by true branch
	// d defined by false branch
	if (a > 5) {
	    b += a;
	    c = a * 2 + d;
	} else {
	    b -= a;
	    d = c * 2 + a;
	}
	return b;
    }

    /* Simple method with one basic block and one operation */
    public int hwgen1(int a, int b) {
	int c = a + b;
	return c;
    }

    /* Simple method with one basic block and several operations */
    public int hwgen2(int a, int b, int d) {
	int c = a * b + d - a;
	return c;
    }

    public boolean hwgen3(boolean a, boolean b, boolean c) {
	return a & b | c;
    }

    public void fire() throws IllegalActionException {
	int a,b,c,d;
	test1 t1=null;

	a=3;
	b=a*4;
	
	t1.e = b;

	a=2;
	c=a+5+t1.e+t1.e;
	d=b-c + f();
	
	if (d > 8){
	    d=a*a+e;
	}
    }
    
}
