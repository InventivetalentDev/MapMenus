var JavaColor = Java.type("java.awt.Color");
var Font = Java.type("java.awt.Font");
var Bukkit = Java.type("org.bukkit.Bukkit");
var Runtime = Java.type("java.lang.Runtime");

var MEGABYTE = 1048576;
var PADDING_SIDE = 16;
var maxMemory;

function init() {
    options.tickSpeed = 10;

    maxMemory = Runtime.getRuntime().maxMemory() / MEGABYTE;
}


var ticks = 0;

function tick() {
    if (ticks > 1) {
        renderer.render();
    }
    if (ticks > 10) {
        renderer.refresh();
        ticks = 0;
    }

    maxMemory = (Runtime.getRuntime().maxMemory() / MEGABYTE);

    var usedMemory = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEGABYTE);
    data.putArray("time-mem", usedMemory, 60000 /*1 minute*/ );

    ticks++;
}


// From http://minecraft.gamepedia.com/Map_item_format
var colorGray = new JavaColor(162, 166, 182);
var colorLime = new JavaColor(125, 202, 25);
var colorGreen = new JavaColor(0, 123, 0);
var colorOrange = new JavaColor(213, 125, 50);
var colorRed = new JavaColor(252, 0, 0);

function render(graphics) {
    var bounds = menu.getBounds(); // Get the menu bounds
    graphics.setColor(JavaColor.WHITE);
    graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height); // Make the background white

    // Center background gray
    graphics.setColor(colorGray);
    graphics.fillRect(bounds.x + PADDING_SIDE, bounds.y + PADDING_SIDE, bounds.width + 1 - PADDING_SIDE * 2, bounds.height - PADDING_SIDE * 2);


    graphics.setColor(JavaColor.black);
    var memArray = data.getArray("time-mem");
    if (memArray === null) return;
    //    print(tpsArray.length);

    var lastX = -1;
    var lastY = -1;


    var scaleWidth = (bounds.width - (PADDING_SIDE * 2)) / memArray.length;
    var scaleHeight = (bounds.height - (PADDING_SIDE * 2)) / maxMemory;

    // Draw grid
    var distance = maxMemory / 4;
    graphics.setColor(JavaColor.gray);
    var heightTwenty = Math.round(bounds.height - ((distance * 4) * scaleHeight + PADDING_SIDE));
    var heightFifteen = Math.round(bounds.height - ((distance * 3) * scaleHeight + PADDING_SIDE));
    var heightTen = Math.round(bounds.height - ((distance * 2) * scaleHeight + PADDING_SIDE));
    var heightFive = Math.round(bounds.height - ((distance * 1) * scaleHeight + PADDING_SIDE));
    graphics.drawLine(PADDING_SIDE, heightTwenty, bounds.width - PADDING_SIDE, heightTwenty);
    graphics.drawLine(PADDING_SIDE, heightFifteen, bounds.width - PADDING_SIDE, heightFifteen);
    graphics.drawLine(PADDING_SIDE, heightTen, bounds.width - PADDING_SIDE, heightTen);
    graphics.drawLine(PADDING_SIDE, heightFive, bounds.width - PADDING_SIDE, heightFive);
    // + Labels
    graphics.drawString("100%", Math.round(PADDING_SIDE), Math.round(heightTwenty));
    graphics.drawString("75%", Math.round(PADDING_SIDE), Math.round(heightFifteen));
    graphics.drawString("50%", Math.round(PADDING_SIDE), Math.round(heightTen));
    graphics.drawString("25%", Math.round(PADDING_SIDE), Math.round(heightFive));

    for (var i = 0; i < memArray.length; i++) {
        var j = Math.round(i * scaleWidth + PADDING_SIDE);
        var timeMem = memArray[i];
        if (timeMem === null) continue;

        var memPixel = Math.round(bounds.height - (timeMem * scaleHeight + PADDING_SIDE));

        if (memPixel < distance) {
            graphics.setColor(colorLime);
        }
        else if (memPixel < distance * 2) {
            graphics.setColor(colorGreen);
        }
        else if (memPixel < distance * 3) {
            graphics.setColor(colorOrange);
        }
        else {
            graphics.setColor(colorRed);
        }

        if (lastX == -1 && lastY == -1) {
            lastX = j;
            lastY = memPixel;
        }

        graphics.drawLine(lastX, lastY, lastX = j, lastY = memPixel);
    }

    graphics.setFont(new Font("Arial", 0, 9));
    graphics.setColor(JavaColor.black);
    graphics.drawString("" + Math.round(((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEGABYTE)) + " mb", bounds.width - (PADDING_SIDE * 3), lastY + 16);
}