package com.serotonin.m2m2.tcp.dataSource;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;

@CSVEntity(typeName=TCPPointLocatorModelDefinition.TYPE_NAME)
public class TCPPointLocatorModel extends PointLocatorModel<TCPPointLocatorVO> {

	public TCPPointLocatorModel(TCPPointLocatorVO data) {
		super(data);
	}
	
	public TCPPointLocatorModel() {
		super(new TCPPointLocatorVO());
	}

	@Override
	public String getTypeName() {
		return TCPPointLocatorModelDefinition.TYPE_NAME;
	}
	
	@JsonGetter("commSequence")
	@CSVColumnGetter(order=19, header="commSequence")
	public String getCommSequence() {
	    return this.data.getCommSequence();
	}

	@JsonSetter("commSequence")
	@CSVColumnSetter(order=19, header="commSequence")
	public void setCommSequence(String commSequence) {
	    this.data.setCommSequence(commSequence);
	}
	@JsonSetter("dataType")
	@Override
	public void setDataTypeId(String dataType) {
	    this.data.setDataTypeId(DataTypes.CODES.getId(dataType));
	}

	@JsonSetter("settable")
	@Override
	public void setSettable(boolean settable) { 
		this.data.setSettable(settable);
	}	
}
