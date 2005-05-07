/* AbstractList.java -- Abstract implementation of most of List
   Copyright (C) 1998, 1999, 2000, 2001, 2002 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */
package ptolemy.backtrack.util.java.util;

import java.lang.Object;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

/** 
 * A basic implementation of most of the methods in the List interface to make
 * it easier to create a List based on a random-access data structure. If
 * the list is sequential (such as a linked list), use AbstractSequentialList.
 * To create an unmodifiable list, it is only necessary to override the
 * size() and get(int) methods (this contrasts with all other abstract
 * collection classes which require an iterator to be provided). To make the
 * list modifiable, the set(int, Object) method should also be overridden, and
 * to make the list resizable, the add(int, Object) and remove(int) methods
 * should be overridden too. Other methods should be overridden if the
 * backing data structure allows for a more efficient implementation.
 * The precise implementation used by AbstractList is documented, so that
 * subclasses can tell which methods could be implemented more efficiently.
 * <p>
 * As recommended by Collection and List, the subclass should provide at
 * least a no-argument and a Collection constructor. This class is not
 * synchronized.
 * @author Original author unknown
 * @author Bryce McKinlay
 * @author Eric Blake <ebb9@email.byu.edu>
 * @see Collection
 * @see List
 * @see AbstractSequentialList
 * @see AbstractCollection
 * @see ListIterator
 * @since 1.2
 * @status updated to 1.4
 */
public abstract class AbstractList extends AbstractCollection implements List, Rollbackable {

    /**     
     * A count of the number of structural modifications that have been made to
     * the list (that is, insertions and removals). Structural modifications
     * are ones which change the list size or affect how iterations would
     * behave. This field is available for use by Iterator and ListIterator,
     * in order to throw a {
@link ConcurrentModificationException    }
 in response
     * to the next operation on the iterator. This <i>fail-fast</i> behavior
     * saves the user from many subtle bugs otherwise possible from concurrent
     * modification during iteration.
     * <p>
     * To make lists fail-fast, increment this field by just 1 in the
     * <code>add(int, Object)</code> and <code>remove(int)</code> methods.
     * Otherwise, this field may be ignored.
     */
    private int modCount;

    protected int getModCount() {
        return modCount;
    }

    protected void setModCount(int modCount) {
        this.$ASSIGN$modCount(modCount);
    }

    /**     
     * The main constructor, for use by subclasses.
     */
    protected AbstractList() {
    }

    /**     
     * Returns the elements at the specified position in the list.
     * @param index the element to return
     * @return the element at that position
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
     */
    public abstract Object get(int index);

    /**     
     * Insert an element into the list at a given position (optional operation).
     * This shifts all existing elements from that position to the end one
     * index to the right.  This version of add has no return, since it is
     * assumed to always succeed if there is no exception. This implementation
     * always throws UnsupportedOperationException, and must be overridden to
     * make a modifiable List.  If you want fail-fast iterators, be sure to
     * increment modCount when overriding this.
     * @param index the location to insert the item
     * @param o the object to insert
     * @throws UnsupportedOperationException if this list does not support the
     * add operation
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     * @throws ClassCastException if o cannot be added to this list due to its
     * type
     * @throws IllegalArgumentException if o cannot be added to this list for
     * some other reason
     * @see #modCount
     */
    public void add(int index, Object o) {
        throw new UnsupportedOperationException();
    }

    /**     
     * Add an element to the end of the list (optional operation). If the list
     * imposes restraints on what can be inserted, such as no null elements,
     * this should be documented. This implementation calls
     * <code>add(size(), o);</code>, and will fail if that version does.
     * @param o the object to add
     * @return true, as defined by Collection for a modified list
     * @throws UnsupportedOperationException if this list does not support the
     * add operation
     * @throws ClassCastException if o cannot be added to this list due to its
     * type
     * @throws IllegalArgumentException if o cannot be added to this list for
     * some other reason
     * @see #add(int, Object)
     */
    public boolean add(Object o) {
        add(size(), o);
        return true;
    }

    /**     
     * Insert the contents of a collection into the list at a given position
     * (optional operation). Shift all elements at that position to the right
     * by the number of elements inserted. This operation is undefined if
     * this list is modified during the operation (for example, if you try
     * to insert a list into itself). This implementation uses the iterator of
     * the collection, repeatedly calling add(int, Object); this will fail
     * if add does. This can often be made more efficient.
     * @param index the location to insert the collection
     * @param c the collection to insert
     * @return true if the list was modified by this action, that is, if c is
     * non-empty
     * @throws UnsupportedOperationException if this list does not support the
     * addAll operation
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     * @throws ClassCastException if some element of c cannot be added to this
     * list due to its type
     * @throws IllegalArgumentException if some element of c cannot be added
     * to this list for some other reason
     * @throws NullPointerException if the specified collection is null
     * @see #add(int, Object)
     */
    public boolean addAll(int index, Collection c) {
        Iterator itr = c.iterator();
        int size = c.size();
        for (int pos = size; pos > 0; pos--) 
            add(index++, itr.next());
        return size > 0;
    }

