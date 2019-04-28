package com.serotonin.m2m2.tcp.server;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 波形片段数据(华科同安的数据是2048,需要合并成8192,但是服务器发过来的数据并不是一直都是有序的,需要能够按照时间进行排序
 */
@Data
public class WaveSegment {

	/**
	 * 需要拼的数据长度
	 */
	public int wave_length;
	private long time;
	private List<Float> data;
	/**
	 * 磁极个数
	 */
	private float poleNum;
	private short keyNum;
	/**
	 * 键相偏移量
	 */
	private List<Float> timeShift=new ArrayList<>();
	
	/**
	 * 添加波形数据
	 *
	 * @param value
	 *                  波形点
	 */
	public void add(Float value) {
		data.add(value);
	}

	public boolean isComplete() {
		return this.data.size() == wave_length;
	}

	public void clear() {
		data.clear();
		timeShift.clear();
		keyNum=0;
	}
}
