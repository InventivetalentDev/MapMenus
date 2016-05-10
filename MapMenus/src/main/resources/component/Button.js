// Button
// author: inventivetalent
//
// Use colors.inactive, colors.hover, colors.clicked, colors.boder to change colors
// Use click.timeout to change the click delay
// Use click.sound.enabled to enable/disable sound
// Use click.sound.type, click.sound.pitch, click.sound.volume to customize the sound
({
    classes: {
        JavaColor: Java.type("java.awt.Color"),
        BukkitSound: Java.type("org.bukkit.Sound")
    },
    text: "",
    colors: {
        inactive: new this.classes.JavaColor(117, 117, 117),
        hover: new this.classes.JavaColor(126, 136, 191),
        clicked: new this.classes.JavaColor(37, 67, 207),
        border: this.classes.JavaColor.black
    },
    click: {
        timeout: 250,
        sound: {
            enabled: true,
            type: this.classes.BukkitSound.UI_BUTTON_CLICK,
            pitch: 0.5,
            volume: 1.0
        }
    },
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
        this.states.put("clicked", player, this.click.timeout);
        if (this.click.sound.enabled) {
            player.playSound(player.getLocation(), this.click.sound.type, this.click.sound.pitch, this.click.sound.volume);
        }
    },
    render: function(graphics, player) {
        var bounds = this.component.getBounds();
        var cursor = this.menu.getCursorPosition(player);

        if (this.states.get("clicked", player)) { // Button clicked
            graphics.setColor(this.colors.clicked);
        }
        else {
            if (cursor !== null && bounds.contains(cursor)) { // Mouse hover
                graphics.setColor(this.colors.hover);
            }
            else { // Inactive
                graphics.setColor(this.colors.inactive);
            }
        }
        // Fill with the color
        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Draw outline
        graphics.setColor(this.colors.border);
        graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        if (this.text !== undefined) {
            // Draw label
            this.menu.renderer.drawStringCentered(graphics, bounds, this.text);
        }
    }
});