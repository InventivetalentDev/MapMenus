var JavaColor = Java.type("java.awt.Color");
var JavaSystem = Java.type("java.lang.System");
var Bukkit = Java.type("org.bukkit.Bukkit");


var ticks = 9;

function init() {
  options.tickSpeed = 20;
}

function tick() {
  if (ticks > 1) {
    renderer.render();
  }
  if (ticks > 10) {
    ticks = 0;

    renderer.refresh();
  }
  ticks++;
}

function render(graphics, player) {
  var bounds = menu.getBounds(); // Get the menu bounds
  graphics.setColor(JavaColor.WHITE);
  graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height); // Make the background white


  if (!states.get("downloadingAvatar", player) && !data.has("Avatar", player)) {
    states.put("downloadingAvatar", player, 20000);
    renderer.downloadImageData("https://crafatar.com/renders/head/" + player.getUniqueId(), function(imgData) {
      data.put("Avatar", player, imgData);
      states.delete("downloadingAvatar", player);
    });
  } else {
    var avatarData = data.get("Avatar", player);
    renderer.drawImageData(graphics, avatarData, 0, 0, Math.max(128, Math.round(bounds.width / 2)), Math.max(128, Math.round(bounds.height / 2)));
  }


  graphics.setColor(JavaColor.BLACK);
  graphics.drawString("Name: ", float2int((bounds.width / 2) + 10), 20)
  graphics.setColor(JavaColor.GREEN);
  graphics.drawString(player.getName(), float2int((bounds.width / 2) + 90), 20);

  graphics.setColor(JavaColor.BLACK);
  graphics.drawString("GameMode: ", float2int((bounds.width / 2) + 10), 40)
  graphics.setColor(JavaColor.GREEN);
  graphics.drawString(player.getGameMode().name().toLowerCase(), float2int((bounds.width / 2) + 90), 40);

  graphics.setColor(JavaColor.BLACK);
  graphics.drawString("Health: ", float2int((bounds.width / 2) + 10), 60)
  graphics.setColor(JavaColor.GREEN);
  graphics.drawString("" + player.getHealth(), float2int((bounds.width / 2) + 90), 60);

  graphics.setColor(JavaColor.BLACK);
  graphics.drawString("Food: ", float2int((bounds.width / 2) + 10), 80)
  graphics.setColor(JavaColor.GREEN);
  graphics.drawString("" + player.getFoodLevel(), float2int((bounds.width / 2) + 90), 80);

  graphics.setColor(JavaColor.BLACK);
  graphics.drawString("TimePlayed: ", float2int((bounds.width / 2) + 10), 100)
  graphics.setColor(JavaColor.GREEN);
  graphics.drawString("" + millisToDaysHoursMinutesSeconds(JavaSystem.currentTimeMillis() - player.getFirstPlayed()), float2int((bounds.width / 2) + 90), 100);
}

function float2int(value) {
  return value | 0;
}

function pad(number) {
  var result = "" + number;
  if (result.length < 2) {
    result = "0" + result;
  }

  return result;
}

function millisToDaysHoursMinutesSeconds(ms) {
  var x = ms / 1000
  var seconds = x % 60
  x /= 60
  var minutes = x % 60
  x /= 60
  var hours = x % 24
  x /= 24
  var days = x

  return pad(float2int(days)) + ":" + pad(float2int(hours)) + ":" + pad(float2int(minutes)) + ":" + pad(float2int(seconds));
}