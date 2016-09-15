package simonlang.coastdove.usagestatistics.usage;

import android.content.ContentValues;

import simonlang.coastdove.usagestatistics.usage.sql.AppUsageContract;
import simonlang.coastdove.lib.InteractionEventData;

/**
 * Helper class for InteractionEventData to provide additional import
 * and export methods
 */
public class InteractionEventDataHelper {
    /**
     * Converts an InteractionEventData object into ContentValues for its SQLite representation
     * @param data           InteractionEventData to convert
     * @param dataEntryID    Primary key of the associated ActivityDataEntry
     * @return ContentValues representing the original object
     */
    public static ContentValues toContentValues(InteractionEventData data, long dataEntryID) {
        ContentValues values = new ContentValues();
        values.put(AppUsageContract.InteractionDetailsTable.COLUMN_NAME_ANDROID_ID, data.getAndroidID());
        values.put(AppUsageContract.InteractionDetailsTable.COLUMN_NAME_TEXT, data.getText());
        values.put(AppUsageContract.InteractionDetailsTable.COLUMN_NAME_DESCRIPTION, data.getDescription());
        values.put(AppUsageContract.InteractionDetailsTable.COLUMN_NAME_CLASS_NAME, data.getClassName());
        values.put(AppUsageContract.InteractionDetailsTable.COLUMN_NAME_DATA_ENTRY_ID, dataEntryID);
        return values;
    }
}
