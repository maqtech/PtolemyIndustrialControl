/* Calculate the inverse discrete cosine transform of a RenderedOp.

@Copyright (c) 1998-2002 The Regents of the University of California.
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
*/

package ptolemy.actor.lib.jai;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// JAIIDCT
/**
   Calculate the inverse discrete cosine transform of an image.  Even 
   though the input image data may be 3 bytes (one for each band) per 
   pixel, the output image data is of a higher resolution, not suitable 
   for displaying or saving.  To display or save the output of this image,
   use the JAIDataCaster actor to cast the data to an appropriate type
   (for instance, byte).
   <p>
   No JAIDataCaster actors should be used in between a JAIDCT and JAIIDCT 
   actor, unless if loss of spectral information is okay (or even desired).

   @see JAIDataCaster
   @see JAIDCT
   @author James Yeh
   @version $Id$
 */

public class JAIIDCT extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIIDCT(CompositeEntity container, String name)
             throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor.
     *  Output the inverse discrete cosine transform of the inputted 
     *  image.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ParameterBlock idctParameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage =  jaiImageToken.getValue();
        idctParameters.addSource(oldImage);
        RenderedOp newImage = JAI.create("idct", idctParameters);
        output.send(0, new JAIImageToken(newImage));
    }
}
