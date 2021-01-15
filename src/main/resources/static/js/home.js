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
/* global diffCount, diffTypes, extensions, sizeLimit, sizeLimitString */


//define string constants
const LEFT = "left";
const RIGHT = "right";
const SIDE = "side";
const FILE = "file";
const FOLDER = "folder";
const PATH = "path";
const FILE_SIZE = "file_size";
const TYPE = "type";
const ROOT = "Root";
const ENCODING = "encoding";
const ENCODING_LEFT = "encoding_left";
const ENCODING_RIGHT = "encoding_right";
const EMPTY = "-";
const FILE_COMPARE = "file_compare";
const FOLDER_LEFT = "folder_left";
const FOLDER_RIGHT = "folder_right";
const FILE_LEFT = "file_left";
const FILE_RIGHT = "file_right";
const FOLDER_COMPARE = "folder_compare";
const SYNCHRONIZED = "synchronized";
var synchronizedBrowsing = true;
var cookieData = {};
var today = new Date();
var expires = new Date(today.getFullYear() + 30, today.getMonth(), today.getDate() + 1, 0, 0, 0, 0);

$(document).ready(function () {
    //all handlers are separated into functions for better organization
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
//If the window size changes, we need to resize the svg element which is used to draw difference polygons
$(window).resize(function () {
    scaleSVG();
});
/**
 * Scale the svg element to fit the document
 * It is positioned as absolute to match perfectly the elements which are connected to display the differences
 */
function scaleSVG() {
    var docWidth = $(window).width();
    var docHeight = $(document).height();
    $("#connectors").css("width", docWidth + "px");
    $("#connectors").css("height", docHeight + "px");
    if ($("#connectors polygon").length > 0) {
        setDiffPolys();
    }
}
/**
 * Handle svg element visibility depending on the selected tab
 */
function addTabHandler() {
    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        if ($(e.target).attr('href') === "#files") {
            $("#connectors").show();
        } else {
            $("#connectors").hide();
        }
    });
}
/**
 * Simple scroll to top of the page element
 */
function addScrollTopHandler() {
    $("#pageUp").click(function () {
        $("html, body").animate({scrollTop: 0}, "fast");
    });
}
/**
 * Handle file actions
 */
function addFileHandler() {
    /**
     * Display selected file contents
     */
    $("button.showFile").click(function () {
        var data = {};
        var side = $(this).data(SIDE);
        data[FILE] = $("#" + side + "FilePane button.filename-button").text();
        data[ENCODING] = $("#" + side + "EncodingSelectButtonText").text();
        if (data[FILE] === EMPTY) {
            showTimedAlert("noFilePath", "File not selected", 'danger');
            return;
        }
        postData("showFile", replaceFile, data, side);
        resetDiff();
    });
    $("#leftEncodingSelection a").click(function () {
        $("#leftEncodingSelectButtonText").text($(this).text());
    });
    $("#rightEncodingSelection a").click(function () {
        $("#rightEncodingSelectButtonText").text($(this).text());
    });
}
/**
 * Handle folder/file compare actions 
 */
function addCompareHandler() {
    /**
     * Compare two selected directories and display the results
     */
    $("#dirCompare").click(function () {
        var data = {};
        data["folder_left"] = $("#leftPane button.root-button").text();
        data["folder_right"] = $("#rightPane button.root-button").text();
        if (data["folder_left"] === "Root" || data["folder_right"] === "Root") {
            showTimedAlert("noRootCompare", "Root comparison not allowed", 'danger');
            return;
        }
        if (data["folder_left"] === data["folder_right"]) {
            showTimedAlert("nothingToCompare", "Nothing to compare - same dir", 'danger');
            return;

        }
        postData(FOLDER_COMPARE, replaceCompared, data, "side");
    });
    /**
     * Compare two selected files and display the results
     */
    $("#fileCompare").click(function () {
        var data = {};
        data[FILE_LEFT] = $("#leftFilePane button.filename-button").text();
        data[FILE_RIGHT] = $("#rightFilePane button.filename-button").text();
        data[ENCODING_LEFT] = $("#leftEncodingSelectButtonText").text();
        data[ENCODING_RIGHT] = $("#rightEncodingSelectButtonText").text();
        if (data[FILE_LEFT] === EMPTY || data[FILE_RIGHT] === EMPTY) {
            showTimedAlert("noFileCompare", "Both files must be selected", 'danger');
            return;
        }

        postData(FILE_COMPARE, replaceComparedFile, data, "");
    });
}
/**
 * Add keyboard shortcuts for filtering the list of files by first letter and reseting the filter with "Escape" key
 */
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
/**
 * Set the synchronized flag for synchronized browsing through both selected folders.
 * Flag is saved in a cookie for future usage.
 */
