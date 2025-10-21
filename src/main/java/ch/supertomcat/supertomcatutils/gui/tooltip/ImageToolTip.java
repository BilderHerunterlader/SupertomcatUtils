package ch.supertomcat.supertomcatutils.gui.tooltip;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolTip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ToolTip for displaying an image
 */
public class ImageToolTip extends JToolTip {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Label to display image
	 */
	private JLabel lblImage = new JLabel();

	/**
	 * Image Display Width
	 */
	private int imageDisplayWidth;

	/**
	 * Image Display Height
	 */
	private int imageDisplayHeight;

	/**
	 * UI Preferred Size
	 */
	private Dimension uiPreferredSize;

	/**
	 * Constructor
	 * 
	 * @param imageDisplayWidth Image Display Width or -1
	 * @param imageDisplayHeight Image Display Height or -1
	 */
	public ImageToolTip(int imageDisplayWidth, int imageDisplayHeight) {
		this.imageDisplayWidth = imageDisplayWidth;
		this.imageDisplayHeight = imageDisplayHeight;
		setLayout(new BorderLayout());
		uiPreferredSize = getUI().getPreferredSize(this);

		addPropertyChangeListener("tiptext", evt -> {
			String toolTipText = (String)evt.getNewValue();

			if (toolTipText == null || toolTipText.isEmpty()) {
				lblImage.setIcon(null);
				return;
			}

			String[] arr = toolTipText.split("\0", 2);
			Path filePath;
			if (arr.length == 2) {
				filePath = Paths.get(arr[1]);
			} else {
				filePath = null;
				lblImage.setIcon(null);
			}

			int width = this.imageDisplayWidth;
			int height = this.imageDisplayHeight;

			if (filePath != null && Files.exists(filePath)) {
				try (InputStream in = Files.newInputStream(filePath)) {
					BufferedImage image = ImageIO.read(in);
					if (image == null) {
						lblImage.setIcon(null);
					} else if (width > 0 || height > 0) {
						int imageWidth = image.getWidth();
						int imageHeight = image.getHeight();
						if (width <= 0) {
							width = imageWidth;
						}
						if (height <= 0) {
							height = imageHeight;
						}

						double scaleWidth = (double)width / imageWidth;
						double scaleHeight = (double)height / imageHeight;
						// Use smaller scale
						double scale = Math.min(scaleWidth, scaleHeight);

						int imageWidthScaled = Math.clamp((int)Math.round(imageWidth * scale), 1, width);
						int imageHeightScaled = Math.clamp((int)Math.round(imageHeight * scale), 1, height);

						lblImage.setIcon(new ImageIcon(image.getScaledInstance(imageWidthScaled, imageHeightScaled, Image.SCALE_SMOOTH)));
						lblImage.setSize(imageWidthScaled, imageHeightScaled);
					} else {
						lblImage.setIcon(new ImageIcon(image));
						lblImage.setSize(image.getWidth(), image.getHeight());
					}
				} catch (IOException e) {
					logger.error("Could not load image: {}", filePath, e);
					lblImage.setIcon(null);
				}
			} else {
				lblImage.setIcon(null);
			}

			uiPreferredSize = getUI().getPreferredSize(this);
		});
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (lblImage.getIcon() == null || uiPreferredSize == null) {
			return;
		}

		// Create a copy of the Graphics so that all changes don't affect orignal
		Graphics2D gCopy = (Graphics2D)g.create();
		gCopy.translate(0, uiPreferredSize.height);
		gCopy.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		gCopy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gCopy.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		lblImage.paint(gCopy);
		gCopy.dispose();
	}

	@Override
	public Dimension getPreferredSize() {
		if (lblImage.getIcon() == null || uiPreferredSize == null) {
			return super.getPreferredSize();
		}

		Dimension imageLabelSize = lblImage.getSize();
		int imageLabelWidth = imageLabelSize.width;
		int imageLabelHeight = imageLabelSize.height;

		int combinedPreferredWidth = uiPreferredSize.width >= imageLabelWidth ? uiPreferredSize.width : imageLabelWidth;
		int combinedPreferredHeight = uiPreferredSize.height + imageLabelHeight;

		return new Dimension(combinedPreferredWidth, combinedPreferredHeight);
	}
}
