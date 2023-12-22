package ch.supertomcat.supertomcatutils.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Search and Replace by Regular Expressions
 */
public class RegexReplace {
	/**
	 * Compiled Pattern
	 */
	protected Pattern pattern = null;

	/**
	 * Search
	 */
	protected String search = "";

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
		if (search == null || replace == null) {
			throw new IllegalArgumentException("Search or replace was null: Search: " + search + ", Replace: " + replace);
		}
		setSearch(search, compilePattern);
		setReplace(replace);
	}

	/**
	 * Returns the Search-String
	 * 
	 * @return Search-String
	 */
	public String getSearch() {
		return search;
	}

	/**
	 * Sets the Search-String and try to compile the Pattern
	 * 
	 * @param search Search-String
	 * @throws PatternSyntaxException
	 */
	public void setSearch(String search) throws PatternSyntaxException {
		setSearch(search, true);
	}

	/**
	 * Sets the Search-String and try to compile the Pattern. Note: If compilePattern is false, then the pattern Member is set to null.
	 * 
	 * @param search Search-String
	 * @param compilePattern True if pattern should be compiled, false otherwise
	 * @throws PatternSyntaxException
	 */
	public void setSearch(String search, boolean compilePattern) throws PatternSyntaxException {
		if (search == null) {
			throw new IllegalArgumentException("Search was null: Search: " + search);
		}
		if (compilePattern) {
			this.pattern = Pattern.compile(search);
		} else {
			this.pattern = null;
		}
		this.search = search;
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
	 * Checks if execution of search or replace is possible and needed
	 * 
	 * @param input Input
	 * @return True if execution of search or replace is possible and needed, false otherwise
	 */
	protected boolean executionNeeded(String input) {
		return pattern != null && !search.isEmpty() && !input.isEmpty();
	}

	/**
	 * Checks if execution of search or replace is possible and needed
	 * 
	 * @param input Input
	 * @param start Start Index
	 * @return True if execution of search or replace is possible and needed, false otherwise
	 */
	protected boolean executionNeeded(String input, int start) {
		return executionNeeded(input) && start >= 0 && start < input.length();
	}

	/**
	 * Search-Method
	 * Returns the start-position when found the pattern
	 * Returns -1 if not found the pattern or an error occured
	 * 
	 * @param input Input
	 * @param start Startposition
	 * @return Found position
	 */
	public int doSearch(String input, int start) {
		// If pattern is not compiled or url is empty we return -1
		if (!executionNeeded(input, start)) {
			return -1;
		}

		Matcher matcher = pattern.matcher(input);
		if (matcher.find(start)) {
			return matcher.start();
		}
		return -1;
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
