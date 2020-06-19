package com.applitools.utils;

import com.applitools.eyes.EyesException;
import com.applitools.eyes.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

/**
 * General purpose utilities.
 */
public class GeneralUtils {

    public static final String APPLITOOLS_SERVER_URL = "APPLITOOLS_SERVER_URL";
    public static final String APPLITOOLS_API_KEY = "APPLITOOLS_API_KEY";
    public static final String APPLITOOLS_BATCH_ID = "APPLITOOLS_BATCH_ID";
    public static final String APPLITOOLS_BATCH_NAME = "APPLITOOLS_BATCH_NAME";
    public static final String APPLITOOLS_BATCH_SEQUENCE = "APPLITOOLS_BATCH_SEQUENCE";
    public static final String APPLITOOLS_BATCH_NOTIFY = "APPLITOOLS_BATCH_NOTIFY";
    public static final String APPLITOOLS_BRANCH = "APPLITOOLS_BRANCH";
    public static final String APPLITOOLS_PARENT_BRANCH = "APPLITOOLS_PARENT_BRANCH";
    public static final String APPLITOOLS_BASELINE_BRANCH = "APPLITOOLS_BASELINE_BRANCH";
    public static final String APPLITOOLS_DONT_CLOSE_BATCHES = "APPLITOOLS_DONT_CLOSE_BATCHES";

    @SuppressWarnings("SpellCheckingInspection")
    private static final String DATE_FORMAT_ISO8601_FOR_OUTPUT =
            "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final String DATE_FORMAT_ISO8601_FOR_INPUT =
            "yyyy-MM-dd'T'HH:mm:ssXXX";

    @SuppressWarnings("SpellCheckingInspection")
    private static final String DATE_FORMAT_RFC1123 =
            "E, dd MMM yyyy HH:mm:ss 'GMT'";

    private static Logger logger;

    private GeneralUtils() {}

    /**
     * @param inputStream The stream which content we would like to read.
     * @return The entire contents of the input stream as a string.
     * @throws java.io.IOException If there was a problem reading/writing
     * from/to the streams used during the operation.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String readToEnd(InputStream inputStream) throws IOException {
        ArgumentGuard.notNull(inputStream, "inputStream");

        //noinspection SpellCheckingInspection
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        return new String(baos.toByteArray());
    }

    /**
     * Formats date and time as represented by a calendar instance to an ISO
     * 8601 string.
     *
     * @param calendar The date and time which we would like to format.
     * @return An ISO8601 formatted string representing the input date and time.
     */
    public static String toISO8601DateTime(Calendar calendar) {
        ArgumentGuard.notNull(calendar, "calendar");

        SimpleDateFormat formatter =
                new SimpleDateFormat(DATE_FORMAT_ISO8601_FOR_INPUT, Locale.ENGLISH);

        // For the string to be formatted correctly you MUST also set
        // the time zone in the formatter! See:
        // http://www.coderanch.com/t/376467/java/java/Display-time-timezones
        formatter.setTimeZone(calendar.getTimeZone());

        return formatter.format(calendar.getTime());
    }

    /**
     * Formats date and time as represented by a calendar instance to an TFC
     * 1123 string.
     *
     * @param calendar The date and time which we would like to format.
     * @return An RFC 1123 formatted string representing the input date and
     * time.
     */
    public static String toRfc1123(Calendar calendar) {
        ArgumentGuard.notNull(calendar, "calendar");

        SimpleDateFormat formatter =
                new SimpleDateFormat(DATE_FORMAT_RFC1123, Locale.ENGLISH);

        // For the string to be formatted correctly you MUST also set
        // the time zone in the formatter! See:
        // http://www.coderanch.com/t/376467/java/java/Display-time-timezones
        formatter.setTimeZone(calendar.getTimeZone());
        return formatter.format(calendar.getTime());
    }

