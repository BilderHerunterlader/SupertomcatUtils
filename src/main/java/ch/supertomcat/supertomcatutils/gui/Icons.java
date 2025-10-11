package ch.supertomcat.supertomcatutils.gui;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BaseMultiResolutionImage;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.image.ImageSizeWaiter;

/**
 * Class which contains a list of icon resources
 */
public final class Icons {
	private static final String DUMMY_ICON_FILENAME = "dummy.png";
	private static final String APPL_ICON_RESOURCE_FORMAT = "/" + Icons.class.getPackage().getName().replace(".", "/") + "/icons/%s";
	private static final String APPL_ICON_SIZE_RESOURCE_FORMAT = "/" + Icons.class.getPackage().getName().replace(".", "/") + "/icons/%dx%d/%s";
	private static final String TANGO_ICON_RESOURCE_FORMAT = "/org/freedesktop/tango/%dx%d/%s";

	private static final int[] TANGO_ICON_SIZES = { 16, 22, 32 };

	private static final int[] APPL_ICON_SIZES = { 16, 22, 32, 64, 128, 256 };

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
	 * Image Cache
	 */
	private static Map<String, Image> imageCache = new HashMap<>();

	/**
	 * Set for not found images
	 */
	private static Set<String> notFoundImages = new HashSet<>();

	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(Icons.class);

	/**
	 * Dummy Image 16 Pixel
	 */
	private static Image dummy16 = getApplImage(DUMMY_ICON_FILENAME, 16);

	/**
	 * Dummy Image 22 Pixel
	 */
	private static Image dummy22 = getApplImage(DUMMY_ICON_FILENAME, 22);

	/**
	 * Dummy Image 32 Pixel
	 */
	private static Image dummy32 = getApplImage(DUMMY_ICON_FILENAME, 32);

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
	 * @return ImageIcon
	 */
	public static ImageIcon getTangoMultiResIcon(String resource) {
		return new ImageIcon(getTangoMultiResImage(resource));
	}

	/**
	 * Returns a Multi Resolution ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getTangoMultiResIcon(String resource, int size) {
		return new ImageIcon(getTangoMultiResImage(resource, size));
	}

	/**
	 * Returns a Multi Resolution Image for the given resource
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static Image getTangoMultiResImage(String resource) {
		return getTangoMultiResImage(resource, 16);
	}

	/**
	 * Returns a Multi Resolution Image for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static Image getTangoMultiResImage(String resource, int size) {
		int resourceCount = TANGO_ICON_SIZES.length;
		if (IntStream.of(TANGO_ICON_SIZES).noneMatch(x -> x == size)) {
			resourceCount++;
		}

		String[] resources = new String[resourceCount];
		resources[0] = String.format(TANGO_ICON_RESOURCE_FORMAT, size, size, resource);
		String[] tempArr = IntStream.of(TANGO_ICON_SIZES).filter(x -> x != size).mapToObj(x -> String.format(TANGO_ICON_RESOURCE_FORMAT, x, x, resource)).toArray(String[]::new);
		System.arraycopy(tempArr, 0, resources, 1, tempArr.length);

		return getMultiResImage(resources);
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
	 * Returns a Multi Resolution ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static ImageIcon getApplMultiResIcon(String resource) {
		return new ImageIcon(getApplMultiResImage(resource));
	}

	/**
	 * Returns a Multi Resolution ImageIcon for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static ImageIcon getApplMultiResIcon(String resource, int size) {
		return new ImageIcon(getApplMultiResImage(resource, size));
	}

	/**
	 * Returns a Multi Resolution Image for the given resource
	 * 
	 * @param resource Resource
	 * @return ImageIcon
	 */
	public static Image getApplMultiResImage(String resource) {
		return getApplMultiResImage(resource, 16);
	}

