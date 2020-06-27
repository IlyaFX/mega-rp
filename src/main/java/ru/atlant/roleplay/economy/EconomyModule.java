package ru.atlant.roleplay.economy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.economy.provider.EconomyProvider;
import ru.atlant.roleplay.economy.provider.TestEconomyProvider;
import ru.atlant.roleplay.economy.provider.VaultEconomyProvider;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;

@RequiredArgsConstructor
public class EconomyModule implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    @Getter
    private EconomyProvider provider;

    @Override
    public void onEnable() {
        if (System.getenv("VAULT") != null) {
            provider = new VaultEconomyProvider();
            System.out.println("WARNING! Running with vault economy provider.");
        } else {
            provider = new TestEconomyProvider();
            System.out.println("WARNING! Running with test economy provider.");
        }
    }
}
