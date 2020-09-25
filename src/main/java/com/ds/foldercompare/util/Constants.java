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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.spi.diff.DiffProvider;

/**
 *
 * @author Dejan Stojanovic
 */
public class Constants {

    public static String DATA = "data";
    public static String DATA_LEFT = "data_left";
    public static String DATA_RIGHT = "data_right";
    public static String DIFF_TYPES = "diff_types";
    public static Charset ENCODING = StandardCharsets.UTF_8;
    public static String FILE = "file";
    public static String FILE_LEFT = "file_left";
    public static String FILE_LIST = "file_list";
    public static String FILE_RIGHT = "file_right";
    public static String FIRST_LEFT_EMPTY = "first_left_empty";
    public static String FIRST_RIGHT_EMPTY = "first_right_empty";
    public static String FOLDER = "folder";
    public static String FOLDER_LEFT = "folder_left";
    public static String FOLDER_RIGHT = "folder_right";
    public static String LINES = "lines";
    public static String LOCK = "lock";
    public static String MESSAGE = "message";
    private static final Difference[] NO_DIFFERENCES = new Difference[0];
    public static String PARENT_FOLDER = "parent_folder";
    public static String ROOT = "root";
    public static String SEPARATOR = File.separator;
    public static String SIDE = "side";
    public static String TIMEZONE = "Europe/Belgrade";
    public static SimpleDateFormat modifiedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static DiffResult diffFiles(File fileLeft, File fileRight) {
        Difference[] diffs;
        DiffResult result = new DiffResult(NO_DIFFERENCES);
        try {

            List<String> fileLeftLines = Constants.readFile(fileLeft);
            List<String> fileRightLines = Constants.readFile(fileRight);
            Reader first = new StringReader(Constants.getText(fileLeftLines));
            Reader second = new StringReader(Constants.getText(fileRightLines));
            DiffProvider diff;
            diff = new BuiltInDiffProvider();
            diffs = diff.computeDiff(first, second);
            result = getDiffLines(fileLeftLines, fileRightLines, diffs);
        } catch (IOException e) {
            Logger.getLogger("Constants").log(Level.SEVERE, "diffFiles error {0}", e);
        }
        return result;
    }

    public static String getDate(Long timestamp) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(TIMEZONE));
        c.setTimeInMillis(timestamp);
        return modifiedFormat.format(c.getTime());
    }

    private static DiffResult getDiffLines(List<String> linesFirst, List<String> linesSecond, Difference[] diffs) {
        DiffResult result = new DiffResult(diffs);
        Difference diff;
        linesFirst.forEach((line) -> {
            result.getLeftFile().add(new DiffLine(line));
        });
        linesSecond.forEach((line) -> {
            result.getRightFile().add(new DiffLine(line));
        });
        for (int j = 0; j < diffs.length; j++) {
            diff = diffs[j];
            result.getDiffTypes().add(diff.getType());
            if (diff.getFirstStart() == 0) {
                result.setFirstLeftEmpty(true);
            } else {
                if (diff.getFirstEnd() < diff.getFirstStart()) {
//                result.getLeftFile().add(diff.getFirstStart(), new DiffLine(true));
                    result.getLeftFile().get(diff.getFirstStart() - 1).addEmpty(j);
                } else {
                    for (int i = diff.getFirstStart(); i <= diff.getFirstEnd(); i++) {
                        result.getLeftFile().get(i - 1).setDiffData(diff.getType(), j);
                    }
                }
            }
            if (diff.getSecondStart() == 0) {
                result.setFirstRightEmpty(true);
            } else {
                if (diff.getSecondEnd() < diff.getSecondStart()) {
//                result.getRightFile().add(diff.getSecondStart(), new DiffLine(true));
                    result.getRightFile().get(diff.getSecondStart() - 1).addEmpty(j);
                } else {
                    for (int i = diff.getSecondStart(); i <= diff.getSecondEnd(); i++) {

                        result.getRightFile().get(i - 1).setDiffData(diff.getType(), j);
                    }
                }
            }
        }
        return result;
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

    public static String getFileType(File file) {
        if (file == null) {
            return "";
        } else {
            if (Files.isSymbolicLink(file.toPath())) {
                return "fa-external-link-square";
            }
            if (file.isDirectory()) {
                return "fa-folder-open";
            } else {
                return "fa-file-text";
            }
        }

    }

    public static String getText(List<String> lines) {
        StringBuilder page = new StringBuilder();
        for (String line : lines) {
            page.append(line).append("\n");
        }
        return page.toString();
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

                File[] fileList = folder.listFiles((file) -> !file.isHidden() && file.isDirectory() && Files.isReadable(file.toPath()));
                if (fileList != null) {
                    files.addAll(Arrays.asList(fileList));
                }

                fileList = folder.listFiles((file) -> !file.isHidden() && file.isFile());
                if (fileList != null) {
                    files.addAll(Arrays.asList(fileList));
                }
            }
        }
        return files;
    }

    public static List<String> readFile(File file) {

        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(file.toPath(), ENCODING);
        } catch (IOException ex) {
            Logger.getLogger("Constants").log(Level.INFO, "Unable to read the file {0}", file.getAbsolutePath() + "\n" + ex);
        }
        return lines;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public enum FILE_SIDE {
        LEFT, RIGHT
    }

}
