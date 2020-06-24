package ru.atlant.roleplay.repository.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.atlant.roleplay.work.Ability;
import ru.atlant.roleplay.work.Fraction;
import ru.atlant.roleplay.work.Job;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RolePlayData {

	private List<FractionData> fractionData;

	@Getter
	public static class AbilityData implements Ability {

		private String id, name;
		private List<String> permissions;

	}

	@Getter
	public static class FractionData implements Fraction {

		private String id, name;
		private List<Job> jobs;

	}

	@Getter
	public static class JobData implements Job {

		private String id, name;
		private List<Ability> abilities;

	}

}
