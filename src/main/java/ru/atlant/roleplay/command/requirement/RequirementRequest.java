package ru.atlant.roleplay.command.requirement;

import com.mojang.brigadier.RequirementException;
import ru.atlant.roleplay.util.Requester;

import java.util.UUID;

public class RequirementRequest {

    public static void request(Requester<UUID, ?> requester, UUID user) {
        if (requester.hasRequest(user))
            return;
        throw new RequirementException("У вас нет ожидающих заявок!");
    }

}
