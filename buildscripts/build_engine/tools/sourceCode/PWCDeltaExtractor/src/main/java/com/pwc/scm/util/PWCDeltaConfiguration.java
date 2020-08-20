package com.pwc.scm.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author Sanjay.Meena
 *
 */
public class PWCDeltaConfiguration {

	/** Singleton instance declaration */
	private static PWCDeltaConfiguration configuration;

	/** FILE containing all delta configurations */
	private static final String CONFFIG_FILE_NAME = "DeltaRules_Config.properties";

	/** Delta Configuration defined in DeltaRules_Config.properties */
	private static Map<String, String> transformedProperties = new HashMap<String, String>();

	/** Delta output generation directory */
	private static String deltaOutputDir;

	/** Target Tag directory */
	private static String targetTagDir;

	/** Origin Tag directory */
	private static String originTagDir;

	/** Rule type: Copy directory/file as is without any validation */
	public static final String AS_IS_RULE = "as_is_rule";

	/** Rule type: File change check only */
	public static final String DELTA_FILE_RULE = "delta_file_rule";

	/** Rule type: File content check */
	public static final String DELTA_FILE_CONTENT_RULE = "delta_file_content_rule";

	/** Rule type: Related File check */
	public static final String RELATED_FILE_RULE = "related_file_rule";

	/** Rule type: Related File content check */
	public static final String RELATED_FILE_CONTENT_RULE = "related_file_content_rule";

	/** Spinner Directory Name */
	public static final String SPINNER = "Spinner";

	/** UNDERSCORE Separator used in config file DeltaRules_Config.properties */
	public static final String UNDERSCORE = "_";

	/** Configuration to store primary key columns for Spinner Files */
	private static Map<String, Integer> primaryKeyColumns = new HashMap<String, Integer>();

	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCDeltaConfiguration.class);

	/** Private constructor */
	private PWCDeltaConfiguration() {
	}

	/** Public method to get the only object of this class */
	public static synchronized PWCDeltaConfiguration getInstance() {
		if (configuration == null) {
			configuration = new PWCDeltaConfiguration();
			logger.info("Created Singleton object");
		}

		return configuration;
	}

	/**
	 * Method to initialize & build configuration, This will be used to get rules
	 * during delta extraction process
	 * 
	 * @param _deltaOutputDir
	 * @param _targetTagDir
	 * @param _originTagDir
	 * @throws IOException
	 */
	public void init(String _deltaOutputDir, String _targetTagDir, String _originTagDir) throws IOException {
		logger.info("Initializing Delta configuration: START");

		targetTagDir = _targetTagDir;
		deltaOutputDir = _deltaOutputDir;
		originTagDir = _originTagDir;

		// build configuration
		buildConfiguration();
		logger.info("Initializing Delta configuration: END");

	}

	/**
	 * Loads configuration and transform the configuration keys to lower case. it
	 * also builds configuration for all elements by applying the parent rules.
	 * 
	 * @throws IOException
	 */
	private void buildConfiguration() throws IOException {
		Properties properties = PWCDeltaExtractorUtils.readPropertiesFile(CONFFIG_FILE_NAME);

		Enumeration keylist = properties.propertyNames();
		while (keylist.hasMoreElements()) {
			String key = (String) keylist.nextElement();
			transformedProperties.put(key.toLowerCase(), (String) properties.get(key));
		}

		// build Primary Key column Data
		buildPrimaryKeyColumns(properties);

		// load configuration for all files
		buildConfFromRoot(Paths.get(targetTagDir), "root", transformedProperties.get("root"));
	}

	/**
	 * 
	 */
	public void buildPrimaryKeyColumns(Properties properties) {

		for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			String value = properties.getProperty(name);
			if (name.startsWith("spinner_primaryKey_file_")) {
				primaryKeyColumns.put(StringUtils.substringAfter(name, "spinner_primaryKey_file_"),
						Integer.valueOf(value));
			}
		}

		// Business
