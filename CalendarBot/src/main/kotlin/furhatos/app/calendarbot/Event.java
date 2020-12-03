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
                if (StringBits[i].matches("-?\\d+th")) { //notice the th, pretty hardcoded
                    number = StringBits[i].substring(0, 2);
                }
            }
            if (month == null) {
                for (int j = 0; j < Constants.MONTHS.length; j++) {
                    if (StringBits[i].equals(Constants.MONTHS[j])) {
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
                if (StringBits[i].equals("m")) {
                    assert i > 0;
                    if (StringBits[i-1].equals("p")) {
                        amORpm = "pm";
                    } else if (StringBits[i-1].equals("a")) {
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
        this.startTime = number + " " + amORpm;
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
                    if (StringBits[i].equals(Constants.TIMES[j])) {
                        time = StringBits[i];
                    }
                }
            }
            if (number != null && time != null)
                break;

        }
        this.duration = "number" + " " + time;
    }

    public void setTimeOfDay(String timeofday) {
        String[] StringBits = date.split(" ");
        for(int i = 0; i < StringBits.length; i++) {
            if (this.timeOfDay == null) {
                for (int j = 0; j < Constants.TIMESOFTHEDAY.length; j++) {
                    if (StringBits[i].equals(Constants.TIMESOFTHEDAY[j])) {
                        this.timeOfDay = StringBits[i];
                    }
                }
            } else {
                break;
            }
        }
    }
}
