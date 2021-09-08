package me.jasonhorkles.expensivedeaths;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;

@SuppressWarnings("ConstantConditions")
public class DeathEvent implements Listener {
    private final Economy econ = ExpensiveDeaths.getInstance().getEconomy();

    private final JavaPlugin plugin;

    public DeathEvent(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void deathEvent(PlayerDeathEvent event) {
        if (event.getEntity().hasPermission("expensivedeaths.bypass")) {
            if (!plugin.getConfig().getString("bypass-message").isBlank())
                event.getEntity().sendMessage(
                    ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bypass-message")));
        } else {
            EconomyResponse result;
            String option = plugin.getConfig().getString("amount-to-take");
            DecimalFormat format = new DecimalFormat(plugin.getConfig().getString("currency-format"));

            if (option.equalsIgnoreCase("ALL")) {
                result = econ.withdrawPlayer(event.getEntity(), econ.getBalance(event.getEntity()));

            } else if (option.contains("%")) {
                result = econ.withdrawPlayer(event.getEntity(),
                    (Double.parseDouble(option.replace("%", "")) / 100) * econ.getBalance(event.getEntity()));

            } else {
                result = econ.withdrawPlayer(event.getEntity(), Double.parseDouble(option));
            }

            if (!plugin.getConfig().getString("death-message").isBlank())
                event.getEntity().sendMessage(
                    ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("death-message")
                        .replace("{MONEY}", String.valueOf(format.format(result.amount)))
                        .replace("{BALANCE}", String.valueOf(format.format(result.balance)))));
        }
    }
}
