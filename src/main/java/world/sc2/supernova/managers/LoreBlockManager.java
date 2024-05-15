package world.sc2.supernova.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Jigsaw;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import world.sc2.config.Config;
import world.sc2.config.ConfigManager;
import world.sc2.nbt.NBTTag;
import world.sc2.utility.ChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoreBlockManager {

    private static final String MISSING_LORE_BLOCK_CONFIG_MESSAGE = "&cThis lore block (identifier=%s) does not have" +
            " a valid config file.";
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
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (!event.hasBlock())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if  (!isLoreBlock(event.getClickedBlock()))
            return;
        event.setCancelled(true);
        if (event.getPlayer().isSneaking())
            return;
        Jigsaw jigsaw = (Jigsaw) Objects.requireNonNull(event.getClickedBlock()).getState();
        String loreBlockIdentifier = loreBlockIdentifierTag.getStoredData(jigsaw);

        YamlConfiguration loreBlockConfig = configManager.getConfig(LORE_BLOCK_CONFIG_DIRECTORY_NAME
                + "/" + loreBlockIdentifier + ".yml").get();

        if (!loreBlockConfig.contains(LORE_BLOCK_CONFIG_COMMANDS_KEY)) {
            event.getPlayer().sendActionBar(
                    ChatUtils.chat(String.format(MISSING_LORE_BLOCK_CONFIG_MESSAGE, loreBlockIdentifier)));
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
        ItemStack itemToPlace = event.getItemInHand();
        if (!isLoreBlock(itemToPlace)) {
            return;
        }
        Block blockPlaced = event.getBlockPlaced();
        if (!(blockPlaced.getState() instanceof Jigsaw loreBlock)) {
            Bukkit.getLogger().warning("Error transferring lore block data when placed: Placed block is not a " +
                    "Jigsaw block even though the placed ItemStack is a valid LoreBlock.");
            return;
        }
        loreBlockIdentifierTag.applyTag(loreBlock, loreBlockIdentifierTag.getStoredData(itemToPlace));
    }

    public ItemStack createLoreBlock(String newLoreBlockIdentifier) {
        ItemStack loreBlock = new ItemStack(Material.JIGSAW);
        ItemMeta loreBlockMeta = loreBlock.getItemMeta();
        // Changing the display name
        Component displayName = Component.text(ChatUtils.chat("&dLore Block"));
        loreBlockMeta.displayName(displayName);

        // Changing the lore to show identifier
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(ChatUtils.chat("&7&oidentifier="+newLoreBlockIdentifier)));
        loreBlockMeta.lore(lore);

        loreBlock.setItemMeta(loreBlockMeta);

        loreBlockIdentifierTag.applyTag(loreBlock, newLoreBlockIdentifier);
        Config newLoreBlockConfig = configManager.getConfig(LORE_BLOCK_CONFIG_DIRECTORY_NAME + "/" +
                newLoreBlockIdentifier + ".yml");
        if (!newLoreBlockConfig.get().contains(LORE_BLOCK_CONFIG_COMMANDS_KEY)) {
            newLoreBlockConfig.set(LORE_BLOCK_CONFIG_COMMANDS_KEY, new ArrayList<>());
            newLoreBlockConfig.save();
        }
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
