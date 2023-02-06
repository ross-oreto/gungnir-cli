package io.oreto.gungnir.cli.prompt.option;

import java.util.Set;

public interface InputOption extends CharSequence {
    String name();

    default String description() {
        return null;
    }

    default boolean ignoreCase() {
        return true;
    }

    default Set<String> names() {
        return OptionPrompt.NO_NAMES;
    }

    int ordinal();

    default boolean match(String value) {
        if (ignoreCase()) {
            return value.equalsIgnoreCase(name()) || names().stream().anyMatch(value::equalsIgnoreCase);
        } else {
            return value.equals(name()) || names().stream().anyMatch(value::equals);
        }
    }

    default boolean match(int value) {
        return (value - 1) == ordinal();
    }

    @Override
    default int length() {
        return name().length();
    }

    @Override
    default char charAt(int index) {
        return name().charAt(index);
    }

    @Override
    default CharSequence subSequence(int start, int end) {
        return name().subSequence(start, end);
    }
}
