package com.example.filelibrary.model;

/**
 * Created by multimeet on 6/12/17.
 */

public class FileDirectory {

    public static final int DIR=11;
    public static final int FILE=22;
    private String name;
    private int fileOrDir;
    private String path;

    public void setName(String name) {
        this.name = name;
    }

    public void setFileOrDir(int fileOrDir) {
        this.fileOrDir = fileOrDir;
    }

    public String getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String size;
    private String date;

    public FileDirectory(String name,int fileOrDir,String size,String date,String path){
        this.name=name;
        this.fileOrDir=fileOrDir;
        this.date=date;
        this.path=path;
        this.size=size;
    }

    public String getName(){
        return name;
    }

    public int getFileOrDir(){
        return fileOrDir;
    }
}
