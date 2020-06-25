package ru.atlant.roleplay.work.impl;

import lombok.RequiredArgsConstructor;
import ru.atlant.roleplay.work.Ability;
import ru.atlant.roleplay.work.Fraction;
import ru.atlant.roleplay.work.Job;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class DefaultJob implements Job {

    private final Fraction fraction;

    @Override
    public String getId() {
        return "default";
    }

    @Override
    public String getName() {
        return "Не имеется";
    }

    @Override
    public Fraction getFraction() {
        return fraction;
    }

    @Override
    public List<Ability> getAbilities() {
        return Collections.emptyList();
    }
}
