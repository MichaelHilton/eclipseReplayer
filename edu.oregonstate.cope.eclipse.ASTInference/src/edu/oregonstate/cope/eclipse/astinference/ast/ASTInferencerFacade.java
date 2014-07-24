package edu.oregonstate.cope.eclipse.astinference.ast;

import edu.illinois.codingtracker.helpers.ResourceHelper;
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
import edu.oregonstate.cope.eclipse.astinference.ast.inferencing.InferredAST;
import edu.oregonstate.cope.eclipse.astinference.recorder.ASTInferenceTextRecorder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.DocumentEvent;

import java.io.File;

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
            InferredAST iASTObj = new InferredAST();
			beforeDocumentChanged(op, op.getEditedText(), op.getEditedFilePath(),iASTObj);
		}
		else
			System.err.println("beforeDocumenChange did not handle: " + userOperation.getClass());
	}
	
	public void beforeDocumentChanged(UserOperation userOperation, String fileContentsBeforeChange, String filePath,InferredAST iASTObj) {
		if (userOperation instanceof TextChangeOperation) {
			TextChangeOperation op= (TextChangeOperation)userOperation;
			DocumentEvent documentEvent= op.getDocumentEvent(fileContentsBeforeChange);
			astRecorder.beforeDocumentChange(documentEvent, filePath,iASTObj);
		}
		else
			System.err.println("beforeDocumenChange did not handle: " + userOperation.getClass());
	}

	public void flushCurrentTextChanges(UserOperation userOperation, InferredAST iASTObj) {
		if (!(userOperation instanceof ASTOperation) && !(userOperation instanceof TextChangeOperation) &&
				!(userOperation instanceof EditedFileOperation) && !(userOperation instanceof EditedUnsychronizedFileOperation) &&
				!(userOperation instanceof NewFileOperation)) {
			//Saving a file does not force flushing since the corresponding AST might be broken.
			astRecorder.flushCurrentTextChanges(!(userOperation instanceof SavedFileOperation),iASTObj);
		}
	}


    public void handleResourceOperation(UserOperation currentUserOperation) {
        if (currentUserOperation instanceof CreatedResourceOperation) {
            astRecorder.recordASTOperationForCreatedResource((CreatedResourceOperation)currentUserOperation);
        }
        
        else if (currentUserOperation instanceof DeletedResourceOperation) {
            DeletedResourceOperation op= (DeletedResourceOperation)currentUserOperation;
            IResource resource= ResourceHelper.findWorkspaceMember(op.getResourcePath());
            astRecorder.recordASTOperationForDeletedResource(resource, true);
        }
        
        else if (currentUserOperation instanceof CopiedResourceOperation) {
			System.err.println("handleResourceOperation did not handle copy");
		}
        
		else if (currentUserOperation instanceof MovedResourceOperation) {
			System.err.println("handleResourceOperation did not handle move");
		}
    }


/*	public void handleResourceOperation(UserOperation currentUserOperation) {
		if (currentUserOperation instanceof CreatedResourceOperation) {
			CreatedResourceOperation op= (CreatedResourceOperation)currentUserOperation;
			IResource resource= ResourceHelper.findWorkspaceMember(op.getResourcePath());

			//see BreakableResourceOperation.initializeFrom() for the hardcoded boolean
			astRecorder.recordASTOperationForCreatedResource(resource, true);
		}
		else if (currentUserOperation instanceof DeletedResourceOperation) {
			DeletedResourceOperation op= (DeletedResourceOperation)currentUserOperation;
			IResource resource= ResourceHelper.findWorkspaceMember(op.getResourcePath());


			astRecorder.recordASTOperationForDeletedResource(resource, true);
		}
		else if (currentUserOperation instanceof CopiedResourceOperation) {
			System.err.println("handleResourceOperation did not handle copy");
		}
		else if (currentUserOperation instanceof MovedResourceOperation) {
			System.err.println("handleResourceOperation did not handle move");
		}
	}*/
	
	public void setRecordingDirectory(File recordingDirectory){
		ASTInferenceTextRecorder.getInstance().setRecordingDirectory(recordingDirectory);
	}
}
