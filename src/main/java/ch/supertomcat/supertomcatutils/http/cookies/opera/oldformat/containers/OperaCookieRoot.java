package ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opera Cookie Root
 */
public class OperaCookieRoot {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Cookie-Datei
	 */
	private String file;

	/**
	 * Datei-Header
	 */
	private OperaCookieHeader och = new OperaCookieHeader();

	/**
	 * Domains
	 */
	private List<OperaDomain> domains = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param file File
	 */
	public OperaCookieRoot(String file) {
		this.file = file;
	}

	/**
	 * Cookie-Datei einlesen
	 * 
	 * @return True if successful, false otherwise
	 */
	public boolean read() {
		try (FileInputStream in = new FileInputStream(file); OperaCookieInputStream oin = new OperaCookieInputStream(in)) {
			// Read Header
			if (oin.read(och) == OperaCookieHeader.HEADER_BYTE_LENGTH) {
				int tagSize = och.getTagSize();
				int recordSize = och.getRecordSize();

				domains.addAll(oin.readDomainComponents(tagSize, recordSize));
				return true;
			} else {
				logger.error("Could not read opera cookie file header");
				return false;
			}
		} catch (IOException e) {
			logger.error("Could not read cookies from: {}", file, e);
			return false;
		}
	}

	/**
	 * @return the och
	 */
	public OperaCookieHeader getOch() {
		return och;
	}

	/**
	 * @return the domains
	 */
	public List<OperaDomain> getDomains() {
		return domains;
	}

	/**
	 * @return All Cookies
	 */
	public List<OperaCookie> getAllCookies() {
		List<OperaCookie> allCookies = new ArrayList<>();
		for (OperaDomain domain : domains) {
			allCookies.addAll(domain.getCookies(true));
		}
		return allCookies;
	}
}
