/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.mapmenus.menu;

import com.google.gson.annotations.Expose;
import jdk.nashorn.api.scripting.JSObject;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.eventcallbacks.PlayerEventCallback;
import org.inventivetalent.mapmanager.controller.MapController;
import org.inventivetalent.mapmenus.MapMenusPlugin;
import org.inventivetalent.mapmenus.MenuScriptExecutionException;
import org.inventivetalent.mapmenus.MoveDirection;
import org.inventivetalent.mapmenus.TimingsHelper;
import org.inventivetalent.mapmenus.bounds.FixedBounds;
import org.inventivetalent.mapmenus.component.AnonymousScriptComponent;
import org.inventivetalent.mapmenus.component.FileScriptComponent;
import org.inventivetalent.mapmenus.component.MenuComponentAbstract;
import org.inventivetalent.mapmenus.component.ScriptComponentAbstract;
import org.inventivetalent.mapmenus.menu.data.MapperData;
import org.inventivetalent.mapmenus.menu.data.MapperStates;
import org.inventivetalent.mapmenus.menu.data.ScriptMenuData;
import org.inventivetalent.mapmenus.menu.data.ScriptMenuStates;
import org.inventivetalent.mapmenus.provider.MenuProviders;
import org.inventivetalent.mapmenus.provider.internal.PlaceholderProvider;
import org.inventivetalent.mapmenus.render.IFrameContainer;
import org.inventivetalent.mapmenus.render.Renderer;
import org.inventivetalent.mapmenus.script.IScriptContainer;
import org.inventivetalent.mapmenus.script.ScriptManagerAbstract;
import org.inventivetalent.mapmenus.script.Scriptifier;
import org.inventivetalent.mapmenus.script.Scriptify;
import org.inventivetalent.scriptconfig.NoSuchFunctionException;
import org.inventivetalent.scriptconfig.RuntimeScriptException;
import org.inventivetalent.scriptconfig.api.ScriptConfig;
import org.inventivetalent.vectors.d2.Vector2DDouble;
import org.inventivetalent.vectors.d3.Vector3DDouble;

import java.awt.*;
import java.util.UUID;
import java.util.logging.Level;

@Data
@EqualsAndHashCode(callSuper = true,
				   doNotUseGetters = true,
				   exclude = { "menu" })
@ToString(callSuper = true,
		  doNotUseGetters = true,
		  exclude = { "menu" })
@NoArgsConstructor
public class ScriptMapMenu extends MapMenuAbstract implements IFrameContainer, IScriptContainer {

	//	private static final Executor SCRIPT_TICK_EXECUTOR = Executors.newSingleThreadExecutor();
	static final int[][] NULL_INT_ARRAY = new int[0][0];

	@Expose private String name;

	protected HoverCallable hoverCallable;

	@Expose private String         scriptName;
	private         ScriptConfig   script;
	public          BukkitRunnable scriptTask;

	boolean noTickFunction;
	boolean noRenderFunction;
	boolean noClickFunction;

	protected int[][] itemFrameIds = NULL_INT_ARRAY;
	@Expose protected UUID[][] itemFrameUUIDs;

	int componentCounter = 1;

	// Script references
	@Scriptify public ScriptMapMenu menu = this;
	@Scriptify public Renderer renderer;
	@Scriptify @Expose public ScriptOptions       options      = new ScriptOptions();
	@Scriptify @Expose public ScriptMenuData      data         = new ScriptMenuData();
	@Scriptify @Expose public ScriptMenuStates    states       = new ScriptMenuStates();
	@Scriptify public         MenuProviders       providers    = MapMenusPlugin.instance.menuProviders;
	@Scriptify public         PlaceholderProvider placeholders = providers.get("Placeholders");

	public ScriptMapMenu(@NonNull ItemFrame baseFrame, @NonNull Vector3DDouble firstCorner, @NonNull Vector3DDouble secondCorner, @NonNull String name) {
		super(baseFrame, firstCorner, secondCorner);
		this.name = name;
		initRenderer();
	}

	public void initRenderer() {
		renderer = new Renderer(this, bounds, this);
	}

