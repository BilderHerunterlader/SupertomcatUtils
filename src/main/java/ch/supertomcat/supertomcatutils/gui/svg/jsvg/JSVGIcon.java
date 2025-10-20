package ch.supertomcat.supertomcatutils.gui.svg.jsvg;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.renderer.animation.AnimationState;
import com.github.weisj.jsvg.renderer.awt.AwtComponentPlatformSupport;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.view.FloatSize;

import ch.supertomcat.supertomcatutils.gui.svg.SVGIconBase;

/**
 * SVGIcon implementation using JSVG
 */
public class JSVGIcon extends SVGIconBase {
	/**
	 * Animation State Client Property
	 */
	public static final String ANIMATION_STATE_CLIENT_PROPERTY = "JSVGAnimationState";

	/**
	 * SVG Document
	 */
	private final SVGDocument svgDocument;

	/**
	 * Constructor
	 * 
	 * @param svgDocument SVG Document
	 */
	public JSVGIcon(SVGDocument svgDocument) {
		this(svgDocument, (int)svgDocument.size().getWidth(), (int)svgDocument.size().getHeight());
	}

	/**
	 * Constructor
	 * 
	 * @param svgDocument SVG Document
	 * @param iconWidth Icon Width
	 * @param iconHeight Icon Height
	 */
	public JSVGIcon(SVGDocument svgDocument, int iconWidth, int iconHeight) {
		super(iconWidth, iconHeight);
		this.svgDocument = svgDocument;
	}

	/**
	 * Returns the svgDocument
	 * 
	 * @return svgDocument
	 */
	public SVGDocument getSvgDocument() {
		return svgDocument;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		int width = getIconWidth();
		int height = getIconHeight();

		FloatSize svgSize = svgDocument.size();
		double svgWidth = svgSize.getWidth();
		double svgHeight = svgSize.getHeight();
		if (width <= 0) {
			width = (int)svgWidth;
		}
		if (height <= 0) {
			height = (int)svgHeight;
		}

		double scaleWidth = width / svgWidth;
		double scaleHeight = height / svgHeight;
		// Use larger scale
		double scale = Math.min(scaleWidth, scaleHeight);

		int svgWidthScaled = Math.clamp((int)Math.round(svgWidth * scale), 0, width);
		int svgHeightScaled = Math.clamp((int)Math.round(svgHeight * scale), 0, height);

		int xCorrection = (width - svgWidthScaled) / 2;
		int yCorrection = (height - svgHeightScaled) / 2;

		/*
		 * Create a copy of the Graphics context, so that changes like scale, rendering hints and so on and whatever JSVG might does are not affecting the
		 * original Graphics context
		 */
		Graphics2D gCopy = (Graphics2D)g.create();
		try {
			gCopy.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			gCopy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			gCopy.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

			gCopy.translate(x + xCorrection, y + yCorrection);

			if (scale != 1.0d) {
				gCopy.scale(scale, scale);
			}

			if (svgDocument.isAnimated() && c instanceof JComponent comp && comp.getClientProperty(ANIMATION_STATE_CLIENT_PROPERTY) instanceof AnimationState animationState) {
				Output output = Output.createForGraphics(gCopy);
				try {
					svgDocument.renderWithPlatform(new AwtComponentPlatformSupport(comp), output, null, animationState);
				} finally {
					output.dispose();
				}
			} else if (c instanceof Component comp) {
				svgDocument.render(comp, gCopy, null);
			}
		} finally {
			gCopy.dispose();
		}
	}
}
