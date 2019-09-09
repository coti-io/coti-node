package io.coti.trustscore.config.rules;

import lombok.Data;

import java.util.List;

@Data
public class ScoreRules {

    private String name;
    private List<UserScoreRules> users;

}
