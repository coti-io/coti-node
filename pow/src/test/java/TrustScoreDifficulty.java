import coti.crypto.IAlgorithm;

class TrustScoreDifficulty {
    private int _trustScore;
    private String _difficulty;

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
        return (int)((100.0 / IAlgorithm.AlgorithmTypes.values().length) * (segmentIndex + 1));
    }
}