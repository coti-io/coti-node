package io.coti.basenode.utilities;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MemoryUtils {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;
    private static final DecimalFormat ROUNDED_DOUBLE_DECIMALFORMAT;
    private static final String MIB = "MiB";
    private static final String STRING_FORMAT = "%s %s";
    private static final MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();

    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        ROUNDED_DOUBLE_DECIMALFORMAT = new DecimalFormat("####0.00", otherSymbols);
        ROUNDED_DOUBLE_DECIMALFORMAT.setGroupingUsed(false);
    }

    private MemoryUtils() {
    }

    public static long getMaxHeap() {
        return memBean.getHeapMemoryUsage().getMax(); // max memory allowed for jvm -Xmx flag (-1 if isn't specified)
    }

    public static long getCommittedHeap() {
        return memBean.getHeapMemoryUsage().getCommitted();  // given memory to JVM by OS ( may fail to reach getMax, if there isn't more memory)
    }

    public static long getUsedHeap() {
        return memBean.getHeapMemoryUsage().getUsed(); // used now by your heap
    }

    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }

    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    private static double bytesToMiB(long memory) {
        return (double) memory / MEGABYTE_FACTOR;
    }

    public static String getTotalMemoryInMiB() {
        double totalMiB = bytesToMiB(getTotalMemory());
        return String.format(STRING_FORMAT, ROUNDED_DOUBLE_DECIMALFORMAT.format(totalMiB), MIB);
    }

    public static String getFreeMemoryInMiB() {
        double freeMiB = bytesToMiB(getFreeMemory());
        return String.format(STRING_FORMAT, ROUNDED_DOUBLE_DECIMALFORMAT.format(freeMiB), MIB);
    }

    public static String getUsedMemoryInMiB() {
        double usedMiB = bytesToMiB(getUsedMemory());
        return String.format(STRING_FORMAT, ROUNDED_DOUBLE_DECIMALFORMAT.format(usedMiB), MIB);
    }

    public static String getMaxMemoryInMiB() {
        double maxMiB = bytesToMiB(getMaxMemory());
        return String.format(STRING_FORMAT, ROUNDED_DOUBLE_DECIMALFORMAT.format(maxMiB), MIB);
    }

    public static double getPercentageUsedHeap() {
        return ((double) getUsedHeap() / getCommittedHeap()) * 100;
    }

    public static String getPercentageUsedHeapFormatted() {
        double percentageUsedHeap = getPercentageUsedHeap();
        return ROUNDED_DOUBLE_DECIMALFORMAT.format(percentageUsedHeap) + "%";
    }

    public static double getPercentageUsed() {
        return ((double) getUsedMemory() / getMaxMemory()) * 100;
    }

    public static String getPercentageUsedFormatted() {
        double usedPercentage = getPercentageUsed();
        return ROUNDED_DOUBLE_DECIMALFORMAT.format(usedPercentage) + "%";
    }

    public static String debugInfo() {
        String heapInfo = "heap information: max: " + getMaxHeap
                () + " , committed: " + getCommittedHeap() + ", used: " + getUsedHeap() + ", %: " + getPercentageUsedHeap();
        String memInfo = "memory information: max " + getMaxMemory() + ", total: " + getTotalMemory() + ", free: " + getFreeMemory() + ", %: " + getPercentageUsed();
        return heapInfo.concat("\n").concat(memInfo);
    }
}
