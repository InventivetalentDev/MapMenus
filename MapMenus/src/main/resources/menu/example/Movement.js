var JavaColor = Java.type("java.awt.Color");
var JavaFont = Java.type("java.awt.Font");

function init() {
    options.tickSpeed = 1;
}

var ticks = 0;

function tick() {
    renderer.render();
    if (ticks++ > 10) {
        renderer.refresh();
        ticks = 0;
    }
}

function click(player) {
    states.toggle("tracking", player);
}

function render(graphics, player) {
    var bounds = menu.getBounds();
    graphics.setFont(new JavaFont("Arial", 0, Math.max(11, bounds.width / 16)));

    graphics.setColor(JavaColor.WHITE);
    graphics.fillRect(0, 0, menu.getBounds().width, menu.getBounds().height);
    if (states.get("tracking", player)) {
        var cursor = menu.getCursorPosition(player);
        if (cursor === undefined || cursor === null) {
            states.remove("tracking", player);
            return;
        }

        var direction = data.get("move-direction", player);
        var amount = data.get("move-amount", player);
        menu.requestMovementInput(player, function(dir, a) {
            if (dir !== undefined && dir !== null) {
                data.put("move-direction", player, dir);
                data.put("move-amount", player, a);
            }
        }, false);
        if (direction !== undefined && direction !== null) {
            graphics.setColor(JavaColor.black);
            renderer.drawStringCentered(graphics, Math.round((bounds.width / 2) - 32), Math.round((bounds.height / 2) - 32), 64, 16, direction);

            if (amount > 0) graphics.setColor(JavaColor.green);
            if (amount < 0) graphics.setColor(JavaColor.red);
            renderer.drawStringCentered(graphics, Math.round((bounds.width / 2) - 32), Math.round((bounds.height / 2) + 8), 64, 16, amount.toString());
            return;
        }
    }

    graphics.setColor(JavaColor.gray);
    renderer.drawStringCentered(graphics, Math.round((bounds.width / 2) - 32), Math.round((bounds.height / 2)), 64, 16, "Click me to start tracking...");
}
