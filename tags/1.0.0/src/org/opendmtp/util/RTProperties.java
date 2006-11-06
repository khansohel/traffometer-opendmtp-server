//----------------------------------------------------------------------------
//Copyright 2006, Martin D. Flynn
//All rights reserved
//----------------------------------------------------------------------------

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

//----------------------------------------------------------------------------
//Description:
//Runtime properties container
//----------------------------------------------------------------------------
//Change History:
//2006/03/26  Martin D. Flynn
//Initial release
//2006/04/02  Martin D. Flynn
//Added ability to separate command-line key/value pairs with a ':'.
//2006/04/23  Martin D. Flynn
//Integrated logging changes made to Print
//----------------------------------------------------------------------------
package org.opendmtp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Holds the runtime properties of the server.
 * 
 * @author Martin D. Flynn
 * @author Nam Nguyen
 */
public class RTProperties implements Cloneable {

  // ------------------------------------------------------------------------

  /**
   * Holds the name of the key that corresponds to the name of the current set of properties.
   */
  public static final String KEY_NAME = "$name";

  /**
   * Holds the name separator character ';'.
   */
  public static final char NameSeparatorChar = ':';
  /**
   * Holds the key=value separator character '='.
   */
  public static final char KeyValSeparatorChar = '=';

  /**
   * Controls whether boolean properties with unspecified values will return true, or false.
   * Example:
   * ""              - getBoolean("bool", dft) returns dft.
   * "bool=true"     - getBoolean("bool", dft) returns 'true'.
   * "bool=false"    - getBoolean("bool", dft) returns 'false'.
   * "bool=badvalue" - getBoolean("bool", dft) returns dft.
   * "bool"          - getBoolean("bool", dft) returns DEFAULT_TRUE_IF_BOOLEAN_STRING_EMPTY.
   */
  private static final boolean DEFAULT_TRUE_IF_BOOLEAN_STRING_EMPTY = true;

  // ------------------------------------------------------------------------

  /**
   * Holds the pairs property:value.
   */
  private Map cfgProperties = null;
  /**
   * Seems to be a case sensitivity setting.
   * Has never been used except for in (set-get)IgnoreKeyCase.
   */
  private boolean ignoreCase = false;

  // ------------------------------------------------------------------------
  /**
   * Returns the index of the Key Value separator.
   * 
   * @param kv A string of the form "Key:Value" or "Key=Value"
   * @return The index of the Key Value separator.
   */
  private static int _findKeyValSeparator(String kv) {
    //return kv.indexOf('=');
    for (int i = 0; i < kv.length(); i++) {
      char ch = kv.charAt(i);
      if ((ch == '=') || (ch == ':')) {
        return i;
      }
    }
    return -1;
  }

  // ------------------------------------------------------------------------

  /**
   * Creates a new RTProperties instance that holds runtime properties.
   * 
   * @param map A map of properties.
   */
  public RTProperties(Map map) {
    this.cfgProperties = map;
  }

  /**
   * Creates a new RTProperties instance that holds runtime properties.
   */
  public RTProperties() {
    this((Map) null);
  }

  /**
   * Creates a new RTProperties instance that holds runtime properties.
   * @param props A list of properties in the form of a string.
   */
  public RTProperties(String props) {
    this();
    this.setProperties(props, true);
  }

  /**
   * Creates a new RTProperties instance that holds runtime properties.
   * 
   * @param argv command line argument properties.
   */
  public RTProperties(String argv[]) {
    this();
    if (argv != null) {
      for (int i = 0; i < argv.length; i++) {
        int p = _findKeyValSeparator(argv[i]); // argv[i].indexOf("=");
        String key = (p >= 0) ? argv[i].substring(0, p) : argv[i];
        String val = (p >= 0) ? argv[i].substring(p + 1) : "";
        while (key.startsWith("-")) {
          key = key.substring(1);
        }
        if (!key.equals("")) {
          //Print._println("KeyVal: " + key + " ==> " + val);
          this.setString(key, val);
        }
      }
    }
  }

