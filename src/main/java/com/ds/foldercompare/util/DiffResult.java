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

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.netbeans.api.diff.Difference;

/**
 * Resulting data for the compared files
 *
 * @author Dejan Stojanovic
 */
@Data
public class DiffResult {
    //list of difference types used for output formatting

    ArrayList<Integer> diffTypes = new ArrayList<>();
    //array of difference objects containing the result of file comparison
    Difference[] diffs = new Difference[0];
    //Indicate if the first difference should indicate empty line on the left/right respectively
    boolean firstLeftEmpty = false;
    boolean firstRightEmpty = false;
    //formated lines for output 
    List<DiffLine> leftFile;
    List<DiffLine> rightFile;

    /**
     * Constructor accepting array of differences
     *
     * @param diffs array of determined differences between left and right file
     */
    public DiffResult(Difference[] diffs) {
        this.diffs = diffs;
        leftFile = new ArrayList<>();
        rightFile = new ArrayList<>();
    }
}
