package ch.supertomcat.supertomcatutils.regex;

import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * Search and Replace by Regular Expressions
 */
public class RegexReplace extends RegexSearch {
	/**
	 * Replace
	 */
	protected String replace = "";

	/**
	 * Constructor
	 */
	public RegexReplace() {
	}

	/**
	 * Constructor
	 * 
	 * @param search Regex Search Pattern
	 * @param replace Replacement String
	 * @throws PatternSyntaxException
	 */
	public RegexReplace(String search, String replace) throws PatternSyntaxException {
		this(search, replace, true);
	}

	/**
	 * Constructor
	 * 
	 * @param search Regex Search Pattern
	 * @param replace Replacement String
	 * @param compilePattern True if pattern should be compiled, false otherwise
	 * @throws PatternSyntaxException
	 */
	public RegexReplace(String search, String replace, boolean compilePattern) throws PatternSyntaxException {
		super(search, compilePattern);
		if (replace == null) {
			throw new IllegalArgumentException("Replace was null");
		}
		setReplace(replace);
	}

	/**
	 * Returns the Replace-String
	 * 
	 * @return Replace-String
	 */
	public String getReplace() {
		return replace;
	}

	/**
	 * Sets the Replace-String
	 * 
	 * @param replace Replace-String
	 */
	public void setReplace(String replace) {
		if (replace == null) {
			throw new IllegalArgumentException("Replace was null: Replace: " + replace);
		}
		this.replace = replace;
	}

	/**
	 * Replace-Method which returns the replaced input String or the input if the pattern is not compiled, the input is empty or the replacement failed
	 * 
	 * @param input Input
	 * @return Replaced String
	 */
	public String doReplace(String input) {
		// If pattern is not compiled or url is empty we return the input
		if (!executionNeeded(input)) {
			return input;
		}

		return pattern.matcher(input).replaceAll(replace);
	}

	/**
	 * Replace-Method which only returns the match region or an empty Stirng if the pattern is not compiled, start parameter is invalid (negative or higher than
	 * input length) or the input is empty
	 * 
	 * @param input Input
	 * @param start Startposition
	 * @return Replaced String
	 */
	public String doReplaceMatchRegion(String input, int start) {
		// If pattern is not compiled or url is empty we return an empty String
		if (!executionNeeded(input, start)) {
			return "";
		}

		Matcher matcher = pattern.matcher(input);
		if (matcher.find(start)) {
			Matcher regionMatcher = pattern.matcher(matcher.group());
			return regionMatcher.replaceAll(replace);
		}
		return "";
	}
}
