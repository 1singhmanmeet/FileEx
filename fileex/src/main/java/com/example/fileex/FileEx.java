package com.example.filelibrary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.filelibrary.model.FileDirectory;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

public class FileEx {


	private Map<String, String> directoryPathMap = new LinkedHashMap<>();
	private Map<String, String> filesPathMap = new LinkedHashMap<>();
	private static final String TAG = FileEx.class.getSimpleName();
	private List<String> completeList = new ArrayList<>();
	private List<String> tempList = new ArrayList<>();
	private static String currentDir = "";
	private static String previousDir = "";
	private static String defaultDir = "";
	private File file = null;
	private File[] files;
	public static final int INTERNAL=7;
	public static final int EXTERNAL=8;
	private static final int DIRECTORY_CREATED = 11;
	private static final int DIRECTORY_ALREADY_EXISTS = 12;
	private static final int DIRECTORY_ERROR = 13;
	private static List<String> fileList=new ArrayList<>();
	public static final String SD_CARD = "sdCard";
	public static final int COPY=11;
	public static final int MOVE=13;
	public static final String EXTERNAL_SD_CARD = "externalSdCard";
	private static final String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";
	private String tempDir = null;
	static Context context;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static List<FileDirectory> searchResultList=new ArrayList<>();
	private static FileEx fileEx = null;

	/**
	 * Method to create FileEx Instance
	 *
	 * @param dir
	 * @return FileEx
	 */

	public static synchronized FileEx newFileManager(String dir, Context c) {

		if (fileEx == null) {
			fileEx = new FileEx(dir);
			currentDir = dir;
			previousDir = null;
			context = c;
		}
		return fileEx;
	}

