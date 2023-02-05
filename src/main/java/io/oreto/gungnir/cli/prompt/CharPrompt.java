package io.oreto.gungnir.cli.prompt;

import io.oreto.gungnir.cli.util.Str;

import java.util.function.Function;

public class CharPrompt extends Prompt<Character> {
    public static CharPrompt range(String message, char start, char end) {
        return new CharPrompt(message
                , String.format("%s..%s", start, end)
                , c -> c >= start && c <= end);
    }

    public CharPrompt(String message
            , String format
            , Function<Character, Boolean> validate
    ) {
        super(message, Str::toChar, format, validate);
    }

    public CharPrompt(String message, String format) {
        super(message, Str::toChar, format, s -> true);
    }

    public CharPrompt(String message) {
        super(message, Str::toChar, null, s -> true);
    }
}
