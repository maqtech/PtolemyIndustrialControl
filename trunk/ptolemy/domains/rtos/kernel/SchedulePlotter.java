/* A debug listener that sends messages to a stream or to standard out.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.rtos.kernel;
//import ptolemy.kernel.util.*;
import ptolemy.plot.*;
import ptolemy.plot.plotml.PlotMLParser;
import java.util.HashMap;
import java.util.ArrayList;

//////////////////////////////////////////////////////////////////////////
//// SchedulePlotter

/**
A director schedule listener that plots the schedule.

@author Johan Eker
@version $Id$
@see NamedObj
@see RecorderListener

*/
public class SchedulePlotter implements ScheduleListener {
    static final int RESET_DISPLAY = -1;
    static final int TASK_SLEEPING = 1;
    static final int TASK_BLOCKED  = 2;
    static final int TASK_RUNNING  = 3;

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create a schedule listener that displays the schedule in 
        a plot window.
     */
    public SchedulePlotter() {
	_taskMap = new HashMap();
	_taskState = new ArrayList();
	try {
	    plot = new Plot();
	    plot.setTitle("RTOS Schedule");
	    plot.setButtons(true);	    
	} catch (Exception e) {
	    System.out.println(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the given scheduling event.
     */
    public void event(String actorName, double time, int scheduleEvent) {
        try {
	    if (scheduleEvent == -1) {
	        plot.clear(true);
                _taskMap.clear();
                _taskState.clear();
	    } else {
	        Object taskID = _taskMap.get(actorName);
                int id; 
	        if (taskID == null) {
	            id = _taskMap.size();
	            _taskMap.put(actorName, new Integer(id));
		    _taskState.add(new Integer(0));
		    plot.addLegend(id, actorName); 
	        } else {
	            id = ((Integer) taskID).intValue();
	        }
	        int _oldState = ((Integer) _taskState.get(id)).intValue();
	        plot.addPoint(id, time, id  + _oldState/2.1, true);
	        plot.addPoint(id, time, id + scheduleEvent/2.1, true);
		plot.repaint();
                _taskState.set(id, new Integer(scheduleEvent)); 
	    }
	} catch (Exception e) {

	}
    }

    public Plot plot;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private HashMap _taskMap;
    private ArrayList _taskState;
    private int _oldScheduleEvent = 0;
}
