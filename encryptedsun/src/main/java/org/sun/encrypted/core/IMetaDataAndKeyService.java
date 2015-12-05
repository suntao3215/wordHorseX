package org.sun.encrypted.core;

import java.util.List;

import org.sun.encrypted.pojo.Sec_Columnmeta;
import org.sun.encrypted.pojo.Sec_Tablemeta;


public interface IMetaDataAndKeyService {

	/**
	 * ��ȡ��ǰ�����Կ
	 * 
	 * @return
	 */
	public String getActivityPrimaryKey();

	/**
	 * �������ı�����ȡ��Ӧ��Ԫ����
	 * 
	 * @param plainName
	 * @return
	 */
	public Sec_Tablemeta getTableMetaByPlainName(String plainName);

	/**
	 * �������ı�����ȡ��Ӧ��Ԫ����
	 * 
	 * @param encryptedName
	 *            ���ı���
	 * @return
	 */
	public Sec_Tablemeta getTableMetaByEncryptedName(String encryptedName);

	/**
	 * ��ȡ�����⴦�����Ԫ���� �磺�������ΪSEARCH��ORDER��HOM
	 * 
	 * @param encryptedName
	 *            ���ı���
	 * @return
	 */
	public List<Sec_Columnmeta> getSpecialColumnMetas(String encryptedName);

	/**
	 * ����Ԫ�����е���������
	 * 
	 * @param tablemeta
	 * @return
	 */
	public String decryptEncryptedName(String encryptedName);

	/**
	 * ��������������ȡ��Ӧ��Ԫ����
	 * 
	 * @param plainName
	 * @return
	 */
	public Sec_Columnmeta getColumnMetaByPlainName(int tableID, String plainName);

	/**
	 * ���ݼ��ܵ�����������ȡ��Ӧ��Ԫ����
	 * 
	 * @param plainName
	 * @return
	 */
	public Sec_Columnmeta getColumnMetaByEncryptName(int tableID,
			String encryptName);

	/**
	 * ��û״̬������Կ
	 * 
	 * @return
	 */
	public String getActivityColumnKey(Sec_Columnmeta columnmeta, int rowID);

	/**
	 * ��ȡ�л��Կ
	 * 
	 * @param tableName
	 * @param encryptColumnName
	 * @return
	 */
	public String[] getActivityColumnKeyByMetaData(String tableName,
			String encryptColumnName, int rowID);
}
