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

import lombok.Data;

/**
 * Helper class defining the structure of a single line in a file diff view It
 * contains all the data required to visually present the file contents
 * differences
 *
 * @author Dejan Stojanovic
 */
@Data
public class DiffLine {

    // Add type of difference - a portion of a file was added in the other
    public static final int ADD = 1;
    // Change type of difference - a portion of a file was changed in the other
    public static final int CHANGE = 2;
    // Delete type of difference - a portion of a file was removed in the other
    public static final int DELETE = 0;
    //default class for same(unchanged) lines
    String className = "bg-secondary";
    //mark if the difference contains deleted line
    boolean deletedLine = false;
    //set the difference number in order to visually join the changed parts of the two files
    int differenceNumber = -1;
    //set the difference type used for coloring the differences based on the type
    int differenceType = -1;
    //set the difference number only for empty/deleted parts in order to visually join the changed parts of the two files
    int emptyDifferenceNumber = -1;
    //line number in the file
    int lineNumber = -1;
    //line contents    //line contents
    String line = "";

    /**
     * Constructor accepting the line contents
     *
     * @param line text of a single line in the file
     * @param lineNum line number in the file
     */
    public DiffLine(String line, int lineNum) {
        this.line = line;
        lineNumber=lineNum;
    }

    /**
     * Retrieve the class for the deleted line, visually represented by line of
     * the appropriate color. It is only used if the difference includes deleted
     * first line
     *
     * @param type difference type value
     * @return class name to color the line marking the deleted text line
     */
    public static String getEmptyClassName(int type) {
        String classEmpty = type == 0 ? "red" : "green";
        return classEmpty + " diff_0";
    }

    /**
     * Specify the deleted line of text and the difference number
     *
     * @param diffNum index of the determined difference
     */
    public void addEmpty(int diffNum) {
        deletedLine = true;
        emptyDifferenceNumber = diffNum;
    }

    /**
     * Get the css class name containing the difference number and appropriate
     * Bootstrap button class used for coloring of the text lines
     *
     * @return Bootstrap button class for coloring and diff_x class, where x is
     * the difference number
     */
    public String getClassName() {
        return className + (differenceNumber == -1 ? "" : " diff_" + differenceNumber);
    }
    /**
     * Get the css class name containing the difference number and appropriate
     * Bootstrap button class used for coloring of the text lines
     *
     * @return Bootstrap button class for coloring and diff_x class, where x is
     * the difference number
     */
    public String getLineNumberClassName() {
        return (differenceNumber == -1 ? "" : " diff_" + differenceNumber);
    }

    /**
     * Get the css class name containing the difference number and class name
     * for coloring of lines marking the deleted text lines on the left pane
     *
     * @return class for <hr> coloring and diff_x class, where x is the
     * difference number
     */
    public String getEmptyClassName() {
        String classEmpty = differenceType == 0 ? "red" : "green";
        return classEmpty + (emptyDifferenceNumber == -1 ? "" : " diff_" + emptyDifferenceNumber);
    }

    /**
     * Get the inverted css class name containing the difference number and
     * class name for coloring of lines marking the deleted text lines on the
     * right pane
     *
     * @return class for <hr> coloring and diff_x class, where x is the
     * difference number
     */
    public String getEmptyClassNameReversed() {
        String classEmpty = differenceType == 0 ? "green" : "red";
        return classEmpty + (emptyDifferenceNumber == -1 ? "" : " diff_" + emptyDifferenceNumber);
    }

    /**
     * Set the parameters based on the determined difference
     *
     * @param type difference type - ADD, CHANGE, DELETE
     * @param diffNum index of the determined difference
     */
    public void setDiffData(int type, int diffNum) {
        differenceNumber = diffNum;
        differenceType = type;
        switch (differenceType) {
            case 0:
                className = "btn-danger";
                break;
            case 1:
                className = "btn-success";
                break;
            case 2:
                className = "btn-info";
                break;
            default:
                className = "bg-secondary";
        }
    }
}
