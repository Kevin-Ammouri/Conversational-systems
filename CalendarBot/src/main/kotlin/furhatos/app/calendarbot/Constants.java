package furhatos.app.calendarbot;

import java.util.ArrayList;
import java.util.HashMap;

public class Constants {

    /** POSSIBLE INTENTS */

    public static final String ADD_INTENT = "ADD";

    public static final String REMOVE_INTENT = "REMOVE";

    public static final String LIST_INTENT = "LIST";

    public static final String GET_INTENT = "GET";

    /** KEY CONSTANTS */

    public static final String DAY = "DAY";

    public static final String DATE = "DATE";

    public static final String END_DATE = "END_DATE";

    public static final String DAY_CONTEXT = "DAY_CONTEXT";

    public static final String START_TIME = "START_TIME";

    public static final String END_TIME = "END_TIME";

    public static final String DURATION = "DURATION";

    public static final String TIME_CONTEXT = "TIME_CONTEXT";

    public static final String ALL_DAY = "ALL_DAY";

    public static final String BOOK_STATEMENT = "BOOK_STATEMENT";

    public static final String NAME = "NAME";

    public static final String DONE = "DONE";

    public static final String TODAY = "today";

    /** Booking Statements */

    public static final String MEETING = "meeting";

    /** INTERACTION OPTIONS WITH TOOLS */

    public static final String PRONOUNCE = "PRO";

    public static final String TIME_OR_CONTEXT = "TOC";

    public static final String REMOVE_PLURAL = "RP";

    public static final String YES = "YES";

    public static final String NO = "NO";

    /** ARRAY CONSTANTS */

    public static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"
    };

    public static final String[] TIMES = {
            "Second", "Seconds", "Minute", "Minutes", "Hour", "Hours"
    };

    public static final String[] TIMES_OF_THE_DAY = {
            "Morning", "Afternoon", "Evening", "Night"
    };

    /** MAPPINGS */

    public static final HashMap<String, ArrayList<String>> TimeOfDay = Tools.createTimeContextList();

    public static final HashMap<String, String> TO24HOUR = Tools.create24HourMapping(true);

    public static final HashMap<String, String> FROM24HOUR = Tools.create24HourMapping(false);


    /** PATHS */

    public static final String HASHMAP_PATH = "src/main/kotlin/furhatos/app/calendarbot/data/hashmap.ser";

}
