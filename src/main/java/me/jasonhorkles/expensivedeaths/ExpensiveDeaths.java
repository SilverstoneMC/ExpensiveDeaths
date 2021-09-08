package me.jasonhorkles.expensivedeaths;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ExpensiveDeaths extends JavaPlugin implements Listener {

    private Economy econ;
    private static ExpensiveDeaths instance;

    public static ExpensiveDeaths getInstance() {
        return instance;
    }

    // Startup
    @Override
    public void onEnable() {
        instance = this;

        setupEconomy();

        getServer().getPluginManager().registerEvents(new DeathEvent(), this);
    }

    public Economy getEconomy() {
        return econ;
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }
}
