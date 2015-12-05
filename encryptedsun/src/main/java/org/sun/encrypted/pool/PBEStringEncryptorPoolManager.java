package org.sun.encrypted.pool;

import org.sun.encrypted.encryptor.StandardPBEStringEncryptor;

public class PBEStringEncryptorPoolManager {

	static private PBEStringEncryptorPoolManager instance;// Ψһ���ݿ����ӳع���ʵ����
	private PBEStringEncryptorPool pools;// ���ӳ�

	/**
	 * ʵ����������
	 */
	private PBEStringEncryptorPoolManager() {
		pools = new PBEStringEncryptorPool();
	}

	/**
	 * �õ�Ψһʵ��������
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
	 * �õ�һ�����ӣ��������ӳص����ֺ͵ȴ�ʱ��
	 * 
	 * @param name
	 * @param time
	 * @return
	 */
	public StandardPBEStringEncryptor getEncryptor() {
		return pools.getEncryptor();
	}

	/**
	 * �ͷ�����
	 * 
	 * @param name
	 * @param con
	 */
	public void returnEncryptor(StandardPBEStringEncryptor encryptor) {
		pools.returnEncryptor(encryptor);
	}

	/**
	 * �ͷ���������
	 */
	public void closeEncryptorPool() {
		pools.closeEncryptorPool();
	}

}
