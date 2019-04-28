/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.tcp;

import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.license.DataSourceTypePointsLimit;
import com.serotonin.m2m2.tcp.dataSource.TCPDataSourceModel;
import com.serotonin.m2m2.tcp.dataSource.TCPStandardDataSourceVO;
import com.serotonin.m2m2.tcp.dwr.TCPEditDwr;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;

public class TCPPDataSourceDefinition extends DataSourceDefinition {
	
	public static final String DATA_SOURCE_TYPE = "HUAKE_TCP";
	
	public void preInitialize() {
		ModuleRegistry.addLicenseEnforcement(new DataSourceTypePointsLimit(getModule().getName(), DATA_SOURCE_TYPE, 20, null));
	}

    @Override
    public String getDataSourceTypeName() {
        return DATA_SOURCE_TYPE;
    }

    /**
     * 同getDataSourceTypeName一起，向数据源列表增加一项选择；数据源的描述名称
     * 获取classes/i18n.properties配置属性，注意该属性名称最好不和其他工程相同，否则覆盖
     */
    @Override
    public String getDescriptionKey() {
        return "tcp.ds";
    }

    /**
     * 点击添加数据源时会调用
     */
    @Override
    protected DataSourceVO<?> createDataSourceVO() {
        return new TCPStandardDataSourceVO();
    }

    @Override
    public String getEditPagePath() {
        return "web/editDs.jsp";
    }

    @Override
    public Class<?> getDwrClass() {
        return TCPEditDwr.class;
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.DataSourceDefinition#getModelClass()
	 */
	@Override
	public Class<? extends AbstractDataSourceModel<?>> getModelClass() {
		return TCPDataSourceModel.class;
	}
}
