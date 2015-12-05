package org.sun.encrypted.pojo;

import java.io.Serializable;

public class HibernateProperty implements Serializable {

	private static final long serialVersionUID = 1255537762411547323L;
	private String propetyName;
	private String dataType;

	public HibernateProperty() {

	}

	public HibernateProperty(String propetyName, String dataType) {
		this.propetyName = propetyName;
		this.dataType = dataType;
	}

	public String getPropetyName() {
		return propetyName;
	}

	public void setPropetyName(String propetyName) {
		this.propetyName = propetyName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

}
