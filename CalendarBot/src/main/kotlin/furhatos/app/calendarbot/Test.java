package furhatos.app.calendarbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Test {

    public static void main(String... args) {
        try {
            GoogleCalendar c = new GoogleCalendar();
            /* Listing
            EventObject ev1 = new EventObject();
            ev1.date = "2021-09-24";
            ev1.startTime = "08:00:00";
            ev1.endTime = "13:00:00";
            List<HashMap<String, String>> list = c.ListEvents(ev1);
            for (HashMap<String, String> event : list) {
                System.out.println(event.get(Constants.NAME));
            }
            */


            EventObject ev = new EventObject();
            ev.date = "2021-08-18";
            ev.bookStatement = "Meeting";
            ev.startTime = "13:00:00";
            ev.endTime = "15:00:00";
            ev.name = "Hello Friday two";
            ev.createID();
            c.InsertEvent(ev);

            /*
            EventObject ev2 = new EventObject();
            ev2.timeContext = "Afternoon";
            ArrayList<String> s = Constants.TimeOfDay.get(ev2.timeContext);
            System.out.println(s);
            */

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}