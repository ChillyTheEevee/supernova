package world.sc2.supernova.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import world.sc2.supernova.managers.LoreBlockManager;

public class BlockPlaceListener implements Listener {

    private final LoreBlockManager loreBlockManager;

    public BlockPlaceListener(LoreBlockManager loreBlockManager) {
        this.loreBlockManager = loreBlockManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        loreBlockManager.onBlockPlaceEvent(event);
    }

}
