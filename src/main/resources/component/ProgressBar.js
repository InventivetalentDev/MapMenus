// ProgressBar
// author: inventivetalent
//
// Use colors.border, colors.bar, colors.background to change colors
// Use setProgress(progress)/getProgress() to modify the progress value
({
    classes: {
        JavaColor: Java.type("java.awt.Color")
    },
    colors: {
        border: this.classes.JavaColor.DARK_GRAY,
        bar: this.classes.JavaColor.BLUE,
        background: this.classes.JavaColor.WHITE
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

        graphics.setColor(this.colors.background);
        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        graphics.setColor(this.colors.bar);
        graphics.fillRect(bounds.x, bounds.y, (this.progress * bounds.width / 100), bounds.height);

        graphics.setColor(this.colors.border);
        graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
});