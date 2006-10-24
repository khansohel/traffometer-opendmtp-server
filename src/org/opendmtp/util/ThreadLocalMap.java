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

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.awt.*;
import java.awt.color.*;

public class ThreadLocalMap
    extends ThreadLocal
    implements Map
{
    
    // ------------------------------------------------------------------------

    private static final Class DefaultMapClass = Hashtable.class;
    
    // ------------------------------------------------------------------------

    private Class mapClass = null;
    
    public ThreadLocalMap() 
    {
        this(null);
    }

    public ThreadLocalMap(Class mapClass) 
    {
        super();
        this.setMapClass(mapClass);
    }
    
    // ------------------------------------------------------------------------

    public Class getMapClass() 
    {
        return this.mapClass;
    }
    
    public void setMapClass(Class map)
    {
        // Should check that 'mapClass' implements 'Map' (just assume for now)
        this.mapClass = (mapClass != null)? mapClass : DefaultMapClass;
    }
    
    // ------------------------------------------------------------------------
    // ThreadLocal override
    
    protected Object initialValue() 
    {
        Class mc = this.getMapClass();
        try {
            return (Map)mc.newInstance();
        } catch (Throwable t) {
            // Give up and try a Hashtable
            Print.logException("Error instantiating: " + mc, t);
            return new Hashtable();
        }
    }
    
    protected Map getMap()
    {
        return (Map)this.get();
    }
    
    // ------------------------------------------------------------------------
    // Map interface
    
    public void clear()
    {
        this.getMap().clear();
    }
    
    public boolean containsKey(Object key)
    {
        return this.getMap().containsKey(key);
    }
    
    public boolean containsValue(Object value)
    {
        return this.getMap().containsValue(value);
    }
    
    public Set entrySet()
    {
        return this.getMap().entrySet();
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof ThreadLocalMap) {
            return this.getMap().equals(((ThreadLocalMap)o).getMap());
        } else {
            return false;
        }
    }
    
    public Object get(Object key)
    {
        return this.getMap().get(key);
    }
    
    public int hashCode()
    {
        return this.getMap().hashCode();
    }
    
    public boolean isEmpty()
    {
        return this.getMap().isEmpty();
    }
    
    public Set keySet()
    {
        return this.getMap().keySet();
    }
    
    public Object put(Object key, Object value)
    {
        return this.getMap().put(key, value);
    }
    
    public void putAll(Map t)
    {
        this.getMap().putAll(t);
    }
    
    public Object remove(Object key)
    {
        return this.getMap().remove(key);
    }
    
    public int size()
    {
        return this.getMap().size();
    }
    
    public Collection values()
    {
        return this.getMap().values();
    }
    
}
