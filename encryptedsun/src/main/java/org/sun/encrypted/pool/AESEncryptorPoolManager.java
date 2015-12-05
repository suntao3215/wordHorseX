package org.sun.encrypted.pool;

import org.sun.encrypted.encryptor.StandardAESIntegerEncryptor;
import org.sun.encrypted.encryptor.StandardAESStringEncryptor;

public class AESEncryptorPoolManager {

	public static final int STRINGENCRYPTOR = 0;
	public static final int INTEGERENCRYPTOR = 1;

	// 唯一加密池管理实例类
	private static volatile AESEncryptorPoolManager instance;

	// 字符串加密池
	private AESStringEncryptorPool stringPools;
	// 数值型加密池
	private AESIntegerEncryptorPool integerPools;

	private AESEncryptorPoolManager() {
		stringPools = new AESStringEncryptorPool();
		integerPools = new AESIntegerEncryptorPool();
	}

	/**
	 * 得到唯一实例管理类
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
	 * 获取加密器
	 * 
	 * @return 字符串加密器
	 */
	public StandardAESStringEncryptor getEncryptor() {
		return stringPools.getEncryptor();
	}

	/**
	 * 获取加密器
	 * 
	 * @return 数值型加密器
	 */
	public StandardAESIntegerEncryptor getIntegerEncryptor() {
		return integerPools.getEncryptor();
	}

	/**
	 * 释放加密器
	 * 
	 * @param encryptor
	 *            字符串加密器
	 */
	public void returnEncryptor(StandardAESStringEncryptor encryptor) {
		stringPools.returnEncryptor(encryptor);
	}

	/**
	 * 释放加密器
	 * 
	 * @param encryptor
	 *            数值型加密器
	 */
	public void returnEncryptor(StandardAESIntegerEncryptor encryptor) {
		integerPools.returnEncryptor(encryptor);
	}

	/**
	 * 释放加密器
	 */
	public void closeEncryptorPool() {
		stringPools.closeEncryptorPool();
		integerPools.closeEncryptorPool();
	}

}