	public void setScriptConfig(String scriptName) {
		this.scriptName = scriptName;
	}

	@Override
	public void reloadScript() {
		// Reload the menu script
		reloadScript(MapMenusPlugin.instance.menuScriptManager);

		// Reload component scripts
		for (MenuComponentAbstract component : getComponents()) {
			if (component instanceof ScriptComponentAbstract) {
				((ScriptComponentAbstract) component).menu = this;
				((ScriptComponentAbstract) component).data = new MapperData(this.data, "__comp#" + ((ScriptComponentAbstract) component).id + "__%s");
				((ScriptComponentAbstract) component).states = new MapperStates(this.states, "__comp#" + ((ScriptComponentAbstract) component).id + "__%s");
			}
//			if (component instanceof IScriptContainer) {
//				((IScriptContainer) component).reloadScript();
//			}
		}
	}

	/**
	 * Reloads this menu's script file from the given ScriptManager
	 */
	public void reloadScript(ScriptManagerAbstract scriptManager) {
		if (this.scriptName == null || this.scriptName.isEmpty()) {
			throw new IllegalStateException("Script not yet set");
		}
		if (!scriptManager.doesScriptExist(this.scriptName)) {
			MapMenusPlugin.instance.getLogger().warning("Menu Script '" + script + "' does not exist (any longer)");
			return;
		}
		// Cancel the current task
		if (scriptTask != null) {
			scriptTask.cancel();
			scriptTask = null;
		}
		// Wrap the script
		this.script = scriptManager.wrapScript(this.scriptName);

		initScriptVariables();
		hoverCallable = new HoverCallable() {
			protected boolean noHoverTextFunction;

			@Override
			public String call(Player player, MapController mapController, int x, int y) {
				if (!noHoverTextFunction) {
					try {
						Object hoverText = ScriptMapMenu.this.script.callFunction("hoverText", player, x, y);
						return hoverText == null ? null : String.valueOf(hoverText);
					} catch (NoSuchFunctionException e) {
						// Ignore
						noHoverTextFunction = true;
					} catch (RuntimeScriptException e) {
						MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling hoverText(): " + e.getException().getMessage(), e);
					}
				}
				return null;
			}
		};

		try {
			this.script.callFunction("init");
		} catch (NoSuchFunctionException e) {
			// Ignore
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling init(): " + e.getException().getMessage(), e);
		}

		(scriptTask = new BukkitRunnable() {
			@Override
			public void run() {
				TimingsHelper.startTiming("MapMenu - tick");

				try {
					tick();
				} catch (MenuScriptExecutionException e) {
					cancel();
					TimingsHelper.stopTiming("MapMenu - tick");
					throw e;
				}

				TimingsHelper.stopTiming("MapMenu - tick");
			}
		}).runTaskTimer(MapMenusPlugin.instance, options.tickSpeed, options.tickSpeed);
	}

	void initScriptVariables() {
		//		this.script.setVariable("menu", this);
		//		this.script.setVariable("renderer", this.renderer);
		//		this.script.setVariable("options", this.options);
		//		this.script.setVariable("data", this.data);
		//		this.script.setVariable("states", this.states);
		//		this.script.setVariable("providers", this.providers);
		//
		//		this.script.setVariable("placeholders", this.placeholders);
		System.out.println("Scriptify menu");
		Scriptifier.scriptify(this, getScript().getScriptEngine());
	}

	/*
	 * File Script Components
	 */

	@Scriptify(targetVar = "menu")
	public JSObject addComponent(String script, int x, int y, int width, int height) {
		return addComponent(script, x, y, width, height, new Object[0]);
	}

	@Scriptify(targetVar = "menu")
	public JSObject addComponent(String script, int x, int y, int width, int height, Object... initArgs) {
		return addComponent(script, new FixedBounds(x, y, width, height), initArgs);
	}

