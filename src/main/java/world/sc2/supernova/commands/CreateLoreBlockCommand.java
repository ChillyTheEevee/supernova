package world.sc2.supernova.commands;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import world.sc2.command.Command;
import world.sc2.config.Config;
import world.sc2.supernova.managers.LoreBlockManager;
import world.sc2.utility.ChatUtils;

import java.util.List;
import java.util.Objects;

public class CreateLoreBlockCommand extends Command {

    private static final String WARNING_UNSPECIFIED_IDENTIFIER_KEY = "messages.warning_unspecified_identifier";
    private static final String WARNING_WHITESPACE_IN_IDENTIFIER_KEY = "messages.warning_whitespace_in_identifier";
    private static final String WARNING_NON_PLAYER_SENDER_KEY = "messages.warning_non_player_sender";
    private static final String SUCCESSFUL_LORE_BLOCK_CREATION_KEY = "messages.successful_lore_block_creation";

    // Fields
    private final LoreBlockManager loreBlockManager;

    public CreateLoreBlockCommand(Config config, LoreBlockManager loreBlockManager) {
        super(config);
        this.loreBlockManager = loreBlockManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ChatUtils.chat(config.get().getString(WARNING_NON_PLAYER_SENDER_KEY)));
            return true;
        }
        if (args.length == 1) {
            commandSender.sendMessage(ChatUtils.chat(config.get().getString(WARNING_UNSPECIFIED_IDENTIFIER_KEY)));
            return true;
        }
        if (args.length > 2) {
            commandSender.sendMessage(ChatUtils.chat(config.get().getString(WARNING_WHITESPACE_IN_IDENTIFIER_KEY)));
            return true;
        }

        String identifier = args[1];

        player.getInventory().addItem(loreBlockManager.createLoreBlock(identifier));

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        player.sendMessage(ChatUtils.chat(String.format(
                Objects.requireNonNull(config.get().getString(SUCCESSFUL_LORE_BLOCK_CREATION_KEY)), identifier)));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args) {
        if (args.length != 2) {
            return null;
        }

        return List.of("<identifier>");
    }

}