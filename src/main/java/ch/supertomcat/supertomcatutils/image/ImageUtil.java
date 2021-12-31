package ch.supertomcat.supertomcatutils.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.io.CopyUtil;

/**
 * Methods for working with Images
 */
public final class ImageUtil {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ImageUtil.class);

	/**
	 * Constructor
	 */
	private ImageUtil() {
	}

	/**
	 * Downloads an image
	 * 
	 * @param url URL
	 * @param connectTimeout Connection Timeout
	 * @param readTimeout Read Timeout
	 * @return Downloaded Image or null if image could not be decoded
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static BufferedImage downloadImage(String url, int connectTimeout, int readTimeout) throws IOException {
		URLConnection con = new URL(url).openConnection();
		con.setConnectTimeout(connectTimeout);
		con.setReadTimeout(readTimeout);
		try (InputStream in = con.getInputStream()) {
			return ImageIO.read(in);
		}
	}

	/**
	 * Downloads an image and returns the downloaded image or the default image in case of an error
	 * 
	 * @param url URL
	 * @param defaultImage Default Image which will be returned in case of an error
	 * @param connectTimeout Connection Timeout
	 * @param readTimeout Read Timeout
	 * @param logStackTrace True if stack trace should be logged, false otherwise
	 * @return Downloaded image or the default image in case of an error
	 */
	public static BufferedImage downloadImage(String url, BufferedImage defaultImage, int connectTimeout, int readTimeout, boolean logStackTrace) {
		try {
			BufferedImage img = downloadImage(url, connectTimeout, readTimeout);
			return img != null ? img : defaultImage;
		} catch (MalformedURLException e) {
			if (logStackTrace) {
				logger.error("ImageUtil: Image URL is malformed: {}", url, e);
			} else {
				logger.error("ImageUtil: Image URL is malformed: '{}'. Error: {}", url, e.getMessage());
			}
		} catch (IOException e) {
			if (logStackTrace) {
				logger.error("ImageUtil: Image could not be downloaded: {}", url, e);
			} else {
				logger.error("ImageUtil: Image could not be downloaded: '{}'. Error: {}", url, e.getMessage());
			}
		}
		return defaultImage;
	}

	/**
	 * Downloads an image
	 * 
	 * @param url URL
	 * @param connectTimeout Connection Timeout
	 * @param readTimeout Read Timeout
	 * @return Downloaded Image data
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static byte[] downloadImageRaw(String url, int connectTimeout, int readTimeout) throws IOException {
		URLConnection con = new URL(url).openConnection();
		con.setConnectTimeout(connectTimeout);
		con.setReadTimeout(readTimeout);
		try (InputStream in = con.getInputStream()) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			CopyUtil.copy(in, out);
			return out.toByteArray();
		}
	}

	/**
	 * Generates a preview for the given Image
	 * To preverse aspect ratio, you can set width to -1 and set height or the other way around
	 * 
	 * @param img Original Image
	 * @param width Width
	 * @param height Height
	 * @return Scaled Image
	 */
	public static BufferedImage generatePreviewImage(Image img, int width, int height) {
		return generatePreviewImage(img, width, height, Image.SCALE_DEFAULT);
	}

	/**
	 * Generates a preview for the given Image
	 * To preverse aspect ratio, you can set width to -1 and set height or the other way around
	 * 
	 * @param img Original Image
	 * @param width Width
	 * @param height Height
	 * @param hints Hints
	 * @return Scaled Image
	 */
	public static BufferedImage generatePreviewImage(Image img, int width, int height, int hints) {
		return generatePreviewImage(img, width, height, hints, BufferedImage.TYPE_INT_RGB);
	}

	/**
	 * Generates a preview for the given Image
	 * To preverse aspect ratio, you can set width to -1 and set height or the other way around
	 * 
	 * @param img Original Image
	 * @param width Width
	 * @param height Height
	 * @param hints Hints
	 * @param type Image Type
	 * @return Scaled Image
	 */
	public static BufferedImage generatePreviewImage(Image img, int width, int height, int hints, int type) {
		Dimension dim = waitForImageDimension(img);
		int origWidth = dim.width;
		int origHeight = dim.height;

		int newWidth = width;
		int newHeight = height;
		if (width < 0) {
			float scaleFactor = (float)height / origHeight;
			newWidth = (int)(origWidth * scaleFactor);
		} else if (height < 0) {
			float scaleFactor = (float)width / origWidth;
			newHeight = (int)(origHeight * scaleFactor);
		}

		Image imgScaled = img.getScaledInstance(width, height, hints);
		if (imgScaled instanceof BufferedImage) {
			return (BufferedImage)imgScaled;
		}

		BufferedImage imgScaledBuffered = new BufferedImage(newWidth, newHeight, type);
		Graphics2D bGr = imgScaledBuffered.createGraphics();
		bGr.drawImage(imgScaled, 0, 0, null);
		bGr.dispose();
		return imgScaledBuffered;
	}

	private static Dimension waitForImageDimension(Image img) {
		final DimensionValue imgWidth = new DimensionValue();
		ImageObserver imgWidthObserver = new ImageObserver() {
			@Override
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
				if (width == -1) {
					return true;
				}
				imgWidth.setValue(width);
				return false;
			}
		};
		int tmpWidth = img.getWidth(imgWidthObserver);
		if (tmpWidth == -1) {
			synchronized (imgWidth) {
				while (!imgWidth.isValueSet()) {
					try {
						imgWidth.wait();
					} catch (InterruptedException e) {
						// Nothing to do
					}
				}
				tmpWidth = imgWidth.getValue();
			}
		}

		final DimensionValue imgHeight = new DimensionValue();
		ImageObserver imgHeightObserver = new ImageObserver() {
			@Override
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
				if (height == -1) {
					return true;
				}
				imgHeight.setValue(height);
				return false;
			}
		};
		int tmpHeight = img.getHeight(imgHeightObserver);
		if (tmpHeight == -1) {
			synchronized (imgHeight) {
				while (!imgHeight.isValueSet()) {
					try {
						imgHeight.wait();
					} catch (InterruptedException e) {
						// Nothing to do
					}
				}
				tmpHeight = imgHeight.getValue();
			}
		}

		return new Dimension(tmpWidth, tmpHeight);
	}

	private static class DimensionValue {
		private int value;
		private boolean valueSet = false;

		/**
		 * Returns the valueSet
		 * 
		 * @return valueSet
		 */
		public boolean isValueSet() {
			return valueSet;
		}

		/**
		 * Returns the value
		 * 
		 * @return value
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Sets the value
		 * 
		 * @param value value
		 */
		public void setValue(int value) {
			this.value = value;
			valueSet = true;
			synchronized (this) {
				notifyAll();
			}
		}
	}
}
