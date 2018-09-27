package net.maxsmr.comparelist.utils;


import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import static net.maxsmr.comparelist.utils.CompareUtils.MatchStringOption.AUTO;
import static net.maxsmr.comparelist.utils.CompareUtils.MatchStringOption.AUTO_IGNORE_CASE;
import static net.maxsmr.comparelist.utils.CompareUtils.MatchStringOption.CONTAINS;
import static net.maxsmr.comparelist.utils.CompareUtils.MatchStringOption.CONTAINS_IGNORE_CASE;
import static net.maxsmr.comparelist.utils.CompareUtils.MatchStringOption.END_WITH;
import static net.maxsmr.comparelist.utils.CompareUtils.MatchStringOption.END_WITH_IGNORE_CASE;
import static net.maxsmr.comparelist.utils.CompareUtils.MatchStringOption.EQUALS;
import static net.maxsmr.comparelist.utils.CompareUtils.MatchStringOption.EQUALS_IGNORE_CASE;
import static net.maxsmr.comparelist.utils.CompareUtils.MatchStringOption.STARTS_WITH;
import static net.maxsmr.comparelist.utils.CompareUtils.MatchStringOption.STARTS_WITH_IGNORE_CASE;


public final class CompareUtils {

    public CompareUtils() {
        throw new AssertionError("no instances.");
    }

    public static boolean objectsEqual(Object one, Object another) {
        return one != null ? one.equals(another) : another == null;
    }

    public static boolean charsEqual(Character one, Character another, boolean ignoreCase) {
        if (ignoreCase) {
            one = one != null ? Character.toUpperCase(one) : null;
            another = another != null ? Character.toUpperCase(another) : null;
        }
        return one != null ? one.equals(another) : another == null;
    }

    public static boolean stringsEqual(String one, String another, boolean ignoreCase) {
        return one != null ? (!ignoreCase ? one.equals(another) : one.equalsIgnoreCase(another)) : another == null;
    }

    public static int compareForNull(Object lhs, Object rhs, boolean ascending) {
        return ascending ?
                lhs != null ? (rhs != null ? 0 : 1) : (rhs == null ? 0 : -1) :
                lhs != null ? (rhs != null ? 0 : -1) : (rhs == null ? 0 : 1);
    }

    public static <C extends Comparable<C>> int compareObjects(C one, C another, boolean ascending) {
        return one != null ? (another != null ? ascending ? one.compareTo(another) : another.compareTo(one) : 1) : another == null ? 0 : -1;
    }

    public static int compareInts(Integer one, Integer another, boolean ascending) {
        return one != null ? (another != null ? !ascending ? (int) Math.signum(another - one) : (int) Math.signum(one - another) : 1) : another == null ? 0 : -1;
    }

    public static int compareLongs(Long one, Long another, boolean ascending) {
        return one != null ? (another != null ? !ascending ? (int) Math.signum(another - one) : (int) Math.signum(one - another) : 1) : another == null ? 0 : -1;
    }

    public static int compareFloats(Float one, Float another, boolean ascending) {
        return one != null ? (another != null ? !ascending ? (int) Math.signum(another - one) : (int) Math.signum(one - another) : 1) : another == null ? 0 : -1;
    }

    public static int compareDouble(Double one, Double another, boolean ascending) {
        return one != null ? (another != null ? !ascending ? (int) Math.signum(another - one) : (int) Math.signum(one - another) : 1) : another == null ? 0 : -1;
    }

    public static int compareChars(Character one, Character another, boolean ascending, boolean ignoreCase) {
        if (charsEqual(one, another, ignoreCase)) {
            return 0;
        }
        return one != null ? (another != null ? !ascending ? (int) Math.signum(another - one) : (int) Math.signum(one - another) : 1) : another == null ? 0 : -1;
    }

    public static int compareStrings(String one, String another, boolean ascending, boolean ignoreCase) {
        if (stringsEqual(one, another, ignoreCase)) {
            return 0;
        }
        return one != null ? another != null ? !ascending ? one.compareTo(another) : another.compareTo(one) : 1 : -1;
    }

    public static int compareDates(Date one, Date another, boolean ascending) {
        return compareLongs(one != null ? one.getTime() : 0, another != null ? another.getTime() : 0, ascending);
    }


