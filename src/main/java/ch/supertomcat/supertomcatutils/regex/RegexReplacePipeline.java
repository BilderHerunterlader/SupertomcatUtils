package ch.supertomcat.supertomcatutils.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RulePipeline
 */
public class RegexReplacePipeline {
	/**
	 * Logger for this class
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * title
	 */
	protected final String title;

	/**
	 * RuleRegExps
	 */
	protected final List<RegexReplace> regexReplaces;

	/**
	 * Constructor
	 * 
	 * @param title Title
	 */
	public RegexReplacePipeline(String title) {
		this(title, new ArrayList<>());
	}

	/**
	 * Constructor
	 * 
	 * @param title Title
	 * @param regexReplaces Regex Replaces
	 */
	public RegexReplacePipeline(String title, List<RegexReplace> regexReplaces) {
		this.title = title;
		this.regexReplaces = regexReplaces;
	}

	/**
	 * Returns the replaced String
	 * 
	 * @param input Input
	 * @param undoWhenEmpty Undo replacement, when replaced String is empty
	 * @param replacementType Replacement Type for logging
	 * @return Replaced String
	 */
	protected String doReplacement(String input, boolean undoWhenEmpty, String replacementType) {
		String result = input;
		for (int i = 0; i < regexReplaces.size(); i++) {
			String beforeResult = result;
			result = regexReplaces.get(i).doReplace(result);
			logger.debug("{} -> Replace done -> Step {} -> Result: {}", beforeResult, i, result);
		}

		if (undoWhenEmpty && result.isEmpty()) {
			logger.debug("{} cannot be empty, so Replacement will be undone", replacementType);
			return input;
		}
		return result;
	}

	/**
	 * Returns the page-title after replacement
	 * 
	 * @param input Input
	 * @return Replaced Page-Title
	 */
	public String getReplacedPageTitle(String input) {
		return doReplacement(input, true, "Page-Title");
	}

	/**
	 * Returns the filename after replacement
	 * 
	 * @param input Input
	 * @return Replaced Filename
	 */
	public String getReplacedFilename(String input) {
		return doReplacement(input, true, "Filename");
	}

	/**
	 * Returns all RuleRegExps
	 * 
	 * @return RuleRegExps
	 */
	public List<RegexReplace> getRegexps() {
		return regexReplaces;
	}

	/**
	 * Returns the RuleRegExp
	 * 
	 * @param index Index in the array
	 * @return RuleRegExp
	 */
	public RegexReplace getRegexp(int index) {
		return regexReplaces.get(index);
	}

	/**
	 * Adds a RuleRegExp to the pipeline
	 * 
	 * @param rre RuleRegExp
	 */
	public void addRegExp(RegexReplace rre) {
		if (rre != null) {
			regexReplaces.add(rre);
		}
	}

	/**
	 * Removes a RuleRegExp from the pipeline
	 * 
	 * @param index1 First Index
	 * @param index2 Second Index
	 */
	public void swapRegExp(int index1, int index2) {
		if (index1 == index2) {
			return;
		} else if (index1 < 0 || index1 >= regexReplaces.size()) {
			return;
		} else if (index2 < 0 || index2 >= regexReplaces.size()) {
			return;
		}

		Collections.swap(regexReplaces, index1, index2);
	}

	/**
	 * Removes a RuleRegExp from the pipeline
	 * 
	 * @param index Index in the array
	 */
	public void removeRegExp(int index) {
		if (index >= 0 && index < regexReplaces.size()) {
			regexReplaces.remove(index);
		}
	}
}
