package io.oreto.gungnir.cli.util;

/**
 * Utility methods for numbers
 */
public class Num {
    /**
     * Defines the type of numbers
     */
    public enum Type {
        natural     // 1, 2, 3, 4...
        , whole     // 0, 1, 2, 3...
        , integer   // -3, -2, -1, 0, 1, 2, 3
        , rational  // -1.1, -.5, 0, 1, 1.5, 2...
    }
}
