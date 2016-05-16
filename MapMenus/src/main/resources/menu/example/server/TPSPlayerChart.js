var JavaColor = Java.type("java.awt.Color");
var Font = Java.type("java.awt.Font");
var Bukkit = Java.type("org.bukkit.Bukkit");
var MinecraftServer;

var PADDING_SIDE = 16;

function init() {
    // Initialize this here, as we don't have access to the "providers" yet above
    MinecraftServer = Java.type(providers.get("Reflection").getNMSClass("MinecraftServer"));

    options.tickSpeed = 10;
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

    data.putArray("time-tps", MinecraftServer.getServer().recentTps[0], 300000 /*5 minutes*/ );
    data.putArray("time-pcount", Bukkit.getOnlinePlayers().size(), 300000);

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
    var pCountArray = data.getArray("time-pcount");
    if (tpsArray === null || pCountArray === null) return;

    var lastX = -1;
    var lastXPlayers = -1;
    var lastY = -1;
    var lastYPlayers = -1;

    var scaleWidth = (bounds.width - (PADDING_SIDE * 2)) / (Math.min(tpsArray.length, pCountArray.length));
    var scaleHeight = (bounds.height - (PADDING_SIDE * 2)) / 22;
    var scaleHeightPlayers = (bounds.height - (PADDING_SIDE * 2)) / (Bukkit.getMaxPlayers() + 1);

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
    graphics.drawString("100%", Math.round(PADDING_SIDE), Math.round(heightTwenty));
    graphics.drawString("75%", Math.round(PADDING_SIDE), Math.round(heightFifteen));
    graphics.drawString("50%", Math.round(PADDING_SIDE), Math.round(heightTen));
    graphics.drawString("25%", Math.round(PADDING_SIDE), Math.round(heightFive));

    var lastTPS = 0;
    var lastCount = 0;
    for (var i = 0; i < Math.min(tpsArray.length, pCountArray.length); i++) {
        var j = Math.round(i * scaleWidth + PADDING_SIDE);
        var timeTPS = tpsArray[i];
        var timeCount = pCountArray[i];
        if (timeTPS === null || timeCount === null) continue;

        var tpsPixel = Math.round(bounds.height - (timeTPS * scaleHeight + PADDING_SIDE));
        var countPixel = Math.round(bounds.height - (timeCount * scaleHeightPlayers + PADDING_SIDE));

        if (timeTPS >= 19.5) {
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
            lastYPlayers = countPixel;
        }

        graphics.drawLine(lastX, lastY, lastX = j, lastY = tpsPixel);

        graphics.setColor(JavaColor.blue);
        graphics.drawLine(lastX, lastYPlayers, lastX = j, lastYPlayers = countPixel);

        lastTPS = timeTPS;
        lastCount = timeCount;
    }

    graphics.setFont(new Font("Arial", 0, 9));
    graphics.setColor(JavaColor.black);
    graphics.drawString("" + Math.round(lastTPS) + " tps", bounds.width - (PADDING_SIDE * 3), lastY + 16);
    graphics.drawString("" + lastCount + " players", bounds.width - (PADDING_SIDE * 3), lastYPlayers - 8);
}
