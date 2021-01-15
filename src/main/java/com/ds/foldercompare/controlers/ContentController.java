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
package com.ds.foldercompare.controlers;

import com.ds.foldercompare.util.Constants;
import static com.ds.foldercompare.util.Constants.CHARSET_SHORTLIST;
import static com.ds.foldercompare.util.Constants.DATA_LEFT;
import static com.ds.foldercompare.util.Constants.DATA_RIGHT;
import static com.ds.foldercompare.util.Constants.DIFF_TYPES;
import static com.ds.foldercompare.util.Constants.ENCODING;
import static com.ds.foldercompare.util.Constants.ENCODINGS;
import static com.ds.foldercompare.util.Constants.ENCODING_LEFT;
import static com.ds.foldercompare.util.Constants.ENCODING_RIGHT;
import static com.ds.foldercompare.util.Constants.EOL_LEFT;
import static com.ds.foldercompare.util.Constants.EOL_RIGHT;
import static com.ds.foldercompare.util.Constants.EXTENSIONS;
import static com.ds.foldercompare.util.Constants.FILE;
import static com.ds.foldercompare.util.Constants.FILE_LEFT;
import static com.ds.foldercompare.util.Constants.FILE_LIST;
import static com.ds.foldercompare.util.Constants.FILE_RIGHT;
import static com.ds.foldercompare.util.Constants.FIRST_LEFT_EMPTY;
import static com.ds.foldercompare.util.Constants.FIRST_RIGHT_EMPTY;
import static com.ds.foldercompare.util.Constants.FOLDER;
import static com.ds.foldercompare.util.Constants.FOLDER_LEFT;
import static com.ds.foldercompare.util.Constants.FOLDER_RIGHT;
import static com.ds.foldercompare.util.Constants.LINES;
import static com.ds.foldercompare.util.Constants.MESSAGE;
import static com.ds.foldercompare.util.Constants.ROOT;
import static com.ds.foldercompare.util.Constants.SEPARATOR;
import static com.ds.foldercompare.util.Constants.SIDE;
import static com.ds.foldercompare.util.Constants.SIZE_LIMIT;
import static com.ds.foldercompare.util.Constants.SIZE_LIMIT_STRING;
import static com.ds.foldercompare.util.Constants.TIMEZONE;
import static com.ds.foldercompare.util.Constants.getLineEnding;
import static com.ds.foldercompare.util.Constants.listFiles;
import com.ds.foldercompare.util.DiffResult;
import com.ds.foldercompare.util.FolderComparator;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller class for serving home page and AJAX requests
 *
 * @author Dejan Stojanovic
 */
@Controller
public class ContentController {

    private final Logger LOG = Logger.getLogger(getClass().getName());
    private long allowedDiffSize = 3000;
    //property defined in application.properties, used to limit the handled file size
    @Value("${diff.allowedDiffSize:3MB}")
    private String allowedDiffSizeString;
    //property defined in application.properties, used to limit the handled file extension
    @Value("${diff.extensions:}#{T(java.util.Collections).emptySet()}")
    private HashSet<String> extensions;
    //property defined in application.properties, used to format the file modified timestamp 
    @Value("${diff.timezone:Europe/Belgrade}")
    private String timezone;

