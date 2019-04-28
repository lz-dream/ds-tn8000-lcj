package com.serotonin.m2m2.tcp.dwr;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.tcp.dataSource.TCPPointLocatorVO;
import com.serotonin.m2m2.tcp.dataSource.TCPStandardDataSourceVO;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class TCPEditDwr extends DataSourceEditDwr {


    @DwrPermission(user = true)
    public ProcessResult saveDataSource(BasicDataSourceVO basic, int serverPort, String ratedPowers, String poleNums) {
        TCPStandardDataSourceVO ds = (TCPStandardDataSourceVO) Common.getUser().getEditDataSource();
        setBasicProps(ds, basic);

        ds.setServerPort(serverPort);


        ProcessResult processResult = valid(poleNums, ratedPowers);

        if (processResult != null)
            return processResult;

        ds.setRatedPowers(ratedPowers);
        ds.setPoleNums(poleNums);
        return tryDataSourceSave(ds);// 对应dataSourceVO需要有writeObject方法，才能保存；有readObject才能读取
    }

    private ProcessResult valid(String poleNums, String ratedPowers) {

        ProcessResult response = new ProcessResult();
        String[] powers = ratedPowers.split(",");
        for (String power : powers) {
            try {
                Float.valueOf(power);
            } catch (Exception ex) {
                response.addContextualMessage("ratedPower", "tcp.ds.alertMustNums", new Object[0]);
                return response;
            }
        }

        String[] poles = poleNums.split(",");

        for (String pole : poles) {
            try {
                Float.valueOf(pole);
            } catch (Exception ex) {
                response.addContextualMessage("poleNum", "tcp.ds.alertMustNums", new Object[0]);
                return response;
            }
        }
        return null;
    }

    @DwrPermission(user = true)
    public ProcessResult savePointLocator(int id, String xid, String name, TCPPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }
}