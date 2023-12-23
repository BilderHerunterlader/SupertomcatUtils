package ch.supertomcat.supertomcatutils.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
import ch.supertomcat.supertomcatutils.io.CopyUtil;

/**
 * Class which handels the settings
 * 
 * @param <T> Settings JAXB Type
 * @param <L> Settings Listener Type
 */
public abstract class SettingsManagerBase<T, L extends SettingsListener> {
	/**
	 * Logger for this class
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * JAXB Context
	 */
	protected final JAXBContext jaxbContext;

	/**
	 * Settings
	 */
	protected T settings = null;

	/**
	 * Settings Folder
	 */
	protected final File settingsFolder;

	/**
	 * Settings File
	 */
	protected final File settingsFile;

	/**
	 * Settings Backup File
	 */
	protected final File settingsBackupFile;

	/**
	 * Resource path to default settings file
	 */
	protected final String defaultSettingsResourcePath;

	/**
	 * Resource path to schema file
	 */
	protected final String settingsSchemaResourcePath;

	/**
	 * Listener
	 */
	protected List<L> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Dummy Constructor. ONLY USE FOR UNIT TESTS.
	 * 
	 * @param objectFactoryClass Object Factory Class
	 * @param defaultSettingsResourcePath Resource path to default settings file
	 * @param settingsSchemaResourcePath Resource path to schema file
	 * @throws JAXBException
	 */
	protected SettingsManagerBase(Class<?> objectFactoryClass, String defaultSettingsResourcePath, String settingsSchemaResourcePath) throws JAXBException {
		settingsFolder = null;
		settingsFile = null;
		settingsBackupFile = null;
		jaxbContext = JAXBContext.newInstance(objectFactoryClass);
		this.defaultSettingsResourcePath = defaultSettingsResourcePath;
		this.settingsSchemaResourcePath = settingsSchemaResourcePath;
	}

	/**
	 * Constructor
	 * 
	 * @param strSettingsFolder Settings Folder
	 * @param strSettingsFilename Settings Filename
	 * @param objectFactoryClass Object Factory Class
	 * @param defaultSettingsResourcePath Resource path to default settings file
	 * @param settingsSchemaResourcePath Resource path to schema file
	 * @throws JAXBException
	 */
	public SettingsManagerBase(String strSettingsFolder, final String strSettingsFilename, Class<?> objectFactoryClass, String defaultSettingsResourcePath,
			String settingsSchemaResourcePath) throws JAXBException {
		settingsFolder = new File(strSettingsFolder);
		settingsFile = new File(settingsFolder, strSettingsFilename);
		settingsBackupFile = new File(settingsFolder, strSettingsFilename + ".backup");

		jaxbContext = JAXBContext.newInstance(objectFactoryClass);
		this.defaultSettingsResourcePath = defaultSettingsResourcePath;
		this.settingsSchemaResourcePath = settingsSchemaResourcePath;

		createDirectoryIfNotExists(settingsFolder);

		if (!settingsFile.exists()) {
			logger.info("Settingsfile not found in folder '{}': {}", settingsFolder.getAbsolutePath(), settingsFile.getAbsolutePath());
			restoreSettingsFileFromBackupFile();
		} else {
			if (settingsFile.length() == 0) {
				logger.error("Settingsfile is empty: {}", settingsFile.getAbsolutePath());
				restoreSettingsFileFromBackupFile();
			} else {
				long now = System.currentTimeMillis();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS");
				String target = settingsFile.getAbsolutePath() + "-" + dateFormat.format(now);
				CopyUtil.copy(settingsFile.getAbsolutePath(), target);

				// Delete old backup-Files
				ApplicationUtil.deleteOldBackupFiles(settingsFolder, strSettingsFilename, 3);

				if (!settingsBackupFile.exists()) {
					backupSettingsFile();
				}
			}
		}
	}

