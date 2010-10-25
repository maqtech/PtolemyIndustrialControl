/* A concept in a product lattice-based ontology.
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

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ProductLatticeConcept

/** A concept in a product lattice-based ontology.
 *  Represents a concept that is composed of a tuple of other concepts derived
 *  from other ontologies.
 * 
 *  @see ProductLatticeOntology
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
*/
public class ProductLatticeConcept extends Concept {

    /** Create a new product lattice concept with the specified name and the
     *  specified product lattice ontology.
     *  
     *  @param ontology The specified product lattice ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public ProductLatticeConcept(ProductLatticeOntology ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);
    }
    
    /** Create a new product lattice concept with the specified name and the
     *  specified product lattice ontology.
     *  
     *  @param ontology The specified product lattice ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @param conceptTuple The list of concepts that compose this product lattice concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public ProductLatticeConcept(ProductLatticeOntology ontology, String name, List<Concept> conceptTuple)
            throws NameDuplicationException, IllegalActionException {
        this(ontology, name);
        _conceptTuple = new ArrayList<Concept>(conceptTuple);
    }
    
    /** Return the list of concepts that compose this product lattice concept.
     *  @return The list of concepts that compose this product lattice concept.
     */
    public List<Concept> getConceptTuple() {
        return new ArrayList<Concept>(_conceptTuple);
    }    
    
    /** Return the product lattice ontology that contains this concept.
     *  @return The containing product lattice ontology.
     */
    public Ontology getOntology() {
        return (Ontology) getContainer();
    }

    /** Return the string that represents this concept, its name.
     *  @return The string name that represents this concept.
     */
    public String toString() {
        return _name;
    }
    
    /** The list of concepts that comprise this product lattice concept. */
    private List<Concept> _conceptTuple;
}
