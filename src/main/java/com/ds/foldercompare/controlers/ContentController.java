/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ds.foldercompare.controlers;

import static com.ds.foldercompare.util.Constants.FILE_LIST;
import static com.ds.foldercompare.util.Constants.FOLDER;
import static com.ds.foldercompare.util.Constants.FOLDER_LEFT;
import static com.ds.foldercompare.util.Constants.FOLDER_RIGHT;
import static com.ds.foldercompare.util.Constants.LOCK;
import static com.ds.foldercompare.util.Constants.ROOT;
import static com.ds.foldercompare.util.Constants.SEPARATOR;
import static com.ds.foldercompare.util.Constants.SIDE;
import static com.ds.foldercompare.util.Constants.listFiles;
import com.ds.foldercompare.util.FolderComparator;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Dejan
 */
@Controller
public class ContentController {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(FILE_LIST, listFiles(ROOT));
        LOG.log(Level.INFO, "Get Homepage");
        return "home";
    }

    @RequestMapping(value = "/list")
    public String getListing(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FOLDER)) {
            model.addAttribute(FOLDER, params.get(FOLDER).endsWith(SEPARATOR) ? params.get(FOLDER) : params.get(FOLDER) + SEPARATOR);
            model.addAttribute(LOCK, params.get(LOCK).equals("true"));
            model.addAttribute(SIDE, params.get(SIDE));
            model.addAttribute(FILE_LIST, listFiles(params.get(FOLDER)));
            return "pane :: paneFragment";
        } else {
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
                    model.addAttribute(LOCK, params.get(LOCK).equals("true"));
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
            return "errors :: noParamsErrorFragment";
        }

    }

    @RequestMapping(value = "/compare")
    public String compare(Model model, @RequestBody HashMap<String, String> params) {
        if (params.containsKey(FOLDER_LEFT) && params.containsKey(FOLDER_RIGHT)) {
            FolderComparator comparator = new FolderComparator(params.get(FOLDER_LEFT), params.get(FOLDER_RIGHT));
            comparator.checkFolders();
            ArrayList<FolderComparator.FileComparatorItem> result = comparator.getResult();
            System.out.println("**************************************************************************");
            for (FolderComparator.FileComparatorItem fileComparatorItem : result) {
                if(!fileComparatorItem.isEqual()){
                System.out.println(fileComparatorItem);
                }
            }
            System.out.println("**************************************************************************");
            return "errors :: noParamsErrorFragment";
        } else {
            return "errors :: noParamsErrorFragment";
        }

    }

//    private void compareFolders(String leftFolder, String rightFolder) {
//        ArrayList<File> leftList = listFiles(leftFolder);
//        ArrayList<File> rightList = listFiles(rightFolder);
//        TreeSet<File> folders = Stream.concat(leftList.stream(), rightList.stream())
//                .filter(file -> file.isDirectory())
//                .collect(
//                        Collectors.toCollection(
//                                () -> new TreeSet<>(
//                                        Comparator.comparing(File::getName)
//                                )
//                        ));
//        TreeSet<File> files = Stream.concat(leftList.stream(), rightList.stream())
//                .filter(file -> file.isFile())
//                .collect(
//                        Collectors.toCollection(
//                                () -> new TreeSet<>(
//                                        Comparator.comparing(File::getName)
//                                )
//                        ));
//    }
}
