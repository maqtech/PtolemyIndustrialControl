/* An attribute that manages generation of NC code.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.domains.nc.kernel;

// Ptolemy imports.
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// NCCodeGenerator
/**
   This is a visible attribute that when configured (by double
   clicking on it or by invoking Configure in the context menu) generates
   NC code and displays it a text editor.  It is up to the user to save
   the NC code in an appropriate file, if necessary.

   @author Yang Zhao and Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (johnr)
*/

public class NCCodeGenerator extends Director {

    /** Construct a code generator with the specified container and name.
     *  @param container The container.
     *  @param name The name of the code generator.
     *  @exception IllegalActionException If the code generator is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public NCCodeGenerator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-40\" y=\"-15\" width=\"80\" height=\"30\" "
                + "style=\"fill:blue\"/>"
                + "<text x=\"-15\" y=\"9\" "
                + "style=\"font-size:24; font-family:SansSerif; fill:white\">"
                + "NC</text></svg>");

        destinationDirectory = new FileParameter(this, "destinationDirectory");
        destinationDirectory.setExpression("$CWD");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The directory into which to write the code.
     */
    public FileParameter destinationDirectory;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do nothing.
     */
    public void fire() {
    }

    /** Do nothing.
     */
    public void initialize() {
    }

    /* Return false.
     */
    public boolean postfire() throws IllegalActionException {
        return false;
    }

    /* Return false.
     */
    public boolean prefire() throws IllegalActionException {
        // FIXME: If we return false, the run doesn't terminate
        // on its own.
        return true;
    }

    /* Generate code.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getContainer() instanceof CompositeActor)) {
            throw new IllegalActionException(this,
                    "Requires the container to be an instance of CompositeActor.");
        }
        CompositeActor container = (CompositeActor)getContainer();

        File directory = destinationDirectory.asFile();
        if (!directory.isDirectory()) {
            // FIXME: Should we create the directory?
            throw new IllegalActionException(this,
                    "No directory named: "
                    + destinationDirectory.getExpression());
        }
        _writeCode(container, null, directory);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Generate NC code for the given model. This does not descend
     *  hierarchically into contained composites. It simply generates
     *  code for the top level of the specified model.
     *  @param model The model for which to generate code.
     *  @return The NC code.
     */
    public static String generateCode(CompositeActor model)
            throws IllegalActionException {
        StringBuffer generatedCode = new StringBuffer();

        String containerName = model.getName();
        generatedCode.append(
                "configuration " + containerName + " {" + _endLine);
        generatedCode.append(_interfaceProvides(model));
        generatedCode.append(_interfaceUses(model));
        generatedCode.append("}" + _endLine);
        generatedCode.append("implementation {" + _endLine);
        generatedCode.append(_includeModule(model));
        generatedCode.append(_includeConnection(model));
        generatedCode.append( "}" + _endLine);

        return generatedCode.toString();
    }

    /** Generate NC code describing the input ports.  Input ports are
     *  described in NC as interfaces "provided" by this module.
     *  @return The code describing the input ports.
     */
    private static String _interfaceProvides(CompositeActor model)
            throws IllegalActionException {

        StringBuffer codeString = new StringBuffer();

        Iterator inPorts = model.inputPortList().iterator();
        while (inPorts.hasNext()) {
            IOPort port = (IOPort)inPorts.next();
            if (port.isOutput()) {
                throw new IllegalActionException(port,
                        "Ports that are both inputs and outputs are not allowed.");
            }
            codeString.append("provides interface "
                    + port.getName()
                    + ";"
                    + _endLine);
        }
        return codeString.toString();
    }

    /** Generate interface the model uses.
     *  @return The code.
     */
    private static String _interfaceUses(CompositeActor model)
            throws IllegalActionException {

        StringBuffer codeString = new StringBuffer();

        Iterator outPorts = model.outputPortList().iterator();
        while (outPorts.hasNext()) {
            IOPort port = (IOPort)outPorts.next();
            if (port.isInput()) {
                throw new IllegalActionException(port,
                        "Ports that are both inputs and outputs are not allowed.");
            }
            codeString.append("uses interface "
                    + port.getName()
                    + ";"
                    + _endLine);
        }

        return codeString.toString();

    }

