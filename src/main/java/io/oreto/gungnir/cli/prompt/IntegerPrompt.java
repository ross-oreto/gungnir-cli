package io.oreto.gungnir.cli.prompt;

import io.oreto.gungnir.cli.util.Str;

import java.util.function.Function;

public class IntegerPrompt extends Prompt<Integer> {
    public static IntegerPrompt range(String message, int start, int end) {
        return new IntegerPrompt(message, String.format("%d..%d", start, end), n -> n >= start && n <= end);
    }

    public IntegerPrompt(String message, String format, Function<Integer, Boolean> validate) {
        super(message, Str::toInteger, format, validate);
    }

    public IntegerPrompt(String message, String format) {
        super(message, Str::toInteger, format, s -> true);
    }

    public IntegerPrompt(String message) {
        super(message, Str::toInteger, null, s -> true);
    }
}
