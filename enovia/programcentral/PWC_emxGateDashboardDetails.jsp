<%--
    Copyright (c) 1992-2018 Dassault Systemes.
    All Rights Reserved.
    This program contains proprietary and trade secret information of MatrixOne,
    Inc.  Copyright notice is precautionary only
    and does not evidence any actual or intended publication of such program

    static const char RCSID[] = $Id$
--%>
<%@include file = "../emxUICommonAppInclude.inc"%>


<%@page import="com.matrixone.apps.domain.util.EnoviaResourceBundle"%>
<%@page import="com.matrixone.apps.program.ProgramCentralUtil"%><html>
<head>

<%@include file = "../common/emxUIConstantsInclude.inc"%>
<!-- [MODIFIED::Aug 30, 2011:S4E:R212:IR-127596V6R2012x::Start]-->
<%@include file = "../emxUICommonHeaderBeginInclude.inc"%>
<!-- [MODIFIED::Aug 30, 2011:S4E:R212:IR-127596V6R2012x::End] -->
<%@include file = "emxProgramCentralGateDashboardMethods.inc"%>

<%@page import="java.util.Map,java.util.HashMap,java.util.Hashtable" %>
<%@page import="com.matrixone.apps.domain.util.MapList" %>
<%@page import="com.matrixone.apps.domain.DomainConstants"%>

<%
	//DELTA
    final String strLang = request.getHeader("Accept-Language");
    String showByTimeLine = EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "ProgramCentral", 
    		"emxProgramCentral.PhaseDashboard.options.showByTimeLine", strLang);
    String showByWBSID = EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "ProgramCentral", 
    		"emxProgramCentral.PhaseDashboard.options.showByWBSID", strLang);
    String waitMsg = EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "ProgramCentral", 
    		"emxProgramCentral.PhaseDashboard.waitMsg", strLang);
    String toolTipbgColor = EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "eServiceApplicationProgramCentral.ToolTipBackGroundColor") ;
	String sTypeGate = EnoviaResourceBundle.getAdminI18NString(context, "Type", ProgramCentralConstants.TYPE_GATE, strLang);

    if(null==toolTipbgColor || "".equals(toolTipbgColor))
        toolTipbgColor = "FFFFFF";    
%>

    <link rel="stylesheet" type="text/css" href="../common/styles/emxUIDefault.css"/>
    <link rel="stylesheet" type="text/css" href="../common/styles/emxUIMenu.css"/>
    <link rel="stylesheet" type="text/css" href="styles/emxUIProgramCentralGateDashboard.css"/>
<link rel="stylesheet" href="../plugins/libs/jqueryui/1.10.3/css/jquery-ui.css">
    <title>Gate Dashboard</title>
</head>
<body onLoad="javascript:sizeDiv();">

	<!-- <div id="tooltip_123" class="xstooltip"></div> -->
	<script src="../common/scripts/jquery-1.9.1.js"></script>
	<script src="../plugins/libs/jqueryui/1.10.3/js/jquery-ui.js"></script>
      
	
<script type="text/javascript">	
function sizeDiv(){
    var dashBoardTable = document.getElementById('gateDashboardTbl');
    if(dashBoardTable && dashBoardTable.offset)
    	   document.getElementById('gateDashboard').style.width = dashBoardTable.offsetWidth + 10 + 'px';
}

    function xstooltip_show (that){
    	var toolTip = arguments[5];
    	$(that).tooltip({
	             items: that,
	             content: toolTip,
	             tooltipClass: "xstooltip",
	             show: false, 
	             hide: false
	         });
    }
    
</script>

