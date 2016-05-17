package com.campus.gomotion.util;

import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Author zhong.zhou
 * Date 5/16/16
 * Email qnarcup@gmail.com
 */
public class FileUtil {
    private static final String TAG = "FileUtil";
    private String fileName;

    public static void writeFile(String fileName,float f){
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        try{
            fileWriter = new FileWriter(fileName);
            printWriter = new PrintWriter(fileWriter);
            printWriter.println(f+"\n");
        }catch (IOException e){
            Log.d(TAG,e.getMessage());
        }finally {
            try{
                if(printWriter!=null){
                    printWriter.close();
                }
                if(fileWriter!=null){
                    fileWriter.close();
                }
            }catch (IOException e){
                Log.d(TAG,e.getMessage());
            }
        }
    }
}
