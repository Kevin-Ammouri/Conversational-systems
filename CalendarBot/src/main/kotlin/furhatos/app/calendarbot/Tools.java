package furhatos.app.calendarbot;

import com.google.api.services.calendar.model.Event;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tools {
    public static boolean formType(String bookStatement) {
        return bookStatement.endsWith("s");
    }

    public static boolean mapNameToID(EventObject ev) {
        String name = ev.name;
        String id = ev.getID();

        if (name == null || id == null) {
            return false;
        }

        HashMap<String, String> map = getHashMap();

        if (map == null)
            return false;

        map.put(name, id);

        try {
            FileOutputStream fos =
                    new FileOutputStream(Constants.HASHMAP_PATH);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(map);
            oos.close();
            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return false;
        }

        return true;
    }

    public static String getIdFromName(String name) {
        HashMap<String, String> map = getHashMap();

        if (map == null)
            return null;

        return map.get(name);
    }

    public static HashMap<String, String> getHashMap() {
        HashMap<String, String> map;

        try {
            FileInputStream fis = new FileInputStream(Constants.HASHMAP_PATH);
            ObjectInputStream ois = new ObjectInputStream(fis);
            map = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return null;
        } catch(ClassNotFoundException c) {
            c.printStackTrace();
            return null;
        }

        return map;
    }

    public static String interOptions(String statement, String option) {
        String sendBack = "";
        switch (option) {
            case Constants.PRONOUNCE:
                if (statement.toLowerCase().startsWith("a") || statement.toLowerCase().startsWith("e")) {
                    sendBack = "an";
                } else {
                    sendBack =  "a";
                }
                break;


            case Constants.TIME_OR_CONTEXT:
                for (String timeofday : Constants.TIMES_OF_THE_DAY) {
                    if (statement.toLowerCase().contains(timeofday.toLowerCase())) {
                        sendBack = Constants.YES;
                    }
                }
                if (!sendBack.equals(Constants.YES)) {
                    sendBack = Constants.NO;
                }
                break;

            case Constants.REMOVE_PLURAL:
                if (formType(statement)) {
                    statement = statement.substring(0, statement.length()-1);
                }
                return statement;

            default: //This is for configuring the day
                //statement is date sent
                //option is the format on how we should return
                for (int i = 0; i < Constants.DAYS_OF_THE_WEEK.length; i++) {
                    if (option.contains(Constants.DAYS_OF_THE_WEEK[i])) {
                        try {
                            DateFormatter formatter = new DateFormatter();
                            String weekday = formatter.WeekDay(statement);
                            return option.replace(Constants.DAYS_OF_THE_WEEK[i], weekday);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                String[] date = statement.split("-");
                String day = date[date.length-1];
                if (!day.startsWith("1") && day.endsWith("1")) {
                    day += "st";
                } else if (!day.startsWith("1") && day.endsWith("2")) {
                    day += "nd";
                } else if (!day.startsWith("1") && day.endsWith("3")){
                    day += "rd";
                } else {
                    day += "th";
                }

                String[] StringBits = option.split(" ");
                for(int i = 0; i < StringBits.length; i++) {
                    if (StringBits[i].matches("-?\\d+th") ||
                            StringBits[i].matches("-?\\d+nd") ||
                            StringBits[i].matches("-?\\d+st")) {

                        return option.replace(StringBits[i], day);

                    }
                }
        }
        return sendBack;
    }

    public static List<HashMap<String, String>> PrettifyItemList(List<Event> items) {
        DateFormatter dateFormatter = new DateFormatter();
        try {
            List<HashMap<String, String>> events = new ArrayList<>();
            for (Event event : items) {
                String[] startInfo = event.getStart().getDateTime().toString().split("T|\\.");
                String[] endInfo = event.getEnd().getDateTime().toString().split("T|\\.");
                String[] bookAndName = event.getSummary().split("->");

                HashMap<String, String> hm = new HashMap<>();
                hm.put(Constants.DATE, startInfo[0]);
                hm.put(Constants.START_TIME, startInfo[1]);
                hm.put(Constants.END_DATE, startInfo[0]);
                hm.put(Constants.END_TIME, endInfo[1]);
                hm.put(Constants.DURATION, dateFormatter.addTime(endInfo[1], "-" + startInfo[1]));
                hm.put(Constants.BOOK_STATEMENT, bookAndName[0]);
                hm.put(Constants.NAME, bookAndName[1]);

                events.add(hm);
            }
            return events;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, ArrayList<String>> createTimeContextList() {
        HashMap<String, ArrayList<String>> TimeOfDay = new HashMap<>();

        ArrayList<String> morning = new ArrayList<>();
        morning.add(0, "06:00:00");
        morning.add(1, "12:00:00");

        TimeOfDay.put(Constants.TIMES_OF_THE_DAY[0].toLowerCase(), morning);

        ArrayList<String> afternoon = new ArrayList<>();
        afternoon.add(0, "12:00:00");
        afternoon.add(1, "18:00:00");

        TimeOfDay.put(Constants.TIMES_OF_THE_DAY[1].toLowerCase(), afternoon);

        ArrayList<String> evening = new ArrayList<>();
        evening.add(0, "18:00:00");
        evening.add(1, "22:00:00");

        TimeOfDay.put(Constants.TIMES_OF_THE_DAY[2].toLowerCase(), evening);

        ArrayList<String> night = new ArrayList<>();
        night.add(0, "22:00:00");
        night.add(1, "06:00:00");

        TimeOfDay.put(Constants.TIMES_OF_THE_DAY[3].toLowerCase(), night);

        return TimeOfDay;
    }

    public static HashMap<String, String> create24HourMapping(boolean returnTo) {
        HashMap<String, String> to24hour = new HashMap<>();
        HashMap<String, String> from24hour = new HashMap<>();

        to24hour.put("12 am", "00:00:00");
        from24hour.put("00:00:00", "12 am");
        for (int i = 1; i < 10; i++) {
            String non_mil = i + " am";
            String mil = "0" + i + ":00:00";
            to24hour.put(non_mil, mil);
            from24hour.put(mil, non_mil);
        }
        to24hour.put("10 am", "10:00:00");
        to24hour.put("11 am", "11:00:00");
        to24hour.put("12 pm", "12:00:00");

        from24hour.put("10:00:00", "10 am");
        from24hour.put("11:00:00", "11 am");
        from24hour.put("12:00:00", "12 pm");
        for (int i = 1; i < 12; i++) {
            String non_mil = i + " pm";
            String mil;
            if (i < 8)
                mil = "1" + (i+2) + ":00:00";
            else
                mil = "2" + (i%8) + ":00:00";
            to24hour.put(non_mil, mil);
            from24hour.put(mil, non_mil);
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
        to24hour.put("01:00:00", "1 hour");
        for (int i = 2; i <= 23; i++) {
            String sentence = i + " hours";
            String mil;
            if (i < 10)
                mil = "0" + i + ":00:00";
            else
                mil = i + ":00:00";
            to24hour.put(sentence, mil);
            to24hour.put(mil, sentence);
        }

        if (returnTo)
            return to24hour;
        else
            return from24hour;
    }

    public static String wordToNumber(String word) {
        if (word.matches("-?\\d+"))
            return word;

        int result = 0;
        int finalResult = 0;

        if(word == null || word.length() <= 0)
            return null;

        word = word.replaceAll("-", " ");
        word = word.toLowerCase().replaceAll(" and", " ");
        String[] splittedParts = word.trim().split("\\s+");

        for(String str : splittedParts)
        {
            if(str.contains("zero"))
                result += 0;
            else if(str.contains("one"))
                result += 1;
            else if(str.contains("two"))
                result += 2;
            else if(str.contains("three"))
                result += 3;
            else if(str.contains("four"))
                result += 4;
            else if(str.contains("five"))
                result += 5;
            else if(str.contains("six"))
                result += 6;
            else if(str.contains("seven"))
                result += 7;
            else if(str.contains("eight"))
                result += 8;
            else if(str.contains("nine"))
                result += 9;
            else if(str.contains("ten"))
                result += 10;
            else if(str.contains("eleven"))
                result += 11;
            else if(str.contains("twelve"))
                result += 12;
            else if(str.contains("thirteen"))
                result += 13;
            else if(str.contains("fourteen"))
                result += 14;
            else if(str.contains("fifteen"))
                result += 15;
            else if(str.contains("sixteen"))
                result += 16;
            else if(str.contains("seventeen"))
                result += 17;
            else if(str.contains("eighteen"))
                result += 18;
            else if(str.contains("nineteen"))
                result += 19;
            else if(str.contains("twenty"))
                result += 20;
            else if(str.contains("thirty"))
                result += 30;
            else if(str.contains("forty"))
                result += 40;
            else if(str.contains("fifty"))
                result += 50;
            else if(str.contains("sixty"))
                result += 60;
            else if(str.contains("seventy"))
                result += 70;
            else if(str.contains("eighty"))
                result += 80;
            else if(str.contains("ninety"))
                result += 90;
        }

        finalResult += result;

        return Integer.toString(finalResult);
    }

    public static void debugPrint(EventObject ev) {
        System.out.println("INTENT: " + ev.intent);

        System.out.println("DATE: " + ev.date);
        System.out.println("DAY: " + ev.day);
        System.out.println("DAY CONTEXT: " + ev.dayContext);

        System.out.println("START TIME: " + ev.startTime);
        System.out.println("END TIME: " + ev.endTime);
        System.out.println("TIME CONTEXT: " + ev.timeContext);
        System.out.println("DURATION: " + ev.duration);

        System.out.println("NAME: " + ev.name);
        System.out.println("BOOK STATEMENT: " + ev.bookStatement);
        System.out.println("bookStatement ends with s?: " + Tools.formType(ev.bookStatement));

        System.out.println("NEXT INFO REQUIRED: " + ev.nextUnfilled());
        System.out.println("------------------------------------");

        /*
        println("FURHAT CAUGHT DATE: " + it.intent.date.toString())
        println("FURHAT CAUGHT startTime: " + it.intent.startTime.toString())
        println("FURHAT CAUGHT endTime: " + it.intent.endTime.toString())
        println("FURHAT CAUGHT duration: " + it.intent.duration.toString())
        println("FURHAT CAUGHT daycontext: " + it.intent.dayContext.toString())
        println("FURHAT CAUGHT name: " + it.intent.name.toString())
        */
    }

    public static String getTimeContext(String time) {
        for (int i = 0; i < Constants.TIMES_OF_THE_DAY.length; i++) {
            ArrayList<String> times = Constants.TimeOfDay.get(Constants.TIMES_OF_THE_DAY[i].toLowerCase());
            if (time.compareTo(times.get(0)) >= 0 && time.compareTo(times.get(1)) < 0)
                return Constants.TIMES_OF_THE_DAY[i].toLowerCase();
        }
        return null;
    }

    public static ArrayList<ArrayList<String>> availableGaps(EventObject ev, GoogleCalendar calendar, int day)
            throws ParseException {

        DateFormatter df = new DateFormatter();
        ev.startTime = null;
        ev.endTime = null;
        List<HashMap<String, String>> list;

        ArrayList<ArrayList<String>> availableTimeGaps = new ArrayList<ArrayList<String>>(); // Create an ArrayList object
        ArrayList<Integer> availableTimeGapsCorrespondingDay = new ArrayList<Integer>(); // Create an ArrayList object

        // Variables used in the for loop to keep track of index and day.
        int gapCounter = 0;


        // Suggest a time slot for the user if only a timeContext is given.
        if (ev.timeContext != null) {
            ArrayList<String> time_bounds = Constants.TimeOfDay.get(ev.timeContext);
            ev.startTime = time_bounds.get(0);
            ev.endTime = time_bounds.get(1);
            list = calendar.listEvents(ev);
        } else {
            list = calendar.listEvents(ev);
            ev.startTime = "08:00:00";
            ev.endTime = "20:00:00";
        }
        // If there is no events book between the specific time slots
        // we return the whole interval.
        if (list == null || list.isEmpty()) {
            // Start and end time of the gap
            ArrayList<String> availableTimeGapToAdd = new ArrayList<String>(); // Create an ArrayList object
            availableTimeGapToAdd.add(0, ev.startTime);
            availableTimeGapToAdd.add(1, ev.endTime);

            // Add the gap into the list.
            availableTimeGaps.add(gapCounter, availableTimeGapToAdd);
            return availableTimeGaps;
        }

        String defaultStart = ev.startTime;
        String defaultEnd = ev.endTime;
        String duration = ev.duration;

        String checkAvailable = df.addTime(defaultStart, duration);
        String startTimePrev = list.get(0).get(Constants.START_TIME);
        String endTimePrev = list.get(0).get(Constants.END_TIME);

        //Get the first base case time gap. if there exist one.
        if (checkAvailable.compareTo(startTimePrev) <= 0) {
            // Start and end time of the gap
            ArrayList<String> availableTimeGapToAdd = new ArrayList<String>(); // Create an ArrayList object
            availableTimeGapToAdd.add(0, defaultStart);
            availableTimeGapToAdd.add(1, startTimePrev);

            // Add the gap into the list.
            availableTimeGaps.add(gapCounter, availableTimeGapToAdd);

            // Corresponding day of the available time gap.
            availableTimeGapsCorrespondingDay.add(gapCounter, day);

            //Update amount of gaps counter
            gapCounter += 1;
        }

        for (HashMap<String, String> event : list.subList(1, list.size())) {
            checkAvailable = df.addTime(endTimePrev, duration);
            String starTime = event.get(Constants.START_TIME);
            String endTime = event.get(Constants.END_TIME);

            //check if the endTime + duration is less than next event startTime.
            if (checkAvailable.compareTo(starTime) <= 0) {
                // Start and end time of the gap
                ArrayList<String> availableTimeGapToAdd = new ArrayList<String>(); // Create an ArrayList object
                availableTimeGapToAdd.add(0, endTimePrev);
                availableTimeGapToAdd.add(1, starTime);

                // Add the gap into the list.
                availableTimeGaps.add(gapCounter, availableTimeGapToAdd);

                // Corresponding day of the available time gap.
                availableTimeGapsCorrespondingDay.add(gapCounter, day);
                gapCounter += 1;
            }
            startTimePrev = starTime;
            endTimePrev = endTime;
        }

        checkAvailable = df.addTime(endTimePrev, duration);

        //Get the last base case time gap. if there exist one.
        if (checkAvailable.compareTo(defaultEnd) <= 0) {
            // Start and end time of the gap
            ArrayList<String> availableTimeGapToAdd = new ArrayList<String>(); // Create an ArrayList object
            availableTimeGapToAdd.add(0, endTimePrev);
            availableTimeGapToAdd.add(1, defaultEnd);

            // Add the gap into the list.
            availableTimeGaps.add(gapCounter, availableTimeGapToAdd);

            // Corresponding day of the available time gap.
            availableTimeGapsCorrespondingDay.add(gapCounter, day);
        }

        // Return all available time gaps found for the given time interval.
        return availableTimeGaps;
    }

    public static ArrayList<AvailableTimes> findAvailableTime(EventObject ev, int amountOfDaysToSuggest, GoogleCalendar calendar)
            throws ParseException {
        // Used in the end to go back to the original date and day in the ev object.
        String startingDate = ev.date;
        String startingDay = ev.day;

        // Creating a list with instances of the class AvailableTimes.
        ArrayList<AvailableTimes> availableTimes = new ArrayList<AvailableTimes>();

        // Creating a instance of the DateFormatter class.
        DateFormatter df = new DateFormatter();

        for(int i = 0; i < amountOfDaysToSuggest; i++) {

            // Method call to availableGaps to extract all available time gaps that our event could fit into.
            // Input, a given event and an instance of the google calendar api class.
            ArrayList<ArrayList<String>> availableGaps = availableGaps(ev, calendar, i);
            //System.out.println(availableGaps);

            // creating an instance of  availableTime object and the available times array list..
            AvailableTimes availableTime = new AvailableTimes();
            ArrayList<String> times = new ArrayList<>();
            //System.out.println(ev.date);
            for (ArrayList<String> gap : availableGaps) {
                String startTime = gap.get(0);
                String endTime = gap.get(1);

                String checkIfValidTime = df.addTime(startTime, ev.duration);
                // While start time + duration is less than or equal to end time.
                // Add new possible time spot for the event to be added into.
                while (checkIfValidTime.compareTo(endTime) <= 0) {
                    times.add(startTime);
                    startTime = df.addTime(startTime, "01:00:00");
                    checkIfValidTime = df.addTime(startTime, ev.duration);
                }
            }

            // System.out.println(times);
            // System.out.println(ev.day);
            // System.out.println("--------------------------");
            // Add all the available times for a specific day into the list that is to be returned.
            availableTime.startTimes = times;
            availableTime.date = ev.date;
            availableTime.day = ev.day;

            // This one is used to avoid problems if today is friday. hence we grep the
            // day value again using the date.
            String day = df.WeekDay(ev.date);

            // I believe that this should be the case, but if it is:
            // Need change if day could be null in any case.
            // Need to change if MEETING  could be null in any case.
            if(day.toLowerCase().contains("friday") && ev.bookStatement.equalsIgnoreCase(Constants.MEETING)){
                ev.date = df.addDate(ev.date,"0000-00-03");
            }else {
                ev.date = df.addDate(ev.date, "0000-00-01");
            }
            ev.day = interOptions(ev.date, startingDay);
            availableTimes.add(i, availableTime);

        }
        // Go back to the original date and day.
        ev.date = startingDate;
        ev.day = startingDay;

        return availableTimes;
    }
}
