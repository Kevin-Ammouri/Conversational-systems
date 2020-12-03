package furhatos.app.calendarbot;

public class Event {
    public String day = null;
    public String date = null;
    public String dayContext = null;

    public String startTime = null;
    public String endTime = null;
    public String duration = null;
    public String timeOfDay = null;

    public String name = null;

    public Event() {}

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
            }
        }

        return Constants.DAY;
    }

    public void setDate(String date) {
        String[] StringBits = date.split(" ");
        String month = null;
        String number = null;
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
            if (number != null && month != null)
                break;
        }
        this.date = number + " " + month;
    }

    public void setStartTime(String time) {
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
        String startTime = number + " " + amORpm;
        this.startTime = Constants.TO24HOUR.get(startTime);
    }

    public void setDuration(String duration) {
        String[] StringBits = duration.split(" ");
        String number = null;
        String time = null;
        for(int i = 0; i < StringBits.length; i++) {
            if (number == null) {
                if (StringBits[i].matches("-?\\d+")) {
                    number = StringBits[i];
                }
            }
            if (time == null) {
                for (int j = 0; j < Constants.TIMES.length; j++) {
                    if (StringBits[i].equalsIgnoreCase(Constants.TIMES[j])) {
                        time = StringBits[i];
                    }
                }
            }
            if (number != null && time != null)
                break;

        }
        String duration_sent = number + " " + time;
        this.duration = Constants.TO24HOUR.get(duration_sent);
    }

    public void setTimeOfDay(String timeofday) {
        String[] StringBits = date.split(" ");
        for(int i = 0; i < StringBits.length; i++) {
            if (this.timeOfDay == null) {
                for (int j = 0; j < Constants.TIMESOFTHEDAY.length; j++) {
                    if (StringBits[i].equalsIgnoreCase(Constants.TIMESOFTHEDAY[j])) {
                        this.timeOfDay = StringBits[i];
                    }
                }
            } else {
                break;
            }
        }
    }

    public void setDay(String day) {
        String[] StringBits = date.split(" ");
        if (StringBits.length == 1) {
            this.day = day;
        }
        for(int i = 0; i < StringBits.length; i++) {
            for(int j = 0; j < Constants.DAYS.length; j++) {
                if (this.day == null)
                    if (StringBits[i].equalsIgnoreCase(Constants.DAYS[j]))
                        this.day = StringBits[i];
                else
                    break;
            }
        }
    }

    public void setName(String name) { //TODO: NOT DONE YET
        this.name = name;
    }
}
