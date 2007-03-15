/* Map/Reduce Algorithm

 Copyright (c) 2006-2007 The Regents of the University of California.
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

package ptolemy.actor.ptalon.lib;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import ptolemy.kernel.util.IllegalActionException;

/** The MapReduce Algorithm.

  <p>See <a href="http://labs.google.com/papers/mapreduce.html">MapReduce: Simplified Data Processing on Large Clusters</a>

 @author Adam Cataldo
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public abstract class MapReduceAlgorithm extends Thread {

    /** Return true if the reduce is finished.
     *  @return Return true if the reduce is finished.
     */ 
    public synchronized boolean isReduceFinished()
            throws IllegalActionException {
        if (_threadError) {
            throw new IllegalActionException("Error writing to key "
                    + reduceKey);
        }
        return _threadDone;
    }

    /**
     * Subclasses should implement their map method here.
     * @param key The key passed to the map method.
     * @param value The value passed to the map method.
     * @return The list of key value pairs for the given input.
     */
    public abstract List<KeyValuePair> map(String key, String value);

    /**
     * Subcasses should implement their reduce method here,
     * calling the take method of the BlockingQueue to get 
     * the next value, and checking the parameter noMoreInputs
     * to test if no more values can be put on the queue.  The
     * last value put on the queue may be the empty string.  If
     * this is the case, just discard it.
     * @param key The key to reduce over.
     * @param values The queue of values in reduction.
     * @return The reduced list of valeus.
     * @exception InterruptedException If thrown while reducing.
     */
    public abstract List<String> reduce(String key,
            BlockingQueue<String> values) throws InterruptedException;

    /**
     * This is used to call the reduce algorithm.
     */
    public void run() {
        _threadDone = false;
        _threadError = false;
        try {
            reduceOutput = reduce(reduceKey, reduceValues);
        } catch (InterruptedException e) {
            _threadError = true;
        }
        _threadDone = true;
    }

    /**
     * Set this true when no more inputs values will be given to the
     * reduce method queue.
     */
    public synchronized void setNoMoreInputs() {
        _noMoreInputs = true;
    }

    /**
     * The key for the reduce algorithm, which should be set externally.
     */
    public String reduceKey;

    /**
     * The values for the reduce algorithm, which should be set externally.
     */
    public BlockingQueue<String> reduceValues;

    /**
     * The list generated by the reduce algorithm, which should be read
     * externally.
     */
    public List<String> reduceOutput;

    /** Return true if no more elements will be added to the list.
     * @return true if no more elements will be added to the list.
     */
    protected synchronized boolean isQueueEmpty() {
        return reduceValues.size() == 0 && _noMoreInputs;
    }

    /**
     * True when the run method is done being called.
     */
    private boolean _threadDone = false;

    /**
     * True if an exception was thrown acessing the blocking queue.
     */
    private boolean _threadError = false;

    /**
     * This is set externally to true when the reduceValues queue no longer
     * needs to accept values.
     */
    private boolean _noMoreInputs = false;

}
