/*  A extended base abstract class for a property solver.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.data.properties;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.properties.lattice.PropertyConstraintAttribute;
import ptolemy.data.properties.token.PropertyTokenAttribute;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////PropertySolver

/**
A extended base abstract class for a property solver.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 7.0
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
 */
public abstract class PropertySolver extends PropertySolverBase {

    /*
     *
     * @param container
     *
     * @param name
     *
     * @exception IllegalActionException
     *
     * @exception NameDuplicationException
     */
    public PropertySolver(NamedObj container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        action = new SharedParameter(this, "action", PropertySolver.class,
                TRAINING);
        action.setStringMode(true);
        _addActions(action);

        _highlighter = new PropertyMoMLHandler(this, "PropertyMoMLHandler");

        manualAnnotation = new Parameter(this, "manualAnnotation",
                BooleanToken.FALSE);
        manualAnnotation.setTypeEquals(BaseType.BOOLEAN);

        all = new SharedParameter(this, "all", PropertySolver.class, "false");
        all.setTypeEquals(BaseType.BOOLEAN);

        // FIXME: We do not want this GUI dependency here...
        // This attribute should be put in the MoML in the library instead
        // of here in the Java code.
        //new PropertyDisplayActions(this, "PropertyDisplayActions");
    }

    public void addErrors(String error) {
        _sharedUtilities.addErrors(error);
    }

    /*
     * Check if there is any regression testing errors after resolving
     * properties. If so, throw a new PropertyFailedRegressionTestException with
     * an error message that includes all the properties that does not match the
     * regression test values.
     *
     * @exception PropertyFailedRegressionTestException Thrown if there is any
     * errors in the regression test.
     */
    public void checkErrors() throws PropertyResolutionException {

        // FIXME: remove the errors as well.

        List errors = _sharedUtilities.removeErrors();

        if (!errors.isEmpty()) {
            String errorMessage = errors.toString();

            if (isTesting()) {
                throw new PropertyFailedRegressionTestException(this,
                        errorMessage);
            } else {
                throw new PropertyResolutionException(this, errorMessage);
            }
        }
    }

    public void checkResolutionErrors() throws IllegalActionException {
        for (Object propertyable : getAllPropertyables()) {
            _recordUnacceptableSolution(propertyable, getProperty(propertyable));
        }
        checkErrors();
    }

    /*
     * Clear the manual annotation constraints assoicated with this solver
     * use-case.
     *
     * @exception IllegalActionException Not Thrown.
     */
    public void clearAnnotations() throws IllegalActionException {

        for (PropertyHelper helper : getAllHelpers()) {
            if (helper.getComponent() instanceof NamedObj) {
                NamedObj namedObj = (NamedObj) helper.getComponent();

                for (AnnotationAttribute attribute : (List<AnnotationAttribute>) namedObj
                        .attributeList(AnnotationAttribute.class)) {

                    if (isIdentifiable(attribute.getUseCaseIdentifier())) {

                        try {
                            attribute.setContainer(null);
                        } catch (NameDuplicationException e) {
                            assert false;
                        }
                    }
                }
            }
        }
        _repaintGUI();
    }

    /**
     * If the value of the highlight parameter is set to
     * true, highlight the given property-able object with
     * the specified color associated with the given
     * property, if there exists any.
     * If the value of the showText parameter is true,
     * show the given property value for the given
     * property-able object. If the property is not null,
     * this looks for the _showInfo parameter in the
     * property-able object. Create a new _showInfo
     * StringParameter, if there does not already exists one.
     * Set its value to the given property value. If the
     * given property is null, this removes the _showInfo
     * parameter from the property-able object.
     * @exception IllegalActionException Thrown if an error
     * occurs when creating or setting the value for the
     * _showInfo parameter in the property-able object.
     * Thrown if an error occurs when creating or setting
     * the value for the highlightColor attribute in the
     * property-able object.
     */
     public void displayProperties() throws IllegalActionException {
        // Do nothing if we are not in a mode that allows display.
        if (!(isResolve() || isView())) {
            return;
        }

        if (_highlighter.highlight.getToken() == BooleanToken.TRUE) {
            _highlighter.highlightProperties();
        }
        if (_highlighter.showText.getToken() == BooleanToken.TRUE) {
            _highlighter.showProperties();
        }
    }