//		primaryKeyColumns.put("target.primaryKey.SpinnerAttributeData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerChannelData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerCommandData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerExpresionData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerGroupData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerInquiryData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerInterfaceData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerMenuData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerPageData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerPolicyData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerPolicyStateData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerPortalData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerProgramData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerPropertyData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerRelationshipData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerRoleData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerRuleData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerTableData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerTableColumnData.xls", 2); 
//	    primaryKeyColumns.put("target.primaryKey.SpinnerTriggerData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerTypeData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerWebFormData.xls", 2);
//	    primaryKeyColumns.put("target.primaryKey.SpinnerWebFormFieldData.xls", 2);
//	    //Relationships        
//	    primaryKeyColumns.put("target.primaryKey.rel-b2b_eService Number Generator.xls", 4);
//	    primaryKeyColumns.put("target.primaryKey.rel-b2b_eService Additional Object.xls", 3);
//	    //Objects              
//	    primaryKeyColumns.put("target.primaryKey.bo_eService Trigger Program Parameters.xls", 3);
//	    primaryKeyColumns.put("bo_eService Object Generator.xls", 3);
//	    primaryKeyColumns.put("bo_eService Number Generator.xls", 3);

	}

	/**
	 * Walk through the target directory structure and set configuration for all
	 * files
	 * 
	 * @param targetTagPath
	 * @param rootConfKey
	 * @param rootConfValue
	 * @param parentDeltaFileRuleClassValue
	 * @param parentConfNewClassValue
	 * @throws IOException
	 */
	private void buildConfFromRoot(Path targetTagPath, String rootConfKey, String rootConfValue) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetTagPath)) {

			for (Path fileEntry : stream) {
				/** Check if it is file, if yes than extract the file name without extension */
				String fileOrDirectoryName = fileEntry.getFileName().toString().toLowerCase();

				/** For building rules for Single Rule/Class configuration */
				String confRuleKey = rootConfKey + UNDERSCORE + fileOrDirectoryName;
				String confRuleValue = transformedProperties.get(confRuleKey);
				String confClassKey = confRuleKey + "_class";
				String confClassValue = transformedProperties.get(confClassKey);

				if (confRuleValue == null) {
					confRuleValue = rootConfValue;
				}

				transformedProperties.put(confRuleKey, confRuleValue);

				if (confRuleValue.contains(AS_IS_RULE)) {
					logger.info(
							"Delta Config Rule for Configuratio Item " + fileOrDirectoryName + " is " + confRuleValue);
				} else if (confRuleValue.contains(DELTA_FILE_RULE)) {
					buildConfDeltaFileRule(fileEntry, confRuleKey, confRuleValue, confClassValue);
				} else if (confRuleValue.contains(DELTA_FILE_CONTENT_RULE)) {
					buildConfDeltaFileContentRule(fileEntry, confRuleKey, confRuleValue, null, null, null, null);
				}

			}

			Set<String> keySet = transformedProperties.keySet();
			for (String key : keySet) {
				logger.info("key: " + key + " value: " + transformedProperties.get(key));

			}

		}
	}

	/**
	 * Walk through the target directory structure and set configuration for all
	 * files
	 * 
	 * @param targetTagPath
	 * @param parentConfRuleKey
	 * @param parentConfRuleValue
	 * @param parentDeltaFileRuleClassValue
	 * @param parentConfNewClassValue
	 * @throws IOException
	 */
	private void buildConfDeltaFileRule(Path targetTagPath, String parentConfRuleKey, String parentConfRuleValue,
			String parentDeltaFileRuleClassValue) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetTagPath)) {

			for (Path fileEntry : stream) {
				/** Check if it is file, if yes than extract the file name without extension */
				String fileOrDirectoryName = fileEntry.getFileName().toString().toLowerCase();

				System.out.println("buildConfDeltaFileRule: " + fileEntry.toFile().getCanonicalPath());

				/** For building rules for Single Rule/Class configuration */
				String confRuleKey = parentConfRuleKey + UNDERSCORE + fileOrDirectoryName;
				String confRuleValue = transformedProperties.get(confRuleKey);
				// System.out.println("confRuleKey: "+ confRuleKey + "
				// confRuleValue:"+confRuleValue);

				String confClassKey = confRuleKey + "_class";
				String confClassValue = transformedProperties.get(confClassKey);
				// System.out.println("confClassKey : "+ confClassKey + "
				// confClassValue:"+confClassValue);

				if (confRuleValue == null) {
					// configuration not defined, use parent configuration which is already delta
					confRuleValue = parentConfRuleValue;
					confClassValue = parentDeltaFileRuleClassValue;
				}

				if (confRuleValue.contains(DELTA_FILE_RULE)) {
					// Delta rule set for this item, either from cofig file or from Parent
					transformedProperties.put(confRuleKey, confRuleValue);
					transformedProperties.put(confClassKey, confClassValue);
					if (Files.isDirectory(fileEntry)) {
						buildConfDeltaFileRule(fileEntry, confRuleKey, confRuleValue, confClassValue);
					}
				} else if (confRuleValue.contains(DELTA_FILE_CONTENT_RULE)) {
					/** this child is configured for delta content */
//	        		System.err.println("CHECK THISSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
					buildConfDeltaFileContentRuleRoot(fileEntry, parentConfRuleKey);
				} else if (confRuleValue.contains(AS_IS_RULE)) {
					/** this child is configured for as IS copy */
					logger.info(
							"Delta Config Rule for Configuratio Item " + fileOrDirectoryName + " is " + confRuleValue);
				}
			}
		}
	}

	/**
	 * Walk through the target directory structure and set configuration for all
	 * files
	 * 
	 * @param targetTagPath
	 * @param parentConfRuleKey
	 * @param parentConfRuleValue
	 * @param parentDeltaFileRuleClassValue
	 * @param parentConfNewClassValue
	 * @throws IOException
	 */
	private void buildConfDeltaFileContentRule(Path targetTagPath, String parentConfRuleKey, String parentConfRuleValue,
			String parentConfNewFileClassValue, String parentConfChangedFileClassValue,
			String parentConfChangedFileChangedLineClassValue, String parentConfChangedFileNewLineClassValue)
			throws IOException {

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetTagPath)) {

			for (Path fileEntry : stream) {

				/** Check if it is file, if yes than extract the file name without extension */
				String fileOrDirectoryName = fileEntry.getFileName().toString().toLowerCase();

				if ("workspace-services".equals(fileOrDirectoryName)) {
					System.out.println("buildConfDeltaFileContentRule: " + fileEntry.toFile().getCanonicalPath());
				}

				/** For building rules for Single Rule/Class configuration */
				String confRuleKey = parentConfRuleKey + UNDERSCORE + fileOrDirectoryName;
				String confRuleValue = transformedProperties.get(confRuleKey);
				// System.out.println("confRuleKey: "+ confRuleKey + "
				// confRuleValue:"+confRuleValue);

				String confNewFileClassKey = confRuleKey + "_newfile_class";
				String confNewFileClassValue = (String) transformedProperties.get(confNewFileClassKey);

				String confChangedFileClassKey = confRuleKey + "_changedfile_class";
				String confChangedFileClassValue = transformedProperties.get(confChangedFileClassKey);

				String confChangedFileChangedLineClassKey = confRuleKey + "_changedfile_changedline_class";
				String confChangedFileChangedLineClassValue = transformedProperties
						.get(confChangedFileChangedLineClassKey);

				String confChangedFileNewLineClassKey = confRuleKey + "_changedfile_newline_class";
				String confChangedFileNewLineClassValue = transformedProperties.get(confChangedFileNewLineClassKey);

				if (confRuleValue == null) {
					// configuration not defined, use parent configuration which is already delta
					// content in this case
					confRuleValue = parentConfRuleValue;
					confNewFileClassValue = parentConfNewFileClassValue;
					confChangedFileClassValue = parentConfChangedFileClassValue;
					confChangedFileChangedLineClassValue = parentConfChangedFileChangedLineClassValue;
					confChangedFileNewLineClassValue = parentConfChangedFileNewLineClassValue;
				}

				if (confRuleValue.contains(DELTA_FILE_CONTENT_RULE)) {
					// Delta content rule set for this item, either from config file or from Parent
					// SET Configuration
					transformedProperties.put(confRuleKey, confRuleValue);
					transformedProperties.put(confNewFileClassKey, confNewFileClassValue);

					if (confChangedFileClassValue != null) {
						transformedProperties.put(confChangedFileClassKey, confChangedFileClassValue);
					} else {
						transformedProperties.put(confChangedFileChangedLineClassKey,
								confChangedFileChangedLineClassValue);
						transformedProperties.put(confChangedFileNewLineClassKey, confChangedFileNewLineClassValue);
					}

					if (Files.isDirectory(fileEntry)) {
						buildConfDeltaFileContentRule(fileEntry, confRuleKey, confRuleValue, confNewFileClassValue,
								confChangedFileClassValue, confChangedFileChangedLineClassValue,
								confChangedFileNewLineClassValue);
					}
				} else if (confRuleValue.contains(DELTA_FILE_RULE)) {
					/** this child is configured for delta content */
//	        		System.err.println("CHECK THISSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
					buildConfDeltaFileRuleRoot(fileEntry, parentConfRuleKey);
				} else if (confRuleValue.contains(AS_IS_RULE)) {
					/** this child is configured for as IS copy */
					logger.info(
							"Delta Config Rule for Configuratio Item " + fileOrDirectoryName + " is " + confRuleValue);
				}
			}
		}
	}

	/**
	 * Walk through the target directory structure and set configuration for all
	 * files
	 * 
	 * @param targetTagPath
	 * @param parentConfRuleKey
	 * @param parentConfRuleValue
	 * @param parentDeltaFileRuleClassValue
	 * @param parentConfNewClassValue
	 * @throws IOException
	 */
	private void buildConfDeltaFileContentRuleRoot(Path targetTagPath, String parentConfRuleKey) throws IOException {

		Path fileEntry = targetTagPath.toFile().toPath();

		/** Check if it is file, if yes than extract the file name without extension */
		String fileOrDirectoryName = fileEntry.getFileName().toString().toLowerCase();

		/** For building rules for Single Rule/Class configuration */
		String confRuleKey = parentConfRuleKey + UNDERSCORE + fileOrDirectoryName;
		String confRuleValue = transformedProperties.get(confRuleKey);
		// System.out.println("confRuleKey: "+ confRuleKey + "
		// confRuleValue:"+confRuleValue);

		String confNewFileClassKey = confRuleKey + "_newfile_class";
		String confNewFileClassValue = (String) transformedProperties.get(confNewFileClassKey);

		String confChangedFileClassKey = confRuleKey + "_changedfile_class";
		String confChangedFileClassValue = transformedProperties.get(confChangedFileClassKey);

		String confChangedFileChangedLineClassKey = confRuleKey + "_changedfile_changedline_class";
		String confChangedFileChangedLineClassValue = transformedProperties.get(confChangedFileChangedLineClassKey);

		String confChangedFileNewLineClassKey = confRuleKey + "_changedfile_newline_class";
		String confChangedFileNewLineClassValue = transformedProperties.get(confChangedFileNewLineClassKey);

		// Delta content rule set for this item, either from config file or from Parent
		// SET Configuration
		transformedProperties.put(confRuleKey, confRuleValue);
		transformedProperties.put(confNewFileClassKey, confNewFileClassValue);

		if (confChangedFileClassValue != null) {
			transformedProperties.put(confChangedFileClassKey, confChangedFileClassValue);
		} else {
			transformedProperties.put(confChangedFileChangedLineClassKey, confChangedFileChangedLineClassValue);
			transformedProperties.put(confChangedFileNewLineClassKey, confChangedFileNewLineClassValue);
		}

		if (Files.isDirectory(fileEntry)) {
			buildConfDeltaFileContentRule(fileEntry, confRuleKey, confRuleValue, confNewFileClassValue,
					confChangedFileClassValue, confChangedFileChangedLineClassValue, confChangedFileNewLineClassValue);
		}
	}

	/**
	 * Walk through the target directory structure and set configuration for all
	 * files
	 * 
	 * @param targetTagPath
	 * @param parentConfRuleKey
	 * @param parentConfRuleValue
	 * @param parentDeltaFileRuleClassValue
	 * @param parentConfNewClassValue
	 * @throws IOException
	 */
	private void buildConfDeltaFileRuleRoot(Path targetTagPath, String parentConfRuleKey) throws IOException {

		Path fileEntry = targetTagPath.toFile().toPath();

		/** Check if it is file, if yes than extract the file name without extension */
		String fileOrDirectoryName = fileEntry.getFileName().toString().toLowerCase();

		System.out.println("buildConfDeltaFileRule: " + fileEntry.toFile().getCanonicalPath());

		/** For building rules for Single Rule/Class configuration */
		String confRuleKey = parentConfRuleKey + UNDERSCORE + fileOrDirectoryName;
		String confRuleValue = transformedProperties.get(confRuleKey);
		// System.out.println("confRuleKey: "+ confRuleKey + "
		// confRuleValue:"+confRuleValue);

		String confClassKey = confRuleKey + "_class";
		String confClassValue = transformedProperties.get(confClassKey);
		// System.out.println("confClassKey : "+ confClassKey + "
		// confClassValue:"+confClassValue);

		if (confRuleValue.contains(DELTA_FILE_RULE)) {
			// Delta rule set for this item, either from cofig file or from Parent
			transformedProperties.put(confRuleKey, confRuleValue);
			transformedProperties.put(confClassKey, confClassValue);
			if (Files.isDirectory(fileEntry)) {
				buildConfDeltaFileRule(fileEntry, confRuleKey, confRuleValue, confClassValue);
			}
		}
	}

	/**
	 * @return the transformedProperties
	 */
	public Map<String, String> getTransformedProperties() {
		return transformedProperties;
	}

	/**
	 * @return the deltaOutputDir
	 */
	public String getDeltaOutputDir() {
		return deltaOutputDir;
	}

	/**
	 * @return the targetTagDir
	 */
	public String getTargetTagDir() {
		return targetTagDir;
	}

	/**
	 * @return the originTagDir
	 */
	public String getOriginTagDir() {
		return originTagDir;
	}

	/**
	 * @return the primaryKeyColumns Count
	 */
	public Integer getPrimaryKeyColumnCount(String fileName) {
		Integer count = primaryKeyColumns.get(fileName);
		return count == null ? 0 : count;
	}

}
