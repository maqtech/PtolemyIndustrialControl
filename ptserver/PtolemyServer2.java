/* Ptolemy server which manages the broker, servlet, and simulations.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import ptserver.control.IServerManager;
import ptserver.control.ServerManager;
import ptserver.control.Ticket;

///////////////////////////////////////////////////////////////////
//// PtolemyServer2

/** This class is responsible for launching the message broker, 
 * enabling users to start, pause, and stop simulations through the
 * servlet, and create independently executing simulations upon request.
 * 
 * @author jkillian
 * @version $Id$
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
 */

public class PtolemyServer2 implements IServerManager {

    /**
     * Logger is used throughout the server to log messages to the PtolemyLog.log file
     */
    public static final Logger LOGGER;
    static {
        Logger logger = null;
        try {
            logger = Logger.getLogger(PtolemyServer2.class.getSimpleName());
            FileHandler logFile = new FileHandler("PtolemyLog.log", true);
            logFile.setFormatter(new XMLFormatter());
            logger.addHandler(logFile);
            logger.setLevel(Level.ALL);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER = logger;
    }

    /** Creates an instance of the Ptolemy server.  This class is a singleton 
     * so only one instance should ever exist at a time.
     */
    private PtolemyServer2() {
        this._broker = null;
        this._servletHost = null;
        this._threadReference = new ConcurrentHashMap<Ticket, Thread>();
    }

    /**
     * Initialize the server and loop while waiting for requests.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            // initialize singleton
            PtolemyServer2.getInstance();
        } catch (Throwable e) {
            // TODO Add nicer handling and logging
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////
    ////                public methods
    /**
     * Get the singleton instance of the Ptolemy server and initialize it
     * by launching the required processes (MQTT broker & command servlet.
     */
    public static PtolemyServer2 getInstance() {
        if (_instance == null) {
            synchronized (_syncRoot) {
                if (_instance == null) {
                    _instance = new PtolemyServer2();
                    _instance.initialize();
                }
            }
        }

        return _instance;
    }

    /**
     * Open a thread on which to load the provided model URL and wait
     * for the user to request it's execution.
     * 
     * @param url Path to the model file
     * @exception IllegalStateException Failed to load model file. 
     * with the provided ticket.
     */
    public Ticket open(URL url) throws Exception {

        Ticket ticket = new Ticket(url);
        this._threadReference.put(ticket, new Thread());
        //TODO: launch the simulation thread

        return ticket;
    }

    /**
     * Start the execution of the simulation on the selected thread by
     * activating the Ptolemy manager.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to start simulation thread.
     */
    public void start(Ticket ticket) throws IllegalStateException {
        try {
            this._threadReference.get(ticket).start();
        } catch (NullPointerException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING,
                    this._nullReferenceExceptionMessage);
            throw new IllegalStateException(
                    this._nullReferenceExceptionMessage, e);
        } catch (SecurityException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING, _securityExceptionMessage
                    + ticket.getTicketID() + ".");
            throw new IllegalStateException(_securityExceptionMessage, e);
        } catch (IllegalThreadStateException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING, "Unable to start thread "
                    + ticket.getTicketID() + ".");
            throw new IllegalStateException("Unable to start thread.", e);
        }
    }

    /**
     * Pause the execution of the simulation on the selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to pause simulation thread.
     */
    public void pause(Ticket ticket) throws IllegalStateException {
        try {
            this._threadReference.get(ticket).wait();
        } catch (NullPointerException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING,
                    this._nullReferenceExceptionMessage);
            throw new IllegalStateException(
                    this._nullReferenceExceptionMessage, e);
        } catch (SecurityException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING, _securityExceptionMessage
                    + ticket.getTicketID() + ".");
            throw new IllegalStateException(_securityExceptionMessage, e);
        } catch (IllegalThreadStateException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING, "Unable to pause thread "
                    + ticket.getTicketID() + ".");
            throw new IllegalStateException("Unable to pause thread.", e);
        } catch (InterruptedException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING, "Thread was interrupted "
                    + ticket.getTicketID() + ".");
            throw new IllegalStateException("Thread was interrupted.", e);
        }
    }

    /**
     * Resume the execution of the simulation on the selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to resume simulation thread.
     */
    public void resume(Ticket ticket) throws IllegalStateException {
        try {
            this._threadReference.get(ticket).notify();
        } catch (NullPointerException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING,
                    this._nullReferenceExceptionMessage);
            throw new IllegalStateException(
                    this._nullReferenceExceptionMessage, e);
        } catch (SecurityException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING, _securityExceptionMessage
                    + ticket.getTicketID() + ".");
            throw new IllegalStateException(_securityExceptionMessage, e);
        } catch (IllegalThreadStateException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING, "Unable to stop thread "
                    + ticket.getTicketID() + ".");
            throw new IllegalStateException("Unable to resume thread.", e);
        }
    }

    /**
     * Stop the execution of the simulation on selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to stop simulation thread.
     */
    public void stop(Ticket ticket) throws IllegalStateException {
        try {
            this._threadReference.get(ticket).stop();
        } catch (NullPointerException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING,
                    this._nullReferenceExceptionMessage);
            throw new IllegalStateException(
                    this._nullReferenceExceptionMessage, e);
        } catch (SecurityException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING, _securityExceptionMessage
                    + ticket.getTicketID() + ".");
            throw new IllegalStateException(_securityExceptionMessage, e);
        } catch (IllegalThreadStateException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING, "Unable to stop thread "
                    + ticket.getTicketID() + ".");
            throw new IllegalStateException("Unable to stop thread.", e);
        }
    }

    /**
     * Shutdown the thread associated with the user's ticket.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to destroy simulation thread.
     */
    public void close(Ticket ticket) throws IllegalStateException {
        try {
            this._threadReference.get(ticket).destroy();
        } catch (NullPointerException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING,
                    this._nullReferenceExceptionMessage);
            throw new IllegalStateException(
                    this._nullReferenceExceptionMessage, e);
        } catch (SecurityException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING, _securityExceptionMessage
                    + ticket.getTicketID() + ".");
            throw new IllegalStateException(_securityExceptionMessage, e);
        } catch (IllegalThreadStateException e) {
            PtolemyServer2.LOGGER.log(Level.WARNING,
                    "Unable to destroy thread " + ticket.getTicketID() + ".");
            throw new IllegalStateException("Unable to destroy thread.", e);
        }
    }

    /**
     * Get a listing of the models available on the server in either the
     * database or the local file system.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to discover available models.
     */
    public String[] getModelListing() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    //////////////////////////////////////////////////////////////////////
    ////                private methods
    /**
     * Initialize child processes for the servlet (synchronous command
     * handler) and the MQTT message broker (asynchronous simulation data).
     * 
     * @exception IllegalStateException Failed to start key processes
     */
    private void initialize() throws IllegalStateException {

        //FIXME: does not work on Mac
        //        try {
        //            /** launch the broker **/
        //            ProcessBuilder builder = new ProcessBuilder(_brokerPath);
        //            builder.redirectErrorStream(true);
        //            this._broker = builder.start();
        //        } catch (IOException e) {
        //            PtolemyServer2.logger.log(Level.SEVERE,
        //                    "Unable to spawn MQTT broker process.");
        //            throw new IllegalStateException(
        //                    "Unable to spawn MQTT broker process.", e);
        //        }

        try {
            /** launch the servlet container **/
            this._servletHost = new Server(SERVLET_PORT);
            ServletContextHandler context = new ServletContextHandler(
                    this._servletHost, "/", ServletContextHandler.SESSIONS);
            context.addServlet(new ServletHolder(new ServerManager(this)),
                    "/ServerManager");
            this._servletHost.setHandler(context);
            this._servletHost.start();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to spawn servlet container.", e);
        }
    }

    /**
     * Shut down supporting processes and destroy active simulation threads.
     * 
     * @exception Throwable
     */
    private void shutdown() throws Exception {
        if (this._broker != null) {
            try {
                this._broker.destroy();
                this._broker = null;
            } catch (NullPointerException e) {
                this._broker = null;
            }
        }

        if (this._servletHost != null) {
            try {
                this._servletHost.stop();
                this._servletHost.destroy();
                this._servletHost = null;
            } catch (NullPointerException e) {
                this._servletHost = null;
            }
        }

        Enumeration enumeration = this._threadReference.keys();
        while (enumeration.hasMoreElements()) {
            this.stop((Ticket) enumeration.nextElement());
        }

        this._threadReference.clear();
        this._threadReference = null;
    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables
    private static PtolemyServer2 _instance;
    private static Object _syncRoot = new Object();
    private Process _broker;
    private Server _servletHost;
    private ConcurrentHashMap<Ticket, Thread> _threadReference;

    // TODO Move these to a configuration part and add the other messages
    private static final ResourceBundle CONFIG = ResourceBundle
            .getBundle("ptserver.PtolemyServerConfig");
    private final String _brokerPath = CONFIG.getString("BROKER_PATH");
    private final int _brokerPort = 1883;
    //private String _brokerPath = "C:\\Studio\\rsmb_1.2.0\\windows\\broker.exe";

    private final String _nullReferenceExceptionMessage = "Invalid ticket reference.";
    private final String _securityExceptionMessage = "Unable to modify thread ";
    private final int SERVLET_PORT = Integer.parseInt(CONFIG
            .getString("SERVLET_PORT"));
}
