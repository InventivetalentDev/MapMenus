// Checkbox
// author: inventivetalent
//
// Use colors.border & colors.cross to change the colors
({
    classes: {
        JavaColor: Java.type("java.awt.Color")
    },
    colors: {
        border: this.classes.JavaColor.black,
        cross: this.classes.JavaColor.green
    },
    click: function(player, relative) {
        this.states.toggle("checkboxChecked", player);
    },
    render: function(graphics, player) {
        var checked = this.states.get("checkboxChecked", player);
        var bounds = this.component.getBounds();

        // Draw outline
        graphics.setColor(this.colors.border);
        graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        if (checked) {
            // Draw the cross if it's checked
            graphics.setColor(this.colors.cross);
            graphics.drawLine(bounds.x + 2, bounds.y + 2, bounds.x + bounds.width - 2, bounds.y + bounds.height - 2);
            graphics.drawLine(bounds.x + 2, bounds.y + bounds.height - 2, bounds.x + bounds.width - 2, bounds.y + 2);
        }
    }
});