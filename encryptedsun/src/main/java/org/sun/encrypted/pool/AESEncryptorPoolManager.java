package org.sun.encrypted.pool;

import org.sun.encrypted.encryptor.StandardAESIntegerEncryptor;
import org.sun.encrypted.encryptor.StandardAESStringEncryptor;

public class AESEncryptorPoolManager {

	public static final int STRINGENCRYPTOR = 0;
	public static final int INTEGERENCRYPTOR = 1;

	// Ψһ���ܳع���ʵ����
	private static volatile AESEncryptorPoolManager instance;

	// �ַ������ܳ�
	private AESStringEncryptorPool stringPools;
	// ��ֵ�ͼ��ܳ�
	private AESIntegerEncryptorPool integerPools;

	private AESEncryptorPoolManager() {
		stringPools = new AESStringEncryptorPool();
		integerPools = new AESIntegerEncryptorPool();
	}

	/**
	 * �õ�Ψһʵ��������
	 * 
	 * @return
	 */
	public static AESEncryptorPoolManager getInstance() {
		if (instance == null) {
			synchronized (AESEncryptorPoolManager.class) {
				if (instance == null) {
					instance = new AESEncryptorPoolManager();
				}
			}
		}
		return instance;
	}

	/**
	 * ��ȡ������
	 * 
	 * @return �ַ���������
	 */
	public StandardAESStringEncryptor getEncryptor() {
		return stringPools.getEncryptor();
	}

	/**
	 * ��ȡ������
	 * 
	 * @return ��ֵ�ͼ�����
	 */
	public StandardAESIntegerEncryptor getIntegerEncryptor() {
		return integerPools.getEncryptor();
	}

	/**
	 * �ͷż�����
	 * 
	 * @param encryptor
	 *            �ַ���������
	 */
	public void returnEncryptor(StandardAESStringEncryptor encryptor) {
		stringPools.returnEncryptor(encryptor);
	}

	/**
	 * �ͷż�����
	 * 
	 * @param encryptor
	 *            ��ֵ�ͼ�����
	 */
	public void returnEncryptor(StandardAESIntegerEncryptor encryptor) {
		integerPools.returnEncryptor(encryptor);
	}

	/**
	 * �ͷż�����
	 */
	public void closeEncryptorPool() {
		stringPools.closeEncryptorPool();
		integerPools.closeEncryptorPool();
	}

}
