/* A list of unique objects with an efficient mapping from the objects
into consecutive integer labels.

 Copyright (c) 2001 The Regents of the University of Maryland.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

*/

package ptolemy.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// LabeledList
/** A list of unique objects (elements) with a mapping from the elements
into consecutive integer labels. The labels are consecutive
integers between 0 and <em>N</em>-1 inclusive, where <em>N</em> is
the total number of elements in the list. This list features <em>O</em>(1)
list insertion, <em>O</em>(1) testing for membership in the list,
<em>O</em>(1) access of a list element from its associated label,
and <em>O</em>(1) access of a label from its corresponding element.
The element labels are useful, for example, in creating mappings from
list elements into elements of arbitrary arrays.
More generally, element labels can be used to maintain arbitrary
<em>m</em>-dimensional matrices that are indexed by the list elements
(via the associated element labels).

<p> Element labels maintain their consistency (remain constant) during periods
when no elements are removed from the list, and the list is not modified
through a sub list (i.e., by using a list obtained through {@link #subList(int,
int)}. When elements are removed, the labels assigned to the remaining elements
may change (see {@link #remove(Object)} for details.  When a sub list is used
to modify the list, the effect on the labels of the list is unpredictable, and
to be safe, it should be assumed that all labels have changed.

<p> Elements themselves must be non-null and distinct, as determined by the
<code>equals</code> method.

@author Shuvra S. Bhattacharyya
@version $Id$
*/

public class LabeledList implements List {

    /** Construct an empty list.
     */
    public LabeledList() {
        _elements = new ArrayList(0);
        _labels = new HashMap(0);
    }

