/* An application that executes non-graphical
   models specified on the command line.

 Copyright (c) 2001 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Ptolemy imports
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// MoMLSimpleApplication
/** A simple application that reads in a .xml file as a command
line argument and runs it.

<p>MoMLApplication sets the look and feel, which starts up Swing,
so we can't use MoMLApplication for non-graphical simulations.

<p>We implement the ChangeListener interface so that this
class will get exceptions thrown by failed change requests.

@author Christopher Hylands
@version $Id$
*/
public class MoMLSimpleApplication implements ChangeListener {
    /** Parse the xml file and run it.
     */
    public MoMLSimpleApplication(String xmlFilename) throws Exception{
        MoMLParser parser = new MoMLParser();
        CompositeActor toplevel =
            (CompositeActor) parser.parseFile(xmlFilename);
        Manager manager = new Manager(toplevel.workspace(),
                "MoMLSimpleApplication");
        toplevel.setManager(manager);
        toplevel.addChangeListener(this);
        manager.execute();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change request has been successfully executed.
     *  This method is called after a change request
     *  has been executed successfully.  In this class, we 
     *  do nothing.
     *  @param change The change that has been executed, or null if
     *   the change was not done via a ChangeRequest.
     */
    public void changeExecuted(ChangeRequest change) {
    }

    /** React to a change request has resulted in an exception.
     *  This method is called after a change request was executed,
     *  but during the execution an exception was thrown.
     *  @param change The change that was attempted or null if
     *   the change was not done via a ChangeRequest.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // If we do not implement ChangeListener, then ChangeRequest
        // will print any errors to stdout and continue.
        // This causes no end of trouble with the test suite

        // We can't throw and Exception here because this method in
        // the base class does not throw Exception.

	// In JDK1.4, we can construct exceptions from exceptions, but
	// not in JDK1.3.1
        //throw new RuntimeException(exception);

	throw new RuntimeException(exception.toString());
    }

    /** Create an instance of a single model and run it
     *  @param args The command-line arguments naming the .xml file to run
     */
    public static void main(String args[]) {
        try {
            MoMLSimpleApplication simpleApplication =
                new MoMLSimpleApplication(args[0]);
        } catch (Exception ex) {
            System.err.println("Command failed: " + ex);
        }
    }
}
