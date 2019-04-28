package com.serotonin.m2m2.tcp.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataPacket {

	private static final int RUNOUT_WAVE_LENGTH = 8192;
	/**
	 * 数据类型
	 */
	private HUAKEDataType dataType;
	/**
	 * 数据时间
	 */
	private long dateTime;

	/**
	 * 数据区
	 */
	private byte[] body; // 数据体

	/**
	 * 最后一次打包时间
	 */
	private long lastPacketTime = -1;

	/**
	 * 每周期采样点数
	 */
	private Short sampling_point;
	/**
	 * 采样周期
	 */
	private Short period;
	
	
	/**
	 * 转速
	 */
	private Float rate;

	/**
	 * 键相个数
	 */
	private Short key_phase_number;
	/**
	 * 数据长度
	 */
	private Integer dataLength;

	/**
	 * 快变量通道数
	 */
	private Byte fast_varibal_number;

	/**
	 * 气息测点数
	 */
	private Short airgap_number;

	/**
	 * 磁场强度测点数
	 */
	private Short magnetic_field_number;

	/**
	 * 磁极个数
	 */
	public static Map<Byte, Float> rate_list = new ConcurrentHashMap<>();

	/**
	 * 工况标识
	 */
	private Byte operating_mode;

	/**
	 * 机组号
	 */
	private Byte unitID;
	
	/**
	 * 磁极数据
	 */
	private Short pole_number;
	

	public String getKey() {
		return this.unitID + this.dataType.getIndex();
	}

	public int getWaveLength() {
		return this.dataType.equals(HUAKEDataType.Airgap) ? (this.sampling_point/2) : RUNOUT_WAVE_LENGTH;
	}
}
