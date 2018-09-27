package net.maxsmr.comparelist.utils.logger;


import net.maxsmr.comparelist.utils.TextUtils;

public abstract class BaseTagLogger extends BaseLogger {


    protected final String tag;

    protected BaseTagLogger( String tag) {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("log tag is empty");
        }
        this.tag = tag;
    }
}
