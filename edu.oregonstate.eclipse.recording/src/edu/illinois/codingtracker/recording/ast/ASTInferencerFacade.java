package edu.illinois.codingtracker.recording.ast;

import org.eclipse.jface.text.DocumentEvent;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingtracker.operations.files.EditedUnsychronizedFileOperation;
import edu.illinois.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.NewFileOperation;
import edu.illinois.codingtracker.operations.resources.CopiedResourceOperation;
import edu.illinois.codingtracker.operations.resources.CreatedResourceOperation;
import edu.illinois.codingtracker.operations.resources.DeletedResourceOperation;
import edu.illinois.codingtracker.operations.resources.MovedResourceOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;

public class ASTInferencerFacade {
	private ASTOperationRecorder astRecorder;

	private static class Instance {
		public static final ASTInferencerFacade _instance= new ASTInferencerFacade();
	}

	public static ASTInferencerFacade getInstance() {
		return Instance._instance;
	}

	private ASTInferencerFacade() {
		astRecorder= ASTOperationRecorder.getInstance();
	}

	public void beforeDocumentChanged(UserOperation userOperation) {
		if (userOperation instanceof TextChangeOperation) {
			TextChangeOperation op= (TextChangeOperation)userOperation;
			DocumentEvent documentEvent= op.getDocumentEvent(op.getEditedText());
			astRecorder.beforeDocumentChange(documentEvent, op.getEditedFilePath());
		}
		else
			System.err.println("beforeDocumenChange did not handle: " + userOperation.getClass());
	}

	public void flushCurrentTextChanges(UserOperation userOperation) {
		if (!(userOperation instanceof ASTOperation) && !(userOperation instanceof TextChangeOperation) &&
				!(userOperation instanceof EditedFileOperation) && !(userOperation instanceof EditedUnsychronizedFileOperation) &&
				!(userOperation instanceof NewFileOperation)) {
			//Saving a file does not force flushing since the corresponding AST might be broken.
			astRecorder.flushCurrentTextChanges(!(userOperation instanceof SavedFileOperation));
		}
	}

	public void handleResourceOperation(UserOperation currentUserOperation) {
		if (currentUserOperation instanceof CreatedResourceOperation) {

		}
		else if (currentUserOperation instanceof DeletedResourceOperation) {

		}
		else if (currentUserOperation instanceof CopiedResourceOperation) {

		}
		else if (currentUserOperation instanceof MovedResourceOperation) {

		}
	}
}