/* A product lattice-based ontology.
 * 
 * Copyright (c) 2007-2010 The Regents of the University of California. All
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
 * 
 */

package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.List;

import ptolemy.data.ArrayToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.ObjectType;
import ptolemy.graph.CPO;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ProductLatticeOntology

/** A product lattice-based ontologies.
 *  Represents an ontology based on a lattice derived from the cross product
 *  of a set of other lattice-based ontologies. Given the specified ontologies,
 *  the list of concepts for the product lattice ontology is automatically
 *  generated by taking all possible tuple combinations for each individual
 *  lattice ontology.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeOntology extends Ontology {

    /** Create a new ProductLatticeOntology with the specified container and
     *  the specified name.
     *  @param container The container.
     *  @param name The name for the ontology.
     *  @throws NameDuplicationException If the container already contains an
     *   ontology with the specified name.
     *  @throws IllegalActionException If the base class throws it.
     */
    public ProductLatticeOntology(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);        
        _initAttributes();
    }

    /** Create a new ProductLatticeOntology with no container or name.
     *  @param workspace The workspace into which to put it.
     *  @exception IllegalActionException If the base class throws it.
     */
    public ProductLatticeOntology(Workspace workspace)
            throws IllegalActionException {
        super(workspace);        
        _initAttributes();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    
    /** The parameter that holds the user-specified array of ontologies from
     *  which the product lattice ontology is derived.
     */
    public Parameter latticeOntologies;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes. When the latticeOntologies
     *  parameter is changed, a new list of {@link ProductLatticeConcept}s is generated
     *  based on the array of Ontologies contained in that parameter.
     *  @param attribute The attribute that changed.
     *  @throws IllegalActionException If the product lattice concepts
     *   cannot be generated.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == latticeOntologies) {
            // When the latticeOntologies parameter changes, delete all the
            // previous concepts contained in the ontology.
            _removeAllConcepts();
            
            List<Ontology> ontologiesList = getLatticeOntologies();           
            if (ontologiesList != null) {
                List<List<Concept>> conceptTuples = _createAllConceptTuples(ontologiesList);
                for (List<Concept> tuple : conceptTuples) {
                    try {
                        String conceptName = _getNameFromConceptTuple(tuple);
                        ProductLatticeConcept newConcept = new ProductLatticeConcept(this, conceptName, tuple);
                        _conceptList.add(newConcept);
                    } catch (NameDuplicationException nameDupEx) {
                        throw new IllegalActionException(this, nameDupEx, "Could not " +
                         "create ProductLatticeConcept for the ProductLatticeOntology.");
                    }
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Get the complete partial order for this product lattice ontology.
     *  @return The complete partial order which is a {@link ProductLatticeCPO}.
     */
    public CPO getCompletePartialOrder() {
        if (workspace().getVersion() != _cpoVersion) {            
            if (_conceptList != null && !_conceptList.isEmpty()) {
                _cpo = new ProductLatticeCPO(_conceptList);
            } else {
                _cpo = null;
            }
            
            // Set the CPO version after creating the new CPO
            _cpoVersion = workspace().getVersion();
        }
        
        return _cpo;
    }
    
    /** Return the list of lattice ontologies that comprise the product
     *  lattice ontology.
     *  @return The list of lattice ontology objects.
     *  @throws IllegalActionException If the latticeOntologies parameter does
     *   not return an array token that contains the ontology objects, or
     *   at least one of the specified component ontologies is not a lattice.
     */
    public List<Ontology> getLatticeOntologies() throws IllegalActionException {
        if (workspace().getVersion() != _latticeVersion) {        
            // The latticeOntologies parameter is type constrainted to always
            // contain an array token of Ontology objects.
            ArrayToken ontologies = (ArrayToken) latticeOntologies.getToken();

            if (ontologies != null && ontologies.length() != 0) {
                Token[] ontologiesTokenArray = (Token[]) ontologies.arrayValue();            
                List<Ontology> ontologiesList = new ArrayList<Ontology>();
                for (int i = 0; i < ontologiesTokenArray.length; i++) {     
                    Ontology ontology = (Ontology) ((ObjectToken) ontologiesTokenArray[i]).getValue(); 
                    if (ontology != null) {
                        if (ontology.isLattice()) {
                            ontologiesList.add(ontology);
                        } else {
                            throw new IllegalActionException(this, "All the ontologies" +
                                    " that comprise a product lattice ontology" +
                                    " must be lattices. The ontology " +
                                    ontology.getName() + " is not a lattice.");
                        }
                    }
                }

                _latticeOntologies = ontologiesList;
            } else {
                _latticeOntologies = null;
            }
            
            // Set the lattice version after creating the new list of
            // lattice ontologies.
            _latticeVersion = workspace().getVersion();
        }
        
        return _latticeOntologies;
    }
    
    /** Return true if the product lattice ontology is a lattice. If all the
     *  component onotlogies are lattices, then the product lattice ontology
     *  will also be a lattice. This is determined by the complete partial order
     *  for this lattice.
     *  @return true if the product lattice ontology is a lattice, false
     *   otherwise.
     *  @see #getCompletePartialOrder()
     *  @see ProductLatticeCPO
     */
    public boolean isLattice() {
        CPO ontologyCPO = getCompletePartialOrder();
        
        if (ontologyCPO != null) {
            return ontologyCPO.isLattice();
        } else {
            return false;
        }        
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Create all combinations of concept tuples from the given list of
     *  ontologies. This list is used to generate the list of
     *  {@link ProductLatticeConcept}s that define the product lattice ontology.
     *  The method is recursive.
     * 
     *  @param ontologiesList The given list of ontologies that comprise this
     *   product lattice ontology.
     *  @return The list of all combinations of concept tuples for the given
     *   ontologies.
     */
    private List<List<Concept>> _createAllConceptTuples(List<Ontology> ontologiesList) {
        List<List<Concept>> conceptTupleList = new ArrayList<List<Concept>>();        
        
        if (ontologiesList != null && !ontologiesList.isEmpty()) {            
            Ontology firstOntology = ontologiesList.get(0);
            List<Ontology> otherOntologies = new ArrayList<Ontology>(ontologiesList);
            otherOntologies.remove(0);

            for (Object concept : firstOntology.entityList(Concept.class)) {
                if (otherOntologies.isEmpty()) {
                    List<Concept> newTuple = new ArrayList<Concept>();
                    newTuple.add((Concept) concept);
                    conceptTupleList.add(newTuple);
                } else {
                    List<List<Concept>> subTuplesList = _createAllConceptTuples(otherOntologies);
                    for (List<Concept> conceptTuple : subTuplesList) {
                        List<Concept> newTuple = new ArrayList<Concept>();
                        newTuple.add((Concept) concept);
                        newTuple.addAll(conceptTuple);
                        
                        conceptTupleList.add(newTuple);
                    }
                }
            }
        }
        
        return conceptTupleList;
    }

    /** Create the name of the ProductLatticeConcept by concatenating its given
     *  list of concepts from a tuple of concepts.
     * 
     *  @param tuple The given concept tuple list.
     *  @return The name of the product lattice concept
     */
    private String _getNameFromConceptTuple(List<Concept> tuple) {
        String name = new String("");
        for(Concept concept : tuple) {
            name += concept.getName();
        }
        
        return name;
    }
    
    /** Initialize the latticeOntologies parameter and internal concept list.
     *  Called in every constructor.
     *  @throws IllegalActionException If the latticeOntologies parameter cannot
     *   be created.
     */
    private void _initAttributes() throws IllegalActionException {
        try {
            latticeOntologies = new Parameter(this, "latticeOntologies");
        } catch (NameDuplicationException nameDupEx) {
            throw new IllegalActionException(this, nameDupEx, "Cannot create " +
            		"the latticeOntologies Parameter in the ProductLatticeOntology.");
        }
        
        latticeOntologies.setTypeEquals(new ArrayType(new ObjectType(Ontology.class)));
        _conceptList = new ArrayList<ProductLatticeConcept>();
    }
    
    /** Remove all concepts from the product lattice ontology and clear the list
     *  of concepts. Used when the latticeOntologies parameter changes and the
     *  list of ProductLatticeConcepts must be invalidated.
     *  @throws IllegalActionException If a concept cannot be removed.
     */
    private void _removeAllConcepts() throws IllegalActionException {
        for (Object concept : entityList(ProductLatticeConcept.class)) {
            try {
                ((ProductLatticeConcept) concept).setContainer(null);
            } catch (NameDuplicationException nameDupEx) {
                throw new IllegalActionException(this, nameDupEx, "Could not " +
                		"remove concepts from the ProductLatticeOntology.");
            }
        }
        
        _conceptList.clear();
    }

    /** The list of {@link ProductLatticeConcept}s that define the ontology. */
    private List<ProductLatticeConcept> _conceptList;
    
    /** The cached CPO. */
    private CPO _cpo;
    
    /** The workspace version at which the cached CPO was valid. */
    private long _cpoVersion = -1L;
    
    /** The list of Ontologies that comprise the product lattice ontology. */
    private List<Ontology> _latticeOntologies;
    
    /** The workspace version at which the cached product lattice was valid. */
    private long _latticeVersion = -1L;
}