  /**
   * Creates a new RTProperties instance that holds runtime properties.
   * 
   * @param cfgFile A file holding runtime properties.
   */
  public RTProperties(File cfgFile) {
    this(CreateDefaultMap());
    if ((cfgFile == null) || cfgFile.equals("")) {
      // ignore this case
    }
    else if (cfgFile.isFile()) {
      if (!RTConfig.getBoolean(RTKey.RT_QUIET, true)) {
        Print.logInfo("Loading config file: " + cfgFile);
      }
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(cfgFile);
        OrderedProperties props = new OrderedProperties();
        props.load(fis); // "props.put(key,value)" is used for insertion
        this.setProperties(props.getOrderedMap(), true);
      }
      catch (IOException ioe) {
        Print.logError("Unable to load config file: " + cfgFile + " [" + ioe + "]");
      }
      finally {
        if (fis != null) {
          try {
            fis.close();
          }
          catch (IOException ioe) {/*ignore*/
          }
        }
      }
    }
    else {
      Print.logError("Config file doesn't exist: " + cfgFile);
    }
  }

  /**
   * Creates a new RTProperties instance that holds runtime properties.
   * 
   * @param rtp An existing RTProperties instance.
   */
  public RTProperties(RTProperties rtp) {
    this();
    this.setProperties(rtp, true);
  }

  // ------------------------------------------------------------------------

  /**
   * Implements the clone function.
   * 
   * @return The cloned from RTProperties object.
   */
  public Object clone() {
    return new RTProperties(this);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the value of ignoreCase field, but is not used anywhere in the project.
   * 
   * @return The value of ignoreCase field.
   */
  public boolean getIgnoreKeyCase() {
    return this.ignoreCase;
  }

  /**
   * Sets the value of the ignoreCase field.
   * 
   * @param ignCase The new value of ignoreCase.
   */
  public void setIgnoreKeyCase(boolean ignCase) {
    this.ignoreCase = ignCase;
    Map props = this.getProperties();
    if (props instanceof OrderedMap) {
      ((OrderedMap) props).setIgnoreCase(this.ignoreCase);
    }
    else {
      Print.logWarn("Backing map is not an 'OrderedMap', case insensitive keys not in effect");
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the name of the current set of properties.
   * 
   * @return The name of the current set of properties.
   */
  public String getName() {
    return this.getString(KEY_NAME, "");
  }

  /**
   * Sets the name of the current set of properties.
   * 
   * @param name The new name.
   */
  public void setName(String name) {
    this.setString(KEY_NAME, name);
  }

  // ------------------------------------------------------------------------

  /**
   * Prints to logfile the list of keys in the properties list for which RTKey has no default 
   * value.
   * This is typically for listing unregistered, and possibly obsolete, properties found 
   * in a config file.
   */
  public void checkDefaults() {
    // This produces a list of keys in the properties list for which RTKey has not 
    // default value.  This is typically for listing unregistered, and possibly 
    // obsolete, properties found in a config file.
    for (Iterator i = this.keyIterator(); i.hasNext();) {
      String key = i.next().toString();
      if (!RTKey.hasDefault(key)) {
        Print.logDebug("No default for key: " + key);
      }
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Specifies the default map class.
   */
  protected static Class DefaultMapClass = OrderedMap.class;

  /**
   * Creates a new map object to hold the pairs property:value.
   * Type of map is specified by variable DefaultMapClass.
   * 
   * @return The newly created map.
   */
  protected static Map CreateDefaultMap() {
    try {
      Map map = (Map) DefaultMapClass.newInstance();
      return map;
    }
    catch (Throwable t) {
      // Give up and try a Hashtable (Do not use 'Print' here!!!)
      System.out.println("[RTProperties] Error instantiating: " + DefaultMapClass); // 
      return new OrderedMap();
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the current set of properties.
   * If the map of properties is empty, create and return a new map of class
   * defined by DefaultMapClass.
   * 
   * @return The current set of properties.
   */
  public Map getProperties() {
    if (this.cfgProperties == null) {
      this.cfgProperties = CreateDefaultMap();
      if (this.cfgProperties instanceof OrderedMap) {
        ((OrderedMap) this.cfgProperties).setIgnoreCase(this.ignoreCase);
      }
    }
    return this.cfgProperties;
  }

  /**
   * Returns a set view of the property keys contained in the map.
   * 
   * @return A set view of the keys contained in the map.
   */
  public Set getPropertyKeys() {
    return this.getProperties().keySet();
  }

  /**
   * Returns an iterator over the elements in the property key set.
   * 
   * @return An iterator over the elements in the property key set.
   */
  public Iterator keyIterator() {
    return this.getPropertyKeys().iterator();
  }

  /**
   * Returns the list of property keys as an OrderedSet that start with a specific substring.
   * 
   * @param startsWith A substring that identifies which keys to return.
   * @return The list of property keys as an OrderedSet that start with a specific substring.
   */
  public Set getPropertyKeys(String startsWith) {
    OrderedSet keys = new OrderedSet();
    for (Iterator i = this.keyIterator(); i.hasNext();) {
      String k = (String) i.next(); // assume keys can only be Strings
      if (StringTools.startsWithIgnoreCase(k, startsWith)) {
        keys.add(k);
      }
    }
    return keys;
  }

  /**
   * Returns a subset of the current set of properties where keys starts with a specific substring.
   * 
   * @param keyStartsWith A substring that identifies which properties to return.
   * @return A subset of the current set of properties where keys starts with a specific substring.
   */
  public RTProperties getSubset(String keyStartsWith) {
    RTProperties rtp = new RTProperties();
    for (Iterator i = this.keyIterator(); i.hasNext();) {
      Object k = i.next();
      if (k instanceof String) {
        String ks = (String) k;
        if (StringTools.startsWithIgnoreCase(ks, keyStartsWith)) {
          String v = this.getString(ks, null);
          rtp.setProperty(ks, v);
        }
      }
    }
    return rtp;
  }

  /**
   * Extracts a Map containing a group of key/values from the runtime configuration.
   * 
   * @param keyEnd A substring that identifies which properties to extract.
   * @param valEnd The value to be set for the extracted properties.
   * @return The extracted from the runtime configuration Map containing a group of key/values.
   */
  public Map extractMap(String keyEnd, String valEnd) {
    Map m = new OrderedMap();
    for (Iterator i = this.keyIterator(); i.hasNext();) {
      String mkKey = (String) i.next();
      if (mkKey.endsWith(keyEnd)) {
        String key = getString(mkKey, null);
        if (key != null) { // <-- will never be null anyway
          String mvKey = mkKey.substring(0, mkKey.length() - keyEnd.length()) + valEnd;
          String val = this.getString(mvKey, "");
          m.put(key, val);
        }
      }
    }
    return m;
  }

  // ------------------------------------------------------------------------

  /**
   * Specifies whether a property exists in the list.
   * 
   * @param key The property to look for.
   * @return True if the property exists, False otherwise.
   */
  public boolean hasProperty(Object key) {
    return (key != null) ? this.getProperties().containsKey(key) : false;
  }

  // ------------------------------------------------------------------------

  /**
   * Sets the property to a value.
   * Adds new property if it is not there. Remove an old one if value is null.
   * 
   * @param key The property to set new value for.
   * @param value The value for the property.
   */
  public void setProperty(Object key, Object value) {
    if (key != null) {
      Map props = this.getProperties();

      /* "!<key>" implies removable of <key> from Map (value is ignored) */
      String k = (key instanceof String) ? (String) key : null;
      if ((k != null) && !k.equals("") && ("|!^".indexOf(k.charAt(0)) >= 0)) {
        key = k.substring(1);
        value = null;
      }

      /* add/remove key/value */
      if (!(props instanceof Properties) || (key instanceof String)) {
        if (value == null) {
          //Print._println("Removing key: " + key);
          props.remove(key);
        }
        else if ((props instanceof OrderedMap) && key.equals(KEY_NAME)) {
          //Print._println("Setting name: " + value);
          ((OrderedMap) props).put(0, key, value);
        }
        else {
          //Print._println("Setting key: " + key + "=" + value);
          props.put(key, value);
        }
      }

    }
  }

  // ------------------------------------------------------------------------

  /**
   * Extracts properties from an existing RTProperties instance and add/overwrite them to the 
   * current list. The name of the set is not extracted.
   *  
   * @param rtp An existing RTProperties instance.
   * @return The name of the current set of the properties.
   */
  public String setProperties(RTProperties rtp) {
    return this.setProperties(rtp, false);
  }

  /**
   * Extracts properties from an existing RTProperties instance and add/overwrite them to the
   * current list.
   *   
   * @param rtp An existing RTProperties instance.
   * @param inclName Specifies to whether extract the name or not.
   * @return The name of the current set of the properties.
   */
  public String setProperties(RTProperties rtp, boolean inclName) {
    if (rtp != null) {
      return this.setProperties(rtp.getProperties(), inclName);
    }
    else {
      return null;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Extracts properties from an existing map of properties and add/overwrite them to the
   * current list. The name is not extracted.
   * 
   * @param props An existing map of properties.
   * @return The name of the current set of the properties.
   */
  public String setProperties(Map props) {
    return this.setProperties(props, false);
  }

  /**
   * Extracts properties from an existing map of properties and add/overwrite them to the
   * current list.
   * 
   * @param props An existing map of properties.
   * @param saveName Specifies to whether extract the name or not.
   * @return The name of the current set of the properties.
   */
  public String setProperties(Map props, boolean saveName) {
    // Note: Does NOT remove old properties (by design)
    if (props != null) {
      String n = null;
      for (Iterator i = props.keySet().iterator(); i.hasNext();) {
        Object key = i.next();
        Object val = props.get(key);
        if (KEY_NAME.equals(key)) {
          n = (val != null) ? val.toString() : null;
          if (saveName) {
            this.setName(n);
          }
        }
        else {
          this.setProperty(key, val);
        }
      }
      return n;
    }
    else {
      return null;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Extracts properties from a string of properties and add/overwrite them to the
   * current list. The name is not extracted.
   * 
   * @param props A string of properties.
   * @return The name of the current set of the properties.
   */
  public String setProperties(String props) {
    return this.setProperties(props, false);
  }

  /**
   * Extracts properties from a string of properties and add/overwrite them to the
   * current list.
   * 
   * @param props A string of properties.
   * @param saveName Specifies to whether extract the name or not.
   * @return The name of the current set of the properties.
   */
  public String setProperties(String props, boolean saveName) {
    if (props != null) {

      /* check for prefixing name in string (ie. "name: key=value") */
      String n = null, p = props.trim();
      for (int i = 0; i < props.length(); i++) {
        char ch = props.charAt(i);
        if (ch == NameSeparatorChar) {
          // name separator found
          n = p.substring(0, i);
          p = p.substring(i + 1).trim();
          break;
        }
        else if (Character.isWhitespace(ch) || (ch == KeyValSeparatorChar)) {
          break;
        }
      }

      /* parse and set properties */
      Map propMap = StringTools.parseProperties(p);
      if (n == null) {
        n = this.setProperties(propMap, saveName);
      }
      else {
        this.setProperties(propMap, false);
        if (saveName) {
          this.setName(n);
        }
      }

      /* return name, if any */
      return n;

    }
    else {

      return null;

    }
  }

  // ------------------------------------------------------------------------

  /**
   * Removes a property from the list.
   * 
   * @param key The property to be removed.
   */
  public void removeProperty(Object key) {
    if (key != null) {
      Map props = this.getProperties();
      if (!(props instanceof Properties) || (key instanceof String)) {
        props.remove(key);
      }
    }
  }

  /**
   * Removes all properties from the list.
   */
  public void clearProperties() {
    this.getProperties().clear();
  }

  /**
   * Removes all existing properties and copies properties from a property map over.
   * 
   * @param props An existing property map.
   */
  public void resetProperties(Map props) {
    this.clearProperties();
    this.setProperties(props, true);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the value of the specified property.
   * 
   * @param key The property to look for.
   * @param dft A draft object that specifies the class of the return value.
   * @return The value of the specified property. 
   */
  public Object getProperty(Object key, Object dft) {
    Object value = this.getProperties().get(key);
    if (value != null) {
      if (dft != null) {
        // convert 'value' to same type (class) as 'dft' (if specified)
        try {
          return convertToType(value, dft.getClass());
        }
        catch (Throwable t) {
          return dft; // inconvertable
        }
      }
      else {
        // return value as-is
        return value;
      }
    }
    else {
      return dft;
    }
  }

  /**
   * Converts an object to a specified type.
   * 
   * @param val The object to be converted.
   * @param type The type to be converted to.
   * @return The converted to the new type object.
   * @throws Throwable If the object cannot be converted.
   */
  protected static Object convertToType(Object val, Class type) throws Throwable {
    if ((type == null) || (val == null)) {
      return val;
    }
    if (type.isAssignableFrom(val.getClass())) {
      return val;
    }
    else if (type == String.class) {
      return val.toString();
    }
    else {
      try {
        Constructor meth = type.getConstructor(new Class[] { type });
        return meth.newInstance(new Object[] { val });
      }
      catch (Throwable t1) {
        try {
          Constructor meth = type.getConstructor(new Class[] { String.class });
          return meth.newInstance(new Object[] { val.toString() });
        }
        catch (Throwable t2) {
          Print.logError("Can't convert value to " + type.getName() + ": " + val);
          throw t2; // inconvertable
        }
      }
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns a string representation of the specified property's value.
   * 
   * @param key The property to look for.
   * @return The string representation of the specified property's value.
   */
  public String getString(String key) {
    return this.getString(key, null);
  }

  /**
   * Returns a string representation of the specified property's value.
   * 
   * @param key The property to look for.
   * @param dft The value to return in case the property's value is null.
   * @return The string representation of the specified property's value.
   */
  public String getString(String key, String dft) {
    Object val = this.getProperty(key, null);
    if (val == null) {
      return dft;
    }
    else if (val.equals(RTKey.NULL_VALUE)) {
      return null;
    }
    else {
      return val.toString();
    }
  }

  /**
   * Sets the value of the specified property.
   * 
   * @param property The property for which value will be changed.
   * @param value The new value.
   */
  public void setString(String property, String value) {
    this.setProperty(property, value);
  }

  // ------------------------------------------------------------------------
  /**
   * Returns the value of the specified property as an array of string.
   * This is for the case when the value is a list of words separated by comma.
   * 
   * @param key The property to look for.
   * @return The value of the specified property as an array of string.
   */
  public String[] getStringArray(String key) {
    return this.getStringArray(key, null);
  }

  /**
   * Returns the value of the specified property as an array of string.
   * This is for the case when the value is a list of words separated by comma.
   * 
   * @param key The property to look for.
   * @param dft An array of string to to be returned if the property's value is null.
   * @return The value of the specified property as an array of string.
   */
  public String[] getStringArray(String key, String dft[]) {
    String val = this.getString(key, null);
    return (val != null) ? StringTools.parseArray(val) : dft;
  }

  /**
   * Assigns the specified property to a value.
   * 
   * @param key The property to be changed.
   * @param val An array representation of the value.
   */
  public void setStringArray(String key, String val[]) {
    this.setStringArray(key, val, true);
  }

  /**
   * Assigns the specified property to a value.
   * 
   * @param key The property to be changed.
   * @param val An array representation of the value.
   * @param alwaysQuote Specifies to whether quote the values or not.
   */
  public void setStringArray(String key, String val[], boolean alwaysQuote) {
    String valStr = StringTools.encodeArray(val, alwaysQuote);
    this.setString(key, valStr);
  }

  /**
   * Assigns the specified property to a value.
   * Always quote the values.
   * 
   * @param key The property to be changed.
   * @param val An array representation of the value.
   */
  public void setProperty(String key, String val[]) {
    this.setStringArray(key, val, true);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns value of the specified property if its a file.
   * Creates a new file if the value is not of type file
   * Returns null otherwise if value is null.
   * 
   * @param key The property to look for
   * @return The value of the specified property if its a file.
   */
  public File getFile(String key) {
    return this.getFile(key, null);
  }

  // do not include the following method, otherwise "getFile(file, null)" would be ambiguous
  //public File getFile(String key, String dft)

  /**
   * Returns value of the specified property if its a file.
   * Creates a new file if the value is not of type file.
   * Returns dft otherwise if value is null.
   * 
   * @param key The property to look for
   * @param dft The file to return if value is null.
   * @return The value of the specified property if its a file.
   */
  public File getFile(String key, File dft) {
    Object val = this.getProperty(key, null);
    if (val == null) {
      return dft;
    }
    else if (val instanceof File) {
      return (File) val;
    }
    else {
      return new File(val.toString());
    }
  }

  /**
   * Sets the value of the specified property.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setFile(String key, File value) {
    this.setProperty(key, value);
  }

  // ------------------------------------------------------------------------
  /**
   * Returns value of the specified property if its of type double.
   * Returns null otherwise.
   * 
   * @param key The property to look for
   * @return The value of the specified property if its of type double.
   */
  public double getDouble(String key) {
    return this.getDouble(key, 0.0);
  }

  /**
   * Returns value of the specified property if its of type double.
   * Returns dft otherwise.
   * 
   * @param key The property to look for
   * @param dft The value of type double to return if value is null.
   * @return The value of the specified property if its of type double.
   */
  public double getDouble(String key, double dft) {
    Object val = this.getProperty(key, null);
    if (val == null) {
      return dft;
    }
    else if (val instanceof Number) {
      return ((Number) val).doubleValue();
    }
    else {
      return StringTools.parseDouble(val.toString(), dft);
    }
  }

  /**
   * Sets the value of the specified property to a double number.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setDouble(String key, double value) {
    this.setProperty(key, value);
  }

  /**
   * Sets the value of the specified property to a double number.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setProperty(String key, double value) {
    this.setProperty(key, new Double(value));
  }

  // ------------------------------------------------------------------------
  /**
   * Returns value of the specified property if its of type float.
   * Returns null otherwise.
   * 
   * @param key The property to look for
   * @return The value of the specified property if its of type float.
   */
  public float getFloat(String key) {
    return this.getFloat(key, 0.0F);
  }

  /**
   * Returns value of the specified property if its of type float.
   * Returns dft otherwise.
   * 
   * @param key The property to look for
   * @param dft The value of type float to return if value is null.
   * @return The value of the specified property if its of type float.
   */
  public float getFloat(String key, float dft) {
    Object val = this.getProperty(key, null);
    if (val == null) {
      return dft;
    }
    else if (val instanceof Number) {
      return ((Number) val).floatValue();
    }
    else {
      return StringTools.parseFloat(val.toString(), dft);
    }
  }

  /**
   * Sets the value of the specified property to a float number.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setFloat(String key, float value) {
    this.setProperty(key, value);
  }

  /**
   * Sets the value of the specified property to a float number.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setProperty(String key, float value) {
    this.setProperty(key, new Float(value));
  }

  // ------------------------------------------------------------------------

  /**
   * Returns value of the specified property if its of type long.
   * Returns null otherwise.
   * 
   * @param key The property to look for
   * @return The value of the specified property if its of type long.
   */
  public long getLong(String key) {
    return this.getLong(key, 0L);
  }

  /**
   * Returns value of the specified property if its of type long.
   * Returns dft otherwise.
   * 
   * @param key The property to look for
   * @param dft The value of type long to return if value is null.
   * @return The value of the specified property if its of type long.
   */
  public long getLong(String key, long dft) {
    Object val = this.getProperty(key, null);
    if (val == null) {
      return dft;
    }
    else if (val instanceof Number) {
      return ((Number) val).longValue();
    }
    else {
      return StringTools.parseLong(val.toString(), dft);
    }
  }

  /**
   * Sets the value of the specified property to a long number.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setLong(String key, long value) {
    this.setProperty(key, value);
  }

  /**
   * Sets the value of the specified property to a long number.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setProperty(String key, long value) {
    this.setProperty(key, new Long(value));
  }

  // ------------------------------------------------------------------------
  /**
   * Returns value of the specified property if its of type int.
   * Returns null otherwise.
   * 
   * @param key The property to look for
   * @return The value of the specified property if its of type int.
   */
  public int getInt(String key) {
    return this.getInt(key, 0);
  }

  /**
   * Returns value of the specified property if its of type int.
   * Returns dft otherwise.
   * 
   * @param key The property to look for
   * @param dft The value of type int to return if value is null.
   * @return The value of the specified property if its of type int.
   */
  public int getInt(String key, int dft) {
    Object val = this.getProperty(key, null);
    if (val == null) {
      return dft;
    }
    else if (val instanceof Number) {
      return ((Number) val).intValue();
    }
    else {
      return StringTools.parseInt(val.toString(), dft);
    }
  }

  /**
   * Sets the value of the specified property to a int number.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setInt(String key, int value) {
    this.setProperty(key, value);
  }

  /**
   * Sets the value of the specified property to a int number.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setProperty(String key, int value) {
    this.setProperty(key, new Integer(value));
  }

  // ------------------------------------------------------------------------
  /**
   * Returns value of the specified property if its of type boolean.
   * Returns null otherwise.
   * 
   * @param key The property to look for
   * @return The value of the specified property if its of type boolean.
   */
  public boolean getBoolean(String key) {
    boolean dft = false;
    return this._getBoolean(key, dft, true);
  }

  /**
   * Returns value of the specified property if its of type boolean.
   * Returns dft otherwise.
   * 
   * @param key The property to look for
   * @param dft The value of type boolean to return if value is null.
   * @return The value of the specified property if its of type boolean.
   */
  public boolean getBoolean(String key, boolean dft) {
    return this._getBoolean(key, dft, DEFAULT_TRUE_IF_BOOLEAN_STRING_EMPTY);
  }

  /**
   * Returns value of the specified property if its of type boolean.
   * Returns dft otherwise.
   * 
   * @param key The property to look for
   * @param dft The value of type boolean to return if value is null.
   * @param dftTrueIfEmpty Specifies to whether return true or dft if the value is "" 
   * @return The value of the specified property if its of type boolean.
   */
  private boolean _getBoolean(String key, boolean dft, boolean dftTrueIfEmpty) {
    Object val = this.getProperty(key, null);
    if (val == null) {
      return dft;
    }
    else if (val instanceof Boolean) {
      return ((Boolean) val).booleanValue();
    }
    else if (val.toString().equals("")) {
      return dftTrueIfEmpty ? true : dft;
    }
    else {
      return StringTools.parseBoolean(val.toString(), dft);
    }
  }

  /**
   * Sets the value of the specified property to a boolean number.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setBoolean(String key, boolean value) {
    this.setProperty(key, value);
  }

  /**
   * Sets the value of the specified property to a boolean number.
   * 
   * @param key The property to look for.
   * @param value The new value.
   */
  public void setProperty(String key, boolean value) {
    this.setProperty(key, new Boolean(value));
  }

  // ------------------------------------------------------------------------

  /**
   * Prints out the list of all properties.
   */
  public void printProperties() {
    this.printProperties(null, null);
  }

  /**
   * Prints out the list of all properties excluding the specific ones.
   * 
   * @param exclProps Properties to be excluded.
   */
  public void printProperties(RTProperties exclProps) {
    this.printProperties(exclProps, null);
  }

  /**
   * Prints out the list of all properties with specified ordering.
   * 
   * @param orderBy The order for the properties to be sorted by.
   */
  public void printProperties(Collection orderBy) {
    this.printProperties(null, orderBy);
  }

  /**
   * Prints out the list of properties excluding the specific ones.
   * 
   * @param exclProps Properties to be excluded.
   * @param orderBy The order for the properties to be sorted by.
   */
  public void printProperties(RTProperties exclProps, Collection orderBy) {
    Print.logInfo(this.toString(exclProps, orderBy, true));
  }

  // ------------------------------------------------------------------------

  /**
   * Implements the equals function.
   * Ordering is ignored.
   * 
   * @param other The object to be compared to.
   * @return True if objects are equal, False otherwise.
   */
  public boolean equals(Object other) {
    if (other instanceof RTProperties) {
      // We need to perform our own 'equals' checking here:
      // Two RTProperties are equal if they contain the same properties irrespective of ordering. 
      // [All property values are compared as Strings]
      RTProperties rtp = (RTProperties) other;
      Map M1 = this.getProperties();
      Map M2 = rtp.getProperties();
      if (M1.size() == M2.size()) {
        for (Iterator i = M1.keySet().iterator(); i.hasNext();) {
          Object key = i.next();
          if (M2.containsKey(key)) {
            Object m1Val = M1.get(key);
            Object m2Val = M2.get(key);
            String m1ValStr = (m1Val != null) ? m1Val.toString() : null;
            String m2ValStr = (m2Val != null) ? m2Val.toString() : null;
            if (m1Val == m2Val) {
              continue; // they are the same object (or both null)
            }
            else if ((m1ValStr != null) && m1ValStr.equals(m2ValStr)) {
              continue; // the values are equals
            }
            else {
              //Print.logInfo("Values not equal: " + m1ValStr + " <==> " + m2ValStr);
              return false; // values are not equal
            }
          }
          else {
            //Print.logInfo("Key doesn't exist in M2");
            return false; // key doesn't exist in M2
          }
        }
        return true; // all key/vals matched
      }
      else {
        //Print.logInfo("Sizes don't match");
        return false;
      }
    }
    else {
      return false;
    }
  }

  // ------------------------------------------------------------------------
  /**
   * Returns a string representation of all properties in the list.
   * All properties are on the same line. 
   * 
   * @return A string representation of properties in the list.
   */
  public String toString() {
    return this.toString(null, null, false);
  }

  /**
   * Returns a string representation of all properties in the list, excluding some specific ones.
   * All properties are on the same line. 
   * 
   * @param exclProps Properties to be excluded.
   * @return A string representation of properties in the list, excluding some specific ones.
   */
  public String toString(RTProperties exclProps) {
    return this.toString(exclProps, null, false);
  }

  /**
   * Returns a string representation of all properties in the list.
   * All properties are on the same line.
   * 
   * @param orderBy Order of the properties in the output string. 
   * @return A string representation of properties in the list.
   */
  public String toString(Collection orderBy) {
    return this.toString(null, orderBy, false);
  }

  /**
   * Returns a string representation of properties in the list, excluding some specific ones.
   * All properties are on the same line.
   * 
   * @param exclProps Properties to be excluded.
   * @param orderBy Order of the properties in the output string.
   * @return A string representation of properties in the list, excluding some specific ones.
   */
  public String toString(RTProperties exclProps, Collection orderBy) {
    return this.toString(null, orderBy, false);
  }

  /**
   * Returns a string representation of properties in the list, excluding some specific ones.
   * 
   * @param exclProps Properties to be excluded.
   * @param orderBy Order of the properties in the output string.
   * @param inclNewline Indicates to whether separate properties with new line character or not.
   * @return A string representation of properties in the list, excluding some specific ones.
   */
  public String toString(RTProperties exclProps, Collection orderBy, boolean inclNewline) {
    StringBuffer sb = new StringBuffer();

    /* append name */
    String n = this.getName();
    if (!n.equals("")) {
      sb.append(n).append(NameSeparatorChar).append(" ");
    }

    /* property maps */
    Map propMap = this.getProperties();
    Map exclMap = (exclProps != null) ? exclProps.getProperties() : null;

    /* order by */
    Set orderSet = null;
    if (orderBy != null) {
      orderSet = new OrderedSet(orderBy, true);
      orderSet.addAll(propMap.keySet());
      // 'orderSet' now contains the union of keys from 'orderBy' and 'propMap.keySet()'
    }
    else {
      orderSet = propMap.keySet();
    }

    /* encode properties */
    for (Iterator i = orderSet.iterator(); i.hasNext();) {
      Object keyObj = i.next(); // possible this key doesn't exist in 'propMap' if 'orderBy' used.
      if (!KEY_NAME.equals(keyObj) && propMap.containsKey(keyObj)) {
        Object valObj = propMap.get(keyObj); // key guaranteed here to be in 'propMap'
        if ((exclMap == null) || !RTProperties.compareMapValues(valObj, exclMap.get(keyObj))) {
          sb.append(keyObj.toString()).append(KeyValSeparatorChar);
          String v = (valObj != null) ? valObj.toString() : "";
          if ((v.indexOf(" ") >= 0) || (v.indexOf("\t") >= 0) || (v.indexOf("\"") >= 0)) {
            sb.append(StringTools.quoteString(v));
          }
          else {
            sb.append(v);
          }
          if (inclNewline) {
            sb.append("\n");
          }
          else if (i.hasNext()) {
            sb.append(" ");
          }
        }
        else {
          //Print.logDebug("Key hasn't changed: " + key);
        }
      }
    }
    return sb.toString().trim();

  }

  /**
   * Compares two objects.
   * Compares their string representation if the objects are not equal.
   * 
   * @param value First object.
   * @param target Second object.
   * @return True if objects or their string representations are equal.
   */
  private static boolean compareMapValues(Object value, Object target) {
    if ((value == null) && (target == null)) {
      return true;
    }
    else if ((value == null) || (target == null)) {
      return false;
    }
    else if (value.equals(target)) {
      return true;
    }
    else {
      return value.toString().equals(target.toString());
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Holds the ordered list runtime properties.
   */
  public static class OrderedProperties extends Properties {
    private OrderedMap orderedMap = null;

    /**
     * Creates a new OrderedProperties instance.
     */
    public OrderedProperties() {
      super();
      this.orderedMap = new OrderedMap();
    }

    /**
     * Inserts the property pair key/value into the list.
     * 
     * @param key The property name.
     * @param value The value for the property.
     * @return The name of the newly inserted property.
     */
    public Object put(Object key, Object value) {
      Object rtn = super.put(key, value);
      this.orderedMap.put(key, value);
      return rtn;
    }

    /**
     * Returns the ordered map of properties.
     * 
     * @return The ordered map of properties.
     */
    public OrderedMap getOrderedMap() {
      return this.orderedMap;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Runs from the command line.
   * 
   * @param argv Command line arguments.
   */
  public static void main(String argv[]) {
    RTConfig.setCommandLineArgs(argv);
  }

}
