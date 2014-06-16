package edu.illinois.codingtracker.operations.files;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import difflib.*;
import edu.illinois.codingtracker.helpers.ResourceHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


public class CompareWithSnapshot extends FileOperation {

	private class Comparator {
		
		private String dir1 = "";
		private String dir2 = "";
		
		List<File> allFiles = new ArrayList<File>();
		
		public Comparator(String dir1, String dir2) {
			this.dir1 = dir1;
			this.dir2 = dir2;
			this.processFiles(dir1);
		}
		
		private String join(Collection s, String delimiter) {
		    StringBuffer buffer = new StringBuffer();
		    Iterator iter = s.iterator();
		    while (iter.hasNext()) {
		        buffer.append(iter.next());
		        if (iter.hasNext()) {
		            buffer.append(delimiter);
		        }
		    }
		    return buffer.toString();
		}

		
		private void processFiles(String dir1) {
			File rootDir = new File(dir1);
			if (rootDir.exists()) {
				traverseDirectories(rootDir);
			}
		}
		
		private void  traverseDirectories(File file) {
			// add all files and directories to list.
			allFiles.add(file);
			if (file.isDirectory()) {
				File[] fileList = file.listFiles();
				for (File fileHandle : fileList) {
					traverseDirectories(fileHandle);
				}
			} else {
				String file1Path = file.getPath();
				String file2Path = file.getPath().replaceAll(Pattern.quote(this.dir1), Matcher.quoteReplacement(this.dir2));
				File file1 = new File(file1Path);
				File file2 = new File(file2Path);
				if(!file2.isFile()) {
					System.out.println("File " + file2Path + " is absent in dir " + file2.getParentFile().getPath());
				} else {
					try {
						String[] original = FileUtils.readFileToString(file1).split("\n");
						String[] compared = FileUtils.readFileToString(file2).split("\n");
						Patch patch = DiffUtils.diff(Arrays.asList(original), Arrays.asList(compared));
						String unifiedDiff = this.join(DiffUtils.generateUnifiedDiff(FileUtils.readFileToString(file1), FileUtils.readFileToString(file2), null, patch, 0), "\n");
						if(!unifiedDiff.trim().isEmpty()) {							
							System.out.println("== Comparing " + file1Path + " with " + file2Path + " == ");
							System.out.println(unifiedDiff);
							System.out.println(" ");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Override
	protected char getOperationSymbol() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDescription() {
		return "Workspace snapshot";
	}
	
	@Override
	public void replay() throws Exception {
		File snapshotZipFile = new File(resourcePath);
		String snapshotZipFileName = snapshotZipFile.getName();
		File eventFile = new File(this.getEventFilePath());
		String snapshotDir = eventFile.getParentFile().getParentFile().getParentFile().getAbsolutePath();
		String snapshotPath = snapshotDir + File.separator + snapshotZipFileName;
		
		try {
			ZipFile snapshotZip = new ZipFile(snapshotPath);
			String extractedDir = snapshotDir + File.separator + "extracted";
			
			// unzipping ... 
			Enumeration<? extends ZipEntry> entries = snapshotZip.entries();
			while (entries.hasMoreElements()) {
			    ZipEntry entry = entries.nextElement();
				File entryDestination = new File(extractedDir + File.separator + entry.getName());
			    //System.out.println("Unzipping to " + entryDestination.getAbsolutePath());
			    entryDestination.getParentFile().mkdirs();
			    InputStream in = snapshotZip.getInputStream(entry);
			    OutputStream out = new FileOutputStream(entryDestination);
			    IOUtils.copy(in, out);
			    IOUtils.closeQuietly(in);
			    IOUtils.closeQuietly(out);
			}
			String projectName = snapshotZipFileName.split("-")[0];
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			
			File projectDir = project.getLocation().toFile();
			
			extractedDir += File.separator + projectDir.getName();  
			
			// comparing two times due to limitations of comparator implementation 
			System.out.println("Comparing " + projectDir.getAbsolutePath() + " and " + extractedDir);
			new Comparator(projectDir.getAbsolutePath(), extractedDir);
			System.out.println("Comparing " + extractedDir + " and " + projectDir.getAbsolutePath());
			new Comparator(extractedDir, projectDir.getAbsolutePath());
			deleteFolder(new File(extractedDir).getParentFile());

		} catch (FileNotFoundException e) {
			System.out.println("Cannot find snapshot file: " + e.getMessage());
		}
	}

	public void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
}
