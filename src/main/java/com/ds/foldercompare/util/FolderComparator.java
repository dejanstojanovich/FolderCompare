/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ds.foldercompare.util;

import static com.ds.foldercompare.util.Constants.listFiles;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;

/**
 *
 * @author Dejan
 */
@Data
public class FolderComparator {

    File parentLeft;
    HashMap<String, File> leftList;
    File parentRight;
    HashMap<String, File> rightList;
    ArrayList<FileComparatorItem> result = new ArrayList<>();

    public FolderComparator(File leftFolder, File rightFolder) {
        parentLeft = leftFolder;
        leftList = mapFiles(parentLeft);
        parentRight = rightFolder;
        rightList = mapFiles(parentRight);
    }

    public FolderComparator(String leftFolder, String rightFolder) {
        parentLeft = new File(leftFolder);
        leftList = mapFiles(parentLeft);
        parentRight = new File(rightFolder);
        rightList = mapFiles(parentRight);
    }

    public boolean checkFolders() {
        boolean same = true;
        Map<Boolean, HashSet<File>> list = getCombinedPartitionedFiles();
        HashSet<File> folders = list.get(false);
        HashSet<File> files = list.get(true);
        TreeSet<String> folderNames = folders.stream()
                .map(p -> p.getName())
                .collect(Collectors.toCollection(TreeSet::new));
        folderNames.forEach((folderName) -> {
            result.add(new FileComparatorItem(folderName, false));
        });
        TreeSet<String> fileNames = files.stream()
                .map(p -> p.getName())
                .collect(Collectors.toCollection(TreeSet::new));
        fileNames.forEach((folderName) -> {
            result.add(new FileComparatorItem(folderName, true));
        });
        for (FileComparatorItem fileComparatorItem : result) {
            if (!fileComparatorItem.equal) {
                same = false;
                break;
            }
        }
        return same;
    }

    public Map<Boolean, HashSet<File>> getCombinedPartitionedFiles() {
        return Stream.concat(leftList.values().stream(), rightList.values().stream())
                .collect(Collectors.partitioningBy(file -> file.isFile(),
                        Collectors.toCollection(HashSet::new)));
    }

//    public HashSet<File> getFolders() {
//        return Stream.concat(leftList.values().stream(), rightList.values().stream())
//                .filter(file -> file.isDirectory())
//                .collect(Collectors.toCollection(HashSet::new));
//    }
//
//    public HashSet<File> getFiles() {
//        return Stream.concat(leftList.values().stream(), rightList.values().stream())
//                .filter(file -> file.isFile())
//                .collect(Collectors.toCollection(HashSet::new));
//    }
    public static HashMap<String, File> mapFiles(File folder) {
        HashMap<String, File> files = new HashMap<>();

        if (folder.exists() && folder.isDirectory() && folder.canRead()) {
            File[] fileList = folder.listFiles();
            files = (HashMap<String, File>) Arrays.asList(fileList).stream().collect(Collectors.toMap(file -> file.getName(), file -> file));

        }
        return files;
    }

    /**
     *
     */
    @Data
    public class FileComparatorItem {

        String filename;
        File leftFile;
        File rightFile;
        boolean equal = false;

        @Override
        public String toString() {
            return "{" + "filename=" + filename + ", leftFile=" + (leftFile == null ? "" : leftFile.getName())
                    + ", rightFile=" + (rightFile == null ? "" : rightFile.getName()) + ", equal=" + equal + '}';
        }

        public FileComparatorItem(String file, boolean handleFiles) {
            filename = file;
            leftFile = leftList.get(file);
            rightFile = rightList.get(file);
            equal = checkIfEqual(leftFile, rightFile, handleFiles);
        }

        private boolean checkIfEqual(File left, File right, boolean handleFiles) {
            if (left == null || right == null) {
                return false;
            }
            if (handleFiles) {
                if (left.isDirectory() || right.isDirectory()) {
                    return false;
                }
                if (left.length() != right.length()) {
                    return false;
                } else {
                    try {
                        return Files.equal(left, right);
                    } catch (IOException ex) {
                        Logger.getLogger(FolderComparator.class.getName()).log(Level.SEVERE, "CheckIfEqual exception {0}", ex);
                        return false;
                    }
                }

            } else {
                if (left.isFile() || right.isFile()) {
                    return false;
                }
                FolderComparator comparator = new FolderComparator(left, right);
                return comparator.checkFolders();
            }
        }

    }
}
