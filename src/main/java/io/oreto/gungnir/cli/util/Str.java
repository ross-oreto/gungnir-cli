package io.oreto.gungnir.cli.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Str implements CharSequence, java.io.Serializable, Comparable<CharSequence> {
    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final String DASH = "-";
    public static final String DOT = ".";
    public static final String SLASH = "/";
    public static final String UNDER_SCORE = "_";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String EQUALS = "=";

    public static final String SEMI = ";";
    protected static final List<CharSequence> emptyList = new ArrayList<>();

    /**
     * Static variables for characters
     */
    public static class Chars {
        public static final char NEGATIVE = '-';
        public static final char POSITIVE = '+';
        public static final char DECIMAL = '.';
        public static final char SPACE = ' ';
        public static final char DASH = '-';
        public static final char SLASH = '/';
        public static final char UNDER_SCORE = '_';
        public static final char ZERO = '0';
        public static final char EQUALS = '=';
    }

    /**
     * Compares two {@code CharSequence} instances lexicographically. Returns a
     * negative value, zero, or a positive value if the first sequence is lexicographically
     * less than, equal to, or greater than the second, respectively.
     *
     * <p>
     * The lexicographical ordering of {@code CharSequence} is defined as follows.
     * Consider a {@code CharSequence} <i>cs</i> of length <i>len</i> to be a
     * sequence of char values, <i>cs[0]</i> to <i>cs[len-1]</i>. Suppose <i>k</i>
     * is the lowest index at which the corresponding char values from each sequence
     * differ. The lexicographic ordering of the sequences is determined by a numeric
     * comparison of the char values <i>cs1[k]</i> with <i>cs2[k]</i>. If there is
     * no such index <i>k</i>, the shorter sequence is considered lexicographically
     * less than the other. If the sequences have the same length, the sequences are
     * considered lexicographically equal.
     *
     * @param cs1 the first {@code CharSequence}
     * @param cs2 the second {@code CharSequence}
     *
     * @return  the value {@code 0} if the two {@code CharSequence} are equal;
     *          a negative integer if the first {@code CharSequence}
     *          is lexicographically less than the second; or a
     *          positive integer if the first {@code CharSequence} is
     *          lexicographically greater than the second.
     */
    @SuppressWarnings("unchecked")
    public static int compare(CharSequence cs1, CharSequence cs2) {
        if (Objects.requireNonNull(cs1) == Objects.requireNonNull(cs2)) {
            return 0;
        }

        if (cs1.getClass() == cs2.getClass() && cs1 instanceof Comparable) {
            return ((Comparable<Object>) cs1).compareTo(cs2);
        }

        for (int i = 0, len = Math.min(cs1.length(), cs2.length()); i < len; i++) {
            char a = cs1.charAt(i);
            char b = cs2.charAt(i);
            if (a != b) {
                return a - b;
            }
        }

        return cs1.length() - cs2.length();
    }

    /**
     * Determine if a given string is numeric.
     * This is better than relying on something like Integer.parseInt
     * to throw an Exception which has to be part of the normal method behavior.
     * Also, better than a regular expression which is more difficult to maintain.
     * @param s A string.
     * @param type the type of number, natural, whole, integer, rational.
     * @return Return true if the value is numeric, false otherwise.
     */
    public static boolean isNumber(CharSequence s, Num.Type type) {
        // do some initial sanity checking to catch easy issues quickly and with very little processing
        // also this makes the state tracking easier below
        if (s == null)
            return false;

        int length = s.length();
        if (isBlank(s)
                || (length == 1 && !Character.isDigit(s.charAt(0)))
                ||  s.charAt(length - 1) == Chars.DECIMAL)
            return false;

        boolean dotted = false;
        int[] arr = s.chars().toArray();

        int len = arr.length;
        for (int i = 0; i < len; i++) {
            char c = (char) arr[i];

            switch (c) {
                // +/- can only be in the start position
                case Chars.NEGATIVE -> {
                    if (i > 0) return false;
                    if (type == Num.Type.natural || type == Num.Type.whole) return false;
                }
                case Chars.POSITIVE -> {
                    if (i > 0) return false;
                }
                case Chars.DECIMAL -> {
                    // only one decimal place allowed
                    if (dotted || type != Num.Type.rational) return false;
                    dotted = true;
                }
                default -> {
                    // this better be a digit
                    if (!Character.isDigit(c))
                        return false;
                }
            }
        }
        // make sure natural number type isn't assigned a 0
        return type != Num.Type.natural || !s.chars().allMatch(it -> (char) it == Chars.ZERO);
    }

    /**
     * Determine if the string is null or empty
     * @param s The string to test
     * @return True if the string is null or empty, false otherwise
     */
    public static boolean isEmpty(final CharSequence s) {
        return s == null || s.length() == 0;
    }

    /**
     * Determine if the string is not empty. Negation of <code>Str.isEmpty(s)</code>
     * @param s The string to test
     * @return True if the string is not empty, false otherwise
     */
    public static boolean isNotEmpty(final CharSequence s) {
        return !isEmpty(s);
    }

    /**
     * Determine if the string is blank
     * @param s The string to test
     * @return True if the string is blank (something other than whitespace)
     */
    public static boolean isBlank(CharSequence s) {
       return isEmpty(s) || s.chars().allMatch(Character::isWhitespace);
    }

    /**
     * Determine if the string is a number
     * @param s The string to test
     * @return True if the string is a valid number, false otherwise
     */
    public static boolean isNumber(CharSequence s) {
        return isNumber(s, Num.Type.rational);
    }

    /**
     * Determine if the string is an integer
     * @param s The string to test
     * @return True if the string is a valid integer, false otherwise
     */
    public static boolean isInteger(CharSequence s) {
        return isNumber(s, Num.Type.integer);
    }

    /**
     * Determine if the string is a boolean
     * @param s The string to test
     * @return True if the string is a valid boolean, false otherwise
     */
    public static boolean isBoolean(CharSequence s) {
        return s != null && (s.equals(TRUE) || s.equals(FALSE));
    }

    /**
     * Convert string to an optional boolean
     * @param s The string to convert
     * @return Optional boolean if the string is a valid boolean, Optional.empty otherwise
     */
    public static Optional<Boolean> toBoolean(CharSequence s) {
        try {
            return isBoolean(s) ? Optional.of(Boolean.parseBoolean(s.toString())) : Optional.empty();
        } catch (Exception ignored) { }
        return Optional.empty();
    }

    /**
     * Convert string to an optional integer
     * @param s The string to convert
     * @return Optional integer if the string is a valid integer, Optional.empty otherwise
     */
    public static Optional<Integer> toInteger(CharSequence s) {
        try {
            return isInteger(s) ? Optional.of(Integer.parseInt(s.toString())) : Optional.empty();
        } catch (NumberFormatException ignored) { }
        return Optional.empty();
    }

    /**
     * Convert string to an optional long
     * @param s The string to convert
     * @return Optional long if the string is a valid long, Optional.empty otherwise
     */
    public static Optional<Long> toLong(CharSequence s) {
        try {
            return isNumber(s) ? Optional.of(Long.parseLong(s.toString())) : Optional.empty();
        } catch (NumberFormatException ignored) { }
        return Optional.empty();
    }

    /**
     * Convert string to an optional double
     * @param s The string to convert
     * @return Optional double if the string is a valid double, Optional.empty otherwise
     */
    public static Optional<Double> toDouble(CharSequence s) {
        try {
            return isNumber(s) ? Optional.of(Double.parseDouble(s.toString())) : Optional.empty();
        } catch (NumberFormatException ignored) { }
        return Optional.empty();
    }

    /**
     * Convert string to an optional big integer
     * @param s The string to convert
     * @return Optional big integer if the string is a valid integer, Optional.empty otherwise
     */
    public static Optional<BigInteger> toBigInteger(CharSequence s) {
        try {
            return isNumber(s) ? Optional.of(new BigInteger(s.toString())) : Optional.empty();
        } catch (NumberFormatException ignored) { }
        return Optional.empty();
    }

    /**
     * Convert string to an optional big decimal
     * @param s The string to convert
     * @return Optional big decimal if the string is a valid decimal, Optional.empty otherwise
     */
    public static Optional<BigDecimal> toBigDecimal(CharSequence s) {
        try {
            return isNumber(s) ? Optional.of(new BigDecimal(s.toString())) : Optional.empty();
        } catch (NumberFormatException ignored) { }
        return Optional.empty();
    }

    /**
     * Convert string to an optional character
     * @param s The string to convert
     * @return Optional character if the string is a valid char, Optional.empty otherwise
     */
    public static Optional<Character> toChar(CharSequence s) {
        try { return Optional.of(s.charAt(0)); } catch (Exception ignored) { return Optional.empty(); }
    }

    /**
     * Convert string to a character array
     * @param s The string to convert
     * @return An array of type character <tt>char[]</tt>
     */
    public static char[] toArray(CharSequence s) {
        int len = s.length();
        char[] arr = new char[len];
        for (int i = 0; i < len; i++) {
            arr[i] = s.charAt(i);
        }
        return arr;
    }

    /**
     * Replace all regex matches with a replacement string
     * @param s The string to replace
     * @param regex The regex to match with
     * @param replacement The string to replace the search string with
     * @param max The maximum amount of replacements to make in the string
     * @return The resulting string after replacement
     */
    public static String replace(CharSequence s, CharSequence regex, CharSequence replacement, int max) {
        return Str.of(s).replace(regex, replacement, max).toString();
    }

    /**
     * Replace all regex matches with a replacement string
     * @param s The string to replace
     * @param regex The regex to match with
     * @param replacement The string to replace the search string with
     * @return The resulting string after replacement
     */
    public static String replace(CharSequence s, CharSequence regex, CharSequence replacement) {
        return Str.of(s).replace(regex, replacement).toString();
    }

    static private Map<Integer, List<CharSequence>> groupBySizes(CharSequence[] search) {
        Map<Integer, List<CharSequence>> strings = new WeakHashMap<>();
        Arrays.stream(search).forEach(it -> {
            if (strings.containsKey(it.length()))
                strings.get(it.length()).add(it);
            else
                strings.put(it.length(), new ArrayList<>() {{
                    add(it);
                }});
        });
        return strings;
    }

    // --------------------------------- WORDS ---------------------------------
    static final char[] delimiters = new char[] { Chars.SPACE, Chars.DASH, Chars.DECIMAL, Chars.UNDER_SCORE};
    static boolean isDelimiter(char c) { return Arrays.binarySearch(delimiters, c) > -1; }

    /**
     * Capitalize the given string
     * @param s The string to capitalize
     * @return The resulting capitalized string
     */
    public static String capitalize(CharSequence s) {
        return Str.of(s).capitalize().toString();
    }

    /**
     * Convert the given string into kebab casing
     * @param s The string to kebab case
     * @return The resulting string in kebab case
     */
    public static String toKebab(CharSequence s) {
        return Str.of(s).toKebab().toString();
    }

    // --------------------------------- static constructors ---------------------------------
    /**
     * New Str object initialized with any specified strings
     * @param s Any number of strings
     * @return The new Str object
     */
    public static Str of(CharSequence... s) {
        return new Str(s);
    }

    /**
     * Crate new Str object initialized with given character
     * @param c The character to add to this string
     * @return The new Str object
     */
    public static Str of(char c) {
        return new Str(c);
    }

    /**
     * Create new Str object with the given capacity
     * @param capacity The capacity of the Str object
     * @return The new Str object
     */
    public static Str of(int capacity) {
        return new Str(capacity);
    }

    /**
     * Defines how to slice a string
     * INCLUDE = include the beginning and end index
     * INCLUDE_EXCLUDE = include the beginning index, exclude the end index
     * EXCLUDE_INCLUDE = exclude the beginning index, include the end index
     * EXCLUDE = exclude the beginning and end index
     */
    public enum Slice {
        INCLUDE, INCLUDE_EXCLUDE, EXCLUDE_INCLUDE, EXCLUDE
    }

    // --------------------------------- END STATIC ---------------------------------

    private final StringBuilder sb;

    private Str(CharSequence... charSequences) {
        this.sb = new StringBuilder();
        add(charSequences);
    }
    private Str(int capacity) {
        this.sb = new StringBuilder(capacity);
    }

    /**
     * Returns the length (character count).
     *
     * @return the length of the sequence of characters currently
     *          represented by this object
     */
    @Override
    public int length() {
        return sb.length();
    }

    /**
     * Returns the {@code char} value in this sequence at the specified index.
     * The first {@code char} value is at index {@code 0}, the next at index
     * {@code 1}, and so on, as in array indexing.
     * <p>
     * The index argument must be greater than or equal to
     * {@code 0}, and less than the length of this sequence.
     *
     * <p>If the {@code char} value specified by the index is a
     * <a href="Character.html#unicode">surrogate</a>, the surrogate
     * value is returned.
     *
     * @param      index   the index of the desired {@code char} value.
     * @return     the {@code char} value at the specified index.
     * @throws     IndexOutOfBoundsException  if {@code index} is
     *             negative or greater than or equal to {@code length()}.
     */
    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     *
     * <p> An invocation of this method of the form
     *
     * <pre>{@code
     * sb.subSequence(begin,&nbsp;end)}</pre>
     *
     * behaves in exactly the same way as the invocation
     *
     * <pre>{@code
     * sb.substring(begin,&nbsp;end)}</pre>
     *
     * This method is provided so that this class can
     * implement the {@link CharSequence} interface.
     *
     * @param      start   the start index, inclusive.
     * @param      end     the end index, exclusive.
     * @return     the specified subsequence.
     *
     * @throws  IndexOutOfBoundsException
     *          if {@code start} or {@code end} are negative,
     *          if {@code end} is greater than {@code length()},
     *          or if {@code start} is greater than {@code end}
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    /**
     * @return A new String representing this Str object
     */
    @Override
    public String toString() {
        return sb.toString();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * @param   obj   the reference object with which to compare.
     * @return  {@code true} if this object is the same as the obj
     *          argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof CharSequence && this.compareTo((CharSequence) obj) == 0;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * @param   o the object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *          is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(CharSequence o) {
        return Str.compare(this, o);
    }

    /**
     * Convert the Str to a byte array
     * @return The byte array representing this Str object
     */
    public byte[] getBytes() {
        char[] chars = toArray();
        ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] b = new byte[bb.remaining()];
        bb.get(b);
        return b;
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring, starting at the specified index
     * @param s The string to search for
     * @param from the index from which to start the search.
     * @return An Optional index ge 0 if the string is found, Optional.empty otherwise
     */
    public Optional<Integer> indexOf(CharSequence s, int from) {
        int i = sb.indexOf(s.toString(), from);
        return i > -1 ? Optional.of(i) : Optional.empty();
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring, starting at the specified index
     * @param s The string to search for
     * @return An Optional index gte 0 if the string is found, Optional.empty otherwise
     */
    public Optional<Integer> indexOf(CharSequence s) {
        return indexOf(s, 0);
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring, starting at the specified index
     * @param c The character to search for
     * @return An Optional index gte 0 if the string is found, Optional.empty otherwise
     */
    public Optional<Integer> indexOf(char c) {
        return indexOf(String.valueOf(c), 0);
    }

    /**
     * Returns the index within this string of the last occurrence of the
     * specified substring
     * @param s The string to search for
     * @param from the index from which to start the search.
     * @return An Optional index gte 0 if the string is found, Optional.empty otherwise
     */
    public Optional<Integer> lastIndexOf(CharSequence s, int from) {
        int i = sb.lastIndexOf(s.toString(), from);
        return i > -1 ? Optional.of(i) : Optional.empty();
    }

    /**
     * Returns the index within this string of the last occurrence of the
     * specified substring
     * @param c The character to search for
     * @return An Optional index gte 0 if the string is found, Optional.empty otherwise
     */
    public Optional<Integer> lastIndexOf(char c) {
        return lastIndexOf(String.valueOf(c), length() - 1);
    }

    /**
     * Find and replace the search string with a replacement string
     * @param search The string to search for
     * @param replacement The string to replace the search string with
     * @param max The maximum amount of replacements to make in the string
     * @return The Str object.
     */
    public Str findAndReplace(CharSequence search, CharSequence replacement, int max) {
        if (isNotEmpty(search)) {
            String r = replacement.toString();
            int searchLength = search.length();
            int replacementLength = replacement.length();

            int count = 0;
            if (max < 0) {
                max = Math.abs(max);

                for (int i = length(); count < max && i >= searchLength; i--) {
                    int from = i - searchLength;
                    if (charAt(from) == search.charAt(0) && subSequence(from, i).equals(search)) {
                        sb.replace(from, i, r);
                        count++;
                    }
                }
            } else {
                String s = search.toString();
                int i = sb.indexOf(s);
                while (count < max && i > -1) {
                    sb.replace(i, i + searchLength, r);
                    i = sb.indexOf(s, i + replacementLength);
                    count++;
                }
            }
        }
        return this;
    }

    /**
     * Find and replace the search string with a replacement string
     * @param search The string to search for
     * @param replacement The string to replace the search string with
     * @return The Str object.
     */
    public Str findAndReplace(CharSequence search, CharSequence replacement) {
        return findAndReplace(search, replacement, length());
    }

    /**
     * Replace all regex matches with a replacement string
     * @param regex The regex to match with
     * @param replacement The string to replace the search string with
     * @param max The maximum amount of replacements to make in the string
     * @return This Str object
     */
    public Str replace(CharSequence regex, CharSequence replacement, int max) {
        Matcher matcher = Pattern.compile(regex.toString()).matcher(toString());
        boolean found = matcher.find();
        int count = 0;
        if (found && count < max) {
            String r = replacement.toString();
            int replacementLength = replacement.length();
            int offset = 0;
            while(found) {
                int from = matcher.start();
                int to = matcher.end();
                sb.replace(from + offset, to + offset, r);
                offset += replacementLength - (to - from);
                count++;
                found = count < max && matcher.find();
            }
        }
        return this;
    }

    /**
     * Replace all regex matches with a replacement string
     * @param regex The regex to match with
     * @param replacement The string to replace the search string with
     * @return This Str object
     */
    public Str replace(CharSequence regex, CharSequence replacement) {
        return replace(regex, replacement, length());
    }

    /**
     * Replace the substring with a replacement
     * @param from the beginning index
     * @param to the end index
     * @param replacement The string to replace the search string with
     * @return This Str object
     */
    public Str replaceFrom(int from, int to, boolean inclusive, CharSequence... replacement) {
        int end = inclusive ? to + 1 : to;
        if (from > -1 && end > -1) {
            int len = length();
            sb.replace(Math.min(from, len), Math.min(end, len), String.join("", replacement));
        }
        return this;
    }

    /**
     * Replace the substring with a replacement
     * @param from The first occurrence of start string
     * @param to the first occurrence of end string, inclusive
     * @param replacement The string to replace the substring with
     * @return This Str object
     */
    public Str replaceFrom(CharSequence from, CharSequence to, boolean inclusive, CharSequence... replacement) {
        int fromIndex = indexOf(from).orElse(-1);
        return replaceFrom(fromIndex
                , indexOf(to, fromIndex).map(i -> inclusive ? i + to.length() : i).orElse(-1)
                , false
                , replacement);
    }

    /**
     * Delete all characters from the Str object
     * @return This Str object
     */
    public Str delete() {
        sb.setLength(0);
        return this;
    }

    /**
     * Determine if the string is null or empty
     * @return True if the string is null or empty, false otherwise
     */
    public boolean isEmpty() {
        return isEmpty(this);
    }

    /**
     * Determine if the string is not empty. Negation of <code>Str.isEmpty(s)</code>
     * @return True if the string is not empty, false otherwise
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * Add all the specified strings to this Str object
     * @param charSequences The strings to add
     * @return The Str object
     */
    public Str add(CharSequence... charSequences) {
        for(CharSequence cs : charSequences)
            sb.append(cs);
        return this;
    }

    public Str insert(int offset, CharSequence... charSequences) {
        for(CharSequence cs : charSequences) {
            sb.insert(offset, cs);
            offset += cs.length();
        }
        return this;
    }

    /**
     * Add all the specified characters to this Str object
     * @param chars The characters to add
     * @return The Str object
     */
    public Str add(char... chars) {
        for(char c : chars)
            sb.append(c);
        return this;
    }

    /**
     * Add all the specified numbers to this Str object
     * @param numbers The characters to add
     * @return The Str object
     */
    public Str add(Number... numbers) {
        for(Number n : numbers)
            sb.append(n);
        return this;
    }

    /**
     * Repeat the specified string len number of times
     * @param s The string to repeat
     * @param len How many times to repeat the string. If len is negative, the string will be prepended
     * @return The Str object
     */
    public Str repeat(CharSequence s, int len) {
        if (len > 0)
            sb.append(String.join(EMPTY, Collections.nCopies(len, s)));
        else if (len < 0)
            sb.insert(0, String.join(EMPTY, Collections.nCopies(Math.abs(len), s)));

        return this;
    }

    /**
     * Repeat the specified string len number of times
     * @param c The character to repeat
     * @param len How many times to repeat the string. If len is negative, the string will be prepended
     * @return The Str object
     */
    public Str repeat(char c, int len) {
        return repeat(String.valueOf(c), len);
    }

    /**
     * Add any number of spaces to the string
     * @param spaces The number of spaces to add
     * @return The Str object
     */
    public Str space(int spaces) {
        return repeat(Chars.SPACE, spaces);
    }

    /**
     * Add a space to the string
     * @return The Str object
     */
    public Str space() {
        return space(1);
    }

    /**
     * Add any number of lines to the string
     * @param lines The number of line separators to add
     * @return The Str object
     */
    public Str br(int lines) {
        return repeat(System.lineSeparator(), lines);
    }

    /**
     * Add a line separator to the string
     * @return The Str object
     */
    public Str br() {
        return br(1);
    }

    /**
     * Deletes i number of characters from the beginning of the string
     * @param i number of characters to skip
     * @return The Str object
     */
    public Str skip(int i) {
        sb.delete(0, i);
        return this;
    }

    /**
     * Delete all characters from the string which are not taken
     * @param i The number of characters to take
     * @return The Str object
     */
    public Str take(int i) {
        sb.delete(i, length());
        return this;
    }

    /**
     * Flexible substring method which supports multiple inclusion policies and negative indexes.
     * INCLUDE from and to are included in the result
     * INCLUDE_EXCLUDE from is included to is excluded
     * EXCLUDE_INCLUDE from is excluded to is included
     * EXCLUDE from and to are excluded
     * NOTE: from greater than to is always undefined and return will result is an empty string or an exception.
     * @param from The start index inclusive or exclusive. If negative the index will count backwards from the tail.
     * @param to The final index inclusive or exclusive. If negative the index will count backwards from the tail.
     * @param policy Determines which indexes should be included or excluded
     * @param failOnOutOfBounds Throws StringIndexOutOfBounds Exception if true and indexes are violated.
     *                          If false out-of-bounds indexes are moved to the ends of the valid range.
     * @return A self referencing Str to support a fluent api.
     */
    public Str slice(int from, int to, Slice policy, boolean failOnOutOfBounds) {
        int len = length();

        from = from >= 0 ? from : len + from;
        to = to >= 0 ? to : len + to;

        if (failOnOutOfBounds && from > to) {
            throw new StringIndexOutOfBoundsException(String.format("begin %s, end %s, length %s", from, to, len));
        }

        switch (policy) {
            case INCLUDE:
                to++;
                break;
            case INCLUDE_EXCLUDE:
                break;
            case EXCLUDE_INCLUDE:
                if (from != to) from++;
                to++;
                break;
            case EXCLUDE:
                from++;
                break;
        }
        if (failOnOutOfBounds) {
            if ((from < 0 || from > len) || (to < 0 || to > len )) {
                throw new StringIndexOutOfBoundsException(String.format("begin %s, end %s, length %s", from, to, len));
            }
        }
        if (from > to || to <= 0 || from >= len) {
            return delete();
        } else {
            return skip(Math.max(Math.min(from, len), 0))
                    .take(Math.min(to - from, length()));
        }
    }

    /**
     * Slices the string into a new substring
     * @param from The beginning index
     * @param to The ending index
     * @param policy Inclusion policy: INCLUDE, INCLUDE_EXCLUDE, EXCLUDE_INCLUDE, EXCLUDE
     * @return The Str object
     */
    public Str slice(int from, int to, Slice policy) {
        return slice(from, to, policy, false);
    }

    /**
     * Trim s [-1 == left, 1 == right, 0 == left and right]
     * @param lr Indicates which direction to trim -1 for left, 0 for left and right, and 1 for right.
     * @param s The string to trim.
     * @return A self referencing Str to support a fluent api.
     */
    protected Str lrtrim(int lr, CharSequence... s) {
        Map<Integer, List<CharSequence>> strings = groupBySizes(s);

        List<Integer> sizes = strings.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .filter(it -> it > 0 && it <= length())
                .toList();

        int i = 0;
        boolean trim;

        // left trim or both
        if (lr == 0 || lr == -1) {
            int len = length();
            do {
                trim = false;
                for (int size : sizes) {
                    int to = i + size;
                    if (to < len && strings.getOrDefault(size, emptyList).contains(subSequence(i, to)) ) {
                        i = to;
                        trim = true;
                        break;
                    }
                }
            } while(trim);

            if (i > 0) sb.delete(0, i);
        }

        // right trim or both
        if (lr == 0 || lr == 1) {
            int len = length();
            i = len;
            do {
                trim = false;
                for (int size : sizes) {
                    int from = i - size;
                    if (from >= 0 && strings.getOrDefault(size, emptyList).contains(subSequence(from, i))) {
                        i = from;
                        trim = true;
                        break;
                    }
                }
            } while(trim);

            if (i < len) sb.delete(i, len);
        }
        return this;
    }

    /**
     * Trim characters at the beginning of the string
     * @param s The strings to trim
     * @return The Str object
     */
    public Str ltrim(CharSequence[] s) {
        if (s.length == 0) {
            ltrim();
        } else {
            lrtrim(-1, s);
        }
        return this;
    }

    /**
     * Trim characters at the beginning of the string
     * @param s The string to trim
     * @return The Str object
     */
    public Str ltrim(CharSequence s) {
        return lrtrim(-1, s);
    }

    /**
     * Trim all whitespace from the beginning of a string
     * @return The Str object
     */
    public Str ltrim() {
        int length = length();
        if (length > 0 && Character.isWhitespace(charAt(0))) {
            int i = 1;
            for (; i < length; i++) {
                if (!Character.isWhitespace(charAt(i))) {
                    break;
                }
            }
            sb.delete(0, i);
        }
        return this;
    }

    /**
     * Trim all whitespace from the end of a string
     * @return The Str object
     */
    public Str rtrim() {
        int length = length();
        int last = length - 1;
        if (length > 0 && Character.isWhitespace(charAt(last))) {
            int i = last - 1;
            for (; i >= 0; i--) {
                if (!Character.isWhitespace(charAt(i))) {
                    break;
                }
            }
            sb.delete(i + 1, length);
        }
        return this;
    }

    /**
     * Trim all whitespace from the beginning and end of a string
     * @return The Str object
     */
    public Str trim() {
        return ltrim().rtrim();
    }

    /**
     * Trim characters at the end of the string
     * @param c The character to trim
     * @return The Str object
     */
    public Str rtrim(char c) {
        int length = length();
        int last = length - 1;
        if (length > 0 && charAt(last) == c) {
            int i = last - 1;
            for (; i >= 0; i--) {
                if (charAt(i) != c) {
                    break;
                }
            }
            sb.delete(i + 1, length);
        }
        return this;
    }

    /**
     * Capitalize the string
     * @return The Str Object
     */
    public Str capitalize() {
        if (isNotEmpty()) {
            char c = charAt(0);
            int i = 1;
            int len = length();
            while(!Character.isAlphabetic(c) && i < len) {
                c = charAt(i);
                i++;
            }

            if (Character.isLowerCase(c)) {
                sb.setCharAt(i - 1, Character.toUpperCase(c));
            }
        }
        return this;
    }

    /**
     * Convert the string into a delimited string
     * @param delimiter The delimiter to use
     * @param lowerCase If true make the string all lower case
     * @return The Str object
     */
    protected Str toDelimitedName(char delimiter, boolean lowerCase) {
        boolean dotted = false;

        for (int i = 0; i < length(); i++) {
            char c = charAt(i);
            if (isDelimiter(c)) {
                if (dotted || i == 0 || i >= length() - 1) {
                    sb.deleteCharAt(i);
                    // update the loop b/c we are looping and modifying
                    // generally don't do this, however this is really efficient string manipulation
                    i--;
                } else {
                    sb.setCharAt(i, delimiter);
                }
                dotted = true;
            } else {
                if (Character.isUpperCase(c)) {
                    if (lowerCase) {
                        sb.setCharAt(i, Character.toLowerCase(c));
                    }
                    if (!dotted && i > 0 && i <= length() - 1) {
                        sb.insert(i, delimiter);
                        i++;
                    }
                } else if (!lowerCase && dotted) {
                    sb.setCharAt(i, Character.toUpperCase(c));
                }
                dotted = false;
            }
        }
        return rtrim(delimiter);
    }

    /**
     * Convert the given string into kebab casing
     * @return The Str object
     */
    public Str toKebab() {
        return toDelimitedName(Chars.DASH, true);
    }

    /**
     * Determine if the string is an integer
     * @return True if the string is a valid integer, false otherwise
     */
    public boolean isInt() {
        return isInteger(this);
    }

    /**
     * Convert string to an optional long
     * @return Optional long if the string is a valid long, Optional.empty otherwise
     */
    public Optional<Long> toLong() {
        return toLong(this);
    }

    /**
     * Convert string to a character array
     * @return An array of type character <tt>char[]</tt>
     */
    public char[] toArray() {
        return toArray(this);
    }
}
