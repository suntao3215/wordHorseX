package org.sun.encrypted.sqlaware;

public interface IHashAlgorithm {
	
	/**
	 * ɢ���㷨������ֵ�͵�ɢ��ֵ
	 * 
	 * @param order
	 * @return
	 */
	public abstract int hash(Object order);
}
