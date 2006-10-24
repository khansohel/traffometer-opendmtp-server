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

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class MethodAction
    implements ActionListener, Runnable
{

    // ------------------------------------------------------------------------

    private Class               targetClass = null;
    private Object              target      = null;
    private AccessibleObject    method      = null;
    private Class               argClass[]  = null;
    private Object              args[]      = null;
    private Object              rtnValue    = null;
    private Throwable           error       = null;

    // ------------------------------------------------------------------------
    // static methods:
    //   target  : either a Class instance, or a class name String
    //   methName: static method name (null or "<init>" for constructor)
    //   argClass: static method arugment types
    //   args    : static method arguments
    // instance methods:
    //   target  : object instance
    //   methName: instance method name
    //   argClass: method arugment types (or null of no arguments)
    //   args    : method arguments (or null of no arguments)

    public MethodAction(Object targ, String methName)
        throws NoSuchMethodException, ClassNotFoundException
    {
        this(targ, methName, null, null);
    }

    public MethodAction(Object targ, String methName, Class argClass[])
        throws NoSuchMethodException, ClassNotFoundException
    {
        this(targ, methName, argClass, null);
    }

    public MethodAction(Object targ, String methName, Class argClass[], Object args[])
        throws NoSuchMethodException, ClassNotFoundException
    {
        this.target      = (targ instanceof String)? Class.forName((String)targ) : targ;
        this.targetClass = (this.target instanceof Class)? (Class)this.target : this.target.getClass();
        this.argClass    = (argClass != null)? argClass : new Class[0];
        if (methName == null) {
            this.method  = this.targetClass.getConstructor(this.argClass);
        } else
        if (methName.equals("<init>")) {
            this.method  = this.targetClass.getConstructor(this.argClass);
        } else {
            this.method  = this.targetClass.getMethod(methName, this.argClass);
        }
        this.setArgs(args);
    }

    // ------------------------------------------------------------------------

    public static MethodAction GetterMethod(Object targ, String fieldName)
        throws NoSuchMethodException, ClassNotFoundException
    {
        String mn = _beanMethodName("get", fieldName);
        return new MethodAction(targ, mn, null);
    }

    public static String InvokeGetter(Object targ, String fieldName)
        throws NoSuchMethodException, ClassNotFoundException, Throwable
    {
        return GetterMethod(targ, fieldName).invoke().toString();
    }

    public static MethodAction SetterMethod(Object targ, String fieldName)
        throws NoSuchMethodException, ClassNotFoundException
    {
        String mn = _beanMethodName("set", fieldName);
        return new MethodAction(targ, mn, new Class[] { String.class });
    }

    public static void InvokeSetter(Object targ, String fieldName, String value)
        throws NoSuchMethodException, ClassNotFoundException, Throwable
    {
        SetterMethod(targ, fieldName).invoke(new Object[] { value });
    }

    protected static String _beanMethodName(String prefix, String fieldName)
    {
        StringBuffer sb = new StringBuffer(prefix);
        sb.append(fieldName.substring(0,1).toUpperCase());
        sb.append(fieldName.substring(1));
        return sb.toString();
    }

    // ------------------------------------------------------------------------

    public Object getTarget()
    {
        return this.target;
    }

    public void setArgs(Object args[])
    {
        this.args = args;
    }

    public Object[] getArgs()
    {
        return this.args;
    }

    // ------------------------------------------------------------------------

    public Object invoke(Object args[])
        throws Throwable
    {
        this.setArgs(args);
        return this.invoke();
    }

    public Object invoke()
        throws Throwable
    {
        this.error = null;
        this.rtnValue = null;
        try {
            if (this.method instanceof Constructor) {
                this.rtnValue = ((Constructor)this.method).newInstance(this.getArgs());
            } else
            if (this.method instanceof Method) {
                this.rtnValue = ((Method)this.method).invoke(this.getTarget(), this.getArgs());
            }
            return this.rtnValue;
        } catch (InvocationTargetException ite) {
            this.error = ite.getCause();
            if (this.error == null) { this.error = ite; }
            throw this.error;
        } catch (Throwable t) { // trap any remaining method invocation error
            this.error = t;
            throw this.error;
        }
    }

    public Object getReturnValue()
    {
        return this.rtnValue;
    }

    // ------------------------------------------------------------------------
    
    public void invokeDelayed(int delayMillis)
    {
        javax.swing.Timer delay = new javax.swing.Timer(delayMillis, this);
        delay.setRepeats(false);
        delay.start();
    }

    // ------------------------------------------------------------------------

    public static void invokeLater(final ActionListener al, final ActionEvent ae)
    {
        MethodAction.invokeLater(new Runnable() { public void run() {al.actionPerformed(ae);} });
    }
    
    public static void invokeLater(Runnable r)
    {
        Toolkit.getDefaultToolkit().getSystemEventQueue().invokeLater(r);
    }

    public static void invokeAndWait(Runnable r)
        throws InterruptedException, InvocationTargetException
    {
        // call from a child thread only!
        Toolkit.getDefaultToolkit().getSystemEventQueue().invokeAndWait(r);
    }

    public void invokeLater()
    {
        MethodAction.invokeLater(this);
    }

    public void invokeAndWait()
        throws InterruptedException, InvocationTargetException
    {
        MethodAction.invokeAndWait(this);
    }

    public void run()
    {
        try {
            this.invoke();
        } catch (Throwable t) { // trap any method invocation error
            Print.logError("'invoke' error " + t);
        }
    }

    public void actionPerformed(ActionEvent ae)
    {
        try {
            this.invoke();
        } catch (Throwable t) { // trap any method invocation error
            Print.logError("'invoke' error " + t);
        }
    }

}
