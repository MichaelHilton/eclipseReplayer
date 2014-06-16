/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.starts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.json.simple.JSONObject;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.oregonstate.cope.clientRecorder.Events;
import edu.oregonstate.cope.clientRecorder.JSONConstants;

/**
 * 
 * @author Caius Brindescu
 * 
 */
public class LaunchedApplicationOperation extends UserOperation {
	
	private Map launchAttributes;
	private String launchConfig;
	private String launchMode;
	private String launchName;
	private String launchFile;
	
	public LaunchedApplicationOperation(String mode) {
		if (mode.equals(Events.debugLaunch.toString()))
			launchMode = ILaunchManager.DEBUG_MODE;
		if (mode.equals(Events.normalLaunch.toString()))
			launchMode = ILaunchManager.RUN_MODE;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.APPLICATION_LAUNCHED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Launched application";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
	}
	
	@Override
	public void parse(JSONObject value) {
		launchAttributes = (Map) value.get(JSONConstants.JSON_LAUNCH_ATTRIBUTES);
		launchConfig = (String) value.get(JSONConstants.JSON_LAUNCH_CONFIGURATION);
		launchName = (String) value.get(JSONConstants.JSON_LAUNCH_NAME);
		launchFile = (String) value.get(JSONConstants.JSON_LAUNCH_FILE);
	}

	@Override
	public void replay() throws Exception {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		writeLaunchFile(launchName, launchFile);
		ILaunchConfiguration launchConfiguration = launchManager.getLaunchConfiguration(launchConfig);
		ILaunchConfigurationWorkingCopy launchConfigWorkingCopy = launchConfiguration.getWorkingCopy();
		launchConfigWorkingCopy.setAttributes(launchAttributes);
		ILaunchConfiguration newLaunchConfig = launchConfigWorkingCopy.doSave();
		ILaunch launch = newLaunchConfig.launch(launchMode, new NullProgressMonitor(), true);
	}

	private void writeLaunchFile(String launchName, String launchFileContents) {
		try {
			Path launchedFolderPath = Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocation().makeAbsolute().toPortableString(), 
					".metadata/.plugins/org.eclipse.debug.core/.launches/");
			Path filePath = launchedFolderPath.resolve(launchName + ".launch");
			boolean success = new File(launchedFolderPath.toString()).mkdir();
			if (!success)
				System.out.println("Couldn't create .launches folder.");
			filePath.toFile().createNewFile();
			Files.write(filePath, 
					launchFileContents.getBytes(), 
					StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	@Override
	public String toString() {
		return "";
	}

}
