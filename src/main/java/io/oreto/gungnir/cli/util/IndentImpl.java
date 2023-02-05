package io.oreto.gungnir.cli.util;

public class IndentImpl implements Indent {
    static final int INDENT_SIZE = 4;

    private int indentationSize;
    private int indentationLevel;

    public IndentImpl(int indentationSize) {
        this.indentationSize = indentationSize;
        this.indentationLevel = 0;
    }

    public IndentImpl() {
        this(INDENT_SIZE);
    }

    public void increaseIndent() {
        indentationLevel++;
    }

    public void decreaseIndent() {
        if (indentationLevel > 0)
            indentationLevel--;
    }

    @Override
    public int getIndentationSize() {
        return indentationSize;
    }

    @Override
    public int getIndentationLevel() {
        return indentationLevel;
    }

    @Override
    public void setIndentationSize(int indentationSize) {
        this.indentationSize = indentationSize;
    }

    @Override
    public void setIndentationLevel(int indentationLevel) {
        this.indentationLevel = indentationLevel;
    }

    public void setIndentation(Indent indentation) {
        this.indentationSize = indentation.getIndentationSize();
        this.indentationLevel = indentation.getIndentationLevel();
    }

    @Override
    public int spaces() {
        return indentationLevel * indentationSize;
    }
}
