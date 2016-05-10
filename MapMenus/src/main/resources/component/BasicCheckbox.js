// BasicCheckbox
// author: inventivetalent
({
    classes: {
        JavaColor: Java.type("java.awt.Color")
    },
    click: function(player, relative) {
        this.states.toggle("checkboxChecked", player);
    },
    render: function(graphics, player) {
        var checked = this.states.get("checkboxChecked", player);

        var bounds = this.component.getBounds();

        graphics.setColor(this.classes.JavaColor.black);
        graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        if (checked) {
            graphics.setColor(this.classes.JavaColor.green);
            graphics.drawLine(bounds.x + 2, bounds.y + 2, bounds.x + bounds.width - 2, bounds.y + bounds.height - 2);
            graphics.drawLine(bounds.x + 2, bounds.y + bounds.height - 2, bounds.x + bounds.width - 2, bounds.y + 2);
        }
    }
});