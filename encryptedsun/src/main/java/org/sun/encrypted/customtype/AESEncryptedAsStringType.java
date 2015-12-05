package org.sun.encrypted.customtype;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.hibernate.util.EqualsHelper;
import org.sun.encrypted.core.CommonSQLUtil;
import org.sun.encrypted.core.EncryptNamingParameter;
import org.sun.encrypted.core.IMetaDataAndKeyService;
import org.sun.encrypted.core.SQLStatementAnalysis;
import org.sun.encrypted.core.SpringContextUtil;
import org.sun.encrypted.encryptor.AESEncryptedHelper;
import org.sun.encrypted.sqlaware.IChineseSplit;

@SuppressWarnings("rawtypes")
public class AESEncryptedAsStringType implements UserType, ParameterizedType {

	private static final int sqlType = Types.VARCHAR;
	private static final int[] sqlTypes = new int[] { sqlType };

	private static final String STRING_INSERT = "insert";
	private static final String STRING_SELECT = "select";
	private static final String STRING_UPDATE = "update";

	private static final String TYPE_DET = "DET";
	private static final String TYPE_SEARCH = "SEARCH";
	private static final String TYPE_ASSIST = "ASSIST";

	// 保存当前操作的类名
	private String className;

	// 元数据和密钥管理主服务
	private IMetaDataAndKeyService metaDataAndKeyService;
	// 中文分词
	private IChineseSplit chineseSplit;
	// 通用查询处理器
	private CommonSQLUtil commonSQLUtil;

	protected String convertToString(final Object object) {
		return object == null ? null : object.toString();
	}

	protected Object convertToObject(final String stringValue) {
		return stringValue;
	}

	public int[] sqlTypes() {
		return (int[]) sqlTypes.clone();
	}

	public Class returnedClass() {
		return String.class;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		return EqualsHelper.equals(x, y);
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	/**
	 * 非参数化查询时调用
	 */
	@SuppressWarnings("finally")
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {

		// if (ThreadContextHolder.getUserId() == null) {
		// return rs.getString(names[0]);
		// }

		checkInitialization();
		int rowNumber = 0;

		try {
			Field innerId = owner.getClass().getDeclaredField("id");
			innerId.setAccessible(true);
			rowNumber = innerId.getInt(owner);
		}
		catch (Exception e) {
		}
		finally {
			// 获取列名
			int columnid = rs.findColumn(names[0]);
			ResultSetMetaData rsmd = rs.getMetaData();
			String columnName = rsmd.getColumnName(columnid);
			String[] result = metaDataAndKeyService.getActivityColumnKeyByMetaData(className, columnName, rowNumber);
			String password = result[0];
			String encryptType = result[1];
			if (encryptType.equals(TYPE_ASSIST)) {
				return null;
			}
			final byte[] message = rs.getBytes(names[0]);
			Object resultObject = rs.wasNull() ? null : convertToObject(AESEncryptedHelper.decrypt(message, password));

			return resultObject;
		}

	}

	/**
	 * 参数化语句调用
	 */
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		checkInitialization();

		String valueResult = convertToString(value);
		// 获取执行的SQL语句并解析出列名
		String originalSql = SQLStatementAnalysis.findSQLStatement(st);
		String propertyName = SQLStatementAnalysis.findPropertyName(originalSql, index);
		// 获取SQL语句的操作类型
		String option = SQLStatementAnalysis.findOperationType(originalSql);
		// 当前操作数据的行号
		int rowNumber = 0;

		// 如果是update和insert语句，则获取操作的数据行号
		if (valueResult != null) {
			if (option.equals(STRING_UPDATE)) {
				rowNumber = this.findRowId(valueResult);
				valueResult = this.findRealValue(valueResult, rowNumber);
			}
			else if (option.equals(STRING_INSERT)) {
				rowNumber = commonSQLUtil.findMaxId(className);
			}
		}

		// 根据列名和表名获得活动密钥
		// 数组0位密钥、1位加密类型
		String[] result = metaDataAndKeyService.getActivityColumnKeyByMetaData(className, propertyName, rowNumber);
		String password = result[0];
		String encryptType = result[1];
		if (value == null) {
			// 为非辅助字段辅助，辅助字段一般为null
			if (!encryptType.equals(TYPE_ASSIST)) {
				st.setNull(index, sqlType);
			}
		}
		else {
			if (encryptType != null) {

				if (encryptType.equals(TYPE_DET)) {
					// 类型为DET直接加密后进行赋值
					byte[] encryptedValue = AESEncryptedHelper.encrypt(valueResult, password);
					st.setBytes(index, encryptedValue);
				}
				else if (encryptType.equals(TYPE_SEARCH)) {
					// 类型为SEARCH
					if (option.equals(STRING_INSERT) || option.equals(STRING_UPDATE)) {
						int indexSearch = SQLStatementAnalysis
								.findPropertyIndex(originalSql, propertyName, encryptType);// 获取“属性_SEARCH”所处索引
						String splitWord = this.splitSearchWord(propertyName, valueResult);
						st.setBytes(index, AESEncryptedHelper.encrypt(valueResult, password));// 对原始列名进行赋值
						st.setString(indexSearch, splitWord);// 对“属性_SEARCH”进行赋值
					}
				}
				else {
					// 类型为ASSIST
					if (option.equals(STRING_SELECT)) {
						// 加密查询内容
						String orginalResult = this.replacePercent(valueResult);
						String searchKey = byteToString(AESEncryptedHelper.encrypt(orginalResult, password));
						if (originalSql.contains("like")) {
							orginalResult = "%" + searchKey + "%";
						}
						else {
							orginalResult = searchKey;
						}
						st.setString(index, orginalResult);
					}
				}

			}
			else {
				st.setNull(index, sqlType);
			}

		}
	}

