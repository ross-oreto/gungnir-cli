package io.oreto.gungnir.cli.prompt.option;

import java.util.Set;

public enum YesNo implements InputOption {
    y {
        @Override
        public Set<String> names() {
            return Set.of("yes");
        }
    }, n {
        @Override
        public Set<String> names() {
            return Set.of("no");
        }
    };

    public boolean toBoolean() {
        return this == y;
    }
}
