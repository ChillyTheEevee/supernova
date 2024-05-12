package world.sc2.supernova.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import world.sc2.supernova.managers.LoreBlockManager;

public class PlayerInteractListener implements Listener {
    private final LoreBlockManager loreBlockManager;

    public PlayerInteractListener(LoreBlockManager loreBlockManager) {
        this.loreBlockManager = loreBlockManager;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        loreBlockManager.onPlayerInteractEvent(event);
    }

}