	/**
	 * 根据value值分割出操作行号
	 * 
	 * @param valueString
	 * @return
	 */
	private int findRowId(String valueString) {
		int result = 0;
		Pattern pattern = Pattern.compile("\\[.*?\\]");
		Matcher matcher = pattern.matcher(valueString);
		if (matcher.find()) {
			String length = matcher.group().replaceAll("\\[", "").replaceAll("\\]", "");
			result = Integer.valueOf(length);
		}
		return result;
	}

	/**
	 * 获取原始Value，清除"[ ]"符号
	 * 
	 * @param valueString
	 * @param rowID
	 * @return
	 */
	private String findRealValue(String valueString, int rowID) {
		String result = valueString;
		if (rowID != 0) {
			StringBuilder valueBuilder = new StringBuilder(valueString);
			valueBuilder.delete(valueBuilder.indexOf("["), valueBuilder.length());
			result = valueBuilder.toString();
			valueBuilder = null;
		}
		return result;
	}

	/**
	 * 替换掉模糊查询参数中的百分号
	 * 
	 * @param value
	 * @return
	 */
	private String replacePercent(String value) {
		if (value.contains("%")) {
			value = value.replaceAll("\\%", "");
		}
		return value;
	}

	/**
	 * 进行中文分词
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value_search
	 *            待分解值
	 * @return
	 */
	private String splitSearchWord(String propertyName, String value_search) {

		StringBuilder splitBuilder = new StringBuilder();
		byte[] value_byte = null;
		// 获取“属性_SEARCH”的列密钥
		String[] result2 = metaDataAndKeyService.getActivityColumnKeyByMetaData(className, propertyName + "_"
				+ TYPE_SEARCH, 0);
		// 进行中文分词
		String[] splits = chineseSplit.splitSearchWord(value_search);

		// 对分词结果进行加密，并且构建“属性_SEARCH”值
		for (int i = 0; i < splits.length; i++) {
			value_byte = AESEncryptedHelper.encrypt(splits[i], result2[0]);
			splitBuilder.append(byteToString(value_byte) + ";");
		}
		splitBuilder.deleteCharAt(splitBuilder.lastIndexOf(";"));

		return splitBuilder.toString();
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public boolean isMutable() {
		return false;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		if (value == null) {
			return null;
		}
		return (Serializable) deepCopy(value);
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		if (cached == null) {
			return null;
		}
		return deepCopy(cached);
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	/**
	 * 获取配置文件中设置的参数
	 */
	public void setParameterValues(Properties parameters) {
		final String paramClassName = parameters.getProperty(EncryptNamingParameter.CLASSNAME);
		if (paramClassName != null) {
			className = paramClassName;
		}
	}

	/**
	 * 初始化必要服务类
	 */
	protected final void checkInitialization() {
		// 初始化元数据密钥主服务
		if (metaDataAndKeyService == null) {
			metaDataAndKeyService = (IMetaDataAndKeyService) SpringContextUtil.getApplicationContext().getBean(
					"metaDataAndKeyServiceImpl");
		}
		// 初始化中文分词器
		if (chineseSplit == null) {
			chineseSplit = (IChineseSplit) SpringContextUtil.getApplicationContext().getBean("chineseSplit");
		}
		if (commonSQLUtil == null) {
			commonSQLUtil = (CommonSQLUtil) SpringContextUtil.getApplicationContext().getBean("commonSQLUtil");
		}
	}

	/**
	 * byte数组转换为String
	 * 
	 * @param src
	 * @return
	 */
	private String byteToString(byte[] src) {
		String result = "";
		try {
			result = new String(src, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

}
