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
/* global diffCount, diffTypes */


//define string constants
const LEFT = "left";
const RIGHT = "right";
const SIDE = "side";
const FILE = "file";
const FOLDER = "folder";
const PATH = "path";
const TYPE = "type";
const ROOT = "Root";
const EMPTY = "-";
const FILE_COMPARE = "file_compare";
const FOLDER_LEFT = "folder_left";
const FOLDER_RIGHT = "folder_right";
const FILE_LEFT = "file_left";
const FILE_RIGHT = "file_right";
const COOKIE_ID = "folder_compare";
const SYNCHRONIZED = "synchronized";
var synchronizedBrowsing = true;
var cookieData = {};
var today = new Date();
var expires = new Date(today.getFullYear() + 30, today.getMonth(), today.getDate() + 1, 0, 0, 0, 0);

$(document).ready(function () {
    addFileHandler();
    addCompareHandler();
    addListingHandler();
    addDirUpHandler();
    addTogglerHandlers();
    addTabHandler();
    addKeyHandlers();
    addScrollTopHandler();
    loadCookieData();
});

$(window).resize(function () {
    scaleSVG();
});

function scaleSVG() {
    var docWidth = $(window).width();
    var docHeight = $(document).height();
    $("#connectors").css("width", docWidth + "px");
    $("#connectors").css("height", docHeight + "px");
}

function addTabHandler() {
    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        if ($(e.target).attr('href') === "#files") {
            $("#connectors").show();
        } else {
            $("#connectors").hide();
        }
    });
}
function addScrollTopHandler() {
    $("#pageUp").click(function () {
        $("html, body").animate({scrollTop: 0}, "fast");
    });
}
function addFileHandler() {
    $("button.showFile").click(function () {
        var data = {};
        var side = $(this).data(SIDE);
        data[FILE] = $("#" + side + "FilePane button.filename-button").text();
        if (data[FILE] === EMPTY) {
            showTimedAlert("noFilePath", "File not selected");
            return;
        }

        postData("showFile", replaceFile, data, side);
        resetDiff();
    });
}
function addCompareHandler() {
    $("#dirCompare").click(function () {
        var data = {};
        data["folder_left"] = $("#leftPane button.root-button").text();
        data["folder_right"] = $("#rightPane button.root-button").text();
        if (data["folder_left"] === "Root" || data["folder_right"] === "Root") {
            showTimedAlert("noRootCompare", "Root comparison not allowed");
            return;
        }
        if (data["folder_left"] === data["folder_right"]) {
            showTimedAlert("nothingToCompare", "Nothing to compare - same dir");
            return;

        }
        postData("compare", replaceCompared, data, "side");
    });
    $("#fileCompare").click(function () {
        var data = {};
        data[FILE_LEFT] = $("#leftFilePane button.filename-button").text();
        data[FILE_RIGHT] = $("#rightFilePane button.filename-button").text();
        if (data[FILE_LEFT] === EMPTY || data[FILE_RIGHT] === EMPTY) {
            showTimedAlert("noFileCompare", "Both files must be selected");
            return;
        }

        postData(FILE_COMPARE, replaceComparedFile, data, "");
    });
}

function doNothing() {

}

function addKeyHandlers() {
    $(document).keyup(function (e) {

        if (e.key === "Escape") { // escape key maps to keycode `27`
            $("button.list-button").parent().show();
        } else if ((e.keyCode >= 48                  //48=0 0,1,2,3...9,a,b...y,z
                && e.keyCode <= 90)                 // 90=z
                || (e.keyCode >= 96                  //96=numpad 0
                        && e.keyCode <= 105)                 // 105=numpad 9
                || e.keyCode === 109                     //109=numpad -
                || e.keyCode === 173                     //173=_ and -
                ) {
            var firstLetter = String.fromCharCode(e.keyCode).toLowerCase();
            $("button.list-button").each(function () {
                if ($(this).text().toLowerCase().startsWith(firstLetter)) {
                    $(this).parent().show();
                } else {
                    $(this).parent().hide();
                }
            });
        }
    });
}
function addTogglerHandlers() {
    $("#synchronized").change(function () {
        synchronizedBrowsing = $(this).prop('checked');
        saveState();
    });
}

