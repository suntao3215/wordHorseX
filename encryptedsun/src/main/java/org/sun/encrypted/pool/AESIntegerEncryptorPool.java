package org.sun.encrypted.pool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.sun.encrypted.encryptor.StandardAESIntegerEncryptor;

@SuppressWarnings("rawtypes")
public class AESIntegerEncryptorPool {

	private int initialSize = 10; // ���ܳصĳ�ʼ��С

	private int incrementalSize = 5;// ���ܳ��Զ����ӵĴ�С

	private int maxSize = 50; // ���ܳ����Ĵ�С

	private Vector encryptors = null; // ��ż����������� , ��ʼʱΪ null

	public AESIntegerEncryptorPool() {
		createPool();
	}

	private void createPool() {

		// ȷ�����ܳ�û�д���
		// ����ؼ��������ˣ���������������� encryptors ����Ϊ��
		if (encryptors != null) {
			return;
		}
		try {
			loadParameters();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			// ������������� , ��ʼʱ�� 0 ��Ԫ��
			encryptors = new Vector();
			// ���� initialSize �����õ�ֵ��������������
			createEncryptorPool(this.initialSize);
			// System.out.println("AES Integer���ܳش����ɹ��� ");
		}
	}

	/**
	 * ������������ļ�������������ļ��еĲ���
	 */
	private void loadParameters() {
		Properties properties = new Properties();
		// ��ȡ�����ļ�
		InputStream inStream = null;
		try {
			inStream = AESIntegerEncryptorPool.class.getResourceAsStream("/encryptpool.properties");
			properties.load(inStream);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (null != inStream)
					inStream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		// ���ò���
		initialSize = Integer.valueOf(properties.getProperty("initialSize"));
		maxSize = Integer.valueOf(properties.getProperty("maxSize"));
		incrementalSize = Integer.valueOf(properties.getProperty("incrementalSize"));
	}

	/**
	 * �������ܳ�
	 * 
	 * @param numConnections
	 */
	@SuppressWarnings("unchecked")
	private void createEncryptorPool(int numConnections) {

		// ѭ������ָ����Ŀ�ļ�����
		for (int x = 0; x < numConnections; x++) {
			// �Ƿ���еļ����������������ﵽ������ֵ�����Ա maxConnections
			// ָ������� maxConnections Ϊ 0 ��������ʾ��������û�����ơ�
			// ��������������ﵽ��󣬼��˳���
			if (this.maxSize > 0 && this.encryptors.size() >= this.maxSize) {
				break;
			}
			// ����һ��������������
			encryptors.addElement(newEncryptor());
		}
	}

	/**
	 * ����һ���µļ�����
	 * 
	 * @return
	 */
	private StandardAESIntegerEncryptor newEncryptor() {
		// ����һ�����ݿ�����
		StandardAESIntegerEncryptor encryptor = new StandardAESIntegerEncryptor();
		return encryptor; // ���ش������µ����ݿ�����
	}

	/**
	 * ��ȡһ��������
	 * 
	 * @return
	 */
	public synchronized StandardAESIntegerEncryptor getEncryptor() {

		// ȷ�����ӳؼ�������
		if (encryptors == null) {
			return null; // ���ӳػ�û�������򷵻� null
		}
		// ���һ�����õļ�����
		StandardAESIntegerEncryptor encryptor = getFreeEncryptor();
		// ���Ŀǰû�п���ʹ�õļ������������еĶ���ʹ����
		while (encryptor == null) {
			// ��һ������
			wait(250);
			// �������ԣ�ֱ����ÿ��õļ�����
			encryptor = getFreeEncryptor();
		}
		// ���ػ�õĿ��õļ�����
		return encryptor;

	}

	/**
	 * ͨ������findFreeEncryptor��������һ�����ü��ܶ���,������������򴴽��µĶ���
	 * 
	 * @return
	 */
	private StandardAESIntegerEncryptor getFreeEncryptor() {

		// �ӳ��л��һ�����õļ��ܶ���
		StandardAESIntegerEncryptor encryptor = findFreeEncryptor();
		if (encryptor == null) {
			// ���Ŀǰ����û�п��õĶ���
			// ����һЩ�µļ��ܶ���
			createEncryptorPool(incrementalSize);
			// ���´ӳ��в����Ƿ��п��ö���
			encryptor = findFreeEncryptor();
			if (encryptor == null) {
				// ����������Ի�ò������õģ��򷵻� null
				return null;
			}
		}
		return encryptor;
	}

	/**
	 * 
	 * ͨ���ж�encryptor��busy����ȷ���ö����Ƿ���� ���û�п��õ����ӣ����� null
	 * 
	 * @return ����һ�����õ����ݿ�����
	 * 
	 */
	private StandardAESIntegerEncryptor findFreeEncryptor() {

		StandardAESIntegerEncryptor encryptor = null;
		// ��ó����������еĶ���
		Enumeration enumerate = encryptors.elements();
		// �������еĶ��󣬿��Ƿ��п��õ�
		while (enumerate.hasMoreElements()) {
			encryptor = (StandardAESIntegerEncryptor) enumerate.nextElement();
			if (!encryptor.isBusy()) {
				// ����˶���æ����������������Ϊæ
				encryptor.setBusy(true);
				break; // �����ҵ�һ�����õ����ӣ��˳�
			}
		}
		return encryptor;// �����ҵ����Ŀ��ü��ܶ���
	}

	public void returnEncryptor(StandardAESIntegerEncryptor encryptor) {

		// ȷ�����ӳش��ڣ��������û�д����������ڣ���ֱ�ӷ���
		if (encryptors == null) {
			System.out.println(" ���ӳز����ڣ��޷����ش����ӵ����ӳ��� !");
			return;
		}
		// ���ü��ܶ���Ϊ����
		encryptor.setBusy(false);
	}

	public void closeEncryptorPool() {

		// ȷ�����ӳش��ڣ���������ڣ�����
		if (encryptors == null) {
			System.out.println(" ���ӳز����ڣ��޷��ر� !");
			return;
		}
		StandardAESIntegerEncryptor encryptor = null;
		Enumeration enumerate = encryptors.elements();
		while (enumerate.hasMoreElements()) {
			encryptor = (StandardAESIntegerEncryptor) enumerate.nextElement();
			// ���æ���� 5 ��
			if (encryptor.isBusy()) {
				wait(5000); // �� 5 ��
			}
			// �����ӳ�������ɾ����
			encryptors.removeElement(encryptor);
		}
		// �����ӳ�Ϊ��
		encryptors = null;
	}

	/**
	 * ʹ����ȴ������ĺ�����
	 * 
	 * @param �����ĺ�����
	 */

	private void wait(int mSeconds) {

		try {
			Thread.sleep(mSeconds);
		}
		catch (InterruptedException e) {
		}
	}

	public int getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}

	public int getIncrementalSize() {
		return incrementalSize;
	}

	public void setIncrementalSize(int incrementalSize) {
		this.incrementalSize = incrementalSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

}
