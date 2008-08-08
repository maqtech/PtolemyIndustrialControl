package ptolemy.data.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.kernel.util.Attribute;

public class SharedUtilities {

    //private SharedParameter _sharedParameter;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    public SharedUtilities(SharedParameter parameter) {
        //_sharedParameter = parameter;
        id = count++;
    }
    
    public void resetAll() {
        _ranSolvers = new HashSet<PropertySolver>();
        _parseTrees = new HashMap<Attribute, ASTPtRootNode>();
        _attributes = new HashMap<ASTPtRootNode, Attribute>();
        _errors = new ArrayList<String>();

        PropertyHelper.resetAll();
    }
    
    /**
     * @param _parseTrees the _parseTrees to set
     */
    public void putParseTrees(Attribute attribute, ASTPtRootNode root) {
        _parseTrees.put(attribute, root);
    }

    /**
     * @return the _parseTrees
     */
    public Map<Attribute, ASTPtRootNode> getParseTrees() {
        return _parseTrees;
    }

    public String toString() {
        String result = "sharedUtilities#" + id;
        return result;
    }
    
    /**
     * @param _attributes the _attributes to set
     */
    protected void putAttributes(ASTPtRootNode node, Attribute attribute) {
        _attributes.put(node, attribute);
    }
    
    /**
     * @return the _attributes
     */
    public  Map<ASTPtRootNode, Attribute> getAttributes() {
        return _attributes;
    }

    public int id;
    public static int count = 0;
    
    /**
     * The set of solvers that have already been invoked.
     */
    private HashSet<PropertySolver> _ranSolvers = new HashSet<PropertySolver>();

    private Map<Attribute, ASTPtRootNode> _parseTrees = 
        new HashMap<Attribute, ASTPtRootNode>();

    private Map<ASTPtRootNode, Attribute> _attributes = 
        new HashMap<ASTPtRootNode, Attribute>();
            
    private ArrayList<String> _errors = new ArrayList<String>();

    protected PropertySolver _previousInvokedSolver = null;

    public Set<PropertySolver> getRanSolvers() {
        return _ranSolvers;
    }

    public void addRanSolvers(PropertySolver solver) {
        _ranSolvers.add(solver);
    }

    /**
     * @return the _errors
     */
    public void addErrors(String error) {
        _errors.add(error);
    }

    /**
     * @return the _errors
     */
    public ArrayList<String> getErrors() {
        Collections.sort(_errors);
        return _errors;
    }

    public List removeErrors() {
        List result = new ArrayList(_errors);
        _errors.clear();
	return result;
    }
}
