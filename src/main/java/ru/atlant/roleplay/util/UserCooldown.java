package ru.atlant.roleplay.util;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UserCooldown extends Cooldown<UUID> {

    public UserCooldown(TimeUnit unit, long val) {
        super(unit, val);
    }

}
