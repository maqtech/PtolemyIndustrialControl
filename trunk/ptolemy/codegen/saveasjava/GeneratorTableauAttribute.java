/* An attribute that stores the configuration of a generator tableau.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.codegen.saveasjava;

import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.*;

import java.io.File;

//////////////////////////////////////////////////////////////////////////
//// GeneratorTableauAttribute
/**
Attribute is a base class for attributes to be attached to instances
of NamedObj.  This base class is itself a NamedObj, with the only
extension being that it can have a container.  The setContainer()
method puts this object on the list of attributes of the container.

@author Edward A. Lee
@version $Id$
*/
public class GeneratorTableauAttribute extends SingletonAttribute {

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GeneratorTableauAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create parameters, and populate them with style hints to
        // use a checkbox on screen.
        deep = new Parameter(this, "deep",
                new BooleanToken(false));
        new CheckBoxStyle(deep, "style");

        show = new Parameter(this, "show",
                new BooleanToken(true));
        new CheckBoxStyle(show, "style");

        compile = new Parameter(this, "compile",
                new BooleanToken(true));
        new CheckBoxStyle(compile, "style");

        run = new Parameter(this, "run",
                new BooleanToken(true));
        new CheckBoxStyle(run, "style");

        // Initialize the default directory.
        String defaultDirectory = "";
        String defaultClasspath = ".";
        try {
            // NOTE: getProperty() will probably fail in applets, which
            // is why this is in a try block.

            // Set the directory attribute.
            String cwd = System.getProperty("user.dir");
            if (cwd != null) {
                defaultDirectory = cwd;
            }

            // Identify a reasonable classpath.
            // NOTE: This property is set by the vergil startup script.
            String home = System.getProperty("ptolemy.ptII.dir");
            if (home == null) {
                defaultClasspath = ".";
            } else {
                defaultClasspath = home + File.pathSeparator + ".";
            }
        } catch (SecurityException ex) {
            // Ignore and use the default.
        }
        directory = new StringAttribute(this, "directory");
        directory.setExpression(defaultDirectory);

        compileOptions = new StringAttribute(this, "compileOptions");
        compileOptions.setExpression("-classpath \"" + defaultClasspath + "\"");

        runOptions = new StringAttribute(this, "runOptions");
        runOptions.setExpression("-classpath \"" + defaultClasspath + "\"");

        packageName = new StringAttribute(this, "packageName");
        packageName.setExpression("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If true, compile the generated code. This has type boolean, and
     *  defaults to true.
     */
    public Parameter compile;

    /** Options issued to the compile command.*/
    public StringAttribute compileOptions;

    /** If true, generate code for the components as well as for the model.
     *  This has type boolean, and defaults to false.
     */
    public Parameter deep;

    /** The directory into which to put the generated code.*/
    public StringAttribute directory;

    /** Options issued to the java command to run the generated code.*/
    public StringAttribute packageName;

    /** If true, run the generated code. This has type boolean, and
     *  defaults to true.
     */
    public Parameter run;

    /** Options issued to the java command to run the generated code.*/
    public StringAttribute runOptions;

    /** If true, show the generated code. This has type boolean and
     *  defaults to true.
     */
    public Parameter show;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // FIXME: Check that directory is writable in attributeChanged.

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new GeneratorTableauAttribute.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        GeneratorTableauAttribute newObject = (GeneratorTableauAttribute)
                 super.clone(workspace);

        newObject.compile = (Parameter)
                 newObject.getAttribute("compile");
        newObject.compileOptions = (StringAttribute)
                 newObject.getAttribute("compileOptions");
        newObject.deep = (Parameter)
                 newObject.getAttribute("deep");
        newObject.directory = (StringAttribute)
                 newObject.getAttribute("directory");
        newObject.packageName = (StringAttribute)
                 newObject.getAttribute("packageName");
        newObject.run = (Parameter)
                 newObject.getAttribute("run");
        newObject.runOptions = (StringAttribute)
                 newObject.getAttribute("runOptions");
        newObject.show = (Parameter)
                 newObject.getAttribute("show");
        return newObject;
    }
}
