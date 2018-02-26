package core;

import java.util.*;

/**
 * A functional interface for mapping a value of one type to
 * a value of another, and static utilities involving it
 * 
 * @author EPICI
 * @version 1.0
 *
 * @param <K>
 * @param <V>
 */
public interface Mapping<K, V> {
	
	/**
	 * Pass a value through this mapping function to get
	 * some other value
	 * 
	 * @param value input value
	 * @return output value
	 */
	public V map(K value);
	
	/**
	 * Like python's map, except it actually computes the whole list
	 * 
	 * @param original
	 * @param mapping
	 * @return
	 */
	public static <K,V> ArrayList<V> map(Iterable<K> original,Mapping<K,V> mapping){
		ArrayList<V> result = new ArrayList<>();
		for(K k:original){
			result.add(mapping.map(k));
		}
		return result;
	}
	
	/**
	 * Sort a list by a key
	 * 
	 * @param list
	 * @param mapping
	 * @param compare
	 */
	public static <K,V> void sort(ArrayList<K> list,Mapping<K,V> mapping,Comparator<V> compare){
		int n = list.size();
		ArrayList<Any.Keyed<V,K>> kl = new ArrayList<>(n);
		for(int i=0;i<n;i++){
			K k = list.get(i);
			kl.add(new Any.Keyed<>(mapping.map(k),k));
		}
		kl.sort((Any.Keyed<V,K> a,Any.Keyed<V,K> b)->compare.compare(a.key, b.key));
		for(int i=0;i<n;i++){
			list.set(i, kl.get(i).value);
		}
	}
	
}
