package org.sun.encrypted.encryptor;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.jasypt.commons.CommonUtils;
import org.jasypt.encryption.pbe.PBEByteCleanablePasswordEncryptor;

import sun.misc.BASE64Decoder;

public class StandardAESByteEncryptor implements
		PBEByteCleanablePasswordEncryptor {

	// 默认的加密算法
	public static final String DEFAULT_ALGORITHM = "AES/ECB/PKCS5Padding";
	private String algorithm = DEFAULT_ALGORITHM;

	// 对称密钥
	private SecretKey mSecretKey;

	// 初始化属性，判断该加密器是否已初始化
	private boolean initialized = false;

	// 加密器
	private Cipher encryptCipher = null;
	// 解密器
	private Cipher decryptCipher = null;

	public void setPassword(String password) {

		CommonUtils.validateNotEmpty(password, "Password cannot be set empty");
		if (this.mSecretKey != null) {
			this.mSecretKey = null;
		}

		BASE64Decoder base64 = new BASE64Decoder();

		try {
			byte[] bKey = base64.decodeBuffer(password);
			mSecretKey = new SecretKeySpec(bKey, "AES");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setPasswordCharArray(char[] password) {

		CommonUtils.validateNotNull(password, "Password cannot be set null");
		CommonUtils.validateIsTrue(password.length > 0,
				"Password cannot be set empty");
		if (this.mSecretKey != null) {
			this.mSecretKey = null;
		}

		BASE64Decoder base64 = new BASE64Decoder();

		try {
			byte[] bKey = base64.decodeBuffer(password.toString());
			mSecretKey = new SecretKeySpec(bKey, "AES");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public byte[] encrypt(byte[] message) {

		byte[] encryptedMessage = null;
		try {
			synchronized (this.encryptCipher) {
				this.encryptCipher.init(Cipher.ENCRYPT_MODE, mSecretKey);
				encryptedMessage = this.encryptCipher.doFinal(message);
			}
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

		return encryptedMessage;
	}

	public byte[] decrypt(byte[] encryptedMessage) {

		byte[] targetByte = null;
		try {
			synchronized (this.decryptCipher) {
				this.decryptCipher.init(Cipher.DECRYPT_MODE, mSecretKey);
				targetByte = this.decryptCipher.doFinal(encryptedMessage);
			}
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

		return targetByte;
	}

	/**
	 * 判断是否初始化
	 * 
	 * @return
	 */
	public boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * 初始化加解密器
	 */
	public synchronized void initialize() {
		if (!this.initialized) {
			try {
				this.encryptCipher = Cipher.getInstance(this.algorithm,
						"SunJCE");
				this.decryptCipher = Cipher.getInstance(this.algorithm,
						"SunJCE");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			}

		}
	}

}
