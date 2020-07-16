package io.coti.pot;

import io.coti.pot.interfaces.IAlgorithm;

class TrustScoreDifficulty {

    private final int _trustScore;
    private final String _difficulty;

    public TrustScoreDifficulty(int trustScore, String difficulty) {
        _trustScore = trustScore;
        _difficulty = difficulty;
    }

    public int getTrustScore() {
        return _trustScore;
    }

    public String getDifficulty() {
        return _difficulty;
    }

    static public int getTrustScoreFromSegment(int segmentIndex) {
        return (int) ((100.0 / IAlgorithm.AlgorithmType.values().length) * (segmentIndex + 1));
    }
}
