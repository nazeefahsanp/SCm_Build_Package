
<%--
 whereusedPreprocessUtil.jsp
  Utilities that are needed to remove

  Copyright (c) 1999-2018 Dassault Systemes.
  All Rights Reserved.
  This program contains proprietary and trade secret information
  of MatrixOne, Inc.  Copyright notice is precautionary only and
  does not evidence any actual or intended publication of such program
--%>

<%-- Common Includes --%>
<%@include file = "../common/emxNavigatorTopErrorInclude.inc"%>
<%@include file = "../emxUICommonAppInclude.inc"%>
<%@include file = "../emxUICommonHeaderBeginInclude.inc" %>

<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Enumeration"%>
<%@page import = "java.util.StringTokenizer"%>
<%@page import="com.matrixone.apps.domain.util.FrameworkException"%>

<%@page import="matrix.db.JPO"%>

<%
boolean bIsError = false;

  try
  {	  
		//DELTA
	  String strLanguage = context.getSession().getLanguage();
	  String strObjIdContext = emxGetParameter(request, "objectId");
	 String parentId = emxGetParameter(request, "parentOID");
     String params = "";
	 Enumeration enumParamNames = request.getParameterNames();
     while(enumParamNames.hasMoreElements()) {
         String paramName = (String) enumParamNames.nextElement();
         String paramValue = (String)request.getParameter(paramName);

         if (paramValue != null ){
        	 params += "&" + XSSUtil.encodeForJavaScript(context, paramName) + "=" + XSSUtil.encodeForJavaScript(context, paramValue); 
         }
     }
     Map tempArgMap = new HashMap();

     tempArgMap.put("objectId", strObjIdContext);
     String[] tempArgs = JPO.packArgs(tempArgMap);
    String menuList ="&toolbar="+ (String) JPO.invoke(context,
             "WhereUsed", null, "getWhereUsedMenu", tempArgs,
             String.class);
    
  
      %>
	 

<SCRIPT language="javascript" type="text/javaScript">	
     //XSSOK
     strURL    = "../common/emxIndentedTable.jsp?expandProgram=WhereUsed:getWhereUsed&table=APPWhereUsedHierarchial&freezePane=Name,Revision&sortColumnName=Type&sortDirection=ascending<%=menuList%>&expandLevelFilter=true&expandLevelFilterMenu=APPWhereUsedExpandMenu&relationshipFilter=true&selection=multiple&HelpMarker=emxhelpwhereused&dataType=hierarchy<%=params%>";
        
     document.location.href=strURL;
     </SCRIPT>
         <% 
  }catch(Exception e){
 	    bIsError=true;
 	    session.putValue("error.message", e.getMessage());
  }
  %>
  
  <%@include file = "../common/emxNavigatorBottomErrorInclude.inc"%>
