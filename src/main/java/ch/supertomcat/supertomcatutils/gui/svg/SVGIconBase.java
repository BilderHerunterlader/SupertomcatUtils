package ch.supertomcat.supertomcatutils.gui.svg;

/**
 * Base class for SVGIcon implementations
 */
public abstract class SVGIconBase implements SVGIcon {
	/**
	 * Icon Width
	 */
	protected final int iconWidth;

	/**
	 * Icon Height
	 */
	protected final int iconHeight;

	/**
	 * Constructor
	 * 
	 * @param iconWidth Icon Width
	 * @param iconHeight Icon Height
	 */
	public SVGIconBase(int iconWidth, int iconHeight) {
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
	}

	@Override
	public int getIconWidth() {
		return iconWidth;
	}

	@Override
	public int getIconHeight() {
		return iconHeight;
	}
}
