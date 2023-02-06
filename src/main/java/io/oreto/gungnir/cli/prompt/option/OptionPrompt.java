package io.oreto.gungnir.cli.prompt.option;

import io.oreto.gungnir.cli.util.Str;

import java.util.*;
import java.util.stream.Collectors;

public class OptionPrompt<T extends Enum<T> & InputOption> {
    @SafeVarargs
    public static <T extends Enum<T> & InputOption> OptionPrompt<T> create(String message, T... options) {
        return new OptionPrompt<>(message, options);
    }

    public static OptionPrompt<YesNo> yesNo(String message) {
        return new OptionPrompt<>(message, YesNo.values()).defaultTo(YesNo.y);
    }

    static final Set<String> NO_NAMES = Set.of();

    private final String message;

    private T defaultOption;

    private Boolean numberOptions;
    private final Set<T> options;

    private final StringBuilder sb;

    @SafeVarargs
    protected OptionPrompt(String message, T... options) {
        this.message = message;
        this.sb = new StringBuilder();
        this.options = new LinkedHashSet<>(List.of(options));
        this.numberOptions = null;
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
                   next = defaultOption;
                } else {
                    Optional<Integer> id = Str.toInteger(value);
                    if (!options.isEmpty()) {
                        if (id.isPresent() && isNumbered())
                            next = options.stream().filter(option -> option.match(id.get())).findFirst().orElse(null);
                        else
                            next = options.stream().filter(option -> option.match(value)).findFirst().orElse(null);
                        if (next == null)
                            System.out.printf("invalid option: %s%n", value);
                    }
                }
                // If we have input and no options
                if (next == null && options.isEmpty() && !value.isBlank())
                    System.out.println("No options to select");
            }
        }
        return next;
    }

    public String getInputString() {
        return getInput().toString();
    }

    protected boolean isNumbered() {
        return numberOptions == null ? options.size() > 2 : numberOptions;
    }

    protected String promptMessage() {
        int optionSize = options.size();
        if (optionSize == 0)
            return String.format("%s: ", message);

        sb.append(message);
        if (isNumbered()) {
            sb.append(':').append('\n');
            for (InputOption option : options) {
                sb.append(' ')
                        .append(option.ordinal() + 1).append(':')
                        .append(' ')
                        .append(option.name());
                if (Objects.nonNull(option.description())) {
                    sb.append(" [ ").append(option.description()).append(" ]");
                }
                sb.append('\n');
            }
            sb.append("enter selection");
            if (Objects.nonNull(defaultOption))
                sb.append(' ').append('(').append("default: ").append(defaultOption).append(')');
            sb.append(' ');
        } else {
            Collection<String> displayOptions = options.stream()
                    .map(option -> option == defaultOption
                            ? defaultOption.length() == 1 ? defaultOption.name().toUpperCase()
                                : String.format("%c%s", '➤', defaultOption.name())
                            : option.name())
                    .collect(Collectors.toList());
            sb.append(' ')
                    .append('[')
                    .append(String.join(optionSize == 2 ? "/" : "", displayOptions))
                    .append("]: ");
        }
        String message = sb.toString();
        sb.setLength(0);
        return message;
    }

    public OptionPrompt<T> defaultTo(T option) {
        this.defaultOption = option;
        return this;
    }

    public OptionPrompt<T> numberOptions(Boolean numberOptions) {
        this.numberOptions = numberOptions;
        return this;
    }
}
