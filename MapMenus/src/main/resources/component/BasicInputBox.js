var JavaColor = Java.type("java.awt.Color");

function click(player, relative) {
  states.put("typing", player);
  menu.requestKeyboardInput(player, function(input) {
    states.delete("typing", player);
    data.put("typedText", player, input);
  }, true);
}

function render(graphics, player) {
  var typedText = data.get("typedText", player);

  var bounds = component.getBounds();

  graphics.setColor(JavaColor.DARK_GRAY);
  graphics.drawLine(bounds.x + 5, bounds.y + bounds.height - 1, bounds.x + bounds.width - 5, bounds.y + bounds.height - 1);

  if (states.get("typing", player)) {
    graphics.setColor(JavaColor.CYAN);
    graphics.fillRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4);

    graphics.setColor(JavaColor.gray);
    graphics.drawString("Typing...", Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
  } else if (typedText !== null) {
    graphics.setColor(JavaColor.black);
    graphics.drawString(typedText, Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
  } else {
    graphics.setColor(JavaColor.gray);
    graphics.drawString("Click to type!", Math.round(bounds.x + 2), Math.round((bounds.y + bounds.height) - 5));
  }
}