    /**     
     * Clear the list, such that a subsequent call to isEmpty() would return
     * true (optional operation). This implementation calls
     * <code>removeRange(0, size())</code>, so it will fail unless remove
     * or removeRange is overridden.
     * @throws UnsupportedOperationException if this list does not support the
     * clear operation
     * @see #remove(int)
     * @see #removeRange(int, int)
     */
    public void clear() {
        removeRange(0, size());
    }

    /**     
     * Test whether this list is equal to another object. A List is defined to be
     * equal to an object if and only if that object is also a List, and the two
     * lists have the same sequence. Two lists l1 and l2 are equal if and only
     * if <code>l1.size() == l2.size()</code>, and for every integer n between 0
     * and <code>l1.size() - 1</code> inclusive, <code>l1.get(n) == null ?
     * l2.get(n) == null : l1.get(n).equals(l2.get(n))</code>.
     * <p>
     * This implementation returns true if the object is this, or false if the
     * object is not a List.  Otherwise, it iterates over both lists (with
     * iterator()), returning false if two elements compare false or one list
     * is shorter, and true if the iteration completes successfully.
     * @param o the object to test for equality with this list
     * @return true if o is equal to this list
     * @see Object#equals(Object)
     * @see #hashCode()
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof List))
            return false;
        int size = size();
        if (size != ((List)o).size())
            return false;
        Iterator itr1 = iterator();
        Iterator itr2 = ((List)o).iterator();
        while (--size >= 0) 
            if (!equals(itr1.next(), itr2.next()))
                return false;
        return true;
    }

    /**     
     * Obtains a hash code for this list. In order to obey the general
     * contract of the hashCode method of class Object, this value is
     * calculated as follows:
     * <pre>hashCode = 1;
     * Iterator i = list.iterator();
     * while (i.hasNext())
     * {
     * Object obj = i.next();
     * hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
     * }</pre>
     * This ensures that the general contract of Object.hashCode() is adhered to.
     * @return the hash code of this list
     * @see Object#hashCode()
     * @see #equals(Object)
     */
    public int hashCode() {
        int hashCode = 1;
        Iterator itr = iterator();
        int pos = size();
        while (--pos >= 0) 
            hashCode = 31 * hashCode + hashCode(itr.next());
        return hashCode;
    }

    /**     
     * Obtain the first index at which a given object is to be found in this
     * list. This implementation follows a listIterator() until a match is found,
     * or returns -1 if the list end is reached.
     * @param o the object to search for
     * @return the least integer n such that <code>o == null ? get(n) == null :
     * o.equals(get(n))</code>, or -1 if there is no such index
     */
    public int indexOf(Object o) {
        ListIterator itr = listIterator();
        int size = size();
        for (int pos = 0; pos < size; pos++) 
            if (equals(o, itr.next()))
                return pos;
        return -1;
    }

    /**     
     * Obtain an Iterator over this list, whose sequence is the list order.
     * This implementation uses size(), get(int), and remove(int) of the
     * backing list, and does not support remove unless the list does. This
     * implementation is fail-fast if you correctly maintain modCount.
     * Also, this implementation is specified by Sun to be distinct from
     * listIterator, although you could easily implement it as
     * <code>return listIterator(0)</code>.
     * @return an Iterator over the elements of this list, in order
     * @see #modCount
     */
    public Iterator iterator() {
        // Bah, Sun's implementation forbids using listIterator(0).
        return new Iterator() {
            private int pos = 0;

            private int size = size();

            private int last = -1;

            private int knownMod = getModCount();

            // This will get inlined, since it is private.
            private void checkMod() {
                if (knownMod != getModCount())
                    throw new ConcurrentModificationException();
            }

            public boolean hasNext() {
                checkMod();
                return pos < size;
            }

            public Object next() {
                checkMod();
                if (pos == size)
                    throw new NoSuchElementException();
                $ASSIGN$last(pos);
                return get($ASSIGN$SPECIAL$pos(11, pos));
            }

            public void remove() {
                checkMod();
                if (last < 0)
                    throw new IllegalStateException();
                AbstractList.this.remove(last);
                $ASSIGN$SPECIAL$pos(12, pos);
                $ASSIGN$SPECIAL$size(12, size);
                $ASSIGN$last(-1);
                $ASSIGN$knownMod(getModCount());
            }

            final class _PROXY_ implements Rollbackable {

                public final void $COMMIT(long timestamp) {
                    $COMMIT_ANONYMOUS(timestamp);
                }

                public final void $RESTORE(long timestamp, boolean trim) {
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }

                public final Checkpoint $GET$CHECKPOINT() {
                    return $GET$CHECKPOINT_ANONYMOUS();
                }

                public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
                    $SET$CHECKPOINT_ANONYMOUS(checkpoint);
                    return this;
                }

            }

            private final int $ASSIGN$SPECIAL$pos(int operator, long newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$pos.add(null, pos, $CHECKPOINT.getTimestamp());
                }
                switch (operator) {
                    case 0:
                        return pos += newValue;
                    case 1:
                        return pos -= newValue;
                    case 2:
                        return pos *= newValue;
                    case 3:
                        return pos /= newValue;
                    case 4:
                        return pos &= newValue;
                    case 5:
                        return pos |= newValue;
                    case 6:
                        return pos ^= newValue;
                    case 7:
                        return pos %= newValue;
                    case 8:
                        return pos <<= newValue;
                    case 9:
                        return pos >>= newValue;
                    case 10:
                        return pos >>>= newValue;
                    case 11:
                        return pos++;
                    case 12:
                        return pos--;
                    case 13:
                        return ++pos;
                    case 14:
                        return --pos;
                    default:
                        return pos;
                }
            }