	private FileEx(String dir) {
		try {
			file = new File(dir);
			currentDir = dir;
			defaultDir = dir;
			previousDir = null;

		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, File> getAllStorageLocations() {
		Map<String, File> storageLocations = new HashMap<>(10);
		File storage = Environment.getExternalStorageDirectory();
		storageLocations.put("Internal Storage",storage);
		storage=storage.getParentFile().getParentFile();
		int i=1;
		for (File media:storage.listFiles()){
			if(!media.getName().equals("self") && !media.getName().equals("emulated")){
				storageLocations.put("External Storage "+i,media);
				i++;
			}
		}
		return storageLocations;
	}
	static String[] extensions=new String[]{"jpg","png","mp4","mp3","gif","xlsx","docx"};

	public static boolean isValidFile(String ext){
		for(String extension:extensions){
			if(extension.equals(ext))
				return true;
		}
		return false;
	}

	void searchUtil(File dir,List<FileDirectory> resultList,String query){
		for(File fileItem:dir.listFiles()){
			if(fileItem.isDirectory()) {
				if(FileEx.containsIgnoreCase(fileItem.getName(),query))
					searchResultList.add(new FileDirectory(fileItem.getName(), FileDirectory.FILE,
							getAbsoluteFileSize(fileItem.getAbsolutePath()), simpleDateFormat.format(file.lastModified()),
							fileItem.getAbsolutePath()));

				searchUtil(fileItem,resultList,query);
			}

			else if(FileEx.containsIgnoreCase(fileItem.getName(),query)){

				searchResultList.add(new FileDirectory(fileItem.getName(), FileDirectory.FILE,
						getAbsoluteFileSize(fileItem.getAbsolutePath()), simpleDateFormat.format(file.lastModified()),
						fileItem.getAbsolutePath()));
			}
		}
	}

	void searchUtilDate(File dir, List<FileDirectory> resultList, Date date){
		try {

			for (File fileItem : dir.listFiles()) {
				if (fileItem.isDirectory()) {
					searchUtilDate(fileItem, resultList, date);
				} else {
					Date tmp=simpleDateFormat.parse(simpleDateFormat.format(fileItem.lastModified()));
					String name=fileItem.getName();
					String ext=MimeTypeMap.getFileExtensionFromUrl(name);

					if(date.compareTo(tmp)<=0 && isValidFile(ext)) {
						searchResultList.add(new FileDirectory(fileItem.getName(), FileDirectory.FILE,
								getAbsoluteFileSize(fileItem.getAbsolutePath()),
								simpleDateFormat.format(fileItem.lastModified()),
								fileItem.getAbsolutePath()));
					}
				}
			}

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void delete(String completePath){
		try {
			File f = new File(completePath);
			File[] files = f.listFiles();
			if (files != null) {
				for (File file : files) {
					delete(file.toString());
				}
			}
			f.delete();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public static boolean containsIgnoreCase(String src, String what) {
		final int length = what.length();
		if (length == 0)
			return true; // Empty string is contained

		final char firstLo = Character.toLowerCase(what.charAt(0));
		final char firstUp = Character.toUpperCase(what.charAt(0));

		for (int i = src.length() - length; i >= 0; i--) {
			// Quick check before calling the more expensive regionMatches() method:
			final char ch = src.charAt(i);
			if (ch != firstLo && ch != firstUp)
				continue;

			if (src.regionMatches(true, i, what, 0, length))
				return true;
		}

		return false;
	}

	public Flowable<List<FileDirectory>> findwithDate(final String directory,final Date date){
		return Flowable.create(new FlowableOnSubscribe<List<FileDirectory>>() {
			@Override
			public void subscribe(FlowableEmitter<List<FileDirectory>> e) throws Exception {
				File file=new File(directory);
				searchResultList.clear();
				searchUtilDate(file,searchResultList,date);
				e.onNext(searchResultList);
				e.onComplete();
			}
		},BackpressureStrategy.BUFFER);
	}

	public Flowable<List<FileDirectory>> find(final String directory,final String query){

		return Flowable.create(new FlowableOnSubscribe<List<FileDirectory>>() {

			@Override
			public void subscribe(FlowableEmitter<List<FileDirectory>> e) throws Exception {
				File file=new File(directory);
				searchResultList.clear();
				searchUtil(file,searchResultList,query);
				e.onNext(searchResultList);
				e.onComplete();
			}
		},BackpressureStrategy.BUFFER);
	}

	/**
	 * Method to go up from current directory
	 *
	 * @return
	 */

	public boolean goUp() {
		if (previousDir == null)
			return false;
		if (currentDir.equals(defaultDir))
			return false;
		currentDir = previousDir;

		try {
			tempDir = new File(previousDir).getParent();

		} catch (NullPointerException e) {
			Log.d(TAG, "You are already at root directory.");
			return false;
		}

		if (isExists(tempDir)) {
			previousDir = tempDir;
		} else
			previousDir = null;

		return true;
	}


	public boolean renameTo(String path,String newName){
		File parent=new File(path).getParentFile();
		File newFile=new File(parent.getAbsolutePath()+"/"+newName);
		return new File(path).renameTo(newFile);
	}

	public String getUsedRootSpace() {
		double total, used, free;
		total = Double.parseDouble(getTotalRootSpace().split(" ")[0]);
		free = Double.parseDouble(getFreeRootSpace().split(" ")[0]);
		if(free > total){
			free/=1000;
		}
		return String.format(Locale.US,"%.2f",(total - free)) + " GB";
	}

	/**
	 * Method get free space in Root directory set at object creation time
	 *
	 * @return
	 */

	public String getFreeRootSpace() {
		try {
			file = new File(defaultDir);
			double size = 0;
			size = file.getFreeSpace();
			StringBuilder unit = new StringBuilder("");
			if (isExists(defaultDir)) {
				unit.append("B");
				if (size > 1024) {
					size /= 1024;
					unit.delete(0, unit.length());
					unit.append("KB");
				}

				if (size > 1024) {
					size /= 1024;
					unit.delete(0, unit.length());
					unit.append("MB");

				}

				if (size > 1024) {
					size /= 1024;
					unit.delete(0, unit.length());
					unit.append("GB");

				}

			}

			return String.format("%.2f", size) + " " + unit;

		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Method get total space in Root directory set at object creation time
	 *
	 * @return String
	 */
	public String getTotalRootSpace() {
		try {
			file = new File(defaultDir);
			double size = 0;
			size = file.getTotalSpace();
			StringBuilder unit = new StringBuilder("");
			if (isExists(defaultDir)) {
				unit.append("B");
				if (size > 1024) {
					size /= 1024;
					unit.delete(0, unit.length());
					unit.append("KB");
				}

				if (size > 1024) {
					size /= 1024;
					unit.delete(0, unit.length());
					unit.append("MB");

				}

				if (size > 1024) {
					size /= 1024;
					unit.delete(0, unit.length());
					unit.append("GB");

				}

			}
			//Log.e("SIZE",""+size);
			return String.format("%.2f", size) + " " + unit;

		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * @param dir
	 * @return
	 */
	public boolean changeRootDirectory(String dir) {
		if (!isExists(dir))
			return false;
		defaultDir = dir;
		currentDir=dir;
		return true;
	}

	/**
	 * Method to set current directory
	 *
	 * @param dir
	 * @return
	 */

	public boolean setCurrentDir(String dir) {
		if (isExists(dir)) {
			currentDir = dir;

			try {
				previousDir = new File(dir).getParent();
			} catch (NullPointerException e) {
				Log.e(TAG, "There is no parent for current directory.");
				return false;
			}
			return true;
		}
		return false;
	}


	/**
	 * Method to list all contents in current directory
	 *
	 * @return
	 */
	public List<String> listFiles() {
		file = new File(currentDir);
		files = file.listFiles();
		directoryPathMap.clear();
		filesPathMap.clear();
		completeList.clear();
		tempList.clear();
		try {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					directoryPathMap.put(files[i].getName(), files[i].getAbsolutePath());
				} else {
					filesPathMap.put(files[i].getName(), files[i].getAbsolutePath());
				}
			}
		} catch (NullPointerException e) {
			return completeList;
		}

		for (String directory : directoryPathMap.keySet())
			completeList.add(directory);

		Collections.sort(completeList);

		for (String file : filesPathMap.keySet())
			tempList.add(file);

		Collections.sort(tempList);

		completeList.addAll(tempList);
		return completeList;
	}

	/**
	 * Method to check whether the given path exist or not
	 *
	 * @param dir
	 * @return
	 */

	public boolean isExists(String dir) {

		if (dir == null)
			return false;
		try {
			return new File(dir).exists();
		} catch (NullPointerException e) {
			return false;
		}

	}


	/**
	 * Method to get Current directory
	 *
	 * @return
	 */

	public String getCurrentDir() {
		return currentDir;
	}

	/**
	 * Method to get complete path of given file name
	 *
	 * @param file
	 * @return
	 */

	public String getFilePath(String file) {

		try {

			return filesPathMap.get(file)!=null
					?filesPathMap.get(file)
					:directoryPathMap.get(file);
		} catch (Exception e) {
			return "File not found!!! ,check files in current diretory by listFiles()";
		}

	}

	/**
	 * Method to run shell commands.
	 * @param command
	 * @return
	 */
	public String Executer(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		String response = output.toString();
		return response;

	}

	/**
	 * Method to open given dir
	 *
	 * @param dir
	 * @return
	 */
	public List<String> openDir(String dir) {
		if (isExists(currentDir + "/" + dir)) {
			previousDir = currentDir;
			currentDir = currentDir + "/" + dir;
			return listFiles();
		} else
			return null;
	}

	/**
	 * Method to check whether the given path is file or dir
	 *
	 * @param name
	 * @return
	 */

	public boolean isFile(String name) {
		if (new File(currentDir + "/" + name).isFile())
			return true;
		return false;
	}

	/**
	 * Method that return openable intent according to the file type
	 *
	 * @param file
	 * @return
	 */

	public Intent getOpenableIntent(String file) {
		return getAbsoluteOpenableIntent(currentDir+"/"+file);
	}

	public Intent getAbsoluteOpenableIntent(String file) {
		if (isExists(file) && new File(file)
				.isFile()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			Uri uri = Uri.fromFile(new File(file));

			String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap
					.getFileExtensionFromUrl(uri.toString()));
			intent.setDataAndType(uri, type);
			if (intent.resolveActivity(context.getPackageManager()) == null)
				return null;
			return intent;
		} else
			return null;
	}

	/**
	 * @param file
	 * @return
	 */
	public String getInfo(String file) {
		return getAbsoluteInfo(currentDir+"/"+file);
	}

	public String getAbsoluteInfo(String file) {
		if (isExists(file)) {

			return simpleDateFormat.format(new File(file)
					.lastModified());
		}
		return null;
	}

	/**
	 * @param file
	 * @return
	 */
	public String getFileSize(String file) {
		String completePath=currentDir + "/" + file;
		return getAbsoluteFileSize(completePath);
	}

	public String getAbsoluteFileSize(String file) {
		double size = 0;
		StringBuilder unit = new StringBuilder("");
		if (isExists(file)) {
			size = ((double) new File(file).length());
			unit.append("B");
			if (size > 1024) {
				size /= 1024;
				unit.delete(0, unit.length());
				unit.append("KB");
			}

			if (size > 1024) {
				size /= 1024;
				unit.delete(0, unit.length());
				unit.append("MB");

			}

			if (size > 1024) {
				size /= 1024;
				unit.delete(0, unit.length());
				unit.append("GB");

			}

		}
		return String.format("%.2f", size) + " " + unit;
	}

	public int createDirectory(String dir) {
		if (isFile(dir))
			return DIRECTORY_ERROR;
		try {
			file = new File(dir);
			if (!file.exists()) {
				file.mkdir();
				return DIRECTORY_CREATED;
			} else
				return DIRECTORY_ALREADY_EXISTS;
		} catch (Exception e) {
			return DIRECTORY_ERROR;
		}
	}

	/**
	 * Method to search files in specified directory
	 * @param dir
	 * @param text
	 * @return
	 */
	public List<String> find(File dir,String text){
		if(!dir.isDirectory())
			return null;
		for(File temp:file.listFiles()) {
			if (file.isDirectory()) {
				find(temp,text);
			}else if(temp.getName().contains(text)){
				fileList.add(temp.getName());
			}
		}
		return fileList;
	}

	private void fileUtility(final String sourcePath,final FileInputStream source, final FileOutputStream destination,
										   FlowableEmitter<Integer> e) throws IOException{

		DataInputStream fileInputStream=new DataInputStream
				(source);
		DataOutputStream fileOutputStream=new DataOutputStream
				(destination);
		byte[] buffer=new byte[2048];
		int i;
		long size=new File(sourcePath).length();
		size/=1024;
		long total=0;

		while((i=fileInputStream.read(buffer))!=-1){
			total+=i/1024;
			if(e!=null)
				e.onNext((int)((total/(double)size)*100));
			fileOutputStream.write(buffer,0,i);
			//Log.e(TAG,"Writing data");
		}

		fileInputStream.close();
		fileOutputStream.close();

	}


	public Flowable<Integer> copyOrMoveFile(final String source, final FileInputStream sourceStream ,
											final FileOutputStream destinationStream, final int selection) {
		try {
			final File f = new File(source);
			return Flowable.create(new FlowableOnSubscribe<Integer>() {
				@Override
				public void subscribe(FlowableEmitter<Integer> e) throws Exception {

					if (!f.isDirectory()) {
						fileUtility(f.getAbsolutePath(),sourceStream
								, destinationStream, e);
						if(selection==MOVE)
							f.delete();
						e.onComplete();
					} else {
						int n = f.listFiles().length, i = 0;
						for (File file : f.listFiles()) {
							fileUtility(file.getAbsolutePath(),sourceStream
									, destinationStream, e);
							i++;
							if(selection==MOVE)
								file.delete();
							e.onNext((int) ((float) i / n * 100));
						}
						e.onComplete();
					}
				}
			}, BackpressureStrategy.BUFFER);
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}


}
