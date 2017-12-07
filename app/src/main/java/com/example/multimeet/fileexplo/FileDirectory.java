package com.example.multimeet.fileexplo;

/**
 * Created by multimeet on 6/12/17.
 */

public class FileDirectory {

    private String name;
    private int fileOrDir;

    public FileDirectory(String name,int fileOrDir){
        this.name=name;
        this.fileOrDir=fileOrDir;
    }

    public String getName(){
        return name;
    }

    public int getFileOrDir(){
        return fileOrDir;
    }
}
