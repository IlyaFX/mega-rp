package ru.atlant.roleplay.work.impl;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.atlant.roleplay.work.Ability;
import ru.atlant.roleplay.work.Fraction;
import ru.atlant.roleplay.work.Job;

import java.util.List;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class SimpleJob implements Job {

    private final String id;
    private final String name;
    private final Fraction fraction;
    private final List<Ability> abilities;

}
