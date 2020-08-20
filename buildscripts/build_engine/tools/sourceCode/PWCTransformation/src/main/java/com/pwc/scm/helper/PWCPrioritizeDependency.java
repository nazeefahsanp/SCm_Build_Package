/**
 * 
 */
package com.pwc.scm.helper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The Class PWCPrioritizeDependency.
 *
 * @author Sanjay.Meena
 */
public class PWCPrioritizeDependency {

	/** The logger. */
	private static Logger logger = Logger.getLogger(PWCPrioritizeDependency.class);

	/** The dependency list string. */
	private static StringBuilder depListString = new StringBuilder();

	/** The dependency map. */
	private static Map<String, String> dependencyMap = new HashMap<>();

	/**
	 * Gets the dependency list.
	 *
	 * @param dependencyMap the dependency map
	 * @return the dependency list
	 */
	public static String getDependencyList(Map<String, String> dependencyMap) {

		for (String moduleName : dependencyMap.keySet()) {
			logger.info("Module Name :- " + moduleName);
			String moduleDependencies = dependencyMap.get(moduleName);
			buildOrderedDependencyList(moduleName, moduleDependencies);
			addDependencyIfNotPresent(moduleName);
			logger.info("depListString: " + depListString);

		}
		logger.info("FINAL depListString: " + depListString);

		return depListString.toString();

	}

	/**
	 * Builds the ordered dependency list.
	 *
	 * @param moduleName         the module name
	 * @param moduleDependencies the module dependencies
	 */
	private static void buildOrderedDependencyList(String moduleName, String moduleDependencies) {
		// Get Dependencies
		if (moduleDependencies == null || moduleDependencies.isEmpty()) {
			// CASE 1: if no dependency for this module ADD it
			addDependencyIfNotPresent(moduleName);
		} else if (!moduleDependencies.contains(",")) {
			// CASE 2: Only 1 dependency exists for this module
			buildOrderedDependencyList(moduleDependencies, dependencyMap.get(moduleDependencies));
			addDependencyIfNotPresent(moduleName);

		} else {
			// CASE 3: multiple dependencies exists for this module
			String depArray[] = moduleDependencies.split(",");
			List<String> depArrayList = Arrays.asList(depArray);
			for (String dep : depArrayList) {
				logger.info("		Dependency " + moduleName + ": " + dep);
				if (!depListString.toString().contains(dep)) {
					buildOrderedDependencyList(dep, dependencyMap.get(dep));
				}
			}
			addDependencyIfNotPresent(moduleName);
		}
	}

	/**
	 * Adds the dependency if not present.
	 *
	 * @param moduleName the module name
	 */
	private static void addDependencyIfNotPresent(String moduleName) {
		if (!depListString.toString().contains(moduleName.trim())) {

			if (depListString.toString().isEmpty()) {
				depListString.append(moduleName.trim());
			} else {
				depListString.append("," + moduleName.trim());
			}
		}
	}

}