    public PropertyMoMLHandler getMoMLHandler() {
        return _highlighter;
    }

    /*
     * Return the previous resolved property for the given object.
     *
     * @param object The given object.
     *
     * @return The previous resolved property for the given object.
     */
    public Property getPreviousProperty(Object object) {
        return (Property) _previousProperties.get(object);
    }

    /*
     * Return the trained exception message string. If there is no trained
     * exception, an empty string is return.
     *
     * @return The trained exception message string.
     */
    public String getTrainedException() {
        StringAttribute attribute = (StringAttribute) getAttribute(_TRAINED_EXCEPTION_ATTRIBUTE_NAME);

        if (attribute == null) {
            return "";
        } else {
            return attribute.getExpression();
        }
    }

    /*
     * Return the name of the trained exception attribute.
     *
     * @return The name of the trained exception attribute.
     */
    public Attribute getTrainedExceptionAttribute() {
        return getAttribute(_TRAINED_EXCEPTION_ATTRIBUTE_NAME);
    }

    public String getTrainedExceptionAttributeName() {
        return _TRAINED_EXCEPTION_ATTRIBUTE_NAME;
    }

    /*
     * Increment the given field the solver statistics by a given number. This
     * is used for incrementing integer type statistics. If the given field does
     * not exist, it starts the count of the field at zero.
     *
     * @param field The given field of the solver statistics.
     *
     * @param increment The given number to increment by.
     */
    public void incrementStats(Object field, long increment) {
        incrementStats(_stats, field, increment);
    }

    public static void incrementStats(Map map, Object field, Number increment) {
        Number current = (Number) map.get(field);
        if (current == null) {
            current = 0;
        }
        map.put(field, current.longValue() + increment.longValue());
    }


    public boolean invokeSolver() {
        return invokeSolver(null);
    }

    public boolean invokeSolver(NamedObj analyzer) {
        boolean success = false;

        try {
            success = resolveProperties(analyzer, true);

            updateProperties();

            checkErrors();

            displayProperties();

            if (isTraining() && success) {
                setTesting();
            }

        } catch (KernelException e) {
            resetAll();
            throw new InternalErrorException(e);
        }

        return success;
    }

    public boolean isClear() {
        return action.getExpression().equals(PropertySolver.CLEAR);
    }

    /*
     * Return true if the solver can be identified by the given use-case string;
     * otherwise, false.
     *
     * @param usecase The given use-case label.
     *
     * @return True if the solver can be identified by the given use-case
     * string; otherwise, false.
     */
    public boolean isIdentifiable(String usecase) {
        return usecase.equals(getName()) || usecase.equals(getUseCaseName())
        || usecase.equals(getExtendedUseCaseName());
    }

    public boolean isManualAnnotate() {
        return manualAnnotation.getExpression().equals("true");
    }

    public boolean isResolve() {
        return ((action.getExpression().equals(ANNOTATE)) ||
                // (action.getExpression().equals(ANNOTATE_ALL)) ||
                // (action.getExpression().equals(MANUAL_ANNOTATE)) ||
                (action.getExpression().equals(TRAINING)));
    }

    public boolean isSettable(Object object) {
        return !_nonSettables.contains(object);
    }

    public boolean isTesting() {
        return action.getExpression().equals(PropertySolver.TEST);
    }

    public boolean isTraining() {
        return action.getExpression().equals(TRAINING);
    }

    public boolean isView() {
        return action.getExpression().equals(PropertySolver.VIEW);
    }

    /*
     * Record the previous property of the given object.
     *
     * @param object The given object.
     *
     * @param property The given property.
     */
    public void recordPreviousProperty(Object object, Property property) {
        _previousProperties.put(object, property);
    }

