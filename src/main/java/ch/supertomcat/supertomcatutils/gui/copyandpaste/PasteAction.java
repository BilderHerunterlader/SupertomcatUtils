package ch.supertomcat.supertomcatutils.gui.copyandpaste;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Paste Action
 */
public class PasteAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private JTextComponent txtComp = null;

	/**
	 * Constructor
	 * 
	 * @param txtComp Text Component
	 */
	public PasteAction(JTextComponent txtComp) {
		super(Localization.getString("Paste"));
		this.txtComp = txtComp;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		txtComp.paste();
	}
}
