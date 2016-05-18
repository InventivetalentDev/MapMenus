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
    text: {
        placeholder: "Click to type!",
        typing: "Typing...",
        isGlobal: false
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
                if(this.text.isGlobal){
                    thisRef.data.put("typedText", input);
                }else{
                    thisRef.data.put("typedText", player, input);
                }
            }, true);
        }
    },
    render: function(graphics, player) {
        var typedText = this.text.isGlobal ? this.data.get("typedText") : this.data.get("typedText", player);
        var bounds = this.component.getBounds();

        graphics.setColor(this.classes.JavaColor.DARK_GRAY);
        graphics.drawLine(bounds.x + 5, bounds.y + bounds.height - 1, bounds.x + bounds.width - 5, bounds.y + bounds.height - 1);

        if (this.states.get("typing", player)) {
            graphics.setColor(this.classes.JavaColor.gray);
            graphics.drawString("Typing...", Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
        }
        else if (typedText !== null) {
            graphics.setColor(this.classes.JavaColor.black);
            graphics.drawString(typedText, Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
        }
        else {
            graphics.setColor(this.classes.JavaColor.gray);
            graphics.drawString("Click to type!", Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
        }
    }
});