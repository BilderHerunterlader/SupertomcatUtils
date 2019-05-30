package ch.supertomcat.supertomcatutils.gui.dialog;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Utility class for file or folder dialogs
 */
public final class FileDialogUtil {
	/**
	 * Constructor
	 */
	private FileDialogUtil() {
	}

	/**
	 * Show Folder Open Dialog
	 * 
	 * @deprecated Use {@link #showFolderOpenDialog} instead
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen folder or null if none chosen
	 */
	@Deprecated
	public static File showFolderDialog(Component owner, String dir, FileFilter filter) {
		return showFolderOpenDialog(owner, dir, filter);
	}

	/**
	 * Show Folder Open Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen folder or null if none chosen
	 */
	public static File showFolderOpenDialog(Component owner, String dir, FileFilter filter) {
		File files[] = showFileDialog(owner, dir, filter, JFileChooser.DIRECTORIES_ONLY, false, false);
		return getSingleSelectionResult(files);
	}

	/**
	 * Show Folder Open Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen folder or null if none chosen
	 */
	public static File showFolderOpenDialog(Component owner, File dir, FileFilter filter) {
		File files[] = showFileDialog(owner, dir, filter, JFileChooser.DIRECTORIES_ONLY, false, false);
		return getSingleSelectionResult(files);
	}

	/**
	 * Show Folder Save Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen file or null if none chosen
	 */
	public static File showFolderSaveDialog(Component owner, String dir, FileFilter filter) {
		File files[] = showFileDialog(owner, dir, filter, JFileChooser.DIRECTORIES_ONLY, true, false);
		return getSingleSelectionResult(files);
	}

	/**
	 * Show Folder Save Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen file or null if none chosen
	 */
	public static File showFolderSaveDialog(Component owner, File dir, FileFilter filter) {
		File files[] = showFileDialog(owner, dir, filter, JFileChooser.DIRECTORIES_ONLY, true, false);
		return getSingleSelectionResult(files);
	}

	/**
	 * Show Multi Folder Open Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen folder or null if none chosen
	 */
	public static File[] showMultiFolderOpenDialog(Component owner, String dir, FileFilter filter) {
		return showFileDialog(owner, dir, filter, JFileChooser.DIRECTORIES_ONLY, false, true);
	}

	/**
	 * Show Multi Folder Open Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen folder or null if none chosen
	 */
	public static File[] showMultiFolderOpenDialog(Component owner, File dir, FileFilter filter) {
		return showFileDialog(owner, dir, filter, JFileChooser.DIRECTORIES_ONLY, false, true);
	}

	/**
	 * Show Multi Folder Save Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen file or null if none chosen
	 */
	public static File[] showMultiFolderSaveDialog(Component owner, String dir, FileFilter filter) {
		return showFileDialog(owner, dir, filter, JFileChooser.DIRECTORIES_ONLY, true, true);
	}

	/**
	 * Show Multi Folder Save Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen file or null if none chosen
	 */
	public static File[] showMultiFolderSaveDialog(Component owner, File dir, FileFilter filter) {
		return showFileDialog(owner, dir, filter, JFileChooser.DIRECTORIES_ONLY, true, true);
	}

	/**
	 * Show File Open Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen file or null if none chosen
	 */
	public static File showFileOpenDialog(Component owner, String dir, FileFilter filter) {
		File files[] = showFileDialog(owner, dir, filter, JFileChooser.FILES_ONLY, false, false);
		return getSingleSelectionResult(files);
	}

	/**
	 * Show File Open Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen file or null if none chosen
	 */
	public static File showFileOpenDialog(Component owner, File dir, FileFilter filter) {
		File files[] = showFileDialog(owner, dir, filter, JFileChooser.FILES_ONLY, false, false);
		return getSingleSelectionResult(files);
	}

	/**
	 * Show File Save Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen file or null if none chosen
	 */
	public static File showFileSaveDialog(Component owner, String dir, FileFilter filter) {
		File files[] = showFileDialog(owner, dir, filter, JFileChooser.FILES_ONLY, false, false);
		return getSingleSelectionResult(files);
	}

	/**
	 * Show File Save Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen file or null if none chosen
	 */
	public static File showFileSaveDialog(Component owner, File dir, FileFilter filter) {
		File files[] = showFileDialog(owner, dir, filter, JFileChooser.FILES_ONLY, false, false);
		return getSingleSelectionResult(files);
	}

	/**
	 * Show Multi File Open Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen files array or null if none chosen
	 */
	public static File[] showMultiFileOpenDialog(Component owner, String dir, FileFilter filter) {
		return showFileDialog(owner, dir, filter, JFileChooser.FILES_ONLY, false, true);
	}

	/**
	 * Show Multi File Open Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen files array or null if none chosen
	 */
	public static File[] showMultiFileOpenDialog(Component owner, File dir, FileFilter filter) {
		return showFileDialog(owner, dir, filter, JFileChooser.FILES_ONLY, false, true);
	}

	/**
	 * Show Multi File Save Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen files array or null if none chosen
	 */
	public static File[] showMultiFileSaveDialog(Component owner, String dir, FileFilter filter) {
		return showFileDialog(owner, dir, filter, JFileChooser.FILES_ONLY, true, true);
	}

	/**
	 * Show Multi File Save Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @return Chosen files array or null if none chosen
	 */
	public static File[] showMultiFileSaveDialog(Component owner, File dir, FileFilter filter) {
		return showFileDialog(owner, dir, filter, JFileChooser.FILES_ONLY, true, true);
	}

	/**
	 * Show File Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @param fileSelectionMode File Selection Mode
	 * @param save True if save, false it open
	 * @param multi True if multi select, false otherwise
	 * @return Chosen files array or null if none chosen
	 */
	public static File[] showFileDialog(Component owner, String dir, FileFilter filter, int fileSelectionMode, boolean save, boolean multi) {
		File startDir = null;
		if (dir != null && !dir.isEmpty()) {
			startDir = new File(dir);
		}
		return showFileDialog(owner, startDir, filter, fileSelectionMode, save, multi);
	}

	/**
	 * Show File Dialog
	 * 
	 * @param owner Owner or null
	 * @param dir Directory or null
	 * @param filter Filter or null
	 * @param fileSelectionMode File Selection Mode
	 * @param save True if save, false it open
	 * @param multi True if multi select, false otherwise
	 * @return Chosen files array or null if none chosen
	 */
	public static File[] showFileDialog(Component owner, File dir, FileFilter filter, int fileSelectionMode, boolean save, boolean multi) {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(fileSelectionMode);
		if (multi) {
			fc.setMultiSelectionEnabled(true);
		}
		if (filter != null) {
			fc.setFileFilter(filter);
		}
		if (dir != null) {
			fc.setCurrentDirectory(dir);
		}
		int retval;
		if (save) {
			retval = fc.showSaveDialog(owner);
		} else {
			retval = fc.showOpenDialog(owner);
		}
		if (retval == JFileChooser.APPROVE_OPTION) {
			if (multi) {
				return fc.getSelectedFiles();
			} else {
				return new File[] { fc.getSelectedFile() };
			}
		} else {
			return null;
		}
	}

	/**
	 * Get single selection result from files array
	 * 
	 * @param files Files
	 * @return First file if array not empty, null otherwise
	 */
	public static File getSingleSelectionResult(File[] files) {
		if (files == null || files.length == 0) {
			return null;
		}
		return files[0];
	}
}
