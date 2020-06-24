package ru.atlant.roleplay.work;

import java.util.List;

public interface Job {

	String getId();

	String getName();

	List<Ability> getAbilities();

}
