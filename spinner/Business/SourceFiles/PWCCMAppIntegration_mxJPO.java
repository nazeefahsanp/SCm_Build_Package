/*
 *  PWCCMAppIntegration
 *  Logic Program to process for CM App interfaces
 *  Owner : HCL
 *
 */

import static com.pwc.common.util.PWCIntegrationUtil.getIntegrationProperty;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.table.DefaultTableModel;
import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessInterface;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.RelationshipType;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ca.pwc.util.table.TableDataObject;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.smaslm.common.util.StringUtil;
import com.ds.common.PWCCommonUtil;
import com.ds.common.PWCConstants;
import com.ds.crypto.DSCipher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.BackgroundProcess;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;
import com.pwc.bom.web.services.CMAppServiceCallbackHandler;
import com.pwc.bom.web.services.CMAppServiceStub;
import com.pwc.common.constants.PWCIntegrationConstants;
import java.util.Enumeration;
import org.apache.commons.io.FileUtils;
import java.util.*;
import java.io.*;
import java.net.*;
import com.matrixone.apps.domain.util.FrameworkException;
//Added for HEAT-C-18499 : Start
import com.technia.tvc.commons.lang.StringUtils;
import java.util.HashSet;
//Added for HEAT-C-18499 : End

public class PWCCMAppIntegration_mxJPO{
	
	/**
	* Constructor.
	* @throws Exception if the operation fails.
	*/
	public PWCCMAppIntegration_mxJPO (Context context, String[] args) throws Exception{
		LOG_FILE_PATH = System.getenv(PWC_ENOVIA_LOG_FOLDER_PATH);
		CMAPP_DESIGN_LEVEL_DETAILS_LOG_FILE_NAME = getIntegrationProperty(context, "PWCIntegration.CMApp.DesignLevel.LogFileName");		
		CMAPP_PMA_DETAILS_LOG_FILE_NAME = getIntegrationProperty(context, "PWCIntegration.CMApp.PMA.LogFileName");
		CMAPP_LRP_DETAILS_LOG_FILE_NAME = getIntegrationProperty(context, "PWCIntegration.CMApp.LRP.LogFileName");
		CMAPP_EC_META_DATA_DETAILS_LOG_FILE_NAME = getIntegrationProperty(context, "PWCIntegration.CMApp.ECMetaData.LogFileName");
		CMAPP_MAKE_FROM_DETAILS_LOG_FILE_NAME = getIntegrationProperty(context, "PWCIntegration.CMApp.MakeFrom.LogFileName");
		CMAPP_PC_MODEL_DETAILS_LOG_FILE_NAME = getIntegrationProperty(context, "PWCIntegration.CMApp.PCModel.LogFileName");
		JSON_COLUMN_IDENTIFIERS = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.ColumnIdentifiers");
		JSON_TABLE_DATA = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.TableData");
		JSON_PROCESS_NAME = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.ProcessName");
		JSON_PROCESS_DATA = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.ProcessData");
		JSON_ITEMS_LIST = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.ItemsList");
		JSON_ITEMS_DATA = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.ItemsData");
		JSON_DESIGN_LEVEL = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.DESIGN_LEVEL");
		JSON_PMA = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.PMA");
		JSON_LEGACY_PR = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.LEGACY_PR");
		JSON_EC_META_DATA_INCORPORATION = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.EC_META_DATA_INCORPORATION");
		JSON_EC_META_DATA_ADD_N_CANCEL = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.EC_META_DATA_ADD_N_CANCEL");
		JSON_EC_META_DATA_OTHER_TEXT = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.EC_META_DATA_OTHER_TEXT");
		JSON_MAKE_FROM = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.MAKE_FROM");
		JSON_PC_DETAILS = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.PC_Details");
		JSON_PC_MODEL = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.PC_MODEL");
		JSON_WHERE_USED = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.WHERE_USED");
		REQ_CHANGE_FOR_REVISE = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.RequestedChange.ForRevise");
		REQ_CHANGE_FOR_PROD = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.RequestedChange.ForProduction");
		REQ_CHANGE_NONE = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.RequestedChange.None");
		CMAPP_WHERE_USED_SERVICE_URL = getIntegrationProperty(context, "PWCIntegration.CMApp.WhereUsed.ServiceURL");
		CMAPP_ISSUE_IDS_TDBD_SERVICE_URL = getIntegrationProperty(context, "PWCIntegration.CMApp.IssueIdsTDBD.ServiceURL");
		LOG_FILE_PATH = LOG_FILE_PATH + System.getProperty("file.separator") + "PWCCMApp";
		JSON_CA_INWORK_META_DATA = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.CA_INWORK_META_DATA");
		JSON_EC_META_DATA = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.EC_META_DATA");
		INTERFACE_DISPOSITION_CODES = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.ChangeAffectedItem.Interface.DispostionCodes");
		STATUS_PASS = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.ProcessStatus.Pass");
		STATUS_FAIL = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.ProcessStatus.Fail");
		STATUS_CANNOT_PROCESS = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.ProcessStatus.CannotProcess");
		PROCESS = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.ProcessStatus.Process");
		MESSAGE_NO_DATA_PROVIDED = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.MailNotification.Message.NoDataProvided");
		MESSAGE_NO_SUCH_PART = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.MailNotification.Message.NoSuchPart");
		MESSAGE_NO_REL_FLAG_PROVIDED = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.MailNotification.Message.NoRelFlagprovided");
		MESSAGE_NO_SUCH_HP = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.MailNotification.Message.NoSuchHP");
		JSON_PART_INFO = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.JSON.PART_INFO");
		CMAPP_PART_INFO_LOG_FILE_NAME = getIntegrationProperty(context, "PWCIntegration.CMApp.PartInfo.LogFileName");
		STR_EC_META_DATA = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ForECMetaData").trim();
		STR_EC_META_DATA_INCORPORATION = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ForECMetaDataIncorporation").trim();
		STR_EC_META_DATA_ADD_N_CANCEL = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ForECMetaDataAddnCancel").trim();
		STR_DESIGN_LEVEL = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ForDesignLevel").trim();
		STR_LEGACY_PR = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ForLegacyPR").trim();
		STR_PMA = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ForPMA").trim();
		STR_CA_IN_WORK_META_DATA = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ForCOMetaData").trim();
		STR_PART_MAKE_FROM = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ForPartMakeFrom").trim();
		STR_PRODUCT_CONFIGURATION_MODEL = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ForPCModel").trim();
		STR_DT_CODE_ATTRIBUTES = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.DTCodeAttributes").trim();
		STR_CLASS_1 = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.ChangeOrder.Class1").trim();
		STR_CLASS_2 = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.ChangeOrder.Class2").trim();
		//START :: Modified for HEAT-C-16656 : Add new range "Class 2 RS" for change class
		STR_CLASS_2_RS = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.ChangeOrder.Class2RS").trim();
		//END :: Modified for HEAT-C-16656 : Add new range "Class 2 RS" for change class
		STR_ADD_N_CANCEL = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.Collection.AddnCancel").trim();
		STR_TRS_NOT_TO_BE_CONSIDERED = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.AddnCancel.TRS.NotToBeConsidered").trim();
		
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
		MESSAGE_NO_SUCH_PARTS = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.MailNotification.Message.NoSuchParts");
		MESSAGE_NO_SUCH_HPS = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.MailNotification.Message.NoSuchHPs");
		//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
		
		//START :: Added for HEAT-C-19459 : CMApp Drop2 UC 06 - CMApp Leftover
		CHAGE_OF_CATEGORY_EED_RELEASE = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.CategoryOfChange.EEDRelease");
		//END :: Added for HEAT-C-19459 : CMApp Drop2 UC 06 - CMApp Leftover
		
		if(System.getProperty("file.separator").toCharArray()[0] == LOG_FILE_PATH.charAt(LOG_FILE_PATH.length() - 1)) 
		{
			backFeedLogs = new File(LOG_FILE_PATH);

		} else {
			backFeedLogs = new File(LOG_FILE_PATH);
		}
		if(!backFeedLogs.exists()){
			backFeedLogs.mkdirs();
		}
	}
	//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	private static final Logger _LOGGER = Logger.getLogger("PWCCMAppIntegration");
	//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	private static final String ATTR_HAS_MANUFACTURING_SUBSTITUTE = PropertyUtil.getSchemaProperty("attribute_HasManufacturingSubstitute");
	private static final String ATTR_PWC_COORDINATE = PropertyUtil.getSchemaProperty("attribute_PWC_COORDINATE");
	private static final String ATTR_PWC_EAPL_LOCK = PropertyUtil.getSchemaProperty("attribute_PWC_EAPLLOCK");
	private static final String ATTR_PWC_EAPL_SYMBOL = PropertyUtil.getSchemaProperty("attribute_PWC_EAPLSYMBOL");
	private static final String ATTR_PWC_INTERIM_DOC_FLAG = PropertyUtil.getSchemaProperty("attribute_PWC_INTERIMDOCFLAG");
	private static final String ATTR_PWC_MODULE_CODE = PropertyUtil.getSchemaProperty("attribute_PWC_ModuleCode");
	private static final String ATTR_PWC_SALEABLE_CODE = PropertyUtil.getSchemaProperty("attribute_PWC_SALEABLECODE");
	private static final String ATTR_PWC_STATUS_CODE = PropertyUtil.getSchemaProperty("attribute_PWC_STATUSCODE");
	private static final String ATTR_PWC_UID = PropertyUtil.getSchemaProperty("attribute_PWC_UID");
	private static final String ATTR_PWC_VECTOR = PropertyUtil.getSchemaProperty("attribute_PWC_VECTOR");
	private static final String ATTR_SOURCE = PropertyUtil.getSchemaProperty("attribute_Source");
	private static final String SELECTABLE_ATTR_END_EFFECTIVITY_DATE = DomainObject.getAttributeSelect(DomainConstants.ATTRIBUTE_END_EFFECTIVITY_DATE);
	private static final String SELECTABLE_ATTR_HAS_MANUFACTURING_SUBSTITUTE = DomainObject.getAttributeSelect(ATTR_HAS_MANUFACTURING_SUBSTITUTE);
	private static final String SELECTABLE_ATTR_NOTES = DomainObject.getAttributeSelect(DomainConstants.ATTRIBUTE_NOTES);
	private static final String SELECTABLE_ATTR_PWC_COORDINATE = DomainObject.getAttributeSelect(ATTR_PWC_COORDINATE);
	private static final String SELECTABLE_ATTR_PWC_EAPL_LOCK = DomainObject.getAttributeSelect(ATTR_PWC_EAPL_LOCK);
	private static final String SELECTABLE_ATTR_EAPL_SYMBOL = DomainObject.getAttributeSelect(ATTR_PWC_EAPL_SYMBOL);
	private static final String SELECTABLE_ATTR_PWC_INTERIM_DOC_FLAG = DomainObject.getAttributeSelect(ATTR_PWC_INTERIM_DOC_FLAG);
	private static final String SELECTABLE_ATTR_PWC_MODULE_CODE = DomainObject.getAttributeSelect(ATTR_PWC_MODULE_CODE);
	private static final String SELECTABLE_ATTR_PWC_SALEABLE_CODE = DomainObject.getAttributeSelect(ATTR_PWC_SALEABLE_CODE);
	private static final String SELECTABLE_ATTR_PWC_STATUS_CODE = DomainObject.getAttributeSelect(ATTR_PWC_STATUS_CODE);
	private static final String SELECTABLE_ATTR_PWC_UID = DomainObject.getAttributeSelect(ATTR_PWC_UID);
	private static final String SELECTABLE_ATTR_PWC_VECTOR = DomainObject.getAttributeSelect(ATTR_PWC_VECTOR);
	private static final String SELECTABLE_ATTR_START_EFFECTIVITY_DATE = DomainObject.getAttributeSelect(DomainConstants.ATTRIBUTE_START_EFFECTIVITY_DATE);
	private static final String SELECTABLE_ATTR_SOURCE = DomainObject.getAttributeSelect(ATTR_SOURCE);
	private static final String PART_STATE_RELEASE = PropertyUtil.getSchemaProperty("policy", DomainConstants.POLICY_EC_PART, "state_Release");
	private static final String PART_STATE_EXPERIMENTAL = PropertyUtil.getSchemaProperty("policy", DomainConstants.POLICY_EC_PART, "state_Experimental");
	private static final String PART_STATE_PRODUCTION = PropertyUtil.getSchemaProperty("policy", DomainConstants.POLICY_EC_PART, "state_Production");
	private static final String PART_STATE_OBSOLETE = PropertyUtil.getSchemaProperty("policy", DomainConstants.POLICY_EC_PART, "state_Obsolete");
	private static final String PART_STATE_COMPLETE = PropertyUtil.getSchemaProperty("Policy",  DomainObject.POLICY_DEVELOPMENT_PART, "state_Complete");
	private static final String PART_STATE_DORMANT = PropertyUtil.getSchemaProperty("Policy",  DomainObject.POLICY_EC_PART, "state_Dormant");
	private static final String ATTR_PWC_ASSEMBLY_LINE_NUMBER = PropertyUtil.getSchemaProperty("attribute_PWC_AssemblyLineNumber");
	private static final String ATTR_PWC_REVISED_DATE = PropertyUtil.getSchemaProperty("attribute_PWC_RevisedDate");
	private static final String ATTR_PWC_REVISED_BY = PropertyUtil.getSchemaProperty("attribute_PWC_RevisedBy");
	private static final String ATTR_PWC_RAW_MATERIAL = PropertyUtil.getSchemaProperty("attribute_PWC_RawMaterial");
	private static final String ATTR_PWC_MAKE_FROM = PropertyUtil.getSchemaProperty("attribute_PWC_MakeFrom");
	private static final String ATTR_PWC_ENG_PART_NUMBER = PropertyUtil.getSchemaProperty("attribute_PWC_EngPartNumber");
	private static final String ATTR_PWC_REFERENCE_DOCUMENT = PropertyUtil.getSchemaProperty("attribute_PWC_ReferenceDocument");
	private static final String ATTR_COMMENT = PropertyUtil.getSchemaProperty("attribute_Comment");
	private static final String ATTR_PWC_MD_DOCUMENT_NUMBER = PropertyUtil.getSchemaProperty("attribute_PWC_MDDocumentNumber");
	private static final String SELECTABLE_ATTR_PWC_ASSEMBLY_LINE_NUMBER = DomainObject.getAttributeSelect(ATTR_PWC_ASSEMBLY_LINE_NUMBER);
	private static final String SELECTABLE_ATTR_PWC_REVISED_DATE = DomainObject.getAttributeSelect(ATTR_PWC_REVISED_DATE);
	private static final String SELECTABLE_ATTR_PWC_REVISED_BY = DomainObject.getAttributeSelect(ATTR_PWC_REVISED_BY);
	private static final String SELECTABLE_ATTR_PWC_RAW_MATERIAL = DomainObject.getAttributeSelect(ATTR_PWC_RAW_MATERIAL);
	private static final String SELECTABLE_ATTR_PWC_MAKE_FROM = DomainObject.getAttributeSelect(ATTR_PWC_MAKE_FROM);
	private static final String SELECTABLE_ATTR_PWC_ENG_PART_NUMBER = DomainObject.getAttributeSelect(ATTR_PWC_ENG_PART_NUMBER);
	private static final String SELECTABLE_ATTR_PWC_REFERENCE_DOCUMENT = DomainObject.getAttributeSelect(ATTR_PWC_REFERENCE_DOCUMENT);
	private static final String SELECTABLE_ATTR_COMMENT = DomainObject.getAttributeSelect(ATTR_COMMENT);
	private static final String SELECTABLE_ATTR_PWC_MD_DOCUMENT_NUMBER = DomainObject.getAttributeSelect(ATTR_PWC_MD_DOCUMENT_NUMBER);
	private static final String TYPE_HARDWARE_PRODUCT = PropertyUtil.getSchemaProperty("type_HardwareProduct");
	private static final String REL_PWC_MODEL_APPLICABILITY = PropertyUtil.getSchemaProperty("relationship_PWC_ModelApplicability");
	private static final String ATTR_PWC_APPLICABILITY = PropertyUtil.getSchemaProperty("attribute_PWC_Applicability");
	private static final String ATTR_PWC_LEAD_ENGINE_MODEL = PropertyUtil.getSchemaProperty("attribute_PWC_LeadEngineModel");
	private static final String REL_LEGACY_REPLACES_PART = PropertyUtil.getSchemaProperty("relationship_LegacyReplacesPart");
	private static final String ATTR_PWC_DOCUMENT_NUMBER = PropertyUtil.getSchemaProperty("attribute_PWC_DocumentNumber");
	private static final String ATTR_PWC_CREATE_DATE = PropertyUtil.getSchemaProperty("attribute_PWC_CreateDate");
	private static final String ATTR_PWC_DOCUMENT_SCOPE = PropertyUtil.getSchemaProperty("attribute_PWC_DocumentScope");
	private static final String ATTR_PWC_REPLACEMENT_SCOPE = PropertyUtil.getSchemaProperty("attribute_PWC_ReplacementScope");
	private static final String ATTR_PWC_DOCUMENT_ACTION = PropertyUtil.getSchemaProperty("attribute_PWC_DocumentAction");
	private static String SELECTABLE_ATTR_PWC_LEAD_ENGINE_MODEL = "attribute[" + ATTR_PWC_LEAD_ENGINE_MODEL + "]";
	private static final String ATTR_ASSEMBLY_LINE_NUMBER = PropertyUtil.getSchemaProperty("attribute_PWC_AssemblyLineNumber");
	private static final String ATTR_ENG_PART_NUMBER = PropertyUtil.getSchemaProperty("attribute_PWC_EngPartNumber");
	private static final String ATTR_MAKE_FROM = PropertyUtil.getSchemaProperty("attribute_PWC_MakeFrom");
	private static final String ATTR_MDDOCUMENT_NUMBER = PropertyUtil.getSchemaProperty("attribute_PWC_MDDocumentNumber");
	private static final String ATTR_RAW_MATERIAL = PropertyUtil.getSchemaProperty("attribute_PWC_RawMaterial");
	private static final String ATTR_REVISED_BY = PropertyUtil.getSchemaProperty("attribute_PWC_RevisedBy");
	private static final String ATTR_REVISED_DATE = PropertyUtil.getSchemaProperty("attribute_PWC_RevisedDate");
	private static final String ATTR_QUANTITY = PropertyUtil.getSchemaProperty("attribute_Quantity");
	private static String RELATIONSHIP_PRODUCT_CONFIGURATION = PropertyUtil.getSchemaProperty("relationship_ProductConfiguration");
	private static String RELATIONSHIP_TOP_LEVEL_PART = PropertyUtil.getSchemaProperty("relationship_TopLevelPart");
	private static String RELATIONSHIP_DERIVED = PropertyUtil.getSchemaProperty("relationship_Derived");
	public static final String STATE_DEVELOPMENT_PART_OBSOLETE = PropertyUtil.getSchemaProperty("policy", DomainConstants.POLICY_DEVELOPMENT_PART, "state_Obsolete");
	private static String ATTRIBUTE_PWC_CHANGE_CLASS = PropertyUtil.getSchemaProperty("attribute_PWC_ChangeClass");
	private static String ATTRIBUTE_PWC_CO_Category = PropertyUtil.getSchemaProperty("attribute_PWC_COCategory");
	private static final String ATTRIBUTE_PWC_Sync_With_CM_App = PropertyUtil.getSchemaProperty("attribute_PWC_SyncWithCMApp");
	private static final String ATTRIBUTE_PWC_CMAppECIssued = PropertyUtil.getSchemaProperty("attribute_PWC_CMAppECIssued");
	private static final String REL_PWC_ENGINE_MODEL = PropertyUtil.getSchemaProperty("relationship_PWCEngineModel");	
	private static final String ATTR_PWC_CO_INCORPORATION = PropertyUtil.getSchemaProperty("attribute_PWCCOIncorporation");
	private static final String ATTR_PWC_STATEMENT_OF_COMPLIANCE = PropertyUtil.getSchemaProperty("attribute_PWC_StatementOfCompliance");
	private static final String ATTR_PWC_SUBSTANTIATION = PropertyUtil.getSchemaProperty("attribute_PWC_Substantiation");
	private static final String SELECTABLE_ATTR_PWC_CO_INCORPORATION = DomainObject.getAttributeSelect(ATTR_PWC_CO_INCORPORATION);
	private static final String SELECTABLE_ATTR_PWC_STATEMENT_OF_COMPLIANCE = DomainObject.getAttributeSelect(ATTR_PWC_STATEMENT_OF_COMPLIANCE);
	private static final String SELECTABLE_ATTR_PWC_SUBSTANTIATION = DomainObject.getAttributeSelect(ATTR_PWC_SUBSTANTIATION);
	private static final String ATTRIBUTE_PWC_GENERAL_COLLECTION = PropertyUtil.getSchemaProperty("attribute_PWCGeneralCollection");
	private static String RELATIONSHIP_REFERENCE_DOCUMENT = PropertyUtil.getSchemaProperty("relationship_ReferenceDocument");
	private static final String TYPE_PWC_GENERAL_DOCUMENT = PropertyUtil.getSchemaProperty("type_PWCGeneralDocument");
	private static String ATTRIBUTE_REQUESTED_CHANGE = PropertyUtil.getSchemaProperty("attribute_RequestedChange");
	private static final String ATTRIBUTE_COMMENTS = PropertyUtil.getSchemaProperty("attribute_Comments");
	private static final String STATE_PRELIM_EC_PART	= PropertyUtil.getSchemaProperty("Policy", DomainObject.POLICY_EC_PART, "state_Preliminary");
	private static final String ATTR_PWC_RECORDTYPE = PropertyUtil.getSchemaProperty("attribute_PWC_RecordTYPE");
	private static final String ATTR_PWC_RECORDTYPE_DESC = PropertyUtil.getSchemaProperty("attribute_PWC_RecordTypeDesc");
	private static final String POLICY_EC_PART = PropertyUtil.getSchemaProperty("policy_ECPart");
	private static final String ATTR_PWC_CO_CATEGORY = PropertyUtil.getSchemaProperty("attribute_PWC_COCategory");
	private static final String ATTR_PWC_ITEM_WEIGHT = PropertyUtil.getSchemaProperty("attribute_PWC_ItemWeight");
	private static final String ATTR_PWC_MAX_WEIGHT = PropertyUtil.getSchemaProperty("attribute_PWC_MaxWeight");
	private static final String ATTR_PWC_WEIGHT_CODE = PropertyUtil.getSchemaProperty("attribute_PWC_WeightCode");
	private static final String ATTR_PWC_WEIGHT_COMMENT = PropertyUtil.getSchemaProperty("attribute_PWC_WeightComment");
	private static final String ATTR_PWC_ROLLED_UP_WEIGHT = PropertyUtil.getSchemaProperty("attribute_PWC_RolledUpWeight");
	private static final String ATTR_PWC_WEIGHT_FIDELITY_MARGIN = PropertyUtil.getSchemaProperty("attribute_PWC_WeightFidelityMargin");
	private static final String ATTR_PWC_WEIGHT_TRL_MARGIN = PropertyUtil.getSchemaProperty("attribute_PWC_WeightTRLMargin");
	private static final String ATTR_PWC_SALE_CODE = PropertyUtil.getSchemaProperty("attribute_PWC_SALE_CODE");
	private static final String ATTR_PWC_SALEBLECODE = PropertyUtil.getSchemaProperty("attribute_PWC_SALEABLECODE");
	private static final String ATTR_UNIT_OF_MEASURE = PropertyUtil.getSchemaProperty("attribute_UnitofMeasure");
	private static final String SELECTABLE_ATTR_PWC_ITEM_WEIGHT = DomainObject.getAttributeSelect(ATTR_PWC_ITEM_WEIGHT);
	private static final String SELECTABLE_ATTR_PWC_MAX_WEIGHT = DomainObject.getAttributeSelect(ATTR_PWC_MAX_WEIGHT);
	private static final String SELECTABLE_ATTR_PWC_WEIGHT_CODE = DomainObject.getAttributeSelect(ATTR_PWC_WEIGHT_CODE);
	private static final String SELECTABLE_ATTR_PWC_WEIGHT_COMMENT = DomainObject.getAttributeSelect(ATTR_PWC_WEIGHT_COMMENT);
	private static final String SELECTABLE_ATTR_PWC_ROLLED_UP_WEIGHT = DomainObject.getAttributeSelect(ATTR_PWC_ROLLED_UP_WEIGHT);
	private static final String SELECTABLE_ATTR_PWC_WEIGHT_FIDELITY_MARGIN = DomainObject.getAttributeSelect(ATTR_PWC_WEIGHT_FIDELITY_MARGIN);
	private static final String SELECTABLE_ATTR_PWC_WEIGHT_TRL_MARGIN = DomainObject.getAttributeSelect(ATTR_PWC_WEIGHT_TRL_MARGIN);
	private static final String SELECTABLE_ATTR_PWC_SALE_CODE = DomainObject.getAttributeSelect(ATTR_PWC_SALE_CODE);
	private static final String SELECTABLE_ATTR_PWC_SALEBLECODE = DomainObject.getAttributeSelect(ATTR_PWC_SALEBLECODE);
	private static final String SELECTABLE_ATTR_UNIT_OF_MEASURE = DomainObject.getAttributeSelect(ATTR_UNIT_OF_MEASURE);
	private static final String CO_STATE_IN_WORK =PropertyUtil.getSchemaProperty("policy", ChangeConstants.POLICY_FASTTRACK_CHANGE, "state_InWork");
	private static final String CO_STATE_IN_APPROVAL =PropertyUtil.getSchemaProperty("policy", ChangeConstants.POLICY_FASTTRACK_CHANGE, "state_InApproval");
	private static final String CO_STATE_COMPLETE =PropertyUtil.getSchemaProperty("policy", ChangeConstants.POLICY_FASTTRACK_CHANGE, "state_Complete");
	private static final String CO_STATE_IMPLEMENTED =PropertyUtil.getSchemaProperty("policy", ChangeConstants.POLICY_FASTTRACK_CHANGE, "state_Implemented");
	private static final String CA_STATE_IN_WORK =PropertyUtil.getSchemaProperty("policy", ChangeConstants.POLICY_CHANGE_ACTION, "state_InWork");
	private static final String CA_STATE_IN_APPROVAL =PropertyUtil.getSchemaProperty("policy", ChangeConstants.POLICY_CHANGE_ACTION, "state_InApproval");
	private static final String CA_STATE_COMPLETE =PropertyUtil.getSchemaProperty("policy", ChangeConstants.POLICY_CHANGE_ACTION, "state_Complete");
	private static String LOG_FILE_PATH = null;
	private static String CMAPP_DESIGN_LEVEL_DETAILS_LOG_FILE_NAME = null;
	private static String CMAPP_PMA_DETAILS_LOG_FILE_NAME = null;
	private static String CMAPP_LRP_DETAILS_LOG_FILE_NAME = null;
	private static String CMAPP_EC_META_DATA_DETAILS_LOG_FILE_NAME = null;
	private static String CMAPP_MAKE_FROM_DETAILS_LOG_FILE_NAME = null;
	private static String CMAPP_PC_MODEL_DETAILS_LOG_FILE_NAME = null;
	private static String PWC_ENOVIA_LOG_FOLDER_PATH = "PWC_ENOVIA_LOG_FOLDER_PATH";
	private static File backFeedLogs = null;
	private static File writeIntoFile = null;
	String strFileName = DomainConstants.EMPTY_STRING;
	private static String JSON_PROCESS_NAME = null;
	private static String JSON_PROCESS_DATA = null;
	private static String JSON_ITEMS_LIST = null;
	private static String JSON_ITEMS_DATA = null;
	private static String JSON_DESIGN_LEVEL = null;
	private static String JSON_PMA = null;
	private static String JSON_LEGACY_PR = null;
	private static String JSON_EC_META_DATA_INCORPORATION = null;
	private static String JSON_EC_META_DATA_ADD_N_CANCEL = null;
	private static String JSON_EC_META_DATA_OTHER_TEXT = null;
	private static String JSON_MAKE_FROM = null;
	private static String JSON_PC_DETAILS = null;
	private static String JSON_PC_MODEL = null;
	private static String JSON_WHERE_USED = null;
	private static String JSON_COLUMN_IDENTIFIERS = null;
	private static String JSON_TABLE_DATA = null;
	private static String REQ_CHANGE_FOR_REVISE = null;
	private static String REQ_CHANGE_FOR_PROD = null;
	private static String REQ_CHANGE_NONE = null;
	private static String CMAPP_WHERE_USED_SERVICE_URL = null;
	private static String CMAPP_ISSUE_IDS_TDBD_SERVICE_URL = null;
	private static JSONArray jsonRelationsArray = new JSONArray();
	private static JSONArray jsonUniquePartsArray = new JSONArray();
	private static JSONObject eachRelationJSONObj = new JSONObject();
	private static JSONObject eachPartJSONObj = new JSONObject();
	private static StringList slUniqueNodesList = null;
	private static String JSON_CA_INWORK_META_DATA = null;
	private static String JSON_EC_META_DATA = null;
	private static String INTERFACE_DISPOSITION_CODES = null;
	private static String STATUS_PASS = null;
	private static String STATUS_FAIL = null;
	private static String STATUS_CANNOT_PROCESS = null;
	private static String PROCESS = null;
	private static String MESSAGE_NO_DATA_PROVIDED = null;
	private static String MESSAGE_NO_SUCH_PART = null;
	private static String MESSAGE_NO_REL_FLAG_PROVIDED = null;
	private static String MESSAGE_NO_SUCH_HP = null;
	private static String JSON_PART_INFO= null;
	private static String CMAPP_PART_INFO_LOG_FILE_NAME = null;
	private static String strMessage = "Message";
	private static String STR_EC_META_DATA = null;
	private static String STR_EC_META_DATA_INCORPORATION = null;
	private static String STR_EC_META_DATA_ADD_N_CANCEL = null;
	private static String STR_DESIGN_LEVEL = null;
	private static String STR_LEGACY_PR = null;
	private static String STR_PMA = null;
	private static String STR_CA_IN_WORK_META_DATA = null;
	private static String STR_PART_MAKE_FROM = null;
	private static String STR_PRODUCT_CONFIGURATION_MODEL = null;
	private static String STR_DT_CODE_ATTRIBUTES = null;
	private static boolean bNotifyIncorporation = true;	
	private static boolean bNotifyAddnCancel = true;	
	private static String STR_CLASS_1 = null;
	private static String STR_CLASS_2 = null;
	//START :: Modified for HEAT-C-16656 : Add new range "Class 2 RS" for change class
	private static String STR_CLASS_2_RS = null;
	//END :: Modified for HEAT-C-16656 : Add new range "Class 2 RS" for change class
	private static String STR_ADD_N_CANCEL = null;
	private static String STR_TRS_NOT_TO_BE_CONSIDERED = null;
	// Added for HEAT-C-15968 : Start
	private static final String STR_PRODUCTION_VAULT = PropertyUtil.getSchemaProperty("vault_eServiceProduction");
	private static final String STR_LEGACY_VAULT = PropertyUtil.getSchemaProperty("vault_Legacy");
	// Added for HEAT-C-15968 : End
	
	//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
	private static final String POLICY_PWCGENDOCUMENT= PropertyUtil.getSchemaProperty("policy_PWCGeneralDocument");
	private static final String ATTRIBUTE_ORIGINATOR = PropertyUtil.getSchemaProperty("attribute_Originator");
	private static final String ATTRIBUTE_TITLE = PropertyUtil.getSchemaProperty("attribute_Title");
	private static final String ATTR_PWC_ENGINE_MODEL = PropertyUtil.getSchemaProperty("attribute_PWCEngineModel");
	public static final String ATTR_PWC_EXC_CONT_TECH_DATA 		= PropertyUtil.getSchemaProperty("attribute_PWC_EXCContainsTechData");
	public static final String ATTR_PWC_EC_MEANS_CLASSIFICATION	= PropertyUtil.getSchemaProperty("attribute_PWC_ECMeansofClassification");
	private static final String ATTR_PWC_EC_XCLASS_ID			= PropertyUtil.getSchemaProperty("attribute_PWC_ECxClassID");
	//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
	
	//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 14 - Sale Code Management
	private static final String POLICY_STANDARD_PART= PropertyUtil.getSchemaProperty("policy_StandardPart");
	private static final String ATTR_PWC_PART_TYPE = PropertyUtil.getSchemaProperty("attribute_PWC_PartType");
	private static final String SELECTABLE_ATTR_PWC_PART_TYPE = DomainObject.getAttributeSelect(ATTR_PWC_PART_TYPE);
	//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 14 - Sale Code Management
	
	//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 17 - Partnership Attribute for Design_Level feed
	private static final String ATTR_PWC_PARTNER_NAME = PropertyUtil.getSchemaProperty("attribute_PWC_PartnerName");
	private static final String SELECTABLE_ATTR_PWC_PARTNER_NAME = DomainObject.getAttributeSelect(ATTR_PWC_PARTNER_NAME);
	
	private static final String ATTR_PWC_PARTNERSHIP_NAME = PropertyUtil.getSchemaProperty("attribute_PWC_PartnershipName");
	private static final String SELECTABLE_ATTR_PWC_PARTNERSHIP_NAME = DomainObject.getAttributeSelect(ATTR_PWC_PARTNERSHIP_NAME);
	//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 17 - Partnership Attribute for Design_Level feed
	
	//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
	private static String MESSAGE_NO_SUCH_PARTS = null;
	private static String MESSAGE_NO_SUCH_HPS = null;
	//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
	
	//START :: Added for HEAT-C-19459 : CMApp Drop2 UC 06 - CMApp Leftover
	private static String CHAGE_OF_CATEGORY_EED_RELEASE = null;
	//END :: Added for HEAT-C-19459 : CMApp Drop2 UC 06 - CMApp Leftover
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	/**
	* This method is to update design level BOM, when pushed from CM App.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void updateDesignLevel(Context context, String[] args) throws Exception{
		JSONObject jsonObjDesignLevel =null;
		JSONObject jsonObjDesignLevellInt = null;
        JSONObject joItemsData = null;
        JSONObject joProcessData = null;
        JSONArray jaColumnIdentifiers = new JSONArray();
        JSONArray jaTableData = new JSONArray();
		boolean isContextPushed = false;
		try{
			String strPushedData = args[0];
			//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			writeIntoFile = setLoggerPath(CMAPP_DESIGN_LEVEL_DETAILS_LOG_FILE_NAME);
			//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			if(!writeIntoFile.exists()){
				writeIntoFile.createNewFile();
			}
		
			// Setting context to CM App process user
			String strCMAppUserPushed = setCMAppProcessUserContext(context);
			
			if ("False".equalsIgnoreCase(strCMAppUserPushed) && !"User Agent".equals(context.getUser()))
			{
				ContextUtil.pushContext(context, PWCConstants.SUPER_USER, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				isContextPushed = true;	
			}
			
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : updateDesignLevel --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			try{
				if(null != strPushedData){
					jsonObjDesignLevel = new JSONObject(strPushedData);
					if(null != jsonObjDesignLevel){
						joProcessData = jsonObjDesignLevel.getJSONObject(JSON_PROCESS_DATA);
						if(null != joProcessData){
							joItemsData = joProcessData.getJSONObject(JSON_ITEMS_DATA);
							if(null != joItemsData){
								jsonObjDesignLevellInt = joItemsData.getJSONObject(JSON_DESIGN_LEVEL);
								/*if(null != writeIntoFile){
									writeDataToFile("PWCCMAppIntegration : updateDesignLevel --> Process Name --> DESIGN_LEVEL \n", writeIntoFile);						
								}*/
							}else{
								if(null != writeIntoFile){
									//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
									_LOGGER.debug("PWCCMAppIntegration : updateDesignLevel -->  NO DATA PROVIDED");
									//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
								}
								notifyCOCoOrdinator(context, null, JSON_DESIGN_LEVEL, STATUS_FAIL, MESSAGE_NO_DATA_PROVIDED);
							}
						}
					}
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
				if(null != writeIntoFile){
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					_LOGGER.debug("Exception in PWCCMAppIntegration : updateDesignLevel (Content Not In JSON Format) :-"+ex.getMessage());
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				}
				notifyCOCoOrdinator(context, null, JSON_DESIGN_LEVEL, STATUS_FAIL, ex.getMessage());
			}
			
			Map mpChange = null;
			String strChangeId = DomainConstants.EMPTY_STRING;
			String strChangeName = DomainConstants.EMPTY_STRING;
			String strMsg = DomainConstants.EMPTY_STRING;
			if(null != jsonObjDesignLevellInt){
				mpChange = checkChangeObjectState(context, jsonObjDesignLevellInt, JSON_DESIGN_LEVEL);
				/*if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : updateDesignLevel --> Change Object --> "+mpChange + "\n", writeIntoFile);
				}*/
				if(null != mpChange){
					strMsg = (String) mpChange.get(strMessage);
					strChangeId = (String) mpChange.get(DomainObject.SELECT_ID);
					strChangeName = (String) mpChange.get(DomainObject.SELECT_NAME);
				}
				
				if(UIUtil.isNotNullAndNotEmpty(strChangeId)){
					if(PROCESS.equals(strMsg)){
						int iCONameIndex = 0;
						int iFromPartIndex = 0;
						int iFindNumIndex = 0;
						int iUIDIndex = 0;
						int iToPartIndex = 0;
						int iQuantityIndex = 0;
						int iRelNameIndex = 0;
						int iNotesIndex = 0;
						int iCCISDocNum = 0;
						String strColIdentifier = DomainConstants.EMPTY_STRING;
						if(jsonObjDesignLevellInt != null){
							// Identifying the indexes
							jaColumnIdentifiers = jsonObjDesignLevellInt.getJSONArray(JSON_COLUMN_IDENTIFIERS);
							if(jaColumnIdentifiers.length() > 0){
				            	for(int i = 0; i < jaColumnIdentifiers.length(); i++){
				            		strColIdentifier = jaColumnIdentifiers.getString(i);
				            		if("DOCUMENT_NO".equalsIgnoreCase(strColIdentifier)){
				            			iCONameIndex = i;
				            		}else if("ASSEMBLY_NO".equalsIgnoreCase(strColIdentifier)){
				            			iFromPartIndex = i;
				            		}else if("ASSEMBLY_ITEM".equalsIgnoreCase(strColIdentifier)){
				            			iFindNumIndex = i;
				            		}else if("SIN".equalsIgnoreCase(strColIdentifier)){
				            			iUIDIndex = i;
				            		}else if("PART_NO".equalsIgnoreCase(strColIdentifier)){
				            			iToPartIndex = i;
				            		}else if("QUANTITY".equalsIgnoreCase(strColIdentifier)){
				            			iQuantityIndex = i;
				            		}else if("IC_FL".equalsIgnoreCase(strColIdentifier)){
				            			iRelNameIndex = i;
				            		}else if("ASSEMBLY_COMMENT".equalsIgnoreCase(strColIdentifier)){
				            			iNotesIndex = i;
				            		}else if("CCIS_DOCUMENT_NO".equalsIgnoreCase(strColIdentifier)){
				            			iCCISDocNum = i;
				            		}
				            	}
				            }
							
							// Table Data
							jaTableData = jsonObjDesignLevellInt.getJSONArray(JSON_TABLE_DATA);
							
							// Internal method - To process Design Level 
							updateDesignLevelInternal(context, mpChange, jaTableData, iCCISDocNum, iFromPartIndex, iFindNumIndex, iUIDIndex, iToPartIndex, iQuantityIndex, iRelNameIndex, iNotesIndex);
							
						}
					}else{
						//notifyCOCoOrdinator(context, mpChange, DomainConstants.EMPTY_STRING, STATUS_CANNOT_PROCESS, strMsg);	
					}
				}else{
					if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						_LOGGER.debug("PWCCMAppIntegration : updateDesignLevel --> Change Object '"+strChangeName+"' doesn't exists in EV6.");
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					}
					notifyCOCoOrdinator(context, null, JSON_DESIGN_LEVEL, STATUS_FAIL, "Change Order '"+strChangeName+"' doesn't exists in EV6.");
				}
			}
		    if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : updateDesignLevel --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateDesignLevel --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : updateDesignLevel :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}finally{
			if(isContextPushed){
				ContextUtil.popContext(context);	
			}
		}	
	}
	
	/**
	* This method is called internally, to update design level BOM.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void updateDesignLevelInternal(Context context, Map mpCO, JSONArray jaTableData, int iCCISDocNum, int iFromPartIndex, int iFindNumIndex, int iUIDIndex, int iToPartIndex, int iQuantityIndex, int iRelNameIndex, int iNotesIndex) throws Exception{
		String strFromPartName = DomainConstants.EMPTY_STRING;
		DomainObject doFROMPart = null;
		DomainObject doTOPart = null;
		MapList mlFROMPart = new MapList();
		Map mpFROMPart = null;
		Map mpTOPart = null;
		String strRelNameFlag = DomainConstants.EMPTY_STRING;
		String strFROMPartId = DomainConstants.EMPTY_STRING;
		String strTOPartId = DomainConstants.EMPTY_STRING;
		String strTOPartName = DomainConstants.EMPTY_STRING;
		MapList mlEBOM = new MapList();
		MapList mlAlternate = new MapList();
		String strToPartName = DomainConstants.EMPTY_STRING;
		MapList mlTOPart = new MapList();
		Map mpEBOM = null;
		Map mpAlternate = null;
		boolean bEBOMExistsFlag = false;
		boolean bAlternateExistsFlag = false;
		Map mpAttributes = null;
		DomainRelationship doEBOMRel = null;
		DomainRelationship doAlternateRel = null;
		String strEBOMToPartName = DomainConstants.EMPTY_STRING;
		String strAlternateToPartName = DomainConstants.EMPTY_STRING;
		String strEBOMPwcUID = DomainConstants.EMPTY_STRING;
		String strPwcUID = DomainConstants.EMPTY_STRING;
		String strEBOMRelationshipId = DomainConstants.EMPTY_STRING;
		String strAlternateRelationshipId = DomainConstants.EMPTY_STRING;
		String strEBOMModCode = DomainConstants.EMPTY_STRING;
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 17 - Partnership Attribute for Design_Level feed
		String strEBOM_PWC_PartnerName = DomainConstants.EMPTY_STRING;
		//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 17 - Partnership Attribute for Design_Level feed
		StringList slConsToBeCheckednDeleted = new StringList();
		StringList slConsToBeKept = new StringList();
		Map mpNewCon = new HashMap();
		MapList mlNewCons = new MapList();
		JSONArray jaTableDataInd = null;
		boolean bNotify = true;
		Map mpFromParts = new HashMap();
		Map mpPartsWithEBOM = new HashMap();
		Map mpPartsWithAlternate = new HashMap();
		boolean bAlreadyCaptured = false;
		boolean bFromPartExists = true;
		// CMApp_Design_Level_Latest_Revision_Update - START
		String strTOPartRev = DomainConstants.EMPTY_STRING;
		String strEBOMToPartRev = DomainConstants.EMPTY_STRING;
		String strAlternateToPartRev = DomainConstants.EMPTY_STRING;
		// CMApp_Design_Level_Latest_Revision_Update - END
		
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		StringList slMissingPartList = new StringList();
		//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		
		try{
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : updateDesignLevelInternal --> Start \n", writeIntoFile);
				writeDataToFile("Execution Start Time of PWCCMAppIntegration : updateDesignLevelInternal --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : updateDesignLevelInternal --> Table Data Length  " + jaTableData.length() + "\n", writeIntoFile);
			}*/
			if(jaTableData.length() > 0){
            	try{
            		PropertyUtil.setGlobalRPEValue(context, "CMAPP_DESIGN_LEVEL_UPDATE", "TRUE");
            		
            		// Starting transaction
					ContextUtil.startTransaction(context,true);
					
					for(int i = 0; i < jaTableData.length(); i++){
	 	           		bAlreadyCaptured = false;
	 	           		bFromPartExists = true;
						// P-18817_CMApp_Design_Level_Update - START
						bEBOMExistsFlag = false;
						// P-18817_CMApp_Design_Level_Update - END 
	 	           		mpNewCon = new HashMap();
	 	           		mpAttributes = new HashMap();
	 	           		jaTableDataInd = jaTableData.getJSONArray(i);
	 	           		
		 	           	strFromPartName = jaTableDataInd.getString(iFromPartIndex).trim();
		 	           	if("null" == strFromPartName || DomainConstants.EMPTY_STRING.equals(strFromPartName)){
							bNotify = false;
							if(null != writeIntoFile){
								//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		   	 					_LOGGER.debug("PWCCMAppIntegration : updateDesignLevelInternal --> FROM Part Name not provided for table data index '"+i+"'");
								//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		           			}
							// Part name not provided
							notifyCOCoOrdinator(context, mpCO, JSON_DESIGN_LEVEL, STATUS_FAIL,  MESSAGE_NO_DATA_PROVIDED);
	           			}else{
		           			if(mpFromParts.containsKey(strFromPartName)){
		           				mlFROMPart = (MapList) mpFromParts.get(strFromPartName);
	 	           				mlEBOM = (MapList) mpPartsWithEBOM.get(strFromPartName);
	 	           				mlAlternate = (MapList) mpPartsWithAlternate.get(strFromPartName);
	 	           				bAlreadyCaptured = true;
	 	           			}else{
	 	           				// Getting the latest revision of FROM part
	 	           				mlFROMPart = getLatestRevisionPart(context, strFromPartName);
	 	           				if(mlFROMPart.size() > 0){
				       				mpFROMPart = (Map) mlFROMPart.get(0);
				       				strFROMPartId = (String) mpFROMPart.get(DomainObject.SELECT_ID);
				       				doFROMPart = DomainObject.newInstance(context, strFROMPartId);
				           			/*if(null != writeIntoFile){
				   	 					writeDataToFile("PWCCMAppIntegration : updateDesignLevelInternal -->From Part Id " + strFROMPartId + "\n", writeIntoFile);
				           			}*/
				           			mpFromParts.put(strFromPartName, mlFROMPart);
				           			// Getting all 1st level EBOM connections of FROM part.
		 	           				mlEBOM = getEBOMConnections(context, doFROMPart);
		 	           				mpPartsWithEBOM.put(strFromPartName, mlEBOM);
		 	           				// Getting all 1st level Alternate connections of FROM part.
			           				mlAlternate = getAlternateConnections(context, doFROMPart);
			           				mpPartsWithAlternate.put(strFromPartName, mlAlternate);
	 	           				}else{
	 	           					bFromPartExists = false;
	 	           				}
	 	           			}
		           			
			       			if(bFromPartExists){
			       				if(!bAlreadyCaptured){
		 	           				// Gathering all the EBOM & Alternate connections, which will be checked and deleted accordingly.
		 	           				if(null != mlEBOM && !mlEBOM.isEmpty()){
		 	           					for(int j = 0; j < mlEBOM.size(); j++){
		 	           						mpEBOM = (Map) mlEBOM.get(j);
		 	           						strEBOMRelationshipId = (String) mpEBOM.get(DomainRelationship.SELECT_ID);
		 	           						if(!slConsToBeCheckednDeleted.contains(strEBOMRelationshipId)){
		 	           							slConsToBeCheckednDeleted.addElement(strEBOMRelationshipId);
		 	           						}
		 	           					}
		 	           				}
		 	           				if(null != mlAlternate && !mlAlternate.isEmpty()){
			 	           				for(int j = 0; j < mlAlternate.size(); j++){
		 	           						mpAlternate = (Map) mlAlternate.get(j);
		 	           						strAlternateRelationshipId = (String) mpAlternate.get(DomainRelationship.SELECT_ID);
		 	           						if(!slConsToBeCheckednDeleted.contains(strAlternateRelationshipId)){
		 	           							slConsToBeCheckednDeleted.addElement(strAlternateRelationshipId);
		 	           						}
		 	           					}
		 	           				}
			           			}
			       				
				 	           	// Getting the relationship flag (If 'N' then EBOM OR if 'Y' then Alternate)
					 	        strRelNameFlag = jaTableDataInd.getString(iRelNameIndex).trim();
					 	        if("null" == strRelNameFlag || DomainConstants.EMPTY_STRING.equals(strRelNameFlag)){
					 	        	strRelNameFlag = DomainConstants.EMPTY_STRING;
					 	        }
				 	       		/*if(null != writeIntoFile){
				 					writeDataToFile("PWCCMAppIntegration : updateDesignLevelInternal --> RelName flag " + strRelNameFlag + "\n", writeIntoFile);
				 				}*/
					 	        if(null != strRelNameFlag && !DomainConstants.EMPTY_STRING.equals(strRelNameFlag)){
			 	           			if("N".equalsIgnoreCase(strRelNameFlag)){
			 	           				strToPartName = jaTableDataInd.getString(iToPartIndex).trim();
				 	           			if("null" == strToPartName || DomainConstants.EMPTY_STRING.equals(strToPartName)){
					 	           			if(null != writeIntoFile){
												//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						   	 					_LOGGER.debug("PWCCMAppIntegration : updateDesignLevelInternal --> TO Part Name not provided for table data index '"+i+"'");
												//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						           			}
					 	           		}else{
				 	           				// Getting the TO part
				 	           				mlTOPart = getLatestReleasedPart(context, strToPartName);
				 	           				if(mlTOPart.size() > 0){
				 	           					mpTOPart = (Map) mlTOPart.get(0);
				 	           					strTOPartId = (String) mpTOPart.get(DomainObject.SELECT_ID);
				 	           					strTOPartName = (String) mpTOPart.get(DomainObject.SELECT_NAME);
				 	           					// CMApp_Design_Level_Latest_Revision_Update - START
				 	           					strTOPartRev = (String) mpTOPart.get(DomainObject.SELECT_REVISION);
				 	           					// CMApp_Design_Level_Latest_Revision_Update - END
				 	           					doTOPart = DomainObject.newInstance(context, strTOPartId);
				 	           					strPwcUID = jaTableDataInd.getString(iUIDIndex).trim();
				 	           					if("null" == strPwcUID){
				 	           						strPwcUID = DomainConstants.EMPTY_STRING;
				 	           					}
					 	           				/*if(null != writeIntoFile){
						 	   	 					writeDataToFile("PWCCMAppIntegration : updateDesignLevelInternal -->To Part Id " + strTOPartId + "\n", writeIntoFile);
					 	   	 				    }*/
				 	           					// Getting the attributes values, coming from down stair
				 	           					mpAttributes = getEBOMAttributesValues(context, jaTableDataInd, iCCISDocNum, iFindNumIndex, iUIDIndex, iQuantityIndex, iNotesIndex);
					 	           		   		
				 	           					// Comparing EBOM-CACHE & Incoming Design Level
				 	           					for(int j = 0; j < mlEBOM.size(); j++){
				 	           						bEBOMExistsFlag = false;
				 	           						mpEBOM = (Map) mlEBOM.get(j);
				 	           						strEBOMToPartName = (String) mpEBOM.get(DomainObject.SELECT_NAME);
				 	           						// CMApp_Design_Level_Latest_Revision_Update - START
				 	           						strEBOMToPartRev = (String) mpEBOM.get(DomainObject.SELECT_REVISION);
				 	           						// CMApp_Design_Level_Latest_Revision_Update - END
				 	           						// CMApp_Design_Level_Latest_Revision_Update - Added Revision Check
				 	           						if(null != strEBOMToPartName && strTOPartName.equals(strEBOMToPartName) && strTOPartRev.equals(strEBOMToPartRev)){
				 	           							strEBOMPwcUID = (String) mpEBOM.get(SELECTABLE_ATTR_PWC_UID);
				 	           							if(null != strEBOMPwcUID && null != strPwcUID && !DomainConstants.EMPTY_STRING.equals(strEBOMPwcUID) && !DomainConstants.EMPTY_STRING.equals(strPwcUID)){
				 	           								strEBOMRelationshipId = (String) mpEBOM.get(DomainRelationship.SELECT_ID);
					 	           							/*if(null != writeIntoFile){
						 	   		 	   	 					writeDataToFile("PWCCMAppIntegration : updateDesignLevelInternal -->EBOM TO Part Name " + strEBOMToPartName + "\n", writeIntoFile);
						 	   		 	   	 				    writeDataToFile("PWCCMAppIntegration : updateDesignLevelInternal -->EBOM Rel Id " + strEBOMRelationshipId + "\n", writeIntoFile);
					 	   		 	   	 				    }*/
				 	           								if(null != strEBOMRelationshipId){
				 	           									if(strEBOMPwcUID.equals(strPwcUID)){
				 	           										bEBOMExistsFlag = true;
				 	           										doEBOMRel = DomainRelationship.newInstance(context, strEBOMRelationshipId);
				 	           										doEBOMRel.setAttributeValues(context, mpAttributes);
				 	           										if(!slConsToBeKept.contains(strEBOMRelationshipId)){
				 	           											slConsToBeKept.addElement(strEBOMRelationshipId);
				 	           										}
				 	           										break;
				 	           									}
				 	           								}
				 	           							}
				 	           						}
				 	           					}
				 	           					
				 	           					// Collecting List of New EBOM connection 
				 	           					if(!bEBOMExistsFlag){
				 	           						mpNewCon.put("FromID", strFROMPartId);
				 	           						mpNewCon.put("ToID", strTOPartId);
				 	           						mpNewCon.put("Relationship", DomainConstants.RELATIONSHIP_EBOM);
				 	           						for(int j = 0; j < mlEBOM.size(); j++){
				 	           							mpEBOM = (Map) mlEBOM.get(j);
				 	           							strEBOMPwcUID = (String) mpEBOM.get(SELECTABLE_ATTR_PWC_UID);
				 	           							if(strPwcUID.equals(strEBOMPwcUID)){
				 	           								strEBOMModCode = (String) mpEBOM.get(SELECTABLE_ATTR_PWC_MODULE_CODE);
				 	           								//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 17 - Partnership Attribute for Design_Level feed
				 	           								strEBOM_PWC_PartnerName = (String) mpEBOM.get(SELECTABLE_ATTR_PWC_PARTNERSHIP_NAME);
				 	           								if(null != strEBOMModCode && UIUtil.isNotNullAndNotEmpty(strEBOM_PWC_PartnerName)){
																//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 17 - Partnership Attribute for Design_Level feed
				 	           									mpAttributes.put(ATTR_PWC_MODULE_CODE, strEBOMModCode);
				 	           									//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 17 - Partnership Attribute for Design_Level feed
				 	           									mpAttributes.put(ATTR_PWC_PARTNERSHIP_NAME, strEBOM_PWC_PartnerName);
				 	           									//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 17 - Partnership Attribute for Design_Level feed
				 	           									break;
				 	           								}
				 	           							}
				 	           						}
				 	           						mpNewCon.put("Attributes", mpAttributes);
				 	           						mlNewCons.add(mpNewCon);
				 	           					}
				 	           				}else{
				 	           					bNotify = false;	
												if(null != writeIntoFile){
													//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
													_LOGGER.debug("PWCCMAppIntegration : updateDesignLevelInternal --> No such part in EV6 (TO) ");
													//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
												}
												// There is no such part in EV6.
												//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
												//notifyCOCoOrdinator(context, mpCO, JSON_DESIGN_LEVEL, STATUS_FAIL, MESSAGE_NO_SUCH_PART);
												slMissingPartList.addElement(strToPartName);
												//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
					 	        		  	}
					 	           		}
			 	           			}else if("Y".equalsIgnoreCase(strRelNameFlag)){
		 	           					strToPartName = jaTableDataInd.getString(iToPartIndex).trim();
			 	           				if("null" == strToPartName || DomainConstants.EMPTY_STRING.equals(strToPartName)){
					 	           			if(null != writeIntoFile){
												//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						   	 					_LOGGER.debug("PWCCMAppIntegration : updateDesignLevelInternal --> TO Part Name not provided for table data index '"+i+"'");
												//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						           			}
					 	           		}else{
			 	           					// Getting the TO part
			 	           					mlTOPart = getLatestReleasedPart(context, strToPartName);
			 	           					if(mlTOPart.size() > 0){
			 	           						mpTOPart = (Map) mlTOPart.get(0);
			 	           						strTOPartId = (String) mpTOPart.get(DomainObject.SELECT_ID);
			 	           						strTOPartName = (String) mpTOPart.get(DomainObject.SELECT_NAME);
			 	           						// CMApp_Design_Level_Latest_Revision_Update - START
				 	           					strTOPartRev = (String) mpTOPart.get(DomainObject.SELECT_REVISION);
				 	           					// CMApp_Design_Level_Latest_Revision_Update - END
			 	           						doTOPart = DomainObject.newInstance(context, strTOPartId);
			 	           						/*if(null != writeIntoFile){
			 			 	   	 					writeDataToFile("PWCCMAppIntegration : updateDesignLevelInternal -->TO Part Id--->"+strTOPartId+"\n", writeIntoFile);
			 		 	   	 				    }*/
			 	           						
			 	           						// Getting the attributes values, coming from down stair
				 	           					mpAttributes = getAlternateAttributesValues(context, jaTableDataInd, iCCISDocNum, iFindNumIndex, iQuantityIndex, iNotesIndex);
				 	           					
				 	           					// Comparing Alternate-CACHE & Incoming Design Level
				 	           					for(int j = 0; j < mlAlternate.size(); j++){
				 	           						bAlternateExistsFlag = false;
				 	           						mpAlternate = (Map) mlAlternate.get(j);
				 	           						strAlternateToPartName = (String) mpAlternate.get(DomainObject.SELECT_NAME);
				 	           						// CMApp_Design_Level_Latest_Revision_Update - START
				 	           						strAlternateToPartRev = (String) mpAlternate.get(DomainObject.SELECT_REVISION);
				 	           						// CMApp_Design_Level_Latest_Revision_Update - END
				 	           						// CMApp_Design_Level_Latest_Revision_Update - Added Revision Check
				 	           						if(null != strAlternateToPartName && strTOPartName.equals(strAlternateToPartName) && strTOPartRev.equals(strAlternateToPartRev)){
				 	           							strAlternateRelationshipId = (String) mpAlternate.get(DomainRelationship.SELECT_ID);
			 	           								if(null != strAlternateRelationshipId){
			 	           									bAlternateExistsFlag = true;
		 	           										doAlternateRel = DomainRelationship.newInstance(context, strAlternateRelationshipId);
		 	           										doAlternateRel.setAttributeValues(context, mpAttributes);
		 	           										if(!slConsToBeKept.contains(strAlternateRelationshipId)){
		 	           											slConsToBeKept.addElement(strAlternateRelationshipId);
			           										}
		 	           										break;
			 	           								}
				 	           						}
				 	           					}
				 	           					
				 	           					// Collecting list of New Alternate connection 
				 	           					if(!bAlternateExistsFlag){
					 	           					mpNewCon.put("FromID", strFROMPartId);
				 	           						mpNewCon.put("ToID", strTOPartId);
				 	           						mpNewCon.put("Relationship", DomainConstants.RELATIONSHIP_ALTERNATE);
				 	           						mpNewCon.put("Attributes", mpAttributes);
				 	           						mlNewCons.add(mpNewCon);
				 	           					}
			 	           					}else{
				 	           					bNotify = false;	
												if(null != writeIntoFile){
													//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
													_LOGGER.debug("PWCCMAppIntegration : updateDesignLevelInternal --> No such part in EV6 (TO) ");
													//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
												}
												// There is no such part in EV6.
												//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
												//notifyCOCoOrdinator(context, mpCO, JSON_DESIGN_LEVEL, STATUS_FAIL,  MESSAGE_NO_SUCH_PART);
												slMissingPartList.addElement(strToPartName);
												//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
					 	        		  	}
					 	           		}
			 	           			}
			 	           		}else{
	 	           					bNotify = false;
		 	           				if(null != writeIntoFile){
										//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
										_LOGGER.debug("PWCCMAppIntegration : updateDesignLevelInternal -->Relationship Flag is not Provided ");
										//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
									}
									// Relationship flag is not provided
									notifyCOCoOrdinator(context, mpCO, JSON_DESIGN_LEVEL, STATUS_FAIL,  MESSAGE_NO_REL_FLAG_PROVIDED);
 	           					}
			 	            }else{
		 	        	   		bNotify = false;
			 	        	   	if(null != writeIntoFile){
									//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	   		 	   	 				_LOGGER.debug("PWCCMAppIntegration : updateDesignLevelInternal --> No such part in EV6 (FROM) ");
									//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		 	   	 				}
								// There is no such part in EV6.
								//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
								//notifyCOCoOrdinator(context, mpCO, JSON_DESIGN_LEVEL, STATUS_FAIL,  MESSAGE_NO_SUCH_PART);
			 	        	   slMissingPartList.addElement(strFromPartName);
							   //END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		 	        	   	}
		 	           	}
	 	           	}
					//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		 	       	if(null != slMissingPartList && slMissingPartList.size()>0){
		 	       		StringBuffer sbMessageForPart = new StringBuffer();
		 	       		sbMessageForPart.append(MESSAGE_NO_SUCH_PARTS);
		 	       		sbMessageForPart.append("\n");
		 	       		sbMessageForPart.append(slMissingPartList.toString());
		 	       		notifyCOCoOrdinator(context, mpCO, JSON_DESIGN_LEVEL, STATUS_FAIL, sbMessageForPart.toString());
	 	           	}
		 	       	//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
					
	 	           	// Deleting EBOM & Alternate connections, which are not in the input from CM App.
	 	           	if(null != slConsToBeCheckednDeleted && slConsToBeCheckednDeleted.size() > 0){
	 	           		deleteEBOMnAlternateConnections(context, slConsToBeCheckednDeleted, slConsToBeKept);
	 	           	}
	 	           	
					// New EBOM & Alternate connections, coming from CM App.
	 	           	if(null != mlNewCons && mlNewCons.size() > 0){
	 	           		connectEBOMnAlternateConnections(context, mlNewCons);
	 	           	}
	 	           	
	 	           	// Committing transaction
	 	           	ContextUtil.commitTransaction(context);
	 	           
	 	           	// Notifying the update status 'Pass' to CO coordinator
	 	           	if(bNotify){
	 	           		notifyCOCoOrdinator(context, mpCO, JSON_DESIGN_LEVEL, STATUS_PASS, null);
	 	           	}
 	           	
            	}catch(Exception ex){
            		ex.printStackTrace();
            		if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        				_LOGGER.debug("Exception in PWCCMAppIntegration : updateDesignLevelInternal ---> "+ex.getMessage());
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        			}
            		// Notifying the update status 'Fail' to CO coordinator
        			notifyCOCoOrdinator(context, mpCO, JSON_DESIGN_LEVEL, STATUS_FAIL, ex.getMessage());
        			
        			// Aborting transaction
        			ContextUtil.abortTransaction(context);
            	}finally{
            		PropertyUtil.setGlobalRPEValue(context, "CMAPP_DESIGN_LEVEL_UPDATE", DomainConstants.EMPTY_STRING);
            	}
            	
             }else{
            	if(null != writeIntoFile){
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
     				_LOGGER.debug("Exception in PWCCMAppIntegration : updateDesignLevelInternal -->No Connections Provided :-");
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
     			}
				// No connections provided.
				notifyCOCoOrdinator(context, mpCO, JSON_DESIGN_LEVEL, STATUS_FAIL,  MESSAGE_NO_DATA_PROVIDED);
             }
			/*if(null != writeIntoFile){
				writeDataToFile("Execution End Time of PWCCMAppIntegration : updateDesignLevelInternal --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
				//writeDataToFile("PWCCMAppIntegration : updateDesignLevelInternal --> Exit \n", writeIntoFile);
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : updateDesignLevelInternal :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}	
	}
	
	/**
	* This method is to get the latest revision part.
	* @param context
	* @param args
	* @returns MapList
	* @throws Exception if the operation fails
	*/
	public MapList getLatestRevisionPart(Context context, String strPartName) throws Exception{
		MapList mlReturn = new MapList();
		try{
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionPart --> Start \n", writeIntoFile);
				writeDataToFile("Execution Start Time of PWCCMAppIntegration : getLatestRevisionPart --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
			}*/
			StringList objectSelects = new StringList();
         	objectSelects.addElement(DomainObject.SELECT_ID);
         	objectSelects.addElement(DomainObject.SELECT_NAME);
			//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
         	objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
         	objectSelects.addElement(SELECTABLE_ATTR_PWC_SALE_CODE);
			//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
         	String strWhrClause = "revision == last";
			strWhrClause += " && policy == \"" + DomainConstants.POLICY_EC_PART + "\"";
         	/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionPart --> Find Objects - Type(s) --> "+DomainConstants.TYPE_PART + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionPart --> Part Name --> " + strPartName + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionPart --> Find Objects - where clause --> "+strWhrClause + "\n", writeIntoFile);	
			}*/
         	if(null != strPartName && !DomainConstants.EMPTY_STRING.equals(strPartName)){
				mlReturn= DomainObject.findObjects(context, 
		               		DomainConstants.TYPE_PART, 
		               		strPartName, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		strWhrClause, 
		               		false, 
		               		objectSelects);
         	}
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionPart --> Object List (Maplist) --> "+mlReturn + "\n", writeIntoFile);
			}
			if(null != writeIntoFile){
				writeDataToFile("Execution End Time of PWCCMAppIntegration : getLatestRevisionPart --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionPart --> Exit \n", writeIntoFile);
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
			//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation			
				_LOGGER.debug("Exception in PWCCMAppIntegration : getLatestRevisionPart :-"+ex.getMessage());
			//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		return mlReturn;
	}
	
	//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	/**
	* This method is to get the all revisions part.
	* @param context
	* @param args
	* @returns MapList
	* @throws Exception if the operation fails
	*/
	public MapList getPartListIncludingLatestRevision(Context context, String strPartName) throws Exception{
		MapList mlReturn = new MapList();
		try{
			StringList objectSelects = new StringList();
         	objectSelects.addElement(DomainObject.SELECT_ID);
         	objectSelects.addElement(DomainObject.SELECT_NAME);
         	objectSelects.addElement(DomainObject.SELECT_REVISION);
         	String strWhrClause = "policy == \"" + DomainConstants.POLICY_EC_PART + "\"";
         	if(null != strPartName && !DomainConstants.EMPTY_STRING.equals(strPartName)){
				mlReturn= DomainObject.findObjects(context, 
		               		DomainConstants.TYPE_PART, 
		               		strPartName, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		strWhrClause, 
		               		false, 
		               		objectSelects);
         	}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				_LOGGER.debug("Exception in PWCCMAppIntegration : getLatestRevisionPart :-"+ex.getMessage());
			}
		}
		return mlReturn;
	}
	//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	
	/**
	* This method is to get all the 1st level EBOM connections.
	* @param context
	* @param args
	* @returns MapList
	* @throws Exception if the operation fails
	*/
	public MapList getEBOMConnections(Context context, DomainObject doPart) throws Exception{
		MapList mlReturn  = new MapList();
		try{
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getEBOMConnections --> Start \n", writeIntoFile);
				writeDataToFile("Execution Start Time of PWCCMAppIntegration : getEBOMConnections --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
			}*/
			StringList objectSelects = new StringList();
         	objectSelects.addElement(DomainObject.SELECT_ID);
         	objectSelects.addElement(DomainObject.SELECT_NAME);
         	// CMApp_Design_Level_Latest_Revision_Update - START
         	objectSelects.addElement(DomainObject.SELECT_REVISION);
         	// CMApp_Design_Level_Latest_Revision_Update - END
         	StringList relSelects = new StringList();
         	relSelects.addElement(DomainRelationship.SELECT_ID);
         	relSelects.addElement(DomainConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION);         	
         	relSelects.addElement(SELECTABLE_ATTR_END_EFFECTIVITY_DATE);
         	relSelects.addElement(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
         	relSelects.addElement(SELECTABLE_ATTR_HAS_MANUFACTURING_SUBSTITUTE);
         	relSelects.addElement(SELECTABLE_ATTR_NOTES);
         	relSelects.addElement(DomainConstants.SELECT_ORIGINATOR);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_COORDINATE);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_EAPL_LOCK);
         	relSelects.addElement(SELECTABLE_ATTR_EAPL_SYMBOL);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_INTERIM_DOC_FLAG);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_MODULE_CODE);
         	//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 17 - Partnership Attribute for Design_Level feed
         	relSelects.addElement(SELECTABLE_ATTR_PWC_PARTNERSHIP_NAME);
         	//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 17 - Partnership Attribute for Design_Level feed
         	relSelects.addElement(SELECTABLE_ATTR_PWC_SALEABLE_CODE);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_STATUS_CODE);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_UID);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_VECTOR);
         	relSelects.addElement(DomainConstants.SELECT_ATTRIBUTE_QUANTITY);
         	relSelects.addElement(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
         	relSelects.addElement(SELECTABLE_ATTR_START_EFFECTIVITY_DATE);
         	relSelects.addElement(SELECTABLE_ATTR_SOURCE);
         	relSelects.addElement(DomainConstants.SELECT_ATTRIBUTE_USAGE);
			mlReturn = doPart.getRelatedObjects(context, 
         			DomainConstants.RELATIONSHIP_EBOM, 
         			DomainConstants.TYPE_PART, 
         			objectSelects, 
         			relSelects, 
         			false,
         			true,
         			(short) 1,
         			null,
         			null,
         			0);
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getEBOMConnections --> EBOM Connection list size (Maplist) --> "+mlReturn.size()+ "\n", writeIntoFile);
				writeDataToFile("Execution End Time of PWCCMAppIntegration : getEBOMConnections --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getEBOMConnections --> Exit \n", writeIntoFile);
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : getEBOMConnections :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		return mlReturn;
	}
	
	/**
	* This method is to get all the 1st level Alternate connections.
	* @param context
	* @param args
	* @returns MapList
	* @throws Exception if the operation fails
	*/
	public MapList getAlternateConnections(Context context, DomainObject doPart) throws Exception{
		MapList mlReturn  = new MapList();
		try{
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getAlternateConnections --> Start \n", writeIntoFile);
				writeDataToFile("Execution Start Time of PWCCMAppIntegration : getAlternateConnections --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
			}*/
			StringList objectSelects = new StringList();
         	objectSelects.addElement(DomainObject.SELECT_ID);
         	objectSelects.addElement(DomainObject.SELECT_NAME);
         	// CMApp_Design_Level_Latest_Revision_Update - START
         	objectSelects.addElement(DomainObject.SELECT_REVISION);
         	// CMApp_Design_Level_Latest_Revision_Update - END
         	StringList relSelects = new StringList();
         	relSelects.addElement(DomainRelationship.SELECT_ID);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_ASSEMBLY_LINE_NUMBER);         	
         	relSelects.addElement(SELECTABLE_ATTR_PWC_REVISED_DATE);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_REVISED_BY);
         	relSelects.addElement(DomainConstants.SELECT_ATTRIBUTE_QUANTITY);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_RAW_MATERIAL);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_MAKE_FROM);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_ENG_PART_NUMBER);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_REFERENCE_DOCUMENT);
         	relSelects.addElement(SELECTABLE_ATTR_COMMENT);
         	relSelects.addElement(SELECTABLE_ATTR_PWC_MD_DOCUMENT_NUMBER);         	
         	//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 18 - Addressing Alternate connection update for HC-18448
         	String strRelWhere  = "attribute[" + ATTR_SOURCE + "] != 'EQUAL' && attribute["+ ATTR_SOURCE + "] != 'PSL'";
			mlReturn = doPart.getRelatedObjects(context, 
         			DomainConstants.RELATIONSHIP_ALTERNATE, 
         			DomainConstants.TYPE_PART, 
         			objectSelects, 
         			relSelects, 
         			false,
         			true,
         			(short) 1,
         			null,
         			strRelWhere,
         			0);
			//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 18 - Addressing Alternate connection update for HC-18448
	        /*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getAlternateConnections --> Alternate Connection list size (Maplist) --> "+mlReturn.size()+ "\n", writeIntoFile);
				writeDataToFile("Execution End Time of PWCCMAppIntegration : getAlternateConnections --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getAlternateConnections --> Exit \n", writeIntoFile);
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : getAlternateConnections :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		return mlReturn;
	}
	
	/**
	* This method is to get the TO part.
	* @param context
	* @param args
	* @returns MapList
	* @throws Exception if the operation fails
	*/
	public MapList getLatestReleasedPart(Context context, String strPartName) throws Exception{
		MapList mlReturn = new MapList();
		try{
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart --> Start \n", writeIntoFile);
				writeDataToFile("Execution Start Time of PWCCMAppIntegration : getLatestReleasedPart --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
			}*/
			StringList objectSelects = new StringList();
         	objectSelects.addElement(DomainObject.SELECT_ID);
         	objectSelects.addElement(DomainObject.SELECT_NAME);
         	// CMApp_Design_Level_Latest_Revision_Update - START
         	objectSelects.addElement(DomainObject.SELECT_REVISION);  	
         	// CMApp_Design_Level_Latest_Revision_Update - END
         	objectSelects.addElement("revindex");
         	// CMApp_Design_Level_Latest_Revision_Update - Modified where clause
         	StringBuilder sbWhereClause = new StringBuilder();
			sbWhereClause.append("((").append(DomainConstants.SELECT_CURRENT).append("==").append("\'").append(PART_STATE_RELEASE).append("\'");
			sbWhereClause.append(" || ").append(DomainConstants.SELECT_CURRENT).append("==").append("\'").append(PART_STATE_EXPERIMENTAL).append("\'");
			sbWhereClause.append(" || ").append(DomainConstants.SELECT_CURRENT).append("==").append("\'").append(PART_STATE_PRODUCTION).append("\'");
			
			//Added conditions for DORMANT and OBSOLETE Part HP-18559
			sbWhereClause.append(" || ").append(DomainConstants.SELECT_CURRENT).append("==").append("\'").append(PART_STATE_OBSOLETE).append("\'");
			sbWhereClause.append(" || ").append(DomainConstants.SELECT_CURRENT).append("==").append("\'").append(PART_STATE_DORMANT).append("\'");
			sbWhereClause.append(")");
			sbWhereClause.append(" && policy").append("!=").append("\'").append(DomainConstants.POLICY_DEVELOPMENT_PART).append("\'");	
			sbWhereClause.append(")");
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart --> Find Objects - Type(s) --> "+DomainConstants.TYPE_PART + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart --> Find Objects - where clause --> "+sbWhereClause.toString() + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart --> Find Objects - Part Name --> "+strPartName + "\n", writeIntoFile);
			}*/
			if(null != strPartName && !DomainConstants.EMPTY_STRING.equals(strPartName)){
				mlReturn= DomainObject.findObjects(context, 
		               		DomainConstants.TYPE_PART, 
		               		strPartName, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		sbWhereClause.toString(), 
		               		false, 
		               		objectSelects);
				/*if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart -->Object List (Maplist) --> "+mlReturn.size() + "\n", writeIntoFile);
				}*/
				
				if(mlReturn.size() > 0){
					mlReturn.sort("revindex", "descending", "integer");
				}else{
					sbWhereClause = new StringBuilder();
					sbWhereClause.append("revision == last");
					/*if(null != writeIntoFile){
						writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart --> Find Objects - Type(s) --> "+DomainConstants.TYPE_PART + "\n", writeIntoFile);
						writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart --> Find Objects - where clause --> "+sbWhereClause.toString() + "\n", writeIntoFile);
						writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart --> Find Objects - Part Name --> "+strPartName + "\n", writeIntoFile);
					}*/
					mlReturn= DomainObject.findObjects(context, 
		               		DomainConstants.TYPE_PART, 
		               		strPartName, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		sbWhereClause.toString(), 
		               		false, 
		               		objectSelects);
							
					/*if(null != writeIntoFile){
						writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart --> Object List (Maplist) --> "+mlReturn.size() + "\n", writeIntoFile);
					}*/
				}
			}
			/*else
			{
				if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart --> Object List (Maplist) --> "+mlReturn.size() + "\n", writeIntoFile);
				}
			}
			if(null != writeIntoFile){
				writeDataToFile("Execution End Time of PWCCMAppIntegration : getLatestReleasedPart --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getLatestReleasedPart --> Exit \n", writeIntoFile);
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : getLatestReleasedPart :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		return mlReturn;
	}
	
	/**
	* This method is to get the EBOM connection attribute values, coming from down stair.
	* @param context
	* @param args
	* @returns Map
	* @throws Exception if the operation fails
	*/
	public Map getEBOMAttributesValues(Context context, JSONArray jaTableDataInd, int iCCISDocNum, int iFindNumIndex, int iUIDIndex, int iQuantityIndex, int iNotesIndex) throws Exception{
		Map mpReturn = new HashMap();
		String strAttrSource = DomainConstants.EMPTY_STRING;
		String strAttrFindNum = DomainConstants.EMPTY_STRING;
		String strAttrPWCUID = DomainConstants.EMPTY_STRING;
		String strAttrQuan = DomainConstants.EMPTY_STRING;
		String strAttrNotes = DomainConstants.EMPTY_STRING;
		try{
			strAttrSource = jaTableDataInd.getString(iCCISDocNum);
			if("null" == strAttrSource){
				strAttrSource = DomainConstants.EMPTY_STRING;
			}  
			strAttrFindNum = jaTableDataInd.getString(iFindNumIndex);
			if("null" == strAttrFindNum){
				strAttrFindNum = DomainConstants.EMPTY_STRING;
			}
			strAttrPWCUID = jaTableDataInd.getString(iUIDIndex);
			if("null" == strAttrPWCUID){
				strAttrPWCUID = DomainConstants.EMPTY_STRING;
			}
			strAttrQuan = jaTableDataInd.getString(iQuantityIndex).trim();
			if("null" == strAttrQuan){
				strAttrQuan = DomainConstants.EMPTY_STRING;
			}else if("999".equals(strAttrQuan)){ // 999 is a notation business use to represent that the quantity is not defined yet.
				strAttrQuan = "0.001";
				// HC-17970 if the value if 0.001 usage should be as required
				mpReturn.put(DomainConstants.ATTRIBUTE_USAGE, "As Required");
			}else if(strAttrQuan.startsWith("-")){ // -quantity (-7) indicates that the quantity of this part, in a model, is maximum.
				strAttrQuan = strAttrQuan.substring(1, strAttrQuan.length());
				// HC-17970 if the value has minus (-) value, usage should be Reference-Eng
				mpReturn.put(DomainConstants.ATTRIBUTE_USAGE, "Reference-Eng");
				// Added_For_Heat-C-18511 - START
			}else if("REF".equalsIgnoreCase(strAttrQuan)){
				strAttrQuan = "0";
				mpReturn.put(DomainConstants.ATTRIBUTE_USAGE, "Reference-Eng");
				// Added_For_Heat-C-18511 - END
			}
			strAttrNotes = jaTableDataInd.getString(iNotesIndex);
			if("null" == strAttrNotes){
				strAttrNotes = DomainConstants.EMPTY_STRING;
			}
			mpReturn.put(ATTR_SOURCE, strAttrSource.trim());
			mpReturn.put(DomainConstants.ATTRIBUTE_FIND_NUMBER, strAttrFindNum.trim());
			mpReturn.put(ATTR_PWC_UID, strAttrPWCUID.trim());
			mpReturn.put(DomainConstants.ATTRIBUTE_QUANTITY, strAttrQuan.trim());
			mpReturn.put(DomainConstants.ATTRIBUTE_NOTES, strAttrNotes.trim());

		}catch(Exception ex){
			ex.printStackTrace();
		}
		return mpReturn;
	}
	
	/**
	* This method is to get the Alternate connection attribute values, coming from down stair.
	* @param context
	* @param args
	* @returns Map
	* @throws Exception if the operation fails
	*/
	public Map getAlternateAttributesValues(Context context, JSONArray jaTableDataInd, int iCCISDocNum, int iFindNumIndex, int iQuantityIndex, int iNotesIndex) throws Exception{
		Map mpReturn = new HashMap();
		String strAttrSource = DomainConstants.EMPTY_STRING;
		String strAttrFindNum = DomainConstants.EMPTY_STRING;
		String strAttrQuan = DomainConstants.EMPTY_STRING;
		String strAttrNotes = DomainConstants.EMPTY_STRING;
		try{
			strAttrSource = jaTableDataInd.getString(iCCISDocNum);
			if("null" == strAttrSource){
				strAttrSource = DomainConstants.EMPTY_STRING;
			}  
			strAttrFindNum = jaTableDataInd.getString(iFindNumIndex);
			if("null" == strAttrFindNum){
				strAttrFindNum = DomainConstants.EMPTY_STRING;
			}
			strAttrQuan = jaTableDataInd.getString(iQuantityIndex);
			if("null" == strAttrQuan){
				strAttrQuan = DomainConstants.EMPTY_STRING;
			}else if("999".equals(strAttrQuan)){ // 999 is a notation business use to represent that the quantity is not defined yet.
				strAttrQuan = "0.001";
			}else if(strAttrQuan.startsWith("-")){ // -quantity (-7) indicates that the quantity of this part, in a model, is maximum.
				strAttrQuan = strAttrQuan.substring(1, strAttrQuan.length());
			}
			strAttrNotes = jaTableDataInd.getString(iNotesIndex);
			if("null" == strAttrNotes){
				strAttrNotes = DomainConstants.EMPTY_STRING;
			}
			mpReturn.put(ATTR_SOURCE, strAttrSource.trim());
			mpReturn.put(DomainConstants.ATTRIBUTE_FIND_NUMBER, strAttrFindNum.trim());
			mpReturn.put(DomainConstants.ATTRIBUTE_QUANTITY, strAttrQuan.trim());
			mpReturn.put(DomainConstants.ATTRIBUTE_NOTES, strAttrNotes.trim());
			// HC-TBD if the value if 0.001 usage should be as required
			if("0.001".equals(strAttrQuan))
			{
				mpReturn.put(DomainConstants.ATTRIBUTE_USAGE, "As Required");				
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}
		return mpReturn;
	}
	
	/**
	* This method is to delete EBOM & Alternate connections, which are not in the input from CM App.
	* @param context
	* @param args
	* @returns Map
	* @throws Exception if the operation fails
	*/
	public void deleteEBOMnAlternateConnections(Context context, StringList slConsToBeCheckednDeleted, StringList slConsToBeKept) throws Exception{
		StringList slConsToBeDeleted = new StringList(slConsToBeCheckednDeleted);
		String strEBOMRelationshipId = DomainConstants.EMPTY_STRING;
		try{
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : deleteEBOMConnections --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : deleteEBOMConnections --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : deleteEBOMConnections --> EBOM Connections List : "+slConsToBeCheckednDeleted +"\n", writeIntoFile);
			}
			if(slConsToBeCheckednDeleted.size() > 0){
				if(slConsToBeKept.size() > 0){
	        	   for(int j = 0; j < slConsToBeCheckednDeleted.size(); j++){
	        		  strEBOMRelationshipId = (String) slConsToBeCheckednDeleted.get(j);
	        		  if(slConsToBeKept.contains(strEBOMRelationshipId)){
	        			  slConsToBeDeleted.remove(strEBOMRelationshipId);
	        			  /*if(null != writeIntoFile){
	        				  writeDataToFile("PWCCMAppIntegration : deleteEBOMConnections -->   EBOM Rel Id to be Kept --> "+strEBOMRelationshipId + "\n", writeIntoFile);
	        			  }*/
	        		  }
	        	   }
				}
				if(slConsToBeDeleted.size() > 0){
        		   String[] strRelsToBeDeleted = new String[slConsToBeDeleted.size()];
        		   for(int i = 0; i < slConsToBeDeleted.size(); i++){
        			   strRelsToBeDeleted[i] = (String) slConsToBeDeleted.get(i);
        		   }
        		   DomainRelationship.disconnect(context, strRelsToBeDeleted);
				}
			}
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : deleteEBOMConnections --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : deleteEBOMConnections --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : deleteEBOMConnections :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
	}
	
	/**
	* This method is to connect EBOM & Alternate connections, which are coming from CM App.
	* @param context
	* @param args
	* @returns Map
	* @throws Exception if the operation fails
	*/
	public void connectEBOMnAlternateConnections(Context context, MapList mlNewCons) throws Exception{
		Map mpNewCon = new HashMap();
		String strRelationshipName = DomainConstants.EMPTY_STRING;
		String strFromId = DomainConstants.EMPTY_STRING;
		String strToId = DomainConstants.EMPTY_STRING;
		Map mpAttributes = null;
		DomainObject doFrom = null;
		DomainObject doTo = null;
		DomainRelationship doRel = null;
		try{
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : connectEBOMnAlternateConnections --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : connectEBOMnAlternateConnections --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			if(mlNewCons.size() > 0){
        	   for(int i = 0; i < mlNewCons.size(); i++){
        		  mpNewCon = (Map) mlNewCons.get(i);
        		  strRelationshipName = (String) mpNewCon.get("Relationship");
        		  strFromId = (String) mpNewCon.get("FromID");
        		  strToId = (String) mpNewCon.get("ToID");
        		  mpAttributes = (Map) mpNewCon.get("Attributes");
        		  if(UIUtil.isNotNullAndNotEmpty(strRelationshipName)){
        			  if(UIUtil.isNotNullAndNotEmpty(strFromId) && UIUtil.isNotNullAndNotEmpty(strToId)){
        				  doFrom = DomainObject.newInstance(context, strFromId);
        				  doTo = DomainObject.newInstance(context, strToId);
        				  /*if(null != writeIntoFile){
        						writeDataToFile("PWCCMAppIntegration : connectEBOMnAlternateConnections : FROM ID -->"+strFromId+"  \n", writeIntoFile);
        						writeDataToFile("PWCCMAppIntegration : connectEBOMnAlternateConnections : TO ID -->"+strToId+"  \n", writeIntoFile);
        						writeDataToFile("PWCCMAppIntegration : connectEBOMnAlternateConnections : RELATIONSHIP -->"+strRelationshipName+"  \n", writeIntoFile);
        					}*/
	        			  if(DomainConstants.RELATIONSHIP_EBOM.equals(strRelationshipName)){
	        				  doRel = DomainRelationship.connect(context, doFrom, DomainConstants.RELATIONSHIP_EBOM, doTo);
	        				  doRel.setAttributeValues(context, mpAttributes);
	        			  }else if(DomainConstants.RELATIONSHIP_ALTERNATE.equals(strRelationshipName)){
	        				  doRel = DomainRelationship.connect(context, doFrom, DomainConstants.RELATIONSHIP_ALTERNATE, doTo);
	        				  doRel.setAttributeValues(context, mpAttributes);
	        			  }
        			  }
        		  }
        	   }
			}
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : connectEBOMnAlternateConnections --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : connectEBOMnAlternateConnections --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : connectEBOMnAlternateConnections :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
	}
	
	/** This method is to notify update status of processes to CO owner.
	* @param context
	* @param args
	* @throws Exception if the operation fails
	*/
	public void SendNotification(Context context, String[] args) throws Exception{
		
		String strChangeName = args[0];	
		String strChangeOwner = args[1];	
		String strChangeId = args[2];	
		String strProcessName = args[3];
		String strNotificationStatus = args[4];
		String strException =  args[5];
		Map mpChange = new HashMap();
		 
		if(UIUtil.isNotNullAndNotEmpty(strChangeName) && UIUtil.isNotNullAndNotEmpty(strChangeOwner) && UIUtil.isNotNullAndNotEmpty(strChangeId))
		{
			mpChange.put(DomainConstants.SELECT_NAME, strChangeName);
			mpChange.put(DomainConstants.SELECT_OWNER, strChangeOwner);
			mpChange.put(DomainConstants.SELECT_ID, strChangeId);
		}
		 
		notifyCOCoOrdinator (context, mpChange, strProcessName, strNotificationStatus, strException);
		
	}
	
	/**
	* This method is to notify update status of processes to CO owner.
	* @param context
	* @param args
	* @throws Exception if the operation fails
	*/
	public void notifyCOCoOrdinator(Context context, Map mpChange, String strProcessName, String strNotificationStatus, String strException) throws Exception{
		try{
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : notifyCOCoOrdinator --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : notifyCOCoOrdinator --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			StringBuilder sbMailSubject = new StringBuilder();
			StringBuilder sbMailContent = new StringBuilder();
			StringList slCCList = new StringList();
			StringList slToList = new StringList();
			String mailTrigger = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.UpdateStatus");
			String successProcessing = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.SuccessProcessingMessage");
			String errorSubject = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ErrorProcessingSubjectMessage");
			String errorcontent = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ErrorProcessingContentMessage");
			String strChangeOwner = DomainConstants.EMPTY_STRING;
			String strChangeName = DomainConstants.EMPTY_STRING;
			String strChangeId = DomainConstants.EMPTY_STRING;
			if(STATUS_PASS.equalsIgnoreCase(strNotificationStatus)){
				if(UIUtil.isNotNullAndNotEmpty(mailTrigger) && "ON".equalsIgnoreCase(mailTrigger))
				{
					if(null !=mpChange && mpChange.size()>0)
					{
						strChangeOwner = (String) mpChange.get(DomainConstants.SELECT_OWNER);
						strChangeName = (String) mpChange.get(DomainConstants.SELECT_NAME);
						strChangeId = (String) mpChange.get(DomainConstants.SELECT_ID);
						slToList.add(strChangeOwner);
					}
				}
				sbMailSubject.append(successProcessing + " ");
				sbMailContent.append(successProcessing + " ");
			}
			else if(STATUS_FAIL.equalsIgnoreCase(strNotificationStatus))
			{
				if(UIUtil.isNotNullAndNotEmpty(mailTrigger) && "ON".equalsIgnoreCase(mailTrigger))
				{
					if(null !=mpChange && mpChange.size()>0)
					{
						strChangeOwner = (String) mpChange.get(DomainConstants.SELECT_OWNER);
						strChangeName = (String) mpChange.get(DomainConstants.SELECT_NAME);
						strChangeId = (String) mpChange.get(DomainConstants.SELECT_ID);
						slToList.add(strChangeOwner);
					}
				}
				String adminBadgeIds = EnoviaResourceBundle.getProperty(context, "pwcCMAPP.ExceptionNotification.AdminUsers");
				StringList slAdminData = FrameworkUtil.split(adminBadgeIds, ",");
				slToList.addAll(slAdminData);
				sbMailSubject.append(errorSubject + " ");
				sbMailContent.append(errorcontent + " ");
			}
			else if(STATUS_CANNOT_PROCESS.equalsIgnoreCase(strNotificationStatus))
			{
				if(UIUtil.isNotNullAndNotEmpty(mailTrigger) && "ON".equalsIgnoreCase(mailTrigger))
				{
					if(null !=mpChange && mpChange.size()>0)
					{
						strChangeOwner = (String) mpChange.get(DomainConstants.SELECT_OWNER);
						strChangeName = (String) mpChange.get(DomainConstants.SELECT_NAME);
						strChangeId = (String) mpChange.get(DomainConstants.SELECT_ID);
						slToList.add(strChangeOwner);
					}
					sbMailSubject.append(strException);
					sbMailContent.append(strException);
				}
			}
			
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : notifyCOCoOrdinator --> Change Owner --> "+strChangeOwner + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : notifyCOCoOrdinator --> Change Name  -->  " + strChangeName + "\n", writeIntoFile);
			}*/
			if(JSON_DESIGN_LEVEL.equalsIgnoreCase(strProcessName)){
				if(UIUtil.isNotNullAndNotEmpty(strChangeName))
				{
					sbMailSubject.append(STR_DESIGN_LEVEL).append(" for ").append(strChangeName);
					sbMailContent.append(STR_DESIGN_LEVEL).append(" for ").append(strChangeName);
				}
				else
				{
					sbMailSubject.append(STR_DESIGN_LEVEL);
					sbMailContent.append(STR_DESIGN_LEVEL);
				}
			}
			else if(JSON_PMA.equalsIgnoreCase(strProcessName)){
				if(UIUtil.isNotNullAndNotEmpty(strChangeName))
				{
					sbMailSubject.append(STR_PMA).append(" for ").append(strChangeName);
					sbMailContent.append(STR_PMA).append(" for ").append(strChangeName);
				}
				else
				{
					sbMailSubject.append(STR_PMA);
					sbMailContent.append(STR_PMA);
				}
			}
			else if(JSON_LEGACY_PR.equalsIgnoreCase(strProcessName)){
				if(UIUtil.isNotNullAndNotEmpty(strChangeName))
				{
					sbMailSubject.append(STR_LEGACY_PR).append(" for ").append(strChangeName);
					sbMailContent.append(STR_LEGACY_PR).append(" for ").append(strChangeName);
				}
				else
				{
					sbMailSubject.append(STR_LEGACY_PR);
					sbMailContent.append(STR_LEGACY_PR);
				}
			}
			else if((JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_INCORPORATION).equalsIgnoreCase(strProcessName)){
				if(UIUtil.isNotNullAndNotEmpty(strChangeName))
				{
					sbMailSubject.append(STR_EC_META_DATA_INCORPORATION).append(" for ").append(strChangeName);
					sbMailContent.append(STR_EC_META_DATA_INCORPORATION).append(" for ").append(strChangeName);
				}
				else
				{
					sbMailSubject.append(STR_EC_META_DATA_INCORPORATION);
					sbMailContent.append(STR_EC_META_DATA_INCORPORATION);
				}
			}
			else if((JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_ADD_N_CANCEL).equalsIgnoreCase(strProcessName)){
				if(UIUtil.isNotNullAndNotEmpty(strChangeName))
				{
					sbMailSubject.append(STR_EC_META_DATA_ADD_N_CANCEL).append( " for ").append(strChangeName);
					sbMailContent.append(STR_EC_META_DATA_ADD_N_CANCEL).append(" for ").append(strChangeName);
				}
				else
				{
					sbMailSubject.append(STR_EC_META_DATA_ADD_N_CANCEL);
					sbMailContent.append(STR_EC_META_DATA_ADD_N_CANCEL);
				}
			}
			else if(JSON_CA_INWORK_META_DATA.equalsIgnoreCase(strProcessName)){
				if(UIUtil.isNotNullAndNotEmpty(strChangeName))
				{
				sbMailSubject.append(STR_CA_IN_WORK_META_DATA).append( " for ").append(strChangeName);
				sbMailContent.append(STR_CA_IN_WORK_META_DATA).append(" for ").append(strChangeName);
				}
				else
				{
					sbMailSubject.append(STR_CA_IN_WORK_META_DATA);
					sbMailContent.append(STR_CA_IN_WORK_META_DATA);
				}
			}
			else if(JSON_MAKE_FROM.equalsIgnoreCase(strProcessName)){
				if(UIUtil.isNotNullAndNotEmpty(strChangeName))
				{
				sbMailSubject.append(STR_PART_MAKE_FROM).append(" for ").append(strChangeName);
				sbMailContent.append(STR_PART_MAKE_FROM).append(" for ").append(strChangeName);
				}
				else
				{
					sbMailSubject.append(STR_PART_MAKE_FROM);
					sbMailContent.append(STR_PART_MAKE_FROM);
				}
			}
			else if(JSON_PC_MODEL.equalsIgnoreCase(strProcessName)){
				if(UIUtil.isNotNullAndNotEmpty(strChangeName))
				{
				sbMailSubject.append(STR_PRODUCT_CONFIGURATION_MODEL).append(" for ").append(strChangeName);
				sbMailContent.append(STR_PRODUCT_CONFIGURATION_MODEL).append(" for ").append(strChangeName);
				}
				else
				{
					sbMailSubject.append(STR_PRODUCT_CONFIGURATION_MODEL);
					sbMailContent.append(STR_PRODUCT_CONFIGURATION_MODEL);
				}
			}
			else if(JSON_EC_META_DATA.equalsIgnoreCase(strProcessName)){
				if(UIUtil.isNotNullAndNotEmpty(strChangeName))
				{
				sbMailSubject.append(STR_EC_META_DATA).append(" for ").append(strChangeName);
				sbMailContent.append(STR_EC_META_DATA).append(" for ").append(strChangeName);
				}
				else
				{
					sbMailSubject.append(STR_EC_META_DATA);
					sbMailContent.append(STR_EC_META_DATA);
				}
			}else if(JSON_PART_INFO.equalsIgnoreCase(strProcessName)){
				if(UIUtil.isNotNullAndNotEmpty(strChangeName))
				{
				sbMailSubject.append(STR_DT_CODE_ATTRIBUTES).append(" for ").append(strChangeName);
				sbMailContent.append(STR_DT_CODE_ATTRIBUTES).append(" for ").append(strChangeName);
				}
				else
				{
					sbMailSubject.append(STR_DT_CODE_ATTRIBUTES);
					sbMailContent.append(STR_DT_CODE_ATTRIBUTES);
				}
			}
			if(STATUS_FAIL.equalsIgnoreCase(strNotificationStatus))
			{
				String ExceptionMessage = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.ExceptionMessage");
				String logCheckMessage = EnoviaResourceBundle.getProperty(context, "PWCIntegration.CMApp.MailNotification.LogCheckMessage");
				sbMailContent.append(" " + ExceptionMessage);
				sbMailContent.append(strException);
				sbMailContent.append(logCheckMessage);
			}
			
			// Sending status mail
			if(!slToList.isEmpty()){
				/*if(UIUtil.isNotNullAndNotEmpty(strChangeId)){
					MailUtil.sendMessage(context, slToList, slCCList, null, sbMailSubject.toString(), sbMailContent.toString(), new StringList(strChangeId));
				}else{
					MailUtil.sendMessage(context, slToList, slCCList, null, sbMailSubject.toString(), sbMailContent.toString(), null);
				}*/
				
				String strToList = DomainConstants.EMPTY_STRING;
				for(int i = 0; i < slToList.size(); i++){
					if(i == 0){
						strToList = (String) slToList.get(i);
					}else{						
						strToList = strToList + "|" + (String) slToList.get(i);
					}
				}
				// Added_For_Heat-C-16867 - START
				Context clonedContext = context.getFrameContext(context.getSession().toString());
				// Added_For_Heat-C-16867 - END
				BackgroundProcess backgroundProcess = new BackgroundProcess();
				if(UIUtil.isNotNullAndNotEmpty(strChangeId)){
					String[] args = {strToList, sbMailSubject.toString(), sbMailContent.toString(), strChangeId};
					// Modified_For_Heat-C-16867 - START
					backgroundProcess.submitJob(clonedContext, "PWCCMAppIntegration", "notifyAsBackgroundProcess",  args , (String) null);
					// Modified_For_Heat-C-16867 - END
				}else{
					String[] args = {strToList, sbMailSubject.toString(), sbMailContent.toString(), null};		
					// Modified_For_Heat-C-16867 - START
					backgroundProcess.submitJob(clonedContext, "PWCCMAppIntegration", "notifyAsBackgroundProcess",  args , (String) null);
					// Modified_For_Heat-C-16867 - END
				}
			}
			
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : notifyCOCoOrdinator --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : notifyCOCoOrdinator --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : notifyCOCoOrdinator :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
	}
	
	/**
	* This method is to notify as background process.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void notifyAsBackgroundProcess(Context context, String[] args) throws Exception{
		StringList slToList = new StringList();
		try{
			/*if (null != writeIntoFile)
			{
				writeDataToFile("PWCCMAppIntegration : notifyAsBackgroundProcess --> Start \n", writeIntoFile);
				//writeDataToFile("Execution Start Time of PWCCMAppIntegration : notifyAsBackgroundProcess --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
			}*/
			if(null != args[0]){
				String strToList = args[0];
				if(strToList.contains("|")){
					slToList = FrameworkUtil.split(strToList, "|");
				}else{
					slToList.addElement(strToList);
				}
			}
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : notifyAsBackgroundProcess --> To List --> "+slToList + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : notifyAsBackgroundProcess --> Change Object Id --> "+args[3] + "\n", writeIntoFile);
			}*/
			if(null != args[3]){
				MailUtil.sendMessage(context, slToList, null, null, args[1], args[2], new StringList(args[3]));
			}else{
				MailUtil.sendMessage(context, slToList, null, null, args[1], args[2], null);
			}
			/*if(null != writeIntoFile){
				//writeDataToFile("Execution End Time of PWCCMAppIntegration : notifyAsBackgroundProcess --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : notifyAsBackgroundProcess --> Exit \n", writeIntoFile);
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
			/*if(null != writeIntoFile){
				writeDataToFile("Exception in PWCCMAppIntegration : notifyAsBackgroundProcess :-"+ex.getMessage(), writeIntoFile);
			}*/
		}
	}
	
	/**
	* This method is to update PMA, when pushed from CM App.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void updatePMA(Context context, String[] args) throws Exception{
		JSONObject jsonObjPMA =null;
		JSONObject jsonObjPMAlInt = null;
		JSONObject joItemsData = null;
		JSONObject joProcessData = null;
		JSONArray jaColumnIdentifiers = new JSONArray();
		JSONArray jaTableData = new JSONArray();
		boolean isContextPushed = false;
		try{	
			String strPushedData = args[0];
			//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			writeIntoFile = setLoggerPath(CMAPP_PMA_DETAILS_LOG_FILE_NAME);
			//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			// Setting context to CM App process user
			String strCMAppUserPushed = setCMAppProcessUserContext(context);
			
			if ("False".equalsIgnoreCase(strCMAppUserPushed) && !"User Agent".equals(context.getUser()))
			{
				ContextUtil.pushContext(context, PWCConstants.SUPER_USER, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				isContextPushed = true;	
			}
			
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : updatePMA --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : updatePMA --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			try{
				if(null != strPushedData){
					jsonObjPMA = new JSONObject(strPushedData);
					if(null != jsonObjPMA){
						joProcessData = jsonObjPMA.getJSONObject(JSON_PROCESS_DATA);
						if(null != joProcessData){
							joItemsData = joProcessData.getJSONObject(JSON_ITEMS_DATA);
							if(null != joItemsData){
								jsonObjPMAlInt = joItemsData.getJSONObject(JSON_PMA);
								/*if(null != writeIntoFile){
									writeDataToFile("PWCCMAppIntegration : updatePMA -->  Process Name--> PMA  \n", writeIntoFile);				
								}*/
							}else{
								if(null != writeIntoFile){
									//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
									_LOGGER.debug("PWCCMAppIntegration : updatePMA -->  NO DATA PROVIDED");						
									//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
								}
								notifyCOCoOrdinator(context, null, JSON_PMA, STATUS_FAIL,  MESSAGE_NO_DATA_PROVIDED);
							}
						}
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
				if(null != writeIntoFile){
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					_LOGGER.debug("Exception in PWCCMAppIntegration : updatePMA (Content Not In JSON Format) :-"+ex.getMessage());
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				}
				notifyCOCoOrdinator(context, null, JSON_PMA, STATUS_FAIL, ex.getMessage());
			}
			
			
			Map mpChange = null;
			String strChangeId = DomainConstants.EMPTY_STRING;
			String strChangeName = DomainConstants.EMPTY_STRING;
			String strMsg = DomainConstants.EMPTY_STRING;
			if(null != jsonObjPMAlInt){
				mpChange = checkChangeObjectState(context, jsonObjPMAlInt, JSON_PMA);
				/*if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : updatePMA --> Change Object --> "+mpChange + "\n", writeIntoFile);
				}*/
				if(null != mpChange){
					strMsg = (String) mpChange.get(strMessage);
					strChangeId = (String) mpChange.get(DomainObject.SELECT_ID);
					strChangeName = (String) mpChange.get(DomainObject.SELECT_NAME);
				}
				
				if(UIUtil.isNotNullAndNotEmpty(strChangeId)){
					if(PROCESS.equals(strMsg)){
						int iCONameIndex = 0;
						int iFromPartIndex = 0;
						int iToHPIndex = 0;
						int iApplicabilityIndex = 0;
						if(jsonObjPMAlInt != null){
							// Identifying the indexes
							jaColumnIdentifiers = jsonObjPMAlInt.getJSONArray(JSON_COLUMN_IDENTIFIERS);
							if(jaColumnIdentifiers.length() > 0){
				            	for(int i = 0; i < jaColumnIdentifiers.length(); i++){
				            		String strColIdentifier = jaColumnIdentifiers.getString(i);
				            		if("DOCUMENT_NO".equalsIgnoreCase(strColIdentifier)){
				            			iCONameIndex = i;
				            		}else if("ITEM".equalsIgnoreCase(strColIdentifier)){
				            			iFromPartIndex = i;
				            		}else if("MODEL_NAME".equalsIgnoreCase(strColIdentifier)){
				            			iToHPIndex = i;
				            		}else if("MODEL_APPLICABILITY".equalsIgnoreCase(strColIdentifier)){
				            			iApplicabilityIndex = i;
				            		}
				            	}
				            }
							
							// Table Data
							jaTableData = jsonObjPMAlInt.getJSONArray(JSON_TABLE_DATA);
							
							// Internal method - To process PMA
			            	updatePMAInternal(context, mpChange, jaTableData, iCONameIndex, iFromPartIndex, iToHPIndex, iApplicabilityIndex);
			            	
						}
					}else{
						//notifyCOCoOrdinator(context, mpChange, DomainConstants.EMPTY_STRING, STATUS_CANNOT_PROCESS, strMsg);
					}
				}else{
					if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						_LOGGER.debug("PWCCMAppIntegration : updatePMA --> Change Object '"+strChangeName+"' doesn't exists in EV6.");
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					}
					notifyCOCoOrdinator(context, null, JSON_PMA, STATUS_FAIL, "Change Order '"+strChangeName+"' doesn't exists in EV6.");
				}
			}
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : updatePMA --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updatePMA --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation				
				_LOGGER.debug("Exception in PWCCMAppIntegration : updatePMA :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}finally{
			if(isContextPushed){
				ContextUtil.popContext(context);	
			}
		}	
	}
	
	/**
	* This method is called internally, to update PMA.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void updatePMAInternal(Context context, Map mpCO, JSONArray jaTableData, int iCONameIndex, int iFromPartIndex, int iToHPIndex, int iApplicabilityIndex) throws Exception{
		String strFromPartName 					= DomainConstants.EMPTY_STRING;
		DomainObject doHP 						= null;
		MapList mlFROMPart 						= new MapList();
		Map mpHP 								= null;
		String strHPId 							= DomainConstants.EMPTY_STRING;
		String strToHPName 						= DomainConstants.EMPTY_STRING;
		MapList mlHP 							= new MapList();
		String strAttrPWCApplicability 			= DomainConstants.EMPTY_STRING;
		String strRelId 						= DomainConstants.EMPTY_STRING;
		DomainRelationship doPWCModelAppRel 	= null;
		JSONArray jaTableDataInd 				= null;
		Map mpAttributes 						= new HashMap();
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		StringList slPartsToCheckAndDeletePMAsOrder 	= new StringList();
		String strToHPFromPartID = DomainConstants.EMPTY_STRING;
		String strAttrPWCLeadEngineModel = DomainConstants.EMPTY_STRING;
		//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		StringList objectSelectsList 			= new StringList();
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		objectSelectsList.add(DomainConstants.SELECT_ID);
		objectSelectsList.addElement("from["+REL_PWC_MODEL_APPLICABILITY+"].to.id");
		objectSelectsList.addElement("from["+REL_PWC_MODEL_APPLICABILITY+"].id");
		objectSelectsList.addElement("from["+REL_PWC_MODEL_APPLICABILITY+"].attribute["+ATTR_PWC_LEAD_ENGINE_MODEL+"]");
		//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		StringList slMissingPartList = new StringList();
		StringList slMissingHPList = new StringList();
		//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		
		Map mpRevision 							= null;
		String strRevId 						= DomainConstants.EMPTY_STRING;
		boolean bNotify 						= true;
		
		try
		{
			if (null != writeIntoFile)
			{
				//writeDataToFile("PWCCMAppIntegration : updatePMAInternal --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : updatePMAInternal --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updatePMAInternal --> Table Data Length  " + jaTableData.length() + "\n", writeIntoFile);
			}
			if(jaTableData.length() > 0)
			{
            	try
            	{
	            	// Starting transaction
					ContextUtil.startTransaction(context,true);
					
	 	           	for(int i = 0; i < jaTableData.length(); i++){
	 	           		jaTableDataInd = jaTableData.getJSONArray(i);
	 	           		strFromPartName = jaTableDataInd.getString(iFromPartIndex).trim();
	 	           		if("null" == strFromPartName){
	 	           			strFromPartName = DomainConstants.EMPTY_STRING;
	 	           		}
		 	           	/*if(null != writeIntoFile){
		 	           		writeDataToFile("PWCCMAppIntegration : updatePMAInternal --> From Part Name  " + strFromPartName + "\n", writeIntoFile);
		 				}*/
 	           			// Getting the FROM part
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
           				mlFROMPart = getPartListIncludingLatestRevision(context, strFromPartName);//Maplist with all the revisions
           				
           				StringList slTobeDeleted = new StringList();
           				StringList slConnectionIdList = new StringList();
           				
 	           			if(mlFROMPart != null && !mlFROMPart.isEmpty()){
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
 	           				
 	           				strToHPName = jaTableDataInd.getString(iToHPIndex).trim();
	 	           			if("null" == strToHPName){
	 	           				strToHPName = DomainConstants.EMPTY_STRING;
		 	           		}
	 	           			/*if(null != writeIntoFile){
		 		 				writeDataToFile("PWCCMAppIntegration : updatePMAInternal --> From Part ID  " + strFROMPartId + "\n", writeIntoFile);
		 		 				writeDataToFile("PWCCMAppIntegration : updatePMAInternal --> To HP Name  " + strToHPName + "\n", writeIntoFile);
	 	           			}*/
	           				// Getting the hardware product
	           				mlHP = getLatestRevisionHP(context, strToHPName);
	           				
	           				if (mlHP.size() > 0)
	           				{
	           					mpHP = (Map) mlHP.get(0);
	           					strHPId  = (String) mpHP.get(DomainObject.SELECT_ID);
	 	           				doHP = DomainObject.newInstance(context, strHPId);
		 	           			
	 	           				/*if (null != writeIntoFile)
		 	           			{
		 	           				writeDataToFile("PWCCMAppIntegration : updatePMAInternal --> HP Id  " + strHPId + "\n", writeIntoFile);
		  	           			}*/
		 	           			strAttrPWCApplicability = jaTableDataInd.getString(iApplicabilityIndex).trim();
		 	           			
		 	           			if ("null" == strAttrPWCApplicability || DomainConstants.EMPTY_STRING.equals(strAttrPWCApplicability))
		 	           			{
		 	           				strAttrPWCApplicability = DomainConstants.EMPTY_STRING;
		 	           			}
		 	           			else
		 	           			{
		 	           				strAttrPWCApplicability = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.PMA.Applicability."+strAttrPWCApplicability.trim().replace(" ", "_"));
		 	           		    }	 	           			
			 	           		
		 	           			if (null != writeIntoFile)
		 	           			{
									//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		  		 					_LOGGER.debug("PWCCMAppIntegration : updatePMAInternal --> strAttrPWCApplicability ->  " + strAttrPWCApplicability);
									//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		  	           			}
			 	           		
			 	           		if (UIUtil.isNotNullAndNotEmpty(strAttrPWCApplicability))
			 	           		{
			 	           			mpAttributes.put(ATTR_PWC_APPLICABILITY, strAttrPWCApplicability);
			 	           		}
			 	           		mpAttributes.put(ATTR_PWC_LEAD_ENGINE_MODEL, "No");
	 	           				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
			 	           		String[] strAllRevId = new String[mlFROMPart.size()];
			 	           		StringList slHPIDList = new StringList();
			 	           		StringList slHPPartsIDList = new StringList();
			 	           		StringList slHPRelList = new StringList();
			 	           		StringList slHPAttrPMAList = new StringList();
	 	           				// Replicating connection to all revisions of part
	 	              			for(int j = 0; j < mlFROMPart.size(); j++){
	 	              				mpRevision = (Map) mlFROMPart.get(j);
	 	              				strRevId = (String) mpRevision.get(DomainConstants.SELECT_ID);
	 	              				
	 	              				strAllRevId[j]=strRevId;
	 	              			}
	 	              			BusinessObjectWithSelectList busClassList = BusinessObject.getSelectBusinessObjectData(context, strAllRevId, objectSelectsList);
	 	              			BusinessObjectWithSelectItr busWithSelectItr = new BusinessObjectWithSelectItr(busClassList);
	 	              			String[] arrBuildIds = new String[busClassList.size()];
	 	              			Map mpRelDetails = new HashMap();
           						if(busClassList != null && busClassList.size()>0){
           							while (busWithSelectItr.next()){
    		 	       					BusinessObjectWithSelect busWithSelect = busWithSelectItr.obj();
    		 	       					slHPPartsIDList = (StringList) busWithSelect.getSelectDataList(DomainConstants.SELECT_ID);
    		 	       					strRevId = (String)slHPPartsIDList.get(0);
    		 	       					slHPIDList = (StringList) busWithSelect.getSelectDataList("from["+REL_PWC_MODEL_APPLICABILITY+"].to.id");
    		 	       					slHPRelList = (StringList) busWithSelect.getSelectDataList("from["+REL_PWC_MODEL_APPLICABILITY+"].id");
    		 	       					slHPAttrPMAList = (StringList) busWithSelect.getSelectDataList("from["+REL_PWC_MODEL_APPLICABILITY+"].attribute["+ATTR_PWC_LEAD_ENGINE_MODEL+"]");
    		 	       					if(slHPAttrPMAList != null && !slHPAttrPMAList.isEmpty()){
    		 	       						for(int cnt=0; cnt<slHPAttrPMAList.size(); cnt++){
    		 	       							strAttrPWCLeadEngineModel = (String)slHPAttrPMAList.get(cnt);
    		 	       							strToHPFromPartID = (String)slHPIDList.get(cnt);
    		 	       							strRelId = (String)slHPRelList.get(cnt);
    		 	       							if(strAttrPWCLeadEngineModel != null && !strAttrPWCLeadEngineModel.equals("Yes") && !strToHPFromPartID.equals(strHPId)){
    		 	       								if(!slTobeDeleted.contains(strRelId)){
    		 	       									slTobeDeleted.addElement(strRelId);
    		 	       								}
    		 	       							}
    		 	       						}
    		 	       					}
	           							if(slHPIDList != null && slHPIDList.size()>0 && slHPIDList.contains(strHPId)){
	              							for(int counter=0; counter<slHPIDList.size(); counter++){
	              								if(strHPId.equals(slHPIDList.get(counter))){
	              									strRelId = (String)slHPRelList.get(counter);
	              								}
	              							}
	              							if (UIUtil.isNotNullAndNotEmpty(strAttrPWCApplicability))
			 	           					{
	              								doPWCModelAppRel = DomainRelationship.newInstance(context, strRelId);
		 	              						doPWCModelAppRel.setAttributeValue(context, ATTR_PWC_APPLICABILITY, strAttrPWCApplicability);
			 	           					}
	              						}else{
	              							slConnectionIdList.addElement(strRevId);
	              							}
	    		 	       			    }
           							
	           						}
           						
           						if(slConnectionIdList != null && slConnectionIdList.size()>0){
           							for(int ctr=0; ctr<slConnectionIdList.size(); ctr++){
           								arrBuildIds[ctr] = (String) slConnectionIdList.get(ctr);
           							}
           							mpRelDetails = DomainRelationship.connect(context, doHP, REL_PWC_MODEL_APPLICABILITY, false, arrBuildIds);
               						if(mpRelDetails != null && arrBuildIds != null && arrBuildIds.length>0){
               							for(int cntr=0; cntr<arrBuildIds.length; cntr++){
               								strRelId = (String) mpRelDetails.get(arrBuildIds[cntr]);
               								if(UIUtil.isNotNullAndNotEmpty(strRelId)){
               									doPWCModelAppRel = DomainRelationship.newInstance(context, strRelId);
               									doPWCModelAppRel.setAttributeValues(context, mpAttributes);
               								}
               							}
               						}
           						}
           						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	           				}
	           				else
	           				{
	           					bNotify = false;
	           					if (null != writeIntoFile)
	           					{
									//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	        						_LOGGER.debug("PWCCMAppIntegration : updatePMAInternal --> No such hardware product in EV6 ");
									//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	        					}
								//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	           					slMissingHPList.addElement(strToHPName);
	           					//notifyCOCoOrdinator(context, mpCO, JSON_PMA, STATUS_FAIL, MESSAGE_NO_SUCH_HP);
								//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	           				}
 	           			}
 	           			else
 	           			{
 	           				bNotify = false;
	 	           			if (null != writeIntoFile)
	 	           			{
								//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	 							_LOGGER.debug("PWCCMAppIntegration : updatePMAInternal --> No such part in EV6");
								//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	 						}
							//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	 	           			slMissingPartList.addElement(strFromPartName);
 	           				//notifyCOCoOrdinator(context, mpCO, JSON_PMA, STATUS_FAIL, MESSAGE_NO_SUCH_PART);
							//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
 	           			}
						//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
 	           			slPartsToCheckAndDeletePMAsOrder.addAll(slTobeDeleted);
						//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	 	           	}
	 	           	
	 	           	// Disconnecting additional EV6 PMAs & Replicating the same to all revisions of part
					//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	 	           if(slPartsToCheckAndDeletePMAsOrder.size() > 0){
	        		   String[] strRelsToBeDeleted = new String[slPartsToCheckAndDeletePMAsOrder.size()];
	        		   for(int i = 0; i < slPartsToCheckAndDeletePMAsOrder.size(); i++){
	        			   strRelsToBeDeleted[i] = (String) slPartsToCheckAndDeletePMAsOrder.get(i);
	        		   }
	        		   DomainRelationship.disconnect(context, strRelsToBeDeleted);
	        	   }
				   //END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	 	           	
	 	           	// Committing transaction
	 	           	ContextUtil.commitTransaction(context);
					//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		 	       	if(null != slMissingHPList && slMissingHPList.size()>0 && null != slMissingPartList && slMissingPartList.size()==0){
		 	       		StringBuffer sbMessageForHP = new StringBuffer();
		 	       		sbMessageForHP.append(MESSAGE_NO_SUCH_HPS);
		 	       		sbMessageForHP.append("\n");
		 	       		sbMessageForHP.append(slMissingHPList.toString());
		 	       		notifyCOCoOrdinator(context, mpCO, JSON_PMA, STATUS_FAIL, sbMessageForHP.toString());
	 	           	}else if(null != slMissingPartList && slMissingPartList.size()>0 && null != slMissingHPList && slMissingHPList.size()==0){
		 	       		StringBuffer sbMessageForPart = new StringBuffer();
		 	       		sbMessageForPart.append(MESSAGE_NO_SUCH_PARTS);
		 	       		sbMessageForPart.append("\n");
		 	       		sbMessageForPart.append(slMissingPartList.toString());
		 	       		notifyCOCoOrdinator(context, mpCO, JSON_PMA, STATUS_FAIL, sbMessageForPart.toString());
	 	           	}else if(null != slMissingHPList && slMissingHPList.size()>0 && null != slMissingPartList && slMissingPartList.size()>0){
		 	           	StringBuffer sbMessageForHPAndPart = new StringBuffer();
		 	           	sbMessageForHPAndPart.append(MESSAGE_NO_SUCH_HPS);
		 	           	sbMessageForHPAndPart.append("\n");
		 	           	sbMessageForHPAndPart.append(slMissingHPList.toString());
		 	           	sbMessageForHPAndPart.append("\n");
		 	           	sbMessageForHPAndPart.append(MESSAGE_NO_SUCH_PARTS);
		 	           	sbMessageForHPAndPart.append("\n");
		 	           	sbMessageForHPAndPart.append(slMissingPartList.toString());
		 	       		notifyCOCoOrdinator(context, mpCO, JSON_PMA, STATUS_FAIL, sbMessageForHPAndPart.toString());
	 	           	}
		 	       	//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	 	           	// Notifying the update status 'Pass' to CO coordinator
	 	           	if(bNotify){
	 	           		notifyCOCoOrdinator(context, mpCO, JSON_PMA, STATUS_PASS, null);
	 	           	}
	 	         
            	}catch(Exception ex){
            		ex.printStackTrace();
            		
            		// Notifying the update status 'Fail' to CO coordinator
        			notifyCOCoOrdinator(context, mpCO, JSON_PMA, STATUS_FAIL, ex.getMessage());
            		
            		// Aborting transaction
        			ContextUtil.abortTransaction(context);
            	}
            	
             }else{
            	 if(null != writeIntoFile){
				 		//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	   	 				_LOGGER.debug("PWCCMAppIntegration : updatePMAInternal --> No connections provided ");
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	 				}       	 
            	notifyCOCoOrdinator(context, mpCO, JSON_PMA, STATUS_FAIL, MESSAGE_NO_DATA_PROVIDED);
             }
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : updatePMAInternal --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updatePMAInternal --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : updatePMAInternal :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}	
	}
	
	/**
	* This method is to get the latest revision hardware product.
	* @param context
	* @param args
	* @returns MapList
	* @throws Exception if the operation fails
	*/
	public MapList getLatestRevisionHP(Context context, String strHPName) throws Exception{
		MapList mlReturn = new MapList();
		try{
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionHP --> Start \n", writeIntoFile);
				writeDataToFile("Execution Start Time of PWCCMAppIntegration : getLatestRevisionHP --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
			}*/
			StringList objectSelects = new StringList();
         	objectSelects.addElement(DomainObject.SELECT_ID);
         	objectSelects.addElement(DomainObject.SELECT_NAME);
         	String strWhrClause = "revision == last";
         	/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionHP --> Find Objects - Type(s) --> "+TYPE_HARDWARE_PRODUCT + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionHP --> Find Objects - where clause --> "+strWhrClause + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionHP --> Find Objects - HP Name --> "+strHPName + "\n", writeIntoFile);

			}*/
         	if(null != strHPName && !DomainConstants.EMPTY_STRING.equals(strHPName)){
				mlReturn= DomainObject.findObjects(context, 
							TYPE_HARDWARE_PRODUCT, 
		               		strHPName, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		strWhrClause, 
		               		false, 
		               		objectSelects);
         	}
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionHP --> Object List (Maplist) --> "+mlReturn + "\n", writeIntoFile);
				writeDataToFile("Execution End Time of PWCCMAppIntegration : getLatestRevisionHP --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : getLatestRevisionHP --> Exit \n", writeIntoFile);
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : getLatestRevisionHP :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		return mlReturn;
	}
	
	/**
	* This method is to delete PMA connections, which are not in the input from CM App.
	* @param context
	* @param args
	* @returns Map
	* @throws Exception if the operation fails
	*/
	public void deleteAdditionalEV6PMAConnections(Context context, StringList slPartsToCheckAndDeletePMAs, JSONArray jaTableData, int iFromPartIndex, int iToHPIndex) throws Exception{
		StringList slPMAConsToBeDeleted = new StringList();
		boolean bNotToBeDeleted = false;
		String strPartId = DomainConstants.EMPTY_STRING;
 		DomainObject doPart = null;
 		MapList mlRevisions = new MapList();
 		Map mpRevision = null;
		String strRevId = DomainConstants.EMPTY_STRING;
		DomainObject doPartRev = null;
 		String strPartName = DomainConstants.EMPTY_STRING;
 		StringBuilder sbWhereClause = new StringBuilder();
 		MapList mlHWProducts = new MapList();
 		Map mpHWProd = null;
		String strHWProdName = DomainConstants.EMPTY_STRING;
		String strHWProdID = DomainConstants.EMPTY_STRING;
		String strRelId = DomainConstants.EMPTY_STRING;
		String strFromPartName = DomainConstants.EMPTY_STRING;
		String strToHPName = DomainConstants.EMPTY_STRING;
		MapList mlToHWProd = new MapList();
		Map mpHP = null;
		String strHPId  = DomainConstants.EMPTY_STRING;    				
		try{
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : deleteAdditionalEV6PMAConnections --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of  PWCCMAppIntegration : deleteAdditionalEV6PMAConnections --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : deleteAdditionalEV6PMAConnections --> PartsToCheckAndDeletePMAs(StringList)"+slPartsToCheckAndDeletePMAs.size()+" \n", writeIntoFile);
			}
			if(slPartsToCheckAndDeletePMAs.size() > 0){
				StringList objectSelects = new StringList();
	         	objectSelects.addElement(DomainObject.SELECT_ID);
	         	objectSelects.addElement(DomainObject.SELECT_NAME);
	         	StringList relSelects = new StringList();
	         	relSelects.addElement(DomainRelationship.SELECT_ID);
	         	for(int i =0; i < slPartsToCheckAndDeletePMAs.size(); i++){
	         		strPartId = (String) slPartsToCheckAndDeletePMAs.get(i);
	         		doPart = DomainObject.newInstance(context, strPartId);
	         		// Getting all the revisions of the part... To replicate disconnection on all revisions
	         		mlRevisions = doPart.getRevisionsInfo(context, objectSelects, new StringList());
           			for(int j = 0; j < mlRevisions.size(); j++){
           				mpRevision = (Map) mlRevisions.get(j);
           				strRevId = (String) mpRevision.get(DomainConstants.SELECT_ID);
           				doPartRev = DomainObject.newInstance(context, strRevId);
		         		strPartName = doPartRev.getName(context);
		         		sbWhereClause = new StringBuilder();
						sbWhereClause.append(SELECTABLE_ATTR_PWC_LEAD_ENGINE_MODEL).append(" != ").append("\'").append("Yes").append("\'");
						// Getting all the non-lead engine models
			         	mlHWProducts = doPartRev.getRelatedObjects(context, 
								REL_PWC_MODEL_APPLICABILITY, 
								TYPE_HARDWARE_PRODUCT, 
			         			objectSelects, 
			         			relSelects, 
			         			false,
			         			true,
			         			(short) 1,
			         			null,
			         			sbWhereClause.toString(),
			         			0);
			           	/*if(null != writeIntoFile){
							writeDataToFile("PWCCMAppIntegration : deleteAdditionalEV6PMAConnections -->All Non Lead Engine Model (Maplist) -->"+mlHWProducts.size()+" \n", writeIntoFile);
						}*/
						for(int k = 0; k < mlHWProducts.size(); k++){
							bNotToBeDeleted = false;
							mpHWProd = (Map) mlHWProducts.get(k);
							strHWProdName = (String) mpHWProd.get(DomainObject.SELECT_NAME);
							strHWProdID = (String) mpHWProd.get(DomainObject.SELECT_ID);
							strRelId = (String) mpHWProd.get(DomainRelationship.SELECT_ID);
							// Checking if the above details matches with the data provided from downstairs... If matches, then marking the same as not-to-be deleted & accordingly removing from the list.
							for(int l = 0; l < jaTableData.length(); l++){
			 	           		JSONArray jaTableDataInd = jaTableData.getJSONArray(l);
			 	           		strFromPartName = jaTableDataInd.getString(iFromPartIndex);
			 	           		if(strPartName.equals(strFromPartName)){
			 	           			strToHPName = jaTableDataInd.getString(iToHPIndex);
			 	           			if(strHWProdName.equals(strToHPName)){
			 	           				mlToHWProd = getLatestRevisionHP(context, strToHPName);
			 	           				if(!mlToHWProd.isEmpty()){
				 	           				mpHP = (Map) mlToHWProd.get(0);
				           					strHPId  = (String) mpHP.get(DomainObject.SELECT_ID);
				           					/*if(null != writeIntoFile){
				    							writeDataToFile("PWCCMAppIntegration : deleteAdditionalEV6PMAConnections -->HP Id-->"+strHPId+" \n", writeIntoFile);
				    						}*/
				 	           				if(strHWProdID.equals(strHPId)){
				 	           					bNotToBeDeleted = true;
				 	           					break;
				 	           				}
			 	           				}
			 	           			}
			 	           		}
		       				}
		       				if(!bNotToBeDeleted){
		       					if(!slPMAConsToBeDeleted.contains(strRelId)){
		       						slPMAConsToBeDeleted.addElement(strRelId);
		       					}
			 	           	}
						}
           			}
           		}
	         	if(slPMAConsToBeDeleted.size() > 0){
        		   String[] strRelsToBeDeleted = new String[slPMAConsToBeDeleted.size()];
        		   for(int i = 0; i < slPMAConsToBeDeleted.size(); i++){
        			   strRelsToBeDeleted[i] = (String) slPMAConsToBeDeleted.get(i);
        		   }
        		   DomainRelationship.disconnect(context, strRelsToBeDeleted);
        	   }
	        }
		    if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : deleteAdditionalEV6PMAConnections --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : deleteAdditionalEV6PMAConnections --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : deleteAdditionalEV6PMAConnections :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
	}
	
	/**
	* This method is to update LRP, when pushed from CM App.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void updateLRP(Context context, String[] args) throws Exception{
		JSONObject jsonObjLRP =null;
		JSONObject jsonObjLRPInt = null;
		JSONObject joItemsData = null;
		JSONObject joProcessData = null;
		JSONArray jaColumnIdentifiers = new JSONArray();
		JSONArray jaTableData = new JSONArray();
		boolean isContextPushed = false;
		try{
			String strPushedData = args[0];
			//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			writeIntoFile = setLoggerPath(CMAPP_LRP_DETAILS_LOG_FILE_NAME);
			//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			// Setting context to CM App process user
			String strCMAppUserPushed = setCMAppProcessUserContext(context);
			
			if ("False".equalsIgnoreCase(strCMAppUserPushed) && !"User Agent".equals(context.getUser()))
			{
				ContextUtil.pushContext(context, PWCConstants.SUPER_USER, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				isContextPushed = true;	
			}			
			
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : updateLRP --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : updateLRP --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation				
			}
			try{
				if(null != strPushedData){
					jsonObjLRP = new JSONObject(strPushedData);
					if(null != jsonObjLRP){
						joProcessData = jsonObjLRP.getJSONObject(JSON_PROCESS_DATA);
						if(null != joProcessData){
							joItemsData = joProcessData.getJSONObject(JSON_ITEMS_DATA);
							if(null != joItemsData){
								jsonObjLRPInt = joItemsData.getJSONObject(JSON_LEGACY_PR);
								/*if(null != writeIntoFile){
									writeDataToFile("PWCCMAppIntegration : updateLRP -->  Process Name --> LEGACY_PR \n", writeIntoFile);						
								}*/								
							}else{
								if(null != writeIntoFile){
									//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
									_LOGGER.debug("PWCCMAppIntegration : updateLRP -->  NO DATA PROVIDED ");
									//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
								}
								notifyCOCoOrdinator(context, null, JSON_LEGACY_PR, STATUS_FAIL, MESSAGE_NO_DATA_PROVIDED);
							}
						}
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
				if(null != writeIntoFile){
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					_LOGGER.debug("Exception in PWCCMAppIntegration : updateLRP(Content Not In JSON Format) :-"+ex.getMessage());
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				}
				notifyCOCoOrdinator(context, null, JSON_LEGACY_PR, STATUS_FAIL, ex.getMessage());
			}
			
			Map mpChange = null;
			String strChangeId = DomainConstants.EMPTY_STRING;
			String strChangeName = DomainConstants.EMPTY_STRING;
			String strMsg = DomainConstants.EMPTY_STRING;
			if(null != jsonObjLRPInt){
				mpChange = checkChangeObjectState(context, jsonObjLRPInt, JSON_LEGACY_PR);
				/*if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : updateLRP --> Change Object --> "+mpChange + "\n", writeIntoFile);
				}*/
				if(null != mpChange){
					strMsg = (String) mpChange.get(strMessage);
					strChangeId = (String) mpChange.get(DomainObject.SELECT_ID);
					strChangeName = (String) mpChange.get(DomainObject.SELECT_NAME);
				}
				
				if(UIUtil.isNotNullAndNotEmpty(strChangeId)){
					if(PROCESS.equals(strMsg)){
						int iCONameIndex = 0;
						int iFromPartIndex = 0;
						int iToPartIndex = 0;
						int iCreateDataIndex = 0;
						int iDocScopeIndex = 0;
						int iReplacementScopeIndex = 0;
						if(jsonObjLRPInt != null){
							// Identifying the indexes
							jaColumnIdentifiers = jsonObjLRPInt.getJSONArray(JSON_COLUMN_IDENTIFIERS);
							if(jaColumnIdentifiers.length() > 0){
				            	for(int i = 0; i < jaColumnIdentifiers.length(); i++){
				            		String strColIdentifier = jaColumnIdentifiers.getString(i);
				            		if("DOCUMENT_NO".equalsIgnoreCase(strColIdentifier)){
				            			iCONameIndex = i;
				            		}else if("REPLACEMENT_PART_NO".equalsIgnoreCase(strColIdentifier)){
				            			iToPartIndex = i;
				            		}else if("PART_NO".equalsIgnoreCase(strColIdentifier)){
				            			iFromPartIndex = i;
				            		}else if("DOCUMENT_ISSUE_DATE".equalsIgnoreCase(strColIdentifier)){
				            			iCreateDataIndex = i;
				            		}else if("SCOPE".equalsIgnoreCase(strColIdentifier)){
				            			iDocScopeIndex = i;
				            		}else if("MODELS".equalsIgnoreCase(strColIdentifier)){
				            			iReplacementScopeIndex = i;
				            		}
				            	}
				            }
							// Table Data
							jaTableData = jsonObjLRPInt.getJSONArray(JSON_TABLE_DATA);
							// Internal method - To process LRP
				            updateLRPInternal(context, mpChange, jaTableData, iCONameIndex, iFromPartIndex, iToPartIndex, iCreateDataIndex, iDocScopeIndex, iReplacementScopeIndex);
						}
					}else{
						//notifyCOCoOrdinator(context, mpChange, DomainConstants.EMPTY_STRING, STATUS_CANNOT_PROCESS, strMsg);
					}
				}else{
					if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						_LOGGER.debug("PWCCMAppIntegration : updateLRP --> Change Object '"+strChangeName+"' doesn't exists in EV6.");
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					}
					notifyCOCoOrdinator(context, null, JSON_LEGACY_PR, STATUS_FAIL, "Change Order '"+strChangeName+"' doesn't exists in EV6.");
				}
			}
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : updateLRP --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateLRP --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : updateLRP :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}finally{
			if(isContextPushed){
				ContextUtil.popContext(context);	
			}
		}	
	}
	
	/**
	* This method is called internally, to update LRP.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void updateLRPInternal(Context context, Map mpCO, JSONArray jaTableData, int iCONameIndex, int iFromPartIndex, int iToPartIndex, int iCreateDataIndex, int iDocScopeIndex, int iReplacementScopeIndex) throws Exception{
		String strFromPartName = DomainConstants.EMPTY_STRING;
		DomainObject doFROMPart = null;
		DomainObject doTOPart = null;
		MapList mlFROMPart = new MapList();
		Map mpFROMPart = null;
		Map mpTOPart = null;
		String strFROMPartId = DomainConstants.EMPTY_STRING;
		String strToPartName = DomainConstants.EMPTY_STRING;
		String strTOPartId = DomainConstants.EMPTY_STRING;
		MapList mlTOPart = new MapList();
		String strAttrPWCApplicability = DomainConstants.EMPTY_STRING;
		MapList mlPart = new MapList();
		StringList objectSelects = new StringList();
     	objectSelects.addElement(DomainObject.SELECT_ID);
     	StringList relSelects = new StringList();
     	relSelects.addElement(DomainRelationship.SELECT_ID);
		String strWhereClause = DomainConstants.EMPTY_STRING;
		Map mpPart = null;
		String strRelId = DomainConstants.EMPTY_STRING;
		DomainRelationship doLRPRel = null;
		JSONArray jaTableDataInd = null;
		Map mpAttributes = null;
		boolean bNotify = true;
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		StringList slMissingPartList = new StringList();
		//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
		try{
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : updateLRPInternal --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : updateLRPInternal --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateLRPInternal --> Table Data Length  " + jaTableData.length() + "\n", writeIntoFile);
			}
			if(jaTableData.length() > 0){
            	try{
	            	// Starting transaction
					ContextUtil.startTransaction(context,true);
					
	 	           	for(int i = 0; i < jaTableData.length(); i++){
	 	           		jaTableDataInd = jaTableData.getJSONArray(i);
	 	           		strFromPartName = jaTableDataInd.getString(iFromPartIndex).trim();
	 	           		if("null" == strFromPartName){
	 	           			strFromPartName = DomainConstants.EMPTY_STRING;
	 	           		}
	 	           		/*if(null != writeIntoFile){
		 					writeDataToFile("PWCCMAppIntegration : updateLRPInternal --> From Part Name  " + strFromPartName + "\n", writeIntoFile);
		 				}*/
 	           			// Getting the FROM part
           				mlFROMPart = getLatestRevisionPart(context, strFromPartName);
           				
 	           			if(mlFROMPart.size() > 0){
 	           				mpFROMPart = (Map) mlFROMPart.get(0);
 	           				strFROMPartId  = (String) mpFROMPart.get(DomainObject.SELECT_ID);
 	           				doFROMPart = DomainObject.newInstance(context, strFROMPartId);
 	           				
 	           				strToPartName = jaTableDataInd.getString(iToPartIndex).trim();
 	           				if("null" == strToPartName){
 	           					strToPartName = DomainConstants.EMPTY_STRING;
 	           				}
	 	           			/*if(null != writeIntoFile){
	  		 					writeDataToFile("PWCCMAppIntegration : updateLRPInternal --> From Part ID  " + strFROMPartId + "\n", writeIntoFile);
	  		 					writeDataToFile("PWCCMAppIntegration : updateLRPInternal --> To Part Name  " + strToPartName + "\n", writeIntoFile);
	  	           			}*/
	           				// Getting the TO part
	           				mlTOPart = getLatestRevisionPart(context, strToPartName);
	           				
	           				if(mlTOPart.size() > 0){
	           					mpTOPart = (Map) mlTOPart.get(0);
	           					strTOPartId  = (String) mpTOPart.get(DomainObject.SELECT_ID);
	           					doTOPart = DomainObject.newInstance(context, strTOPartId);
	           					/*if(null != writeIntoFile){
		  		 					writeDataToFile("PWCCMAppIntegration : updateLRPInternal --> Part (Map)  " + mpTOPart + "\n", writeIntoFile);
		  	           			}*/
	           					mpAttributes = getLRPAttributesValues(context, jaTableDataInd, iCONameIndex, iCreateDataIndex, iDocScopeIndex, iReplacementScopeIndex);
	           					
	           					// Establishing new LRP connection and updating attributes   
	           					doLRPRel =DomainRelationship.connect(context, doFROMPart, REL_LEGACY_REPLACES_PART, doTOPart);
	           					doLRPRel.setAttributeValues(context, mpAttributes);
	 	           				
	           				}else{
	           					bNotify = false;
	           					if(null != writeIntoFile){
									//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	        						_LOGGER.debug("PWCCMAppIntegration : updateLRPInternal -->  No such part in EV6 ");
									//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	        					}
								//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	           					//notifyCOCoOrdinator(context, mpCO, JSON_LEGACY_PR, STATUS_FAIL, MESSAGE_NO_SUCH_PART);
	           					slMissingPartList.addElement(strToPartName);
								//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	           				}
 	           			}else{
 	           				bNotify = false;
 	           				if(null != writeIntoFile){
								//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	 							_LOGGER.debug("PWCCMAppIntegration : updateLRPInternal -->  No such part in EV6 ");
								//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	 						}
							//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
 	           				//notifyCOCoOrdinator(context, mpCO, JSON_LEGACY_PR, STATUS_FAIL, MESSAGE_NO_SUCH_PART);
 	           				slMissingPartList.addElement(strFromPartName);
							//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
 	           			}
	 	           	}
					//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	 	           if(null != slMissingPartList && slMissingPartList.size()>0){
		 	       		StringBuffer sbMessageForPart = new StringBuffer();
		 	       		sbMessageForPart.append(MESSAGE_NO_SUCH_PARTS);
		 	       		sbMessageForPart.append("\n");
		 	       		sbMessageForPart.append(slMissingPartList.toString());
		 	       		notifyCOCoOrdinator(context, mpCO, JSON_LEGACY_PR, STATUS_FAIL, sbMessageForPart.toString());
	 	           	}
					//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 09 - PMA Performance improvement
	 	           	// Committing transaction
	 	           	ContextUtil.commitTransaction(context);
	 	           	
	 	           	// Notifying the update status 'Pass' to CO coordinator
	 	           	if(bNotify){
	 	           		notifyCOCoOrdinator(context, mpCO, JSON_LEGACY_PR, STATUS_PASS, null);
	 	           	}
	 	         
            	}catch(Exception ex){
            		ex.printStackTrace();
            		if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        				_LOGGER.debug("Exception in PWCCMAppIntegration : updateLRPInternal-->Notify Update Status :-"+ex.getMessage());
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        			}
            		// Notifying the update status 'Fail' to CO coordinator
        			notifyCOCoOrdinator(context, mpCO, JSON_LEGACY_PR, STATUS_FAIL, ex.getMessage());
            		
            		// Aborting transaction
        			ContextUtil.abortTransaction(context);
            	}
            	
             }else{
        		 if(null != writeIntoFile){
				 	//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        			 _LOGGER.debug("PWCCMAppIntegration : updateLRPInternal -->  No connections provided ");						
					 //END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				 }
            	 notifyCOCoOrdinator(context, mpCO, JSON_LEGACY_PR, STATUS_FAIL, MESSAGE_NO_DATA_PROVIDED);
             }
			 if(null != writeIntoFile){
			 	//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : updateLRPInternal --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateLRPInternal --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : updateLRPInternal :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}	
	}
	
	/**
	* This method is to get the LRP connection attribute values, coming from down stair.
	* @param context
	* @param args
	* @returns Map
	* @throws Exception if the operation fails
	*/
	public Map getLRPAttributesValues(Context context, JSONArray jaTableDataInd, int iCONameIndex, int iCreateDataIndex, int iDocScopeIndex, int iReplacementScopeIndex) throws Exception{
		Map mpReturn = new HashMap();
		String strAttrDocNum = DomainConstants.EMPTY_STRING;
		String strAttrCreateData = DomainConstants.EMPTY_STRING;
		String strAttrDocScope = DomainConstants.EMPTY_STRING;
		String strAttrReplacementScope = DomainConstants.EMPTY_STRING;
		try{
			strAttrDocNum = jaTableDataInd.getString(iCONameIndex);
			if("null" == strAttrDocNum){
				strAttrDocNum = DomainConstants.EMPTY_STRING;
			}
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getLRPAttributesValues --> Start \n", writeIntoFile);
				writeDataToFile("Execution Start Time of PWCCMAppIntegration : getLRPAttributesValues --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
			}*/
			strAttrCreateData = jaTableDataInd.getString(iCreateDataIndex);
			if("null" == strAttrCreateData){
				strAttrCreateData = DomainConstants.EMPTY_STRING;
			}else{
				Date date = new Date(strAttrCreateData);
				SimpleDateFormat mxDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),context.getLocale());
				strAttrCreateData = mxDateFormat.format(date);
			}
			strAttrDocScope = jaTableDataInd.getString(iDocScopeIndex);
			if("null" == strAttrDocScope || DomainConstants.EMPTY_STRING.equals(strAttrDocScope)){
				strAttrDocScope = DomainConstants.EMPTY_STRING;
			}else{
				strAttrDocScope = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.LRP.DocumentScope."+strAttrDocScope.trim().replace(" ", "_"));
			}	
			strAttrReplacementScope = jaTableDataInd.getString(iReplacementScopeIndex);
			if("null" == strAttrReplacementScope){
				strAttrReplacementScope = DomainConstants.EMPTY_STRING;
			}
			mpReturn.put(ATTR_PWC_DOCUMENT_NUMBER, strAttrDocNum.trim());
			mpReturn.put(ATTR_PWC_CREATE_DATE, strAttrCreateData.trim());
			mpReturn.put(ATTR_PWC_DOCUMENT_SCOPE, strAttrDocScope.trim());
			mpReturn.put(ATTR_PWC_REPLACEMENT_SCOPE, strAttrReplacementScope.trim());
			mpReturn.put(ATTR_PWC_DOCUMENT_ACTION, EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.LRP.DocumentAction.REPLACED_BY"));
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : getLRPAttributesValues --> LRP Attribute Value(Map)-->"+mpReturn.size()+"\n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : getLRPAttributesValues --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : getLRPAttributesValues --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : getLRPAttributesValues :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}			
		}
		return mpReturn;
	}
	
	/**
	 * This Method invokes the job queuing framework to create job object. 
	 * @param context
	 * @param args
	 * @return - void
	 * @throws Exception - if operation fails
	 */  
	public void CMAppJobQueuing(Context context, String args[]) throws Exception{
		String strJPOName = "PWC_JobQueueUtil";
    	String strMethodName = "buildJobQueueObjectForCMApp";   
		try{
			ContextUtil.pushContext(context);
			String strCMAppPushedData = args[0];
			if(null != strCMAppPushedData){
				byte[] decoded = Base64.decodeBase64(strCMAppPushedData.getBytes()); 
				strCMAppPushedData = new String(decoded);
	        	String[] arrJPOArgs = {strCMAppPushedData};
	        	JPO.invoke(context, strJPOName, null, strMethodName, arrJPOArgs);
	        }
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			ContextUtil.popContext(context);
		}
    }
	
	/**
	* This method rebuilds the relationship details map.
	* @param context 
	* @param args
	* @return MapList
	* @throws Exception if the operation fails
	*/
	public MapList reBuildRelatedAttributeMap(MapList mlRelItems, Map mpDetails) throws Exception{
		Map mpDataOutput = null;
		Map mpData = null;
		String strKey = DomainConstants.EMPTY_STRING;
		String strValue = DomainConstants.EMPTY_STRING;
		MapList mlReturn = new MapList();
		try{
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : reBuildRelatedAttributeMap --> Start \n", writeIntoFile);
				writeDataToFile("Execution Start Time of PWCCMAppIntegration : reBuildRelatedAttributeMap --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
			}*/
			if (!mlRelItems.isEmpty()){
				for (int i = 0; i < mlRelItems.size(); i++){
					mpDataOutput = new HashMap();
					mpData = (Map) mlRelItems.get(i);
					Iterator itr = mpData.entrySet().iterator();
					while (itr.hasNext()){
						 Entry entry = (Entry) itr.next();
						 strKey =(String) entry.getKey();
						 strValue =(String) entry.getValue();
						 if (strKey.contains("attribute")){
							 strKey = strKey.substring(strKey.indexOf("[")+1, strKey.length()-1);
							 mpDataOutput.put(strKey, strValue);
						 }else{
							 mpDataOutput.put(strKey, strValue);
						 }
					}
					if(null != mpDetails){
						mpDataOutput.putAll(mpDetails);
					}
					mlReturn.add(i, mpDataOutput);
				}
			}
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : reBuildRelatedAttributeMap --> ReBuild Maplist Data size --> "+mlReturn.size() + "\n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : reBuildRelatedAttributeMap  --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : reBuildRelatedAttributeMap --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : reBuildRelatedAttributeMap :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		return mlReturn;
	}
	
	/**
	* This method generate column identifiers and table data.
	* @param context 
	* @param args
	* @return String
	* @throws Exception if the operation fails
	*/
	public String generateColumnIdentifiersAndTableData (MapList mlData) throws Exception{
		String strReturn = null;
		Map mpData = null;
		try{
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : generateColumnIdentifiersAndTableData --> Start \n", writeIntoFile);
				writeDataToFile("Execution Start Time of PWCCMAppIntegration : generateColumnIdentifiersAndTableData --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
			}*/
			DefaultTableModel dtm = new DefaultTableModel();
			if(null != mlData && !mlData.isEmpty()){
				Set stHeader = ((Map) mlData.get(0)).keySet();
				Object[] objArrHeader = (Object[]) stHeader.toArray();
		
				// Setting column identifiers
				dtm.setColumnIdentifiers(objArrHeader);
		
				// Building table data rows
				for (int i = 0; i < mlData.size(); i++){
					mpData = (Map) mlData.get(i);
					Object[] objArrMapValue = new Object[objArrHeader.length];
					for (int j = 0; j < objArrHeader.length; j++){
						objArrMapValue[j] = (Object) mpData.get((Object) objArrHeader[j]);
					}
					dtm.addRow(objArrMapValue);
				}
			}else{
				Object[] objArrHeader = new Object[0];		
				Object[] objArrMapValue = new Object[0];
				dtm.setColumnIdentifiers(objArrHeader);
				dtm.addRow(objArrMapValue);
			}
			
			// Transforming the table data object to json object
			TableDataObject dataCOTableObject = TableDataObject.createTableDataObject(dtm);
			Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
			strReturn = gson.toJson(dataCOTableObject);
			/*if(null != writeIntoFile){
				writeDataToFile("Execution End Time of PWCCMAppIntegration : generateColumnIdentifiersAndTableData  --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : generateColumnIdentifiersAndTableData --> Exit \n", writeIntoFile);
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : generateColumnIdentifiersAndTableData :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		return strReturn; 
	}
	
	/**
	 * This Method pushes the Make From info to CM App. 
	 * @param context
	 * @param args
	 * @return - void
	 * @throws Exception - if operation fails
	 */  
	@SuppressWarnings("deprecation")
	public void pushMakeFromInfoToCMApp(Context context, String[] args) throws Exception{
		JSONObject jsonObjMakeFrom =null;
		JSONObject jsonObjMakeFromInt = null;
		JSONArray jaItemsList  = null;
        JSONObject joItemsData = null;
        JSONObject joProcessData = null;
		String[] rels = new String[1];
		StringList slRelSelect = new StringList();
		try{
			//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			writeIntoFile = setLoggerPath(CMAPP_MAKE_FROM_DETAILS_LOG_FILE_NAME);
			//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : pushMakeFromInfoToCMApp --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : pushMakeFromInfoToCMApp --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			String strRelId = args[0];
			rels[0]=strRelId;
			slRelSelect = getMakeFromSelectable();
			MapList mlMakeFromCon = DomainRelationship.getInfo(context, rels, slRelSelect);
			Map mMap = (Map) mlMakeFromCon.get(0);
			mMap.put("Action", "Delete");
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : pushMakeFromInfoToCMApp --> RelationshipId -->"+strRelId + "\n", writeIntoFile);
			}*/
			// Re-Build MapList
			MapList mlMakeFrom = reBuildRelatedAttributeMap(mlMakeFromCon, null);
			
			// Building json object of column identifier & table data
			jsonObjMakeFromInt = new JSONObject(generateColumnIdentifiersAndTableData (mlMakeFromCon));
			
			// Building the final json object - jsonObjMakeFrom
			jaItemsList  = new JSONArray();
			jaItemsList.put(JSON_MAKE_FROM);
			joItemsData = new JSONObject();
			joItemsData.put(JSON_MAKE_FROM, jsonObjMakeFromInt);
			joProcessData = new JSONObject();
			joProcessData.put(JSON_ITEMS_LIST, jaItemsList);
			joProcessData.put(JSON_ITEMS_DATA, joItemsData);
			jsonObjMakeFrom = new JSONObject();
			jsonObjMakeFrom.put(JSON_PROCESS_NAME, JSON_MAKE_FROM);
			jsonObjMakeFrom.put(JSON_PROCESS_DATA, joProcessData);
			
			String strMakeFrom = jsonObjMakeFrom.toString();
			// Encoding the string
			byte[] bytesEncoded = Base64.encodeBase64(strMakeFrom.getBytes());
			String strEncoded = new String(bytesEncoded);
			
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : pushMakeFromInfoToCMApp --> Make From --> "+strMakeFrom + "\n", writeIntoFile);
			}*/
			
			try{
				/*if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : pushMakeFromInfoToCMApp --> Pushing Data to CMApp (Make From) -->\n", writeIntoFile);
				}*/
				// Pushing Data
				CMAppServiceStub.PushProcessToLegacy pushProcessToLegacy = new CMAppServiceStub.PushProcessToLegacy();
				pushProcessToLegacy.setProcessDetails(strEncoded);
				CMAppServiceStub.PushProcessToLegacyE pushProcLegE = new CMAppServiceStub.PushProcessToLegacyE();
		        pushProcLegE.setPushProcessToLegacy(pushProcessToLegacy);
		        CMAppServiceStub cmAppStub = new CMAppServiceStub();
		        CMAppServiceCallbackHandler callBack = new CMAppServiceCallbackHandler();
		        cmAppStub.startpushProcessToLegacy(pushProcLegE, callBack);
				notifyCOCoOrdinator(context, null, JSON_MAKE_FROM, STATUS_PASS, null);
			}catch(Exception exp){
				if(null != writeIntoFile){
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					_LOGGER.debug("Exception in PWCCMAppIntegration : pushMakeFromInfoToCMApp :-"+exp.getMessage());
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				}
				notifyCOCoOrdinator(context, null, JSON_MAKE_FROM, STATUS_FAIL, exp.getMessage());
			}
			
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : pushMakeFromInfoToCMApp --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : pushMakeFromInfoToCMApp --> Exit \n", writeIntoFile);
			}			
		}catch(Exception exp){
			exp.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : pushMakeFromInfoToCMApp :-"+exp.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
	}
	
	/**
	 * @since: getMakeFromSelectable
	 * @logic: Object selects for the Part Make From
	 * @return: StringList
	 * @throws: Exception
	 */
	@SuppressWarnings("unchecked")
	private StringList getMakeFromSelectable(){
		StringList slSelect = new StringList();
		slSelect.add("attribute["+ATTR_COMMENT+"]");
		slSelect.add("attribute["+ATTR_ASSEMBLY_LINE_NUMBER+"]");
		slSelect.add("attribute["+ATTR_ENG_PART_NUMBER+"]");
		slSelect.add("attribute["+ATTR_MAKE_FROM+"]");
		slSelect.add("attribute["+ATTR_MDDOCUMENT_NUMBER+"]");
		slSelect.add("attribute["+ATTR_RAW_MATERIAL+"]");
		slSelect.add("attribute["+ATTR_PWC_REFERENCE_DOCUMENT+"]");
		slSelect.add("attribute["+ATTR_REVISED_BY+"]");
		slSelect.add("attribute["+ATTR_REVISED_DATE+"]");
		slSelect.add("attribute["+ATTR_QUANTITY+"]");

		slSelect.add(DomainRelationship.SELECT_FROM_ID);
		slSelect.add(DomainRelationship.SELECT_FROM_NAME);
		slSelect.add(DomainRelationship.SELECT_FROM_TYPE);
		slSelect.add(DomainRelationship.SELECT_FROM_REVISION);
		slSelect.add("from."+DomainConstants.SELECT_DESCRIPTION);
		slSelect.add("from."+DomainConstants.SELECT_CURRENT);
		
		slSelect.add(DomainRelationship.SELECT_TO_ID);
		slSelect.add(DomainRelationship.SELECT_TO_NAME);
		slSelect.add(DomainRelationship.SELECT_TO_TYPE);
		slSelect.add(DomainRelationship.SELECT_TO_REVISION);
		slSelect.add("to."+DomainConstants.SELECT_DESCRIPTION);
		slSelect.add("to."+DomainConstants.SELECT_CURRENT);

		return slSelect;
	}
	
	/**
	* Trigger Called when Product Configuration Relationship Deleted in Context of HP
	* @param context 
	* @param String
	* @return void
	* @throws Exception if the operation fails
	*/
	public void getPCDetailsOnDeletePCRel(Context context, String[] args) throws Exception{
		try{
			//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			writeIntoFile = setLoggerPath(CMAPP_PC_MODEL_DETAILS_LOG_FILE_NAME);
			//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : getPCDetailsOnDeletePCRel --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : getPCDetailsOnDeletePCRel --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			// Getting the PC Details
			getPCDetails(context, args);
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : getPCDetailsOnDeletePCRel --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : getPCDetailsOnDeletePCRel --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : getPCDetailsOnDeletePCRel :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
	}
	
	/**
	* Trigger Called when Top Level Part Relationship Deleted in Context of PC
	* @param context 
	* @param String
	* @return void
	* @throws Exception if the operation fails
	*/
	public void getPCDetailsOnDeleteTopLevelRel(Context context, String[] args) throws Exception{
		try{
			//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			writeIntoFile = setLoggerPath(CMAPP_PC_MODEL_DETAILS_LOG_FILE_NAME);
			//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : getPCDetailsOnDeleteTopLevelRel --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : getPCDetailsOnDeleteTopLevelRel --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			// Getting the PC Details
			getPCDetails(context,args);
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : getPCDetailsOnDeleteTopLevelRel --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : getPCDetailsOnDeleteTopLevelRel --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : getPCDetailsOnDeleteTopLevelRel :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
	}
	
	/**
	* This method is to get the PC Details, which is to be pushed to CM App.
	* @param context 
	* @param String
	* @return void
	* @throws Exception if the operation fails
	*/
	public void getPCDetails(Context context, String[] args) throws Exception{
		JSONObject jsonObjPCModel =null;
		JSONObject jsonObjPCModelInt = null;
		JSONArray jaItemsList = null;
        JSONObject joItemsData = null;
        JSONObject joProcessData = null;
		StringList slSelectables= new StringList();
		try{
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : getPCDetails --> Start \n", writeIntoFile);
				writeDataToFile("Execution Start Time of PWCCMAppIntegration : getPCDetails --> " + java.util.Calendar.getInstance().getTime() + "\n", writeIntoFile);
			}*/
			String strObjectId = args[0];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
				DomainObject domPC = new DomainObject(strObjectId);
			    Map mAttrList=domPC.getAttributeMap(context);
				slSelectables.add(PWCIntegrationConstants.SELECT_ID);
				slSelectables.add(PWCIntegrationConstants.SELECT_NAME);
				slSelectables.add(PWCIntegrationConstants.SELECT_TYPE);
				slSelectables.add(PWCIntegrationConstants.SELECT_REVISION);
				slSelectables.add(PWCIntegrationConstants.SELECT_CURRENT);
				slSelectables.add(PWCIntegrationConstants.SELECT_MODIFIED);
				slSelectables.add(PWCIntegrationConstants.SELECT_ORIGINATOR);
				slSelectables.add(PWCIntegrationConstants.SELECT_DESCRIPTION);
				slSelectables.add(PWCIntegrationConstants.SELECT_VAULT);	
				slSelectables.add("from["+RELATIONSHIP_TOP_LEVEL_PART+"].to.name");
				slSelectables.add("to["+RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.to["+RELATIONSHIP_DERIVED+"].from.name");
				Map mpData = domPC.getInfo(context, slSelectables);
				mpData.putAll(mAttrList);			
				MapList mlPCData = new MapList();
				mlPCData.add(mpData);	        
				/*if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : getPCDetails -->  Product Configuration Object Id --> "+strObjectId + "\n", writeIntoFile);
					writeDataToFile("PWCCMAppIntegration : getPCDetails --> PC Details  " + mpData + "\n", writeIntoFile);
				}*/
				
				// Re-Build MapList
				MapList mlPC = reBuildRelatedAttributeMap(mlPCData, null);
				
				// Building json object of column identifier & table data
				jsonObjPCModelInt = new JSONObject(generateColumnIdentifiersAndTableData(mlPC));
				
				// Building the final json object - jsonObjMakeFrom
		        jaItemsList  = new JSONArray();
		        jaItemsList.put(JSON_PC_DETAILS);
		        joItemsData = new JSONObject();
		        joItemsData.put(JSON_PC_DETAILS, jsonObjPCModelInt);
		        joProcessData = new JSONObject();
		        joProcessData.put(JSON_ITEMS_LIST, jaItemsList);
		        joProcessData.put(JSON_ITEMS_DATA, joItemsData);
		        jsonObjPCModel = new JSONObject();
		        jsonObjPCModel.put(JSON_PROCESS_NAME, JSON_PC_MODEL);
		        jsonObjPCModel.put(JSON_PROCESS_DATA, joProcessData);
		        
		        String strPCModel = jsonObjPCModel.toString();

		        // Encoding the string 
		        byte[] bytesEncoded = Base64.encodeBase64(strPCModel.getBytes());
		        String strEncoded = new String(bytesEncoded);	        
				/*if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : getPCDetails --> PC Model --> "+strPCModel + "\n", writeIntoFile);
				}*/
				try{
					/*if(null != writeIntoFile){
						writeDataToFile("PWCCMAppIntegration : getPCDetails -->Pushing PC data to CMApp --> \n", writeIntoFile);
					}*/					
			        // Pushing Data
			        CMAppServiceStub.PushProcessToLegacy pushProcessToLegacy = new CMAppServiceStub.PushProcessToLegacy();
			        pushProcessToLegacy.setProcessDetails(strEncoded);
			        CMAppServiceStub.PushProcessToLegacyE pushProcLegE = new CMAppServiceStub.PushProcessToLegacyE();
			        pushProcLegE.setPushProcessToLegacy(pushProcessToLegacy);
			        CMAppServiceStub cmAppStub = new CMAppServiceStub();
			        CMAppServiceCallbackHandler callBack = new CMAppServiceCallbackHandler();
			        cmAppStub.startpushProcessToLegacy(pushProcLegE, callBack);
			        notifyCOCoOrdinator(context, null, JSON_PC_MODEL, STATUS_PASS, null);
				}catch(Exception exp){
					if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						_LOGGER.debug("Exception in PWCCMAppIntegration : getPCDetails :-"+exp.getMessage());
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					}
					notifyCOCoOrdinator(context, null, JSON_PC_MODEL, STATUS_FAIL, exp.getMessage());
				}
				if(null != writeIntoFile){
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					_LOGGER.debug("Execution End Time of PWCCMAppIntegration : getPCDetails --> " + java.util.Calendar.getInstance().getTime());
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					//writeDataToFile("PWCCMAppIntegration : getPCDetails --> Exit \n", writeIntoFile);
				}	
		     }
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppProcess : getPCDetails :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
	}
	
	/**
	* This function is to check PWC_SyncWithCMApp/PWC_DesignLevelComplete attribute's value of related CO on context Part object
	* @param context the eMatrix <code>Context</code> object
	* @param args String
	* @return boolean
	* @
	**/
	public static boolean checkAttributeValueAndCompleteAction(Context context, String objectID) 
			throws Exception 
	{
		boolean bReturn = false;
		String strKeyForAffectedItemSelectable = DomainConstants.EMPTY_STRING;
		String strKeyToAffectedItemGetValue = DomainConstants.EMPTY_STRING;
		
		String strKeyForImplementedItemSelectable = DomainConstants.EMPTY_STRING;
		String strKeyToImplementedItemGetValue = DomainConstants.EMPTY_STRING;
		
		try 
		{
			if (UIUtil.isNotNullAndNotEmpty(objectID))
			{
				//Object ID's List to get values
				StringList slObjectIDList = new StringList(objectID);
				
				// HC-TBD added conditions to consider ONLY active COs
				//Selectable keys
				// Affected Item
				strKeyForAffectedItemSelectable 	= "to[" + ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM + "].from.to[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "|(from.type==\"" + ChangeConstants.TYPE_CHANGE_ORDER + "\" && from.current != Completed && from.current != Implemented && from.current != 'On Hold' && from.current != Cancelled)].from.attribute[" + ATTRIBUTE_PWC_Sync_With_CM_App + "].value";
				strKeyToAffectedItemGetValue 		= "to[" + ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM + "].from.to[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "].from.attribute[" + ATTRIBUTE_PWC_Sync_With_CM_App + "].value";
				
				// HC-TBD added conditions to consider ONLY active COs
				// Implemented Item
				strKeyForImplementedItemSelectable 	= "to[" + ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM + "].from.to[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "|(from.type==\"" + ChangeConstants.TYPE_CHANGE_ORDER + "\" && from.current != Completed && from.current != Implemented && from.current != 'On Hold' && from.current != Cancelled)].from.attribute[" + ATTRIBUTE_PWC_Sync_With_CM_App + "].value";
				strKeyToImplementedItemGetValue 	= "to[" + ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM + "].from.to[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "].from.attribute[" + ATTRIBUTE_PWC_Sync_With_CM_App + "].value";
				
				StringList slObjectSelect = new StringList(strKeyForAffectedItemSelectable);
				slObjectSelect.addElement(strKeyForImplementedItemSelectable);
				
				// Getting required details from objects
				BusinessObjectWithSelectList busClassList 		= BusinessObject.getSelectBusinessObjectData(context, (String[]) slObjectIDList.toArray(new String[0]), slObjectSelect);
				BusinessObjectWithSelectItr busWithSelectItr 	= new BusinessObjectWithSelectItr(busClassList);
				StringList slSyncWithCMAppValueList 			= new StringList();
				StringList slAffItemsFlagList 					= new StringList();
				StringList slImplItemsFlagList 					= new StringList();
				
				while (busWithSelectItr.next())
				{
					BusinessObjectWithSelect busWithSelect 	= busWithSelectItr.obj();
					slAffItemsFlagList 						= (StringList) busWithSelect.getSelectDataList(strKeyToAffectedItemGetValue);
					slImplItemsFlagList 					= (StringList) busWithSelect.getSelectDataList(strKeyToImplementedItemGetValue);
					
					if (null != slAffItemsFlagList && !slAffItemsFlagList.isEmpty())
					{
						for (int i = 0; i < slAffItemsFlagList.size(); i++)
						{
							slSyncWithCMAppValueList.add(slAffItemsFlagList.get(i));
						}
					}
                	
					if (null != slImplItemsFlagList && !slImplItemsFlagList.isEmpty())
					{
                		for (int i = 0; i < slImplItemsFlagList.size(); i++)
                		{
							slSyncWithCMAppValueList.add(slImplItemsFlagList.get(i));
						}
                	}	  
					
					if (null != slSyncWithCMAppValueList && slSyncWithCMAppValueList.contains("TRUE"))
					{
						bReturn = true;
					}
				}	
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
        return bReturn;
	}
	
	/**
	* This function is to check PWC_SyncWithCMApp attribute's value of related CO on context Part object
	* @param context the eMatrix <code>Context</code> object
	* @param args String
	* @return int
	* @
	**/
	public int validateSyncWithCMAppOnCO (Context context, String []args) 
			throws Exception 
	{
		if (args == null || args.length < 1) 
		{
			throw (new IllegalArgumentException());
		}
		
		boolean bReturn 		= false;
		int intReturnVal 		= 0;
		int index 				= 0;
		String strByPassTrigger = DomainConstants.EMPTY_STRING;
		String strFromObjectID 	= args[index++];
		
		try 
		{
			if (UIUtil.isNotNullAndNotEmpty(strFromObjectID))
			{
				strByPassTrigger = PropertyUtil.getGlobalRPEValue(context, "CMAPP_DESIGN_LEVEL_UPDATE");
				
				if (UIUtil.isNullOrEmpty(strByPassTrigger) || "null".equals(strByPassTrigger) || "FALSE".equalsIgnoreCase(strByPassTrigger))
				{
					strByPassTrigger = PropertyUtil.getGlobalRPEValue(context, "CMAPP_FLOAT_ON_RELEASE");
				}
				
				if (UIUtil.isNullOrEmpty(strByPassTrigger) || "null".equals(strByPassTrigger) || "FALSE".equalsIgnoreCase(strByPassTrigger))
				{
					strByPassTrigger = PropertyUtil.getGlobalRPEValue(context, "CMAPP_BYPASS_CHILD_PART_RELEASE");
				}
				
				if (UIUtil.isNullOrEmpty(strByPassTrigger) 
						|| (UIUtil.isNotNullAndNotEmpty(strByPassTrigger) && !"TRUE".equalsIgnoreCase(strByPassTrigger)) )
				{
					bReturn = checkAttributeValueAndCompleteAction(context, strFromObjectID);
					if (bReturn)
					{
						intReturnVal = 1;
						String strMessage = i18nNow.getI18nString("pwcEngineeringCentral.Part.RelationshipBOM.Action.ErrorMessage", "emxEngineeringCentralStringResource", "en");
						MqlUtil.mqlCommand(context, "notice $1",strMessage);
						return intReturnVal;
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return intReturnVal;
	}
	
	/**
	* This function is to check Sync with CM App value of context Part object
	* @param context the eMatrix <code>Context</code> object
	* @param args
	* @return boolean
	* @
	**/
	public boolean checkSyncWithCMAppValue(Context context, String [] args) throws Exception{
		boolean bFlag = false;
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");
			StringList slSyncWithCMAppValueList = new StringList();
			StringList slAffItemsFlagList = new StringList();
			StringList slImplItemsFlagList = new StringList();
			if(!UIUtil.isNullOrEmpty(strObjectId)){
				String strArray[] = new String[1];
				strArray[0] = strObjectId;
				StringList ObjectSelect = new StringList();
				ObjectSelect.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"|.from.type==\""+ChangeConstants.TYPE_CHANGE_ORDER+"\"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");		
				ObjectSelect.add("to["+ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"|.from.type==\""+ChangeConstants.TYPE_CHANGE_ORDER+"\"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");
				BusinessObjectWithSelectList busClassList = BusinessObject.getSelectBusinessObjectData(context, strArray, ObjectSelect);				
				BusinessObjectWithSelectItr busWithSelectItr = new BusinessObjectWithSelectItr(busClassList);		
				while (busWithSelectItr.next()){
					BusinessObjectWithSelect busWithSelect = busWithSelectItr.obj();
					slAffItemsFlagList = (StringList) busWithSelect.getSelectDataList("to["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");
					slImplItemsFlagList = (StringList) busWithSelect.getSelectDataList("to["+ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");
					if(null != slAffItemsFlagList && !slAffItemsFlagList.isEmpty()){
						for(int i = 0; i < slAffItemsFlagList.size(); i++){
							slSyncWithCMAppValueList.add(slAffItemsFlagList.get(i));
						}
					}
                	if(null != slImplItemsFlagList && !slImplItemsFlagList.isEmpty()){
                		for(int i = 0; i < slImplItemsFlagList.size(); i++){
							slSyncWithCMAppValueList.add(slImplItemsFlagList.get(i));
						}
                	}	  
					if(null != slSyncWithCMAppValueList && slSyncWithCMAppValueList.contains("TRUE")){
						bFlag = true;
					}				
			    }
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return bFlag;
	}
	
	/**
	* This function is to return programHTMLOutput String to context Part object
	* @param context the eMatrix <code>Context</code> object
	* @param args String
	* @return string
	* @
	**/
	public String showWarningLabelForSyncWithCMApp(Context context, String [] args) throws Exception{
		String strLabelForSyncWithCMApp = DomainConstants.EMPTY_STRING;
		try {
			String strMessageForSyncWithCMApp = i18nNow.getI18nString("pwcEngineeringCentral.Label.MessageOnPartPropertiesForSyncWithCMAppValue", "emxEngineeringCentralStringResource", "en");
			strLabelForSyncWithCMApp = "<font size=\"2\" color=\"black\"><strong>"+ strMessageForSyncWithCMApp +"</strong></font>";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strLabelForSyncWithCMApp;
	}
	
	/**
	 * Function added for getting Range for Change Class in CO Create Form
	 * @param context
	 * @param args
	 * @return Map
	 * @throws Exception
	 */
	public Map getChangeClass(Context context, String args[]) throws Exception{
		Map mReturnRangeValue = new HashMap();
		try{
			StringList strlRanges = FrameworkUtil.getRanges(context,ATTRIBUTE_PWC_CHANGE_CLASS);			
			strlRanges.add(0, DomainConstants.EMPTY_STRING);
			mReturnRangeValue.put("field_choices", strlRanges);
			mReturnRangeValue.put("field_display_choices",strlRanges);		
		}catch(Exception e){
			e.printStackTrace();
		}	
		return mReturnRangeValue;
	}
	/**
	 * Function added for getting Range for CO Category in CO Create Form
	 * @param context
	 * @param args
	 * @return Map
	 * @throws Exception
	 */
	public Map getCOCategory(Context context, String args[]) throws Exception{
		Map mReturnRangeValue = new HashMap();
		try{
			StringList strlRanges = FrameworkUtil.getRanges(context,ATTRIBUTE_PWC_CO_Category);			
			strlRanges.add(0, DomainConstants.EMPTY_STRING);
			mReturnRangeValue.put("field_choices", strlRanges);
			mReturnRangeValue.put("field_display_choices",strlRanges);		
		}catch(Exception e){
			e.printStackTrace();
		}
		return mReturnRangeValue;
	}
	
	/**
	 * This method is to allow editing Sync With CMapp field based on state
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	 public boolean showEditableSyncWithCMAppAttributeForForm(Context context, String args[]) throws Exception{
		boolean isEditAccess = false;
		boolean boolAccess = true;
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");
			String strAttributeName = DomainConstants.EMPTY_STRING ;
			HashMap hmSettings = (HashMap)programMap.get("SETTINGS");
			if(!UIUtil.isNullOrEmpty(strObjectId)){
				DomainObject domObj = DomainObject.newInstance(context,strObjectId);
				StringList strlSelectList = new StringList(1);
				strlSelectList.add(DomainObject.SELECT_CURRENT);
				Map mData = (Map)domObj.getInfo(context,strlSelectList);
				String strCOState = (String)mData.get(DomainObject.SELECT_CURRENT);
				if(null != hmSettings && !hmSettings.isEmpty()){
					strAttributeName = (String) hmSettings.get("Admin Type");
				}
				if(ChangeConstants.STATE_FASTTRACKCHANGE_PREPARE.equals(strCOState) || ChangeConstants.STATE_FASTTRACKCHANGE_INWORK.equals(strCOState)){
					if((strAttributeName.equals("attribute_"+ATTRIBUTE_PWC_Sync_With_CM_App))){
						isEditAccess=true;
					}
				}
				if(isEditAccess){
					hmSettings.put("Editable", "true");
				}else{
					hmSettings.put("Editable", "false");
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
		return boolAccess;
	 }
	 
	/**PostProcess Call From Jsp
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void setAttributeCMAppECIssued(Context context, String [] args) throws Exception{
		try 
		{
			HashMap programMap 	= (HashMap)JPO.unpackArgs(args);
			String strCOId	= (String)programMap.get("newObjectId");
			if(UIUtil.isNotNullAndNotEmpty(strCOId))
			{			
				DomainObject dCOObject = DomainObject.newInstance(context, strCOId);	
				String sPWCSyncWithCMApp = dCOObject.getAttributeValue(context,ATTRIBUTE_PWC_Sync_With_CM_App);
				if("TRUE".equalsIgnoreCase(sPWCSyncWithCMApp)){
					dCOObject.setAttributeValue(context, ATTRIBUTE_PWC_CMAppECIssued, "No");
				}else{
					dCOObject.setAttributeValue(context, ATTRIBUTE_PWC_CMAppECIssued, "N/A");
				}
			}						
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**Check Trigger On CA
	 * Program to Ensure CA connected to one CO Attribute CMAppEcIssued value
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public int CheckAttributeCMAppEcIssued(Context context, String[] args)throws Exception{
		try{
			if (args == null || args.length < 1) {
				throw (new IllegalArgumentException());
			}
			String strCAId= args[0];
			String strCOId = DomainConstants.EMPTY_STRING;
			Map mpRel = new Hashtable();
			if(UIUtil.isNotNullAndNotEmpty(strCAId)){
				DomainObject dCAObject = DomainObject.newInstance(context, strCAId);
				StringList objSels = new StringList(1);
				objSels.add(DomainConstants.SELECT_ID);
				SelectList relSelects = new SelectList(2);
				relSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
				relSelects.addElement(DomainConstants.SELECT_POLICY);	
				MapList mlCOList = dCAObject.getRelatedObjects(context, 
						ChangeConstants.RELATIONSHIP_CHANGE_ACTION, 
						ChangeConstants.TYPE_CHANGE_ORDER,
						objSels,
						relSelects,
						true,
						false,
						(short) 1,
						null,
						null,
						0);
				if(null != mlCOList && mlCOList.size()>0){
					mpRel = (Hashtable)mlCOList.get(0);
					strCOId = (String) mpRel.get(DomainConstants.SELECT_ID);			    	   
					if(UIUtil.isNotNullAndNotEmpty(strCOId)){
						DomainObject domCO = DomainObject.newInstance(context, strCOId);
						String sPWCCMAppEcIssued = domCO.getAttributeValue(context, ATTRIBUTE_PWC_CMAppECIssued);
						String sPWCChangeClass = domCO.getAttributeValue(context, ATTRIBUTE_PWC_CHANGE_CLASS);
						String strPWCCOCategory = domCO.getAttributeValue(context, ATTR_PWC_CO_CATEGORY);
						//START :: Modified for HEAT-C-16656 : Add new range "Class 2 RS" for change class
						if(STR_CLASS_1.equalsIgnoreCase(sPWCChangeClass) || STR_CLASS_2.equalsIgnoreCase(sPWCChangeClass) || STR_CLASS_2_RS.equalsIgnoreCase(sPWCChangeClass)){
						//END :: Modified for HEAT-C-16656 : Add new range "Class 2 RS" for change class
							if(!"Release".equalsIgnoreCase(strPWCCOCategory)){
								if("No".equalsIgnoreCase(sPWCCMAppEcIssued)){
									String langStr = context.getSession().getLanguage();
									String strMessage = i18nNow.getI18nString("EnterpriseChangeMgt.CheckAttributeCMAppECIssued", "emxEnterpriseChangeMgtStringResource", "en");
									MqlUtil.mqlCommand(context, "notice $1",strMessage);
									return 1;
								}
							}
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**This method is to check the sync with CM App value
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public Vector getSyncWithCMApp(Context context, String[] args) throws Exception{
		Vector returnValues = new Vector();
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String iconGreen = "../common/images/iconParameterizationIndexedAttribute.gif";
		String iconRed = "../common/images/iconParameterizationNotIndexedAttribute.gif";
		StringList slSyncWithCmApp = new StringList();
		StringList slImplementedItems = new StringList();
		StringBuffer strBuffer = new StringBuffer();
		String strPartId = DomainConstants.EMPTY_STRING;
		Map mpData = null;
		BusinessObjectWithSelectList busClassList = null;
		BusinessObjectWithSelectItr busWithSelectItr = null;
		BusinessObjectWithSelect busWithSelect = null;
		StringList slSyncWithCMAppValueList = new StringList();
		StringList slAffItemsFlagList = new StringList();
		StringList slImplItemsFlagList = new StringList();
		try{
			StringList ObjectSelect = new StringList();
			ObjectSelect.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"|.from.type==\""+ChangeConstants.TYPE_CHANGE_ORDER+"\"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");		
			ObjectSelect.add("to["+ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"|.from.type==\""+ChangeConstants.TYPE_CHANGE_ORDER+"\"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");
			MapList objList = (MapList) programMap.get("objectList");
			ListIterator litr = objList.listIterator();
			while(litr.hasNext()){
				slSyncWithCMAppValueList = new StringList();
				strBuffer = new StringBuffer();
				mpData = (Map)litr.next();
				strPartId = (String) mpData.get(DomainConstants.SELECT_ID);
				String strArray[] = new String[1];
				strArray[0] = strPartId;
				busClassList = BusinessObject.getSelectBusinessObjectData(context, strArray, ObjectSelect);		
				busWithSelectItr = new BusinessObjectWithSelectItr(busClassList);
				while (busWithSelectItr.next()){
					busWithSelect = busWithSelectItr.obj();
					slAffItemsFlagList = (StringList) busWithSelect.getSelectDataList("to["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");
					slImplItemsFlagList = (StringList) busWithSelect.getSelectDataList("to["+ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");				
					if(null != slAffItemsFlagList && !slAffItemsFlagList.isEmpty()){
						for(int i = 0; i < slAffItemsFlagList.size(); i++){
							slSyncWithCMAppValueList.add(slAffItemsFlagList.get(i));
						}
					}
					if(null != slImplItemsFlagList && !slImplItemsFlagList.isEmpty()){
						for(int i = 0; i < slImplItemsFlagList.size(); i++){
							slSyncWithCMAppValueList.add(slImplItemsFlagList.get(i));
						}
					}	  
					if(null != slSyncWithCMAppValueList && slSyncWithCMAppValueList.contains("TRUE")){
						strBuffer.append("<html>");
						strBuffer.append("<img src=\"").append(StringUtil.htmlEncode(iconRed)).append("\"/>");
						strBuffer.append("</html>");
					}else{
						strBuffer.append("<html>");
						strBuffer.append("<img src=\"").append(StringUtil.htmlEncode(iconGreen)).append("\"/>");
						strBuffer.append("</html>");
					}
			    }
				returnValues.add(strBuffer.toString());
			}					
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return returnValues;							
	}
	
	/**
	* This method is to update EC Meta Data, when pushed from CM App.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void updateECMetaData(Context context, String[] args) throws Exception{
		JSONObject jsonObjECMetaData =null;
		JSONObject jsonObjIncorporation = null;
		JSONObject jsonObjAddnCancel = null;
		JSONObject jsonObjOtherText = null;
		JSONArray jaItemsList = null;
        JSONObject joItemsData = null;
        JSONObject joProcessData = null;
        JSONArray jaColumnIdentifiers = new JSONArray();
        JSONArray jaTableData = new JSONArray();
		boolean isContextPushed = false;
		String strColIdentifier = DomainConstants.EMPTY_STRING;
		String strItem = DomainConstants.EMPTY_STRING;
		bNotifyIncorporation = true;	
		bNotifyAddnCancel = true;
		try{
			String strPushedData = args[0];
			//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			writeIntoFile = setLoggerPath(CMAPP_EC_META_DATA_DETAILS_LOG_FILE_NAME);
			//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			// Setting context to CM App process user
			String strCMAppUserPushed = setCMAppProcessUserContext(context);
						
			if ("False".equalsIgnoreCase(strCMAppUserPushed) && !"User Agent".equals(context.getUser()))
			{
				ContextUtil.pushContext(context, PWCConstants.SUPER_USER, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				isContextPushed = true;	
			}
			
			if (null != writeIntoFile)
			{
				//writeDataToFile("PWCCMAppIntegration : updateECMetaData --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : updateECMetaData --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			try{
				if(null != strPushedData){
					jsonObjECMetaData = new JSONObject(strPushedData);
					if(null != jsonObjECMetaData){
						joProcessData = jsonObjECMetaData.getJSONObject(JSON_PROCESS_DATA);
						if(null != joProcessData){
							jaItemsList = joProcessData.getJSONArray(JSON_ITEMS_LIST);
							joItemsData = joProcessData.getJSONObject(JSON_ITEMS_DATA);
							if(null != joItemsData){
								/*if(null != writeIntoFile){
									writeDataToFile("PWCCMAppIntegration : updateECMetaData -->Process Name--> EC_META_DATA \n", writeIntoFile);						
								}*/
								if(jaItemsList.length() > 0){
									for(int i = 0; i < jaItemsList.length(); i++){
										strItem = jaItemsList.getString(i);
										if(JSON_EC_META_DATA_INCORPORATION.equalsIgnoreCase(strItem)){
											jsonObjIncorporation = joItemsData.getJSONObject(JSON_EC_META_DATA_INCORPORATION);
										}else if(JSON_EC_META_DATA_ADD_N_CANCEL.equalsIgnoreCase(strItem)){
											jsonObjAddnCancel = joItemsData.getJSONObject(JSON_EC_META_DATA_ADD_N_CANCEL);
										}else if(JSON_EC_META_DATA_OTHER_TEXT.equalsIgnoreCase(strItem)){
											jsonObjOtherText = joItemsData.getJSONObject(JSON_EC_META_DATA_OTHER_TEXT);
										}
									}
								}
							}else{
								if(null != writeIntoFile){
									//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
									_LOGGER.debug("PWCCMAppIntegration : updateECMetaData -->  NO DATA PROVIDED ");
									//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
								}
								notifyCOCoOrdinator(context, null, JSON_EC_META_DATA, STATUS_FAIL, MESSAGE_NO_DATA_PROVIDED);
							}
						}
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
				if(null != writeIntoFile){
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaData (Content Not In JSON Format) :-"+ex.getMessage());
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				}
				notifyCOCoOrdinator(context, null, JSON_EC_META_DATA, STATUS_FAIL, ex.getMessage());
			}
			
			Map mpChange = null;
			String strChangeId = DomainConstants.EMPTY_STRING;
			String strChangeName = DomainConstants.EMPTY_STRING;
			String strMsg = DomainConstants.EMPTY_STRING;
			boolean bJsonError = true;
			if(null != jsonObjIncorporation){
				bJsonError = false;
				mpChange = checkChangeObjectState(context, jsonObjIncorporation, JSON_EC_META_DATA);
			}else if(null != jsonObjAddnCancel){
				bJsonError = false;
				mpChange = checkChangeObjectState(context, jsonObjAddnCancel, JSON_EC_META_DATA);
			}else if(null != jsonObjOtherText){
				bJsonError = false;
				mpChange = checkChangeObjectState(context, jsonObjOtherText, JSON_EC_META_DATA);
			}
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : updateECMetaData --> Change Object --> "+mpChange + "\n", writeIntoFile);
			}*/
			if(null != mpChange){
				strMsg = (String) mpChange.get(strMessage);
				strChangeId = (String) mpChange.get(DomainObject.SELECT_ID);
				strChangeName = (String) mpChange.get(DomainObject.SELECT_NAME);
			}
			
			if(UIUtil.isNotNullAndNotEmpty(strChangeId)){
				if(PROCESS.equals(strMsg)){
					if(null != jsonObjIncorporation){
						int iCONameIndex = 0;
						int iHPIndex = 0;
						int iIncorporationIndex = 0;
						int iComplianceIndex = 0;
						int iComplianceCodeIndex = 0;
						int iSubstantiationIndex = 0;
						//START :: Added for HEAT-C-19459 : CMApp leftover items
						int iBuildSpecIndex = 0;
						//END :: Added for HEAT-C-19459 : CMApp leftover items
						jaColumnIdentifiers = jsonObjIncorporation.getJSONArray(JSON_COLUMN_IDENTIFIERS);
						if(jaColumnIdentifiers.length() > 0){
			            	for(int i = 0; i < jaColumnIdentifiers.length(); i++){
			            		strColIdentifier = jaColumnIdentifiers.getString(i);
			            		if("DOCUMENT_NO".equalsIgnoreCase(strColIdentifier)){
			            			iCONameIndex = i;
			            		}else if("MODEL_NAME".equalsIgnoreCase(strColIdentifier)){
			            			iHPIndex = i;
			            		}else if("INCORPORATION".equalsIgnoreCase(strColIdentifier)){
			            			iIncorporationIndex = i;
			            		}else if("COMPLIANCE".equalsIgnoreCase(strColIdentifier)){
			            			iComplianceIndex = i;
			            		}else if("SUBSTANTIATION".equalsIgnoreCase(strColIdentifier)){
			            			iSubstantiationIndex = i;
			            		}else if("COMPLIANCE_CD".equalsIgnoreCase(strColIdentifier)){
			            			iComplianceCodeIndex = i;
			            		}
			            		//START :: Added for HEAT-C-19459 : CMApp leftover items
			            		else if("BUILD_SPECS".equalsIgnoreCase(strColIdentifier)){
			            			iBuildSpecIndex = i;
			            		}
			            		//END :: Added for HEAT-C-19459 : CMApp leftover items
			            	}
			            }
						// Table Data
						jaTableData = jsonObjIncorporation.getJSONArray(JSON_TABLE_DATA);
						// Internal method - To process EC Meta Data_Incorporation
						//START :: Added for HEAT-C-19459 : CMApp leftover items
			            updateECMetaDataIncorporation(context, mpChange, jaTableData, iCONameIndex, iHPIndex, iIncorporationIndex, iComplianceIndex, iSubstantiationIndex, iComplianceCodeIndex, iBuildSpecIndex);
			            //END :: Added for HEAT-C-19459 : CMApp leftover items
					}
					
					if(null != jsonObjAddnCancel){
						int iCONameIndex = 0;
						int iBlockIndex = 0;
						int iReferenceFLIndex = 0;
						int iModelsIndex = 0;
						int iIncNotIncECSIndex = 0;
						int iEAPLsIndex = 0;
						int iTextIndex = 0;
						int iUIDIndex = 0;
						int iLevelIndex = 0;
						int iAddedTRSIndex = 0;
						int iAddedPartIndex = 0;
						int iAddedRevIndex = 0;
						int iPRCIndex = 0;
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
						int iSCIndex = 0;
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
						int iAddedQtyIndex = 0;
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
						int iDescriptionIndex = 0;
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
						int iCancelledTRSIndex = 0;
						int iCancelledPartIndex = 0;
						int iCancelledRevIndex = 0;
						int iCancelledQtyIndex = 0;
						int iDIIndex = 0;
						int iCCISIndex = 0;
						int iBlockSeqIndex = 0;
						jaColumnIdentifiers = jsonObjAddnCancel.getJSONArray(JSON_COLUMN_IDENTIFIERS);
						if(jaColumnIdentifiers.length() > 0){
			            	for(int i = 0; i < jaColumnIdentifiers.length(); i++){
			            		strColIdentifier = jaColumnIdentifiers.getString(i);
			            		if("DOCUMENT_NO".equalsIgnoreCase(strColIdentifier)){
			            			iCONameIndex = i;
			            		}else if("BLOCK".equalsIgnoreCase(strColIdentifier)){
			            			iBlockIndex = i;
			            		}else if("REFERENCE_FL".equalsIgnoreCase(strColIdentifier)){
			            			iReferenceFLIndex = i;
			            		}else if("MODELS".equalsIgnoreCase(strColIdentifier)){
			            			iModelsIndex = i;
			            		}else if("INCORP_NOT_INCORP_ECS".equalsIgnoreCase(strColIdentifier)){
			            			iIncNotIncECSIndex = i;
			            		}else if("EAPLS".equalsIgnoreCase(strColIdentifier)){
			            			iEAPLsIndex = i;
			            		}else if("TEXT".equalsIgnoreCase(strColIdentifier)){
			            			iTextIndex = i;
			            		}else if("SIN".equalsIgnoreCase(strColIdentifier)){
			            			iUIDIndex = i;
			            		}else if("AC_LINE_LEVEL".equalsIgnoreCase(strColIdentifier)){
			            			iLevelIndex = i;
			            		}else if("ADD_TRAN".equalsIgnoreCase(strColIdentifier)){
			            			iAddedTRSIndex = i;
			            		}else if("ADD_PART".equalsIgnoreCase(strColIdentifier)){
			            			iAddedPartIndex = i;
			            		}else if("ADD_REVISION_LETTER".equalsIgnoreCase(strColIdentifier)){
			            			iAddedRevIndex = i;
			            		}else if("PRC_CD".equalsIgnoreCase(strColIdentifier)){
			            			iPRCIndex = i;
			            		}
			            		//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
			            		else if("SALEABILITY_CD".equalsIgnoreCase(strColIdentifier)){
			            			iSCIndex = i;
			            		}
			            		//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
			            		else if("ADD_QTY_ASSY".equalsIgnoreCase(strColIdentifier)){
			            			iAddedQtyIndex = i;
			            		}
								//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
								else if("DESCRIPTION".equalsIgnoreCase(strColIdentifier)){
			            			iDescriptionIndex = i;
			            		}
								//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
								else if("CANCEL_TRAN".equalsIgnoreCase(strColIdentifier)){
			            			iCancelledTRSIndex = i;
			            		}else if("CANCEL_PART".equalsIgnoreCase(strColIdentifier)){
			            			iCancelledPartIndex = i;
			            		}else if("CANCEL_REVISION_LETTER".equalsIgnoreCase(strColIdentifier)){
			            			iCancelledRevIndex = i;
			            		}else if("CANCEL_QTY_ASSY".equalsIgnoreCase(strColIdentifier)){
			            			iCancelledQtyIndex = i;
			            		}else if("DISPOSITION_MATERIAL_CD".equalsIgnoreCase(strColIdentifier)){
			            			iDIIndex = i;
			            		}else if("CCIS_ASSY_COMMENT".equalsIgnoreCase(strColIdentifier)){
			            			iCCISIndex = i;
			            		}else if("BLOCK_SEQ".equalsIgnoreCase(strColIdentifier)){
			            			iBlockSeqIndex = i;
			            		}
			            	}
			            }
						// Table Data
						jaTableData = jsonObjAddnCancel.getJSONArray(JSON_TABLE_DATA);
						// Internal method - To process EC Meta Data_AddnCancel
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
						updateECMetaDataAddnCancel(context, mpChange, jaTableData, iCONameIndex, iBlockIndex, iReferenceFLIndex, iModelsIndex, iIncNotIncECSIndex, iEAPLsIndex, iTextIndex, iUIDIndex, iLevelIndex, iAddedTRSIndex, iAddedPartIndex, iAddedRevIndex, iPRCIndex, iSCIndex, iAddedQtyIndex, iDescriptionIndex, iCancelledTRSIndex, iCancelledPartIndex, iCancelledRevIndex, iCancelledQtyIndex, iDIIndex, iCCISIndex, iBlockSeqIndex);
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
					}
				
					if(null != jsonObjOtherText){
						int iCONameIndex = 0;
						int iTextTypeIndex = 0;
						int iTextIndex = 0;
						jaColumnIdentifiers = jsonObjOtherText.getJSONArray(JSON_COLUMN_IDENTIFIERS);
						if(jaColumnIdentifiers.length() > 0){
			            	for(int i = 0; i < jaColumnIdentifiers.length(); i++){
			            		strColIdentifier = jaColumnIdentifiers.getString(i);
			            		if("DOCUMENT_NO".equalsIgnoreCase(strColIdentifier)){
			            			iCONameIndex = i;
			            		}else if("TEXT_TYPE_CD".equalsIgnoreCase(strColIdentifier)){
			            			iTextTypeIndex = i;
			            		}else if("TEXT".equalsIgnoreCase(strColIdentifier)){
			            			iTextIndex = i;
			            		}
			            	}
			            }
						// Table Data
						jaTableData = jsonObjOtherText.getJSONArray(JSON_TABLE_DATA);
						// Internal method - To process EC Meta Data_OtherText
						updateECMetaDataOtherText(context, mpChange, jaTableData, iCONameIndex, iTextTypeIndex, iTextIndex);
						
					}
					
					// Notifying the update status 'Pass' to CO coordinator
					if(bNotifyIncorporation && bNotifyAddnCancel){
						notifyCOCoOrdinator(context, mpChange, JSON_EC_META_DATA, STATUS_PASS, null);
					}else if(bNotifyIncorporation){
						notifyCOCoOrdinator(context, mpChange, JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_INCORPORATION, STATUS_PASS, null);
					}else if(bNotifyAddnCancel){
						notifyCOCoOrdinator(context, mpChange, JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_ADD_N_CANCEL, STATUS_PASS, null);
					}
					
				}else{
					//notifyCOCoOrdinator(context, mpChange, DomainConstants.EMPTY_STRING, STATUS_CANNOT_PROCESS, strMsg);
				}
			}else{
				if(null != writeIntoFile){
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					_LOGGER.debug("PWCCMAppIntegration : updateECMetaData --> Change Object '"+strChangeName+"' doesn't exists in EV6.");
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				}
				if(!bJsonError){
					notifyCOCoOrdinator(context, null, JSON_EC_META_DATA, STATUS_FAIL, "Change Order '"+strChangeName+"' doesn't exists in EV6.");
				}
			}
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : updateECMetaData --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateECMetaData --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaData :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}finally{
			if(isContextPushed){
				ContextUtil.popContext(context);	
			}
		}	
	}
	
	/**
	* This method is called internally, to update EC Meta Data's Incorporation.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void updateECMetaDataIncorporation(Context context, Map mpCO, JSONArray jaTableData, int iCONameIndex, int iHPIndex, int iIncorporationIndex, int iComplianceIndex, int iSubstantiationIndex, int iComplianceCodeIndex, int iBuildSpecIndex) throws Exception{
		JSONArray jaTableDataInd = null;
		String strHPName = DomainConstants.EMPTY_STRING;
		//START :: Added for HEAT-C-19459 : CMApp leftover items
		String strBuildSpecName = DomainConstants.EMPTY_STRING;
		//END :: Added for HEAT-C-19459 : CMApp leftover items
		MapList mlHP = new MapList();
		Map mpHP = null;
		String strHpId = DomainConstants.EMPTY_STRING;
		DomainObject doHP = null;
		String strWhereClause = DomainConstants.EMPTY_STRING;
		MapList mlEngineModel = new MapList();
		Map mpEngMod = null;
		String strEngModRelId = DomainConstants.EMPTY_STRING;
		DomainRelationship doEngModRel = null;
		String strAttrCOIncorporation = DomainConstants.EMPTY_STRING;
		String strAttrStatementOfCompliance = DomainConstants.EMPTY_STRING;
		String strAttrSubstantiation = DomainConstants.EMPTY_STRING;
		Map mpRelAttr = null;
		String strPushedAttrIncorporation = DomainConstants.EMPTY_STRING;
		String strPushedAttrCompliance = DomainConstants.EMPTY_STRING;
		String strPushedAttrSubstantiation = DomainConstants.EMPTY_STRING;
		String strPushedAttrComplianceCode = DomainConstants.EMPTY_STRING;
		DomainObject doCO = null;
		// boolean bNotify = true;
		try{
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : updateECMetaDataIncorporation --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : updateECMetaDataIncorporation --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateECMetaDataIncorporation --> Table Data Length  " + jaTableData.length() + "\n", writeIntoFile);
			}
			if(jaTableData.length() > 0){
            	try{
	            	// Starting transaction
					ContextUtil.startTransaction(context,true);
					
					String strCOId = (String) mpCO.get(DomainObject.SELECT_ID);
					StringList objectSelects = new StringList();
		            objectSelects.addElement(DomainObject.SELECT_ID);
		            objectSelects.addElement(DomainObject.SELECT_NAME);
		            objectSelects.addElement(DomainObject.SELECT_OWNER);
		            StringList relSelects = new StringList();
					relSelects.addElement(DomainRelationship.SELECT_ID);
					relSelects.addElement(SELECTABLE_ATTR_PWC_CO_INCORPORATION);
					relSelects.addElement(SELECTABLE_ATTR_PWC_STATEMENT_OF_COMPLIANCE);
					relSelects.addElement(SELECTABLE_ATTR_PWC_SUBSTANTIATION);
					
					for(int i = 0; i < jaTableData.length(); i++){
						jaTableDataInd = jaTableData.getJSONArray(i);
						strHPName = jaTableDataInd.getString(iHPIndex);
						//START :: Added for HEAT-C-19459 : CMApp leftover items
						strBuildSpecName = jaTableDataInd.getString(iBuildSpecIndex);
						//END :: Added for HEAT-C-19459 : CMApp leftover items
						if("null" == strHPName){
							strHPName = DomainConstants.EMPTY_STRING;
						}
						//START :: Added for HEAT-C-19459 : CMApp leftover items
						if("null" == strBuildSpecName){
							strBuildSpecName = DomainConstants.EMPTY_STRING;
						}
						if(UIUtil.isNotNullAndNotEmpty(strBuildSpecName)){
							if(UIUtil.isNotNullAndNotEmpty(strHPName)){
								strHPName = strHPName + "-" + strBuildSpecName;
							}
						}
						//END :: Added for HEAT-C-19459 : CMApp leftover items
						// Getting the hardware product
						//START :: Modified for HEAT-C-19459 : CMApp leftover items
						if((null != strHPName && !DomainConstants.EMPTY_STRING.equals(strHPName))){
						   	mlHP = DomainObject.findObjects(context, 
									TYPE_HARDWARE_PRODUCT, 
									strHPName,
									DomainConstants.QUERY_WILDCARD, 
									DomainConstants.QUERY_WILDCARD, 
									DomainConstants.QUERY_WILDCARD, 
									null, 
									false, 
									objectSelects);
						}
					   	/*if(null != writeIntoFile){
		 					writeDataToFile("PWCCMAppIntegration : updateECMetaDataIncorporation --> mlHP -> " + mlHP + "\n", writeIntoFile);
		 				}*/
					   	
					   	if(null != mlHP && mlHP.size() > 0){
				   			mpHP = (Map) mlHP.get(0);
							strHpId = (String) mpHP.get(DomainObject.SELECT_ID);
							doHP = DomainObject.newInstance(context, strHpId);
							strWhereClause = "id == \"" + strCOId + "\"";	
							mlEngineModel = doHP.getRelatedObjects(context, 
													REL_PWC_ENGINE_MODEL, 
													ChangeConstants.TYPE_CHANGE_ORDER, 
													objectSelects, 
													relSelects, 
													true,
													false,
													(short) 1,
													strWhereClause,
													null,
													0);
							/*if(null != writeIntoFile){
			 					writeDataToFile("PWCCMAppIntegration : updateECMetaDataIncorporation --> mlEngineModel -> " + mlEngineModel + "\n", writeIntoFile);
			 				}*/
							if(mlEngineModel.size() > 0)
							{
								mpEngMod = (Map) mlEngineModel.get(0);
								strEngModRelId = (String) mpEngMod.get(DomainRelationship.SELECT_ID);
								doEngModRel = DomainRelationship.newInstance(context, strEngModRelId);
								strAttrCOIncorporation = (String) mpEngMod.get(SELECTABLE_ATTR_PWC_CO_INCORPORATION);
								strAttrStatementOfCompliance = (String) mpEngMod.get(SELECTABLE_ATTR_PWC_STATEMENT_OF_COMPLIANCE);
								strAttrSubstantiation = (String) mpEngMod.get(SELECTABLE_ATTR_PWC_SUBSTANTIATION);
								mpRelAttr = new HashMap();
								strPushedAttrIncorporation = jaTableDataInd.getString(iIncorporationIndex);
								if("null".equals(strPushedAttrIncorporation) && UIUtil.isNullOrEmpty(strAttrCOIncorporation)){
									strPushedAttrIncorporation = DomainConstants.EMPTY_STRING;
									mpRelAttr.put(ATTR_PWC_CO_INCORPORATION, strPushedAttrIncorporation);
								}else if(!"null".equals(strPushedAttrIncorporation) && !strPushedAttrIncorporation.equals(strAttrCOIncorporation)){
									mpRelAttr.put(ATTR_PWC_CO_INCORPORATION, strPushedAttrIncorporation);
								}
								strPushedAttrCompliance =jaTableDataInd.getString(iComplianceIndex);
								if("null".equals(strPushedAttrCompliance)){
									strPushedAttrCompliance = DomainConstants.EMPTY_STRING;
								}
								strPushedAttrComplianceCode =jaTableDataInd.getString(iComplianceCodeIndex);
								if("null".equals(strPushedAttrComplianceCode)){
									strPushedAttrComplianceCode = DomainConstants.EMPTY_STRING;
								}
								if(UIUtil.isNotNullAndNotEmpty(strPushedAttrComplianceCode)){
									strPushedAttrCompliance = (strPushedAttrComplianceCode.length() > 2 ? strPushedAttrComplianceCode.substring(strPushedAttrComplianceCode.length() - 2) : strPushedAttrComplianceCode) + ": " + strPushedAttrCompliance;
								}
								if(UIUtil.isNullOrEmpty(strPushedAttrCompliance) && UIUtil.isNullOrEmpty(strAttrStatementOfCompliance)){
									mpRelAttr.put(ATTR_PWC_STATEMENT_OF_COMPLIANCE, strPushedAttrCompliance.trim());
								}else if(UIUtil.isNotNullAndNotEmpty(strPushedAttrCompliance) && !strPushedAttrCompliance.equals(strAttrStatementOfCompliance)){
									mpRelAttr.put(ATTR_PWC_STATEMENT_OF_COMPLIANCE, strPushedAttrCompliance);
								}
								strPushedAttrSubstantiation =jaTableDataInd.getString(iSubstantiationIndex);
								if("null".equals(strPushedAttrSubstantiation) && UIUtil.isNullOrEmpty(strAttrSubstantiation)){
									strPushedAttrSubstantiation = DomainConstants.EMPTY_STRING;
									mpRelAttr.put(ATTR_PWC_SUBSTANTIATION, strPushedAttrSubstantiation);
								}else if(!"null".equals(strPushedAttrSubstantiation) && !strPushedAttrSubstantiation.equals(strAttrSubstantiation)){
									mpRelAttr.put(ATTR_PWC_SUBSTANTIATION, strPushedAttrSubstantiation);
								}
								// updating rel attributes
								doEngModRel.open(context);
								doEngModRel.setAttributeValues(context, mpRelAttr);
								doEngModRel.close(context);
							}
							else
							{
								doCO = DomainObject.newInstance(context, strCOId);
								doEngModRel=DomainRelationship.connect(context, doCO, REL_PWC_ENGINE_MODEL, doHP);
								mpRelAttr = new HashMap();
								strPushedAttrIncorporation = jaTableDataInd.getString(iIncorporationIndex);
								if("null".equals(strPushedAttrIncorporation)){
									strPushedAttrIncorporation = DomainConstants.EMPTY_STRING;
								} 
								strPushedAttrCompliance =jaTableDataInd.getString(iComplianceIndex);
								if("null".equals(strPushedAttrCompliance)){
									strPushedAttrCompliance = DomainConstants.EMPTY_STRING;
								}
								
								strPushedAttrComplianceCode =jaTableDataInd.getString(iComplianceCodeIndex);
								if("null".equals(strPushedAttrComplianceCode)){
									strPushedAttrComplianceCode = DomainConstants.EMPTY_STRING;
								}
								if(UIUtil.isNotNullAndNotEmpty(strPushedAttrComplianceCode)){
									strPushedAttrCompliance = (strPushedAttrComplianceCode.length() > 2 ? strPushedAttrComplianceCode.substring(strPushedAttrComplianceCode.length() - 2) : strPushedAttrComplianceCode) + ": " + strPushedAttrCompliance;
								}
																
								strPushedAttrSubstantiation =jaTableDataInd.getString(iSubstantiationIndex);
								if("null".equals(strPushedAttrSubstantiation)){
									strPushedAttrSubstantiation = DomainConstants.EMPTY_STRING;
								} 
								mpRelAttr.put(ATTR_PWC_CO_INCORPORATION, strPushedAttrIncorporation);
								mpRelAttr.put(ATTR_PWC_STATEMENT_OF_COMPLIANCE, strPushedAttrCompliance);
								mpRelAttr.put(ATTR_PWC_SUBSTANTIATION, strPushedAttrSubstantiation);
								
								// Establishing new connection and updating rel attributes
								doEngModRel.open(context);
								doEngModRel.setAttributeValues(context, mpRelAttr);
								doEngModRel.close(context);
					  		}
					   		//END :: Modified for HEAT-C-19459 : CMApp leftover items
					  }else{
						  // bNotify = false;
						  if(null != writeIntoFile){
							  //START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
 	   	 					_LOGGER.debug("PWCCMAppIntegration : updateECMetaDataIncorporation --> No such HP in EV6 ");
							//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
   	 				      }
					  	// notifyCOCoOrdinator(context, mpCO, JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_INCORPORATION, STATUS_FAIL, MESSAGE_NO_SUCH_HP);
					  }
				  }

	 	          // Committing transaction
	 	          ContextUtil.commitTransaction(context);
	 	           	
	 	          // Notifying the update status 'Pass' to CO coordinator
	 	          /*if(bNotify){
	 	        	  notifyCOCoOrdinator(context, mpCO, JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_INCORPORATION, STATUS_PASS, null);
	 	          }*/
	 	         
            	}catch(Exception ex){
            		ex.printStackTrace();
            		if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        				_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaDataIncorporation --> Notifying the update status as FAIL :-"+ex.getMessage());
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        			}
            		bNotifyIncorporation = false;
            		// Notifying the update status 'Fail' to CO coordinator
        			notifyCOCoOrdinator(context, mpCO, JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_INCORPORATION, STATUS_FAIL, ex.getMessage());
            		
            		// Aborting transaction
        			ContextUtil.abortTransaction(context);
            	}
            	
             }else{
            	 if(null != writeIntoFile){
				 	//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
      				_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaDataIncorporation --> No Connections Provided :-");
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
      			 }
				// notifyCOCoOrdinator(context, mpCO, JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_INCORPORATION, STATUS_FAIL, MESSAGE_NO_DATA_PROVIDED);
             }
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : updateECMetaDataIncorporation --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateECMetaDataIncorporation --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaDataIncorporation :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}	
	}
	
	/**
	* This method is called internally, to update EC Meta Data's Incorporation.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
	public void updateECMetaDataAddnCancel(Context context, Map mpCO, JSONArray jaTableData, int iCONameIndex, int iBlockIndex, int iReferenceFLIndex, int iModelsIndex, int iIncNotIncECSIndex, int iEAPLsIndex, int iTextIndex, int iUIDIndex, int iLevelIndex, int iAddedTRSIndex, int iAddedPartIndex, int iAddedRevIndex, int iPRCIndex, int iSCIndex, int iAddedQtyIndex, int iDescriptionIndex, int iCancelledTRSIndex, int iCancelledPartIndex, int iCancelledRevIndex, int iCancelledQtyIndex, int iDIIndex, int iCCISIndex, int iBlockSeqIndex) throws Exception{
		//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
		JSONObject joAddnCancel = new JSONObject();
		JSONArray jaTableDataInd = new JSONArray();
		JSONArray jaTop = new JSONArray();
		JSONArray jaTopInd = null;
		JSONArray jaAfterTop = new JSONArray();
		JSONArray jaAfterTopInd = null;
		TreeMap tmTopBlockCode = new TreeMap();
		StringList slAfterTopBlockCode = new StringList();
		String strBlockSeqNum = DomainConstants.EMPTY_STRING;
		String strBlock = DomainConstants.EMPTY_STRING;
		String strKey = DomainConstants.EMPTY_STRING;
		String strValue = DomainConstants.EMPTY_STRING;
		JSONArray jaBlock = null;
		JSONArray jaBlockInt = null;
		ArrayList alBlockCode = new ArrayList();
		try
		{
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : updateECMetaDataAddnCancel --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : updateECMetaDataAddnCancel --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateECMetaDataAddnCancel --> Table Data Length  " + jaTableData.length() + "\n", writeIntoFile);
			}
			if(jaTableData.length() > 0){
				try{
					// Building top blocks & after-top blocks
            		for(int i = 0; i < jaTableData.length(); i++){
						jaTableDataInd = jaTableData.getJSONArray(i);
						strBlockSeqNum = jaTableDataInd.getString(iBlockSeqIndex);
						strBlock = jaTableDataInd.getString(iBlockIndex);
						if(!"null".equals(strBlockSeqNum)){
							jaTop.put(jaTableDataInd);
							if(tmTopBlockCode.containsKey(Integer.parseInt(strBlockSeqNum))){
								tmTopBlockCode.put(Integer.parseInt(strBlockSeqNum), (String) tmTopBlockCode.get(Integer.parseInt(strBlockSeqNum)) + "+" + strBlock);
							}else{
								tmTopBlockCode.put(Integer.parseInt(strBlockSeqNum), strBlock);
							}
						}else{
							jaAfterTop.put(jaTableDataInd);
							if(!slAfterTopBlockCode.contains(strBlock)){
								slAfterTopBlockCode.add(strBlock);
							}
						}
					}

            		// Building the json object on block code base of top blocks
            		if(null != tmTopBlockCode){
            			Iterator itr = tmTopBlockCode.entrySet().iterator();
            			StringList slBlockList = new StringList();
            			StringList slUniqueBlockList = new StringList();
        		        while (itr.hasNext()){
        		        	slUniqueBlockList = new StringList();
        		        	Entry entry = (Entry) itr.next();
        		            strBlock =(String) entry.getValue();
        		            if(strBlock.contains("+")){
        		            	slBlockList = new StringList();
        		            	slBlockList = FrameworkUtil.split(strBlock, "+");
        		            	for(int i = 0; i < slBlockList.size(); i++){
        		            		String strBlockTemp = (String) slBlockList.get(i);
        		            		if(!slUniqueBlockList.contains(strBlockTemp)){
        		            			slUniqueBlockList.add(strBlockTemp);
        		            		}
        		            	}
        		            	for(int j = 0; j < slUniqueBlockList.size(); j++){
        		            		strBlock = (String) slUniqueBlockList.get(j);
        		            		jaBlock = new JSONArray();
        		            		for(int i = 0; i < jaTop.length(); i++){
    	        		            	jaBlockInt = new JSONArray();
    	        		            	jaTopInd = jaTop.getJSONArray(i);
    	        		            	if(strBlock.equals(jaTopInd.getString(iBlockIndex))){
    	        		            		jaBlockInt = jaTopInd;
    	        		            		jaBlock.put(jaBlockInt);
    	        		            	}
    	        		            }
    	        		            joAddnCancel.put(strBlock, jaBlock);
    	        		            alBlockCode.add(strBlock);
        		            	}
        		            }else{
        		            	jaBlock = new JSONArray();
	        		            for(int i = 0; i < jaTop.length(); i++){
	        		            	jaBlockInt = new JSONArray();
	        		            	jaTopInd = jaTop.getJSONArray(i);
	        		            	if(strBlock.equals(jaTopInd.getString(iBlockIndex))){
	        		            		jaBlockInt = jaTopInd;
	        		            		jaBlock.put(jaBlockInt);
	        		            	}
	        		            }
	        		            joAddnCancel.put(strBlock, jaBlock);
	        		            alBlockCode.add(strBlock);
        		            }
        		        }
            		}
            		
            		// Building the json object on block code base of after-top blocks
            		if(slAfterTopBlockCode.size() > 0){
            			for(int i = 0; i < slAfterTopBlockCode.size(); i++){
            				jaBlock = new JSONArray();
            				strBlock =(String) slAfterTopBlockCode.get(i);
            				for(int j = 0; j < jaAfterTop.length(); j++){
	       		            	 jaBlockInt = new JSONArray();
	       		            	 jaAfterTopInd = jaAfterTop.getJSONArray(j);
	       		            	 if(strBlock.equals(jaAfterTopInd.getString(iBlockIndex))){
	       		            		 jaBlockInt = jaAfterTopInd;
	       		            		 jaBlock.put(jaBlockInt);
	       		            	 }
       		             	}
       		             	joAddnCancel.put(strBlock, jaBlock);
       		             	alBlockCode.add(strBlock);
       		             }
            		}
            		
            		/*if(null != writeIntoFile){
	 					writeDataToFile("PWCCMAppIntegration : updateECMetaDataAddnCancel --> alBlockCode -> " + alBlockCode + "\n", writeIntoFile);
	 				}*/

            		String strCOName = (String) mpCO.get(DomainObject.SELECT_NAME);
            		String strFileName = strCOName + "_AddnCancel.xlsx";
            		Workbook workbook = new XSSFWorkbook();
            		Sheet sheet = null;
            		Row row = null;
            		Cell cell = null;
            		int rowIndex = 0;
            		String strText = DomainConstants.EMPTY_STRING;
            		String strModels = DomainConstants.EMPTY_STRING;
            		String strIncNotIncECS = DomainConstants.EMPTY_STRING;
            		String strReferenceFL = DomainConstants.EMPTY_STRING;
  				  	String strEAPLs = DomainConstants.EMPTY_STRING;
  				  	String strUID = DomainConstants.EMPTY_STRING;
  				  	String strLevel = DomainConstants.EMPTY_STRING;
  				  	String strAddedTRS = DomainConstants.EMPTY_STRING;
  				  	String strAddedParts = DomainConstants.EMPTY_STRING;
  				  	String strAddedRev = DomainConstants.EMPTY_STRING;
  				  	String strPRC = DomainConstants.EMPTY_STRING;
  				  	//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
  				  	String strSC = DomainConstants.EMPTY_STRING;
  				  	//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
  				  	String strAddedQTY = DomainConstants.EMPTY_STRING;
  				  	//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
  				  	String strAddedDesc = DomainConstants.EMPTY_STRING;
  				  	//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
  				  	String strCancelledTRS = DomainConstants.EMPTY_STRING;
  				  	String strCancelledParts = DomainConstants.EMPTY_STRING;
  				  	String strCancelledRev = DomainConstants.EMPTY_STRING;
  				  	String strCancelledQty = DomainConstants.EMPTY_STRING;
  				  	String strCancelledDesc = DomainConstants.EMPTY_STRING;
  				  	String strDI = DomainConstants.EMPTY_STRING;
  				  	String strCCISIndex = DomainConstants.EMPTY_STRING;
            		CellStyle cellStyle = null;
            		XSSFFont font = null;
            		StringList slDI = new StringList();
            		StringList slPRC = new StringList();
            		StringList slSC = new StringList();
            		DomainObject doCO = null;
					StringList objSels = new StringList();
					objSels.addElement(DomainConstants.SELECT_ID);
					StringList relSelects = new SelectList();
					relSelects.addElement(DomainRelationship.SELECT_ID);
					String strWhereClause = DomainConstants.EMPTY_STRING;
 					MapList mlGenDocList = new MapList();
 					Map mpGenDoc = null;
 					String strGenDocId = DomainConstants.EMPTY_STRING;
     				StringList slAffectedEAPLs = new StringList();
            		StringList slReferenceEAPLs = new StringList();
            		StringList slEAPLs = new StringList();
            		String strEAPL = DomainConstants.EMPTY_STRING;
            		StringList slNotNeededTRS = new StringList();
            		if(UIUtil.isNotNullAndNotEmpty(STR_TRS_NOT_TO_BE_CONSIDERED)){
            			slNotNeededTRS = FrameworkUtil.split(STR_TRS_NOT_TO_BE_CONSIDERED, ",");
            		}
            		
            		// Iterating over each block code & writing into excel accordingly
            		if(alBlockCode.size() > 0){
            			sheet = workbook.createSheet("Invisible Sheet 1");
            			sheet = workbook.createSheet("Invisible Sheet 2");
            			sheet = workbook.createSheet("Add - Cancel Report (EV6)");
            			sheet.setColumnWidth(0, 4000);
            			sheet.setColumnWidth(1, 1800);
            			sheet.setColumnWidth(2, 1200);
            			sheet.setColumnWidth(3, 4000);
            			sheet.setColumnWidth(4, 1200);
            			sheet.setColumnWidth(5, 1200);
            			sheet.setColumnWidth(6, 1200);
            			sheet.setColumnWidth(7, 1200);
            			sheet.setColumnWidth(8, 4000);
            			sheet.setColumnWidth(9, 1200);
            			sheet.setColumnWidth(10, 5000);
            			sheet.setColumnWidth(11, 1200);
            			sheet.setColumnWidth(12, 1200);
            			sheet.setColumnWidth(13, 4000);
            			sheet.setColumnWidth(14, 1200);
            			workbook.setSheetHidden(0, true);
            			workbook.setSheetHidden(1, true);
            			workbook.setActiveSheet(2);
            			StringList slAddnCancelHeader = new StringList();
            			String strAddnCancelHeader = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.AddnCancel.Row.Header");
            			if(null != strAddnCancelHeader && !DomainConstants.EMPTY_STRING.equals(strAddnCancelHeader)){
            				slAddnCancelHeader = FrameworkUtil.split(strAddnCancelHeader, ",");
            			}
            			Iterator itr=alBlockCode.iterator();
            			while(itr.hasNext()){
            				  jaBlock = new JSONArray();
            				  strBlock = (String) itr.next();
            				  jaBlock = joAddnCancel.getJSONArray(strBlock);
            				  jaBlockInt = jaBlock.getJSONArray(0);
            				  row = sheet.createRow(rowIndex++);
            				  cell = row.createCell(0);
            				  sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 14));
            				  cellStyle = workbook.createCellStyle();
            				  cellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            				  cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            				  font = (XSSFFont) workbook.createFont();
            				  font.setColor(IndexedColors.WHITE.getIndex());
            				  font.setBold(true);
            				  cellStyle.setFont(font);
            				  cellStyle.setWrapText(true);
            				  cell.setCellStyle(cellStyle);
            				  strText = jaBlockInt.getString(iTextIndex);
            				  if("null" == strText){
            					  strText = DomainConstants.EMPTY_STRING;
        					  }
            				  strModels = jaBlockInt.getString(iModelsIndex);
            				  if("null" == strModels){
            					  strModels = DomainConstants.EMPTY_STRING;
        					  }
            				  if(UIUtil.isNotNullAndNotEmpty(strModels)){
            					  if(strModels.contains(",")){
            						  strModels = strModels.replace(",", ", ");
            					  }
            				  }
            				  if(!DomainConstants.EMPTY_STRING.equals(strText)){
            					  strModels = strText + "\n" + strModels;
            				  }
            				  if(strModels.contains("\t")){
            					  strModels = strModels.replace("\t", ": ");
            				  }
            				  cell.setCellValue(strModels);
            				  
            				  row = sheet.createRow(rowIndex++);
            				  cell = row.createCell(0);
            				  sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 14));
            				  cellStyle = workbook.createCellStyle();
            				  cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());	
            				  cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            				  font = (XSSFFont) workbook.createFont();
            				  font.setItalic(true);
            				  cellStyle.setFont(font);
            				  cellStyle.setWrapText(true);
            				  cell.setCellStyle(cellStyle);
            				  strIncNotIncECS = jaBlockInt.getString(iIncNotIncECSIndex);
            				  if("null" == strIncNotIncECS){
            					  strIncNotIncECS = DomainConstants.EMPTY_STRING;
        					  }
            				  cell.setCellValue(strIncNotIncECS);
            				  
            				  cellStyle = workbook.createCellStyle();
            				  cellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            				  cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            				  font = (XSSFFont) workbook.createFont();
            				  font.setColor(IndexedColors.WHITE.getIndex());
            				  font.setBold(true);
            				  cellStyle.setFont(font);
            				  cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            				  row = sheet.createRow(rowIndex++);
            				  for(int i = 0; i < slAddnCancelHeader.size(); i++){
            					  cell = row.createCell(i);
                				  cell.setCellStyle(cellStyle);
                				  cell.setCellValue((String) slAddnCancelHeader.get(i));
            				  }
            				  cellStyle = workbook.createCellStyle();
            				  cellStyle.setBorderRight(CellStyle.BORDER_THIN);
            				  cellStyle.setRightBorderColor(IndexedColors.BLUE_GREY.getIndex());
            				  cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            				  for(int i = 0; i < jaBlock.length(); i++){
								  Map mpPartDetails = new HashMap();
            					  jaBlockInt = jaBlock.getJSONArray(i);
            					  row = sheet.createRow(rowIndex++);
            					  if(i == jaBlock.length() - 1){
            						  cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
                    				  cellStyle.setBottomBorderColor(IndexedColors.BLUE_GREY.getIndex());
            					  }
            					  cell = row.createCell(0);
            					  cell.setCellStyle(cellStyle);
            					  strUID = jaBlockInt.getString(iUIDIndex);
            					  if("null" == strUID){
            						  strUID = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strUID);
            					  cell = row.createCell(1);
            					  cell.setCellStyle(cellStyle);
            					  strLevel = jaBlockInt.getString(iLevelIndex);
            					  if("null" == strLevel){
            						  strLevel = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strLevel);
            					  cell = row.createCell(2);
            					  cell.setCellStyle(cellStyle);
            					  strAddedTRS = jaBlockInt.getString(iAddedTRSIndex).trim();
            					  if("null" == strAddedTRS){
            						  strAddedTRS = DomainConstants.EMPTY_STRING;
            					  }else if(slNotNeededTRS.contains(strAddedTRS)){
            						  strAddedTRS = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strAddedTRS);
            					  cell = row.createCell(3);
            					  cell.setCellStyle(cellStyle);
            					  strAddedParts = jaBlockInt.getString(iAddedPartIndex);
            					  if("null" == strAddedParts){
            						  strAddedParts = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strAddedParts);
								  //START :: Added for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
								  if(UIUtil.isNotNullAndNotEmpty(strAddedParts)){
									  mpPartDetails = getSaleabilityCodeAndDescription(context, strAddedParts);
								  }
								  //END :: Added for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel

            					  cell = row.createCell(4);
            					  cell.setCellStyle(cellStyle);
            					  strAddedRev = jaBlockInt.getString(iAddedRevIndex);
            					  if("null" == strAddedRev){
            						  strAddedRev = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strAddedRev);
            					  cell = row.createCell(5);
            					  cell.setCellStyle(cellStyle);
            					  strPRC = jaBlockInt.getString(iPRCIndex);
            					  if("null" == strPRC){
            						  strPRC = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strPRC);
            					  cell = row.createCell(6);
            					  cell.setCellStyle(cellStyle);
            					  //START :: Added for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
								  if(null != mpPartDetails){
									  strSC = (String) mpPartDetails.get(SELECTABLE_ATTR_PWC_SALE_CODE);
								  }
            					  //END :: Added for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
            					  if("null" == strSC){
            						  strSC = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strSC);
            					  cell = row.createCell(7);
            					  cell.setCellStyle(cellStyle);
            					  strAddedQTY = jaBlockInt.getString(iAddedQtyIndex);
            					  if("null" == strAddedQTY){
            						  strAddedQTY = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strAddedQTY);
            					  cell = row.createCell(8);
            					  cell.setCellStyle(cellStyle);
								  //START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
            					  // Modified_For_Heat-C-19626 - START
            					  strAddedDesc = jaBlockInt.getString(iDescriptionIndex);
            					  if("null" == strAddedDesc){
            						  strAddedDesc = DomainConstants.EMPTY_STRING;
            					  }
            					  strCCISIndex = jaBlockInt.getString(iCCISIndex);
            					  if("null" == strCCISIndex){
            						  strCCISIndex = DomainConstants.EMPTY_STRING;
            					  }
								  if (UIUtil.isNotNullAndNotEmpty(strAddedParts)){
									  if(null != mpPartDetails && mpPartDetails.size() > 0){
										  strAddedDesc = (String) mpPartDetails.get(DomainConstants.SELECT_DESCRIPTION);
									  }
									  //END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
	            					  /*if("null" == strAddedDesc){
	            						  strAddedDesc = DomainConstants.EMPTY_STRING;
	            					  }*/
	            					  if(UIUtil.isNotNullAndNotEmpty(strAddedDesc)){
	            						  if(UIUtil.isNotNullAndNotEmpty(strCCISIndex)){
	            							  strAddedDesc = strAddedDesc + "\n" + strCCISIndex;
	            						  }
	            					  }else if(UIUtil.isNotNullAndNotEmpty(strCCISIndex)){
	            						  strAddedDesc = strCCISIndex;
	                				  }else{
	                					  strAddedDesc = DomainConstants.EMPTY_STRING;
	                				  }
            				  	  }else{
            				  		  if(UIUtil.isNotNullAndNotEmpty(strAddedDesc)){
            				  			  if(UIUtil.isNotNullAndNotEmpty(strCCISIndex)){
	            							  strAddedDesc = strAddedDesc + "\n" + strCCISIndex;
	            						  }
	            					  }else if(UIUtil.isNotNullAndNotEmpty(strCCISIndex)){
	            						  strAddedDesc = strCCISIndex;
	                				  }else{
	                					  strAddedDesc = DomainConstants.EMPTY_STRING;
	                				  }
            				  	  }
								  // Modified_For_Heat-C-19626 - END
            					  cell.setCellValue(strAddedDesc);
            					  cell = row.createCell(9);
            					  cell.setCellStyle(cellStyle);
            					  strCancelledTRS = jaBlockInt.getString(iCancelledTRSIndex).trim();
            					  if("null" == strCancelledTRS){
            						  strCancelledTRS = DomainConstants.EMPTY_STRING;
            					  }else if(slNotNeededTRS.contains(strCancelledTRS)){
            						  strCancelledTRS = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strCancelledTRS);
            					  cell = row.createCell(10);
            					  cell.setCellStyle(cellStyle);
            					  strCancelledParts = jaBlockInt.getString(iCancelledPartIndex);
            					  if("null" == strCancelledParts){
            						  strCancelledParts = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strCancelledParts);
            					  cell = row.createCell(11);
            					  cell.setCellStyle(cellStyle);
            					  strCancelledRev = jaBlockInt.getString(iCancelledRevIndex);
            					  if("null" == strCancelledRev){
            						  strCancelledRev = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strCancelledRev);
            					  cell = row.createCell(12);
            					  cell.setCellStyle(cellStyle);
            					  strCancelledQty = jaBlockInt.getString(iCancelledQtyIndex);
            					  if("null" == strCancelledQty){
            						  strCancelledQty = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strCancelledQty);
            					  cell = row.createCell(13);
            					  cell.setCellStyle(cellStyle);
								  //START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
            					  strCancelledDesc = jaBlockInt.getString(iDescriptionIndex);
            					  if("null" == strCancelledDesc){
            						  strCancelledDesc = DomainConstants.EMPTY_STRING;
            					  }
								  if(UIUtil.isNotNullAndNotEmpty(strCancelledParts)){
									  mpPartDetails = getSaleabilityCodeAndDescription(context, strCancelledParts);
									if(null != mpPartDetails  && mpPartDetails.size() > 0){
									  strCancelledDesc = (String) mpPartDetails.get(DomainConstants.SELECT_DESCRIPTION);
									}
									// Modified_For_Heat-C-19626 - START
									if(UIUtil.isNotNullAndNotEmpty(strCancelledDesc)){
										if(UIUtil.isNotNullAndNotEmpty(strCCISIndex)){
											strCancelledDesc = strCancelledDesc + "\n" + strCCISIndex;
	            						}
	            					}else if(UIUtil.isNotNullAndNotEmpty(strCCISIndex)){
	            						strCancelledDesc = strCCISIndex;
	            					}else{
	            						strCancelledDesc = DomainConstants.EMPTY_STRING;
	            					}
								  }else{
									  if(UIUtil.isNotNullAndNotEmpty(strCancelledDesc)){
	            						  if(UIUtil.isNotNullAndNotEmpty(strCCISIndex)){
	            							  strCancelledDesc = strCancelledDesc + "\n" + strCCISIndex;
	            						  }
	            					  }else if(UIUtil.isNotNullAndNotEmpty(strCCISIndex)){
	            						  strCancelledDesc = strCCISIndex;
	                				  }else{
	                					  strCancelledDesc = DomainConstants.EMPTY_STRING;
	                				  }
									// Modified_For_Heat-C-19626 - END
								  }
            					  //strCancelledDesc = strAddedDesc;
								  //END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
            					  /*if("null" == strCancelledDesc){
            						  strCancelledDesc = DomainConstants.EMPTY_STRING;
            					  }*/
            					  
            					  cell.setCellValue(strCancelledDesc);
            					  cell = row.createCell(14);
            					  cell.setCellStyle(cellStyle);
            					  strDI = jaBlockInt.getString(iDIIndex);
            					  if("null" == strDI){
            						  strDI = DomainConstants.EMPTY_STRING;
            					  }
            					  cell.setCellValue(strDI);
            					  
            					  if(strDI != null && !DomainConstants.EMPTY_STRING.equals(strDI) && !slDI.contains(strDI)){
            						  slDI.add(strDI);
            					  }
            					  if(strPRC != null && !DomainConstants.EMPTY_STRING.equals(strPRC) && !slPRC.contains(strPRC)){
            						  slPRC.add(strPRC);
            					  }
            					  if(strSC != null && !DomainConstants.EMPTY_STRING.equals(strSC) && !slSC.contains(strSC)){
            						  slSC.add(strSC);
            					  }
            					  
            				  }

            				  row = sheet.createRow(rowIndex++);
            				  sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 14));
            				  row = sheet.createRow(rowIndex++);
            				  cell = row.createCell(0);
            				  cellStyle = workbook.createCellStyle();
            				  cellStyle.setWrapText(true);
            				  cell.setCellStyle(cellStyle);
            				  sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 14));
            				  strReferenceFL = jaBlockInt.getString(iReferenceFLIndex);
            				  strEAPLs = jaBlockInt.getString(iEAPLsIndex);
            				  if("null" == strEAPLs){
            					  strEAPLs = DomainConstants.EMPTY_STRING;
        					  }
            				  if(UIUtil.isNotNullAndNotEmpty(strEAPLs)){
        						  if(strEAPLs.contains(",")){
        							  strEAPLs = strEAPLs.replace(",", ", ");
        						  }
            				  }
            				  if("N".equalsIgnoreCase(strReferenceFL)){
            					  if(null != strEAPLs && !DomainConstants.EMPTY_STRING.equals(strEAPLs)){
            						  slEAPLs =	FrameworkUtil.split(strEAPLs, ",");
            						  for(int i = 0; i < slEAPLs.size(); i++){
            							  strEAPL = (String) slEAPLs.get(i);
            							  strEAPL = strEAPL.trim();
            							  if(!slAffectedEAPLs.contains(strEAPL)){
            								  slAffectedEAPLs.add(strEAPL);
            							  }
            						  }
            					  }
								  //START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 01 - Remove 'Affected/Reference EAPL' if the value is empty for a block
            					  if(UIUtil.isNotNullAndNotEmpty(strEAPLs)){
            						  strEAPLs = "EAPL Affected: \n" + strEAPLs;
            					  }
								  //END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 01 - Remove 'Affected/Reference EAPL' if the value is empty for a block
            				  }else if("Y".equalsIgnoreCase(strReferenceFL)){
            					  if(null != strEAPLs && !DomainConstants.EMPTY_STRING.equals(strEAPLs)){
            						  slEAPLs =	FrameworkUtil.split(strEAPLs, ",");
            						  for(int i = 0; i < slEAPLs.size(); i++){
            							  strEAPL = (String) slEAPLs.get(i);
            							  strEAPL = strEAPL.trim();
            							  if(!slReferenceEAPLs.contains(strEAPL)){
            								  slReferenceEAPLs.add(strEAPL);
            							  }
            						  }
            					  }
								  //START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 01 - Remove 'Affected/Reference EAPL' if the value is empty for a block
            					  if(UIUtil.isNotNullAndNotEmpty(strEAPLs)){
            						  strEAPLs = "Reference EAPL: \n" + strEAPLs;
            					  }
								  //END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 01 - Remove 'Affected/Reference EAPL' if the value is empty for a block
            				  }
            				  cell.setCellValue(strEAPLs);
            				  row = sheet.createRow(rowIndex++);
            				  sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 14));
            			  }
            			  
            			  // Writing comments sheet
            			  slDI.sort();
        				  slPRC.sort();
        				  slSC.sort();
        				  rowIndex = 0;
            			  sheet = workbook.createSheet("Comments");
            			  sheet.setColumnWidth(0, 30000);
            			  row = sheet.createRow(rowIndex++);
        				  cell = row.createCell(0);
        				  cellStyle = workbook.createCellStyle();
        				  cellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        				  cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        				  font = (XSSFFont) workbook.createFont();
        				  font.setColor(IndexedColors.WHITE.getIndex());
        				  font.setBold(true);
        				  cellStyle.setFont(font);
        				  cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        				  cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        				  cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        				  cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        				  cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        				  cell.setCellStyle(cellStyle);
        				  cell.setCellValue(EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.AddnCancel.Comments.Header"));
            			  
        				  cellStyle = workbook.createCellStyle();
        				  cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        				  cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        				  cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        				  cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        				  cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        				  
        				  if(slDI.size() > 0){
	        				  for(int i = 0; i < slDI.size(); i++){
	            				  strDI = (String) slDI.get(i);
	            				  row = sheet.createRow(rowIndex++);
	            				  cell = row.createCell(0);
	            				  cell.setCellStyle(cellStyle);            				  
	            				  cell.setCellValue(EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.AddnCancel.CommentsDI."+strDI));	            				  
	            				  row = sheet.createRow(rowIndex++);
	            				  cell = row.createCell(0);
	            				  cell.setCellStyle(cellStyle);
	            			  }
        				  }
        				  
        				  if(slPRC.size() > 0){
	        				  for(int i = 0; i < slPRC.size(); i++){
	            				  strPRC = (String) slPRC.get(i);
	            				  row = sheet.createRow(rowIndex++);
	            				  cell = row.createCell(0);
	            				  cell.setCellStyle(cellStyle);
	            				  cell.setCellValue(EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.AddnCancel.CommentsPRC."+strPRC));
	            				  row = sheet.createRow(rowIndex++);
	            				  cell = row.createCell(0);
	            				  cell.setCellStyle(cellStyle);
	            			  }
        				  }
        				  
        				  if(slSC.size() > 0){
	        				  for(int i = 0; i < slSC.size(); i++){
	            				  strSC = (String) slSC.get(i);
	            				  row = sheet.createRow(rowIndex++);
	            				  cell = row.createCell(0);
	            				  cell.setCellStyle(cellStyle);
	            				  cell.setCellValue(EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.AddnCancel.CommentsSC."+strSC));
	            				  row = sheet.createRow(rowIndex++);
	            				  cell = row.createCell(0);
	            				  cell.setCellStyle(cellStyle);
	            			  }
        				  }
        			
        				  String strWSPath = context.createWorkspace();
      					  String strFilePath = strWSPath + "/";
      					  FileOutputStream fos = new FileOutputStream(strFilePath + strFileName);
        				  workbook.write(fos);
        				  fos.close();
        				  
        				  // Starting transaction
      					  ContextUtil.startTransaction(context,true);
      					
      					  // Check-in
      					  String strCOId = (String) mpCO.get(DomainObject.SELECT_ID);
	      				  StringList objGenDocSels = new StringList();
	      				  matrix.db.File boFile = null;
						  String strCheckedInFile = DomainConstants.EMPTY_STRING;
						  String strStore = "STORE";
						  String strFalse = "false";
						  String strServer = "server";
						  objGenDocSels.addElement(DomainConstants.SELECT_ID);
      					  if(null != strCOId && !DomainConstants.EMPTY_STRING.equals(strCOId))
      					  {
							  doCO = DomainObject.newInstance(context, strCOId);
							  strWhereClause = "attribute[" + ATTRIBUTE_PWC_GENERAL_COLLECTION + "] == '" + STR_ADD_N_CANCEL + "'";
							  mlGenDocList = doCO.getRelatedObjects(context, 
							   		RELATIONSHIP_REFERENCE_DOCUMENT, 
							   		TYPE_PWC_GENERAL_DOCUMENT, 
							   		objGenDocSels, 
							   		relSelects, 
							   		false, 
							   		true, 
							   		(short) 1, 
							   		strWhereClause, 
							   		null, 
							   		0);
							  /*if (null != writeIntoFile)
							  {
		      	 					writeDataToFile("PWCCMAppIntegration : updateECMetaDataAddnCancel --> mlGenDocList -> " + mlGenDocList + "\n", writeIntoFile);
							  }*/
							  
							  if (mlGenDocList.size() > 0)
							  {
								  mpGenDoc = (Map) mlGenDocList.get(0);
								  strGenDocId = (String) mpGenDoc.get(DomainConstants.SELECT_ID);
								  String FORMAT_VIEWABLE= "Viewable";
								  FileList fileNameList = new FileList();
								  boolean isGeneric = false;
								  if (UIUtil.isNotNullAndNotEmpty(strGenDocId))
								  {
									  CommonDocument comDocument = new CommonDocument(strGenDocId);
									  fileNameList  = comDocument.getFiles(context, FORMAT_VIEWABLE);
									  if(null == fileNameList || fileNameList.size()==0)
									  {
										  fileNameList  = comDocument.getFiles(context, DomainConstants.FORMAT_GENERIC);
										  if(fileNameList.size()>0)
										  {
											  isGeneric = true;
										  }
									  }
									  Iterator fileItr = fileNameList.iterator();
									  while (fileItr.hasNext())
									  {
										  boFile = (matrix.db.File) fileItr.next();
										  if (null != boFile)
										  {
											  strCheckedInFile = boFile.getName();
											  if (UIUtil.isNotNullAndNotEmpty(strCheckedInFile) && isGeneric)
											  {
												  if (strCheckedInFile.equalsIgnoreCase(strFileName))
												  {
													  comDocument.deleteFile(context, strCheckedInFile, DomainConstants.FORMAT_GENERIC);
												  }
											  }
											  else if (UIUtil.isNotNullAndNotEmpty(strCheckedInFile) && !isGeneric)
											  {
												  if (strCheckedInFile.equalsIgnoreCase(strFileName))
												  {
													   comDocument.deleteFile(context, strCheckedInFile,FORMAT_VIEWABLE);
												  }
											  }
										  }
									  }
									  String[] args = new String[10];
									  args[0] = strGenDocId;
									  args[1] = strFilePath;
									  args[2] = strFileName;
									  args[3] = FORMAT_VIEWABLE;
									  args[4] = strStore;
									  args[5] = strFalse;
									  args[6] = strServer;
									  args[7] = DomainConstants.EMPTY_STRING;
									  JPO.invoke(context, "emxCommonDocument", null, "checkinBus", args);
								  }
							  }
      					  }
      					  
      					  /*if (null != writeIntoFile)
      					  {
      	 					writeDataToFile("PWCCMAppIntegration : updateECMetaDataAddnCancel --> slAffectedEAPLs -> " + slAffectedEAPLs + "\n", writeIntoFile);
      	 					writeDataToFile("PWCCMAppIntegration : updateECMetaDataAddnCancel --> slReferenceEAPLs -> " + slReferenceEAPLs + "\n", writeIntoFile);
            			  }*/
      					  
      					  // Updating Affected/Reference EAPLs
      					  int iAffEAPLsSize = slAffectedEAPLs.size();
    					  int iRefEAPLsSize = slReferenceEAPLs.size();
    					  if (iAffEAPLsSize > 0 || iRefEAPLsSize > 0){
    						  updateAffectednReferenceEAPLs(context, doCO, slAffectedEAPLs, slReferenceEAPLs, mpCO);
    					  }
    					  
      					  // Committing transaction
      					  ContextUtil.commitTransaction(context);
      		 	           	
      					  // Notifying the update status 'Pass' to CO coordinator
      					  // notifyCOCoOrdinator(context, mpCO, JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_ADD_N_CANCEL, STATUS_PASS, null);
        				  
            		}
            		  
            	}catch(Exception ex){
            		ex.printStackTrace();
            		if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        				_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaDataAddnCancel --> Notifying the update status as FAIL :-"+ex.getMessage());
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        			}
            		bNotifyAddnCancel = false;
            		// Notifying the update status 'Fail' to CO coordinator
        			notifyCOCoOrdinator(context, mpCO, JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_ADD_N_CANCEL, STATUS_FAIL, ex.getMessage());
            		
            		// Aborting transaction
        			ContextUtil.abortTransaction(context);
	            }	            	
            }else{
	            if(null != writeIntoFile){
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	       			_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaDataAddnCancel --> No Data Provided :-");
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	       		}
				// notifyCOCoOrdinator(context, mpCO, JSON_EC_META_DATA + " - " + JSON_EC_META_DATA_ADD_N_CANCEL, STATUS_FAIL, MESSAGE_NO_DATA_PROVIDED);
            }
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : updateECMetaDataAddnCancel --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateECMetaDataAddnCancel --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaDataAddnCancel :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}				
	}
	
	/**
	* This method is called internally, to update EC Meta Data's Other text.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void updateECMetaDataOtherText(Context context, Map mpCO, JSONArray jaTableData, int iCONameIndex, int iTextTypeIndex, int iTextIndex) throws Exception{
		JSONArray jaTableDataInd = null;
		DomainObject doCO = null;
		String strTextType = DomainConstants.EMPTY_STRING;
		String strText = DomainConstants.EMPTY_STRING;
		StringBuilder sbComments = new StringBuilder();
		boolean bNewLineReq = true;
		try{
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : updateECMetaDataOtherText --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : updateECMetaDataOtherText --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateECMetaDataOtherText --> Table Data Length  " + jaTableData.length() + "\n", writeIntoFile);
			}
			if(jaTableData.length() > 0){
            	try{
            		String strCOId = (String) mpCO.get(DomainObject.SELECT_ID);
            		if(UIUtil.isNotNullAndNotEmpty(strCOId)){
            			doCO = DomainObject.newInstance(context, strCOId);
            			for(int i = 0; i < jaTableData.length(); i++){
            				bNewLineReq = true;
							jaTableDataInd = jaTableData.getJSONArray(i);
							strTextType = jaTableDataInd.getString(iTextTypeIndex);
							if("null" == strTextType){
								strTextType = DomainConstants.EMPTY_STRING;
							} 
							strText = jaTableDataInd.getString(iTextIndex);
							if("null" == strText){
								strText = DomainConstants.EMPTY_STRING;
							}
							/*if(null != writeIntoFile){
								writeDataToFile("PWCCMAppIntegration : updateECMetaDataOtherText --> TEXT_TYPE_CD -> " + strTextType + "\n", writeIntoFile);
			 					writeDataToFile("PWCCMAppIntegration : updateECMetaDataOtherText --> TEXT -> " + strText + "\n", writeIntoFile);
			 				}*/
							if(UIUtil.isNotNullAndNotEmpty(strTextType) && UIUtil.isNotNullAndNotEmpty(strText)){
								sbComments.append(strTextType).append(": ").append(strText);
							}else if(UIUtil.isNotNullAndNotEmpty(strTextType) && UIUtil.isNullOrEmpty(strText)){
								sbComments.append(strTextType);
							}else if(UIUtil.isNullOrEmpty(strTextType) && UIUtil.isNotNullAndNotEmpty(strText)){
								sbComments.append(strText);
							}else{
								bNewLineReq = false;
							}
							if(i < jaTableData.length() - 1){
								if(bNewLineReq){
									sbComments.append(System.lineSeparator());
								}
							}
	            		}
            			/*if(null != writeIntoFile){
							writeDataToFile("PWCCMAppIntegration : updateECMetaDataOtherText --> strCOId -> " + strCOId + "\n", writeIntoFile);
		 					writeDataToFile("PWCCMAppIntegration : updateECMetaDataOtherText --> Comments -> " + sbComments.toString().trim() + "\n", writeIntoFile);
		 				}*/
            			if(null != sbComments){
            				doCO.setAttributeValue(context, ATTRIBUTE_COMMENTS, sbComments.toString().trim());
            			}
					}
            	}catch(Exception ex){
            		ex.printStackTrace();
            		if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        				_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaDataOtherText :-"+ex.getMessage());
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
        			}
            	}            	
             }else{
            	 if(null != writeIntoFile){
				 	//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
      				_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaDataOtherText --> No Data Provided.");
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
      			 }
             }
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : updateECMetaDataOtherText --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : updateECMetaDataOtherText --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : updateECMetaDataOtherText :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}	
	}
	
	/** 
	 * This method is to update Affected & Reference EAPLs.
	 * @param context
	* @param lists
	* @returns void
	 * @throws Exception if any exception occurs.
	 */
	public void updateAffectednReferenceEAPLs(Context context, DomainObject doCO, StringList slAffectedEAPLs, StringList slReferenceEAPLs, Map mpCO) throws Exception{
		String strAffItemSelectable = DomainConstants.EMPTY_STRING;
		StringList objectSelects = new StringList();
		Map mpCOData = null;
		StringList slAffectedItems = new StringList();
		String strCAId = DomainConstants.EMPTY_STRING;
		DomainObject doCA = null;
		MapList mlAffItem = new MapList();
		Map mpAffItem = null;
		String strAffItem = DomainConstants.EMPTY_STRING;
		DomainObject doAffItem = null;
		DomainRelationship doAffItemRel = null;
		DomainRelationship doChangeAffItemRel = null;
		String strRelId = DomainConstants.EMPTY_STRING;
		StringList slBusSelects = new StringList();
		slBusSelects.addElement("next.id");
		slBusSelects.addElement("next.revision");
		slBusSelects.addElement("next.current");
		Map mpNextRevData = null;
		String strNextRev = DomainConstants.EMPTY_STRING;
		String strNextId = DomainConstants.EMPTY_STRING;
		String strNextCurrent = DomainConstants.EMPTY_STRING;
		BusinessObject boRevisedAffItem = null;
		DomainObject doImplItem = null;
		String strRevisedAffItem = DomainConstants.EMPTY_STRING;
		DomainRelationship doImplItemRel = null;
		int iAffEAPLsSize = slAffectedEAPLs.size();
		int iRefEAPLsSize = slReferenceEAPLs.size();
		String strEAPL = DomainConstants.EMPTY_STRING;
		StringList objSels = new StringList();
		objSels.addElement(DomainConstants.SELECT_ID);
		StringList relSelects = new SelectList();
		relSelects.addElement(DomainRelationship.SELECT_ID);
		AttributeList attrList = new AttributeList();
		MapList mlChanAffnImplItems = new MapList();
		Map mpChanAffOrImplItem = null;
		Map mpExistingAffItem = new HashMap();
		Map mpExistingImplItem = new HashMap();
		
		Object objCAID;
		
		try
		{
			if(null != doCO)
			{
				strAffItemSelectable = "from[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "].to.from[" + ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM + "].id";
				objectSelects.addElement("from[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "].to.id");
				DomainObject.MULTI_VALUE_LIST.add(strAffItemSelectable);
				objectSelects.addElement(strAffItemSelectable);		  
				mpCOData = doCO.getInfo(context, objectSelects);
				DomainObject.MULTI_VALUE_LIST.remove(strAffItemSelectable);  
				
				if (!mpCOData.isEmpty() && mpCOData.containsKey(strAffItemSelectable)){
					slAffectedItems = (StringList) mpCOData.get(strAffItemSelectable);
				}
			
			// Modified to handle StringList-String casting issue which comes randomly with same API.
			//strCAId = (String) mpCOData.get("from[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "].to.id");
			objCAID = mpCOData.get("from[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "].to.id");
  
			if (objCAID != null)
			{
				if (objCAID  instanceof StringList)
				{
					strCAId = (String) ((StringList) objCAID).get(0);
				}
				else
				{
					strCAId = (String) objCAID;
				}
			}
				  
				  
				  
				  if (UIUtil.isNotNullAndNotEmpty(strCAId)){
					  doCA = DomainObject.newInstance(context, strCAId);
					  mlChanAffnImplItems = doCA.getRelatedObjects(context, 
          						ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM + "," + ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM, 
          						DomainConstants.TYPE_PART, 
          						objSels,
          						relSelects, 
          						false,
          						true, 
          						(short) 1,
          						null,
          						null,
          						0);
					  if(mlChanAffnImplItems.size() > 0){
						  for(int i = 0; i < mlChanAffnImplItems.size(); i++){
							  mpChanAffOrImplItem = (Map) mlChanAffnImplItems.get(i);
							  if(ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM.equals((String) mpChanAffOrImplItem.get(DomainConstants.KEY_RELATIONSHIP))){
								  mpExistingAffItem.put((String) mpChanAffOrImplItem.get(DomainConstants.SELECT_ID), (String) mpChanAffOrImplItem.get(DomainRelationship.SELECT_ID));
							  }else if(ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM.equals((String) mpChanAffOrImplItem.get(DomainConstants.KEY_RELATIONSHIP))){
								  mpExistingImplItem.put((String) mpChanAffOrImplItem.get(DomainConstants.SELECT_ID), (String) mpChanAffOrImplItem.get(DomainRelationship.SELECT_ID));
							  }
						  }
					  }
				  }

				  if (iAffEAPLsSize > 0){
					  for (int i = 0; i < iAffEAPLsSize; i++){
						  strEAPL = (String) slAffectedEAPLs.get(i);
						  mlAffItem = getLatestReleasedPart(context, strEAPL);
						  if(mlAffItem.size() > 0){
							  mpAffItem = (Map) mlAffItem.get(0);
							  
							  strAffItem = (String) mpAffItem.get(DomainObject.SELECT_ID);
							  if (!slAffectedItems.contains(strAffItem)){
								  	doAffItem = DomainObject.newInstance(context, strAffItem);
								  	if(mpExistingAffItem.containsKey(strAffItem)){
								  		strRelId = (String) mpExistingAffItem.get(strAffItem);
								  		if(UIUtil.isNotNullAndNotEmpty(strRelId)){
	 	           							doChangeAffItemRel = DomainRelationship.newInstance(context, strRelId);
	 	           							doChangeAffItemRel.setAttributeValue(context, ATTRIBUTE_REQUESTED_CHANGE, REQ_CHANGE_FOR_REVISE);
	 	           						}
								  	}else{
								  		// attrList = new AttributeList();
	 	           						// attrList.addElement(new Attribute(new AttributeType(ATTRIBUTE_REQUESTED_CHANGE), REQ_CHANGE_FOR_REVISE));
	 	           						// doCA.connect(context, null, new RelationshipType(ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM), true, doAffItem, attrList);
								  		doChangeAffItemRel = DomainRelationship.connect(context, doCA, new RelationshipType(ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM), doAffItem);
								  		doChangeAffItemRel.setAttributeValue(context, ATTRIBUTE_REQUESTED_CHANGE, REQ_CHANGE_FOR_REVISE);
								  		doChangeAffItemRel.addBusinessInterface(context, new BusinessInterface(INTERFACE_DISPOSITION_CODES, null));
								  	}	 	           					
		 	           				mpNextRevData = doAffItem.getInfo(context, slBusSelects);
		 	           				strNextRev = (String) mpNextRevData.get("next.revision");
		 	           				strNextId = (String) mpNextRevData.get("next.id");
		 	           				strNextCurrent = (String) mpNextRevData.get("next.current");
		 	           				if(UIUtil.isNullOrEmpty(strNextRev) || UIUtil.isNullOrEmpty(strNextId) || UIUtil.isNullOrEmpty(strNextCurrent)){
		 	           					boRevisedAffItem = doAffItem.reviseObject(context, false);
		 	           				}else{
	   									if(UIUtil.isNotNullAndNotEmpty(strNextCurrent)){
	   										if(!PART_STATE_RELEASE.equals(strNextCurrent) && !PART_STATE_PRODUCTION.equals(strNextCurrent) && !PART_STATE_EXPERIMENTAL.equals(strNextCurrent) && !DomainConstants.STATE_PART_OBSOLETE.equals(strNextCurrent) && !PART_STATE_COMPLETE.equals(strNextCurrent) && !PART_STATE_DORMANT.equals(strNextCurrent)){
	   											boRevisedAffItem = new BusinessObject(strNextId);
	   										}else{
	   											boRevisedAffItem = doAffItem.reviseObject(context, false);
	   										}
	   									}
		 	           				}
	   								doImplItem = new DomainObject(boRevisedAffItem);
	   								strRevisedAffItem = doImplItem.getId();
	   								if(mpExistingImplItem.containsKey(strRevisedAffItem)){
								  		strRelId = (String) mpExistingImplItem.get(strRevisedAffItem);
								  		if(UIUtil.isNotNullAndNotEmpty(strRelId)){
								  			doImplItemRel = DomainRelationship.newInstance(context, strRelId);
	 	           							doImplItemRel.setAttributeValue(context, ATTRIBUTE_REQUESTED_CHANGE, REQ_CHANGE_FOR_PROD);
	 	           						}
								  	}else{
								  		attrList = new AttributeList();
	 	           						attrList.addElement(new Attribute(new AttributeType(ATTRIBUTE_REQUESTED_CHANGE), REQ_CHANGE_FOR_PROD));
	 	           						doCA.connect(context, null, new RelationshipType(ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM), true, doImplItem, attrList);
	 	           						doImplItem.setOwner(context, (String) mpCO.get(DomainObject.SELECT_OWNER));
	 	           						doImplItem.setAttributeValue(context, DomainConstants.ATTRIBUTE_ORIGINATOR, (String) mpCO.get(DomainObject.SELECT_OWNER));
								  	}
							  	}
					  		}
					  	}
				  }					  
					  
				  if (iRefEAPLsSize > 0){
					  for (int i = 0; i < iRefEAPLsSize; i++){
						  strEAPL = (String) slReferenceEAPLs.get(i);
						  mlAffItem = getLatestReleasedPart(context, strEAPL);
						  if (mlAffItem.size() > 0){
							  mpAffItem = (Map) mlAffItem.get(0);
							  strAffItem = (String) mpAffItem.get(DomainObject.SELECT_ID);
							  if (!slAffectedItems.contains(strAffItem)){
								  	doAffItem = DomainObject.newInstance(context, strAffItem);
								  	if(mpExistingAffItem.containsKey(strAffItem)){
								  		strRelId = (String) mpExistingAffItem.get(strAffItem);
								  		if(UIUtil.isNotNullAndNotEmpty(strRelId)){
	 	           							doChangeAffItemRel = DomainRelationship.newInstance(context, strRelId);
	 	           							doChangeAffItemRel.setAttributeValue(context, ATTRIBUTE_REQUESTED_CHANGE, REQ_CHANGE_NONE);
	 	           						}
								  	}else{
								  		// attrList = new AttributeList();
	 	           						// attrList.addElement(new Attribute(new AttributeType(ATTRIBUTE_REQUESTED_CHANGE), REQ_CHANGE_NONE));
	 	           						// doCA.connect(context, null, new RelationshipType(ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM), true, doAffItem, attrList);
								  		doChangeAffItemRel = DomainRelationship.connect(context, doCA, new RelationshipType(ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM), doAffItem);
								  		doChangeAffItemRel.setAttributeValue(context, ATTRIBUTE_REQUESTED_CHANGE, REQ_CHANGE_NONE);
								  		doChangeAffItemRel.addBusinessInterface(context, new BusinessInterface(INTERFACE_DISPOSITION_CODES, null));
								  	}
							   }
						  }
					  }
				  }		  
			}
		}catch (Exception ex){
			bNotifyAddnCancel = false;
			ex.printStackTrace();
		}
	}
	
	
	/**
	* This method is to build the Part Where Used.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public String buildPartWhereUsed(Context context, String[] args) throws Exception{
		String strReturn = DomainConstants.EMPTY_STRING;
		JSONObject jsonObjWhereUsed = new JSONObject();
		JSONObject jsonObjWhereUsedInt = new JSONObject();
		JSONObject joItemsData = null;
        JSONObject joProcessData = null;
        JSONArray jaColumnIdentifiers = new JSONArray();
        JSONArray jaTableData = new JSONArray();
        JSONArray jaTableDataInd = null;
        JSONArray jaTableDataIndTemp = null;
		try{
			String strContextPartId = args[0];
			String strDecodedWhereUsed = args[1];
			if(null != strDecodedWhereUsed){
				// byte[] decoded = Base64.decodeBase64(strDecodedWhereUsed.getBytes()); 
				// strDecodedWhereUsed = new String(decoded);
				try{
					if(null != strDecodedWhereUsed){
						jsonObjWhereUsedInt = new JSONObject(strDecodedWhereUsed);
						/*jsonObjWhereUsed = new JSONObject(strDecodedWhereUsed);
						if(null != jsonObjWhereUsed){
							joProcessData = jsonObjWhereUsed.getJSONObject(JSON_PROCESS_DATA);
							if(null != joProcessData){
								joItemsData = joProcessData.getJSONObject(JSON_ITEMS_DATA);
								if(null != joItemsData){
									jsonObjWhereUsedInt = joItemsData.getJSONObject(JSON_WHERE_USED);
								}else{
									// No Data provided.
								}
							}
						}*/
					}
				}catch(Exception ex){
					ex.printStackTrace();
					// Content is not in json format.
				}

				int iPartIndex = 0;
				int iLevelIndex = 0;
				String strColIdentifier = DomainConstants.EMPTY_STRING;
				String strPartName = DomainConstants.EMPTY_STRING;
				String strLevel = DomainConstants.EMPTY_STRING;
				String strLevelTemp = DomainConstants.EMPTY_STRING;
				
				MapList mlFromPart = new MapList();
				MapList mlToPart = new MapList();
				Map mpFromPart = null;
				Map mpToPart = null;
				String strFromPartId = DomainConstants.EMPTY_STRING;
				String strToPartId = DomainConstants.EMPTY_STRING;
				if(null != jsonObjWhereUsedInt){
					// Identifying the indexes
					jaColumnIdentifiers = jsonObjWhereUsedInt.getJSONArray(JSON_COLUMN_IDENTIFIERS);
					if(jaColumnIdentifiers.length() > 0){
		            	for(int i = 0; i < jaColumnIdentifiers.length(); i++){
		            		strColIdentifier = jaColumnIdentifiers.getString(i);
		            		if("ASSEMBLY_NO".equals(strColIdentifier)){
		            			iPartIndex = i;
		            		}else if("LEVEL".equals(strColIdentifier)){
		            			iLevelIndex = i;
		            		}
		            	}
		            }
					
					jaTableData = jsonObjWhereUsedInt.getJSONArray(JSON_TABLE_DATA);
					slUniqueNodesList = new StringList();
					int k=0;
					String strProduct = DomainConstants.EMPTY_STRING;
					DomainObject doContextPart = DomainObject.newInstance(context, strContextPartId);
					StringList objectSelects = new StringList();
					objectSelects.addElement(DomainObject.SELECT_ID);
				    objectSelects.addElement(DomainObject.SELECT_TYPE);
				    objectSelects.addElement(DomainObject.SELECT_NAME);
				    objectSelects.addElement(DomainObject.SELECT_REVISION);
				    objectSelects.addElement("revindex");
				    objectSelects.addElement(DomainObject.SELECT_CURRENT);
				    objectSelects.addElement(DomainObject.SELECT_POLICY);
				    objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
				    String strSelectableDerived = "last.to["+RELATIONSHIP_TOP_LEVEL_PART+"].from.to["+RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.to["+RELATIONSHIP_DERIVED+"].from.name";
				    String strSelectableRootHP = "last.to["+RELATIONSHIP_TOP_LEVEL_PART+"].from.to["+RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.name";
				    objectSelects.addElement(strSelectableDerived);
				    objectSelects.addElement(strSelectableRootHP);
					Map mpContextPartInfo = doContextPart.getInfo(context, objectSelects);
					Map mpParts = new HashMap();
					
					// Variable to Manage Level and Branch.
					int iBranch = 0;
					int iSubLevel = 0;
					
					StringList slNodes = new StringList();
					int x = 0;
					StringList subBranchNodes = new StringList();
					String strPartIdTemp = DomainConstants.EMPTY_STRING;
					
					for (int i = 0; i < jaTableData.length(); i++)
					{
						eachRelationJSONObj = new JSONObject();
						eachPartJSONObj 	= new JSONObject();
	 	           		jaTableDataInd 		= jaTableData.getJSONArray(i);
	 	           		strPartName 		= jaTableDataInd.getString(iPartIndex);
	 	           		if(mpParts.containsKey(strPartName)){
	 	           			mlToPart = (MapList) mpParts.get(strPartName);
	 	           		}else{
	 	           			mlToPart = getLastRevisionPart(context, strPartName);
	 	           		}
						
	 	           		if (mlToPart.size() > 0)
						{
							mpToPart = (Map) mlToPart.get(0);
		 	           		strLevel = jaTableDataInd.getString(iLevelIndex);
		 	           		strToPartId = (String) mpToPart.get(DomainObject.SELECT_ID);
		 	           		
		 	           		// Adding the part... so that can be referred from this list accordingly
		 	           		mpParts.put(strPartName, mlToPart);
		 	           		
		 	           		// If BS HP doesn't exist then PC will be directly with Root HP. Below If condition is to manage that.
			 	           	strProduct = (String) mpToPart.get(strSelectableDerived);
							if (UIUtil.isNullOrEmpty(strProduct))
							{
								strProduct = (String) mpToPart.get(strSelectableRootHP);
							}
							
							// If the Level is 1 - that is first link from root context Part. Which is start of a new branch.
		 	           		if (UIUtil.isNotNullAndNotEmpty(strLevel) && "1".equals(strLevel))
		 	           		{
		 	           			// New Branch 
		 	           			iBranch++;
		 	           			
		 	           			// Level Start
		 	           			iSubLevel = 1;
		 	           			
		 	           			slNodes = new StringList();
		 	           			x = 0;
		 	           			subBranchNodes = new StringList();
		 	           		
		 	           			// If the input contains more than one JSON block
		 	           			if (jaTableData.length() > i + 1)
		 	           			{
		 	           				// Get the next JSON block to compare the level
		 	           				jaTableDataIndTemp = jaTableData.getJSONArray(i + 1);
		 	           				
		 	           				// Get the level of next JSON block
		 	           				strLevelTemp = jaTableDataIndTemp.getString(iLevelIndex);
		 	           				
		 	           				// If the level is greater than current one - If greater then its not an END item
		 	           				if (Integer.valueOf(strLevelTemp) > Integer.valueOf(strLevel))
		 	           				{
		 	           					// Building Non-End Item's Edge and Nodes
		 	           					buildNonEndItemNodesnEdge(context, strContextPartId, mpContextPartInfo, strToPartId, mpToPart, strProduct, strLevel, iBranch, iSubLevel, true);
		 	           				}
		 	           				// The level of next JSON item is NOT greater than current one - So its an END item
		 	           				else
		 	           				{		 	           					
		 	           					k++;
		 	           					// Building End Item's Edge and Nodes
		 	           					buildEndItemNodesnEdge(context, strContextPartId, mpContextPartInfo, strToPartId, mpToPart, strProduct, strLevel, iBranch, iSubLevel, true, k);
		 	           				}
		 	           			}
		 	           			// If the input contains ONLY one JSON block which is the end item
		 	           			else
		 	           			{
		 	           				k++;
		 	           				// Building End Item's Edge and Nodes
		 	           				buildEndItemNodesnEdge(context, strContextPartId, mpContextPartInfo, strToPartId, mpToPart, strProduct, strLevel, iBranch, iSubLevel, true, k);
		 	           			}
		 	           		}
		 	           		// The level is greater than 1
		 	           		else if (UIUtil.isNotNullAndNotEmpty(strLevel) && !"1".equals(strLevel))
		 	           		{
		 	           			// Go back to the level which is one less than the current JSON block level.
		 	           			// This is to draw another sub branch from current main branch iBranch.
		 	           			for (int j = i - 1; j < jaTableData.length(); j--)
		 	           			{
		 	           				jaTableDataIndTemp 	= jaTableData.getJSONArray(j);
		 	           				strLevelTemp 		= jaTableDataIndTemp.getString(iLevelIndex);
		 	           				if (Integer.valueOf(strLevelTemp) == Integer.valueOf(strLevel) - 1)
		 	           				{
		 	           					// Re-setting iSubLevel to the level value of TO side node
		 	           					iSubLevel 		= Integer.valueOf(strLevel);
		 	           					
		 	           					strPartName 	= jaTableDataIndTemp.getString(iPartIndex);
		 	           					mlFromPart 		= (MapList) mpParts.get(strPartName);
		 	           					mpFromPart 		= (Map) mlFromPart.get(0);
		 	           					strFromPartId 	= (String) mpFromPart.get(DomainObject.SELECT_ID);
		 	           					break;
		 	           				}
		 	           			}
		 	           			
		 	           			// This is again checking if the Input is greater than current loop number + 1, to get next one and compare
			 	           		if (jaTableData.length() > i + 1)
			 	           		{
			 	           			// Get the next JSON block to compare the level
		 	           				jaTableDataIndTemp = jaTableData.getJSONArray(i + 1);
		 	           				
		 	           				// Get the level of next JSON block
		 	           				strLevelTemp = jaTableDataIndTemp.getString(iLevelIndex);
		 	           				
		 	           				// If the level is greater than current one - If greater then its not an END item
		 	           				// iSubLevel to be decremented to 1 to get the from side Node
		 	           				if (Integer.valueOf(strLevelTemp) > Integer.valueOf(strLevel))
		 	           				{
		 	           					// Building Non-End Item's Edge and Nodes
		 	           					if(!slNodes.contains(strToPartId + "B" + iBranch + "L" + iSubLevel)){
		 	           						slNodes.addElement(strToPartId + "B" + iBranch + "L" + iSubLevel);
		 	           						buildNonEndItemNodesnEdge(context, strFromPartId, mpFromPart, strToPartId, mpToPart, strProduct, strLevel, iBranch, iSubLevel, false);
		 	           					}else{
		 	           						x++;
		 	           						if(x == 1){
		 	           							buildNonEndItemNodesnEdge(context, strFromPartId, mpFromPart, strToPartId + x, mpToPart, strProduct, strLevel, iBranch, iSubLevel, false);
		 	           							subBranchNodes.addElement(strToPartId + x);
		 	           						}else{
		 	           							if(null != subBranchNodes && !subBranchNodes.isEmpty()){
			 	           							for(int p = 1; p < x; p++){
			 	           								strPartIdTemp = strFromPartId + (x-p);
				 	           							if(subBranchNodes.contains(strPartIdTemp)){
				 	           								strFromPartId = strPartIdTemp;
				 	           								break;
				 	           							}
			 	           							}
		 	           							}
		 	           							buildNonEndItemNodesnEdge(context, strFromPartId, mpFromPart, strToPartId + x, mpToPart, strProduct, strLevel, iBranch, iSubLevel, false);
		 	           							if(!subBranchNodes.contains(strToPartId + x)){
		 	           								subBranchNodes.addElement(strToPartId + x);
		 	           							}
		 	           						}
		 	           					}
		 	           				}
		 	           				// The level of next JSON item is NOT greater than current one - So its an END item
		 	           				else
		 	           				{          					
		 	           					k++;
		 	           					// Building End Item's Edge and Nodes
			 	           				if(x == 0){
				 	           				buildEndItemNodesnEdge(context, strFromPartId, mpFromPart, strToPartId, mpToPart, strProduct, strLevel, iBranch, iSubLevel, false, k);
				 	           			}else{
				 	           				buildEndItemNodesnEdge(context, strFromPartId + x, mpFromPart, strToPartId, mpToPart, strProduct, strLevel, iBranch, iSubLevel, false, k);			 	           				
				 	           			}		 	           					
			 	           			}
		 	           			}
			 	           		// This will be end item
			 	           		else
			 	           		{	
			 	           			k++;
			 	           			// Building End Item's Edge and Nodes
				 	           		if(x == 0){
			 	           				buildEndItemNodesnEdge(context, strFromPartId, mpFromPart, strToPartId, mpToPart, strProduct, strLevel, iBranch, iSubLevel, false, k);
			 	           			}else{
			 	           				buildEndItemNodesnEdge(context, strFromPartId + x, mpFromPart, strToPartId, mpToPart, strProduct, strLevel, iBranch, iSubLevel, false, k);			 	           				
			 	           			}
		 	           			}
		 	           		}
		 	           		
		 	           		// Commenting since the for loop to bo back to previous revision is setting the level properly
		 	           		// Increment the level
	 	           			//iSubLevel++;
						}
						else
						{
							// Log - 'No such Part in EV6'
						}
					}
					JSONObject jsonCompleteList = new JSONObject();
					jsonCompleteList.put("nodes", jsonUniquePartsArray);
					jsonCompleteList.put("edges", jsonRelationsArray);
					strReturn = jsonCompleteList.toString();
				}
			}	
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return strReturn;
	}
	
	/**
	* This method is to build Non-End Item's Edge and Nodes.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void buildNonEndItemNodesnEdge(Context context, String strFromPartId, Map mpFromPart, String strToPartId, Map mpToPart, String strProduct, String strLevel, int iBranch, int iSubLevel, boolean bIsFirstlevel) throws Exception{
		try{
			String strFromPartIdTemp =  DomainConstants.EMPTY_STRING;
			// If required to build first level edge/node OR non-first level edge/node
			if(bIsFirstlevel){
				strFromPartIdTemp = strFromPartId;
			}else{
				strFromPartIdTemp = strFromPartId + "B" + iBranch + "L" + (iSubLevel - 1);
			}
			eachRelationJSONObj.put("from", strFromPartIdTemp);
			eachRelationJSONObj.put("fromType", (String) mpFromPart.get(DomainObject.SELECT_TYPE));
			eachRelationJSONObj.put("fromRevIndex", (String) mpFromPart.get("revindex"));
			eachRelationJSONObj.put("to", strToPartId + "B" + iBranch + "L" + iSubLevel);
			eachRelationJSONObj.put("product", strProduct);
			eachRelationJSONObj.put("toType", (String) mpToPart.get(DomainObject.SELECT_TYPE));
			eachRelationJSONObj.put("toRevIndex", (String) mpToPart.get("revindex"));
			eachRelationJSONObj.put("level", strLevel);
			jsonRelationsArray.put(eachRelationJSONObj);

			if (!slUniqueNodesList.contains(strFromPartIdTemp)){
				eachPartJSONObj.put("name", (String) mpFromPart.get(DomainObject.SELECT_NAME));
				eachPartJSONObj.put("partId", (String) mpFromPart.get(DomainObject.SELECT_ID));
				eachPartJSONObj.put("partNumber", (String) mpFromPart.get(DomainObject.SELECT_NAME));
				eachPartJSONObj.put("type", (String) mpFromPart.get(DomainObject.SELECT_TYPE));
				eachPartJSONObj.put("revision", (String) mpFromPart.get(DomainObject.SELECT_REVISION));
				eachPartJSONObj.put("status", (String) mpFromPart.get(DomainObject.SELECT_CURRENT));
				eachPartJSONObj.put("policy", (String) mpFromPart.get(DomainObject.SELECT_POLICY));
				eachPartJSONObj.put("revIndex", (String) mpFromPart.get("revindex"));
				eachPartJSONObj.put("description", (String) mpFromPart.get(DomainObject.SELECT_DESCRIPTION));
				eachPartJSONObj.put("id", strFromPartIdTemp);
				if(bIsFirstlevel){
					eachPartJSONObj.put("isContextPart", "true");
				}
				jsonUniquePartsArray.put(eachPartJSONObj);	
				slUniqueNodesList.add(strFromPartIdTemp);
			}

			if (!slUniqueNodesList.contains(strToPartId + "B" + iBranch + "L" + iSubLevel)){
				eachPartJSONObj = new JSONObject();											
				eachPartJSONObj.put("name", (String) mpToPart.get(DomainObject.SELECT_NAME));
				eachPartJSONObj.put("partId", (String) mpToPart.get(DomainObject.SELECT_ID));
				eachPartJSONObj.put("partNumber", (String) mpToPart.get(DomainObject.SELECT_NAME));
				eachPartJSONObj.put("type", (String) mpToPart.get(DomainObject.SELECT_TYPE));
				eachPartJSONObj.put("revision", (String) mpToPart.get(DomainObject.SELECT_REVISION));
				eachPartJSONObj.put("status", (String) mpToPart.get(DomainObject.SELECT_CURRENT));
				eachPartJSONObj.put("policy", (String) mpToPart.get(DomainObject.SELECT_POLICY));
				eachPartJSONObj.put("revIndex", (String) mpToPart.get("revindex"));
				eachPartJSONObj.put("description", (String) mpToPart.get(DomainObject.SELECT_DESCRIPTION));
				eachPartJSONObj.put("id", strToPartId + "B" + iBranch + "L" + iSubLevel);
				if(bIsFirstlevel){
					if(strToPartId.equals(strFromPartId)){
						eachPartJSONObj.put("isContextPart","true");
					}else{
						eachPartJSONObj.put("isContextPart","false");
					}
				}
				jsonUniquePartsArray.put(eachPartJSONObj);
				slUniqueNodesList.add(strToPartId + "B" + iBranch + "L" + iSubLevel);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	* This method is to build End Item's Edge and Nodes.
	* @param context
	* @param args
	* @returns void
	* @throws Exception if the operation fails
	*/
	public void buildEndItemNodesnEdge(Context context, String strFromPartId, Map mpFromPart, String strToPartId, Map mpToPart, String strProduct, String strLevel, int iBranch, int iSubLevel, boolean bIsFirstlevel, int k) throws Exception{
		try{
			String strFromPartIdTemp =  DomainConstants.EMPTY_STRING;
			// If required to build first level edge/node OR non-first level edge/node
			if(bIsFirstlevel){
				strFromPartIdTemp = strFromPartId;
			}else{
				strFromPartIdTemp = strFromPartId + "B" + iBranch + "L" + (iSubLevel - 1);
			}
			eachRelationJSONObj.put("from", strFromPartIdTemp);
			eachRelationJSONObj.put("fromType", (String) mpFromPart.get(DomainObject.SELECT_TYPE));
			eachRelationJSONObj.put("fromRevIndex", (String) mpFromPart.get("revindex"));
			eachRelationJSONObj.put("to", strToPartId + "B" + iBranch + "L" + iSubLevel + k);
			eachRelationJSONObj.put("product", strProduct);
			eachRelationJSONObj.put("toType", (String) mpToPart.get(DomainObject.SELECT_TYPE));
			eachRelationJSONObj.put("toRevIndex", (String) mpToPart.get("revindex"));
			eachRelationJSONObj.put("level", strLevel);
			jsonRelationsArray.put(eachRelationJSONObj);
			
			if (!slUniqueNodesList.contains(strFromPartIdTemp)){
				eachPartJSONObj.put("name", (String) mpFromPart.get(DomainObject.SELECT_NAME));
				eachPartJSONObj.put("partId", (String) mpFromPart.get(DomainObject.SELECT_ID));
				eachPartJSONObj.put("partNumber", (String) mpFromPart.get(DomainObject.SELECT_NAME));
				eachPartJSONObj.put("type", (String) mpFromPart.get(DomainObject.SELECT_TYPE));
				eachPartJSONObj.put("revision", (String) mpFromPart.get(DomainObject.SELECT_REVISION));
				eachPartJSONObj.put("status", (String) mpFromPart.get(DomainObject.SELECT_CURRENT));
				eachPartJSONObj.put("policy", (String) mpFromPart.get(DomainObject.SELECT_POLICY));
				eachPartJSONObj.put("revIndex", (String) mpFromPart.get("revindex"));
				eachPartJSONObj.put("description", (String) mpFromPart.get(DomainObject.SELECT_DESCRIPTION));
				eachPartJSONObj.put("id", strFromPartIdTemp);
				if(bIsFirstlevel){
					eachPartJSONObj.put("isContextPart", "true");
				}
				jsonUniquePartsArray.put(eachPartJSONObj);	
				slUniqueNodesList.add(strFromPartIdTemp);
			}

			if (!slUniqueNodesList.contains(strToPartId + "B" + iBranch + "L" + iSubLevel + k)){
				eachPartJSONObj = new JSONObject();											
				eachPartJSONObj.put("name", (String) mpToPart.get(DomainObject.SELECT_NAME));
				eachPartJSONObj.put("partId", (String) mpToPart.get(DomainObject.SELECT_ID));
				eachPartJSONObj.put("partNumber", (String) mpToPart.get(DomainObject.SELECT_NAME));
				eachPartJSONObj.put("type", (String) mpToPart.get(DomainObject.SELECT_TYPE));
				eachPartJSONObj.put("revision", (String) mpToPart.get(DomainObject.SELECT_REVISION));
				eachPartJSONObj.put("status", (String) mpToPart.get(DomainObject.SELECT_CURRENT));
				eachPartJSONObj.put("policy", (String) mpToPart.get(DomainObject.SELECT_POLICY));
				eachPartJSONObj.put("revIndex", (String) mpToPart.get("revindex"));
				eachPartJSONObj.put("description", (String) mpToPart.get(DomainObject.SELECT_DESCRIPTION));
				eachPartJSONObj.put("id", strToPartId + "B" + iBranch + "L" + iSubLevel + k);
				if(bIsFirstlevel){
					if(strToPartId.equals(strFromPartId)){
						eachPartJSONObj.put("isContextPart","true");
					}else{
						eachPartJSONObj.put("isContextPart","false");
					}
				}
				jsonUniquePartsArray.put(eachPartJSONObj);
				slUniqueNodesList.add(strToPartId + "B" + iBranch + "L" + iSubLevel + k);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	* This method is to get the latest revision part.
	* @param context
	* @param args
	* @returns MapList
	* @throws Exception if the operation fails
	*/
	public MapList getLastRevisionPart(Context context, String strPartName) throws Exception{
		MapList mlReturn = new MapList();
		try{
			StringList objectSelects = new StringList();
			objectSelects.addElement(DomainObject.SELECT_ID);
		    objectSelects.addElement(DomainObject.SELECT_TYPE);
		    objectSelects.addElement(DomainObject.SELECT_NAME);
		    objectSelects.addElement(DomainObject.SELECT_REVISION);
		    objectSelects.addElement("revindex");
		    objectSelects.addElement(DomainObject.SELECT_CURRENT);
		    objectSelects.addElement(DomainObject.SELECT_POLICY);
		    objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
		    String strSelectable = "last.to["+RELATIONSHIP_TOP_LEVEL_PART+"].from.to["+RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.to["+RELATIONSHIP_DERIVED+"].from.name";
		    objectSelects.addElement(strSelectable);
         	String strWhrClause = "revision == last";
			mlReturn= DomainObject.findObjects(context, 
	               		DomainConstants.TYPE_PART, 
	               		strPartName, 
	               		DomainConstants.QUERY_WILDCARD, 
	               		DomainConstants.QUERY_WILDCARD, 
	               		DomainConstants.QUERY_WILDCARD, 
	               		strWhrClause, 
	               		false, 
	               		objectSelects);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return mlReturn;
	}
	
	/**
	* This method is to show the Full history Where used.
	* @param context
	* @param args
	* @returns String
	* @throws Exception if the operation fails
	*/
	public String showWhereUsedGraph(Context context, String[] args) throws Exception{
		String strReturn = DomainConstants.EMPTY_STRING;
		try{
			String strObjectId = args[0];
			DomainObject dmObj	= DomainObject.newInstance(context, strObjectId);
			String partNumber = dmObj.getInfo(context,DomainObject.SELECT_NAME);
			String strJSONWhereUsed = whereUsed(CMAPP_WHERE_USED_SERVICE_URL, partNumber, "all");
			if(null != strJSONWhereUsed && !DomainConstants.EMPTY_STRING.equals(strJSONWhereUsed)){
				String[] arrJPOArgs = {strObjectId, strJSONWhereUsed};
				strReturn = buildPartWhereUsed(context, arrJPOArgs);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return strReturn;
	}
	
	/**
	* This method is to call CM App and get the Full History data.
	* @param context
	* @param Part Number
	* @returns String
	* @throws Exception if the operation fails
	*/
	public String whereUsed(String serverURL, String partNumber, String strType) throws Exception{
		StringBuffer response = new StringBuffer();
		try{
			StringBuffer urlParm = new StringBuffer();
		    urlParm.append(serverURL)
			.append("?partno=")
			.append(partNumber)
			.append("&type=")
			.append(strType);
			URL url = new URL(urlParm.toString());
			HttpURLConnection c = (HttpURLConnection)url.openConnection();
			c.setRequestMethod("GET");
			// Connection establishment timeout
			c.setConnectTimeout(5000);  
			// Socket read timeout (this should be BIG to prevent HTTP 500 error)
			c.setReadTimeout(300000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
			String line = DomainConstants.EMPTY_STRING;
			while(null != (line = reader.readLine())){
				response.append(line);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return response.toString();
	}
		/**
	 * Reloads the Record Type Description field on Edit Page of Part Web-Form
	 * @param context
	 * @param args
	 * @throws Exception
	 **
	 */
	public Map reloadRecordDescField(Context context, String[] args) throws Exception {
		Map argMap = (Map)JPO.unpackArgs(args);
	    Map requestMap = (Map)argMap.get("requestMap");
	   String strObjectId = (String)requestMap.get("objectId");
	    String strRecordTypeDesc = DomainConstants.EMPTY_STRING;
	    Map fieldMap = new HashMap();
	    try{
			    if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
			    DomainObject domObj=DomainObject.newInstance(context, strObjectId);
			    strRecordTypeDesc = domObj.getAttributeValue(context,ATTR_PWC_RECORDTYPE_DESC);
			    }
			    fieldMap.put("SelectedValues", strRecordTypeDesc);
			    fieldMap.put("SelectedDisplayValues", strRecordTypeDesc);
	    }
	    catch(Exception e){
			throw new FrameworkException(e);
		}
	    return fieldMap;
	}
	/**
	 * Reloads the Record Type  field on Edit Page of Part Web-Form
	 * @param context
	 * @param args
	 * @throws Exception
	 **
	 */
	public Map reloadRecordTypeField(Context context, String[] args) throws Exception {
		Map argMap = (Map)JPO.unpackArgs(args);
	    Map requestMap = (Map)argMap.get("requestMap");
	    String strObjectId = (String)requestMap.get("objectId");
	    String strRecordType = DomainConstants.EMPTY_STRING;
	    Map fieldMap = new HashMap();
	    try{
			    if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
			    DomainObject domObj=DomainObject.newInstance(context, strObjectId);
			    strRecordType = domObj.getAttributeValue(context,ATTR_PWC_RECORDTYPE);
			    }
			    fieldMap.put("SelectedValues", strRecordType);
			    fieldMap.put("SelectedDisplayValues", strRecordType);
	    }
	    catch(Exception e){
			throw new FrameworkException(e);
		}
	    return fieldMap;
	}

	/**
	 * Function added for editability check for Record Fields on part view/edit form
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public static boolean checkEditablityOfRecordAtt(Context context, String [] args) throws Exception{
		boolean boolAccessPolicy = false;
		try {
				HashMap programMap = (HashMap)JPO.unpackArgs(args);							
				String strObjectId = (String)programMap.get("objectId");		
				String strCopyObjectId = (String)programMap.get("copyObjectId");
				String strAttributeName = DomainConstants.EMPTY_STRING ;
				if(UIUtil.isNullOrEmpty(strObjectId) && !UIUtil.isNullOrEmpty(strCopyObjectId) ){
					strObjectId = strCopyObjectId;
				}
				boolean isEditAccess = false;
				HashMap hmSettings = (HashMap)programMap.get("SETTINGS");		
				if (!UIUtil.isNullOrEmpty(strObjectId)) 
				{
					if (null != hmSettings && !hmSettings.isEmpty())
					{
						strAttributeName = (String) hmSettings.get("Admin Type");
						String sName =   (String) hmSettings.get("name");
					}		
					if(UIUtil.isNullOrEmpty(strAttributeName)){
						strAttributeName = DomainConstants.EMPTY_STRING ;
					}			
					DomainObject domObj = DomainObject.newInstance(context, strObjectId);
					StringList strlSelectList = new StringList();
					strlSelectList.add(DomainObject.SELECT_POLICY);
					strlSelectList.add(DomainObject.SELECT_CURRENT);
					Map mData = (Map) domObj.getInfo(context, strlSelectList);
					String strPartPolicy = (String) mData.get(DomainObject.SELECT_POLICY);
					String strPartState = (String) mData.get(DomainObject.SELECT_CURRENT);
					String strSymbolicRecordTypeDescName 			= FrameworkUtil.getAliasForAdmin(context,"attribute","PWC_RecordTypeDesc",false);
					String strSymbolicRecordTypeName = FrameworkUtil.getAliasForAdmin(context,"attribute","PWC_RecordTYPE",false);
					//START :: Modifed for HEAT-C-16867 : CMApp Drop2 UC 15 - Record Type for Standard Part
					if ((POLICY_EC_PART.equals(strPartPolicy) || POLICY_STANDARD_PART.equals(strPartPolicy)) && STATE_PRELIM_EC_PART.equals(strPartState)) 
					//END :: Modifed for HEAT-C-16867 : CMApp Drop2 UC 15 - Record Type for Standard Part
					{
						if (strAttributeName.equalsIgnoreCase(strSymbolicRecordTypeDescName) || strAttributeName.equals(strSymbolicRecordTypeName))
						{
							isEditAccess = true;
						}
					}
					if (isEditAccess) {
						hmSettings.put("Editable", "true");
					} else {
						hmSettings.put("Editable", "false");
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}	
	/**
	* This method is to get the Part Details, which is to be pushed to CM App.
	* @param context 
	* @param String
	* @return void
	* @throws Exception if the operation fails
	*/
	public void getPartRecordAttributes(Context context, String[] args) throws Exception{

		JSONObject jsonObjPart =null;
		JSONObject jsonObjPartlInt = null;
		JSONArray jaItemsList = null;
        JSONObject joItemsData = null;
        JSONObject joProcessData = null;
		StringList slSelectables= new StringList();
		try{
			//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			writeIntoFile = setLoggerPath(CMAPP_PART_INFO_LOG_FILE_NAME);
			//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : getPartRecordAttributes --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : getPartRecordAttributes --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			String strObjectId = args[0];
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
				String strSelectRecordType = "attribute["+ATTR_PWC_RECORDTYPE+"]";
				String strSelectRecordTypeDesc= "attribute["+ATTR_PWC_RECORDTYPE_DESC+"]";
				DomainObject domPart = new DomainObject(strObjectId);
				slSelectables.add(PWCIntegrationConstants.SELECT_NAME);
				slSelectables.add(PWCIntegrationConstants.SELECT_TYPE);
				slSelectables.add(PWCIntegrationConstants.SELECT_REVISION);
				slSelectables.add(strSelectRecordType);
				slSelectables.add(strSelectRecordTypeDesc);
				Map mpData = domPart.getInfo(context, slSelectables);	
				MapList mlPCData = new MapList();
				mlPCData.add(mpData);	        
				/*if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : getPartRecordAttributes -->  Part Object Id --> "+strObjectId + "\n", writeIntoFile);
					writeDataToFile("PWCCMAppIntegration : getPartRecordAttributes --> Part Details  " + mpData + "\n", writeIntoFile);
				}*/				
				// Re-Build MapList
				MapList mlPart = reBuildRelatedAttributeMap(mlPCData, null);
				// Building json object of column identifier & table data
				jsonObjPartlInt = new JSONObject(generateColumnIdentifiersAndTableData(mlPart));				
				// Building the final json object - jsonObjMakeFrom
		        jaItemsList  = new JSONArray();
		        jaItemsList.put(JSON_PART_INFO);
		        joItemsData = new JSONObject();
		        joItemsData.put(JSON_PART_INFO, jsonObjPartlInt);
		        joProcessData = new JSONObject();
		        joProcessData.put(JSON_ITEMS_LIST, jaItemsList);
		        joProcessData.put(JSON_ITEMS_DATA, joItemsData);
		        jsonObjPart = new JSONObject();
		        jsonObjPart.put(JSON_PROCESS_NAME, JSON_PART_INFO);
		        jsonObjPart.put(JSON_PROCESS_DATA, joProcessData);		        
		        
		        String strPartDetails = jsonObjPart.toString();
		        // Encoding the string 
		        byte[] bytesEncoded = Base64.encodeBase64(strPartDetails.getBytes());
		        String strEncoded = new String(bytesEncoded);	        
				/*if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : getPartRecordAttributes --> Part Details --> "+strPartDetails + "\n", writeIntoFile);
				}*/
				try{
		        // Pushing Data
				/*if(null != writeIntoFile){
					writeDataToFile("PWCCMAppIntegration : getPartRecordAttributes -->Pushing Part Record and Record Type Desc data to CMApp --> \n", writeIntoFile);
				}*/				
		        // Pushing Data
		        CMAppServiceStub.PushProcessToLegacy pushProcessToLegacy = new CMAppServiceStub.PushProcessToLegacy();
		        pushProcessToLegacy.setProcessDetails(strEncoded);
		        CMAppServiceStub.PushProcessToLegacyE pushProcLegE = new CMAppServiceStub.PushProcessToLegacyE();
		        pushProcLegE.setPushProcessToLegacy(pushProcessToLegacy);
		        CMAppServiceStub cmAppStub = new CMAppServiceStub();
		        CMAppServiceCallbackHandler callBack = new CMAppServiceCallbackHandler();
		        cmAppStub.startpushProcessToLegacy(pushProcLegE, callBack);
		        notifyCOCoOrdinator(context, null, JSON_PART_INFO, STATUS_PASS, null);
				}catch(Exception exp){
					if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						_LOGGER.debug("Exception in PWCCMAppIntegration : getPartRecordAttributes :-"+exp.getMessage());
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					}
					notifyCOCoOrdinator(context, null, JSON_PART_INFO, STATUS_FAIL, exp.getMessage());
				}
				if(null != writeIntoFile){
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					_LOGGER.debug("Execution End Time of PWCCMAppIntegration : getPartRecordAttributes --> " + java.util.Calendar.getInstance().getTime());
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					//writeDataToFile("PWCCMAppIntegration : getPartRecordAttributes --> Exit \n", writeIntoFile);
				}
		     }
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppProcess : getPartRecordAttributes :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
	}
	
	/**
	* This function is to check Sync with CM App value of context Part object
	* @param context the eMatrix <code>Context</code> object
	* @param args
	* @return boolean
	* @
	**/
	public boolean checkSyncWithCMAppValueAlternate(Context context, String [] args) throws Exception{
		boolean bFlag = true;
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");
			StringList slSyncWithCMAppValueList = new StringList();
			StringList slAffItemsFlagList = new StringList();
			StringList slImplItemsFlagList = new StringList();
			if(!UIUtil.isNullOrEmpty(strObjectId)){
				String strArray[] = new String[1];
				strArray[0] = strObjectId;
				StringList ObjectSelect = new StringList();
				ObjectSelect.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"|.from.type==\""+ChangeConstants.TYPE_CHANGE_ORDER+"\"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");		
				ObjectSelect.add("to["+ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"|.from.type==\""+ChangeConstants.TYPE_CHANGE_ORDER+"\"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");
				BusinessObjectWithSelectList busClassList = BusinessObject.getSelectBusinessObjectData(context, strArray, ObjectSelect);				
				BusinessObjectWithSelectItr busWithSelectItr = new BusinessObjectWithSelectItr(busClassList);		
				while (busWithSelectItr.next()){
					BusinessObjectWithSelect busWithSelect = busWithSelectItr.obj();
					slAffItemsFlagList = (StringList) busWithSelect.getSelectDataList("to["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");
					slImplItemsFlagList = (StringList) busWithSelect.getSelectDataList("to["+ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.attribute["+ATTRIBUTE_PWC_Sync_With_CM_App+"].value");
					if(null != slAffItemsFlagList && !slAffItemsFlagList.isEmpty()){
						for(int i = 0; i < slAffItemsFlagList.size(); i++){
							slSyncWithCMAppValueList.add(slAffItemsFlagList.get(i));
						}
					}
                	if(null != slImplItemsFlagList && !slImplItemsFlagList.isEmpty()){
                		for(int i = 0; i < slImplItemsFlagList.size(); i++){
							slSyncWithCMAppValueList.add(slImplItemsFlagList.get(i));
						}
                	}	  
					if(null != slSyncWithCMAppValueList && slSyncWithCMAppValueList.contains("TRUE")){
						bFlag = false;
					}				
			    }
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return bFlag;
	}
	
	/**
	* This Method is to check the change object's state.
	* @param context
	* @param JSONObject
	* @return String
	* @throws Exception if operation fails
	**/
	public Map checkChangeObjectState(Context context, JSONObject jsonObject, String strProcessName) throws Exception{
		Map mpReturn = new HashMap();
		String strMsg = PROCESS;
		String strColIdentifier = DomainConstants.EMPTY_STRING;
		int iCONameIndex = 0;
		JSONArray jaColumnIdentifiers = new JSONArray();
		JSONArray jaTableData = new JSONArray();
		Map mpChange = null;
		String strChangeId = DomainConstants.EMPTY_STRING;
		String strChangeType = DomainConstants.EMPTY_STRING;
		String strChangeName = DomainConstants.EMPTY_STRING;
		String strChangeState = DomainConstants.EMPTY_STRING;
		String strChangeOwner = DomainConstants.EMPTY_STRING;
		//START :: Added for HEAT-C-19459 : CMApp leftover items
		String strCategoryOfChange = DomainConstants.EMPTY_STRING;
		//END :: Added for HEAT-C-19459 : CMApp leftover items
		boolean bCOFlag = true;
		try{
			if(null != writeIntoFile){
				//writeDataToFile("PWCCMAppIntegration : checkChangeObjectState --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : checkChangeObjectState --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			if(null != jsonObject){
				jaColumnIdentifiers = jsonObject.getJSONArray(JSON_COLUMN_IDENTIFIERS);
				if(jaColumnIdentifiers.length() > 0){
	            	for(int i = 0; i < jaColumnIdentifiers.length(); i++){
	            		strColIdentifier = jaColumnIdentifiers.getString(i);
	            		if("DOCUMENT_NO".equalsIgnoreCase(strColIdentifier)){
	            			iCONameIndex = i;
	            			break;
	            		}
	            	}
	            }
				jaTableData = jsonObject.getJSONArray(JSON_TABLE_DATA);
				JSONArray jaTableDataInd = jaTableData.getJSONArray(0);
				strChangeName = jaTableDataInd.getString(iCONameIndex).trim();
				if("null" == strChangeName){
					strChangeName = DomainConstants.EMPTY_STRING;
				}

				// Getting the Change Object
				MapList mlChange = new MapList();
				StringList objectSelects = new StringList();
	            objectSelects.addElement(DomainObject.SELECT_ID);
	            objectSelects.addElement(DomainObject.SELECT_TYPE);
	            objectSelects.addElement(DomainObject.SELECT_NAME);
	            objectSelects.addElement(DomainObject.SELECT_OWNER);
	            objectSelects.addElement(DomainObject.SELECT_CURRENT);
				//START :: Modified for HEAT-C-19459 : CMApp leftover items
	            objectSelects.addElement("attribute["+DomainConstants.ATTRIBUTE_CATEGORY_OF_CHANGE+"]");
				//END :: Modified for HEAT-C-19459 : CMApp leftover items
	            Pattern typePattern=new Pattern(ChangeConstants.TYPE_CHANGE_ORDER);
	            if(!JSON_EC_META_DATA.equals(strProcessName)){
	            	typePattern.addPattern(ChangeConstants.TYPE_CHANGE_ACTION);
	            }
	            if(UIUtil.isNotNullAndNotEmpty(strChangeName)){
		            mlChange = DomainObject.findObjects(context, 
		            		typePattern.getPattern(), 
		            		strChangeName, 
		            		"-", 
		            		DomainConstants.QUERY_WILDCARD, 
		            		DomainConstants.QUERY_WILDCARD, 
		            		null, 
		            		false, 
		            		objectSelects);
	            
		           /* if(null != writeIntoFile){
						writeDataToFile("PWCCMAppIntegration : checkChangeObjectState --> Maplist --> "+mlChange + "\n", writeIntoFile);
					}*/
					if(mlChange.size() > 0){
						mpChange = (Map) mlChange.get(0);
						strChangeType = (String) mpChange.get(DomainObject.SELECT_TYPE);
						strChangeState = (String) mpChange.get(DomainObject.SELECT_CURRENT);
		            	strChangeId = (String) mpChange.get(DomainObject.SELECT_ID);
		            	strChangeOwner = (String) mpChange.get(DomainObject.SELECT_OWNER);
		            	strChangeName = (String) mpChange.get(DomainObject.SELECT_NAME);
						//START :: Modified for HEAT-C-19459 : CMApp leftover items
		            	strCategoryOfChange = (String) mpChange.get("attribute["+DomainConstants.ATTRIBUTE_CATEGORY_OF_CHANGE+"]");
						//END :: Modified for HEAT-C-19459 : CMApp leftover items
		            	/*if(null != writeIntoFile){
		            		writeDataToFile("PWCCMAppIntegration : checkChangeObjectState --> Change Type --> "+ strChangeType + "\n", writeIntoFile);
		            		writeDataToFile("PWCCMAppIntegration : checkChangeObjectState --> Change Name --> "+ strChangeName + "\n", writeIntoFile);
							writeDataToFile("PWCCMAppIntegration : checkChangeObjectState --> Change Object state -> '"+strChangeState + "'\n", writeIntoFile);
							writeDataToFile("PWCCMAppIntegration : checkChangeObjectState --> Change Object owner -> '"+strChangeOwner + "'\n", writeIntoFile);
							writeDataToFile("PWCCMAppIntegration : checkChangeObjectState --> Process name -> '"+strProcessName + "'\n", writeIntoFile);
						}*/
		            	
		    			if(JSON_EC_META_DATA.equals(strProcessName)){
		    				if(!CO_STATE_IN_WORK.equals(strChangeState)){
	    						strMsg = "Cannot process " + STR_EC_META_DATA + " - CO is in '" +strChangeState  + "' state";
	    						if(null != writeIntoFile){
									//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	    							_LOGGER.debug("PWCCMAppIntegration : checkChangeObjectState --> Cannot process " + STR_EC_META_DATA + " - CO is in '" +strChangeState  + "' state ");
									//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	    						}
	    					}
		    			}else if(JSON_DESIGN_LEVEL.equals(strProcessName)){
		    				if(ChangeConstants.TYPE_CHANGE_ACTION.equals(strChangeType)){
		    					if(!CA_STATE_IN_WORK.equals(strChangeState) && !CA_STATE_IN_APPROVAL.equals(strChangeState) && !CA_STATE_COMPLETE.equals(strChangeState)){
		    						strMsg = "Cannot process " + STR_DESIGN_LEVEL + " - CA is in '" +strChangeState  + "' state";
		    						if(null != writeIntoFile){
										//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    							_LOGGER.debug("PWCCMAppIntegration : checkChangeObjectState --> Cannot process " + STR_DESIGN_LEVEL + " - CA is in '" +strChangeState  + "' state ");
										//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    						}
		    					}
		    				}else if(ChangeConstants.TYPE_CHANGE_ORDER.equals(strChangeType)){
		    					if(!CO_STATE_IN_APPROVAL.equals(strChangeState) && !CO_STATE_COMPLETE.equals(strChangeState) && !CO_STATE_IMPLEMENTED.equals(strChangeState)){
		    						//START :: Modified for HEAT-C-19459 : CMApp leftover items
		    						strMsg = "Cannot process " + STR_DESIGN_LEVEL + " - CO is in '" +strChangeState  + "' state";
		    						if(UIUtil.isNotNullAndNotEmpty(strCategoryOfChange)){
		    							if((CO_STATE_IN_WORK.equals(strChangeState) && !CHAGE_OF_CATEGORY_EED_RELEASE.equals(strCategoryOfChange))){
		    								StringBuffer sbBuffer = new StringBuffer("Cannot process ").append(STR_DESIGN_LEVEL).append(" - Category Of Change attribute value is '").append(strCategoryOfChange).append("'");
			    							strMsg = sbBuffer.toString();
			    						}else if((CO_STATE_IN_WORK.equals(strChangeState) && CHAGE_OF_CATEGORY_EED_RELEASE.equals(strCategoryOfChange))){
			    							strMsg = PROCESS;
			    						}
		    						}
		    						//END :: Modified for HEAT-C-19459 : CMApp leftover items
		    						if(null != writeIntoFile){
										//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    							_LOGGER.debug("PWCCMAppIntegration : checkChangeObjectState --> Cannot process " + STR_DESIGN_LEVEL + " - CO is in '" +strChangeState  + "' state ");
										//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    						}
		    					}
		    				}
		    			}else if(JSON_LEGACY_PR.equals(strProcessName)){
		    				if(ChangeConstants.TYPE_CHANGE_ACTION.equals(strChangeType)){
		    					if(!CA_STATE_IN_APPROVAL.equals(strChangeState) && !CA_STATE_COMPLETE.equals(strChangeState)){
		    						strMsg = "Cannot process " + STR_LEGACY_PR + " - CA is in '" +strChangeState  + "' state";
		    						if(null != writeIntoFile){
										//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    							_LOGGER.debug("PWCCMAppIntegration : checkChangeObjectState --> Cannot process " + STR_LEGACY_PR + " - CA is in '" +strChangeState  + "' state ");
										//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    						}
		    					}
		    				}else if(ChangeConstants.TYPE_CHANGE_ORDER.equals(strChangeType)){
			    				if(!CO_STATE_IN_APPROVAL.equals(strChangeState) && !CO_STATE_COMPLETE.equals(strChangeState) && !CO_STATE_IMPLEMENTED.equals(strChangeState)){
			    					strMsg = "Cannot process " + STR_LEGACY_PR + " - CO is in '" +strChangeState  + "' state";
			    					if(null != writeIntoFile){
										//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    							_LOGGER.debug("PWCCMAppIntegration : checkChangeObjectState --> Cannot process " + STR_LEGACY_PR + " - CO is in '" +strChangeState  + "' state ");
										//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    						}
			    				}
		    				}
		    			}else if(JSON_PMA.equals(strProcessName)){
		    				if(ChangeConstants.TYPE_CHANGE_ACTION.equals(strChangeType)){
		    					if(!CA_STATE_IN_APPROVAL.equals(strChangeState) && !CA_STATE_COMPLETE.equals(strChangeState)){
		    						strMsg = "Cannot process " + STR_PMA + " - CA is in '" +strChangeState  + "' state";
		    						if(null != writeIntoFile){
										//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    							_LOGGER.debug("PWCCMAppIntegration : checkChangeObjectState --> Cannot process " + STR_PMA + " - CA is in '" +strChangeState  + "' state ");
										//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    						}
		    					}
		    				}else if(ChangeConstants.TYPE_CHANGE_ORDER.equals(strChangeType)){
			    				if(!CO_STATE_IN_APPROVAL.equals(strChangeState) && !CO_STATE_COMPLETE.equals(strChangeState) && !CO_STATE_IMPLEMENTED.equals(strChangeState)){
			    					strMsg = "Cannot process " + STR_PMA + " - CO is in '" +strChangeState  + "' state";
			    					if(null != writeIntoFile){	
										//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    							_LOGGER.debug("PWCCMAppIntegration : checkChangeObjectState --> Cannot process " + STR_PMA + " - CO is in '" +strChangeState  + "' state ");
										//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
		    						}
			    				}
		    				}
		    			}
					}else{
						if(null != writeIntoFile){
							//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
							_LOGGER.debug("PWCCMAppIntegration : checkChangeObjectState --> Change Object '"+strChangeName+"' doesn't exists in EV6.");
							//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						}
					}
	            }else{
	            	if(null != writeIntoFile){
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						_LOGGER.debug("PWCCMAppIntegration : checkChangeObjectState --> Change Object name not provided");
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					}
	            }
				mpReturn.put(DomainConstants.SELECT_NAME, strChangeName);
				mpReturn.put(DomainConstants.SELECT_OWNER, strChangeOwner);
				mpReturn.put(DomainConstants.SELECT_ID, strChangeId);
				mpReturn.put(strMessage, strMsg);
			}
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : checkChangeObjectState --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				//writeDataToFile("PWCCMAppIntegration : checkChangeObjectState --> Exit \n", writeIntoFile);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : checkChangeObjectState :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		return mpReturn;
	}
	
	/**
	* This Method is to set the context to CM App process user.
	* @param context
	* @return boolean
	* @throws Exception if operation fails
	**/
	public String setCMAppProcessUserContext(Context context) 
			throws Exception
	{
		String strCMAppProcessPushed = "False";
		
		try
		{
			if (null != writeIntoFile)
			{
				//writeDataToFile("PWCCMAppIntegration : setCMAppProcessUserContext --> Start \n", writeIntoFile);
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : setCMAppProcessUserContext --> " + java.util.Calendar.getInstance().getTime());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
			String strCMAppProcessUserName = getIntegrationProperty(context, "PWCIntegration.CMApp.ProcessUser.UserName");
			String strCMAppProcessUserPwd = getIntegrationProperty(context, "PWCIntegration.CMApp.ProcessUser.Password");
			/*if(null != writeIntoFile){
				writeDataToFile("PWCCMAppIntegration : setCMAppProcessUserContext --> CM App Process UserName --> " + strCMAppProcessUserName + "\n", writeIntoFile);
				writeDataToFile("PWCCMAppIntegration : setCMAppProcessUserContext -->CM App Process Encrypted Password --> " + strCMAppProcessUserPwd + "\n", writeIntoFile);
			}*/
			
			if (UIUtil.isNotNullAndNotEmpty(strCMAppProcessUserName) && UIUtil.isNotNullAndNotEmpty(strCMAppProcessUserPwd))
			{
				String strResult = MqlUtil.mqlCommand(context, "list person " + strCMAppProcessUserName);
				if (UIUtil.isNotNullAndNotEmpty(strResult))
				{
					if (null != writeIntoFile)
					{
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						_LOGGER.debug("PWCCMAppIntegration : setCMAppProcessUserContext --> Person '" + strCMAppProcessUserName + "' Exists. Pushing context to this person. ");
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					}
					
					if (!strCMAppProcessUserName.equals(context.getUser()))
					{
						ContextUtil.pushContext(context, strCMAppProcessUserName, DSCipher.decrypt(strCMAppProcessUserPwd), DomainConstants.EMPTY_STRING);
					}
					strCMAppProcessPushed = "True";
				}
				else
				{
					if (null != writeIntoFile)
					{
						//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
						_LOGGER.debug("PWCCMAppIntegration : setCMAppProcessUserContext --> Pushing context to User Agent.");
						//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					}
					//ContextUtil.pushContext(context, PWCConstants.SUPER_USER, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					strCMAppProcessPushed = "False";
				}
			}
			else
			{
				if(null != writeIntoFile)
				{
					//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
					_LOGGER.debug("PWCCMAppIntegration : setCMAppProcessUserContext --> Pushing context to User Agent.");
					//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				}
				//ContextUtil.pushContext(context, PWCConstants.SUPER_USER, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
				strCMAppProcessPushed = "False";
			}
			
			if (null != writeIntoFile)
			{
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : setCMAppProcessUserContext --> " + java.util.Calendar.getInstance().getTime());
				_LOGGER.debug("PWCCMAppIntegration : setCMAppProcessUserContext --> Exit");
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			if (null != writeIntoFile)
			{
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
				_LOGGER.debug("Exception in PWCCMAppIntegration : setCMAppProcessUserContext :-"+ex.getMessage());
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		
		return strCMAppProcessPushed;
		
	}
	
	/**
	* This method is to get the list of issue ids.
	* @param context
	* @param args
	* @returns MapList
	* @throws Exception if the operation fails
	*/
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCMAppIssueIdList(Context context, String[] args) throws Exception{
		MapList mlReturn = new MapList();
		try{
			//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report			
			StringList slSelectable = new StringList();
			slSelectable.addElement(DomainObject.SELECT_NAME);
			slSelectable.addElement(DomainObject.SELECT_TYPE);
			String type_HardwareProduct = PropertyUtil.getSchemaProperty(context, "type_HardwareProduct");
			//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report	
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String) paramMap.get("objectId");
			DomainObject dmObj	= DomainObject.newInstance(context, strObjectId);
			//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
			String partNumber = DomainConstants.EMPTY_STRING;
			String partType = DomainConstants.EMPTY_STRING;
			Map mpDetailsMap = dmObj.getInfo(context, slSelectable);
			if(mpDetailsMap != null){
				partNumber = (String) mpDetailsMap.get(DomainObject.SELECT_NAME);
				partType = (String) mpDetailsMap.get(DomainObject.SELECT_TYPE);
				String strJSONIssueIds = IssueIdsTDBD(CMAPP_ISSUE_IDS_TDBD_SERVICE_URL, partNumber, true, partType);
				if(null != strJSONIssueIds && !"".equals(strJSONIssueIds)){
					HashMap mpReturn = new HashMap();
					String[] arrJPOArgs = {strJSONIssueIds};
					mlReturn = buildIssueIdsList(context, arrJPOArgs);
				}
			//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return mlReturn;
	}
	
	//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
	/**
	* This method is to call CM App and get the list of Issue ids/TDBDs.
	* @param context
	* @param Part Number
	* @returns String
	* @throws Exception if the operation fails
	*/
	public String IssueIdsTDBD(String serverURL, String strToFetchWithItem, boolean bGetIssueIds, String partType) throws Exception{
		StringBuffer response = new StringBuffer();
		try{
			if(bGetIssueIds){
				//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
				if(UIUtil.isNotNullAndNotEmpty(partType)){
					if(DomainConstants.TYPE_PART.equals(partType)){
						strToFetchWithItem = "?assy=" + strToFetchWithItem;
					}else{
						strToFetchWithItem = "?model=" + strToFetchWithItem;
					}
				}
				//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
			}else{
				strToFetchWithItem = "?issuerid=" + strToFetchWithItem;
			}
			StringBuffer urlParm = new StringBuffer();
		    urlParm.append(serverURL)
			.append(strToFetchWithItem);
			URL url = new URL(urlParm.toString());
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			// Connection establishment timeout
			c.setConnectTimeout(5000);  
			// Socket read timeout (this should be BIG to prevent HTTP 500 error)
			c.setReadTimeout(300000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
			String line = DomainConstants.EMPTY_STRING;
			while(null != (line = reader.readLine())){
				response.append(line);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return response.toString();
	}
	//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
	
	/**
	* This method is to build the Issue Ids List.
	* @param context
	* @param args
	* @returns StringList
	* @throws Exception if the operation fails
	*/
	public MapList buildIssueIdsList(Context context, String[] args) throws Exception{
		MapList mlReturn = new MapList();
		JSONObject jsonObjIssueIds = new JSONObject();
		JSONArray jaColumnIdentifiers = new JSONArray();
        JSONArray jaTableData = new JSONArray();
        JSONArray jaTableDataInd = null;
        String strColIdentifier = DomainConstants.EMPTY_STRING;
        String strIssueId = DomainConstants.EMPTY_STRING;
        String strModelNo = DomainConstants.EMPTY_STRING;
        String strTopAssy = DomainConstants.EMPTY_STRING;
        String strTDBDDate = DomainConstants.EMPTY_STRING;
        String strComment = DomainConstants.EMPTY_STRING;
        int iIssueIdIndex = 0;
        int iModelIndex = 0;
        int iTopAssyIndex = 0;
        int iTDBDDateIndex = 0;
        int iCommentIndex = 0;
        Map mpReturn = new HashMap();
		try{
			String strDecodedIssueIds = args[0];
			if(null != strDecodedIssueIds){
				//byte[] decoded = Base64.decodeBase64(strDecodedIssueIds.getBytes()); 
				//strDecodedIssueIds = new String(decoded);
				jsonObjIssueIds = new JSONObject(strDecodedIssueIds);
				if(null != jsonObjIssueIds){
					// Identifying the issue id index
					jaColumnIdentifiers = jsonObjIssueIds.getJSONArray(JSON_COLUMN_IDENTIFIERS);
					if(jaColumnIdentifiers.length() > 0){
		            	for(int i = 0; i < jaColumnIdentifiers.length(); i++){
		            		strColIdentifier = jaColumnIdentifiers.getString(i);
		            		if("TDBD_ISSUE_ID".equals(strColIdentifier)){
		            			iIssueIdIndex = i;
			            	}else if("MODEL_NAME".equals(strColIdentifier)){
			            		iModelIndex = i;
			            	}else if("TOP_ASSY_NO".equals(strColIdentifier)){
			            		iTopAssyIndex = i;
			            	}else if("TDBD_DATE".equals(strColIdentifier)){
			            		iTDBDDateIndex = i;
			            	}else if("ISSUE_COMMENT".equals(strColIdentifier)){
			            		iCommentIndex = i;
			            	}
			            }
					}
					
					jaTableData = jsonObjIssueIds.getJSONArray(JSON_TABLE_DATA);
					for (int i = 0; i < jaTableData.length(); i++){
						mpReturn = new HashMap();
						jaTableDataInd = jaTableData.getJSONArray(i);
						strIssueId = jaTableDataInd.getString(iIssueIdIndex);
						if("null" == strIssueId){
							strIssueId = "";
						}
						strModelNo = jaTableDataInd.getString(iModelIndex);
						if("null" == strModelNo){
							strModelNo = "";
						}
						strTopAssy = jaTableDataInd.getString(iTopAssyIndex);
						if("null" == strTopAssy){
							strTopAssy = "";
						}
						strTDBDDate = jaTableDataInd.getString(iTDBDDateIndex);
						if("null" == strTDBDDate){
							strTDBDDate = "";
						}
						strComment = jaTableDataInd.getString(iCommentIndex);
						if("null" == strComment){
							strComment = "";
						}
						mpReturn.put("id", strIssueId);
						mpReturn.put("model", strModelNo);
						mpReturn.put("topAssy", strTopAssy);
						mpReturn.put("tdbdDate", strTDBDDate);
						mpReturn.put("comment", strComment);
						mlReturn.add(mpReturn);
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return mlReturn;
	}
	
	/**
	* This method is to get the list of issue ids.
	* @param context
	* @param args
	* @returns StringList
	* @throws Exception if the operation fails
	*/
	public StringList getIssueIds(Context context, String[] args) throws Exception{
		StringList slReturn = new StringList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
	        MapList mlIssueIdsList = (MapList) programMap.get("objectList");
	        for (int i = 0; i < mlIssueIdsList.size(); i++){
	            Map mpIssueId = (Map) mlIssueIdsList.get(i);
	            String strIssueId = (String) mpIssueId.get("id");
	            slReturn.addElement(strIssueId);
	        }
		}catch(Exception ex){
			ex.printStackTrace();
		}
        return slReturn;
    }
	
	/**
	* This method is to get the list of Model Numbers.
	* @param context
	* @param args
	* @returns StringList
	* @throws Exception if the operation fails
	*/
	public StringList getModelNums(Context context, String[] args) throws Exception{
		StringList slReturn = new StringList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
	        MapList mlIssueIdsList = (MapList) programMap.get("objectList");
	        for (int i = 0; i < mlIssueIdsList.size(); i++){
	            Map mpIssueId = (Map) mlIssueIdsList.get(i);
	            String strModel = (String) mpIssueId.get("model");
	            slReturn.addElement(strModel);
	        }
		}catch(Exception ex){
			ex.printStackTrace();
		}
        return slReturn;
    }
	
	/**
	* This method is to get the list of Top assembly.
	* @param context
	* @param args
	* @returns StringList
	* @throws Exception if the operation fails
	*/
	public StringList getTopAssembly(Context context, String[] args) throws Exception{
		StringList slReturn = new StringList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
	        MapList mlIssueIdsList = (MapList) programMap.get("objectList");
	        for (int i = 0; i < mlIssueIdsList.size(); i++){
	            Map mpIssueId = (Map) mlIssueIdsList.get(i);
	            String strTopAssy = (String) mpIssueId.get("topAssy");
	            slReturn.addElement(strTopAssy);
	        }
		}catch(Exception ex){
			ex.printStackTrace();
		}
        return slReturn;
    }
	
	/**
	* This method is to get the list of TDBD dates.
	* @param context
	* @param args
	* @returns StringList
	* @throws Exception if the operation fails
	*/
	public StringList getTDBDDates(Context context, String[] args) throws Exception{
		StringList slReturn = new StringList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
	        MapList mlIssueIdsList = (MapList) programMap.get("objectList");
	        for (int i = 0; i < mlIssueIdsList.size(); i++){
	            Map mpIssueId = (Map) mlIssueIdsList.get(i);
	            String strTDBDDate = (String) mpIssueId.get("tdbdDate");
	            slReturn.addElement(strTDBDDate);
	        }
		}catch(Exception ex){
			ex.printStackTrace();
		}
        return slReturn;
    }
	
	/**
	* This method is to get the list of Issue comments.
	* @param context
	* @param args
	* @returns StringList
	* @throws Exception if the operation fails
	*/
	public StringList getIssueComments(Context context, String[] args) throws Exception{
		StringList slReturn = new StringList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
	        MapList mlIssueIdsList = (MapList) programMap.get("objectList");
	        for (int i = 0; i < mlIssueIdsList.size(); i++){
	            Map mpIssueId = (Map) mlIssueIdsList.get(i);
	            String strComment = (String) mpIssueId.get("comment");
	            slReturn.addElement(strComment);
	        }
		}catch(Exception ex){
			ex.printStackTrace();
		}
        return slReturn;
    }
	
	/**
	* This method is to generate the weight report.
	* @param context
	* @param args
	* @returns Workbook
	* @throws Exception if the operation fails
	*/
	public Workbook generateWeightReport(Context context, String[] args) throws Exception{
	System.out.println("--------- Start of generateWeightReport-------------");
		Workbook workbook = null;
		JSONObject jsonTDBDplusWeight = new JSONObject();
		try{
			String strObjectId = args[0];
			String strIssueId = args[1];
			String strJSONTDBD = IssueIdsTDBD(CMAPP_ISSUE_IDS_TDBD_SERVICE_URL, strIssueId, false, null);
			if(null != strJSONTDBD && !"".equals(strJSONTDBD)){
				workbook = new XSSFWorkbook();
				String[] arrJPOArgs = {strJSONTDBD};
				jsonTDBDplusWeight = buildTDBDplusWeightJsonObject(context, arrJPOArgs);
				if(null != jsonTDBDplusWeight && jsonTDBDplusWeight.length()>0){
					workbook = buildExcelReport(context, jsonTDBDplusWeight);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("--------- End of generateWeightReport-------------");
        return workbook;
    }
	
	/**
	* This method is to build a json object having TDBD details and weight attributes.
	* @param context
	* @param args
	* @returns JSONObject
	* @throws Exception if the operation fails
	*/
	public JSONObject buildTDBDplusWeightJsonObject(Context context, String[] args) throws Exception{
		JSONObject jsonObjReturn = new JSONObject();
		JSONObject jsonObjTDBD = new JSONObject();
		JSONArray jaColumnIdentifiers = new JSONArray();
        JSONArray jaTableData = new JSONArray();
        JSONArray jaTableDataInd = null;
        JSONArray jaTDBDInd = new JSONArray();
        String strColIdentifier = DomainConstants.EMPTY_STRING;
        int iLevelIndex = 0;
        int iPartNameIndex = 0;
        int iUIDIndex = 0;
        int iQuantityIndex = 0;
        int iDocNumIndex = 0;
        int iFindNumIndex = 0;
		int iParentPartNameIndex =0;
      //  MapList mlPart = new MapList();
        Map mpPart = new HashMap();
		MapList mlExistingPart = new MapList();
        String strLevel = DomainConstants.EMPTY_STRING;
        String strPartNumber = DomainConstants.EMPTY_STRING;
		String strUID = DomainConstants.EMPTY_STRING;
		String strDescription = DomainConstants.EMPTY_STRING;
		String strRevision = DomainConstants.EMPTY_STRING;
		String strQuantity = DomainConstants.EMPTY_STRING;
	  	String strDocNum = DomainConstants.EMPTY_STRING;
	  	String strFindNum = DomainConstants.EMPTY_STRING;
	  	String strModuleCode = DomainConstants.EMPTY_STRING;
		String strPartnerName = DomainConstants.EMPTY_STRING;
	  	String strItemWeight = DomainConstants.EMPTY_STRING;
	  	String strMaxWeight = DomainConstants.EMPTY_STRING;
	  	String strWeightCode = DomainConstants.EMPTY_STRING;
	  	String strWeightComment = DomainConstants.EMPTY_STRING;
	  	String strRolledUpWeight = DomainConstants.EMPTY_STRING;
	  	String strWeightFidelityMargin = DomainConstants.EMPTY_STRING;
	  	String strWeightTRLMargin = DomainConstants.EMPTY_STRING;
	  	String strType = DomainConstants.EMPTY_STRING;
	  	String strSaleCode = DomainConstants.EMPTY_STRING;
	  	String strSaleableCode = DomainConstants.EMPTY_STRING;
	  	String strState = DomainConstants.EMPTY_STRING;
	  	String strUOM = DomainConstants.EMPTY_STRING;
	  	String strPartId = DomainConstants.EMPTY_STRING;
		String strParentPartName = DomainConstants.EMPTY_STRING;
	  	DomainObject doPart = null;
		try{
			String strDecodedTDBD = args[0];
			if(null != strDecodedTDBD){
				//byte[] decoded = Base64.decodeBase64(strDecodedTDBD.getBytes()); 
				//strDecodedTDBD = new String(decoded);
				jsonObjTDBD = new JSONObject(strDecodedTDBD);
				if(null != jsonObjTDBD){
					// Identifying the indexes
					jaColumnIdentifiers = jsonObjTDBD.getJSONArray(JSON_COLUMN_IDENTIFIERS);
					if(jaColumnIdentifiers.length() > 0){
		            	for(int i = 0; i < jaColumnIdentifiers.length(); i++){
		            		strColIdentifier = jaColumnIdentifiers.getString(i);
		            		if("TD_LEVEL".equals(strColIdentifier)){
		            			iLevelIndex = i;
			            	}else if("PART_NAME".equals(strColIdentifier)){
			            		iPartNameIndex = i;
			            	}else if("PWC_UID".equals(strColIdentifier)){
			            		iUIDIndex = i;
			            	}else if("QUANTITY".equals(strColIdentifier)){
			            		iQuantityIndex = i;
			            	}else if("DOCUMENT_NO".equals(strColIdentifier)){
			            		iDocNumIndex = i;
			            	}else if("FIND_NUMBER".equals(strColIdentifier)){
			            		iFindNumIndex = i;
			            	}
							else if("PARENT_PART_NAME".equals(strColIdentifier))
							{
								iParentPartNameIndex = i;
							}
			            }
					}
				
					jaTableData = jsonObjTDBD.getJSONArray(JSON_TABLE_DATA);
					int jaTableDataSize = jaTableData.length();
					Map mpPartData = new HashMap();
					for (int i = 0; i < jaTableDataSize; i++){
						 strModuleCode = DomainConstants.EMPTY_STRING;
						 strPartnerName = DomainConstants.EMPTY_STRING;
						 strItemWeight = DomainConstants.EMPTY_STRING;
						 strMaxWeight = DomainConstants.EMPTY_STRING;
						 strWeightCode = DomainConstants.EMPTY_STRING;
						 strWeightComment = DomainConstants.EMPTY_STRING;
						 strRolledUpWeight = DomainConstants.EMPTY_STRING;
						 strWeightFidelityMargin = DomainConstants.EMPTY_STRING;
						 strWeightTRLMargin = DomainConstants.EMPTY_STRING;
						 strSaleCode = DomainConstants.EMPTY_STRING;
						 strSaleableCode = DomainConstants.EMPTY_STRING;
						 strUOM = DomainConstants.EMPTY_STRING;
						 strDescription = DomainConstants.EMPTY_STRING;
						strRevision = DomainConstants.EMPTY_STRING;
						 strType = DomainConstants.EMPTY_STRING;
						 strState = DomainConstants.EMPTY_STRING;
						 
						jaTableDataInd = jaTableData.getJSONArray(i);
						strLevel = jaTableDataInd.getString(iLevelIndex);
						strPartNumber = jaTableDataInd.getString(iPartNameIndex).trim();
						strUID = jaTableDataInd.getString(iUIDIndex).trim();
						strQuantity = jaTableDataInd.getString(iQuantityIndex);
						strDocNum = jaTableDataInd.getString(iDocNumIndex);
						strFindNum = jaTableDataInd.getString(iFindNumIndex);
						strParentPartName = jaTableDataInd.getString(iParentPartNameIndex).trim();
						
						
						if(UIUtil.isNotNullAndNotEmpty(strPartNumber)){
						System.out.println("strPartNumber=======>>>" + strPartNumber);
							MapList mlFilteredList =  new MapList();
							MapList mlPart = new MapList();
							if(!mpPartData.containsKey(strPartNumber))
							{
								date = new Date();
							
								mlPart = getWeightAttributesOfLastRevisionPart(context, strPartNumber);
								date = new Date();
							
								mpPartData.put(strPartNumber,mlPart);
							}
							else
							{
								mlPart = (MapList) mpPartData.get(strPartNumber);
							}
							mlFilteredList = getFilteredData(context,mlPart,strPartNumber,strUID,strParentPartName);
							
							if(mlFilteredList.size() > 0){
							
								mpPart = (Map) mlFilteredList.get(0);
								
								strModuleCode = (String) mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_MODULE_CODE+"]");
								strPartnerName = (String) mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_PARTNERSHIP_NAME+"]");
								strItemWeight = (String) mpPart.get(SELECTABLE_ATTR_PWC_ITEM_WEIGHT);
								strMaxWeight = (String) mpPart.get(SELECTABLE_ATTR_PWC_MAX_WEIGHT);
								strWeightCode = (String) mpPart.get(SELECTABLE_ATTR_PWC_WEIGHT_CODE);
								strWeightComment = (String) mpPart.get(SELECTABLE_ATTR_PWC_WEIGHT_COMMENT);
								strRolledUpWeight = (String) mpPart.get(SELECTABLE_ATTR_PWC_ROLLED_UP_WEIGHT);
								strWeightFidelityMargin = (String) mpPart.get(SELECTABLE_ATTR_PWC_WEIGHT_FIDELITY_MARGIN);
								strWeightTRLMargin = (String) mpPart.get(SELECTABLE_ATTR_PWC_WEIGHT_TRL_MARGIN);
								strType = (String) mpPart.get(DomainObject.SELECT_TYPE);
								strRevision = (String) mpPart.get(DomainObject.SELECT_REVISION);
								strSaleCode = (String) mpPart.get(SELECTABLE_ATTR_PWC_SALE_CODE);
								
								strSaleableCode = (String)mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_SALEABLE_CODE+"]");
								strState = (String) mpPart.get(DomainObject.SELECT_CURRENT);
								strUOM = (String) mpPart.get(SELECTABLE_ATTR_UNIT_OF_MEASURE);
								strDescription = (String) mpPart.get(DomainObject.SELECT_DESCRIPTION);
							}
						}
						jaTDBDInd = new JSONArray();
						jaTDBDInd.put(strLevel);
						jaTDBDInd.put(strDescription);
						jaTDBDInd.put(strPartNumber);
						jaTDBDInd.put(strRevision);
						jaTDBDInd.put(strUID);
						jaTDBDInd.put(strQuantity);
						jaTDBDInd.put(strItemWeight);
						jaTDBDInd.put(strMaxWeight);
						jaTDBDInd.put(strWeightCode);
						if(null == strModuleCode )
						{
						jaTDBDInd.put(DomainConstants.EMPTY_STRING);
						}
						else
						{
							jaTDBDInd.put(strModuleCode);
						}
						
						if(null == strPartnerName )
						{
							jaTDBDInd.put(DomainConstants.EMPTY_STRING);
						}
						else
						{
						
								jaTDBDInd.put(strPartnerName);
						}
						
						jaTDBDInd.put(strWeightComment);
						jaTDBDInd.put(strRolledUpWeight);
						jaTDBDInd.put(strFindNum);
						jaTDBDInd.put(strWeightFidelityMargin);
						jaTDBDInd.put(strWeightTRLMargin);
						jaTDBDInd.put(strType);
						jaTDBDInd.put(strSaleCode);
						if(null == strSaleableCode)
						{
							jaTDBDInd.put(DomainConstants.EMPTY_STRING);
						}
						else
						{
						
							jaTDBDInd.put(strSaleableCode);
						}
						jaTDBDInd.put(strState);
						jaTDBDInd.put(strUOM);
						jaTDBDInd.put(strDocNum);
						jsonObjReturn.put(Integer.toString(i), jaTDBDInd);
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return jsonObjReturn;
	}
	
	/**
	* This method is to get the weight attributes of latest revision part.
	* @param context
	* @param args
	* @returns MapList
	* @throws Exception if the operation fails
	*/
	public MapList getWeightAttributesOfLastRevisionPart(Context context, String strPartName) throws Exception{
		MapList mlReturn = new MapList();
		try{
			StringList objectSelects = new StringList();
			objectSelects.addElement(DomainObject.SELECT_ID);
		    objectSelects.addElement(DomainObject.SELECT_TYPE);
		    objectSelects.addElement(DomainObject.SELECT_REVISION);
		    objectSelects.addElement(DomainObject.SELECT_CURRENT);
		    objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
		    objectSelects.addElement(SELECTABLE_ATTR_PWC_ITEM_WEIGHT);
		    objectSelects.addElement(SELECTABLE_ATTR_PWC_MAX_WEIGHT);
		    objectSelects.addElement(SELECTABLE_ATTR_PWC_WEIGHT_CODE);
		    objectSelects.addElement(SELECTABLE_ATTR_PWC_WEIGHT_COMMENT);
		    objectSelects.addElement(SELECTABLE_ATTR_PWC_ROLLED_UP_WEIGHT);
		    objectSelects.addElement(SELECTABLE_ATTR_PWC_WEIGHT_FIDELITY_MARGIN);
		    objectSelects.addElement(SELECTABLE_ATTR_PWC_WEIGHT_TRL_MARGIN);
		    objectSelects.addElement(SELECTABLE_ATTR_PWC_SALE_CODE);
		   
		
			
		    objectSelects.addElement(SELECTABLE_ATTR_UNIT_OF_MEASURE);
			
			//Modified for HEAT-C-18499 : Start
			
			//Added attributes in multivalue list
			DomainObject.MULTI_VALUE_LIST.add("to["+DomainConstants.RELATIONSHIP_EBOM+"].from.name");
			DomainObject.MULTI_VALUE_LIST.add("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_MODULE_CODE+"]");
			DomainObject.MULTI_VALUE_LIST.add("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_UID+"]");
			DomainObject.MULTI_VALUE_LIST.add("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_SALEABLE_CODE+"]");
			DomainObject.MULTI_VALUE_LIST.add("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_PARTNERSHIP_NAME+"]");
			objectSelects.addElement("to["+DomainConstants.RELATIONSHIP_EBOM+"|type == "+DomainConstants.RELATIONSHIP_EBOM+"].from.name");
			objectSelects.addElement("to["+DomainConstants.RELATIONSHIP_EBOM+"|type == "+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_MODULE_CODE+"]");
			objectSelects.addElement("to["+DomainConstants.RELATIONSHIP_EBOM+"|type == "+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_UID+"]");
			objectSelects.addElement("to["+DomainConstants.RELATIONSHIP_EBOM+"|type == "+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_SALEABLE_CODE+"]");
			objectSelects.addElement("to["+DomainConstants.RELATIONSHIP_EBOM+"|type == "+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_PARTNERSHIP_NAME+"]");
			
			
			// Hiding below attribute queries since its impacting performance. Need to revisit in 3.19.0
			/*
			if(UIUtil.isNotNullAndNotEmpty(strUID) && !"null".equals(strUID) && UIUtil.isNotNullAndNotEmpty(strParentPartName) && !"null".equals(strParentPartName))
			{
				objectSelects.addElement("to["+DomainConstants.RELATIONSHIP_EBOM+"|(attribute["+ATTR_PWC_UID+"] == '"+strUID+"' && from.name=='"+strParentPartName+"')].attribute["+ATTR_PWC_MODULE_CODE+"]");
				objectSelects.addElement("to["+DomainConstants.RELATIONSHIP_EBOM+"|(attribute["+ATTR_PWC_UID+"] == '"+strUID+"' && from.name=='"+strParentPartName+"')].attribute["+ATTR_PWC_SALEABLE_CODE+"]");
			}
			*/
			date = new Date();
			System.out.println("Time Before==getWeightAttributesOfLastRevisionPart()==Part-strPartName==>>>" + dateFormat.format(date));
		
		   String strWhrClause = "revision == last";
		   String vault = STR_PRODUCTION_VAULT + "," + STR_LEGACY_VAULT;
			mlReturn= DomainObject.findObjects(context, 
	               		DomainConstants.TYPE_PART, 
	               		strPartName,
	               		DomainConstants.QUERY_WILDCARD, 
	               		DomainConstants.QUERY_WILDCARD, 
	               		vault, 
	               		strWhrClause, 
	               		false, 
	               		objectSelects);
			
			//Removing attributes from multivalue list
			DomainObject.MULTI_VALUE_LIST.remove("to["+DomainConstants.RELATIONSHIP_EBOM+"].from.name");
			DomainConstants.MULTI_VALUE_LIST.remove("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_MODULE_CODE+"]");
			DomainConstants.MULTI_VALUE_LIST.remove("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_UID+"]");
			DomainConstants.MULTI_VALUE_LIST.remove("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_SALEABLE_CODE+"]");
			DomainObject.MULTI_VALUE_LIST.remove("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_PARTNERSHIP_NAME+"]");
			
			date = new Date();
			System.out.println("Time After==getWeightAttributesOfLastRevisionPart()==>>>" + dateFormat.format(date));
				
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			//Removing attributes from multivalue list
			DomainObject.MULTI_VALUE_LIST.remove("to["+DomainConstants.RELATIONSHIP_EBOM+"].from.name");
			DomainConstants.MULTI_VALUE_LIST.remove("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_MODULE_CODE+"]");
			DomainConstants.MULTI_VALUE_LIST.remove("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_UID+"]");
			DomainConstants.MULTI_VALUE_LIST.remove("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_SALEABLE_CODE+"]");
			DomainObject.MULTI_VALUE_LIST.remove("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_PARTNERSHIP_NAME+"]");
		}	
		//Modified for HEAT-C-18499 : End
	
		return mlReturn;
	}
	/**
	* This method is to get filtered data of latest revision part.
	* @param context
	* @param MapList contains all data regarding Part
	* @param String as Part Name from JSON
	* @param String as UID from JSON
	* @param String as Parent Part Name from JSON
	* @returns MapList
	* @throws Exception if the operation fails
	*/
		public MapList getFilteredData(Context context, MapList mlReturn, String strPartName, String strUID , String strParentPartName) throws Exception{
		
		MapList mlReturnList = new MapList();
		Map mpNewMap= new HashMap();
		try{
			
			if(!mlReturn.isEmpty()){
					Map mpPart = (Map) mlReturn.get(0);
					mpNewMap.put(DomainObject.SELECT_ID, (String) mpPart.get(DomainObject.SELECT_ID));
					mpNewMap.put(SELECTABLE_ATTR_PWC_ITEM_WEIGHT, (String) mpPart.get(SELECTABLE_ATTR_PWC_ITEM_WEIGHT));
					mpNewMap.put(SELECTABLE_ATTR_PWC_MAX_WEIGHT,(String) mpPart.get(SELECTABLE_ATTR_PWC_MAX_WEIGHT));
					mpNewMap.put(SELECTABLE_ATTR_PWC_WEIGHT_CODE, (String) mpPart.get(SELECTABLE_ATTR_PWC_WEIGHT_CODE));
					mpNewMap.put(SELECTABLE_ATTR_PWC_WEIGHT_COMMENT,(String) mpPart.get(SELECTABLE_ATTR_PWC_WEIGHT_COMMENT));
					mpNewMap.put(SELECTABLE_ATTR_PWC_ROLLED_UP_WEIGHT, (String) mpPart.get(SELECTABLE_ATTR_PWC_ROLLED_UP_WEIGHT));
					mpNewMap.put(SELECTABLE_ATTR_PWC_WEIGHT_FIDELITY_MARGIN, (String) mpPart.get(SELECTABLE_ATTR_PWC_WEIGHT_FIDELITY_MARGIN));
					mpNewMap.put(SELECTABLE_ATTR_PWC_WEIGHT_TRL_MARGIN,(String) mpPart.get(SELECTABLE_ATTR_PWC_WEIGHT_TRL_MARGIN));
					mpNewMap.put(DomainObject.SELECT_TYPE,(String) mpPart.get(DomainObject.SELECT_TYPE));
					mpNewMap.put(DomainObject.SELECT_REVISION,(String) mpPart.get(DomainObject.SELECT_REVISION));
					mpNewMap.put(SELECTABLE_ATTR_PWC_SALE_CODE,(String) mpPart.get(SELECTABLE_ATTR_PWC_SALE_CODE));
					mpNewMap.put(DomainObject.SELECT_CURRENT,(String) mpPart.get(DomainObject.SELECT_CURRENT));
					mpNewMap.put(SELECTABLE_ATTR_UNIT_OF_MEASURE,(String) mpPart.get(SELECTABLE_ATTR_UNIT_OF_MEASURE));
					mpNewMap.put(DomainObject.SELECT_DESCRIPTION,(String) mpPart.get(DomainObject.SELECT_DESCRIPTION));
					String strModuleCode = DomainConstants.EMPTY_STRING; 
					String strSaleableCode = DomainConstants.EMPTY_STRING; 
					String strPartnerName = DomainConstants.EMPTY_STRING;
					//Check if Parent Part name and UID from JSON are not empty or Null
					if(UIUtil.isNotNullAndNotEmpty(strUID) && !"null".equals(strUID) && UIUtil.isNotNullAndNotEmpty(strParentPartName) && !"null".equals(strParentPartName))
					{
						//get all the attributes in stringlist
						/*StringList slEBOMFromNames = (StringList) mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].from.name");
						StringList slModuleCodes = (StringList) mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_MODULE_CODE+"]");
						StringList slUIDs = (StringList) mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_UID+"]");
						StringList slSaleableCodes = (StringList) mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_SALEABLE_CODE+"]");
						StringList slPartnerNames = (StringList) mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_PARTNERSHIP_NAME+"]");*/
						StringList slEBOMFromNames = new StringList();
						StringList slModuleCodes = new StringList();
						StringList slUIDs = new StringList();
						StringList slSaleableCodes = new StringList();
						StringList slPartnerNames = new StringList();
						Object objslEBOMFromNames = (Object)mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].from.name");
						Object objslModuleCodes = (Object)mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_MODULE_CODE+"]");
						Object objslUIDs = (Object)mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_UID+"]");
						Object objslSaleableCodes = (Object)mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_SALEABLE_CODE+"]");
						Object objslPartnerNames = (Object)mpPart.get("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_PARTNERSHIP_NAME+"]");
						if(objslEBOMFromNames instanceof String)
						{
							slEBOMFromNames.add((String)objslEBOMFromNames);
						}else if(objslEBOMFromNames instanceof StringList)
						{
							slEBOMFromNames = (StringList)objslEBOMFromNames;
						}
						if(objslModuleCodes instanceof String)
						{
							slModuleCodes.add((String)objslModuleCodes);
						}else if(objslModuleCodes instanceof StringList)
						{
							slModuleCodes = (StringList)objslModuleCodes;
						}
						
						if(objslUIDs instanceof String)
						{
							slUIDs.add((String)objslUIDs);
						}else if(objslUIDs instanceof StringList)
						{
							slUIDs = (StringList)objslUIDs;
						}
						
						if(objslSaleableCodes instanceof String)
						{
							slSaleableCodes.add((String)objslSaleableCodes);
						}else if(objslSaleableCodes instanceof StringList)
						{
							slSaleableCodes = (StringList)objslSaleableCodes;
						}
						
						if(objslPartnerNames instanceof String)
						{
							slPartnerNames.add((String)objslPartnerNames);
						}else if(objslPartnerNames instanceof StringList)
						{
							slPartnerNames = (StringList)objslPartnerNames;
						}
						StringList slModuleCode = new StringList(); 
						HashSet hsetModuleCode = new HashSet();
						HashSet hsetSaleCode = new HashSet();
						HashSet hsetPartnerName = new HashSet();
						StringList slSaleableCode = new StringList(); 
						StringList slPartnerName = new StringList(); 
						
						// if From part name stringlist and UID stringlist are not null
						if(null !=slEBOMFromNames && null !=slUIDs)
						{
							int iEBOMFromNameSize = slEBOMFromNames.size(); // from name stringlist size
							int iUIDSize = slUIDs.size(); // UID stringlist size
							for(int i=0;i<iEBOMFromNameSize;i++)
							{
								String strFromPartName = (String)slEBOMFromNames.get(i); // get from part name
								String strUIDCode = (String)slUIDs.get(i);	 // get UID
								// Check if UID and from part name are not null or empty
								if(UIUtil.isNotNullAndNotEmpty(strUIDCode) && !"null".equals(strUIDCode) && UIUtil.isNotNullAndNotEmpty(strFromPartName) && !"null".equals(strFromPartName))
									{
										// Check if UID and from part name from JSON are equal to relationship attribute value then get Module code and Salebale code 
										if(strUIDCode.equals(strUID) && strFromPartName.equals(strParentPartName))
												{
													String strModuleCodeVal =(String)slModuleCodes.get(i);
													String strSaleableCodeVal =(String)slSaleableCodes.get(i);
													String strPartnerNameVal =(String)slPartnerNames.get(i);
													if(UIUtil.isNotNullAndNotEmpty(strModuleCodeVal))
													{
														hsetModuleCode.add(strModuleCodeVal);
													}
													if(UIUtil.isNotNullAndNotEmpty(strSaleableCodeVal))
													{
														hsetSaleCode.add(strSaleableCodeVal);
													}
													if(UIUtil.isNotNullAndNotEmpty(strPartnerNameVal))
													{
														hsetPartnerName.add(strPartnerNameVal);
													}
												}
									}
							}
							slModuleCode.addAll(hsetModuleCode);
							slSaleableCode.addAll(hsetSaleCode);
							slPartnerName.addAll(hsetPartnerName);
						}
						//last comma(,) removed from module code and saleable code
						if(!slModuleCode.isEmpty() && slModuleCode.size() > 1)
						{
								strModuleCode = StringUtils.join(slModuleCode,",");
						}
						else if(!slModuleCode.isEmpty() && slModuleCode.size() == 1)
						{
								strModuleCode = (String)slModuleCode.get(0);
						}
						if(!slSaleableCode.isEmpty() && slSaleableCode.size() > 1)
						{
								strSaleableCode = StringUtils.join(slSaleableCode,",");
						}
						else if(!slSaleableCode.isEmpty() && slSaleableCode.size() == 1)
						{
								strSaleableCode = (String)slSaleableCode.get(0);
						}
						
						if(!slPartnerName.isEmpty() && slPartnerName.size() > 1)
						{
								strPartnerName = StringUtils.join(slPartnerName,",");
						}
						else if(!slPartnerName.isEmpty() && slPartnerName.size() == 1)
						{
								strPartnerName = (String)slPartnerName.get(0);
						}
					}
					// put module code and saleable code in Map
					mpNewMap.put("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_MODULE_CODE+"]",strModuleCode);
					mpNewMap.put("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_SALEABLE_CODE+"]",strSaleableCode);
					mpNewMap.put("to["+DomainConstants.RELATIONSHIP_EBOM+"].attribute["+ATTR_PWC_PARTNERSHIP_NAME+"]",strPartnerName);
					mlReturnList.add(mpNewMap);
				}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//Modified for HEAT-C-18499 : End
	
		return mlReturnList;
		}
	
	/**
	* This method is to build the excel report.
	* @param context
	* @param args
	* @returns Workbook
	* @throws Exception if the operation fails
	*/
	public Workbook buildExcelReport(Context context, JSONObject jsonTDBDplusWeight) throws Exception{
		Workbook workbook = new XSSFWorkbook();
		JSONArray jaTDBDplusWeight = new JSONArray();
		try{
			if(null != jsonTDBDplusWeight){
				Sheet sheet = null;
				Row row = null;
				Cell cell = null;
				CellStyle cellStyle = null;
				XSSFFont font = null;
				sheet = workbook.createSheet("Report");
				sheet.setColumnWidth(2, 7000);
				sheet.setColumnWidth(3, 4000);
    			sheet.setColumnWidth(5, 3000);
    			sheet.setColumnWidth(21, 3000);
				// Modified  : Removed first row and first column : Start
				StringList slWeightReportRow2Header = new StringList();
				String strWeightReportRow2Header = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.WeightReport.Row2.Header");
				if(null != strWeightReportRow2Header && !"".equals(strWeightReportRow2Header)){
					slWeightReportRow2Header = FrameworkUtil.split(strWeightReportRow2Header.trim(), ",");
				}
				String strCMApp = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.WeightReport.Application.CMApp").trim();
				String strEnovia = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.WeightReport.Application.Enovia").trim();
				String strColumn = DomainConstants.EMPTY_STRING;

				if(slWeightReportRow2Header.size() > 0 && jsonTDBDplusWeight.length() > 0){
					// Row 1
					row = sheet.createRow(0);
					row.setHeight((short) 1200);
					for(int i = 0; i < slWeightReportRow2Header.size()-1; i++){
					  cell = row.createCell(i);
					  cellStyle = workbook.createCellStyle();
					   cellStyle.setWrapText(true);
					  cell.setCellStyle(cellStyle);
					  cell.setCellValue((String) slWeightReportRow2Header.get(i+1));
					}
					// Data Rows
					String strValue = DomainConstants.EMPTY_STRING;
					int jsonTDBDplusWeightSize = jsonTDBDplusWeight.length();
					for(int i = 0; i < jsonTDBDplusWeightSize; i++){
						jaTDBDplusWeight = (JSONArray) jsonTDBDplusWeight.get(Integer.toString(i));
						row = sheet.createRow(i + 1);
						for(int j = 0; j < jaTDBDplusWeight.length(); j++){
							cell = row.createCell(j);
							cellStyle = workbook.createCellStyle();
							cell.setCellStyle(cellStyle);
							strValue = jaTDBDplusWeight.getString(j);
							if("null" == strValue){
								strValue = "";
							}
							cell.setCellValue(strValue);
						}
					}

				}
					// Modified  : Removed first row and first column : End
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return workbook;
	}
	
	//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	/**
	 * Use to set the log file path and layout.
	 * @param Log File Name
	 * @param args
	 * @returns File
	 * @throws Exception if the operation fails
	 */
	public File setLoggerPath(String sLogFileName)
	{
		File logFile = null;
		FileAppender appender = new FileAppender();
		try
		{
			logFile = getLogFile("PWCCMApp"+System.getProperty("file.separator")+sLogFileName);
			PatternLayout layout = new PatternLayout("%m%n");
			
			Enumeration enu=_LOGGER.getAllAppenders();
			while( enu.hasMoreElements() ) {
				appender = (FileAppender)enu.nextElement();
				appender.close();
			}
			
			FileAppender appender1 = new FileAppender(layout, logFile.getAbsolutePath(), true);
			appender1.setAppend(true);
			_LOGGER.addAppender(appender1);
			_LOGGER.setAdditivity(false);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return logFile;
	}

	/**
	 * Use to check the log file folder structure is available or not, if not then create new.
	 * @param sLogFileName contains the name of log file
	 * @return file contains the absolute path of log file
	 * @throws IOException if the operation fail
	 */
	public File getLogFile(String sLogFileName) throws IOException
	{
		File fileExecutionLog = null;
		try
		{
			String strSystemCommonLogFilePath = System.getenv("PWC_ENOVIA_LOG_FOLDER_PATH");
			if(UIUtil.isNotNullAndNotEmpty(strSystemCommonLogFilePath))
			{
				if(System.getProperty("file.separator").toCharArray()[0] == strSystemCommonLogFilePath.charAt(strSystemCommonLogFilePath.length() - 1))
				{
					fileExecutionLog = new File(strSystemCommonLogFilePath + sLogFileName);
				} else {
					fileExecutionLog = new File(strSystemCommonLogFilePath + System.getProperty("file.separator") + sLogFileName);
				}
				if(!fileExecutionLog.exists()){
					File parent = fileExecutionLog.getParentFile();
					if (!parent.exists() && !parent.mkdirs()) {
						parent.mkdirs();
					}
					fileExecutionLog.createNewFile();
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return fileExecutionLog;
	}



	//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
	
	//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
	/**
	 * This method is to allow the command to be shown in different views
	 * @param context
	 * @param args
	 * @return boolean
	 * @throws Exception
	 */
	 public boolean showHideTDBDReportGenerationCmd(Context context, String args[]) throws Exception{
		boolean boolAccess = true;
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strObjectId = (String)programMap.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
				DomainObject domObj = DomainObject.newInstance(context, strObjectId);
				String strObjectType = domObj.getInfo(context, DomainConstants.SELECT_TYPE);
				if(UIUtil.isNotNullAndNotEmpty(strObjectType) && PWCConstants.TYPE_MODEL.equals(strObjectType)){
					boolAccess = false;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
		return boolAccess;
	 }
	 
	 	/**
		* This method is to generate the TDBD report.
		* @param context
		* @param args
		* @returns Workbook
		* @throws Exception if the operation fails
		*/
		public Workbook generateTDBDReport(Context context, String[] args) throws Exception{
			
			Workbook workbook = null;
			JSONObject jsonTDBDplusWeight = new JSONObject();
			try{
				String strObjectId = args[0];
				String strIssueId = args[1];
				StringList slSelectables = new StringList();
				slSelectables.addElement(DomainConstants.SELECT_TYPE);
				slSelectables.addElement(DomainConstants.SELECT_NAME);
				String strObjectType = DomainConstants.EMPTY_STRING;
				String strObjectName = DomainConstants.EMPTY_STRING;
				 if(UIUtil.isNotNullAndNotEmpty(strObjectId) && UIUtil.isNotNullAndNotEmpty(strIssueId)){
					 DomainObject domainObj = DomainObject.newInstance(context, strObjectId);
					 Map mpDetailsMap = domainObj.getInfo(context, slSelectables);
					 strObjectType = (String)mpDetailsMap.get(DomainConstants.SELECT_TYPE);
					 strObjectName = (String)mpDetailsMap.get(DomainConstants.SELECT_NAME);
					 if(PWCConstants.TYPE_MODEL.equals(strObjectType)){
						 String strObjectDetails = getModelNameOrPartNameForIssue(context, strObjectName, strIssueId, strObjectType);
						 if(UIUtil.isNotNullAndNotEmpty(strObjectDetails)){
							 StringList slPartDetails = FrameworkUtil.split(strObjectDetails.trim(), ",");
							 if(slPartDetails != null && slPartDetails.size()>1){
								 strObjectName = (String)slPartDetails.get(0);
								 strObjectId = (String)slPartDetails.get(1);
							 }
						 }
					 }
				StringBuffer sb = new StringBuffer();
				String strJSONTDBD = IssueIdsTDBD(CMAPP_ISSUE_IDS_TDBD_SERVICE_URL, strIssueId, false, null);
				if(null != strJSONTDBD && !"".equals(strJSONTDBD)){
					workbook = new XSSFWorkbook();
					String[] arrJPOArgs = {strJSONTDBD};
					jsonTDBDplusWeight = buildTDBDJsonObject(context, arrJPOArgs);
					if(null != jsonTDBDplusWeight && jsonTDBDplusWeight.length()>0){
						workbook = buildExcelForTDBDReport(context, jsonTDBDplusWeight);
					}
				}
				if(null != workbook && workbook.getNumberOfSheets()>0){
							createDocumentAndCheckInFile(context, strObjectId, workbook, strIssueId, strObjectName, strObjectType);
						}
				 }
			}catch(Exception ex){
				ex.printStackTrace();
			}
	        return workbook;
	    }
		
		/**
		* This method is to create a general document and check-in the file.
		* @param context
		* @param args
		* @returns Void
		* @throws Exception if the operation fails
		*/
		public void createDocumentAndCheckInFile(Context context, String strObjectId, Workbook workbook, String strIssueId, String strObjectName, String strObjectType) throws Exception{
			try{
				String strReportName = DomainConstants.EMPTY_STRING;
				String strName = DomainConstants.EMPTY_STRING;
				String strWhereClause = DomainConstants.EMPTY_STRING;
				String rev = DomainConstants.EMPTY_STRING;
				String FORMAT_VIEWABLE= "Viewable";
				if(null != strObjectId && !DomainConstants.EMPTY_STRING.equals(strObjectId)){
					DomainObject doPart = DomainObject.newInstance(context, strObjectId);
					if(null != workbook && workbook.getNumberOfSheets()>0){
						strReportName = "TDBD"+"_"+strIssueId+"_"+strObjectName+".xlsx";
						strName = "TDBD"+"_"+strIssueId+"_"+strObjectName;
						String strWSPath = context.createWorkspace();
						String strFilePath = ""; 
						strFilePath = strWSPath + "/";
						FileOutputStream fos = new FileOutputStream(strFilePath + strReportName);
						workbook.write(fos);
						fos.close();  
						// Starting transaction
						ContextUtil.startTransaction(context,true);
						// Check-in
						  StringList objGenDocSels = new StringList();
						  objGenDocSels.addElement(DomainConstants.SELECT_ID);
						  StringList relSelects = new SelectList();
						  relSelects.addElement(DomainRelationship.SELECT_ID);
						  matrix.db.File boFile = null;
						  String strCheckedInFile = DomainConstants.EMPTY_STRING;
						  String strStore = "STORE";
						  String strFalse = "false";
						  String strServer = "server";

						  Map mpGenDoc = null;
						  String strGenDocId = DomainConstants.EMPTY_STRING;
							  strWhereClause = "name == '" + strName + "'";
							  MapList mlGenDocList = doPart.getRelatedObjects(context, 
							   		DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT, 
							   		PWCIntegrationConstants.TYPE_GENERAL_DOCUMENT, 
							   		objGenDocSels, 
							   		relSelects, 
							   		false, 
							   		true, 
							   		(short) 1, 
							   		strWhereClause, 
							   		null, 
							   		0);
							  
							  if (mlGenDocList.size() > 0)
							  {
								  mpGenDoc = (Map) mlGenDocList.get(0);
								  strGenDocId = (String) mpGenDoc.get(DomainConstants.SELECT_ID);
							  }else{
								  strGenDocId = findPWCGeneralDocumentByName(context, strName);
								  if(UIUtil.isNullOrEmpty(strGenDocId)){
								  Policy policyObj = new Policy("Document");
						          policyObj.open(context);
						          if(policyObj.hasSequence()){
						        	  rev = policyObj.getFirstInSequence(context);
						          }
								  String strCommand = "add bus " + PWCIntegrationConstants.TYPE_GENERAL_DOCUMENT + " " + strName + " " + rev + " policy Document vault \"" + PWCConstants.PRODUCTION_VAULT + "\"";
								  String strResult = MqlUtil.mqlCommand(context, strCommand);
								  BusinessObject GeneralDocObject = new BusinessObject(PWCIntegrationConstants.TYPE_GENERAL_DOCUMENT, strName, rev, PWCConstants.PRODUCTION_VAULT);
									  if(GeneralDocObject.exists(context))
									  {
										  strGenDocId = GeneralDocObject.getObjectId(context);
										  DomainRelationship.connect(context, strObjectId, RELATIONSHIP_REFERENCE_DOCUMENT, strGenDocId, true);
									  }
								  }else{
									  DomainRelationship.connect(context, strObjectId, RELATIONSHIP_REFERENCE_DOCUMENT, strGenDocId, true);
								  }
							  }
							  
							  FileList fileNameList = new FileList();
							  boolean isGeneric = false;
							  if (UIUtil.isNotNullAndNotEmpty(strGenDocId))
							  {
								  CommonDocument comDocument = new CommonDocument(strGenDocId);
								  fileNameList  = comDocument.getFiles(context, FORMAT_VIEWABLE);
								  if(null == fileNameList || fileNameList.size()==0)
								  {
									  fileNameList  = comDocument.getFiles(context, DomainConstants.FORMAT_GENERIC);
									  if(fileNameList.size()>0)
									  {
										  isGeneric = true;
									  }
								  }
								  Iterator fileItr = fileNameList.iterator();
								  while (fileItr.hasNext())
								  {
									  boFile = (matrix.db.File) fileItr.next();
									  if (null != boFile)
									  {
										  strCheckedInFile = boFile.getName();
										  if (UIUtil.isNotNullAndNotEmpty(strCheckedInFile) && isGeneric)
										  {
											  if (strCheckedInFile.equalsIgnoreCase(strReportName))
											  {
												  comDocument.deleteFile(context, strCheckedInFile, DomainConstants.FORMAT_GENERIC);
											  }
										  }
										  else if (UIUtil.isNotNullAndNotEmpty(strCheckedInFile) && !isGeneric)
										  {
											  if (strCheckedInFile.equalsIgnoreCase(strReportName))
											  {
												   comDocument.deleteFile(context, strCheckedInFile,FORMAT_VIEWABLE);
											  }
										  }
									  }
								  }
								  
								  setIPECAttributesProjectAndOwnershipOnDocument(context, strObjectName, strIssueId, strGenDocId, PWCIntegrationConstants.TYPE_GENERAL_DOCUMENT, strName, strObjectType);
								  connectIPECToGenDocument(context, strGenDocId);
								  
								  String[] arguments = new String[10];
								  arguments[0] = strGenDocId;
								  arguments[1] = strFilePath;
								  arguments[2] = strReportName;
								  arguments[3] = DomainConstants.FORMAT_GENERIC;
								  arguments[4] = strStore;
								  arguments[5] = strFalse;
								  arguments[6] = strServer;
								  arguments[7] = DomainConstants.EMPTY_STRING;
								  JPO.invoke(context, "emxCommonDocument", null, "checkinBus", arguments);
								  if(UIUtil.isNotNullAndNotEmpty(strGenDocId)){
									  DomainObject domDocumentObj = DomainObject.newInstance(context, strGenDocId);
									  domDocumentObj.setOwner(context, "Corporate");
								  }
								  
							  }
						 // Committing transaction
						ContextUtil.commitTransaction(context);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		/**
		* This method is to get the model name from part and issue id
		* @param context
		* @param args
		* @returns String
		* @throws Exception if the operation fails
		*/
		public String getModelNameOrPartNameForIssue(Context context, String strPartName, String IssueName, String strType) throws Exception{
			
			String strReturn = DomainConstants.EMPTY_STRING;
			String strObjectId = DomainConstants.EMPTY_STRING;
			JSONObject jsonObjIssueIds = new JSONObject();
			JSONArray jaColumnIdentifiers = new JSONArray();
	        JSONArray jaTableData = new JSONArray();
	        JSONArray jaTableDataInd = null;
	        String strColIdentifier = DomainConstants.EMPTY_STRING;
	        String strIssueId = DomainConstants.EMPTY_STRING;
	        String strModelNo = DomainConstants.EMPTY_STRING;
	        String strTopAssy = DomainConstants.EMPTY_STRING;
	        String strTDBDDate = DomainConstants.EMPTY_STRING;
	        String strComment = DomainConstants.EMPTY_STRING;
	        int iIssueIdIndex = 0;
	        int iModelIndex = 0;
	        int iTopAssyIndex = 0;
			
			try{
				if(UIUtil.isNotNullAndNotEmpty(strPartName)&& UIUtil.isNotNullAndNotEmpty(strType)){
					String strJSONIssueIds = IssueIdsTDBD(CMAPP_ISSUE_IDS_TDBD_SERVICE_URL, strPartName, true, strType);
					
					if(null != strJSONIssueIds && !"".equals(strJSONIssueIds)){
							//byte[] decoded = Base64.decodeBase64(strJSONIssueIds.getBytes()); 
							//strJSONIssueIds = new String(decoded);
							jsonObjIssueIds = new JSONObject(strJSONIssueIds);
							if(null != jsonObjIssueIds){
								// Identifying the issue id index
								jaColumnIdentifiers = jsonObjIssueIds.getJSONArray(JSON_COLUMN_IDENTIFIERS);
								if(jaColumnIdentifiers.length() > 0){
									int iCount = jaColumnIdentifiers.length();
					            	for(int i = 0; i < iCount; i++){
					            		strColIdentifier = jaColumnIdentifiers.getString(i);
					            		if("TDBD_ISSUE_ID".equals(strColIdentifier)){
					            			iIssueIdIndex = i;
						            	}else if("MODEL_NAME".equals(strColIdentifier)){
						            		iModelIndex = i;
						            	}else if("TOP_ASSY_NO".equals(strColIdentifier)){
						            		iTopAssyIndex = i;
						            	}
						            }
								}
								
								jaTableData = jsonObjIssueIds.getJSONArray(JSON_TABLE_DATA);
								int iCounter = jaTableData.length();
								for (int i = 0; i < iCounter; i++){
									jaTableDataInd = jaTableData.getJSONArray(i);
									strIssueId = jaTableDataInd.getString(iIssueIdIndex);
									if("null" == strIssueId){
										strIssueId = "";
									}
									if(UIUtil.isNotNullAndNotEmpty(strIssueId) && UIUtil.isNotNullAndNotEmpty(IssueName) && strIssueId.equals(IssueName)){
										if(PWCConstants.TYPE_MODEL.equals(strType)){
											strTopAssy = jaTableDataInd.getString(iTopAssyIndex);
											if("null" == strTopAssy){
												strTopAssy = "";
											}
										}else if(DomainConstants.TYPE_PART.equals(strType)){
											strModelNo = jaTableDataInd.getString(iModelIndex);
											if("null" == strModelNo){
												strModelNo = "";
											}
										}
										break;
									}
								}
								if(PWCConstants.TYPE_MODEL.equals(strType) && UIUtil.isNotNullAndNotEmpty(strTopAssy)){
									MapList mlPart = getBasicFieldAndTDBDAttributesOfLastRevisionPart(context, strTopAssy);
									if(mlPart != null && mlPart.size() > 0){
										Map mpPart = (Map) mlPart.get(0);
										strObjectId = (String)mpPart.get(DomainConstants.SELECT_ID);
									}
								}
								if(PWCConstants.TYPE_MODEL.equals(strType)){
									if(UIUtil.isNotNullAndNotEmpty(strTopAssy) && UIUtil.isNotNullAndNotEmpty(strObjectId)){
										strReturn = strTopAssy +","+ strObjectId;
									}
								}else if(DomainConstants.TYPE_PART.equals(strType)){
									if(UIUtil.isNotNullAndNotEmpty(strModelNo)){
										strReturn = strModelNo;
									}
								}
							}
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
	        return strReturn;
	    }
		
		/**
		* This method is to set the attributes to the general document.
		* @param context
		* @param args
		* @returns Void
		* @throws Exception if the operation fails
		*/
		public void setIPECAttributesProjectAndOwnershipOnDocument(Context context, String strObjectName, String strIssueId, String strDocumentId, String strType, String sName, String strObjType) throws Exception{
			try{
				Map attrMap = new HashMap();
				String strCODocDesc = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.CMApp.TDBD_REPORT.DESCRIPTION").trim();
				String STR_TDBD = EnoviaResourceBundle.getProperty(context, "PWCIntegration.DocumentumEV6.Collection.TDBD").trim();
				String strCODocumentDesc = strObjectName + strCODocDesc;
				String strContextUser = context.getUser();
				if(UIUtil.isNotNullAndNotEmpty(strObjectName) && UIUtil.isNotNullAndNotEmpty(strIssueId) && UIUtil.isNotNullAndNotEmpty(strDocumentId)){
          		  	attrMap.put(ATTR_PWC_EXC_CONT_TECH_DATA, "No");
          		  	attrMap.put(ATTR_PWC_EC_MEANS_CLASSIFICATION, "xClass");
          		  	attrMap.put(ATTRIBUTE_PWC_GENERAL_COLLECTION, STR_TDBD);
          		  	attrMap.put(ATTRIBUTE_ORIGINATOR, strContextUser);
          		  	attrMap.put(ATTRIBUTE_TITLE, strCODocumentDesc);
          		  	DomainObject domObj = DomainObject.newInstance(context, strDocumentId);
          		  	domObj.setDescription(context,strCODocumentDesc);
          		  	domObj.setOwner(context, strContextUser);
					String strModelName = getModelNameOrPartNameForIssue(context, strObjectName, strIssueId, strObjType);
					if(UIUtil.isNotNullAndNotEmpty(strModelName)){
						attrMap.put(ATTR_PWC_ENGINE_MODEL, strModelName);
					}
					
					StringBuffer sbXClassId = new StringBuffer();
					if(UIUtil.isNotNullAndNotEmpty(strType) && UIUtil.isNotNullAndNotEmpty(sName)){
						sbXClassId.append(strType);
						sbXClassId.append(PWCConstants.STR_PIPE);	
						sbXClassId.append(STR_TDBD);
						sbXClassId.append(PWCConstants.STR_PIPE);
						sbXClassId.append(sName);
					}
					attrMap.put(ATTR_PWC_EC_XCLASS_ID, sbXClassId.toString());
          		  	domObj.setAttributeValues(context, attrMap);
          		  	String strCommand = "modify bus "+strDocumentId+" project 'Product Engineering'";
					MqlUtil.mqlCommand(context, strCommand);
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
	    }
		
		/**
		* This method is to connect IPEC classes to general document.
		* @param context
		* @param args
		* @returns Void
		* @throws Exception if the operation fails
		*/
		public void connectIPECToGenDocument(Context context, String strDocumentId) throws Exception{
			try{
				String strCompanyName = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.Default.Company.Name");
				if(UIUtil.isNotNullAndNotEmpty(strDocumentId)){
					DomainObject domObj = DomainObject.newInstance(context, strDocumentId);
					String strIPSource = domObj.getInfo(context, "to["+PWCConstants.RELATIONSHIP_PWC_IP_SOURCE+"]");
					if(UIUtil.isNotNullAndNotEmpty(strIPSource) && "FALSE".equalsIgnoreCase(strIPSource)){
						MapList mlDetails = DomainObject.findObjects(context, 
	          		  			DomainConstants.TYPE_COMPANY, 
	          		  			strCompanyName, 
			               		DomainConstants.QUERY_WILDCARD, 
			               		DomainConstants.QUERY_WILDCARD, 
			               		PWCCommonUtil.getVaultPattern(context), 
			               		null, 
			               		false, 
			               		new StringList(DomainConstants.SELECT_ID));
	          		  	if(mlDetails != null && !mlDetails.isEmpty()){
	          		  		Map mpDetails = (Map)mlDetails.get(0);
	          		  		String strObjectId = (String)mpDetails.get(DomainConstants.SELECT_ID);
	          		  		if(UIUtil.isNotNullAndNotEmpty(strObjectId)){
	          		  			DomainRelationship.connect(context, strObjectId, PWCConstants.RELATIONSHIP_PWC_IP_SOURCE, strDocumentId, true);
	          		  		}
	          		  	}
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
	    }
	
		/**
		* This method is to build a json object having TDBD details and weight attributes.
		* @param context
		* @param args
		* @returns JSONObject
		* @throws Exception if the operation fails
		*/
		public JSONObject buildTDBDJsonObject(Context context, String[] args) throws Exception{
			JSONObject jsonObjReturn = new JSONObject();
			JSONObject jsonObjTDBD = new JSONObject();
			JSONArray jaColumnIdentifiers = new JSONArray();
	        JSONArray jaTableData = new JSONArray();
	        JSONArray jaTableDataInd = null;
	        JSONArray jaTDBDInd = new JSONArray();
	        String strColIdentifier = DomainConstants.EMPTY_STRING;
	        int iLevelIndex = 0;
	        int iPartNameIndex = 0;
	        int iUIDIndex = 0;
	        int iQuantityIndex = 0;
	        int iDocNumIndex = 0;
			int iParentPartNameIndex =0;
			int iTDBDIssueIDIndex =0;
			int iSequenceNumber =0;
			int iTDBDComment =0;
	        MapList mlPart = new MapList();
	        Map mpPart = new HashMap();
	        String strLevel = DomainConstants.EMPTY_STRING;
	        String strPartNumber = DomainConstants.EMPTY_STRING;
			String strUID = DomainConstants.EMPTY_STRING;
			String strDescription = DomainConstants.EMPTY_STRING;
			String strRevision = DomainConstants.EMPTY_STRING;
			String strQuantity = DomainConstants.EMPTY_STRING;
		  	String strDocNum = DomainConstants.EMPTY_STRING;
		  	String strType = DomainConstants.EMPTY_STRING;
		  	String strState = DomainConstants.EMPTY_STRING;
			String strSaleCode = DomainConstants.EMPTY_STRING;
			String strParentPartName = DomainConstants.EMPTY_STRING;
		  	String strTDBDIssueID = DomainConstants.EMPTY_STRING;
		  	String strSequenceNumber = DomainConstants.EMPTY_STRING;
		  	String strTDBDComment = DomainConstants.EMPTY_STRING;
			try{
				String strDecodedTDBD = args[0];
				if(null != strDecodedTDBD){
					//byte[] decoded = Base64.decodeBase64(strDecodedTDBD.getBytes()); 
					//strDecodedTDBD = new String(decoded);
					jsonObjTDBD = new JSONObject(strDecodedTDBD);
					if(null != jsonObjTDBD){
						// Identifying the indexes
						jaColumnIdentifiers = jsonObjTDBD.getJSONArray(JSON_COLUMN_IDENTIFIERS);
						if(jaColumnIdentifiers.length() > 0){
							int iCount = jaColumnIdentifiers.length();
			            	for(int i = 0; i < iCount; i++){
			            		strColIdentifier = jaColumnIdentifiers.getString(i);
								if("TD_LEVEL".equals(strColIdentifier)){
			            			iLevelIndex = i;
				            	}else if("PART_NAME".equals(strColIdentifier)){
				            		iPartNameIndex = i;
				            	}else if("PWC_UID".equals(strColIdentifier)){
				            		iUIDIndex = i;
				            	}else if("QUANTITY".equals(strColIdentifier)){
				            		iQuantityIndex = i;
				            	}else if("DOCUMENT_NO".equals(strColIdentifier)){
				            		iDocNumIndex = i;
				            	}else if("TDBD_ISSUE_ID".equals(strColIdentifier)){
				            		iTDBDIssueIDIndex = i;
				            	}else if("SEQUENCE_NUMBER".equals(strColIdentifier)){
				            		iSequenceNumber = i;
				            	}else if("TDBD_COMMENT".equals(strColIdentifier)){
				            		iTDBDComment = i;
				            	}
				            }
						}
						jaTableData = jsonObjTDBD.getJSONArray(JSON_TABLE_DATA);
						int jaTableDataSize = jaTableData.length();
						for (int i = 0; i < jaTableDataSize; i++){
							 strSaleCode = DomainConstants.EMPTY_STRING;
							 strDescription = DomainConstants.EMPTY_STRING;
							strRevision = DomainConstants.EMPTY_STRING;
							 strType = DomainConstants.EMPTY_STRING;
							 strState = DomainConstants.EMPTY_STRING;
							jaTableDataInd = jaTableData.getJSONArray(i);
							strLevel = jaTableDataInd.getString(iLevelIndex);
							strPartNumber = jaTableDataInd.getString(iPartNameIndex);
							strUID = jaTableDataInd.getString(iUIDIndex);
							strQuantity = jaTableDataInd.getString(iQuantityIndex);
							strDocNum = jaTableDataInd.getString(iDocNumIndex);
							strParentPartName = jaTableDataInd.getString(iParentPartNameIndex);
							strTDBDIssueID = jaTableDataInd.getString(iTDBDIssueIDIndex);
							strSequenceNumber = jaTableDataInd.getString(iSequenceNumber);
							strTDBDComment = jaTableDataInd.getString(iTDBDComment);
							if(UIUtil.isNotNullAndNotEmpty(strPartNumber)){
								mlPart = getBasicFieldAndTDBDAttributesOfLastRevisionPart(context, strPartNumber);
								if(mlPart != null && mlPart.size() > 0){
									mpPart = (Map) mlPart.get(0);
									strType = (String) mpPart.get(DomainObject.SELECT_TYPE);
									strRevision = (String) mpPart.get(DomainObject.SELECT_REVISION);
									strSaleCode = (String) mpPart.get(SELECTABLE_ATTR_PWC_SALE_CODE);
									strState = (String) mpPart.get(DomainObject.SELECT_CURRENT);
									strDescription = (String) mpPart.get(DomainObject.SELECT_DESCRIPTION);
								}
							}
							
							jaTDBDInd = new JSONArray();
							jaTDBDInd.put(strLevel);
							jaTDBDInd.put(strDescription);
							jaTDBDInd.put(strPartNumber);
							jaTDBDInd.put(strRevision);
							jaTDBDInd.put(strUID);
							jaTDBDInd.put(strQuantity);
							jaTDBDInd.put(strType);
							jaTDBDInd.put(strSaleCode);
							jaTDBDInd.put(strState);
							jaTDBDInd.put(strDocNum);
							jaTDBDInd.put(strTDBDIssueID);
							jaTDBDInd.put(strSequenceNumber);
							jaTDBDInd.put(strTDBDComment);
							jsonObjReturn.put(Integer.toString(i), jaTDBDInd);
						}
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return jsonObjReturn;
		}
		
		/**
		* This method is to get the TDBD attributes of latest revision part.
		* @param context
		* @param args
		* @returns MapList
		* @throws Exception if the operation fails
		*/
		public MapList getBasicFieldAndTDBDAttributesOfLastRevisionPart(Context context, String strPartName) throws Exception{
			MapList mlReturn = new MapList();
			try{
				StringList objectSelects = new StringList();
				objectSelects.addElement(DomainObject.SELECT_ID);
			    objectSelects.addElement(DomainObject.SELECT_TYPE);
			    objectSelects.addElement(DomainObject.SELECT_REVISION);
			    objectSelects.addElement(DomainObject.SELECT_CURRENT);
			    objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
			    objectSelects.addElement(SELECTABLE_ATTR_PWC_SALE_CODE);
			   String strWhrClause = "revision == last";
			   String vault = STR_PRODUCTION_VAULT + "," + STR_LEGACY_VAULT;
				mlReturn= DomainObject.findObjects(context, 
		               		DomainConstants.TYPE_PART, 
		               		strPartName, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		vault, 
		               		strWhrClause, 
		               		false, 
		               		objectSelects);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return mlReturn;
		}
		
		/**
		* This method is to build the excel report.
		* @param context
		* @param args
		* @returns Workbook
		* @throws Exception if the operation fails
		*/
		public Workbook buildExcelForTDBDReport(Context context, JSONObject jsonTDBDplusWeight) throws Exception{
			Workbook workbook = new XSSFWorkbook();
			JSONArray jaTDBDplusWeight = new JSONArray();
			try{
				if(null != jsonTDBDplusWeight){
					Sheet sheet = null;
					Row row = null;
					Cell cell = null;
					CellStyle cellStyle = null;
					XSSFFont font = null;
					sheet = workbook.createSheet("Report");
					sheet.setColumnWidth(2, 7000);
					sheet.setColumnWidth(3, 4000);
	    			sheet.setColumnWidth(5, 3000);
	    			sheet.setColumnWidth(21, 3000);
					StringList slWeightReportRow2Header = new StringList();
					String strWeightReportRow2Header = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.TDBDReport.Row2.Header").trim();
					if(null != strWeightReportRow2Header && !"".equals(strWeightReportRow2Header)){
						slWeightReportRow2Header = FrameworkUtil.split(strWeightReportRow2Header.trim(), ",");
					}
					String strCMApp = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.WeightReport.Application.CMApp").trim();
					String strEnovia = EnoviaResourceBundle.getProperty(context, "PWCCMAppIntegration.WeightReport.Application.Enovia").trim();
					String strColumn = DomainConstants.EMPTY_STRING;

					if(slWeightReportRow2Header.size() > 0 && jsonTDBDplusWeight.length() > 0){
						// Row 1
						row = sheet.createRow(0);
						row.setHeight((short) 1200);
						int iCount = slWeightReportRow2Header.size()-1;
						for(int i = 0; i < iCount; i++){
						  cell = row.createCell(i);
						  cellStyle = workbook.createCellStyle();
						   cellStyle.setWrapText(true);
						  cell.setCellStyle(cellStyle);
						  cell.setCellValue((String) slWeightReportRow2Header.get(i+1));
						}
						// Data Rows
						String strValue = DomainConstants.EMPTY_STRING;
						long startTime1 = System.currentTimeMillis();
						int jsonTDBDplusWeightSize = jsonTDBDplusWeight.length();
						for(int i = 0; i < jsonTDBDplusWeightSize; i++){
							jaTDBDplusWeight = (JSONArray) jsonTDBDplusWeight.get(Integer.toString(i));
							row = sheet.createRow(i + 1);
							int iCounter = jaTDBDplusWeight.length();
							for(int j = 0; j < iCounter; j++){
								cell = row.createCell(j);
								cellStyle = workbook.createCellStyle();
								cell.setCellStyle(cellStyle);
								strValue = jaTDBDplusWeight.getString(j);
								if("null" == strValue){
									strValue = "";
								}
								cell.setCellValue(strValue);
							}
						}
					}
					// Modified  : Removed first row and first column : End
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return workbook;
		}
		//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
		
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 14 - SALE Code Management
		/**
		* This method is to set the record status attribute on action trigger of part creation.
		* @param context 
		* @param args
		* @return int
		* @throws Exception if the operation fails
		*/
		public int checkSaleCodeAttrForPromotion(Context context, String[] args) throws Exception{
			if (args == null || args.length < 1) 
			{
				throw (new IllegalArgumentException());
			}
			
			int intReturnVal 		= 0;
			String strObjectID = args[0];
			
			try{
				if(UIUtil.isNotNullAndNotEmpty(strObjectID)){
					DomainObject domObj = DomainObject.newInstance(context, strObjectID);
					String strSaleCodeAttr = domObj.getInfo(context, SELECTABLE_ATTR_PWC_SALE_CODE);
					if(UIUtil.isNullOrEmpty(strSaleCodeAttr)){
						String strMessage = i18nNow.getI18nString("pwcEngineeringCentral.Part.ECPartReviewPromoteAction.ErrorMessage", "emxEngineeringCentralStringResource", "en");
						MqlUtil.mqlCommand(context, "notice $1",strMessage);
						intReturnVal = 1;
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return intReturnVal;
		}
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 14 - SALE Code Management
		
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
	/**
	* This Method is to get the part description and Saleability code.
	* @param context
	* @param strAddedPart
	* @return Map
	* @throws Exception if operation fails
	**/
	public Map getSaleabilityCodeAndDescription(Context context, String strPartName) throws Exception{
		Map mpReturn = new HashMap();
		String strColIdentifier = DomainConstants.EMPTY_STRING;
		int iPartNameIndex = 0;
		
		try{
			if(null != writeIntoFile){
				_LOGGER.debug("Execution Start Time of PWCCMAppIntegration : getSaleabilityCodeAndDescription --> " + java.util.Calendar.getInstance().getTime());
			}
				if("null" == strPartName){
					strPartName = DomainConstants.EMPTY_STRING;
				}
				if(UIUtil.isNotNullAndNotEmpty(strPartName)){
					MapList mlLatestPartDetails = getLatestRevisionPartAnC(context, strPartName);
					if(mlLatestPartDetails != null && !mlLatestPartDetails.isEmpty()){
						mpReturn = (Map)mlLatestPartDetails.get(0);
					}
				}
			if(null != writeIntoFile){
				_LOGGER.debug("Execution End Time of PWCCMAppIntegration : getSaleabilityCodeAndDescription --> " + java.util.Calendar.getInstance().getTime());
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
				_LOGGER.debug("Exception in PWCCMAppIntegration : getSaleabilityCodeAndDescription :-"+ex.getMessage());
			}
		}
		return mpReturn;
	}

	//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
		
		//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
		/**
		* This Method is to get the part Id using find objects method.
		* @param context
		* @param JSONObject
		* @return String
		* @throws Exception if operation fails
		**/
		public String findPWCGeneralDocumentByName(Context context, String strName) throws Exception{
			String strReturn = DomainConstants.EMPTY_STRING;
			StringList objectSelects = new StringList();
         	objectSelects.addElement(DomainObject.SELECT_ID);
			try{
	         	String strWhrClause = "revision == last";
	         	if(null != strName && !DomainConstants.EMPTY_STRING.equals(strName)){
					MapList mlDetails= DomainObject.findObjects(context, 
								PWCIntegrationConstants.TYPE_GENERAL_DOCUMENT, 
			               		strName, 
			               		DomainConstants.QUERY_WILDCARD, 
			               		DomainConstants.QUERY_WILDCARD, 
			               		DomainConstants.QUERY_WILDCARD, 
			               		strWhrClause, 
			               		false, 
			               		objectSelects);
					if(null != mlDetails && !mlDetails.isEmpty()){
						Map mpDetails = new HashMap();
						mpDetails = (Map)mlDetails.get(0);
						strReturn = (String)mpDetails.get(DomainConstants.SELECT_ID);
					}
	         	}
			}catch(Exception ex){
				ex.printStackTrace();
				if(null != writeIntoFile){
					_LOGGER.debug("Exception in PWCCMAppIntegration : findPWCGeneralDocumentByName :-"+ex.getMessage());
				}
			}
			return strReturn;
		}
		//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 06 - TDBD Report
		
		
		/**
	* This method is to get the latest revision part.
	* @param context
	* @param args
	* @returns MapList
	* @throws Exception if the operation fails
	*/
	public MapList getLatestRevisionPartAnC(Context context, String strPartName) throws Exception{
		MapList mlReturn = new MapList();
		try{
			StringList objectSelects = new StringList();
         	objectSelects.addElement(DomainObject.SELECT_ID);
         	objectSelects.addElement(DomainObject.SELECT_NAME);
			//START :: Added for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
         	objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
         	objectSelects.addElement(SELECTABLE_ATTR_PWC_SALE_CODE);
			//END :: Added for HEAT-C-16867 : CMApp Drop2 UC 16 - Retrieve Salability Code and Description from EV6 for Add/Cancel excel
         	String strWhrClause = "revision == last";
			strWhrClause += " && policy != \"" + DomainConstants.POLICY_DEVELOPMENT_PART + "\"";
         	if(null != strPartName && !DomainConstants.EMPTY_STRING.equals(strPartName)){
				mlReturn= DomainObject.findObjects(context, 
		               		DomainConstants.TYPE_PART, 
		               		strPartName, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		DomainConstants.QUERY_WILDCARD, 
		               		strWhrClause, 
		               		false, 
		               		objectSelects);
         	}

		}catch(Exception ex){
			ex.printStackTrace();
			if(null != writeIntoFile){
			//START :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation			
				_LOGGER.debug("Exception in PWCCMAppIntegration : getLatestRevisionPartAnC :-"+ex.getMessage());
			//END :: Modified for HEAT-C-16867 : CMApp Drop2 UC 10 - Logging through log4j and weekly based log file generation
			}
		}
		return mlReturn;
	}
		
		
}
