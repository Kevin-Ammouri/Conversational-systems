package furhatos.app.calendarbot;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class Tools {
    public static boolean GoogleAPICall(EventObject ev) {
        GoogleCalendar calendar;
        try {
            calendar = new GoogleCalendar();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean SendToRemoveAPI(String ID) {
        //Skicka id:et till Lucas
        //Lucas skickar en boolean från sin metod, True eller False

        return false;
    }

    public static boolean FormType(String bookStatement) {
        return bookStatement.endsWith("s");
    }

    public static boolean MapNameToID(String name, String id) {
        HashMap<String, String> map = GetHashMap();

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

    public static String GetIDFromName(String name) {
        HashMap<String, String> map = GetHashMap();

        if (map == null)
            return null;

        return map.get(name);
    }

    private static HashMap<String, String> GetHashMap() {
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

    public static HashMap<String, String> createTO24HOUR() {
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
