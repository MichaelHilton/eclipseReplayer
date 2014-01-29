/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.compare.helpers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.illinois.codingtracker.helpers.ResourceHelper;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Extracted this class from CodeChangeTracker
 * 
 */
@SuppressWarnings("restriction")
public class EditorHelper {

	public static IEditorPart getActiveEditor() {
		return JavaPlugin.getActivePage().getActiveEditor();
	}

	public static void closeAllEditors() {
		JavaPlugin.getActivePage().closeAllEditors(false);
	}

	public static void closeEditorSynchronously(IEditorPart editorPart) {
		// This closes the given editor synchronously.
		boolean success = editorPart.getSite().getPage().closeEditor(editorPart, false);
		if (!success) {
			throw new RuntimeException("Could not close editor: " + editorPart);
		}
	}

	public static Set<ITextEditor> getExistingEditors(String resourcePath) throws PartInitException {
		Set<ITextEditor> existingResourceEditors = new HashSet<ITextEditor>();

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IEditorReference[] editorReferences = activeWindow.getActivePage().getEditorReferences();

		for (IEditorReference editorReference : editorReferences) {
			IEditorInput editorInput = editorReference.getEditorInput();
			if (editorInput instanceof FileEditorInput && (ResourceHelper.getPortableResourcePath(((FileEditorInput) editorInput).getFile()).startsWith(resourcePath))) {
				existingResourceEditors.add((ITextEditor) editorReference.getEditor(true));
			}
		}

		return existingResourceEditors;
	}

	public static void closeAllEditorsForResource(String resourcePath) throws PartInitException {
		for (ITextEditor resourceEditor : getExistingEditors(resourcePath)) {
			closeEditorSynchronously(resourceEditor);
		}
	}

	public static ITextEditor createEditor(String filePath) throws JavaModelException, PartInitException {
		IFile file = (IFile) ResourceHelper.findWorkspaceMember(filePath);
		ITextEditor newTextEditor = (ITextEditor) JavaUI.openInEditor(JavaCore.createCompilationUnitFrom(file), false, false);

		return newTextEditor;
	}

	public static void activateEditor(IEditorPart editor) {
		JavaPlugin.getActivePage().activate(editor);
	}

	public static ITextEditor getAndOpenEditor(String resourcePath) throws PartInitException, JavaModelException {
		ITextEditor editor = getExistingEditorForResource(resourcePath);
		if (editor == null)
			editor = createEditor(resourcePath);

		activateEditor(editor);
		return editor;
	}

	private static ITextEditor getExistingEditorForResource(String resourcePath) throws PartInitException {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IEditorReference[] editorReferences = activeWindow.getActivePage().getEditorReferences();

		for (IEditorReference editorReference : editorReferences) {
			String fileLocation = ((FileEditorInput) editorReference.getEditorInput()).getFile().getFullPath().toString();

			if (fileLocation.equals(resourcePath)) {
				return (ITextEditor) editorReference.getEditor(true);
			}
		}

		return null;
	}

	public static IDocument getDocumentForEditor(String resourcePath) throws PartInitException, JavaModelException {
		ITextEditor editor = getAndOpenEditor(resourcePath);
		return editor.getDocumentProvider().getDocument(editor.getEditorInput());
	}

	public static IDocument getDocumentForEditor(ITextEditor editor) {
		return editor.getDocumentProvider().getDocument(editor.getEditorInput());
	}

	public static ISourceViewer getViewerForEditor(String fileName) throws PartInitException, JavaModelException {
		return (ISourceViewer) getAndOpenEditor(fileName).getAdapter(ITextOperationTarget.class);
	}

}
