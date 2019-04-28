package com.serotonin.m2m2.tcp.handler;

import java.util.ArrayList;
import java.util.List;

public class TCPConfig {

	private int serverPort = 60000;
	private List<Float> ratedPowers;
	private List<Short> poleNums;

	private long tryTime = 4000l; // 重连间隔时间

	/**
	 * Socket超时时间，默认30秒 ------T0（超过，重新连接）
	 */
	public int connectTimeOut = 30 * 1000; //
	/**
	 * U格式测试报文发送后，应答的返回超时时间， 默认15秒 ------ T1（超过，重新连接）
	 */
	public long testTimeOut = 15 * 1000l;
	/**
	 * 从站等待主站回S格式的超时时间， 默认10秒 ------ T2 （超过，发送S帧[接收8帧回复1帧]）
	 */
	public long sFormatDataTime = 10 * 1000l;
	/**
	 * 没有实际的数据交换时，任何一端启动U格式测试过程的最大间隔时间，默认20秒 ------T3（超过，发送U帧测试）
	 */
	public long testTimeInterval = 20 * 1000l;

	public TCPConfig(int serverPort, long tryTime) {
		super();
		this.serverPort = serverPort;
		this.tryTime = tryTime;
	}

	public TCPConfig(int serverPort, String poleNums, String ratedPowers) {
		super();
		this.serverPort = serverPort;
		this.ratedPowers = convertToIntList(ratedPowers);
		this.poleNums = convertToShortList(poleNums);
	}

	private List<Float> convertToIntList(String str) {
		String[] arrays = str.split(",");
		List<Float> list = new ArrayList<>();
		for (int i = 0; i < arrays.length; i++) {
			String power = arrays[i];
			Float v = Float.valueOf(power);
			list.add(v);
		}
		return list;
	}

	private List<Short> convertToShortList(String str) {
		String[] arrays = str.split(",");
		List<Short> list = new ArrayList<>();
		for (int i = 0; i < arrays.length; i++) {
			String power = arrays[i];
			Short v = Short.valueOf(power);
			list.add(v);
		}
		return list;
	}

	public TCPConfig(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public long getTryTime() {
		return tryTime;
	}

	public void setTryTime(long tryTime) {
		this.tryTime = tryTime;
	}

	public List<Short> getPoleNums() {
		return poleNums;
	}

	public void setPoleNums(List<Short> poleNums) {
		this.poleNums = poleNums;
	}

}
