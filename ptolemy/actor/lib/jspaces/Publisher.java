/* An actor that sends entries to a Java Space.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jspaces;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.LongToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;

import java.rmi.RemoteException;
import net.jini.space.JavaSpace;
import net.jini.core.transaction.TransactionException;
import net.jini.core.lease.Lease;

//////////////////////////////////////////////////////////////////////////
//// Publisher
/**
An actor that pulish TokenEntries to Java Spaces. The Java Space that the
entries are published to is identified by the <i>jspaceName</i>
parameter. TokenEntries in Java Spaces has a name, a serial number, 
and a Ptolemy token. This actor has a single input port.
When the actor is fired, it consumes at most one token from the input
port and publish the token to the Java Space with the name specifed by
the <i>entyName</i> parameter. The serial number of the TokenEntry is
always set to 0. If there is already an entry in the
Java Spaces with the entry name, The new token will override the existing
one. The entry exists in the Java Space as long as the lease time is 
not expired. The lease time of an entry is specified by the 
<i>leaseTime</i> parameter in terms of milliseconds.

@see TokenEntry
@author Jie Liu, Yuhong Xiong
@version $Id$
*/

public class Publisher extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Publisher(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

    	jspaceName = new Parameter(this, "jspaceName",
                new StringToken("JavaSpaces"));
        jspaceName.setTypeEquals(BaseType.STRING);

        entryName = new Parameter(this, "entryName",
                new StringToken(""));
        entryName.setTypeEquals(BaseType.STRING);

        leaseTime = new Parameter(this, "leaseTime",
                new LongToken(Lease.FOREVER));
        leaseTime.setTypeEquals(BaseType.LONG);

        input.setMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The Java Space name. The default name is "JavaSpaces" of
     *  type StringToken.
     */
    public Parameter jspaceName;

    /** The name for the entries to be published. The default value is
     *  an empty string of type StringToken.
     */
    public Parameter entryName;

    /** The lease time for entries written into the space. The default
     *  is Least.FOREVER. This parameter must contain a LongToken.
     */
    public Parameter leaseTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Find the JavaSpaces according to the <i>jspaceName</i> parameter.
     *  If there are already enties in the Java Space with the 
     *  specified entry name, then remove all the old entries.
     *  @exception IllegalActionException If the removal 
     *  action fails due to network problems, transaction errors,
     *  or any remote exceptions.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();
	String name = ((StringToken)jspaceName.getToken()).stringValue();
        _lookupThread = Thread.currentThread();
	_space = SpaceFinder.getSpace(name);
        _lookupThread = null;

        String entryname = ((StringToken)entryName.getToken()).stringValue();
        TokenEntry tokenTemplate = new TokenEntry(name, null, null);
        try {
            TokenEntry oldEntry;
            do {
                oldEntry = (TokenEntry)_space.takeIfExists(
                        tokenTemplate, null, 1000);
            } while (oldEntry != null);
        } catch (RemoteException re) {
	    throw new IllegalActionException(this, "Cannot write into " +
                    "JavaSpace. " + re.getMessage());
	} catch (TransactionException te) {
	    throw new IllegalActionException(this, "Cannot write into " +
                    "JavaSpace. " + te.getMessage());
	} catch (InterruptedException ie) {
            throw new IllegalActionException(this, "Cannot write into " +
                    "JavaSpace. " + ie.getMessage());
        } catch (net.jini.core.entry.UnusableEntryException ue) {
            throw new IllegalActionException(this, "Unusable Entry " +
                    ue.getMessage());
        }
        if (_debugging) {
            _debug(getName(), "Finished preinitialization.");
        }
    }


    /** Read one input token, if there is one, from the input
     *  and publish an entry into the space for the token read.
     *  Do nothing if there's no token in the input port.
     *  @exception IllegalActionException If the publication
     *  action fails due to network problems, transaction errors,
     *  or any remote exceptions.
     */
    public void fire() throws IllegalActionException {
	try {
            if (input.hasToken(0)) {
                Token token = input.get(0);
                String name =
                    ((StringToken)entryName.getToken()).stringValue();
                long time = ((LongToken)leaseTime.getToken()).longValue();

                TokenEntry template = new TokenEntry(name, null, null);
                _space.takeIfExists(template, null, 500);
                TokenEntry entry = new TokenEntry(name,
                        new Long(0), token);
                _space.write(entry, null, Lease.FOREVER);
                if(_debugging) {
                    _debug(getName(), "Publisher writes " + token);
                }
            }
	} catch (RemoteException re) {
	    throw new IllegalActionException(this, "Cannot write into " +
                    "JavaSpace. " + re.getMessage());
	} catch (TransactionException te) {
	    throw new IllegalActionException(this, "Cannot write into " +
                    "JavaSpace. " + te.getMessage());
	} catch (InterruptedException ie) {
            throw new IllegalActionException(this, "Cannot write into " +
                    "JavaSpace. " + ie.getMessage());
        } catch (net.jini.core.entry.UnusableEntryException ue) {
            throw new IllegalActionException(this, "Unusable Entry " +
                    ue.getMessage());
        }
    }

    /** Read one input token, if there is one, from the input port,
     *  publish an entry into the space for the token read, and 
     *  return true.
     *  Simply return true if there's no token in the input port.
     *  @exception IllegalActionException If the publication
     *  action fails due to network problems, transaction errors,
     *  or any remote exceptions.
     */
    public boolean postfire() throws IllegalActionException {
        fire();
        return true;
    }

    /** Kill the lookup thread if it is not returned. The lookup
     *  thread is the thread of the execution of the model.
     */
    public void stopFire() {
        if (_lookupThread != null) {
            _lookupThread.interrupt();
        }
        super.stopFire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // The Java Space.
    private JavaSpace _space;

    // The thread that finds jini.
    private Thread _lookupThread;

}

