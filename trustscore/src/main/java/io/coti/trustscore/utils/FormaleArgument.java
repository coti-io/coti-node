package io.coti.trustscore.utils;

import lombok.Data;

@Data
public class FormaleArgument {
    private String argument;
    private double value;

    public FormaleArgument(String argument, double value) {
        this.argument = argument;
        this.value = value;
    }

}
