package ru.atlant.roleplay.economy.provider;

import java.util.UUID;

public class TestEconomyProvider implements EconomyProvider {

    @Override
    public double getBalance(UUID uuid) {
        return Math.pow(1000, 3);
    }

    @Override
    public void take(UUID uuid, double value) {
    }

    @Override
    public void deposit(UUID uuid, double value) {
    }

}