    /** Generate code for the components used in the model.
     *  @return The code.
     */
    private static String _includeModule(CompositeActor model)
            throws IllegalActionException {

        StringBuffer codeString = new StringBuffer();

        // Include components.
        Iterator actors = model.entityList().iterator();
        boolean isFirst = true;
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            String actorName = StringUtilities.sanitizeName(
                    ((NamedObj)actor).getName());
            if (actorName.length() == 0) {
                actorName = "Unnamed";
            }
            if (isFirst) {
                codeString.append("components " + actorName);
                isFirst = false;
            } else {
                codeString.append(", " + actorName);
            }
        }
        codeString.append(";" + _endLine);
        return codeString.toString();
    }

    /** Generate code for the connections.
     *  @return The connections code.
     */
    private static String _includeConnection(CompositeActor model, Actor actor)
            throws IllegalActionException {

        StringBuffer codeString = new StringBuffer();

        String actorName = StringUtilities.
            sanitizeName(((NamedObj) actor).getName());

        for (Iterator inPorts = actor.inputPortList().iterator();
             inPorts.hasNext();) {
            IOPort inPort = (IOPort) inPorts.next();
            String sanitizedInPortName =
                StringUtilities.sanitizeName(inPort.getName());
            List sourcePortList = inPort.connectedPortList();
            if (sourcePortList.size() > 1) {
                throw new IllegalActionException(inPort,
                        "Input port (provides) cannot connect to "
                        + "multiple output ports (requires) in NC.");
            }
            if (sourcePortList.size()== 1) {
                IOPort sourcePort = (IOPort) sourcePortList.get(0);
                String sanitizedSourcePortName =
                    StringUtilities.sanitizeName(
                            sourcePort.getName());
                String sourceActorName = StringUtilities.sanitizeName(
                        sourcePort.getContainer().getName());
                if (sourcePort.getContainer() == model) {
                    codeString.append(sanitizedSourcePortName
                            + " = "
                            + actorName
                            + "."
                            + sanitizedInPortName
                            + ";");
                } else {
                    codeString.append(sourceActorName
                            + "."
                            + sanitizedSourcePortName
                            + " -> "
                            + actorName
                            + "."
                            + sanitizedInPortName
                            + ";");
                }
                codeString.append(_endLine);
            }
        }

        return codeString.toString();
    }

    /** Generate code for the connections.
     *  The order of ports in model has effect
     *  on the order of driver input parameters
     *  @return The drivers code.
     */
    private static String _includeConnection(CompositeActor model)
            throws IllegalActionException {

        String codeString = "";
        Actor actor;

        // generate "Driver functions" for common actors.
        Iterator actors = model.entityList().iterator();
        while (actors.hasNext()) {
            actor = (Actor) actors.next();
            if (_needsInputDriver(actor)) {
                codeString += _includeConnection(model, actor);
            }
        }

        Iterator outPorts = model.outputPortList().iterator();
        while (outPorts.hasNext()) {
            IOPort port = (IOPort)outPorts.next();
            // FIXME: Assuming ports are either
            // input or output and not both.
            //String portID = port.getName();
            System.out.println("tring to get the connected port for: "
                    + port.getName());
            List sourcePortList = port.insidePortList();
            //FIXME: can the list be empty?
            if (sourcePortList.size() > 1) {
                throw new IllegalActionException(port, "Input port " +
                        "cannot receive data from multiple sources in NC.");
            }
            IOPort sourcePort;
            if (sourcePortList != null ) {

                sourcePort = (IOPort) sourcePortList.get(0);
                String sanitizedOutPortName =
                    StringUtilities.sanitizeName(
                            sourcePort.getName());
                String sourceActorName = StringUtilities.sanitizeName(
                        sourcePort.getContainer().getName());
                codeString += sourceActorName
                    + "."
                    + sanitizedOutPortName
                    + " = "
                    + port.getName()
                    + ";"
                    + _endLine;
            }
        }
        return codeString;
    }


    /** Return true if the given actor has at least one input port, which
     *  requires it to have an input driver.
     */
    private static boolean _needsInputDriver(Actor actor) {
        if (actor.inputPortList().size() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    /** Generate and write the code for the specified model
     *  and the models it contains.  This method writes code
     *  into files whose names are constructed from the names
     *  of the composites relative to the specified top level.
     *  @param model The model for which to generate code.
     *  @param toplevel The top level, used to determine the file
     *   name of the destination, or null to just use the name
     *   of the model.
     *  @param directory The directory in which to write the
     *   file.
     */
    private void _writeCode(
            CompositeActor model,
            CompositeActor toplevel,
            File directory)
            throws IllegalActionException {

        String code = generateCode(model);

        String name;
        if (toplevel != null) {
            name = toplevel.getName() + "_" + model.getName(toplevel);
        } else {
            toplevel = model;
            name = toplevel.getName();
        }
        name = StringUtilities.sanitizeName(name);

        // FIXME: We just overwrite the file.
        File writeFile = new File(directory, name + ".nc");
        // FIXME: Check whether the file exists?  How to manage overwriting?
        try {
            FileWriter writer = new FileWriter(writeFile);
            writer.write(code);
            writer.close();
        } catch (IOException e) {
            throw new IllegalActionException(this, e,
                    "Failed to open file for writing.");
        }

        // Descend recursively into contained composites.
        Iterator entities = model.entityList(CompositeActor.class).iterator();
        while (entities.hasNext()) {
            CompositeActor contained = (CompositeActor)entities.next();
            _writeCode(contained, toplevel, directory);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static String _endLine = "\n";

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class CodeDisplayerFactory extends EditorFactory {

        public CodeDisplayerFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create an editor for configuring the specified object with the
         *  specified parent window.
         *  @param object The object to configure.
         *  @param parent The parent window, or null if there is none.
         */
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Configuration configuration
                    = ((TableauFrame)parent).getConfiguration();

                // NamedObj container = (NamedObj)object.getContainer();

                CompositeActor model = (CompositeActor)
                    NCCodeGenerator.this.getContainer();

                // Preinitialize and resolve types.
                /**
                   CompositeActor toplevel = (CompositeActor)model.toplevel();
                   Manager manager = toplevel.getManager();
                   if (manager == null) {
                   manager = new Manager(
                   toplevel.workspace(), "manager");
                   toplevel.setManager(manager);
                   }

                   manager.preinitializeAndResolveTypes();
                */

                TextEffigy codeEffigy = TextEffigy.newTextEffigy(
                        configuration.getDirectory(),
                        generateCode(model));
                codeEffigy.setModified(true);
                configuration.createPrimaryTableau(codeEffigy);
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                        "Cannot generate code. Perhaps outside Vergil?");
            }
        }
    }
}
