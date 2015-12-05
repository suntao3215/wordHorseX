package org.sun.encrypted.encryptor;

import org.sun.encrypted.pool.AESEncryptorPoolManager;

/**
 * �ӽ��ܸ����� ��װ�ӽ�������
 * 
 * @author Administrator
 * 
 */
public class AESEncryptedHelper {

	/**
	 * ��װ�ַ��ͼ�������
	 * 
	 * @param message
	 * @param password
	 * @return
	 */
	public static byte[] encrypt(String message, String password) {

		byte[] result = null;
		AESEncryptorPoolManager poolManager = AESEncryptorPoolManager.getInstance();
		StandardAESStringEncryptor pbeEncryptor = poolManager.getEncryptor();
		pbeEncryptor.setPassword(password);
		result = pbeEncryptor.encrypt(message);
		poolManager.returnEncryptor(pbeEncryptor);
		return result;
	}

	/**
	 * ��װ��ֵ�ͼ�������
	 * 
	 * @param message
	 * @param password
	 * @return
	 */
	public static byte[] encryptInteger(Object message, String password) {

		byte[] result = null;
		AESEncryptorPoolManager poolManager = AESEncryptorPoolManager.getInstance();
		StandardAESIntegerEncryptor pbeEncryptor = poolManager.getIntegerEncryptor();
		pbeEncryptor.setPassword(password);
		result = pbeEncryptor.encrypt(message);
		poolManager.returnEncryptor(pbeEncryptor);
		return result;
	}

	/**
	 * ��װ�ַ��ͽ�������
	 * 
	 * @param message
	 * @param password
	 * @return
	 */
	public static String decrypt(byte[] message, String password) {

		String result = null;

		AESEncryptorPoolManager poolManager = AESEncryptorPoolManager.getInstance();
		StandardAESStringEncryptor pbeEncryptor = poolManager.getEncryptor();
		pbeEncryptor.setPassword(password);
		result = pbeEncryptor.decrypt(message);
		poolManager.returnEncryptor(pbeEncryptor);

		return result;

	}

	/**
	 * ��װ��ֵ�ͽ�������
	 * 
	 * @param message
	 * @param password
	 * @param dataType
	 * @return
	 */
	public static Object decryptInteger(byte[] message, String password, String dataType) {

		Object result;

		AESEncryptorPoolManager poolManager = AESEncryptorPoolManager.getInstance();
		StandardAESIntegerEncryptor pbeEncryptor = poolManager.getIntegerEncryptor();
		pbeEncryptor.setPassword(password);
		result = pbeEncryptor.decrypt(message, dataType);
		poolManager.returnEncryptor(pbeEncryptor);

		return result;

	}

}
