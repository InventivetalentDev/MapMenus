var JavaColor = Java.type("java.awt.Color");
var Bukkit = Java.type("org.bukkit.Bukkit");
var MinecraftServer = Java.type("net.minecraft.server.v1_9_R1.MinecraftServer"); // Note: change the version to your server version

var onlinePlayers = "0";
var tps = "0";
var motd = "";
var world = "";

var ticks = 9;

function init() {
  options.tickSpeed = 20;

  world = menu.getWorldName();
}

function tick() {
  if (ticks > 1) {
    onlinePlayers = "" + Bukkit.getOnlinePlayers().size() + " / " + Bukkit.getMaxPlayers();
    tps = "" + MinecraftServer.getServer().recentTps[0];
    motd = Bukkit.getMotd();

    renderer.render();
  }
  if (ticks > 10) {
    ticks = 0;

    renderer.refresh();
  }
  ticks++;

  print(ticks);
}

function render(graphics, player) {
  var bounds = menu.getBounds(); // Get the menu bounds
  graphics.setColor(JavaColor.WHITE);
  graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height); // Make the background white

  graphics.setColor(JavaColor.BLACK);
  graphics.drawString("OnlinePlayers: ", 10, 20);
  graphics.setColor(JavaColor.GREEN);
  graphics.drawString("" + onlinePlayers, 130, 20);

  graphics.setColor(JavaColor.BLACK);
  graphics.drawString("TPS: ", 10, 40);
  graphics.setColor(JavaColor.GREEN);
  graphics.drawString("" + tps, 130, 40);

  graphics.setColor(JavaColor.BLACK);
  graphics.drawString("MOTD: ", 10, 60);
  graphics.setColor(JavaColor.GREEN);
  graphics.drawString(motd, 130, 60);

  graphics.setColor(JavaColor.BLACK);
  graphics.drawString("World: ", 10, 80);
  graphics.setColor(JavaColor.GREEN);
  graphics.drawString(world, 130, 80);

  graphics.setColor(JavaColor.BLACK);
  graphics.drawString("You: ", 10, 100);
  graphics.setColor(JavaColor.GREEN);
  graphics.drawString(player.getName(), 130, 100);
}