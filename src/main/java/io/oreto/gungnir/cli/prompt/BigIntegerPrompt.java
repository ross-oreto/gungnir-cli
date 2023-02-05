package io.oreto.gungnir.cli.prompt;

import io.oreto.gungnir.cli.util.Str;

import java.math.BigInteger;
import java.util.function.Function;

public class BigIntegerPrompt extends Prompt<BigInteger> {
    public static BigIntegerPrompt range(String message, BigInteger start, BigInteger end) {
        return new BigIntegerPrompt(message
                , String.format("%d..%d", start, end)
                , n -> n.compareTo(start) >= 0 && n.compareTo(end) <= 0);
    }
    public BigIntegerPrompt(String message, String format, Function<BigInteger, Boolean> validate) {
        super(message, Str::toBigInteger, format, validate);
    }

    public BigIntegerPrompt(String message, String format) {
        super(message, Str::toBigInteger, format, s -> true);
    }

    public BigIntegerPrompt(String message) {
        super(message, Str::toBigInteger, null, s -> true);
    }
}
