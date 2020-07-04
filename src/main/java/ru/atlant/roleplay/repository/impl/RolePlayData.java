package ru.atlant.roleplay.repository.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RolePlayData {

    private List<AbilityData> abilities;
    private List<FractionData> fractionData;
    private List<BoardData> boards;
    private Map<String, String> config;

    @Getter
    public static class BoardData {

        private String id, title;
        private List<String> lines;

    }

    @Getter
    public static class AbilityData {

        private String id, name;
        private List<String> permissions;

    }

    @Getter
    public static class FractionData {

        private String id, name;
        private List<JobData> jobs;

    }

    @Getter
    public static class JobData {

        private String id, name;
        private List<String> abilities;

    }

}
