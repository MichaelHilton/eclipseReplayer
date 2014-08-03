package tests;

import static org.junit.Assert.*;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import edu.illinois.codingtracker.operations.textchanges.ConflictEditorTextChangeOperation;
import edu.oregonstate.cope.eclipse.astinference.ast.ASTOperationRecorder;
import edu.oregonstate.cope.eclipse.astinference.ast.inferencing.ASTOperationInferencer;
import edu.oregonstate.cope.eclipse.astinference.ast.inferencing.CoherentTextChange;
import edu.oregonstate.cope.eclipse.astinference.ast.inferencing.InferredAST;

public class ASTInferenceUnitTests {

	@Test
	public void testAddInt() {
		String preTestText ="";
		String postTestText = "int i = 0;";
		InferredAST iASTObj = new InferredAST();
		JSONArray returnedASTArray = textChangeTest(preTestText, postTestText,iASTObj);
		JSONObject firstArrayElement = (JSONObject) returnedASTArray.get(0);
		//{"parentID":7,"Action":"INS","TypeLabel":"FieldDeclaration","Node":"","Position":25,"Id":6}
		//System.out.println(firstArrayElement.get("A"));
		assertEquals(firstArrayElement.get("Action").toString(),"INS");
	}
	
	@Test
	public void testDeleteStatement() {
		String preTestText ="int i = 0;";
		String postTestText = "";
		InferredAST iASTObj = new InferredAST();
		JSONArray returnedASTArray = textChangeTest(preTestText, postTestText,iASTObj);
		JSONObject firstArrayElement = (JSONObject) returnedASTArray.get(0);
		//{"parentID":7,"Action":"INS","TypeLabel":"FieldDeclaration","Node":"","Position":25,"Id":6}
		//System.out.println(firstArrayElement.get("A"));
		assertEquals(firstArrayElement.get("Action").toString(),"DEL");
	}

	private JSONArray textChangeTest(String preTestText, String postTestText, InferredAST iASTObj) {
		Document document = new Document("initialDoc");
		DocumentEvent event = new DocumentEvent(new Document("\npublic class testGoL {\n "+preTestText+" \n}\n"), 0, 20, "\npublic class testGoL {\n "+postTestText+" \n}\n");

		long timestamp = 1;
		CoherentTextChange oneTextChange = new CoherentTextChange(event, ConflictEditorTextChangeOperation.isReplaying, timestamp );
		ASTOperationInferencer astOperationInferencer = new ASTOperationInferencer(oneTextChange);
		ASTOperationRecorder astRecorder = ASTOperationRecorder.getInstance();
		JSONArray jArr = astRecorder.inferViaGumTree(astOperationInferencer, iASTObj);
		return jArr;
	}

}
