package net.maxsmr.comparelist.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Units {

    public static String timeToString(long t, TimeUnit sizeUnit) {
        return timeToString(t, sizeUnit, null);
    }

    /**
     * @param t                  time
     * @param timeUnit           unit for time
     * @param timeUnitsToExclude list of units to avoid in result string
     */
    public static String timeToString(long t, TimeUnit timeUnit, Collection<TimeUnit> timeUnitsToExclude) {
        if (t < 0) {
            throw new IllegalArgumentException("incorrect time: " + t);
        }
        if (timeUnitsToExclude == null) {
            timeUnitsToExclude = Collections.emptySet();
        }
        t = timeUnit.toNanos(t);
        final long targetTime;
        StringBuilder sb = new StringBuilder();
        if (t < TimeUnitConstants.C1 && !timeUnitsToExclude.contains(TimeUnit.NANOSECONDS)) {
            targetTime = t;
            sb.append(targetTime);
            sb.append(" ns");
        } else if ((timeUnitsToExclude.contains(TimeUnit.NANOSECONDS) || t >= TimeUnitConstants.C1 && t < TimeUnitConstants.C2) && !timeUnitsToExclude.contains(TimeUnit.MICROSECONDS)) {
            targetTime = TimeUnit.NANOSECONDS.toMicros(t);
            sb.append(targetTime);
            sb.append(" micros");
        } else if ((timeUnitsToExclude.contains(TimeUnit.MICROSECONDS) || t >= TimeUnitConstants.C2 && t < TimeUnitConstants.C3) && !timeUnitsToExclude.contains(TimeUnit.MILLISECONDS)) {
            targetTime = TimeUnit.NANOSECONDS.toMillis(t);
            sb.append(targetTime);
            sb.append(" ms");
        } else if ((timeUnitsToExclude.contains(TimeUnit.MILLISECONDS) || t >= TimeUnitConstants.C3 && t < TimeUnitConstants.C4) && !timeUnitsToExclude.contains(TimeUnit.SECONDS)) {
            targetTime = TimeUnit.NANOSECONDS.toSeconds(t);
            sb.append(targetTime);
            sb.append(" sec");
        } else if ((timeUnitsToExclude.contains(TimeUnit.SECONDS) || t >= TimeUnitConstants.C4 && t < TimeUnitConstants.C5) && !timeUnitsToExclude.contains(TimeUnit.MINUTES)) {
            targetTime = TimeUnit.NANOSECONDS.toMinutes(t);
            sb.append(targetTime);
            sb.append(" mins");
        } else if ((timeUnitsToExclude.contains(TimeUnit.MINUTES) || t >= TimeUnitConstants.C5 && t < TimeUnitConstants.C6) && !timeUnitsToExclude.contains(TimeUnit.HOURS)) {
            targetTime = TimeUnit.NANOSECONDS.toHours(t);
            sb.append(targetTime);
            sb.append(" hrs");
        } else if ((timeUnitsToExclude.contains(TimeUnit.HOURS) || t >= TimeUnitConstants.C6) && !timeUnitsToExclude.contains(TimeUnit.DAYS)) {
            targetTime = TimeUnit.NANOSECONDS.toDays(t);
            sb.append(targetTime);
            sb.append(" days");
        }
        return sb.toString();
    }

    public static String sizeToString(double s, SizeUnit sizeUnit) {
        return sizeToString(s, sizeUnit, null);
    }

    /**
     * @param s                  size
     * @param sizeUnit           unit for s
     * @param sizeUnitsToExclude list of units to avoid in result string
     */
    public static String sizeToString(double s, SizeUnit sizeUnit, Collection<SizeUnit> sizeUnitsToExclude) {
        if (s < 0) {
            throw new IllegalArgumentException("incorrect size: " + s);
        }
        if (!sizeUnit.isBytes()) {
            throw new IllegalArgumentException("sizeUnit must be bytes only");
        }
        if (sizeUnitsToExclude == null) {
            sizeUnitsToExclude = Collections.emptySet();
        }
        s = sizeUnit.toBytes(s);
        StringBuilder sb = new StringBuilder();
        if (s < SizeUnit.C1 && !sizeUnitsToExclude.contains(SizeUnit.BYTES)) {
            sb.append((long) s);
            sb.append(" Bytes");
        } else if ((sizeUnitsToExclude.contains(SizeUnit.BYTES) || s >= SizeUnit.C1 && s < SizeUnit.C2) && !sizeUnitsToExclude.contains(SizeUnit.KBYTES)) {
            double kbytes = SizeUnit.BYTES.toKBytes(s);
            sb.append(!sizeUnitsToExclude.contains(SizeUnit.BYTES) ? (long) kbytes : kbytes);
            sb.append(" KBytes");
            double restBytes = s - SizeUnit.KBYTES.toBytes(kbytes);
            if (restBytes > 0) {
                sb.append(", ");
                sb.append(sizeToString(restBytes, SizeUnit.BYTES, sizeUnitsToExclude));
            }
        } else if ((sizeUnitsToExclude.contains(SizeUnit.KBYTES) || s >= SizeUnit.C2 && s < SizeUnit.C3) && !sizeUnitsToExclude.contains(SizeUnit.MBYTES)) {
            double mbytes = SizeUnit.BYTES.toMBytes(s);
            sb.append(!sizeUnitsToExclude.contains(SizeUnit.KBYTES) ? (long) mbytes : mbytes);
            sb.append(" MBytes");
            double restBytes = s - SizeUnit.MBYTES.toBytes(mbytes);
            if (restBytes > 0) {
                sb.append(", ");
                sb.append(sizeToString(restBytes, SizeUnit.BYTES, sizeUnitsToExclude));
            }
        } else if ((sizeUnitsToExclude.contains(SizeUnit.MBYTES) || s >= SizeUnit.C3) && !sizeUnitsToExclude.contains(SizeUnit.GBYTES)) {
            double gbytes = SizeUnit.BYTES.toGBytes(s);
            sb.append(!sizeUnitsToExclude.contains(SizeUnit.MBYTES) ? (long) gbytes : gbytes);
            sb.append(" GBytes");
            double restBytes = s - SizeUnit.GBYTES.toBytes(gbytes);
            if (restBytes > 0) {
                sb.append(", ");
                sb.append(sizeToString(restBytes, SizeUnit.BYTES, sizeUnitsToExclude));
            }
        }
        return sb.toString();
    }

    public enum SizeUnit {

        BYTES {
            @Override
            public long toBytes(double s) {
                return (long) s;
            }

            @Override
            public double toKBytes(double s) {
                return s / C1;
            }

            @Override
            public double toMBytes(double s) {
                return s / C2;
            }

            @Override
            public double toGBytes(double s) {
                return s / C3;
            }

            @Override
            public long toBits(double s) {
                return (long) toBitsFromBytes(s);
            }

            @Override
            public double toKBits(double s) {
                return toBitsFromBytes(toKBytes(s));
            }

            @Override
            public double toMBits(double s) {
                return toBitsFromBytes(toMBytes(s));
            }

            @Override
            public double toGBits(double s) {
                return toBitsFromBytes(toGBytes(s));
            }
        },

        KBYTES {
            @Override
            public long toBytes(double s) {
                return (long) (s * C1);
            }

            @Override
            public double toKBytes(double s) {
                return s;
            }

            @Override
            public double toMBytes(double s) {
                return s / C1;
            }

            @Override
            public double toGBytes(double s) {
                return s / C2;
            }

            @Override
            public long toBits(double s) {
                return (long) toBitsFromBytes(s);
            }

            @Override
            public double toKBits(double s) {
                return toBitsFromBytes(toKBytes(s));
            }

            @Override
            public double toMBits(double s) {
                return toBitsFromBytes(toMBytes(s));
            }

            @Override
            public double toGBits(double s) {
                return toBitsFromBytes(toGBytes(s));
            }
        },

        MBYTES {
            @Override
            public long toBytes(double s) {
                return (long) (s * C2);
            }

            @Override
            public double toKBytes(double s) {
                return s * C1;
            }

            @Override
            public double toMBytes(double s) {
                return s;
            }

            @Override
            public double toGBytes(double s) {
                return s / C1;
            }

            @Override
            public long toBits(double s) {
                return (long) toBitsFromBytes(s);
            }

            @Override
            public double toKBits(double s) {
                return toBitsFromBytes(toKBytes(s));
            }

            @Override
            public double toMBits(double s) {
                return toBitsFromBytes(toMBytes(s));
            }

            @Override
            public double toGBits(double s) {
                return toBitsFromBytes(toGBytes(s));
            }
        },

        GBYTES {
            @Override
            public long toBytes(double s) {
                return (long) (s * C3);
            }

            @Override
            public double toKBytes(double s) {
                return s * C2;
            }

            @Override
            public double toMBytes(double s) {
                return s * C1;
            }

            @Override
            public double toGBytes(double s) {
                return s;
            }

            @Override
            public long toBits(double s) {
                return (long) toBitsFromBytes(s);
            }

            @Override
            public double toKBits(double s) {
                return toBitsFromBytes(toKBytes(s));
            }

            @Override
            public double toMBits(double s) {
                return toBitsFromBytes(toMBytes(s));
            }

            @Override
            public double toGBits(double s) {
                return toBitsFromBytes(toGBytes(s));
            }
        },

        BITS {
            @Override
            public long toBytes(double s) {
                return (long) toBytesFromBits(s);
            }

            @Override
            public double toKBytes(double s) {
                return toBytesFromBits(toKBits(s));
            }

            @Override
            public double toMBytes(double s) {
                return toBytesFromBits(toMBits(s));
            }

            @Override
            public double toGBytes(double s) {
                return toBytesFromBits(toGBits(s));
            }

            @Override
            public long toBits(double s) {
                return (long) s;
            }

            @Override
            public double toKBits(double s) {
                return s / C1;
            }

            @Override
            public double toMBits(double s) {
                return s / C2;
            }

            @Override
            public double toGBits(double s) {
                return s / C3;
            }
        },

        KBITS {
            @Override
            public long toBytes(double s) {
                return (long) toBytesFromBits(s);
            }

            @Override
            public double toKBytes(double s) {
                return toBytesFromBits(toKBits(s));
            }

            @Override
            public double toMBytes(double s) {
                return toBytesFromBits(toMBits(s));
            }

            @Override
            public double toGBytes(double s) {
                return toBytesFromBits(toGBits(s));
            }

            @Override
            public long toBits(double s) {
                return (long) (s * C1);
            }

            @Override
            public double toKBits(double s) {
                return s;
            }

            @Override
            public double toMBits(double s) {
                return s / C2;
            }

            @Override
            public double toGBits(double s) {
                return s / C3;
            }
        },

        MBITS {
            @Override
            public long toBytes(double s) {
                return (long) toBytesFromBits(s);
            }

            @Override
            public double toKBytes(double s) {
                return toBytesFromBits(toKBits(s));
            }

            @Override
            public double toMBytes(double s) {
                return toBytesFromBits(toMBits(s));
            }

            @Override
            public double toGBytes(double s) {
                return toBytesFromBits(toGBits(s));
            }

            @Override
            public long toBits(double s) {
                return (long) (s * C2);
            }

            @Override
            public double toKBits(double s) {
                return s * C1;
            }

            @Override
            public double toMBits(double s) {
                return s;
            }

            @Override
            public double toGBits(double s) {
                return s / C1;
            }
        },

        GBITS {
            @Override
            public long toBytes(double s) {
                return (long) toBytesFromBits(s);
            }

            @Override
            public double toKBytes(double s) {
                return toBytesFromBits(toKBits(s));
            }

            @Override
            public double toMBytes(double s) {
                return toBytesFromBits(toMBits(s));
            }

            @Override
            public double toGBytes(double s) {
                return toBytesFromBits(toGBits(s));
            }

            @Override
            public long toBits(double s) {
                return (long) (s * C3);
            }

            @Override
            public double toKBits(double s) {
                return s * C2;
            }

            @Override
            public double toMBits(double s) {
                return s * C1;
            }

            @Override
            public double toGBits(double s) {
                return s;
            }
        };

        public static final long C0 = 8;
        public static final long C1 = 1024L;
        public static final long C2 = C1 * 1024L;
        public static final long C3 = C2 * 1024L;

        public abstract long toBytes(double s);

        public abstract double toKBytes(double s);

        public abstract double toMBytes(double s);

        public abstract double toGBytes(double s);

        public abstract long toBits(double s);

        public abstract double toKBits(double s);

        public abstract double toMBits(double s);

        public abstract double toGBits(double s);

        public boolean isBits() {
            return this == BITS || this == KBITS || this == MBITS || this == GBITS;
        }

        public boolean isBytes() {
            return this == BYTES || this == KBYTES || this == MBYTES || this == GBYTES;
        }

        public static double toBitsFromBytes(double s) {
            return s * C0;
        }

        public static double toBytesFromBits(double s) {
            return s / C0;
        }

        public static double convert(long what, SizeUnit from, SizeUnit to) {
            final double result;
            switch (to) {
                case BITS:
                    result = from.toBits(what);
                    break;
                case BYTES:
                    result = from.toBytes(what);
                    break;
                case KBITS:
                    result = from.toKBits(what);
                    break;
                case KBYTES:
                    result = from.toKBytes(what);
                    break;
                case MBITS:
                    result = from.toMBits(what);
                    break;
                case MBYTES:
                    result = from.toMBytes(what);
                    break;
                case GBITS:
                    result = from.toGBits(what);
                    break;
                case GBYTES:
                    result = from.toGBytes(what);
                    break;
                default:
                    result = 0f;
                    break;
            }
            return result;
        }
    }

    private interface TimeUnitConstants {

        long C0 = 1L;
        long C1 = 1000L;
        long C2 = 1000000L;
        long C3 = 1000000000L;
        long C4 = 60000000000L;
        long C5 = 3600000000000L;
        long C6 = 86400000000000L;
        long MAX = 9223372036854775807L;
    }
}
