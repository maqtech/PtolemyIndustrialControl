/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2014-2015 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
*/
package org.ptolemy.ssm;

import java.util.Arrays;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** 
 * An occupancy grid map.
 *
 * @author Ilge Akkaya
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Map extends MirrorDecorator {
 
    /** Construct a StateSpaceModel with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Map(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The occupancy grid map. */
    public PortParameter map;

    /** A 2-d array denoting the map origin in x and y directions
     * respectively.
     */
    public Parameter origin;

    /** Map resolution in meters/pixel.
     */
    public Parameter resolution;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the state accordingly.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If
     *   <i>stateVariableNamest</i> cannot be evaluated or cannot be
     *   converted to the output type, or if the superclass throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == map) {
            RecordToken mapToken = (RecordToken) map.getToken();
            _width = ((IntToken)mapToken.get("width")).intValue();
            _height = ((IntToken)mapToken.get("height")).intValue();
            Token[] gridContent = ((ArrayToken)mapToken.get("grid")).arrayValue();
            _occupancyGrid = new int[_height][_width];
            for (int i=0; i < _height; i++) {
                for (int j = 0; j < _width; j++) {
                    int index = j + i*_width;
                    _occupancyGrid[i][j] = ((IntToken)gridContent[index]).intValue();
                } 
            }
        } else if (attribute == origin) {
            _origin[0] = ((DoubleToken)((ArrayToken)origin.getToken()).
                    getElement(0)).doubleValue();
            _origin[1] = ((DoubleToken)((ArrayToken)origin.getToken()).
                    getElement(1)).doubleValue();
        } else if (attribute == resolution) {
            _resolution = ((DoubleToken) resolution.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Map newObject = (Map) super
                .clone(workspace); 
        newObject._origin = new double[2];
        newObject._occupancyGrid = null; 
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        map.update();
    }
 
    public int[][] getOccupancyGrid() {
        int [][] copyOccupancy = new int[_occupancyGrid.length][_occupancyGrid[0].length];
        for (int i = 0; i < copyOccupancy.length; i++) {
            copyOccupancy[i] = Arrays.copyOf(_occupancyGrid[i],_occupancyGrid[i].length);
        }
        return copyOccupancy;
    }
    
    public double[] getOrigin() {
        return Arrays.copyOf(_origin, _origin.length);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the class. */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        
        resolution = new Parameter(this, "resolution");
        resolution.setExpression("0.05");
        resolution.setTypeEquals(BaseType.DOUBLE); 

        origin = new Parameter(this, "origin");
        origin.setExpression("{0.0,0.0}");
        origin.setTypeEquals(new ArrayType(BaseType.DOUBLE));  
        
        map = new PortParameter(this,"map");
        map.setExpression("{width = 2, height=1, grid={0,0}}");
        String[] mapLabels = {"width","height","grid"};
        Type[] types = {BaseType.INT, BaseType.INT, new ArrayType(BaseType.INT)};
        map.setTypeEquals(new RecordType(mapLabels, types));
        
        _origin = new double[2];
        
    } 
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _width;
    private int _height;
    private int[][] _occupancyGrid;
    private double[] _origin;
    private double _resolution;
    
}
