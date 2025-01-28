package ch.supertomcat.supertomcatutils.image;

import java.awt.Image;
import java.awt.image.ImageObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to wait for image size available
 */
public class ImageSizeWaiter {
	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Image
	 */
	private final Image image;

	/**
	 * Sync Object
	 */
	private final Object syncObject = new Object();

	/**
	 * Size Available Flag
	 */
	private boolean sizeAvailable = false;

	/**
	 * Error Flag
	 */
	private boolean error = false;

	/**
	 * Constructor
	 * 
	 * @param image Image
	 */
	public ImageSizeWaiter(Image image) {
		this.image = image;
	}

	/**
	 * Wait until size available
	 * 
	 * @return True if image size available, false otherwise
	 */
	public boolean waitForSize() {
		return waitForSize(0L);
	}

	/**
	 * Wait until size available or timeout
	 * 
	 * @param timeout Timeout or 0
	 * 
	 * @return True if image size available, false otherwise
	 */
	public boolean waitForSize(long timeout) {
		synchronized (syncObject) {
			ImageObserver observer = new ImageObserver() {

				@Override
				public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
					if ((infoflags & (ImageObserver.HEIGHT | ImageObserver.WIDTH)) != 0) {
						synchronized (syncObject) {
							sizeAvailable = true;
							syncObject.notifyAll();
						}
						return false;
					}

					if ((infoflags & ImageObserver.ALLBITS) != 0) {
						synchronized (syncObject) {
							sizeAvailable = true;
							syncObject.notifyAll();
						}
						return false;
					}

					if ((infoflags & (ImageObserver.ERROR | ImageObserver.ABORT)) != 0) {
						synchronized (syncObject) {
							error = true;
							syncObject.notifyAll();
						}
						return false;
					}

					return true;
				}
			};

			int width = image.getWidth(observer);
			int height = image.getHeight(observer);

			if (width > -1 && height > -1) {
				return true;
			}

			while (!sizeAvailable && !error) {
				try {
					syncObject.wait(timeout);
					if (sizeAvailable) {
						return true;
					}
					if (error) {
						return false;
					}
					if (timeout > 0) {
						return false;
					}
				} catch (InterruptedException e) {
					logger.error("Wait for image size available was interrupted", e);
					return false;
				}
			}
			return sizeAvailable;
		}
	}
}
