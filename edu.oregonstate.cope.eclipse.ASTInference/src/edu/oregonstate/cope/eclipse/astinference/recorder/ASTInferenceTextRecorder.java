/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.oregonstate.cope.eclipse.astinference.recorder;


import java.io.File;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperationDescriptor;
import edu.illinois.codingtracker.operations.ast.CompositeNodeDescriptor;
import edu.illinois.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.oregonstate.cope.clientRecorder.RecorderFacade;
import edu.oregonstate.cope.clientRecorder.StorageManager;
import edu.oregonstate.cope.eclipse.astinference.ast.ASTOperationRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTInferenceTextRecorder {
	
	private static class Instance{
		public static final ASTInferenceTextRecorder _instance = new ASTInferenceTextRecorder();
	}

	private final static ASTOperationRecorder astRecorder= ASTOperationRecorder.getInstance();

	private static long lastTimestamp;

	private ASTJSONRecorder recorder;
	
	private ASTInferenceTextRecorder() {
		setRecordingDirectory(new File("copeRecording"));
	}

	public static ASTInferenceTextRecorder getInstance(){
		return Instance._instance;
	}

	public void setRecordingDirectory(final File recordingDirectory){
		ASTRecorderFacade astRecorderFacade = new ASTRecorderFacade(new StorageManager() {
			
			private File getStorage(){
				return recordingDirectory;
			}
			
			@Override
			public File getVersionedLocalStorage() {
				return getStorage();
			}
			
			@Override
			public File getVersionedBundleStorage() {
				return getStorage();
			}
			
			@Override
			public File getLocalStorage() {
				return getStorage();
			}
			
			@Override
			public File getBundleStorage() {
				return getStorage();
			}
			
		}, "Eclipse");
		
		recorder = (ASTJSONRecorder) astRecorderFacade.getClientRecorder();
	}
	
	/**
	 * When isSimulatedRecord is true, this method flushes the text changes, if necessary, and
	 * updates the timestamp.
	 * 
	 * @param userOperation
	 */
	public void record(UserOperation userOperation) {
		long operationTime= userOperation.getTime();
		//Before any user operation, except text change operations, flush the accumulated AST changes.
		if (!(userOperation instanceof TextChangeOperation)) {
			//TODO: Some part of the below code are duplicated in TextRecorder.
			//Saving a file does not force flushing since the corresponding AST might be broken.
			astRecorder.flushCurrentTextChanges(!(userOperation instanceof SavedFileOperation));
		}
		lastTimestamp= operationTime;
		
		if(userOperation instanceof TextChangeOperation){
			TextChangeOperation op = (TextChangeOperation) userOperation;
			recorder.recordTextChange(op.getNewText(), op.getOffset(), op.getLength(), "missing", "glued");
		}
		else
			System.err.println(userOperation.getClass().getName() + " not supported in inference recording");
	}

	public void recordASTOperation(ASTOperationDescriptor operationDescriptor, CompositeNodeDescriptor affectedNodeDescriptor) {
		ASTOperation astOperation= new ASTOperation(operationDescriptor, affectedNodeDescriptor, getASTOperationTimestamp());
		
		recorder.recordASTOperation(astOperation);
	}

	public void recordASTFileOperation(String astFilePath) {
		//performRecording(new ASTFileOperation(astFilePath, getASTOperationTimestamp()));
		
		//do not record this
	}

	private long getASTOperationTimestamp() {
		if (Configuration.isInReplayMode) {
			return lastTimestamp;
		} else {
			return System.currentTimeMillis();
		}
	}
}