            private final int $ASSIGN$SPECIAL$size(int operator, long newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$size.add(null, size, $CHECKPOINT.getTimestamp());
                }
                switch (operator) {
                    case 0:
                        return size += newValue;
                    case 1:
                        return size -= newValue;
                    case 2:
                        return size *= newValue;
                    case 3:
                        return size /= newValue;
                    case 4:
                        return size &= newValue;
                    case 5:
                        return size |= newValue;
                    case 6:
                        return size ^= newValue;
                    case 7:
                        return size %= newValue;
                    case 8:
                        return size <<= newValue;
                    case 9:
                        return size >>= newValue;
                    case 10:
                        return size >>>= newValue;
                    case 11:
                        return size++;
                    case 12:
                        return size--;
                    case 13:
                        return ++size;
                    case 14:
                        return --size;
                    default:
                        return size;
                }
            }

            private final int $ASSIGN$last(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$last.add(null, last, $CHECKPOINT.getTimestamp());
                }
                return last = newValue;
            }

            private final int $ASSIGN$knownMod(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$knownMod.add(null, knownMod, $CHECKPOINT.getTimestamp());
                }
                return knownMod = newValue;
            }

            public void $COMMIT_ANONYMOUS(long timestamp) {
                FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                $RECORD$$CHECKPOINT.commit(timestamp);
            }

            public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                pos = $RECORD$pos.restore(pos, timestamp, trim);
                size = $RECORD$size.restore(size, timestamp, trim);
                last = $RECORD$last.restore(last, timestamp, trim);
                knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
                if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                    $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, new _PROXY_(), timestamp, trim);
                    FieldRecord.popState($RECORDS);
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }
            }

            public final Checkpoint $GET$CHECKPOINT_ANONYMOUS() {
                return $CHECKPOINT;
            }

            public final Object $SET$CHECKPOINT_ANONYMOUS(Checkpoint checkpoint) {
                if ($CHECKPOINT != checkpoint) {
                    Checkpoint oldCheckpoint = $CHECKPOINT;
                    if (checkpoint != null) {
                        $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                        FieldRecord.pushState($RECORDS);
                    }
                    $CHECKPOINT = checkpoint;
                    oldCheckpoint.setCheckpoint(checkpoint);
                    checkpoint.addObject(new _PROXY_());
                }
                return this;
            }

            private FieldRecord $RECORD$pos = new FieldRecord(0);

            private FieldRecord $RECORD$size = new FieldRecord(0);

            private FieldRecord $RECORD$last = new FieldRecord(0);

            private FieldRecord $RECORD$knownMod = new FieldRecord(0);

            private FieldRecord[] $RECORDS = new FieldRecord[] {
                    $RECORD$pos,
                    $RECORD$size,
                    $RECORD$last,
                    $RECORD$knownMod
                };

            {
                $CHECKPOINT.addObject(new _PROXY_());
            }

        };
    }

    /**     
     * Obtain the last index at which a given object is to be found in this
     * list. This implementation grabs listIterator(size()), then searches
     * backwards for a match or returns -1.
     * @return the greatest integer n such that <code>o == null ? get(n) == null
     * : o.equals(get(n))</code>, or -1 if there is no such index
     */
    public int lastIndexOf(Object o) {
        int pos = size();
        ListIterator itr = listIterator(pos);
        while (--pos >= 0) 
            if (equals(o, itr.previous()))
                return pos;
        return -1;
    }

    /**     
     * Obtain a ListIterator over this list, starting at the beginning. This
     * implementation returns listIterator(0).
     * @return a ListIterator over the elements of this list, in order, starting
     * at the beginning
     */
    public ListIterator listIterator() {
        return listIterator(0);
    }

    /**     
     * Obtain a ListIterator over this list, starting at a given position.
     * A first call to next() would return the same as get(index), and a
     * first call to previous() would return the same as get(index - 1).
     * <p>
     * This implementation uses size(), get(int), set(int, Object),
     * add(int, Object), and remove(int) of the backing list, and does not
     * support remove, set, or add unless the list does. This implementation
     * is fail-fast if you correctly maintain modCount.
     * @param index the position, between 0 and size() inclusive, to begin the
     * iteration from
     * @return a ListIterator over the elements of this list, in order, starting
     * at index
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     * @see #modCount
     */
    public ListIterator listIterator(final int index) {
        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size());
        return new ListIterator() {
            private int knownMod = getModCount();

            private int position = index;

            private int lastReturned = -1;

            private int size = size();

            // This will get inlined, since it is private.
            private void checkMod() {
                if (knownMod != getModCount())
                    throw new ConcurrentModificationException();
            }

            public boolean hasNext() {
                checkMod();
                return position < size;
            }

            public boolean hasPrevious() {
                checkMod();
                return position > 0;
            }

            public Object next() {
                checkMod();
                if (position == size)
                    throw new NoSuchElementException();
                $ASSIGN$lastReturned(position);
                return get($ASSIGN$SPECIAL$position(11, position));
            }

            public Object previous() {
                checkMod();
                if (position == 0)
                    throw new NoSuchElementException();
                $ASSIGN$lastReturned($ASSIGN$SPECIAL$position(14, position));
                return get(lastReturned);
            }

            public int nextIndex() {
                checkMod();
                return position;
            }

            public int previousIndex() {
                checkMod();
                return position - 1;
            }

            public void remove() {
                checkMod();
                if (lastReturned < 0)
                    throw new IllegalStateException();
                AbstractList.this.remove(lastReturned);
                $ASSIGN$SPECIAL$size(12, size);
                $ASSIGN$position(lastReturned);
                $ASSIGN$lastReturned(-1);
                $ASSIGN$knownMod(getModCount());
            }

            public void set(Object o) {
                checkMod();
                if (lastReturned < 0)
                    throw new IllegalStateException();
                AbstractList.this.set(lastReturned, o);
            }

            public void add(Object o) {
                checkMod();
                AbstractList.this.add($ASSIGN$SPECIAL$position(11, position), o);
                $ASSIGN$SPECIAL$size(11, size);
                $ASSIGN$lastReturned(-1);
                $ASSIGN$knownMod(getModCount());
            }

            final class _PROXY_ implements Rollbackable {

                public final void $COMMIT(long timestamp) {
                    $COMMIT_ANONYMOUS(timestamp);
                }

                public final void $RESTORE(long timestamp, boolean trim) {
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }

                public final Checkpoint $GET$CHECKPOINT() {
                    return $GET$CHECKPOINT_ANONYMOUS();
                }

                public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
                    $SET$CHECKPOINT_ANONYMOUS(checkpoint);
                    return this;
                }

            }

            private final int $ASSIGN$knownMod(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$knownMod.add(null, knownMod, $CHECKPOINT.getTimestamp());
                }
                return knownMod = newValue;
            }

            private final int $ASSIGN$position(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$position.add(null, position, $CHECKPOINT.getTimestamp());
                }
                return position = newValue;
            }

            private final int $ASSIGN$SPECIAL$position(int operator, long newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$position.add(null, position, $CHECKPOINT.getTimestamp());
                }
                switch (operator) {
                    case 0:
                        return position += newValue;
                    case 1:
                        return position -= newValue;
                    case 2:
                        return position *= newValue;
                    case 3:
                        return position /= newValue;
                    case 4:
                        return position &= newValue;
                    case 5:
                        return position |= newValue;
                    case 6:
                        return position ^= newValue;
                    case 7:
                        return position %= newValue;
                    case 8:
                        return position <<= newValue;
                    case 9:
                        return position >>= newValue;
                    case 10:
                        return position >>>= newValue;
                    case 11:
                        return position++;
                    case 12:
                        return position--;
                    case 13:
                        return ++position;
                    case 14:
                        return --position;
                    default:
                        return position;
                }
            }

            private final int $ASSIGN$lastReturned(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$lastReturned.add(null, lastReturned, $CHECKPOINT.getTimestamp());
                }
                return lastReturned = newValue;
            }

            private final int $ASSIGN$SPECIAL$size(int operator, long newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$size.add(null, size, $CHECKPOINT.getTimestamp());
                }
                switch (operator) {
                    case 0:
                        return size += newValue;
                    case 1:
                        return size -= newValue;
                    case 2:
                        return size *= newValue;
                    case 3:
                        return size /= newValue;
                    case 4:
                        return size &= newValue;
                    case 5:
                        return size |= newValue;
                    case 6:
                        return size ^= newValue;
                    case 7:
                        return size %= newValue;
                    case 8:
                        return size <<= newValue;
                    case 9:
                        return size >>= newValue;
                    case 10:
                        return size >>>= newValue;
                    case 11:
                        return size++;
                    case 12:
                        return size--;
                    case 13:
                        return ++size;
                    case 14:
                        return --size;
                    default:
                        return size;
                }
            }

            public void $COMMIT_ANONYMOUS(long timestamp) {
                FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                $RECORD$$CHECKPOINT.commit(timestamp);
            }

            public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
                position = $RECORD$position.restore(position, timestamp, trim);
                lastReturned = $RECORD$lastReturned.restore(lastReturned, timestamp, trim);
                size = $RECORD$size.restore(size, timestamp, trim);
                if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                    $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, new _PROXY_(), timestamp, trim);
                    FieldRecord.popState($RECORDS);
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }
            }

            public final Checkpoint $GET$CHECKPOINT_ANONYMOUS() {
                return $CHECKPOINT;
            }

            public final Object $SET$CHECKPOINT_ANONYMOUS(Checkpoint checkpoint) {
                if ($CHECKPOINT != checkpoint) {
                    Checkpoint oldCheckpoint = $CHECKPOINT;
                    if (checkpoint != null) {
                        $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                        FieldRecord.pushState($RECORDS);
                    }
                    $CHECKPOINT = checkpoint;
                    oldCheckpoint.setCheckpoint(checkpoint);
                    checkpoint.addObject(new _PROXY_());
                }
                return this;
            }

            private FieldRecord $RECORD$knownMod = new FieldRecord(0);

            private FieldRecord $RECORD$position = new FieldRecord(0);

            private FieldRecord $RECORD$lastReturned = new FieldRecord(0);

            private FieldRecord $RECORD$size = new FieldRecord(0);

            private FieldRecord[] $RECORDS = new FieldRecord[] {
                    $RECORD$knownMod,
                    $RECORD$position,
                    $RECORD$lastReturned,
                    $RECORD$size
                };

            {
                $CHECKPOINT.addObject(new _PROXY_());
            }

        };
    }

    /**     
     * Remove the element at a given position in this list (optional operation).
     * Shifts all remaining elements to the left to fill the gap. This
     * implementation always throws an UnsupportedOperationException.
     * If you want fail-fast iterators, be sure to increment modCount when
     * overriding this.
     * @param index the position within the list of the object to remove
     * @return the object that was removed
     * @throws UnsupportedOperationException if this list does not support the
     * remove operation
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
     * @see #modCount
     */
    public Object remove(int index) {
        throw new UnsupportedOperationException();
    }

    /**     
     * Remove a subsection of the list. This is called by the clear and
     * removeRange methods of the class which implements subList, which are
     * difficult for subclasses to override directly. Therefore, this method
     * should be overridden instead by the more efficient implementation, if one
     * exists. Overriding this can reduce quadratic efforts to constant time
     * in some cases!
     * <p>
     * This implementation first checks for illegal or out of range arguments. It
     * then obtains a ListIterator over the list using listIterator(fromIndex).
     * It then calls next() and remove() on this iterator repeatedly, toIndex -
     * fromIndex times.
     * @param fromIndex the index, inclusive, to remove from.
     * @param toIndex the index, exclusive, to remove to.
     */
    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator itr = listIterator(fromIndex);
        for (int index = fromIndex; index < toIndex; index++) {
            itr.next();
            itr.remove();
        }
    }

    /**     
     * Replace an element of this list with another object (optional operation).
     * This implementation always throws an UnsupportedOperationException.
     * @param index the position within this list of the element to be replaced
     * @param o the object to replace it with
     * @return the object that was replaced
     * @throws UnsupportedOperationException if this list does not support the
     * set operation
     * @throws IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
     * @throws ClassCastException if o cannot be added to this list due to its
     * type
     * @throws IllegalArgumentException if o cannot be added to this list for
     * some other reason
     */
    public Object set(int index, Object o) {
        throw new UnsupportedOperationException();
    }

    /**     
     * Obtain a List view of a subsection of this list, from fromIndex
     * (inclusive) to toIndex (exclusive). If the two indices are equal, the
     * sublist is empty. The returned list should be modifiable if and only
     * if this list is modifiable. Changes to the returned list should be
     * reflected in this list. If this list is structurally modified in
     * any way other than through the returned list, the result of any subsequent
     * operations on the returned list is undefined.
     * <p>
     * This implementation returns a subclass of AbstractList. It stores, in
     * private fields, the offset and size of the sublist, and the expected
     * modCount of the backing list. If the backing list implements RandomAccess,
     * the sublist will also.
     * <p>
     * The subclass's <code>set(int, Object)</code>, <code>get(int)</code>,
     * <code>add(int, Object)</code>, <code>remove(int)</code>,
     * <code>addAll(int, Collection)</code> and
     * <code>removeRange(int, int)</code> methods all delegate to the
     * corresponding methods on the backing abstract list, after
     * bounds-checking the index and adjusting for the offset. The
     * <code>addAll(Collection c)</code> method merely returns addAll(size, c).
     * The <code>listIterator(int)</code> method returns a "wrapper object"
     * over a list iterator on the backing list, which is created with the
     * corresponding method on the backing list. The <code>iterator()</code>
     * method merely returns listIterator(), and the <code>size()</code> method
     * merely returns the subclass's size field.
     * <p>
     * All methods first check to see if the actual modCount of the backing
     * list is equal to its expected value, and throw a
     * ConcurrentModificationException if it is not. 
     * @param fromIndex the index that the returned list should start from
     * (inclusive)
     * @param toIndex the index that the returned list should go to (exclusive)
     * @return a List backed by a subsection of this list
     * @throws IndexOutOfBoundsException if fromIndex &lt; 0
     * || toIndex &gt; size()
     * @throws IllegalArgumentException if fromIndex &gt; toIndex
     * @see ConcurrentModificationException
     * @see RandomAccess
     */
    public List subList(int fromIndex, int toIndex) {
        // This follows the specification of AbstractList, but is inconsistent
        // with the one in List. Don't you love Sun's inconsistencies?
        if (fromIndex > toIndex)
            throw new IllegalArgumentException(fromIndex + " > "+toIndex);
        if (fromIndex < 0 || toIndex > size())
            throw new IndexOutOfBoundsException();
        if (this instanceof RandomAccess)
            return new RandomAccessSubList(this, fromIndex, toIndex);
        return new SubList(this, fromIndex, toIndex);
    }

    private final int $ASSIGN$modCount(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$modCount.add(null, modCount, $CHECKPOINT.getTimestamp());
        }
        return modCount = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        modCount = $RECORD$modCount.restore(modCount, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$modCount = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$modCount
        };

}