<%
    // Initialization
    //
    String selectedOption = emxGetParameter(request,"selectedOption");
    selectedOption = selectedOption==null?"option2":selectedOption;
    HashMap requestMap = new HashMap();
    requestMap.put("selectedOption",selectedOption);
    Locale locale = (Locale)request.getLocale();
    //Added:28-Dec-2010:vf2:R211 PRG:IR-076845
    // [MODIFIED::Jul 8, 2011:s4e:R212:IR-119271V6R2012x::Start]
    String strTimeZone = PersonUtil.getActualTimeZonePreference(context);
    //TimeZone tz = TimeZone.getTimeZone(strTimeZone);     
    TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
    double dbMilisecondsOffset = (double)(-1)*tz.getRawOffset();
    double clientTZOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();
   // [MODIFIED::Jul 8, 2011:s4e:R212:IR-119271V6R2012x::Start]
    String objId = emxGetParameter(request,"objectId") == null?emxGetParameter(request,"parentOID"):emxGetParameter(request,"objectId");
    requestMap.put("projectID",objId);
    //End:28-Dec-2010:vf2:R211 PRG:IR-076845
    MapList mlWBSSortedData = null;
    int numberOfGates = 0;
    int numberOfPhases = 0;
    MapList mlGate = new MapList();
    MapList finalGatePhaseList = new MapList();
    // Get the data
    //
    try
    {
        String[] args = JPO.packArgs(requestMap);
        mlWBSSortedData = (MapList)JPO.invoke(context,"emxGateReport",null,"getStageGateMap",args,MapList.class);

        if(null != mlWBSSortedData && mlWBSSortedData.size() > 0){
            MapList mlMilestones = new MapList();

            for(int i=0;i<mlWBSSortedData.size();i++){
                Map tempMap = (Hashtable) mlWBSSortedData.get(i);
                String type = (String) tempMap.get(SELECT_TYPE);
                String isGate = (String) tempMap.get(ProgramCentralConstants.SELECT_IS_GATE);
                String isPhase = (String) tempMap.get(ProgramCentralConstants.SELECT_IS_PHASE);
                if("TRUE".equalsIgnoreCase(isGate)){
                    numberOfGates++;
                }   // TODO!  Add Stage here, in addition to phase
                else if("TRUE".equalsIgnoreCase(isPhase)){
                    String id = (String) tempMap.get(SELECT_ID);

                    // Get the milestones for this phase, and store them in the final MapList
                    //
                    mlMilestones = getMilestones(context, id);

                    // Update status of milestones (i.e. Late, Complete, etc.)
                    //
                    for(int j=0; j < mlMilestones.size(); j++){
                        Map rowMap = (Hashtable) mlMilestones.get(j);
                        String status = getTaskStatus(context, rowMap);
                        rowMap.put(STATUS, status);
                    }
                    tempMap.put("milestones", mlMilestones);
                    numberOfPhases++;
                }
            }

            // initialized the finalGatePhaseList
         finalGatePhaseList = mlWBSSortedData;

            // Change the finalGatePhaseList structure by calling method modifyFinalMapList
            // Two new parameters gateColor and lastDecision will be added to the gate Maps.
            modifyFinalMapList(context,finalGatePhaseList,mlMilestones,numberOfGates,numberOfPhases);
        }
        else{
            String emptyWBSMessage = EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "ProgramCentral", 
            		"emxProgramCentral.PhaseDashboard.MessageWhenNoPhaseOrGate", strLang);
            %>
            <center><h3><%= emptyWBSMessage %></h3></center>
            <%
            return;
        }
    }
    catch(MatrixException ex){
        ex.printStackTrace();
    }
    catch(Exception e)
    {
        e.printStackTrace();
    }
    finally
    {
    }
    // <h1>Phase-Gate Dashboard</h1>
