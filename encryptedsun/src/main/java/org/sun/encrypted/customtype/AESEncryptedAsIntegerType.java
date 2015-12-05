package org.sun.encrypted.customtype;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.hibernate.util.EqualsHelper;
import org.sun.encrypted.core.EncryptNamingParameter;
import org.sun.encrypted.core.IMetaDataAndKeyService;
import org.sun.encrypted.core.SQLStatementAnalysis;
import org.sun.encrypted.core.SpringContextUtil;
import org.sun.encrypted.encryptor.AESEncryptedHelper;
import org.sun.encrypted.sqlaware.IHashAlgorithm;

public class AESEncryptedAsIntegerType implements UserType, ParameterizedType {

	static final int sqlType = Types.INTEGER;
	static final int[] sqlTypes = new int[] { sqlType };

	private static final String STRING_INSERT = "insert";
	private static final String STRING_SELECT = "select";
	private static final String STRING_UPDATE = "update";

	private static final String TYPE_DET = "DET";
	private static final String TYPE_ORDER = "ORDER";
	private static final String TYPE_ASSIST = "ASSIST";

	private String className;
	private String dataType;

	// Ԫ���ݺ���Կ����������
	private IMetaDataAndKeyService metaDataAndKeyService;
	// ɢ���㷨
	private IHashAlgorithm hashAlgorithm;

	protected String convertToString(final Object object) {
		return object == null ? null : object.toString();
	}

	protected Object convertToOriginal(final Object object) {

		Object original = null;

		if (this.dataType.equals("Integer")) {
			original = object == null ? 0 : Integer.valueOf(object.toString());
		} else if (this.dataType.equals("Float")) {
			original = object == null ? 0 : Float.valueOf(object.toString());
		} else {
			original = object == null ? 0 : Double.valueOf(object.toString());
		}
		return original;
	}

	public int[] sqlTypes() {
		return (int[]) sqlTypes.clone();
	}

	@SuppressWarnings("rawtypes")
	public Class returnedClass() {
		return Integer.class;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		return EqualsHelper.equals(x, y);
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@SuppressWarnings("finally")
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		checkInitialization();

		int rowID = 0;
		try {
			Field inner = owner.getClass().getDeclaredField("id");
			inner.setAccessible(true);
			rowID = inner.getInt(owner);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// ��ȡ����
			int id = rs.findColumn(names[0]);
			ResultSetMetaData rsmd = rs.getMetaData();
			String columnName = rsmd.getColumnName(id);

			String[] result = metaDataAndKeyService
					.getActivityColumnKeyByMetaData(className, columnName,
							rowID);
			String password = result[0];
			String encryptType = result[1];

			if (encryptType.equals(TYPE_ASSIST)) {
				return 0;
			}

			final byte[] message = rs.getBytes(names[0]);

			Object resultObject = rs.wasNull() ? null : AESEncryptedHelper
					.decryptInteger(message, password, dataType);

			return resultObject;
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {

		checkInitialization();

		// ��ȡִ�е�SQL��䲢����������
		String originalSql = SQLStatementAnalysis.findSQLStatement(st);
		String propertyName = SQLStatementAnalysis.findPropertyName(
				originalSql, index);
		// ���������ͱ�����û��Կ
		String[] result = metaDataAndKeyService.getActivityColumnKeyByMetaData(
				className, propertyName, 0);
		String password = result[0];
		String encryptType = result[1];

		if (value == null) {

			// Ϊ�Ǹ����ֶθ����������ֶ�һ��Ϊnull
			if (!encryptType.equals(TYPE_ASSIST)) {
				st.setNull(index, sqlType);
			}

		} else {
			// ��ȡSQL���Ĳ�������
			String sqlOption = SQLStatementAnalysis
					.findOperationType(originalSql);
			if (null != encryptType && encryptType.equals(TYPE_DET)) {

				// ����ΪDETֱ�Ӽ��ܺ���и�ֵ
				st.setBytes(index, AESEncryptedHelper.encryptInteger(
						convertToOriginal(value), password));

			} else if (null != encryptType && encryptType.equals(TYPE_ORDER)) {
				// ����ΪORDER
				// ��ȡ������_ORDER����������
				int index_order = SQLStatementAnalysis.findPropertyIndex(
						originalSql, propertyName, encryptType);
				if (sqlOption.equals(STRING_INSERT)
						|| sqlOption.equals(STRING_UPDATE)) {

					// ��ԭʼ�������и�ֵ
					st.setBytes(index, AESEncryptedHelper.encryptInteger(
							convertToOriginal(value), password));
					// �ԡ�����_ORDER�����и�ֵ
					st.setInt(index_order, hashAlgorithm.hash(value));
				}
			} else if (null != encryptType && encryptType.equals(TYPE_ASSIST)) {

				// ����ΪASSIST
				if (sqlOption.equals(STRING_SELECT)) {

					st.setInt(index, hashAlgorithm.hash(value));
				}
			}

		}

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

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		if (cached == null) {
			return null;
		}
		return deepCopy(cached);
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	public void setParameterValues(Properties parameters) {

		// ��ȡ����
		final String paramClassName = parameters
				.getProperty(EncryptNamingParameter.CLASSNAME);
		// ��ȡ�������ͣ����а���Integer Double Float�ȣ�
		final String parmDataType = parameters
				.getProperty(EncryptNamingParameter.DATATYPE);

		if (paramClassName != null) {
			this.className = paramClassName;
		}
		if (parmDataType != null) {
			this.dataType = parmDataType;
		} else {
			this.dataType = "Integer";
		}
	}

	protected final void checkInitialization() {

		// ��ʼ��Ԫ������Կ������
		if (metaDataAndKeyService == null) {
			metaDataAndKeyService = (IMetaDataAndKeyService) SpringContextUtil
					.getApplicationContext().getBean(
							"metaDataAndKeyServiceImpl");
		}
		// ��ʼ��ɢ���㷨��
		if (hashAlgorithm == null) {
			hashAlgorithm = (IHashAlgorithm) SpringContextUtil
					.getApplicationContext().getBean("hashAlgorithm");
		}
	}

}
