package tests;

import static org.junit.Assert.*;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;

import edu.illinois.codingtracker.operations.textchanges.ConflictEditorTextChangeOperation;
import edu.oregonstate.cope.eclipse.astinference.ast.ASTOperationRecorder;
import edu.oregonstate.cope.eclipse.astinference.ast.inferencing.ASTOperationInferencer;
import edu.oregonstate.cope.eclipse.astinference.ast.inferencing.CoherentTextChange;
import edu.oregonstate.cope.eclipse.astinference.ast.inferencing.InferredAST;

public class ASTInferenceUnitTests {

	@Test
	public void test() {
		InferredAST iASTObj = new InferredAST();
		Document document = new Document("initialDoc");
		DocumentEvent event = new DocumentEvent(new Document("\npublic class testGoL {\n\n}\n"), 0, 20, "\npublic class testGoL {\n\n}\n");

		long timestamp = 1;
		CoherentTextChange oneTextChange = new CoherentTextChange(event, ConflictEditorTextChangeOperation.isReplaying, timestamp );
		ASTOperationInferencer astOperationInferencer = new ASTOperationInferencer(oneTextChange);
		ASTOperationRecorder astRecorder = ASTOperationRecorder.getInstance();
		astRecorder.inferViaGumTree(astOperationInferencer, iASTObj);
	}

}
