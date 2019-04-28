package com.serotonin.m2m2.tcp.dataSource;

import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

public class TCPPointLocatorRT extends PointLocatorRT<TCPPointLocatorVO> {

	private final boolean settable;

	public TCPPointLocatorRT(TCPPointLocatorVO vo, boolean settable) {
		super(vo);
		this.settable = settable;
	}

	@Override
	public boolean isSettable() {
		return settable;
	}

	public TCPPointLocatorVO getVo() {
		return vo;
	}
}
