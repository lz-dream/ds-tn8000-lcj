package com.serotonin.m2m2.tcp.handler;

import cn.oge.common.wave.compress.util.NumberFormatUtil;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.tcp.dataSource.TCPPointLocatorVO;
import com.serotonin.m2m2.tcp.server.DataPacket;
import com.serotonin.m2m2.tcp.server.HUAKEDataType;
import com.serotonin.m2m2.tcp.server.WaveSegment;
import com.serotonin.m2m2.utils.ZLibUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MessageUtil {
    private Map<String, UnitDataTicket> unitTimes = new ConcurrentHashMap<>();
    private static MessageUtil messageUtilNew = null;

    public static MessageUtil getInstance() {
        if (messageUtilNew == null) {
            synchronized (MessageUtil.class) {
                if (messageUtilNew == null)
                    messageUtilNew = new MessageUtil();
            }
        }
        return messageUtilNew;
    }

    // 处理方法
    public void executeMsg(DataPacket message, List<DataPointRT> dataPointRTs) {
        // 由于每次报文是按照机组单独传递的,每台机组之间的数据是有时间差的,所以需要为每台机组设置最后更新的时间
        unitTimes.putIfAbsent(message.getKey(), new UnitDataTicket(-1L, new HashMap<>(), message.getOperating_mode().equals("3")));
        Map<String, DataPointRT> mapPointRT = mapPointRT(dataPointRTs);
        if (CollectionUtils.isEmpty(mapPointRT)) {
            return;
        }
        HUAKEDataType dataType = message.getDataType();
        switch (dataType) {

            case Runout:
                // 解析振摆数据
                executeZHENBAI(mapPointRT, message);
                break;
            case Airgap:
                // 解析气隙数据
                executeQIXI(mapPointRT, message);
                break;
        }
    }

    private Map<String, DataPointRT> mapPointRT(List<DataPointRT> dataPointRTS) {
        if (CollectionUtils.isEmpty(dataPointRTS)) {
            return null;
        }
        Map<String, DataPointRT> maps = new ConcurrentHashMap<>(dataPointRTS.size());
        for (DataPointRT rt : dataPointRTS) {
            TCPPointLocatorVO vo = rt.getVO().getPointLocator();
            maps.put(vo.getCommSequence(), rt);
        }
        return maps;
    }

    // 气隙解析
    private void executeQIXI(Map<String, DataPointRT> mapPointRT, DataPacket dataPacket) {
        String key = dataPacket.getKey();
        AtomicInteger count = new AtomicInteger(1);
        // 解析前四个点
        PointValueTime pointValueTime;
        for (int i = 0; i < 12; i++) {
            DataPointRT pointRT = mapPointRT.get(key + "_" + count.getAndIncrement());
            if (pointRT != null) {
                pointValueTime = new PointValueTime((float) NumberFormatUtil.transShort(dataPacket.getBody(), i * 2), dataPacket.getDateTime());
                pointRT.updatePointValue(pointValueTime);
            }

        }
        DataPointRT airgap_number = mapPointRT.get(key + "_" + count.getAndIncrement());
        if (airgap_number != null) {
            pointValueTime = new PointValueTime((float) dataPacket.getAirgap_number(), dataPacket.getDateTime());
            airgap_number.updatePointValue(pointValueTime);
        }
        DataPointRT magnetic_number = mapPointRT.get(key + "_" + count.getAndIncrement());
        if (magnetic_number != null) {
            pointValueTime = new PointValueTime((float) dataPacket.getMagnetic_field_number(), dataPacket.getDateTime());
            magnetic_number.updatePointValue(pointValueTime);
        }
        // 解析气隙浮点数据
        for (short i = 0; i < dataPacket.getAirgap_number() + dataPacket.getMagnetic_field_number(); i++) {
            for (short j = 0; j < 3; j++) {

                DataPointRT pointRT = mapPointRT.get(key + "_" + count.getAndIncrement());
                if (pointRT != null) {
                    pointValueTime = new PointValueTime(NumberFormatUtil.transFloat(dataPacket.getBody(), i * 12 + j * 4 + 28),
                            dataPacket.getDateTime());
                    pointRT.updatePointValue(pointValueTime);
                }
            }
        }

        try {
            // 振摆波形从顺序42（第一个从1开始）开始
            count.set(42);
            // 解析波形数据
            handleWave(mapPointRT, dataPacket, key, count);
        } catch (Exception ex) {
            log.error("振摆数据处理异常", ex);
            // ex.printStackTrace();
        }

        //解析增补测点912个
        count.set(51);
        for (int i = 0; i < 1002; i++) {
            DataPointRT pointRT = mapPointRT.get(key + "_" + count.getAndIncrement());
            if (pointRT != null) {
                PointValueTime valueTime = new PointValueTime(NumberFormatUtil.transFloat(dataPacket.getBody(), 136 + 4 * 100 * 128 * 9 + i * 4),
                        dataPacket.getDateTime());
                pointRT.updatePointValue(valueTime);
            }
        }
        dataPacket.setBody(null);
    }

    // 振摆解析
    private void executeZHENBAI(Map<String, DataPointRT> mapPointRT, DataPacket dataPacket) {
        String key = dataPacket.getKey();
        AtomicInteger count = new AtomicInteger(1);
        // 解析11个无符号整数
        for (int i = 0; i < 11; i++) {
            DataPointRT pointRT = mapPointRT.get(key + "_" + count.getAndIncrement());
            if (pointRT != null) {
                PointValueTime pointValueTime = new PointValueTime((float) NumberFormatUtil.transShort(dataPacket.getBody(), i * 2),
                        dataPacket.getDateTime());
                pointRT.updatePointValue(pointValueTime);
            }
        }
        // 解析7个浮点数
        for (int i = 0; i < 7; i++) {
            DataPointRT pointRT = mapPointRT.get(key + "_" + count.getAndIncrement());
            if (pointRT != null) {
                PointValueTime pointValueTime = new PointValueTime(NumberFormatUtil.transFloat(dataPacket.getBody(), 22 + i * 4),
                        dataPacket.getDateTime());
                pointRT.updatePointValue(pointValueTime);
            }
        }
        // 解析三个byte
        for (int i = 0; i < 3; i++) {
            DataPointRT pointRT = mapPointRT.get(key + "_" + count.getAndIncrement());
            if (pointRT != null) {
                PointValueTime pointValueTime = new PointValueTime((float) dataPacket.getBody()[50 + i], dataPacket.getDateTime());
                pointRT.updatePointValue(pointValueTime);
            }
        }
        // 解析快变量通道点值
        for (int i = 0; i < dataPacket.getFast_varibal_number(); i++) {
            for (int j = 0; j < 2; j++) {
                DataPointRT pointRT = mapPointRT.get(key + "_" + count.getAndIncrement());
                if (pointRT != null) {
                    PointValueTime pointValueTime = new PointValueTime(
                            NumberFormatUtil.transFloat(dataPacket.getBody(), 53 + i * 8 + j * 4), dataPacket.getDateTime());
                    pointRT.updatePointValue(pointValueTime);
                }
            }
        }
        try {
            // 振摆波形从顺序118（从1开始）开始
            count.set(118);
            handleWave(mapPointRT, dataPacket, key, count);
        } catch (Exception ex) {
            // log.error("振摆数据处理异常", ex);
            ex.printStackTrace();
        }
        //解析增补测点288个
        count.set(166);
        for (int i = 0; i < 288; i++) {
            DataPointRT pointRT = mapPointRT.get(key + "_" + count.getAndIncrement());
            if (pointRT != null) {
                PointValueTime pointValueTime = new PointValueTime(NumberFormatUtil.transFloat(dataPacket.getBody(), 437 + 4 * 48 * 2048 + i * 4),
                        dataPacket.getDateTime());
                pointRT.updatePointValue(pointValueTime);
            }
        }
        dataPacket.setBody(null);
    }

    /**
     * 处理振摆波形数据 波形数据处理方式 1. 如果暂态情况下，状态发生变化时，如稳态变到暂态，暂态变到稳态，则存储波形数据 2.
     * 如果一直稳态状态下，则累计数据达到8192条存储且时间间隔大约10分钟存储
     *
     * @param mapPointRT 已经配置的波形数据
     * @param dataPacket 数据报文
     * @param key        键
     * @param count      计数器
     */
    private synchronized void handleWave(Map<String, DataPointRT> mapPointRT, DataPacket dataPacket, String key, AtomicInteger count) {
        // 获取上一次机组数据包
        UnitDataTicket ticket = unitTimes.get(dataPacket.getKey());
        // 如果转速为0，则是停机状态，不存储波形
        if (dataPacket.getRate() == 0f) {
            ticket.setLastStatusStability(false);
            return;
        }

        boolean isCurrentStability = dataPacket.getOperating_mode() == 3;

        if (!(isCurrentStability == ticket.isLastStatusStability())) {
            // 当状态发生变化时，稳态-》暂态，暂态-》稳态，则打包数据，并且直接写库
            boolean isComplete = packetWave(dataPacket, mapPointRT, ticket.getUnitDatas(), true);
            ticket.setLastStatusStability(isCurrentStability);
            if (isComplete)
                // 如果存储完毕波形数据，则清空波形片段MAP
                ticket.getUnitDatas().clear();
            return;
        }
        // 点值数据长度
        int pointLength = dataPacket.getDataType().equals(HUAKEDataType.Runout) ? 437 : 136;
        int newPointLength = dataPacket.getDataType().equals(HUAKEDataType.Runout) ? 1152 : 3648;
        byte[] waves = Arrays.copyOfRange(dataPacket.getBody(), pointLength, dataPacket.getDataLength() - newPointLength);
//        dataPacket.setBody(null);
        // 稳态下存储
        if (isCurrentStability) {
            // 如果当前测点没有记录过稳态波形数据(或距上次记录的时间已超出10分钟),则直接进行累加

            if (ticket.getLastUpdateTime() == -1 || Common.timer.currentTimeMillis() - ticket.getLastUpdateTime() >= 10 * 60 * 1000) {
                buildWaveSegments(dataPacket, waves, count, mapPointRT, ticket.getUnitDatas());
                boolean isComplete = packetWave(dataPacket, mapPointRT, ticket.getUnitDatas(), false);
                if (isComplete) {
                    if (ticket.getLastUpdateTime() != -1L) {
                        log.debug("lastPacketTime值相差为(s):" + (Common.timer.currentTimeMillis() - ticket.getLastUpdateTime()) / 1000
                                + ",没有超出10分钟");
                    }
                    ticket.setLastUpdateTime(Common.timer.currentTimeMillis());
                    ticket.getUnitDatas().clear();
                }

            } else {
                log.debug("lastPacketTime值为:" + ticket.getLastUpdateTime() + ",没有超出10分钟");
            }
        }
        // 暂态下,震摆只有够8192个点了就存储
        else if (dataPacket.getOperating_mode() == 2) {
            buildWaveSegments(dataPacket, waves, count, mapPointRT, ticket.getUnitDatas());
            boolean isComplete = packetWave(dataPacket, mapPointRT, ticket.getUnitDatas(), false);
            if (isComplete)
                ticket.getUnitDatas().clear();
        }
        ticket.setLastStatusStability(isCurrentStability);
    }

    /**
     * 将各个波形片段添加到各个对象的键值对
     *
     * @param dataPacket 数据区报文
     *                   编码前缀
     * @param count      起止数量
     * @param unitDatas  机组数据
     */
    private void buildWaveSegments(DataPacket dataPacket, byte[] waves, AtomicInteger count, Map<String, DataPointRT> mapPointRT,
                                   Map<String, WaveSegment> unitDatas) {
        WaveSegment segment;
        int wave_number = dataPacket.getDataType().equals(HUAKEDataType.Runout) ? dataPacket.getFast_varibal_number()
                : (dataPacket.getAirgap_number() + dataPacket.getMagnetic_field_number());
        int temp_number = dataPacket.getDataType().equals(HUAKEDataType.Runout) ? 8192 : 51200;
        int fact_number = dataPacket.getDataType().equals(HUAKEDataType.Runout) ? dataPacket.getSampling_point() * dataPacket.getPeriod()
                : dataPacket.getSampling_point();
        //解析华科同安波形数据，存储到WaveSegment
        for (int i = 0; i < wave_number; i++) {
            String newKey = dataPacket.getKey() + "_" + count.getAndIncrement();
            if (mapPointRT.containsKey(newKey)) {
                segment = unitDatas.computeIfAbsent(newKey, k -> {
                    WaveSegment s1 = new WaveSegment();
                    s1.setPoleNum(dataPacket.getPole_number());
                    s1.setData(new ArrayList<>());
                    s1.setWave_length(dataPacket.getWaveLength());
                    return s1;

                });

                segment.setTime(dataPacket.getDateTime());
                List<Float> list = segment.getTimeShift();
                // 周期
                Float time = 60 / dataPacket.getRate();
                Float temp = 0f;
                if (list.size() != 0) {
                    temp = list.get(list.size() - 1);
                }
                for (int j = 1; j <= dataPacket.getKey_phase_number(); j++) {
                    list.add(j * time + temp);
                }
                segment.setTime(dataPacket.getDateTime());
                segment.setTimeShift(list);
                segment.setKeyNum((short) (dataPacket.getKey_phase_number() + segment.getKeyNum()));
                if (dataPacket.getDataType().equals(HUAKEDataType.Runout)) {

                    for (int j = 0; j < fact_number; j++) {

                        if (!segment.isComplete()) {
                            segment.add(NumberFormatUtil.transFloat(waves, i * temp_number + j * 4));
                        }
                    }
                } else {

                    for (int j = 0; j < fact_number; j = j + 2) {

                        if (!segment.isComplete()) {
                            segment.add(NumberFormatUtil.transFloat(waves, i * temp_number + j * 4));
							/*if((j/128f)>30f) {
								
								log.error("index {},  v1 {} ,v2 {} ,v3 {} ,v4 {}",i * temp_number + j * 4,waves[i * temp_number + j * 4],waves[i * temp_number + j * 4+1],waves[i * temp_number + j * 4+2],waves[i * temp_number + j * 4+3]);
								log.error(" P  {}", (j/128f));
							}*/
                        }
                    }
                }
            }
        }
    }

    /**
     * 打包波形并且转换成奥技异的波形格式存储
     *
     * @param dataPacket    数据
     * @param mapPointRT    数据源map
     * @param unitDatas     机组数据
     * @param isDirectWrite 是否是直接写入库
     * @return bool
     */
    private boolean packetWave(DataPacket dataPacket, Map<String, DataPointRT> mapPointRT, Map<String, WaveSegment> unitDatas, boolean isDirectWrite) {

        boolean isComplete = false;
        for (Map.Entry<String, WaveSegment> entry : unitDatas.entrySet()) {

            WaveSegment wave = entry.getValue();
            if (entry.getValue().isComplete() || isDirectWrite) {
                isComplete = true;

                DataPointRT pointRT = mapPointRT.get(entry.getKey());
                AlphanumericValue value = convertToKDMWave(wave);
                wave.clear();
                if (pointRT != null && value != null)
                    pointRT.updatePointValue(new PointValueTime(value, wave.getTime()));
            }
        }
        return isComplete;
    }

    /**
     * @param waveSegment 转换前的波形
     * @return 转换后的波形
     */
    private AlphanumericValue convertToKDMWave(WaveSegment waveSegment) {
        Short key_number = waveSegment.getKeyNum();
        int wave_length = waveSegment.getData().size();
        // 起始标志位+数据包字节数+磁极数目+无效的4个字节+DSP时间戳+数据包时长+键相个数+键相位置+波形数据个数+波形数据+结束标志位
        Integer waveByteNum = 1 + 4 + 1 + 8 + 4 + 4 + key_number * 4 + 4 + wave_length * 4 + 1;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            NumberFormatUtil.pushByte(output, 0x68);// 1 起始标志位
            NumberFormatUtil.pushInt(output, waveByteNum);// 4 数据包字节数
            NumberFormatUtil.pushByte(output, (int) waveSegment.getPoleNum());// 1 磁极数目
            NumberFormatUtil.pushInt(output, 0);// 4 无效字节
            NumberFormatUtil.pushDouble(output, 0);// 8 DSP时间戳
            NumberFormatUtil.pushInt(output, (int) Math.round((waveSegment.getTimeShift().get(key_number - 1) * 1000 * 1000 + 0.5)));// 4
            // 数据包时长;波形的总时长(单位:微秒)
            NumberFormatUtil.pushInt(output, key_number);// 4键相个数

            for (int i = 0; i < key_number; i++) {
                NumberFormatUtil.pushInt(output, Math.round(waveSegment.getTimeShift().get(i) * 1000 * 1000));// 4
            }
            NumberFormatUtil.pushInt(output, wave_length);// 4

            for (float data : waveSegment.getData()) {
                NumberFormatUtil.pushFloat(output, data);// 4
            }

            NumberFormatUtil.pushByte(output, 0x10);// 1
            output.flush();
            byte[] compress = ZLibUtils.compress(output.toByteArray());
            output.close();
            return new AlphanumericValue(Base64.encodeBase64String(compress));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
