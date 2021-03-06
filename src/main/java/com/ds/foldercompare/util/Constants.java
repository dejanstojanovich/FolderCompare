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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.util.Exceptions;

/**
 * Collection of static constants, variables and methods
 *
 * @author Dejan Stojanovic
 */
public class Constants {

    private final static Logger LOG = Logger.getLogger(Constants.class.getName());
    public final static String DATA = "data";
    public final static String DATA_LEFT = "data_left";
    public final static String DATA_RIGHT = "data_right";
    public final static String DIFF_TYPES = "diff_types";
    public final static String EXTENSIONS = "extensions";
    public final static String FILE = "file";
    public final static String FILE_LEFT = "file_left";
    public final static String FILE_LIST = "file_list";
    public final static String FILE_RIGHT = "file_right";
    public final static String EOL_LEFT = "eol_left";
    public final static String EOL_RIGHT = "eol_right";
    public final static String FIRST_LEFT_EMPTY = "first_left_empty";
    public final static String FIRST_RIGHT_EMPTY = "first_right_empty";
    public final static String FOLDER = "folder";
    public final static String FOLDER_LEFT = "folder_left";
    public final static String FOLDER_RIGHT = "folder_right";
    public final static String LINES = "lines";
    public final static String LOCK = "lock";
    public final static String MESSAGE = "message";
    public final static String ENCODINGS = "encodings";
    public final static String ENCODING = "encoding";
    public final static String ENCODING_LEFT = "encoding_left";
    public final static String ENCODING_RIGHT = "encoding_right";
    private static final Difference[] NO_DIFFERENCES = new Difference[0];
    public static String PARENT_FOLDER = "parent_folder";
    public static String ROOT = "root";
    public static String SEPARATOR = File.separator;
    public static String SIDE = "side";
    public final static String SIZE_LIMIT = "size_limit";
    public final static String SIZE_LIMIT_STRING = "size_limit_string";
    public static String TIMEZONE = "timezone";
    public static SimpleDateFormat fileModifiedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Comparator<File> fileTreeComparator = (File f1, File f2)
            -> (f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase()));
    public static SortedMap<String, Charset> CHARSET_AVAILABLE = Charset.availableCharsets();
    public static LinkedHashMap<String, String> CHARSET_SHORTLIST = new LinkedHashMap<>();

    static {
        setShortEncodingList();
    }

    /**
     * Perform the matching and determine all the differences on the selected
     * files
     *
     * @param fileLeft first file to compare, displayed in the left pane
     * @param fileRight second file to compare, displayed in the right pane
     * @param encodingLeft charset used when saving the left side file
     * @param encodingRight charset used when saving the right side file
     * @return DiffResult object containing all the resulting data of the
     * comparison
     */
    public static DiffResult diffFiles(File fileLeft, File fileRight, String encodingLeft, String encodingRight) {
        Difference[] diffs;
        DiffResult result = new DiffResult(NO_DIFFERENCES);
        try {

            List<String> fileLeftLines = Constants.readFile(fileLeft, encodingLeft);
            List<String> fileRightLines = Constants.readFile(fileRight, encodingRight);
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

    /**
     * Get the formatted date/time for the provided timestamp and specified time
     * zone
     *
     * @param timestamp long timestamp
     * @param tz standard time zone string
     * @return formatted date/time (yyyy-MM-dd HH:mm:ss)
     */
    public static String getDate(Long timestamp, String tz) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(tz));
        c.setTimeInMillis(timestamp);
        return fileModifiedFormat.format(c.getTime());
    }

    /**
     * Prepare the output diff result from both compared files and make it ready
     * for visual presentation
     *
     * @param linesFirst list of lines containing left file
     * @param linesSecond list of lines containing right file
     * @param diffs array of determined differences
     * @return DiffResult object containing both file contents with marked
     * differences per line and difference data
     */
    private static DiffResult getDiffLines(List<String> linesFirst, List<String> linesSecond, Difference[] diffs) {
        DiffResult result = new DiffResult(diffs);
        Difference diff;
        for (int i = 0; i < linesFirst.size(); i++) {
            result.getLeftFile().add(new DiffLine(linesFirst.get(i), i + 1));

        }
        for (int i = 0; i < linesSecond.size(); i++) {
            result.getRightFile().add(new DiffLine(linesSecond.get(i), i + 1));

        }

        for (int j = diffs.length - 1; j >= 0; j--) {
            diff = diffs[j];
            result.getDiffTypes().add(0, diff.getType());
            if (diff.getFirstStart() == 0) {
                result.setFirstLeftEmpty(true);
            } else {
                if (diff.getFirstEnd() < diff.getFirstStart()) {
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

    /**
     * Get the file/folder size for the provided File object Directory size is
     * calculated as sum of all files/directories it contains
     *
     * @param file file or directory to get the size from
     * @return total size expressed in bytes
     */
    public static long getFileSize(File file) {
        long fileSize = 0L;
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                for (File child : children) {
                    fileSize += getFileSize(child);
                }
            } else {
                fileSize = file.length();
            }
        }
        return fileSize;
    }

    /**
     * Helper method used for Font Awesome icons selection used to visually
     * distinguish files/directories in the list
     *
     * @param file
     * @return
     */
    public static String getFileType(File file) {
        if (file == null) {
            return "";
        } else {
            if (file.isDirectory()) {
                return "fa-folder-open";
            } else {
                return "fa-file-text";
            }
        }

    }

    /**
     * Combine list of lines read from the file into the single string
     *
     * @param lines list of strings
     * @return single string containing complete file contents
     */
    public static String getText(List<String> lines) {
        StringBuilder page = new StringBuilder();
        lines.forEach((line) -> {
            page.append(line).append("\n");
        });
        return page.toString();
    }

    /**
     * List the directory and return the list of file objects contained in it
     *
     * @param parent absolute path of the directory or "ROOT" string
     * @return if "ROOT" is passed as parameter, list of partitions/root folder
     * objects is returned otherwise a directory listing is returned
     */
    public static ArrayList<File> listFiles(String parent) {
        TreeSet<File> sortedFolders = new TreeSet<>(fileTreeComparator);
        TreeSet<File> sortedFiles = new TreeSet<>(fileTreeComparator);
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
                    sortedFolders.addAll(Arrays.asList(fileList));
                    files.addAll(sortedFolders);
                }

                fileList = folder.listFiles((file) -> !file.isHidden() && file.isFile());
                if (fileList != null) {
                    sortedFiles.addAll(Arrays.asList(fileList));
                    files.addAll(sortedFiles);
                }
            }
        }
        return files;
    }

    /**
     * Helper method for reading UTF8 files
     *
     * @param file file object to be read
     * @param encodingName charset used when saving the file
     * @return list of strings containing file lines
     */
    public static List<String> readFile(File file, String encodingName) {
        String encodingId;
        Charset encoding;
        if (CHARSET_SHORTLIST.containsKey(encodingName)) {
            encodingId = CHARSET_SHORTLIST.get(encodingName);
            encoding = CHARSET_AVAILABLE.get(encodingId);
        } else {
            encoding = StandardCharsets.UTF_8;
        }
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(file.toPath(), encoding);
        } catch (java.nio.charset.UnmappableCharacterException ex) {
            Logger.getLogger("Constants").log(Level.INFO, "Bad encoding {0}", file.getAbsolutePath() + "\n" + ex);
            lines.add("Wrong character encoding specified");
        } catch (java.nio.charset.MalformedInputException ex) {
            Logger.getLogger("Constants").log(Level.INFO, "Bad encoding {0}", file.getAbsolutePath() + "\n" + ex);
            lines.add("Malformed input or wrong character encoding specified");
        } catch (IOException ex) {
            Logger.getLogger("Constants").log(Level.INFO, "Unable to read the file {0}", file.getAbsolutePath() + "\n" + ex);
        }
        return lines;
    }

    /**
     * Format the file size based on the value and adding appropriate file size
     * units
     *
     * @param size file size expressed in bytes
     * @return string containing the file size rounded to one decimal with
     * appropriate unit
     */
    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Set up short list of available charsets
     */
    public static void setShortEncodingList() {

        CHARSET_SHORTLIST.put("Unicode (UTF-8)", "UTF-8");
        CHARSET_SHORTLIST.put("Western Europe (US-ASCII)", "US-ASCII");
        CHARSET_SHORTLIST.put("Arabic (ISO-8859-6)", "ISO-8859-6");
        CHARSET_SHORTLIST.put("Arabic (Windows-1256)", "Windows-1256");
        CHARSET_SHORTLIST.put("Baltic (ISO-8859-4)", "ISO-8859-4");
        CHARSET_SHORTLIST.put("Baltic (Windows-1257)", "Windows-1257");
        CHARSET_SHORTLIST.put("Chinese simplified (GB-2312)", "GB2312");
        CHARSET_SHORTLIST.put("Chinese simplified (GB-18030)", "GB18030");
        CHARSET_SHORTLIST.put("Chinese simplified (GBK)", "GBK");
        CHARSET_SHORTLIST.put("Chinese simplified (ISO-2022-CN)", "ISO-2022-CN");
        CHARSET_SHORTLIST.put("Chinese traditional (Big5)", "Big5");
        CHARSET_SHORTLIST.put("Chinese traditional (BIG5-HKSCS)", "Big5-HKSCS");
        CHARSET_SHORTLIST.put("Cyrilic (ISO-8859-5)", "ISO-8859-5");
        CHARSET_SHORTLIST.put("Cyrilic (Windows-1251)", "Windows-1251");
        CHARSET_SHORTLIST.put("Eastern Europe (ISO-8859-2)", "ISO-8859-2");
        CHARSET_SHORTLIST.put("Eastern Europe (ISO-8859-13)", "ISO-8859-13");
        CHARSET_SHORTLIST.put("Eastern Europe (Windows-1250)", "Windows-1250");
        CHARSET_SHORTLIST.put("Greek (ISO-8859-7)", "ISO-8859-7");
        CHARSET_SHORTLIST.put("Greek (Windows-1253)", "Windows-1253");
        CHARSET_SHORTLIST.put("Hebrew (ISO-8859-8)", "ISO-8859-8");
        CHARSET_SHORTLIST.put("Hebrew (Windows-1255)", "Windows-1255");
        CHARSET_SHORTLIST.put("Japanese (EUC-JP)", "EUC-JP");
        CHARSET_SHORTLIST.put("Japanese (ISO-2022-JP)", "ISO-2022-JP");
        CHARSET_SHORTLIST.put("Japanese (Shift-JIS)", "Shift_JIS");
        CHARSET_SHORTLIST.put("Korean (EUC-KR)", "EUC-KR");
        CHARSET_SHORTLIST.put("Korean (ISO-2022-KR)", "ISO-2022-KR");
        CHARSET_SHORTLIST.put("Latin 3 (ISO-8859-3)", "ISO-8859-3");
        CHARSET_SHORTLIST.put("Latin 5 (ISO-8859-9)", "ISO-8859-9");
        CHARSET_SHORTLIST.put("Thai (TIS-620)", "TIS-620");
        CHARSET_SHORTLIST.put("Turkish (Windows-1254)", "Windows-1254");
        CHARSET_SHORTLIST.put("Unicode (UTF-16)", "UTF-16");
        CHARSET_SHORTLIST.put("Vietnamese (Windows-1258)", "Windows-1258");
        CHARSET_SHORTLIST.put("Western Europe (ISO-8859-1)", "ISO-8859-1");
        CHARSET_SHORTLIST.put("Western Europe (ISO-8859-15)", "ISO-8859-15");
        CHARSET_SHORTLIST.put("Western Europe (Windows-1252)", "Windows-1252");

        CHARSET_SHORTLIST.forEach((name, id)
                -> {
            if (!CHARSET_AVAILABLE.containsKey(id)) {
                CHARSET_AVAILABLE.remove(id);
            }
        });
    }

    /**
     * Determine the line ending for the file
     *
     * @param file file object to be checked
     * @return End of Line control character or sequence "\r\n" (Windows), "\n"
     * (Unix) and "\r" (Macintosh)
     */
    public static String getLineEnding(File file) {
        String endLineString = "";

        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            int i = -1;

            while (((i = r.read()) != -1)) {
                if (i == '\r') {
                    i = r.read();
                    if (i == '\n') {
                        endLineString = "CRLF (\\r\\n)";
                    } else {
                        endLineString = "CR (\\r)";
                    }
                } else if (i == '\n') {
                    endLineString = "LF (\\n)";
                }

            }
        } catch (FileNotFoundException ex) {
            LOG.log(Level.SEVERE, "getLineEnding error {0}", ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "getLineEnding error {0}", ex);
        }
        return endLineString;
    }
}
