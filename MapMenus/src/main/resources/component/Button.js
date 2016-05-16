// Button
// author: inventivetalent
({
    classes: {
        JavaColor: Java.type("java.awt.Color"),
        BukkitSound: Java.type("org.bukkit.Sound")
    },
    text: "",
    setText: function(text) {
        this.text = text;
    },
    getText: function() {
        return this.text;
    },
    init: function(text) {
        this.text = text;
    },
    click: function(player) {
        this.states.put("clicked", player, 250);
        player.playSound(player.getLocation(), this.classes.BukkitSound.UI_BUTTON_CLICK, 0.5, 1.0);

    },
    render: function(graphics, player) {
        var bounds = this.component.getBounds();
        var cursor = this.menu.getCursorPosition(player);

        if (this.states.get("clicked", player)) { // Button clicked
            graphics.setColor(new this.classes.JavaColor(37, 67, 207));
        }
        else {

            if (cursor !== null && bounds.contains(cursor)) { // Mouse hover
                graphics.setColor(new this.classes.JavaColor(126, 136, 191));
            }
            else { // Inactive
                graphics.setColor(new this.classes.JavaColor(117, 117, 117));
            }

        }
        // Fill with the color
        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Draw outline
        graphics.setColor(this.classes.JavaColor.black);
        graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        if (this.text !== undefined) {

            // Draw label
            this.menu.renderer.drawStringCentered(graphics, bounds, this.text);
        }
    }
})