package ru.atlant.roleplay.command.requirement;

import ru.atlant.roleplay.util.Requester;

import java.util.UUID;
import java.util.function.Predicate;

public class RequirementRequest implements Predicate<UUID> {

    private final Requester<UUID, ?> requester;

    public RequirementRequest(Requester<UUID, ?> requester) {
        this.requester = requester;
    }

    public static RequirementRequest request(Requester<UUID, ?> requester) {
        return new RequirementRequest(requester);
    }

    @Override
    public boolean test(UUID uuid) {
        return requester.hasRequest(uuid);
    }
}
