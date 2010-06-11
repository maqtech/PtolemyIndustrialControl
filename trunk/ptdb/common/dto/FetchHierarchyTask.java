/*
 * 
 */
package ptdb.common.dto;

import java.util.ArrayList;

///////////////////////////////////////////////////////////////
//// FetchHierarchyTask

/**
 * Contain the list of models to fetch the parent hierarchy for them. 
 * 
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class FetchHierarchyTask extends Task {

    /**
     *  Construct an instance of FetchHierarchyTask
     *  and set it as a select task.  
     */
    public FetchHierarchyTask() {
        _isUpdateTask = false;
    }

    //////////////////////////////////////////////////////////////////////
    ////		public variables 				//////

    //////////////////////////////////////////////////////////////////////
    ////		public methods 					//////

    /**
     * Return the list of models for which we need 
     * to fetch parent hierarchy. 
     * 
     * @return - List of models for which we need to 
     * fetch hierarchy.
     */
    public ArrayList<XMLDBModel> getModelsList() {
        return _modelsList;
    }

    /**
     * Set the list of models for which we need 
     * to fetch parent hierarchy.
     *  
     * @param modelsList- List of models for which we need to 
     * fetch hierarchy.
     */
    public void setModelsList(ArrayList<XMLDBModel> modelsList) {
        _modelsList = modelsList;
    }

    //////////////////////////////////////////////////////////////////////
    ////		protected methods 				//////

    //////////////////////////////////////////////////////////////////////
    ////		protected variables 				//////

    //////////////////////////////////////////////////////////////////////
    ////		private methods 				//////

    //////////////////////////////////////////////////////////////////////
    ////		private variables				//////
    /**
     * List of models for which we need to 
     * fetch hierarchy.
     */
    private ArrayList<XMLDBModel> _modelsList;

}
