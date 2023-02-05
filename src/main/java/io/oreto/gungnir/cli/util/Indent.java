package io.oreto.gungnir.cli.util;

public interface Indent {
    int getIndentationSize();
    int getIndentationLevel();
    void setIndentationSize(int indentationSize);
    void setIndentationLevel(int indentationLevel);
    int spaces();

    void increaseIndent();
    void decreaseIndent();
}