	@Scriptify(targetVar = "menu")
	public JSObject addComponent(String scriptName, FixedBounds bounds, Object[] initArgs) {
		if (!MapMenusPlugin.instance.componentScriptManager.doesScriptExist(scriptName)) {
			MapMenusPlugin.instance.getLogger().warning("Component Script '" + scriptName + "' does not exist");
			return null;
		}
		tickLocked = true;

		FileScriptComponent component = new FileScriptComponent(this.bounds, bounds, scriptName, initArgs);
		component.id = componentCounter++;
		component.menu = this;
		component.data = new MapperData(this.data, "__comp#" + componentCounter + "__%s");
		component.states = new MapperStates(this.states, "__comp#" + componentCounter + "__%s");
		components.put(component.getUuid(), component);
		component.reloadScript();
		//		Scriptifier.scriptify(component, component.getScript().getScriptEngine());

		tickLocked = false;
		return (JSObject) component.getScriptConfig().getContent();
	}

	/*
	 * Anonymous Script Components
	 */
	@Scriptify(targetVar = "menu")
	public JSObject addComponent(int x, int y, int width, int height, Object function) {
		return addComponent(new FixedBounds(x, y, width, height), function);
	}

	@Scriptify(targetVar = "menu")
	public JSObject addComponent(FixedBounds bounds, Object function) {
		if (function == null || !(function instanceof JSObject)) {
			MapMenusPlugin.instance.getLogger().info("Menu " + getName() + " attempted to add anonymous component, but the parameter is no function (It's " + (function == null ? "null" : function.getClass()) + ")");
			return null;
		}
		tickLocked = true;

		AnonymousScriptComponent component = new AnonymousScriptComponent(this.bounds, bounds, (JSObject) function);
		component.id = componentCounter++;
		component.menu = this;
		components.put(component.getUuid(), component);
		System.out.println("Scriptify anonymous component");
		Scriptifier.scriptify(component, component.getScript());
		try {
			component.init();
		} catch (NoSuchFunctionException e) {
			// Ignore
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling init(): " + e.getException().getMessage(), e);
		}

		tickLocked = false;
		return component.getScript();
	}

	public ScriptComponentAbstract removeComponent(UUID uuid) {
		tickLocked = true;

		ScriptComponentAbstract component = components.remove(uuid);

		tickLocked = false;
		return component;
	}

	@Scriptify(targetVar = "menu")
	public CursorPosition getCursorPosition(Player player) {
		if (baseVector.distanceSquared(new Vector3DDouble(player.getLocation())) > 1024) {
			return null;// Player is too far away
		}
		return CursorPosition.calculateFor(player, this);
	}

	@Override
	public void tick() {
		if (tickLocked) { return; }

		if (!noTickFunction) {
			try {
				script.callFunction("tick");
			} catch (NoSuchFunctionException e) {
				// Ignore this
				noTickFunction = true;
			} catch (RuntimeScriptException e) {
				MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling tick(): " + e.getException().getMessage(), e);
				throw new MenuScriptExecutionException("ScriptException in Menu tick()", e);
			}
		}

		for (MenuComponentAbstract component : getComponents()) {
			component.tick();
		}
		//				throw new RuntimeScriptException(new Exception());
	}

	@Override
	public void render(Graphics2D graphics, Player player) {
		if (baseVector.distanceSquared(new Vector3DDouble(player.getLocation())) > 1024) { return; }// Don't render if the player is more than 32 block away
		if (!noRenderFunction) {
			try {
				script.callFunction("render", graphics, player);
			} catch (NoSuchFunctionException e) {
				// Ignore this
				noRenderFunction = true;
			} catch (RuntimeScriptException e) {
				MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling render(): " + e.getException().getMessage(), e);
				throw new MenuScriptExecutionException("ScriptException in Menu render()", e);
			}
		}

		for (MenuComponentAbstract component : getComponents()) {
			component.render(graphics, player);
		}
	}

	@Override
	public boolean click(Player player, CursorPosition absolutePosition, int action) {
		boolean clickHandled = false;

		if (!noClickFunction) {
			try {
				Object result = script.callFunction("click", player, absolutePosition, action);
				clickHandled = result != null;
			} catch (NoSuchFunctionException e) {
				// Ignore this
				noClickFunction = true;
			} catch (RuntimeScriptException e) {
				MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling click(): " + e.getException().getMessage(), e);
				throw new MenuScriptExecutionException("ScriptException in Menu click()", e);
			}
		}

		for (MenuComponentAbstract component : getComponents()) {
			TimingsHelper.startTiming("MapMenu:handleInteract:click:component");
			if (component.click(player, absolutePosition, action)) { clickHandled = true; }
			TimingsHelper.stopTiming("MapMenu:handleInteract:click:component");
		}
		return clickHandled;
	}

