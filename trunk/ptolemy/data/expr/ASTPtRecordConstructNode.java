/* ASTPtRecordConstructNode represents record construction in the parse tree.

 Copyright (c) 2000 The Regents of the University of California.
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

@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : December 2000

*/

package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.math.Complex;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

import java.util.LinkedList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ASTPtRecordConstructNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents record construction using
the following syntax: <code>{foo = "abc", bar = 1}</code>. The result of
parsing and evaluating this expression is a record token with two fields:
a field <i>foo</i> containing a StringToken of value "abc", and a field
<i>bar</i> containing a IntToken of value 1.

@author Xiaojun Liu
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtRecordConstructNode extends ASTPtRootNode {

    public ASTPtRecordConstructNode(int id) {
        super(id);
    }

    public ASTPtRecordConstructNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtRecordConstructNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtRecordConstructNode(p, id);
    }

    protected ptolemy.data.Token _resolveNode() throws IllegalActionException {
	int numFields = _fieldNames.size();
	if (numFields != jjtGetNumChildren()) {
	    throw new InternalErrorException("The number of labels and values "
                    + "does not match in parsing a record expression.");
	}
	String[] labels = new String[numFields];
	int i = 0;
	Iterator fields = _fieldNames.iterator();
	while (fields.hasNext()) {
	    labels[i] = (String)fields.next();
	    ++i;
	}
	return new RecordToken(labels, _childTokens);
    }

    /** The list of field names for the record.
     */
    protected LinkedList _fieldNames = new LinkedList();

}
