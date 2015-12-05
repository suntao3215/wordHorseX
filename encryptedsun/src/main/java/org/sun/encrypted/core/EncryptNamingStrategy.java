package org.sun.encrypted.core;

import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.util.StringHelper;
import org.sun.encrypted.pojo.Sec_Columnmeta;
import org.sun.encrypted.pojo.Sec_Tablemeta;


/**
 * �Զ��嶯̬����������ӳ��
 * 
 * @author Administrator
 * 
 */
@SuppressWarnings("serial")
public class EncryptNamingStrategy extends ImprovedNamingStrategy {

	// ��ʱ��¼��Ԫ���ݣ�����Ԫ����ȫ��ӳ����ɺ󣬸���Ϊ�µı�Ԫ����
	private Sec_Tablemeta tablemeta;

	private IMetaDataAndKeyService metaDataAndKeyService;

	public String classToTableName(String className) {
		String returnClass = null;

		// ��ȡȥ������������
		String noPackageName = StringHelper.unqualify(className).toLowerCase();

		this.tablemeta = metaDataAndKeyService
				.getTableMetaByPlainName(noPackageName.toLowerCase());

		if (this.tablemeta != null) {
			returnClass = metaDataAndKeyService.decryptEncryptedName(tablemeta
					.getEncryptName());
		}

		return returnClass;
	}

	/**
	 * ���������ͱ�Ԫ���ݼ����Ӧ�����ݿ��ֶ�
	 */
	public String propertyToColumnName(String propertyName) {
		String returnProperty = null;
		Sec_Columnmeta columnmeta = metaDataAndKeyService
				.getColumnMetaByPlainName(this.tablemeta.getId(), propertyName);
		if (columnmeta != null) {
			returnProperty = metaDataAndKeyService
					.decryptEncryptedName(columnmeta.getEncryptName());
		} else {
			System.out.println("�Ҳ�����ӦԪ���ݣ�propertyName=" + propertyName
					+ " tablemeta=" + tablemeta);
		}
		return returnProperty;
	}

	public String tableName(String tableName) {
		return tableName;
	}

	public String columnName(String columnName) {
		return columnName;
	}

	public String propertyToTableName(String className, String propertyName) {
		return classToTableName(className) + '_'
				+ propertyToColumnName(propertyName);
	}

	/**
	 * ����ע����
	 * 
	 * @return
	 */

	public void setTablemeta(Sec_Tablemeta tablemeta) {
		this.tablemeta = tablemeta;
	}

	public void setMetaDataAndKeyService(
			IMetaDataAndKeyService metaDataAndKeyService) {
		this.metaDataAndKeyService = metaDataAndKeyService;
	}

}
