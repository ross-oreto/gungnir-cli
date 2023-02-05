package io.oreto.gungnir.cli.prompt;

import io.oreto.gungnir.cli.util.Str;

import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;

public class Prompt<T> {
    private final String message;
    private final String format;

    private final Function<String, Optional<T>> transform;

    private final Function<T, Boolean> validate;

    private T defaultValue;

    private final StringBuilder sb;

    public Prompt(String message
            , Function<String, Optional<T>> transform
            , String format
            , Function<T, Boolean> validate) {
        this.message = message;
        this.format = format;
        this.transform = transform;
        this.validate = validate;
        this.sb = new StringBuilder();
    }

    protected String promptMessage() {
        sb.append(message);
        if (!Str.isEmpty(format)) {
            sb.append(' ').append('[').append(format).append(']');
        }
        if (Objects.nonNull(defaultValue)) {
            sb.append(' ').append('(').append("default: ").append(defaultValue).append(')');
        }
        sb.append(':').append(' ');
        String message = sb.toString();
        sb.setLength(0);
        return message;
    }

    public T getInput() {
        String promptMessage = promptMessage();
        T next = null;
        Scanner scanner = new Scanner(System.in);
        while (next == null) {
            System.out.print(promptMessage);

            if (scanner.hasNextLine()) {
                String value = scanner.nextLine().trim();
                if (value.isBlank()) {
                    next = defaultValue;
                } else {
                    next = transform.apply(value).orElse(null);
                }
                if (Objects.nonNull(next) && !validate.apply(next)) {
                    next = null;
                    System.out.printf("invalid value: %s", value);
                }
            }
        }
        return next;
    }

    public Prompt<T> defaultTo(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
}
