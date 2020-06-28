package ru.atlant.roleplay.board.impl;

import lombok.Getter;
import lombok.Setter;
import ru.atlant.roleplay.util.Colors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleBoardObjective {

    @Getter
    private IServerScoreboardObjective objective;
    @Getter
    private List<Record> lines;
    @Getter
    private int colorId;
    @Getter
    private String displayName;
    @Getter
    @Setter
    private boolean storeInHistory = true;
    private static final int FIRST_RECORD_SLOT = 31;
    private int recordSlot = FIRST_RECORD_SLOT;

    public SimpleBoardObjective(IServerScoreboardObjective objective) {
        this.objective = objective;
        lines = Collections.synchronizedList(new ArrayList<>());
        colorId = 0;
        setDisplayName("§eScoreBoard");
    }

    public void setDisplayName(String displayName) {
        if (objective instanceof ControlledServerScoreboardObjective) {
            this.displayName = displayName;
            return;
        }
        objective.setDisplayName(this.displayName = "   " + displayName + "   ");
    }

    public void update() {
        List<Record> records = this.lines;
        for (int i = 0, j = records.size(); i < j; i++) {
            records.get(i).update();
        }
    }

    private Record record(int slot) {
        return new Record(this, slot);
    }

    public SimpleBoardObjective record(String name, int score) {
        record(recordSlot--).setName(name).setValue(score).build();
        return this;
    }

    public SimpleBoardObjective record(String score) {
        return record("", score);
    }

    public SimpleBoardObjective record(String name, String score) {
        record(recordSlot--).setName(name).setValue(score).build();
        return this;
    }

    public SimpleBoardObjective record(IRecordData recordData) {
        return record("", recordData);
    }

    public SimpleBoardObjective record(String name, IRecordData recordData) {
        record(recordSlot--).setName(name).setRecordData(recordData).build();
        return this;
    }

    public SimpleBoardObjective multiLineRecord(IRecordData.Multiline recordData, int linesReserved) {
        record(recordSlot).setRecordData(recordData, linesReserved).build();
        recordSlot -= linesReserved;
        return this;
    }

    public SimpleBoardObjective startGroup(String group, IRecordData recordData) {
        if (recordSlot != FIRST_RECORD_SLOT)
            emptyLine();
        record(recordSlot--).setName(Colors.cWhite + group).setRecordData(recordData).build();
        return this;
    }

    public SimpleBoardObjective startGroup(String group) {
        if (recordSlot != FIRST_RECORD_SLOT)
            emptyLine();
        record(recordSlot--).setName(Colors.cWhite + group).build();
        return this;
    }

    public SimpleBoardObjective emptyLine() {
        this.colorId++;
        record(recordSlot--).setName(Colors.colorChar + getColorId()).setEmptyLine(true).build();
        return this;
    }

}
