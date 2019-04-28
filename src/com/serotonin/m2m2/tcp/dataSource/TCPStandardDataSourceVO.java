package com.serotonin.m2m2.tcp.dataSource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.regex.Pattern;

import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;
import com.serotonin.util.SerializationHelper;

/**
 * 对数据源进行实例化
 *
 * @author admin
 */
@JsonEntity
public class TCPStandardDataSourceVO extends DataSourceVO<TCPStandardDataSourceVO> {

	private static final long serialVersionUID = 8807998104177469591L;
	private static final ExportCodes EVENT_CODES = new ExportCodes();

	@JsonProperty
	private int serverPort = 7010;

	@JsonProperty
	private String poleNums;

	@JsonProperty
	private String ratedPowers;

	@Override
	public AbstractDataSourceModel<?> asModel() {
		return new TCPDataSourceModel(this);
	}

	@Override
	public TranslatableMessage getConnectionDescription() {
		return new TranslatableMessage("common.default", "0.0.0.0:" + serverPort);
	}

	@Override
	public TCPPointLocatorVO createPointLocator() {
		return new TCPPointLocatorVO();
	}

	@Override
	public TCPStandardDataSourceRT createDataSourceRT() {
		return new TCPStandardDataSourceRT(this);
	}

	@Override
	public ExportCodes getEventCodes() {
		return EVENT_CODES;
	}

	@Override
	protected void addEventTypes(List<EventTypeVO> eventTypes) {

	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getRatedPowers() {
		return ratedPowers;
	}

	public void setRatedPowers(String ratedPowers) {
		this.ratedPowers = ratedPowers;
	}

	public String getPoleNums() {
		return poleNums;
	}

	public void setPoleNums(String poleNums) {
		this.poleNums = poleNums;
	}

	/**
	 * 判断是否为合法IP
	 *
	 * @param ipAddress
	 * @return
	 */
	public boolean isboolIp(String ipAddress) {
		if (ipAddress == null || ipAddress.trim() == "") {
			return false;
		}
		String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
		return Pattern.compile(ip).matcher(ipAddress).matches();
	}

	/**
	 * 校验数据源保存，各属性是否正常可用
	 */
	public void validate(ProcessResult response) {
		super.validate(response);

		if (this.serverPort <= 0 || this.serverPort > 65535)
			response.addContextualMessage("serverPort", "invalid.port", new Object[0]);

	}

	private static final int SERIAL_VERSION = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(SERIAL_VERSION);
		SerializationHelper.writeSafeUTF(out, this.serverPort + "");
		SerializationHelper.writeSafeUTF(out, this.ratedPowers);
		SerializationHelper.writeSafeUTF(out, this.poleNums);
	}

	private void readObject(ObjectInputStream in) throws IOException {
		int ver = in.readInt();
		if (ver == SERIAL_VERSION) {
			this.serverPort = Integer.parseInt(SerializationHelper.readSafeUTF(in));
			this.ratedPowers = SerializationHelper.readSafeUTF(in);
			this.poleNums = SerializationHelper.readSafeUTF(in);
		}
	}
}