    /** Construct an empty list with enough storage allocated to hold
     *  the specified number of elements.  Memory management is more
     *  efficient with this constructor (assuming the number of elements is
     *  known).
     */
    public LabeledList(int size) {
        _elements = new ArrayList(size);
        _labels = new HashMap(size);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an element to the list. The label assigned to this element
     *  will be equal to the number of elements in the list
     *  prior to insertion of the element.
     *  @param element The element to insert.
     *  @return True unconditionally (assuming an exception does not occur).
     *  @exception IllegalArgumentException If the specified element is null,
     *  or if it already exists in the list.
     */
    public boolean add(Object element) {
        if (element == null) {
            throw new IllegalArgumentException("Attempt to insert a null "
                    + "element");
        } else if (_labels.containsKey(element)) {
            throw new IllegalArgumentException("Attempt to insert a duplicate "
                    + "element." + _elementDump(element));
        } else {
            _labels.put(element, new Integer(_elements.size()));
            _elements.add(element);
            return true;
        }
    }

    /*  Unsupported optional method of the list interface.
     *  @exception UnsupportedOperationException Always thrown.
     */
    public void add(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    /*  Unsupported optional method of the list interface.
     *  @exception UnsupportedOperationException Always thrown.
     */
    public boolean addAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    /*  Unsupported optional method of the list interface.
     *  @exception UnsupportedOperationException Always thrown.
     */
    public boolean addAll(int index, Collection collection) {
        throw new UnsupportedOperationException();
    }

    /** Clear all of the elements in this list.
     */
    public void clear() {
        // Invoke the remove(int) and size() methods, which get overridden in
        // sub lists.
        int elements = size();
        for (int i = 0; i < elements; i++) {
            remove(0);
        }
    }

    /* Return true if the specified object is an element of this list.
     * @param object The specified object.
     * @return True if the specified object is an element of this list;
     * return false if the object is null or is not in the list.
     */
    public boolean contains(Object object) {
        if (object == null) {
            return false;
        } else {
            return _labels.containsKey(object);
        }
    }

    /** Returns true if this list contains all of the elements of the
     *  specified collection
     * @param collection The specified collection.
     * @return True if this list contains all of the elements of the
     * specified collection.
     */
    public boolean containsAll(Collection collection) {
        Iterator elements = collection.iterator();
        while (elements.hasNext()) {
            if (!contains(elements.next())) return false;
        }
        return true;
    }

    /** Compares the specified object with this list for equality.
     *  @param object The object.
     *  @return True if the specified object is equal to this list.
     */
    public boolean equals(Object object) {
        return _elements.equals(object);
    }

    /** Return the element that has a specified label.
     *  @param label The label.
     *  @return The element.
     *  @exception IndexOutOfBoundsException If there is no element that
     *  has the specified label.
     */
    public Object get(int label) {
        if ((label < 0) || (label >= _elements.size()))  {
            throw new IndexOutOfBoundsException("Invalid label: " + label);
        }
        return _elements.get(label);
    }

    /** Return the hash code value for this list.
     *  @return The hash code value.
     */
    public int hashCode() {
        return _elements.hashCode();
    }

    /** Return the label in this list of the specified
     *  element; return -1 if the element is null or this list does not
     *  contain the element.
     *  @param element The element.
     *  @return The label of the element.
     *  @see #label(Object).
     */
    public int indexOf(Object element) {
        if (element == null) {
            return -1;
        } else {
            Integer label = (Integer)(_labels.get(element));
            if (label == null) {
                return -1;
            } else {
                return label.intValue();
            }
       }
    }

    /** Returns true if this list contains no elements.
     *  @return True if this list contains no elements.
     */
    public boolean isEmpty() {
        return size() == 0;
    }


    /** Return an iterator over the elements in the list. The iterator
     *  returned is safe in that it cannot be used to modify the list.
     *  @return An iterator over the elements in the list;
     */
    public Iterator iterator() {
        return Collections.unmodifiableList(_elements).iterator();
    }

    /** Return the label of the specified element.
     *  @return The corresponding label.
     *  @exception IllegalArgumentException If the specified element is not
     *  not in this list.
     *  @exception NullPointerException If the specified element is null.
     *  @see #indexOf(Object).
     */
    public final int label(Object element) {
        if (element == null) {
            throw new NullPointerException("Null element specified.");
        } else {
            Integer label = (Integer)(_labels.get(element));
            if (label == null) {
                throw new IllegalArgumentException("The specified object is not"
                        + " an element of this list. " + _elementDump(element));
            } else {
                return label.intValue();
            }
        }
    }

    /** Returns the index in this list of the last occurrence of the specified
     *  element, or -1 if this list does not contain this element.
     *  Since elements in a labeled list are distinct, this is the same
     *  as {@link #indexOf(Object)}, and is maintained only for conformance
     *  with the list interface.
     */
    public int lastIndexOf(Object element) {
        return _elements.indexOf(element);
    }

    /** Return a list iterator over the elements in the list. The iterator
     *  returned is safe in that it cannot be used to modify the list.
     *  @return A list iterator over the elements in the list;
     */
    public ListIterator listIterator() {
        return Collections.unmodifiableList(_elements).listIterator();
    }

    /** Return a list iterator over the elements in the list, starting
     *  at a specified position in the list. The iterator
     *  returned is safe in that it cannot be used to modify the list.
     *  @param index The specified starting position.
     *  @return A list iterator over the elements in the list;
     */
    public ListIterator listIterator(int index) {
        return Collections.unmodifiableList(_elements).listIterator(index);
    }

    /** Remove an element from the list.
     * Elements that have higher-valued
     * labels than this element will have their labels reduced in value
     * by one. All other element labels will remain unchanged.
     * If the specified element is not in the list, leave the list
     * unchanged.
     * @param element The element.
     * @return True If this list contained the element.
     */
    public boolean remove(Object element) {
        int label;
        try {
            label = label(element);
        } catch (IllegalArgumentException exception) {
            throw  new IllegalArgumentException("Attempt to remove a "
                    + "non-existent element. " + _elementDump(element));
        }
        _labels.remove(element);
        _elements.remove(label);
        _labelElements(label);
        return true;
    }

    /** Remove and return an element with a specified label from the list.
     * Elements that have higher-valued
     * labels than this element will have their labels reduced in value
     * by one. All other element labels will remain unchanged.
     * @param element The specified label.
     * @return The element that is removed.
     * @exception IndexOutOfBoundsException If there is no element with
     * the specified label.
     */
    public Object remove(int label) {
        Object element = get(label);
        _labels.remove(element);
        Object removed = _elements.remove(label);
        _labelElements(label);
        return removed;
    }

    /**  Unsupported optional method of the list interface.
     *  @exception UnsupportedOperationException Always thrown.
     */
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**  Unsupported optional method of the list interface.
     *  @exception UnsupportedOperationException Always thrown.
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**  Unsupported optional method of the list interface.
     *  @exception UnsupportedOperationException Always thrown.
     */
    public Object set(int index, Object element)  {
        throw new UnsupportedOperationException();
    }

    /** Return the number of elements in this list.
     *  @return The number of elements.
     */
    public int size() {
        return _elements.size();
    }

    /** Return a view of the portion of this list between fromIndex, inclusive,
     *  and toIndex, exclusive.
     *  @param fromIndex The low endpoint (inclusive) of the subList.
     *  @param toIndex The high endpoint (exclusive) of the subList.
     *  @return A view of the specified range within this list.
     *  @exception IndexOutOfBoundsException If an endpoint index value is out
     *  of range (fromIndex < 0 || toIndex > size).
     *  @exception IllegalArgumentException If endpoint indices are out of order
     *  (fromIndex > toIndex).
     */
    public List subList(int fromIndex, int toIndex) {
        return new SubList(this, fromIndex, toIndex);
    }

    /** Returns an array containing all of the elements in this list in
     *  proper sequence.
     *  @return An array containing all of the elements in this list.
     */
    public Object[] toArray() {
        return _elements.toArray();
    }

    /** Returns an array containing all of the elements in this list in
     *  proper sequence; the runtime type of the returned array is that of
     *  the specified array.
     *  @param array The specified array.
     *  @return An array containing all of the elements in this list.
     */
    public Object[] toArray(Object[] array) {
        return _elements.toArray(array);
    }

    /** Return a string representation of this list. The string
     *  representation is constructed by the concatenating
     *  the string representations of the individual elements,
     *  according to the order of their labels. The element strings
     *  are separated by the specified delimiter, and are optionally
     *  preceded by the associated labels.
     *  @param delimeter The delimeter that separates elements in the
     *  generated string.
     *  @param includeLabels If this is <code>/true<code>, then precede each
     *  element with its label (followed by a colon and space) in the
     *  generated string; otherwise, omit the labels.
     *  @return A string representation of this list.
     */
    public String toString(String delimeter, boolean includeLabels) {
        Iterator elements = iterator();
        StringBuffer result = new StringBuffer();
        while (elements.hasNext()) {
            Object element = elements.next();
            result.append(((includeLabels) ? label(element) + ": " : "") +
                    element + (elements.hasNext() ? delimeter : ""));
        }
        return result.toString();
    }

    /** Return a string representation of this list. The string
     *  representation is constructed by the concatenating
     *  the string representations of the individual elements,
     *  according to the order of their labels. The element strings
     *  are separated by newlines. The element labels are not included
     *  in the string representation.
     *  @return A string representation of this list.
     */
    public String toString() {
        return toString("\n", false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                     ////

    // This inner class is adapted from the SubList class in
    // java.util.AbstractList.java (jdk1.3.1).
    private class SubList extends LabeledList {
        private LabeledList list;
        private int offset;
        private int size;

        SubList(LabeledList parent, int fromIndex, int toIndex) {
            if (fromIndex < 0) {
                throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
            } else if (toIndex > parent.size()) {
                throw new IndexOutOfBoundsException("toIndex = " + toIndex);
            } else if (fromIndex > toIndex) {
                throw new IllegalArgumentException("fromIndex(" + fromIndex +
                        ") > toIndex(" + toIndex + ")");
            } else {
                list = parent;
                offset = fromIndex;
                size = toIndex - fromIndex;
            }
        }

        public boolean add(Object element) {
            add(size, element);
            return true;
        }

        public Object get(int index) {
            _rangeCheck(index);
            return list.get(index+offset);
        }

        public int size() {
            return size;
        }

        public void add(int index, Object element) {
            if (index < 0 || index > size)
                throw new IndexOutOfBoundsException();
            list._add(index+offset, element);
            size++;
        }

        public Object remove(int index) {
            _rangeCheck(index);
            Object result = list.remove(index+offset);
            size--;
            return result;
        }

        public Iterator iterator() {
            return listIterator(0);
        }

        public ListIterator listIterator(final int index) {
            if (index < 0 || index > size)
                throw new IndexOutOfBoundsException(
                    "Index: "+index+", Size: "+size);

            return new ListIterator() {
                private ListIterator elements = list.listIterator(index+offset);

                public boolean hasNext() {
                    return nextIndex() < size;
                }

                public Object next() {
                    if (hasNext()) {
                        return elements.next();
                    }
                    else {
                        throw new NoSuchElementException();
                    }
                }

                public boolean hasPrevious() {
                    return previousIndex() >= 0;
                }

                public Object previous() {
                        if (hasPrevious()) {
                            return elements.previous();
                        }
                        else {
                            throw new NoSuchElementException();
                        }
                }

                public int nextIndex() {
                    return elements.nextIndex() - offset;
                }

                public int previousIndex() {
                    return elements.previousIndex() - offset;
                }

                public void remove() {
                    elements.remove();
                    size--;
                }

                public void set(Object o) {
                    elements.set(o);
                }

                public void add(Object o) {
                    elements.add(o);
                    size++;
                }
            };
        }

        public List subList(int fromIndex, int toIndex) {
            return new SubList(this, fromIndex, toIndex);
        }

        private void _rangeCheck(int index) {
            if (index < 0 || index >= size)
                throw new IndexOutOfBoundsException("Index: "+index+
                                                ",Size: "+size);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // This method is provide to provide support for sub lists, as specified
    // by the list interface. It adds an element at a given index, and
    // re-labels the list elements accordingly.
    private void _add(int index, Object element) {
        _elements.add(index, element);
        _labelElements(index);
    }

    // Return a dump of a list element that is suitable for inclusion
    // in an error message.
    private String _elementDump(Object element) {
        return "The offending element follows:\n" +
                ((element == null) ? "null" : element) + "\n";
    }

    // Fill in the labels map with the appropriate indices of
    // the array list, starting at a specified index.
    private void _labelElements(int startIndex) {
        for (int i = startIndex; i < _elements.size(); i++) {
            _labels.put(_elements.get(i), new Integer(i));
        }
    }

    private SubList l;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The elements that are associated with this list.
    private ArrayList _elements;

    // Translation from list element to label. The keys of this HashMap
    // are list elements (instances of Object), and the values are
    // the corresponding element labels (instances of Integer).
    // This translation can also be
    // done with indexOf(), but a HashMap is faster.
    private HashMap _labels;
}