    /*
     * @param exceptionMessage
     *
     * @exception IllegalActionException
     */
    public void recordTrainedException(String exceptionMessage)
    throws IllegalActionException {
        StringAttribute attribute = (StringAttribute) getAttribute(_TRAINED_EXCEPTION_ATTRIBUTE_NAME);
        if (attribute == null) {

            try {
                attribute = new StringAttribute(this,
                        _TRAINED_EXCEPTION_ATTRIBUTE_NAME);

            } catch (NameDuplicationException e) {
                assert false;
            }
        }
        attribute.setExpression(exceptionMessage);
    }

    public void reset() {
        super.reset();
        _analyzer = null;
        _previousProperties = new HashMap<Object, Property>();
        _stats = new TreeMap<Object, Object>();
    }

    public void resolveProperties() throws KernelException {
        resolveProperties(_analyzer, false);
    }

    /*
     * Resolve the properties.
     *
     * @exception KernelException
     */
    public boolean resolveProperties(boolean isInvoked) throws KernelException {
        return resolveProperties(_analyzer, isInvoked);
    }

    /*
     * Resolve the properties (invoked from a ModelAnalyzer).
     *
     * @exception KernelException
     */
    public boolean resolveProperties(NamedObj analyzer)
    throws KernelException {
        return resolveProperties(analyzer, false);
    }

    /*
     * Resolve the property values for the top-level entity that contains the
     * solver.
     *
     * @param analyzer The model analyzer that invokes the solver. However, this
     * is null if the solver is invoked directly from its GUI.
     *
     * @param isInvoked Whether the solver is directly invoked or activated
     * through solver dependencies.
     *
     * @return True if resolution succeeds as expected; Otherwise, false.
     *
     * @exception IllegalActionException TODO
     */
    public boolean resolveProperties(NamedObj analyzer, boolean isInvoked)
    throws KernelException {

        boolean success = true;

        boolean noException = true;

        try {

            getSharedUtilities().addRanSolvers(this);

            _analyzer = analyzer;
            _isInvoked = isInvoked;

            // Clear the resolved properties for the chosen solver.
            String actionValue = action.getExpression();
            if (actionValue.equals(CLEAR_ANNOTATION)) {
                if (isInvoked) {
                    clearAnnotations();
                }
                return true;
            } else if (actionValue.equals(CLEAR)) {
                if (isInvoked) {
                    _resolvedProperties.clear();
                    _highlighter.clearProperties();
                    _highlighter.clearDisplay();
                }
                return true;

            } else if (actionValue.equals(VIEW)) {
                if (isInvoked) {
                    _highlighter.clearDisplay();
                    displayProperties();
                }
                return true;

            }

            // If this is not an intermediate (invoked) solver,
            // we need to clear the display.
            if (isInvoked && isResolve()) {
                PropertySolver previousSolver = _sharedUtilities._previousInvokedSolver;

                // Clear the display properties of the previous invoked solver.
                // If no solver is invoked previously, at least clear
                // the previous highlighting for this solver.
                if (previousSolver == null || previousSolver.getContainer() == null) {
                    previousSolver = this;
                }

                previousSolver._highlighter.clearDisplay();

                _sharedUtilities._previousInvokedSolver = this;

                // If we are in TRAINING mode, then keep
                // all the intermediate results.
                boolean keepIntermediates =
                    // actionValue.equals(ANNOTATE_ALL) ||
                    ((BooleanToken) all.getToken()).booleanValue()
                    || actionValue.equals(TRAINING);

                for (String solverName : _dependentUseCases) {
                    PropertySolver dependentSolver = findSolver(solverName);

                    dependentSolver.resolveProperties(analyzer,
                            keepIntermediates);

                    dependentSolver.updateProperties();
                }
            } else if (isInvoked && isTesting()) {
                for (String solverName : _dependentUseCases) {
                    PropertySolver dependentSolver = findSolver(solverName);

                    dependentSolver.resolveProperties(analyzer, false);
                }
            }
            _resolveProperties(analyzer);

            checkResolutionErrors();

        } catch (PropertyResolutionException ex) {
            noException = false;
            // resolution exceptions. that means resolution ended prematurely.
            // But that may not means that this is an improper behavior
            // Check whether we are expecting an exception,
            // if in testing mode, then add a RegressionTestErrorException
            PropertySolver failedSolver = (PropertySolver) ex.getSolver();

            // Remove '\r' characters to make Windows-Linux comparable strings.
            String trainedException = failedSolver.getTrainedException()
            .replaceAll("\r", "");
            String exception = ex.getMessage().replaceAll("\r", "");
            if (isTesting()) {
                if (!exception.equals(trainedException)) {
                    addErrors(PropertySolver
                            .getTrainedExceptionMismatchMessage(exception,
                                    trainedException));
                }
            } else if (isResolve()) {
                if (!exception.equals(trainedException)) {

                    // ask the user if this is expected,
                    boolean doRecord = MessageHandler
                    .yesNoQuestion(PropertySolver
                            .getTrainedExceptionMismatchMessage(
                                    exception, trainedException)
                                    + "Do you want to record it?");

                    if (doRecord) {
                        // If so, record the exception in ex.solver.
                        failedSolver.recordTrainedException(exception);
                    } else {
                        if (isTraining()) {
                            // Don't set mode to TEST because the user
                            // did not train (record) this exception.
                            success = false;
                        }
                        throw ex;
                    }
                }
            }
        }

        if (isTesting() && noException && getTrainedException().length() > 0) {
            // if in TEST mode, if there is a previously trained
            // RegressionTestErrorExceptionException
            // and we do not get one in the resolution,
            // then we throw an exception.
            addErrors(PropertySolver.getTrainedExceptionMismatchMessage("",
                    getTrainedException()));
        }

        return success;
    }

