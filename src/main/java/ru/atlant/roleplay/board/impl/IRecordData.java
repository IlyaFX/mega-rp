package ru.atlant.roleplay.board.impl;

import java.util.List;

@FunctionalInterface
public interface IRecordData {
    String getValue();

    @FunctionalInterface
    public static interface Multiline {
        List<String> getValue();
    }
}