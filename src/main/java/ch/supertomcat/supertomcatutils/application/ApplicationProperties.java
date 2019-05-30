package ch.supertomcat.supertomcatutils.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class for storing application wide properties
 */
public final class ApplicationProperties {
	/**
	 * Application Properties
	 */
	private static final Properties applicationProperties = new Properties();

	/**
	 * Constructor
	 */
	private ApplicationProperties() {
	}

	/**
	 * @param in InputStream
	 * @throws IOException
	 */
	public static void initProperties(InputStream in) throws IOException {
		applicationProperties.load(in);
	}

	/**
	 * @param name Name
	 * @return Property
	 */
	public static String getProperty(String name) {
		return applicationProperties.getProperty(name);
	}

	/**
	 * @param name Name
	 * @param value Value
	 */
	public static void setProperty(String name, String value) {
		applicationProperties.setProperty(name, value);
	}
}
