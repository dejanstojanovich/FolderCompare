/* 
 * Copyright (C) 2020 Dejan Stojanovic <dejanstojanovich@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ds.foldercompare.util;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;

/**
 * Contains logic to match the folders and determine the differences
 *
 * @author Dejan Stojanovic
 */
@Data
public class FolderComparator {

    HashMap<String, File> leftList;
    HashMap<String, File> rightList;
    File parentLeft;
    File parentRight;
    ArrayList<FileComparatorItem> result = new ArrayList<>();
    private String timezone;

    /**
     * Constructor accepting directory files to match
     *
     * @param leftFolder left directory File to match the contents
     * @param rightFolder right directory File to match the contents
     * @param tz time zone used for file modified formatting
     */
    public FolderComparator(File leftFolder, File rightFolder, String tz) {
        parentLeft = leftFolder;
        leftList = mapFiles(parentLeft);
        parentRight = rightFolder;
        rightList = mapFiles(parentRight);
        timezone = tz;
    }

    /**
     * Constructor accepting directory absolute paths to match
     *
     * @param leftFolder left directory absolute path to match the contents
     * @param rightFolder right directory absolute path to match the contents
     * @param tz time zone used for file modified formatting
     */
    public FolderComparator(String leftFolder, String rightFolder, String tz) {
        this(new File(leftFolder), new File(rightFolder), tz);
    }

