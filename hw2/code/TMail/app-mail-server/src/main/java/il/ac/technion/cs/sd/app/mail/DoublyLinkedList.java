package il.ac.technion.cs.sd.app.mail;

/*
 * We need this implementation of doubly linked list - because Java's LinkedList 
 * does not support removal of arbitrary nodes in O(1) - deleting a node threw
 * one iterator invalidates all other iterator according to the doc.
 * 
 * The implementation in this module is based on existing code found at:
 * http://algs4.cs.princeton.edu/13stacks/DoublyLinkedList.java.html
 * 
 */

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class DoublyLinkedList<Item> implements Iterable<Item> {
    private int size;
    private Node beforeFirst;
    private Node afterLast;

    public DoublyLinkedList() {
        beforeFirst  = new Node();
        afterLast = new Node();
        beforeFirst.next = afterLast;
        afterLast.prev = beforeFirst;
    }

    public class Node {
        private Item item;
        private Node next;
        private Node prev;
    }

    public boolean isEmpty()    { return size == 0; }
    public int size()           { return size;      }

    // add the item to the list
    public void add(Item item) {
        Node last = afterLast.prev;
        Node x = new Node();
        x.item = item;
        x.next = afterLast;
        x.prev = last;
        afterLast.prev = x;
        last.next = x;
        size++;
    }

    public ListIterator<Item> iterator()  { return new DoublyLinkedListIterator(); }

    // assumes no calls to DoublyLinkedList.add() during iteration
    private class DoublyLinkedListIterator implements ListIterator<Item> {
        private Node current      = beforeFirst.next;  // the node that is returned by next()
        private Node lastAccessed = null;      // the last node to be returned by prev() or next()
                                               // reset to null upon intervening remove() or add()
        private int index = 0;

        public boolean hasNext()      { return index < size; }
        public boolean hasPrevious()  { return index > 0; }
        public int previousIndex()    { return index - 1; }
        public int nextIndex()        { return index;     }

        public Item next() {
            if (!hasNext()) throw new NoSuchElementException();
            lastAccessed = current;
            Item item = current.item;
            current = current.next; 
            index++;
            return item;
        }

        public Item previous() {
            if (!hasPrevious()) throw new NoSuchElementException();
            current = current.prev;
            index--;
            lastAccessed = current;
            return current.item;
        }

        // replace the item of the element that was last accessed by next() or previous()
        // condition: no calls on this itertator to remove() or add() after last 
        //call to next() or previous()
        public void set(Item item) {
            if (lastAccessed == null) throw new IllegalStateException();
            lastAccessed.item = item;
        }

        // remove the element that was last accessed by next() or previous()
        // condition: no calls on this iterator to remove() or add() after last call to next() or previous()
        // calls to those methods on other iterators is fine.
        public void remove() { 
            if (lastAccessed == null) throw new IllegalStateException();
            Node x = lastAccessed.prev;
            Node y = lastAccessed.next;
            x.next = y;
            y.prev = x;
            size--;
            if (current == lastAccessed)
                current = y;
            else
                index--;
            lastAccessed = null;
        }

        // add element to list 
        public void add(Item item) {
            Node x = current.prev;
            Node y = new Node();
            Node z = current;
            y.item = item;
            x.next = y;
            y.next = z;
            z.prev = y;
            y.prev = x;
            size++;
            index++;
            lastAccessed = null;
        }

    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Item item : this)
            s.append(item + " ");
        return s.toString();
    }

}


