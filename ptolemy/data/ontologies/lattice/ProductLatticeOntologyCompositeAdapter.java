/** A product lattice-based ontology adapter for composite actors whose
 *  constraints are derived from the component ontology solvers.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
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
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 */

package ptolemy.data.ontologies.lattice;

import java.util.List;

import ptolemy.data.ontologies.Ontology;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ProductLatticeOntologyCompositeAdapter

/** A product lattice-based ontology adapter for composite actors whose
 *  constraints are derived from the component ontology solvers.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeOntologyCompositeAdapter extends
LatticeOntologyCompositeAdapter {

    /** Construct the product lattice ontology adapter associated
     *  with the given composite actor.
     *  @param solver  The product lattice-based ontology solver for this adapter.
     *  @param component The given composite actor.
     *  @exception IllegalActionException Thrown if the adapter cannot be created.
     */
    public ProductLatticeOntologyCompositeAdapter(
            ProductLatticeOntologySolver solver, CompositeEntity component)
                    throws IllegalActionException {
        super(solver, component);
        _tupleAdapters = ProductLatticeOntologyAdapter.getTupleAdapters(solver,
                component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the constraints of this component. The constraints is a list of
     * inequalities. The constraints are generated from the component ontology
     * constraint lists.
     * @return The constraints of this component.
     * @exception IllegalActionException Thrown if there is a problem creating
     *  the constraints.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        for (LatticeOntologyAdapter adapter : _tupleAdapters) {
            if (adapter != null) {
                // For each component adapter, add the default constraints
                // and the interconnection constraints, but not
                // the subadapter constraints, because that the subadapters
                // will be added by their respective product lattice ontology
                // adapters when super.constraintList() is called.
                Ontology adapterOntology = adapter.getSolver().getOntology();
                adapter._addDefaultConstraints(adapter.getSolver()
                        ._getConstraintType());
                ((LatticeOntologyCompositeAdapter) adapter)
                ._addInterConnectionConstraints();
                ProductLatticeOntologyAdapter
                .addConstraintsFromTupleOntologyAdapter(
                        adapter._ownConstraints, adapterOntology, this);
            }
        }
        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add all the constraints between actors inside the composite actor
     *  referenced by this adapter.  In this derived class for ProductLatticeOntologies
     *  this method does nothing because the ProductLatticeOntologyAdapter's
     *  constraints are generated by composing the constraints
     *  of the component LatticeOntologyAdapters.
     *  @exception IllegalActionException Not thrown in this derived class.
     */
    @Override
    protected void _addInterConnectionConstraints()
            throws IllegalActionException {
        // Do nothing here.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of adapters for the model component for each ontology that
     *  comprises the product lattice ontology.
     */
    List<LatticeOntologyAdapter> _tupleAdapters;
}
