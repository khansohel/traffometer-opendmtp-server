// ----------------------------------------------------------------------------
// Copyright 2006, Martin D. Flynn
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  This class provides an ordered Set
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

/**
 * This class provides an ordered set that can keep track of the events adding or removing an entry
 * to the set as well as the entries by adding a listner. This class also provides the functionality
 * to prevent the element already contained from being replaced with a new element.
 * 
 * @author Martin D. Flynn
 * @author Yoshiaki Iinuma
 */
public class OrderedSet implements Set, java.util.List, Cloneable {

  // ------------------------------------------------------------------------

  /** integer value representing adding event to an ordered set. */
  protected static final int ENTRY_ADDED = 1;
  /** integer value representing removing event from an ordered set. */
  protected static final int ENTRY_REMOVED = 2;

  /**
   * This class provides an interface to add a listener to an ordered set object. The listner is
   * notified when the ordered set is added or removed an entry object.
   * 
   * @author Martin D. Flynn
   * @author Yoshiaki Iinuma
   */
  public static interface ChangeListener {

    /**
     * Invoked when an object is added to an ordered set.
     * 
     * @param set an OrderedSet object to which an object is added
     * @param obj an entry object to be added to the set
     */
    public void entryAdded(OrderedSet set, Object obj);

    /**
     * Invoked when an object is removed from an ordered set.
     * 
     * @param set an OrderedSet object from which an object is removed
     * @param obj an entry object to be removed from a set
     */
    public void entryRemoved(OrderedSet set, Object obj);
  }

  /**
   * This class provides an adapter for the listner to detect adding or removing event to an ordered
   * set. This class exists as convenience for creating listner objects for adding and removing
   * event to an ordered set.
   * 
   * @author Martin D. Flynn
   * @author Yoshiaki Iinuma
   */
  public static class ChangeListenerAdapter implements ChangeListener {

    /**
     * Invoked when an object is added to an ordered set.
     * 
     * @param set an OrderedSet object to which an object is added
     * @param obj an entry object to be added to the set
     */
    public void entryAdded(OrderedSet set, Object obj) {
      // Print.logDebug("Item added: " + obj);
    }

    /**
     * Invoked when an object is removed from an ordered set.
     * 
     * @param set an OrderedSet object from which an object is removed
     * @param obj an entry object to be removed from a set
     */
    public void entryRemoved(OrderedSet set, Object obj) {
      // Print.logDebug("Item removed: " + obj);
    }
  }

  // ------------------------------------------------------------------------

  private java.util.List elements = null;
  private boolean retainOriginalValue = false;
  private java.util.List changeListeners = null;
  private int addChangeCount = 0;
  private int removeChangeCount = 0;

  /**
   * Constructs a new empty ordered set.
   * 
   */
  public OrderedSet() {
    super();
  }

  /**
   * Constructs an empty ordered set with an option to determine whether the element contained in
   * the set can be replaced with the same element.
   * 
   * @param retainOriginalValue the boolean value which indicates that an original value is retained
   *        when the same element is tried to add.
   */
  public OrderedSet(boolean retainOriginalValue) {
    this.setRetainOriginalValue(retainOriginalValue);
  }

  /**
   * Constructs an ordered set containing all the elements in the specified collection with an
   * option, which determines whether the element contained in the set can be replaced with the same
   * element.
   * 
   * @param c collection whose elemetns are to be placed into the ordered set
   * @param retainOriginalValue boolean value which indicates that an original value is retained
   */
  public OrderedSet(Collection c, boolean retainOriginalValue) {
    this(retainOriginalValue);
    this.addAll(c);
  }

  /**
   * Constructs an ordered set containing all of the elements in the specified collection. As
   * default, the elements contained the set can be replaced.
   * 
   * @param c collection whose elemetns are to be placed into the ordered set
   */
  public OrderedSet(Collection c) {
    this(c, false);
  }

  /**
   * Constructs an ordered set containing all of the elements as the specified ordered set.
   * 
   * @param os the ordered set to be deep copied.
   */
  public OrderedSet(OrderedSet os) {
    super();
    this.setRetainOriginalValue(os.getRetainOriginalValue());
    this.getBackingList().addAll(os.getBackingList());
  }

