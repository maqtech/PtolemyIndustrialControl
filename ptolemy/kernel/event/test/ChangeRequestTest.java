/* Test for ChangeRequest.

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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.kernel.event.test;

import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ChangeRequestTest
/** 
Test for ChangeRequest.

@author  Edward A. Lee
@version $Id$
@see ptolemy.kernel.event.ChangeRequest
*/
public class ChangeRequestTest {

    /** Constructor.
     */	
    public ChangeRequestTest()
             throws IllegalActionException, NameDuplicationException {
        TypedCompositeActor _top = new TypedCompositeActor();
        _top.setName("top");
        _manager = new Manager();
        SDFDirector director = new SDFDirector();
        _top.setDirector(director);
        _top.setManager(_manager);

        _const = new Const(_top, "const");
        _rec = new Recorder(_top, "rec");
        _top.connect(_const.output, _rec.input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Finish a run.  Return the results.
     */	
    public Enumeration finish() throws KernelException {
        _manager.iterate();
        _manager.wrapup();
        return _rec.getRecord(0);
    }

    /** Mutate.
     */
    public void mutate() throws ChangeFailedException {
        // Create an anonymous inner class
        ChangeRequest change = new ChangeRequest(_top, "test") {
            public void execute() throws ChangeFailedException {
                try {
                    _const.value.setToken(new DoubleToken(2.0));
                } catch (IllegalActionException ex) {
                    throw new ChangeFailedException(this, ex);
                }
            }
        };
        _manager.requestChange(change);
    }

    /** Start a run.
     */	
    public void start() throws KernelException {
        _manager.initialize();
        _manager.iterate();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Manager _manager;
    private Recorder _rec;
    private Const _const;
    private TypedCompositeActor _top;
}
