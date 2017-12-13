package com.example.fileex;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FileEx {


	private Map<String,String> directoryPathMap=new LinkedHashMap<>();
	private Map<String,String> filesPathMap=new LinkedHashMap<>();
	private static final String TAG=FileEx.class.getSimpleName();
	private List<String> completeList=new ArrayList<>();
	private List<String> tempList=new ArrayList<>();
	private static String currentDir="";
	private static String previousDir="";
	
	private File file=null;
	private File[] files;
	
	private String tempDir=null;

	private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	private static FileEx fileEx =null;
	
	/**
	 * Method to create FileEx Instance
	 * 
	 * @param dir
	 * @return FileEx
	 */
	
	public static FileEx newFileManager(String dir){
		
		fileEx =new FileEx(dir);
		currentDir=dir;
		previousDir=null;
		
		return fileEx;
	}
	
	private FileEx(String dir){
		try{
			file=new File(dir);
			currentDir=dir;
			previousDir=null;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Method to go up from current directory
	 *
	 * @return
	 */
	public boolean goUp(){
		if(previousDir==null)
			return false;
		currentDir=previousDir;
		try {
			tempDir = new File(previousDir).getParent();

		}catch (NullPointerException e){
			Log.d(TAG,"You are already at root directory.");
			return false;
		}

		if(isExists(tempDir)){
			previousDir=tempDir;
		}
		else
			previousDir=null;
		
		return true;
	}

	/**
	 * Method to set current directory
	 *
	 * @param dir
	 * @return
	 */
	public boolean setCurrentDir(String dir){
		if(isExists(dir)){
			currentDir=dir;
			try {
				previousDir = new File(dir).getParent();
			}catch (NullPointerException e){
				Log.e(TAG,"There is no parent for current directory.");
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
	public List<String> listFiles(){
		file=new File(currentDir);
		files=file.listFiles();
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
		}catch (NullPointerException e){
			return completeList;
		}
		for(String directory :directoryPathMap.keySet())
			completeList.add(directory);
		
		Collections.sort(completeList);
		
		for(String file : filesPathMap.keySet())
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

	public boolean isExists(String dir){

		if(dir==null)
			return false;
		try {
			return new File(dir).exists();
		}catch (NullPointerException e){
			return false;
		}

	}



	/**
	 * Method to get Current directory
	 *
	 * @return
	 */
	public String getCurrentDir(){
		return currentDir;
	}

	/**
	 * Method to get complete path of given file name
	 * @param file
	 * @return
	 */

	public String getFilePath(String file){
		
		try{
			return filesPathMap.get(file);
		}catch (Exception e) {
			return "File not found!!! ,check files in current diretory by listFiles()";
		}
		 
	}

	/**
	 * Method to open given dir
	 *
	 * @param dir
	 * @return
	 */
	public List<String> openDir(String dir){
		if(isExists(currentDir+"/"+dir)){
            previousDir=currentDir;
            currentDir=currentDir+"/"+dir;
			return listFiles();
		}else
			return null;
	}

	/**
	 * Method to check whether the given path is file or dir
	 *
	 * @param name
	 * @return
	 */
	public boolean isFile(String name){
		if(new File(currentDir+"/"+name).isFile())
			return true;
		return false;
	}

    /**
     * Method that return openable intent according to the file type
     *
     * @param file
     * @return
     */

	public Intent getOpenableIntent(String file){
		if(isExists(currentDir+"/"+file) && new File(currentDir+"/"+file)
                .isFile()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			Uri uri = Uri.fromFile(new File(currentDir+"/"+file));

			String type=MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap
					.getFileExtensionFromUrl(uri.toString()));
			intent.setDataAndType(uri,type);

			return intent;
		}
		else
			return null;
	}

	public String getInfo(String file){
	    if(isExists(currentDir+"/"+file)){
	        return simpleDateFormat.format(new File(currentDir+"/"+file)
					.lastModified());
        }
        return null;
    }


	public String getFileSize(String file){
	    double size=0;
	    StringBuilder unit=new StringBuilder("");
	    if(isExists(currentDir+"/"+file)){
	        size=((double)new File(currentDir+"/"+file).length());
            unit.append("B");
            if(size>1024) {
                size/=1024;
                unit.delete(0,unit.length());
                unit.append("KB");
            }

            if(size > 1024){
                size/=1024;
                unit.delete(0,unit.length());
                unit.append("MB");

            }

            if(size > 1024){
                size/=1024;
                unit.delete(0,unit.length());
                unit.append("GB");

            }

        }
		Log.e("SIZE",""+size);
		return String.format("%.2f", size)+" "+unit;
    }
}

