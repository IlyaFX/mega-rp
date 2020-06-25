package ru.atlant.roleplay.work.impl;

import ru.atlant.roleplay.work.Fraction;
import ru.atlant.roleplay.work.Job;

import java.util.Collections;
import java.util.List;

public class DefaultFraction implements Fraction {

    @Override
    public String getId() {
        return "default";
    }

    @Override
    public String getName() {
        return "Не имеется";
    }

    @Override
    public List<Job> getJobs() {
        return Collections.singletonList(new DefaultJob(this));
    }
}
