var JavaColor = Java.type("java.awt.Color");
var BukkitSound = Java.type("org.bukkit.Sound");

var text;

function init(t) {
    this.text = t;
}

function click(player) {
    // Store the player's click
    states.put("clicked", player, 250);

    player.playSound(player.getLocation(), BukkitSound.UI_BUTTON_CLICK, 0.5, 1.0);
}

function render(graphics, player) {
    var bounds = component.getBounds();
    var cursor = menu.getCursorPosition(player);

    if(states.get("clicked", player)) {
        graphics.setColor(new JavaColor(37, 67, 207));
    } else {
        if(cursor !== null && bounds.contains(cursor)) {
            graphics.setColor(new JavaColor(126, 136, 191));
        } else {
            graphics.setColor(new JavaColor(117, 117, 117));
        }
    }
    graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

    graphics.setColor(new JavaColor(0, 0, 0));
    graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

    if(text !== undefined) {
        menu.renderer.drawStringCentered(graphics, bounds, text);
    }
}