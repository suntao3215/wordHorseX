package org.sun.encrypted.encryptor;

import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

public class StandardAESStringEncryptor {

	private static final String MESSAGE_CHARSET = "UTF-8";

	private final StandardAESByteEncryptor byteEncryptor;

	// 判断该对象是否正在被使用
	private boolean busy = false;

	private final Base64 base64;

	private boolean passwordSet = false;

	public StandardAESStringEncryptor() {
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

	public byte[] encrypt(String message) {

		if (message == null) {
			return null;
		}

		if (!isInitialized()) {
			initialize();
		}

		if (!this.passwordSet) {
			return null;
		}

		this.passwordSet = false;

		try {

			final byte[] messageBytes = message.getBytes(MESSAGE_CHARSET);

			byte[] encryptedMessage = this.byteEncryptor.encrypt(messageBytes);

			encryptedMessage = this.base64.encode(encryptedMessage);

			return encryptedMessage;

		} catch (EncryptionInitializationException e) {
			throw e;
		} catch (EncryptionOperationNotPossibleException e) {
			throw e;
		} catch (Exception e) {
			throw new EncryptionOperationNotPossibleException();
		}

	}

	public String decrypt(byte[] encryptedMessage) {

		if (encryptedMessage == null) {
			return null;
		}

		if (!isInitialized()) {
			initialize();
		}

		if (!this.passwordSet) {
			return null;
		}

		this.passwordSet = false;

		try {

			byte[] encryptedMessageBytes = null;

			encryptedMessageBytes = this.base64.decode(encryptedMessage);

			final byte[] message = this.byteEncryptor
					.decrypt(encryptedMessageBytes);

			return new String(message, MESSAGE_CHARSET);

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
