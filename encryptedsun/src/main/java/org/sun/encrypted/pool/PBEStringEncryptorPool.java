package org.sun.encrypted.pool;

import java.util.Enumeration;
import java.util.Vector;

import org.sun.encrypted.encryptor.StandardPBEStringEncryptor;

@SuppressWarnings("rawtypes")
public class PBEStringEncryptorPool {

	private int initialSize = 10; // ���ӳصĳ�ʼ��С

	private int incrementalSize = 5;// ���ӳ��Զ����ӵĴ�С

	private int maxSize = 50; // ���ӳ����Ĵ�С

	private Vector encryptors = null; // ������ӳ������ݿ����ӵ����� , ��ʼʱΪ null

	public PBEStringEncryptorPool() {
		createPool();
	}

	/**
	 * ����һ�����ݿ����ӳأ����ӳ��еĿ������ӵ������������Ա initialConnections �����õ�ֵ
	 */

	public synchronized void createPool() {

		// ȷ�����ӳ�û�д���
		// ������ӳؼ��������ˣ��������ӵ����� connections ����Ϊ��
		if (encryptors != null) {
			return; // ��������������򷵻�
		}

		// �����������ӵ����� , ��ʼʱ�� 0 ��Ԫ��
		encryptors = new Vector();

		// ���� initialConnections �����õ�ֵ���������ӡ�
		createEncryptorPool(this.initialSize);
		System.out.println(" PBE���ܳش����ɹ��� ");
	}

	/**
	 * �������ܳ�
	 * 
	 * @param numConnections
	 */
	@SuppressWarnings("unchecked")
	private void createEncryptorPool(int numConnections) {

		// ѭ������ָ����Ŀ�����ݿ�����
		for (int x = 0; x < numConnections; x++) {

			// �Ƿ����ӳ��е����ݿ����ӵ����������ﵽ������ֵ�����Ա maxConnections
			// ָ������� maxConnections Ϊ 0 ��������ʾ��������û�����ơ�
			// ��������������ﵽ��󣬼��˳���
			if (this.maxSize > 0 && this.encryptors.size() >= this.maxSize) {
				break;
			}

			// add a new PooledConnection object to connections vector
			// ����һ�����ӵ����ӳ��У����� connections �У�
			encryptors.addElement(newEncryptor());
			System.out.println(" PBE�����༺���� ......");

		}
	}

	/**
	 * ����һ���µļ�����
	 * 
	 * @return
	 */
	private StandardPBEStringEncryptor newEncryptor() {
		// ����һ�����ݿ�����
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

		return encryptor; // ���ش������µ����ݿ�����

	}

	/**
	 * ͨ������ getFreeConnection() ��������һ�����õ����ݿ����� ,
	 * �����ǰû�п��õ����ݿ����ӣ����Ҹ�������ݿ����Ӳ��ܴ����������ӳش�С�����ƣ����˺����ȴ�һ���ٳ��Ի�ȡ��
	 * 
	 * @return ����һ�����õ����ݿ����Ӷ���
	 */
	public synchronized StandardPBEStringEncryptor getEncryptor() {

		// ȷ�����ӳؼ�������
		if (encryptors == null) {
			return null; // ���ӳػ�û�������򷵻� null
		}

		StandardPBEStringEncryptor encryptor = getFreeEncryptor(); // ���һ�����õ����ݿ�����

		// ���Ŀǰû�п���ʹ�õ����ӣ������е����Ӷ���ʹ����
		while (encryptor == null) {
			// ��һ������
			wait(250);
			// �������ԣ�ֱ����ÿ��õ����ӣ�
			encryptor = getFreeEncryptor();
		}

		return encryptor;// ���ػ�õĿ��õ�����

	}

	/**
	 * ͨ������findFreeEncryptor��������һ�����ü��ܶ���,������������򴴽��µĶ���
	 * 
	 * @return
	 */
	private StandardPBEStringEncryptor getFreeEncryptor() {

		// �ӳ��л��һ�����õļ��ܶ���

		StandardPBEStringEncryptor encryptor = findFreeEncryptor();

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
	private StandardPBEStringEncryptor findFreeEncryptor() {

		StandardPBEStringEncryptor encryptor = null;
		// ��ó����������еĶ���
		Enumeration enumerate = encryptors.elements();
		// �������еĶ��󣬿��Ƿ��п��õ�
		while (enumerate.hasMoreElements()) {

			encryptor = (StandardPBEStringEncryptor) enumerate.nextElement();

			if (!encryptor.isBusy()) {
				// ����˶���æ����������������Ϊæ
				encryptor.setBusy(true);
				break; // �����ҵ�һ�����õ����ӣ��˳�
			}
		}
		return encryptor;// �����ҵ����Ŀ��ü��ܶ���

	}

	/**
	 * �˺�������һ�����ܶ��󵽳��У�����Ϊ���С�
	 * 
	 * @param �践�ص����ӳ��е����Ӷ���
	 */

	public void returnEncryptor(StandardPBEStringEncryptor encryptor) {

		// ȷ�����ӳش��ڣ��������û�д����������ڣ���ֱ�ӷ���
		if (encryptors == null) {
			System.out.println(" ���ӳز����ڣ��޷����ش����ӵ����ӳ��� !");
			return;
		}

		// ���ü��ܶ���Ϊ����
		encryptor.setBusy(false);

	}

	/**
	 * �ر����ӳ������е����ӣ���������ӳء�
	 */

	public synchronized void closeEncryptorPool() {

		// ȷ�����ӳش��ڣ���������ڣ�����
		if (encryptors == null) {
			System.out.println(" ���ӳز����ڣ��޷��ر� !");
			return;
		}

		StandardPBEStringEncryptor encryptor = null;

		Enumeration enumerate = encryptors.elements();

		while (enumerate.hasMoreElements()) {

			encryptor = (StandardPBEStringEncryptor) enumerate.nextElement();
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
		} catch (InterruptedException e) {

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
