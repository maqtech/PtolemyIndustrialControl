/* The graph frame for interface automata.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy.fsm;

import diva.graph.GraphPane;

import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;

//////////////////////////////////////////////////////////////////////////
//// InterfaceAutomatonGraphFrame
/**
This is a graph editor frame for ptolemy InterfaceAutomaton models.
Given a composite entity and a tableau, it creates an editor and populates
the menus and toolbar.  This overrides the base class to associate with the
editor an instance of InterfaceAutomatonGraphController.

@author  Steve Neuendorffer, Yuhong Xiong
@contributor Edward A. Lee
@version $Id$
*/
public class InterfaceAutomatonGraphFrame extends FSMGraphFrame {

    /** Construct a frame associated with the specified interface automaton
     *  model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public InterfaceAutomatonGraphFrame(CompositeEntity entity,
                                        Tableau tableau) {
	super(entity, tableau);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be careful to not reference
     *  local variables that may not have yet been created.
     */
    protected GraphPane _createGraphPane() {
	_controller = new InterfaceAutomatonGraphController(this, _directory);
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);
	final FSMGraphModel graphModel = new FSMGraphModel(getModel());
	return new GraphPane(_controller, graphModel);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // private InterfaceAutomatonGraphController _controller;
}