function addListingHandler() {
    $(".list-button").unbind('click');
    $(".list-button").click(function () {
        var type = $(this).data(TYPE);
        var path = $(this).data(PATH);
        var side = $(this).parent().parent().data(SIDE);
        if (type === FILE) {
            $("#" + side + "FilePane button.filename-button").text(path);
            $("#" + side + "FileContents button").remove();
            resetDiff();
            showTimedAlert("fileSet", "File selected on the " + side + " side");
            return;
        }
        if (path === EMPTY) {
            return;
        }
        var folderName = $(this).text();

        loadDir(path, side);

        if (synchronizedBrowsing) {
            side = getOtherSide(side);
            if ($("#" + side + "Pane button.root-button").text() !== ROOT && $("#" + side + "Pane button.list-button:contains('" + folderName + "')").length === 0) {
                $("#synchronized").bootstrapToggle('off');
                return;
            }
            path = $("#" + side + "Pane button.root-button").text() === ROOT ? $(this).text() : $("#" + side + "Pane button.root-button").text() + folderName;
            loadDir(path, side);
        }
    });
}

function loadDir(dir, side) {
    if (dir === ROOT) {
        return;
    }
    var data = {};
    data[FOLDER] = dir;
    data[SIDE] = side;
    postData("list", replaceListing, data, data[SIDE]);
}

function addDirUpHandler() {
    $(".up-button").unbind('click');
    $(".up-button").click(function () {
        var data = {};
        var side = $(this).parent().parent().data(SIDE);
        data[FOLDER] = $("#" + side + "Pane button.root-button").data('path');
        data[SIDE] = side;
        postData("up", replaceListing, data, side);
        if (synchronizedBrowsing) {
            side = getOtherSide(side);
            data[FOLDER] = $("#" + side + "Pane button.root-button").data('path');
            data[SIDE] = side;
            postData("up", replaceListing, data, side);
        }
    });
}

function replaceFile(side, html) {

    $("#" + side + "FileContents button").remove();
    $("#" + side + "FileContents").html(html);
    scaleSVG();
    saveState();
}
function replaceListing(side, html) {

    $("#" + side + "Pane").html(html);
    addListingHandler();
    addDirUpHandler();
    $("html, body").animate({scrollTop: 0}, "fast");
    saveState();
}

function replaceCompared(side, html) {

    $("#listing-data").html(html);
    addListingHandler();
    addDirUpHandler();
    $("html, body").animate({scrollTop: 0}, "fast");
}
function replaceComparedFile(side, html) {
    if ($(html).find('h3.error').length > 0) {
        showTimedAlert("errorCompareAlert", html);
    } else {
        $("#filePane").html(html);
        scaleSVG();
//    setDiffLines();
        setDiffPolys();
    }
}

function getOtherSide(side) {
    if (side === LEFT) {
        return RIGHT;
    } else {
        return LEFT;
    }
}

function postData(content, action, data, side) {
    $.ajax({
        type: "POST",
        url: content,
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(data),
        success: function (response) {
            action(side, response);
        }
    });
}

function getData(target, action) {
    $.ajax({
        type: "GET",
        url: target,
        async: false,
        success: function (response) {
            action(response);
        }
    });
}

function noAlert(html) {
}
function showAlert(id, text) {
    if ($("#" + id).length === 0) {
        var $alert = $('<div id="' + id + '" class="alert alert-danger alert-dismissible fade show">'
                + text +
                '<button type="button" class="close" data-dismiss="alert">&times;</button></div>');
        $("body").prepend($alert);
    }
}
function showTimedAlert(id, text) {
    if ($("#" + id).length === 0) {
        var $alert = $('<div id="' + id + '" class="popupAlert alert alert-info alert-dismissible fade show">'
                + text +
                '<button type="button" class="close" data-dismiss="alert">&times;</button></div>');
        $("body").prepend($alert);
        setTimeout(function () {
            $("#" + id).alert('close');
        }, 1000);
    }
}


