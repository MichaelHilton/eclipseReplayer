/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.illinois.codingtracker.operations.files.ClosedFileOperation;
import edu.illinois.codingtracker.operations.files.CompareWithSnapshot;
import edu.illinois.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingtracker.operations.files.SaveProjectSnapshot;
import edu.illinois.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.NewFileOperation;
import edu.illinois.codingtracker.operations.junit.TestSessionStartedOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.resources.CreatedResourceOperation;
import edu.illinois.codingtracker.operations.resources.DeletedResourceOperation;
import edu.illinois.codingtracker.operations.resources.ExternallyModifiedResourceOperation;
import edu.illinois.codingtracker.operations.starts.LaunchedApplicationOperation;
import edu.illinois.codingtracker.operations.starts.StartedRefactoringOperation;
import edu.illinois.codingtracker.operations.textchanges.PerformedTextChangeOperation;

//TODO: Decide on where this class should be and how it should be used
/**
 * 
 * @author Stas Negara
 * 
 */
public class OperationDeserializer {

	private String eventFilePath = "";
	
	public String getEventFilePath() {
		return eventFilePath;
	}

	public void setEventFilePath(String eventFilePath) {
		this.eventFilePath = eventFilePath;
	}
	
	public OperationDeserializer(String eventFilePath) {
		this.eventFilePath = eventFilePath;
	}
	
	private static final String OPERATIONS_SEPARATOR = "\n\\$@\\$";
	 
	public List<UserOperation> getUserOperations(String operationsRecord) {
	  List<UserOperation> userOperations= new LinkedList<UserOperation>();
	  ArrayList<JSONObject> JSONObjs = new ArrayList<JSONObject>();
	  String[] operationsList = operationsRecord.split(OPERATIONS_SEPARATOR);
	  JSONParser parser = new JSONParser();
      JSONObject value = null;
      String strValue = null;
      
      for(String operation : operationsList) {
	      try {
	          if(operation.isEmpty()) {
	        	  continue;
	          }
	          value = (JSONObject) parser.parse(operation);
	          JSONObjs.add(value);
	      } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
	      }
      }
     
      preprocessJSONArray(JSONObjs);
      
      for(JSONObject jsonObj : JSONObjs){
    	  String eventName = (String) jsonObj.get("eventType");
          System.out.println(eventName);
          addUserOperation(userOperations, jsonObj, eventName);
      }
      return userOperations;
	}

	
	private void swapClasspathWithProject(ArrayList<JSONObject> jsonObjs){
		for(int j = 0; j < jsonObjs.size(); j++) {
			JSONObject jsonObj = jsonObjs.get(j);
			//CHECK IF CLASSPATH IS BEFORE PROJECT
			if(jsonObj.get("eventType").toString().equals("resourceAdded")){
				if(jsonObj.get("entityAddress").toString().contains(".classpath")){
					//check if next entry is .project
					int currLoc = jsonObjs.indexOf(jsonObj);
					JSONObject nextObj = jsonObjs.get(currLoc+1);
					if(nextObj.get("eventType").toString().equals("resourceAdded") && 
							nextObj.get("entityAddress").toString().contains(".project")){
						jsonObjs.set(currLoc, nextObj);
 						jsonObjs.set(currLoc+1, jsonObj);
					}
				}
			}
		}
	}
	
	private void removeSpuriousEventsBeforeRecourceCreated(ArrayList<JSONObject> jsonObjs){
		for(int j = 0; j < jsonObjs.size(); j++) {
			JSONObject jsonObj = jsonObjs.get(j);
			//CHECK FOR FILE EVENTS BEFORE RESOURCECREATED
			if(jsonObj.get("eventType").toString().equals("resourceAdded")){
				String currResource = jsonObj.get("entityAddress").toString();
				int currLoc = jsonObjs.indexOf(jsonObj);
				
				for(int i = currLoc-1; i >= 0; i--){
					if(jsonObjs.get(i).containsKey("entityAddress")){
						if(jsonObjs.get(i).get("entityAddress").equals(currResource)){
							jsonObjs.remove(i);
							j--;
						}
					}
				}
			}
		}
	}
	
	private void preprocessJSONArray(ArrayList<JSONObject> jsonObjs) {
		swapClasspathWithProject(jsonObjs);
		removeSpuriousEventsBeforeRecourceCreated(jsonObjs);
	}

	private void addUserOperation(List<UserOperation> userOperations, JSONObject value, String eventName) {
		UserOperation userOperation= createEmptyUserOperation(eventName);
		if(userOperation != null){ 
			userOperation.parse(value);
			userOperation.setEventFilePath(this.getEventFilePath());
			userOperations.add(userOperation);
		}
	}
	
	

	private static UserOperation createEmptyUserOperation(String operationSymbol) {
		UserOperation userOperation = null;
		if(operationSymbol.equals("textChange")){
			userOperation= new PerformedTextChangeOperation();
		}else if(operationSymbol.equals("fileOpen")){
			userOperation= new EditedFileOperation();
		}else if(operationSymbol.equals("snapshot")){
			userOperation= new SaveProjectSnapshot();
			userOperation= new CompareWithSnapshot();
		}else if(operationSymbol.equals("fileClose")){
			userOperation= new ClosedFileOperation();
		}else if(operationSymbol.equals("testRun")){
			userOperation= new TestSessionStartedOperation();
		}else if(operationSymbol.equals("normalLaunch")){
			userOperation= new LaunchedApplicationOperation(operationSymbol);
		}else if(operationSymbol.equals("debugLaunch")){
			userOperation= new LaunchedApplicationOperation(operationSymbol);
		}else if(operationSymbol.equals("launchEnd")){
			//userOperation= new LaunchedApplicationOperation();
		}else if(operationSymbol.equals("fileSave")){
			userOperation= new SavedFileOperation();
		}else if(operationSymbol.equals("refactoringLaunch")){
			userOperation= new StartedRefactoringOperation();
		}else if(operationSymbol.equals("refactoringEnd")){
			userOperation= new FinishedRefactoringOperation();
		}else if(operationSymbol.equals("resourceRemoved")){
			userOperation= new DeletedResourceOperation();
		}else if(operationSymbol.equals("resourceAdded")){
			userOperation= new CreatedResourceOperation();
		}else if(operationSymbol.equals("refresh")){
			userOperation= new ExternallyModifiedResourceOperation();
		}
		//refactoringLaunch
		
		//userOperation= new SavedFileOperation();
		//normalLaunch
		return userOperation;
	}
	
	
	
