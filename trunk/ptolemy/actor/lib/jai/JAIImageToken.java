/* A token that contains a javax.media.jai.RenderedOp.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

package ptolemy.actor.lib.jai;

import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.Image;

import javax.media.jai.PlanarImage;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ptolemy.data.ImageToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.type.*;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// JAIImageToken

/**
 A token that contains a javax.media.jai.RenderedOp.  This token is used
 when dealing with images in the Java Advanced Imaging (JAI) library.
 Because it extends ImageToken, it can be used with the standard image
 processing tools by simply calling asAWTImage().
 @author James Yeh
 @version $Id$
 */
public class JAIImageToken extends ImageToken {

    /** Construct a token with a specified RenderedOp.
     */
    public JAIImageToken(RenderedOp value) {
        _renderedOp = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token and
     *  the argument.  The image size is the size of the intersection of 
     *  the two images.  The number of bands in the image is equal to
     *  the smallest number of bands of the two sources.  The data type
     *  is the smallest data type to have sufficient range for both input
     *  data types.
     *
     * @param rightArgument The token to add to this token.
     * @return A new token containing the result.
     * @throws IllegalActionException if the data type is not supported.
     */
    public Token add(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof JAIImageToken) {
            ParameterBlock parameters =
                _parameterize(_renderedOp, (JAIImageToken) rightArgument);
            return new JAIImageToken(JAI.create("add", parameters));
        } else {
            throw new IllegalActionException("illegal data type");
        }
    }

    /** Convert a javax.media.jai.RenderedOp to a java.awt.Image and
     *  return it.
     */
    public Image asAWTImage() {
        _planarImage = _renderedOp.getRendering();
        _bufferedImage = _planarImage.getAsBufferedImage();
        _awtImage = (Image) _bufferedImage;
        return _awtImage;
    }

    /** Return a new token whose value is the division of this 
     *  token and the argument.  The image size is the size of the 
     *  intersection of the two images.  The number of bands in the image 
     *  is equal to the smallest number of bands of the two sources.  The 
     *  data type is the bigger of the two input data types
     *
     * @param rightArgument The token to divide this token by.
     * @return A new token containing the result.
     * @throws IllegalActionException if the data type is not supported.
     */
    public Token divide(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof JAIImageToken) {
            ParameterBlock parameters =
                    _parameterize(_renderedOp, (JAIImageToken) rightArgument);
            return new JAIImageToken(JAI.create("divide", parameters));
        } else {
            throw new IllegalActionException("illegal data type");
        }
    }

    /** Return the type of this token.
     *  @return BaseType.OBJECT
     */
    public Type getType() {
        return BaseType.OBJECT;
    }

    /** Return the value of the token, a renderedop.
     *  @return The RenderedOp in this token.
     */
    public RenderedOp getValue() {
        return _renderedOp;
    }

    /** Return a new token whose value is the multiplication of this 
     *  token and the argument.  The image size is the size of the 
     *  intersection of the two images.  The number of bands in the image 
     *  is equal to the smallest number of bands of the two sources.  The 
     *  data type is the bigger of the two input data types
     *
     * @param rightArgument The token to multiply this token by.
     * @return A new token containing the result.
     * @throws IllegalActionException if the data type is not supported.
     */
    public Token multiply(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof JAIImageToken) {
            ParameterBlock parameters =
                    _parameterize(_renderedOp, (JAIImageToken) rightArgument);
            return new JAIImageToken(JAI.create("multiply", parameters));
        } else {
            throw new IllegalActionException("illegal data type");
        }
    }

    /** Return a new token whose value is the subtraction of this token 
     *  from the argument.  The image size is the size of the intersection 
     *  of the two images.  The number of bands in the image is equal to
     *  the smallest number of bands of the two sources.  The data type
     *  is the smallest data type to have sufficient range for both input
     *  data types.
     *
     * @param rightArgument The token to subtract from this token.
     * @return A new token containing the result.
     * @throws IllegalActionException if the data type is not supported.
     */
    public Token subtract(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof JAIImageToken) {
            ParameterBlock parameters =
                    _parameterize(_renderedOp, (JAIImageToken) rightArgument);
            return new JAIImageToken(JAI.create("subtract", parameters));
        } else {
            throw new IllegalActionException("illegal data type");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Create a ParameterBlock containing two RenderedOp's, the first
    // being the internal Image, the second being from an ImageToken.
    private ParameterBlock _parameterize(RenderedOp left, ImageToken right) {
        ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(right.asAWTImage());
        RenderedOp rightOp = JAI.create("awtImage", parameters);
        parameters = new ParameterBlock();
        parameters.addSource(left);
        parameters.addSource(rightOp);
        return parameters;
    }

    // Create a ParameterBlock containing two RenderedOp's, the first
    // being the internal Image, the second being from a JAIImageToken.
    private ParameterBlock _parameterize(RenderedOp left, JAIImageToken right) {
        ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(left);
        parameters.addSource(right.getValue());
        return parameters;
    }

    // Create a ParameterBlock containing one RenderedOp and one double,
    // the first being the internal Image, the second from a ScalarToken.
    private ParameterBlock _parameterize(RenderedOp left, ScalarToken right)
            throws IllegalActionException {
        ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(left);
        parameters.add(right.doubleValue());
        return parameters;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Image _awtImage;
    private BufferedImage _bufferedImage;
    private PlanarImage _planarImage;
    private RenderedOp _renderedOp;

}
