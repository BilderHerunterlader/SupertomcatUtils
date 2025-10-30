package ch.supertomcat.supertomcatutils.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Search and Replace by Regular Expressions
 */
public class RegexSearch {
	/**
	 * Compiled Pattern
	 */
	protected Pattern pattern = null;

	/**
	 * Search
	 */
	protected String search = "";

	/**
	 * Constructor
	 */
	public RegexSearch() {
	}

	/**
	 * Constructor
	 * 
	 * @param search Regex Search Pattern
	 * @throws PatternSyntaxException
	 */
	public RegexSearch(String search) throws PatternSyntaxException {
		this(search, true);
	}

	/**
	 * Constructor
	 * 
	 * @param search Regex Search Pattern
	 * @param compilePattern True if pattern should be compiled, false otherwise
	 * @throws PatternSyntaxException
	 */
	public RegexSearch(String search, boolean compilePattern) throws PatternSyntaxException {
		if (search == null) {
			throw new IllegalArgumentException("Search was null");
		}
		setSearch(search, compilePattern);
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
	 * Match
	 * 
	 * @param input Input
	 * @return True if matched, false otherwise
	 */
	public boolean match(String input) {
		// If pattern is not compiled or url is empty we return false
		if (!executionNeeded(input, 0)) {
			return false;
		}

		return pattern.matcher(input).matches();
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
}
