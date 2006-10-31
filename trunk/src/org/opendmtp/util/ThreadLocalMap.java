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
//  Provides a per-thread Map instance
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Provides a thread-local map instance. All the variables in the class have an independent instance
 * for each thread that accesses them.
 * 
 * @author Martin D. Flynn
 * @author Guanghong Yang
 * 
 */
public class ThreadLocalMap extends ThreadLocal implements Map {

  /** The default value for <tt>mapClass</tt>. Used when no parameter is given in constructor. */
  private static final Class DefaultMapClass = Hashtable.class;

  /** The class of the <tt>ThreadLocalMap</tt> instance. */
  private Class mapClass = null;

  /** Non-parameter constructor. Same as <tt>ThreadLocalMap(null)</tt>. */
  public ThreadLocalMap() {
    this(null);
  }

  /**
   * Creates a new thread-local map. Sets the mapClass variable.
   * 
   * @param mapClass The class to be set to mapClass.
   */
  public ThreadLocalMap(Class mapClass) {
    super();
    this.setMapClass(mapClass);
  }

  /**
   * Returns the mapClass.
   * 
   * @return The mapClass variable.
   */
  public Class getMapClass() {
    return this.mapClass;
  }

  /**
   * Sets the mapClass to be the parameter. If the parameter is null, mapClass will be set as
   * <tt>Hashtable</tt>.
   * 
   * Note that the parameter map is not checked to be a Map class or subclass.
   * 
   * @param map The object to be set to mapClass.
   */
  public void setMapClass(Class map) {
    // Should check that 'mapClass' implements 'Map' (just assume for now)
    this.mapClass = (mapClass != null) ? mapClass : DefaultMapClass;
  }

  /**
   * Overides <tt>initialValue()</tt> of superclass (<tt>ThreadLocal</tt>). Creates a new
   * instance when this thread first access the class object. If exception occurs, return a new
   * instance of <tt>Hashtable</tt>.
   * 
   * @return New instance of <tt>mapClass</tt>.
   * @see java.lang.ThreadLocal#initialValue()
   */
  protected Object initialValue() {
    Class mc = this.getMapClass();
    try {
      return (Map) mc.newInstance();
    }
    catch (Throwable t) {
      // Give up and try a Hashtable
      Print.logException("Error instantiating: " + mc, t);
      return new Hashtable();
    }
  }

  /**
   * Returns the map instance in the current thread's copy of this thread-local variable. Creates
   * and initializes the copy if this is the first time the thread has called this method.
   * 
   * @return the current thread's value of this thread-local.
   * @see java.lang.ThreadLocal#get()
   */
  protected Map getMap() {
    return (Map) this.get();
  }

  /**
   * Implements <tt>clear()</tt> method in Map. Removes all mappings from the map instance.
   */
  public void clear() {
    this.getMap().clear();
  }

  /**
   * Implements <tt>containsKey(Object)</tt> method in Map. Returns true if the map instance
   * contains a mapping for the specified key.
   * 
   * @param key - The key to be matched.
   * @return True if the map instance contains key, or false otherwise.
   */
  public boolean containsKey(Object key) {
    return this.getMap().containsKey(key);
  }

  /**
   * Implements <tt>containsValue(Object)</tt> method in Map. Returns true if the map instance
   * maps one or more keys to the specified value.
   * 
   * @param value - The value to be matched.
   * @return True if the map instance contains value, or false otherwise.
   */
  public boolean containsValue(Object value) {
    return this.getMap().containsValue(value);
  }

  /**
   * Implements <tt>entrySet()</tt> method in Map. Returns a set view of the mappings contained in
   * this map.
   * 
   * @return A set view of the mappings contained in this map.
   */
  public Set entrySet() {
    return this.getMap().entrySet();
  }

  /**
   * Overides <tt>equals(Object)</tt> method in Map. Returns a set view of the mappings contained
   * in the map instance. Each element in the returned set is a Map.Entry.
   * 
   * @param o The map Object to be compared.
   * @return True if the map instance is equal to o, or false otherwise.
   */
  public boolean equals(Object o) {
    if (o instanceof ThreadLocalMap) {
      return this.getMap().equals(((ThreadLocalMap) o).getMap());
    }
    else {
      return false;
    }
  }

  /**
   * Implements <tt>get(Object)</tt> method in Map. Returns the value to which this map maps the
   * specified key. Returns null if the map contains no mapping for this key. A return value of null
   * does not necessarily indicate that the map contains no mapping for the key; it's also possible
   * that the map explicitly maps the key to null. The containsKey operation may be used to
   * distinguish these two cases.
   * 
   * @param key Key whose associated value is to be returned.
   * @return The object mapped to the key in the map instance.
   */
  public Object get(Object key) {
    return this.getMap().get(key);
  }

  /**
   * Overides <tt>hashCode()</tt> method in Map. Return The hash code value of the map instance.
   * 
   * @return The hash code value of the map instance.
   */
  public int hashCode() {
    return this.getMap().hashCode();
  }

  /**
   * Implements <tt>isEmpty()</tt> method in Map. Returns true if this map instance contains no
   * key-value mappings.
   * 
   * @return True if the map instance is empty, or false otherwise.
   */
  public boolean isEmpty() {
    return this.getMap().isEmpty();
  }

  /**
   * Implements <tt>keySet()</tt> method in Map. Returns a set view of the keys contained in the
   * map instance.
   * 
   * @return A set view of the keys contained in the map instance.
   */
  public Set keySet() {
    return this.getMap().keySet();
  }

  /**
   * Implements <tt>put(Object, Object)</tt> method in Map. Associates the specified value with
   * the specified key in the map instance.
   * 
   * @param key Key with which the specified value is to be associated.
   * @param value Value to be associated with the specified key.
   * @return previous value associated with specified key, or null if there was no mapping for key.
   *         A null return can also indicate that the map previously associated null with the
   *         specified key, if the implementation supports null values.
   */
  public Object put(Object key, Object value) {
    return this.getMap().put(key, value);
  }

  /**
   * Implements <tt>putAll(Map)</tt> method in Map. Copies all of the mappings from the specified
   * map to this map. The effect of this call is equivalent to that of calling put(k, v) on this map
   * once for each mapping from key k to value v in the specified map. The behavior of this
   * operation is unspecified if the specified map is modified while the operation is in progress.
   * 
   * @param t Mappings to be stored in this map.
   */
  public void putAll(Map t) {
    this.getMap().putAll(t);
  }

  /**
   * Implements <tt>remove(Object)</tt> method in Map. Removes the mapping for this key from this
   * map if it is present.
   * 
   * @param key Key whose mapping is to be removed from the map.
   * @return A set view of the keys contained in the map instance.
   */
  public Object remove(Object key) {
    return this.getMap().remove(key);
  }

  /**
   * Implements <tt>size()</tt> method in Map. Returns the number of key-value mappings in this
   * map instance.
   * 
   * @return A set view of the keys contained in the map instance.
   */
  public int size() {
    return this.getMap().size();
  }

  /**
   * Implements <tt>values()</tt> method in Map. Returns a collection view of the values contained
   * in this map.
   * 
   * @return A collection view of the values contained in this map.
   */
  public Collection values() {
    return this.getMap().values();
  }

}
