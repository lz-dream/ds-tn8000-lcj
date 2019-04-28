<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<script type="text/javascript">
    function initImpl() {

        var pointList = <fmt:message key="tcp.ds.pointList"/>;
        var pointName = document.getElementById("pointName");
        for (var i = 0; i < pointList.length; i++) {
            var option = document.createElement("option");
            option.text = pointList[i].pointName;
            option.value = pointList[i].commSequence;
            plantSelect.add(option, null)

        }
        loadDataSource();
    }

    function appendPointListColumnFunctions(pointListColumnHeaders, pointListColumnFunctions) {
    }

    var cellFuncs = [
        function (data) {
            return data.tag;
        },
        function (data) {
            if (data.dataTypeId == ${applicationScope['constants.DataTypes.BINARY']})
                return "<fmt:message key="common.dataTypes.binary"/>";
            if (data.dataTypeId == ${applicationScope['constants.DataTypes.MULTISTATE']})
                return "<fmt:message key="common.dataTypes.multistate"/>";
            if (data.dataTypeId == ${applicationScope['constants.DataTypes.NUMERIC']})
                return "<fmt:message key="common.dataTypes.numeric"/>";
            if (data.dataTypeId == ${applicationScope['constants.DataTypes.ALPHANUMERIC']})
                return "<fmt:message key="common.dataTypes.alphanumeric"/>";
            return "<fmt:message key="common.unknown"/> (" + data.dataTypeId + ")";
        },
        function (data) {
            return data.settable;
        },
        function (data) {
            return "<input type='checkbox' name='addTag'/>";
        }
    ];

    function editPointCBImpl(locator) {
        $set("dataTypeId", locator.dataTypeId);
        $set("commSequence", locator.commSequence);
    }

    function newDataSource() {
        if (${dataSource.id}!=
        -1
    )
        {
            return false;
        }
        return true;
    }

    function loadDataSource() {

        var poleNums = new String('${dataSource.poleNums}').split(",");
        poleNums.forEach(function (value, index) {

            var name = (index + 1) + '<fmt:message  key="tcp.ds.machineNumber"/>';
            var row = "<tr>" +
                "<td class='formLabelRequired'><span>" + name + "</span></td>" +
                "<td class='formField'><input name='poleNum' value='" + value + "' type='text' /><img class='ptr' src='/images/delete.png' title='删除' onclick='removeRow(this)'></td>" +
                "</tr>";
            jQuery("#tblpoleNum").append(row);
        })

        var ratedPowers = new String('${dataSource.ratedPowers}').split(",");
        ratedPowers.forEach(function (value, index) {

            var name = (index + 1) + '<fmt:message  key="tcp.ds.machineNumber"/>';
            var row = "<tr>" +
                "<td class='formLabelRequired'><span>" + name + "</span></td>" +
                "<td class='formField'><input name='ratedSpeed' value='" + value + "' type='text' /><img class='ptr' src='/images/delete.png' title='删除' onclick='removeRow(this)'></td>" +
                "</tr>";
            jQuery("#tblRatedSpeed").append(row);
        })
    }

    function saveDataSourceImpl(basic) {

        var ratedSpeeds = [];
        jQuery("[name=ratedSpeed]").each(function (index, item) {
            if (!item.value) {
                alert('<fmt:message  key="tcp.ds.alertMustInput"/>');
                ratedSpeeds = [];
                return false;
            }
            ratedSpeeds.push(item.value);
        });

        var poleNums = [];
        jQuery("[name=poleNum]").each(function (index, item) {
            if (!item.value) {
                alert('<fmt:message  key="tcp.ds.alertMustInput"/>');
                poleNums = [];
                return false;
            }
            poleNums.push(item.value);

        });
		if(ratedSpeeds.length==0){
			ratedSpeeds.push("1")
		}
        if (poleNums.length > 0 && ratedSpeeds.length > 0) {
            TCPEditDwr.saveDataSource(basic, $get("serverPort"),
                ratedSpeeds.join(","), poleNums.join(","), saveDataSourceCB);
        }

    }

    function savePointImpl(locator) {
        delete locator.settable;

        locator.dataTypeId = $get("dataTypeId");
        locator.commSequence = $get("commSequence");


        TCPEditDwr.savePointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
    }

    function removeRow(column) {
        var $this = jQuery(column).parent().parent();
        $this.remove();
    }


    /**
     * 增加额定转速
     */
    function addRatedSpeed() {

        var name = (jQuery("#tblRatedSpeed").find('tr').length + 1) + '<fmt:message
        key="tcp.ds.machineNumber"/>';
        var row = "<tr>" +
            "<td class='formLabelRequired'><span>" + name + "</span></td>" +
            "<td class='formField'><input name='ratedSpeed' type='text' />  <img class='ptr' src='/images/delete.png' title='删除' onclick='removeRow(this)'></td>" +
            "</tr>";
        jQuery("#tblRatedSpeed").append(row);
    }
    function addPoleNum() {

        var name = (jQuery("#tblpoleNum").find('tr').length + 1) + '<fmt:message
        key="tcp.ds.machineNumber"/>';
        var row = "<tr>" +
            "<td class='formLabelRequired'><span>" + name + "</span></td>" +
            "<td class='formField'><input name='poleNum' type='text' /> <img class='ptr' src='/images/delete.png' title='删除' onclick='removeRow(this);'></td></td>" +
            "</tr>";
        jQuery("#tblpoleNum").append(row);
    }

</script>

<tag:dataSourceAttrs descriptionKey="tcp.ds.edit">
    <jsp:body>
        <%-- <tr>
            <td class="formLabelRequired"><fmt:message
                    key="tcp.ds.serverIp"/></td>
            <td class="formField"><input id="serverIp" type="text"
                                         value="${dataSource.serverIp}"/></td>
        </tr> --%>
        <tr>
            <td class="formLabelRequired"><fmt:message
                    key="tcp.ds.serverPort"/></td>
            <td class="formField"><input id="serverPort" type="text"
                                         value="${dataSource.serverPort}"/> (7010~7019)
            </td>
        </tr>


        <tr>
            <td class="formField" colspan="2">
                <button id="btnAddRatedSpeed" onclick="addRatedSpeed()"><fmt:message
                        key="tcp.ds.addRatedSpeed"/></button>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <table id="tblRatedSpeed">
                </table>
            </td>
        </tr>


        <tr>
            <td class="formField" colspan="2">
                <button id="btnAddPoleNum" onclick="addPoleNum()"><fmt:message
                        key="tcp.ds.addPoleSpeed"/></button>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <table id="tblpoleNum">
                </table>
            </td>
        </tr>

<%--         <tr>
            <td class="formLabelRequired"><fmt:message
                    key="dsEdit.updatePeriod"/></td>
            <td class="formField">
                <input type="text" id="updatePeriods"
                       value="${dataSource.updatePeriods}" class="formShort"/>
                <tag:timePeriods id="updatePeriodType"
                                 value="${dataSource.updatePeriodType}" ms="true" s="true"
                                 min="true" h="true"/>
            </td>
        </tr> --%>

    </jsp:body>
</tag:dataSourceAttrs>

<tag:pointList>
    <tr>
        <td class="formLabelRequired"><fmt:message
                key="dsEdit.pointDataType"/></td>
        <td class="formField"><tag:dataTypeOptions id="dataTypeId"
                                                   excludeImage="true"/></td>
    </tr>

    <tr>
        <td class="formLabelRequired"><fmt:message
                key="tcp.ds.commSequence"/></td>
        <td class="formField"><input type="text" id="commSequence"/></td>
    </tr>
</tag:pointList>