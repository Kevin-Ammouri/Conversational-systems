package furhatos.app.calendarbot;

import java.util.HashMap;

public class Constants {
    public static final String ADD_INTENT = "ADD";

    public static final String REMOVE_INTENT = "REMOVE";

    public static final String LIST_INTENT = "LIST";

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

    public static final HashMap<String, String> TO24HOUR = createTO24HOUR();

    private static HashMap<String, String> createTO24HOUR() {
        HashMap<String, String> to24hour = new HashMap<>();
        to24hour.put("12 am", "00:00:00");
        for (int i = 1; i < 10; i++) {
            String non_mil = i + " am";
            String mil = "0" + i + ":00:00";
            to24hour.put(non_mil, mil);
        }
        to24hour.put("10 am", "10:00:00");
        to24hour.put("11 am", "11:00:00");
        for (int i = 1; i < 12; i++) {
            String non_mil = i + " pm";
            String mil;
            if (i < 8)
                mil = "1" + (i+2) + ":00:00";
            else
                mil = "2" + (i%8) + ":00:00";
            to24hour.put(non_mil, mil);
        }

        to24hour.put("1 minute", "00:01:00");
        for (int i = 2; i <= 60; i++) {
            String sentence = i + " minutes";
            String mil;
            if (i < 10)
                mil = "00:0" + i + ":00";
            else
                mil = "00:" + i + ":00";
            to24hour.put(sentence, mil);
        }

        to24hour.put("1 hour", "01:00:00");
        for (int i = 2; i <= 23; i++) {
            String sentence = i + " hours";
            String mil;
            if (i < 10)
                mil = "0" + i + ":00:00";
            else
                mil = i + ":00:00";
            to24hour.put(sentence, mil);
        }

        return to24hour;
    }

}
