import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import matrix.db.BusinessObject;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;

import org.apache.log4j.Logger;

import matrix.util.Pattern;
import matrix.util.StringList;
import com.ds.common.PWCConstants;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.costanalytics.CAConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.FrameworkStringResource;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralConstants;


public class PWC_Supplier_Document_Util_mxJPO {

	private static final Logger logger = Logger.getLogger("PWC_Supplier_Document_Util");
	public static final String REL_PROJECTPART = PropertyUtil.getSchemaProperty("relationship_PWC_ProjectPart");
	public static final String STR_ATTR_ROUTE_COMPLETION_ACTION = PropertyUtil.getSchemaProperty("attribute_RouteCompletionAction");
	public static final String STR_ATTR_AUTOSTOP_ON_REJECTION = PropertyUtil.getSchemaProperty("attribute_AutoStopOnRejection" );
	public static final String STR_PRODUCTION_VAULT = PropertyUtil.getSchemaProperty("vault_eServiceProduction");
	public static final String REL_CHANGEAFFECTEDITEM = PropertyUtil.getSchemaProperty("relationship_ChangeAffectedItem");
	public static final String TYPE_PWCSPARTSPECIFICATION = PropertyUtil.getSchemaProperty("type_PWC_PartSpecification");
	public static final String POLICY_STANDARDPART = PropertyUtil.getSchemaProperty("policy_StandardPart");
	public static final String POLICY_ECPART = PropertyUtil.getSchemaProperty("policy_ECPart");
	private static final String RELATIONSHIP_IMPLEMENTED_ITEM = PropertyUtil.getSchemaProperty("relationship_ImplementedItem");
	private static final String ATTR_SUPPLIER_PART_NUMBER_PSPEC = PropertyUtil.getSchemaProperty("attribute_PWCSupplierPartNumber");
	private static final String ATTR_SUPPLIER_PART_NUMBER_PART = PropertyUtil.getSchemaProperty("attribute_PWC_SupplierPartNumber");
	private static final String ATTR_SUPPLIER_NAME = PropertyUtil.getSchemaProperty("attribute_PWCSupplierName");
	private static final String ATTR_ASSOCIATED_PART = PropertyUtil.getSchemaProperty("attribute_PWC_AssociatedPartNumbers");
	private static final String RELATIONSHIP_PROJECT_PART = PropertyUtil.getSchemaProperty("relationship_PWC_ProjectPart");
	private static final String ATTR_PWCPARTSUPPLIERDWG= PropertyUtil.getSchemaProperty("attribute_PWC_SupplierDwg");
	private static final String ATTR_PWCPARTSUPPLIERLIST = PropertyUtil.getSchemaProperty("attribute_PWC_SupplierPartList");
	private static final String ATTR_PWCPARTSUPPLIERTESTPROCEDURE = PropertyUtil.getSchemaProperty("attribute_PWC_SupplierTestProcedure");
	private static final String ATTR_PARTSPECCOLLECTION = PropertyUtil.getSchemaProperty("attribute_PWC_PartSpecCollection");
	/**
	 * This method is used for show Part Number value field in create Form
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns String
	 * @throws Exception if the operation fails
	 */
	public String showpartNumberForSFI(Context context,String[] args) throws Exception
	{
		String strName=DomainConstants.EMPTY_STRING;
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String strParentId= (String)requestMap.get("parentOID");
			if(UIUtil.isNotNullAndNotEmpty(strParentId))
			{
				DomainObject domainObject= DomainObject.newInstance(context, strParentId);
				if(domainObject.isKindOf(context, DomainConstants.TYPE_PART))
				{
					strName=domainObject.getName(context);
				
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return strName;
	}
	/**
	 * This method is used for show Project Number field value in create Form
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns String
	 * @throws Exception if the operation fails
	 */
	public String showProjectNumberForSFI(Context context,String[] args) throws Exception
	{
		String strName=DomainConstants.EMPTY_STRING;
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String strParentId= (String)requestMap.get("parentOID");
			StringList slSelectStatment= new StringList();
			slSelectStatment.add(DomainConstants.SELECT_NAME);
			slSelectStatment.add("to["+RELATIONSHIP_PROJECT_PART+"].from.name");
			if(UIUtil.isNotNullAndNotEmpty(strParentId))
			{
				DomainObject domainObject= DomainObject.newInstance(context, strParentId);
				Map mpObjectInfo=domainObject.getInfo(context,slSelectStatment);
				if(domainObject.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE))
				{
					strName=(String)mpObjectInfo.get(DomainConstants.SELECT_NAME);
				}else if(domainObject.isKindOf(context, DomainConstants.TYPE_PART))
				{
					strName=(String)mpObjectInfo.get("to["+RELATIONSHIP_PROJECT_PART+"].from.name");
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return strName;
	}
	/**
	 * This method is used for Connect SFI Part to Project ,Part and PSPEC
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns void
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void connectSFIToPartAndProject (Context context, String[] args)	throws Exception
	{
		boolean blnPushContext = false;
		String strName=DomainConstants.EMPTY_STRING;
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strParentId= (String)requestMap.get("parentOID");
			//Modified for HEAT-C-16529 - Start
			String strSupplierNameId= (String)requestMap.get("IPSourcesOID");
			//Modified for HEAT-C-16529 - End
			String strRouteTemplateId= (String)requestMap.get("ApprovalRouteTemplateOID");
			String strProjectNumberId= DomainConstants.EMPTY_STRING;
			String strProjectNumbername= DomainConstants.EMPTY_STRING;
			String strPartNumberId= (String)requestMap.get("PartNumberOID");
			String strObjId= (String)paramMap.get("objectId");

			if(UIUtil.isNotNullAndNotEmpty(strObjId))
			{
				DomainObject partObject= DomainObject.newInstance(context, strObjId);
				if(UIUtil.isNotNullAndNotEmpty(strParentId))
				{
					DomainObject parentObject= DomainObject.newInstance(context, strParentId);
					if(parentObject.isKindOf(context, DomainConstants.TYPE_PART))
					{
						//Modified for HEAT-C-16529 - Start
						StringList slObjSelect = new StringList("to["+RELATIONSHIP_PROJECT_PART+"].from.id");
						slObjSelect.add("to["+RELATIONSHIP_PROJECT_PART+"].from.name");
						Map mpObjData = parentObject.getInfo(context, slObjSelect);
						strProjectNumberId=(String)mpObjData.get("to["+RELATIONSHIP_PROJECT_PART+"].from.id");
						strProjectNumbername=(String)mpObjData.get("to["+RELATIONSHIP_PROJECT_PART+"].from.name");
						//Modified for HEAT-C-16529 - End
						DomainRelationship.connect(context,partObject,DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT,parentObject);
					}
					else if(parentObject.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE))
					{
						DomainRelationship.connect(context,parentObject,REL_PROJECTPART,partObject);
						//Added for HEAT-C-16529
						strProjectNumbername = parentObject.getName();
						partObject.setAttributeValue(context,PropertyUtil.getSchemaProperty("attribute_PWC_ProjectNumber"),strProjectNumbername);
					}
				}
				if(UIUtil.isNotNullAndNotEmpty(strSupplierNameId))
				{
					DomainObject supplierObject= DomainObject.newInstance(context, strSupplierNameId);
					DomainRelationship.connect(context,supplierObject,DomainConstants.RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,partObject);
				}
				if(UIUtil.isNotNullAndNotEmpty(strRouteTemplateId))
				{
					DomainObject routeApprovalObject= DomainObject.newInstance(context, strRouteTemplateId);
					DomainRelationship.connect(context,partObject,DomainConstants.RELATIONSHIP_OBJECT_ROUTE,routeApprovalObject);
				}
				
				if(UIUtil.isNotNullAndNotEmpty(strProjectNumberId))
				{
					try{
						ContextUtil.pushContext(context, CAConstants.PERSON_USER_AGENT,emxcommonPushPopShadowAgent_mxJPO.getShadowAgentPassword(context, new String[]{}), null);
						blnPushContext=true;	
						DomainObject projectObject= DomainObject.newInstance(context, strProjectNumberId);
						DomainRelationship.connect(context,projectObject,REL_PROJECTPART,partObject);
						//Added for HEAT-C-16529
						partObject.setAttributeValue(context,PropertyUtil.getSchemaProperty("attribute_PWC_ProjectNumber"),strProjectNumbername);
					}catch(Exception e) {
						e.printStackTrace();
					}
					finally {
						if(blnPushContext)
							ContextUtil.popContext(context);
					}
				}
				if(UIUtil.isNotNullAndNotEmpty(strPartNumberId))
				{
				/*	if(strPartNumberId.contains("|"))
					{
						StringList slList = FrameworkUtil.split(strPartNumberId, "|");
						DomainRelationship.connect(context, partObject, DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT, true,(String[])slList.toArray(new String[]{}));
					}else
					{
						DomainObject parentPartObject= DomainObject.newInstance(context, strPartNumberId);
						DomainRelationship.connect(context,partObject,DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT,parentPartObject);
					} */
					DomainObject parentPartObject= DomainObject.newInstance(context, strPartNumberId);
					DomainRelationship.connect(context,partObject,DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT,parentPartObject);
				}
			}
			PWC_EXCCommon_mxJPO excCommon =new PWC_EXCCommon_mxJPO(context,args);
			excCommon.connectClassificationPendingForXClass(context,args);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	/**
	 * This method is used for create Route and start On 
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns void
	 * @throws Exception if the operation fails
	 */
	public void createRouteOnPromoteToInWork(Context context, String[] args) throws Exception
	{
		String strRouteTemplateId= DomainConstants.EMPTY_STRING;
		String strRouteOwnership= DomainConstants.EMPTY_STRING;
		String strRouteBaseState = DomainConstants.EMPTY_STRING;
		String strRouteBasePolicy=DomainConstants.EMPTY_STRING;
		String strPartPolicy=DomainConstants.EMPTY_STRING;
		String strPartId=DomainConstants.EMPTY_STRING;
		StringList slPartSpecList=new StringList();
		StringList slRouteList=new StringList();
		boolean blnPushContext = false;
		try{
			if (null != args && args.length > 1) {
				String strSFIPartId = args[0];
				if(UIUtil.isNotNullAndNotEmpty(strSFIPartId))
				{
					DomainObject sfiPartObject=DomainObject.newInstance(context,strSFIPartId);
					StringList selectRelStmts = new StringList();
					Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
					relPattern.addPattern(DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT);
					relPattern.addPattern(DomainConstants.RELATIONSHIP_PART_SPECIFICATION);
					StringList selectStmts = new StringList();
					selectStmts.addElement(DomainConstants.SELECT_ID);
					selectStmts.addElement(DomainConstants.SELECT_TYPE);
					selectStmts.addElement(DomainConstants.SELECT_CURRENT);
					selectStmts.addElement(DomainConstants.SELECT_POLICY);
					MapList mlDataList=sfiPartObject.getRelatedObjects(context,// context
							relPattern.getPattern(), // relationship pattern
							DomainConstants.QUERY_WILDCARD, // object pattern
							selectStmts, // object selects
							selectRelStmts, // relationship selects
							false, // to direction
							true, // from direction
							(short) 1, // recursion level
							null, // object where clause
							null, // relationship where clause
							0); 
					
					int mlsize=mlDataList.size();
					if(mlsize>0)
					{
					for (int i = 0; i < mlsize; i++) {
						Map hmdata=(Map)mlDataList.get(i);
						String strType=(String) hmdata.get(DomainConstants.SELECT_TYPE);
						String strState=(String) hmdata.get(DomainConstants.SELECT_CURRENT);
						String strId=(String) hmdata.get(DomainConstants.SELECT_ID);
						String strPolicy=(String) hmdata.get(DomainConstants.SELECT_POLICY);
						
						if(UIUtil.isNotNullAndNotEmpty(strType)&& TYPE_PWCSPARTSPECIFICATION.equalsIgnoreCase(strType))
						{
							if("Preliminary".equalsIgnoreCase(strState)||"Review".equalsIgnoreCase(strState))
							{
							slPartSpecList.add(strId);
							}
						}
						if(UIUtil.isNotNullAndNotEmpty(strType)&& DomainConstants.TYPE_ROUTE_TEMPLATE.equalsIgnoreCase(strType))
						{
							strRouteTemplateId =strId;
						}
						if(UIUtil.isNotNullAndNotEmpty(strType)&& DomainConstants.TYPE_ROUTE.equalsIgnoreCase(strType))
						{
							if(!"Complete".equalsIgnoreCase(strState))
							{
								slRouteList.add(strId);
							}
						}
						/*if(UIUtil.isNotNullAndNotEmpty(strType)&& DomainConstants.TYPE_PART.equalsIgnoreCase(strType))
						{
							if("Preliminary".equalsIgnoreCase(strState)||"Review".equalsIgnoreCase(strState))
							{
							strPartId=strId;
							strPartPolicy=strPolicy;
							}
						}*/
					}
					}
					
					if((slRouteList.size()==0)||(slRouteList.isEmpty()))
					{
						//getting the Route template from Previous Revison
						boolean isRevised=false;
						if(UIUtil.isNullOrEmpty(strRouteTemplateId))
						{
							isRevised=true;
							BusinessObject busPreviousObject= sfiPartObject.getPreviousRevision(context);
							String strPrevObjId=busPreviousObject.getObjectId();
							DomainObject domPrevRevObject =DomainObject.newInstance(context, strPrevObjId);
							strRouteTemplateId=domPrevRevObject.getInfo(context, "from["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"].to["+DomainConstants.TYPE_ROUTE_TEMPLATE+"].id");
						}
						StringList slObjSelectsRT  = new StringList(4);
						slObjSelectsRT.addElement(DomainConstants.SELECT_CURRENT);
						slObjSelectsRT.addElement(DomainConstants.SELECT_DESCRIPTION);
						slObjSelectsRT.addElement("attribute[" + STR_ATTR_AUTOSTOP_ON_REJECTION + "]");
						slObjSelectsRT.addElement("attribute[" + DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE + "]");

						// here is to create domain object of template
						DomainObject domObjRouteTemplate = DomainObject.newInstance(context,strRouteTemplateId);
						if(isRevised)
						{
						DomainRelationship.connect(context,sfiPartObject,DomainConstants.RELATIONSHIP_OBJECT_ROUTE,domObjRouteTemplate);
						}
						Map mapRouteTemplateInfo = (Map) domObjRouteTemplate.getInfo(context,slObjSelectsRT);
						String strRouteTemplateDesc = (String) mapRouteTemplateInfo.get(DomainConstants.SELECT_DESCRIPTION);
						String strRouteTemplateState = (String) mapRouteTemplateInfo.get(DomainConstants.SELECT_CURRENT);
						String strAutoStopOnRejection = (String) mapRouteTemplateInfo.get("attribute[" + STR_ATTR_AUTOSTOP_ON_REJECTION + "]");
						String strRouteBasePurpose = (String) mapRouteTemplateInfo.get("attribute[" + DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE + "]");
						//DomainObject domEngObj = DomainObject.newInstance(context, strtEngDocId);
						strRouteOwnership= sfiPartObject.getInfo(context, DomainConstants.SELECT_OWNER);
						if ("Active".equals(strRouteTemplateState)) {
							String strRoutePolicyAdminAlias = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_POLICY, DomainConstants.POLICY_ROUTE, true);
							String strRouteTypeAdminAlias   = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, DomainConstants.TYPE_ROUTE, true);
							String sRouteId = FrameworkUtil.autoName(context,strRouteTypeAdminAlias, "", strRoutePolicyAdminAlias,STR_PRODUCTION_VAULT);
							Route routeObj = (Route) DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
							routeObj.setId(sRouteId);
							String strRouteName = routeObj.getInfo(context,DomainConstants.SELECT_NAME);
							routeObj.getName();
							String strRouteCompletionAction = FrameworkStringResource.getString("emxFramework.Range.Route_Completion_Action.Promote_Connected_Object", context.getLocale()); // Route Completion Action - Promoted to Connected Object
							strRouteName = "Route_"+ strRouteName;
							routeObj.open(context);
							// Update Route Attributes.
							routeObj.setName(context, strRouteName);
							routeObj.setDescription(context, strRouteTemplateDesc);
							routeObj.setAttributeValue(context,STR_ATTR_ROUTE_COMPLETION_ACTION,strRouteCompletionAction);
							routeObj.setAttributeValue(context,STR_ATTR_AUTOSTOP_ON_REJECTION,strAutoStopOnRejection);
							routeObj.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strRouteBasePurpose);

							// HashMap to carry all the attribute values to be set
							Map routeObjectRelAttrMap = new HashMap();
							//set the all the relationships from created route object
							RelationshipType relationshipType = new RelationshipType(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
							DomainRelationship partRel = null;
							DomainRelationship newRel =null;
							if(UIUtil.isNotNullAndNotEmpty(strSFIPartId))
							{
								strRouteBaseState = "In Work";
								strRouteBasePolicy="PWC_SFI"; 
								routeObjectRelAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE,FrameworkUtil.reverseLookupStateName(context, strRouteBasePolicy, strRouteBaseState));
								routeObjectRelAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_POLICY,FrameworkUtil.getAliasForAdmin(context, "Policy", strRouteBasePolicy, false));
								routeObjectRelAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE, strRouteBasePurpose);
								newRel = routeObj.addFromObject(context,relationshipType, strSFIPartId);
								newRel.setAttributeValues(context, routeObjectRelAttrMap);
							} 

							/*if((strPartPolicy.equalsIgnoreCase(DomainConstants.POLICY_EC_PART) || strPartPolicy.equalsIgnoreCase(POLICY_STANDARDPART)) && UIUtil.isNotNullAndNotEmpty(strPartId))
							{
								strRouteBasePolicy=strPartPolicy;
								strRouteBaseState="Review";
								routeObjectRelAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE,FrameworkUtil.reverseLookupStateName(context, strRouteBasePolicy, strRouteBaseState));
								routeObjectRelAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_POLICY,FrameworkUtil.getAliasForAdmin(context, "Policy", strRouteBasePolicy, false));
								routeObjectRelAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE, strRouteBasePurpose);
								partRel = routeObj.addFromObject(context,relationshipType, strPartId);
								partRel.setAttributeValues(context, routeObjectRelAttrMap);
							}*/
							if(slPartSpecList.size()>0)
							{
								strRouteBasePolicy="Part Specification";
								strRouteBaseState="Review";

								routeObjectRelAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_STATE,FrameworkUtil.reverseLookupStateName(context, strRouteBasePolicy, strRouteBaseState));
								routeObjectRelAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_POLICY,FrameworkUtil.getAliasForAdmin(context, "Policy", strRouteBasePolicy, false));
								routeObjectRelAttrMap.put(DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE, strRouteBasePurpose);
								//Modified for HEAT-C-16529 - Start
								ContextUtil.pushContext(context, CAConstants.PERSON_USER_AGENT,emxcommonPushPopShadowAgent_mxJPO.getShadowAgentPassword(context, new String[]{}), null);
								blnPushContext=true;
								Map hmPartSpecRel=DomainRelationship.connect(context, routeObj, relationshipType,false, (String[])slPartSpecList.toArray(new String[0]));
								
								Iterator productItr = hmPartSpecRel.keySet().iterator();
								while (productItr.hasNext())
								{
									String strProductId = (String)productItr.next();
									String strRelId = (String)hmPartSpecRel.get(strProductId);
									if(UIUtil.isNotNullAndNotEmpty(strRelId))
									{
										DomainRelationship domRel =	DomainRelationship.newInstance(context,strRelId);	
										domRel.setAttributeValues(context, routeObjectRelAttrMap);
									}	

								}
								if (blnPushContext){
									ContextUtil.popContext(context);
									blnPushContext = false;
								}
								//Modified for HEAT-C-16529 - End
							}
							// add member list from template to route
							routeObj.addMembersFromTemplate(context, strRouteTemplateId);
							// Update relationship Object Route Attributes.
							routeObj.setId(sRouteId);
							routeObj.setOwner(context, strRouteOwnership);
							//routeObj.setId(sRouteId);
							routeObj.close(context);
							autoStartRouteForState(context,strSFIPartId);
						}
					}
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}finally{
			//Modified for HEAT-C-16529 - Start
			if(blnPushContext){
				ContextUtil.popContext(context);
			}
		}
		//Modified for HEAT-C-16529 - End
	}

	/**
	 * This trigger action method start a approval route automatically whenever Eng Document promote InReview to InApproval.
	 * @param context The ematrix context of the request.
	 * @param args : contains object id 
	 * @returns void
	 * @throws Exception if the operation fails
	 */
	public void autoStartRouteForState(Context context, String strSFIPartId) throws Exception {
		logger.debug("Start of PWC_Program_Document_Util:autoStartRouteForState");
		boolean blnPushContext = false;
		try {
			if (UIUtil.isNotNullAndNotEmpty(strSFIPartId)) {
				DomainObject domObjEngDoc = DomainObject.newInstance(context,strSFIPartId);
				String strNextState = (String) domObjEngDoc.getInfo(context, DomainConstants.SELECT_CURRENT);
				PWC_RFALifeCycleManagement_mxJPO pwc_RFALifeCycleManagement = new PWC_RFALifeCycleManagement_mxJPO();
				ContextUtil.pushContext(context, CAConstants.PERSON_USER_AGENT,emxcommonPushPopShadowAgent_mxJPO.getShadowAgentPassword(context, new String[]{}), null);
				blnPushContext=true;
				pwc_RFALifeCycleManagement.startRouteOnNextState(context, strSFIPartId,strNextState);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Exception in PWC_Program_Document_Util:autoStartRouteForState()"+ex.getMessage());
			throw ex;
		}finally{
			if(blnPushContext){
				ContextUtil.popContext(context);
			}
		}

		logger.debug("End of PWC_Program_Document_Util:autoStartRouteForState()");
	}

	/**
	 * This trigger action method uses for all SFI Connected object promote to Inwork state and update the attribute value of SFI Object
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns void
	 * @throws Exception if the operation fails
	 */
	public void promoteObjectsToInWork(Context context, String[] args) throws Exception
	{
		StringBuilder sbPartListTilte=new StringBuilder();
		StringBuilder sbDrawingPritTitle=new StringBuilder();
		StringBuilder sbTestProcedureTitle=new StringBuilder();
		try{
			if (null != args && args.length > 1) {
				String strSFIPartId = args[0];
				if(UIUtil.isNotNullAndNotEmpty(strSFIPartId)){
					StringList slSelectable=new StringList(DomainConstants.SELECT_ID);
					slSelectable.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
					slSelectable.add("attribute["+ATTR_PARTSPECCOLLECTION+"]");
					slSelectable.add(DomainObject.SELECT_CURRENT);
					Pattern typePattern = new Pattern(TYPE_PWCSPARTSPECIFICATION);
					Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_PART_SPECIFICATION);
					DomainObject sfiPartObject=DomainObject.newInstance(context,strSFIPartId);
					String StrSFICurrent = sfiPartObject.getInfo(context, DomainConstants.SELECT_CURRENT);
					DomainObject domainObject=DomainObject.newInstance(context);
					MapList	mlDatalist = sfiPartObject.getRelatedObjects(context,// context
							relPattern.getPattern(), // relationship pattern
							typePattern.getPattern(), // object pattern
							slSelectable, // object selects
							null, // relationship selects
							false, // to direction
							true, // from direction
							(short) 1, // recursion level
							null, // object where clause
							null, // relationship where clause
							0); 
					int mlSize=mlDatalist.size();
					if( mlSize>0 ){
						String strPartspecId = DomainConstants.EMPTY_STRING;
						String strPartspecCollection = DomainConstants.EMPTY_STRING;
						String strPartspecTitle = DomainConstants.EMPTY_STRING;
						String strPartspecState= DomainConstants.EMPTY_STRING;
						HashMap ATTR_MAP = new HashMap();
						for(int i=0;i<mlSize;i++)
						{
							Map hmData=(Map) mlDatalist.get(i);
							strPartspecId =(String) hmData.get(DomainConstants.SELECT_ID);
							strPartspecCollection =(String) hmData.get("attribute["+ATTR_PARTSPECCOLLECTION+"]");
							strPartspecTitle =(String) hmData.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
							strPartspecState =(String) hmData.get(DomainObject.SELECT_CURRENT);
							if("Preliminary".equals(strPartspecState) && !"Approved".equals(StrSFICurrent)){
								domainObject.setId(strPartspecId);
								domainObject.promote(context);
							}
							if("Supplier Furnished Information (SFI)".equalsIgnoreCase(strPartspecCollection)){
								if(sbDrawingPritTitle.length()>0){
									sbDrawingPritTitle.append(",").append(strPartspecTitle);
								}else{
									sbDrawingPritTitle.append(strPartspecTitle);
								}
							}else if ("Acceptance Test Procedure (ATP)".equalsIgnoreCase(strPartspecCollection)) {
								if(sbTestProcedureTitle.length()>0){
									sbTestProcedureTitle.append(",").append(strPartspecTitle);
								}else{
									sbTestProcedureTitle.append(strPartspecTitle);
								}
							}else if("Supplier Parts List".equalsIgnoreCase(strPartspecCollection)){
								if(sbPartListTilte.length()>0){
									sbPartListTilte.append(",").append(strPartspecTitle);
								}else{
									sbPartListTilte.append(strPartspecTitle);
								}
							}
						}
						ATTR_MAP.put(ATTR_PWCPARTSUPPLIERDWG, sbDrawingPritTitle.toString());
						ATTR_MAP.put(ATTR_PWCPARTSUPPLIERTESTPROCEDURE, sbTestProcedureTitle.toString());
						ATTR_MAP.put(ATTR_PWCPARTSUPPLIERLIST, sbPartListTilte.toString());
						sfiPartObject.setAttributeValues(context, ATTR_MAP);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	/**
	 * This trigger method uses for check whether EC part connected on SFI Part
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns int : if 0 means action Trigger success
	 * @throws Exception if the operation fails
	 */
	public int checkECPartconnectionWithSFI(Context context,String [] args)
	{
		int returnValue=1;
		String strPrtId=DomainConstants.EMPTY_STRING;
		try
		{
			if (null != args && args.length > 1) {
				String strSFIPartId = args[0];
				if(UIUtil.isNotNullAndNotEmpty(strSFIPartId))
				{
					DomainObject sfiPartObject=DomainObject.newInstance(context,strSFIPartId);
					strPrtId= sfiPartObject.getInfo(context, "from["+DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT+"]");
					if("true".equalsIgnoreCase(strPrtId))
					{
						returnValue=0;
					}
					else
					{
						returnValue=1;
					}
				}
			}
			if(returnValue==1)
			{
				String strMsg= EnoviaResourceBundle.getProperty(context,"emxEngineeringCentralStringResource",context.getLocale(), "emxEngineeringCentral.SFI.ECPART.Connect.Message");
				MqlUtil.mqlCommand(context, "notice '$1'", false, strMsg);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return returnValue;
	}
	/**
	 * This trigger action method uses for demote all objects to Preliminary state.
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns void 
	 * @throws Exception if the operation fails
	 */
	public void demoteObjectsToPreliminary(Context context, String[] args) throws Exception
	{
		StringList slPartSpecList=new StringList();
		boolean isRouteDemote=false; 
		try{
			if (null != args && args.length > 1) {
				String strSFIPartId = args[0];
				if(UIUtil.isNotNullAndNotEmpty(strSFIPartId))
				{
					isRouteDemote=deleteRouteOndemotetoPreliminary(context,args);
					if(isRouteDemote)
					{
					DomainObject sfiPartObject=DomainObject.newInstance(context,strSFIPartId);
					DomainObject partSpecObject=DomainObject.newInstance(context);
					StringList slObjSelect = new StringList();
					slObjSelect.add("from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"|to.current==Review].to["+TYPE_PWCSPARTSPECIFICATION+"].id");
					Hashtable hReltaedObjects =  sfiPartObject.getBusinessObjectData(context, slObjSelect);
					if(!hReltaedObjects.isEmpty() && hReltaedObjects.size()>0 )
					{
						slPartSpecList= (StringList)hReltaedObjects.get("from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"|to.current==Review].to["+TYPE_PWCSPARTSPECIFICATION+"].id");
						for(int i=0;i<slPartSpecList.size();i++)
						{
							String strPartspecId=(String)slPartSpecList.get(i);
							partSpecObject.setId(strPartspecId);
							partSpecObject.demote(context);
						}
					}
				}
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	/**
	 * This methos is use for complete route and task after demote SFI object To Preliminary
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns void 
	 * @throws Exception if the operation fails
	 */
	public boolean completeRouteOndemotetoPreliminary(Context context,String args[]) throws Exception
	{
		boolean isRoutecomplete= false;
		try{
			if (null != args && args.length > 1) {
				String strSFIPartId = args[0];
				StringList slRouteList =new StringList();
				StringList slSelectableList =new StringList();
				String strWhere=DomainConstants.EMPTY_STRING;
				if(UIUtil.isNotNullAndNotEmpty(strSFIPartId))
				{
					DomainObject sfiPartObject=DomainObject.newInstance(context,strSFIPartId);
					StringBuilder sbState=new StringBuilder();
					sbState.append("'").append("In Process").append("'");
					StringList slObjSelect = new StringList();
					slObjSelect.add("from["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"|to.current=="+sbState.toString()+"].to["+DomainConstants.TYPE_ROUTE+"].id");
					Hashtable hReltaedObjects =  sfiPartObject.getBusinessObjectData(context, slObjSelect);
					if(!hReltaedObjects.isEmpty() && hReltaedObjects.size()>0 )
					{
						slRouteList= (StringList)hReltaedObjects.get("from["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"|to.current=="+sbState.toString()+"].to["+DomainConstants.TYPE_ROUTE+"].id");
						int routelistsize=slRouteList.size();
						if(routelistsize>0)
						{
							String strRouteId=(String)slRouteList.get(0);
							Route routeObj = (Route) DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
							routeObj.setId(strRouteId);
							
							slSelectableList.add(DomainConstants.SELECT_ID);
						//code start For complete Task which is connected to Route	
							strWhere="current==Assigned";
							MapList mlTaskList=routeObj.getRelatedObjects(context,// context
									DomainConstants.RELATIONSHIP_ROUTE_TASK, // relationship pattern
									DomainConstants.TYPE_INBOX_TASK, // object pattern
									slSelectableList, // object selects
									null, // relationship selects
									true, // to direction
									false, // from direction
									(short) 1, // recursion level
									strWhere, // object where clause
									null, // relationship where clause
									0); 
							int taskListsize=mlTaskList.size();
							String strTaskId=DomainConstants.EMPTY_STRING;
							DomainObject taskObject=DomainObject.newInstance(context);
							if(taskListsize>0)
							{
							
								for (int i = 0; i < taskListsize; i++) {
									Map hmdata=(Map)mlTaskList.get(i);
									strTaskId=(String)hmdata.get(DomainConstants.SELECT_ID);
									taskObject.setId(strTaskId);
									 ContextUtil.pushContext(context);
									taskObject.setState(context, "Complete");
									 ContextUtil.popContext(context);
								}
							}
							routeObj.setState(context, "Complete");
							routeObj.setAttributeValue(context, "Route Status", "Finished");
							isRoutecomplete=true;
						}
						
					}
				}
			}
		}
		catch(Exception e)
		{
			isRoutecomplete=false;
			e.printStackTrace();
			throw e;
		}
		return isRoutecomplete;
	}
	/**
	 *  This trigger action method uses for demote all objects to Review state.
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns void
	 * @throws Exception if the operation fails
	 */
	public void demoteObjectsToreview(Context context, String[] args) throws Exception
	{
		StringList slPartSpecList=new StringList();
		StringList slPartList=new StringList();
		try{
			if (null != args && args.length > 1) {
				String strSFIPartId = args[0];
				if(UIUtil.isNotNullAndNotEmpty(strSFIPartId))
				{
					DomainObject sfiPartObject=DomainObject.newInstance(context,strSFIPartId);
					DomainObject domainObject=DomainObject.newInstance(context);
					StringList slObjSelect = new StringList();


					slObjSelect.add("from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"|to.current==Approved].to["+TYPE_PWCSPARTSPECIFICATION+"].id");
					slObjSelect.add("from["+DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT+"|to.current==Approved].to["+DomainConstants.TYPE_PART+"].id");
					Hashtable hReltaedObjects =  sfiPartObject.getBusinessObjectData(context, slObjSelect);
					if(!hReltaedObjects.isEmpty() && hReltaedObjects.size()>0 )
					{
						slPartList=(StringList)hReltaedObjects.get("from["+DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT+"|to.current==Approved].to["+DomainConstants.TYPE_PART+"].id");
						slPartSpecList= (StringList)hReltaedObjects.get("from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"|to.current==Approved].to["+TYPE_PWCSPARTSPECIFICATION+"].id");
						if(slPartList.size()>0)
						{
							String strPartId=(String)slPartList.get(0);
							domainObject.setId(strPartId);
							domainObject.demote(context);
							
						}
						for(int i=0;i<slPartSpecList.size();i++)
						{
							String strPartspecId=(String)slPartSpecList.get(i);
							domainObject.setId(strPartspecId);
							domainObject.demote(context);
						}

					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * This methos is use for Update the field values of SFI Part
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns void
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public void updateSFiPartFields(Context context, String[] args) throws Exception {

		String strKey=DomainConstants.EMPTY_STRING;
		String strObjId=DomainConstants.EMPTY_STRING;
		String strNewObjId=DomainConstants.EMPTY_STRING;
		boolean blnPushContext = false;  
		try{
			DomainObject domainObject=DomainObject.newInstance(context);

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap fieldMap = (HashMap)programMap.get("fieldMap");
			HashMap hmSettingsMap = (HashMap)fieldMap.get("settings");
			StringList slAltOID = (StringList)fieldMap.get("field_alt_oid");
			HashMap hmparamMap = (HashMap)programMap.get("paramMap");
			strKey=(String)hmSettingsMap.get("key");
			strObjId=(String)hmparamMap.get("objectId");
			strNewObjId=(String)hmparamMap.get("New OID");

			if(UIUtil.isNotNullAndNotEmpty(strObjId))
			{
				StringList slSelectable=new StringList();
				StringList selectRelStmts=new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
				selectRelStmts.add(DomainConstants.SELECT_RELATIONSHIP_NAME);
				Pattern typePattern = new Pattern(DomainConstants.TYPE_ROUTE_TEMPLATE);
				typePattern.addPattern(DomainConstants.TYPE_PART);
				typePattern.addPattern(DomainConstants.TYPE_PROJECT_SPACE);
				typePattern.addPattern(DomainConstants.TYPE_COMPANY);
				Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
				relPattern.addPattern(DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT);
				relPattern.addPattern(DomainConstants.RELATIONSHIP_MANUFACTURING_RESPONSIBILITY);
				relPattern.addPattern(REL_PROJECTPART);
				domainObject.setId(strObjId);
				//String strPartNumberRelId=DomainConstants.EMPTY_STRING;
				String strSupplierNameRelId=DomainConstants.EMPTY_STRING;
				String strProjectNumberRelId=DomainConstants.EMPTY_STRING;
				String strRouteTemplateRelId=DomainConstants.EMPTY_STRING;
				MapList	mlDatalist = domainObject.getRelatedObjects(context,// context
						relPattern.getPattern(), // relationship pattern
						typePattern.getPattern(), // object pattern
						slSelectable, // object selects
						selectRelStmts, // relationship selects
						true, // to direction
						true, // from direction
						(short) 1, // recursion level
						null, // object where clause
						null, // relationship where clause
						0); 
				int mlSize=mlDatalist.size();
				if( mlSize>0 ) {
					for(int i=0;i<mlSize;i++) {
						Map hmData=(Map) mlDatalist.get(i);
						String strRelName =(String) hmData.get(DomainConstants.SELECT_RELATIONSHIP_NAME);
						String strRelId=(String) hmData.get(DomainConstants.SELECT_RELATIONSHIP_ID);
						/*if(strRelName.equalsIgnoreCase(DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT)) {
							strPartNumberRelId=strRelId;
						}*/ if(strRelName.equalsIgnoreCase(DomainConstants.RELATIONSHIP_MANUFACTURING_RESPONSIBILITY)) {
							strSupplierNameRelId=strRelId;
						}else if(strRelName.equalsIgnoreCase(DomainConstants.RELATIONSHIP_OBJECT_ROUTE)) {
							strRouteTemplateRelId=strRelId;
						} else if(strRelName.equalsIgnoreCase(REL_PROJECTPART)) {
							strProjectNumberRelId=strRelId;
						}

					}
				}
				if(UIUtil.isNotNullAndNotEmpty(strKey)&&"PartNumber".equalsIgnoreCase(strKey)) {
					/*if(UIUtil.isNotNullAndNotEmpty(strPartNumberRelId)) {
						DomainRelationship.disconnect(context, strPartNumberRelId);
					}*/ if(UIUtil.isNotNullAndNotEmpty(strNewObjId) && !slAltOID.contains(strNewObjId)) {
						DomainObject newDomainObject=DomainObject.newInstance(context,strNewObjId);

						DomainRelationship.connect(context,domainObject,DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT,newDomainObject);

					}
				}
				else if(UIUtil.isNotNullAndNotEmpty(strKey)&&"SupplierName".equalsIgnoreCase(strKey)) {
					if(UIUtil.isNotNullAndNotEmpty(strSupplierNameRelId)) {
						//String strRelId=(String)slSupplierNameRelList.get(0);
						DomainRelationship.disconnect(context, strSupplierNameRelId);
					}
					if(UIUtil.isNotNullAndNotEmpty(strNewObjId)) {
						DomainObject newDomainObject=DomainObject.newInstance(context,strNewObjId);
						DomainRelationship.connect(context,newDomainObject,DomainConstants.RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,domainObject);

					}
				}else if(UIUtil.isNotNullAndNotEmpty(strKey)&&"RouteTemplate".equalsIgnoreCase(strKey)) {
					if(UIUtil.isNotNullAndNotEmpty(strRouteTemplateRelId)) {
						//String strRelId=(String)slSupplierNameRelList.get(0);
						DomainRelationship.disconnect(context, strRouteTemplateRelId);
					}
					if(UIUtil.isNotNullAndNotEmpty(strNewObjId)) {
						DomainObject newDomainObject=DomainObject.newInstance(context,strNewObjId);
						DomainRelationship.connect(context,domainObject,DomainConstants.RELATIONSHIP_OBJECT_ROUTE,newDomainObject);
					}
				}
				else if(UIUtil.isNotNullAndNotEmpty(strKey)&&"ProjectNumber".equalsIgnoreCase(strKey)) {   
					try{
						ContextUtil.pushContext(context, CAConstants.PERSON_USER_AGENT,emxcommonPushPopShadowAgent_mxJPO.getShadowAgentPassword(context, new String[]{}), null);
						blnPushContext=true;
						if(UIUtil.isNotNullAndNotEmpty(strProjectNumberRelId)) {
							DomainRelationship.disconnect(context, strProjectNumberRelId);
						}
						if(UIUtil.isNotNullAndNotEmpty(strNewObjId)) {
							DomainObject newDomainObject=DomainObject.newInstance(context,strNewObjId);
							DomainRelationship.connect(context,newDomainObject,REL_PROJECTPART,domainObject);

						}
					}catch(Exception ex){
						ex.printStackTrace();
					}
					finally {
						if(blnPushContext)
							ContextUtil.popContext(context);
					}
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
     * Gets the SFI Equivalent Parts attached to an Enterprise Part.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a HashMap of the following entries: objectId - a String
     *            containing the Enterprise Part id.
     * @return a MapList of SFI Equivalent Part object ids and
     *         relationship ids.
     * @throws Exception
     *             if the operation fails.
     */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSFIEquivalentParts(Context context, String[] args) throws Exception {
		MapList listSFI_Ids = new MapList();
		String policy_SFIPart = PropertyUtil.getSchemaProperty(context,"policy_PWC_SFI");
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) paramMap.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(objectId)){
				DomainObject partObj = DomainObject.newInstance(context, objectId);
				StringList selectStmts = new StringList(2);
				selectStmts.addElement(DomainConstants.SELECT_ID);
				StringList selectRelStmts = new StringList(2);
				selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
				StringBuffer typePattern = new StringBuffer(DomainConstants.TYPE_PART);
				String strWhere = "policy==\"" + policy_SFIPart + "\"";
				//Modified for HEAT-C-16529 - Start
				boolean getTo = true;
				boolean getFrom = false;
				String strPolicy = partObj.getInfo(context, DomainObject.SELECT_POLICY);
				if(strPolicy.equals(policy_SFIPart)){
					strWhere = "policy==\"" + POLICY_ECPART + "\" || policy==\"" + POLICY_STANDARDPART + "\"";
					getTo = false;
					getFrom = true;
				}
				//Modified for HEAT-C-16529 - End
				listSFI_Ids = partObj.getRelatedObjects(context,// context
						DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT, // relationship pattern
						typePattern.toString(), // object pattern
						selectStmts, // object selects
						selectRelStmts, // relationship selects
						getTo, // to direction
						getFrom, // from direction
						(short) 1, // recursion level
						strWhere, // object where clause
						null, // relationship where clause
						0); 
			}
		} catch (FrameworkException Ex) {
			throw Ex;
		}
		return listSFI_Ids;
	}
	
	/**
	 * This method excludeConnectedobjects where exclude objects from search result.
	 * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @return StringList
     * @throws Exception if the operation fails
     */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedObjects(Context context, String[] args)throws Exception
	{
		StringList returnList = new StringList();
		Map programMap = (Map) JPO.unpackArgs(args);
		String strObjectId = (String) programMap.get("objectId");
		String strRelationship = (String)programMap.get("strRelationship");
		strRelationship	= PropertyUtil.getSchemaProperty(context,strRelationship);
		try{
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
			{
				DomainObject domObject = DomainObject.newInstance(context, strObjectId);
				if(UIUtil.isNullOrEmpty(strRelationship)){
					strRelationship = (String)programMap.get("srcDestRelName");
					strRelationship	= PropertyUtil.getSchemaProperty(context,strRelationship);
					returnList = domObject.getInfoList(context, "from["+strRelationship+"].to.id");
				}else{
					returnList = domObject.getInfoList(context, "to["+strRelationship+"].from.id"); 
				}
			}
		}	 catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return returnList;
	}
	
	/**
	    * This method is used for show Part and Project Number Field editable or non Editable.
	    *
	    * @param context the eMatrix <code>Context</code> object
	    * @param args holds the following list of arguments:
	    * @return boolean true 
	    * @throws Exception if the operation fails
	    */
	    public boolean showProjectNumberOnCreate(Context context,String[] args) throws Exception
	    {
	            boolean flDisplayField = true;
	            
	            HashMap programMap = (HashMap) JPO.unpackArgs(args);
				String strParentOid= (String)programMap.get("parentOID");
				HashMap hmSettingsMap = (HashMap)programMap.get("SETTINGS");
				String strKey= (String)hmSettingsMap.get("key");
				if(UIUtil.isNotNullAndNotEmpty(strParentOid))
				{
					DomainObject domainObject= DomainObject.newInstance(context, strParentOid);
					if(domainObject.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE))
					{
						if("ProjectNumber".equalsIgnoreCase(strKey))
						{
						hmSettingsMap.put("Editable", "false");
						}else
						{
							hmSettingsMap.put("Editable", "true");
							hmSettingsMap.put("Field Type", "Basic");
						}
					
					}
					else if(domainObject.isKindOf(context, DomainConstants.TYPE_PART))
					{
						if("PartNumber".equalsIgnoreCase(strKey))
						{
							hmSettingsMap.put("Editable", "false");
						
						}
						else
						{
							hmSettingsMap.put("Editable", "true");
							hmSettingsMap.put("Field Type", "Basic");
						}
					}
				}
	          
	        return flDisplayField;
	    }


	
	/**
	 * Added to filter out the DOCUMENTS based on collection value.
     * Used for APPDocumentSummary table
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns Object
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getDocuments(Context context, String[] args) throws Exception
    {
    	try
    	{
    		MapList documentList = new MapList();
    		if (null != args && args.length > 0){
    			String strAttributePWC_PartSpecCollection = PropertyUtil.getSchemaProperty(context,"attribute_PWC_PartSpecCollection");
    			documentList = (MapList) JPO.invoke(context, "emxCommonDocumentUI", null,  "getDocuments", args, MapList.class);
    			MapList filteredDocumentMaplist = new MapList();
    			for(Object oDocument : documentList){
    				Map mDocument =(Map)oDocument;
    				String strDocumentId = (String)mDocument.get(DomainConstants.SELECT_ID);
    				DomainObject domObj = DomainObject.newInstance(context, strDocumentId);
    				String strAttrPWC_PartSpecCollectionValue = domObj.getInfo(context, "attribute["+strAttributePWC_PartSpecCollection+"]");
    				if("Supplier Furnished Information (SFI)".equalsIgnoreCase(strAttrPWC_PartSpecCollectionValue) || "Acceptance Test Procedure (ATP)".equalsIgnoreCase(strAttrPWC_PartSpecCollectionValue)  || "Supplier Parts List".equalsIgnoreCase(strAttrPWC_PartSpecCollectionValue)){
    					filteredDocumentMaplist.add(mDocument);
    				}
    			}
    			if(filteredDocumentMaplist.size() > 0){
    				documentList.removeAll(filteredDocumentMaplist);
    			}
    		}
    		return documentList;
    	}catch (Exception ex){
    		ex.printStackTrace();
    		throw ex;
    	}

    }
    /**
	 * This trigger method uses for checkwhether SFI Part is in Approved stste or not at the time of Ca promotion Inwork to approval
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns int : if 0 means action Trigger success
	 * @throws Exception if the operation fails
	 */
    public int checkForConnectedSFIPartIsApproved(Context context,String [] args)
    {
    	int returnValue=0;
    	String strPolicy=DomainConstants.EMPTY_STRING;
    	StringBuilder sbPartName=new StringBuilder();
    	try
    	{
    		if (null != args && args.length > 1) {
    			String strCAId = args[0];
				if(UIUtil.isNotNullAndNotEmpty(strCAId))
    			{
    				DomainObject domCAObject=DomainObject.newInstance(context,strCAId);
    				StringList selectList = new StringList("from["+REL_CHANGEAFFECTEDITEM+"|to.policy==PWC_SFI].to.current");
    				selectList.add("from["+RELATIONSHIP_IMPLEMENTED_ITEM+"|to.policy==PWC_SFI].to.current");
    				selectList.add("from["+RELATIONSHIP_IMPLEMENTED_ITEM+"|to.policy==PWC_SFI].to.name");
    				selectList.add("from["+RELATIONSHIP_IMPLEMENTED_ITEM+"|to.policy==PWC_SFI].to.revision");
    				selectList.add("from["+REL_CHANGEAFFECTEDITEM+"|to.policy==PWC_SFI].to.name");
    				selectList.add("from["+REL_CHANGEAFFECTEDITEM+"|to.policy==PWC_SFI].to.revision");
    				Hashtable objData = domCAObject.getBusinessObjectData(context, selectList);
					
    				StringList affectedItemList = (StringList)objData.get("from["+REL_CHANGEAFFECTEDITEM+"|to.policy==PWC_SFI].to.current");
    				StringList implementedItemList = (StringList)objData.get("from["+RELATIONSHIP_IMPLEMENTED_ITEM+"|to.policy==PWC_SFI].to.current");
    				StringList affectedItemListName = (StringList)objData.get("from["+REL_CHANGEAFFECTEDITEM+"|to.policy==PWC_SFI].to.name");
    				StringList implementedItemListName = (StringList)objData.get("from["+RELATIONSHIP_IMPLEMENTED_ITEM+"|to.policy==PWC_SFI].to.name");
                    StringList affectedItemListRev = (StringList)objData.get("from["+REL_CHANGEAFFECTEDITEM+"|to.policy==PWC_SFI].to.revision");
    				StringList implementedItemListRev = (StringList)objData.get("from["+RELATIONSHIP_IMPLEMENTED_ITEM+"|to.policy==PWC_SFI].to.revision");
					
					 
    				if(implementedItemList.contains("Preliminary") || implementedItemList.contains("In Work") || affectedItemList.contains("Preliminary") || affectedItemList.contains("In Work"))
    				{
    					returnValue = 1;  // in that case we don't need to add SFI Co to EC part / PSPEC
						if(affectedItemList.size()>0)
    					{
							for(int i=0;i<affectedItemList.size();i++)
							{
								String strAffectedItemState = (String)affectedItemList.get(i);
								if(strAffectedItemState.equals("Preliminary") || strAffectedItemState.equals("In Work"))
									sbPartName.append(affectedItemListName.get(i) + " Rev: " + affectedItemListRev.get(i));
							}
    					}
    					if(implementedItemList.size()>0)
    					{
							for(int i=0;i<implementedItemList.size();i++)
							{
								String strImplementedItemState = (String)implementedItemList.get(i);
								if(strImplementedItemState.equals("Preliminary") || strImplementedItemState.equals("In Work"))
									sbPartName.append(implementedItemListName.get(i) + " Rev: " + implementedItemListRev.get(i));
							}
						}
    				
    				}
    			}
    		}
    		if(returnValue==1)
    		{
    			StringBuilder sbMsg = new StringBuilder();
    			String strMsg= EnoviaResourceBundle.getProperty(context,"emxEngineeringCentralStringResource",context.getLocale(), "emxEngineeringCentral.CA.SFI.Promote.Message");
    			//StringList slMsg=FrameworkUtil.split(strMsg, "\n");
    			sbMsg.append(strMsg);
				sbMsg.append("\n");
				sbMsg.append(sbPartName.toString());
    			MqlUtil.mqlCommand(context, "notice '$1'", false, sbMsg.toString());
    		}
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return returnValue;
    }
    
   /* *//**
     *Added for connect PSPEC for Latest Revision SFI Part 
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @return void
     * @throws Exception if the operation fails
     */	
	public void connectPSPECwithLatestRevisionSFI(Context context, String args[]) throws Exception
	{
		try {
			String strSupplierName = DomainConstants.EMPTY_STRING;
			String strPartNumber = DomainConstants.EMPTY_STRING;
			String strSupplierPartNumber = DomainConstants.EMPTY_STRING;
			HashMap PSPEC_ATTR_MAP = new HashMap();
			if(args!= null && args.length >= 1){
				String strNewObjectid = args[0];
				String strOldObjectid = args[2];
				StringList selectStmts =new StringList();
				StringList slObjectIdList =new StringList();
				selectStmts.add(DomainConstants.SELECT_ID);
				selectStmts.add(DomainConstants.SELECT_NAME);
				selectStmts.add("attribute["+ATTR_SUPPLIER_PART_NUMBER_PART+"]");
				selectStmts.add("to[Manufacturing Responsibility].from.name"); // Supplier name
				selectStmts.add("from[Manufacturer Equivalent].to.name"); // 3M part name
				
				StringBuilder sbWhere=new StringBuilder();
				sbWhere.append("policy==").append("PWC_SFI").append("&&").append("revision==last");
				DomainObject doOldObject=  DomainObject.newInstance(context,strOldObjectid);
				
				MapList mlDataList=doOldObject.getRelatedObjects(context,// context
						DomainConstants.RELATIONSHIP_PART_SPECIFICATION, // relationship pattern
						DomainConstants.TYPE_PART, // object pattern
						selectStmts, // object selects
						null, // relationship selects
						true, // to direction
						false, // from direction
						(short) 1, // recursion level
						sbWhere.toString(), // object where clause
						null, // relationship where clause
						0); 
				
				int mlsize=mlDataList.size();
				if(mlsize>0)
				{
					Map hmdata=(Map)mlDataList.get(0); // this list will always return only one object
					String strId=(String)hmdata.get(DomainConstants.SELECT_ID);
					slObjectIdList.add(strId);
					strSupplierPartNumber = (String)hmdata.get("attribute["+ATTR_SUPPLIER_PART_NUMBER_PART+"]");
					PSPEC_ATTR_MAP.put(ATTR_SUPPLIER_PART_NUMBER_PSPEC,strSupplierPartNumber);
					
					if(hmdata.containsKey("to[Manufacturing Responsibility].from.name")) {
						strSupplierName = (String)hmdata.get("to[Manufacturing Responsibility].from.name");
						PSPEC_ATTR_MAP.put(ATTR_SUPPLIER_NAME,strSupplierName);
					}
					
					if(hmdata.containsKey("from[Manufacturer Equivalent].to.name")) {
						if(hmdata.get("from[Manufacturer Equivalent].to.name") instanceof StringList) {
							StringList partList = (StringList)hmdata.get("from[Manufacturer Equivalent].to.name");
							strPartNumber = FrameworkUtil.join(partList, ",");
						} else {
							strPartNumber = (String)hmdata.get("from[Manufacturer Equivalent].to.name");
						}
						PSPEC_ATTR_MAP.put(ATTR_ASSOCIATED_PART,strPartNumber);
					}	
				}
				DomainObject doNewObject=  DomainObject.newInstance(context,strNewObjectid);
				//DomainRelationship.connect(context, doNewObject,DomainConstants.RELATIONSHIP_PART_SPECIFICATION,false, (String[])slObjectIdList.toArray(new String[0]));
				doNewObject.setAttributeValues(context, PSPEC_ATTR_MAP);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		}
	
	
	/**
	 * This methos is use for delete route and task after demote SFI object To Preliminary
	 * @param context The ematrix context of the request.
	 * @param args : contains object id and other static args value
	 * @returns boolean 
	 * @throws Exception if the operation fails
	 */
	public boolean deleteRouteOndemotetoPreliminary(Context context,String args[]) throws Exception
	{
		boolean isRouteDelete= false;
		boolean blnPushContext = false;
		try{
			if (null != args && args.length > 1) {
				String strSFIPartId = args[0];
				StringList slRouteList =new StringList();
				StringList slSelectableList =new StringList();
				String strWhere=DomainConstants.EMPTY_STRING;
				StringList selectStmts = new StringList();
				selectStmts.add(DomainConstants.SELECT_ID);
			
				if(UIUtil.isNotNullAndNotEmpty(strSFIPartId))
				{
					DomainObject sfiPartObject=DomainObject.newInstance(context,strSFIPartId);
					StringBuilder sbState=new StringBuilder();
					sbState.append("'").append("In Process").append("'");
					StringList slObjSelect = new StringList();
					StringBuilder sbWhere=new StringBuilder();
					sbWhere.append("current==").append(sbState);
					
					//slObjSelect.add("from["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"|to.current=="+sbState.toString()+"].to["+DomainConstants.TYPE_ROUTE+"].id");
					MapList mlDataList=sfiPartObject.getRelatedObjects(context,// context
							DomainConstants.RELATIONSHIP_OBJECT_ROUTE, // relationship pattern
							DomainConstants.TYPE_ROUTE, // object pattern
							selectStmts, // object selects
							null, // relationship selects
							false, // to direction
							true, // from direction
							(short) 1, // recursion level
							sbWhere.toString(), // object where clause
							null, // relationship where clause
							0); 
					int mlSize=mlDataList.size();
					if(mlSize>0)
					{
						Iterator itr = mlDataList.iterator();
						Map mapTemp = new HashMap();
						while (itr.hasNext()) {
							mapTemp = (Map) itr.next();	
							String strRouteId=(String) mapTemp.get(DomainConstants.SELECT_ID);
							Route routeObj = (Route) DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
							routeObj.setId(strRouteId);
							//Modified for HEAT-C-16529 - Start
							ContextUtil.pushContext(context, CAConstants.PERSON_USER_AGENT,emxcommonPushPopShadowAgent_mxJPO.getShadowAgentPassword(context, new String[]{}), null);
							blnPushContext =true;
							//Modified for HEAT-C-16529 - End
							routeObj.deleteObject(context);
							isRouteDelete=true;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			isRouteDelete=false;
			e.printStackTrace();
			throw e;
		}finally {
			//Modified for HEAT-C-16529 - Start
			if(blnPushContext)
				ContextUtil.popContext(context);
		}
		//Modified for HEAT-C-16529 - End
		return isRouteDelete;
	}
}
