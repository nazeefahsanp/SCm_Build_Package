<%@page import="com.matrixone.apps.framework.ui.UIUtil"%>
<%@page import="com.matrixone.apps.engineering.ChartUtil"%>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@page import = "com.matrixone.apps.domain.*"%>
<%@page import = "com.matrixone.apps.domain.util.*"%>
<%@page import = "com.matrixone.apps.framework.ui.UINavigatorUtil"%>
<%@page import = "matrix.db.JPO"%>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<%@include file = "../emxStyleDefaultInclude.inc"%>

<% 	
	//DELTA
	ChartUtil chartUtil = new ChartUtil(context);
	long startTime = System.currentTimeMillis();
	Map<String,String> barChartData = new HashMap();
	String sMode = emxGetParameter(request, "mode");
	if(UIUtil.isNotNullAndNotEmpty(sMode)){
		if(sMode.equals("Part")){
			barChartData = chartUtil.getStateDataforChartsPart(context);	
		}
		else if(sMode.equals("ChangeOrder")){
			barChartData = chartUtil.getDataforCOCharts(context);	
		}
		else if(sMode.equals("ChangeAction")){
			barChartData = chartUtil.getDataforCACharts(context);	
		}
	}
	
%>

<html>
<body>
value :<%=barChartData%>:
</body>
</html>


