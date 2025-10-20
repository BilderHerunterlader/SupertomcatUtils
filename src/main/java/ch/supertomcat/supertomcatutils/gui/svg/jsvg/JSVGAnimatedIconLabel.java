package ch.supertomcat.supertomcatutils.gui.svg.jsvg;

import java.awt.EventQueue;

import javax.swing.Icon;
import javax.swing.JLabel;

import com.github.weisj.jsvg.ui.AnimationPlayer;

import ch.supertomcat.supertomcatutils.gui.svg.AnimatedSVGIcon;

/**
 * JSVG Animated Icon Label
 */
public class JSVGAnimatedIconLabel extends JLabel implements AnimatedSVGIcon {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Animation Player
	 */
	private final AnimationPlayer animationPlayer;

	/**
	 * Constructor
	 * 
	 * @param icon Icon
	 * @param horizontalAlignment Horizontal Alignment
	 */
	public JSVGAnimatedIconLabel(Icon icon, int horizontalAlignment) {
		this(null, icon, horizontalAlignment);
	}

	/**
	 * Constructor
	 * 
	 * @param icon Icon
	 */
	public JSVGAnimatedIconLabel(Icon icon) {
		this(null, icon, CENTER);
	}

	/**
	 * Constructor
	 * 
	 * @param text Text
	 * @param icon Icon
	 * @param horizontalAlignment Horizontal Alignment
	 */
	public JSVGAnimatedIconLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		this.animationPlayer = new AnimationPlayer(e -> updateAnimation());
		if (icon instanceof JSVGIcon svgIcon) {
			animationPlayer.setAnimation(svgIcon.getSvgDocument().animation());
		}
	}

	@Override
	public void setIcon(Icon icon) {
		super.setIcon(icon);
		if (icon instanceof JSVGIcon svgIcon) {
			animationPlayer.setAnimation(svgIcon.getSvgDocument().animation());
		}
	}

	/**
	 * Update animation
	 */
	private void updateAnimation() {
		if (getIcon() instanceof JSVGIcon) {
			putClientProperty(JSVGIcon.ANIMATION_STATE_CLIENT_PROPERTY, animationPlayer.animationState());
			if (EventQueue.isDispatchThread()) {
				repaint();
			} else {
				EventQueue.invokeLater(this::repaint);
			}
		}
	}

	@Override
	public void startAnimation() {
		if (getIcon() instanceof JSVGIcon) {
			animationPlayer.start();
		}
	}

	@Override
	public void stopAnimation() {
		if (getIcon() instanceof JSVGIcon) {
			animationPlayer.stop();
		}
	}

}
