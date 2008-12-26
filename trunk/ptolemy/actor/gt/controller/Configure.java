/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import java.awt.Frame;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.data.ArrayToken;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Configure

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Configure extends InitializableGTEvent {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public Configure(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        List<Settable> parameters = attributeList(Settable.class);
        for (Settable parameter : parameters) {
            _ignoredParameters.add(parameter.getName());
        }
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Configure newObject = (Configure) super.clone(workspace);
        newObject._ignoredParameters = new HashSet<String>(_ignoredParameters);
        newObject._oldValues = new HashMap<Settable, String>();
        return newObject;
    }

    public RefiringData fire(ArrayToken arguments)
            throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        _executeChangeRequests();

        Configurer configurer = new Configurer();
        ComponentDialog dialog = new ComponentDialog((Frame) null, getName(),
                configurer, new String[]{"Set", "Unset"});
        if (dialog.buttonPressed().equals("Set")) {
            _executeChangeRequests();
        } else {
            configurer.restore(false);
        }

        return data;
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        List<Settable> parameters = attributeList(Settable.class);
        _oldValues.clear();
        for (Settable parameter : parameters) {
            if (_isVisible(parameter)) {
                _oldValues.put(parameter, parameter.getExpression());
            }
        }
    }

    public void wrapup() throws IllegalActionException {
        for (Map.Entry<Settable, String> entry : _oldValues.entrySet()) {
            entry.getKey().setExpression(entry.getValue());
        }
        _oldValues.clear();
        super.wrapup();
    }

    private void _executeChangeRequests() {
        List<Settable> parameters = attributeList(Settable.class);
        for (Settable parameter : parameters) {
            if (_isVisible(parameter)) {
                ((NamedObj) parameter).executeChangeRequests();
            }
        }
    }

    private boolean _isVisible(Settable settable) {
        return !_ignoredParameters.contains(settable.getName()) &&
                Configurer.isVisible(this, settable);
    }

    private Set<String> _ignoredParameters = new HashSet<String>();

    private Map<Settable, String> _oldValues = new HashMap<Settable, String>();

    private class Configurer extends ptolemy.actor.gui.Configurer {

        /** Construct a configurer for the specified object.  This stores
         *  the current values of any Settable attributes of the given object,
         *  and then defers to any editor pane factories contained by
         *  the given object to populate this panel with widgets that
         *  edit the attributes of the given object.  If there are no
         *  editor pane factories, then a default editor pane is created.
         */
        public Configurer() {
            super(Configure.this);
        }

        /** Return true if the given settable should be visible in this
         *  configurer panel for the specified target. Any settable with
         *  visibility FULL or NOT_EDITABLE will be visible.  If the target
         *  contains an attribute named "_expertMode", then any
         *  attribute with visibility EXPERT will also be visible.
         *  @param settable The object whose visibility is returned.
         *  @return True if settable is FULL or NOT_EDITABLE or True
         *  if the target has an _expertMode attribute and the settable
         *  is EXPERT.  Otherwise, return false.
         */
        public boolean isVisible(Settable settable) {
            return _isVisible(settable);
        }
    }
}
