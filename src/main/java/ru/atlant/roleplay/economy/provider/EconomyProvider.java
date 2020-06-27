package ru.atlant.roleplay.economy.provider;

import java.util.UUID;

public interface EconomyProvider {

    double getBalance(UUID uuid);

    void take(UUID uuid, double value);

    void deposit(UUID uuid, double value);

    default boolean has(UUID uuid, double value) {
        return getBalance(uuid) >= value;
    }

}
