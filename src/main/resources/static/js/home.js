/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var LEFT = "left";
var RIGHT = "right";
var lockDirs = false;
var syncBrowsing = true;
var actionSide = LEFT;

$(document).ready(function () {
    addCompareHandler();
    addListingHandler();
    addUpHandler();
    addTogglerHandlers();
    addKeyHandlers();
});

function addCompareHandler() {
    $("#dirCompare").click(function () {
        var data = {};
//        data["folder_left"] = "C:\\netBeansProjects\\5rob\\weatherguidance\\hailintel\\demo\\";
//        data["folder_right"] = "C:\\netBeansProjects\\5rob\\weatherguidance\\hailintel\\demo1\\";
        data["folder_left"] = $("#leftPane button.root-button").text();
        data["folder_right"] = $("#rightPane button.root-button").text();
        if (data["folder_left"] === "Root" || data["folder_right"] === "Root") {
              showAlert("Root comparison not allowed");
            return;
        }
        if (data["folder_left"] ===  data["folder_right"]) {
              showAlert("Nothing to compare");
            return;
          
        }
        postData("compare", doNothing, data, "side");
    });
}

function doNothing() {

}

function addKeyHandlers() {
    $(document).keyup(function (e) {
        if (e.key === "Escape") { // escape key maps to keycode `27`
            $("button.list-button").parent().show();
        } else {
            var firstLetter = String.fromCharCode(e.keyCode).toLowerCase();
            console.log(firstLetter);
            $("button.list-button").each(function () {
                console.log($(this).text().toLowerCase());
                console.log($(this).text().toLowerCase().startsWith(firstLetter));
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
        syncBrowsing = $(this).prop('checked');
    });
    $("#lockDirs").change(function () {
        lockDirs = !$(this).prop('checked');
        if (lockDirs) {
            $("#synchronized").bootstrapToggle('on');
        }
    });
}

function addListingHandler() {
    $(".list-button").unbind('click');
    $(".list-button").click(function () {
        var data = {};
        var type = $(this).data("type");
        if (type === 'file') {
            return;
        }
        var folderName = $(this).text();
        var side = $(this).parent().parent().data("side");
        actionSide = side;
        data["folder"] = $(this).data("path");
        data["lock"] = lockDirs;
        data["side"] = side;
        postData("list", replaceListing, data, side);
        if (syncBrowsing) {
            side = getOtherSide(side);
            if ($("#" + side + "Pane button.root-button").text() !== "Root" && $("#" + side + "Pane button.list-button:contains('" + folderName + "')").length === 0) {
                $("#synchronized").bootstrapToggle('off');
                return;
            }
            data["folder"] = $("#" + side + "Pane button.root-button").text() === "Root" ? $(this).text() : $("#" + side + "Pane button.root-button").text() + folderName;
            data["lock"] = lockDirs;
            data["side"] = side;
            postData("list", replaceListing, data, side);
        }
    });
}
function addUpHandler() {
    $(".up-button").unbind('click');
    $(".up-button").click(function () {
        var data = {};
        var side = $(this).parent().parent().data("side");
        data["folder"] = $("#" + side + "Pane button.root-button").attr('id');
        data["lock"] = lockDirs;
        data["side"] = side;
        postData("up", replaceListing, data, side);
        if (syncBrowsing) {
            side = getOtherSide(side);
            data["folder"] = $("#" + side + "Pane button.root-button").attr('id');
            data["lock"] = lockDirs;
            data["side"] = side;
            postData("up", replaceListing, data, side);
        }
    });
}

function replaceListing(side, html) {

    $("#" + side + "Pane").html(html);
//    if (syncBrowsing && !lockDirs) {
//        $("#" + getOtherSide(side) + "Pane").html(html);
//    }
    addListingHandler();
    addUpHandler();
    $("html, body").animate({scrollTop: 0}, "fast");
    if (actionSide !== side &&
            $("#leftPane button.root-button").text() === $("#rightPane button.root-button").text()) {
        $("#lockDirs").bootstrapToggle('on');
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
function showAlert(text) {
var $alert=$('<div class="alert alert-danger alert-dismissible fade show">'
+text+
'<button type="button" class="close" data-dismiss="alert">&times;</button></div>');
    $("body").prepend($alert);
}
