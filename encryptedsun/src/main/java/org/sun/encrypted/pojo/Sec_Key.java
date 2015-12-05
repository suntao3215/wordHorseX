package org.sun.encrypted.pojo;

import java.io.Serializable;

public class Sec_Key implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static int STATE_ACTIVITY = 1;
	public static int STATE_WAIT = 0;

	private int id;
	private String secretKey;
	private String startTime;
	private int state;
	private int familyID;
	private int hot;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getFamilyID() {
		return familyID;
	}

	public void setFamilyID(int familyID) {
		this.familyID = familyID;
	}

	public int getHot() {
		return hot;
	}

	public void setHot(int hot) {
		this.hot = hot;
	}

}