function addTogglerHandlers() {
    $("#synchronized").change(function () {
        synchronizedBrowsing = $(this).prop('checked');
        saveState();
    });
}
/**
 * Set the file/folder listing handlers
 */
function addListingHandler() {
    /**
     * Reset the click handler on listed file/folder items
     * Click on folder item shows the listing of the selected folder.
     * Click on file item checks if the file extension and size is allowed and sets the parameters on the "Files" tab.
     * Alert message is displayed if the file is not allowed for this action.
     */
    $(".list-button").unbind('click');
    $(".list-button").click(function () {
        var type = $(this).data(TYPE);
        var path = $(this).data(PATH);
        var side = $(this).parent().parent().data(SIDE);
        if (type === FILE) {
            var checkData = isFilePermitted($(this));
            if (!checkData.extensionValid) {
                showTimedAlert("fileExtensionAlert", "File extension not permitted.  Only " + JSON.stringify(extensions) + " extensions are permitted!", 'danger');
                return;
            }
            if (!checkData.sizeValid) {
                showTimedAlert("fileSizeAlert", "File size too large.  Maximum size is (" + sizeLimitString + ")!", 'danger');
                return;
            }
            $("#" + side + "FilePane button.filename-button").text(path);
            $("#" + side + "FileContents div.line-group").remove();
            $("#" + side + "FileContents hr").remove();
            resetDiff();
            showTimedAlert("fileSet", "File selected on the " + side + " side", 'primary');
        } else {
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
        }
    });
    /**
     * Set the hover icon to diff icon only for files that are valid by extension and size
     */
    $(".list-button").unbind('hover');
    $(".list-button[data-type='file']").hover(
            function () {
                var checkData = isFilePermitted($(this));
                if (checkData.extensionValid && checkData.sizeValid) {
                    $(this).css('cursor', "url('../img/diff.png'), pointer");
                } else {
                    $(this).css('cursor', 'pointer');
                }
            },
            function () {
                $(this).css('cursor', 'pointer');
            }
    );
}
/**
 * Check if the file extension and size are allowed
 * @param $fileElement ".list-button" element which contains all the file parameters
 * @returns object containing flags for extension and size validation
 */
function isFilePermitted($fileElement) {
    var data = {};
    var fileExt = $($fileElement).data(PATH).split('.').pop();
    var fileSize = $($fileElement).data(FILE_SIZE);
    data.extensionValid = extensions.length === 0 || extensions.includes(fileExt);
    data.sizeValid = sizeLimit > parseInt(fileSize);
    return data;
}
/**
 * Action function to prepare and send AJAX request for folder listing
 * @param  dir absolute path of the directory to list (or ROOT for root listing)
 * @param  side pane side where the listing should be displayed
 */
function loadDir(dir, side) {
    if (dir === ROOT) {
        return;
    }
    var data = {};
    data[FOLDER] = dir;
    data[SIDE] = side;
    postData("list", replaceListing, data, data[SIDE]);
}
/**
 * Handle parent directory listing when the user clicks on the up arrow next to the directory path
 */
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

