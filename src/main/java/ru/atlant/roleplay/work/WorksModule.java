package ru.atlant.roleplay.work;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.repository.RepositoryModule;
import ru.atlant.roleplay.repository.impl.RolePlayData;
import ru.atlant.roleplay.repository.impl.RolePlayDataRepository;
import ru.atlant.roleplay.work.impl.DefaultFraction;
import ru.atlant.roleplay.work.impl.SimpleAbility;
import ru.atlant.roleplay.work.impl.SimpleFraction;
import ru.atlant.roleplay.work.impl.SimpleJob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@LoadAfter(clazz = {RepositoryModule.class})
public class WorksModule implements Module {

    @Getter
    private final Fraction defaultFraction = new DefaultFraction();
    @Getter
    private final Job defaultJob = defaultFraction.getJobs().get(0);

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    private Map<String, Ability> abilityMap = new HashMap<>();
    private Map<String, Fraction> fractionMap = new HashMap<>();

    @Override
    public void onEnable() {
        RolePlayDataRepository repository = registry.get(RepositoryModule.class).getRepository();
        repository.subscribe(this::reload, true);
    }

    private void reload(RolePlayData data) {
        this.abilityMap = data.getAbilities().stream().collect(Collectors.toMap(
                RolePlayData.AbilityData::getId,
                ability -> new SimpleAbility(ability.getId(), ability.getName(), ability.getPermissions())
        ));
        this.fractionMap = data.getFractionData().stream().collect(Collectors.toMap(
                RolePlayData.FractionData::getId,
                frac -> {
                    SimpleFraction fraction = new SimpleFraction(frac.getId(), frac.getName(), null);
                    List<Job> jobs = frac.getJobs().stream().map(job -> {
                        return new SimpleJob(job.getId(), job.getName(), fraction, job.getAbilities().stream().map(abilityMap::get).filter(Objects::nonNull).collect(Collectors.toList()));
                    }).collect(Collectors.toList());
                    fraction.setJobs(jobs);
                    return fraction;
                }
        ));
    }

    public Job find(String fraction, String job) {
        Fraction frac = find(fraction);
        if (frac == null)
            return defaultJob;
        return frac.getJobs().stream().filter(jobObj -> jobObj.getId().equals(job) || jobObj.getName().equals(job)).findFirst().orElse(defaultJob);
    }

    public Fraction find(String fraction) {
        Fraction frac = fractionMap.get(fraction);
        if (frac == null)
            frac = fractionMap.values().stream().filter(fract -> fract.getName().equals(fraction)).findFirst().orElse(null);
        return frac;
    }
}
