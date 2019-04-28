package com.serotonin.m2m2.tcp.dataSource;

import com.serotonin.m2m2.module.ModelDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractRestModel;

public class TCPPointLocatorModelDefinition extends ModelDefinition {

	public static final String TYPE_NAME = "PL.HUAKE_TCP";
	@Override
	public String getModelKey() {
		return "";
	}

	@Override
	public String getModelTypeName() {
		return TYPE_NAME;
	}

	@Override
	public AbstractRestModel<?> createModel() {
		return new TCPPointLocatorModel();
	}

	@Override
	public boolean supportsClass(Class<?> clazz) {
		return TCPPointLocatorModel.class.equals(clazz);
	}

	@Override
	public Class<? extends AbstractRestModel<?>> getModelClass() {
		return TCPPointLocatorModel.class;
	}

}
