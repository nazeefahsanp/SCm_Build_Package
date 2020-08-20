/**
 * 
 */
package com.pwc.scm.spinner;

import java.io.File;
import java.io.IOException;

import com.pwc.scm.util.PWCDeltaExtractorUtils;

/**
 * 
 * @author Sanjay.Meena
 *
 */
public abstract class PWCDeltaExtractor {


	/** File from ORIGIN TAG*/
	protected File _originFile;

	/** File from TARGET TAG*/
	protected File _targetFile;	
	
	/** The TAB SEPERATOR for reading writing to file*/
	public static final char TAB_SEPERATOR  = '\t';
	
	/** Method to check if file from target tag is same or changed w.r.t. file from origin tag*/
	protected boolean isTargetFileChanged() throws IOException {
		if(_originFile == null) {
			return true;
		}
		String targetChecksum = PWCDeltaExtractorUtils.checksum(_targetFile);
		String originChecksum = PWCDeltaExtractorUtils.checksum(_originFile);
		return !targetChecksum.equals(originChecksum);
			
	}
	
	/** extract new/changed file*/
	public abstract void extract() throws IOException;
	
	
}