function saveState() {
    cookieData[FOLDER_LEFT] = $("#leftPane button.root-button").text();
    cookieData[FOLDER_RIGHT] = $("#rightPane button.root-button").text();
    cookieData[SYNCHRONIZED] = synchronizedBrowsing;
    setCookie(COOKIE_ID, JSON.stringify(cookieData));              //save variables as JSON
}
//load variables from cookie or create new ones
function loadCookieData() {
    var data = getCookie(COOKIE_ID);                         //load cookie data
    if (data !== null) {                                    //if cookie exists
        cookieData = JSON.parse(data);                            //parse JSON created from variables and set the variables and data
        loadDir(cookieData[FOLDER_LEFT], LEFT);
        loadDir(cookieData[FOLDER_RIGHT], RIGHT);
        synchronizedBrowsing = cookieData[SYNCHRONIZED];
        $("#synchronized").bootstrapToggle(synchronizedBrowsing ? 'on' : 'off');
    }
}
//retreive cookie by key
function getCookie(key) {
    var keyValue = document.cookie.match('(^|;) ?' + key + '=([^;]*)(;|$)');            //regex for finding key
    //use decodeURIComponent to convert special characters to original
    return keyValue ? decodeURIComponent(keyValue[2]) : null;
}
//set cookie to expire after 30 years
function setCookie(key, value) {
    //use encodeURIComponent to escape special characters
    document.cookie = key + '=' + encodeURIComponent(value) + '; SameSite=None; Secure;expires=' + expires.toUTCString() + ';path=/';
}


function addLine(id, x1, y1, x2, y2, color) {
    $(document.createElementNS('http://www.w3.org/2000/svg', 'line'))
            .attr({
                id: id,
                x1: x1,
                y1: y1,
                x2: x2,
                y2: y2,
                "stroke": color,
                "stroke-width": "2px"
            })
            .appendTo("#connectors");
}
function addPolygon(id, x1, y1, x2, y2, x3, y3, x4, y4, strokeColor, fillColor) {
    $(document.createElementNS('http://www.w3.org/2000/svg', 'polygon'))
            .attr({
                id: id,
                points: x1 + "," + y1 + " " + x2 + "," + y2 + " " + x3 + "," + y3 + " " + x4 + "," + y4,

                "stroke": strokeColor,
                "fill": fillColor
            })
            .appendTo("#connectors");
}
function resetDiff() {
    $("#connectors polygon").remove();
//      $("#connectors line").remove();
}

function setDiffPolys() {
    var leftCoord, rightCoord;
    resetDiff();
    for (var i = 0; i < diffTypes.length; i++) {
        leftCoord = getDiffCoords(i, LEFT);
        rightCoord = getDiffCoords(i, RIGHT);
        addPolygon("diff" + i, leftCoord.xFirst, leftCoord.yFirst, rightCoord.xFirst, rightCoord.yFirst,
                rightCoord.xLast, rightCoord.yLast, leftCoord.xLast, leftCoord.yLast, getDiffColor(diffTypes[i]), getDiffColor(diffTypes[i]));
    }
}
function setDiffLines() {
    var leftCoord, rightCoord;
    resetDiff();
    for (var i = 0; i < diffTypes.length; i++) {
        leftCoord = getDiffCoords(i, LEFT);
        rightCoord = getDiffCoords(i, RIGHT);
        addLine("diffTop" + i, leftCoord.xFirst, leftCoord.yFirst, rightCoord.xFirst, rightCoord.yFirst, '#999');
        addLine("diffBottom" + i, leftCoord.xLast, leftCoord.yLast, rightCoord.xLast, rightCoord.yLast, '#999');
    }
}
function getDiffCoords(diffNum, side) {
    var radius = convertRemToPixels(0.2);
    var firstRadius, lastRadius;
    var diffElements = $("#" + side + "FilePane .diff_" + diffNum);
    var $first = $(diffElements).first();
    var $last = $(diffElements).last();
    firstRadius = $($first).is('hr') ? 0 : radius;
    lastRadius = $($last).is('hr') ? 0 : radius;
    if (side === LEFT) {
        return {'xFirst': $($first).offset().left + $($first).outerWidth(),
            'yFirst': $($first).offset().top + firstRadius,
            'xLast': $($last).offset().left + $($first).outerWidth(),
            'yLast': $($last).offset().top + $($first).outerHeight() - firstRadius
        };
    } else {
        return {'xFirst': $($first).offset().left,
            'yFirst': $($first).offset().top + lastRadius,
            'xLast': $($last).offset().left,
            'yLast': $($last).offset().top + $($first).outerHeight() - lastRadius
        };
    }
}

function convertRemToPixels(rem) {
    return rem * parseFloat(getComputedStyle(document.documentElement).fontSize);
}
function getDiffColor(type) {
    switch (type) {
        case 0:
            return '#dc3545';
            break;
        case 1:
            return '#218838';
            break;
        case 2:
            return '#138496';
            break;

        default:
            return '#999';
            break;
    }
}