package com.pwc.scm.main;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.pwc.scm.helper.PWCPrioritizeDependency;
import com.pwc.scm.helper.PWCTargetTransformation;
import com.pwc.scm.util.PWCTransformationUtils;

/**
 * The Class ScmInitiator.
 * 
 * @author ANKUR
 */
public class PWCScmTranformInitiator {

	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCScmTranformInitiator.class);

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException     Signals that an I/O exception has occurred.
	 * @throws GitAPIException the git API exception
	 */
	public static void main(String[] args) throws IOException {

		String branchName = args[0];
		String originTag = args[1];
		String targetTag = args[2];
		String sourcePackageDir = args[3];

		String targetPackageDir = PWCTransformationUtils.prepareTargetPackageDirPath(branchName, originTag, targetTag,
				sourcePackageDir);

		String logFilePath = targetPackageDir + "/logs/buildLog.txt";
		String propertyFile = "transformation.properties";

		PWCTransformationUtils.configureLogger(logFilePath);
		Properties properties = PWCTransformationUtils.readPropertiesFile(propertyFile);

		PWCTargetTransformation pwcTargetTransformObj;

		logger.info("****************** STARTS *****************");

		logger.info("Number of argument passed :- " + args.length);

		if (StringUtils.EMPTY.equalsIgnoreCase(originTag.trim())
				&& StringUtils.EMPTY.equalsIgnoreCase(targetTag.trim())) {

			logger.info("No repository tag have been provided, Check the parameter passed");

		} else if (StringUtils.EMPTY.equalsIgnoreCase(originTag.trim())
				|| StringUtils.EMPTY.equalsIgnoreCase(targetTag.trim())) {

			logger.info("One repository tag have been provided, Process starts for FULL build. Tag :- " + originTag
					+ " " + targetTag);

			// Creating final target transformed build structure
			pwcTargetTransformObj = new PWCTargetTransformation();
			pwcTargetTransformObj.transformToTargetStructure(properties, targetTag, sourcePackageDir, targetPackageDir);
			
		} else if (!StringUtils.EMPTY.equalsIgnoreCase(originTag.trim())
				&& !StringUtils.EMPTY.equalsIgnoreCase(targetTag.trim())) {

			logger.info("Two repository tags have been provided, Transformation starts. Tag :- " + originTag
					+ StringUtils.SPACE + targetTag);

			// Creating final target transformed build structure
			pwcTargetTransformObj = new PWCTargetTransformation();
			pwcTargetTransformObj.transformToTargetStructure(properties, "distrib/delta/", sourcePackageDir, targetPackageDir);
			
		} else {
			logger.info("There is some issue with repository tag values. Please check !!");
		}
		
		Map<String, String> dependencyResultMap = PWCTransformationUtils.resolveDependency(sourcePackageDir+targetTag+"/");
		
		logger.info("dependencies="+PWCPrioritizeDependency.getDependencyList(dependencyResultMap));
		
		logger.info("****************** END *****************");
		
		System.out.println("dependencies="+PWCPrioritizeDependency.getDependencyList(dependencyResultMap));
		
		System.exit(0);

	}

	

}
