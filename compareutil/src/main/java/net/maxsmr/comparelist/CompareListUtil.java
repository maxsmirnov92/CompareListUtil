package net.maxsmr.comparelist;

import net.maxsmr.comparelist.utils.ArgsParser;
import net.maxsmr.comparelist.utils.FileHelper;
import net.maxsmr.comparelist.utils.Predicate;
import net.maxsmr.comparelist.utils.TextUtils;
import net.maxsmr.comparelist.utils.logger.BaseLogger;
import net.maxsmr.comparelist.utils.logger.SimpleSystemLogger;
import net.maxsmr.comparelist.utils.logger.holder.BaseLoggerHolder;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompareListUtil {

    private static final BaseLogger logger;

    static {
        System.setErr(System.out); // to avoid asynchronous outputs
        BaseLoggerHolder.initInstance(() -> new BaseLoggerHolder(false) {
            @Override
            protected BaseLogger createLogger(Class<?> clazz) {
                if (clazz != FileHelper.class) {
                    return new SimpleSystemLogger();
                } else {
                    return new BaseLogger.Stub();
                }
            }
        });
        logger = BaseLoggerHolder.getInstance().getLogger(CompareListUtil.class);
    }

    private static final String[] argsNames =
            {"-sourceFilePath", "-destinationFilePath"};

    private static ArgsParser argsParser;

    private static File sourceListFile;
    private static File destinationListFile;

    private static String getSourceListFilePath() {
        return argsParser.getPairArg(argsParser.findArgWithIndex(0, true));
    }

    private static String getDestinationListFilePath() {
        return argsParser.getPairArg(argsParser.findArgWithIndex(1, true));
    }

    public static void main(String args[]) {

        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Args not specified!");
        }

        argsParser = new ArgsParser(argsNames);
        argsParser.setArgs(args);

        final String sourceListFilePath = getSourceListFilePath();
        FileHelper.checkFile(sourceListFilePath, false);
        sourceListFile = new File(sourceListFilePath);

        final String destinationListFilePath = getDestinationListFilePath();
        FileHelper.checkFile(destinationListFilePath, false);
        destinationListFile = new File(destinationListFilePath);

        if (sourceListFile.equals(destinationListFile)) {
            throw new IllegalArgumentException("Source file path \"" + sourceListFile + "\" is same as destination filed path \"" + destinationListFile + "\"");
        }

        Set<Integer> unhandledIndexes = argsParser.getUnhandledArgsIndexes();
        for (Integer index : unhandledIndexes) {
            logger.e("Unknown argument \"" + args[index] + "\" (position: " + index + ")");
        }

        final List<String> sourceLines = FileHelper.readStringsFromFile(sourceListFile);
        final List<String> destinationLines = FileHelper.readStringsFromFile(destinationListFile);

        final Map<Integer, String> result = Predicate.Methods.filterWithIndex(sourceLines, source
                -> !TextUtils.isEmpty(source) && !Predicate.Methods.contains(destinationLines, destination -> destination.equals(source)));

        if (result.isEmpty()) {
            logger.i("No difference found");
        } else {
            logger.i("Difference found: " + result);
        }
    }
}
