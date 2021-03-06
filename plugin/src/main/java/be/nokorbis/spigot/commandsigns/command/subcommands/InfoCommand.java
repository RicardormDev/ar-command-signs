package be.nokorbis.spigot.commandsigns.command.subcommands;

import be.nokorbis.spigot.commandsigns.command.CommandRequiringManager;
import be.nokorbis.spigot.commandsigns.controller.NCommandSignsManager;
import be.nokorbis.spigot.commandsigns.model.CommandBlock;
import be.nokorbis.spigot.commandsigns.model.CommandBlockPendingInteraction;
import be.nokorbis.spigot.commandsigns.model.CommandSignsCommandException;
import be.nokorbis.spigot.commandsigns.utils.CommandSignUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;


/**
 * Created by nokorbis on 1/20/16.
 */
public class InfoCommand extends CommandRequiringManager {

	public InfoCommand(NCommandSignsManager manager) {
		super(manager, "info", new String[] {"i"});
		this.basePermission = "commandsign.admin.info";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) throws CommandSignsCommandException {
		if (!(sender instanceof Player)) {
			throw new CommandSignsCommandException(commandMessages.get("error.command.player_requirement"));
		}
		Player player = (Player) sender;

		if (args.isEmpty()) {
			if (isPlayerAvailable(player)) {
				CommandBlockPendingInteraction interaction = new CommandBlockPendingInteraction();
				interaction.type = CommandBlockPendingInteraction.Type.INFO;
				interaction.player = player;
				manager.addPendingInteraction(interaction);
				player.sendMessage(commandMessages.get("howto.click_for_info"));
			}
		}
		else {
			try {
				long id = Long.parseLong(args.get(0));
				CommandBlock cmd = manager.getCommandBlock(id);
				if (cmd == null) {
					throw new CommandSignsCommandException(commandMessages.get("error.invalid_command_id"));
				}
				CommandSignUtils.info(player, cmd, manager.getAddons());
			}
			catch (NumberFormatException ex) {
				throw new CommandSignsCommandException(commandMessages.get("error.command.number_requirement"));
			}
		}
		return true;
	}

	@Override
	public void printUsage(CommandSender sender) {
		sender.sendMessage("/commandsign info [ID]");
	}
}