    public static boolean stringMatches(CharSequence oneS, CharSequence anotherS, int matchFlags, String... separators) {

        if (oneS == null) {
            oneS = "";
        }

        if (anotherS == null) {
            anotherS = "";
        }

        String one = oneS.toString();
        String another = anotherS.toString();

        boolean match = false;

        if (CompareUtils.MatchStringOption.contains(EQUALS, matchFlags)) {
            if (one.equals(another)) {
                match = true;
            }
        }
        if (!match && CompareUtils.MatchStringOption.contains(EQUALS_IGNORE_CASE, matchFlags)) {
            if (one.equalsIgnoreCase(another)) {
                match = true;
            }
        }
        if (!match && CompareUtils.MatchStringOption.contains(CONTAINS, matchFlags)) {
            if (one.contains(another)) {
                match = true;
            }
        }
        if (!match && CompareUtils.MatchStringOption.contains(CONTAINS_IGNORE_CASE, matchFlags)) {
            if (one.toLowerCase().contains(another.toLowerCase())) {
                match = true;
            }
        }
        if (!match && CompareUtils.MatchStringOption.contains(STARTS_WITH, matchFlags)) {
            if (one.startsWith(another)) {
                match = true;
            }
        }
        if (!match && CompareUtils.MatchStringOption.contains(STARTS_WITH_IGNORE_CASE, matchFlags)) {
            if (one.toLowerCase().startsWith(another.toLowerCase())) {
                match = true;
            }
        }
        if (!match && CompareUtils.MatchStringOption.contains(END_WITH, matchFlags)) {
            if (one.startsWith(another)) {
                match = true;
            }
        }
        if (!match && CompareUtils.MatchStringOption.contains(END_WITH_IGNORE_CASE, matchFlags)) {
            if (one.toLowerCase().startsWith(another.toLowerCase())) {
                match = true;
            }
        }
        if (!match && (CompareUtils.MatchStringOption.contains(AUTO, matchFlags) || CompareUtils.MatchStringOption.contains(AUTO_IGNORE_CASE, matchFlags))) {
            if (!TextUtils.isEmpty(one)) {
                one = CompareUtils.MatchStringOption.contains(AUTO_IGNORE_CASE, matchFlags) ? one.toLowerCase().trim() : one;
                another = CompareUtils.MatchStringOption.contains(AUTO_IGNORE_CASE, matchFlags) ? another.toLowerCase().trim() : another;
                if (stringsEqual(one, another, false)) {
                    match = true;
                } else {
                    final String[] parts = one.split("[" + (separators != null && separators.length > 0 ? TextUtils.join("", separators) : " ") + "]+");
                    if (parts.length > 0) {
                        for (String word : parts) {
                            if (!CompareUtils.MatchStringOption.containsAny(matchFlags, CompareUtils.MatchStringOption.valuesExceptOf(AUTO, AUTO_IGNORE_CASE).toArray(new CompareUtils.MatchStringOption[]{}))) {
                                if (word.startsWith(another)) {
                                    match = true;
                                    break;
                                }
                            } else {
                                if (stringMatches(word, another, CompareUtils.MatchStringOption.resetFlags(matchFlags, AUTO, AUTO_IGNORE_CASE))) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return match;
    }

    public enum MatchStringOption {

        AUTO(1), AUTO_IGNORE_CASE(1 << 1), EQUALS(1 << 2), EQUALS_IGNORE_CASE(1 << 3), CONTAINS(1 << 4), CONTAINS_IGNORE_CASE(1 << 5), STARTS_WITH(1 << 6), STARTS_WITH_IGNORE_CASE(1 << 7), END_WITH(1 << 8), END_WITH_IGNORE_CASE(1 << 9);

        public final int flag;

        MatchStringOption(int flag) {
            this.flag = flag;
        }

        public static boolean contains(MatchStringOption option, int flags) {
            return (flags & option.flag) == option.flag;
        }

        public static boolean containsAny(int flags, MatchStringOption... options) {
            boolean contains = false;
            if (options != null) {
                for (MatchStringOption o : options) {
                    if (o != null && contains(o, flags)) {
                        contains = true;
                        break;
                    }
                }
            }
            return contains;
        }

        public static int setFlag(int flags, MatchStringOption option) {
            return flags | option.flag;
        }

        public static int setFlags(int flags, MatchStringOption... options) {
            if (options != null) {
                for (MatchStringOption o : options) {
                    if (o != null) {
                        flags = setFlag(flags, o);
                    }
                }
            }
            return flags;
        }

        public static int resetFlag(int flags, MatchStringOption option) {
            return flags & ~option.flag;
        }

        public static int resetFlags(int flags, MatchStringOption... options) {
            if (options != null) {
                for (MatchStringOption o : options) {
                    if (o != null) {
                        flags = resetFlag(flags, o);
                    }
                }
            }
            return flags;
        }


        public static Set<MatchStringOption> valuesExceptOf(MatchStringOption... exclude) {
            Set<MatchStringOption> result = new LinkedHashSet<>();
            result.addAll(Arrays.asList(values()));
            if (exclude != null) {
                result.removeAll(Arrays.asList(exclude));
            }
            return result;
        }
    }

    public enum Condition {

        LESS, LESS_OR_EQUAL, EQUAL, MORE, MORE_OR_EQUAL;

        public boolean apply(Character one, Character another, boolean ignoreCase) {
            return fromResult(compareChars(one, another, true, ignoreCase), this);
        }

        public boolean apply(Number one, Number another) {
            return fromResult(compareDouble(one != null ? one.doubleValue() : null, another != null ? another.doubleValue() : null, true), this);
        }

        public static boolean fromResult(int result, Condition c) {
            switch (c) {
                case LESS:
                    return result < 0;
                case LESS_OR_EQUAL:
                    return result <= 0;
                case EQUAL:
                    return result == 0;
                case MORE:
                    return result > 0;
                case MORE_OR_EQUAL:
                    return result >= 0;
                default:
                    throw new IllegalArgumentException("Unknown " + Condition.class.getSimpleName() + ": " + c);
            }
        }
    }

}
