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
//  This class provides many List/Collection/Array based utilities.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Provides many List/Collection/Array based utilities.
 * 
 * @author Martin D. Flynn
 * 
 */
public class ListTools 
{

	// ------------------------------------------------------------------------

	/**
	 * Calculates the value for the offset constrained by length.
	 * @param ofs The offset value.
	 * @param length The length value.
	 * @return The constrained value of the offset (either less than length or zero).
	 */
	private static int _constrainOffset(int ofs, int length) 
	{
		if ((ofs < 0) || (length <= 0)) 
		{
			return 0;
		}
		else if (ofs >= length) 
		{
			return length - 1;
		}
		else 
		{
			return ofs;
		}
	}

	/**
	 * Calculates the value of length constrained by the parameters.
	 * @param ofs The offset value.
	 * @param len A value of length.
	 * @param length The length value being constrained.
	 * @return The constrained value for length. 
	 */
	private static int _constrainLength(int ofs, int len, int length) 
	{
		if (len < 0) 
		{
			return length;
		}
		else if (len > (length - ofs)) 
		{
			return length - ofs;
		}
		else 
		{
			return len;
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Converts an Object array to a list.
	 * @param a The Object array.
	 * @return The list corresponding to the Object array.
	 */
	public static java.util.List toList(Object a[]) 
	{
		return ListTools.toList(a, null);
	}

	/**
	 * Converts an Object array to a list.
	 * @param a The object array to be converted.
	 * @param list Either null or the list ot have the Object array inserted into.
	 * @return The list corresponding to the Object array.
	 */
	public static java.util.List toList(Object a[], java.util.List list) 
	{
		return ListTools.toList(a, 0, -1, list);
	}

	/**
	 * Converts an Object array to a list. 
	 * @param a The object array to be converted.
	 * @param ofs An offset value for the Object array.
	 * @param len A length value for the Object array.
	 * @param list Either null or the list to have the Object array contents inserted into.
	 * @return The list corresponding to the Object array limited by the parameters for offset and length.
	 */
	public static java.util.List toList(Object a[], int ofs, int len, java.util.List list) 
	{
		java.util.List v = (list != null) ? list : new Vector();
		int alen = (a != null) ? a.length : 0;
		ofs = _constrainOffset(ofs, alen);
		len = _constrainLength(ofs, len, alen);
		for (int i = ofs; i < len; i++) 
		{
			v.add(a[i]);
		}
		return v;
	}

	// ------------------------------------------------------------------------

	/**
	 * Converts an enumeration to a list.
	 * @param e The enumeration to be converted. 
	 * @return The list resulting from the conversion of the enumeration.
	 */
	public static java.util.List toList(Enumeration e) 
	{
		return ListTools.toList(e, null);
	}

	/**
	 * Converts an enumeration to a list.
	 * @param e The enumeration to be converted. 
	 * @param list Either null or the list to have the enumeration contents inserted into.
	 * @return The list resulting from the conversion of the enumeration.
	 */
	public static java.util.List toList(Enumeration e, java.util.List list) 
	{
		java.util.List v = (list != null) ? list : new Vector();
		if (e != null) 
		{
			for (; e.hasMoreElements();) 
			{
				v.add(e.nextElement());
			}
		}
		return v;
	}

	// ------------------------------------------------------------------------

	/**
	 * Converts an iterator to a list.
	 * @param i The iterator to be converted.
	 * @return The list resulting from the conversion of the iterator.
	 */
	public static java.util.List toList(Iterator i) 
	{
		return ListTools.toList(i, null);
	}

	/**
	 * Converts an iterator to a list.
	 * @param i The iterator to be converted.
	 * @param list Either null or the list to have the iterator's contents inserted into.
	 * @return The list resulting from the conversion of the iterator.
	 */
	public static java.util.List toList(Iterator i, java.util.List list) 
	{
		java.util.List v = (list != null) ? list : new Vector();
		if (i != null) 
		{
			for (; i.hasNext();) 
			{
				v.add(i.next());
			}
		}
		return v;
	}

	// ------------------------------------------------------------------------

	/**
	 * Converts a set to a list.
	 * @param s The set to be converted.
	 * @return The list resulting from the conversion of the set.
	 */
	public static java.util.List toList(Set s) 
	{
		return ListTools.toList(s, null);
	}

	/**
	 * Converts a set to a list.
	 * @param s The set to be converted.
	 * @param list Either null or the list to have the set's contents inserted into.
	 * @return The list resulting from the conversion of the set.
	 */
	public static java.util.List toList(Set s, java.util.List list) 
	{
		return ListTools.toList(((s != null) ? s.iterator() : null), list);
	}

	// ------------------------------------------------------------------------

	/**
	 * Converts a StringTokenizer to a list.
	 * @param st The StringTokenizer to be converted.
	 * @return The list resulting from the conversion of the StringTokenizer.
	 */
	public static java.util.List toList(StringTokenizer st) 
	{
		return ListTools.toList(st, null);
	}

	/**
	 * Converts a StringTokenizer to a list.
	 * @param st The StringTokenizer to be converted.
	 * @param list Either null or the list to have the StringTokenizer's contents inserted into.
	 * @return The list resulting from the conversion of the StringTokenizer.
	 */
	public static java.util.List toList(StringTokenizer st, java.util.List list) 
	{
		java.util.List v = (list != null) ? list : new Vector();
		if (st != null) 
		{
			for (; st.hasMoreTokens();) 
			{
				v.add(st.nextToken());
			}
		}
		return v;
	}

	// ------------------------------------------------------------------------

	/**
	 * Converts a list to a list.
	 * @param ls The list to be converted to a list.
	 * @return The list resulting from the coversion of the list to a list.
	 */
	public static java.util.List toList(java.util.List ls) 
	{
		return ListTools.toList(ls, null);
	}

	/**
	 * Converts a list to a list.
	 * @param ls The list to be converted to a list.
	 * @param list Either null or the list to have the list's contents inserted into.
	 * @return The list resulting from the coversion of the list to a list.
	 */
	public static java.util.List toList(java.util.List ls, java.util.List list) 
	{
		java.util.List v = (list != null) ? list : new Vector();
		if (ls != null) 
		{
			v.addAll(ls);
		}
		return v;
	}

	// ------------------------------------------------------------------------

	/**
	 * Checks if the list passed is of class type.
	 * @param list The list to be checked.
	 * @param type A class type.
	 * @return True if list is a class type, false otherwise.
	 */
	public static boolean isClassType(java.util.List list, Class type) 
	{
		if ((type == null) || (type == Object.class)) 
		{
			return true;
		}
		else if (list != null) 
		{
			for (Iterator i = list.iterator(); i.hasNext();) 
			{
				Object obj = i.next();
				if ((obj != null) && !type.isAssignableFrom(obj.getClass())) 
				{
					return false;
				}
			}
			return true;
		}
		else 
		{
			return false;
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Converts the list to an Object array.
	 * @param list The list to be converted.
	 * @return The Object array resulting from converting list.
	 */
	public static Object[] toArray(Collection list) 
	{
		return ListTools.toArray(list, null);
	}

	/**
	 * Converts the list to an array of class type.
	 * @param list The list to be converted.
	 * @param type The desired type of the array.
	 * @return The array resulting from the conversion of list to an array of type specified.
	 */
	public static Object[] toArray(Collection list, Class type) 
	{
		if (type == null) 
		{
			type = Object.class;
		}
		if (list != null) 
		{
			Object array[] = (Object[]) Array.newInstance(type, list.size());
			return list.toArray(array);
		}
		else 
		{
			return (Object[]) Array.newInstance(type, 0);
		}
	}

	/**
	 * Converts the array to an object array subject to the values specified for offset and length.
	 * @param arry The array to be converted.
	 * @param ofs The offset value.
	 * @param len The length value.
	 * @return The array resulting from the conversion of the passed array subject to the values specified for offset and length.
	 */
	public static Object[] toArray(Object arry[], int ofs, int len) 
	{
		if (arry != null) 
		{
			int alen = arry.length;
			ofs = _constrainOffset(ofs, alen);
			len = _constrainLength(ofs, len, alen);
			Class type = arry.getClass().getComponentType();
			Object newArry[] = (Object[]) Array.newInstance(type, len);
			System.arraycopy(arry, ofs, newArry, 0, len);
			return newArry;
		}
		else 
		{
			return arry;
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Converts the array to a set.
	 * @param a The array to be converted.
	 * @return The resulting conversion of the array into a set.
	 */
	public static Set toSet(Object a[]) 
	{
		return ListTools.toSet(a, null);
	}

	/**
	 * Converts the array to a set.
	 * @param a The array to be converted.
	 * @param set Either null or the set to have the array's contents inserted into.
	 * @return The set resulting from the conversion of the array.
	 */
	public static Set toSet(Object a[], Set set) 
	{
		return ListTools.toSet(a, 0, -1, set);
	}

	/**
	 * Converts the array to a set subject to the specified values for offset and length.
	 * @param a The array to be converted.
	 * @param ofs The offset value.
	 * @param len The length value.
	 * @param set Either null or the set to have the array's contents inserted into.
	 * @return The set resulting from the conversion of the array.
	 */
	public static Set toSet(Object a[], int ofs, int len, Set set) 
	{
		Set v = (set != null) ? set : new HashSet();
		int alen = (a != null) ? a.length : 0;
		ofs = _constrainOffset(ofs, alen);
		len = _constrainLength(ofs, len, alen);
		for (int i = ofs; i < len; i++) 
		{
			v.add(a[i]);
		}
		return v;
	}

	// ------------------------------------------------------------------------

	/**
	 * Converts an array into a map.
	 * @param arry The array to be converted.
	 * @return The map resulting from the conversion of the array.
	 */
	public static Map toMap(Object arry[][]) 
	{
		return ListTools.toMap(arry, (Map) null);
	}

	/**
	 * Converts an array into a map, using the map specified.
	 * @param arry The array to be converted.
	 * @param map Either null or the map to have the array's contents inserted into.
	 * @return The map resulting from the conversion of the array.
	 */
	public static Map toMap(Object arry[][], Map map) 
	{
		Map m = (map != null) ? map : new OrderedMap();
		if (arry != null) 
		{
			for (int i = 0; i < arry.length; i++) 
			{
				if (arry[i].length >= 2) 
				{
					Object key = arry[i][0], val = arry[i][1];
					if ((key != null) && (val != null)) 
					{
						m.put(key, val);
					}
				}
			}
		}
		return m;
	}

	// ------------------------------------------------------------------------

	/**
	 * Inserts the object into the list and returns the resulting list.
	 * @param list The list to have an element inserted.
	 * @param obj The object to be inserted into the list.
	 * @return The resulting list after the insertion.
	 */
	public static java.util.List add(java.util.List list, Object obj) 
	{
		return ListTools.insert(list, obj, -1);
	}

	/**
	 * Inserts the object into the list at the specified index and returns the resulting list.
	 * @param list The list to have an element inserted.
	 * @param obj The object to be inserted.
	 * @param ndx The index for the inserted object.
	 * @return The resulting list after the insertion.
	 */
	public static java.util.List insert(java.util.List list, Object obj, int ndx) 
	{
		if (list != null) 
		{
			list.add(ndx, obj);
		}
		return list;
	}

	/**
	 * Removes the object at the specified index from the list.
	 * @param list The list to have an item removed.
	 * @param ndx The index of the object being removed.
	 * @return The resulting list after the operation.
	 */
	public static java.util.List remove(java.util.List list, int ndx) 
	{
		if (list != null) 
		{
			list.remove(ndx);
		}
		return list;
	}

	/**
	 * Inserts the object into the array.
	 * @param list The array to have an object inserted into.
	 * @param obj The object to be inserted.
	 * @return The resulting array after the insertion.
	 */
	public static Object[] add(Object list[], Object obj) 
	{
		return ListTools.insert(list, obj, -1);
	}

	/**
	 * Inserts the object into the array at the specified index.
	 * @param list The array to have an object inserted into.
	 * @param obj The object to be inserted.
	 * @param index The index where the object should be inserted.
	 * @return The resulting array after the insertion.
	 */
	public static Object[] insert(Object list[], Object obj, int index)
		// throws ArrayStoreException
	{
		if (list != null) 
		{
			int ndx = ((index > list.length) || (index < 0)) ? list.length : index;
			Class type = list.getClass().getComponentType();
			int size = (list.length > ndx) ? (list.length + 1) : (ndx + 1);
			Object array[] = (Object[]) Array.newInstance(type, size);
			if (ndx > 0) 
			{
				int maxLen = (list.length >= ndx) ? ndx : list.length;
				System.arraycopy(list, 0, array, 0, maxLen);
			}
			array[ndx] = obj; // <-- may throw ArrayStoreException
			if (ndx < list.length) 
			{
				int maxLen = list.length - ndx;
				System.arraycopy(list, ndx, array, ndx + 1, maxLen);
			}
			return array;
		}
		else 
		{
			return null;
		}
	}

	/**
	 * Removes the object in the array at the specified index.
	 * @param list The array having object removed.
	 * @param ndx The index of the item in the array that will be removed.
	 * @return The resulting array after the remove operation.
	 */
	public static Object[] remove(Object list[], int ndx) 
	{
		if ((list != null) && (ndx >= 0) && (ndx < list.length)) 
		{
			Class type = list.getClass().getComponentType();
			Object array[] = (Object[]) Array.newInstance(type, list.length - 1);
			if (ndx > 0) 
			{
				System.arraycopy(list, 0, array, 0, ndx);
			}
			if (ndx < (list.length - 1)) 
			{
				System.arraycopy(list, ndx + 1, array, ndx, list.length - ndx - 1);
			}
			return array;
		}
		else 
		{
			return null;
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Finds the index to the Object specified in the list.
	 * @param list The list to be searched.
	 * @param item The object to have its index found.
	 * @return The index of the Object specified.
	 */
	public static int indexOf(java.util.List list, Object item) 
	{
		if (list == null) 
		{
			return -1;
		}
		else 
		{
			return list.indexOf(item);
		}
	}

	/**
	 * Finds the index to the specified string (case-insensitive) in the list.
	 * @param list The list to be searched.
	 * @param item The string to have its index found.
	 * @return The index of the string specified.
	 */
	public static int indexOfIgnoreCase(java.util.List list, String item) 
	{
		if (list == null) 
		{
			return -1;
		}
		else 
		{
			int index = 0;
			for (Iterator i = list.iterator(); i.hasNext(); index++) 
			{
				Object listObj = i.next();
				String listStr = (listObj != null) ? listObj.toString() : null;
				if (listStr == item) 
				{ // also takes care of 'null == null'
					return index;
				}
				else if ((listStr != null) && listStr.equalsIgnoreCase(item)) 
				{
					return index;
				}
			}
			return -1;
		}
	}

	/**
	 * Finds the index of the item in the array.
	 * @param list The array to be searched.
	 * @param item The item to have its index found.
	 * @return The index corresponding to the item.
	 */
	public static int indexOf(Object list[], Object item) 
	{
		return ListTools.indexOf(list, 0, -1, item);
	}

	/**
	 * Finds and returns the index corresponding to the item in the array subject to the offset and length values specified.
	 * @param list The array to be searched.
	 * @param ofs The offset value.
	 * @param len The length value.
	 * @param item The item to have its index found.
	 * @return The index corresponding to the item subject to the offset and length values specified.
	 */
	public static int indexOf(Object list[], int ofs, int len, Object item) 
	{
		if (list == null) 
		{

			/* no list */
			return -1;

		}
		else 
		{

			/* constrain offset/length */
			int alen = (list != null) ? list.length : 0;
			ofs = _constrainOffset(ofs, alen);
			len = _constrainLength(ofs, len, alen);

			/* loop through array checking for item */
			for (int i = ofs; i < len; i++) 
			{
				if (list[i] == item) 
				{ // also takes care of 'null == null'
					return i;
				}
				else if ((list[i] != null) && list[i].equals(item)) 
				{
					return i;
				}
			}

			/* still not found */
			return -1;

		}
	}

	/**
	 * Finds the index of the string specified (case-insensitive) in the list of strings.
	 * @param list The list to be searched.
	 * @param item The string to have its index found.
	 * @return The index corresponding to the item.
	 */
	public static int indexOfIgnoreCase(String list[], String item) 
	{
		if (list == null) 
		{
			return -1;
		}
		else 
		{
			for (int i = 0; i < list.length; i++) 
			{
				if (list[i] == item) 
				{ // also takes care of 'null == null'
					return i;
				}
				else if ((list[i] != null) && list[i].equalsIgnoreCase(item)) 
				{
					return i;
				}
			}
			return -1;
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Checks to see if the list contains the item.
	 * @param list The list to be searched.
	 * @param item The item to be found.
	 * @return True if the item is in the list, false otherwise.
	 */
	public static boolean contains(java.util.List list, Object item) 
	{
		return (ListTools.indexOf(list, item) >= 0);
	}

	/**
	 * Checks to see if the string (case-insensitive) is in the list.
	 * @param list The list to be searched.
	 * @param item The string item to be found.
	 * @return True if the item is in the list, false otherwise.
	 */
	public static boolean containsIgnoreCase(java.util.List list, String item) 
	{
		return (ListTools.indexOfIgnoreCase(list, item) >= 0);
	}

	/**
	 * Checks to see if the item specified is in the array.
	 * @param list The array to be searched.
	 * @param item The item to be found.
	 * @return True if the item is in the array, false otherwise.
	 */
	public static boolean contains(Object list[], Object item) 
	{
		return (ListTools.indexOf(list, 0, -1, item) >= 0);
	}

	/**
	 * Checks to see if the array contains the item, subject to the offset and lengths specified.
	 * @param list The array to be searched.
	 * @param ofs The offset value.
	 * @param len The length value.
	 * @param item The item to be found.
	 * @return True if the item is in the array subject to the offset and lengths specified, false otherwise.
	 */
	public static boolean contains(Object list[], int ofs, int len, Object item) 
	{
		return (ListTools.indexOf(list, ofs, len, item) >= 0);
	}

	/**
	 * Checks to see if the array contains the string (case-insensitive).
	 * @param list The array to be searched.
	 * @param item The item to be found.
	 * @return True if the item is in the list, false otherwise.
	 */
	public static boolean containsIgnoreCase(String list[], String item) 
	{
		return (ListTools.indexOfIgnoreCase(list, item) >= 0);
	}

	// ------------------------------------------------------------------------

	/**
	 * Sorts the list according to the comparator specified.
	 * @param list The list to be sorted.
	 * @param comp The comparator to be used to sort the list.
	 * @return The list sorted using the comparator.
	 */
	public static java.util.List sort(java.util.List list, Comparator comp) 
	{
		return ListTools.sort(list, comp, true);
	}

	/**
	 * Sorts the list using the comparator in either forward or reverse order.
	 * @param list The list to be sorted.
	 * @param comp The comparator to be used to sort the list.
	 * @param forwardOrder True means the sort will be in forward order; false in backward order.
	 * @return The list sorted using the comparator in either forward or reverse order.
	 */
	public static java.util.List sort(java.util.List list, Comparator comp, boolean forwardOrder) 
	{
		if (list != null) 
		{
			Comparator c = comp;
			if (c == null) 
			{
				c = new StringComparator(forwardOrder);
			}
			else if (forwardOrder) 
			{
				c = comp;
			}
			else 
			{
				c = new ReverseOrderComparator(comp);
			}
			Collections.sort(list, c);
		}
		return list;
	}

	/**
	 * Sorts the array in forward order using the comparator specified.
	 * @param list The array to be sorted.
	 * @param comp The comparator used to sort the array.
	 * @return The array sorted using the comparator in either forward order.
	 */
	public static Object[] sort(Object list[], Comparator comp) 
	{
		return ListTools.sort(list, comp, true);
	}

	/**
	 * Sorts the array using the comparator in either forward or reverse order.
	 * @param list The array to be sorted.
	 * @param comp The comparator to be used to sort the array.
	 * @param forwardOrder True means the sort will be in forward order; false in backward order.
	 * @return The array sorted using the comparator in either forward or reverse order.
	 */
	public static Object[] sort(Object list[], Comparator comp, boolean forwardOrder) 
	{
		if (list != null) 
		{
			Comparator c = comp;
			if (c == null) 
			{
				c = new StringComparator(forwardOrder);
			}
			else if (forwardOrder) 
			{
				c = comp;
			}
			else 
			{
				c = new ReverseOrderComparator(comp);
			}
			Arrays.sort(list, c);
		}
		return list;
	}

	/**
	 * Provides implementation for a comparator of Strings.
	 * @author Martin D. Flynn
	 * 
	 */
	public static class StringComparator implements Comparator 
	{
		private boolean ascending = true;

		/**
		 * Constructs a default instance of StringComparator.
		 */
		public StringComparator() 
		{
			this(true);
		}

		/**
		 * Constructs an instance of StringComparator with ascending or descending order.
		 * @param ascending True for ascending order, False for descending order.
		 */
		public StringComparator(boolean ascending) 
		{
			this.ascending = ascending;
		}

		/**
		 * Compares two strings.
		 * @param o1 The first object being compared.
		 * @param o2 The second object being compared.
		 * @return int The integer representation of the comparison.
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) 
		{
			String s1 = (o1 != null) ? o1.toString() : "";
			String s2 = (o2 != null) ? o2.toString() : "";
			return this.ascending ? s1.compareTo(s2) : s2.compareTo(s1);
		}

		/**
		 * Compares the object in scope to the object parameter.
		 * @param obj The object this is being compared to.
		 * @return True if the objects are considered equal, false otherwise.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) 
		{
			if (obj instanceof StringComparator) 
			{
				return this.ascending == ((StringComparator) obj).ascending;
			}
			return false;
		}
	}

	/**
	 * Provides implementation for a reverse order comparator.
	 * @author Martin D. Flynn
	 * 
	 */
	public static class ReverseOrderComparator implements Comparator 
	{
		private Comparator otherComp = null;

		/**
		 * Constructs an instance of ReverseOrderComparator from the comparator specified.
		 * @param comp A comparator to be used in creating the ReverseOrderComparator.
		 */
		public ReverseOrderComparator(Comparator comp) 
		{
			this.otherComp = (comp != null) ? comp : new StringComparator();
		}

		/**
		 * Compares the first object to the second object.
		 * @param o1 The first object being compared.
		 * @param o2 The second object being compared.
		 * @return The integer representation of the comparison of the two objects.
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) 
		{
			int compVal = this.otherComp.compare(o1, o2);
			if (compVal > 0) 
			{
				return -1;
			}
			if (compVal < 0) 
			{
				return 1;
			}
			return 0;
		}

		/**
		 * Compares the object in scope to the object parameter.
		 * @param obj The object this is being compared to.
		 * @return True if the objects are considered equal, false otherwise.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) 
		{
			if (obj instanceof ReverseOrderComparator) 
			{
				ReverseOrderComparator descComp = (ReverseOrderComparator) obj;
				return this.otherComp.equals(descComp.otherComp);
			}
			return false;
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Reverses the order of the array in place.
	 * @param list The array to have its order reversed.
	 * @return The resulting reversed array.
	 */
	public static Object[] reverseOrder(Object list[]) 
	{
		if ((list != null) && (list.length > 1)) 
		{
			int len = list.length / 2;
			for (int i = 0; i < len; i++) 
			{
				int i2 = (list.length - 1) - i;
				Object obj = list[i];
				list[i] = list[i2];
				list[i2] = obj;
			}
		}
		return list;
	}

	/**
	 * Reverses the order of the list in place.
	 * @param list The list to have its order reversed.
	 * @return The resulting reversed list.
	 */
	public static java.util.List reverseOrder(java.util.List list) 
	{
		Collections.reverse(list);
		return list;
	}

	// ------------------------------------------------------------------------

	/**
	 * Provides implementation of proxy to a collection.
	 * @author Martin D. Flynn
	 * 
	 */
	public static class CollectionProxy implements Collection 
	{
		private Collection delegate = null;

		/**
		 * Constructs a new CollectionProxy using the passed collection.
		 * @param c A collection to be used in creating the CollectionProxy.
		 */
		public CollectionProxy(Collection c) 
		{
			this.delegate = c;
		}

		/**
		 * Adds the object to the internal collection.
		 * @param o The object to be added.
		 * @return True if the object was added successfully, false otherwise.
		 * @see java.util.Collection#add(java.lang.Object)
		 */
		public boolean add(Object o) 
		{
			return this.delegate.add(o);
		}

		/**
		 * Adds the collection to the internal collection.
		 * @param c The collection to be added.
		 * @return True if the collection was added successfully, false otherwise.
		 * @see java.util.Collection#addAll(java.util.Collection)
		 */
		public boolean addAll(Collection c) 
		{
			return this.delegate.addAll(c);
		}

		/**
		 * Clears the internal collection.
		 * 
		 * @see java.util.Collection#clear()
		 */
		public void clear() 
		{
			this.delegate.clear();
		}

		/**
		 * Checks to see if the internal collection contains the object.
		 * @param o The object to be found.
		 * @return True if the object is in the internal collection, false otherwise.
		 * 
		 * @see java.util.Collection#contains(java.lang.Object)
		 */
		public boolean contains(Object o) 
		{
			return this.delegate.contains(o);
		}

		/**
		 * Checks to see if the internal collection contains the collection passed.
		 * @param c The collection to be found.
		 * @return True if the collection is in the internal collection, false otherwise.
		 * @see java.util.Collection#containsAll(java.util.Collection)
		 */
		public boolean containsAll(Collection c) 
		{
			return this.delegate.containsAll(c);
		}

		/**
		 * Checks to see if the specified object is an equivalent CollectionProxy.
		 * @param o An object to be compared to the local object.
		 * @return True if the object is an equivalent CollectionProxy, false otherwise.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o) 
		{
			if (o instanceof CollectionProxy) 
			{
				return this.delegate.equals(((CollectionProxy) o).delegate);
			}
			else 
			{
				return false;
			}
		}

		/**
		 * Returns the hashcode of the internal collection.
		 * @return The hascode value of the internal collection.
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() 
		{
			return this.delegate.hashCode();
		}

		/**
		 * Checks to see if the internal collection is empty.
		 * @return True if the internal collection is empty, false otherwise.
		 * @see java.util.Collection#isEmpty()
		 */
		public boolean isEmpty() 
		{
			return this.delegate.isEmpty();
		}

		/**
		 * Returns the iterator for the internal collection.
		 * @return The iterator for the internal collection.
		 * 
		 * @see java.util.Collection#iterator()
		 */
		public Iterator iterator() 
		{
			return this.delegate.iterator();
		}

		/**
		 * Removes the object specified from the internal collection.
		 * @param o The object to be removed.
		 * @return True if the object was removed, false otherwise.
		 * @see java.util.Collection#remove(java.lang.Object)
		 */
		public boolean remove(Object o) 
		{
			return this.delegate.remove(o);
		}

		/**
		 * Removes all of the contents of the passed collection from the internal collection.
		 * @param c The collection of values to be removed.
		 * @return True if the collection of values was removed from the internal collection, false otherwise.
		 * @see java.util.Collection#removeAll(java.util.Collection)
		 */
		public boolean removeAll(Collection c) 
		{
			return this.delegate.removeAll(c);
		}

		/**
		 * Retains only the elements in internal collection that are contained in the specified collection.
		 * @param c The collection of elements to be retained.
		 * @return True if this collection changed as a result of the call, false otherwise.
		 * @see java.util.Collection#retainAll(java.util.Collection)
		 */
		public boolean retainAll(Collection c) 
		{
			return this.delegate.retainAll(c);
		}

		/**
		 * Returns the size of the internal collection.
		 * @return The size of the internal collection.
		 * @see java.util.Collection#size()
		 */
		public int size() 
		{
			return this.delegate.size();
		}

		/**
		 * Converts the internal collection to an array.
		 * @return The array corresponding to the internal collection.
		 * @see java.util.Collection#toArray()
		 */
		public Object[] toArray() 
		{
			return this.delegate.toArray();
		}

		/**
		 * Converts the internal collection to an array using the array specified.
		 * @param a The array into which the elements of this collection are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose.
		 * @return The array corresponding to the internal collection.
		 * @see java.util.Collection#toArray(T[])
		 */
		public Object[] toArray(Object[] a) 
		{
			return this.delegate.toArray(a);
		}
	}

	// ------------------------------------------------------------------------

}
