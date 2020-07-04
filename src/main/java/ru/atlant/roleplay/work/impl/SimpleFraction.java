package ru.atlant.roleplay.work.impl;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.atlant.roleplay.work.Fraction;
import ru.atlant.roleplay.work.Job;

import java.util.List;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class SimpleFraction implements Fraction {

    private final String id;
    private final String name;
    @Setter
    private List<Job> jobs;

}
