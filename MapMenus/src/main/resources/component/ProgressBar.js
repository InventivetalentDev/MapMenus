// ProgressBar
// author: inventivetalent
({
    classes: {
        JavaColor: Java.type("java.awt.Color")
    },
    progress: 0,
    setProgress: function(progress) {
        this.progress = progress;
    },
    getProgress: function() {
        return this.progress;
    },
    render: function(graphics, player) {
        var bounds = this.component.getBounds();

        graphics.setColor(this.classes.JavaColor.BLUE);
        graphics.fillRect(bounds.x, bounds.y, (this.progress * bounds.width / 100), bounds.height);

        graphics.setColor(this.classes.JavaColor.DARK_GRAY);
        graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
});