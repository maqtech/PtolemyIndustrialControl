/* CalendarQueue, an O(1) implementation of Priority Queue.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Yellow (lmuliadi@eecs.berkeley.edu)
@AcceptedRating Yellow (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.util;

import collections.*;

//////////////////////////////////////////////////////////////////////////
//// CalendarQueue
/**
This class implements a fast priority queue. Entries are sorted ascendingly
according to their sort-key. A dequeue operation will remove the entry
that has the smallest sort-key. As will be explained in the next paragraph,
this class can be used in many different applications, not just for the DE
domain.
<p>
For reusability, the sort-keys are only restricted to be instances of Object.
A client needs to implement the CQComparator interface to define how the
sort-keys are arranged in the queue. This implementation is then passed into
the CalendarQueue's constructor and is immutable. Note that the CQComparator
object can be shared among different independent queues, since it doesn't
contain any state information.
<p>
Note: If the distribution of the number of entries in the queue
is known in advance, it is possible to make further adjustment to the queue
parameters. There are two additional arguments to specify in the second
constructor, namely the minimum number of buckets and the threshold factor.
The first argument specifies the initial number of buckets and also serves
as lower bound on the number of buckets. The second specifies by what
factor the number of bucket will grow and shrink. For optimum performance,
choose these additional arguments so that the number of queue resize operations
is minimized.
<p>
Entries are enqueued using the put() method, and dequeued using the take()
method. The take() method returns the entry associated with the
smallest key.
<p>
CalendarQueue operates like a 'bag' collection. This simply means that an
entry will be added into the queue even if it already exists in the queue.
If a 'set' behavior is desired, one can derive from CalendarQueue and
override the put() method.
<p>
Associated with the take() method, we have getNextKey() and getPreviousKey().
The first returns the current smallest sort-key, while the latter returns
the sort-key associated with the entry that was last dequeued using the
take() method. For example, suppose the smallest-key entry is associated
with value 'CC', and key 'S', then the sequence getNextKey(), take(), and
getPreviousKey() will return 'S', 'CC', and 'S'.
<p>
Current implementation doesn't support enumerating CalendarQueue. This is
chosen because of two reasons. First, Ptolemy 0 implementation doesn't support
it either. Second, the most immediate use of the CalendarQueue class will be
in the Ptolemy II DE domain, which doesn't require this functionality.
<p>
This implementation is based on:
<ul>
<li>Randy Brown, <i>CalendarQueues:A Fast Priority Queue Implementation for
the Simulation Event Set Problem</i>, Communications of the ACM, October 1988,
Volume 31, Number 10.
<li>A. Banerjea and E. W. Knightly, <i>Ptolemy 0 implementation:
CalendarQueue.cc</i>
</ul>
@author Lukito Muliadi
@version $Id$
@see CalendarQueueComparator
*/

public class CalendarQueue {

    public static final boolean DEBUG = false;
    public static final boolean VERBOSE = false;

    /** Construct an empty queue with a given CQComparator object
     *  for arranging entries into bins.
     * @param comparator The CQComparator implementation.
     */
    public CalendarQueue(CQComparator comparator) {

        _cqComparator = comparator;
        // initialization is already done using
        // field initialization during the
        // declaration of variables
    }

