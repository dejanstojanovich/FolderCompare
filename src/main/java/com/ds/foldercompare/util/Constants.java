/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ds.foldercompare.util;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 *
 * @author Dejan
 */
public class Constants {

    public static String FOLDER = "folder";
    public static String FOLDER_LEFT = "folder_left";
    public static String FOLDER_RIGHT = "folder_right";
    public static String PARENT_FOLDER = "parent_folder";
    public static String ROOT = "root";
    public static String LOCK = "lock";
    public static String SIDE = "side";
    public static String FILE_LIST = "file_list";
    public static SimpleDateFormat modifiedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String TIMEZONE = "Europe/Belgrade";
    public static String SEPARATOR = File.separator;

    public static String getDate(Long timestamp) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(TIMEZONE));
        c.setTimeInMillis(timestamp);
        return modifiedFormat.format(c.getTime());
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static long getFileSize(File file) {
        long fileSize = 0L;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File child : children) {
                fileSize += getFileSize(child);
            }
        } else {
            fileSize = file.length();
        }
        return fileSize;
    }

    public static ArrayList<File> listFiles(String parent) {
        ArrayList<File> files = new ArrayList<>();
        if (parent.equals(ROOT)) {
            FileSystem fs = FileSystems.getDefault();
            fs.getRootDirectories().forEach((Path path) -> {
                files.add(path.toFile());
            });
        } else {
            File folder = new File(parent);
            if (folder.exists() && folder.isDirectory() && folder.canRead()) {
                File[] fileList = folder.listFiles((file) -> file.isDirectory());
                if (fileList != null) {
                    files.addAll(Arrays.asList(fileList));
                }

                fileList = folder.listFiles((file) -> file.isFile());
                if (fileList != null) {
                    files.addAll(Arrays.asList(fileList));
                }
            }
        }
        return files;
    }

   
}
