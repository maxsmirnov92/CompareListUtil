package net.maxsmr.comparelist.utils.logger.holder;


import net.maxsmr.comparelist.utils.TextUtils;
import net.maxsmr.comparelist.utils.logger.BaseTagLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseTagLoggerHolder extends BaseLoggerHolder {

    /** %1s - app prefix, %2s - log tag */
    private static final String TAG_FORMAT = "%1s/%2s";

    private static final Map<Class, String> TAGS = Collections.synchronizedMap(new HashMap<Class, String>());

    private final String logTag;

    public BaseTagLoggerHolder(String logTag, boolean isNullInstancesAllowed) {
        super(isNullInstancesAllowed);
        if (TextUtils.isEmpty(logTag)) {
            throw new IllegalArgumentException("log tag is empty");
        }
        this.logTag = logTag;
    }

    protected abstract BaseTagLogger createLogger( Class<?> clazz);

    @Override
    public BaseTagLogger getLogger(Class<?> clazz) {
        return (BaseTagLogger) super.getLogger(clazz);
    }

    protected String getTag(Class clazz) {
        return TAGS.computeIfAbsent(clazz, c -> String.format(TAG_FORMAT, logTag, c.getSimpleName()));
    }
}
