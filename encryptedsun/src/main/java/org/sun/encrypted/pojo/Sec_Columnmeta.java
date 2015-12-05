package org.sun.encrypted.pojo;

import java.io.Serializable;

public class Sec_Columnmeta implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2641100908132614805L;

	private int id;
	private String plainName;
	private String encryptName;
	private String encryptType;
	private int tableID;
	private int familyID;
	private int groupID;

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

	public String getEncryptType() {
		return encryptType;
	}

	public void setEncryptType(String encryptType) {
		this.encryptType = encryptType;
	}

	public int getTableID() {
		return tableID;
	}

	public void setTableID(int tableID) {
		this.tableID = tableID;
	}

	public int getFamilyID() {
		return familyID;
	}

	public void setFamilyID(int familyID) {
		this.familyID = familyID;
	}

	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	@Override
	public String toString() {
		return "Id: " + this.id + "  plainName: " + this.plainName
				+ "  encryptName: " + this.encryptName + "  encryptType: "
				+ this.encryptType + "  tableID: " + this.tableID
				+ "  familyID: " + this.familyID;
	}

}
