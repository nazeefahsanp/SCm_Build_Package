package com.pwc.scm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * The Class PWCDeltaExtractorUtil.
 */
public final class PWCTransformationUtils {

	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCTransformationUtils.class);

	/**
	 * Read properties file.
	 *
	 * @param fileName the file name
	 * @return the properties
	 */
	public static Properties readPropertiesFile(String fileName) {

		logger.info("Resource File :- " + fileName);

		Properties properties = null;

		try {
			properties = new Properties();

			if (fileName.contains("/") || fileName.contains("\\")) {
				logger.info("Inside If condition : " + fileName);
				properties.load(new FileInputStream(fileName));

			} else {
				logger.info("Inside else condition : " + fileName);
				properties.load(PWCTransformationUtils.class.getResourceAsStream("/" + fileName));
			}

		} catch (FileNotFoundException exception) {
			logger.error("Exception Occurs", exception);
		} catch (IOException ioe) {
			logger.error("Exception Occurs", ioe);
		}

		return properties;
	}

	/**
	 * Configure logger.
	 *
	 * @param logFilePath the log file path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void configureLogger(String logFilePath) throws IOException {

		Logger.getRootLogger().setLevel(Level.ALL);

		// Uncomment below two line to use File Appender
		Layout layout = new PatternLayout("%d [%t] %-5p %c{2} - %m%n");
		Logger.getRootLogger().addAppender(new FileAppender(layout, logFilePath));

		// Use it while working on JAVA IDE
		//Layout layout1 = new PatternLayout("%t | %m%n");
		//Logger.getRootLogger().addAppender(new ConsoleAppender(layout1));

	}

	/**
	 * Prepare target package dir path.
	 *
	 * @param branchname             the branchname
	 * @param originTag              the origin tag
	 * @param targetTag              the target tag
	 * @param sourcePackageDirectory the source package directory
	 * @return the string
	 */
	public static String prepareTargetPackageDirPath(String branchname, String originTag, String targetTag,
			String sourcePackageDirectory) {

		String targetSourcePackageDir = StringUtils.EMPTY;

		if (StringUtils.EMPTY.equalsIgnoreCase(originTag.trim())
				&& StringUtils.EMPTY.equalsIgnoreCase(targetTag.trim())) {
			logger.info("No repository tag have been provided, Check the parameter passed");

		} else if (StringUtils.EMPTY.equalsIgnoreCase(originTag.trim())
				|| StringUtils.EMPTY.equalsIgnoreCase(targetTag.trim())) {
			targetSourcePackageDir = sourcePackageDirectory + File.separator + "pwc." + branchname + ".build."
					+ targetTag;

		} else if (!StringUtils.EMPTY.equalsIgnoreCase(originTag.trim())
				&& !StringUtils.EMPTY.equalsIgnoreCase(targetTag.trim())) {
			targetSourcePackageDir = sourcePackageDirectory + File.separator + "pwc." + branchname + ".build."
					+ originTag + "_" + targetTag;

		} else {
			logger.info("Please checked the values provided");
		}

		return targetSourcePackageDir;

	}

	/**
	 * Find specified files.
	 *
	 * @param sourceDirectory the source directory
	 * @param resultList      the result list
	 * @param fileName        the file extension
	 * @return the list
	 */
	public static List<String> findSpecifiedFiles(String sourceDirectory, List<String> resultList, String fileName) {

		File dir = new File(sourceDirectory);

		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				findSpecifiedFiles(file.getAbsolutePath(), resultList, fileName);
			}

			if (file.getName().equalsIgnoreCase(fileName)) {
				resultList.add(file.getAbsolutePath());
			}
		}

		return resultList;
	}

	public static Map<String, String> resolveDependency(String targetTagCheckOutDir) {

		String sourceDirectoryPath = targetTagCheckOutDir;

		File file = new File(sourceDirectoryPath);

		Map<String, String> dependencyResultMap = new HashMap<>();

		if (file.exists()) {
			File sourceCodeDir = new File(sourceDirectoryPath + "/sourcecode/");

			if (sourceCodeDir.exists()) {

				List<String> listOfFiles = PWCTransformationUtils.findSpecifiedFiles(sourceCodeDir.getAbsolutePath(),
						new ArrayList<String>(), "settings.properties");

				logger.info("Number of settings.property files found : " + listOfFiles.size());

				for (String settingsFilePath : listOfFiles) {

					logger.info("Absolute path of settings.properties files : " + settingsFilePath);

					Properties settingProperties = PWCTransformationUtils.readPropertiesFile(settingsFilePath);

					String moduleDependency = (String) settingProperties.get("dependencies");
					String moduleName = new File(settingsFilePath).getParentFile().getName();

					if (moduleDependency != null) {
						dependencyResultMap.put(moduleName, moduleDependency);
					} else {
						logger.info("There are no dependencies found for module : " + moduleName);
					}
				}

			} else {
				logger.info("Source Code directory does not exists");
			}
		} else {
			logger.info("Directory does not exist : " + file.getAbsolutePath());
		}
		return dependencyResultMap;
	}

}