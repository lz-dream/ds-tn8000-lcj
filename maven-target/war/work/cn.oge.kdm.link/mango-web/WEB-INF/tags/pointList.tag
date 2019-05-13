<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
--%><%@tag import="com.serotonin.m2m2.Common"%>
<%@include file="/WEB-INF/tags/decl.tagf"%>
<%@attribute name="pointHelpId" %>
    <div id="pointDetails" class="borderDiv marB" style="display:none" >
    <div id="pointProperties" style="display:none"></div> <!-- For tricking the legacy modules to believe this is still in use, it will be "shown" when a data source is saved or viewed -->    
        <table class="wide">
          <tr>
            <td>
              <span class="smallTitle"><fmt:message key="dsEdit.points.details"/></span>
              <c:if test="${!empty pointHelpId}"><tag:help id="${pointHelpId}"/></c:if>
            </td>
            <td align="right">
              <tag:img id="toggleDataPoint" png="icon_ds" onclick="togglePoint()" style="display:none" />
              <tag:img id="pointSaveImg" png="save" onclick="savePoint()" title="common.save"/>
              <tag:img id="pointDeleteImg" png="delete" onclick="deletePoint()" title="common.delete" />
              <tag:img png="emport" title="emport.export" onclick="exportDataPoint()"/>
              <tag:img png="cross" title="common.close" onclick="closePoint()"/>
            </td>
          </tr>
        </table>
        <div id="pointMessage" class="ctxmsg formError"></div>
        
        <%-- [OGE][+][20170404][BEGIN] --%>
        <div id="light" class="white_content">
            <table class="kks_table">
                <tr>
                    <td>
                        <fmt:message key="kdm.url.desc" />
                    </td>
                    <td colspan="3"><input id="kdmUrl" />
                        <a href="javascript:void(0)" onclick="saveKDMUrl();"><img src="/images/save.png" /></a>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="kdm.search.plant" />
                    </td>
                    <td><select id="plantSelect" onchange="getUnit()"></select></td>
                    <td>
                        <fmt:message key="kdm.search.unit" />
                    </td>
                    <td><select id="unitSelect" onchange="searchKKS()"></select></td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="events.search.keywords" />
                    </td>
                    <td colspan="3">
                        <input id="keywords" />
                        <a href="javascript:void(0)" onclick="searchKKS();"><img src="/images/arrow_refresh.png" /></a>
                    </td>
                </tr>
            </table>
            <div style="max-height:550px; overflow-y:auto;">
                <table class="kks_table" id="kksTable">
                    <tr>
                        <th>
                            <fmt:message key="kdm.table.kksname.desc" />
                        </th>
                        <th>KKS</th>
                        <th>
                            <fmt:message key="kdm.table.operation" />
                        </th>
                    </tr>
                </table>
            </div>
            <div style="text-align:center; margin-top:10px; ">
                <a href="javascript:void(0)" onclick="closeKKSWindow(0)">
                    <fmt:message key="kdm.table.choose" />
                </a>
                <a href="javascript:void(0)" onclick="closeKKSWindow(1)">
                    <fmt:message key="kdm.table.nochoose" />
                </a>
            </div>
        </div>
        <div id="fade" class="black_overlay"> </div>
        <%-- [OGE][+][20170404][END] --%>

        <table>
          <tr>
            <td class="formLabelRequired"><fmt:message key="dsEdit.deviceName"/></td>
            <td class="formField"><input id="deviceName" /></td>
          </tr>
          <tr>
            <td class="formLabelRequired"><fmt:message key="dsEdit.points.name"/></td>
            <td class="formField"><input type="text" id="name" /></td>
          </tr>
          <tr>
            <td class="formLabelRequired"><fmt:message key="common.xid"/></td>
            <%-- [OGE][#][20170404][BEGIN] --%>
            <%--
            <td class="formField"><input type="text" id="xid" /></td>
            --%>
            <td class="formField">
              <input type="text" id="xid" />
              <a href="javascript:void(0)" onclick="showKKSWindow();"><img src="/images/copy.png" /></a>
            </td>
            <%-- [OGE][#][20170404][END] --%>
          </tr>
          <tr>
            <td class="formLabel"><fmt:message key="pointEdit.props.permission.read"/></td>
            <td class="formField">
              <input type="text" id="readPermission" class="formLong"/>
              <tag:img png="bullet_down" onclick="permissionUI.viewPermissions('readPermission')"/>
              <tag:help id="permissions"/>
            </td>
          </tr>
          <tr>
            <td class="formLabel"><fmt:message key="pointEdit.props.permission.set"/></td>
            <td class="formField">
              <input type="text" id="setPermission" class="formLong"/>
              <tag:img png="bullet_down" onclick="permissionUI.viewPermissions('setPermission')"/>
            </td>
          </tr>
          <jsp:doBody/>
        </table>
        <div id="extraPointSettings">
        <hr class="styled-hr"></hr>
        <jsp:include page="/WEB-INF/snippet/view/dataPoint/dataPointTemplate.jsp"/>
        <hr class="styled-hr"></hr>
        <jsp:include page="/WEB-INF/snippet/view/dataPoint/pointProperties.jsp" />
        <hr class="styled-hr"></hr>
        <jsp:include page="/WEB-INF/snippet/view/dataPoint/loggingProperties.jsp" />
        <hr class="styled-hr"></hr>
        <jsp:include page="/WEB-INF/snippet/view/dataPoint/valuePurge.jsp" />
        <hr class="styled-hr"></hr>
        <jsp:include page="/WEB-INF/snippet/view/dataPoint/textRenderer.jsp" />
        <hr class="styled-hr"></hr>
        <jsp:include page="/WEB-INF/snippet/view/dataPoint/chartRenderer.jsp" />
        <hr class="styled-hr"></hr>
        <jsp:include page="/WEB-INF/snippet/view/dataPoint/eventDetectors.jsp" />
        </div>
      </div>