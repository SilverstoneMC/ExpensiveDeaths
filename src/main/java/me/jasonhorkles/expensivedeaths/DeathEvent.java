package me.jasonhorkles.expensivedeaths;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {
    final Economy econ = ExpensiveDeaths.getInstance().getEconomy();

    @EventHandler
    public void deathEvent(PlayerDeathEvent event) {
        if (event.getEntity().hasPermission("balanceclearer.bypass")) return;

        econ.withdrawPlayer(event.getEntity(), econ.getBalance(event.getEntity()));
    }
}
