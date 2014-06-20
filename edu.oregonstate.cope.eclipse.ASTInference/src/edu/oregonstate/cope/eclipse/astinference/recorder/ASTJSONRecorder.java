package edu.oregonstate.cope.eclipse.astinference.recorder;

import org.json.simple.JSONObject;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.oregonstate.cope.clientRecorder.ClientRecorder;
import edu.oregonstate.cope.clientRecorder.RecordException;

public class ASTJSONRecorder extends ClientRecorder {
	
	private enum ASTEvents{
		astOperation
	}
	
	public void recordASTOperation(ASTOperation astOperation){
		try {
			changePersister.persist(buildASTJSON(astOperation));
		} catch (RecordException e) {
			logger.error(this, e.getMessage(), e);
		}
	}

	private JSONObject buildASTJSON(ASTOperation astOperation) {
		String opString = astOperation.generateSerializationText().toString();

		JSONObject obj = buildCommonJSONObj(ASTEvents.astOperation);
		obj.put("content", opString);
		
		return obj;
	}
}
