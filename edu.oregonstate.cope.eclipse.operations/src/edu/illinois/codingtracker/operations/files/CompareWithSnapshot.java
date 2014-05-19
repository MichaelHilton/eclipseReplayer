package edu.illinois.codingtracker.operations.files;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import difflib.*;
import edu.illinois.codingtracker.helpers.ResourceHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;


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
			String destDir = snapshotDir + File.separator + "extracted";
			String extractedDir = destDir;
			
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			String projectName = "p10";
			IProjectDescription newProjectDescription = workspace.newProjectDescription(projectName);
			IProject newProject = workspace.getRoot().getProject(projectName);
			newProject.create(newProjectDescription, null);
			newProject.open(null);

			unzip(snapshotPath, destDir);
			
			IOverwriteQuery overwriteQuery = new IOverwriteQuery(){
				public String queryOverwrite(String file){return ALL;}
			};
			ImportOperation importOperation = new ImportOperation(newProject.getFullPath(),new File(destDir),FileSystemStructureProvider.INSTANCE,overwriteQuery);
			importOperation.setCreateContainerStructure(false);
			importOperation.run(new NullProgressMonitor());
			
			
			
//			//ZipFile zipFile = new ZipFile(workspace.getRoot().getLocation() + "/" + projectName + ".zip");
//			IOverwriteQuery overwriteQuery = new IOverwriteQuery() {
//			    public String queryOverwrite(String file) { return ALL; }
//			};
//			ZipLeveledStructureProvider provider = new ZipLeveledStructureProvider(snapshotZip);
//			List<Object> fileSystemObjects = new ArrayList<Object>();
//			Enumeration<? extends ZipEntry> entries = snapshotZip.entries();
//			while (entries.hasMoreElements()) {
//			    fileSystemObjects.add((Object)entries.nextElement());
//			}
//			ImportOperation importOperation = new ImportOperation(newProject.getFullPath(), new ZipEntry(projectName), provider, overwriteQuery, fileSystemObjects);
//			importOperation.setCreateContainerStructure(false);
//			importOperation.run(new NullProgressMonitor());
//			
//			
			
			
//			unzip(snapshotPath, snapshotDir + File.separator + "extracted");
//			
//			importProject(new File(snapshotDir + File.separator + "extracted" + File.separator + "p10"), "p10");
//			
//			
//			IWorkspace workspace = ResourcesPlugin.getWorkspace();
//			Path currPath = (Path) workspace.getRoot().getLocation();
//			
			//ImportOperation importOperation = new ImportOperation(currPath,);

			
			
			//ImportOperation importOperation = new ImportOperation(newProject.getFullPath(), new ZipEntry(projectName), provider, overwriteQuery, fileSystemObjects);
//			importOperation.setCreateContainerStructure(false);
//			IProgressMonitor monitor = new NullProgressMonitor();
//			//importOperation.run(new NullProgressMonitor());
//			importOperation.run(monitor);

			
			
//			IWorkspace workspace = ResourcesPlugin.getWorkspace();
//			String projectName = "p10";
//			IProjectDescription newProjectDescription = workspace.newProjectDescription(projectName);
//			//IProject newProject = workspace.getRoot().getProject(projectName);
//			IProject newProject = workspace.getRoot().getProject(projectName);
//			newProject.create(newProjectDescription, null);
//			newProject.open(null);
//			//
//			ZipFile zipFile = new ZipFile(snapshotPath);
//			IOverwriteQuery overwriteQuery = new IOverwriteQuery() {
//			    public String queryOverwrite(String file) { return ALL; }
//			};
//			ZipLeveledStructureProvider provider = new ZipLeveledStructureProvider(zipFile);
//			List<Object> fileSystemObjects = new ArrayList<Object>();
//			Enumeration<? extends ZipEntry> entries = zipFile.entries();
//			while (entries.hasMoreElements()) {
//			    fileSystemObjects.add((Object)entries.nextElement());
//			}
//			ImportOperation importOperation = new ImportOperation(newProject.getFullPath(), new ZipEntry(projectName), provider, overwriteQuery, fileSystemObjects);
//			importOperation.setCreateContainerStructure(false);
//			IProgressMonitor monitor = new NullProgressMonitor();
//			//importOperation.run(new NullProgressMonitor());
//			importOperation.run(monitor);
			
			
			
			
			
			
			
