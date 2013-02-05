/* Interface for MetroII event handler.

 Copyright (c) 2012-2013 The Regents of the University of California.
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

package ptolemy.domains.metroII.kernel;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;

/**
 * Yieldadapter interface for getfire(), Any class implementing 
 * this interface has the ability to 'yield return' in getfire()
 * by calling 'resultHandler.handleResult(events)'.
 * 
 * Instead of calling getfire() directly, the caller function 
 * should obtain an YieldAdapterIterable from adapter(). Every 
 * time YieldAdapterIterable.next() is called, getfire() 
 * starts or resumes from the last execution and runs until the 
 * next 'yield return' or 'return'. YieldAdapterIterable.next() 
 * returns an iterable<Event.Builder>, which is the list of 
 * events passed by 'resultHandler.handleResult(events)'. If 
 * YieldAdapterIterable.hasNext() returns false, it means 
 * getfire() has reached 'return' and terminated. 
 * 
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposeRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 */

public interface MetroIIEventHandler {

    /**
     * Return the iterator for the caller function of getfire(). 
     * 
     * @return iterator
     */
    public YieldAdapterIterable<Iterable<Event.Builder>> adapter();

    /**
     * An implementation of getfire() has the ability to 'yield return' 
     * in getfire() by calling 'resultHandler.handleResult(events)'.
     * 
     * @param resultHandler Iterable of events 'yield returned'
     * @throws CollectionAbortedException
     */
    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException;
}
