/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.starts;

import org.json.simple.JSONObject;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.oregonstate.cope.clientRecorder.JSONConstants;

/**
 * This operation is no longer recorded.
 * 
 * @author Stas Negara
 * 
 */
public class StartedRefactoringOperation extends UserOperation {

	private String refactoring_ID;

	public StartedRefactoringOperation() {
		super();
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.REFACTORING_STARTED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Started refactoring";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		//Nothing to populate here
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		//Nothing to initialize		
	}

	@Override
	public void replay() {
		//isReplayedRefactoring= true;
	}
	
	@Override
	public void parse(JSONObject value) {
		super.parse(value);
		
		refactoring_ID = (String) value.get(JSONConstants.JSON_REFACTORING_ID);
	}
	
	@Override
	public String toString() {
		return super.toString() + "\n" + "refactoringID: " + refactoring_ID;
	}
}
