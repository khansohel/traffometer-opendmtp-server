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
//  Java reflection convenience class
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class provides a way to invoke an object's methods by name.  This object can even be used
 * to run the named method of the target object in a new thread.
 * @author Martin D. Flynn
 * @author Mark Stillwell
 */
public class MethodAction implements ActionListener, Runnable {

  // ------------------------------------------------------------------------

  private Class targetClass = null;
  private Object target = null;
  private AccessibleObject method = null;
  private Class argClass[] = null;
  private Object args[] = null;
  private Object rtnValue = null;
  private Throwable error = null;

  // ------------------------------------------------------------------------
  // static methods:
  // target : either a Class instance, or a class name String
  // methName: static method name (null or "<init>" for constructor)
  // argClass: static method arugment types
  // args : static method arguments
  // instance methods:
  // target : object instance
  // methName: instance method name
  // argClass: method arugment types (or null of no arguments)
  // args : method arguments (or null of no arguments)

  /**
   * Creates a new MethodAction object that can be used to run the method of the given name of the 
   * given object in a new thread.
   * @param targ an object
   * @param methName the name of a method of the given object
   * @throws ClassNotFoundException if the class of the given object can not be found
   * @throws NoSuchMethodException if the given class does not have a method with the given name
   */
  public MethodAction(Object targ, String methName) 
      throws NoSuchMethodException, ClassNotFoundException {
    this(targ, methName, null, null);
  }

  /**
   * Creates a new MethodAction object that can be used to run the method with the given name and
   * argument types of the given object in a new thread.
   * @param targ an object
   * @param methName the name of a method of the given object
   * @param argClass an array of the argument types of the given method
   * @throws ClassNotFoundException if the class of the given object can not be found
   * @throws NoSuchMethodException if the given class does not have a method with the given name
   */  
  public MethodAction(Object targ, String methName, Class argClass[])
      throws NoSuchMethodException, ClassNotFoundException {
    this(targ, methName, argClass, null);
  }

