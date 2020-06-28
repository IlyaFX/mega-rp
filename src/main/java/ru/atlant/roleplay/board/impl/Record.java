package ru.atlant.roleplay.board.impl;

import lombok.Getter;
import lombok.val;
import org.bukkit.ChatColor;
import ru.atlant.roleplay.util.Colors;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class Record {

    private SimpleBoardObjective iBoardObjective;
    private int slot;
    private String name;
    private String value;
    private IRecordData recordData;
    private String line;
    private boolean emptyLine;

    private boolean multiline = false;
    private int linesReserved = 0;
    private List<String> multilineLines = null;
    private IRecordData.Multiline multilineRecordData;

    public Record(SimpleBoardObjective iBoardObjective, int slot) {
        this.iBoardObjective = iBoardObjective;
        this.slot = slot;
        recordData = null;
    }

    void update() {
        try {
            IServerScoreboardObjective objective = iBoardObjective.getObjective();
            if (multiline) {
                val newLines = getNewLines();
                val oldLines = multilineLines;
                if (newLines.equals(oldLines)) return;
                multilineLines = newLines;
                val oldSize = oldLines.size();
                for (int i = 0; i < linesReserved && i < newLines.size(); i++) {
                    val oldLine = oldSize <= i ? null : oldLines.get(i);
                    if (oldLine == null || !oldLine.equals(newLines.get(i))) {
                        objective.setScore(newLines.get(i), slot - i);
                        if (oldLine != null) {
                            objective.removeScore(oldLine);
                        }
                    }
                }
                for (int i = Math.min(newLines.size(), linesReserved); i < linesReserved && i < oldLines.size(); i++) {
                    objective.removeScore(oldLines.get(i));
                }
            } else {
                val oldLine = line;
                val newLine = getNewLine();
                if (newLine.equals(oldLine)) return;
                line = newLine;
                objective.setScore(newLine, slot);
                if (oldLine != null) {
                    objective.removeScore(oldLine);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Record setEmptyLine(boolean b) {
        emptyLine = b;
        return this;
    }

    public Record setName(String name) {
        this.name = name;
        return this;
    }

    public Record setValue(String value) {
        this.value = value;
        return this;
    }

    public Record setValue(int value) {
        this.value = String.valueOf(value);
        return this;
    }

    public Record setRecordData(IRecordData recordData) {
        this.recordData = recordData;
        return this;
    }

    public SimpleBoardObjective build() {
        iBoardObjective.getLines().add(this);
        return iBoardObjective;
    }

    public String getNewLine() {
        try {
            if (emptyLine) {
                return name;
            }
            if (ChatColor.stripColor(name).isEmpty()) {
                return Colors.cWhite + getNewValue();
            }
            return Colors.cWhite + name + " > " + getNewValue();
        } catch (Throwable t) {
            t.printStackTrace();
            return Colors.cRed + "##SL ERROR##";
        }
    }

    public List<String> getNewLines() {
        try {
            val out = multilineRecordData.getValue();
            out.replaceAll(s -> Colors.cWhite + s);
            return out;
        } catch (Throwable t) {
            t.printStackTrace();
            return Collections.singletonList(Colors.cRed + "##ML ERROR##");
        }
    }

    public boolean hasValue() {
        return multilineRecordData != null || recordData != null || (value != null && !Objects.equals(value, ""));
    }

    public String getNewValue() {
        try {
            return recordData != null ? recordData.getValue() : value;
        } catch (Throwable t) {
            t.printStackTrace();
            return Colors.cRed + "#####";
        }
    }

    public Record setRecordData(IRecordData.Multiline recordData, int linesReserved) {
        multiline = true;
        multilineRecordData = recordData;
        this.linesReserved = linesReserved;
        return this;
    }

}