    /**
     * Creates {@link java.util.Calendar} instance from an ISO 8601 formatted
     * string.
     *
     * @param dateTime An ISO 8601 formatted string.
     * @return A {@link java.util.Calendar} instance representing the given
     *          date and time.
     * @throws java.text.ParseException If {@code dateTime} is not in the ISO
     * 8601 format.
     */
    public static Calendar fromISO8601DateTime(String dateTime)
            throws ParseException {
        ArgumentGuard.notNull(dateTime, "dateTime");
        String timezoneId = "UTC";
        // Remove second fractions
        if (dateTime.contains("T")) {
            if (dateTime.endsWith("Z")) {
                dateTime = dateTime.replaceAll("\\.(\\d+)Z", "Z");
            } else if (dateTime.contains("+")) {
                dateTime = dateTime.replaceAll("\\.(\\d+)\\+", "+");
                timezoneId += "+" + dateTime.split("\\+")[1];
            } else if (dateTime.contains("-")) {
                dateTime = dateTime.replaceAll("\\.(\\d+)\\+", "+");
                timezoneId += "-" + dateTime.split("-")[1];
            }
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_ISO8601_FOR_INPUT);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezoneId));
        cal.setTime(formatter.parse(dateTime));
        return cal;
    }

    /**
     * Sleeps the input amount of milliseconds.
     *
     * @param milliseconds The number of milliseconds to sleep.
     */
    public static void sleep(long milliseconds)
    {
        try
        {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException("sleep interrupted", ex);
        }
    }

    /**
     * @param format The date format parser.
     * @param date The date string in a format matching {@code format}.
     * @return The {@link java.util.Date} represented by the input string.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static Date getDate(DateFormat format, String date) {
        try {
            return format.parse(date);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     *
     * @param start The start time. (Milliseconds)
     * @param end The end time. (Milliseconds).
     * @return The elapsed time between the start and end times, rounded up
     * to a full second, in milliseconds.
     */
    public static long getFullSecondsElapsedTimeMillis(long start, long end) {
        return ((long) Math.ceil((end - start) / 1000.0)) * 1000;
    }

    @SuppressWarnings("UnusedDeclaration")
    /**
     * Creates a {@link String} from a file specified by {@code resource}.
     *
     * @param resource The resource path.
     * @return The resource's text.
     * @throws EyesException If there was a problem reading the resource.
     */
    public static String readTextFromResource(String resource) {
        InputStream is = GeneralUtils.class.getClassLoader()
                .getResourceAsStream(resource);

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        try {
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            try {
                br.close();
            } catch (IOException e) {
                // Nothing to do.
            }
        } catch (IOException e) {
            try {
                br.close();
            } catch (IOException e2) {
                // Nothing to do.
            }
            throw new EyesException("Failed to read text from resource: ", e);
        }
        return sb.toString();
    }

    public static void logExceptionStackTrace(Exception ex) {
        logExceptionStackTrace(logger, ex);
    }

    public static void logExceptionStackTrace(Logger logger, Throwable ex) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(2048);
        PrintWriter writer = new PrintWriter(stream, true);
        ex.printStackTrace(writer);
        logger.log(ex.toString());
        try {
            logger.log(stream.toString("UTF-8"));
            writer.close();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initLogger(Logger logger) {
        GeneralUtils.logger = logger;
    }

    public static URI getServerUrl() {
        String serverUrl = getEnvString(APPLITOOLS_SERVER_URL);
        try {
            return new URI(serverUrl != null ? serverUrl : "https://eyesapi.applitools.com");
        } catch (URISyntaxException ex) {
            throw new EyesException(ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param domJson JSON as string to be gzipped
     * @return byte[] of the gzipped string
     */
    public static byte[] getGzipByteArrayOutputStream(String domJson) {
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();

        try {
            GZIPOutputStream gzip = new GZIPOutputStream(resultStream);
            gzip.write(domJson.getBytes());
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultStream.toByteArray();
    }

    public static String getEnvString(String variableName) {
        return System.getenv(variableName) == null ? System.getenv("bamboo_" + variableName) : System.getenv(variableName);
    }

    public static boolean getDontCloseBatches() {
        return "true".equalsIgnoreCase(GeneralUtils.getEnvString(APPLITOOLS_DONT_CLOSE_BATCHES));
    }
}
