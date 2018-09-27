package net.maxsmr.comparelist.utils.logger;


import net.maxsmr.comparelist.utils.Predicate;

import java.util.Collection;

public abstract class BaseLogger {

    private boolean isLoggingEnabled = true;

    public boolean isLoggingEnabled() {
        return isLoggingEnabled;
    }

    public void setLoggingEnabled(boolean toggle) {
        this.isLoggingEnabled = toggle;
    }

    public abstract void v(String message);

    public abstract void v(Throwable exception);

    public abstract void v(String message, Throwable exception);

    public abstract void d(String message);

    public abstract void d(Throwable exception);

    public abstract void d(String message, Throwable exception);

    public abstract void i(String message);

    public abstract void i(Throwable exception);

    public abstract void i(String message, Throwable exception);

    public abstract void w(String message);

    public abstract void w(Throwable exception);

    public abstract void w(String message, Throwable exception);

    public abstract void e(String message);

    public abstract void e(Throwable exception);

    public abstract void e(String message, Throwable exception);

    public abstract void wtf(String message);

    public abstract void wtf(Throwable exception);

    public abstract void wtf(String message, Throwable exception);

    public enum Level {

        VERBOSE, DEBUG, INFO, WARN, ERROR, WTF;

        public boolean isVerbose() {
            return isInRange(VERBOSE);
        }

        public boolean isDebug() {
            return isInRange(DEBUG);
        }

        public boolean isInfo() {
            return isInRange(INFO);
        }

        public boolean isWarn() {
            return isInRange(WARN);
        }

        public boolean isError() {
            return isInRange(ERROR);
        }

        public boolean isWtf() {
            return isInRange(WTF);
        }

        private boolean isInRange( Level level) {
            return ordinal() <= level.ordinal();
        }

        public static boolean contains(final Level level, Collection<Level> levels) {
            return Predicate.Methods.contains(levels, element -> element == level);
        }
    }

    public static class Stub extends BaseLogger {

        @Override
        public void v(String message) {

        }

        @Override
        public void v(Throwable exception) {

        }

        @Override
        public void v(String message, Throwable exception) {

        }

        @Override
        public void d(String message) {

        }

        @Override
        public void d(Throwable exception) {

        }

        @Override
        public void d(String message, Throwable exception) {

        }

        @Override
        public void i(String message) {

        }

        @Override
        public void i(Throwable exception) {

        }

        @Override
        public void i(String message, Throwable exception) {

        }

        @Override
        public void w(String message) {

        }

        @Override
        public void w(Throwable exception) {

        }

        @Override
        public void w(String message, Throwable exception) {

        }

        @Override
        public void e(String message) {

        }

        @Override
        public void e(Throwable exception) {

        }

        @Override
        public void e(String message, Throwable exception) {

        }

        @Override
        public void wtf(String message) {

        }

        @Override
        public void wtf(Throwable exception) {

        }

        @Override
        public void wtf(String message, Throwable exception) {

        }
    }
}
