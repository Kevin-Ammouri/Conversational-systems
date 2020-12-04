package furhatos.app.calendarbot.flow

import furhatos.app.calendarbot.Constants
import furhatos.app.calendarbot.Event
import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.calendarbot.nlu.*
import furhatos.nlu.Intent

var ev = Event()

val Start : State = state(Interaction) {

    onEntry {
        furhat.ask("")
    }

    onResponse<Add>{
        ev.intent = Constants.ADD_INTENT
        val startTime = it.intent.data?.startTime
        val date = it.intent.data?.date
        val day = it.intent.data?.day
        val duration = it.intent.data?.duration
        val dayContext = it.intent.data?.dayContext
        val endTime = it.intent.data?.endTime
        val timeOfTheday = it.intent.data?.timeOfDay
        val name = it.intent.data?.name

        System.out.println("DATE: " + date)
        System.out.println("START TIME: " + startTime)
        System.out.println("DURATION: " + duration)


        if (startTime != null) {
            ev.setTime(startTime.toText(), Constants.START_TIME)
        }

        if (date != null) {
            ev.setDate(date.toText())
        }

        if (day != null) {
            ev.setDay(day.toText())
        }

        if (duration != null) {
            ev.setDuration(duration.toText())
        }

        if (dayContext != null) {
            ev.setDayContext(dayContext.toText())
        }

        if (endTime != null) {
            ev.setTime(endTime.toText(), Constants.END_TIME)
        }

        if (timeOfTheday != null) {
            ev.setTimeOfDay(timeOfTheday.toText())
        }

        if (name != null) {
            ev.setName(name.toText())
        }


        var nextInfo = ev.nextUnfilled()
        while (nextInfo != Constants.DONE) {
            System.out.println(ev.toString())
            if (nextInfo == Constants.DATE) {
                var date = furhat.askFor<DaysOfTheWeek>(random("" +
                        "Sure, which date does this event concern?", "Okay. Which date?"
                ))
                ev.setDate(date?.toText())
            } else if (nextInfo == Constants.DURATION) {
                var duration = furhat.askFor<Duration>(random(
                        "Sure, how long will the event last?", "Okay, how long will the event go on for?"
                ))
                System.out.println("DURATIONNNN: " + duration)
                ev.setDuration(duration?.toText())
                //TODO: Since we now have start time and duration, we should calculate the end time and insert it into the event object.
                ev.setTime("9 p m", Constants.END_TIME) // TEMPORARY HARDCODED STATEMENT
            } else if (nextInfo == Constants.START_TIME) {
                var startTime = furhat.askFor<Time>(random(
                        "Sure, when will the event begin?", "Okay, when does the event start?"
                ))
                ev.setTime(startTime?.toText(), Constants.START_TIME)
            } else if (nextInfo == Constants.NAME) {
                var name = furhat.askFor<Name>(random(
                        "Sure, what will you name the event?", "Okay, what is the name of the event?"
                ))
                System.out.println("NAAAAME: " + name)
                ev.setName(name?.toText())
            }
            nextInfo = ev.nextUnfilled()
        }
        System.out.println(ev.toString())
        furhat.say("Your event has been added to your calendar.")
    }

    onResponse<Remove>{
        goto(RemoveEvent)
    }

    onResponse<ListEv> {
        goto(ListEvent)
    }

    onResponse<Edit> {
        goto(EditEvent)
    }

    onResponse<Greeting> {
        goto(Greet)
    }
}

val AddEvent = state(Interaction) {
    onEntry{

    }
}



val RemoveEvent = state(Interaction) {

}

val ListEvent = state(Interaction) {

}

val EditEvent = state(Interaction) {

}

val Greet = state(Interaction) {
    onEntry {
        furhat.say { "Hello, how can I help you?"}
        goto(Start)
    }
}