/**
 * Action handler which is called on the AJAX response for directory listing.
 * It replaces the listing pane on the web page and saves the parameters to the cookie.
 * @param side define which listing should be replaced (LEFT or RIGHT)
 * @param html response containing new file/folder listing 
 */
function replaceListing(side, html) {

    $("#" + side + "Pane").html(html);
    addListingHandler();
    addDirUpHandler();
    $("html, body").animate({scrollTop: 0}, "fast");
    saveState();
}

/**
 * Action handler which is called on the AJAX response for compared directory listing.
 * It replaces the both listing panes on the web page and shows marked differences.
 * @param side not used, but remained to preserve the same action function signature
 * @param html response containing new compared file/folder listing 
 */
function replaceCompared(side, html) {

    $("#listing-data").html(html);
    addListingHandler();
    addDirUpHandler();
    $("html, body").animate({scrollTop: 0}, "fast");
}
/**
 * Action handler which is called on the AJAX response for displaying the file contents. 
 * Alert message is displayed in case of error.
 * @param side define the side on which the file is to be displayed
 * @param html file contents as the list of lines
 */
function replaceFile(side, html) {
    if (html.search("class='error'") !== -1) {
        showTimedAlert("errorCompareAlert", html, 'danger');
    } else {
        $("#" + side + "FileContents button").remove();
        $("#" + side + "FileContents").html(html);
        scaleSVG();
        saveState();
    }
}
/**
 * Action handler which is called on the AJAX response for displaying the compared file contents. 
 * Upon loading the html contents, svg polygons are drawn to connect the individual differences on both files.
 * Alert message is displayed in case of error.
 * @param side not used, but remained to preserve the same action function signature
 * @param html formatted output containing both file contents with marked differences on the compared files
 */
function replaceComparedFile(side, html) {
    if (html.search("class='error'") !== -1) {
        showTimedAlert("errorCompareAlert", html, 'danger');
    } else {
        $("#filePane").html(html);
        scaleSVG();

//    setDiffLines();
        setDiffPolys();
    }
}

function scaleLines() {

}

/**
 * Simple function to negate the pane side
 * @param side to be negated
 * @returns the other side in regards to the parameter 
 */
function getOtherSide(side) {
    if (side === LEFT) {
        return RIGHT;
    } else {
        return LEFT;
    }
}
/**
 * Universal AJAX function for sending POST request and assigning a response handler
 * @param endpoint endpoint target for POST request
 * @param action action function which handles the response data
 * @param data request parameters object
 * @param side the pane side which is forwarded to the action function for handling the response
 */
function postData(endpoint, action, data, side) {
    $.ajax({
        type: "POST",
        url: endpoint,
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(data),
        success: function (response) {
            action(side, response);
        }
    });
}

/**
 * Display alert message
 * @param id alert element id used to prevent multiple alerts for the same message
 * @param text message to display 
 * @param alertType the type of the alert to display (Bootstrap alert types)
 */

function showTimedAlert(id, text, alertType) {
    alertType = alertType === undefined ? 'info' : alertType;
    if ($("#" + id).length > 0) {
        $("#" + id).remove();
    }
    var $alert = $('<div id="' + id + '" class="popupAlert alert alert-' + alertType + ' alert-dismissible fade show">'
            + text +
            '<button type="button" class="close" data-dismiss="alert">&times;</button></div>');
    $("body").prepend($alert);
    setTimeout(function () {
        $("#" + id).alert('close');
    }, 1500);
}
/**
 * Save the selected folder paths in a cookie for future use
 */

function saveState() {
    cookieData[FOLDER_LEFT] = $("#leftPane button.root-button").text();
    cookieData[FOLDER_RIGHT] = $("#rightPane button.root-button").text();
    cookieData[SYNCHRONIZED] = synchronizedBrowsing;
    setCookie(FOLDER_COMPARE, JSON.stringify(cookieData));              //save variables as JSON
}
/**
 * load variables from the cookie and list the folders 
 */
