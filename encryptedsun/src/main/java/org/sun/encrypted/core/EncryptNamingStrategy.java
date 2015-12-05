package org.sun.encrypted.core;

import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.util.StringHelper;
import org.sun.encrypted.pojo.Sec_Columnmeta;
import org.sun.encrypted.pojo.Sec_Tablemeta;


/**
 * 自定义动态表名、列名映射
 * 
 * @author Administrator
 * 
 */
@SuppressWarnings("serial")
public class EncryptNamingStrategy extends ImprovedNamingStrategy {

	// 暂时记录表元数据，在列元数据全部映射完成后，更换为新的表元数据
	private Sec_Tablemeta tablemeta;

	private IMetaDataAndKeyService metaDataAndKeyService;

	public String classToTableName(String className) {
		String returnClass = null;

		// 获取去掉包名的类名
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
	 * 根据列名和表元数据计算对应的数据库字段
	 */
	public String propertyToColumnName(String propertyName) {
		String returnProperty = null;
		Sec_Columnmeta columnmeta = metaDataAndKeyService
				.getColumnMetaByPlainName(this.tablemeta.getId(), propertyName);
		if (columnmeta != null) {
			returnProperty = metaDataAndKeyService
					.decryptEncryptedName(columnmeta.getEncryptName());
		} else {
			System.out.println("找不到对应元数据：propertyName=" + propertyName
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
	 * 依赖注入类
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
