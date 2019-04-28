package com.serotonin.m2m2.tcp.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据类型
 */
@Getter
@AllArgsConstructor
public enum HUAKEDataType {

	/**
	 * 气隙数据
	 */
	Airgap("气隙", "2"),
	/**
	 * 振摆
	 */
	Runout("振摆", "1");
	private String name;
	private String index;

	public void setName(String name) {
		this.name = name;
	}

	public void setIndex(String index) {
		this.index = index;
	}

}
