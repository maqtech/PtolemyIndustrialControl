/** An inequality over a CPO.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

*/

package ptolemy.graph;

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// Inequality
/**
An inequality over a CPO.
Each inequality consists of two <code>InequalityTerms</code>, the lesser
term and the greater term. The relation between them is <i>less than or
equal to</i>.  In addition, an inequality keeps a list of variables in it.
The variables are <code>Inequalityterms</code> that consist of a single
variable.

@author Yuhong Xiong
$Id$
@see InequalityTerm
*/

public class Inequality {

    /** Construct an inequality.
     *  @param lesserTerm an <code>InequalityTerm</code> that is less than or
     *   equal to the second argument.
     *  @param greaterTerm an <code>InequalityTerm</code> that is greater than
     *   or equal to the first argument.
     *  @exception IllegalArgumentException <code>lesserTerm</code> or
     *   <code>greaterTerm</code> is <code>null</code>.
     */
    public Inequality(InequalityTerm lesserTerm, InequalityTerm greaterTerm) {
	if (lesserTerm == null || greaterTerm == null) {
	    throw new IllegalArgumentException("Inequality.Inequality: " +
                    "lesserTerm of greaterTerm is null.");
	}

        _lesserTerm = lesserTerm;
        _greaterTerm = greaterTerm;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified <code>InequalityTerm</code> to the list of
     *  variables in this inequality. The specified
     *  <code>InequalityTerm</code> should be a single variable, i.e.,
     *  it should return <code>true</code> in <code>settable</code>.
     *  @param variable an <code>InequalityTerm</code> representing
     *   a variable in this inequality.
     *  @exception IllegalArgumentException the specified
     *   <code>InequalityTerm</code> is not a variable, or is
     *   <code>null</code>.
     */
    public void addVariable(InequalityTerm variable) {
	if ( !variable.isSettable()) {
	    throw new IllegalArgumentException("Inequality.addVariable: " +
                    "the specified InequalityTerm is not a variable.");
	}
	if (variable == null) {
	    throw new IllegalArgumentException("Inequality.addVariable: " +
                    "the specified InequalityTerm is null.");
	}
	_variables.addElement(variable);
    }

    /** Return the greater term of this inequality.
     *  @return an <code>InequalityTerm</code>
     */
    public InequalityTerm getGreaterTerm() {
        return _greaterTerm;
    }
 
    /** Return the lesser term of this inequality.
     *  @return an <code>InequalityTerm</code>
     */
    public InequalityTerm getLesserTerm() {
        return _lesserTerm;
    }

    /** Test if this inequality is satisfied with the current value
     *  of variables.
     *  @param cpo a CPO over which this inequality is defined.
     *  @return <code>true</code> if this inequality is satisfied;
     *   <code>false</code> otherwise.
     */
    public boolean satisfied(CPO cpo) {
        int result = cpo.compare(_lesserTerm.getValue(),
                _greaterTerm.getValue());
        return (result == CPO.STRICT_LESS || result == CPO.EQUAL);
    }
    
    /** Return all the variables in this inequality. The variables are
     *  the ones added by the <code>addVariable</code>.
     *  @return an array of <code>InequalityTerms</code> that are 
     *   variables in this inequality.
     */
    public InequalityTerm[] variables() {
	// note: can't use toArray() since return type is InequalityTerm[]
	InequalityTerm[] result = new InequalityTerm[_variables.size()];
	for (int i = 0; i < _variables.size(); i++) {
	    result[i] = (InequalityTerm)_variables.elementAt(i);
	}
	return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private InequalityTerm _lesserTerm = null;
    private InequalityTerm _greaterTerm = null;

    // list of InequalityTerms that are variables. initialCapacity is 2.
    private Vector _variables = new Vector(2);
}

