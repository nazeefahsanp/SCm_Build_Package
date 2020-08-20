package com.pwc.scm.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.pwc.scm.spinner.impl.PWCSpinnerNormalization;
import com.pwc.scm.util.PWCDeltaConfiguration;
import com.pwc.scm.util.PWCDeltaExtractorUtils;
import com.pwc.scm.util.PWCSpinnerUtils;

/**
 * The Class ScmInitiator.
 * 
 * @author Sanjay.Meena
 */
public class PWCScmInitiator {

	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCScmInitiator.class);

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException     Signals that an I/O exception has occurred.
	 * @throws GitAPIException the git API exception
	 */
	public static void main(String[] args) throws IOException {
		
		logger.info("******************* DELTA PROCESS START *******************");
		
		String branchName = args[0];
		String originTag = args[1];
		String targetTag = args[2];
		String buildDir = args[3];
		
		String logFilePath = buildDir + File.separator + targetTag + "/logs/buildLog.txt";
		//Load configuration property file
		String deltaOutputDir = buildDir + File.separator + "distrib/delta/" ;
	    String targetTagDir = buildDir + File.separator + targetTag;
	    String originTagDir = buildDir + File.separator + originTag;
	    PWCDeltaExtractorUtils.configureLogger(logFilePath);
	    
	    /**Initialize one-time configuration */
		PWCDeltaConfiguration.getInstance().init(deltaOutputDir, targetTagDir, originTagDir);

		/**Setup DELTA generation location */
	    Path targetTagPath = Paths.get(targetTagDir);
	    
	    /**Normalize the Spinner files */
	    normalizeSpinnerFiles(args, branchName, originTag, targetTag, buildDir);
	    	    
	    /**Setup DELTA generation location */
	    PWCDeltaExtractorUtils.beginDeltaExtractionProcess(targetTagPath, "root");
		
		logger.info("******************* DELTA PROCESS END *******************");

	}
	
	/**
	 * Normalize spinner files.
	 *
	 * @param args the args
	 * @param branchName the branch name
	 * @param originTag the origin tag
	 * @param targetTag the target tag
	 * @param sourcePackageDir the source package dir
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void normalizeSpinnerFiles(String[] args, String branchName, String originTag, String targetTag,
			String sourcePackageDir) throws IOException {
		String targetPackageDir = PWCSpinnerUtils.prepareTargetPackageDirPath(branchName, originTag, targetTag,
				sourcePackageDir);

		String logFilePath = targetPackageDir + "/logs/buildLog.txt";
		String propertyFile = "transformation.properties";

		PWCSpinnerUtils.configureLogger(logFilePath);
		Properties properties = PWCSpinnerUtils.readPropertiesFile(propertyFile);

		logger.info("****************** STARTS *****************");

		logger.info("Number of argument passed :- " + args.length);
		
		if (!StringUtils.EMPTY.equalsIgnoreCase(originTag.trim())
				&& !StringUtils.EMPTY.equalsIgnoreCase(targetTag.trim())) {

			logger.info("Two repository tags have been provided, Process starts for DELTA build. Tag :- " + originTag
					+ StringUtils.SPACE + targetTag);

			// OriginTag directory object
			File originTagDir = new File(sourcePackageDir + originTag);

			// TargetTag directory object
			File targetTagDir = new File(sourcePackageDir + targetTag);

			// Normalizing and transformed spinner files
			PWCSpinnerNormalization pwcSpinnerDeltaFileExt = new PWCSpinnerNormalization();
			pwcSpinnerDeltaFileExt.transform(properties, targetTagDir, sourcePackageDir);
			pwcSpinnerDeltaFileExt.transform(properties, originTagDir, sourcePackageDir);

		} else {
			logger.info("");
		}

		logger.info("******************* END *******************");
	}

}
