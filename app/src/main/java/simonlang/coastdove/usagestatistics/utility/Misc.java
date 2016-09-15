/*  Coast Dove
    Copyright (C) 2016  Simon Lang
    Contact: simon.lang7 at gmail dot com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package simonlang.coastdove.usagestatistics.utility;

import android.content.SharedPreferences;
import android.util.Log;

import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A collection of general utility functions
 */
public class Misc {
    // Date / Time
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATE_TIME_FILENAME = "yyyy-MM-dd_HH-mm-ss_SSS";

    // Detectable App Details Activity
    public static final boolean DEFAULT_DETECT_LAYOUTS = true;
    public static final boolean DEFAULT_DETECT_INTERACTIONS = true;
    public static final boolean DEFAULT_DETECT_SCREEN_STATE = true;
    public static final boolean DEFAULT_DETECT_NOTIFICATIONS = true;
    public static final boolean DEFAULT_REPLACE_PRIVATE_DATA = false;

    // Scroll positions
    public static final String ADD_APP_SCROLL_POSITION_PREF = "scroll_position_add_app";


    /**
     * Converts milliseconds to a String of the format "[Hh ][Mm ]Ss", e.g., "1h 20m 3s", "45m 22s" or "54s"
     * @param ms    Duration in milliseconds
     */
    public static String msToDurationString(long ms) {
        long hours = TimeUnit.MILLISECONDS.toHours(ms);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(minutes);
        String hoursString = hours == 0 ? "" : hours + "h ";
        String minutesString = minutes == 0 ? "" : minutes + "m ";
        String secondsString = seconds == 0 ? "< 1s" : seconds + "s";
        return hoursString + minutesString + secondsString;
    }

    /**
     * Sets and commits the given preference (appPackageName+preference) with the given value
     * @param preferences       Shared preferences to commit to
     * @param appPackageName    Package name for which to set the preference
     * @param preference        Preference name to set
     * @param value             Desired value of the preference
     */
    public static void setPreference(SharedPreferences preferences, String appPackageName, String preference,
                                     boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(appPackageName + preference, value);
        editor.commit();
    }

    /**
     * Retrieves the given preference (appPackageName+preference)
     * @param preferences       Shared preferences to retrieve from
     * @param appPackageName    Package name for which to retrieve the preference
     * @param preference        Preference name to get
     * @param defaultValue      Default value if the preference is not set
     * @return The preference's value
     */
    public static boolean getPreferenceBoolean(SharedPreferences preferences, String appPackageName, String preference,
                                               boolean defaultValue) {
        return preferences.getBoolean(appPackageName + preference, defaultValue);
    }

    /**
     * Parses an XML file
     * @param inputStream    Input stream to parse from
     * @return The XML as a Document
     */
    public static Document parseXMLFile(InputStream inputStream) {
        Document result;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            result = db.parse(inputStream);
        } catch (Exception e) {
            Log.e("XMLHelper", "Cannot parse XML: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        return result;
    }
}
