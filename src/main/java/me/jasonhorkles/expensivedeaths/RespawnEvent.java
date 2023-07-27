package me.jasonhorkles.expensivedeaths;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

public class RespawnEvent implements Listener {
    public RespawnEvent(ExpensiveDeaths plugin) {
        this.plugin = plugin;
    }

    private final ExpensiveDeaths plugin;

    @EventHandler(ignoreCancelled = true)
    public void respawnEvent(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.hasPermission("expensivedeaths.bypass")) return;

                final Function<String, String> parser = s -> s.replace("{PLAYER}", player.getName())
                    .replace("{DISPLAYNAME}", player.getDisplayName());
                plugin.run(Execution.Type.RESPAWN_CONSOLE, player, null, parser);
                plugin.run(Execution.Type.RESPAWN_PLAYER, player, null, parser);
            }
        }.runTaskLater(plugin, 5);
    }
}
