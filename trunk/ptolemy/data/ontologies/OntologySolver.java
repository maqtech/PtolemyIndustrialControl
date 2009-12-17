/* A solver for ontologies.
 * 
 * Copyright (c) 2007-2009 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.data.ontologies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.TypeConflictException;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.PropertyConstraintHelper.Inequality;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.tester.lib.Testable;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;


//////////////////////////////////////////////////////////////////////////
//// OntologySolver

/**
 * An instance of this solver contains a <i>use case</i>, which itself
 * contains a lattice and default constraints.
 * <p>
 * This class is based on the PropertyConstraintSolver in the properties package
 * by Man-Kit Leung.
 * 
 * @author Man-Kit Leung, Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class OntologySolver extends PropertySolver implements Testable {

    /**
     * @param container The given container.
     * @param name The given name
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public OntologySolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        // Provide a default model so that this is never empty.
        _model = new CompositeEntity(workspace());

        solvingFixedPoint = new StringParameter(this, "solvingFixedPoint");
        solvingFixedPoint.setExpression("least");

        actorConstraintType = new StringParameter(this, "actorConstraintType");
        actorConstraintType.setExpression("out >= in");

        connectionConstraintType = new StringParameter(this,
                "connectionConstraintType");
        connectionConstraintType.setExpression("sink >= src");

        compositeConnectionConstraintType = new StringParameter(this,
                "compositeConnectionConstraintType");
        compositeConnectionConstraintType.setExpression("sink >= src");

        fsmConstraintType = new StringParameter(this, "fsmConstraintType");
        fsmConstraintType.setExpression("sink >= src");

        expressionASTNodeConstraintType = new StringParameter(this,
                "expressionASTNodeConstraintType");
        expressionASTNodeConstraintType.setExpression("parent >= child");

        logMode = new Parameter(this, "logMode");
        logMode.setTypeEquals(BaseType.BOOLEAN);
        logMode.setExpression("false");

        // Set to path of the model.
        logDirectory = new FileParameter(this, "Log directory");
        // In Windows, this should map to C:\temp\, /home/tmp/ in Linux.
        logDirectory.setExpression("$HOME/temp/ConstraintFiles");

        trainedConstraintDirectory = new FileParameter(this,
                "Trained constraint directory");
        trainedConstraintDirectory
                .setExpression("$CLASSPATH/trainedConstraints");

        _addChoices();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"120\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nResolve Properties</text></svg>");
        
        /* Removed to make compile
         * --Ben on 12/04/2009
        new PropertySolverGUIFactory(this,
                "_propertyConstraintSolverGUIFactory");
         */

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * 
     */
    public static enum ConstraintType {
        EQUALS, NONE, NOT_EQUALS, SINK_EQUALS_GREATER, SINK_EQUALS_MEET, SINK_GREATER, SRC_EQUALS_GREATER, SRC_EQUALS_MEET, SRC_GREATER
    }

    public void addAnnotated(Object object) {
        _annotatedObjects.add(object);
    }

    /**
     * React to a change in an attribute.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If the change is not acceptable to this
     * container (not thrown in this class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == logMode) {
            _logMode = logMode.getToken() == BooleanToken.TRUE;
        }
        super.attributeChanged(attribute);
    }

    public List<ptolemy.graph.InequalityTerm> getAffectedTerms(ptolemy.graph.InequalityTerm updateTerm)
            throws IllegalActionException {
        return _propertyTermManager.getAffectedTerms(updateTerm);
    }

    /**
     * @return the constraintManager
     */
    public ConstraintManager getConstraintManager() {
        return _constraintManager;
    }

    /**
     * Returns the adapter that contains property information for the given AST
     * node.
     * @param node The given ASTPtRootNode.
     * @return The associated property constraint adapter.
     */
    public PropertyConstraintASTNodeHelper getHelper(ASTPtRootNode node)
            throws IllegalActionException {

        return (PropertyConstraintASTNodeHelper) _getHelper(node);
    }

    /**
     * Returns the adapter that contains property information for the given
     * component.
     * @param component The given component
     * @return The associated property constraint adapter.
     */
    public PropertyHelper getHelper(NamedObj component)
            throws IllegalActionException {

        return _getHelper(component);
    }

    /**
     * Return the property constraint adapter associated with the given object.
     * @param object The given object.
     */
    public PropertyHelper getHelper(Object object)
            throws IllegalActionException {

        return _getHelper(object);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     * Return the property value associated with the specified object.
     * @param object The specified object.
     * @return The property of the specified object.
     */
    public Concept getProperty(Object object) {
        try {
            return (Concept) getPropertyTerm(object).getValue();
        } catch (IllegalActionException ex) {
            return null;
        }
    }

    /**
     * Return the property term from the given object.
     * @param object The given object.
     * @return The property term of the given object.
     * @exception IllegalActionException
     */
    public ptolemy.graph.InequalityTerm getPropertyTerm(Object object) {
        return getPropertyTermManager().getPropertyTerm(object);
    }

    public PropertyTermFactory getPropertyTermManager() {
        if (_propertyTermManager == null) {
            _propertyTermManager = _getPropertyTermManager();
        }
        return _propertyTermManager;
    }

    public boolean isAnnotatedTerm(Object object) {
        return _annotatedObjects.contains(object);
    }

    /**
     * Return true if the solver is in collect constraints mode; otherwise,
     * return false.
     * @return True if the solver is in collect constraints mode; otherwise,
     * return false.
     */
    public boolean isCollectConstraints() {
        return action.getExpression().equals(COLLECT_CONSTRAINTS);
    }

    /**
     * Return true if the solver is in initialization mode; otherwise, return
     * false.
     * @return True if the solver is in initialization mode; otherwise, return
     * false.
     */
    public boolean isInitializeSolver() {
        return action.getExpression().equals(INITIALIZE_SOLVER);
    }

    public Boolean isLogMode() {
        return _logMode;
    }

    /**
     * Override the base class method. Return true if the solver is in
     * initialization mode; otherwise, return the result of the super method
     * (default).
     * @return True if the solver is in initialization mode; otherwise, return
     * the result of the super method (default).
     */
    public boolean isResolve() {
        return isCollectConstraints() || isInitializeSolver()
                || super.isResolve();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void reset() {
        super.reset();
        _propertyTermManager = null;
        _trainedConstraints.clear();
    }

    public void setLogMode(boolean isLogMode) {
        _logMode = isLogMode;
    }

    /**
     * Prepare for automatic testing.
     */
    public void setOptions(Map options) {
        super.setOptions(options);

        // By default, look up the logMode parameter in the model to
        // see if we need to test the constraints.
        if (options.containsKey(NONDEEP_TEST_OPTION)) {
            setLogMode(false);
        }

        if (isLogMode()) {
            System.out.println("doing deep testing.");
        } else {
            System.out.println("NOT deep testing: " + options.size());
        }
    }

    /** Run a test. This invokes the solver in TEST mode.
     *  @throws IllegalActionException If the test fails.
     */
    public void test() throws IllegalActionException {
        // FIXME: Brute force method here just sets the action to TEST.
        // However, the TEST and TRAINING modes should be removed.
        String previousAction = action.getExpression();
        try {
            action.setExpression(TEST);
            invokeSolver();
            resetAll();
        } finally {
            action.setExpression(previousAction);
        }
    }
    
    /** Train a test. This invokes the solver in TRAINING mode.
     */
    public void train() {
        // FIXME: Brute force method here just sets the action to TRAINING.
        // However, the TEST and TRAINING modes should be removed.
        String previousAction = action.getExpression();
        try {
            workspace().getWriteAccess();
            action.setExpression(TRAINING);
            invokeSolver();
            resetAll();
        } finally {
            action.setExpression(previousAction);
            workspace().doneWriting();
        }
    }

    /**
     * Update the property.
     * @exception IllegalActionException
     */
    public void updateProperties() throws IllegalActionException {
        super.updateProperties();

        // Only need to look at the constraints of the top level adapter.
        PropertyHelper adapter;
        adapter = getHelper(_toplevel());

        if (isLogMode()) {
            String constraintFilename = _getTrainedConstraintFilename()
                    + "_resolved.txt";

            if (super.isResolve()) {

                _trainedConstraints.clear();

                // Populate the _trainedConstraints list.
                _logHelperConstraints((PropertyConstraintHelper) adapter);

                // Write the list to file.
                _updateConstraintFile(constraintFilename);

            } else if (isTesting() && isLogMode()) {
                // Populate the _trainedConstraints list.
                _readConstraintFile(constraintFilename);

                // Match and remove from the list.
                _regressionTestConstraints((PropertyConstraintHelper) adapter);

                // Check if there are unmatched constraints.
                _checkMissingConstraints();
            }
        }
    }

    protected PropertyHelper _getHelper(Object component)
            throws IllegalActionException {
        PropertyHelper adapter = null;

        try {
            adapter = super._getHelper(component);
        } catch (IllegalActionException ex) {
        }

        if (adapter == null) {
            if (component instanceof FSMActor) {
                /* Removed to make compile
                 * --Ben on 12/04/2009
                adapter = new PropertyConstraintFSMHelper(this,
                        (FSMActor) component);
            } else if (component instanceof ptolemy.domains.modal.kernel.FSMActor) {
                adapter = new PropertyConstraintModalFSMHelper(this,
                        (ptolemy.domains.modal.kernel.FSMActor) component);
                 */
            } else if (component instanceof CompositeEntity) {
                adapter = new PropertyConstraintCompositeHelper(this,
                        (CompositeEntity) component);
            } else if (component instanceof ASTPtRootNode) {
                adapter = new PropertyConstraintASTNodeHelper(this,
                        (ASTPtRootNode) component);
            } else {
                adapter = new PropertyConstraintHelper(this, component);
            }
        }
        _adapterStore.put(component, adapter);
        return adapter;
    }

    /**
     * Return a new property term manager. Subclass can implements a its own
     * manager and override this method to instantiate a instance of the new
     * class.
     * @return A property term manager.
     */
    protected PropertyTermManager _getPropertyTermManager() {
        //      FIXME: doesn't work for other use-cases!
        //      return new StaticDynamicTermManager(this);
        return new PropertyTermManager(this);
    }

    protected void _initializeStatistics() {
        super._initializeStatistics();
        getStats().put("# of default constraints", 0);
        getStats().put("# of composite default constraints", 0);
        getStats().put("# of atomic actor default constraints", 0);
        getStats().put("# of AST default constraints", 0);
        getStats().put("# of generated constraints", 0);
        getStats().put("# of trained constraints", 0);
    }

    /**
     * Resolve the property values for the toplevel entity that contains this
     * solver, given the model analyzer that invokes this.
     * @param analyzer The given model analyzer.
     */
    protected void _resolveProperties(NamedObj analyzer) throws KernelException {
        super._resolveProperties(analyzer);

        NamedObj toplevel = _toplevel();
        PropertyConstraintHelper toplevelHelper = (PropertyConstraintHelper) getHelper(toplevel);

        toplevelHelper.reinitialize();

        toplevelHelper
                ._addDefaultConstraints(_getConstraintType(actorConstraintType
                        .stringValue()));

        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        toplevelHelper._setConnectionConstraintType(
                _getConstraintType(connectionConstraintType.stringValue()),
                _getConstraintType(compositeConnectionConstraintType
                        .stringValue()), _getConstraintType(fsmConstraintType
                        .stringValue()),
                _getConstraintType(expressionASTNodeConstraintType
                        .stringValue()));
        
        // Collect and solve type constraints.
        List<Inequality> constraintList = toplevelHelper.constraintList();
         
        _resolveProperties(toplevel, toplevelHelper, constraintList);
    }

    /** Resolve the properties of the given top-level container,
     * subject to the given constraint list.
     * 
     * @param toplevel The top-level container
     * @param toplevelHelper Must be toplevel.getHelper()
     * @param constraintList The constraint list that we are solving
     * @throws TypeConflictException If an unacceptable solution is reached
     * @throws PropertyResolutionException If constraints are unsatisfiable
     */
    protected void _resolveProperties(NamedObj toplevel,
            PropertyConstraintHelper toplevelHelper,
            List<Inequality> constraintList) throws TypeConflictException,
            PropertyResolutionException {
        Writer writer = null;

        List<Inequality> conflicts = new LinkedList<Inequality>();
        List<Inequality> unacceptable = new LinkedList<Inequality>();
        
        try {
            // Check declared properties across all connections.
            //List propertyConflicts = topLevel._checkDeclaredProperties();
            //conflicts.addAll(propertyConflicts);

          
            /*
             * // FIXME: this is the iterative approach. List constraintList =
             * new ArrayList(); Iterator adapters =
             * _adapterStore.values().iterator(); while (adapters.hasNext()) {
             * PropertyConstraintHelper adapter = (PropertyConstraintHelper)
             * adapters.next();
             * 
             * constraintList.addAll(adapter.constraintList()); } //
             */

            // NOTE: To view all property constraints, uncomment these.
            /*
             * Iterator constraintsIterator = constraintList.iterator(); while
             * (constraintsIterator.hasNext()) {
             * System.out.println(constraintsIterator.next().toString()); }
             */

            if (constraintList.size() > 0) {
                CPO lattice = null;
                
                // Added by Charles Shelton 12/17/09
                // Check to see if the ontology is a lattice
                // and throw an exception if it isn't.                
                if (getOntology().isLattice()) {                
                    lattice = getOntology().getGraph();
                } else {
                    throw new IllegalActionException(this, "This Ontology is not a lattice, "
                            + "and therefore we cannot resolve the model using the least fixed point algorithm.");
                }

                // Instantiate our own customized version of InequalitySolver.
                ptolemy.graph.InequalitySolver solver = new ptolemy.graph.InequalitySolver(lattice);
                //InequalitySolver solver = new InequalitySolver(cpo, this);

                solver.addInequalities(constraintList.iterator());
                _constraintManager.setConstraints(constraintList);

                //              BEGIN CHANGE Thomas, 04/10/2008
                // Collect statistics.
                getStats().put("# of generated constraints",
                        constraintList.size());
                getStats().put("# of property terms",
                        _propertyTermManager.terms().size());

                // log initial constraints to file
                File file = null;
                Date date = new Date();
                String timestamp = date.toString().replace(":", "_");
                String logFilename = getContainer().getName() + "__"
                        + getName() + "__" + timestamp.replace(" ", "_")
                        + ".txt";

                if (super.isResolve() && isLogMode()) {
                    String directoryPath = logDirectory.getExpression();
                    directoryPath += directoryPath.endsWith("/")
                            || directoryPath.endsWith("\\") ? "" : "/";

                    if (directoryPath.startsWith("$CLASSPATH")) {
                        URI directory = new File(URIAttribute.getModelURI(this))
                                .getParentFile().toURI();

                        file = FileUtilities.nameToFile(directoryPath
                                .substring(11)
                                + logFilename, directory);

                    } else {
                        if (!logDirectory.asFile().exists()) {
                            if (!logDirectory.asFile().mkdirs()) {
                                throw new IllegalActionException(this,
                                        "Failed to create \""
                                                + logDirectory.asFile()
                                                        .getAbsolutePath()
                                                + "\" directory.");
                            }
                        }
                        file = FileUtilities.nameToFile(logFilename,
                                logDirectory.asFile().toURI());
                    }

                    try {
                        if (!file.exists()) {
                            if (!file.getParentFile().exists()) {
                                if (!file.getParentFile().mkdirs()) {
                                    throw new IllegalActionException(this,
                                            "Failed to create \""
                                                    + file.getParentFile()
                                                            .getAbsolutePath()
                                                    + "\" directory.");
                                }

                            }
                            if (!file.createNewFile()) {
                                throw new IllegalActionException(this,
                                        "Failed to create \""
                                                + file.getAbsolutePath()
                                                + "\".");
                            }
                        }

                        writer = new FileWriter(file);

                        writer.write(_getStatsAsString("\t"));
                        writer.write(_getConstraintsAsLogFileString(
                                constraintList, "I"));

                    } catch (IOException ex) {
                        throw new PropertyResolutionException(this, ex,
                                "Error writing to constraint log file \""
                                        + file.getAbsolutePath() + "\".");
                    }
                }

                // Record the initial constraints.
                // FIXME: merge comment: Jackies code is "if (super.isResolve() && isLogMode()) {"; why different?
                // FIXME: Charles Shelton 05/27/09 - We took the change that Jackie made to include the isLogMode() condition.
                if (super.isResolve() && isLogMode()) {
                    String constraintFilename = _getTrainedConstraintFilename()
                            + "_initial.txt";

                    // Populate the _trainedConstraints list.
                    _logHelperConstraints(toplevelHelper);

                    // Write the list to file.
                    _updateConstraintFile(constraintFilename);
                }

                //              END CHANGE Thomas, 04/10/2008

                if (!isCollectConstraints()) {
                    // Find the greatest solution (most general type)
                    if (solvingFixedPoint.stringValue().equals("greatest")) {
                        solver.solveGreatest();
                    } else {
                        solver.solveLeast();
                    }
                }

                // log resolved constraints to file.
                if (super.isResolve() && isLogMode()) {
                    try {
                        writer.write(_getConstraintsAsLogFileString(
                                constraintList, "R"));
                        writer.close();
                    } catch (IOException ex) {
                        throw new PropertyResolutionException(this, ex,
                                "Error writing to constraint log file \""
                                        + file.getAbsolutePath() + "\".");
                    }
                }

                // If some inequalities are not satisfied, or type variables
                // are resolved to unacceptable types, such as
                // BaseType.UNKNOWN, add the inequalities to the list of
                // property conflicts.
                for (Inequality inequality : constraintList) {

                    if (!inequality.isSatisfied(lattice)) {
                        conflicts.add(inequality);

                    } else {
                        // Check if there exist an unacceptable term value.
                        boolean isAcceptable = true;

                        InequalityTerm[] lesserVariables = inequality
                                .getLesserTerm().getVariables();

                        InequalityTerm[] greaterVariables = inequality
                                .getGreaterTerm().getVariables();

                        for (InequalityTerm variable : lesserVariables) {
                            if (!variable.isValueAcceptable()) {
                                unacceptable.add(inequality);
                                isAcceptable = false;
                                break;
                            }
                        }

                        if (isAcceptable) {
                            // Continue checking for unacceptable terms.
                            for (InequalityTerm variable : greaterVariables) {
                                if (!variable.isValueAcceptable()) {
                                    unacceptable.add(inequality);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Check for solution correctness.
            // In initialize mode, we can skip this.
            if (!isInitializeSolver() && !isCollectConstraints()) {
                if (conflicts.size() > 0) {
                    throw new PropertyResolutionException(this, toplevel(),
                            "Properties conflicts occurred in "
                                    + toplevel().getFullName()
                                    + " on the following inequalities:\n"
                                    + conflicts);
                }
                if (unacceptable.size() > 0) {
                    throw new TypeConflictException(unacceptable,
                            "Properties resolved to unacceptable types in "
                                    + toplevel.getFullName()
                                    + " due to the following inequalities:");
                }
            }
        } catch (IllegalActionException ex) {
            // This should not happen. The exception means that
            // _checkDeclaredProperty or constraintList is called on a
            // transparent actor.
            throw new PropertyResolutionException(this, toplevel, ex,
                    "Concept resolution failed because of an error "
                            + "during property inference");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    throw new PropertyResolutionException(this, toplevel(), ex,
                            "Failed to close a file");
                }
            }
        }
        
        
    }

    /**
     * Add choices to the parameters.
     * @exception IllegalActionException If there is a problem accessing files
     * or parameters.
     */
    private void _addChoices() throws IllegalActionException {
        solvingFixedPoint.addChoice("least");
        solvingFixedPoint.addChoice("greatest");

        actorConstraintType.addChoice("in >= out");
        actorConstraintType.addChoice("out >= in");
        actorConstraintType.addChoice("out == in");
        actorConstraintType.addChoice("out == meet(in1, in2, ...)");
        actorConstraintType.addChoice("in == meet(out1, out2, ...)");
        actorConstraintType.addChoice("NONE");
        //      actorConstraintType.addChoice("in > out");
        //      actorConstraintType.addChoice("out > in");
        //      actorConstraintType.addChoice("out != in");

        connectionConstraintType.addChoice("src >= sink");
        connectionConstraintType.addChoice("sink >= src");
        connectionConstraintType.addChoice("sink == src");
        connectionConstraintType.addChoice("src == meet(sink1, sink2, ...)");
        connectionConstraintType.addChoice("sink == meet(src1, src2, ...)");
        connectionConstraintType.addChoice("NONE");
        //      connectionConstraintType.addChoice("src > sink");
        //      connectionConstraintType.addChoice("sink > src");
        //      connectionConstraintType.addChoice("sink != src");

        compositeConnectionConstraintType.addChoice("src >= sink");
        compositeConnectionConstraintType.addChoice("sink >= src");
        compositeConnectionConstraintType.addChoice("sink == src");
        compositeConnectionConstraintType
                .addChoice("src == meet(sink1, sink2, ...)");
        compositeConnectionConstraintType
                .addChoice("sink == meet(src1, src2, ...)");
        compositeConnectionConstraintType.addChoice("NONE");
        //      compositeConnectionConstraintType.addChoice("src > sink");
        //      compositeConnectionConstraintType.addChoice("sink > src");
        //      compositeConnectionConstraintType.addChoice("sink != src");

        expressionASTNodeConstraintType.addChoice("child >= parent");
        expressionASTNodeConstraintType.addChoice("parent >= child");
        expressionASTNodeConstraintType.addChoice("parent == child");
        //expressionASTNodeConstraintType.addChoice("child == meet(parent1, parent2, ...)");
        expressionASTNodeConstraintType
                .addChoice("parent == meet(child1, child2, ...)");
        expressionASTNodeConstraintType.addChoice("NONE");
        //      expressionASTNodeConstraintType.addChoice("child > parent");
        //      expressionASTNodeConstraintType.addChoice("parent > child");
        //      expressionASTNodeConstraintType.addChoice("parent != child");

        fsmConstraintType.addChoice("src >= sink");
        fsmConstraintType.addChoice("sink >= src");
        fsmConstraintType.addChoice("sink == src");
        fsmConstraintType.addChoice("src == meet(sink1, sink2, ...)");
        fsmConstraintType.addChoice("sink == meet(src1, src2, ...)");
        fsmConstraintType.addChoice("NONE");
        //      fsmConstraintType.addChoice("src > sink");
        //      fsmConstraintType.addChoice("sink > src");
        //      fsmConstraintType.addChoice("sink != src");

        action.addChoice(INITIALIZE_SOLVER);
        action.addChoice(COLLECT_CONSTRAINTS);
    }

    private void _checkMissingConstraints() {
        StringBuffer errorMessage = new StringBuffer(_eol + "Concept \"" + getName()
                + "\" resolution failed." + _eol);

        boolean hasError = false;

        for (String trainedValue : _trainedConstraints) {
            errorMessage.append("    Missing constraint: \"" + trainedValue + "\"."
                    + _eol);

            hasError = true;
        }

        if (hasError) {
            getSharedUtilities().addErrors(errorMessage.toString());
        }
    }

    //    /**
    //     * Return the property value associated with the given property lattice
    //     * and the given port.
    //     * @param object The given port.
    //     * @param lattice The given lattice.
    //     * @return The property value of the given port.
    //     * @exception IllegalActionException
    //     */
    //  public Concept getProperty(Object object) {
    //  ptolemy.graph.InequalityTerm term = (ptolemy.graph.InequalityTerm) getPropertyTerm(object);
    //  return (Concept) term.getValue();
    //  }

    /**
     * Return the Constraints as a log file string.
     * @param inequality
     * @param annotation
     * @return The Constraints.
     * @exception IllegalActionException
     */
    private List _getConstraintAsLogFileString(Inequality inequality,
            String annotation) throws IllegalActionException {
        List<String> logConstraints = new LinkedList<String>();

        String output = "";
        ptolemy.graph.InequalityTerm lesserTerm = (ptolemy.graph.InequalityTerm) inequality.getLesserTerm();
        ptolemy.graph.InequalityTerm greaterTerm = (ptolemy.graph.InequalityTerm) inequality.getGreaterTerm();

        output = inequality.getHelper().getClass().getPackage().toString()
                .replace("package ", "")
                + "\t"
                + inequality.getHelper().getClass().getSimpleName()
                + "\t"
                + _getReducedFullName(inequality.getHelper().getComponent())
                + "\t"
                + (inequality.isBase() ? "base" : "not base")
                + "\t"
                + _getConstraintLogString(lesserTerm, "")
                + "\t"
                + "<="
                + "\t"
                + _getConstraintLogString(greaterTerm, "");
        logConstraints.add(output);

        // also write variables of FunctionTerms to log-Files
        if (lesserTerm instanceof MonotonicFunction) {
            for (InequalityTerm variable : lesserTerm.getVariables()) {
                output = inequality.getHelper().getClass().getPackage()
                        .toString().replace("package ", "")
                        + "\t"
                        + inequality.getHelper().getClass().getSimpleName()
                        + "\t"
                        + _getReducedFullName(inequality.getHelper()
                                .getComponent())
                        + "\t"
                        + (inequality.isBase() ? "base" : "not base")
                        + "\t"
                        + _getConstraintLogString((ptolemy.graph.InequalityTerm) variable, "")
                        + "\t"
                        + "MFV"
                        + "\t"
                        + _getConstraintLogString(lesserTerm,
                                _getReducedFullName(inequality.getHelper()
                                        .getComponent()));
                logConstraints.add(output);
            }
            /*  FIXME: Removing chunks of code wholesale now.
             * --Ben 12/04/2009
            for (InequalityTerm constant : lesserTerm.getConstants()) {
                output = inequality.getHelper().getClass().getPackage()
                        .toString().replace("package ", "")
                        + "\t"
                        + inequality.getHelper().getClass().getSimpleName()
                        + "\t"
                        + _getReducedFullName(inequality.getHelper()
                                .getComponent())
                        + "\t"
                        + (inequality.isBase() ? "base" : "not base")
                        + "\t"
                        + _getConstraintLogString((ptolemy.graph.InequalityTerm) constant, "")
                        + "\t"
                        + "MFC"
                        + "\t"
                        + _getConstraintLogString(lesserTerm,
                                _getReducedFullName(inequality.getHelper()
                                        .getComponent()));
                logConstraints.add(output);
            }
             */
        }

        return logConstraints;
    }

    private String _getConstraintLogString(ptolemy.graph.InequalityTerm propertyTerm,
            String actorName) throws IllegalActionException {
        if (propertyTerm instanceof Concept) {
            // FIXME: This is bogus unreadable syntax. "eff" means "effective"
            // (whatever that means).
            return (true ? "eff" : "ineff") + "\t" + "\t"
                    + propertyTerm.getClass().getSuperclass().getSimpleName()
                    + "\t" + propertyTerm.toString() + "\t"
                    + propertyTerm.getValue();
        } else if (propertyTerm instanceof MonotonicFunction) {
            return (true ? "eff" : "ineff")
                    + "\t"
                    + actorName
                    + "\t"
                    + propertyTerm.getClass().getSuperclass().getSimpleName()
                    + "\t"
                    + propertyTerm.getClass().toString()
                            .substring(
                                    propertyTerm.getClass().toString()
                                            .lastIndexOf(".")) + "\t"
                    + propertyTerm.getValue();
        } else {
            Object object = propertyTerm.getAssociatedObject();
            String containerName = "";

            if (object != null) {
                if (object instanceof ASTPtRootNode) {
                    try {
                        if (((ASTPtRootNode) object).jjtGetParent() != null
                                && !(((ASTPtRootNode) object).jjtGetParent() instanceof ASTPtAssignmentNode)) {
                            containerName = _getReducedFullName(((ASTPtRootNode) object)
                                    .jjtGetParent());
                        } else {
                            containerName = _getReducedFullName(getHelper(
                                    object).getContainerEntity(
                                    (ASTPtRootNode) object));
                        }
                    } catch (IllegalActionException e) {
                        assert false;
                    }
                }
                // FIXME: effective is not implemented.
                return (true ? "eff" : "ineff") + "\t"
                        + containerName + "\t"
                        + object.getClass().getSimpleName() + "\t"
                        + _getReducedFullName(object) + "\t"
                        + propertyTerm.getValue();
            } else {
                return "NO" + "\t" + "ASSOCIATED" + "\t" + "OBJECT";
            }
        }

    }

    private String _getConstraintsAsLogFileString(
            List<Inequality> constraintList, String annotation)
            throws IllegalActionException {

        StringBuffer output = new StringBuffer();
        for (Inequality inequality : constraintList) {
            output.append(_getConstraintAsLogFileString(inequality, annotation)
                    + _eol);
        }

        return output.toString();
    }

    /**
     * Return the constraint type.
     * @param typeValue
     * @return The constraint type.
     * @exception IllegalActionException
     */
    protected static ConstraintType _getConstraintType(String typeValue)
            throws IllegalActionException {
        boolean isEquals = typeValue.contains("==");
        boolean isSrc = typeValue.startsWith("src")
                || typeValue.startsWith("in") || typeValue.startsWith("child");
        boolean isNotEquals = typeValue.contains("!=");

        boolean hasMeet = typeValue.contains("meet");

        if (typeValue.equals("NONE")) {
            return ConstraintType.NONE;
        }
        if (hasMeet) {
            return isSrc ? ConstraintType.SRC_EQUALS_MEET
                    : ConstraintType.SINK_EQUALS_MEET;

        } else if (isEquals) {
            return ConstraintType.EQUALS;

        } else if (isNotEquals) {
            return ConstraintType.NOT_EQUALS;

        } else {
            return isSrc ? ConstraintType.SRC_EQUALS_GREATER
                    : ConstraintType.SINK_EQUALS_GREATER;
        }
    }

    private String _getReducedFullName(Object object) {
        if (object instanceof NamedObj) {
            String name = ((NamedObj) object).getFullName();
            if (name.indexOf(".", 2) > 0) {
                name = name.substring(name.indexOf(".", 2));
            } else {
                name = "topLevelComposite";
            }
            return name;
        } else {
            return object.toString();
        }
    }

    /**
     * Return the trained constraint filename.
     * @return The trained constraint filename.
     * @exception IllegalActionException If there is a problem getting the name
     * of the top level or the value of the <i>trainedConstraintDirectory</i>
     * parameter.
     */
    private String _getTrainedConstraintFilename()
            throws IllegalActionException {
        // Make an unique file name from the toplevel container.
        // FIXME: don't use __, they make the filenames too long.
        String constraintFilename = _toplevel().getName() + "__"
                + getName();

        String directoryPath = trainedConstraintDirectory.getExpression();
        directoryPath = directoryPath.replace("\\", "/");
        directoryPath += directoryPath.endsWith("/")
                || directoryPath.endsWith("\\") ? "" : "/";

        File constraintFile;
        if (directoryPath.startsWith("$CLASSPATH")) {

            // FIXME: for a cloned URIAttribute, the URI is not set.
            URI directory = new File(URIAttribute.getModelURI(this))
                    .getParentFile().toURI();

            constraintFile = FileUtilities.nameToFile(directoryPath
                    .substring(11)
                    + constraintFilename, directory);

        } else {
            if (!trainedConstraintDirectory.asFile().exists()) {
                if (!trainedConstraintDirectory.asFile().mkdirs()) {
                    throw new IllegalActionException(this,
                            "Failed to create \""
                                    + trainedConstraintDirectory.asFile()
                                            .getAbsolutePath()
                                    + "\" directory.");
                }
            }
            constraintFile = FileUtilities.nameToFile(constraintFilename,
                    trainedConstraintDirectory.asFile().toURI());
        }
        return constraintFile.getAbsolutePath().replace("\\", "/").replaceAll(
                "%5c", "/");
    }

    /**
     * 
     * @param adapter
     * @exception IllegalActionException
     */
    private void _logHelperConstraints(PropertyConstraintHelper adapter)
            throws IllegalActionException {
        List<Inequality>[] constraintSet = new List[2];
        /*  FIXME: Removing chunks of code wholesale now.
         * --Ben 12/04/2009
        constraintSet[0] = adapter._ownConstraints;
        constraintSet[1] = adapter._subHelperConstraints;

        for (int i = 0; i < 2; i++) {
            //String whichSet = (i == 0) ? " own " : " subHelper's ";

            for (Inequality constraint : constraintSet[i]) {
                Iterator logConstraints = _getConstraintAsLogFileString(
                        constraint, "").iterator();
                while (logConstraints.hasNext()) {
                    _trainedConstraints.add((String) logConstraints.next());
                }
            }
        }
        */
    }

    private void _readConstraintFile(String filename){

        File file = new File(filename);

        try {
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new FileReader(file));

                String line = reader.readLine();
                while (line != null) {
                    _trainedConstraints.add(line);
                    line = reader.readLine();
                }

                getStats().put("# of trained constraints",
                        _trainedConstraints.size());
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException ex) {
            /* FIXME: Removed to make compile
             * --Ben 12/04/2009
            throw new PropertyFailedRegressionTestException(this,
                    "Failed to open or read the constraint file \"" + filename
                            + "\".");
            */
        }
    }

    private void _regressionTestConstraints(PropertyConstraintHelper adapter)
            throws IllegalActionException {
        Object object = adapter.getComponent();
        if (!(object instanceof NamedObj)) {
            return;
        }
        NamedObj namedObj = (NamedObj) object;

        StringBuffer errorMessage = new StringBuffer(_eol +
                "Concept \"" + getName() +
                "\" resolution failed for " +
                namedObj.getFullName() +
                "'s adapter." + _eol);

        List<Inequality>[] constraintSet = new List[2];
        /*  FIXME: Removing chunks of code wholesale now.
         * --Ben 12/04/2009
        constraintSet[0] = adapter._ownConstraints;
        constraintSet[1] = adapter._subHelperConstraints;

        boolean hasError = false;

        for (int i = 0; i < 2; i++) {
            String whichSet = i == 0 ? " own " : " subHelper's ";

            for (Inequality constraint : constraintSet[i]) {

                Iterator logConstraints = _getConstraintAsLogFileString(
                        constraint, "").iterator();
                while (logConstraints.hasNext()) {
                    String constraintString = (String) logConstraints.next();
                    // Remove from the trained set so we can test for duplicates.
                    if (!_trainedConstraints.remove(constraintString)) {
                        errorMessage.append("    Extra" + whichSet
                                + "constraint generated: \"" + constraintString
                                + "\"." + _eol);

                        hasError = true;
                    }
                }
            }
        }

        if (hasError) {
            getSharedUtilities().addErrors(errorMessage.toString());
        }
        */

    }

    /**
     * Log the trained constraints in a subdirectory under the specified
     * logDirectory. The contraint file has an unique name consist of the name
     * of toplevel container and this solver. If the constraint file already
     * exists, an overwrite warning message is sent to the user.
     * @param filename
     * @exception PropertyResolutionException Thrown if there is a problem
     * opening, writing, or closing the constraint file.
     */
    private void _updateConstraintFile(String filename)
            throws IllegalActionException {
        if (!super.isResolve() || !isLogMode()) {
            return;
        }

        try {
            File constraintFile = new File(filename);

            if (constraintFile.exists()) {
                if (_analyzer != null
                        && ((Parameter) _analyzer
                                .getAttribute("overwriteConstraint"))
                                .getExpression().equals("false")) {
                    return;
                }
                // Ask user for a decision.
                if (_analyzer == null && !isLogMode()) {
                    /*
                     * !MessageHandler.yesNoQuestion( "The constraint file \"" +
                     * filename + "\"" + " exists. OK to overwrite?")) {
                     */
                    // Don't overwrite, do nothing and return.
                    return;
                }
            } else {
                if (!constraintFile.getParentFile().exists()) {
                    if (!constraintFile.getParentFile().mkdirs()) {
                        throw new IllegalActionException(this,
                                "Failed to create \""
                                        + constraintFile.getParentFile()
                                                .getAbsolutePath()
                                        + "\" directory.");
                    }
                }
                if (!constraintFile.createNewFile()) {
                    throw new IllegalActionException(this,
                            "Failed to create \""
                                    + constraintFile.getAbsolutePath() + "\".");
                }
            }

            Writer writer = null;
            try {
                writer = new FileWriter(filename);
                for (String constraint : _trainedConstraints) {
                    writer.write(constraint + _eol);
                }
            } finally {
                writer.close();
            }

        } catch (IOException ex) {
            throw new PropertyResolutionException(this, ex,
                    "Failed to train the constraint log file \"" + filename
                            + "\".");
        }
    }

    public StringParameter actorConstraintType;

    public StringParameter compositeConnectionConstraintType;

    public StringParameter connectionConstraintType;

    public StringParameter expressionASTNodeConstraintType;

    public StringParameter fsmConstraintType;

    public FileParameter logDirectory;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    public Parameter logMode;

    /**
     * Indicate whether to compute the least or greatest fixed point solution.
     */
    public StringParameter solvingFixedPoint;

    public FileParameter trainedConstraintDirectory;

    /**
     * The set of Object that has been manually annotated.
     */
    private final HashSet<Object> _annotatedObjects = new HashSet<Object>();

    private final ConstraintManager _constraintManager = new ConstraintManager(
            this);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _logMode;

    private PropertyTermManager _propertyTermManager;

    /**
     * The set of trained constraints. This set is populated from parsing the
     * constraint file when training mode is off.
     */
    private final List<String> _trainedConstraints = new LinkedList<String>();

    protected static final String _USER_DEFINED_LATTICE = "Attribute::";

    protected static final String COLLECT_CONSTRAINTS = "COLLECT_CONSTRAINTS";

    protected static final String INITIALIZE_SOLVER = "INITIALIZE_SOLVER";

}
