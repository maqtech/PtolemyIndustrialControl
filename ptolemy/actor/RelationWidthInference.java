/* A utility class to infer the width of relations that don't specify the width

Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.actor;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////RelationWidthInference

/**
A class that offers convenience utility methods to infer the widths of
relations in a composite actor.


@author Bert Rodiers
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Red (rodiers)
*/

public class RelationWidthInference {
   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////


    /**
     *  Infer the width of the relations for which no width has been
     *  specified yet. 
     *  The specified actor must be the top level container of the model.
     *  @exception IllegalActionException If the widths of the relations at port are not consistent
     *                  or if the width cannot be inferred for a relation.
     */
    public void inferWidths() throws IllegalActionException {
        if (_needsWidthInference) {
            long startTime = (new Date()).getTime();
            
            // widthChanged will become true if the width of a relation has been changed. In this
            // case we need to update the version of the workspace.        
            Boolean widthChanged = new Boolean(false);
            HashSet<IORelation> originalUnspecifiedSet = new HashSet<IORelation>();
            
            try {
                _topLevel.workspace().getWriteAccess();
                    
                Set<ComponentRelation> relationList = _topLevel.deepRelationSet();
                Set<IORelation> workingSet = new HashSet<IORelation>();
                HashSet<IORelation> unspecifiedSet = new HashSet<IORelation>();        
                
                for (ComponentRelation componentRelation : relationList) {
                    if (componentRelation instanceof IORelation) {
                        IORelation relation = (IORelation) componentRelation;                    
                        if (relation.needsWidthInference()) {
                            unspecifiedSet.add(relation);
                        }
                        if (relation.isWidthFixed()) {
                            // We know the width of this relation.
                            workingSet.add(relation);
                        } else {
                            // If connected to non-multiports => the relation should be 1                        
                            for (Object object : relation.linkedObjectsList()) {
                                if (object instanceof IOPort) {
                                    IOPort port = (IOPort) object;
                                    
                                    if (!port.isMultiport()) {
                                        boolean relationWidthChanged = relation._setInferredWidth(1);
                                            //FIXME: Can be zero in the case that this relation
                                            //      has no other port connected to it
                                        
                                        widthChanged = widthChanged || relationWidthChanged;    
                                        workingSet.add(relation);
                                        break; //Break the for loop.
                                    }                            
                                }
                            }
                        }
                    }
                }               
                
                originalUnspecifiedSet = (HashSet<IORelation>) unspecifiedSet.clone();
                
                LinkedList<IORelation> workingSet2 = new LinkedList<IORelation>(workingSet);
                
                System.out.println("Width inference - initialization: " +                
                        (System.currentTimeMillis() - startTime)
                                + " ms.");
                long afterinit = (new Date()).getTime();
                
                while (!workingSet2.isEmpty() && !unspecifiedSet.isEmpty()) {
                    IORelation relation = workingSet2.pop();
                    
                    unspecifiedSet.remove(relation);
                    assert  !relation.needsWidthInference(); 
                    int width = relation.getWidth();            
                    assert  width >= 0;
                    
                    //All relations in the same relation group need to have the same width
                    for (Object otherRelationObject : relation.relationGroupList()) {
                        IORelation otherRelation = (IORelation) otherRelationObject;
                        if (relation != otherRelation && otherRelation.needsWidthInference()) {
                            boolean relationWidthChanged = relation._setInferredWidth(width);
                            widthChanged = widthChanged || relationWidthChanged;
                            unspecifiedSet.remove(otherRelation);
                            // We don't need to add otherRelation to unspecifiedSet since
                            // we will process all ports linked to the complete relationGroup
                            // all at once.
                        }
                    }
                    
                    // Now we see whether we can determine the widths of relations directly connected
                    // at the multiports linked to this relation.
                    
                    // linkedPortList() can contain a port more than once. We only want
                    // them once. We will also only add multiports
                    HashSet<IOPort> multiports = new HashSet<IOPort>();
                    for (Object port : relation.linkedPortList()) {
                        if (((IOPort) port).isMultiport()) {
                            multiports.add((IOPort) port);
                        }
                    }
                    for (IOPort port : multiports) {
                        List<IORelation> updatedRelations = _relationsWithUpdatedWidthForMultiport(port, widthChanged);
                        for (IORelation otherRelation : updatedRelations) {
                            workingSet2.add(otherRelation);
                        }
                        
                    }                      
                }
                System.out.println("Actual algorithm: " +                
                        (System.currentTimeMillis() - afterinit)
                                + " ms.");                                
                
                if (!unspecifiedSet.isEmpty()) {
                    IORelation relation = unspecifiedSet.iterator().next();
                    throw new IllegalActionException(relation, 
                            "The width of relation " + relation.getFullName() +
                            " can not be uniquely inferred.\n"                        
                            + "Please make the width inference deterministic by"
                            + " explicitly specifying the width of this relation.");
                    
                }
            } finally {
    
                if (widthChanged) {
                    _topLevel.workspace().incrVersion();
                        // We increment the version here explicitly and don't call
                        // workpace.doneWriting() since we want to keep the lock until
                        // the end of this function. 
                    
    
                    // reset version for inferred widths. This is necessary since we
                    // incremented the version of the workspace
                    for (IORelation relation : originalUnspecifiedSet) {
                        relation._revalidateWidth();
                    }            
                }
                _topLevel.workspace().doneTemporaryWriting();
                System.out.println("Time to do width inference: " +                
                        (System.currentTimeMillis() - startTime)
                                + " ms.");
                
            }
            _needsWidthInference = false;            
        }
        
        
    }    

