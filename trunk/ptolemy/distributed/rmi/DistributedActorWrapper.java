/* Wrapper for a distributed actor.

@Copyright (c) 2005 The Regents of Aalborg University.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

*/
package ptolemy.distributed.rmi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.distributed.actor.DistributedDirector;
import ptolemy.distributed.actor.DistributedTypedCompositeActor;
import ptolemy.distributed.actor.DistributedTypedIORelation;
import ptolemy.distributed.util.DistributedUtilities;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// DistributedActorWrapper

/**
   The DistributedActorWrapper implements the RemoteDistributedActor interface.
   It is a remote distributed actor and wraps actors inside, making them believe
   they are executed locally while they are distributed. It receives rmi calls.
   The calls to methods that exist in the actor interface are forwarded to the
   wrapped actor.

   @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
   @version
   @since
   @Pt.ProposedRating Red (kapokasa)
   @Pt.AcceptedRating
*/

public class DistributedActorWrapper implements RemoteDistributedActor {

    /** Construct an DistributedActorWrapper. This empty constructor
     *  is required by RMI.
     */
    public DistributedActorWrapper() throws java.rmi.RemoteException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the wrapped actor.
     *
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public void fire() throws java.rmi.RemoteException {
        if (VERBOSE) {
            System.out.println(actor.toString() + " fire()");
        }
        try {
            actor.fire();
        } catch (IllegalActionException e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    /** Return the Internet Address where the wrapper is executing.
     *
     *  @return InetAdress the Internet Address where the distributed actor is being executed.
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public InetAddress getAddress() throws java.rmi.RemoteException {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Begin execution of the wrapped actor.
     *
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public void initialize() throws java.rmi.RemoteException {
        if (VERBOSE) {
            System.out.println(actor.toString() + " initialize()");
        }
        try {
            actor.initialize();
        } catch (IllegalActionException e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    /** Invoke a specified number of iterations of the wrapped actor.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public int iterate(int count) throws java.rmi.RemoteException {
        if (VERBOSE) {
            System.out.println(actor.toString() + " iterate()");
        }
        try {
            return actor.iterate(count);
        } catch (IllegalActionException e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    /** Load a moml representation of an actor. The moml code is processed
     *  before loading.
     *
     *  @param moml The moml code representing the actor to be loaded.
     *  @return True if the loading was successful.
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public boolean loadMoML(String moml) throws java.rmi.RemoteException {

        if (VERBOSE) {
            System.out.println("Loading: " + moml);
        }

        momlParser = new MoMLParser(new Workspace());
        compositeActor = null;
        String processedMoML = processMoML(moml);

        try {
            compositeActor = (DistributedTypedCompositeActor) momlParser.parse(processedMoML);
            actor = (Actor) compositeActor.entityList().get(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Remote Exception: " + e.getMessage());
        }

        if (VERBOSE) {
            System.out.println("MoML Comparison after loading: " +
                    moml.equals(((ComponentEntity) actor).exportMoML()));
        }

        System.out.println("Class: " + actor.getClass() + " loaded.");

        return true;
    }

    /** This method should be invoked once per iteration, after the last
     *  invocation of fire() in that iteration.
     *
     *  @return True if the execution can continue.
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public boolean postfire() throws java.rmi.RemoteException {
        System.out.println(actor.toString() + " postfire()");
        try {
            return actor.postfire();
        } catch (IllegalActionException e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    /** This method should be invoked once per iteration, before the first
     *  invocation of fire() in that iteration. The call is forwarded to the
     *  wrapped actor.
     *
     *  @return True if the iteration can proceed.
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public boolean prefire() throws java.rmi.RemoteException {
        System.out.println(actor.toString() + " prefire()");
        try {
            return actor.prefire();
        } catch (IllegalActionException e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    /** This method should be invoked exactly once per execution
     *  of a model, before any of these other methods are invoked.
     *  The call is forwarded to the wrapped actor.
     *
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public void preinitialize() throws java.rmi.RemoteException {
        System.out.println(actor.toString() + " preinitialize()");
        try {
            actor.preinitialize();
        } catch (IllegalActionException e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    /** Puts copies of the token received into the Receivers included in the IDlist.
     *  The data map contains a token and a list of IDs.
     *
     *  @param data contains a token and a list of IDs.
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public void put(HashMap data) throws RemoteException {

        Token token = (Token) data.keySet().iterator().next();
        LinkedList idsList = (LinkedList) data.get(token);
        if (VERBOSE) {
            System.out.println("Received data. Token: " + token.toString() + " ids: " + idsList);
        }
        for (Iterator ids = idsList.iterator(); ids.hasNext();) {
            Integer id = (Integer) ids.next();
            Receiver receiver = (Receiver) idsReceiversMap.get(id);
            receiver.put(token);
        }
        if (VERBOSE) {
            System.out.println("Data Transferred to receivers");
        }
    }

    /** Set the "virtual connections" concerning the wrapped actor.
     *  The connections HashMap contains a list of ports, and for each
     *  of them a mapping depending on the type of port.
     *  - If the port is an input: a list of receiver IDs that are
     *  contained by the port.
     *  i.e.: inputport,(IDa,...,IDn)
     *  - If it is an output port: a mapping of services to lists of
     *  receivers.
     *  i.e.: outputport,(servicea,(IDi,...,IDk),
     *                         ...
     *                    servicen,(IDn,...,IDs))
     *
     *  For the input ports, a new relation of the type is created of the
     *  type DistributedTypepIORelation for every ID that is received. This
     *  is to force a Receiver to be created whenever createReceivers in the
     *  corresponding port is called.
     *
     *  For the output ports, one only relation of the type DistributedTypepIORelation
     *  is created and the mapping of services to IDs is set into the relation. The
     *  relation sets that same structure in its internal DistributedReceiver that is
     *  in charge of token forwarding whenever send is called on the port.
     *
     *  @param connections a mapping of ports, services and receiver IDs (see before).
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public void setConnections(HashMap connections) throws java.rmi.RemoteException {
        if (VERBOSE) {
            System.out.println("Received Connections: " + connections.toString());
        }

        int number = 0;
        DistributedDirector director = (DistributedDirector) compositeActor.getDirector();

        for (Iterator portsIterator = connections.keySet().iterator();
             portsIterator.hasNext();) {

            String portName = (String) portsIterator.next();
            IOPort port = (IOPort) ((ComponentEntity) actor).getPort(portName);
            DistributedTypedIORelation relation = null;

            if (port.isInput()) {
                Integer[][] integerReceivers = (Integer[][]) connections.get(portName);
                if (VERBOSE) {
                    System.out.println("Receivers received for " + portName + "\n" +
                            DistributedUtilities.integersArrayToString(integerReceivers));
                }
                for (int i = 0; i < integerReceivers.length; i++) {

                    try {
                        relation = (DistributedTypedIORelation) compositeActor.newRelation(portName + number);
                        number += 1;
                        if (VERBOSE) {
                            //                            System.out.println("> for Port : " + portName + " created Relation: " + relation.getName());
                        }
                        port.link(relation);
                    } catch (NameDuplicationException e) {
                        e.printStackTrace();
                    } catch (IllegalActionException e) {
                        e.printStackTrace();
                    }
                }
                director.setListOfIds(DistributedUtilities.convertIntegersToList(integerReceivers));
                try {
                    port.createReceivers();
                } catch (IllegalActionException e) {
                    e.printStackTrace();
                }
                if (VERBOSE) {
                    System.out.println("Receivers created for " + portName + "\n" +
                            DistributedUtilities.receiversArrayToString(port.getReceivers()));
                }


            }

            if (port.isOutput()) {
                try {
                    relation = (DistributedTypedIORelation) compositeActor.newRelation(portName);
                    if (VERBOSE) {
                        //                        System.out.println("> for Port : " + portName + " created Relation: " + relation.getName());
                    }
                    port.link(relation);
                } catch (NameDuplicationException e) {
                    e.printStackTrace();
                } catch (IllegalActionException e) {
                    e.printStackTrace();
                }
                relation.setServicesReceiversListMap((HashMap) connections.get(portName));
            }
        }
        idsReceiversMap = director.getIdsReceiversMap();
        if (VERBOSE) {
            System.out.println("IDs Receivers Map: " + idsReceiversMap.keySet().toString());
        }
    }

    /** Set the port types of the wrapped actor.
     *
     *  @param portTypes is a Map of ports to port types.
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public void setPortTypes(HashMap portTypes) throws RemoteException {
        if (VERBOSE) {
            System.out.println("Received port Types: " + portTypes.toString());
        }

        for (Iterator portsIterator = portTypes.keySet().iterator();
             portsIterator.hasNext();) {

            String portName = (String) portsIterator.next();
            TypedIOPort port = (TypedIOPort) ((ComponentEntity) actor).getPort(portName);
            Type type = (Type) portTypes.get(portName);
            port.setTypeEquals(type);

            // This is not needed, does not trigger runtime type checking on
            // these ports.
            /*
              if (port.isOutput()) {
              DistributedTypedIORelation relation = (DistributedTypedIORelation) compositeActor.getRelation(portName);
              relation.connectedPort().setTypeEquals(type);
              }
            */
        }
    }

