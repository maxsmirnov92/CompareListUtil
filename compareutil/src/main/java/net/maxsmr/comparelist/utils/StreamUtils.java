package net.maxsmr.comparelist.utils;

import net.maxsmr.comparelist.utils.logger.BaseLogger;
import net.maxsmr.comparelist.utils.logger.holder.BaseLoggerHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public final class StreamUtils {

    private final static BaseLogger logger = BaseLoggerHolder.getInstance().getLogger(StreamUtils.class);

    private StreamUtils() {
        throw new AssertionError("no instances.");
    }

    public static boolean revectorStream(InputStream in, OutputStream out) {
        return revectorStream(in, out, true, true);
    }

    public static boolean revectorStream(InputStream in, OutputStream out, boolean closeInput, boolean closeOutput) {
        return revectorStream(in, out, null, closeInput, closeOutput);
    }

    public static boolean revectorStream(InputStream in, OutputStream out, IStreamNotifier notifier) {
        return revectorStream(in, out, notifier, true, true);
    }

    public static boolean revectorStream(InputStream in, OutputStream out, IStreamNotifier notifier, boolean closeInput, boolean closeOutput) {

        if (in == null || out == null)
            return false;

        boolean result = true;

        try {
            byte[] buff = new byte[256];

            int bytesWriteCount = 0;
            int totalBytesCount = 0;
            try {
                totalBytesCount = in.available();
            } catch (IOException e) {
                logger.e("an IOException occurred", e);
            }

            int len;
            long lastNotifyTime = 0;
            while ((len = in.read(buff)) > 0) {
                if (notifier != null) {
                    long interval = notifier.notifyInterval();
                    if (interval <= 0 || lastNotifyTime == 0 || (System.currentTimeMillis() - lastNotifyTime) >= interval) {
                        if (!notifier.onProcessing(in, out, bytesWriteCount,
                                totalBytesCount > 0 && bytesWriteCount <= totalBytesCount ? totalBytesCount - bytesWriteCount : 0)) {
                            result = false;
                            break;
                        }
                        lastNotifyTime = System.currentTimeMillis();
                    }

                }
                out.write(buff, 0, len);
                bytesWriteCount += len;
            }

        } catch (IOException e) {
            logger.e("an IOException occurred", e);
            result = false;

        } finally {
            try {
                if (closeInput) {
                    in.close();
                }
                if (closeOutput) {
                    out.close();
                }
            } catch (IOException e) {
                logger.e("an IOException occurred during close()", e);
            }
        }

        return result;
    }

    @Nullable
    public static byte[] readBytesFromInputStream(InputStream inputStream) {
        return readBytesFromInputStream(inputStream, true);
    }

    @Nullable
    public static byte[] readBytesFromInputStream(InputStream inputStream, boolean closeInput) {

        if (inputStream != null) {

            try {

                byte[] data = new byte[inputStream.available()];
                int readByteCount;
                do {
                    readByteCount = inputStream.read(data, 0, data.length);
                } while (readByteCount > 0);

                return data;

            } catch (IOException e) {
                logger.e("an Exception occurred", e);

            } finally {
                if (closeInput) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.e("an IOException occurred during close()", e);
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    public static String readStringFromInputStream(InputStream is, boolean closeInput) {
        return readStringFromInputStream(is, 0, closeInput);
    }

    @Nullable
    public static String readStringFromInputStream(InputStream is, int count, boolean closeInput) {
        Collection<String> strings = readStringsFromInputStream(is, count, closeInput);
        return !strings.isEmpty() ? TextUtils.join(System.getProperty("line.separator"), strings) : null;
    }

    @NotNull
    public static List<String> readStringsFromInputStream(InputStream is, boolean closeInput) {
        return readStringsFromInputStream(is, 0, closeInput);
    }

    @NotNull
    public static List<String> readStringsFromInputStream(InputStream is, int count, boolean closeInput) {
        if (is != null) {
            BufferedReader in = null;
            try {
                List<String> out = new ArrayList<>();
                in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((count <= 0 || out.size() < count) && (line = in.readLine()) != null) {
                    out.add(line);
                }
                return out;
            } catch (IOException e) {
                logger.e("an IOException occurred", e);
            } finally {
                if (closeInput) {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            logger.e("an IOException occurred during close()", e);
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    @Nullable
    public static String convertInputStreamToString(InputStream inputStream) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        revectorStream(inputStream, result);
        try {
            return result.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.e("an Exception occurred", e);
            return null;
        }
    }

    public interface IStreamNotifier {

        long notifyInterval();

        boolean onProcessing(InputStream inputStream, OutputStream outputStream, long bytesWrite, long bytesLeft);
    }

}
