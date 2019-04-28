package com.serotonin.m2m2.tcp.dataSource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;
import com.serotonin.util.SerializationHelper;

public class TCPPointLocatorVO extends AbstractPointLocatorVO<TCPPointLocatorVO> implements JsonSerializable {

	private static final long serialVersionUID = 3376925771929000460L;

	@Override
	public void validate(ProcessResult response, DataPointVO dpvo) {
		super.validate(response, dpvo);
	}

	@JsonProperty
	private boolean settable;
	private static final int version = 2;
	@JsonProperty
	private List<Map<String, String>> nameList;
	@JsonProperty
	private String commSequence; // 通信序号，104通信必须
	private int dataTypeId = DataTypes.NUMERIC;


	@Override
	public TranslatableMessage getConfigurationDescription() {
		return new TranslatableMessage("common.default", dataTypeId + "-" + commSequence);
	}

	@Override
	public boolean isSettable() {
		return false;
	}
	public void setSettable(boolean settable) {
		this.settable = settable;
	}
	@Override
	public void validate(ProcessResult response) {
		if (!DataTypes.CODES.isValidId(this.dataTypeId, new int[0]))
			response.addContextualMessage("dataTypeId", "validate.invalidValue", new Object[0]);
	}
	@Override
	public PointLocatorModel<TCPPointLocatorVO> asModel() {
		return new TCPPointLocatorModel(this);
	}

	public String getCommSequence() {
		return commSequence;
	}

	public void setCommSequence(String commSequence) {
		this.commSequence = commSequence;
	}
	@Override
	public int getDataTypeId() {
		return dataTypeId;
	}

	public void setDataTypeId(int dataTypeId) {
		this.dataTypeId = dataTypeId;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(version);
		out.writeInt(this.dataTypeId);
		out.writeBoolean(settable);
		SerializationHelper.writeSafeUTF(out, this.commSequence);
	}

	private void readObject(ObjectInputStream in) throws IOException {
		int ver = in.readInt();
		if (ver == 1) {
			this.dataTypeId = in.readInt();
			settable = in.readBoolean();
			this.commSequence = SerializationHelper.readSafeUTF(in);
		}
		if (ver == 2) {
			this.dataTypeId = in.readInt();
			 settable = in.readBoolean();
			this.commSequence = SerializationHelper.readSafeUTF(in);
		}
	}
	@Override
	public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
		writeDataType(writer);
	}
	@Override
	public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
		Integer value = readDataType(jsonObject, DataTypes.IMAGE);
		if (value != null)
			dataTypeId = value;
	}


	public List<Map<String, String>> getNameList() {
		return nameList;
	}

	public void setNameList(List<Map<String, String>> nameList) {
		this.nameList = nameList;
	}

	@Override
	public PointLocatorRT<TCPPointLocatorVO> createRuntime() {

		return new TCPPointLocatorRT(this, isSettable());
	}

}
