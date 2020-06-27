package ru.atlant.roleplay.util;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UserRequester extends Requester<UUID, UUID> {

    public UserRequester(TimeUnit unit, long value) {
        super(unit, value);
    }

}
