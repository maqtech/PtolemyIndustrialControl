/* One Port Test Actor for DT
@Copyright (c) 1998-2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.domains.dt.kernel.test;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.dt.kernel.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.actor.lib.*;
import ptolemy.math.Complex;


public class OnePort extends TypedAtomicActor {
    public OnePort(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = (SDFIOPort) newPort("input");
        input.setInput(true);
        input.setTokenConsumptionRate(1);
        input.setTypeEquals(BaseType.DOUBLE);
        inrate= new Parameter(this, "inrate", new IntToken(1));
        _inrate = 1;


        output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTokenProductionRate(1);
        output.setTypeSameAs(input);
        outrate = new Parameter(this, "outrate", new IntToken(1));
        _outrate = 1;



        initialOutputs = new Parameter(this, "initialOutputs",
                new IntMatrixToken(defaultValues));


        //Parameter tokenInitProduction = new Parameter(output,"tokenInitProduction",
        //                     new IntMatrixToken(defaultValues));

    }

    public SDFIOPort input;
    public SDFIOPort output;

    public Parameter inrate;
    public Parameter outrate;

    public Parameter initialOutputs;

    public Parameter value;
    public Parameter step;
    //public Parameter tokenInitProduction;

    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        Director dir = getDirector();

        if (dir != null) {
            _inrate = ((IntToken) inrate.getToken()).intValue();
            _outrate = ((IntToken) outrate.getToken()).intValue();
            input.setTokenConsumptionRate(_inrate);
            output.setTokenProductionRate(_outrate);
            dir.invalidateSchedule();
        }
    }


    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        OnePort newObject = (OnePort)(super.clone(workspace));
        newObject.input =  (SDFIOPort)newObject.getPort("input");
        newObject.output = (SDFIOPort)newObject.getPort("output");
        newObject.inrate = (Parameter) newObject.getAttribute("inrate");
        newObject.outrate = (Parameter) newObject.getAttribute("outrate");
        return newObject;
    }


    public final void fire() throws IllegalActionException  {
        int i;
        int integer, remainder;
        DoubleToken token = new DoubleToken(0.0);;
        _buffer = new Token[_inrate];

        _buffer[0] = token;

        //DTDebug debug = new DTDebug(true);
        //debug.prompt(""+input.getWidth());
        if (input.getWidth() >= 1) {
            for(i=0;i<_inrate;i++) {
                // FIXME: should consider port widths
                //if (input.hasToken(0)) {
                //token = (DoubleToken) (input.get(0));
                _buffer[i] = input.get(0);
                //} else {
                //    throw new IllegalActionException(
                //              "no Tokens available for OnePort during firing");
                //}
            }
        }

        for(i=0;i<_outrate;i++) {
            //output.send(0, new DoubleToken(0.0));
            output.send(0, _buffer[i%_inrate]);
        }
    }

    private int _inrate;
    private int _outrate;
    private int defaultValues[][] = {{0,0}};
    private Token[] _buffer;
}
