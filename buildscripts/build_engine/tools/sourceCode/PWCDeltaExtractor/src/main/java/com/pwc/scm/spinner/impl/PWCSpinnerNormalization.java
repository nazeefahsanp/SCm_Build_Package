package com.pwc.scm.spinner.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;

/**
 * The Class PWCSpinnerDeltaFileExtractor.
 * 
 * @author ANKUR
 */
public class PWCSpinnerNormalization {

	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCSpinnerNormalization.class);

	/** The dependent files. */
	private List<File> dependentFiles;

	private final static String TAB = "\\t";

	private final static String PIPE = "\\|";

	/**
	 * Extract.
	 *
	 * @param properties    the properties
	 * @param   the origin tag directory
	 * @param targetTagDir  the target tag directory
	 * @param checkOutLevel the check out level
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	public void transform(Properties properties, File targetTagDir, String sourcePackageDir) throws IOException {

		Set<String> settingsColumnFile = new HashSet<>(Arrays.asList(((String) properties.get("keyName.settings")).split(",")));
		Set<String> multivaluedColumns = new HashSet<>(
				Arrays.asList(((String) properties.get("keyName.multivaluedcolumns")).split(",")));
		Set<String> columnsToSort = new HashSet<>(Arrays.asList(((String) properties.get("pwc.column.to.be.sort")).split(",")));

		// Normalize the file into /temp/TargetTag_spinner_transformed
		File targetSpinnerDir = new File(targetTagDir.getPath() + "/spinner/");
		for (File file : FileUtils.listFiles(targetSpinnerDir, null, Boolean.TRUE)) {

			logger.info("File Name :- " + file.getName());

			File targetFile = new File(sourcePackageDir + "distrib/"+targetTagDir.getName()
					+"/spinner/"+ StringUtils.substringAfter(file.getAbsolutePath(), "spinner"));
			
			if (!targetFile.exists()){
		        Files.createDirectories(Paths.get(targetFile.getParent()));
		    }
			
			try (CSVWriter writer = new CSVWriter(new FileWriter(targetFile))) {

				if (settingsColumnFile.contains(file.getName()) && multivaluedColumns.contains(file.getName())) {
					writer.writeAll(normalizeBoth(file, columnsToSort));
					writer.flush();
				} else if (settingsColumnFile.contains(file.getName())) {
					writer.writeAll(normalizeSpinnerFileSettingColumn(file));
					writer.flush();
				} else if (multivaluedColumns.contains(file.getName())) {
					writer.writeAll(normalizeSpinnerFileMultiValuedColumn(file, columnsToSort));
					writer.flush();
				} else {
					FileUtils.copyFile(file, new File(sourcePackageDir + "distrib/"+targetTagDir.getName()
					+"/spinner/" + StringUtils.substringAfter(file.getAbsolutePath(), "spinner")));
				}
			} catch (Exception e) {
				logger.error(e);
			}

		}

	}

	private List<String[]> normalizeBoth(File file, Set<String> columnsToSort) {

		List<String[]> csvBody = null;

		try (com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new FileReader(file))) {

			csvBody = reader.readAll();

			String[] header = csvBody.get(0);

			List<Integer> multiValuedColumnIndices = new ArrayList<>();
			int settingColumnIndex = 0;

			for (int i = 0; i < header[0].split(TAB).length; i++) {
				if (columnsToSort.contains(header[0].split(TAB)[i])) {
					multiValuedColumnIndices.add(i);
				} else if (header[0].split(TAB)[i].startsWith("Settings")) {
					settingColumnIndex = i;
				}
			}

			logger.info(multiValuedColumnIndices);
			logger.info(settingColumnIndex);

			TreeSet<String> multiValueColumntreeSet;

			// Get CSV row column and replace with by using row and column
			for (int i = 1; i < csvBody.size(); i++) {

				String[] strArray = csvBody.get(i);
				String[] cell = strArray[0].split(TAB);

				// Normalizing multivalued columns
				multiValueColumntreeSet = new TreeSet<>();
				for (Integer inte : multiValuedColumnIndices) {

					if (inte < cell.length) {
						for (int p = 0; p < cell[inte].split(PIPE).length; p++) {
							multiValueColumntreeSet.add(cell[inte].split(PIPE)[p]);
						}

						cell[inte] = String.join("|", multiValueColumntreeSet);
						strArray[0].split(TAB)[inte] = String.join("|", multiValueColumntreeSet);

					}
				}

				strArray[0] = String.join("\t", cell);
				logger.info(strArray[0]);

				// Normalize setting and its value column
				Map<String, String> settingsValueTreeMap = new TreeMap<>();
				for (int p = 0; p < cell[settingColumnIndex].split(PIPE).length; p++) {
					settingsValueTreeMap.put(cell[settingColumnIndex].split(PIPE)[p],
							cell[settingColumnIndex + 1].split(PIPE)[p]);
				}

				cell[settingColumnIndex] = String.join("|", settingsValueTreeMap.keySet());
				cell[settingColumnIndex + 1] = String.join("|", settingsValueTreeMap.values());

				strArray[0].split(TAB)[settingColumnIndex] = String.join("|", settingsValueTreeMap.keySet());
				strArray[0].split(TAB)[settingColumnIndex + 1] = String.join("|", settingsValueTreeMap.values());

				strArray[0] = String.join("\t", cell);
				logger.info(strArray[0]);

				return csvBody;

			}
		} catch (IOException e) {
			logger.error(e);
		}
		return csvBody;

	}

	private List<String[]> normalizeSpinnerFileMultiValuedColumn(File file, Set<String> columnsToSort) {

		List<String[]> csvBody = null;

		try (com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new FileReader(file))) {

			csvBody = reader.readAll();

			String[] header = csvBody.get(0);
			List<Integer> multiValuedColumnIndices = new ArrayList<>();

			for (int i = 0; i < header[0].split(TAB).length; i++) {
				if (columnsToSort.contains(header[0].split(TAB)[i])) {
					multiValuedColumnIndices.add(i);
					break;
				}
			}

			logger.info(multiValuedColumnIndices);

			TreeSet<String> obj;

			// Get CSV row column and replace with by using row and column
			for (int i = 1; i < csvBody.size(); i++) {

				String[] strArray = csvBody.get(i);
				String[] cell = strArray[0].split(TAB);

				obj = new TreeSet<>();
				for (Integer inte : multiValuedColumnIndices) {

					if (inte < cell.length) {
						for (int p = 0; p < cell[inte].split(PIPE).length; p++) {
							obj.add(cell[inte].split(PIPE)[p]);
						}

						cell[inte] = String.join("|", obj);
						strArray[0].split(TAB)[inte] = String.join("|", obj);
					}
				}

				strArray[0] = String.join("\t", cell);
			}

			return csvBody;

		} catch (IOException e) {
			logger.error(e);
		}

		return csvBody;

	}

	private List<String[]> normalizeSpinnerFileSettingColumn(File file) {

		File inputFile = file;

		List<String[]> csvBody = null;

		try (com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new FileReader(inputFile))) {

			csvBody = reader.readAll();

			String[] header = csvBody.get(0);
			int settingColumnIndex = 0;
			for (int i = 0; i < header[0].split(TAB).length - 1; i++) {
				if (header[0].split(TAB)[i].startsWith("Settings")) {
					settingColumnIndex = i;
					break;
				}
			}

			TreeMap<String, String> obj;

			// Get CSV row column and replace with by using row and column
			for (int i = 1; i < csvBody.size(); i++) {

				String[] strArray = csvBody.get(i);
				String[] cell = strArray[0].split(TAB);

				obj = new TreeMap<>();
				for (int p = 0; p < cell[settingColumnIndex].split(PIPE).length; p++) {
					obj.put(cell[settingColumnIndex].split(PIPE)[p], cell[settingColumnIndex + 1].split(PIPE)[p]);
				}

				cell[settingColumnIndex] = String.join("|", obj.keySet());
				cell[settingColumnIndex + 1] = String.join("|", obj.values());

				strArray[0].split(TAB)[settingColumnIndex] = String.join("|", obj.keySet());
				strArray[0].split(TAB)[settingColumnIndex + 1] = String.join("|", obj.values());

				strArray[0] = String.join("\t", cell);

			}

			return csvBody;

		} catch (IOException e) {
			logger.error(e);
		}

		return csvBody;
	}

	/**
	 * Gets the dependent files.
	 *
	 * @return the dependent files
	 */
	public List<File> getDependentFiles() {
		return dependentFiles;
	}

	/**
	 * Sets the dependent files.
	 *
	 * @param dependentFiles the new dependent files
	 */
	public void setDependentFiles(List<File> dependentFiles) {
		this.dependentFiles = dependentFiles;
	}

}
