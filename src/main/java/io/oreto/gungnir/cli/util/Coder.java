package io.oreto.gungnir.cli.util;

import java.util.*;
import java.util.stream.Collectors;

public class Coder extends IndentImpl {
    public enum Modifiers {
        PUBLIC, PRIVATE, PROTECTED, FINAL, STATIC, ABSTRACT, VOLATILE, TRANSIENT, SYNCHRONIZED
    }

    static final Collection<Modifiers> DEFAULT_MODIFIERS = List.of(Modifiers.PUBLIC);

    public Coder(int indentationSize) {
        super(indentationSize);
    }

    public Coder() {
        super();
    }

    public Method method(String name, Object returnType) {
        Method method = new Method(name, returnType);
        method.setIndentation(this);
        method.getBody().setIndentation(method);
        method.getBody().increaseIndent();
        return method;
    }

    public static class Annotation {
        private final String name;
        private final Map<String, Object> values;

        public Annotation(String name) {
            this.name = name;
            this.values = new LinkedHashMap<>();
        }

        public Annotation withDefault(Object defaultValue) {
            values.put(null, defaultValue);
            return this;
        }

        public Annotation withFieldValue(String name, Object value) {
            values.put(name, value);
            return this;
        }

        @Override
        public String toString() {
            String fieldValues = values.keySet().stream().map(k -> {
                Object value = values.get(k);
                String s;
                if (CharSequence.class.isAssignableFrom(value.getClass())) {
                    s = String.format("\"%s\"", value);
                } else if (Enum.class.isAssignableFrom(value.getClass())) {
                    s = ((Enum<?>) value).name();
                } else {
                    s = values.toString();
                }
                return k == null ? s : String.format("%s = %s", k, s);
            }).collect(Collectors.joining(", "));
            return fieldValues.isEmpty() ? String.format("@%s", name) : String.format("@%s(%s)", name, fieldValues);
        }
    }

    public static class Method extends IndentImpl {
        private final String name;
        private final String returnType;

        private final List<Modifiers> modifiers;

        private final Map<String, String> parameters;

        private final List<Annotation> annotations;

        private final Body body;

        public Method(String name, Object returnType) {
            this.modifiers = new ArrayList<>();
            this.name = name;
            this.returnType = returnType.toString();
            this.parameters = new LinkedHashMap<>();
            this.annotations = new ArrayList<>();
            this.body = new Body();
        }

        public Method annotate(Annotation... annotations) {
            if (annotations.length == 1)
                this.annotations.add(annotations[0]);
            else
                this.annotations.addAll(List.of(annotations));
            return this;
        }

        public Method modifiers(Modifiers... modifiers) {
            if (modifiers.length == 1)
                this.modifiers.add(modifiers[0]);
            else
                this.modifiers.addAll(List.of(modifiers));
            return this;
        }

        public Body getBody() {
            return body;
        }

        @Override
        public String toString() {
            String mods = modifiers.isEmpty()
                    ? DEFAULT_MODIFIERS.stream().map(m -> m.name().toLowerCase()).collect(Collectors.joining(" "))
                    : modifiers.stream().map(m -> m.name().toLowerCase()).collect(Collectors.joining(" "));
            String params = parameters.keySet().stream()
                    .map(k -> String.format("%s %s", parameters.get(k), k))
                    .collect(Collectors.joining(" "));
            String annotate = annotations.stream().map(Annotation::toString).collect(Collectors.joining("\n"));

            String indent = " ".repeat(spaces());
            return """
                    %s%s
                    %s%s %s %s(%s) {
                    %s
                    %s}
                    """.formatted(indent, annotate
                    , indent, mods, returnType, name, params
                    , body.toString()
                    , indent);
        }
    }

    public static class Body extends IndentImpl {
        private final Str str;

        public Body() {
            this.str = Str.of();
            indent();
        }

        public Body ln() {
            str.br();
            return indent();
        }

        public Body indent() {
            str.space(spaces());
            return this;
        }

        public Body statement(String... statements) {
            indent();
            for (String statement : statements) {
                str.add(statement);
            }
            str.add(Str.SEMI);
            return this;
        }

        @Override
        public String toString() {
            return str.toString();
        }
    }
}