  /**
   * Creates a new MethodAction object that can be used to run the method with the given name and 
   * argument types of the given object using arguments of the given values in a new thread.
   * @param targ an object
   * @param methName the name of a method of the given object
   * @param argClass an array of the argument types of the method
   * @param args an array of argument values to use when invoking the method
   * @throws ClassNotFoundException if the class of the given object can not be found
   * @throws NoSuchMethodException if the given class does not have a method with the given name
   */  
  public MethodAction(Object targ, String methName, Class argClass[], Object args[])
      throws NoSuchMethodException, ClassNotFoundException {
    this.target = (targ instanceof String) ? Class.forName((String) targ) : targ;
    this.targetClass = 
        (this.target instanceof Class) ? (Class) this.target : this.target.getClass();
    this.argClass = (argClass != null) ? argClass : new Class[0];
    if (methName == null) {
      this.method = this.targetClass.getConstructor(this.argClass);
    }
    else if (methName.equals("<init>")) {
      this.method = this.targetClass.getConstructor(this.argClass);
    }
    else {
      this.method = this.targetClass.getMethod(methName, this.argClass);
    }
    this.setArgs(args);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns a MethodAction object that can by used to run the get method for the field with
   * the given name of the given object.
   * @param targ an object with a field of the given name
   * @param fieldName the name of a field
   * @return a MethodAction object
   * @throws ClassNotFoundException if the class of the given object can not be found
   * @throws NoSuchMethodException if the given class does not have a method with the given name
\   */
  public static MethodAction GetterMethod(Object targ, String fieldName)
      throws NoSuchMethodException, ClassNotFoundException {
    String mn = _beanMethodName("get", fieldName);
    return new MethodAction(targ, mn, null);
  }

  /**
   * Returns a string derived from the return value of the get method of the field with the
   * given name in the given object.
   * @param targ an object
   * @param fieldName the name of a field of the given object
   * @return a string derived from the value of the field of the given object with the given name
   * @throws ClassNotFoundException if the class of the given object can not be found
   * @throws NoSuchMethodException if the object does not have an accessor method for the field
   * @throws Throwable if the invoked accessor method throws something
   */
  public static String InvokeGetter(Object targ, String fieldName) 
      throws NoSuchMethodException, ClassNotFoundException, Throwable {
    return GetterMethod(targ, fieldName).invoke().toString();
  }

  /**
   * Returns a MethodAction object that can by used to run the set method for the field with
   * the given name of the given object.
   * @param targ an object with a field of the given name
   * @param fieldName the name of a field
   * @return a MethodAction object
   * @throws ClassNotFoundException if the class of the given object can not be found
   * @throws NoSuchMethodException if the given class does not have a method with the given name
   */
  public static MethodAction SetterMethod(Object targ, String fieldName)
      throws NoSuchMethodException, ClassNotFoundException {
    String mn = _beanMethodName("set", fieldName);
    return new MethodAction(targ, mn, new Class[] { String.class });
  }

  /**
   * Invokes the set method of the field with the given name in the given object, passing a string
   * representing the value to set.
   * @param targ an object
   * @param fieldName the name of a field of the given object
   * @param value a string representing the value to set the field with the given name to
   * @throws ClassNotFoundException if the class of the given object can not be found
   * @throws NoSuchMethodException if the object does not have an accessor method for the field
   * @throws Throwable if the invoked accessor method throws something
   */
  public static void InvokeSetter(Object targ, String fieldName, String value)
      throws NoSuchMethodException, ClassNotFoundException, Throwable {
    SetterMethod(targ, fieldName).invoke(new Object[] { value });
  }

  /**
   * Returns a string created by capitalizing the first letter of fieldName appending it to prefix.
   * If prefix is "get" or "set" and fieldName is the name of a private field of a javabean, then 
   * the return value is the name of a standard javabean accessor method. 
   * @param prefix the string prefix
   * @param fieldName the name of the field of a javabean
   * @return the name of an accessor method for the field with the given name
   */
  protected static String _beanMethodName(String prefix, String fieldName) {
    StringBuffer sb = new StringBuffer(prefix);
    sb.append(fieldName.substring(0, 1).toUpperCase());
    sb.append(fieldName.substring(1));
    return sb.toString();
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the target object of this MethodAction object.
   * @return the target object
   */
  public Object getTarget() {
    return this.target;
  }

  /**
   * Sets the arguments to the method of the target object which can be invoked by this 
   * MethodAction object.
   * @param args an array of argument values
   */
  public void setArgs(Object args[]) {
    this.args = args;
  }

  /**
   * Gets the current arguments to the method of the target object which can be invoked by this 
   * MethodAction object.
   * @return an array of argument values
   */
  public Object[] getArgs() {
    return this.args;
  }

  // ------------------------------------------------------------------------

  /**
   * Invokes the method of the target object which can be invoked by this MethodAction object using
   * the given arguments.
   * @param args an array of argument values
   * @return the return value of the target method of the target object
   * @throws Throwable if the invoked method throws something
   */
  public Object invoke(Object args[]) throws Throwable {
    this.setArgs(args);
    return this.invoke();
  }

  /**
   * Invokes the method of the target object which can be invoked by this MethodAction object.
   * @return the return value of the invoked method
   * @throws Throwable if the invoked method throws something
   */
  public Object invoke() throws Throwable {
    this.error = null;
    this.rtnValue = null;
    try {
      if (this.method instanceof Constructor) {
        this.rtnValue = ((Constructor) this.method).newInstance(this.getArgs());
      }
      else if (this.method instanceof Method) {
        this.rtnValue = ((Method) this.method).invoke(this.getTarget(), this.getArgs());
      }
      return this.rtnValue;
    }
    catch (InvocationTargetException ite) {
      this.error = ite.getCause();
      if (this.error == null) {
        this.error = ite;
      }
      throw this.error;
    }
    catch (Throwable t) { // trap any remaining method invocation error
      this.error = t;
      throw this.error;
    }
  }

  /**
   * Returns the last return value of the invoked method of the target object. Returns null if the
   * method has not been invoked.   
   * @return the return value of the invoked method
   */
  public Object getReturnValue() {
    return this.rtnValue;
  }

  // ------------------------------------------------------------------------

  /**
   * Invokes the target method of the target object after the given number of milliseconds.
   * @param delayMillis the number of milliseconds to delay
   */
  public void invokeDelayed(int delayMillis) {
    javax.swing.Timer delay = new javax.swing.Timer(delayMillis, this);
    delay.setRepeats(false);
    delay.start();
  }

  // ------------------------------------------------------------------------

  /**
   * Causes the actionPerformed method of the given ActionListener to be called in the dispatch
   * thread of the EventQueue.  This will happen after all pending events are processed.
   * @param al an ActionListener object whose actionPerformed method will be invoked
   * @param ae an ActionEvent to pass to the actionPerformed method of the ActionListener 
   */
  public static void invokeLater(final ActionListener al, final ActionEvent ae) {
    MethodAction.invokeLater(new Runnable() {
      public void run() {
        al.actionPerformed(ae);
      }
    });
  }

  /**
   * Causes runnable to have its run method called in the dispatch thread of the EventQueue. 
   * This will happen after all pending events are processed.
   * @param r a runnable object
   */
  public static void invokeLater(Runnable r) {
    Toolkit.getDefaultToolkit().getSystemEventQueue().invokeLater(r);
  }

  /**
   * Causes runnable to have its run  method called in the dispatch thread of the EventQueue. 
   * This will happen after all pending events are processed.  This call blocks until this has 
   * happened.  This method will throw an Error if called from the event dispatcher thread.
   * @param r a runnable object
   * @throws InterruptedException if another thread has interrupted this thread
   * @throws InvocationTargetException if a throwable is thrown while running runnable
   */
  public static void invokeAndWait(Runnable r) 
      throws InterruptedException, InvocationTargetException {
    // call from a child thread only!
    Toolkit.getDefaultToolkit().getSystemEventQueue().invokeAndWait(r);
  }

  /**
   * Causes this object to have its run  method called in the dispatch thread of the EventQueue. 
   */
  public void invokeLater() {
    MethodAction.invokeLater(this);
  }

  /**
   * Causes this object to have its run  method called in the dispatch thread of the EventQueue. 
   * This will happen after all pending events are processed.  This call blocks until this has 
   * happened.  This method will throw an Error if called from the event dispatcher thread.
   * @throws InterruptedException if another thread has interrupted this thread
   * @throws InvocationTargetException if a throwable is thrown while running runnable
   */
  public void invokeAndWait() throws InterruptedException, InvocationTargetException {
    MethodAction.invokeAndWait(this);
  }

  /**
   * Invokes the target method of the target object when a Thread runs this object.
   */
  public void run() {
    try {
      this.invoke();
    }
    catch (Throwable t) { // trap any method invocation error
      Print.logError("'invoke' error " + t);
    }
  }

  /**
   * Invokes the target method of the target object when an action occurs for which this
   * object is registered as an ActionListener.
   * @param ae the ActionEvent which caused this method to be invoked
   */
  public void actionPerformed(ActionEvent ae) {
    try {
      this.invoke();
    }
    catch (Throwable t) { // trap any method invocation error
      Print.logError("'invoke' error " + t);
    }
  }

}
