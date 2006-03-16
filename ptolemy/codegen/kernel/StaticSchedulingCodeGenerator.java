/* Base class for code generators for static scheduling models of computation.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.codegen.c.actor.TypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// StaticSchedulingCodeGenerator

/** Base class for code generators for static scheduling models of computation.
 *
 *  FIXME: need documentation on the following:
 *  1. Define static-scheduling,
 *  2. what should the subclasses do.
 *  3. Define body code, wrapup, initialze section.
 *
 *
 *  @author Edward A. Lee, Gang Zhou, Ye Zhou, Contributor: Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 5.1
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
 */
public class StaticSchedulingCodeGenerator extends CodeGenerator implements
        ActorCodeGenerator {
    /** Create a new instance of the code generator.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If super class throws it.
     *  @exception NameDuplicationException If super class throws it.
     */
    public StaticSchedulingCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the body code that lies between variable declaration
     *  and wrapup.
     *  @return The generated body code.
     *  @exception IllegalActionException If the
     *  {@link #generateFireCode()} method throws the exceptions.
     */
    public String generateBodyCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("\nStatic schedule:"));
        CompositeEntity model = (CompositeEntity) getContainer();

        TypedCompositeActor modelHelper = (TypedCompositeActor) _getHelper(model);

        // NOTE: The cast is safe because setContainer ensures
        // the container is an Actor.
        ptolemy.actor.Director director = ((Actor) model).getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "The model "
                    + model.getName() + " does not have a director.");
        }

        if (!(director instanceof StaticSchedulingDirector)) {
            throw new IllegalActionException(this, "The director of the model "
                    + model.getName() + " is not a StaticSchedulingDirector.");
        }
        Attribute iterations = director.getAttribute("iterations");

        if (iterations == null) {
            throw new IllegalActionException(director,
                    "The Director does not have an attribute name: "
                            + "\"iterations\"");
        } else {
            int iterationCount = ((IntToken) ((Variable) iterations).getToken())
                    .intValue();

            if (iterationCount <= 0) {
                code.append("while (true) {\n");
            } else {
                // Declare iteration outside of the loop to avoid
                // "error: `for' loop initial declaration used outside C99
                // mode" with gcc-3.3.3
                code.append("for (iteration = 0; iteration < " + iterationCount
                        + "; iteration ++) {\n");
            }
        }

        boolean inline = ((BooleanToken) this.inline.getToken()).booleanValue();

        if (inline) {
            code.append(generateFireCode());
        } else {
            code.append(StringUtilities.sanitizeName(model.getFullName())
                    + "();\n");
        }

        // The code generated in generateModeTransitionCode() is executed
        // after one global iteration, e.g., in HDF model.
        modelHelper.generateModeTransitionCode(code);

        code.append("}\n");

        return code.toString();
    }

    /** Generate code.  This is the main entry point.
     *  @param code The code buffer into which to generate the code.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If a type conflict occurs or the model
     *  is running.
     */
    public int generateCode(StringBuffer code) throws KernelException {
        // If necessary, create a manager.
        Actor container = ((Actor) getContainer());
        Manager manager = container.getManager();

        if (manager == null) {
            CompositeActor toplevel = (CompositeActor) ((NamedObj) container)
                    .toplevel();
            manager = new Manager(toplevel.workspace(), "Manager");
            toplevel.setManager(manager);
        }

        int returnValue = -1;
        try {
            manager.preinitializeAndResolveTypes();
            returnValue = super.generateCode(code);
        } finally {
            manager.wrapup();
        }
        return returnValue;
    }

    /** Generate into the specified code buffer the code associated
     *  with the execution of the container composite actor. This method
     *  calls the generateFireCode() method of the code generator helper
     *  associated with the director of the container.
     *  @return The generated code.
     *  @exception IllegalActionException If a static scheduling director is
     *   missing or the generateFireCode(StringBuffer) method of the
     *   director helper throws the exception.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeEntity model = (CompositeEntity) getContainer();
        TypedCompositeActor modelHelper = (TypedCompositeActor) _getHelper(model);
        code.append(modelHelper.generateFireCode());
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the code generator helper associated with the given component.
     *  @param actor The given component actor.
     *  @exception IllegalActionException If the helper of the given actor
     *  is not an instance of ActorCodeGenerator.
     *  @return The code generator helper.
     */
    protected ComponentCodeGenerator _getHelper(NamedObj actor)
            throws IllegalActionException {
        ComponentCodeGenerator helperObject = super._getHelper(actor);

        if (!(helperObject instanceof ActorCodeGenerator)) {
            throw new IllegalActionException(this,
                    "Cannot generate code for this actor: " + actor
                            + ". Its helper class does not"
                            + " implement ActorCodeGenerator.");
        }

        return helperObject;
    }
}
