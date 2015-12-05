package org.sun.encrypted.sqlaware;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultChineseSplit implements IChineseSplit {

	/**
	 * 中文分词，分割出第一个字和整个字
	 * 
	 * @param src
	 * @return
	 */
	public String[] splitSearchWord(String src) {

		String regEx = "[\\u4e00-\\u9fa5]";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(src);

		StringBuilder stringBuilder = new StringBuilder();

		int count = 0;
		while (matcher.find()) {
			if (count == 0) {
				stringBuilder.append(matcher.group(0) + ";" + matcher.group(0));
			} else {
				stringBuilder.append(matcher.group(0));
			}
			count++;
		}

		return stringBuilder.toString().split(";");
	}

}
