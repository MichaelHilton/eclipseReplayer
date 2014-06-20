/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.oregonstate.cope.eclipse.astinference.recorder;


import java.io.File;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;
import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperationDescriptor;
import edu.illinois.codingtracker.operations.ast.CompositeNodeDescriptor;
import edu.illinois.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.oregonstate.cope.clientRecorder.StorageManager;
import edu.oregonstate.cope.eclipse.astinference.ast.ASTOperationRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTInferenceTextRecorder {

	private final static ASTOperationRecorder astRecorder= ASTOperationRecorder.getInstance();

	private final static SafeRecorder safeRecorder= new SafeRecorder("codingtracker/codechanges_ast.txt");
	
	private static long lastTimestamp;
	
	private ASTInferenceTextRecorder() {
		ASTRecorderFacade astRecorderFacade = new ASTRecorderFacade(new StorageManager() {
			private final static String filePath = "copeRecorder";
			
			private File getStorage(){
				return new File(filePath);
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
		
		ASTJSONRecorder recorder = (ASTJSONRecorder) astRecorderFacade.getClientRecorder();
	}


	/**
	 * When isSimulatedRecord is true, this method flushes the text changes, if necessary, and
	 * updates the timestamp.
	 * 
	 * @param userOperation
	 */
	public static void record(UserOperation userOperation) {
		long operationTime= userOperation.getTime();
		//Before any user operation, except text change operations, flush the accumulated AST changes.
		if (!(userOperation instanceof TextChangeOperation)) {
			//TODO: Some part of the below code are duplicated in TextRecorder.
			//Saving a file does not force flushing since the corresponding AST might be broken.
			astRecorder.flushCurrentTextChanges(!(userOperation instanceof SavedFileOperation));
		}
		lastTimestamp= operationTime;
		performRecording(userOperation);
	}

	public static void recordASTOperation(ASTOperationDescriptor operationDescriptor, CompositeNodeDescriptor affectedNodeDescriptor) {
		ASTOperation astOperation= new ASTOperation(operationDescriptor, affectedNodeDescriptor, getASTOperationTimestamp());
		performRecording(astOperation);
	}

	public static void recordASTFileOperation(String astFilePath) {
		performRecording(new ASTFileOperation(astFilePath, getASTOperationTimestamp()));
	}

	private static void performRecording(UserOperation userOperation) {
		safeRecorder.record(userOperation.generateSerializationText());
	}

	private static long getASTOperationTimestamp() {
		if (Configuration.isInReplayMode) {
			return lastTimestamp;
		} else {
			return System.currentTimeMillis();
		}
	}
}
