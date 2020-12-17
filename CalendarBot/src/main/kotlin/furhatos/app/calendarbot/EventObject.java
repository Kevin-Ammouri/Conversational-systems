package furhatos.app.calendarbot;

import java.text.ParseException;

public class EventObject {
    public String intent = null;

    // For adding
    public String day = null;
    public String date = null;
    public String dayContext = null;

    public String startTime = null;
    public String endTime = null;
    public String duration = null;
    public String timeContext = null;

    public String name = null;

    // For removing
    private String ID = null;

    // For listing
    public String dateTo = null;
    public String dayTo = null;

    // For interaction
    public String bookStatement = null;

    // For formatting dates
    public DateFormatter Formatter;

    public EventObject() {
        Formatter = new DateFormatter();
    }

    public String nextUnfilled() {
        if (day != null || date != null) {
            if (startTime != null && endTime != null) {
                if (name != null) {
                    return Constants.DONE;
                } else {
                    return Constants.NAME;
                }
            } else if (startTime != null && endTime == null) {
                return Constants.DURATION;
            } else if (startTime == null && (endTime != null || duration != null)) {
                return Constants.START_TIME;
            } else if (startTime == null && endTime == null && duration == null) {
                return Constants.START_TIME;
            }
        }

        return Constants.DATE;
    }

    public boolean setDate(String date, boolean startDate) {
        String[] StringBits = date.split(" ");
        String month = null;
        String number = null;
        String number_month = null;
        boolean specificDate = false;

        for(int i = 0; i < StringBits.length; i++) {
            if (number == null) {
                if (StringBits[i].matches("-?\\d+th") ||
                        StringBits[i].matches("-?\\d+nd") ||
                    StringBits[i].matches("-?\\d+st")) {
                    number = StringBits[i].substring(0, 2);
                }
            }
            if (month == null) {
                for (int j = 0; j < Constants.MONTHS.length; j++) {
                    if (StringBits[i].equalsIgnoreCase(Constants.MONTHS[j])) {
                        month = StringBits[i];
                    }
                }
            }
            if (number != null && month != null) {
                specificDate = true;
                number_month = number + " " + month;
                break;
            }
        }

        try {
            if (number_month == null) {
                if (startDate)
                    this.date = Formatter.format(date);
                else
                    this.dateTo = Formatter.format(date);
            } else {
                if (startDate)
                    this.date = Formatter.getDate(number_month);
                else
                    this.dateTo = Formatter.getDate(number_month);
            }
            if (startDate)
                this.day = date.toLowerCase();
            else
                this.dayTo = date.toLowerCase();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return specificDate;
    }

    public boolean setTime(String time, String startOrEnd) {
        /*
            Sets both the start time and end time in military time
            depending on the second argument input.
         */
        String[] StringBits = time.split(" ");
        String number = null;
        String amORpm = null;
        boolean timeContext = false;
        for(int i = 0; i < StringBits.length; i++) {
            if (amORpm == null) {
                if (StringBits[i].equalsIgnoreCase("m")) {
                    assert i > 0;
                    if (StringBits[i-1].equalsIgnoreCase("p")) {
                        amORpm = "pm";
                    } else if (StringBits[i-1].equalsIgnoreCase("a")) {
                        amORpm = "am";
                    }
                }
            }
            if (number == null) {
                if (StringBits[i].matches("-?\\d+")) {
                    number = StringBits[i];
                }
            }
            if (number != null && amORpm != null)
                break;

        }

        if (number == null && amORpm == null) {
            for (int i = 0; i < StringBits.length; i++) {
                for (int j = 0; j < Constants.TIMES_OF_THE_DAY.length; j++)
                    if (StringBits[i].equalsIgnoreCase(Constants.TIMES_OF_THE_DAY[j])) {
                        timeContext = true;
                        this.timeContext = time.toLowerCase();
                    }
            }
        } else {
            String toMilitary = Constants.TO24HOUR.get(number + " " + amORpm);
            if (startOrEnd.equalsIgnoreCase(Constants.START_TIME))
                this.startTime = toMilitary;
            else if (startOrEnd.equalsIgnoreCase(Constants.END_TIME))
                this.endTime = toMilitary;
        }

        return timeContext;
    }

    public void setDuration(String duration) {
        String[] StringBits = duration.split(" ");
        String number = null;
        String time = null;
        for(int i = 0; i < StringBits.length; i++) {
            if (number == null) {
                String numFound = Tools.wordToNumber(StringBits[i]);
                if (numFound.equalsIgnoreCase("0"))
                    continue;

                number = numFound;
            }

            if (time == null)
                for (int j = 0; j < Constants.TIMES.length; j++)
                    if (StringBits[i].equalsIgnoreCase(Constants.TIMES[j]))
                        time = StringBits[i];


            if (number != null && time != null)
                break;

        }

        String duration_sent = number + " " + time;
        this.duration = Constants.TO24HOUR.get(duration_sent);

        if (this.startTime != null && endTime == null) {
            try {
                this.endTime = Formatter.addTime(this.startTime, this.duration);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTimeContext(String timeofday) {
        String[] StringBits = timeofday.split(" ");
        for(int i = 0; i < StringBits.length; i++) {
            if (this.timeContext == null) {
                for (int j = 0; j < Constants.TIMES_OF_THE_DAY.length; j++) {
                    if (StringBits[i].equalsIgnoreCase(Constants.TIMES_OF_THE_DAY[j])) {
                        this.timeContext = StringBits[i];
                    }
                }
            } else {
                break;
            }
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean createID() {
        if (date != null && startTime != null) {
            String id = date + startTime;
            id = id.replaceAll(":", "");
            id = id.replaceAll("-", "");
            id = id.replaceAll(" ", "");
            setID(id);
            return true;
        }
        return false;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return this.ID;
    }

    public String toString() {
        return "[{Day: " + day + ", Date: " + date + ", DayContext: " + dayContext + "}\n{" +
                "Start Time: " + startTime + ", End Time: " + endTime + ", Duration: " + duration +
                ", Time of the day: " + timeContext + "}\n{" + "Name: " + name + "}]\n";
    }
}
