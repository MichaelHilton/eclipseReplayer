package edu.oregonstate.cope.eclipse.astinference.recorder;

import org.json.simple.JSONObject;

import edu.oregonstate.cope.clientRecorder.ClientRecorder;
import edu.oregonstate.cope.clientRecorder.RecordException;

public class ASTJSONRecorder extends ClientRecorder {
	public void recordASTOperation(){
		try {
			changePersister.persist(buildASTJSON());
		} catch (RecordException e) {
			logger.error(this, e.getMessage(), e);
		}
	}

	private JSONObject buildASTJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}
