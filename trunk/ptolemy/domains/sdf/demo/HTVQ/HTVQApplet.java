/* An applet that uses Ptolemy II SDF domain.

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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.demo.HTVQ;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.Enumeration;
import java.lang.Math;
import java.net.URL;
import javax.swing.JPanel;
import javax.swing.JLabel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.gui.Display;
import ptolemy.domains.sdf.demo.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.domains.sdf.lib.vq.*;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// HTVQApplet
/**
An applet that uses Ptolemy II SDF domain.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.3
*/
public class HTVQApplet extends MoMLApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Place the placeable objects in a customized panel.
     */
    public void _createView() {
        super._createView();

        JPanel displayPanel = new JPanel();
        getContentPane().add(displayPanel, BorderLayout.SOUTH);

        // So that the background color comes through...
        displayPanel.setBackground(null);

        JPanel originalPanel = new JPanel();
        displayPanel.add(originalPanel, BorderLayout.WEST);

        JPanel compressedPanel = new JPanel();
        displayPanel.add(compressedPanel, BorderLayout.CENTER);

        JPanel prnPanel = new JPanel();
        // So the background shows through.
        prnPanel.setBackground(null);
        prnPanel.setLayout(new BorderLayout());
        prnPanel.add(new JLabel("SNR (dB)"), BorderLayout.NORTH);
        displayPanel.add(prnPanel, BorderLayout.EAST);

        CompositeEntity toplevel = (CompositeEntity)_toplevel;
        ImageDisplay consumer = (ImageDisplay)toplevel.getEntity("Compressed");
        consumer.place(compressedPanel);
        consumer.setBackground(null);

        ImageDisplay original = (ImageDisplay)toplevel.getEntity("Original");
        original.place(originalPanel);
        original.setBackground(null);

        // Display actor puts the text at the right of the
        // applet window. Text Area size is set to be 7*10 (row* column)
        // in order to fit well with the image size.
        Display prn = (Display)toplevel.getEntity("Display");
        // To control the position, we put this in its own panel.
        JPanel textPanel = new JPanel();
        prnPanel.add(textPanel, BorderLayout.SOUTH);
        prn.place(textPanel);
        textPanel.setBackground(null);
    }
}