//	public static List<UserOperation> getUserOperations(String operationsRecord) {
//		List<UserOperation> userOperations= new LinkedList<UserOperation>();
//		OperationLexer operationLexer= new OperationLexer(operationsRecord);
//		while (operationLexer.hasNextOperation()) {
//			operationLexer.startNewOperation();
//			UserOperation userOperation= createEmptyUserOperation(operationLexer.getCurrentOperationSymbol());
//			userOperation.deserialize(operationLexer);
//			userOperations.add(userOperation);
//		}
//		return userOperations;
//	}
//
//	private static UserOperation createEmptyUserOperation(char operationSymbol) {
//		UserOperation userOperation;
//		switch (operationSymbol) {
//			case OperationSymbols.ECLIPSE_STARTED_SYMBOL:
//				userOperation= new StartedEclipseOperation();
//				break;
//			case OperationSymbols.REFACTORING_FINISHED_SYMBOL:
//				userOperation= new FinishedRefactoringOperation();
//				break;
//			case OperationSymbols.NEW_REFACTORING_STARTED_SYMBOL:
//				userOperation= new NewStartedRefactoringOperation();
//				break;
//			case OperationSymbols.REFACTORING_STARTED_SYMBOL:
//				userOperation= new StartedRefactoringOperation();
//				break;
//			case OperationSymbols.REFACTORING_PERFORMED_SYMBOL:
//				userOperation= new PerformedRefactoringOperation();
//				break;
//			case OperationSymbols.REFACTORING_UNDONE_SYMBOL:
//				userOperation= new UndoneRefactoringOperation();
//				break;
//			case OperationSymbols.REFACTORING_REDONE_SYMBOL:
//				userOperation= new RedoneRefactoringOperation();
//				break;
//			case OperationSymbols.CONFLICT_EDITOR_OPENED_SYMBOL:
//				userOperation= new OpenedConflictEditorOperation();
//				break;
//			case OperationSymbols.CONFLICT_EDITOR_CLOSED_SYMBOL:
//				userOperation= new ClosedConflictEditorOperation();
//				break;
//			case OperationSymbols.CONFLICT_EDITOR_SAVED_SYMBOL:
//				userOperation= new SavedConflictEditorOperation();
//				break;
//			case OperationSymbols.RESOURCE_CREATED_SYMBOL:
//				userOperation= new CreatedResourceOperation();
//				break;
//			case OperationSymbols.RESOURCE_MOVED_SYMBOL:
//				userOperation= new MovedResourceOperation();
//				break;
//			case OperationSymbols.RESOURCE_COPIED_SYMBOL:
//				userOperation= new CopiedResourceOperation();
//				break;
//			case OperationSymbols.RESOURCE_DELETED_SYMBOL:
//				userOperation= new DeletedResourceOperation();
//				break;
//			case OperationSymbols.FILE_CLOSED_SYMBOL:
//				userOperation= new ClosedFileOperation();
//				break;
//			case OperationSymbols.FILE_SAVED_SYMBOL:
//				userOperation= new SavedFileOperation();
//				break;
//			case OperationSymbols.RESOURCE_EXTERNALLY_MODIFIED_SYMBOL:
//				userOperation= new ExternallyModifiedResourceOperation();
//				break;
//			case OperationSymbols.FILE_UPDATED_SYMBOL:
//				userOperation= new UpdatedFileOperation();
//				break;
//			case OperationSymbols.FILE_SVN_INITIALLY_COMMITTED_SYMBOL:
//				userOperation= new SVNInitiallyCommittedFileOperation();
//				break;
//			case OperationSymbols.FILE_CVS_INITIALLY_COMMITTED_SYMBOL:
//				userOperation= new CVSInitiallyCommittedFileOperation();
//				break;
//			case OperationSymbols.FILE_SVN_COMMITTED_SYMBOL:
//				userOperation= new SVNCommittedFileOperation();
//				break;
//			case OperationSymbols.FILE_CVS_COMMITTED_SYMBOL:
//				userOperation= new CVSCommittedFileOperation();
//				break;
//			case OperationSymbols.FILE_REFACTORED_SAVED_SYMBOL:
//				userOperation= new RefactoredSavedFileOperation();
//				break;
//			case OperationSymbols.FILE_NEW_SYMBOL:
//				userOperation= new NewFileOperation();
//				break;
//			case OperationSymbols.FILE_REFRESHED_SYMBOL:
//				userOperation= new RefreshedFileOperation();
//				break;
//			case OperationSymbols.FILE_EDITED_SYMBOL:
//				userOperation= new EditedFileOperation();
//				break;
//			case OperationSymbols.FILE_EDITED_UNSYNCHRONIZED_SYMBOL:
//				userOperation= new EditedUnsychronizedFileOperation();
//				break;
//			case OperationSymbols.TEXT_CHANGE_PERFORMED_SYMBOL:
//				userOperation= new PerformedTextChangeOperation();
//				break;
//			case OperationSymbols.TEXT_CHANGE_UNDONE_SYMBOL:
//				userOperation= new UndoneTextChangeOperation();
//				break;
//			case OperationSymbols.TEXT_CHANGE_REDONE_SYMBOL:
//				userOperation= new RedoneTextChangeOperation();
//				break;
//			case OperationSymbols.CONFLICT_EDITOR_TEXT_CHANGE_PERFORMED_SYMBOL:
//				userOperation= new PerformedConflictEditorTextChangeOperation();
//				break;
//			case OperationSymbols.CONFLICT_EDITOR_TEXT_CHANGE_UNDONE_SYMBOL:
//				userOperation= new UndoneConflictEditorTextChangeOperation();
//				break;
//			case OperationSymbols.CONFLICT_EDITOR_TEXT_CHANGE_REDONE_SYMBOL:
//				userOperation= new RedoneConflictEditorTextChangeOperation();
//				break;
//			case OperationSymbols.TEST_SESSION_LAUNCHED_SYMBOL:
//				userOperation= new TestSessionLaunchedOperation();
//				break;
//			case OperationSymbols.TEST_SESSION_STARTED_SYMBOL:
//				userOperation= new TestSessionStartedOperation();
//				break;
//			case OperationSymbols.TEST_SESSION_FINISHED_SYMBOL:
//				userOperation= new TestSessionFinishedOperation();
//				break;
//			case OperationSymbols.TEST_CASE_STARTED_SYMBOL:
//				userOperation= new TestCaseStartedOperation();
//				break;
//			case OperationSymbols.TEST_CASE_FINISHED_SYMBOL:
//				userOperation= new TestCaseFinishedOperation();
//				break;
//			case OperationSymbols.APPLICATION_LAUNCHED_SYMBOL:
//				userOperation= new LaunchedApplicationOperation();
//				break;
//			case OperationSymbols.WORKSPACE_OPTIONS_CHANGED_SYMBOL:
//				userOperation= new WorkspaceOptionsChangedOperation();
//				break;
//			case OperationSymbols.PROJECT_OPTIONS_CHANGED_SYMBOL:
//				userOperation= new ProjectOptionsChangedOperation();
//				break;
//			case OperationSymbols.REFERENCING_PROJECTS_CHANGED_SYMBOL:
//				userOperation= new ReferencingProjectsChangedOperation();
//				break;
//			case OperationSymbols.AST_OPERATION_SYMBOL:
//				userOperation= new ASTOperation();
//				break;
//			case OperationSymbols.AST_FILE_OPERATION_SYMBOL:
//				userOperation= new ASTFileOperation();
//				break;
//			case OperationSymbols.INFERRED_REFACTORING_OPERATION_SYMBOL:
//				userOperation= new InferredRefactoringOperation();
//				break;
//			case OperationSymbols.INFERRED_UNKNOWN_TRANSFORMATION_OPERATION_SYMBOL:
//				userOperation= new InferredUnknownTransformationOperation();
//				break;
//			default:
//				throw new RuntimeException("Unsupported operation symbol: " + operationSymbol);
//		}
//		return userOperation;
//	}

}
