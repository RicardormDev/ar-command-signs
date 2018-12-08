package be.nokorbis.spigot.commandsigns.model;

import java.util.*;

import be.nokorbis.spigot.commandsigns.api.addons.Addon;
import be.nokorbis.spigot.commandsigns.api.addons.AddonConfigurationData;
import be.nokorbis.spigot.commandsigns.api.addons.AddonExecutionData;
import be.nokorbis.spigot.commandsigns.api.exceptions.CommandSignsException;
import be.nokorbis.spigot.commandsigns.utils.CommandBlockValidator;
import com.google.gson.JsonObject;
import org.bukkit.Location;

import be.nokorbis.spigot.commandsigns.CommandSignsPlugin;


public class CommandBlock {
	private transient static Set<Long> usedIds = new HashSet<>();
	private transient static Long biggerUsedId = 0L;

	private long id;
	private String name;

	private Location location;

	private boolean disabled;

	private Map<String, AddonConfigurationData> addonConfigurations= new HashMap<>();
	private Map<String, AddonExecutionData> addonExecutions = new HashMap<>();

	private final List<String> commands = new ArrayList<>();
	private final List<String> permissions = new ArrayList<>();


	private Integer timeBeforeExecution; // Value in seconds
	private Boolean resetOnMove;
	private Boolean cancelledOnMove;


	public CommandBlock() {
		this.setTimeBeforeExecution(0);
		this.resetOnMove = false;
		this.cancelledOnMove = false;

		this.setId(getFreeId());
	}

	public CommandBlock(Long id) {
		this.setTimeBeforeExecution(0);
		this.resetOnMove = false;
		this.cancelledOnMove = false;

		if (usedIds.contains(id)) {
			CommandSignsPlugin.getPlugin().getLogger().warning("A strange error occured : It seems that the registered id (" + id + ") is already in used... Getting a new one...");
			id = getFreeId();
		}
		this.setId(id);
	}

	public static long getBiggerUsedId() {
		return biggerUsedId;
	}

	private static long getFreeId() {
		for (long i = 0; i <= biggerUsedId; i++) {
			if (!usedIds.contains(i)) {
				return i;
			}
		}
		return ++biggerUsedId;
	}

	/* Getters and setters */

	/* Id */

	private void setId(long id) {
		this.id = id;
		usedIds.add(id);

		if (id > biggerUsedId) {
			biggerUsedId = id;
		}
	}

	public long getId() {
		return this.id;
	}

	/* Configuration data */

	public AddonConfigurationData getAddonConfigurationData(final Addon addon) {
		if (addon == null) {
			return null;
		}
		return this.addonConfigurations.computeIfAbsent(addon.getName(), (name) -> {
			JsonObject data = addon.createConfigurationData();
			if (data != null) {
				return new AddonConfigurationData(addon, data);
			}
			return null;
		});
	}

	public AddonExecutionData getAddonExecutionData(final Addon addon) {
		if (addon == null) {
			return null;
		}
		return this.addonExecutions.computeIfAbsent(addon.getName(), (name) -> {
			JsonObject data = addon.createExecutionData();
			if (data != null) {
				return new AddonExecutionData(addon, data);
			}
			return null;
		});
	}

	/* Name */

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/* Block */
	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location loc) {
		this.location = loc;
	}

	/* Commands */
	public void addCommand(String command) {
		this.commands.add(command);
	}

	public List<String> getCommands() {
		return this.commands;
	}

	public boolean removeCommand(int index) {
		if (index < 0) {
			return false;
		}
		if (this.commands.size() <= index) {
			return false;
		}
		this.commands.remove(index);
		return true;
	}

	public void editCommand(int index, String newCmd) {
		if (index < 0) {
			return;
		}
		removeCommand(index);
		this.commands.add(index, newCmd);
	}

	/* Permissions */
	public void addPermission(String permission) {
		this.permissions.add(permission);
	}

	public List<String> getPermissions() {
		return this.permissions;
	}

	public boolean removePermission(int index) {
		if (index < 0) {
			return false;
		}
		if (this.permissions.size() <= index) {
			return false;
		}

		this.permissions.remove(index);
		return true;
	}

	public void editPermission(int index, String newPerm) {
		if (index < 0) {
			return;
		}
		removePermission(index);
		this.permissions.add(index, newPerm);
	}

	/* Timers */

	public Integer getTimeBeforeExecution() {
		return this.timeBeforeExecution;
	}

	public void setTimeBeforeExecution(Integer timer) {
		if ((timer == null) || (timer < 0)) {
			timer = 0;
		}
		this.timeBeforeExecution = timer;
	}

	public Boolean isCancelledOnMove() {
		return this.cancelledOnMove;
	}

	public void setCancelledOnMove(Boolean cancel) {
		if (cancel == null) {
			cancel = false;
		}
		this.cancelledOnMove = cancel;
	}

	public Boolean isResetOnMove() {
		return this.resetOnMove;
	}

	public void setResetOnMove(Boolean reset) {
		if (reset == null) {
			reset = false;
		}
		this.resetOnMove = reset;
	}

	public boolean hasTimer() {
		return this.timeBeforeExecution >= 1;
	}


	/* Disabled */

	public boolean isDisabled() {
		return this.disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/* Business */

	public CommandBlock copy() {
		CommandBlock newBlock = new CommandBlock();

		for (String perm : this.permissions) {
			newBlock.addPermission(perm);
		}

		for (String cmd : this.commands) {
			newBlock.addCommand(cmd);
		}

		if (this.hasTimer()) {
			newBlock.setTimeBeforeExecution(this.timeBeforeExecution);
		}

		if (this.cancelledOnMove != null && this.cancelledOnMove) {
			newBlock.setCancelledOnMove(true);
		}

		if (this.resetOnMove != null && this.resetOnMove) {
			newBlock.setResetOnMove(true);
		}

		return newBlock;
	}

	public boolean validate() throws CommandSignsException {
		if (this.location == null) {
			throw new CommandSignsException("A command block is invalid due to null location. You may think about deleting it, its id : " + this.id);
		}
		if (!CommandBlockValidator.isValidBlock(this.location.getBlock())) {
			throw new CommandSignsException("A command block is invalid due to an invalid type (must be sign, plate or button). You may think about deleting it, its id : " + this.id);
		}
		return true;
	}

	public String blockSummary() {
		if (this.location == null) {
			return "";
		}
		return this.location.getBlock().getType() + " #" + this.location.getX() + ":" + this.location.getZ() + "(" + this.location.getY() + ")";
	}

	public static void reloadUsedIDs() {
		usedIds = new HashSet<>();
		biggerUsedId = 0L;
	}

	public static void reloadUsedID(long id) {
		usedIds.remove(id);
	}
}
