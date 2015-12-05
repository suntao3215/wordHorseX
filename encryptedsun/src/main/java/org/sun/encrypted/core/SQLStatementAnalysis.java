package org.sun.encrypted.core;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mysql.jdbc.JDBC4PreparedStatement;

public class SQLStatementAnalysis {

	private static final String STRING_INSERT = "insert";
	private static final String STRING_SELECT = "select";
	private static final String STRING_UPDATE = "update";

	private static final String TYPE_SEARCH = "SEARCH";
	private static final String TYPE_ORDER = "ORDER";

	/**
	 * 通过反射获取PreparedStatement中执行的SQL语句
	 * 
	 * @param st
	 * @return 返回原始SQL语句
	 */
	public static String findSQLStatement(PreparedStatement st) {
		String result = "";
		try {
			// c3p0数据源
			if (st instanceof com.mchange.v2.c3p0.impl.NewProxyPreparedStatement) {
				Field inner = st.getClass().getDeclaredField("inner");
				inner.setAccessible(true);
				com.mysql.jdbc.JDBC4PreparedStatement object = (JDBC4PreparedStatement) inner.get(st);
				result = object.toString();
				result = result.substring(result.indexOf(" ") + 1);
				// dbcp数据源
			}
			else if (st instanceof org.apache.commons.dbcp.DelegatingPreparedStatement) {
				result = st.toString();
				result = result.substring(result.indexOf(" ") + 1);
			}
			/**
			 * 其他数据源有待扩展
			 */
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 根据PreparedStatement中的SQL语句解析出列名
	 * 
	 * @param sql
	 * @param index
	 * @return
	 */
	public static String findPropertyName(String sql, int index) {

		String propertyName = "";

		if (sql.matches("select(.+)")) {
			Pattern patternColumn = Pattern.compile("\\.(\\w+)(|=|\\slike\\s|>|<)(\\*\\*\\sNOT SPECIFIED\\s\\*\\*)");
			Matcher matcherColumn = patternColumn.matcher(sql);
			if (matcherColumn.find()) {
				propertyName = matcherColumn.group(1);
			}
		}
		else if (sql.matches("insert(.+)")) {
			String propertyString = sql.substring(sql.indexOf("(") + 1, sql.indexOf(")"));
			String[] propertyNames = propertyString.split(",");
			propertyName = propertyNames[index - 1].trim();
		}
		else if (sql.matches("update(.+)")) {
			String propertyString = sql.substring(sql.indexOf("set") + 4, sql.indexOf("where"));
			String[] propertyNames = propertyString.split(",");
			String tempProperty = propertyNames[index - 1].trim();
			String[] tempArray = tempProperty.split("=");
			propertyName = tempArray[0];
		}

		return propertyName;
	}

	/**
	 * 根据属性名称获取"属性_SEARCH"所处位置
	 * 
	 * @param wholeString
	 * @param propertyName
	 * @return
	 */
	public static int findPropertyIndex(String sql, String propertyName, String encryptType) {

		// insert into `d68aa7504bc511cecd4f0ad00b70b3d4` (uid, SecurityColumn1,
		// SecurityColumn2, date, SecurityColumn3, SecurityColumn4,
		// SecurityColumn5, SecurityColumn6, SecurityColumn7, SecurityColumn8,
		// SecurityColumn9, SecurityColumn10, SecurityColumn11,
		// SecurityColumn12, SecurityColumn13, SecurityColumn14,
		// SecurityColumn15, SecurityColumn16, SecurityColumn17,
		// SecurityColumn18, SecurityColumn19, SecurityColumn20,
		// SecurityColumn21, SecurityColumn22, SecurityColumn23,
		// SecurityColumn24, SecurityColumn25, SecurityColumn26,
		// SecurityColumn27, SecurityColumn28, SecurityColumn29,
		// SecurityColumn30, SecurityColumn31, SecurityColumn0,
		// SecurityColumn33, SecurityColumn32) values ('yiyuan001ceshi0005',
		// x'356F6E416F74797670624B5974386836656B6E4442413D3D', null, null,
		// null, null, null, null, null, null, null, null, null, null, null,
		// null, null, null, null, null, null, null, null, null, null, null,
		// null, null, null, null, null, null,
		// x'644C756839564152794439654D773945654B7A4B37513D3D', ** NOT SPECIFIED
		// **, ** NOT SPECIFIED **, ** NOT SPECIFIED **)

		int propertyIndex = 0;
		String sqlArray[] = sql.split(" ");
		String postfix = "";

		// 根据加密类型，设置辅助字段postfix的值
		if (encryptType.equals(TYPE_SEARCH)) {
			postfix = "_" + TYPE_SEARCH;
		}
		else if (encryptType.equals(TYPE_ORDER)) {
			postfix = "_" + TYPE_ORDER;
		}

		if (sqlArray[0].equals(STRING_INSERT)) {
			// 如果是INSERT语句
			String propertyString = sql.substring(sql.indexOf("(") + 1, sql.indexOf(")"));
			String[] propertyNames = propertyString.split(",");
			for (int i = 0, len = propertyNames.length; i < len; i++) {
				if (propertyNames[i].trim().equals(propertyName + postfix)) {
					propertyIndex = i + 1;
					break;
				}
			}
		}
		else if (sqlArray[0].equals(STRING_UPDATE)) {
			// 如果是UPDATE语句
			String propertyString = sql.substring(sql.indexOf("set") + 4, sql.indexOf("where"));
			String[] propertyNames = propertyString.split(",");
			for (int i = 0; i < propertyNames.length; i++) {
				if (propertyNames[i].trim().contains(propertyName + postfix)) {
					propertyIndex = i + 1;
					break;
				}
			}
		}
		else if (sqlArray[0].equals(STRING_SELECT)) {
			// 如果是SELECT语句
			String propertyString = sql.substring(sql.indexOf("where") + 6);
			List<String> propertyNames = selectSqlSplit(propertyString);
			for (int i = 0, len = propertyNames.size(); i < len; i++) {
				if (propertyNames.get(i).trim().equals(propertyName)) {
					propertyIndex = i + 1;
					break;
				}
			}
		}
		else {
			System.out.println("拆分SQL语句出错");
		}
		return propertyIndex;
	}

	/**
	 * 获取SQL语句中的密文表名
	 * 
	 * @param sql
	 *            原始SQL语句
	 * @return
	 */
	public static String[] findTableName(String sql) {

		if (sql.matches("insert(.+)")) {
			Pattern patternTable = Pattern.compile("into\\s(\\w+)\\s");
			Matcher matcherTable = patternTable.matcher(sql);
			if (matcherTable.find()) {
				String[] result = new String[1];
				result[0] = matcherTable.group(1);
				return result;
			}
		}
		else if (sql.matches("select(.+)")) {
			Pattern patternTable = Pattern.compile("(from|join)\\s(\\w+)\\s");
			Matcher matcherTable = patternTable.matcher(sql);
			List<String> list = new ArrayList<String>();
			while (matcherTable.find()) {
				String name = matcherTable.group(2);
				if (!list.contains(name)) {
					list.add(name);
				}
			}
			return (String[]) list.toArray(new String[list.size()]);
		}
		else if (sql.matches("update(.+)")) {
			Pattern patternTable = Pattern.compile("update\\s(\\w+)\\s");
			Matcher matcherTable = patternTable.matcher(sql);
			if (matcherTable.find()) {
				String[] result = new String[1];
				result[0] = matcherTable.group(1);
				return result;
			}
		}

		return null;
	}

	/**
	 * 根据SQL语句获取操作类型 如：select、insert等
	 * 
	 * @param sql
	 * @return
	 */
	public static String findOperationType(String sql) {
		Pattern patternOption = Pattern.compile("(\\w+)\\s");
		Matcher matcherOption = patternOption.matcher(sql);
		String result = "";
		if (matcherOption.find()) {
			result = matcherOption.group(1);
		}
		return result;
	}

	/**
	 * 分割以Select开头的语句，返回where语句之后的列名 如：select * from xxx where name = ？ and
	 * createtime >xxxx or createtime < xxxx 则 拆分后的List为
	 * {name,createtime,createtime}
	 * 
	 * @param selectSql
	 * @return
	 */
	private static List<String> selectSqlSplit(String selectSql) {

		List<String> resultList = new ArrayList<String>();
		String temp = selectSql;

		// 去掉SQL语句中的“（”，“）”，order by 和 group by
		if (selectSql.contains("(") || selectSql.contains(")")) {
			temp = temp.replaceAll("\\(", "").replaceAll("\\)", "");
		}
		if (temp.contains("order")) {
			temp = temp.substring(0, temp.indexOf("order"));
		}
		if (temp.contains("group")) {
			temp = temp.substring(0, temp.indexOf("group"));
		}

		// 将SQL语句按照“and”和“or”分割
		String splitByAnd[] = temp.split("and");
		for (int i = 0; i < splitByAnd.length; i++) {

			String splitByOr[] = splitByAnd[i].split("or");

			for (int j = 0; j < splitByOr.length; j++) {

				// 将所有操作符替换为“=”
				String replaceSymbol = splitByOr[j].trim().replace(">=", "=").replace("<=", "=").replace("<>", "=")
						.replace("<", "=").replace(">", "=").replace("like", "=");

				// 用等号分割 如：electricte0_.SecurityColumn0=** NOT SPECIFIED **
				// 并获得第一部分
				String splitByEqual[] = replaceSymbol.split("=");

				// 用“.”分割 如：electricte0_.SecurityColumn0
				// 并获得第二部分,即为列名
				String splitByDot[] = splitByEqual[0].split("\\.");

				resultList.add(splitByDot[1].trim());
			}
		}

		return resultList;
	}

}