    public String setAction(String actionString) {
        String oldAction = action.getExpression();
        action.setExpression(actionString);
        return oldAction;
    }

    /*
     * Set the solver to testing mode.
     */
    public void setTesting() {
        action.setPersistent(true);
        setAction(TEST);
        _repaintGUI();
    }

    /*
     * Update the property. This method is called from both invoked and
     * auxiliary solvers.
     *
     * @exception IllegalActionException
     *
     * @exception IllegalActionException
     */
    public void updateProperties() throws IllegalActionException {
        if (isView() || isClear()) {
            return;
        }

        boolean hasDecided = false;
        boolean userDecision = true;

        // Only test the invoked solver.
        boolean doTest = isTesting() && _isInvoked;
        boolean doUpdate = isResolve();

        _addStatistics();

        for (Object propertyable : getAllPropertyables()) {

            if (!NamedObj.class.isInstance(propertyable)) {
                // FIXME: This happens when the propertyable is an ASTNodes,
                // or any non-Ptolemy objects. We are not updating their
                // property values, nor doing regression test for them.
                continue;
            }

            NamedObj namedObj = (NamedObj) propertyable;

            // Get the value resolved by the solver.
            Property property = getProperty(namedObj);

            if (doTest) { // Regression testing.
                _regressionTest(namedObj, property);

            } else if (doUpdate) {
                Property previous = getPreviousProperty(namedObj);

                if (!_isInvoked && !hasDecided) {

                    // Check if the previous and resolved properties are
                    // different.
                    if ((previous == null && property != null)
                            || previous != null && !previous.equals(property)) {

                        if (_analyzer == null) {
                            // Get user's decision.
                            userDecision = MessageHandler
                            .yesNoQuestion("Resolved auxilary property for \""
                                    + getExtendedUseCaseName()
                                    + "\" is different from previous. "
                                    + "Update this property?");
                        } else {
                            // Suppress the dialog.
                            userDecision = ((Parameter) _analyzer.getAttribute(
                            "overwriteDependentProperties")).getToken()
                            == BooleanToken.TRUE;

                        }
                        // Remember that we have made a decision.
                        hasDecided = true;
                    }
                }

                // Do nothing only if the previous resolved property
                // did not exist AND the user did not want to update.
                if (userDecision || previous != null) {

                    // Get the property attribute so we can either update
                    // its value or compare its value against the resolved
                    // value (regression testing).
                    PropertyAttribute attribute = _getPropertyAttribute(namedObj);
                    _updatePropertyAttribute(attribute, userDecision ? property
                            : previous);

                }
            }
        }

        System.out.println(_getStatsAsString(": "));

    }

