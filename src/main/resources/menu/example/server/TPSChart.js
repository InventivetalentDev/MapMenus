var JavaColor = Java.type("java.awt.Color");
var Font = Java.type("java.awt.Font");
var MinecraftServer;

var PADDING_SIDE = 16;

function init() {
    // Initialize this here, as we don't have access to the "providers" yet above
    MinecraftServer = Java.type(providers.get("Reflection").getNMSClass("MinecraftServer"));

    options.tickSpeed = 10;

    renderer.getGraphics().setFont(new Font("Arial", 0, 12));
}

var ticks = 0;

function tick() {
    if (ticks > 1) {
        renderer.render();

        data.putArray("time-tps", MinecraftServer.getServer().recentTps[0], 300000 /*5 minutes*/ );
    }
    if (ticks > 10) {
        renderer.refresh();
        ticks = 0;
    }

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
    var tpsArray = data.getArray("time-tps");
    if (tpsArray === null) return;
    print(tpsArray.length);

    var lastX = -1;
    var lastY = -1;

    var scaleWidth = (bounds.width - (PADDING_SIDE * 2)) / (tpsArray.length - 1);
    var scaleHeight = (bounds.height - (PADDING_SIDE * 2)) / 22;

    graphics.setFont(new Font("Arial", 0, 12));
    // Draw grid
    graphics.setColor(JavaColor.gray);
    var heightTwenty = Math.round(bounds.height - (20 * scaleHeight + PADDING_SIDE));
    var heightFifteen = Math.round(bounds.height - (15 * scaleHeight + PADDING_SIDE));
    var heightTen = Math.round(bounds.height - (10 * scaleHeight + PADDING_SIDE));
    var heightFive = Math.round(bounds.height - (5 * scaleHeight + PADDING_SIDE));
    graphics.drawLine(PADDING_SIDE, heightTwenty, bounds.width - PADDING_SIDE, heightTwenty);
    graphics.drawLine(PADDING_SIDE, heightFifteen, bounds.width - PADDING_SIDE, heightFifteen);
    graphics.drawLine(PADDING_SIDE, heightTen, bounds.width - PADDING_SIDE, heightTen);
    graphics.drawLine(PADDING_SIDE, heightFive, bounds.width - PADDING_SIDE, heightFive);
    // + Labels
    graphics.drawString("20", Math.round(PADDING_SIDE), Math.round(heightTwenty));
    graphics.drawString("15", Math.round(PADDING_SIDE), Math.round(heightFifteen));
    graphics.drawString("10", Math.round(PADDING_SIDE), Math.round(heightTen));
    graphics.drawString("5", Math.round(PADDING_SIDE), Math.round(heightFive));

    var lastTPS = 0;
    for (var i = 0; i < tpsArray.length; i++) {
        var j = Math.round(i * scaleWidth + PADDING_SIDE);
        var timeTPS = tpsArray[i];
        if (timeTPS == null) continue;

        var tpsPixel = Math.round(bounds.height - (timeTPS * scaleHeight + PADDING_SIDE));

        if (timeTPS >= 19.9) {
            graphics.setColor(colorLime);
        }
        else if (timeTPS >= 18.5) {
            graphics.setColor(colorGreen);
        }
        else if (timeTPS > 16) {
            graphics.setColor(colorOrange);
        }
        else {
            graphics.setColor(colorRed);
        }

        if (lastX == -1 && lastY == -1) {
            lastX = j;
            lastY = tpsPixel;
        }

        graphics.drawLine(lastX, lastY, lastX = j, lastY = tpsPixel);

        lastTPS = timeTPS;
    }

    graphics.setFont(new Font("Arial", 0, 9));
    graphics.setColor(JavaColor.black);
    graphics.drawString("" + lastTPS, bounds.width - PADDING_SIDE * 2, lastY + 16);
}
