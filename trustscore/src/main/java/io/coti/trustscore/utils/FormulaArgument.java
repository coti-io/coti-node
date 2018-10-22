package io.coti.trustscore.utils;

import lombok.Data;

@Data
public class FormulaArgument {
    private String argument;
    private double value;

    public FormulaArgument(String argument, double value) {
        this.argument = argument;
        this.value = value;
    }

}
