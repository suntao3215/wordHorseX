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

	// ���浱ǰ����������
	private String className;

	// Ԫ���ݺ���Կ����������
	private IMetaDataAndKeyService metaDataAndKeyService;
	// ���ķִ�
	private IChineseSplit chineseSplit;
	// ͨ�ò�ѯ������
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
	 * �ǲ�������ѯʱ����
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
			// ��ȡ����
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
	 * ������������
	 */
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		checkInitialization();

		String valueResult = convertToString(value);
		// ��ȡִ�е�SQL��䲢����������
		String originalSql = SQLStatementAnalysis.findSQLStatement(st);
		String propertyName = SQLStatementAnalysis.findPropertyName(originalSql, index);
		// ��ȡSQL���Ĳ�������
		String option = SQLStatementAnalysis.findOperationType(originalSql);
		// ��ǰ�������ݵ��к�
		int rowNumber = 0;

		// �����update��insert��䣬���ȡ�����������к�
		if (valueResult != null) {
			if (option.equals(STRING_UPDATE)) {
				rowNumber = this.findRowId(valueResult);
				valueResult = this.findRealValue(valueResult, rowNumber);
			}
			else if (option.equals(STRING_INSERT)) {
				rowNumber = commonSQLUtil.findMaxId(className);
			}
		}

		// ���������ͱ�����û��Կ
		// ����0λ��Կ��1λ��������
		String[] result = metaDataAndKeyService.getActivityColumnKeyByMetaData(className, propertyName, rowNumber);
		String password = result[0];
		String encryptType = result[1];
		if (value == null) {
			// Ϊ�Ǹ����ֶθ����������ֶ�һ��Ϊnull
			if (!encryptType.equals(TYPE_ASSIST)) {
				st.setNull(index, sqlType);
			}
		}
		else {
			if (encryptType != null) {

				if (encryptType.equals(TYPE_DET)) {
					// ����ΪDETֱ�Ӽ��ܺ���и�ֵ
					byte[] encryptedValue = AESEncryptedHelper.encrypt(valueResult, password);
					st.setBytes(index, encryptedValue);
				}
				else if (encryptType.equals(TYPE_SEARCH)) {
					// ����ΪSEARCH
					if (option.equals(STRING_INSERT) || option.equals(STRING_UPDATE)) {
						int indexSearch = SQLStatementAnalysis
								.findPropertyIndex(originalSql, propertyName, encryptType);// ��ȡ������_SEARCH����������
						String splitWord = this.splitSearchWord(propertyName, valueResult);
						st.setBytes(index, AESEncryptedHelper.encrypt(valueResult, password));// ��ԭʼ�������и�ֵ
						st.setString(indexSearch, splitWord);// �ԡ�����_SEARCH�����и�ֵ
					}
				}
				else {
					// ����ΪASSIST
					if (option.equals(STRING_SELECT)) {
						// ���ܲ�ѯ����
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
	 * ����valueֵ�ָ�������к�
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
	 * ��ȡԭʼValue�����"[ ]"����
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
	 * �滻��ģ����ѯ�����еİٷֺ�
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
	 * �������ķִ�
	 * 
	 * @param propertyName
	 *            ������
	 * @param value_search
	 *            ���ֽ�ֵ
	 * @return
	 */
	private String splitSearchWord(String propertyName, String value_search) {

		StringBuilder splitBuilder = new StringBuilder();
		byte[] value_byte = null;
		// ��ȡ������_SEARCH��������Կ
		String[] result2 = metaDataAndKeyService.getActivityColumnKeyByMetaData(className, propertyName + "_"
				+ TYPE_SEARCH, 0);
		// �������ķִ�
		String[] splits = chineseSplit.splitSearchWord(value_search);

		// �Էִʽ�����м��ܣ����ҹ���������_SEARCH��ֵ
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
	 * ��ȡ�����ļ������õĲ���
	 */
	public void setParameterValues(Properties parameters) {
		final String paramClassName = parameters.getProperty(EncryptNamingParameter.CLASSNAME);
		if (paramClassName != null) {
			className = paramClassName;
		}
	}

	/**
	 * ��ʼ����Ҫ������
	 */
	protected final void checkInitialization() {
		// ��ʼ��Ԫ������Կ������
		if (metaDataAndKeyService == null) {
			metaDataAndKeyService = (IMetaDataAndKeyService) SpringContextUtil.getApplicationContext().getBean(
					"metaDataAndKeyServiceImpl");
		}
		// ��ʼ�����ķִ���
		if (chineseSplit == null) {
			chineseSplit = (IChineseSplit) SpringContextUtil.getApplicationContext().getBean("chineseSplit");
		}
		if (commonSQLUtil == null) {
			commonSQLUtil = (CommonSQLUtil) SpringContextUtil.getApplicationContext().getBean("commonSQLUtil");
		}
	}

	/**
	 * byte����ת��ΪString
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
