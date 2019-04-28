package com.serotonin.m2m2.tcp.dataSource;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.EventDataSource;
import com.serotonin.m2m2.tcp.handler.TCPConfig;
import com.serotonin.m2m2.tcp.server.TCPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPStandardDataSourceRT extends EventDataSource<TCPStandardDataSourceVO> {

    private static final Log LOG = LogFactory.getLog(TCPStandardDataSourceRT.class);

    private final TCPStandardDataSourceVO vo;
    public static final int POINT_READ_EXCEPTION_EVENT = 1;
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 2;
    private TCPServer tcpServer;

    /**
     * 点击激活数据源时调用，然后执行initialize方法
     *
     * @param vo
     */
    public TCPStandardDataSourceRT(TCPStandardDataSourceVO vo) {
        super(vo);
        LOG.info("初始化TCPStandardDataSourceRT");
        this.vo = vo;
    }

    @Override
    public void initialize() {
        LOG.info("initialize TCPMaster");
        TCPConfig config = new TCPConfig(vo.getServerPort(), vo.getPoleNums(), vo.getRatedPowers());
        tcpServer = new TCPServer(config, dataPoints);
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(tcpServer);
        } catch (Exception e) {
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, new Date().getTime(), true,
                    new TranslatableMessage("event.exception2", vo.getName(), e.getMessage()));
            e.printStackTrace();
        }
        super.initialize();
    }

    @Override
    public void terminate() {
        super.terminate();
        try {
            tcpServer.close();
        } catch (Exception e) {
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, new Date().getTime(), true,
                    new TranslatableMessage("event.exception2", vo.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    @Override
    public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {

    }

    @Override
    public void setPointValueImpl(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {

    }
}