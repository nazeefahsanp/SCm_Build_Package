/**
 * 
 */
package com.pwc.scm.helper;

import java.io.File;

import org.apache.log4j.Logger;

import com.pwc.scm.spinner.PWCDeltaExtractor;
import com.pwc.scm.spinner.impl.PWCDeltaFileExtractor;
import com.pwc.scm.spinner.impl.PWCSpinnerDeltaFileExtractor;
import com.pwc.scm.spinner.impl.PWCSpinnerDeltaLineExtractor;
import com.pwc.scm.spinner.impl.PWCSpinnerDeltaSameAdminObjectAllLinesExtractor;

/**
 * @author Sanjay.Meena
 *
 */
public class PWCSpinnerDeltaExtractorFactory {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCSpinnerDeltaExtractorFactory.class);

	
	/**
	 * Default Constructor
	 */
	public PWCSpinnerDeltaExtractorFactory() {
	}
	
	/**
	 * Factory method o get Extractor class
	 * @param originFile
	 * @param targetFile
	 * @param relatedFile
	 * @param configuredClass
	 * @return
	 */
	public static PWCDeltaExtractor getSpinnerDeltaExtractor(File originFile, File targetFile, File relatedFile, String configuredClass) {
		logger.info("Initializing Configuration Class ---> "+configuredClass);
		if ( configuredClass.equals("PWCDeltaFileExtractor") ) {
		    return new PWCDeltaFileExtractor(originFile,targetFile);
		} else if ( configuredClass.equals("PWCSpinnerDeltaLineExtractor") ) {
	    	return new PWCSpinnerDeltaLineExtractor(originFile,targetFile, relatedFile);
	    } else if ( configuredClass.equals("PWCSpinnerDeltaFileExtractor") ) {
	    	return new PWCSpinnerDeltaFileExtractor(originFile,targetFile, relatedFile);
	    } else if ( configuredClass.equals("PWCSpinnerDeltaSameAdminObjectAllLinesExtractor") ) {
	    	return new PWCSpinnerDeltaSameAdminObjectAllLinesExtractor(originFile,targetFile);
	    } else {
	    	logger.error("Incompatible delta spinner configuration define. Please correct delta config properties.");
        }

	    return null;
	  }
	
	

}
