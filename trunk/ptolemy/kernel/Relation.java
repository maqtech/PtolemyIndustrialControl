/* A Relation serves as a connection class between Entities in 
a hierarchical graph.

 Copyright (c) 1997 The Regents of the University of California.
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

package pt.kernel;

import java.util.Hashtable;
import java.util.Enumeration;
import pt.exceptions.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Relation
/** 
/* A Relation serves as a connection class between Entities in a 
hierarchical graph. A Relation connects n links such that each link has 
access to the other n-1 links. In our case, a "link" is a Port. We say 
that a Relation is <\EM> dangling </EM> if it has only one Port connected 
to it.  FIXME: Eventually we will set a variable in Port for determining if 
it is connected to a dangling Relation. 
@author John S. Davis, II
@version $Id$
*/
public class Relation extends Node {
    /** 
     */	
    public Relation() {
	super();
    }

    /** 
     * @param name The name of the Relation.
     */	
    public Relation(String name) {
	super(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Connect a Port to this Relation.
     * @param port The Port being connected to the Relation.
     * @exception NameDuplicationException Attempt to store two instances of
     * the same class with identical names in the same container.
     */	
    public void connectPort(Port port) throws NameDuplicationException {
	Port duplicatePort;
	if( _links == null ) {
	     _links = new Hashtable();
	}
	duplicatePort = (Port)_links.put( port.getName(), port );
	if( duplicatePort != null ) {
	     duplicatePort = (Port)_links.put
		( duplicatePort.getName(), duplicatePort );
	     throw new NameDuplicationException( duplicatePort.getName() );
	}
        return;
    }

    /** Disconnect a Port from this Relation.
     * @param port The Port being disconnected from the Relation.
     * @return Return the disconnected Port; returns null if the Port is
     * not found.
     */	
    public Port disconnectPort(Port port) {
	if( _links == null ) {
	     return null;
	}
	return (Port)_links.remove( port.getName() );
    }

    /** Return the Ports which are connected to this Relation.
     * @return Return an Enumeration of Ports; returns null if the
     * collection of Ports is null.
     */	
    public Enumeration getPorts() {
	if( _links == null ) {
	     return null;
	}
	if( size() == 0 ) {
	     return null;
	}
        return _links.elements();
    }

    /** Initialize this Relation.
     */
    public void init() {}

    /** Determine if the Relation is dangling? By dangling, we mean that the
     *  Relation has exactly one Port connection.
     * @return Return true if the Relation is dangling; returns false otherwise.
     */	
    public boolean isDangling() {
	if( _links == null ) {
	     return false;
	}
	if( _links.size() == 1 ) {
	     return true;
	}
        return false;
    }

    /** Determine if a given Port is connected to this Relation.
     * @param portName The name of the Port for which we check connectivity.
     * @return Return true if the Port is connected to this Relation. Return 
     * false otherwise.
     */	
    public boolean isPortConnected(String portName) {
	if( _links == null ) {
	     return false;
	}
	return _links.containsKey( portName );
    }

    /** Return the number of Ports connected to the net.
     */	
    public int size() {
	if( _links == null ) {
	     return 0;
	}
        return _links.size();
    }

    /** Generate a Relation object from a String.
     *  FIXME: How should this method be implemented??
     */
    public static Relation valueOf(String string) {
        return new Relation(string);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* A hashtable of links which are connected to this Relation.
     */
    private Hashtable _links;
}






