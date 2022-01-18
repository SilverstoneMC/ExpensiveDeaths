package me.jasonhorkles.expensivedeaths;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.apache.commons.lang.LocaleUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.ThreadLocalRandom;

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
            format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(
                LocaleUtils.toLocale("en_" + plugin.getConfig().getString("currency-country"))));

            if (option.equalsIgnoreCase("ALL"))
                result = econ.withdrawPlayer(event.getEntity(), econ.getBalance(event.getEntity()));
            else if (option.contains("%")) if (option.contains("-")) {
                double min = Double.parseDouble(option.replaceAll("-.*", ""));
                double max = Double.parseDouble(option.replaceAll(".*-", "").replace("%", ""));
                double r = ThreadLocalRandom.current().nextDouble(min, max + 1);
                result = econ.withdrawPlayer(event.getEntity(), (r / 100) * econ.getBalance(event.getEntity()));
            } else result = econ.withdrawPlayer(event.getEntity(),
                (Double.parseDouble(option.replace("%", "")) / 100) * econ.getBalance(event.getEntity()));
            else result = econ.withdrawPlayer(event.getEntity(), Double.parseDouble(option));

            if (!plugin.getConfig().getString("death-message").isBlank())
                event.getEntity().sendMessage(
                    ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("death-message")
                        .replace("{MONEY}", String.valueOf(format.format(result.amount)))
                        .replace("{BALANCE}", String.valueOf(format.format(result.balance)))));


            for (String cmd : plugin.getConfig().getStringList("bonus.console-commands"))
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{PLAYER}", event.getEntity().getName())
                    .replace("{DISPLAYNAME}", event.getEntity().getDisplayName()));
        }
    }
}
