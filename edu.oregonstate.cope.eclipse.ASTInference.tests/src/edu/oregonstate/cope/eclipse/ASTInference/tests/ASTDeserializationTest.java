package edu.oregonstate.cope.eclipse.ASTInference.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.ltk.core.refactoring.TextChange;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.oregonstate.cope.eclipse.astinference.recorder.ASTRecorderFacade;

public class ASTDeserializationTest {
	
	private static final String TEST_DATA_AST = "testData/serializedAST";
	private static String astEventFileContents;

	@BeforeClass
	public static void setup() throws IOException {
		Path astFilePath = Paths.get(TEST_DATA_AST);
		assertTrue(Files.exists(astFilePath));
		
		astEventFileContents = new String(Files.readAllBytes(astFilePath));
	}
	
	@Test
	public void testASTDeserialization() throws Exception {
		List<UserOperation> userOperations = new OperationDeserializer(null).getUserOperations(astEventFileContents);
		int totalEvents = 0;
		int astEvents = 0;
		int astAdded = 0;
		int astChanged = 0;
		int astDeleted = 0;
		
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof ASTOperation) {
				ASTOperation astOP = (ASTOperation) userOperation;
				astEvents++;
				
				if (astOP.isAdd()) {
					astAdded ++;
				}
				
				if (astOP.isChange()) {
					astChanged ++;
				}
				
				if (astOP.isDelete()) {
					astDeleted ++;
				}
			}
			
			totalEvents ++;
		}
		
		assertEquals(43, totalEvents);
		assertEquals(38, astEvents);
		assertEquals(20, astAdded);
		assertEquals(2, astChanged);
		assertEquals(16, astDeleted);
	}
}
