package world.sc2.supernova.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Jigsaw;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import world.sc2.config.Config;
import world.sc2.config.ConfigManager;
import world.sc2.nbt.NBTTag;

import java.util.*;

public class LoreBlockManager {

    private static final String LORE_BLOCK_CONFIG_NAME_TAG_KEY = "loreBlockID";
    private static final String LORE_BLOCK_CONFIG_DIRECTORY_NAME = "loreBlockConfigs";
    private static final String LORE_BLOCK_CONFIG_COMMANDS_KEY = "commands";

    // Dependencies
    private final ConfigManager configManager;

    // Fields
    private final NBTTag<String, String> loreBlockIdentifierTag;

    public LoreBlockManager(JavaPlugin plugin, ConfigManager configManager) {
        this.configManager = configManager;
        this.loreBlockIdentifierTag = new NBTTag<>(new NamespacedKey(plugin, LORE_BLOCK_CONFIG_NAME_TAG_KEY), PersistentDataType.STRING);

        generateExampleLoreBlockConfig();
    }

    /**
     * Called every {@link PlayerInteractEvent}. Handles logic related to running commands upon clicking a lore block.
     * @param event The PlayerInteractEvent to handle.
     */
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!event.hasBlock())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if  (!isLoreBlock(event.getClickedBlock()))
            return;
        Jigsaw jigsaw = (Jigsaw) Objects.requireNonNull(event.getClickedBlock()).getState();
        String loreBlockIdentifier = loreBlockIdentifierTag.getStoredData(jigsaw);

        YamlConfiguration loreBlockConfig = configManager.getConfig(LORE_BLOCK_CONFIG_DIRECTORY_NAME
                + "/" + loreBlockIdentifier + ".yml").get();

        if (!loreBlockConfig.contains(LORE_BLOCK_CONFIG_COMMANDS_KEY)) {
            Bukkit.getLogger().warning("Lore block clicked does not have commands in the Config file!");
            return;
        }

        List<String> commandsToExecute = loreBlockConfig.getStringList(LORE_BLOCK_CONFIG_COMMANDS_KEY);

        for (String command : commandsToExecute) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
        }

    }

    /**
     * Called every {@link BlockPlaceEvent}. Handles logic related to transferring the lore block identifier from an
     * ItemStack to a BlockEntity any time a lore block is placed.
     * @param event The BlockPlaceEvent to handle
     */
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Bukkit.getLogger().info("onBlockPlaceEvent() called!");
        ItemStack itemToPlace = event.getItemInHand();
        if (!isLoreBlock(itemToPlace)) {
            return;
        }
        Bukkit.getLogger().info("Placed a lore block. Copying data...");
        Block blockPlaced = event.getBlockPlaced();
        if (!(blockPlaced.getState() instanceof Jigsaw loreBlock)) {
            Bukkit.getLogger().warning("Error transferring lore block data when placed: Placed block is not a " +
                    "Jigsaw block even though the placed ItemStack is a valid LoreBlock.");
            return;
        }
        loreBlockIdentifierTag.applyTag(loreBlock, loreBlockIdentifierTag.getStoredData(itemToPlace));
    }

    // todo make it so that running this does not overwrite already existent commands.
    public ItemStack createLoreBlock(String newLoreBlockIdentifier) {
        ItemStack loreBlock = new ItemStack(Material.JIGSAW);
        loreBlockIdentifierTag.applyTag(loreBlock, newLoreBlockIdentifier);
        Config newLoreBlockConfig = configManager.getConfig(LORE_BLOCK_CONFIG_DIRECTORY_NAME + "/" +
                newLoreBlockIdentifier + ".yml");
        newLoreBlockConfig.set(LORE_BLOCK_CONFIG_COMMANDS_KEY, new ArrayList<>());
        newLoreBlockConfig.save();
        return loreBlock;
    }

    /**
     * Returns true if the specified block is a lore block.
     * @param block The instance of block to test
     * @return true if the specified block is a lore block.
     */
    private boolean isLoreBlock(Block block) {
        if (!(Objects.requireNonNull(block).getState() instanceof Jigsaw jigsaw))
            return false;
        if (!loreBlockIdentifierTag.hasTag(jigsaw))
            return false;

        return true;
    }

    /**
     * Returns true if the specified {@link ItemStack} is a lore block.
     * @param item The ItemStack to test
     * @return true if the specified block is a lore block.
     */
    private boolean isLoreBlock(ItemStack item) {
        if (item.getType() != Material.JIGSAW) {
            return false;
        }
        if (!loreBlockIdentifierTag.hasTag(item)) {
            return false;
        }

        return true;
    }

    private void generateExampleLoreBlockConfig() {
        String exampleLoreBlockConfigPath = LORE_BLOCK_CONFIG_DIRECTORY_NAME + "/exampleLoreBlock.yml";
        configManager.getConfig(exampleLoreBlockConfigPath).save();
    }

}
