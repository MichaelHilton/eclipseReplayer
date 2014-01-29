/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.json.simple.JSONObject;

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.oregonstate.cope.clientRecorder.JSONConstants;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ExternallyModifiedResourceOperation extends ResourceOperation {

	private boolean isDeleted;
	private String text;


	public ExternallyModifiedResourceOperation() {
		super();
	}

	public ExternallyModifiedResourceOperation(IResource externallyModifiedResource, boolean isDeleted) {
		super(externallyModifiedResource);
		this.isDeleted= isDeleted;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.RESOURCE_EXTERNALLY_MODIFIED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Externally modified resource";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(isDeleted);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		if (!Configuration.isOldFormat) {
			isDeleted= operationLexer.readBoolean();
		} else {
			isDeleted= false;
		}
	}

	@Override
	public void replay() throws CoreException {
		IResource resource= findResource();
		//EditorHelper.closeAllEditorsForResource(resourcePath);
		try {
			Files.write(Paths.get(resource.getLocation().makeAbsolute().toPortableString()), text.getBytes(), StandardOpenOption.WRITE);
		} catch (IOException e) {
		}
		resource.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
		//EditorHelper.openEditor(resourcePath);
	}
	
	@Override
	public void parse(JSONObject value) {
		resourcePath = (String) value.get(JSONConstants.JSON_ENTITY_ADDRESS);
		text = (String) value.get(JSONConstants.JSON_TEXT);
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append(resourcePath);
		sb.append(" was replaced with ");
		sb.append(text);
		return sb.toString();
	}

}
