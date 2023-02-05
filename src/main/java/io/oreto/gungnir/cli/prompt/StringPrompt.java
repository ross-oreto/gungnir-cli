package io.oreto.gungnir.cli.prompt;

import java.util.Optional;
import java.util.function.Function;

public class StringPrompt extends Prompt<String> {
    public static StringPrompt range(String message, String start, String end) {
        return new StringPrompt(message
                , String.format("%s..%s", start, end)
                , s -> s.compareTo(start) >= 0 && s.compareTo(end) <= 0);
    }

    public StringPrompt(String message
            , String format
            , Function<String, Boolean> validate
    ) {
        super(message, Optional::of, format, validate);
    }

    public StringPrompt(String message, String format) {
        super(message, Optional::of, format, s -> true);
    }

    public StringPrompt(String message) {
        super(message, Optional::of, null, s -> true);
    }
}