  // ------------------------------------------------------------------------

  /**
   * Returns a deep copy of this ordered set instance.
   * 
   * @return a deep copy of this ordered set instance
   */
  public Object clone() {
    return new OrderedSet(this);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the list containing all of the ChangeListeners registered to this set.
   * 
   * @param create the boolean value to indicate the creation of a new instance of a list if the
   *        list does not exist.
   * @return true if this set has the list to keep track of ChangeListeners.
   */
  protected java.util.List getChangeListeners(boolean create) {
    if ((this.changeListeners == null) && create) {
      this.changeListeners = new Vector();
    }
    return this.changeListeners;
  }

  /**
   * Tests if a ChangeListener is registered to this set.
   * 
   * @return true if a ChangeListener is registered.
   */
  protected boolean hasChangeListeners() {
    return (this.getChangeListeners(false) != null);
  }

  /**
   * Adds a ChangeListener object to this set.
   * 
   * @param cl the ChangeListner object to be added
   */
  public void addChangeListener(ChangeListener cl) {
    if (cl != null) {
      java.util.List listeners = this.getChangeListeners(true);
      if (!listeners.contains(cl)) {
        // Print.dprintln("Adding ChangeListener: " + StringTools.className(cl));
        listeners.add(cl);
      }
    }
  }

  /**
   * Removes the specified ChangeListener object registered to this set.
   * 
   * @param cl the ChangeListener object to be removed.
   */
  public void removeChangeListener(ChangeListener cl) {
    if (cl != null) {
      java.util.List listeners = this.getChangeListeners(false);
      if (listeners != null) {
        // Print.dprintln("Removing ChangeListener: " + StringTools.className(cl));
        listeners.remove(cl);
      }
    }
  }

  /**
   * Notify the action to the object to keep track of the activity.
   * 
   * @param action the action to be notified.
   * @param obj the object
   */
  protected void notifyChangeListeners(int action, Object obj) {
    java.util.List listeners = this.getChangeListeners(false);
    if (listeners != null) {
      for (Iterator i = listeners.iterator(); i.hasNext();) {
        ChangeListener cl = (ChangeListener) i.next();
        if (action == ENTRY_ADDED) {
          cl.entryAdded(this, obj);
          addChangeCount++;
        }
        else if (action == ENTRY_REMOVED) {
          cl.entryRemoved(this, obj);
          removeChangeCount++;
        }
        else {
          Print.logError("Unrecognized action: " + action);
        }
      }
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the list containing all the elements in this set.
   * 
   * @return the list containing all the elements in this set.
   */
  protected java.util.List getBackingList() {
    if (this.elements == null) {
      this.elements = new Vector();
    }
    return this.elements;
  }

  /**
   * Return the list contains all elements in this set.
   * 
   * @return the list contains all elements in this set.
   */
  public java.util.List getList() {
    return this.getBackingList();
  }

  // ------------------------------------------------------------------------

  /**
   * Return the current value of the RetainOriginalValue.
   * 
   * @return the current value of the RetainOriginalValue.
   */
  public boolean getRetainOriginalValue() {
    return this.retainOriginalValue;
  }

  /**
   * Sets the new value to the RetainOriginalValue.
   * 
   * @param state the boolean value to be set.
   */
  public void setRetainOriginalValue(boolean state) {
    this.retainOriginalValue = state;
  }

  // ------------------------------------------------------------------------

  /**
   * Retruns the element at the specified position in this set.
   * 
   * @param ndx the index at which the specified element is to be returned.
   * @return the element at the specified position.
   */
  public Object get(int ndx) {
    // java.util.List (mandatory)
    // allowed, since this is an ordered set (backed by a List)
    return this.getBackingList().get(ndx);
  }

  /**
   * Replaces the element at the specified postion in this set with the specified element. However,
   * this method is not supported yet.
   * 
   * @param ndx the index at which the specified element is to be replaced.
   * @param obj the object to be inserted.
   * @return the element to be previously stored at the specified position.
   * @throws UnsupportedOperationException
   */
  public Object set(int ndx, Object obj) {
    // java.util.List (optional)
    throw new UnsupportedOperationException();
  }

  // ------------------------------------------------------------------------

  /**
   * Inserts the specified element at the specified position in this set. If the index is out of
   * bound, the element is added in the end of the set. This method is called by other add method to
   * notify the adding event when ChangeListner is registered.
   * 
   * @param ndx the index at which the specified element is to be inserted.
   * @param obj the object to be inserted.
   */
  protected void _add(int ndx, Object obj) {
    // Print.dprintln("Adding: " + obj);
    if ((ndx < 0) || (ndx >= this.getBackingList().size())) {
      this.getBackingList().add(obj); // add to end
    }
    else {
      this.getBackingList().add(ndx, obj); // insert at index
    }
    this.notifyChangeListeners(ENTRY_ADDED, obj);
  }

  /**
   * Adds the specified element in the end of the set if this set does not contain it. If it does,
   * whether the present element is replaced depends on the value of retainOriginalValue. If its
   * value is true, the present element is retained, and the specified element is discared;
   * Otherwise, the present element is replaced by the specified element.
   * 
   * @param obj an object to be added
   * @return true if this set did not contain the specified element before the addition.
   */
  public boolean add(Object obj) {
    if (this.getRetainOriginalValue()) {
      boolean contained = this.contains(obj);
      if (!contained) { // retain original
        this._add(-1, obj);
      }
      return !contained;
    }
    else {
      boolean contained = this.remove(obj); // remove original
      this._add(-1, obj);
      return !contained;
    }
  }

  /**
   * Adds all of the elements in the specified collection into this set.
   * 
   * @param c the collection whose elements to be added into this list.
   * @return true if this list changed as a result of the call.
   */
  public boolean addAll(Collection c) {
    if ((c != null) && (c.size() > 0)) {
      for (Iterator i = c.iterator(); i.hasNext();) {
        this.add(i.next());
      }
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Inserts the specified element at the specified position in this set.
   * 
   * @param ndx the index at which to insert the element.
   * @param obj the object to be inserted.
   */
  public void add(int ndx, Object obj) {
    if (this.getRetainOriginalValue()) {
      boolean contained = this.contains(obj);
      if (!contained) { // retain original
        this._add(ndx, obj);
      }
      // return !contained;
    }
    else {
      boolean contained = this.remove(obj); // remove original
      this._add(ndx, obj);
      // return !contained;
    }
  }

  /**
   * Inserts all of the elements in the specified collection into this set, starting at the
   * specified position. However, this method is not supported.
   * 
   * @param ndx the index at which to insert the first element in the specified collection.
   * @param c the collection whose elements to be inserted into this list.
   * @return true if this list changed as a result of the call.
   * @throws UnsupportedOperationException if this method is called.
   */
  public boolean addAll(int ndx, Collection c) {
    // java.util.List (optional)
    throw new UnsupportedOperationException();
  }

  // ------------------------------------------------------------------------

  /**
   * Tests if this set contains the specified object.
   * 
   * @param obj the object whose presence in this set to be tested.
   * @return true if the specified object is contained by this set.
   */
  public boolean contains(Object obj) {
    return this.getBackingList().contains(obj);
  }

  /**
   * Tests if this set contains all of the elements in the specified collection.
   * 
   * @param c the collection to be tested for the presence of all the elements in this set.
   * @return true if this set contains all of the elements in the specified collection.
   */
  public boolean containsAll(Collection c) {
    return this.getBackingList().containsAll(c);
  }

  // ------------------------------------------------------------------------

  /**
   * Test if the specified object is an ordered set and has the same elements as this set in the
   * same order.
   * 
   * @param other the object to be compared with this set.
   * @return true if this set is the same as the specified object.
   */
  public boolean equals(Object other) {
    if (other instanceof OrderedSet) {
      OrderedSet os = (OrderedSet) other;
      java.util.List L1 = this.getBackingList();
      java.util.List L2 = os.getBackingList();
      // boolean eq = L1.containsAll(L2) && L2.containsAll(L1);
      boolean eq = L1.equals(L2); // same elements, same order
      return eq;
    }
    else {
      return false;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns a hash code value for this set.
   * 
   * @return a hash code value for this set.
   */
  public int hashCode() {
    return this.getBackingList().hashCode();
  }

  // ------------------------------------------------------------------------

  /**
   * Remove the first occurrence of the specified element from this set. This method is called by
   * other remove methods to notify the removing event when ChangeListner is registered.
   * 
   * @param obj the element to be removed from this set.
   * @return true if this set contains the specified element.
   */
  protected boolean _remove(Object obj) {
    // Print.dprintln("Removing: " + obj);
    if (this.getBackingList().remove(obj)) {
      this.notifyChangeListeners(ENTRY_REMOVED, obj);
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Remove the element specified by the iterator from this set. This method is called by other
   * remove methods inside iteration to notify the removing event.
   * 
   * @param obj the element to be removed from this set.
   * @param i the iterator pointing at the specified element.
   * @return true always.
   */
  protected boolean _remove(Object obj, Iterator i) {
    // Print.dprintln("Removing: " + obj);
    i.remove();
    this.notifyChangeListeners(ENTRY_REMOVED, obj);
    return true;
  }

  /**
   * Removes the element at specified position in the set. However, this method is not supported
   * yet.
   * 
   * @param ndx index of the element to be removed.
   * @return the element previously at the specified position in the set.
   * @throws UnsupportedOperationException if this method is called.
   */
  public Object remove(int ndx) {
    // java.util.List (optional)
    throw new UnsupportedOperationException();
  }

  /**
   * Remove the first occurrence of the specified element from this set.
   * 
   * @param obj the element to be removed from this set.
   * @return true if this set contains the specified element.
   */
  public boolean remove(Object obj) {
    return this._remove(obj);
  }

  /**
   * Removes all of the elements contained in the specified collection from this set.
   * 
   * @param c the collection that specifies the elements to be removed.
   * @return true if this set changed as a result of the call.
   */
  public boolean removeAll(Collection c) {
    if (!this.hasChangeListeners()) {
      return this.getBackingList().removeAll(c);
    }
    else if (c == this) {
      if (this.size() > 0) {
        this.clear();
        return true;
      }
      else {
        return false;
      }
    }
    else if ((c != null) && (c.size() > 0)) {
      boolean changed = false;
      for (Iterator i = c.iterator(); i.hasNext();) {
        if (this.remove(i.next())) {
          changed = true;
        }
      }
      return changed;
    }
    else {
      return false;
    }
  }

  /**
   * Removes the elements that are not contained in the specified collection from this set.
   * 
   * @param c the collection that specifies the elements to be retained.
   * @return true if this set changed as a result of the call.
   */
  public boolean retainAll(Collection c) {
    if (!this.hasChangeListeners()) {
      return this.getBackingList().retainAll(c);
    }
    else if (c == this) {
      return false;
    }
    else if ((c != null) && (c.size() > 0)) {
      boolean changed = false;
      for (Iterator i = this.getBackingList().iterator(); i.hasNext();) {
        Object obj = i.next();
        if (!c.contains(obj)) {
          this._remove(obj, i);
          changed = true;
        }
      }
      return changed;
    }
    else {
      return false;
    }
  }

  /**
   * Remove all of the elements in this set.
   */
  public void clear() {
    if (!this.hasChangeListeners()) {
      this.getBackingList().clear();
    }
    else {
      for (Iterator i = this.getBackingList().iterator(); i.hasNext();) {
        Object obj = i.next();
        this._remove(obj, i);
      }
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the number of the elements in this set.
   * 
   * @return the number of the elements in this set.
   */
  public int size() {
    return this.getBackingList().size();
  }

  /**
   * Tests if this set is empty.
   * 
   * @return true if this set has no elements.
   */
  public boolean isEmpty() {
    return (this.size() == 0);
  }

  // ------------------------------------------------------------------------

  /**
   * Searches for the first occurrence of the specified element in this set.
   * 
   * @param obj the object to be searched.
   * @return the index of the first occurrence of the specified object in the set; returns -1 if the
   *         object is not found.
   */
  public int indexOf(Object obj) {
    // java.util.List
    return this.getBackingList().indexOf(obj);
  }

  /**
   * Searches for the last occurrence of the specified element in this set.
   * 
   * @param obj the object to be searched.
   * @return the index of the last occurrence of the specified object in the set; returns -1 if the
   *         object is not found.
   */
  public int lastIndexOf(Object obj) {
    // java.util.List
    return this.getBackingList().lastIndexOf(obj);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns an iterator over the elements in this set.
   * 
   * @return an iterator over the elements in this set.
   */
  public Iterator iterator() {
    if (!this.hasChangeListeners()) {
      // OK, since only 'remove' is allowed
      return this.getBackingList().iterator();
    }
    else {
      return new Iterator() {
        private Object thisObject = null;
        private Iterator i = OrderedSet.this.getBackingList().iterator();

        public boolean hasNext() {
          return i.hasNext();
        }

        public Object next() {
          this.thisObject = i.next();
          return this.thisObject;
        }

        public void remove() {
          OrderedSet.this._remove(this.thisObject, i);
          this.thisObject = null;
        }
      };
    }
  }

  /**
   * Returns a list iterator over the elements in this set.
   * 
   * @return a list iterator over the elements in this set. 
   */
  public ListIterator listIterator() {
    // java.util.List (mandatory)
    return this.listIterator(-1);
  }

  /**
   * Returns a list iterator over the elements in this set, starting at the specified position in the
   * set.
   * 
   * @param ndx the index of the first element to be returned from the list iterator.
   * @return a list iterator of the elements in this set, starting at the specified position in the
   *         set.
   */
  public ListIterator listIterator(final int ndx) {
    if (!this.hasChangeListeners()) {
      // OK, since only 'remove' is allowed
      return (ndx >= 0) ? this.getBackingList().listIterator(ndx) : this.getBackingList()
          .listIterator();
    }
    else {
      return new ListIterator() {
        private Object thisObject = null;
        private ListIterator i = (ndx >= 0) ? OrderedSet.this.getBackingList().listIterator(ndx)
            : OrderedSet.this.getBackingList().listIterator();

        public boolean hasNext() {
          return i.hasNext();
        }

        public boolean hasPrevious() {
          return i.hasPrevious();
        }

        public Object next() {
          this.thisObject = i.next();
          return this.thisObject;
        }

        public int nextIndex() {
          return i.nextIndex();
        }

        public Object previous() {
          this.thisObject = i.previous();
          return this.thisObject;
        }

        public int previousIndex() {
          return i.previousIndex();
        }

        public void remove() {
          OrderedSet.this._remove(this.thisObject, i);
          this.thisObject = null;
        }

        public void add(Object obj) {
          throw new UnsupportedOperationException();
        }

        public void set(Object obj) {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns a subet of this set between fromIndex and toIndex. However, this method is not
   * supported yet.
   * 
   * @param fromIndex the low endpoint of the sublist.
   * @param toIndex the high endpoint of the sublist.
   * @return a subset of this set specified between fromIndex and toIndex..
   * @throws UnsupportedOperationException if this method is called.
   */
  public List subList(int fromIndex, int toIndex) {
    // java.util.List (mandatory?)
    // not currently worth the effort to implement this
    throw new UnsupportedOperationException();
  }

  // ------------------------------------------------------------------------

  /**
   * Returns an array containing all of the elements in this set.
   * 
   * @return an array containing all of the elemetns in this set.
   */
  public Object[] toArray() {
    return this.getBackingList().toArray();
  }

  /**
   * Returns an array containing all of the elements in this set. If the elements fits in the
   * specified array, it is returned therein. Ohterwise, a new array is allocated with the size of
   * the set.
   * 
   * @param a the array into which the elements of the set are to be stored.
   * @return an array containing the elemetns in this set.
   */
  public Object[] toArray(Object a[]) {
    return this.getBackingList().toArray(a);
  }

  // ------------------------------------------------------------------------

  /**
   * Prints all of the elements in this set into the log.
   */
  public void printContents() {
    int n = 0;
    for (Iterator i = this.iterator(); i.hasNext();) {
      Print.logInfo("" + (n++) + "] " + i.next());
    }
  }

  // ------------------------------------------------------------------------

}
