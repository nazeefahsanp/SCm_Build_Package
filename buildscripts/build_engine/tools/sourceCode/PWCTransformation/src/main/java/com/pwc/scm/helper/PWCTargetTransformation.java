package com.pwc.scm.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * The Class PWCFullBuildExtractor.
 * 
 * @author ANKUR
 */
public class PWCTargetTransformation {

	/** The Constant KEY_TARGET. */
	private static final String KEY_TARGET = "target.";

	/** The Constant KEY_TARGET_DIR. */
	@SuppressWarnings("unused")
	private static final String KEY_TARGET_DIR = "output.structure.deploymentpackage.directory";

	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCTargetTransformation.class);

	/**
	 * Extract.
	 *
	 * @param properties       the properties
	 * @param repoTagName      the repository tag name
	 * @param sourcePackageDir the source package directory
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void transformToTargetStructure(Properties properties, String repoTagName, String sourcePackageDir, String targetPackageDir)
			throws IOException {

		Set<Object> allPropKeys = properties.keySet();

		Set<String> targetKeys = new HashSet<>();
		for (Object key : allPropKeys) {
			if (key.toString().startsWith(KEY_TARGET)) {
				targetKeys.add(key.toString());
			}
		}

		for (String key : targetKeys) {
			
			logger.info("Keys :- " + key);

			String targetDirectory = targetPackageDir + properties.getProperty(key);

			if (properties.getProperty(key).isEmpty()) {
				logger.info("Value of property is empty !! " + key);

			} else if (key.startsWith("target.thirdparty")) {

				String thirdPartySourceDirectory = sourcePackageDir + repoTagName + "/thirdparty/";

				copyThirdPartyJars(thirdPartySourceDirectory, targetDirectory);

			} else {

				String filePath = sourcePackageDir + repoTagName + '/'
						+ key.substring(KEY_TARGET.length()).replace(".", "/");

				File dir = new File(filePath);

				if (dir.exists()) {
					copyFileOrFolder(targetDirectory, dir);
				} else if (checkIfFile(targetDirectory, filePath)) {
					logger.info("File has been copied");
				} else {
					logger.info("Directory or File does not exists for key. Key : " + key);
				}
			}

		}

		logger.info(targetKeys.size());

	}

	/**
	 * Copy third party jars.
	 *
	 * @param thirdPartySourceDirectory the third party source directory
	 * @param thirdPartyTargetDirectory the third party target directory
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void copyThirdPartyJars(String thirdPartySourceDirectory, String thirdPartyTargetDirectory)
			throws IOException {

		List<String> jarFileList = findSpecifiedFiles(thirdPartySourceDirectory, new ArrayList<String>(), "jar");

		for (String jarFile : jarFileList) {
			copyFileOrFolder(thirdPartyTargetDirectory, new File(jarFile));
		}
	}

	/**
	 * Find specified files.
	 *
	 * @param sourceDirectory the source directory
	 * @param resultList      the result list
	 * @param fileExtension   the file extension
	 * @return the list
	 */
	List<String> findSpecifiedFiles(String sourceDirectory, List<String> resultList, String fileExtension) {

		File dir = new File(sourceDirectory);

		if (dir.listFiles() != null)
			for (File file : dir.listFiles()) {
				if (file.isDirectory()) {
					findSpecifiedFiles(file.getAbsolutePath(), resultList, fileExtension);
				}

				if (file.getName().endsWith(("." + fileExtension))) {
					resultList.add(file.getAbsolutePath());
				}
			}

		return resultList;
	}

	/**
	 * Check if file.
	 *
	 * @param targetDirectory the target directory
	 * @param filePath        the file path
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private boolean checkIfFile(String targetDirectory, String filePath) throws IOException {

		int index = filePath.lastIndexOf('/');

		if (index == -1) {
			logger.info(filePath);
		}

		File file = new File(filePath.substring(0, index) + "." + filePath.substring(index + "/".length()));

		if (file.isFile()) {
			copyFileOrFolder(targetDirectory, file);
			return true;
		}

		return false;
	}

	/**
	 * Copy file or folder.
	 *
	 * @param target the target
	 * @param file   the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void copyFileOrFolder(String target, File file) throws IOException {

		File destination = null;

		if (file.isDirectory()) {

			destination = new File(target);
			FileUtils.copyDirectory(file, destination);
			logger.info(file.getName() + " Directory has been copied. From : " + file.getAbsolutePath() + ", To : "
					+ destination.getAbsolutePath());
		} else {
			destination = new File(target);
			
			if(file.exists()) {
				FileUtils.copyFileToDirectory(file, destination);
				logger.info(file.getName() + " File has been copied. From : " + file.getAbsolutePath() + ", To : "
						+ destination.getAbsolutePath());
			} else {
				logger.error("File does not exists :-" + file.getAbsolutePath());
			}

		}
	}
}