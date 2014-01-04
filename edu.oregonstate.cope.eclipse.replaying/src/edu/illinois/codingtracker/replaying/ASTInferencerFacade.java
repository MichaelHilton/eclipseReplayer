package edu.illinois.codingtracker.replaying;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;

public class ASTInferencerFacade {
	private ASTOperationRecorder astRecorder;

	private static class Instance {
		public static final ASTInferencerFacade _instance = new ASTInferencerFacade();
	}

	public static ASTInferencerFacade getInstance() {
		return Instance._instance;
	}

	private ASTInferencerFacade() {
		astRecorder = ASTOperationRecorder.getInstance();
	}

	public void beforeDocumentChanged(UserOperation currentUserOperation) {
		if (!(currentUserOperation instanceof TextChangeOperation))
			return;
	}
}
