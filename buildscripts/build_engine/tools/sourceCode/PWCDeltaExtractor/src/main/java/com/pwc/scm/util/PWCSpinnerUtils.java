package com.pwc.scm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * The Class PWCDeltaExtractorUtil.
 */
public final class PWCSpinnerUtils {

	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCSpinnerUtils.class);

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
				properties.load(PWCSpinnerUtils.class.getResourceAsStream("/"+fileName));
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
		//Layout layout = new PatternLayout("%d [%t] %-5p %c{2} - %m%n");
		//Logger.getRootLogger().addAppender(new FileAppender(layout, logFilePath));

		// Use it while working on JAVA IDE
		Layout layout = new PatternLayout("%t | %m%n");
		Logger.getRootLogger().addAppender(new ConsoleAppender(layout));

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

		if (StringUtils.EMPTY.equalsIgnoreCase(originTag.trim()) && StringUtils.EMPTY.equalsIgnoreCase(targetTag.trim())) {
			logger.info("No repository tag have been provided, Check the parameter passed");

		} else if (StringUtils.EMPTY.equalsIgnoreCase(originTag.trim()) || StringUtils.EMPTY.equalsIgnoreCase(targetTag.trim())) {
			targetSourcePackageDir = sourcePackageDirectory + File.separator + "pwc." + branchname + ".build." + targetTag;

		} else if (!StringUtils.EMPTY.equalsIgnoreCase(originTag.trim()) && !StringUtils.EMPTY.equalsIgnoreCase(targetTag.trim())) {
			targetSourcePackageDir = sourcePackageDirectory + File.separator + "pwc." + branchname + ".build." + originTag + "_"
					+ targetTag;

		} else {
			logger.info("Please checked the values provided");
		}

		return targetSourcePackageDir;

	}

	/**
	 * Creates the dir ifnot exists.
	 *
	 * @param file the file
	 * @return the file
	 */
	public static File createDirIfnotExists(File file) {

		if (!file.exists()) {
			file.mkdir();
		}

		return file;
	}

}