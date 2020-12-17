package furhatos.app.calendarbot;

import com.google.api.services.calendar.model.Event;

import java.io.*;
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
}