%>
    <%-- Phase/Gate Dashboard --%>

    <div id="gateDashboard">
        <table class="list" id="gateDashboardTbl">
    <%
            // String buffers needed for building HTML
            //
            StringBuffer strHeaderHTML = new StringBuffer();
            StringBuffer strProgressHTML = new StringBuffer();
            StringBuffer strPhaseGateHTML = new StringBuffer();
            StringBuffer strPhaseGateDetailsHTML = new StringBuffer();

            int gateCount = 1;
            int phaseCount = 0;

            for(int CNT=0;CNT<mlWBSSortedData.size();CNT++)
            {
                Map WBSRowMap = (Map)mlWBSSortedData.get(CNT);
                WBSRowMap = (Map) finalGatePhaseList.get(CNT);
                // System.out.println("************");
                // System.out.println("WBSRowMap = " + WBSRowMap);
                // System.out.println("************");

                // Selectables
                //
                String type = (String) WBSRowMap.get(DomainConstants.SELECT_TYPE);
                String isGate = (String) WBSRowMap.get(ProgramCentralConstants.SELECT_IS_GATE);
                String isPhase = (String) WBSRowMap.get(ProgramCentralConstants.SELECT_IS_PHASE);
                String name = (String) WBSRowMap.get(DomainConstants.SELECT_NAME);
                
                if(name.length()>30)
                {
                	name = name.substring(0, 29)+"...";
                }
                String id   = (String) WBSRowMap.get(DomainConstants.SELECT_ID);
                String currentState = (String) WBSRowMap.get(SELECT_CURRENT);

                // TODO! These should be constants, not defined here
                //
                String startDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]";
                String finishDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]";
                String strStartDate = (String) WBSRowMap.get(startDate);
                String strFinishDate = (String) WBSRowMap.get(finishDate);



                if(null != type && !"".equals(type))
                {
                    String cssClass="";
                    String arrowHere = "";

                    // Tooltip string (phase or gate)
                    //
                    String toolTipString = getToolTipInfo(WBSRowMap, clientTZOffset, locale, strLang);

                    if ("TRUE".equalsIgnoreCase(isGate))
                    {
                        // Save gate info for legend table
                        //
                        mlGate.add(WBSRowMap);

                        // Header
                        //
                        strHeaderHTML.append("<th class=\"gate\">");
                        strHeaderHTML.append(sTypeGate);
                        strHeaderHTML.append(ProgramCentralConstants.SPACE);
                        strHeaderHTML.append( gateCount++);
                        strHeaderHTML.append("</th>");

                        // Progress (where we are in the cycle)
                        //
                        arrowHere = (String)WBSRowMap.get(ISARROWHERE);
                        if (null != arrowHere && !"".equals(arrowHere))
                        {
                            strProgressHTML.append("<td class=\"" + arrowHere + "\">");
                            strProgressHTML.append("<img src=\"../common/images/iconGateCurrentActive.png\"/></td>");
                        }
                        else
                        {
                            strProgressHTML.append("<td></td>");
                        }

                        // Gate
                        //
                        String gateStatus = (String)WBSRowMap.get(STATUS);

                        // CSS class based on status
                        //
                        cssClass = "gate " + gateStatus;

                        strPhaseGateHTML.append("<td><a href='javascript:showModalDialog(\"");
                        strPhaseGateHTML.append("../common/emxTree.jsp?objectId=" + id);
                        strPhaseGateHTML.append("\", \"850\", \"600\")'");
                        strPhaseGateHTML.append(" id=\"gate" + id + "\"");
                        strPhaseGateHTML.append(" onmouseover=\"xstooltip_show(this, 'tooltip_123', " + "'phase" + id + "', 289, 49,'" + toolTipString.replaceAll("'","&quot;") +"');\"");
                        strPhaseGateHTML.append(" class=\"" + cssClass + "\">");
                        strPhaseGateHTML.append("</a></td>");

                        // Gate details
                        //
                        strPhaseGateDetailsHTML.append(getDeliveryChecklistHTML(context,id,strLang));
                    }
                    else if ("TRUE".equalsIgnoreCase(isPhase))
                    {
                        // Header
                        //
                        strHeaderHTML.append("<th>");
                        strHeaderHTML.append(eMatrixDateFormat.getFormattedDisplayDate(strStartDate, clientTZOffset, locale) + " - " +
                            eMatrixDateFormat.getFormattedDisplayDate(strFinishDate, clientTZOffset, locale));
                        strHeaderHTML.append("</th>");

                        // Progress (where we are in the cycle)
                        //
                        arrowHere = (String)WBSRowMap.get(ISARROWHERE);
                        if (null != arrowHere && !"".equals(arrowHere))
                        {
                            strProgressHTML.append("<td class=\"" + arrowHere + "\">");
                            strProgressHTML.append("<img src=\"../common/images/iconGateCurrentActive.png\"/></td>");
                        }
                        else
                        {
                            strProgressHTML.append("<td></td>");
                        }

                        // Phase
                        //
                        String phaseStatus = (String)WBSRowMap.get(STATUS);

                        // CSS class based on status
                        //
                        cssClass = "phase " + phaseStatus;

                        strPhaseGateHTML.append("<td><a href='javascript:showModalDialog(\"");
                        strPhaseGateHTML.append("../common/emxTree.jsp?objectId=" + id);
                        strPhaseGateHTML.append("\", \"850\", \"600\")'");
                        strPhaseGateHTML.append(" id=\"phase" + id + "\"");
                        strPhaseGateHTML.append(" onmouseover=\"xstooltip_show(this, 'tooltip_123', " + "'phase" + id + "', 289, 49,'" + toolTipString.replaceAll("'","&quot;") +"');\"");
                        strPhaseGateHTML.append(" class=\"" + cssClass + "\">");
                        strPhaseGateHTML.append("<span>" + name +   "</span></a></td>");

                        // Phase details
                        //
                        MapList mlMilestones = (MapList)WBSRowMap.get("milestones");
                        strPhaseGateDetailsHTML.append(getMilestonesHTML(context, mlMilestones, clientTZOffset, locale, strLang));

                        phaseCount++;
                    }
                }
            }
    %>
            <tr><%=strHeaderHTML%></tr>
            <tr class="progress"><%=strProgressHTML%></tr>
            <tr class="phase-gate"><%=strPhaseGateHTML%></tr>
            <tr class="phase-gate-details"><%=strPhaseGateDetailsHTML%></tr>
        </table>
    </div><!-- /#gateDashboard -->

    <!-- Gate Table -->
    <%
        // Only display gate legend if there are gates
        //
        if (mlGate.size() > 0)
        {
    %>
            <h2><%=EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "ProgramCentral", "emxProgramCentral.PhaseDashboard.gateLegend", strLang)%></h2>
            <div id="legend">
                <table>
                    <tr>
                        <th><%=EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "ProgramCentral", "emxProgramCentral.Type.Gate", strLang)%></th>
                        <th><%=EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "ProgramCentral", "emxProgramCentral.Common.Name", strLang)%></th>
                        <th><%=EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "ProgramCentral", "emxProgramCentral.Common.State", strLang)%></th>                      
                        <th><%=EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "ProgramCentral", "emxProgramCentral.Common.EstimatedFinishDate", strLang)%></th>
                        <th><%=EnoviaResourceBundle.getProperty(ProgramCentralUtil.getAnonymousContext(), "ProgramCentral", "emxProgramCentral.Common.ActualFinishDate", strLang)%></th>
                    </tr>
                    <%
                        for(int i=0;i < mlGate.size();i++){
                            Map gateMap = (Map) mlGate.get(i);
                            // System.out.println("************");
                            // System.out.println("gateMap = " + gateMap);
                            // System.out.println("************");
                            String policy = (String)gateMap.get(SELECT_POLICY);
                            String state = (String)gateMap.get(SELECT_CURRENT);
                            String gateName = (String)gateMap.get(SELECT_NAME);
                            
                            if(gateName.length()>25)
                            {
                            	gateName = gateName.substring(0, 24)+"...";
                            }
                            
                            String GateEstFinishDate = (String) gateMap.get("attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]");
                            String strEstFinishDate = eMatrixDateFormat.getFormattedDisplayDate(GateEstFinishDate, clientTZOffset, locale);
                            String GateActFinishDate = (String) gateMap.get("attribute[" + DomainConstants.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE+ "]");
                            String strActFinishDate = eMatrixDateFormat.getFormattedDisplayDate(GateActFinishDate, clientTZOffset, locale);

                            // CSS based on status
                            //
                            String cssClass = "\"" + (String)gateMap.get(STATUS) + "\"";
                    %>
                            <tr class="<%=cssClass%>">
                                <td><%=i+1%></td>
                                <td><%=gateName %></td>
                                <td><%=i18nNow.getStateI18NString(policy,state, strLang)%></td>
                                 <td><%=strEstFinishDate %></td>
                                <td><%=strActFinishDate %></td>
                            </tr>
                    <%
                        } //End for
                    %>
                </table>
            </div>
    <%
        }
    %>

<script type="text/javascript">
//XSSOK
    var selectedOption = "<%= selectedOption %>";
    function reloadPage(e){
        var opt = e.id;
        var submitURL = document.location.href;
        if(submitURL.indexOf("selectedOption")!= -1){
            var i = submitURL.lastIndexOf("=");
            submitURL = submitURL.substring(0,i);
            submitURL += "=" + opt;
        }
        else{
            submitURL = submitURL + "&selectedOption=" + opt;
        }
        document.location.href = submitURL;
    }
</script>
</body>
</html>
