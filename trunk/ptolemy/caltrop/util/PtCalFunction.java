/*
@Copyright (c) 2003 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.caltrop.util;

import caltrop.interpreter.Function;
import ptolemy.data.FunctionToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// PtCalFunction
/**
This class is an adapter for {@link caltrop.interpreter.Function
Function} objects that provides the Ptolemy II
<tt>FunctionToken.Function</tt> interface. It allows them to be
seamlessly used with Ptolemy II-generated function objects.

@author J�rn W. Janneck <janneck@eecs.berkeley.edu>
@version $Id$
@since Ptolemy II 3.1
@see caltrop.interpreter.Context
@see caltrop.interpreter.Function
*/
public class PtCalFunction implements FunctionToken.Function {

    public Token apply(List list) throws IllegalActionException {
        // TODO: should we allow non-token returns and tokenize them?
        return (Token) f.apply(list.toArray());
    }

    public int getNumberOfArguments() {
        return f.arity();
    }

    public boolean isCongruent(FunctionToken.Function function) {
        return false;
    }

    public PtCalFunction(Function f) {
        this.f = f;
    }

    private Function f;
}
