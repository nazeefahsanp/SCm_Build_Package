package com.pwc.scm.spinner.impl;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.pwc.scm.util.PWCDeltaExtractorUtils;

/**
 * 
 * @author Sanjay.Meena
 *
 */
public class PWCSpinnerDeltaFileExtractor extends PWCDeltaFileExtractor {

	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCSpinnerDeltaFileExtractor.class);

	/** The dependent files. */
	private File dependentFile ;

	/** The constructor with origin & source file arguments. */
	public  PWCSpinnerDeltaFileExtractor() {
	}
	
	/** The constructor with origin & source file arguments. */
	public  PWCSpinnerDeltaFileExtractor(File originFile, File targetFile, File relatedFile) {
		super(originFile, targetFile);
		dependentFile = relatedFile;
	}
	
	
	
	@Override
	public void extract() throws IOException {
		// TODO Auto-generated method stub
		super.extract();
		
		//ONLY APPLICABLE FOR CHNAGED FILE AND NOT the NEW FILE
		if(dependentFile != null && isTargetFileChanged()) {
			PWCDeltaExtractorUtils.extractSpinnerLineByAdminObjectName(dependentFile, _targetFile.getName());
			logger.info("extracting DEPENDENT file content from "+ dependentFile.getName() + "  for admin object : " +_targetFile.getName());
		}
			
		
	}

	/**
	 * @return the dependentFile
	 */
	public File getDependentFile() {
		return dependentFile;
	}

	/**
	 * @param dependentFile the dependentFile to set
	 */
	public void setDependentFile(File dependentFile) {
		this.dependentFile = dependentFile;
	}

	
}
