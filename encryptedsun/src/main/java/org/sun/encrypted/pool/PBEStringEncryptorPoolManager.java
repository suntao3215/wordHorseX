package org.sun.encrypted.pool;

import org.sun.encrypted.encryptor.StandardPBEStringEncryptor;

public class PBEStringEncryptorPoolManager {

	static private PBEStringEncryptorPoolManager instance;// 唯一数据库连接池管理实例类
	private PBEStringEncryptorPool pools;// 连接池

	/**
	 * 实例化管理类
	 */
	private PBEStringEncryptorPoolManager() {
		pools = new PBEStringEncryptorPool();
	}

	/**
	 * 得到唯一实例管理类
	 * 
	 * @return
	 */
	static synchronized public PBEStringEncryptorPoolManager getInstance() {
		if (instance == null) {
			instance = new PBEStringEncryptorPoolManager();
		}
		return instance;
	}

	/**
	 * 得到一个连接，根据连接池的名字和等待时间
	 * 
	 * @param name
	 * @param time
	 * @return
	 */
	public StandardPBEStringEncryptor getEncryptor() {
		return pools.getEncryptor();
	}

	/**
	 * 释放连接
	 * 
	 * @param name
	 * @param con
	 */
	public void returnEncryptor(StandardPBEStringEncryptor encryptor) {
		pools.returnEncryptor(encryptor);
	}

	/**
	 * 释放所有连接
	 */
	public void closeEncryptorPool() {
		pools.closeEncryptorPool();
	}

}
