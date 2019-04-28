package com.serotonin.m2m2.tcp.handler;


import com.serotonin.m2m2.tcp.server.WaveSegment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 单个机组缓存
 */
@Data
@AllArgsConstructor
public class UnitDataTicket {

    /**
     * 该机组上次稳态更新的时间
     */
    private volatile long lastUpdateTime;
    /**
     * 该机组下的测点波形数据
     */
    private Map<String, WaveSegment> unitDatas;
    
    
    /**
 * 上次机组状态是否为稳态
 */
private volatile boolean lastStatusStability;
}
