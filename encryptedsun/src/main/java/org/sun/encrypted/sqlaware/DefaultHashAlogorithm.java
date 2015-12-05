package org.sun.encrypted.sqlaware;

public class DefaultHashAlogorithm implements IHashAlgorithm {

	public int hash(Object order) {
		String orderString = order == null ? "" : order.toString();
		return orderString.hashCode();
	}

}
