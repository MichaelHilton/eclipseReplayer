package edu.oregonstate.cope.eclipse.astinference.recorder;

import edu.oregonstate.cope.clientRecorder.ClientRecorder;
import edu.oregonstate.cope.clientRecorder.RecorderFacade;
import edu.oregonstate.cope.clientRecorder.StorageManager;

public class ASTRecorderFacade extends RecorderFacade {

	public ASTRecorderFacade(StorageManager manager, String IDE) {
		super(manager, IDE);
	}
	
	@Override
	protected ClientRecorder instantiateRecorder() {
		return new ASTJSONRecorder();
	}
}