    /** Construct an empty queue with the specified CQComparator
     *  implementation, the minimum number of bucket, and the threshold factor.
     * @param comparator The CQComparator implementation.
     * @param minNumBucket The minimum number of bucket.
     * @param thresholdFactor The threshold factor.
     */
    public CalendarQueue(CQComparator comparator,
            int minNumBucket,
            int thresholdFactor) {
        this(comparator);
        _minNumBucket = minNumBucket;
        _thresholdFactor = thresholdFactor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Empty this calendar queue.
     */
    public synchronized void clear() {
        _zeroRef = null;
        _qSize = 0;
        _takenKey = null;
    }


    /** Return the key associated with the object that's at the head of the
     *  queue (i.e. the one that would be obtained on the next take).
     *  If the queue is empty, then an IllegalAccessException will be thrown.
     *
     *  NOTE: due to implementation detail, this method is less efficient
     *  than the similar method getPreviousKey(). Therefore, it is
     *  recommended to use getPreviousKey() whenever possible.
     * @return Object The smallest sort-key in the queue.
     * @exception IllegalAccessException If invoked when the queue is empty.
     */
    public Object getNextKey() throws IllegalAccessException {
        // first check if the queue is empty, if it is, return null
        if (_qSize == 0) {
            throw new IllegalAccessException("Invoking getNextKey() on "+
                    "empty queue is not allowed.");
        }
        // Search buckets starting from index: _minBucket
        //   analogy: starting from the current page(month) of the calendar
        for (int i = _minBucket, j = 0; ; )
            {
                // Starting from the lastBucket, we go through each bucket in
                // a cyclic fashion (i.e. modulo fashion) for one whole cycle.
                // At each bucket, we first determine if the bucket is empty.
                // If not, then we check if the content of the bucket is in the
                // current year. (This latter check is done simply by
                // comparing the virtual bucket number)
                //
                // A bucket that's found to satisfy both condition is where
                // we're going to take our next item.
                //
                // If turns out, after a whole cycle, we still can't find such
                // bucket, then we go to direct search. Using brute force, we
                // compare the items with smallest key of each bucket and find
                // the one with smallest key among those entries. The bucket
                // containing that 'smallest smallest' key item is then
                // recorded as the lastBucket, and we resume by calling take()
                // but now start searching from the new lastBucket.

                if (    !_bucket[i].isEmpty()
                        &&
                        _getBinIndex(_bucket[i].peekKey())
                        == _minVirtualBucket + j
                        ) {

                    // Item to take has been found, remove that
                    // item from the list
                    CQEntry linkFound = (CQEntry)_bucket[i].first();

                    // Return the item found.
                    return linkFound.key;
                }
                else {
                    // Prepare to check next bucket or
                    // else go to a direct search.
                    ++i; ++j;
                    if (i == _nBuckets) i = 0;
                    // If one round of search already elapsed,
                    // then go to direct search
                    if (i == _minBucket) {
                        // Go to direct search
                        break;
                    }
                }
            }
        // Directly search for minimum key event.
        // Find smallest key by examining first event of each bucket.
        // Note that the first event of a bucket corresponds to the
        // one with smallest key among those in that bucket.

        // startComparing being false, indicate we have yet to find
        // a non-empty bucket, so can't compare yet.
        boolean startComparing = false;
        long minVirtualBucket = 0;
        int minBucket = -1;
        Object minKey = null;
        for (int i = 0; i < _nBuckets; i++) {
            // First check if bucket[i] is empty
            if (_bucket[i].isEmpty()) {
                // do nothing if it is empty
            } else {
                if (!startComparing) {
                    minBucket = i;
                    minKey = _bucket[i].peekKey();
                    minVirtualBucket = _getBinIndex(minKey);
                    startComparing = true;
                } else {
                    Object maybeMinKey = _bucket[i].peekKey();
                    if (_cqComparator.compare(maybeMinKey, minKey) < 0) {
                        minKey = maybeMinKey;
                        minVirtualBucket = _getBinIndex(minKey);
                        minBucket = i;
                    }
                }
            }
        }

        if (minBucket == -1)
            throw new IllegalStateException(
                    "Direct search error in CalendarQueue.getNextKey"
                    );
        // Return the smallest key of minBucket
        return ((CQEntry)_bucket[minBucket].first()).key;

    }

    /** Return the key of the last object dequeued using take() method.
     *  If the queue <i>was</i> empty when the last take() method was invoked,
     *  then throw an exception (Note that the last take() method would have
     *  also thrown an exception). If take() has never been called, then throw
     *  an exception as well.
     *  NOTE: a typical application would call take() followed
     *  by getPreviousKey() to get the value and its corresponding
     *  key, respectively.
     *
     * @return The sort key associated with the last entry dequeued by the
     *  take() method.
     * @exception IllegalAccessException If invoked when the queue is empty.
     */
    public Object getPreviousKey() throws IllegalAccessException {
        // First check if _takenKey == null which means either the last take()
        // threw an exception or take() has never been called. If it is then
        // thrown an exception.
        if (_takenKey == null) {
            throw new IllegalAccessException("No take() or valid take()" +
                    " precedes this operation");
        }
        return _takenKey;
    }

    /** Query whether a specific entry is in the queue,
     *  return true is succeed and false otherwise.
     *
     * @param key The sort-key of the entry
     * @param value The value of the entry
     *
     * @return boolean
     */
    public boolean includes(Object key, Object value) {
        // if the queue is empty then return false
        if (_qSize == 0) return false;

        // create a CQEntry object to wrap value and key
        CQEntry cqEntry = new CQEntry(value, key);

        // calculate i, the index into the queue array
        long i = _getBinIndex(key);
        i = i % _nBuckets;
        if (i < 0)
            i += _nBuckets;

        // call the includes method in private
        // class CQLinkedList.
        return _bucket[(int)i].includes(cqEntry);
    }

    /** Check whether the queue is empty.
     * @return True if empty, false otherwise.
     */
    public boolean isEmpty() {
        if (_qSize == 0) {
            // also need to set _minKey equal to null,
            // because a lower bound of empty set doesn't make sense.
            _minKey = null;
            return true;
        } else {
            return false;
        }
    }

    /** Add one entry to the queue. An entry is specified by its key and
     *  its value. If the key is null, then an IllegalArgumentException
     *  is thrown.
     *  <p>
     *  The method returns true if the operation succeeded, and false
     *  otherwise. Since the CalendarQueue class adopts 'bag' behavior,
     *  it will always return true, unless there's an exception thrown.
     *  <p>
     *  Later, when need arises to have subclass of CalendarQueue that adopts
     * 'set' behavior, then the overridden put method should return false
     *  if the entry is already in the queue, and true otherwise.
     *
     * @param key The key of the entry to be put/added into the queue.
     * @param value The value of the entry to be put/added into the queue.
     * @return True is succeed, false otherwise.
     * @exception IllegalArgumentException The key may not be null.
     */
    public synchronized boolean put(Object key, Object value) {
        if (key == null)
            throw new IllegalArgumentException(
                    "CalendarQueue.put() can't accept null key"
                    );


        // if this is the first put since the queue creation,
        // then do initialization.
        // The initialization is deferred until this stage because
        // when the queue is created, we don't know what kind of
        // sort-key implementation we'll get, so can't
        // initialize the zeroRef.
        // The zero reference is chosen to be the first entry, while
        // initial bin width is obtained by passing null argument to
        // the getBinWidth() method.
        if (_zeroRef == null) {
            _zeroRef = key;
            _qSize = 0;
            _localInit(_minNumBucket, _cqComparator.getBinWidth(null), key);
        }

        // create a CQEntry object to wrap value and key
        CQEntry cqEntry = new CQEntry(value, key);

        // calculate i, the index into the queue array according to
        // these steps:
        // 1. quantize the sort-key object using the getBinIndex() method.
        // 2. calculate the modulo with respect to nBuckets.
        // 3. add nBuckets if necessary to get a positive modulo.

        long i = _getBinIndex(key);
        i = i % _nBuckets;
        if (i < 0)
            i += _nBuckets;

        // if _minKey equal to null (happens when there are no entries
        // in the queue) or the new entry has lower key than the current
        // smallest key) then update.
        if (_minKey == null || _cqComparator.compare(key, _minKey) < 0) {
            _minKey = key;
            _minVirtualBucket = _getBinIndex(_minKey);
            _minBucket = (int)(_minVirtualBucket % _nBuckets);
            if (_minBucket < 0) _minBucket += _nBuckets;
        }

        // Insert entry into bucket i in sorted list
        _bucket[(int)i].insertAndSort(cqEntry);
        // Increase the queue size
        ++_qSize;

        // double the calendar size if needed
        if (_qSize > _topThreshold) {
            _resize(_nBuckets*_thresholdFactor);
        }

        // Notify other thread waitings on the CalendarQueue object, that
        // the content has changed.
        notifyAll();

        return true;
    }

    /** Remove a specific entry (specified by the key and the
     *  value). This method returns true if the entry
     *  is found and successfully removed, and returns false
     *  if it is either not found or the queue is empty.
     *  <p>
     *  The equality is tested by doing this operation:
     *  value.equal(value2) && key.equal(key2), with value2 and key2 ranging
     *  through all elements in the queue.
     *  <p>
     *  If there are multiple entries in the queue that satisfies the test
     *  described above, only the first one is dequeued. The first one always
     *  correspond to the one enqueued first among those multiple entries.
     *  Therefore, it follows FIFO behavior.
     * @param key the sort-key corresponding to that object
     * @param value the object that you want to remove
     * @return true is succeed, false otherwise
     */
    public synchronized boolean remove(Object key, Object value) {
        // if the queue is empty then return false
        if (_qSize == 0) {
            return false;
        }

        // create a CQEntry object to wrap obj and priority
        CQEntry cqEntry = new CQEntry(value, key);

        // calculate i, the index into the queue array
        long i = _getBinIndex(key);
        i = i % _nBuckets;
        if (i < 0)
            i += _nBuckets;

        // Remove the object by calling the method in
        // inner class CQLinkedList
        boolean result = _bucket[(int)i].remove(cqEntry);

        // if the operation succeeded then reduces the number of
        // element in the queue.
        if (result) {
            _qSize--;
        }
        return result;
    }

    /** Return the queue size.
     * @return The queue size.
     */
    public int size() {
        return _qSize;
    }

    /** Remove the smallest entry from the queue and return the value
     *  associated with that entry. If there are multiple smallest entries,
     *  then FIFO behavior is implemented. Note that since values are
     *  permitted to be null, this method could return null.
     *  <p>
     *  If this method is called while the queue is empty, then an
     *  IllegalAccessException is thrown.
     * @return The value associated with the smallest key.
     * @exception IllegalAccessException If invoked when the queue is empty.
     */
    public synchronized Object take() throws IllegalAccessException {
        // first check if the queue is empty, if it is, return null
        if (_qSize == 0) {
            _takenKey = null;
            _minKey = null;
            throw new IllegalAccessException("Invoking take() on empty"+
                    " queue is not allowed.");
        }

        // Search buckets starting from index: _minBucket
        //   analogy: starting from the current page(month) of the calendar
        for (int i = _minBucket, j = 0; ; )
            {
                // Starting from the lastBucket, we go through each bucket in
                // a cyclic fashion (i.e. modulo fashion) for one whole cycle.
                // At each bucket, we first determine if the bucket is empty.
                // If not, then we check if the content of the bucket is in the
                // current year. (This latter check is done simply by
                // comparing the virtual bucket number)
                //
                // A bucket that's found to satisfy both condition is where
                // we're going to take our next item.
                //
                // If turns out, after a whole cycle, we still can't find such
                // bucket, then we go to direct search. Using brute force, we
                // compare the items with smallest key in each bucket and find
                // the one with smallest key among those items. The bucket
                // containing that 'smallest smallest' entry is then
                // recorded as the lastBucket, and we resume by calling take()
                // but now start searching from the new lastBucket.

                if (    !_bucket[i].isEmpty()
                        &&
                        _getBinIndex(_bucket[i].peekKey())
                        == _minVirtualBucket + j
                        ) {

                    // Item to take has been found, remove that
                    // item from the list
                    CQEntry linkFound = (CQEntry)_bucket[i].take();

                    // Update position on calendar
                    _minBucket = i;
                    _minKey = linkFound.key;
                    _minVirtualBucket = _getBinIndex(_minKey);
                    --_qSize;

                    // Halve calendar size if needed.
                    if (_qSize < _botThreshold) {
                        // if it is already minimum, then do nothing.
                        if (_nBuckets != _minNumBucket) {
                            if (_nBuckets/_thresholdFactor > _minNumBucket) {
                                _resize (_nBuckets/_thresholdFactor);
                            } else {
                                _resize (_minNumBucket);
                            }
                        }
                    }
                    // Return the item found.
                    _takenKey = linkFound.key;
                    return linkFound.value;
                }
                else {
                    // Prepare to check next bucket or
                    // else go to a direct search.
                    ++i; ++j;
                    if (i == _nBuckets) i = 0;
                    // If one round of search already elapsed,
                    // then go to direct search
                    if (i == _minBucket) {
                        // Go to direct search
                        break;
                    }
                }
            }
        // Directly search for minimum key entry.
        // Find smallest entry by examining first event of each bucket.
        // Note that the first entry of a bucket corresponds to the
        // one with smallest key among those in that bucket.

        // startComparing being false, indicate we have yet to find
        // a non-empty bucket, so can't compare yet.
        boolean startComparing = false;
        long minVirtualBucket = 0;
        int minBucket = -1;
        Object minKey = null;
        for (int i = 0; i < _nBuckets; i++) {
            // First check if bucket[i] is empty
            if (!_bucket[i].isEmpty()) {

                if (!startComparing) {
                    minBucket = i;
                    minKey = _bucket[i].peekKey();
                    minVirtualBucket = _getBinIndex(minKey);
                    startComparing = true;
                } else {
                    Object maybeMinKey = _bucket[i].peekKey();
                    if (_cqComparator.compare(maybeMinKey, minKey) < 0) {
                        minKey = maybeMinKey;
                        minVirtualBucket = _getBinIndex(minKey);
                        minBucket = i;
                    }
                }

            }
        }

        if (minBucket == -1)
            throw new IllegalStateException("Failed Direct search");
        // Set lastBucket, lastPrio, and bucketTop for this event
        _minBucket = minBucket;
        _minVirtualBucket = minVirtualBucket;
        _minKey = minKey;
        // Resume search at that minimum bucket
        return (take());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // This function calculates the width to use for buckets
    // It does so by these steps:
    // a. Figure out how many samples to take (as a function of qSize)
    // b. Disable resize and take nSamples elements from the queue
    // c. Record the statistics of those elements
    // d. Put the element back in and re-enable resize
    // e. Pass the array containing the data into the priority class
    //    to let it calculate the width object.

    private Object _computeNewWidth() {
        int nSamples;
        Object[] sampledData;
        Object[] sampledKey;
        /* Decide how many queue elements to sample */
        if (_qSize < 2) return _cqComparator.getBinWidth(null);
        if (_qSize <= 5) {
            nSamples = _qSize;
        }
        else {
            nSamples = 5 + _qSize/10;
        }
        if (nSamples > 25) {
            nSamples = 25;
        }
        // Record lastPrio, lastBucket, bucketTop
        Object savedLastPrio = _minKey;
        int savedLastBucket = _minBucket;
        long savedLastVirtualBucket = _minVirtualBucket;

        // Take nsample events from the queue and record their priorities
        // with resizeEnabled equal to false
        // Data sample is done by dequeuing nsample elements, record
        // their values, and then enqueuing them back in again.
        // With resizeEnabled equal to false, the overhead of sampling
        // process is reduced. Besides, more importantly, to do resize
        // we need to call this method (computeNewWidth) anyway; so it
        // also prevents infinite loop.
        //
        _resizeEnabled = false;
        sampledData = new Object[nSamples];
        sampledKey = new Object[nSamples];
        for (int i = 0; i < nSamples; ++i) {
            try {
                sampledData[i] = take();
            } catch (IllegalAccessException e) {
                // do nothing
            }
            try {
                sampledKey[i] = getPreviousKey();
            } catch (IllegalAccessException e) {
                // do nothing
            }
        }
        // Restore the sampled events to the queue using put
        for (int i = nSamples-1; i >= 0; --i) {
            put(sampledKey[i], sampledData[i]);
        }

        // Done sampling data and putting them back in
        // therefore, we can reenable resize
        _resizeEnabled = true;
        // Restore lastPrio, lastBucket, and bucketTop
        _minKey = savedLastPrio;
        _minBucket = savedLastBucket;
        _minVirtualBucket = savedLastVirtualBucket;

        /* Calculate average separation of sampled events */
        return _cqComparator.getBinWidth(sampledKey);
    }

    // Note: This is basically a macro..
    private long _getBinIndex(Object key) {
        return _cqComparator.getBinIndex(key, _zeroRef, _width);
    }

    // do local initialization on bucket[] array starting from index qbase,
    // as many as nbuckets. This method is called inside resize() method and
    // during the first invocation of put.
    //
    // nbuckets: number of total buckets
    // bwidth: bucket width
    // startprio: starting date of the new calendar
    private void _localInit(
            int nbuckets,
            Object bwidth,
            Object startkey) {

        _width = bwidth;
        _nBuckets = nbuckets;
        _bucket = new CQLinkedList[_nBuckets];

        for (int i = 0; i < _nBuckets; ++i) {
            // initialize each bucket with an empty CQLinkedList
            // that uses _cqComparator for sorting.
            _bucket[i] = new CQLinkedList();
        }

        // Set up initial position in queue
        _minKey = startkey;
        _minVirtualBucket = _getBinIndex(startkey);
        _minBucket = (int)(_minVirtualBucket % _nBuckets);
        if (_minBucket < 0) _minBucket += _nBuckets;

        // Set up queue size change threshold
        // Theoretically we can use botThreshold and topThreshold equal to
        // half and twice nBuckets, respectively. But in practice, botThreshold
        // is calculated as nBuckets/2 - 2. The minus two is here, so that
        // nBuckets will be biased to larger value.
        _botThreshold = _nBuckets/_thresholdFactor;
        _topThreshold = _nBuckets*_thresholdFactor;
    }

    // Copy the queue onto a calendar with newsize number of buckets.
    // Unlike described in the paper, the new calendar is reallocated
    // each time the number of buckets (_nBuckets) changes.
    // The resize methods follow these steps:
    // 1. a new width is computed as a function of the element statistics.
    // 2. Save the old queue to a temp variable.
    // 3. Create the new queue and initialize it
    // 4. Do transfer of all elements in the old queue into the new queue
    private void _resize(int newsize) {
        Object new_width;
        int old_nbuckets;
        CQLinkedList[] old_bucket;

        if (!_resizeEnabled) return;

        if (DEBUG) {
            System.out.println("Resize method is called with old size = " +
                    _nBuckets + " and new size = " + newsize +
                    " and queue size = " + _qSize);
        }

        // Find new bucket width
        new_width = _computeNewWidth();

        // Save location and size of old calendar
        // for use when copying calendar
        old_bucket = _bucket;
        old_nbuckets = _nBuckets;

        // Initialize new calendar
        _localInit(newsize, new_width, _minKey);

        // Note that the old buckets already contain sorted linked list. So,
        // what I do here is to do a merge sort on them, and at the same time
        // passing iteration, place each element into the appropriate location
        // in the new buckets.
        // I checked with a profiler tool that this extra complexity sped up
        // the put() and take() operation.
        do {
            // at each iteration, pick the smallest element among all elements
            // that the old buckets 'head pointer' pointed to.

            // initialize the minimum cell and minimum index.
            LLCell minCell = null;
            int minIndex = -1;

            // go through each of the old buckets.
            for (int i = 0; i < old_nbuckets; i++) {

                // Useful debugging stuff.. you can ignore this..
                if (DEBUG && VERBOSE) {
                    for (int j = 0; j < _nBuckets; j++) {
                        System.out.print("Bucket" + j + " : ");
                        LLCell curr = _bucket[j].head;
                        while (curr != null) {
                            CQEntry entry = (CQEntry)curr.element();
                            System.out.print(((Double)entry.key).doubleValue());
                            System.out.print(" , ");
                            curr = curr.next();
                        }
                        System.out.println("");
                    }
                }
                if (DEBUG && VERBOSE) {

                    for (int j = 0; j < old_nbuckets; j++) {
                        System.out.print("old Bucket" + j + " : ");
                        LLCell curr = old_bucket[j].head;
                        while (curr != null) {
                            CQEntry entry = (CQEntry)curr.element();
                            System.out.print(((Double)entry.key).doubleValue());
                            System.out.print(" , ");
                            curr = curr.next();
                        }
                        System.out.println("");
                    }
                }

                // check if this particular old bucket is non-empty.
                if (old_bucket[i].head != null) {
                    LLCell cell = old_bucket[i].head;
                    // if minCell has never been updated (still equal to the
                    // initialized value, i.e. null), then always
                    // accept this cell (one that belong to the
                    // current old bucket)as the minimum.
                    if (minCell == null) {
                        minCell = cell;
                        minIndex = i;
                    } else {
                        // Useful debugging information, you can ignore it.
                        if (DEBUG) {
                            if (_cqComparator.compare(
                                    ((CQEntry)cell.element()).key,
                                    ((CQEntry)minCell.element()).key
                                    ) == 0) {
                                System.out.println("Different bucket has "+
                                        "equal element.. no way!");
                            }
                        }
                        // minCell is a valid value, so we can compare it with
                        // the current old bucket 'head cell'. Then update
                        // minCell if minCell is not less than the 'head cell'.
                        if (_cqComparator.compare(
                                ((CQEntry)cell.element()).key,
                                ((CQEntry)minCell.element()).key
                                ) < 0) {
                            minCell = cell;
                            minIndex = i;
                        }
                    } // if (minCell == null) with the else case.
                    // finished with this bucket.

                } // if (old_bucket[i].head != null)
                // since old_bucket[i].head == null then skip this empty bucket.

            } // for (int i = 0; i < old_nbuckets; i++)
            // this is the end of one iteration through the whole old buckets
            // set.

            // up to this point, it is either minCell still null, or minCell
            // equal to the minimum cell of this iteration.
            if (minCell == null) {
                // Since minCell is still equal to the initialized value,
                // that means no more of the old buckets is non-empty..
                // This means that we finished emptying the old buckets
                // and moving it into the new buckets.. Yipee... done!
                break;
            } else {
                // minCell is obtained, we update old_bucket[minIndex] by
                // removing its head of the list. (because that's the minimum,
                // remember that all buckets are internally sorted already)

                // update old_bucket[minIndex].
                // Make the head pointer to point to the next link.
                old_bucket[minIndex].head = minCell.next();
                // If minCell happen to be old_bucket[minIndex] tail, then
                // set tail to be null, because it just got removed.
                if (old_bucket[minIndex].tail == minCell) {
                    old_bucket[minIndex].tail = null;
                }

                // Now that I finished updating the old_bucket, let's put
                // the minCell into the new queue. The minCell will be put
                // at the tail of a bucket in the new queue.

                // First calculate the index, using cqComparator methods.
                long newindex = _getBinIndex(((CQEntry)minCell.element()).key);
                newindex = newindex % _nBuckets;
                if (newindex < 0) {
                    newindex += _nBuckets;
                }

                // Zero in on the targetted bucket in the new buckets set.
                CQLinkedList targetLL = _bucket[(int)newindex];

                // Since minCell is being put at tail position, make sure
                // its next field point to null.
                // BTW, removing this will result in a bug that will take hours
                // to find! Ask the author (lmuliadi) :-)
                minCell.next(null);
                // Check if targetLL is an empty list.. Note that empty list
                // will have both head and tail fields to be null.
                if (targetLL.tail != null) {
                    // targetLL is not an empty list, so append minCell to
                    // the end of the list.
                    targetLL.tail.next(minCell);
                    // Debugging stuff.. ignore..
                    if (DEBUG) {
                        if (targetLL.tail == minCell) {
                            System.out.println("Circular dependency!");
                        }
                    }
                    // Update the tail field.
                    targetLL.tail = minCell;
                } else {
                    // Debugging stuff.. ignore...
                    if (DEBUG) {
                        if (targetLL.head != null) {
                            System.out.println("Tail equal to null, " +
                                    "but head's not ???");
                        }
                    }
                    // targedLL is an empty list, so after updating it will
                    // have one element which is minCell.
                    // Both tail and head field should point to this sole
                    // 'new' element.
                    targetLL.tail = minCell;
                    targetLL.head = minCell;
                }
            }
            // keep iterating until a break is issued when the old buckets
            // are all empty.
        } while (true);

    }


    ////////////////////////////////////////////////////////////////////////
            ////                 private inner class                            ////

            // CQEntry: encapsulate both the objects and its priority
            // to be inserted into the queue.
            private class CQEntry {
                // Construct a CQEntry with the supplied content (obj)
                // and priority (priority)
                public CQEntry(Object v, Object k) {
                    value = v;
                    key = k;
                }

                // override Object.equal() method
                // This is needed, because 2 CQEntry object being equal
                // doesn't mean cqEntry_a == cqEntry_b, but instead
                // that both their members (object and priority) are equals.
                public boolean equals(Object obj) {
                    if (!(obj instanceof CQEntry)) {
                        return false;
                    } else {
                        CQEntry snd = (CQEntry) obj;
                        boolean sameValue = false;
                        boolean sameKey = false;

                        if (value == null && snd.value == null) {
                            sameValue = true;
                        } else if (value == null && snd.value != null) {
                            sameValue = false;
                        } else if (value.equals(snd.value)) {
                            sameValue = true;
                        }

                        if (this.key == null || snd.key == null) {
                            throw new IllegalStateException(
                                    "Bug in CalendarQueue.CQEntry.equals"
                                    );
                        } else if (this.key.equals(snd.key)){
                            sameKey = true;
                        }

                        if (sameValue && sameKey) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }

                // I want to make this public, so it'll be more efficient
                // than using method call to access these fields.
                public Object value;
                public Object key;

            }

    // CQLinkedList
    // This class implement a function ( insertAndSort) that does
    // both the insertion of entry into the linked list and then call sort
    // Note that for efficiency, this class just uses the Comparator object
    // from CalendarQueue class. I.e. it uses _cqComparator field for sorting.
    private class CQLinkedList {

        // Construct an empty CQLinkedList.
        public CQLinkedList() {
            head = null;
            tail = null;
        }

        public Object first() {
            return head.element();
        }

        public boolean includes(Object obj) {
            return (head.find(obj) != null);
        }

        public boolean isEmpty() {
            return (head == null);
        }

        // Insert and sort into the linked list.
        // Use FIFO behaviour when objects with the same weight are
        // encountered.
        public void insertAndSort(Object obj) {

            // Special cases:
            // Linked list is empty.
            if (head == null) {
                head = new LLCell(obj, null);
                tail = head;
                return;
            }

            // LinkedList is not empty.
            // I assert that by construction, when head != null, tail != null
            // as well.

            // Check if obj is greater than or equal to tail.

            if ( _cqComparator.compare(
                    ((CQEntry)obj).key,
                    ((CQEntry)tail.element()).key) >= 0
                 ) {
                // obj becomes new tail.
                LLCell newTail = new LLCell(obj, null);
                tail.next(newTail);
                tail = newTail;
                return;
            }

            // Check if head is strictly greater than obj
            if ( _cqComparator.compare(
                    ((CQEntry)head.element()).key,
                    ((CQEntry)obj).key) > 0
                 ) {
                // obj becomes the new head
                head = new LLCell(obj, head);
                return;
            }

            // No more special cases..
            // Iterate from head of queue and progresses..
            LLCell prevCell = head;
            LLCell currCell = prevCell.next();
            // Note that this loop will always terminate via the return
            // statement. This is because tail is made sure to be strictly
            // greater than obj.
            do {
                // check if currCell is strictly greater than obj
                if ( _cqComparator.compare(
                        ((CQEntry)currCell.element()).key,
                        ((CQEntry)obj).key) > 0
                     ) {
                    // insert obj between prevCell and currCell
                    LLCell newcell = new LLCell(obj, currCell);
                    prevCell.next(newcell);
                    return;
                }
                prevCell = currCell;
                currCell = prevCell.next();
            } while (currCell != null);

        }


        // basically a macro, to shorten typing
        public Object peekKey() {
            if (head != null)
                return ((CQEntry)head.element()).key;
            else
                return null;
        }

        // remove a specific element from the queue.
        // NOTE: it only removed the first element found from the linked list.
        // More specifically, this element would be the one closest to the
        // head of the linked list.
        // returns true if succeed, false otherwise
        public boolean remove(CQEntry cqEntry) {

            // two special cases:
            // Case 1: linked-list is empty.. always return false
            if (head == null) return false;
            // Case 2: The element I want is at head of linked-list
            if (head.element().equals(cqEntry)) {
                // Remove the 'head' cell.
                if (head != tail) {
                    // Linked list has at least two cells.
                    head = head.next();
                } else {
                    // Linked list contains only one cell
                    head = null;
                    tail = null;
                }
                return true;
            }

            // non-special case that requires looping...
            LLCell prevCell = head;
            LLCell currCell = prevCell.next();

            do {
                if (currCell.element().equals(cqEntry)) {
                    // Cool! currCell contains the entry to be removed.
                    // Remove that link and the return true.
                    if (tail == currCell) {
                        // Removing tail.. need to update
                        tail = prevCell;
                    }
                    prevCell.next(currCell.next());
                    return true;
                }
                // Too bad.. currCell doesn't contain cqEntry. Iterate one link
                // down.
                prevCell = currCell;
                currCell = currCell.next();
            } while (currCell != null);

            // Out of the loop, which means currCell == null, i.e. entry
            // not found.
            return false;
        }

        public Object take() {
            // remove the head
            LLCell oldhead = head;
            head = head.next();
            if (head == null) {
                tail = null;
            }
            return oldhead.element();
        }

        // These fields are public because I can assume that the outer
        // class know what it is doing.
        // head
        public LLCell head;
        // tail
        public LLCell tail;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // _nBuckets: number of buckets in the current calendar
    private int _nBuckets;
    // _qSize: current queue size
    private int _qSize = 0;
    // _width: Object representing width of each bucket
    private Object _width;
    // _topThreshold: largest queue size before number of buckets get doubled
    private int _topThreshold;
    // _botThreshold: smallest queue size before number of buckets get halfed
    private int _botThreshold;
    // _zeroRef: the point zero needed to quantize a Object object
    private Object _zeroRef = null;
    // _bucket: an array of nBuckets buckets
    private CQLinkedList[] _bucket;

    // _minNumBucket: the number of buckets to start with and the lower bound
    //  on the number of buckets.
    private int _minNumBucket = 2; // default values
    // _thresholdFactor: the factor by which to multiply (or divide)
    //  the number of bins to get the top threshold (or the bottom threshold).
    private int _thresholdFactor = 2; // default values


    // _resizeEnabled: enable/disable resize() invocation
    private boolean _resizeEnabled = true;

    // _cqComparator: Comparator to determine how entries are put into bins.
    /* private */ CQComparator _cqComparator;

    // _takenKey: save the key corresponding to the entry removed by the
    //  take() method.
    private Object _takenKey = null;

    // _minKey: all elements in the queue is of lower or equal
    //    priority from this _minKey, hence the name.
    private Object _minKey = null;
    // _minVirtualBucket: at all times equal to the quantized value of
    //    _minKey.
    private long _minVirtualBucket;
    // _minBucket: at all times equal to the positive modulo of
    //    _minKey with _nBuckets.
    private int _minBucket;

}