//			 String zipFile = snapshotPath;
//		     String outputFolder = snapshotDir + File.separator + "extracted";
//		 
//		        System.out.println("Begin unzip "+ zipFile + " into "+outputFolder);
//		        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
//		        ZipEntry ze = zis.getNextEntry();
//		        while(ze!=null){
//		            String entryName = ze.getName();
//		            System.out.print("Extracting " + entryName + " -> " + outputFolder + File.separator +  entryName + "...");
//		            File f = new File(outputFolder + File.separator +  entryName);
//		            //create all folder needed to store in correct relative path.
//		            f.getParentFile().mkdirs();
//		            FileOutputStream fos = new FileOutputStream(f);
//		            int len;
//		            byte buffer[] = new byte[1024];
//		            while ((len = zis.read(buffer)) > 0) {
//		                fos.write(buffer, 0, len);
//		            }
//		            fos.close();   
//		            System.out.println("OK!");
//		            ze = zis.getNextEntry();
//		        }
//		        zis.closeEntry();
//		        zis.close();
//		 
//		        System.out.println( zipFile + " unzipped successfully");
//
//			
//			
//			ZipInputStream zis = new ZipInputStream(new FileInputStream(snapshotPath));
//	        ZipEntry ze = zis.getNextEntry();
//	        while(ze!=null){
//	            String entryName = ze.getName();
//	            System.out.println("Extracting " + entryName + " -> " + extractedDir + File.separator +  entryName + "...");
//	            File f = new File(snapshotPath + File.separator +  entryName);
//	            //create all folder needed to store in correct relative path.
//	            if(f.isDirectory() && !f.exists()){
//	            	f.mkdirs();
//	            }else{
//	              f.getParentFile().mkdirs();
//	              FileOutputStream fos = new FileOutputStream(f);
//	              int len;
//	              byte buffer[] = new byte[1024];
//	              while ((len = zis.read(buffer)) > 0) {
//	                fos.write(buffer, 0, len);
//	              }
//	              fos.close();   
//	            }
//	            System.out.println("OK!");
//	            ze = zis.getNextEntry();
//	        }
//	        zis.closeEntry();
//	        zis.close();
//			
			
//			// unzipping ... 
//			Enumeration<? extends ZipEntry> entries = snapshotZip.entries();
//			while (entries.hasMoreElements()) {
//			    ZipEntry entry = entries.nextElement();
//				File entryDestination = new File(extractedDir + File.separator + entry.getName());
//			    //System.out.println("Unzipping to " + entryDestination.getAbsolutePath());
//			    entryDestination.getParentFile().mkdirs();
//			    InputStream in = snapshotZip.getInputStream(entry);
//			    OutputStream out = new FileOutputStream(entryDestination);
//			    IOUtils.copy(in, out);
//			    IOUtils.closeQuietly(in);
//			    IOUtils.closeQuietly(out);
//			}
//			String projectName = snapshotZipFileName.split("-")[0];
//			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			
			//IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			
//			File projectDir = project.getLocation().toFile();
//			
//			extractedDir += File.separator + projectDir.getName();  
//			
//			// comparing two times due to limitations of comparator implementation 
//			System.out.println("Comparing " + projectDir.getAbsolutePath() + " and " + extractedDir);
//			new Comparator(projectDir.getAbsolutePath(), extractedDir);
//			System.out.println("Comparing " + extractedDir + " and " + projectDir.getAbsolutePath());
//			new Comparator(extractedDir, projectDir.getAbsolutePath());
//			deleteFolder(new File(extractedDir).getParentFile());

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
	
	private static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                System.out.println("is Dir? "+ze.isDirectory());
                if(ze.isDirectory()){
                	newFile.mkdirs();
                }else{
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                }
                fos.close();
                }
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }
	
	
	 private static void importProject(final File baseDirectory, final String projectName) throws CoreException {
			IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(
					new Path(baseDirectory.getAbsolutePath() + "/.project"));
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			project.create(description, null);
			project.open(null);
		}
}

