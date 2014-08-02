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
	public void testASTDeserializationBasicFile() throws Exception {
		List<UserOperation> userOperations = new OperationDeserializer(null).getUserOperations(astEventFileContents);
		assertASTDeserializationForBasicFile(userOperations);
	}

	public void assertASTDeserializationForBasicFile(List<UserOperation> userOperations) {
		int expectedEvents = 20;
		int expectedAstDeleted = 0;
		int expectedASTEvents = 15;
		int expectedAstChanged = 0;
		int expectedASTAdds = 15;

		performCheckofASTOps(userOperations, expectedEvents,
				expectedAstDeleted, expectedASTEvents, expectedASTAdds , expectedAstChanged);
	}

	private void performCheckofASTOps(List<UserOperation> userOperations,
			int expectedEvents, int expectedAstDeleted, int expectedASTEvents, int expectedASTAdds,
			int expectedAstChanged) {
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
		
		assertEquals(expectedEvents, totalEvents);
		assertEquals(expectedASTEvents, astEvents);
		assertEquals(expectedASTAdds, astAdded);
		assertEquals(expectedAstChanged, astChanged);
		assertEquals(expectedAstDeleted, astDeleted);
	}

	public void assertASTDeserializationForGoLFile(
			List<UserOperation> userOperations) {
		int expectedEvents = 853;
		int expectedASTEvents = 553;
		int expectedASTAdds = 552;
		int expectedAstChanged = 1;
		int expectedAstDeleted = 0;

		performCheckofASTOps(userOperations, expectedEvents,
				expectedAstDeleted, expectedASTEvents, expectedASTAdds , expectedAstChanged);
		
	}

	public void assertASTDeserializationForLyFile(
			List<UserOperation> userOperations) {
		int expectedEvents = 210;
		int expectedASTEvents = 107;
		int expectedASTAdds = 90;
		int expectedAstChanged = 3;
		int expectedAstDeleted = 14;

		performCheckofASTOps(userOperations, expectedEvents,
				expectedAstDeleted, expectedASTEvents, expectedASTAdds , expectedAstChanged);
		
	}

	public void assertASTDeserializationForMineFFile(
			List<UserOperation> userOperations) {
		int expectedEvents = 946;
		int expectedASTEvents = 489;
		int expectedASTAdds = 466;
		int expectedAstChanged = 0;
		int expectedAstDeleted = 23;

		performCheckofASTOps(userOperations, expectedEvents,
				expectedAstDeleted, expectedASTEvents, expectedASTAdds , expectedAstChanged);
		
	}
}
