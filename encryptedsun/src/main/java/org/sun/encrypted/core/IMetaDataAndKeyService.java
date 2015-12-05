package org.sun.encrypted.core;

import java.util.List;

import org.sun.encrypted.pojo.Sec_Columnmeta;
import org.sun.encrypted.pojo.Sec_Tablemeta;


public interface IMetaDataAndKeyService {

	/**
	 * 获取当前活动主密钥
	 * 
	 * @return
	 */
	public String getActivityPrimaryKey();

	/**
	 * 根据明文表名获取对应表元数据
	 * 
	 * @param plainName
	 * @return
	 */
	public Sec_Tablemeta getTableMetaByPlainName(String plainName);

	/**
	 * 根据密文表名获取对应表元数据
	 * 
	 * @param encryptedName
	 *            密文表名
	 * @return
	 */
	public Sec_Tablemeta getTableMetaByEncryptedName(String encryptedName);

	/**
	 * 获取需特殊处理的列元数据 如：加密类别为SEARCH、ORDER、HOM
	 * 
	 * @param encryptedName
	 *            密文表名
	 * @return
	 */
	public List<Sec_Columnmeta> getSpecialColumnMetas(String encryptedName);

	/**
	 * 解密元数据中的密文名称
	 * 
	 * @param tablemeta
	 * @return
	 */
	public String decryptEncryptedName(String encryptedName);

	/**
	 * 根据明文列名获取对应列元数据
	 * 
	 * @param plainName
	 * @return
	 */
	public Sec_Columnmeta getColumnMetaByPlainName(int tableID, String plainName);

	/**
	 * 根据加密的密文列名获取对应列元数据
	 * 
	 * @param plainName
	 * @return
	 */
	public Sec_Columnmeta getColumnMetaByEncryptName(int tableID,
			String encryptName);

	/**
	 * 获得活动状态的列密钥
	 * 
	 * @return
	 */
	public String getActivityColumnKey(Sec_Columnmeta columnmeta, int rowID);

	/**
	 * 获取列活动密钥
	 * 
	 * @param tableName
	 * @param encryptColumnName
	 * @return
	 */
	public String[] getActivityColumnKeyByMetaData(String tableName,
			String encryptColumnName, int rowID);
}
