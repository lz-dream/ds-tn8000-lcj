package com.serotonin.m2m2.tcp.server;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.tcp.dataSource.TCPPointLocatorVO;
import com.serotonin.m2m2.tcp.handler.MessageUtil;
import com.serotonin.m2m2.tcp.handler.TCPConfig;
import com.serotonin.m2m2.time.CP56Time2a;

import cn.oge.common.wave.compress.util.NumberFormatUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TCPServerHandler extends ChannelInboundHandlerAdapter {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private List<DataPointRT> dataPointRTs;
	private TCPConfig config;
	private static final int MIN_SIZE = 1620;

	public TCPServerHandler(TCPConfig config, List<DataPointRT> dataPointRTs) {
		this.dataPointRTs = dataPointRTs;
		this.config = config;
	}

	/* (non-Javadoc)
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel.ChannelHandlerContext, java.lang.Object)
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf buf = (ByteBuf) msg;
		if (buf == null) {
			return;
		}

		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		buf.release();
		if (!(req[0] == -21 && req[1] == -112 && req[2] == -21 && req[3] == -112 && req[4] == -21 && req[5] == -112) || req.length < MIN_SIZE) {
			logger.error("无效数据");
			return;
		}
		DataPacket dataPacket = new DataPacket();
		byte[] cp56time2a = Arrays.copyOfRange(req, 11, 18);
		dataPacket.setDateTime(new CP56Time2a(cp56time2a).get21CenturyTimestamp());
		dataPacket.setLastPacketTime(-1L);
		dataPacket.setDataLength(NumberFormatUtil.transInt(req, 19));
		dataPacket.setOperating_mode(req[18]);
		byte[] dataBody = new byte[dataPacket.getDataLength()];
		System.arraycopy(req, 23, dataBody, 0, dataPacket.getDataLength());
		dataPacket.setBody(dataBody);
		dataPacket.setUnitID(req[7]);
		if (req[8] == 1) {
			dataPacket.setDataType(HUAKEDataType.Runout);
			dataPacket.setSampling_point(NumberFormatUtil.transShort(dataBody, 0));
			dataPacket.setPeriod(NumberFormatUtil.transShort(dataBody, 2));
			dataPacket.setKey_phase_number(NumberFormatUtil.transShort(dataBody, 4));
			dataPacket.setFast_varibal_number(dataBody[52]);
			dataPacket.setRate(NumberFormatUtil.transFloat(dataBody, 22));
			DataPacket.rate_list.put(req[7], dataPacket.getRate());
		} else {
			if (!DataPacket.rate_list.containsKey(req[7])) {
				return;
			}
			dataPacket.setRate(DataPacket.rate_list.get(req[7]));
			dataPacket.setDataType(HUAKEDataType.Airgap);
			dataPacket.setSampling_point(NumberFormatUtil.transShort(dataBody, 2));
			dataPacket.setPeriod(NumberFormatUtil.transShort(dataBody, 4));
			dataPacket.setKey_phase_number(NumberFormatUtil.transShort(dataBody, 6));
			dataPacket.setAirgap_number(NumberFormatUtil.transShort(dataBody, 24));
			dataPacket.setMagnetic_field_number(NumberFormatUtil.transShort(dataBody, 26));
			dataPacket.setRate(NumberFormatUtil.transFloat(dataBody, 22));
		}
		Short poleNum = this.config.getPoleNums().get(dataPacket.getUnitID() - 1);
		dataPacket.setPole_number(poleNum);
		MessageUtil.getInstance().executeMsg(dataPacket, dataPointRTs);
//		 MessageUtilTest.getInstance().executeMsg(dataPacket, dataPointRTs);
		updateGongkuang(dataPacket.getDateTime(), req[18], dataPacket.getUnitID());
	}

	/**
	 * 更新工况值
	 *
	 * @param time
	 * @param bodyDatum
	 * @param unitId
	 */
	private void updateGongkuang(long time, byte bodyDatum, int unitId) {
		String gongkuang = unitId + "_GK";
		float value = (float) bodyDatum;
		if (dataPointRTs == null) {
			return;
		}
		for (int i = 0; i < dataPointRTs.size(); i++) {
			DataPointRT vo = dataPointRTs.get(i);
			TCPPointLocatorVO tcLocatorVO = vo.getVO().getPointLocator();
			if (tcLocatorVO.getCommSequence().equals(gongkuang)) {
				vo.updatePointValue(new PointValueTime(value, time));
			}
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}

	public static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}
}
