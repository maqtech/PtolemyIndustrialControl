/* A product lattice-based ontology.
 *
 * Copyright (c) 2007-2013 The Regents of the University of California. All
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
import ptolemy.data.ontologies.ConceptGraph;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.ObjectType;
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
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeOntology extends Ontology {

    /** Create a new ProductLatticeOntology with the specified container and
     *  the specified name.
     *  @param container The container.
     *  @param name The name for the ontology.
     *  @exception NameDuplicationException If the container already contains an
     *   ontology with the specified name.
     *  @exception IllegalActionException If the base class throws it.
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
     *  @exception IllegalActionException If the product lattice concepts
     *   cannot be generated.
     */
    @Override
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
                        new ProductLatticeConcept(this, conceptName, tuple);
                    } catch (NameDuplicationException nameDupEx) {
                        throw new IllegalActionException(
                                this,
                                nameDupEx,
                                "Could not "
                                        + "create ProductLatticeConcept for the ProductLatticeOntology.");
                    }
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return the current ontology from which concepts derive their highlight
     *  color definitions.
     *  @return The current ontology from which concepts derive their highlight
     *   color definitions.
     *  @see #setColorOntology(Ontology)
     */
    public Ontology getColorOntology() {
        return _colorOntology;
    }

    /** Return the product lattice concept in this ontology derived from
     *  the specified tuple of concepts.
     *  @param conceptTuple The list of concepts from which to create a
     *   product lattice concept in this ontology.
     *  @return The result product lattice concept.
     *  @exception IllegalActionException Thrown if the input list of concepts is
     *   not composed of concepts from the component ontologies of this
     *   product lattice ontology.
     */
    public ProductLatticeConcept getProductLatticeConceptFromTuple(
            List<Concept> conceptTuple) throws IllegalActionException {
        if (conceptTuple.size() != _latticeOntologies.size()) {
            throw new IllegalActionException(
                    this,
                    "The input conceptTuple does not have the "
                            + "correct number of entries for the number of ontologies that compose this "
                            + "product lattice ontology.");
        } else if (_conceptOntologiesDontMatch(conceptTuple)) {
            throw new IllegalActionException(
                    this,
                    "The input conceptTuple does not "
                            + "have concepts from the correct ontologies that compose this "
                            + "product lattice ontology.");
        } else {
            // First try to find the concept with the given tuple in the ontology.
            // If it is not there, then one or more of the component ontologies must
            // have infinite concepts, and a new product lattice concept must be
            // generated.
            ProductLatticeConcept concept = _findProductLatticeConceptByTuple(conceptTuple);
            if (concept == null) {
                try {
                    String conceptName = _getNameFromConceptTuple(conceptTuple);
                    concept = new ProductLatticeConcept(this, conceptName,
                            conceptTuple);
                } catch (NameDuplicationException ex) {
                    throw new IllegalActionException(
                            this,
                            ex,
                            "Could not create the product lattice concept "
                                    + "in the given product lattice ontology because one with that name already exists.");
                }
            }
            return concept;
        }
    }

    /** Return the list of lattice ontologies that comprise the product
     *  lattice ontology.
     *  @return The list of lattice ontology objects.
     *  @exception IllegalActionException If the latticeOntologies parameter does
     *   not return an array token that contains the ontology objects, or
     *   at least one of the specified component ontologies is not a lattice.
     */
    public List<Ontology> getLatticeOntologies() throws IllegalActionException {
        if (workspace().getVersion() != _latticeVersion) {
            // The latticeOntologies parameter is type constrainted to always
            // contain an array token of Ontology objects.
            ArrayToken ontologies = (ArrayToken) latticeOntologies.getToken();

            List<Ontology> ontologiesList = new ArrayList<Ontology>();
            if (ontologies != null && ontologies.length() != 0) {
                Token[] ontologiesTokenArray = ontologies.arrayValue();
                for (Token element : ontologiesTokenArray) {
                    Ontology ontology = (Ontology) ((ObjectToken) element)
                            .getValue();
                    if (ontology != null) {
                        if (ontology.isLattice()) {
                            ontologiesList.add(ontology);
                        } else {
                            throw new IllegalActionException(
                                    this,
                                    "All the ontologies"
                                            + " that comprise a product lattice ontology"
                                            + " must be lattices. The ontology "
                                            + ontology.getName()
                                            + " is not a lattice.");
                        }
                    }
                }
            }
            _latticeOntologies = ontologiesList;

            // Set the lattice version after creating the new list of
            // lattice ontologies.
            _latticeVersion = workspace().getVersion();
        }

        return _latticeOntologies;
    }

    /** Set the component ontology from which the colors will be derived
     *  for the concepts in this product lattice ontology. If the specified
     *  ontology is not a part of this product lattice ontology, the color
     *  ontology will be set to null.
     *  @param colorOntology The specified ontology to use for the color
     *   definitions for each product lattice concept. Or null if the concepts
     *   should have no color highlighting.
     *  @see #getColorOntology()
     *  @exception IllegalActionException Thrown if the specified ontology is not
     *   part of the product lattice ontology.
     */
    public void setColorOntology(Ontology colorOntology)
            throws IllegalActionException {
        if (colorOntology != null
                && !_latticeOntologies.contains(colorOntology)) {
            throw new IllegalActionException(this, "The ontology "
                    + colorOntology.getName() + " is not a component "
                    + "of the product lattice ontology " + this.getName());
        } else {
            _colorOntology = colorOntology;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the graph represented by this ontology. For a product lattice
     *  ontology this is a {@link ProductLatticeCPO}.
     *  @return The concept graph as a ProductLatticeCPO.
     */
    @Override
    protected ConceptGraph _buildConceptGraph() {
        if (workspace().getVersion() != _graphVersion) {
            _graph = new ProductLatticeCPO(this);

            // Set the graph version after creating the new graph
            _graphVersion = workspace().getVersion();
        }

        return _graph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if the list of concepts in the concept tuple are not
     *  from the ontologies that comprise this product lattice ontology, and
     *  false if they do.
     *  @param conceptTuple The input list of concepts.
     *  @return true if the ontologies for the list of concepts do not match
     *   the component ontologies of this product lattice ontology, and false
     *   if they do.
     */
    private boolean _conceptOntologiesDontMatch(List<Concept> conceptTuple) {
        for (int i = 0; i < _latticeOntologies.size(); i++) {
            if (!conceptTuple.get(i).getOntology().getName()
                    .equals(_latticeOntologies.get(i).getName())) {
                return true;
            }
        }
        return false;
    }

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
    private List<List<Concept>> _createAllConceptTuples(
            List<Ontology> ontologiesList) {
        List<List<Concept>> conceptTupleList = new ArrayList<List<Concept>>();

        if (ontologiesList != null && !ontologiesList.isEmpty()) {
            Ontology firstOntology = ontologiesList.get(0);
            List<Ontology> otherOntologies = new ArrayList<Ontology>(
                    ontologiesList);
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

    /** Find the product lattice concept in this ontology that has the given
     *  list of concepts as its tuple.
     *  @param conceptTuple The input list of concepts that should be contained
     *   by the product lattice concept to be found.
     *  @return The product lattice concept that contains the given list of
     *   concept as its tuple, or null if it cannot be found in this ontology.
     */
    private ProductLatticeConcept _findProductLatticeConceptByTuple(
            List<Concept> conceptTuple) {
        for (Object concept : entityList(ProductLatticeConcept.class)) {
            List<Concept> productLatticeTuple = ((ProductLatticeConcept) concept)
                    .getConceptTuple();
            if (productLatticeTuple.equals(conceptTuple)) {
                return (ProductLatticeConcept) concept;
            }
        }

        return null;
    }

    /** Create the name of the ProductLatticeConcept by concatenating its given
     *  list of concepts from a tuple of concepts.
     *
     *  @param tuple The given concept tuple list.
     *  @return The name of the product lattice concept
     */
    private String _getNameFromConceptTuple(List<Concept> tuple) {
        StringBuffer nameBuffer = new StringBuffer();
        for (Concept concept : tuple) {
            nameBuffer.append(concept.getName());
        }
        return nameBuffer.toString();
    }

    /** Initialize the latticeOntologies parameter and internal concept list.
     *  Called in every constructor.
     *  @exception IllegalActionException If the latticeOntologies parameter cannot
     *   be created.
     */
    private void _initAttributes() throws IllegalActionException {
        try {
            latticeOntologies = new Parameter(this, "latticeOntologies");
        } catch (NameDuplicationException nameDupEx) {
            throw new IllegalActionException(
                    this,
                    nameDupEx,
                    "Cannot create "
                            + "the latticeOntologies Parameter in the ProductLatticeOntology.");
        }

        _attachText("_iconDescription", _ICON);

        latticeOntologies.setTypeEquals(new ArrayType(new ObjectType(
                Ontology.class)));
    }

    /** Remove all concepts from the product lattice ontology and clear the list
     *  of concepts. Used when the latticeOntologies parameter changes and the
     *  list of ProductLatticeConcepts must be invalidated.
     *  @exception IllegalActionException If a concept cannot be removed.
     */
    private void _removeAllConcepts() throws IllegalActionException {
        for (Object concept : entityList(ProductLatticeConcept.class)) {
            try {
                ((ProductLatticeConcept) concept).setContainer(null);
            } catch (NameDuplicationException nameDupEx) {
                throw new IllegalActionException(this, nameDupEx, "Could not "
                        + "remove concepts from the ProductLatticeOntology.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The component ontology of this product lattice ontology from which the
     *  product lattice concepts should derive their highlight color attributes.
     */
    private Ontology _colorOntology = null;

    /** The list of Ontologies that comprise the product lattice ontology. */
    private List<Ontology> _latticeOntologies;

    /** The workspace version at which the cached product lattice was valid. */
    private long _latticeVersion = -1L;

    /** The icon description used for rendering. */
    private static final String _ICON = "<svg>"
            + "<line x1=\"0\" y1=\"-20\" x2=\"10\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"0\" y1=\"-20\" x2=\"-10\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"0\" y1=\"-20\" x2=\"0\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"0\" y1=\"20\" x2=\"10\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"0\" y1=\"20\" x2=\"-10\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"0\" y1=\"20\" x2=\"0\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<circle cx=\"0\" cy=\"-20\" r=\"6\" style=\"fill:blue\"/>"
            + "<circle cx=\"0\" cy=\"20\" r=\"6\" style=\"fill:red\"/>"
            + "<circle cx=\"10\" cy=\"-1\" r=\"6\" style=\"fill:white\"/>"
            + "<circle cx=\"-10\" cy=\"-1\" r=\"6\" style=\"fill:white\"/>"
            + "<circle cx=\"0\" cy=\"0\" r=\"6\" style=\"fill:white\"/>"
            + "<text x=\"17\" y=\"8\""
            + "  style=\"font-size:28; font-family:SansSerif\">x</text>"
            + "<line x1=\"50\" y1=\"-20\" x2=\"60\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"-20\" x2=\"40\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"-20\" x2=\"50\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"20\" x2=\"60\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"20\" x2=\"40\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"50\" y1=\"20\" x2=\"50\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<circle cx=\"50\" cy=\"-20\" r=\"6\" style=\"fill:blue\"/>"
            + "<circle cx=\"50\" cy=\"20\" r=\"6\" style=\"fill:red\"/>"
            + "<circle cx=\"60\" cy=\"-1\" r=\"6\" style=\"fill:white\"/>"
            + "<circle cx=\"40\" cy=\"-1\" r=\"6\" style=\"fill:white\"/>"
            + "<circle cx=\"50\" cy=\"0\" r=\"6\" style=\"fill:white\"/>"
            + "</svg>";
}
