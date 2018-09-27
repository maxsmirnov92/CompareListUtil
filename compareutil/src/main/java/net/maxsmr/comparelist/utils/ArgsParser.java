package net.maxsmr.comparelist.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ArgsParser {

    protected final Set<Integer> handledArgsIndexes = new LinkedHashSet<>();

    protected final Set<String> argsNames;

    protected final List<String> args = new ArrayList<>();

    public ArgsParser(String... argsNames) {
        this.argsNames = argsNames != null ? new LinkedHashSet<>(Arrays.asList(argsNames)) : Collections.emptySet();
    }

    public void setArgs(String... args) {
        setArgs(args != null ? Arrays.asList(args) : null);
    }

    public void setArgs(List<String> args) {
        this.args.clear();
        if (args != null) {
            this.args.addAll(args);
        }
        handledArgsIndexes.clear();
    }

    public boolean containsArg(int index, boolean ignoreCase) {
        return findArgWithIndex(index, ignoreCase) != null;
    }

    public String findArg(int index, boolean ignoreCase) {
        Pair<Integer, String> arg = findArgWithIndex(index, ignoreCase);
        return arg != null ? arg.second : null;
    }

    public String getPairArg(Pair<Integer, String> pair) {
        Pair<Integer, String> arg = getPairArgWithIndex(pair);
        return arg != null ? arg.second : null;
    }

    public Pair<Integer, String> findArgWithIndex(int index, boolean ignoreCase) {
        Pair<Integer, String> arg = findArgWithIndex(argsNames, args, index, ignoreCase);
        if (arg != null && arg.first != null) {
            handledArgsIndexes.add(arg.first);
        }
        return arg;
    }

    public Pair<Integer, String> getPairArgWithIndex(Pair<Integer, String> pair) {
        Pair<Integer, String> arg = getPairArgWithIndex(args, pair);
        if (arg != null) {
            handledArgsIndexes.add(arg.first);
        }
        return arg;
    }

    public Set<Integer> getUnhandledArgsIndexes() {
        Set<Integer> result = new LinkedHashSet<>();
        for (int i = 0; i < args.size(); i++) {
            int finalIndex = i;
            if (!Predicate.Methods.contains(handledArgsIndexes, element -> element != null && element == finalIndex)) {
                result.add(finalIndex);
            }
        }
        return result;
    }

    public static boolean containsArg(Collection<String> argsNames, String[] args, int index) {
        return findArgWithIndex(argsNames, args, index) != null;
    }

    public static Pair<Integer, String> findArgWithIndex(Collection<String> argsNames, String[] args, int index) {
        return findArgWithIndex(argsNames, args != null? Arrays.asList(args) : null, index);
    }

    public static Pair<Integer, String> findArgWithIndex(Collection<String> argsNames, String[] args, int index, boolean ignoreCase) {
        return findArgWithIndex(argsNames, args != null? Arrays.asList(args) : null, index, ignoreCase);
    }

    public static Pair<Integer, String> findArgWithIndex(Collection<String> argsNames, Collection<String> args, int index) {
        return findArgWithIndex(argsNames, args, index, false);
    }

    public static Pair<Integer, String> findArgWithIndex(Collection<String> argsNames, Collection<String> args, int index, boolean ignoreCase) {
        if (argsNames == null || argsNames.isEmpty()) {
            throw new IllegalArgumentException("Incorrect args names: " + argsNames);
        }
        if (index < 0 || index >= argsNames.size()) {
            throw new IllegalArgumentException("Incorrect arg name index: " + index);
        }
        Set<String> argsNamesSet = argsNames instanceof Set ? (Set<String>) argsNames : new LinkedHashSet<>(argsNames);
        List<String> argsNamesList = new ArrayList<>(argsNamesSet);
        return args != null ?
                Predicate.Methods.findWithIndex(args, element -> element != null && (ignoreCase ? element.equalsIgnoreCase(argsNamesList.get(index)) : element.equals(argsNamesList.get(index))))
                : null;
    }

    public static Pair<Integer, String> getPairArgWithIndex(String[] args, Pair<Integer, String> pair) {
        return getPairArgWithIndex(args != null? Arrays.asList(args) : null, pair);
    }

    public static Pair<Integer, String> getPairArgWithIndex(String[] args, int index) {
        return getPairArgWithIndex(args != null? Arrays.asList(args) : null, index);
    }

    public static Pair<Integer, String> getPairArgWithIndex(Collection<String> args, Pair<Integer, String> pair) {
        return args != null && pair != null && pair.first != null ? getPairArgWithIndex(args, pair.first) : null;
    }

    public static Pair<Integer, String> getPairArgWithIndex(Collection<String> args, int index) {
        List<String> argsList = args instanceof List? (List<String>) args : new ArrayList<>(args);
        return !argsList.isEmpty() && index < argsList.size() - 1 ? new Pair<>(index + 1, argsList.get(index + 1)) : null;
    }
}