    /**
     * Determine if two folders have equal contents
     *
     * @return true if both folders contain equal files and folders
     */
    public boolean checkFolders() {
        boolean same = true;
        Map<Boolean, HashSet<File>> list = getCombinedPartitionedFiles(leftList, rightList);
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

    /**
     * Combine all file instances from two compared folders and separate file
     * and folder lists
     *
     * @param leftList HashMap with filename/File object pairs for the left pane
     * folder
     * @param rightList HashMap with filename/File object pairs for the right
     * pane folder
     * @return HashMap where true key contains a HashSet of all the files in
     * both folders and false key contains all the folders
     */
    public Map<Boolean, HashSet<File>> getCombinedPartitionedFiles(HashMap<String, File> leftList, HashMap<String, File> rightList) {
        return Stream.concat(leftList.values().stream(), rightList.values().stream())
                .collect(Collectors.partitioningBy(file -> file.isFile(),
                        Collectors.toCollection(HashSet::new)));
    }

    /**
     * List the directory index and collect filename/File object pairs for each
     * File object contained
     *
     * @param folder Parent folder to list the files
     * @return HashMap with filename/File object pairs for the parent folder
     */
    private HashMap<String, File> mapFiles(File folder) {
        HashMap<String, File> files = new HashMap<>();

        if (folder != null && folder.exists() && folder.isDirectory() && folder.canRead()) {
            File[] fileList = folder.listFiles();
            files = (HashMap<String, File>) Arrays.asList(fileList).stream().collect(Collectors.toMap(file -> file.getName(), file -> file));

        }
        return files;
    }

    /**
     * Inner class which contains the structure for compared file objects and
     * resulting equality
     */
    @Data
    public class FileComparatorItem {

        boolean equal = false;

        String filename;
        File leftFile;
        File rightFile;

        /**
         * Constructor accepting file object to be checked in both folders and
         * flag to distinguish file and folder checking
         *
         * @param file file object to be checked in both folders
         * @param handleFiles true if the file object is file, false for
         * directory
         */
        public FileComparatorItem(String file, boolean handleFiles) {
            filename = file;
            leftFile = leftList.get(file);
            rightFile = rightList.get(file);
            equal = checkIfEqual(leftFile, rightFile, handleFiles);
        }

        /**
         * Check the equality of the same object in compared directories
         *
         * @param left file object in the left pane directory
         * @param right file object in the right pane directory
         * @param handleFiles true to check if comparison is performed on a
         * file, false if directories are matched
         * @return true if both file objects have same contents, false otherwise
         */
        private boolean checkIfEqual(File left, File right, boolean handleFiles) {
            if (left == null || right == null) {
                return false;
            }
            //perform matching on files
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
            } else {         //perform matching on directories
                if (left.isFile() || right.isFile()) {
                    return false;
                }
                FolderComparator comparator = new FolderComparator(left, right,timezone);
                return comparator.checkFolders();
            }
        }

        /**
         * Helper method for visual representation of the matched results for
         * the file on the left pane
         *
         * @return red - danger Bootstrap button class for different file dark -
         * for file missing in the left folder grey - for files of equal
         * contents
         */
        public String getLeftFileClass() {
            if (equal) {
                return "btn-secondary";
            } else {
                return leftFile == null ? "btn-dark" : "btn-danger";
            }
        }

        /**
         * Helper method for visual representation of the matched results for
         * the file on the right pane
         *
         * @return red - danger Bootstrap button class for different file dark -
         * for file missing in the left folder grey - for files of equal
         * contents
         */
        public String getRightFileClass() {
            if (equal) {
                return "btn-secondary";
            } else {
                return rightFile == null ? "btn-dark" : "btn-danger";
            }

        }

        /**
         * Helper method for retrieving the left file object size
         *
         * @return file size in bytes
         */
        public String getLeftFileLength() {
            return leftFile == null ? "" : leftFile.length() + "";
        }

        /**
         * Helper method for retrieving the right file object size
         *
         * @return file size in bytes
         */
        public String getRightFileLength() {
            return rightFile == null ? "" : rightFile.length() + "";
        }

        /**
         * Helper method for retrieving the left file object timestamp of last
         * modification
         *
         * @return formatted last modified date/time of the file
         */
        public String getLeftFileModified() {
            return leftFile == null ? "" : Constants.getDate(leftFile.lastModified(), timezone);
        }

        /**
         * Helper method for retrieving the right file object timestamp of last
         * modification
         *
         * @return formatted last modified date/time of the file
         */
        public String getRightFileModified() {
            return rightFile == null ? "" : Constants.getDate(rightFile.lastModified(), timezone);
        }

        /**
         * Helper method for retrieving the left file object name or "-" if the
         * file is not available in the left folder
         *
         * @return file name or "-" if the file doesn't exist in the left folder
         */
        public String getLeftFileName() {
            return leftFile == null ? "-" : leftFile.getName();
        }

        /**
         * Helper method for retrieving the right file object name or "-" if the
         * file is not available in the right folder
         *
         * @return file name or "-" if the file doesn't exist in the left folder
         */
        public String getRightFileName() {
            return rightFile == null ? "-" : rightFile.getName();
        }

        /**
         * Helper method for retrieving the left file object absolute path or
         * "-" if the file is not available in the left folder
         *
         * @return absolute file path or "-" if the file doesn't exist in the
         * left folder
         */
        public String getLeftFilePath() {
            return leftFile == null ? "-" : leftFile.getAbsolutePath();
        }

        /**
         * Helper method for retrieving the right file object absolute path or
         * "-" if the file is not available in the right folder
         *
         * @return absolute file path or "-" if the file doesn't exist in the
         * right folder
         */
        public String getRightFilePath() {
            return rightFile == null ? "-" : rightFile.getAbsolutePath();
        }

        /**
         * Helper method for retrieving formatted left file object size
         * containing units
         *
         * @return file size expressed in appropriate units based on the size
         */
        public String getLeftFileSize() {
            return leftFile == null ? "" : Constants.readableFileSize(leftFile.length());
        }

        /**
         * Helper method for retrieving formatted right file object size
         * containing units
         *
         * @return file size expressed in appropriate units based on the size
         */
        public String getRightFileSize() {
            return rightFile == null ? "" : Constants.readableFileSize(rightFile.length());
        }

        /**
         * Helper method to retrieve if the left file object is file or
         * directory
         *
         * @return file if the object is file or empty string if the object is
         * directory
         */
        public String getLeftFileType() {
            return leftFile == null ? "" : (leftFile.isFile() ? "file" : "");
        }

        /**
         * Helper method to retrieve if the right file object is file or
         * directory
         *
         * @return file if the object is file or empty string if the object is
         * directory
         */
        public String getRightFileType() {
            return rightFile == null ? "" : (rightFile.isFile() ? "file" : "");
        }

    }
}