	/**
	 * Returns a Multi Resolution Image for the given resource
	 * 
	 * @param resource Resource
	 * @param size Size
	 * @return ImageIcon
	 */
	public static Image getApplMultiResImage(String resource, int size) {
		int resourceCount = APPL_ICON_SIZES.length;
		if (IntStream.of(APPL_ICON_SIZES).noneMatch(x -> x == size)) {
			resourceCount++;
		}

		String[] resources = new String[resourceCount];
		resources[0] = String.format(APPL_ICON_SIZE_RESOURCE_FORMAT, size, size, resource);
		String[] tempArr = IntStream.of(APPL_ICON_SIZES).filter(x -> x != size).mapToObj(x -> String.format(APPL_ICON_SIZE_RESOURCE_FORMAT, x, x, resource)).toArray(String[]::new);
		System.arraycopy(tempArr, 0, resources, 1, tempArr.length);

		return getMultiResImage(resources);
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
		return new ImageIcon(getMultiResImage(resources));
	}

	/**
	 * Returns a Mutli Resolution Image for the given resources
	 * 
	 * @param resources Resources
	 * @return ImageIcon
	 */
	public static Image getMultiResImage(String... resources) {
		Image[] images = new Image[resources.length];

		/*
		 * Use media tracker to wait until images are fully loaded, so that ImageIcon actually gets the image size and displays the image.
		 */
		int loadedImages = 0;
		synchronized (MEDIA_TRACKER) {
			for (int i = 0; i < resources.length; i++) {
				boolean first = i == 0;
				int id = getNextMediaTrackerID();
				Image loadedImage = getImage(resources[i], null, first);

				if (loadedImage == null) {
					continue;
				}

				MEDIA_TRACKER.addImage(images[i], id);
				try {
					MEDIA_TRACKER.waitForID(id, 0);
				} catch (InterruptedException e) {
					logger.error("Wait for media tracker was interrupted", e);
				}
				MEDIA_TRACKER.removeImage(images[i], id);

				ImageSizeWaiter imageSizeWaiter = new ImageSizeWaiter(loadedImage);
				if (!imageSizeWaiter.waitForSize(500)) {
					logger.warn("Image size not yet available: {}", resources[i]);
				}

				images[loadedImages] = loadedImage;
				loadedImages++;
			}
		}

		if (loadedImages == 0) {
			return dummy16;
		}

		if (loadedImages != images.length) {
			Image[] tempArr = new Image[loadedImages];
			System.arraycopy(images, 0, tempArr, 0, loadedImages);
			images = tempArr;
		}

		return new BaseMultiResolutionImage(images);
	}

	/**
	 * Returns an Image for the given resource
	 * 
	 * @param resource Resource
	 * @return Image or dummy image if not found
	 */
	public static Image getImage(String resource) {
		return getImage(resource, dummy16, true);
	}

	/**
	 * Returns an Image for the given resource
	 * 
	 * @param resource Resource
	 * @param defaultValue
	 * @return Image or defualt value if not found
	 */
	public static Image getImage(String resource, Image defaultValue) {
		return getImage(resource, defaultValue, true);
	}

	/**
	 * Returns an Image for the given resource
	 * 
	 * @param resource Resource
	 * @param defaultValue
	 * @param logError True if an error should be logged, if the image was not found, false otherwise
	 * @return Image or defualt value if not found
	 */
	public static synchronized Image getImage(String resource, Image defaultValue, boolean logError) {
		Image image = imageCache.get(resource);
		if (image != null) {
			return image;
		}

		if (notFoundImages.contains(resource)) {
			return defaultValue;
		}

		image = loadImage(resource, logError);
		if (image != null) {
			imageCache.put(resource, image);
			return image;
		}

		notFoundImages.add(resource);

		return defaultValue;
	}

	/**
	 * Load image
	 * 
	 * @param resource Resource
	 * @param logError True if an error should be logged, if the image could not be found, false otherwise
	 * @return Image or null if it could not be loaded
	 */
	private static Image loadImage(String resource, boolean logError) {
		try {
			URL resourceURL = Icons.class.getResource(resource);
			if (resourceURL != null) {
				return Toolkit.getDefaultToolkit().getImage(resourceURL);
			} else {
				if (logError) {
					logger.error("Could not load image: {}", resource);
				}
				return null;
			}
		} catch (Exception e) {
			logger.error("Could not load image: {}", resource, e);
			return null;
		}
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
