/* Wrapper class to start up the DSP version

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui.jnlp;

import ptolemy.vergil.VergilApplication;
import ptolemy.gui.MessageHandler;

import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// DSPApplication
/** A wrapper class that calls ptolemy.actor.gui.MoMLApplication for
use with Java Network Launching Protocol (JNLP) aka Web Start.

<p>In Web Start 1.0.1, it is necessary to sign the application
if it is to have access to the local disk etc.  The way that this is
handled is that the .jnlp file that defines the application
is copied to the .jar file that defines the main() method for
the application and the .jar file is signed.  Unfortunately, this means
that two Web Start applications cannot share one jar file, so
we create these wrapper classes that call the appropriate main class.
<p>For more information about JNLP, see $PTII/mk/jnlp.in.

@see ptolemy.vergil.VergilApplication

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class DSPApplication {
    public static void main(final String [] args) {
        // We set the security manager to null for two reasons

        // 1) Get rid of the following message when we open the file browser:
        // "There is no disk in the drive. Please insert a disk into drive A"
        // with the standard Abort/Retry/Ignore buttons.
        // See:
        // http://forum.java.sun.com/thread.jsp?forum=38&thread=71610

        // 2)Speed things up, see
        // http://forums.java.sun.com/thread.jsp?forum=38&thread=134393

        System.setSecurityManager(null);

	// Invoke in the event thread so that we don't have problems
	// rendering HTML.
	// Note that this makes Web Start a little slower to come up,
	// but it avoids the problems in rendering that HyVisual 2.2-beta had.
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    try {
			ptolemy.vergil.VergilApplication.main(args);
		    } catch (Exception ex) {
			MessageHandler.error("Command failed", ex);
			System.exit(0);
		    }
		}
	    });
    }
}
