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
//  This class provides an ordered HashMap
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Extends HashMap and implements a Map which maintains the order of key values.
 * @author Martin D. Flynn
 * @author Mark Stillwell
 */
public class OrderedMap extends HashMap implements Map {

  // ------------------------------------------------------------------------

  private OrderedSet keyOrder = null;
  private Map ignoredCaseMap = null;

  /**
   * Creates a new OrderedMap.
   */
  public OrderedMap() {
    super();
    this.keyOrder = new OrderedSet();
  }

  /**
   * Creates a new OrderedMap with the same key to value mapping as the given Map.
   * @param map a Map of key/value pairs to copy
   */
  public OrderedMap(Map map) {
    this();
    this.putAll(map);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns true if this OrderedMap maintains an additional Map that ignores the case of keys.
   * @return true if this object maintains an additional Map that ignores the case of keys
   */
  public boolean isIgnoreCase() {
    return (this.ignoredCaseMap != null);
  }

  /**
   * Sets whether this OrderedMap maintains an additional Map that ignores the case of keys.
   * @param ignoreCase true if this Map should maintain another map that ignores the case of keys
   */
  public void setIgnoreCase(boolean ignoreCase) {
    if (ignoreCase) {
      if (this.ignoredCaseMap == null) {
        this.ignoredCaseMap = new HashMap();
        for (Iterator i = this.keyOrder.iterator(); i.hasNext();) {
          Object key = i.next();
          if (key instanceof String) {
            this.ignoredCaseMap.put(((String)key).toLowerCase(), key);
          }
        }
      }
    }
    else {
      if (this.ignoredCaseMap != null) {
        this.ignoredCaseMap = null;
      }
    }
  }

  /**
   * Returns the object mapped to by the Map that ignores the case of keys. If this OrderedMap does
   * not maintain a separate Map that ignores the case of keys, or if the ignore case Map does not
   * contain the given key, or if the given key is not a String, returns the the given key.
   * @param key a key possibly associated to a value by this Map
   * @return a value in this Map, or key
   */
  public Object keyCaseFilter(Object key) {
    if ((this.ignoredCaseMap != null) && (key instanceof String)) {
      Object k = this.ignoredCaseMap.get(((String)key).toLowerCase());
      if (k != null) {
        // if (!k.equals(key)) { Print.logStackTrace("Filtered key: " + key + " ==> " + k); }
        return k;
      }
    }
    return key;
  }

  // ------------------------------------------------------------------------

  /**
   * Removes all mappings from this map.
   */
  public void clear() {
    super.clear();
    this.keyOrder.clear();
    if (this.ignoredCaseMap != null) {
      this.ignoredCaseMap.clear();
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns a set view of the mappings contained in this map.
   * @return a set view of the mappings contained in this map
   */
  public Set entrySet() {
    // Attempting to return an ordered set of 'Map.Entry' entries.
    // The effect this will have on calls to this method from HashMap itself
    // isn't fully known.

    /* Map.Entry map */
    Set es = super.entrySet(); // unordered
    HashMap meMap = new HashMap();
    for (Iterator i = es.iterator(); i.hasNext();) {
      Map.Entry me = (Map.Entry) i.next();
      Object key = me.getKey();
      meMap.put(key, me);
    }

    /* place in keyOrder */
    OrderedSet entSet = new OrderedSet();
    for (Iterator i = this.keyOrder.iterator(); i.hasNext();) {
      Object key = i.next();
      Object me = meMap.get(key);
      if (me == null) {
        Print.logError("Map.Entry is null!!!");
      }
      entSet.add(me);
    }
    return entSet;

  }

  // ------------------------------------------------------------------------

  /**
   * Returns a set view of the keys contained in this map.
   * @return a set view of the keys contained in this map
   */
  public Set keySet() {
    return new OrderedSet(this.keyOrder);
  }

  /**
   * Returns a set view of the values contained in this map.
   * @return a set view of the values contained in this map
   */
  public Set valueSet() {
    OrderedSet valSet = new OrderedSet();
    for (Iterator i = this.keyOrder.iterator(); i.hasNext();) {
      valSet.add(super.get(i.next()));
    }
    return valSet;
  }

  // ------------------------------------------------------------------------

  /**
   * Returns a collection view of the values contained in this map.
   * @return a collection view of the values contained in this map
   */
  public Collection values() {
    // All this work is to make sure the returned Collection is still backed by the Map
    return new ListTools.CollectionProxy(super.values()) {
      public Iterator iterator() {
        return new Iterator() {
          private Iterator i = OrderedMap.this.keySet().iterator();

          public boolean hasNext() {
            return i.hasNext();
          }

          public Object next() {
            return OrderedMap.this.get(i.next());
          }

          public void remove() {
            throw new UnsupportedOperationException("'remove' not supported here");
          }
        };
      }

      public Object[] toArray() {
        return ListTools.toList(this.iterator()).toArray();
      }

      public Object[] toArray(Object a[]) {
        return ListTools.toList(this.iterator()).toArray(a);
      }
    };
  }

  // ------------------------------------------------------------------------

  /**
   * Puts a value in this Map with the given key in the given position.
   * @param ndx position to put the key in this map
   * @param key key to map to value
   * @param value value to be put in this Map
   * @return previous value associated with the given key, or null if no such value exists
   */
  public Object put(int ndx, Object key, Object value) {
    if ((this.ignoredCaseMap != null) && (key instanceof String)) {
      this.ignoredCaseMap.put(((String) key).toLowerCase(), key);
    }
    this.keyOrder.add(ndx, key);
    return super.put(key, value);
  }

  /**
   * Puts a value in this Map with the given key.  The key is placed in the last position.
   * @param key to map to value
   * @param value a value to be put in this Map
   * @return previous value associated with the given key, or null if no such value exists   
   */
  public Object put(Object key, Object value) {
    if ((this.ignoredCaseMap != null) && (key instanceof String)) {
      this.ignoredCaseMap.put(((String) key).toLowerCase(), key);
    }
    this.keyOrder.add(key);
    return super.put(key, value);
  }

  /**
   * Puts a value in this Map with the given String as its key.  The key is placed in the last 
   * position.
   * @param key a String to map to value
   * @param value a value to be put in this map
   * @return previous value associated with the given key, or null if no such value exists   
   */
  public Object setProperty(String key, String value) {
    return this.put(key, value);
  }

  /**
   * Adds the contents of the given map to the end of this map.
   * @param map a map to add to the end of this one
   */
  public void putAll(Map map) {
    for (Iterator i = map.keySet().iterator(); i.hasNext();) {
      Object key = i.next();
      this.put(key, map.get(key));
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Tests if this map has the given key without considering case.
   * @param key a key mapped to a value by this Map
   * @return true if this map has the given key (ignores case)
   */
  public boolean containsKeyIgnoreCase(String key) {
    if (key != null) {
      // TODO: Optimize!
      for (Iterator i = this.keyOrder.iterator(); i.hasNext();) {
        Object k = i.next();
        if ((k instanceof String) && key.equalsIgnoreCase((String) k)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if this map contains a mapping for the specified key.
   * @param key a key mapped to a value by this Map
   * @return true if this map contains a mapping for the specified key
   */
  public boolean containsKey(Object key) {
    return super.containsKey(this.keyCaseFilter(key));
  }

  // ------------------------------------------------------------------------

  /**
   * Removes the mapping for this key from this map if it is present.
   * @param key ey whose mapping is to be removed from the map
   * @return previous value associated with specified key, or null  if there was no mapping
   */
  public Object remove(Object key) {
    Object k = this.keyCaseFilter(key);
    if ((this.ignoredCaseMap != null) && (key instanceof String)) {
      this.ignoredCaseMap.remove(((String) key).toLowerCase());
    }
    this.keyOrder.remove(k);
    return super.remove(k);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the value to which this map maps the specified key.
   * @param key key whose associated value is to be returned
   * @return value to which this map maps the key, or null if the map contains no mapping
   */
  public Object get(Object key) {
    return super.get(this.keyCaseFilter(key));
  }

  /**
   * Returns a String representing the object associated with the given key in this map. If no such
   * object exists, returns the given default value.
   * @param key a key mapped to a value by this Map.
   * @param dft a default return value
   * @return a String representing the value associated with key if it exists, dft otherwise
   */
  public String getProperty(String key, String dft) {
    if (this.containsKey(key)) {
      Object val = this.get(key);
      return (val != null) ? val.toString() : null;
    }
    else {
      return dft;
    }
  }

  /**
   * Returns a String representing the object associated with the given key in this map.
   * @param key a key mapped to a value by this Map
   * @return a String representing the value associated with key if it exists, null otherwise
   */
  public String getProperty(String key) {
    return this.getProperty(key, null);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the key in the given position. Returns null if this position is less than 0 or larger
   * than the position of the last key.
   * @param ndx the index of the key to return
   * @return the key at position ndx 
   */
  public Object getKey(int ndx) {
    return ((ndx >= 0) && (ndx < this.keyOrder.size())) ? this.keyOrder.get(ndx) : null;
  }

  /**
   * Returns the value whose key is in the given position. Returns null if this position is less 
   * than 0 or larger than the position of the last key.
   * @param ndx the index of the key to return
   * @return the value at position ndx
   */
  public Object getValue(int ndx) {
    Object key = this.getKey(ndx);
    return (key != null) ? super.get(key) : null;
  }

  /**
   * Removes the key/value pair at the given position.
   * @param ndx the index of the key/value pair to remove
   */
  public void remove(int ndx) {
    this.remove(this.getKey(ndx));
  }

  /**
   * Returns an Iterator over the keys for this map. The Iterator returns keys in the order
   * maintained by this Map.
   * @return an Iterator over the keys for this map
   */
  public Iterator keys() {
    // copied to prevent 'remove'
    return (new Vector(this.keyOrder)).iterator();
  }

  // ------------------------------------------------------------------------

}
