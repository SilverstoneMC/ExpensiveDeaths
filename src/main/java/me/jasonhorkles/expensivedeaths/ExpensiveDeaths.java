package me.jasonhorkles.expensivedeaths;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ExpensiveDeaths extends JavaPlugin implements Listener, CommandExecutor {
    public static ExpensiveDeaths getInstance() {
        return instance;
    }

    private Economy econ;
    private final Map<Execution.Type, Execution> executions = new HashMap<>();
    private static ExpensiveDeaths instance;

    @Override
    public void onEnable() {
        instance = this;

        setupEconomy();
        saveDefaultConfig();
        loadExecutions();

        getServer().getPluginManager().registerEvents(new DeathEvent(this), this);
        getServer().getPluginManager().registerEvents(new RespawnEvent(this), this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        saveDefaultConfig();
        reloadConfig();
        loadExecutions();

        sender.sendMessage(ChatColor.GREEN + "ExpensiveDeaths reloaded!");
        return true;
    }

    public Economy getEconomy() {
        return econ;
    }

    public void run(Execution.Type type, Player player, Player agent, Function<String, String> parser) {
        final Execution execution = this.executions.get(type);
        if (execution != null) execution.run(player, agent, parser, type.isConsole());
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager()
            .getRegistration(Economy.class);
        if (rsp == null) return;
        econ = rsp.getProvider();
    }

    private void loadExecutions() {
        this.executions.clear();
        loadExecution(Execution.Type.DEATH_CONSOLE, "console-commands-on-death");
        loadExecution(Execution.Type.DEATH_PLAYER, "player-commands-on-death");
        loadExecution(Execution.Type.KILL_CONSOLE, "console-commands-on-killed");
        loadExecution(Execution.Type.KILL_PLAYER, "player-commands-on-killed");
        loadExecution(Execution.Type.RESPAWN_CONSOLE, "console-commands-on-respawn");
        loadExecution(Execution.Type.RESPAWN_PLAYER, "player-commands-on-respawn");
    }

    private void loadExecution(Execution.Type type, String key) {
        final Execution execution = Execution.of(getConfig().get("bonus." + key));
        if (execution != null) this.executions.put(type, execution);
    }
}
