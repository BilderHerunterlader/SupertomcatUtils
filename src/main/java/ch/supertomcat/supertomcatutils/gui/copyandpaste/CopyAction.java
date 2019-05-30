package ch.supertomcat.supertomcatutils.gui.copyandpaste;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

import ch.supertomcat.supertomcatutils.gui.Localization;

/**
 * Copy Action
 */
public class CopyAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private JTextComponent txtComp = null;

	/**
	 * Constructor
	 * 
	 * @param txtComp Text Component
	 */
	public CopyAction(JTextComponent txtComp) {
		super(Localization.getString("Copy"));
		this.txtComp = txtComp;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		txtComp.copy();
	}
}
