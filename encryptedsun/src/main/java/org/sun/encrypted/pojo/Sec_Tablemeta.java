package org.sun.encrypted.pojo;

import java.io.Serializable;

public class Sec_Tablemeta implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1672794462680836784L;

	private int id;
	private String plainName;
	private String encryptName;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPlainName() {
		return plainName;
	}

	public void setPlainName(String plainName) {
		this.plainName = plainName;
	}

	public String getEncryptName() {
		return encryptName;
	}

	public void setEncryptName(String encryptName) {
		this.encryptName = encryptName;
	}

	@Override
	public String toString() {
		return "Id: " + this.id + "  plainName: " + this.plainName
				+ "  encryptName: " + this.encryptName;
	}

}
