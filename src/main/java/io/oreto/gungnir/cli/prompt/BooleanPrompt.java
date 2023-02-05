package io.oreto.gungnir.cli.prompt;

import io.oreto.gungnir.cli.util.Str;

public class BooleanPrompt extends Prompt<Boolean> {

    public BooleanPrompt(String message, String format) {
        super(message, Str::toBoolean, format, b -> b);
    }

    public BooleanPrompt(String message) {
        super(message, Str::toBoolean, "true/false", s -> true);
    }
}
