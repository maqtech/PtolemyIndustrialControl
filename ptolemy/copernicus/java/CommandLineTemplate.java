/* A Ptolemy application that instantiates classnames given on the command
   line.

 Copyright (c) 1999-2002 The Regents of the University of California.
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

@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.copernicus.java;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.ModelFrame;
import ptolemy.actor.gui.ModelPane;
import ptolemy.actor.gui.Placeable;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.System;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;


/////////////////////////////////////////////////////////////////
//// CommandLineTemplace
/**
This class is similar to CompositeActorApplication, except that it
does not parse command line elements.   It is used as
a template for generating a command line interface for code generated
from a ptolemy model.
<p>
In this case, parsing the command line is not necessary because
parameter values and the class values are fixed by the code generator.

@author Steve Neuendorffer
@version $Id$
*/
public class CommandLineTemplate {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new application with the specified command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            CommandLineTemplate app = new CommandLineTemplate();
            app.processArgs(args);
            app.waitForFinish();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();

            //  System.exit(0);
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    /** Parse the command-line arguments, creating models as specified.
     *  @param args The command-line arguments.
     *  @exception If something goes wrong.
     */
    public void processArgs(String args[]) throws Exception {
        if (args != null) {
           
            // start the models.
            Iterator models = _models.iterator();
            while(models.hasNext()) {

                // First, we gc and then print the memory stats
                // BTW to get more info about gc, 
                // use java -verbose:gc . . .
                System.gc();
                Thread.sleep(1000);


                long startTime = System.currentTimeMillis();

                Runtime runtime = Runtime.getRuntime();

                // Get the memory stats before we get the model name
                // just to be sure that getting the model name does
                // not skew are data too much
                long totalMemory1 = runtime.totalMemory()/1024;
                long freeMemory1 = runtime.freeMemory()/1024;

                CompositeActor model = (CompositeActor)models.next();
                String modelName = model.getName();

                System.out.println(modelName +
                        ": Stats before execution:    "
                        + timeAndMemory(startTime,
                                totalMemory1, freeMemory1));
                
                // Second, we run and print memory stats.
                startRun(model);
                
                long totalMemory2 = runtime.totalMemory()/1024;
                long freeMemory2 = runtime.freeMemory()/1024;
                String standardStats = timeAndMemory(startTime,
                                totalMemory2, freeMemory2);

                System.out.println(modelName +
                        ": Execution stats:           "
                        + standardStats);


                // Third, we gc and print memory stats.
                System.gc();
                Thread.sleep(1000);

                long totalMemory3 = runtime.totalMemory()/1024;
                long freeMemory3 = runtime.freeMemory()/1024;
                System.out.println(modelName +
                        ": After Garbage Collection:  "
                        + timeAndMemory(startTime,
                                totalMemory3, freeMemory3));
                System.out.println(modelName +
                        ": construction size:         "
                        + totalMemory1 + "K - " + freeMemory1 + "K = "
                        + (totalMemory1 - freeMemory1) + "K");
                System.out.println(modelName +
                        ": model alloc. while exec. : "
                        + freeMemory1 + "K - " + freeMemory3 + "K = "
                        + (freeMemory1 - freeMemory3) + "K");
                System.out.println(modelName +
                        ": model alloc. runtime data: "
                        + freeMemory3 + "K - " + freeMemory2 + "K = "
                        + (freeMemory3 - freeMemory2) + "K");

                // Print out the standard stats at the end
                // so as not to break too many scripts
                System.out.println(standardStats
                        + " Stat: " + (totalMemory1 - freeMemory1)
                        + "K StatRT: " + (freeMemory1 - freeMemory3)
                        + "K DynRT: " + (freeMemory3 - freeMemory2)
                        + "K");
            }
        }
    }

    /** Report an exception.  This prints a message to the standard error
     *  stream, followed by the stack trace.
     *  @param ex The exception to report.
     */
    public void report(Exception ex) {
        report("", ex);
    }

    /** Report a message to the user.
     *  This prints a message to the standard output stream.
     *  @param message The message to report.
     */
    public void report(String message) {
        System.out.println(message);
    }

