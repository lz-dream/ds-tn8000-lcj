/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 *
 * @author Terry Packer
 */
package com.serotonin.m2m2.tcp.dataSource;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;

/**
 * @author Terry Packer
 */
public class TCPDataSourceModel extends AbstractDataSourceModel<TCPStandardDataSourceVO> {

	/**
	 * @param data
	 */
	public TCPDataSourceModel(TCPStandardDataSourceVO data) {
		super(data);
	}

	public TCPDataSourceModel() {
		super(new TCPStandardDataSourceVO());
	}

	@JsonGetter("serverPort")
	public int getServerPort() {
		return this.data.getServerPort();
	}

	@JsonSetter("serverPort")
	public void setServerPort(int serverPort) {
		this.data.setServerPort(serverPort);
	}

	
	/*@JsonGetter("periodNumber")
	public String getPeriodNumber() {
		return  this.data.getPeriodNumber();
	}
	@JsonGetter("periodNumber")
	public void setPeriodNumber(String periodNumber) {
		this.data.setPeriodNumber(periodNumber);
	}


	@JsonGetter("poleNums")
	public String getPoleNums() {
		return this.data.getPoleNums();
	}

	@JsonSetter("poleNums")
	public void setPoleNums(String poleNums) {
		this.data.setPoleNums(poleNums);
	}*/

}
