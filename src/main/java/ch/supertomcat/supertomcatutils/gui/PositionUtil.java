package ch.supertomcat.supertomcatutils.gui;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

/**
 * Class which provides methods to set the dimension and position of windows in the middle of the screen
 * or in the middle of another window
 */
public final class PositionUtil {
	/**
	 * Constructor
	 */
	private PositionUtil() {
	}

	/**
	 * Sets the position of the window to the middle of the screen. This method will try to place the window on the same
	 * screen as the parent if provided, otherwise the main screen is used.
	 * 
	 * @param window Window
	 * @param parent Parent Window
	 */
	public static void setPositionMiddleScreen(Window window, Component parent) {
		if (window == null) {
			return;
		}

		GraphicsDevice device = getScreenDeviceOfComponent(parent);
		if (device != null) {
			Rectangle screenBounds = device.getDefaultConfiguration().getBounds();

			Point screenCenterPoint = getCenterPoint(screenBounds);
			int screenX = screenBounds.x;
			int screenY = screenBounds.y;
			int screenCenterX = screenCenterPoint.x;
			int screenCenterY = screenCenterPoint.y;
			int screenWidth = screenBounds.width;
			int screenHeight = screenBounds.height;

			int windowWidth = window.getWidth();
			int windowHeight = window.getHeight();

			int targetX = screenCenterX - windowWidth / 2;
			int targetY = screenCenterY - windowHeight / 2;

			// Prevent window being placed outside of screen
			// Check Bottom (Needs to be done before Top, for the case where window height is bigger than screen height)
			if (targetY + windowHeight > screenY + screenHeight) {
				targetY = screenY + screenHeight - windowHeight;
			}

			// Check Top (Needs to be done after Bottom, for the case where window height is bigger than screen height)
			if (targetY < screenY) {
				targetY = screenY;
			}

			// Check Right (Needs to be done before Left, for the case where window width is bigger than screen width)
			if (targetX + windowWidth > screenX + screenWidth) {
				targetX = screenX + screenWidth - windowWidth;
			}

			// Check Left (Needs to be done after Right, for the case where window width is bigger than screen width)
			if (targetX < screenX) {
				targetX = screenX;
			}

			window.setLocation(targetX, targetY);
		} else {
			window.setLocationRelativeTo(null);
		}
	}

	/**
	 * Returns the screen device of the given component or null if not found
	 * 
	 * @param comp Component
	 * @return Screen device of the given component or null if not found
	 */
	public static GraphicsDevice getScreenDeviceOfComponent(Component comp) {
		if (comp == null) {
			return null;
		}
		GraphicsConfiguration graphicsConfig = comp.getGraphicsConfiguration();
		if (graphicsConfig != null) {
			return graphicsConfig.getDevice();
		}
		return null;
	}

	/**
	 * Center point of bounds
	 * 
	 * @param bounds Bounds
	 * @return Center point of bounds
	 */
	public static Point getCenterPoint(Rectangle bounds) {
		return getCenterPoint(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	/**
	 * Center point of bounds
	 * 
	 * @param x X
	 * @param y Y
	 * @param width Width
	 * @param height Height
	 * @return Center point of bounds
	 */
	public static Point getCenterPoint(int x, int y, int width, int height) {
		return new Point(x + width / 2, y + height / 2);
	}
}
