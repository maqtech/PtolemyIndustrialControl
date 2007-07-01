package ptolemy.actor.gt.data;

import java.util.Collection;
import java.util.Iterator;

public class FastLinkedList<E> implements Collection<E> {

    public boolean add(E element) {
        Entry entry = new Entry(element);
        if (_head == null) {
            _head = _tail = entry;
        } else {
            entry._previous = _tail;
            _tail._next = entry;
            _tail = entry;
        }
        _size++;
        return true;
    }

    public boolean addAll(Collection<? extends E> collection) {
        for (E element : collection) {
            add(element);
        }
        return true;
    }

    public void clear() {
        _head = _tail = null;
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Object element) {
        return findEntry((E) element) != null;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    public Entry findEntry(E element) {
        Entry entry = _head;
        while (entry != null) {
            if (entry._value == element || entry._value.equals(element)) {
                return entry;
            }
            entry = entry._next;
        }
        return null;
    }

    public Entry getHead() {
        return _head;
    }

    public Entry getTail() {
        return _tail;
    }

    public boolean isEmpty() {
        return _head != null;
    }

    public Iterator<E> iterator() {
        // FIXME: Implement later.
        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        Entry entry = findEntry((E) o);
        if (entry == null) {
            return false;
        } else {
            if (entry == _head) {
                _head = entry._next;
            }
            if (entry == _tail) {
                _tail = entry._previous;
            }
            if (entry._previous != null) {
                entry._previous._next = entry._next;
            }
            if (entry._next != null) {
                entry._next._previous = entry._previous;
            }
            _size--;
            return true;
        }
    }

    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object element : c) {
            modified |= remove(element);
        }
        return modified;
    }

    public boolean removeAllAfter(Entry entry) {
        if (entry == _tail) {
            return false;
        } else {
            entry._next = null;
            _tail = entry;
            _recalculateSize = true;
            return true;
        }
    }

    public boolean removeAllBefore(Entry entry) {
        if (entry == _head) {
            return false;
        } else {
            entry._previous = null;
            _head = entry;
            _recalculateSize = true;
            return true;
        }
    }

    public boolean retainAll(Collection<?> collection) {
        boolean modified = false;
        Entry entry = _head;
        while (entry != null) {
            if (!collection.contains(entry._value)) {
                remove(entry);
                modified = true;
            }
            entry = entry._next;
        }
        return modified;
    }

    public int size() {
        if (_recalculateSize) {
            _size = 0;
            Entry entry = _head;
            while (entry != null) {
                _size++;
                entry = entry._next;
            }
        }
        return _size;
    }

    public Object[] toArray() {
        Object[] array = new Object[size()];
        Entry entry = _head;
        int i = 0;
        while (entry != null) {
            array[i++] = entry._value;
            entry = entry._next;
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public <S> S[] toArray(S[] array) {
        if (array.length < size()) {
            array = (S[]) java.lang.reflect.Array.newInstance(
                    array.getClass().getComponentType(), size());
        }
        int i = 0;
        Entry entry = _head;
        while (entry != null) {
            array[i++] = (S) entry._value;
            entry = entry._next;
        }
        return array;
    }

    public class Entry {

        public FastLinkedList<E> getList() {
            return FastLinkedList.this;
        }

        public Entry getNext() {
            return _next;
        }

        public Entry getPrevious() {
            return _previous;
        }

        public E getValue() {
            return _value;
        }

        private Entry(E value) {
            this._value = value;
        }

        private Entry _next;

        private Entry _previous;

        private E _value;
    }

    private Entry _head;

    private boolean _recalculateSize;

    private int _size;

    private Entry _tail;
}