	@Override
	public void dispose() {
		try {
			script.callFunction("dispose");
		} catch (NoSuchFunctionException e) {
			// Ignore this
		} catch (RuntimeScriptException e) {
			MapMenusPlugin.instance.getLogger().log(Level.WARNING, "Unexpected ScriptException whilst calling dispose(): " + e.getException().getMessage(), e);
		}

		// Dispose all components
		for (MenuComponentAbstract component : getComponents()) {
			component.dispose();
		}
		this.components.clear();

		// Cancel the ticks
		if (scriptTask != null) { scriptTask.cancel(); }

		// Remove this menu
		MapMenusPlugin.instance.menuManager.removeMenu(this);
	}

	@Scriptify(targetVar = "menu")
	public void requestKeyboardInput(Player player, Object invocable) {
		requestKeyboardInput(player, invocable, true);
	}

	@Scriptify(targetVar = "menu")
	public void requestKeyboardInput(final Player player, final Object invocable, final boolean cancelMessage) {
		if (invocable instanceof JSObject) {
			MapMenusPlugin.instance.eventCallbacks.listenFor(AsyncPlayerChatEvent.class, new PlayerEventCallback<AsyncPlayerChatEvent>(player) {
				@Override
				public void callPlayer(AsyncPlayerChatEvent event) {
					String message = null;
					if (event != null) {
						message = event.getMessage();
						if (cancelMessage) {
							event.setCancelled(true);
						}
					}
					((JSObject) invocable).call(ScriptMapMenu.this.menu, message);
				}
			});
		} else {
			MapMenusPlugin.instance.getLogger().warning("Second argument for 'requestKeyboardInput' must be invocable in Menu '" + getName() + "'");
		}
	}

	@Scriptify(targetVar = "menu")
	public void requestCommandInput(Player player, Object invocable) {
		requestCommandInput(player, invocable, true);
	}

	@Scriptify(targetVar = "menu")
	public void requestCommandInput(Player player, final Object invocable, final boolean cancelCommand) {
		if (invocable instanceof JSObject) {
			MapMenusPlugin.instance.eventCallbacks.listenFor(PlayerCommandPreprocessEvent.class, new PlayerEventCallback<PlayerCommandPreprocessEvent>(player) {
				@Override
				public void callPlayer(PlayerCommandPreprocessEvent event) {
					String message = null;
					if (event != null) {
						message = event.getMessage();
						if (cancelCommand) {
							event.setCancelled(true);
						}
					}
					((JSObject) invocable).call(ScriptMapMenu.this.menu, message);
				}
			});
		} else {
			MapMenusPlugin.instance.getLogger().warning("Second argument for 'requestCommandInput' must be invocable in Menu '" + getName() + "'");
		}
	}

	@Scriptify(targetVar = "menu")
	public void requestMovementInput(Player player, Object invocable) {
		requestMovementInput(player, invocable, true);
	}

