package com.example.multimeet.fileexplo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.fileex.FileEx;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv;
    FileEx fileEx;
    List<FileDirectory> list=new ArrayList<>();
    public static final int READ_WRITE=22;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv=findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case READ_WRITE:

                for(int i=0;i<grantResults.length;i++){
                    if(grantResults[i]!=PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(),"Permission denied",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                fileEx=FileEx.newFileManager(Environment.getExternalStorageDirectory().toString());

                for(String s: fileEx.listFiles()){
                    if(fileEx.isFile(s))
                        list.add(new FileDirectory(s,Constants.FILE));
                    else
                        list.add(new FileDirectory(s,Constants.DIR));
                }
                rv.setAdapter(new FilesAdapter(list,fileEx));
                Toast.makeText(getApplicationContext(),"Permission granted",Toast.LENGTH_SHORT).show();
        }
    }
    public void checkPermissions(){
        if(Build.VERSION.SDK_INT<23)
            return;
        if(ContextCompat.checkSelfPermission(this
                , Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this
                , Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this
                    ,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this
                            ,Manifest.permission.READ_EXTERNAL_STORAGE)){

            }
            else{
                ActivityCompat.requestPermissions(this
                        ,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ,Manifest.permission.READ_EXTERNAL_STORAGE},READ_WRITE);

            }

        }else {
            fileEx=FileEx.newFileManager(Environment.getExternalStorageDirectory().toString());

            for(String s: fileEx.listFiles()){
                if(fileEx.isFile(s))
                    list.add(new FileDirectory(s,Constants.FILE));
                else
                    list.add(new FileDirectory(s,Constants.DIR));
            }
            rv.setAdapter(new FilesAdapter(list,fileEx));


        }
    }
}
