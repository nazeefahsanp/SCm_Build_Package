package com.pwc.scm.spinner.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.pwc.scm.spinner.PWCDeltaExtractor;
import com.pwc.scm.util.PWCDeltaExtractorUtils;
/**
 * 
 * @author Sanjay.Meena
 *
 */
public class PWCDeltaFileExtractor extends PWCDeltaExtractor {

	/** The result directory. */
	public String resultDir = "";

	/** The check out level. */
	public String checkOutLevel = "";

	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCDeltaFileExtractor.class);
	
	/** The constructor with origin & source file arguments. */
	public  PWCDeltaFileExtractor(File originFile, File targetFile) {
		this._originFile = originFile;
		this._targetFile = targetFile;
		
	}
	
	/**
	 * Instantiates a new compare folders.
	 */
	public PWCDeltaFileExtractor() {
		super();
	}


	@Override
	/**
	 * Main method to extract delta
	 */
	public void extract() throws IOException {
		// Copy file from target to origin
		if(isTargetFileChanged()) {
			FileUtils.copyFile(_targetFile, PWCDeltaExtractorUtils.getCorrespondingDeltaOutputFileFromTarget(_targetFile));
		}
			
		
	}

}