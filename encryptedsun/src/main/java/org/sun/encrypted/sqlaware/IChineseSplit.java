package org.sun.encrypted.sqlaware;

public interface IChineseSplit {

	/**
	 * 中文分词，分割出第一个字和整个字
	 * 
	 * @param src
	 * @return
	 */
	public abstract String[] splitSearchWord(String src);

}