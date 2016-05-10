({
  classes: {
    JavaColor: Java.type("java.awt.Color"),
    BukkitSound: Java.type("org.bukkit.Sound")
  },
  text: "",
  ticks: 0,
  setText: function(text) {
    this.text = text;
  },
  getText: function() {
    return this.text;
  },
  init: function(text) {
    this.text = text;
    options.tickSpeed = 20;
  },
  click: function(player) {
    this.states.put("clicked", player, 250);
    player.playSound(player.getLocation(), this.classes.BukkitSound.UI_BUTTON_CLICK, 0.5, 1.0);
  },
  render: function(graphics, player) {
    var bounds = this.component.getBounds();
    var cursor = this.menu.getCursorPosition(player);

    if (this.states.get("clicked", player)) {
      graphics.setColor(new this.classes.JavaColor(37, 67, 207));
    } else {
      if (cursor !== null && bounds.contains(cursor)) {
        graphics.setColor(new this.classes.JavaColor(126, 136, 191));
      } else {
        graphics.setColor(new this.classes.JavaColor(117, 117, 117));
      }
    }
    graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

    graphics.setColor(new this.classes.JavaColor(0, 0, 0));
    graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

    if (this.text !== undefined) {
      this.menu.renderer.drawStringCentered(graphics, bounds, this.text);
    }
  }
});