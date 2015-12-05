package org.sun.encrypted.encryptor;

import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

public class StandardAESIntegerEncryptor {

	private static final String MESSAGE_CHARSET = "UTF-8";

	// private static final String ENCRYPTED_MESSAGE_CHARSET = "US-ASCII";

	private final StandardAESByteEncryptor byteEncryptor;

	// 判断该对象是否正在被使用
	private boolean busy = false;

	private final Base64 base64;

	private boolean passwordSet = false;

	public StandardAESIntegerEncryptor() {
		base64 = new Base64();
		byteEncryptor = new StandardAESByteEncryptor();
		this.initialize();
	}

	public void setPassword(String password) {
		this.byteEncryptor.setPassword(password);
		this.passwordSet = true;
	}

	public void setPasswordCharArray(char[] password) {
		this.byteEncryptor.setPasswordCharArray(password);
		this.passwordSet = true;
	}

	public byte[] encrypt(Object message) {

		if (!isInitialized()) {
			initialize();
		}

		if (!this.passwordSet) {
			return null;
		}

		this.passwordSet = false;

		try {

			String messageString = String.valueOf(message);

			final byte[] messageBytes = messageString.getBytes(MESSAGE_CHARSET);

			byte[] encryptedMessage = this.byteEncryptor.encrypt(messageBytes);

			// String result = null;
			encryptedMessage = this.base64.encode(encryptedMessage);
			// result = new String(encryptedMessage, ENCRYPTED_MESSAGE_CHARSET);

			return encryptedMessage;

		} catch (EncryptionInitializationException e) {
			throw e;
		} catch (EncryptionOperationNotPossibleException e) {
			throw e;
		} catch (Exception e) {
			throw new EncryptionOperationNotPossibleException();
		}
	}

	public Object decrypt(byte[] encryptedMessage, String dataType) {

		if (encryptedMessage == null) {
			return 0;
		}

		if (!isInitialized()) {
			initialize();
		}

		if (!this.passwordSet) {
			return 0;
		}

		this.passwordSet = false;

		try {

			byte[] encryptedMessageBytes = null;

			// encryptedMessageBytes = encryptedMessage
			// .getBytes(ENCRYPTED_MESSAGE_CHARSET);
			encryptedMessageBytes = this.base64.decode(encryptedMessage);

			// Let the byte encyptor decrypt
			final byte[] message = this.byteEncryptor
					.decrypt(encryptedMessageBytes);

			String resultString = new String(message, MESSAGE_CHARSET);

			if (dataType.equals("Integer")) {

				return Integer.valueOf(resultString);

			} else if (dataType.equals("Float")) {

				return Float.valueOf(resultString);

			} else {

				return Double.valueOf(resultString);

			}

		} catch (EncryptionInitializationException e) {
			throw e;
		} catch (EncryptionOperationNotPossibleException e) {
			throw e;
		} catch (Exception e) {
			throw new EncryptionOperationNotPossibleException();
		}
	}

	/**
	 * 判断是否初始化
	 * 
	 * @return
	 */
	public boolean isInitialized() {
		return this.byteEncryptor.isInitialized();
	}

	/**
	 * 初始化加解密器
	 */
	public void initialize() {
		if (!this.isInitialized()) {
			this.byteEncryptor.initialize();
		}
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

}
