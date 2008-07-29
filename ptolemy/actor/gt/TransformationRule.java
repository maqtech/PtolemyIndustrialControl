/*

@Copyright (c) 2007-2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.gt;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.util.VariableScope;
import ptolemy.actor.lib.hoc.MultiCompositeActor;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ActorToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ScopeExtender;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// TransformationRule

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class TransformationRule extends MultiCompositeActor implements
        GTCompositeActor, MatchCallback, ValueListener {

    public TransformationRule(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    public TransformationRule(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == matchOnly) {
            // Show or hide other parameters if matchOnly is set/unset.
            if (isMatchOnly()) {
                mode.setVisibility(Settable.EXPERT);
                repeatCount.setVisibility(Settable.EXPERT);
                repeatUntilFixpoint.setVisibility(Settable.EXPERT);
            } else {
                mode.setVisibility(Settable.FULL);
                repeatCount.setVisibility(Settable.FULL);
                repeatUntilFixpoint.setVisibility(Settable.FULL);
            }

        } else if (attribute == mode || attribute == repeatUntilFixpoint) {
            // Check whether the mode is not set to single run but
            // repeatUntilFixpoint is set to true. If so, raise exception.
            String modeString = mode.getExpression();
            boolean singleRunMode =
                modeString.equals(Mode.REPLACE_FIRST.toString())
                    || modeString.equals(Mode.REPLACE_ANY.toString())
                    || modeString.equals(Mode.REPLACE_ALL.toString());
            boolean repeat = ((BooleanToken) repeatUntilFixpoint.getToken())
                    .booleanValue();
            if (repeat && !singleRunMode) {
                throw new IllegalActionException("When the mode is set to \""
                        + modeString + "\", repeatUntilFixpoint must be "
                        + "false.");
            }
        }

        super.attributeChanged(attribute);
    }

    public Object clone() throws CloneNotSupportedException {
        TransformationRule actor = (TransformationRule) super.clone();
        actor._lastModel = null;
        actor._lastResults = new LinkedList<MatchResult>();
        actor._random = new Random();
        return actor;
    }

    public void fire() throws IllegalActionException {
        try {
            // Obtain updated value for any PortParameter before each firing.
            try {
                _workspace.getReadAccess();
                for (Object parameterObject : attributeList()) {
                    if (parameterObject instanceof PortParameter) {
                        ((PortParameter) parameterObject).update();
                    }
                }
            } finally {
                _workspace.doneReading();
            }

            if (modelInput.hasToken(0)) {
                ActorToken token = (ActorToken) modelInput.get(0);
                _lastModel = (CompositeEntity) token.getEntity(new Workspace());
                _lastModel.setDeferringChangeRequests(false);

                Mode mode = _getMode();

                GraphMatcher matcher = new GraphMatcher();
                matcher.setMatchCallback(this);

                boolean isMatchOnly = isMatchOnly();
                _collectAllMatches = !isMatchOnly && mode != Mode.REPLACE_FIRST;
                _lastResults.clear();

                // Obtain a working copy of this transformation rule, which can
                // be safely modified in the process of pattern matching.
                TransformationRule workingCopy = _getWorkingCopy();

                // Transfer the most updated values of the PortParameters to the
                // working copy.
                for (Object parameterObject : attributeList()) {
                    if (parameterObject instanceof PortParameter) {
                        PortParameter param = (PortParameter) parameterObject;
                        Token paramToken = param.getToken();
                        PortParameter paramCopy = (PortParameter) _workingCopy
                                .getAttribute(param.getName());
                        paramCopy.setToken(paramToken);
                    }
                }
                matcher.match(workingCopy.getPattern(), _lastModel);

                if (isMatchOnly) {
                    matched.send(0, BooleanToken.getInstance(
                            !_lastResults.isEmpty()));
                    _lastResults.clear();
                    return;
                }

                if (mode == Mode.REPLACE_FIRST || mode == Mode.REPLACE_ANY
                        || mode == Mode.REPLACE_ALL) {
                    boolean foundMatch = !_lastResults.isEmpty();
                    if (foundMatch) {
                        boolean untilFixpoint = ((BooleanToken)
                                repeatUntilFixpoint.getToken()).booleanValue();
                        long count = LongToken.convert(repeatCount.getToken())
                                .longValue();
                        while (!_lastResults.isEmpty()) {
                            if (count <= 0) {
                                break;
                            }

                            switch (mode) {
                            case REPLACE_FIRST:
                                MatchResult result = _lastResults.peek();
                                GraphTransformer.transform(workingCopy, result);
                                break;
                            case REPLACE_ANY:
                                result = _lastResults.get(_random
                                        .nextInt(_lastResults.size()));
                                GraphTransformer.transform(workingCopy, result);
                                break;
                            case REPLACE_ALL:
                                GraphTransformer.transform(workingCopy,
                                        _lastResults);
                                break;
                            }
                            if (!untilFixpoint && --count <= 0) {
                                break;
                            }
                            _lastResults.clear();
                            matcher.match(workingCopy.getPattern(), _lastModel);
                        }
                    }

                    modelOutput.send(0, new ActorToken(_lastModel));
                    matched.send(0, BooleanToken.getInstance(foundMatch));
                    return;
                }
            }

            if (matchInput.getWidth() > 0 && matchInput.hasToken(0)
                    && _lastModel != null) {
                ObjectToken token = (ObjectToken) matchInput.get(0);
                MatchResult match = (MatchResult) token.getValue();
                if (match != null) {
                    TransformationRule workingCopy = _getWorkingCopy();
                    CompositeEntity host = (CompositeEntity) match
                            .get(workingCopy.getPattern());
                    if (_lastModel != host && !_lastModel.deepContains(host)) {
                        throw new IllegalActionException(this,
                                "The match result cannot be used with the "
                                        + "current model.");
                    }
                    GraphTransformer.transform(workingCopy, match);
                    modelOutput.send(0, new ActorToken(_lastModel));
                }
            }

            if (trigger.getWidth() > 0 && trigger.hasToken(0)
                    && !_lastResults.isEmpty()) {
                trigger.get(0);
                _lastResultsOperation = LastResultsOperation.REMOVE_FIRST;
                MatchResult result = _lastResults.peek();
                matchOutput.send(0, new ObjectToken(result));
            }
        } catch (TransformationException e) {
            throw new IllegalActionException(this, e,
                    "Unable to transform model.");
        }

        remaining.send(0, new IntToken(_lastResults.size()));
    }

    public boolean foundMatch(GraphMatcher matcher) {
        _lastResults.add((MatchResult) matcher.getMatchResult().clone());
        return !_collectAllMatches;
    }

    public Pattern getPattern() {
        return (Pattern) getEntity("Pattern");
    }

    public Replacement getReplacement() {
        return (Replacement) getEntity("Replacement");
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastModel = null;
        _lastResults.clear();
    }

    public boolean isMatchOnly() {
        try {
            return ((BooleanToken) matchOnly.getToken()).booleanValue();
        } catch (IllegalActionException e) {
            return false;
        }
    }

    public boolean postfire() throws IllegalActionException {
        switch (_lastResultsOperation) {
        case CLEAR:
            _lastResults.clear();
            break;
        case NONE:
            break;
        case REMOVE_FIRST:
            _lastResults.poll();
            break;
        }
        return true;
    }

    public boolean prefire() throws IllegalActionException {
        _lastResultsOperation = LastResultsOperation.NONE;
        String modeString = mode.getExpression();
        if (modeString.equals(Mode.REPLACE_FIRST.toString())
                || modeString.equals(Mode.REPLACE_ANY.toString())
                || modeString.equals(Mode.REPLACE_ALL.toString())) {
            return modelInput.hasToken(0);
        } else {
            return modelInput.hasToken(0) || matchInput.getWidth() > 0
                    && matchInput.hasToken(0) && _lastModel != null
                    || trigger.getWidth() > 0 && trigger.hasToken(0)
                    && !_lastResults.isEmpty();
        }
    }

    public Set<Inequality> typeConstraints() throws IllegalActionException {
        return _EMPTY_SET;
    }

    public void valueChanged(Settable settable) {
        // Create or remove ports depending on the mode.
        if (mode != null && matchOnly != null
                && (settable == mode || settable == matchOnly)) {
            try {
                boolean showFullControlPorts = false;
                boolean isMatchOnly = isMatchOnly();
                if (!isMatchOnly) {
                    String modeString = mode.getExpression();
                    showFullControlPorts = modeString.equals(Mode.EXPERT
                            .toString());
                }

                if (modelInput == null) {
                    modelInput = new TypedIOPort(this, "modelInput", true,
                            false);
                    modelInput.setTypeEquals(ActorToken.TYPE);
                    modelInput.setPersistent(false);
                }

                if (showFullControlPorts) {
                    if (matchInput == null) {
                        matchInput = new TypedIOPort(this, "matchInput", true,
                                false);
                        matchInput.setTypeEquals(BaseType.OBJECT);
                        matchInput.setPersistent(false);
                    }
                    if (matchOutput == null) {
                        matchOutput = new TypedIOPort(this, "matchOutput",
                                false, true);
                        matchOutput.setTypeEquals(BaseType.OBJECT);
                        matchOutput.setPersistent(false);
                    }
                    if (modelOutput == null) {
                        modelOutput = new TypedIOPort(this, "modelOutput",
                                false, true);
                        modelOutput.setTypeEquals(ActorToken.TYPE);
                        modelOutput.setPersistent(false);
                    }
                    if (trigger == null) {
                        trigger = new TypedIOPort(this, "trigger", true, false);
                        trigger.setTypeEquals(BaseType.BOOLEAN);
                        trigger.setPersistent(false);
                        new StringAttribute(trigger, "_cardinal")
                                .setExpression("SOUTH");
                    }
                    if (remaining == null) {
                        remaining = new TypedIOPort(this, "remaining", false,
                                true);
                        remaining.setTypeEquals(BaseType.INT);
                        remaining.setPersistent(false);
                        new StringAttribute(remaining, "_cardinal")
                                .setExpression("SOUTH");
                    }
                    if (matched != null) {
                        matched.setContainer(null);
                        matched = null;
                    }
                } else {
                    if (matchInput != null) {
                        matchInput.setContainer(null);
                        matchInput = null;
                    }
                    if (matchOutput != null) {
                        matchOutput.setContainer(null);
                        matchOutput = null;
                    }
                    if (trigger != null) {
                        trigger.setContainer(null);
                        trigger = null;
                    }
                    if (remaining != null) {
                        remaining.setContainer(null);
                        remaining = null;
                    }
                    if (isMatchOnly) {
                        if (modelOutput != null) {
                            modelOutput.setContainer(null);
                            modelOutput = null;
                        }
                    } else {
                        if (modelOutput == null) {
                            modelOutput = new TypedIOPort(this, "modelOutput",
                                    false, true);
                            modelOutput.setTypeEquals(ActorToken.TYPE);
                            modelOutput.setPersistent(false);
                        }
                        if (matched == null) {
                            matched = new TypedIOPort(this, "matched", false,
                                    true);
                            matched.setTypeEquals(BaseType.BOOLEAN);
                            matched.setPersistent(false);
                            new StringAttribute(matched, "_cardinal")
                                    .setExpression("SOUTH");
                        }
                    }
                }
            } catch (KernelException e) {
                throw new InternalErrorException(this, e,
                        "Cannot add or remove port.");
            }
        }
    }

    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _lastResults.clear();
    }

    public TypedIOPort matchInput;

    public Parameter matchOnly;

    public TypedIOPort matchOutput;

    public TypedIOPort matched;

    public StringParameter mode;

    public TypedIOPort modelInput;

    public TypedIOPort modelOutput;

    public TypedIOPort remaining;

    public Parameter repeatCount;

    public Parameter repeatUntilFixpoint;

    public TypedIOPort trigger;

    public static class TransformationDirector extends Director {

        public TransformationDirector(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            setClassName("ptolemy.actor.gt.TransformationRule$GTDirector");
        }

        public void initialize() throws IllegalActionException {
        }

        public void preinitialize() throws IllegalActionException {
        }

        public void wrapup() throws IllegalActionException {
        }
    }

    public enum Mode {
        EXPERT {
            public String toString() {
                return "full control";
            }
        },
        REPLACE_ALL {
            public String toString() {
                return "replace all";
            }
        },
        REPLACE_ANY {
            public String toString() {
                return "replace any";
            }
        },
        REPLACE_FIRST {
            public String toString() {
                return "replace first";
            }
        }
    }

    protected TransformationRule _getWorkingCopy()
    throws IllegalActionException {
        if (_workingCopyVersion != _workspace.getVersion()) {
            try {
                _workingCopy = (TransformationRule) clone(new Workspace());
                new WorkingCopyScopeExtender(_workingCopy, "_scopeExtender");
            } catch (Exception e) {
                throw new IllegalActionException(this, e, "Cannot get a " +
                        "working copy this transformation rule.");
            }
            _workingCopyVersion = _workspace.getVersion();
        }
        return _workingCopy;
    }

    protected void _init() throws IllegalActionException,
            NameDuplicationException {
        setClassName("ptolemy.actor.gt.TransformationRule");

        // Create the default refinement.
        new Pattern(this, "Pattern");
        new Replacement(this, "Replacement");

        mode = new StringParameter(this, "mode");
        for (int i = Mode.values().length - 1; i >= 0; i--) {
            mode.addChoice(Mode.values()[i].toString());
        }
        mode.addValueListener(this);
        mode.setExpression(Mode.REPLACE_FIRST.toString());

        repeatUntilFixpoint = new Parameter(this, "repeatUntilFixpoint");
        repeatUntilFixpoint.setTypeEquals(BaseType.BOOLEAN);
        repeatUntilFixpoint.setToken(BooleanToken.FALSE);

        repeatCount = new Parameter(this, "repeatCount");
        repeatCount.setTypeAtMost(BaseType.LONG);
        repeatCount.setExpression("1");

        matchOnly = new Parameter(this, "matcherOnly");
        matchOnly.setTypeEquals(BaseType.BOOLEAN);
        matchOnly.addValueListener(this);
        matchOnly.setToken(BooleanToken.FALSE);

        new TransformationDirector(this, "GTDirector");
    }

    private Mode _getMode() throws IllegalActionException {
        String modeString = mode.getExpression();
        if (modeString.equals(Mode.REPLACE_FIRST.toString())) {
            return Mode.REPLACE_FIRST;
        } else if (modeString.equals(Mode.REPLACE_ANY.toString())) {
            return Mode.REPLACE_ANY;
        } else if (modeString.equals(Mode.REPLACE_ALL.toString())) {
            return Mode.REPLACE_ALL;
        } else if (modeString.equals(Mode.EXPERT.toString())) {
            return Mode.EXPERT;
        } else {
            throw new IllegalActionException("Unexpected mode: " + modeString);
        }
    }

    private static final Set<Inequality> _EMPTY_SET = new HashSet<Inequality>();

    private boolean _collectAllMatches;

    private CompositeEntity _lastModel;

    private LinkedList<MatchResult> _lastResults =
        new LinkedList<MatchResult>();

    private LastResultsOperation _lastResultsOperation;

    private Random _random = new Random();

    private TransformationRule _workingCopy;

    private long _workingCopyVersion = -1;

    private class WorkingCopyScopeExtender extends Attribute
    implements ScopeExtender {

        public List<?> attributeList() {
            NamedObj container = TransformationRule.this;
            Set<?> names = VariableScope.getAllScopedVariableNames(null,
                    container);
            List<Variable> variables = new LinkedList<Variable>();
            for (Object name : names) {
                variables.add(VariableScope.getScopedVariable(null, container,
                        (String) name));
            }
            return variables;
        }

        public Attribute getAttribute(String name) {
            NamedObj container = TransformationRule.this;
            return VariableScope.getScopedVariable(null, container, name);
        }

        WorkingCopyScopeExtender(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }
    }

    private enum LastResultsOperation {
        CLEAR, NONE, REMOVE_FIRST
    }

}
