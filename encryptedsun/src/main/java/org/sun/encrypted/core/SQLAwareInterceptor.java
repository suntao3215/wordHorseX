package org.sun.encrypted.core;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

public class SQLAwareInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 1L;

	@Override
	public String onPrepareStatement(String sql) {

		String[] tableName = SQLStatementAnalysis.findTableName(sql);
		try {
			for (int i = 0; i < tableName.length; i++) {
				String string = tableName[i];
				if (!string.contains("`")) {
					sql = sql.replace(string, "`" + string + "`");
				}
			}
			// 如果是查询语句则替换查询列名
			if (sql.matches("select(.+)")) {
				Pattern pattern = Pattern.compile("\\.(SecurityColumn\\d+)\\slike");
				Matcher matcher = pattern.matcher(sql);
				if (matcher.find()) {
					matcher.reset();
					StringBuffer sb = new StringBuffer();
					while (matcher.find()) {
						String result = "." + matcher.group(1) + "_SEARCH like";
						matcher.appendReplacement(sb, result);
					}
					matcher.appendTail(sb);
					sql = sb.toString();
				}
			}
			return super.onPrepareStatement(sql);
		}
		catch (Exception e) {
			// e.printStackTrace();
			return super.onPrepareStatement(sql);
		}
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) {
		for (int i = 0; i < currentState.length; i++) {
			Object state = currentState[i];
			Type type = types[i];
			if (type.getName().equals("com.sun.encrypt.type.AESEncryptedAsStringType")
					|| type.getName().equals("com.sun.encrypt.type.AESEncryptedAsIntegerType")) {
				if (state != null) {
					if (state instanceof String) {
						state = state.toString() + "[" + id + "]";
						currentState[i] = state;
					}
				}
			}
		}
		return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
	}

}
