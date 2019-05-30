package ch.supertomcat.supertomcatutils.application.libraries;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for read out information about libraries
 */
public final class LibraryInfoUtil {
	/**
	 * Pattern for manifest resources
	 */
	private static final Pattern LIBRARY_NAME_PATTERN = Pattern.compile("(?i).+/(.+?)\\.jar!/META-INF/MANIFEST\\.MF$");

	/**
	 * Libraries
	 */
	private static List<LibraryInfo> libraries = null;

	/**
	 * Constructor
	 */
	private LibraryInfoUtil() {
	}

	/**
	 * @return Libraries
	 */
	public static synchronized List<LibraryInfo> getLibraries() {
		if (libraries == null) {
			loadLibraries();
		}
		return libraries;
	}

	/**
	 * Load Libraries
	 */
	private static synchronized void loadLibraries() {
		Logger logger = LoggerFactory.getLogger(LibraryInfoUtil.class);

		List<LibraryInfo> libs = new ArrayList<>();

		// Load Third Party Licenses
		Properties thirdPartyLicenses = new Properties();
		try {
			Enumeration<URL> thirdPartyFiles = LibraryInfoUtil.class.getClassLoader().getResources("THIRD-PARTY.txt");
			while (thirdPartyFiles.hasMoreElements()) {
				URL url = thirdPartyFiles.nextElement();
				try (InputStream in = url.openStream()) {
					thirdPartyLicenses.load(in);
				} catch (IOException e) {
					logger.error("Could not load THIRD-PARTY.txt from: {}", url, e);
				}
			}
		} catch (IOException e) {
			logger.error("Could not get THIRD-PARTY.txt list", e);
		}

		// Load Manifests from jar files
		try {
			Enumeration<URL> manifestFiles = LibraryInfoUtil.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (manifestFiles.hasMoreElements()) {
				URL url = manifestFiles.nextElement();

				Matcher matcher = LIBRARY_NAME_PATTERN.matcher(url.getPath());
				if (!matcher.matches()) {
					logger.error("Could not get library name from path: {}", url.getPath());
					continue;
				}
				String name = matcher.group(1);
				String version = "";
				String license = thirdPartyLicenses.getProperty(name, "");

				try (InputStream in = url.openStream()) {
					Manifest manifest = new Manifest(in);
					Attributes attributes = manifest.getMainAttributes();
					String implVersion = attributes.getValue("Implementation-Version");
					String bundleVersion = attributes.getValue("Bundle-Version");
					String bundleLicense = attributes.getValue("Bundle-License");
					if (implVersion != null) {
						version = implVersion;
					} else if (bundleVersion != null) {
						version = bundleVersion;
					}
					if (bundleLicense != null) {
						if (license.isEmpty()) {
							license = bundleLicense;
						} else {
							license += "\n" + bundleLicense;
						}
					}
				} catch (IOException e) {
					logger.error("Could not load manifest from: {}", url, e);
				}

				libs.add(new LibraryInfo(name, version, license));
			}
		} catch (IOException e) {
			logger.error("Could not get MANIFEST.MF list", e);
		}

		// Load additional information from Application_Library_Licenses.properties
		try {
			Enumeration<URL> licensesPropFiles = LibraryInfoUtil.class.getClassLoader().getResources("Application_Library_Licenses.properties");
			while (licensesPropFiles.hasMoreElements()) {
				URL url = licensesPropFiles.nextElement();

				try (InputStream in = url.openStream()) {
					Properties properties = new Properties();
					properties.load(in);

					Set<String> keys = new HashSet<>();

					for (Object key : properties.keySet()) {
						String strKey = key.toString();
						int pos = strKey.lastIndexOf('.');
						if (pos > 0) {
							keys.add(strKey.substring(0, pos));
						}
					}

					for (String key : keys) {
						String name = properties.getProperty(key + ".name");
						if (name == null) {
							continue;
						}
						String version = properties.getProperty(key + ".version");
						String license = properties.getProperty(key + ".license");
						libs.add(new LibraryInfo(name, version != null ? version : "", license != null ? license : ""));
					}
				} catch (IOException e) {
					logger.error("Could not load Application_Library_Licenses.properties from: {}", url, e);
				}
			}
		} catch (IOException e) {
			logger.error("Could not get Application_Library_Licenses.properties list", e);
		}

		Collections.sort(libs);
		libraries = Collections.unmodifiableList(libs);
	}
}