	@Scriptify(targetVar = "menu")
	public void requestMovementInput(final Player player, final Object invocable, final boolean cancelMove) {
		if (invocable instanceof JSObject) {
			MapMenusPlugin.instance.eventCallbacks.listenFor(PlayerMoveEvent.class, new PlayerEventCallback<PlayerMoveEvent>(player) {
				@Override
				public void callPlayer(PlayerMoveEvent event) {
					Location moveDiff = null;
					if (event != null && event.getFrom().getWorld() == event.getTo().getWorld()) {
						double x = event.getTo().getX() - event.getFrom().getX();
						double y = event.getTo().getY() - event.getFrom().getY();
						double z = event.getTo().getZ() - event.getFrom().getZ();
						float yaw = event.getTo().getYaw() - event.getFrom().getYaw();
						float pitch = event.getTo().getPitch() - event.getFrom().getPitch();
						moveDiff = new Location(event.getFrom().getWorld(), x, y, z, yaw, pitch);

						if (cancelMove) {
							event.setTo(event.getFrom());
						}
					}

					MoveDirection baseDirection = MoveDirection.getBaseDirection(moveDiff);
					MoveDirection moveDirection = baseDirection.getLookDirection(player.getLocation());
					double value = baseDirection.getValue(moveDiff);

					if (((JSObject) invocable).keySet().isEmpty()) {// function(type, amount)
						if (moveDiff == null) {
							((JSObject) invocable).call(ScriptMapMenu.this.menu, null, 0);
						} else {
							((JSObject) invocable).call(ScriptMapMenu.this.menu, moveDirection.getCodeName(), value);
						}
					} else {// {north:function(amount){},...}
						if (moveDiff != null) {
							for (String s : ((JSObject) invocable).keySet()) {
								Object member = ((JSObject) invocable).getMember(s);
								if (member instanceof JSObject) {
									if (s.equals(baseDirection.getCodeName())) {
										((JSObject) member).call(ScriptMapMenu.this.menu, value);
									}
									if (baseDirection == moveDirection) { continue; }
									if (s.equals(moveDirection.getCodeName())) {
										((JSObject) member).call(ScriptMapMenu.this.menu, value);
									}
								}
							}
						}
					}
				}
			});
		} else {
			MapMenusPlugin.instance.getLogger().warning("Second argument for 'requestMovementInput' must be invocable in Menu '" + getName() + "'");
		}
	}

	public HoverCallable getHoverCallable() {
		return hoverCallable;
	}

	@Override
	public void refreshFrames() {
		if (!MapMenusPlugin.instance.isEnabled()) { return; }
		Bukkit.getScheduler().runTask(MapMenusPlugin.instance, new Runnable() {
			@Override
			public void run() {
				TimingsHelper.startTiming("MapMenu - refreshItemFrames");

				final World world = getWorld();
				if (world.getPlayers().isEmpty()) {
					itemFrameIds = NULL_INT_ARRAY;
				} else {
					itemFrameIds = new int[getBlockWidth()][getBlockHeight()];
					itemFrameUUIDs = new UUID[getBlockWidth()][getBlockHeight()];

					//				Vector startVector = new Vector(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
					Vector2DDouble startVector = minCorner2d;

					for (Entity entity : world.getNearbyEntities(baseVector.toBukkitLocation(world), getBlockWidth(), getBlockHeight(), getBlockWidth())) {
						if (entity instanceof ItemFrame) {
							if (MapMenusPlugin.instance.debugParticles) { getWorld().spawnParticle(Particle.FLAME, entity.getLocation(), 0); }
							if (ScriptMapMenu.this.boundingBox.expand(0.1).contains(new Vector3DDouble(entity.getLocation()))) {
								for (int y = 0; y < getBlockHeight(); y++) {
									for (int x1 = 0; x1 < getBlockWidth(); x1++) {
										int x = facing.isFrameModInverted() ? (getBlockWidth() - 1 - x1) : x1;
										Vector3DDouble vector3d = facing.getPlane().to3D(startVector.add(x, y), baseVector.getX(), baseVector.getZ());
										if (entity.getLocation().getBlockZ() == vector3d.getZ().intValue()) {
											if (entity.getLocation().getBlockX() == vector3d.getX().intValue()) {
												if (entity.getLocation().getBlockY() == vector3d.getY().intValue()) {
													itemFrameIds[x1][y] = entity.getEntityId();
													itemFrameUUIDs[x1][y] = entity.getUniqueId();

													entity.setMetadata("MAP_MENUS_META", new FixedMetadataValue(MapMenusPlugin.instance, ScriptMapMenu.this));
												}
											}
										}
									}
								}
							}
						}
					}
				}
				TimingsHelper.stopTiming("MapMenu - refreshItemFrames");
			}
		});
	}

	@Override
	public World getWorld() {
		return super.getWorld();
	}

	@Override
	public int[][] getItemFrameIds() {
		//		refreshItemFrames();
		return itemFrameIds;
	}

	public String getName() {
		return name;
	}
}