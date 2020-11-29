package com.luffy.mulmedia.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
    public static String getString(InputStream instream) {
        StringBuilder stringBuffer = new StringBuilder();
        byte[] line = new byte[1024];
        int length = 0;
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(instream);
            while ((length = bufferedInputStream.read(line)) != -1) {
                Log.d("TAG", "read length" + length);
                String cur = new String(line, "utf-8");
                Log.d("TAG", "read length" + cur);
                stringBuffer.append(cur);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuffer.toString();
    }


    public static String getString1(InputStream instream) {
        StringBuilder stringBuffer = new StringBuilder();
        String line = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(instream));
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuffer.toString();
    }


    public static String getStringFromAssets(Context context, String filename) {
        String data = "";
        try {
            InputStream inputStream = context.getAssets().open(filename);
            data = getString1(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static String getString(String filename) {
        String data = "";
        try {
            File file = new File(filename);
            if (file == null || !file.exists() || file.isDirectory() || !file.canRead()) return "";
            InputStream inputStream = new FileInputStream(new File(filename));
            data = getString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

}
