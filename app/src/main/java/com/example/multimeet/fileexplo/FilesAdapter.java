package com.example.multimeet.fileexplo;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fileex.FileEx;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by multimeet on 6/12/17.
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    List<FileDirectory> fileDirectories=new ArrayList<>();
    FileEx fileEx;
    public FilesAdapter(List<FileDirectory> fileDirectories, FileEx fileEx){
        this.fileDirectories=fileDirectories;
        this.fileEx = fileEx;
    }
    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.file_directory_view,parent,false);
        return new FileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, final int position) {
        holder.name.setText(fileDirectories.get(position).getName());
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileDirectories.get(position).getFileOrDir()== Constants.DIR){
                    fileEx.openDir(fileDirectories.get(position).getName());
                    fileDirectories.clear();
                    for(String s: fileEx.listFiles()){
                        if(fileEx.isFile(s))
                            fileDirectories.add(new FileDirectory(s,Constants.FILE));
                        else
                            fileDirectories.add(new FileDirectory(s,Constants.DIR));
                    }
                    notifyDataSetChanged();
                }

            }
        });
        if(fileDirectories.get(position).getFileOrDir()== Constants.DIR){
            holder.icon.setImageResource(R.drawable.folder);
        }else
            holder.icon.setImageResource(R.drawable.file);

    }

    @Override
    public int getItemCount() {
        return fileDirectories.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        ImageView icon;
        ConstraintLayout constraintLayout;
        public FileViewHolder(View v){
            super(v);
            constraintLayout=(ConstraintLayout) v;
            name=v.findViewById(R.id.name);
            icon=v.findViewById(R.id.icon);
        }
    }
}