    /*
     * Record tracing statistics.
     *
     * @exception IllegalActionException
     */
    protected void _addStatistics() throws IllegalActionException {
        _stats.put("# of helpers", _helperStore.size());
        _stats.put("# of propertyables", getAllPropertyables().size());
        _stats.put("# of resolved properties", _resolvedProperties.size());
        _stats.put("# of resolution errors", _sharedUtilities.getErrors()
                .size());
        _stats.put("has trained resolution errors", getTrainedException()
                .length() > 0);
    }

    /*
     * Get the propertyable attribute contained by the given propertyable.
     *
     * @param propertyable The given propertyable object.
     *
     * @return The property attribute contained by the given propertyable.
     *
     * @exception IllegalActionException
     */
    protected PropertyAttribute _getPropertyAttribute(NamedObj propertyable)
    throws IllegalActionException {
        PropertyAttribute attribute = null;

        // write results to attribute
        if (getExtendedUseCaseName().startsWith("lattice")) {
            attribute = (PropertyConstraintAttribute) propertyable
            .getAttribute(getExtendedUseCaseName());

            if (attribute == null) {
                try {
                    attribute = new PropertyConstraintAttribute(propertyable,
                            getExtendedUseCaseName());
                } catch (NameDuplicationException e) {
                    // This shouldn't happen. If another attribute
                    // has the same name, we should find it before.
                    assert false;
                }
            }
        } else if (getExtendedUseCaseName().startsWith("token")) {
            attribute = (PropertyTokenAttribute) propertyable
            .getAttribute(getExtendedUseCaseName());
            if (attribute == null) {
                try {
                    attribute = new PropertyTokenAttribute(propertyable,
                            getExtendedUseCaseName());
                } catch (NameDuplicationException e) {
                    // This shouldn't happen. See reason above.
                    assert false;
                }
            }
        } else {
            // FIXME: Error checking?
            throw new PropertyResolutionException(this, propertyable,
            "Failed to get the PropertyAttribute.");
        }
        return attribute;
    }

    /*
     * Return the string representation of the recorded statistics.
     *
     * @param separator The delimiter to separate the statistics fields.
     *
     * @return The string representation of the recorded statistics.
     */
    protected String _getStatsAsString(String separator) {
        StringBuffer result = new StringBuffer();
        for (Object field : _stats.keySet()) {
            result.append(field + separator + _stats.get(field) + _eol);
        }
        return result.toString();
    }

    /*
     * Prepare for automatic testing. In this base class, do nothing.
     */
    public void setOptions(Map options) {
        return;
    }

    /*
     * Check the given property against the trained property recorded on the
     * given NamedObj. It also restore the trained property that is temporarily
     * cleared for regression testing.
     *
     * @param namedObj The given NamedObj.
     *
     * @param property The given resolved property.
     *
     * @exception PropertyResolutionException Thrown if there are errors restoring
     * the trained property.
     */
    protected void _regressionTest(NamedObj namedObj, Property property)
    throws PropertyResolutionException {

        Property previousProperty = getPreviousProperty(namedObj);

        // Restore the previous resolved property, if there exists one.
        if (previousProperty != null) {
            try {
                PropertyAttribute attribute = _getPropertyAttribute(namedObj);
                _updatePropertyAttribute(attribute, previousProperty);

            } catch (IllegalActionException ex) {
                throw new PropertyResolutionException(this, ex);
            }
        }

        // The first check is for singleton elements, and the equals()
        // comparison is necessary for "equivalent" elements, such as
        // those in the SetLattice usecase.
        if ((previousProperty == null && property != null)
                || (previousProperty != null && !previousProperty
                        .equals(property))) {

            addErrors(_eol + "Property \"" + getUseCaseName()
                    + "\" resolution failed for " + namedObj.getFullName()
                    + "." + _eol + "    Trained value: \"" + previousProperty
                    + "\"; Resolved value: \"" + property + "\".");
        }
    }

