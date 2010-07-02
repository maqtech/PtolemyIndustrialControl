/*
 *
 */
package ptdb.kernel.bl.search;

import java.util.ArrayList;

import ptdb.common.dto.DBGraphSearchCriteria;
import ptdb.common.dto.SearchCriteria;

///////////////////////////////////////////////////////////////////
//// GraphSearcher

/**
 * Inherits from the AbstractSearch, and contains the common
 * functions used in the graph search.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public abstract class GraphSearcher extends AbstractSearcher {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * A factory which returns the concrete GraphSearcher objects
     * according to the complexity of the search criteria from the user.
     *
     * @param searchCriteria The search criteria input by the user.
     * @return The list that contains the required graph searchers, created
     *  according to the search criteria.  The searchers in the returned list
     *  should be used in their order in the list.
     */
    public static ArrayList<GraphSearcher> getGraphSearcher(
            SearchCriteria searchCriteria) {

        // For now, construct two graph searchers, which are the 
        // XQueryGraphSearcher and PatternMatchGraphSearcher, and return both
        // of them in a list. 
        // The first searcher in the list will be the instance of the 
        // XQueryGraphSearcher, and the second will be 
        // PatternMatchGraphSearcher. 

        ArrayList<GraphSearcher> graphSearchers = new ArrayList<GraphSearcher>();

        XQueryGraphSearcher xQueryGraphSearcher = new XQueryGraphSearcher(
                searchCriteria.getDBGraphSearchCriteria());

        graphSearchers.add(xQueryGraphSearcher);

        PatternMatchGraphSearcher patternMatchGraphSearcher = new PatternMatchGraphSearcher(
                searchCriteria.getDBGraphSearchCriteria());
        graphSearchers.add(patternMatchGraphSearcher);

        return graphSearchers;
    }

    ///////////////////////////////////////////////////////////////////
    ////        protected methods                                  ////

    @Override
    protected boolean _isSearchCriteriaSet() {

        if (_dbGraphSearchCriteria != null
                && (!_dbGraphSearchCriteria.getPortsList().isEmpty()
                        || !_dbGraphSearchCriteria.getComponentEntitiesList()
                                .isEmpty() || !_dbGraphSearchCriteria
                        .getCompositeEntities().isEmpty())) {
            return true;
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////        protected variables                                ////

    /**
     * The search criteria from graph pattern matching that input by the 
     * user. 
     */
    protected DBGraphSearchCriteria _dbGraphSearchCriteria;

}
