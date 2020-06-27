package ru.atlant.roleplay.economy.provider;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class VaultEconomyProvider implements EconomyProvider {

    private Economy economy;

    public VaultEconomyProvider() {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider == null || (economy = provider.getProvider()) == null) {
            System.out.println("Trying to use vault economy, but got null. Shutdown!");
            Bukkit.shutdown();
        }
    }

    @Override
    public double getBalance(UUID uuid) {
        return economy.getBalance(Bukkit.getOfflinePlayer(uuid));
    }

    @Override
    public void take(UUID uuid, double value) {
        economy.withdrawPlayer(Bukkit.getOfflinePlayer(uuid), value);
    }

    @Override
    public void deposit(UUID uuid, double value) {
        economy.depositPlayer(Bukkit.getOfflinePlayer(uuid), value);
    }
}