	/**
	 * Restore settings file from backup file
	 * 
	 * @return True if settings file could be restored, false otherwise
	 */
	protected synchronized boolean restoreSettingsFileFromBackupFile() {
		if (settingsBackupFile.exists() && settingsBackupFile.length() > 0) {
			logger.info("Restoring Settingsfile with backup: {}", settingsBackupFile.getAbsolutePath());
			CopyUtil.copy(settingsBackupFile.getAbsolutePath(), settingsFile.getAbsolutePath());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Backup settings file
	 * 
	 * @return True if settings file could be backed up, false otherwise
	 */
	protected synchronized boolean backupSettingsFile() {
		if (settingsFile.exists() && settingsFile.length() > 0) {
			logger.debug("Backing up Settingsfile: {}", settingsBackupFile.getAbsolutePath());
			CopyUtil.copy(settingsFile.getAbsolutePath(), settingsBackupFile.getAbsolutePath());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Create directory if it not exists
	 * 
	 * @param directory Directory
	 * @return True if successful, false otherwise
	 */
	protected boolean createDirectoryIfNotExists(File directory) {
		if (!directory.exists()) {
			try {
				Files.createDirectories(directory.toPath());
				return true;
			} catch (IOException e) {
				logger.error("Settings-Folder could not be created: {}", directory.getAbsolutePath(), e);
				return false;
			}
		}
		return true;
	}

	/**
	 * Load User Settings File
	 * 
	 * @return Settings
	 * @throws IOException
	 * @throws SAXException
	 * @throws JAXBException
	 */
	protected synchronized T loadUserSettingsFile() throws IOException, SAXException, JAXBException {
		try (FileInputStream in = new FileInputStream(settingsFile)) {
			return loadSettingsFile(in, false);
		}
	}

	/**
	 * Load Default Settings File
	 * 
	 * @return Settings
	 * @throws IOException
	 * @throws SAXException
	 * @throws JAXBException
	 */
	protected synchronized T loadDefaultSettingsFile() throws IOException, SAXException, JAXBException {
		try (InputStream in = getClass().getResourceAsStream(defaultSettingsResourcePath)) {
			if (in == null) {
				throw new IllegalArgumentException("Resource not found: " + defaultSettingsResourcePath);
			}
			return loadSettingsFile(in, true);
		}
	}

	/**
	 * Load Settings File
	 * 
	 * @param in InputStream
	 * @param validateSchema True if schema should be validated, false otherwise. Schema should only be validated for the default settings file and not for user
	 *        settings files, because also settings files created with older versions of the program must be loaded.
	 * @return Settings
	 * @throws SAXException
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	protected synchronized T loadSettingsFile(InputStream in, boolean validateSchema) throws SAXException, JAXBException {
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		if (validateSchema) {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(getClass().getResource(settingsSchemaResourcePath));
			unmarshaller.setSchema(schema);
		}
		return (T)unmarshaller.unmarshal(in);
	}

	/**
	 * Write Settings File
	 * 
	 * @param settings Settings
	 * @param out OutputStream
	 * @param validateSchema True if schema should be validated, false otherwise. Schema should only be validated for the default settings file and not for user
	 *        settings files, because also settings files created with older versions of the program must be loaded.
	 * @throws SAXException
	 * @throws JAXBException
	 */
	protected synchronized void writeSettingsFile(T settings, OutputStream out, boolean validateSchema) throws SAXException, JAXBException {
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		if (validateSchema) {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(getClass().getResource(settingsSchemaResourcePath));
			marshaller.setSchema(schema);
		}
		marshaller.marshal(settings, out);
	}

	/**
	 * Settings Changed
	 */
	protected void settingsChanged() {
		for (SettingsListener listener : listeners) {
			listener.settingsChanged();
		}
	}

	/**
	 * Add listener
	 * 
	 * @param l Listener
	 */
	public void addSettingsListener(L l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * Remove listener
	 * 
	 * @param l Listener
	 */
	public void removeSettingsListener(L l) {
		listeners.remove(l);
	}

	/**
	 * Returns the settings
	 * 
	 * @return settings
	 */
	public T getSettings() {
		return settings;
	}
}