    /** Request that execution of the wrapped actor to stop as
     *  soon as possible.
     *
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public void stop() throws java.rmi.RemoteException {
        System.out.println(actor.toString() + " stop()");
        actor.stop();
    }

    /** Request that execution of the current iteration of the
     *  wrapped actor complete.
     *
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public void stopFire() throws java.rmi.RemoteException {
        System.out.println(actor.toString() + " stopFire()");
        actor.stopFire();
    }

    /** Terminate any currently executing model with extreme prejudice.
     *
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public void terminate() throws java.rmi.RemoteException {
        System.out.println(actor.toString() + " terminate()");
        actor.terminate();
    }

    /** This method is invoked exactly once per execution
     *  of an application.
     *
     *  @exception RemoteException is the common superclass for a number of
     *  communication-related exceptions that may occur during the execution of a
     *  remote method call.
     */

    public void wrapup() throws java.rmi.RemoteException {
        System.out.println(actor.toString() + " wrapup()");
        try {
            actor.wrapup();
        } catch (IllegalActionException e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Process a string of moml (corresponding to an actor description)
     *  adding it some extra moml code that embeds it in a DistributedTypedCompositeActor
     *  with a DistributedDirector.
     *
     *  @param moml A String containing moml code that describes an actor.
     *  @return String Containing moml code.
     */

    protected String processMoML (String moml) {

        String header = "<entity name=\"model\" class=\"ptolemy.distributed.actor.DistributedTypedCompositeActor\">\n";
        String director = "<property name=\"Distributed Director\" class=\"ptolemy.distributed.actor.DistributedDirector\"/>";
        String footer = "\n</entity>";
        moml = header + director + moml + footer;

        return moml;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** A distributedTypedCompositeActor that will embed the distributed actor. */
    protected DistributedTypedCompositeActor compositeActor = null;

    /** The distributed actor. */
    protected Actor actor = null;

    /** A MoML parser. */
    protected MoMLParser momlParser = null;

    /**  Map containing information about the ports, services and receiver IDs. */
    protected HashMap idsReceiversMap = new HashMap();

    /**  Turns debugging messages on when true. */
    protected boolean VERBOSE = false;
}

/*
  public boolean loadClass(String className) throws java.rmi.RemoteException {
  Class classTemplate = null;

  try {
  classTemplate = Class.forName(className);
  } catch (ClassNotFoundException e) {
  e.printStackTrace();
  throw new RemoteException("Remote ClassNotFoundException: " + e.getMessage());
  }
  return loadClass(classTemplate);
  }

  public boolean loadClass(Class classTemplate) throws java.rmi.RemoteException {
  System.out.println("Loading class " + classTemplate.getName());

  Class[] argumentsTemplate = {container.getClass(), name.getClass()};
  Object[] arguments = {container, name};

  try {
  constructor = classTemplate.getConstructor(argumentsTemplate);
  } catch (SecurityException e) {
  e.printStackTrace();
  throw new RemoteException("Remote SecurityException: " + e.getMessage());
  } catch (NoSuchMethodException e) {
  e.printStackTrace();
  throw new RemoteException("Remote NoSuchMethodException: " + e.getMessage());
  }

  try {
  actor = (Actor) constructor.newInstance(arguments);
  } catch (IllegalArgumentException e) {
  e.printStackTrace();
  throw new RemoteException("Remote IllegalArgumentException: " + e.getMessage());
  } catch (InvocationTargetException e) {
  e.printStackTrace();
  throw new RemoteException("Remote InvocationTargetException: " + e.getMessage());
  } catch (IllegalAccessException e) {
  e.printStackTrace();
  throw new RemoteException("Remote IllegalAccessException: " + e.getMessage());
  } catch (InstantiationException e) {
  e.printStackTrace();
  throw new RemoteException("Remote InstantiationException: " + e.getMessage());
  }

  System.out.println(((NamedObj) actor).exportMoML());
  System.out.println("InputPorts: ");
  List portList = actor.inputPortList();
  for (int i = 0; i < portList.size(); i++) {
  System.out.println("    " + ((TypedIOPort)portList.get(i)).toString());
  }

  System.out.println("OutputPorts: ");
  portList = actor.outputPortList();
  for (int i = 0; i < portList.size(); i++) {
  System.out.println("    " + ((TypedIOPort)portList.get(i)).toString());
  }
  return true;
  }
*/
/*
  public void sendReceivers(HashMap receiversMap) throws java.rmi.RemoteException {
  if (VERBOSE) {
  System.out.println("Received Receivers: " + receiversMap.toString());

  }
  portsReceiversMap = receiversMap;

  }

  /* (non-Javadoc)
  * @see ptolemy.actor.Executable#stop()
  */
/*
  public void sendServer(ServiceItem server) throws java.rmi.RemoteException {
  if (VERBOSE) {
  System.out.println("Received Service: " + server.serviceID.toString() +
  " in " + ((DistributedActor)server.service).getAddress().toString());

  }

  }
*/