// class AbstractList
/** 
 * This class follows the implementation requirements set forth in{
@link AbstractList#subList(int, int)}
. It matches Sun's implementation
 * by using a non-public top-level class in the same package.
 * @author Original author unknown
 * @author Eric Blake <ebb9@email.byu.edu>
 */
class SubList extends AbstractList implements Rollbackable {

    // Package visible, for use by iterator.
    /**     
     * The original list. 
     */
    private final AbstractList backingList;

    /**     
     * The index of the first element of the sublist. 
     */
    final int offset;

    /**     
     * The size of the sublist. 
     */
    private int size;

    AbstractList getBackingList() {
        return backingList;
    }

    int getSize() {
        return size;
    }

    void setSize(int size) {
        this.$ASSIGN$size(size);
    }

    /**     
     * Construct the sublist.
     * @param backing the list this comes from
     * @param fromIndex the lower bound, inclusive
     * @param toIndex the upper bound, exclusive
     */
    SubList(AbstractList backing, int fromIndex, int toIndex) {
        backingList = backing;
        setModCount(backing.getModCount());
        offset = fromIndex;
        $ASSIGN$size(toIndex - fromIndex);
    }

    /**     
     * This method checks the two modCount fields to ensure that there has
     * not been a concurrent modification, returning if all is okay.
     * @throws ConcurrentModificationException if the backing list has been
     * modified externally to this sublist
     */
    // This can be inlined. Package visible, for use by iterator.
    void checkMod() {
        if (getModCount() != backingList.getModCount())
            throw new ConcurrentModificationException();
    }

