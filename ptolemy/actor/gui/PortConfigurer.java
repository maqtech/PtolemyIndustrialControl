/* A GUI widget for configuring ports.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.IOPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.moml.MoMLChangeRequest;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.BoxLayout;

//////////////////////////////////////////////////////////////////////////
//// PortConfigurer
/**
This class is an editor to configure the ports of an object.
It supports setting their input, output, and multiport properties,
and adding and removing ports.  Only ports that extend the IOPort
class are listed, since more primitive ports cannot be configured
in this way.

@see Configurer
@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public class PortConfigurer extends Query implements QueryListener {

    /** Construct a port configurer for the specified entity.
     *  @param object The entity to configure.
     */
    public PortConfigurer(Entity object) {
        super();
	this.addQueryListener(this);
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	setTextWidth(25);

        _object = object;

	Iterator ports = _object.portList().iterator();
        while (ports.hasNext()) {
            Object candidate = ports.next();
            if (candidate instanceof IOPort) {
                IOPort port = (IOPort)candidate;
                Set optionsDefault = new HashSet();
                if (port.isInput()) optionsDefault.add("input");
                if (port.isOutput()) optionsDefault.add("output");
                if (port.isMultiport()) optionsDefault.add("multiport");

                addSelectButtons(port.getName(), port.getName(),
                        _optionsArray, optionsDefault);
            }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply the changes by configuring the ports that have changed.
     */
    public void apply() {
        Iterator names = _changed.iterator();
        while (names.hasNext()) {
            String name = (String)names.next();
            String value = stringValue(name);

            // First, parse the value, which may be a comma-separated list.
            Set selectedValues = new HashSet();
            StringTokenizer tokenizer = new StringTokenizer(value, ",");
            while (tokenizer.hasMoreTokens()) {
                selectedValues.add(tokenizer.nextToken().trim());
            }
            // Next, configure the ports.
            Port port = _object.getPort(name);
            if (port instanceof IOPort) {
                // The context for the MoML should be the first container
                // above this port in the hierarchy that defers its
                // MoML definition, or the immediate parent if there is none.
                NamedObj parent = MoMLChangeRequest.getDeferredToParent(port);
                if (parent == null) {
                    parent = (NamedObj)port.getContainer();
                }
		StringBuffer moml = new StringBuffer("<port name=\"");
                moml.append(port.getName(parent));
                moml.append("\">");

                if (selectedValues.contains("input")) {
                    moml.append("<property name=\"input\"/>");
                } else {
                    moml.append("<property name=\"input\" value=\"false\"/>");
                }
                if (selectedValues.contains("output")) {
                    moml.append("<property name=\"output\"/>");
                } else {
                    moml.append("<property name=\"output\" value=\"false\"/>");
                }
                if (selectedValues.contains("multiport")) {
                    moml.append("<property name=\"multiport\"/>");
                } else {
                    moml.append("<property name=\"multiport\" value=\"false\"/>");
                }
                moml.append("</port>");
                ChangeRequest request = new MoMLChangeRequest(
                        this,            // originator
                        parent,          // context
                        moml.toString(), // MoML code
                        null);           // base

                // NOTE: There is no need to listen for completion
                // or errors in this change request, since, in theory,
                // it will just work.  Will someone report the error
                // if one occurs?  I hope so...

                parent.requestChange(request);
            }
        }
    }

    /** Called to notify that one of the entries has changed.
     *  This simply sets a flag that enables application of the change
     *  when the apply() method is called.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
        _changed.add(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The set of names of ports that have changed.
    private Set _changed = new HashSet();

    // The object that this configurer configures.
    private Entity _object;

    // The possible configurations for a port.
    private String[] _optionsArray = {"input", "output", "multiport"};
}
