package me.jasonhorkles.expensivedeaths;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.LocaleUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@SuppressWarnings("DataFlowIssue")
public class DeathEvent implements Listener {
    public DeathEvent(ExpensiveDeaths plugin) {
        this.plugin = plugin;
    }

    private final Economy econ = ExpensiveDeaths.getInstance().getEconomy();
    private final Permission perms = ExpensiveDeaths.getInstance().getPermissions();
    private final ExpensiveDeaths plugin;

    @EventHandler(ignoreCancelled = true)
    public void deathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Check if the player has the bypass permission
        if (player.hasPermission("expensivedeaths.bypass")) {
            if (!plugin.getConfig().getString("bypass-message").isBlank())
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("bypass-message")));
            return;
        }

        // Get the player's primary group, or "default" if they don't have one from the config
        String primaryGroup = perms.getPrimaryGroup(null, player).toLowerCase();
        if (!plugin.getConfig().contains("amount-to-take." + primaryGroup)) primaryGroup = "default";

        EconomyResponse result;
        String option = plugin.getConfig().getString("amount-to-take." + primaryGroup);
        DecimalFormat format = new DecimalFormat(plugin.getConfig().getString("currency-format"));
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(LocaleUtils.toLocale("en_" + plugin
            .getConfig().getString("currency-country"))));

        // Empty balance
        if (option.equalsIgnoreCase("ALL")) result = econ.withdrawPlayer(player, econ.getBalance(player));
            // Random percent range
        else if (option.contains("%")) if (option.contains("-")) {
            double min = Double.parseDouble(option.replaceAll("-.*", ""));
            double max = Double.parseDouble(option.replaceAll(".*-", "").replace("%", ""));
            double r = ThreadLocalRandom.current().nextDouble(min, max + 1);
            result = econ.withdrawPlayer(player, (r / 100) * econ.getBalance(player));
            // Percent
        } else result = econ.withdrawPlayer(player,
            (Double.parseDouble(option.replace("%", "")) / 100) * econ.getBalance(player));
            // Fixed amount
        else result = econ.withdrawPlayer(player, Double.parseDouble(option));

        // Send the death message
        final String money = String.valueOf(format.format(result.amount));
        final String balance = String.valueOf(format.format(result.balance));
        if (!plugin.getConfig().getString("death-message").isBlank())
            if (!money.equals("0.00")) player.sendMessage(ChatColor.translateAlternateColorCodes(
                '&',
                plugin.getConfig().getString("death-message").replace("{MONEY}", money)
                    .replace("{BALANCE}", balance)));

        // Run the executions
        Player killer = player.getKiller();
        final Function<String, String> parser = str -> {
            String s = str.replace("{PLAYER}", player.getName()).replace("{DISPLAYNAME}",
                player.getDisplayName()).replace("{MONEY}", money).replace("{BALANCE}", balance);
            if (killer != null) s = s.replace("{KILLER}", killer.getName()).replace("{KILLER_DISPLAYNAME}",
                killer.getDisplayName());
            return s;
        };

        plugin.run(Execution.Type.DEATH_CONSOLE, player, killer, parser);
        plugin.run(Execution.Type.DEATH_PLAYER, player, killer, parser);
        if (killer != null) {
            plugin.run(Execution.Type.KILL_CONSOLE, player, killer, parser);
            plugin.run(Execution.Type.KILL_PLAYER, player, killer, parser);
        }
    }
}