    /** Report an exception with an additional message.
     *  This prints a message to standard error, followed by the
     *  stack trace.
     *  @param message The message.
     *  @param ex The exception to report.
     */
    public void report(String message, Exception ex) {
        System.err.println("Exception thrown.\n" + message + "\n"
                + ex.toString());
        ex.printStackTrace();
    }

    /** If the specified model has a manager and is not already running,
     *  then execute the model in a new thread.  Otherwise, do nothing.
     *  If the model contains an atomic entity that implements Placeable,
     *  we create create an instance of ModelFrame, if nothing implements
     *  Placeable, then we do not create an instance of ModelFrame.  This
     *  allows us to run non-graphical models on systems that do not have
     *  a display.
     *  <p>
     *  We then start the model running.
     *
     *  @param model The model to execute.
     *  @see ptolemy.actor.Manager#startRun()
     */
    public synchronized void startRun(CompositeActor model) {
        // This method is synchronized so that it can atomically modify
        // the count of executing processes.

        // NOTE: If you modify this method, please be sure that it
        // will work for non-graphical models in the nightly test suite.

        // Iterate through the model, looking for something that is Placeable.
        boolean hasPlaceable = false;
        /* Iterator atomicEntities = model.allAtomicEntityList().iterator();
        while (atomicEntities.hasNext()) {
            Object object = atomicEntities.next();
            if(object instanceof Placeable) {
                hasPlaceable = true;
                break;
            }
           }

        if (hasPlaceable) {
            // The model has an entity that is Placeable, so create a frame.
            try {
                // A model frame with no buttons... just place the 
                // placeable actors.
                ModelFrame frame = new ModelFrame(model, null,
                        new ModelPane(model, ModelPane.HORIZONTAL, 0));
                          
                _openCount++;
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosed(WindowEvent event) {
                        synchronized(CommandLineTemplate.this) {
                            _openCount--;
                            CommandLineTemplate.this.notifyAll();
			    // FIXME: is this right?  We need
			    // to exit if all the windows are closed?
			    if (_openCount == 0) {
				System.exit(0);
			    }
                        }
                    }
                });
                frame.setBackground(new Color(0xe5e5e5));
                frame.pack();
                frame.centerOnScreen();
                frame.setVisible(true);
                // FIXME: Use a JFrame listener to determine when all windows
                // are closed.
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("startRun: " + ex);
            }
        }
         */

        Manager manager = model.getManager();
        try {
            if (manager == null) {
                model.setManager(new Manager(model.workspace(), "manager"));
                manager = model.getManager();
            }      
            model.workspace().setReadOnly(true);
            long startTime = System.currentTimeMillis();
            manager.startRun();
            System.out.println("Execution stats:");
            System.out.println(timeAndMemory(startTime));
        
        } catch (IllegalActionException ex) {
            // Model is already running.  Ignore.
            System.out.println("Exception = " + ex);
            ex.printStackTrace();
        }
    }
    
    // copied from Manager.
    public static String timeAndMemory(long startTime) {
	Runtime runtime = Runtime.getRuntime();
	long totalMemory = runtime.totalMemory()/1024;
	long freeMemory = runtime.freeMemory()/1024;
        return timeAndMemory(startTime, totalMemory, freeMemory);
    }

    public static String timeAndMemory(long startTime,
            long totalMemory, long freeMemory) {
	Runtime runtime = Runtime.getRuntime();
	return System.currentTimeMillis() - startTime
	    + " ms. Memory: "
	    + totalMemory + "K Free: " + freeMemory + "K ("
	    + Math.round( (((double)freeMemory)/((double)totalMemory))
			  * 100.0)
	    + "%)";
    }

    /** If the specified model has a manager and is executing, then
     *  stop execution by calling the finish() method of the manager.
     *  If there is no manager, do nothing.
     *  @param model The model to stop.
     */
    public void stopRun(CompositeActor model) {
        Manager manager = model.getManager();
        if(manager != null) {
            manager.finish();
        }
    }

    /** Wait for all windows to close.
     */
    public synchronized void waitForFinish() {
        while (_openCount > 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** The list of all the models */
    protected List _models = new LinkedList();

    /** The count of currently open windows. */
    protected int _openCount = 0;
    
    /** Are we testing? */
    protected static boolean _test = false;

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Flag indicating that the previous argument was -class.
    // Exists to mirror CompositeActorApplication.
    private boolean _expectingClass = false;

}
