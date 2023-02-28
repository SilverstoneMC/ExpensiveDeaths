package me.jasonhorkles.expensivedeaths;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RespawnEvent implements Listener {
    public RespawnEvent(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private final JavaPlugin plugin;

    @EventHandler(ignoreCancelled = true)
    public void deathEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("expensivedeaths.bypass")) return;

        for (String cmd : plugin.getConfig().getStringList("bonus.console-commands-on-respawn"))
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                cmd.replace("{PLAYER}", player.getName()).replace("{DISPLAYNAME}", player.getDisplayName()));

        for (String cmd : plugin.getConfig().getStringList("bonus.player-commands-on-respawn"))
            player.performCommand(
                cmd.replace("{PLAYER}", player.getName()).replace("{DISPLAYNAME}", player.getDisplayName()));
    }
}
