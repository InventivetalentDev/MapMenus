// Label
// author: inventivetalent
({
    classes: {
        JavaColor: Java.type("java.awt.Color")
    },
    text: "",
    borderVisible: false,
    setText: function(text) {
        this.text = text;
    },
    getText: function() {
        return this.text;
    },
    setBorderVisible: function(borderVisible) {
        this.borderVisible = borderVisible;
    },
    isBorderVisible: function() {
        return this.borderVisible;
    },
    init: function(text, borderVisible) {
        this.text = text;
        this.borderVisible = borderVisible;
    },
    render: function(graphics, player) {
        var bounds = this.component.getBounds();
        graphics.setColor(this.classes.JavaColor.black);
        if (this.borderVisible) {
            graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        this.menu.renderer.drawStringCentered(graphics, bounds, this.text);
    }
});