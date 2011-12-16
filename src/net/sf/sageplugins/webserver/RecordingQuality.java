/**
 * Created January 12, 2006
 */
package net.sf.sageplugins.webserver;

import java.text.DecimalFormat;

import net.sf.sageplugins.sageutils.SageApi;

public class RecordingQuality
{
    private String recordingQualityName;
    private static final DecimalFormat bitrateFormat = new DecimalFormat("0.0");

    public RecordingQuality(String recordingQualityName) {
        this.recordingQualityName = recordingQualityName;
    }

    public String getName() {
        return recordingQualityName;
    }

    public long getBitrate() throws Exception {
        return ((Long) SageApi.Api("GetRecordingQualityBitrate", recordingQualityName)).longValue();
    }

    public String getGigabytesPerHour() throws Exception {
        long rawbitrate = getBitrate();  // bits per second
        double bitrate = rawbitrate * 3600; // bits per hour
        bitrate /= 8; // Bytes per hour
        bitrate /= 1000000000; // Gigabytes per hour
        String formattedBitrate = bitrateFormat.format(bitrate);
        return formattedBitrate;
    }

    public String getFormat() throws Exception {
        return SageApi.StringApi("GetRecordingQualityFormat", new Object[] {recordingQualityName});
    }

    public String getDescription() throws Exception {
        return recordingQualityName + " - " + getFormat() + " @ " + getGigabytesPerHour() + " GB/hr";
    }

    public static RecordingQuality getDefaultRecordingQuality() throws Exception {
        return new RecordingQuality(getDefaultRecordingQualityName());
    }

    public static String getDefaultRecordingQualityName() throws Exception {
        return SageApi.StringApi("GetDefaultRecordingQuality", null);
    }

    public static void setDefaultRecordingQuality(RecordingQuality recordingQuality) throws Exception {
        setDefaultRecordingQuality(recordingQuality.getName());
    }

    public static void setDefaultRecordingQuality(String recordingQualityName) throws Exception {
        SageApi.Api("SetDefaultRecordingQuality", recordingQualityName);
    }

    public static RecordingQuality[] getRecordingQualities() throws Exception {
        String[] recordingQualityNames = getRecordingQualityNames();
        RecordingQuality[] recordingQualities = new RecordingQuality[recordingQualityNames.length];

        for (int i = 0; i < recordingQualityNames.length; i++) {
            recordingQualities[i] = new RecordingQuality(recordingQualityNames[i]);
        }

        return recordingQualities;
    }

    public static String[] getRecordingQualityNames() throws Exception {
        String[] recordingQualityNames = (String[]) SageApi.Api("GetRecordingQualities");
        recordingQualityNames = (String[]) SageApi.Api("Sort", new Object[] {recordingQualityNames, Boolean.TRUE, "GetRecordingQualityBitrate"});
        recordingQualityNames = (String[]) SageApi.Api("Sort", new Object[] {recordingQualityNames, Boolean.FALSE, "GetRecordingQualityFormat"});
        return recordingQualityNames;
    }
}
