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

}
