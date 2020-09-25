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
import static com.ds.foldercompare.util.Constants.DATA_LEFT;
import static com.ds.foldercompare.util.Constants.DATA_RIGHT;
import static com.ds.foldercompare.util.Constants.DIFF_TYPES;
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
import static com.ds.foldercompare.util.Constants.listFiles;
import com.ds.foldercompare.util.DiffLine;
import com.ds.foldercompare.util.DiffResult;
import com.ds.foldercompare.util.FolderComparator;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.spi.diff.DiffProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Dejan Stojanovic
 */
@Controller
public class ContentController {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    @Value("${diff.allowedDiffSize:2097152}") //2MB
    private long allowedDiffSize;

    @Value("${diff.extensions:}#{T(java.util.Collections).emptySet()}")
    private HashSet<String> extensions;

    @RequestMapping(value = "/compare")
    public String compare(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FOLDER_LEFT) && params.containsKey(FOLDER_RIGHT)) {
            FolderComparator comparator = new FolderComparator(params.get(FOLDER_LEFT), params.get(FOLDER_RIGHT));
            comparator.checkFolders();
            ArrayList<FolderComparator.FileComparatorItem> result = comparator.getResult();
            model.addAttribute(FOLDER_LEFT, params.get(FOLDER_LEFT));
            model.addAttribute(FOLDER_RIGHT, params.get(FOLDER_RIGHT));
            model.addAttribute(FILE_LIST, result);
            return "compareView :: compareFragment";
        } else {
            LOG.log(Level.SEVERE, "noParamsErrorFragment returned on compare");
            return "errors :: noParamsErrorFragment";
        }

    }

    @RequestMapping(value = "/file_compare")
    public String fileCompare(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FILE_LEFT) && params.containsKey(FILE_RIGHT)) {

            File fileLeft = new File(params.get(FILE_LEFT));
            File fileRight = new File(params.get(FILE_RIGHT));
            if (isExtensionValid(fileLeft) && isExtensionValid(fileRight)) {
                if (!fileLeft.exists()) {
                    model.addAttribute(FILE, params.get(FILE_LEFT));
                    LOG.log(Level.SEVERE, "fileExistErrorFragment returned on fileCompare FILE_LEFT");
                    return "errors :: fileExistErrorFragment";
                }

                if (!fileRight.exists()) {
                    model.addAttribute(FILE, params.get(FILE_RIGHT));
                    LOG.log(Level.SEVERE, "fileExistErrorFragment returned on fileCompare FILE_RIGHT");
                    return "errors :: fileExistErrorFragment";
                }
                model.addAttribute(FILE_LEFT, params.get(FILE_LEFT));
                model.addAttribute(FILE_RIGHT, params.get(FILE_RIGHT));
                DiffResult data = Constants.diffFiles(fileLeft, fileRight);

                model.addAttribute(FIRST_LEFT_EMPTY, data.isFirstLeftEmpty());
                model.addAttribute(FIRST_RIGHT_EMPTY, data.isFirstRightEmpty());
                model.addAttribute(DIFF_TYPES, data.getDiffTypes());
                model.addAttribute(DATA_LEFT, data.getLeftFile());
                model.addAttribute(DATA_RIGHT, data.getRightFile());
                return "diffPane :: diffFragment";
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

    @RequestMapping(value = "/list")
    public String getListing(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FOLDER)) {
            model.addAttribute(FOLDER, params.get(FOLDER).endsWith(SEPARATOR) ? params.get(FOLDER) : params.get(FOLDER) + SEPARATOR);
            model.addAttribute(SIDE, params.get(SIDE));
            model.addAttribute(FILE_LIST, listFiles(params.get(FOLDER)));
            return "pane :: paneFragment";
        } else {
            LOG.log(Level.SEVERE, "noParamsErrorFragment returned on getListing");
            model.addAttribute(MESSAGE, params.get(SIDE));
            return "errors :: noParamsErrorFragment";
        }

    }

    @RequestMapping(value = "/up")
    public String getParentListing(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FOLDER)) {
            File current = new File(params.get(FOLDER));
            if (current.exists()) {
                File parent = current.getParentFile();
                if (parent != null && parent.exists()) {
                    model.addAttribute(FOLDER, parent.getAbsolutePath().endsWith(SEPARATOR) ? parent.getAbsolutePath() : parent.getAbsolutePath() + SEPARATOR);
                    model.addAttribute(SIDE, params.get(SIDE));
                    model.addAttribute(FILE_LIST, listFiles(parent.getAbsolutePath()));
                    return "pane :: paneFragment";

                } else {
                    model.addAttribute(FOLDER, ROOT);
                    model.addAttribute(FILE_LIST, listFiles(ROOT));
                    return "rootPane :: paneFragment";
                }

            } else {
                model.addAttribute(FOLDER, ROOT);
                model.addAttribute(FILE_LIST, listFiles(ROOT));
                return "rootPane :: paneFragment";
            }

        } else {
            LOG.log(Level.SEVERE, "noParamsErrorFragment returned on getParentListing");
            return "errors :: noParamsErrorFragment";
        }

    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(FILE_LIST, listFiles(ROOT));
        LOG.log(Level.INFO, "Get Homepage");
        return "home";
    }

    private boolean isExtensionValid(File file) {
        String extension = "";

        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }
        return extensions.isEmpty() || extensions.contains(extension);
    }

    private boolean isFileSizeValid(File file) {
        return allowedDiffSize < file.length() || allowedDiffSize < 0;
    }

    @RequestMapping(value = "/showFile")
    public String showFile(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FILE)) {
            File file = new File(params.get(FILE));
            if (isExtensionValid(file)) {
                if (file.exists()) {
                    List<String> lines = Constants.readFile(file);
                    model.addAttribute(LINES, lines);
                    return "filePane :: linesFragment";
                } else {
                    model.addAttribute(FILE, params.get(FILE));
                    LOG.log(Level.SEVERE, "fileExistErrorFragment returned on showFile");
                    return "errors :: fileExistErrorFragment";
                }
            } else {
                return "errors :: extensionNotValidErrorFragment";
            }

        } else {
            LOG.log(Level.SEVERE, "noParamsErrorFragment returned on showFile");
            return "errors :: noParamsErrorFragment";
        }

    }

}
