package furhatos.app.calendarbot;

import java.util.HashMap;

public class Constants {
    public static final String ADD_INTENT = "ADD";

    public static final String REMOVE_INTENT = "REMOVE";

    public static final String LIST_INTENT = "LIST";

    public static final String GET_INTENT = "GET";

    public static final String DAY = "DAY";

    public static final String DATE = "DATE";

    public static final String DAY_CONTEXT = "DAY_CONTEXT";

    public static final String START_TIME = "START_TIME";

    public static final String END_TIME = "END_TIME";

    public static final String DURATION = "DURATION";

    public static final String TIME_OF_DAY = "TIME_OF_DAY";

    public static final String NAME = "NAME";

    public static final String DONE = "DONE";

    public static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    public static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"
    };

    public static final String[] TIMES = {
            "Second", "Seconds", "Minute", "Minutes", "Hour", "Hours"
    };

    public static final String[] TIMESOFTHEDAY = {
            "Morning", "Afternoon", "Evening"
    };

    public static final HashMap<String, String> TO24HOUR = Tools.createTO24HOUR();

    public static final String HASHMAP_PATH = "src/main/kotlin/furhatos/app/calendarbot/data/hashmap.ser";

}