    /**
     * Post construct method for parsing the string file size defined in
     * application.properties into long value
     */
    @PostConstruct
    private void parseDiffSize() {
        try {
            DataSize dataSize = DataSize.parse(allowedDiffSizeString.toUpperCase());
            allowedDiffSize = dataSize.toBytes();
        } catch (IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "allowedDiffSize property not valid {0}", e);
            LOG.log(Level.SEVERE, "defaulting to {0}", allowedDiffSize);
        }
    }

    /**
     * Handle AJAX call to compare two folders passed as in the form of the
     * HashMap
     *
     * @param model MVC model object for passing data to view
     * @param params AJAX parameters containing absolute paths of the folders to
     * compare
     * @return compareFragment used to display the contents of the provided
     * folders, marking the files/folders which are not equal. In case of
     * validation error, error message is returned
     */
    @RequestMapping(value = "/folder_compare")
    public String folderCompare(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FOLDER_LEFT) && params.containsKey(FOLDER_RIGHT)) {
            FolderComparator comparator = new FolderComparator(params.get(FOLDER_LEFT), params.get(FOLDER_RIGHT), timezone);
            comparator.checkFolders();
            ArrayList<FolderComparator.FileComparatorItem> result = comparator.getResult();
            model.addAttribute(FOLDER_LEFT, params.get(FOLDER_LEFT));
            model.addAttribute(FOLDER_RIGHT, params.get(FOLDER_RIGHT));
            model.addAttribute(FILE_LIST, result);
            return "viewCompareDirectory :: compareDirFragment";
        } else {
            LOG.log(Level.SEVERE, "noParamsErrorFragment returned on compare");
            return "errors :: noParamsErrorFragment";
        }
    }

    /**
     * Handle AJAX call to compare two files passed as parameters
     *
     * @param model MVC model object for passing data to view
     * @param params AJAX parameters containing absolute paths of the files to
     * compare
     * @return diffFragment used to display the contents of the provided files,
     * marking the lines in the files which are different. In case of validation
     * error, error message is returned
     */
    @RequestMapping(value = "/file_compare")
    public String fileCompare(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FILE_LEFT) && params.containsKey(FILE_RIGHT)
                && params.containsKey(FILE_LEFT) && params.containsKey(FILE_RIGHT)) {
            String encodingLeft = params.get(ENCODING_LEFT);
            String encodingRight = params.get(ENCODING_RIGHT);
            File fileLeft = new File(params.get(FILE_LEFT));
            File fileRight = new File(params.get(FILE_RIGHT));
            if (isExtensionValid(fileLeft) && isExtensionValid(fileRight)) {
                if (!fileLeft.exists()) {
                    model.addAttribute(FILE, params.get(FILE_LEFT));
                    LOG.log(Level.SEVERE, "fileExistErrorFragment returned on fileCompare FILE_LEFT");
                    return "errors :: fileExistErrorFragment";
                }
                if (!isFileSizeValid(fileLeft)) {
                    model.addAttribute(MESSAGE, String.format("Error - left file size exceeds limit (%s)", Constants.readableFileSize(allowedDiffSize)));
                    return "errors :: errorFragment";
                }
                if (!fileRight.exists()) {
                    model.addAttribute(FILE, params.get(FILE_RIGHT));
                    LOG.log(Level.SEVERE, "fileExistErrorFragment returned on fileCompare FILE_RIGHT");
                    return "errors :: fileExistErrorFragment";
                }
                if (!isFileSizeValid(fileRight)) {
                    model.addAttribute(MESSAGE, String.format("Error - right file size exceeds limit (%s)", Constants.readableFileSize(allowedDiffSize)));
                    return "errors :: errorFragment";
                }
                model.addAttribute(FILE_LEFT, params.get(FILE_LEFT));
                model.addAttribute(FILE_RIGHT, params.get(FILE_RIGHT));

                DiffResult data = Constants.diffFiles(fileLeft, fileRight, encodingLeft, encodingRight);

                model.addAttribute(FIRST_LEFT_EMPTY, data.isFirstLeftEmpty());
                model.addAttribute(FIRST_RIGHT_EMPTY, data.isFirstRightEmpty());
                model.addAttribute(DIFF_TYPES, data.getDiffTypes());
                model.addAttribute(DATA_LEFT, data.getLeftFile());
                model.addAttribute(DATA_RIGHT, data.getRightFile());
                model.addAttribute(EOL_LEFT,  getLineEnding(fileLeft));
                model.addAttribute(EOL_RIGHT, getLineEnding(fileRight));
                return "viewCompareFile :: diffFileFragment";
            } else {
                if (!isExtensionValid(fileLeft) && !isExtensionValid(fileRight)) {
                    model.addAttribute(MESSAGE, "Error - both file extensions not permitted");
                } else if (!isExtensionValid(fileLeft)) {
                    model.addAttribute(MESSAGE, "Error - left file extension not permitted");
                } else if (!isExtensionValid(fileRight)) {
                    model.addAttribute(MESSAGE, "Error - right file extension not permitted");
                }
                return "errors :: errorFragment";
            }
        } else {
            LOG.log(Level.SEVERE, "noParamsErrorFragment returned on fileCompare");
            return "errors :: noParamsErrorFragment";
        }

    }

    /**
     * Handle AJAX call to list the folder passed as parameter
     *
     * @param model MVC model object for passing data to view
     * @param params AJAX parameters containing absolute path of the folder to
     * list and the side of the panel to display the list
     * @return paneFragment containing list of files/folders, folders first,
     * found in the provided folder. Error message is returned if any of the
     * parameters is missing
     */
    @RequestMapping(value = "/list")
    public String getListing(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FOLDER) && params.containsKey(SIDE)) {
            model.addAttribute(FOLDER, params.get(FOLDER).endsWith(SEPARATOR) ? params.get(FOLDER) : params.get(FOLDER) + SEPARATOR);
            model.addAttribute(TIMEZONE, timezone);
            model.addAttribute(SIDE, params.get(SIDE));
            model.addAttribute(FILE_LIST, listFiles(params.get(FOLDER)));
            return "pane :: paneFragment";
        } else {
            LOG.log(Level.SEVERE, "noParamsErrorFragment returned on getListing");
            model.addAttribute(MESSAGE, params.get(SIDE));
            return "errors :: noParamsErrorFragment";
        }

    }

    /**
     * Handle AJAX call for "Up" button to move one directory up
     *
     * @param model MVC model object for passing data to view
     * @param params AJAX parameters containing absolute path of the current
     * folder
     * @return paneFragment containing list of files/folders, folders first,
     * found in the parent directory of the provided folder. If the parent
     * folder does not exist or has limited access, root/partition listing is
     * returned. Error message is returned if any of the parameters is missing
     */
    @RequestMapping(value = "/up")
    public String getParentListing(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FOLDER) && params.containsKey(SIDE)) {
            File current = new File(params.get(FOLDER));
            if (current.exists()) {
                File parent = current.getParentFile();
                if (parent != null && parent.exists()) {
                    model.addAttribute(FOLDER, parent.getAbsolutePath().endsWith(SEPARATOR) ? parent.getAbsolutePath() : parent.getAbsolutePath() + SEPARATOR);
                    model.addAttribute(SIDE, params.get(SIDE));
                    model.addAttribute(TIMEZONE, timezone);
                    model.addAttribute(FILE_LIST, listFiles(parent.getAbsolutePath()));
                    return "pane :: paneFragment";

                } else {
                    model.addAttribute(FOLDER, ROOT);
                    model.addAttribute(FILE_LIST, listFiles(ROOT));
                    return "viewRootPane :: paneFragment";
                }

            } else {
                model.addAttribute(FOLDER, ROOT);
                model.addAttribute(FILE_LIST, listFiles(ROOT));
                return "viewRootPane :: paneFragment";
            }

        } else {
            LOG.log(Level.SEVERE, "noParamsErrorFragment returned on getParentListing");
            return "errors :: noParamsErrorFragment";
        }

    }

    /**
     * Home page handler
     *
     * @param model model MVC model object for passing data to home view
     * @return home page populated with root/partition listing
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(EXTENSIONS, extensions);
        model.addAttribute(SIZE_LIMIT, allowedDiffSize);
        model.addAttribute(SIZE_LIMIT_STRING, Constants.readableFileSize(allowedDiffSize));
        model.addAttribute(FILE_LIST, listFiles(ROOT));
        model.addAttribute(ENCODINGS, CHARSET_SHORTLIST.keySet());
        LOG.log(Level.INFO, "Get Homepage");
        return "home";
    }

    /**
     * Validation method for checking if the file to be displayed or compared
     * has the allowed extension. Only textual files should be supported.
     *
     * @param file file object to check
     * @return true if the extension is allowed or no extension filter is set.
     */
    private boolean isExtensionValid(File file) {
        String extension = "";

        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }
        return extensions.isEmpty() || extensions.contains(extension);
    }

    /**
     * Validation method for checking if the file to be displayed or compared is
     * not larger than permitted. Since the results are displayed in a browser,
     * it makes sense to limit the file size to a reasonable value.
     *
     * @param file file object to check
     * @return true if the file size is within the allowed limit
     */
    private boolean isFileSizeValid(File file) {
        return file.length() < allowedDiffSize || allowedDiffSize < 0;
    }

    /**
     * Handle AJAX call to retrieve the file contents
     *
     * @param model MVC model object for passing data to view
     * @param params AJAX parameters containing absolute path of the file to
     * show
     * @return linesFragment containing list text lines which are contained in
     * the file. Error message is returned if the file extension or size are not
     * allowed or a parameter is missing
     */
    @RequestMapping(value = "/showFile")
    public String showFile(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FILE) && params.containsKey(ENCODING)) {
            File file = new File(params.get(FILE));
            if (isExtensionValid(file)) {
                if (file.exists()) {
                    if (isFileSizeValid(file)) {
                        List<String> lines = Constants.readFile(file, params.get(ENCODING));
                        model.addAttribute(LINES, lines);
                        return "filePane :: linesFragment";
                    } else {
                        model.addAttribute(MESSAGE, String.format("Error - file size exceeds limit (%s)", Constants.readableFileSize(allowedDiffSize)));
                        return "errors :: errorFragment";
                    }
                } else {
                    model.addAttribute(FILE, params.get(FILE));
                    LOG.log(Level.SEVERE, "fileExistErrorFragment returned on showFile");
                    return "errors :: fileExistErrorFragment";
                }

            } else {
                model.addAttribute(MESSAGE, String.format("Error - file extension not permitted. Only (%s) extensions are permitted!", String.join(", ", extensions)));
                return "errors :: errorFragment";
            }

        } else {
            LOG.log(Level.SEVERE, "noParamsErrorFragment returned on showFile");
            return "errors :: noParamsErrorFragment";
        }

    }

}
