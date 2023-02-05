package io.oreto.gungnir.cli.prompt;

import io.oreto.gungnir.cli.util.Str;

import java.util.function.Function;

public class DoublePrompt extends Prompt<Double> {
    public static DoublePrompt range(String message, double start, double end) {
        return new DoublePrompt(message, String.format("%f..%f", start, end), n -> n >= start && n <= end);
    }
    public DoublePrompt(String message, String format, Function<Double, Boolean> validate) {
        super(message, Str::toDouble, format, validate);
    }

    public DoublePrompt(String message, String format) {
        super(message, Str::toDouble, format, s -> true);
    }

    public DoublePrompt(String message) {
        super(message, Str::toDouble, null, s -> true);
    }
}