    /*
     * Resolve the property values for the specified top-level entity. Print out
     * the name of the this solver. Sub-classes should overrides this method.
     *
     * @param analyzer The specified model analyzer.
     *
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected void _resolveProperties(NamedObj analyzer)
    throws KernelException {

        System.out.println("Invoking \"" + getName() + "\" ("
                + getExtendedUseCaseName() + "):");

    }

    private void _recordUnacceptableSolution(Object propertyable,
            Property property) {

        // Check for unacceptable solution.
        if ((property != null) && (!property.isAcceptableSolution())) {
            addErrors("Property \"" + property
                    + "\" is not an acceptable solution for " + propertyable
                    + "." + _eol);
        }
    }

    private void _repaintGUI() {
        requestChange(new ChangeRequest(this, "Repaint the GUI.") {
            protected void _execute() throws Exception {
            }
        });
    }

    /*
     * @param attribute
     *
     * @param property
     *
     * @exception IllegalActionException
     */
    private void _updatePropertyAttribute(PropertyAttribute attribute,
            Property property) throws IllegalActionException {
        if (property != null) {
            // Write results to attribute
            attribute.setExpression(property.toString());

        } else {
            attribute.setExpression("");
        }
    }

    /*
     * Return the error message string that shows the mismatch between the two
     * given exception strings. This method does not compare the content between
     * the input strings. It merely wraps the input strings into a larger error
     * message that says there is a mismatch between the two. This is used to
     * generate the error message for failed regression test that detects a
     * mismatch between the expected (trained) exception and the generate
     * exception.
     *
     * @param exception The first input error message.
     *
     * @param trainedException The second input error message.
     *
     * @return The exception message string.
     */
    public static String getTrainedExceptionMismatchMessage(String exception,
            String trainedException) {
        return "The generated exception:" + _eol
        + "-------------------------------------------------------"
        + _eol + exception + _eol
        + "-------------------------------------------------------"
        + _eol + " does not match the trained exception:" + _eol
        + "-------------------------------------------------------"
        + _eol + trainedException + _eol
        + "-------------------------------------------------------"
        + _eol;
    }


    /*
     *
     * @param actionParameter
     */
    protected static void _addActions(Parameter actionParameter) {
        actionParameter.addChoice(ANNOTATE);
        actionParameter.addChoice(CLEAR);
        actionParameter.addChoice(TEST);
        actionParameter.addChoice(TRAINING);
        actionParameter.addChoice(VIEW);
        actionParameter.addChoice(CLEAR_ANNOTATION);
    }

    public Map<Object, Object> getStats() {
        return _stats;
    }

    public Parameter action;

    public Parameter all;

    public Parameter manualAnnotation;

    public static final String NONDEEP_TEST_OPTION = "-nondeep";

    protected NamedObj _analyzer = null;

    /*
     * The PropertyHighlighter that controls the property visualization.
     */
    protected PropertyMoMLHandler _highlighter;

    protected boolean _isInvoked;

    private Map<Object, Object> _stats = new LinkedHashMap<Object, Object>();

    private HashMap<Object, Property> _previousProperties = new HashMap<Object, Property>();

    protected static final String _eol = StringUtilities
    .getProperty("line.separator");

    /* The display label for "annotate" in the action choices */
    protected static final String ANNOTATE = "ANNOTATE";

    /* The display label for "clear" in the action choices */
    protected static final String CLEAR = "CLEAR";

    /* The display label for "clear annotation" in the action choices */
    protected static final String CLEAR_ANNOTATION = "CLEAR_ANNOTATION";

    /* The display label for "test" in the action choices */
    protected static final String TEST = "TEST";

    /* The display label for "training" in the action choices */
    protected static final String TRAINING = "TRAINING";

    /* The display label for "view" in the action choices */
    protected static final String VIEW = "VIEW";

    private static String _TRAINED_EXCEPTION_ATTRIBUTE_NAME = "PropertyResolutionExceptionMessage";
}
