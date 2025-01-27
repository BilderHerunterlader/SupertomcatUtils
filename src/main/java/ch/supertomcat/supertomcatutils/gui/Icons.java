package ch.supertomcat.supertomcatutils.gui;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BaseMultiResolutionImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which contains a list of icon resources
 */
public final class Icons {
	private static final String APPL_ICON_RESOURCE_FORMAT = "/" + Icons.class.getPackage().getName().replace(".", "/") + "/icons/%s";
	private static final String APPL_ICON_SIZE_RESOURCE_FORMAT = "/" + Icons.class.getPackage().getName().replace(".", "/") + "/icons/%dx%d/%s";
	private static final String TANGO_ICON_RESOURCE_FORMAT = "/org/freedesktop/tango/%dx%d/%s";

	private static final int[] TANGO_ICON_SIZES = { 16, 22, 32 };

	private static final Component MEDIA_TRACKER_COMPONENT = new Component() {

		/**
		 * serialVersionUID
		 */
		private static final long serialVersionUID = 1L;
		// Empty Component
	};

	private static final MediaTracker MEDIA_TRACKER = new MediaTracker(MEDIA_TRACKER_COMPONENT);

	private static int mediaTrackerID = 0;

	/**
	 * Resources
	 */
	private static Map<String, Image> iconCache = new HashMap<>();

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(Icons.class);

	/**
	 * Dummy Image 16 Pixel
	 */
	private static Image dummy16 = getApplImage("dummy.png", 16);

	/**
	 * Dummy Image 22 Pixel
	 */
	private static Image dummy22 = getApplImage("dummy.png", 22);

	/**
	 * Dummy Image 32 Pixel
	 */
	private static Image dummy32 = getApplImage("dummy.png", 32);

	private Icons() {
	}

	/**
	 * @return Next Media Tracker ID
	 */
	private static int getNextMediaTrackerID() {
		synchronized (MEDIA_TRACKER) {
			return ++mediaTrackerID;
		}
	}

	/**
	 * Returns an ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getTangoIcon(String resource, int size) {
		return getIcon(String.format(TANGO_ICON_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns a Multi Resolution ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getTangoMultiResIcon(String resource, int size) {
		int resourceCount = TANGO_ICON_SIZES.length;
		if (IntStream.of(TANGO_ICON_SIZES).noneMatch(x -> x == size)) {
			resourceCount++;
		}

		String[] resources = new String[resourceCount];
		resources[0] = String.format(TANGO_ICON_RESOURCE_FORMAT, size, size, resource);
		String[] tempArr = IntStream.of(TANGO_ICON_SIZES).filter(x -> x != size).mapToObj(x -> String.format(TANGO_ICON_RESOURCE_FORMAT, x, x, resource)).toArray(String[]::new);
		System.arraycopy(tempArr, 0, resources, 1, tempArr.length);

		return getMultiResIcon(resources);
	}

	/**
	 * Returns an Image for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return Image
	 */
	public static Image getTangoImage(String resource, int size) {
		return getImage(String.format(TANGO_ICON_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns an ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static ImageIcon getApplIcon(String resource) {
		return getIcon(String.format(APPL_ICON_RESOURCE_FORMAT, resource));
	}

	/**
	 * Returns an ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getApplIcon(String resource, int size) {
		return getIcon(String.format(APPL_ICON_SIZE_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns an Image for the given resource
	 * 
	 * @param resource Resource
	 * @return Image
	 */
	public static Image getApplImage(String resource) {
		return getImage(String.format(APPL_ICON_RESOURCE_FORMAT, resource));
	}

	/**
	 * Returns an Image for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return Image
	 */
	public static Image getApplImage(String resource, int size) {
		return getImage(String.format(APPL_ICON_SIZE_RESOURCE_FORMAT, size, size, resource));
	}

	/**
	 * Returns an ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static ImageIcon getIcon(String resource) {
		return new ImageIcon(getImage(resource));
	}

	/**
	 * Returns a Mutli Resolution ImageIcon for the given resources
	 * 
	 * @param resources Resources
	 * @return ImageIcon
	 */
	public static ImageIcon getMultiResIcon(String... resources) {
		Image[] images = new Image[resources.length];

		/*
		 * Use media tracker to wait until images are fully loaded, so that ImageIcon actually gets the image size and displays the image.
		 */
		synchronized (MEDIA_TRACKER) {
			for (int i = 0; i < resources.length; i++) {
				int id = getNextMediaTrackerID();
				images[i] = getImage(resources[i]);
				MEDIA_TRACKER.addImage(images[i], id);
				try {
					MEDIA_TRACKER.waitForID(id, 0);
				} catch (InterruptedException e) {
					logger.error("Wait for media tracker was interrupted", e);
				}
				MEDIA_TRACKER.removeImage(images[i], id);
			}
		}

		BaseMultiResolutionImage multiResolutionImage = new BaseMultiResolutionImage(images);
		return new ImageIcon(multiResolutionImage);
	}

	/**
	 * Returns an Image for the given resource or null
	 * 
	 * @param resource Resource
	 * @return Image
	 */
	public static synchronized Image getImage(String resource) {
		Image image = iconCache.get(resource);
		if (image == null) {
			image = loadImage(resource);
			if (image != null) {
				iconCache.put(resource, image);
			} else {
				logger.error("Could not load image: {}", resource);
			}
		}
		return image;
	}

	private static Image loadImage(String resource) {
		try {
			URL resourceURL = Icons.class.getResource(resource);
			if (resourceURL != null) {
				return Toolkit.getDefaultToolkit().getImage(resourceURL);
			} else {
				logger.error("Could not load image: {}", resource);
			}
		} catch (Exception e) {
			logger.error("Could not load image: {}", resource, e);
		}
		return dummy16;
	}

	/**
	 * Get Dummy Image
	 * 
	 * @param size Size
	 * @return Dummy Image
	 */
	public static Image getDummyImage(int size) {
		if (size >= 32) {
			return dummy32;
		} else if (size >= 22 && size < 32) {
			return dummy22;
		} else {
			return dummy16;
		}
	}
}