    /**
     *  Return whether the current widths of the relation in the model
     *  are no longer valid anymore and the widths need to be inferred again.
     *  @return True when width inference needs to be executed again.
     */
    public boolean needsWidthInference() {
        return _needsWidthInference;        
    }
    
    /**
     *  Notify the width inference algorithm that the connectivity in the model changed
     *  (width of relation changed, relations added, linked to different ports, ...).
     *  This will invalidate the current width inference.
     */
    public void notifyConnectivityChange() {
        _needsWidthInference = true;        
    }    

    /**
     * Set the top level to the value given as argument.
     * @param topLevel The top level CompositeActor.
     * @exception IllegalArgumentException If the specified actor is not the
     *   top level container. That is, its container is not null.
     */
     public void setTopLevel(CompositeActor compositeActor) {
         // Extra test for compositeActor != null since when the manager is changed
         // the old manager gets a null pointer as compositeActor.
         // Afterwards width inference should not be done anymore on this manager
         // (this will throw a null pointer exception since _topLevel will be set to null).
         if (compositeActor != null && compositeActor.getContainer() != null) {
             throw new IllegalArgumentException(
                     "TypedCompositeActor.resolveTypes: The specified actor is "
                             + "not the top level container.");
         }
         
         _topLevel = compositeActor;       
     }
     
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////   

    /**
     * Filter the relations for which the width still has to be inferred.
     * @param  relationList The relations that need to be filtered.
     * @return The relations for which the width still has to return. 
     */
    static private Set<IORelation> _relationsWithUnspecifiedWidths(List<?> relationList) {
        Set<IORelation> result = new HashSet<IORelation>();
        for (Object relation : relationList) {
            assert relation != null;
            if (((IORelation) relation).needsWidthInference()) {
                result.add((IORelation) relation);                
            }
        }
        return result;
    }

    
    /**
     * Infer the width for the relations connected to the port. If the width can be
     * inferred, update the width and return the relations for which the width has been
     * updated. If for one relation the width has actually been changed, widthChanged will be set
     * to true.
     * @param  port The port for whose connected relations the width should be inferred.
     * @param  widthChanged The parameter will be set to true if the width has been changed.
     * @return The relations for which the width has been updated. 
     * @exception IllegalActionException If the widths of the relations at port are not consistent.  
     */
    static private List<IORelation> _relationsWithUpdatedWidthForMultiport(IOPort port, Boolean widthChanged) throws IllegalActionException {              
        List<IORelation> result = new LinkedList<IORelation>();
        
        Set<IORelation> insideUnspecifiedWidths = _relationsWithUnspecifiedWidths(port.insideRelationList());
        int insideUnspecifiedWidthsSize = insideUnspecifiedWidths.size(); 

        Set<IORelation> outsideUnspecifiedWidths = _relationsWithUnspecifiedWidths(port.linkedRelationList());
                //port.linkedRelationList() returns the outside relations
        
        int outsideUnspecifiedWidthsSize = outsideUnspecifiedWidths.size();
        if ((insideUnspecifiedWidthsSize > 0 && outsideUnspecifiedWidthsSize > 0)
                || (insideUnspecifiedWidthsSize == 0 && outsideUnspecifiedWidthsSize == 0)) {
            return result;
        }

        int insideWidth = port._getInsideWidth(null);
        int outsideWidth = port._getOutsideWidth(null);
        
        Set<IORelation> unspecifiedWidths = null;
        int difference = -1;
        
        if (insideUnspecifiedWidthsSize > 0) {
            unspecifiedWidths = insideUnspecifiedWidths;
            difference = outsideWidth - insideWidth;
        } else {
            assert outsideUnspecifiedWidthsSize > 0;
            unspecifiedWidths = outsideUnspecifiedWidths;
            difference = insideWidth - outsideWidth;            
        }
        if (difference < 0) {
            throw new IllegalActionException(port, 
                    "The inside and outside widths of port " + port.getFullName()
                    + " are not consistent.\nThe inferred width of relation "
                    + unspecifiedWidths.iterator().next().getFullName()
                    + " would be negative.");            
        }

        int unspecifiedWidthsSize = unspecifiedWidths.size();
        
        if (difference > 0 && difference < unspecifiedWidthsSize) {
            throw new IllegalActionException(port, 
                    "The inside and outside widths of port " + port.getFullName()
                    + " are not consistent.\n Can't determine a unique width for relation"
                    + unspecifiedWidths.iterator().next().getFullName());            
        }         
        
        if (unspecifiedWidthsSize == 1 || unspecifiedWidthsSize == difference || difference == 0) {
            int width = difference / unspecifiedWidthsSize;
            for (IORelation relation : unspecifiedWidths) {
                boolean relationWidthChanged = relation._setInferredWidth(width);
                widthChanged = widthChanged || relationWidthChanged;
                result.add(relation);
            }
        }
        return result;               
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    //True when width inference needs to happen again
    private boolean _needsWidthInference = true;
    
    //The top level of the model.    
    private CompositeActor _topLevel = null;

       
}
