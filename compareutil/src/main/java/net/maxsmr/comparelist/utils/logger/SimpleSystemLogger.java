package net.maxsmr.comparelist.utils.logger;

public class SimpleSystemLogger extends BaseLogger {

    @Override
    public void v(String message) {
        if (isLoggingEnabled()) {
            if (message != null) {
                System.out.println(message);
            }
        }
    }

    @Override
    public void v(Throwable exception) {
        if (isLoggingEnabled()) {
            if (exception != null) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void v(String message, Throwable exception) {
        v(message);
        e(exception);
    }

    @Override
    public void d(String message) {
        v(message);
    }

    @Override
    public void d(Throwable exception) {
        v(exception);
    }

    @Override
    public void d(String message, Throwable exception) {
        v(message, exception);
    }

    @Override
    public void i(String message) {
        v(message);
    }

    @Override
    public void i(Throwable exception) {
        v(exception);
    }

    @Override
    public void i(String message, Throwable exception) {
        v(message, exception);
    }

    @Override
    public void w(String message) {
        v(message);
    }

    @Override
    public void w(Throwable exception) {
        v(exception);
    }

    @Override
    public void w(String message, Throwable exception) {
        v(message, exception);
    }

    @Override
    public void e(String message) {
        if (isLoggingEnabled()) {
            if (message != null) {
                System.err.println(message);
            }
        }
    }

    @Override
    public void e(Throwable exception) {
        v(exception);
    }

    @Override
    public void e(String message, Throwable exception) {
        e(message);
        e(exception);
    }

    @Override
    public void wtf(String message) {
        e(message);
    }

    @Override
    public void wtf(Throwable exception) {
        e(exception);
    }

    @Override
    public void wtf(String message, Throwable exception) {
        e(message, exception);
    }
}
