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

	private int initialSize = 10; // 加密池的初始大小

	private int incrementalSize = 5;// 加密池自动增加的大小

	private int maxSize = 50; // 加密池最大的大小

	private Vector encryptors = null; // 存放加密器的向量 , 初始时为 null

	public AESIntegerEncryptorPool() {
		createPool();
	}

	private void createPool() {

		// 确保加密池没有创建
		// 如果池己经创建了，保存加密器的向量 encryptors 不会为空
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
			// 创建保存的向量 , 初始时有 0 个元素
			encryptors = new Vector();
			// 根据 initialSize 中设置的值，创建加密器。
			createEncryptorPool(this.initialSize);
			// System.out.println("AES Integer加密池创建成功！ ");
		}
	}

	/**
	 * 如果存在配置文件，则加载配置文件中的参数
	 */
	private void loadParameters() {
		Properties properties = new Properties();
		// 读取配置文件
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

		// 设置参数
		initialSize = Integer.valueOf(properties.getProperty("initialSize"));
		maxSize = Integer.valueOf(properties.getProperty("maxSize"));
		incrementalSize = Integer.valueOf(properties.getProperty("incrementalSize"));
	}

	/**
	 * 创建加密池
	 * 
	 * @param numConnections
	 */
	@SuppressWarnings("unchecked")
	private void createEncryptorPool(int numConnections) {

		// 循环创建指定数目的加密器
		for (int x = 0; x < numConnections; x++) {
			// 是否池中的加密器的数量己经达到最大？最大值由类成员 maxConnections
			// 指出，如果 maxConnections 为 0 或负数，表示连接数量没有限制。
			// 如果连接数己经达到最大，即退出。
			if (this.maxSize > 0 && this.encryptors.size() >= this.maxSize) {
				break;
			}
			// 增加一个加密器到池中
			encryptors.addElement(newEncryptor());
		}
	}

	/**
	 * 创建一个新的加密类
	 * 
	 * @return
	 */
	private StandardAESIntegerEncryptor newEncryptor() {
		// 创建一个数据库连接
		StandardAESIntegerEncryptor encryptor = new StandardAESIntegerEncryptor();
		return encryptor; // 返回创建的新的数据库连接
	}

	/**
	 * 获取一个加密器
	 * 
	 * @return
	 */
	public synchronized StandardAESIntegerEncryptor getEncryptor() {

		// 确保连接池己被创建
		if (encryptors == null) {
			return null; // 连接池还没创建，则返回 null
		}
		// 获得一个可用的加密器
		StandardAESIntegerEncryptor encryptor = getFreeEncryptor();
		// 如果目前没有可以使用的加密器，即所有的都在使用中
		while (encryptor == null) {
			// 等一会再试
			wait(250);
			// 重新再试，直到获得可用的加密器
			encryptor = getFreeEncryptor();
		}
		// 返回获得的可用的加密器
		return encryptor;

	}

	/**
	 * 通过调用findFreeEncryptor方法查找一个可用加密对象,如果都不可用则创建新的对象
	 * 
	 * @return
	 */
	private StandardAESIntegerEncryptor getFreeEncryptor() {

		// 从池中获得一个可用的加密对象
		StandardAESIntegerEncryptor encryptor = findFreeEncryptor();
		if (encryptor == null) {
			// 如果目前池中没有可用的对象
			// 创建一些新的加密对象
			createEncryptorPool(incrementalSize);
			// 重新从池中查找是否有可用对象
			encryptor = findFreeEncryptor();
			if (encryptor == null) {
				// 如果创建后仍获得不到可用的，则返回 null
				return null;
			}
		}
		return encryptor;
	}

	/**
	 * 
	 * 通过判断encryptor的busy属性确定该对象是否可用 如果没有可用的连接，返回 null
	 * 
	 * @return 返回一个可用的数据库连接
	 * 
	 */
	private StandardAESIntegerEncryptor findFreeEncryptor() {

		StandardAESIntegerEncryptor encryptor = null;
		// 获得池向量中所有的对象
		Enumeration enumerate = encryptors.elements();
		// 遍历所有的对象，看是否有可用的
		while (enumerate.hasMoreElements()) {
			encryptor = (StandardAESIntegerEncryptor) enumerate.nextElement();
			if (!encryptor.isBusy()) {
				// 如果此对象不忙，则获得它并把它设为忙
				encryptor.setBusy(true);
				break; // 己经找到一个可用的连接，退出
			}
		}
		return encryptor;// 返回找到到的可用加密对象
	}

	public void returnEncryptor(StandardAESIntegerEncryptor encryptor) {

		// 确保连接池存在，如果连接没有创建（不存在），直接返回
		if (encryptors == null) {
			System.out.println(" 连接池不存在，无法返回此连接到连接池中 !");
			return;
		}
		// 设置加密对象为可用
		encryptor.setBusy(false);
	}

	public void closeEncryptorPool() {

		// 确保连接池存在，如果不存在，返回
		if (encryptors == null) {
			System.out.println(" 连接池不存在，无法关闭 !");
			return;
		}
		StandardAESIntegerEncryptor encryptor = null;
		Enumeration enumerate = encryptors.elements();
		while (enumerate.hasMoreElements()) {
			encryptor = (StandardAESIntegerEncryptor) enumerate.nextElement();
			// 如果忙，等 5 秒
			if (encryptor.isBusy()) {
				wait(5000); // 等 5 秒
			}
			// 从连接池向量中删除它
			encryptors.removeElement(encryptor);
		}
		// 置连接池为空
		encryptors = null;
	}

	/**
	 * 使程序等待给定的毫秒数
	 * 
	 * @param 给定的毫秒数
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
