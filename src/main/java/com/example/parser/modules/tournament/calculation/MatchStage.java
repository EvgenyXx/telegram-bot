package com.example.parser.modules.tournament.calculation;

public enum MatchStage {

    FINAL {
        @Override
        public boolean matches(String stage) {
            if (stage == null) return false;

            String s = normalize(stage);

            return s.contains("финал") && !s.contains("1/2");
        }
    },

    SEMI_FINAL {
        @Override
        public boolean matches(String stage) {
            if (stage == null) return false;

            String s = normalize(stage);

            return s.contains("1/2")
                    || s.contains("полуфинал")
                    || s.contains("semi");
        }
    };

    public abstract boolean matches(String stage);

    protected String normalize(String stage) {
        return stage.toLowerCase()
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}