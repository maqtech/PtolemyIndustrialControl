/* RTMaude Code generator helper class for the FSMActor class.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.domains.fsm.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.codegen.rtmaude.kernel.util.ListTerm;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////Director

/**
* Generate RTMaude code for a FSMActor in DE domain.
*
* @see ptolemy.domains.fsm.kernel.FSMActor
* @author Kyungmin Bae
* @version $Id: FSMActor.java 53821 2009-04-12 19:12:45Z cxh $
* @Pt.ProposedRating Red (kquine)
*
*/
public class FSMActor extends Entity {
    
    public FSMActor(ptolemy.domains.fsm.kernel.FSMActor component) {
        super(component);
    }
    
    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        ptolemy.domains.fsm.kernel.FSMActor fa = 
            (ptolemy.domains.fsm.kernel.FSMActor) getComponent();
        
        String initstate = fa.getInitialState().getName();
        
        atts.put("currState", "'" + initstate);
        atts.put("initState", "'" + initstate);
        
        ArrayList transitions = new ArrayList();
        for(State s : (List<State>)fa.entityList(State.class))
            transitions.addAll(s.outgoingPort.linkedRelationList());

        atts.put("transitions",
            new ListTerm<Transition>("emptyTransitionSet", " ;" + _eol, transitions) {
                public String item(Transition t) throws IllegalActionException {
                    return ((RTMaudeAdaptor) _getHelper(t)).generateTermCode();
                }
            }.generateCode()
        );

        return atts;
    }
    
}
