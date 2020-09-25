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
 *
 * @author Dejan Stojanovic
 */
@Data
public class DiffLine {

    /**
     * Delete type of difference - a portion of a file was removed in the other
     */
    public static final int DELETE = 0;

    /**
     * Add type of difference - a portion of a file was added in the other
     */
    public static final int ADD = 1;

    /**
     * Change type of difference - a portion of a file was changed in the other
     */
    public static final int CHANGE = 2;
    String line = "";
    int differenceType = -1;
    int differenceNumber = -1;
    int emptyDifferenceNumber = -1;
    String className = "bg-secondary";
    boolean deletedLine = false;

    public DiffLine(String line) {
        this.line = line;
    }

    public DiffLine(boolean deleted) {
        deletedLine = deleted;
    }

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

    public String getClassName() {
        return className + (differenceNumber == -1 ? "" : " diff_" + differenceNumber);
    }

    public static String getEmptyClassName(int type) {
        String classEmpty = type == 0 ? "red" : "green";
        return classEmpty + " diff_0";
    }

    public String getEmptyClassName() {
        String classEmpty = differenceType == 0 ? "red" : "green";
        return classEmpty + (emptyDifferenceNumber == -1 ? "" : " diff_" + emptyDifferenceNumber);
    }
    public String getEmptyClassNameRev() {
        String classEmpty = differenceType == 0 ? "green": "red" ;
        return classEmpty + (emptyDifferenceNumber == -1 ? "" : " diff_" + emptyDifferenceNumber);
    }

    public void addEmpty(int diffNum) {
        deletedLine = true;
        emptyDifferenceNumber = diffNum;
    }
}
