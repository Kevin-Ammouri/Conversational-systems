package furhatos.app.calendarbot;

import java.text.ParseException;

public class EventObject {
    public String intent = null;

    /** For adding */
    public String day = null;
    public String date = null;
    public String dayContext = null;

    public String startTime = null;
    public String endTime = null;
    public String duration = null;
    public String timeContext = null;

    public String name = null;

    /** For removing */
    private String ID = null;

    /** For listing */
    public String dateTo = null;
    public String dayTo = null;

    /** For interaction */
    public String bookStatement = null;
    public String addStatement = null;
    public String removeStatement = null;
    public String listStatement = null;

    /** Status codes and other options */
    public String status = Constants.PENDING;

    /** For formatting dates */
    public DateFormatter Formatter;

    public EventObject() {
        Formatter = new DateFormatter();
    }

    public String nextUnfilled() {
        switch(this.intent) {
            case Constants.ADD_INTENT:
                if (day != null && date != null) {

                    if (timeContext != null && duration != null && startTime == null) {
                        return Constants.BE_PROACTIVE;
                    } else if (timeContext != null && duration == null) {
                        return Constants.DURATION;
                    }

                    if (startTime != null && endTime != null) {
                        if (name != null && name != "null") {
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

            case Constants.REMOVE_INTENT:
                if (name != null)
                    return Constants.DONE;

                if (this.date != null && this.startTime == null)
                    return Constants.LIST_INTENT;

                if (date == null)
                    return Constants.DATE;

                if (startTime == null)
                    return Constants.START_TIME;

                return Constants.DONE;

            case Constants.LIST_INTENT:
                if (this.date == null)
                    return Constants.DATE;

                return Constants.DONE;

            case Constants.GET_INTENT:
                if (this.date == null)
                    return Constants.DATE;

                if (this.startTime == null)
                    return Constants.START_TIME;

                return Constants.DONE;
        }

        return null;
    }

    public boolean setDate(String date) {
        String[] StringBits = date.split(" ");
        String month = null;
        String number = null;
        String number_month = null;
        boolean specificDate = false;

        for(int i = 0; i < StringBits.length; i++) {
            if (number == null) {
                if (StringBits[i].matches("-?\\d+th") || StringBits[i].matches("-?\\d+nd") ||
                    StringBits[i].matches("-?\\d+st") || StringBits[i].matches("-?\\d+rd")) {
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
            if (number != null && month == null) {
                number_month = number + " " + Formatter.getMonth(number);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        try {
            if (number_month == null) {
                this.date = Formatter.format(date);
            } else {
                this.date = Formatter.getDate(number_month);
            }

            if (date.contains("null")) {
                this.day = date.toLowerCase().replace("null", "");
            } else {
                this.day = date.toLowerCase();
            }

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

        boolean timeContext = false;
        if (number == null || amORpm == null) {
            for (int i = 0; i < StringBits.length; i++) {
                for (int j = 0; j < Constants.TIMES_OF_THE_DAY.length; j++)
                    if (StringBits[i].equalsIgnoreCase(Constants.TIMES_OF_THE_DAY[j])) {
                        timeContext = true;
                        this.timeContext = time.toLowerCase();
                    }
            }
            if (!timeContext) {
                if (startOrEnd.equalsIgnoreCase(Constants.START_TIME))
                    this.startTime = time;
                else if (startOrEnd.equalsIgnoreCase(Constants.END_TIME))
                    this.endTime = time;
            }
        } else {
            String toMilitary = Constants.TO24HOUR.get(number + " " + amORpm);
            if (startOrEnd.equalsIgnoreCase(Constants.START_TIME))
                this.startTime = toMilitary;
            else if (startOrEnd.equalsIgnoreCase(Constants.END_TIME))
                this.endTime = toMilitary;
        }

        try {
            if (this.startTime != null && this.endTime != null) {
                this.duration = Formatter.addTime(this.endTime, "-" + this.startTime);
            } else if (this.startTime != null && this.duration != null) {
                this.endTime = Formatter.addTime(this.startTime, this.duration);
            } else if (this.endTime != null && this.duration != null) {
                this.startTime = Formatter.addTime(this.endTime, "-" + this.duration);
            }
        } catch (ParseException e) {
            e.printStackTrace();
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

        try {
            if (this.startTime != null && endTime == null) {
                this.endTime = Formatter.addTime(this.startTime, this.duration);
            } else if (this.startTime == null && this.endTime != null) {
                this.startTime = Formatter.addTime(this.endTime, "-" + this.duration);
            }
        } catch (ParseException e) {
            e.printStackTrace();
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
        String id = Tools.getIdFromName(name);
        this.ID = id;
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
