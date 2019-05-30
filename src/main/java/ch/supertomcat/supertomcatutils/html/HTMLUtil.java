package ch.supertomcat.supertomcatutils.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Class which provides methods to escape, unescape HTML Strings or get the encoding from a HTML source
 */
public final class HTMLUtil {
	private static final Pattern REGEX_ENCODING_PATTERN = Pattern.compile("<meta http-equiv=\"Content-Type\" content=\".*?; charset=(.*?)\" />");

	/**
	 * Constructor
	 */
	private HTMLUtil() {
	}

	/**
	 * Returns the charset defined in meta http-equiv="Content-Type" tag if available otherwise an empty String
	 * 
	 * @param htmlCode Page Source Code
	 * @return Encoding or an empty String
	 */
	public static String getEncodingFromSourceCode(String htmlCode) {
		Matcher matcher = REGEX_ENCODING_PATTERN.matcher(htmlCode);
		if (matcher.find()) {
			Matcher regionMatcher = REGEX_ENCODING_PATTERN.matcher(matcher.group());
			return regionMatcher.replaceAll("$1");
		}
		return "";
	}

	/**
	 * Escapes HTML String
	 * 
	 * @param input
	 * @return Escaped String
	 */
	public static String escapeHTML(String input) {
		return StringEscapeUtils.escapeHtml4(input);
	}

	/**
	 * Unescapes HTML String
	 * 
	 * @param input
	 * @return Unescaped String
	 */
	public static String unescapeHTML(String input) {
		return StringEscapeUtils.unescapeHtml4(input);
	}
}
