package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import com.applitools.utils.Iso8610CalendarSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
/**
 * A batch of tests.
 */
public class BatchInfo {

    private static final String BATCH_TIMEZONE = "UTC";
    private String id;
    private final String name;
    private final String startedAt;
    @JsonProperty("batchSequenceName")
    private String batchSequenceName;

    /**
     * Creates a new BatchInfo instance.
     *
     * @param name      Name of batch or {@code null} if anonymous.
     * @param startedAt Batch start time
     */
    public BatchInfo(String name, Calendar startedAt) {
        ArgumentGuard.notNull(startedAt, "startedAt");
        String envVarBatchId = GeneralUtils.getEnvString(GeneralUtils.APPLITOOLS_BATCH_ID);
        this.id = envVarBatchId != null ? envVarBatchId : UUID.randomUUID().toString();
        this.name = name != null ? name : GeneralUtils.getEnvString(GeneralUtils.APPLITOOLS_BATCH_NAME);
        this.startedAt = GeneralUtils.toISO8601DateTime(startedAt);
        this.batchSequenceName = GeneralUtils.getEnvString(GeneralUtils.APPLITOOLS_BATCH_SEQUENCE);
    }

    /**
     * See {@link #BatchInfo(String, Calendar)}.
     * {@code startedAt} defaults to the current time.
     *
     * @param name The name of the batch.
     */
    public BatchInfo(String name) {
        this(name, Calendar.getInstance(TimeZone.getTimeZone(BATCH_TIMEZONE)));
    }

    /**
     * @return The name of the batch or {@code null} if anonymous.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The id of the current batch.
     */
    public String getId () {
        return id;
    }

    /**
     * Sets a unique identifier for the batch. Sessions with batch info which
     * includes the same ID will be grouped together.
     * @param id The batch's ID
     */
    public void setId (String id) {
        ArgumentGuard.notNullOrEmpty(id, "id");
        this.id = id;
    }

    /**
     * Sets a unique identifier for the batch and allows chaining of the id
     * with the instance then returns that instance. Sessions with batch
     * info which includes the same ID will be grouped together.
     * @param id The batch's ID
     */
    public BatchInfo withBatchId(String id) {
        ArgumentGuard.notNullOrEmpty(id, "id");
        this.id = id;
        return this;
    }

    /**
     * @return The batch start date and time in ISO 8601 format.
     */
    @SuppressWarnings("UnusedDeclaration")
    @JsonSerialize(using = Iso8610CalendarSerializer.class)
    public Calendar getStartedAt() {
        try {
            return GeneralUtils.fromISO8601DateTime(startedAt);
        } catch (ParseException ex) {
            throw new EyesException("Failed to parse batch start time", ex);
        }
    }

    @JsonProperty("batchSequenceName")
    public String getSequenceName() {
        return batchSequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.batchSequenceName = sequenceName;
    }

    @Override
    public String toString() {
        return "'" + name + "' - " + startedAt;
    }
}
