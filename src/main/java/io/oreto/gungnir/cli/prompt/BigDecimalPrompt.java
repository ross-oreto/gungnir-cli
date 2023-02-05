package io.oreto.gungnir.cli.prompt;

import io.oreto.gungnir.cli.util.Str;

import java.math.BigDecimal;
import java.util.function.Function;

public class BigDecimalPrompt extends Prompt<BigDecimal> {
    public static BigDecimalPrompt range(String message, BigDecimal start, BigDecimal end) {
        return new BigDecimalPrompt(message
                , String.format("%f..%f", start, end)
                , n -> n.compareTo(start) >= 0 && n.compareTo(end) <= 0);
    }

    public BigDecimalPrompt(String message, String format, Function<BigDecimal, Boolean> validate) {
        super(message, Str::toBigDecimal, format, validate);
    }

    public BigDecimalPrompt(String message, String format) {
        super(message, Str::toBigDecimal, format, s -> true);
    }

    public BigDecimalPrompt(String message) {
        super(message, Str::toBigDecimal, null, s -> true);
    }
}
