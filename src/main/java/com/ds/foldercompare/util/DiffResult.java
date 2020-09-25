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
 *
 * @author Dejan Stojanovic
 */
@Data
public class DiffResult {

    boolean firstLeftEmpty = false;
    boolean firstRightEmpty = false;
    List<DiffLine> leftFile;
    List<DiffLine> rightFile;
    Difference[] diffs = new Difference[0];
    ArrayList<Integer> diffTypes = new ArrayList<>();

    public DiffResult(Difference[] diffs) {
        this.diffs = diffs;
        leftFile = new ArrayList<>();
        rightFile = new ArrayList<>();
    }
}
