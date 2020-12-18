package furhatos.app.calendarbot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class DateFormatter {
    SimpleDateFormat DateFormat;

    public DateFormatter() {
        DateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public String format(String s) throws ParseException {
        if (s == null) {
            s = "";
        }
        Calendar cal = Calendar.getInstance();
        Date curr = cal.getTime();
        String[] DaysOfWeek = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        String DateToAdd = "0000-00-00";

        boolean fixed = false;
        if (s.toLowerCase().contains("tomorrow")) {
            DateToAdd = "0000-00-01";
            fixed = true;
        }
        if (s.toLowerCase().contains("next week")) {
            DateToAdd = "0000-00-07";
            fixed = true;
        }
        if (s.toLowerCase().contains("next month")) {
            DateToAdd = "0000-01-00";
            fixed = true;
        }
        int idx = 0;
        String[] tmpArr = s.toLowerCase().split(" ");
        if (!fixed && s.toLowerCase().contains("weeks")) {
            for (int i = 0; i < tmpArr.length; i++) {
                if (tmpArr[i].equalsIgnoreCase("weeks")) {
                    idx = i;
                    break;
                }
            }
            int weekToAdd = Integer.parseInt(Tools.wordToNumber(tmpArr[idx-1])) * 7;
            if (weekToAdd < 10) {
                DateToAdd = "0000-00-0" + weekToAdd;
            } else {
                DateToAdd = "0000-00-" + weekToAdd;
            }

        }

        if (!fixed && s.toLowerCase().contains("months")) {
            for (int i = 0; i < tmpArr.length; i++) {
                if (tmpArr[i].equalsIgnoreCase("months")) {
                    idx = i;
                    break;
                }
            }
            int monthToAdd = Integer.parseInt(Tools.wordToNumber(tmpArr[idx-1]));
            if (monthToAdd < 10) {
                DateToAdd = "0000-0" + monthToAdd + "-00";
            } else {
                DateToAdd = "0000-" + monthToAdd + "-00";
            }

        }
        if (!fixed && s.toLowerCase().contains("days")) {
            for (int i = 0; i < tmpArr.length; i++) {
                if (tmpArr[i].equalsIgnoreCase("days")) {
                    idx = i;
                    break;
                }
            }
            int dayToAdd = Integer.parseInt(Tools.wordToNumber(tmpArr[idx-1]));
            if (dayToAdd < 10) {
                DateToAdd = "0000-00-0" + dayToAdd;
            } else {
                DateToAdd = "0000-00-" + dayToAdd;
            }

        }

        //String date = addDate(DateToAdd);
        String dayTarget = "";
        for (String day: DaysOfWeek) {
            if (s.toLowerCase().contains(day)) {
                dayTarget = day;
            }
        }

        int diff = 0;
        DayOfWeek CurrDay = LocalDate.now().getDayOfWeek();
        DayOfWeek o = LocalDate.now().getDayOfWeek();
        if (!dayTarget.equals("")) {
            while(!CurrDay.name().equalsIgnoreCase(dayTarget)) {
                diff++;
                CurrDay = LocalDate.now().plusDays(diff).getDayOfWeek();
            }
            String[] arr = DateToAdd.split("-");

            int years = Integer.parseInt(arr[0]);
            int months = Integer.parseInt(arr[1]);
            int days = Integer.parseInt(arr[2]);

            cal.add(Calendar.DAY_OF_MONTH, days);;
            cal.add(Calendar.MONTH, months);
            cal.add(Calendar.YEAR, years);

            if (o.getValue() > CurrDay.getValue()){
                diff -= 7;
            }
            cal.add(Calendar.DAY_OF_MONTH, diff);

        }

        Date date = cal.getTime();
        String strDate = DateFormat.format(date);

        //String toReturn = addDate(strDate);
        cal.setTime(curr);
        return strDate;
    }

    public String getDate(String currDate) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy");

        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        int year = cal.get(Calendar.YEAR);
        String resDate = currDate + " " + year;
        Date tmp = df.parse(resDate);
        if (tmp.before(now)) {
            tmp = df.parse(currDate + " " + (year+1));
        }
        String d = DateFormat.format(tmp);

        return d;
    }
    
    public String addTime(String StartTime, String Duration) throws ParseException {
        Calendar cal = Calendar.getInstance();
        Date curr = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        Date sDay = df.parse(StartTime);

        cal.setTime(sDay);
        // Demonstrate Calendar's get()method
        String[] arr = Duration.split(":");

        int hours = Integer.parseInt(arr[0]);
        int minutes = Integer.parseInt(arr[1]);
        int seconds = Integer.parseInt(arr[2]);

        cal.add(Calendar.HOUR, hours);
        cal.add(Calendar.MINUTE, minutes);
        cal.add(Calendar.SECOND, seconds);

        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String strDate = dateFormat.format(date);

        cal.setTime(curr);
        return strDate;
    }

    public String WeekDay(String date) throws ParseException {

        //cal.setTime(DateFormat.parse(date));
        DateFormat format2 = new SimpleDateFormat("EEEE");

        return format2.format(DateFormat.parse(date));
    }
}
