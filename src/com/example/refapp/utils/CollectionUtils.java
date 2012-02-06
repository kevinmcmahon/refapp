package com.example.refapp.utils;

import android.util.Log;

import java.util.*;

public class CollectionUtils {
	static private final String TAG = "CollectionUtils";

	static public boolean isEmpty(Object[] objects) {
		return objects==null || objects.length==0;
	}

	static public boolean isEmpty(int[] values) {
		return values==null || values.length==0;
	}

	static public boolean isEmpty(Map<?, ?> map) {
		return map==null || map.isEmpty();
	}

	static public boolean isEmpty(Collection<?> objects) {
		return objects==null || objects.isEmpty();
	}

	public static int[] toIntArray(Collection<Integer> list) {
		if (list == null) return null;

		int size = list.size();
		int[] result = new int[size];

		if (size > 0) {
			int index = 0;
			for (Integer eachVal : list) {
				result[index] = eachVal;
				index++;
			}
		}

		return result;
	}

	public interface IField<T, O> {
		T getValue(O value);
	}

	public interface IWhere<T> {
		boolean isConditionTrue(T data);
	}

	public static <K, V, C extends Collection<V>> SortedMap<K, C> group(C collection, IField<K, V> groupBy) {
		SortedMap<K, C> hash = new TreeMap<K, C>();

		try {
			for (V eachItem : collection) {
				K key = groupBy.getValue(eachItem);

				C childCollection;
				if (hash.containsKey(key)) {
					childCollection = hash.get(key);
				} else {
					//noinspection unchecked
					childCollection = (C) collection.getClass().newInstance();
					hash.put(key, childCollection);
				}
				childCollection.add(eachItem);
			}
		} catch (InstantiationException e) {
			Log.e(TAG, "InstantiationException when grouping collections", e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "IllegalAccessException when grouping collections", e);
		}
		return hash;
	}

	public static <K, V> LinkedHashMap<K, V> toLinkedHash(Collection<V> collection, IField<K, V> uniqueField) {
		LinkedHashMap<K, V> hash = new LinkedHashMap<K, V>();

		for (V eachItem : collection) {
			hash.put(uniqueField.getValue(eachItem), eachItem);
		}
		return hash;
	}

	public static <K, V> List<K> selectField(Collection<V> collection, IField<K, V> field, IWhere<V> where) {
		List<K> result = new ArrayList<K>();

		for (V eachItem : collection) {
			if (where == null || where.isConditionTrue(eachItem)) {
				result.add(field.getValue(eachItem));
			}
		}
		return result;
	}

	public static <T> T findFirst(Collection<T> collection, IWhere<T> where) {
		for (T eachItem : collection) {
			if (where.isConditionTrue(eachItem)) {
				return eachItem;
			}
		}
		return null;
	}

	public static <T> int findFirstIndex(Collection<T> collection, IWhere<T> where) {
		int index = 0;
		for (T eachItem : collection) {
			if (where.isConditionTrue(eachItem)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public static <T, C extends Collection<T>> C findAll(C collection, IWhere<T> where) {
		C result = null;

		try {
			for (T eachItem : collection) {
				if (where.isConditionTrue(eachItem)) {
					if (result == null) {
						//noinspection unchecked
						result = (C) collection.getClass().newInstance();
					}
					result.add(eachItem);
				}
			}
		} catch (InstantiationException e) {
			Log.e(TAG, "InstantiationException when grouping collections", e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "IllegalAccessException when grouping collections", e);
		}

		return result;
	}	
}