/* A graphical component displaying matrix contents.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.actor.gui;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import java.awt.Container;
import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import ptolemy.data.*;
import ptolemy.math.Complex;
import ptolemy.actor.lib.*;

//////////////////////////////////////////////////////////////////////////
//// MatrixViewer
/** 

A graphical component that displays the contents of a Matrix. This
actor has a single input port, which only accepts MatrixTokens. One
token is consumed per firing.  The data in the MatrixToken is
displayed in a table format with scrollbars, using the swing table
class.

@author Bart Kienhuis
@version $Id$
*/

public class MatrixViewer extends Sink implements Placeable {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MatrixViewer(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
                super(container, name);
                // input.setTypeEquals(BaseType.MATRIX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        MatrixViewer newobj = (MatrixViewer)super.clone(ws);
        newobj.input
            = (TypedIOPort)newobj.getPort("input");
        return newobj;
    }

    /** If a token exists on the <i>input</i> port, first determine
     *  the type of MatrixToken that it is, then pass it to the proper
     *  function to convert its data into an Object[][].  The
     *  Object[][] is then used to create a new instance of a JTable.
     */
    public void fire() throws IllegalActionException {

        Token in = null;
        Object[][][] returnValue;

        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            if ( input.hasToken(i) ) {
                in = input.get(i);
                _matrixTable = new MatrixAsTable( (MatrixToken) in );
                table.setModel( _matrixTable );
            }
        }
    }

    /** Create a new JTable on the screen.  If a panel has not been
     *  specified, place the JTable into its own frame.  Otherwise,
     *  place it in the specified panel.  
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if ( table == null ) {
            place(_container);
        }
        if ( _frame != null ) {
            _frame.setVisible(true);
        }
    }

    /** Specify the container in which the data should be displayed.
     *  An instance of JTextArea will be added to that container.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, an instance of TextArea will be placed in its own frame.
     *  The text area is also placed in its own frame if this method
     *  is called with a null argument.
     *  The background of the text area is set equal to that of the container
     *  (unless it is null).
     *
     *  @param container The container into which to place the text area.
     */
    public void place(Container container) {
        _container = container;
        if (_container == null) {
            // place the text area in its own frame.
            // FIXME: This probably needs to be a PtolemyFrame, when one
            // exists, so that the close button is dealt with, etc.
            _frame = new JFrame(getFullName());
            table = new JTable();
            _scrollPane = new JScrollPane(table);
            _frame.getContentPane().add(_scrollPane);
        } else {
            table = new JTable();
            _scrollPane = new JScrollPane(table);
            _container.add(_scrollPane);
            table.setBackground(_container.getBackground());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public  JTable table;


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Container into which this plot should be placed */
    private Container _container;

    /** The scroll pane of the panel. */
    private JScrollPane _scrollPane;

    /** Frame into which plot is placed, if any. */
    private JFrame _frame = null;

    /** The Abstract Table Model of a Matrix. */
    private MatrixAsTable _matrixTable = null;

    ///////////////////////////////////////////////////////////////////
    ////                         Inner Class                       ////

    /** This class provides the implementations of the methods in the
        TableModel interface. To create a concrete TableModel of a
        Matrix as a sublcass of AbstractTableModel, the class provides
        implementations for the most important methods. */
    public class MatrixAsTable extends AbstractTableModel {

        /** Construct for a specific matrix.
            @param matrix The matrix.
         */
        MatrixAsTable( MatrixToken matrix ) {
            _matrix = matrix;   
        }

        /** Get the row count of the Matrix.
            @return the row count.
         */
        public int getRowCount() {
            return _matrix.getRowCount();
        }

        /** Get the column count of the Matrix.
            @return the column count.
         */
        public int getColumnCount() {
            return _matrix.getColumnCount();
        }

        /** Get a specific entry from the matrix given by the row and
            column number.
            @param row The row number.
            @param column The column number.
            @return The object store in the matrix at the specified location.
        */
        public Object getValueAt(int row, int column) {
            return (Object) (_matrix.getElementAsToken( row, column )).
                stringValue();
        }

        /** The Matrix for which a Table Model is created. */
        private MatrixToken _matrix;
        
    }
   
}


