/* An actor that reads a String input token naming a URL and outputs an Image of type Object.

@Copyright (c) 2001 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.apps.fullscreen;

import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.ImageIcon;

//////////////////////////////////////////////////////////////////////////
//// ImageReader
/**
An actor that reads a String input token naming a URL and outputs an 
Object Token that contains a java.awt.Image

<p>It is possible to load a file
from the local file system by using the prefix "file://" instead of
"http://". Relative file paths are allowed. To specify a file
relative to the current directory, use "../" or "./". For example,
if the current directory contains a file called "test.jpg", then
<i>sourceURL</i> should be set to "file:./test.jpg". If the parent
directory contains a file called "test.jpg", then <i>sourceURL</i>
should be set to "file:../test.jpg". To reference the file
test.jpg, located at "/tmp/test.jpg", <i>sourceURL</i>
should be set to "file:///tmp/test.jpg" The default value is
"file:///tmp/test.jpg".

@see ImageReader
@author  Christopher Hylands
@version $Id$ */
public class URLToImage extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public URLToImage(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setTypeEquals(BaseType.OBJECT);
        input.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read on StringToken from the input, and output an ObjectToken
     *  that contains a java.awt.Image object to the output port.	
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
	StringToken urlToken = (StringToken)input.get(0);
	try {
	    URL url = new URL(urlToken.stringValue());
	    Image image = new ImageIcon(url).getImage();
	    output.send(0, new ObjectToken(image));
	} catch (MalformedURLException ex) {
	    throw new IllegalActionException("'" + urlToken.stringValue()
					     + "' is malformed: " + ex);
	}

    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }
        return super.prefire();
    }

}