    /**     
     * This method checks that a value is between 0 and size (inclusive). If
     * it is not, an exception is thrown.
     * @param index the value to check
     * @throws IndexOutOfBoundsException if the value is out of range
     */
    // This will get inlined, since it is private.
    private void checkBoundsInclusive(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size);
    }

    /**     
     * This method checks that a value is between 0 (inclusive) and size
     * (exclusive). If it is not, an exception is thrown.
     * @param index the value to check
     * @throws IndexOutOfBoundsException if the value is out of range
     */
    // This will get inlined, since it is private.
    private void checkBoundsExclusive(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size);
    }

    /**     
     * Specified by AbstractList.subList to return the private field size.
     * @return the sublist size
     */
    public int size() {
        checkMod();
        return size;
    }

    /**     
     * Specified by AbstractList.subList to delegate to the backing list.
     * @param index the location to modify
     * @param o the new value
     * @return the old value
     */
    public Object set(int index, Object o) {
        checkMod();
        checkBoundsExclusive(index);
        return backingList.set(index + offset, o);
    }

    /**     
     * Specified by AbstractList.subList to delegate to the backing list.
     * @param index the location to get from
     * @return the object at that location
     */
    public Object get(int index) {
        checkMod();
        checkBoundsExclusive(index);
        return backingList.get(index + offset);
    }

    /**     
     * Specified by AbstractList.subList to delegate to the backing list.
     * @param index the index to insert at
     * @param o the object to add
     */
    public void add(int index, Object o) {
        checkMod();
        checkBoundsInclusive(index);
        backingList.add(index + offset, o);
        $ASSIGN$SPECIAL$size(11, size);
        setModCount(backingList.getModCount());
    }

    /**     
     * Specified by AbstractList.subList to delegate to the backing list.
     * @param index the index to remove
     * @return the removed object
     */
    public Object remove(int index) {
        checkMod();
        checkBoundsExclusive(index);
        Object o = backingList.remove(index + offset);
        $ASSIGN$SPECIAL$size(12, size);
        setModCount(backingList.getModCount());
        return o;
    }

    /**     
     * Specified by AbstractList.subList to delegate to the backing list.
     * This does no bounds checking, as it assumes it will only be called
     * by trusted code like clear() which has already checked the bounds.
     * @param fromIndex the lower bound, inclusive
     * @param toIndex the upper bound, exclusive
     */
    protected void removeRange(int fromIndex, int toIndex) {
        checkMod();
        backingList.removeRange(offset + fromIndex, offset + toIndex);
        $ASSIGN$SPECIAL$size(1, toIndex - fromIndex);
        setModCount(backingList.getModCount());
    }

    /**     
     * Specified by AbstractList.subList to delegate to the backing list.
     * @param index the location to insert at
     * @param c the collection to insert
     * @return true if this list was modified, in other words, c is non-empty
     */
    public boolean addAll(int index, Collection c) {
        checkMod();
        checkBoundsInclusive(index);
        int csize = c.size();
        boolean result = backingList.addAll(offset + index, c);
        $ASSIGN$SPECIAL$size(0, csize);
        setModCount(backingList.getModCount());
        return result;
    }

    /**     
     * Specified by AbstractList.subList to return addAll(size, c).
     * @param c the collection to insert
     * @return true if this list was modified, in other words, c is non-empty
     */
    public boolean addAll(Collection c) {
        return addAll(size, c);
    }

    /**     
     * Specified by AbstractList.subList to return listIterator().
     * @return an iterator over the sublist
     */
    public Iterator iterator() {
        return listIterator();
    }

    /**     
     * Specified by AbstractList.subList to return a wrapper around the
     * backing list's iterator.
     * @param index the start location of the iterator
     * @return a list iterator over the sublist
     */
    public ListIterator listIterator(final int index) {
        checkMod();
        checkBoundsInclusive(index);
        return new ListIterator() {
            private final ListIterator i = backingList.listIterator(index + offset);

            private int position = index;

            public boolean hasNext() {
                checkMod();
                return position < size;
            }

            public boolean hasPrevious() {
                checkMod();
                return position > 0;
            }

            public Object next() {
                if (position == size)
                    throw new NoSuchElementException();
                $ASSIGN$SPECIAL$position(11, position);
                return i.next();
            }

            public Object previous() {
                if (position == 0)
                    throw new NoSuchElementException();
                $ASSIGN$SPECIAL$position(12, position);
                return i.previous();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            public int previousIndex() {
                return i.previousIndex() - offset;
            }

            public void remove() {
                i.remove();
                $ASSIGN$SPECIAL$size(12, size);
                $ASSIGN$position(nextIndex());
                setModCount(backingList.getModCount());
            }

            public void set(Object o) {
                i.set(o);
            }

            public void add(Object o) {
                i.add(o);
                $ASSIGN$SPECIAL$size(11, size);
                $ASSIGN$SPECIAL$position(11, position);
                setModCount(backingList.getModCount());
            }
            // Here is the reason why the various modCount fields are mostly
            // ignored in this wrapper listIterator.
            // If the backing listIterator is failfast, then the following holds:
            //   Using any other method on this list will call a corresponding
            //   method on the backing list *after* the backing listIterator
            //   is created, which will in turn cause a ConcurrentModException
            //   when this listIterator comes to use the backing one. So it is
            //   implicitly failfast.
            // If the backing listIterator is NOT failfast, then the whole of
            //   this list isn't failfast, because the modCount field of the
            //   backing list is not valid. It would still be *possible* to
            //   make the iterator failfast wrt modifications of the sublist
            //   only, but somewhat pointless when the list can be changed under
            //   us.
            // Either way, no explicit handling of modCount is needed.
            // However modCount = backingList.modCount must be executed in add
            // and remove, and size must also be updated in these two methods,
            // since they do not go through the corresponding methods of the subList.

            final class _PROXY_ implements Rollbackable {

                public final void $COMMIT(long timestamp) {
                    $COMMIT_ANONYMOUS(timestamp);
                }

                public final void $RESTORE(long timestamp, boolean trim) {
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }

                public final Checkpoint $GET$CHECKPOINT() {
                    return $GET$CHECKPOINT_ANONYMOUS();
                }

                public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
                    $SET$CHECKPOINT_ANONYMOUS(checkpoint);
                    return this;
                }

            }

            private final int $ASSIGN$position(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$position.add(null, position, $CHECKPOINT.getTimestamp());
                }
                return position = newValue;
            }

            private final int $ASSIGN$SPECIAL$position(int operator, long newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$position.add(null, position, $CHECKPOINT.getTimestamp());
                }
                switch (operator) {
                    case 0:
                        return position += newValue;
                    case 1:
                        return position -= newValue;
                    case 2:
                        return position *= newValue;
                    case 3:
                        return position /= newValue;
                    case 4:
                        return position &= newValue;
                    case 5:
                        return position |= newValue;
                    case 6:
                        return position ^= newValue;
                    case 7:
                        return position %= newValue;
                    case 8:
                        return position <<= newValue;
                    case 9:
                        return position >>= newValue;
                    case 10:
                        return position >>>= newValue;
                    case 11:
                        return position++;
                    case 12:
                        return position--;
                    case 13:
                        return ++position;
                    case 14:
                        return --position;
                    default:
                        return position;
                }
            }

            public void $COMMIT_ANONYMOUS(long timestamp) {
                FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                $RECORD$$CHECKPOINT.commit(timestamp);
            }

            public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                $RECORD$i.restore(i, timestamp, trim);
                position = $RECORD$position.restore(position, timestamp, trim);
                if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                    $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, new _PROXY_(), timestamp, trim);
                    FieldRecord.popState($RECORDS);
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }
            }

            public final Checkpoint $GET$CHECKPOINT_ANONYMOUS() {
                return $CHECKPOINT;
            }

            public final Object $SET$CHECKPOINT_ANONYMOUS(Checkpoint checkpoint) {
                if ($CHECKPOINT != checkpoint) {
                    Checkpoint oldCheckpoint = $CHECKPOINT;
                    if (checkpoint != null) {
                        $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                        FieldRecord.pushState($RECORDS);
                    }
                    $CHECKPOINT = checkpoint;
                    oldCheckpoint.setCheckpoint(checkpoint);
                    checkpoint.addObject(new _PROXY_());
                }
                return this;
            }

            private FieldRecord $RECORD$i = new FieldRecord(0);

            private FieldRecord $RECORD$position = new FieldRecord(0);

            private FieldRecord[] $RECORDS = new FieldRecord[] {
                    $RECORD$i,
                    $RECORD$position
                };

            {
                $CHECKPOINT.addObject(new _PROXY_());
            }

        };
    }

    private final int $ASSIGN$size(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$size.add(null, size, $CHECKPOINT.getTimestamp());
        }
        return size = newValue;
    }

    private final int $ASSIGN$SPECIAL$size(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$size.add(null, size, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return size += newValue;
            case 1:
                return size -= newValue;
            case 2:
                return size *= newValue;
            case 3:
                return size /= newValue;
            case 4:
                return size &= newValue;
            case 5:
                return size |= newValue;
            case 6:
                return size ^= newValue;
            case 7:
                return size %= newValue;
            case 8:
                return size <<= newValue;
            case 9:
                return size >>= newValue;
            case 10:
                return size >>>= newValue;
            case 11:
                return size++;
            case 12:
                return size--;
            case 13:
                return ++size;
            case 14:
                return --size;
            default:
                return size;
        }
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        $RECORD$backingList.restore(backingList, timestamp, trim);
        size = $RECORD$size.restore(size, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$backingList = new FieldRecord(0);

    private FieldRecord $RECORD$size = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$backingList,
            $RECORD$size
        };

}

// class SubList
/** 
 * This class is a RandomAccess version of SubList, as required by{
@link AbstractList#subList(int, int)}
.
 * @author Eric Blake <ebb9@email.byu.edu>
 */
final class RandomAccessSubList extends SubList implements RandomAccess, Rollbackable {

    /**     
     * Construct the sublist.
     * @param backing the list this comes from
     * @param fromIndex the lower bound, inclusive
     * @param toIndex the upper bound, exclusive
     */
    RandomAccessSubList(AbstractList backing, int fromIndex, int toIndex) {
        super(backing, fromIndex, toIndex);
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord[] $RECORDS = new FieldRecord[] {
        };

}

// class RandomAccessSubList
