var JavaColor = Java.type("java.awt.Color");

function click(player, relative) {
  states.toggle("checkboxChecked", player);
}

function render(graphics, player) {
  var checked = states.get("checkboxChecked", player);

  var bounds = component.getBounds();

  graphics.setColor(JavaColor.black);
  graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

  if (checked) {
    graphics.setColor(JavaColor.green);
    graphics.drawLine(bounds.x + 2, bounds.y + 2, bounds.x + bounds.width - 2, bounds.y + bounds.height - 2);
    graphics.drawLine(bounds.x + 2, bounds.y + bounds.height - 2, bounds.x + bounds.width - 2, bounds.y + 2);
  }
}