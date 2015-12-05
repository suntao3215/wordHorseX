package org.sun.encrypted.encryptor;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.jasypt.commons.CommonUtils;
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.normalization.Normalizer;
import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.salt.SaltGenerator;

public class StandardPBEStringEncryptor implements PBEStringCleanablePasswordEncryptor {

	/**
	 * The default algorithm to be used if none specified: PBEWithMD5AndDES.
	 */
	public static final String DEFAULT_ALGORITHM = "PBEWithMD5AndDES";

	/**
	 * The default number of hashing iterations applied for obtaining the
	 * encryption key from the specified password, set to 1000.
	 */
	public static final int DEFAULT_KEY_OBTENTION_ITERATIONS = 1000;

	/**
	 * The default salt size, only used if the chosen encryption algorithm is
	 * not a block algorithm and thus block size cannot be used as salt size.
	 */
	public static final int DEFAULT_SALT_SIZE_BYTES = 8;

	private String algorithm = DEFAULT_ALGORITHM;

	private static final String MESSAGE_CHARSET = "UTF-8";

	/**
	 * base64所需
	 */
	private static final String ENCRYPTED_MESSAGE_CHARSET = "US-ASCII";

	private int saltSizeBytes = DEFAULT_SALT_SIZE_BYTES;

	private int keyObtentionIterations = DEFAULT_KEY_OBTENTION_ITERATIONS;

	private SaltGenerator saltGenerator = null;

	private boolean passwordSet = false;
	private boolean initialized = false;

	// Encryption key generated.
	private SecretKey key = null;

	// Ciphers to be used for encryption and decryption.
	private Cipher encryptCipher = null;
	private Cipher decryptCipher = null;

	// BASE64 encoder which will make sure the returned results are
	// valid US-ASCII strings.
	// The Base64 encoder is THREAD-SAFE
	private final Base64 base64;

	// 判断该对象是否正在被使用
	private boolean busy = false;

	public StandardPBEStringEncryptor() {
		this.base64 = new Base64();
		this.initialize();
	}

	public String encrypt(String message) {

		if (!this.passwordSet) {
			return null;
		}

		this.passwordSet = false;

		// Check initialization
		if (!isInitialized()) {
			initialize();
		}

		try {

			final byte[] messageBytes = message.getBytes(MESSAGE_CHARSET);

			// Create salt
			final byte[] salt = this.saltGenerator
					.generateSalt(this.saltSizeBytes);

			final PBEParameterSpec parameterSpec = new PBEParameterSpec(salt,
					this.keyObtentionIterations);

			byte[] encryptedMessage = null;
			synchronized (this.encryptCipher) {
				this.encryptCipher.init(Cipher.ENCRYPT_MODE, this.key,
						parameterSpec);
				encryptedMessage = this.encryptCipher.doFinal(messageBytes);
			}

			// The StandardPBEByteEncryptor does its job.
			encryptedMessage = CommonUtils.appendArrays(salt, encryptedMessage);

			// We encode the result in BASE64 or HEXADECIMAL so that we obtain
			// the safest result String possible.
			String result = null;

			encryptedMessage = this.base64.encode(encryptedMessage);
			result = new String(encryptedMessage, ENCRYPTED_MESSAGE_CHARSET);

			return result;

		} catch (EncryptionInitializationException e) {
			throw e;
		} catch (EncryptionOperationNotPossibleException e) {
			throw e;
		} catch (Exception e) {
			throw new EncryptionOperationNotPossibleException();
		}
	}

	public String decrypt(String encryptedMessage) {

		if (!this.passwordSet) {
			return null;
		}

		this.passwordSet = false;

		// Check initialization
		if (!isInitialized()) {
			initialize();
		}

		byte[] encryptedMessageBytes = null;

		try {

			encryptedMessageBytes = encryptedMessage
					.getBytes(ENCRYPTED_MESSAGE_CHARSET);
			encryptedMessageBytes = this.base64.decode(encryptedMessageBytes);

			if (this.saltGenerator.includePlainSaltInEncryptionResults()) {
				// Check that the received message is bigger than the salt
				if (encryptedMessageBytes.length <= this.saltSizeBytes) {
					throw new EncryptionOperationNotPossibleException();
				}
			}

			byte[] salt = null;
			byte[] encryptedMessageKernel = null;

			final int saltStart = 0;
			final int saltSize = (this.saltSizeBytes < encryptedMessageBytes.length ? this.saltSizeBytes
					: encryptedMessageBytes.length);
			final int encMesKernelStart = (this.saltSizeBytes < encryptedMessageBytes.length ? this.saltSizeBytes
					: encryptedMessageBytes.length);
			final int encMesKernelSize = (this.saltSizeBytes < encryptedMessageBytes.length ? (encryptedMessageBytes.length - this.saltSizeBytes)
					: 0);

			salt = new byte[saltSize];
			encryptedMessageKernel = new byte[encMesKernelSize];

			System.arraycopy(encryptedMessageBytes, saltStart, salt, 0,
					saltSize);
			System.arraycopy(encryptedMessageBytes, encMesKernelStart,
					encryptedMessageKernel, 0, encMesKernelSize);

			final PBEParameterSpec parameterSpec = new PBEParameterSpec(salt,
					this.keyObtentionIterations);

			byte[] decryptedMessage = null;

			synchronized (this.decryptCipher) {
				this.decryptCipher.init(Cipher.DECRYPT_MODE, this.key,
						parameterSpec);
				decryptedMessage = this.decryptCipher
						.doFinal(encryptedMessageKernel);
			}

			// Return the results
			return new String(decryptedMessage, MESSAGE_CHARSET);

		} catch (InvalidKeyException e) {
			throw new EncryptionOperationNotPossibleException();
		} catch (Exception e) {
			throw new EncryptionOperationNotPossibleException();
		}
	}

	public void setPassword(String password) {

		CommonUtils.validateNotEmpty(password, "Password cannot be set empty");

		char[] charPassword = password.toCharArray();

		// Normalize password to NFC form
		final char[] normalizedPassword = Normalizer
				.normalizeToNfc(charPassword);

		PBEKeySpec pbeKeySpec = new PBEKeySpec(normalizedPassword);

		cleanPassword(normalizedPassword);

		SecretKeyFactory factory;
		try {

			factory = SecretKeyFactory.getInstance(this.algorithm);
			this.key = factory.generateSecret(pbeKeySpec);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}

		this.passwordSet = true;
	}

	public void setPasswordCharArray(char[] password) {

	}

	public boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * 初始化加密解密器
	 */
	public synchronized void initialize() {

		// Double-check to avoid synchronization issues
		if (!this.initialized) {

			if (this.saltGenerator == null) {
				this.saltGenerator = new RandomSaltGenerator();
			}

			try {

				this.encryptCipher = Cipher.getInstance(this.algorithm);
				this.decryptCipher = Cipher.getInstance(this.algorithm);

			} catch (EncryptionInitializationException e) {
				throw e;
			} catch (Throwable t) {
				throw new EncryptionInitializationException(t);
			}

			int algorithmBlockSize = this.encryptCipher.getBlockSize();
			if (algorithmBlockSize > 0) {
				this.saltSizeBytes = algorithmBlockSize;
			}

			this.initialized = true;
		}

	}

	/**
	 * 清空密码
	 * 
	 * @param password
	 */
	private static void cleanPassword(final char[] password) {
		if (password != null) {
			synchronized (password) {
				final int pwdLength = password.length;
				for (int i = 0; i < pwdLength; i++) {
					password[i] = (char) 0;
				}
			}
		}
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

}