function loadCookieData() {
    var data = getCookie(FOLDER_COMPARE);
    if (data !== null) {
        cookieData = JSON.parse(data);
        loadDir(cookieData[FOLDER_LEFT], LEFT);
        loadDir(cookieData[FOLDER_RIGHT], RIGHT);
        synchronizedBrowsing = cookieData[SYNCHRONIZED];
        $("#synchronized").bootstrapToggle(synchronizedBrowsing ? 'on' : 'off');
    }
}
/**
 * Load cookie data via js-cookie lib
 * @param key cookie name
 * @returns cookie data or null if it doesn't exist
 */
//retreive cookie by key
function getCookie(key) {
    return Cookies.get(key);
}
/**
 * Read cookie data via js-cookie lib
 * @param  cookieId cookie name/ID
 * @param  cookieData data to be saved in the cookie
 */

//set cookie to expire after 30 years
function setCookie(cookieId, cookieData) {
    Cookies.set(cookieId, cookieData, {expires: 365});
}
/**
 * Add svg polygon connector which connects the same difference on both panes
 * @param id element id
 * @param pointList list of absolute coordinates which define the polygon
 * @param strokeColor the color of the polygon line
 * @param fillColor the color of the polygon fill
 */
function addPolygon(id, pointList, strokeColor, fillColor) {
    $(document.createElementNS('http://www.w3.org/2000/svg', 'polygon'))
            .attr({
                id: id,
                points: pointList,
                "stroke": strokeColor,
                "fill": fillColor
            })
            .appendTo("#connectors");
}
/**
 * Remove all svg polygons. Used to reset the diff view on new file or comparison.
 */
function resetDiff() {
    $("#connectors polygon").remove();
}
/**
 * Iterate through all found differences, determine the connecting points of the diff elements and 
 * call a function to draw connectors
 */
function setDiffPolys() {
    var leftCoord, rightCoord;
    resetDiff();
    for (var i = 0; i < diffTypes.length; i++) {
        leftCoord = getDiffCoords(i, LEFT);
        rightCoord = getDiffCoords(i, RIGHT);
        addPolygon("diff" + i,
                (leftCoord.xFirst + "," + leftCoord.yFirst + " " + rightCoord.xFirst + "," + rightCoord.yFirst + " "
                        + rightCoord.xLast + "," + rightCoord.yLast + " " + leftCoord.xLast + "," + leftCoord.yLast)
                , getDiffColor(diffTypes[i]), getDiffColor(diffTypes[i]));
    }
}

/**
 * Get the edge coordinates of diff elements which should be joined by connectors
 * @param diffNum difference index which is used to match the elements on both sides
 * @param side pane side to check the element coordinates. 
 * @returns object containing the upper right and lower right coordinate of the matched elements on the left pane.
 * Or upper left and lower left coordinate of the matched elements on the right pane, depending on the pane side passed as parameter.
 */
function getDiffCoords(diffNum, side) {
    var radius = convertRemToPixels(0.2);
    var firstRadius, lastRadius;
    var diffElements = $("#" + side + "FilePane .svgConnection.diff_" + diffNum);
//    var diffElements = $("#" + side + "FilePane .diff_" + diffNum);
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
/**
 * Function which converts rem unit to pixel unit
 * @param rem size expressed in rem unit
 * @returns size expressed in pixels
 */
function convertRemToPixels(rem) {
    return rem * parseFloat(getComputedStyle(document.documentElement).fontSize);
}
/**
 * Get the appropriate diff connector color based on the difference type
 * @param type difference type
 * @returns hex color that matches diff elements
 */
function getDiffColor(type) {
    switch (type) {
        case 0:                     //DELETE
            return '#dc3545';
            break;
        case 1:                     //ADD
            return '#218838';
            break;
        case 2:                     //CHANGE
            return '#138496';
            break;

        default:
            return '#999';
            break;
    }
}