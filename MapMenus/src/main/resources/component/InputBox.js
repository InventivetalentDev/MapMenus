// InputBox
// author: inventivetalent
//
// Use colors.baseLine, colors.placeholder, colors.text to change colors
// Use text.placeholder, text.typing to change the default placeholders
// Use getText(player)/setText(player, text) to access the value
({
    classes: {
        JavaColor: Java.type("java.awt.Color")
    },
    colors: {
        baseLine: this.classes.JavaColor.DARK_GRAY,
        placeholder: this.classes.JavaColor.gray,
        text: this.classes.JavaColor.black
    },
    text: {
        placeholder: "Click to type!",
        typing: "Typing..."
    },
    getText: function(player) {
        return this.data.get("typedText", player);
    },
    setText: function(player, text) {
        this.data.put("typedText", player, text);
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

        graphics.setColor(this.colors.baseLine);
        graphics.drawLine(bounds.x + 5, bounds.y + bounds.height - 1, bounds.x + bounds.width - 5, bounds.y + bounds.height - 1);

        if (this.states.get("typing", player)) {
            graphics.setColor(this.colors.placeholder);
            graphics.drawString("Typing...", Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
        }
        else if (typedText !== null) {
            graphics.setColor(this.colors.text);
            graphics.drawString(typedText, Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
        }
        else {
            graphics.setColor(this.colors.placeholder);
            graphics.drawString("Click to type!", Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
        }
    }
});