/*

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.lib;

import ptolemy.domains.dde.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// DDEStringSource
/**


@author John S. Davis II
@version $Id$
*/
public abstract class DDEStringSource extends DDESourceActor {

    /**
     */
    public DDEStringSource(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

	output = new DDEIOPort( this, "output", false, true );
	output.setTypeEquals(StringToken.class);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables		   ////

    public DDEIOPort output;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this actor.
     */
    public abstract LinkedList setUpStrings();

    /** Return the current time of this actor.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	reinvokeAfterDelay( 0.0 );
        _contents = setUpStrings();
    }

    /**
     */
    public void fire() throws IllegalActionException {
        int cnt = 0;
	boolean nextOutputReady = false;
	boolean notFinishedYet = true;
        StringToken strToken = null;
        StringTime strTime = null;

	// FIXME: This is valid because DDESourceActor.initialize()
        // calls getNextToken();
        while( cnt <= _contents.size() ) {
	    if( cnt == _contents.size() ) {
	        notFinishedYet = false;
                getNextToken();
	    }

	    if( notFinishedYet ) {
                getNextToken();
	    }

	    if( nextOutputReady ) {
		output.send( 0, strToken, getCurrentTime() );
		nextOutputReady = false;
	    }

	    if( notFinishedYet ) {
                strTime = (StringTime)_contents.at(cnt);
		strToken = new StringToken( strTime.getString() );
		double fireTime = strTime.getTime();
                reinvokeAfterDelay( fireTime );
	        nextOutputReady = true;
	    }

            cnt++;
        }

        System.out.println(getName()+" is finished with fire()");
    }

    ///////////////////////////////////////////////////////////////////
    ////                        inner class                        ////

    protected class StringTime {
        public StringTime() {
            ;
        }

        public StringTime(double time, String str) {
            _str = str;
            _time = time;
        }

        public void set(double time, String str) {
            _str = str;
            _time = time;
        }

        public String getString() {
            return _str;
        }

        public double getTime() {
            return _time;
        }

        private String _str;
        private double _time = TimedQueueReceiver.INACTIVE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList _contents;


}
