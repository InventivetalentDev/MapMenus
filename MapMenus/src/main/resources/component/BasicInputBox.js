// BasicInputBox
// author: inventivetalent
({
    classes: {
        JavaColor: Java.type("java.awt.Color")
    },
    click: function(player, relative) {
        if (this.states.toggle("typing", player)) {
            var thisRef = this;
            this.menu.requestKeyboardInput(player, function(input) {
                thisRef.states.remove("typing", player);
                thisRef.data.put("typedText", player, input);
            }, true);
        }
    },
    render: function(graphics, player) {
        var typedText = this.data.get("typedText", player);
        var bounds = this.component.getBounds();

        graphics.setColor(this.classes.JavaColor.DARK_GRAY);
        graphics.drawLine(bounds.x + 5, bounds.y + bounds.height - 1, bounds.x + bounds.width - 5, bounds.y + bounds.height - 1);

        if (this.states.get("typing", player)) {
            graphics.setColor(this.classes.JavaColor.CYAN);
            graphics.fillRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4);

            graphics.setColor(this.classes.JavaColor.gray);
            graphics.drawString("Typing...", Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
        } else if (typedText !== null) {
            graphics.setColor(this.classes.JavaColor.black);
            graphics.drawString(typedText, Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
        } else {
            graphics.setColor(this.classes.JavaColor.gray);
            graphics.drawString("Click to type!", Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
        }
    }
});