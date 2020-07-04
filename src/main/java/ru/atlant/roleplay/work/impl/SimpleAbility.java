package ru.atlant.roleplay.work.impl;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.atlant.roleplay.work.Ability;

import java.util.List;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class SimpleAbility implements Ability {

    private final String id;
    private final String name;
    private final List<String> permissions;

}
