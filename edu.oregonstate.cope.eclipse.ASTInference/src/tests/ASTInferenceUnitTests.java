package tests;

import static org.junit.Assert.*;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import static org.mockito.Mockito.*;
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
		String preTestText ="\npublic class testGoL {\n int i = 10;\n \n}\n" ;
		String postTestText = "\npublic class testGoL {\n  \n}\n" ;
		
		ASTOperationRecorder astRecorder = ASTOperationRecorder.getInstance();
		JSONArray calculatedDiff = astRecorder.calcASTDiffOnTwoStrings(preTestText,postTestText);
		
//		String preTestText ="int i = 0;";
//		String postTestText = "";
//		InferredAST iASTObj = new InferredAST();
//		
//		//mock creation
//		ASTOperationInferencer mockedInferencer = mock(ASTOperationInferencer.class);
//		//using mock object
//		//when(i.next()).thenReturn("Hello").thenReturn("World");
//		ASTNode mockedBeforeAST = mock(ASTNode.class);
//		ASTNode mockedAfterAST = mock(ASTNode.class);
//		when(mockedBeforeAST.getLength()).thenReturn(10);
//		//when(mockedBeforeAST.getAST()).thenReturn("\npublic class testGoL {\n "+preTestText+" \n}\n");
////		when(mockedBeforeAST.getLength()).thenReturn(10);
//		
//		when(mockedInferencer.getOldRootNode()).thenReturn(mockedBeforeAST);
////		when(mockedInferencer.getOldRootNode().getLength()).thenReturn(10);
//		when(mockedInferencer.getNewRootNode()).thenReturn(mockedAfterAST);
//		
//		ASTOperationRecorder astRecorder = ASTOperationRecorder.getInstance();
////		
//		JSONArray returnedASTArray = astRecorder.inferViaGumTree(mockedInferencer, iASTObj);
//		//return jArr;
//		 
		
//		
//		Document document = new Document("initialDoc");
//		DocumentEvent event = new DocumentEvent(new Document("\npublic class testGoL {\n int i = 10;\n \n}\n"), 0, 1, "");
//		//DocumentEvent event = new DocumentEvent(new Document("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), 0, 2, "abcdefghijklmnopqrstuvwxyz");
//
//		long timestamp = 1;
//		CoherentTextChange oneTextChange = new CoherentTextChange(event, ConflictEditorTextChangeOperation.isReplaying, timestamp );
//		ASTOperationInferencer astOperationInferencer = new ASTOperationInferencer(oneTextChange);
//		
//
//		ASTOperationRecorder astRecorder = ASTOperationRecorder.getInstance();
//		
//		JSONArray returnedASTArray = astRecorder.inferViaGumTree(astOperationInferencer, iASTObj);
//		//return jArr;
//		
//		
////		JSONArray returnedASTArray = textChangeTest(preTestText, postTestText,iASTObj);
//		JSONObject firstArrayElement = (JSONObject) returnedASTArray.get(0);
//		//{"parentID":7,"Action":"INS","TypeLabel":"FieldDeclaration","Node":"","Position":25,"Id":6}
////		System.out.println(firstArrayElement.get("A"));
//		
//		
//		
//		assertEquals(firstArrayElement.get("Action").toString(),"DEL");
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




