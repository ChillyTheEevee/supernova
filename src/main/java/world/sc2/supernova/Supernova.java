package world.sc2.supernova;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import world.sc2.command.CommandManager;
import world.sc2.config.ConfigManager;
import world.sc2.supernova.commands.CreateLoreBlockCommand;
import world.sc2.supernova.listeners.BlockPlaceListener;
import world.sc2.supernova.listeners.PlayerInteractListener;
import world.sc2.supernova.managers.LoreBlockManager;

public final class Supernova extends JavaPlugin {

    // managers
    private ConfigManager configManager;
    private CommandManager commandManager;
    private LoreBlockManager loreBlockManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Create managers
        configManager = new ConfigManager(this);
        commandManager = new CommandManager(this, configManager);
        loreBlockManager = new LoreBlockManager(this, configManager);

        // Register events
        registerEvents(new PlayerInteractListener(loreBlockManager));
        registerEvents(new BlockPlaceListener(loreBlockManager));

        // Register commands
        CreateLoreBlockCommand createLoreBlockCommand = new CreateLoreBlockCommand(
                configManager.getConfig("commands/createloreblock.yml"), loreBlockManager);
        commandManager.addCommand("createloreblock", createLoreBlockCommand);

        // Save configs
        configManager.saveConfigs();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // Save configs
        configManager.saveConfigs();
    }

    private void registerEvents(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

}
