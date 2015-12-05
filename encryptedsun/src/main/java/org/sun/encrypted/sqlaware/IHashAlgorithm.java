package org.sun.encrypted.sqlaware;

public interface IHashAlgorithm {
	
	/**
	 * 散列算法，求数值型的散列值
	 * 
	 * @param order
	 * @return
	 */
	public abstract int hash(Object order);
}
