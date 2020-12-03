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
        val date = it.intent.dateWrapper?.date
        val time = it.intent.dateWrapper?.time
        ev.setDate(date?.toText())
        ev.setStartTime(time?.toText())
        var nextInfo = ev.nextUnfilled()
        while (nextInfo != Constants.DONE) {
            if (nextInfo == Constants.DAY) {
                var day = furhat.askFor<DaysOfTheWeek>(random("" +
                        "Sure, which day?", "Okay. Which day of the week?"
                ))
                ev.setDay(day?.toText())
            } else if (nextInfo == Constants.DURATION) {
                var duration = furhat.askFor<Duration>(random(
                        "Sure, how long will the event last?", "Okay, how long will the event go on for?"
                ))
                ev.setDuration(duration?.toText())
            } else if (nextInfo == Constants.START_TIME) {
                var startTime = furhat.askFor<Time>(random(
                        "Sure, when will the event begin?", "Okay, when does the event start?"
                ))
                ev.setStartTime(startTime?.toText())
            } else if (nextInfo == Constants.NAME) {
                var name = furhat.askFor<Name>(random(
                        "Sure, what will you name the event?", "Okay, what is the name of the event?"
                ))
            }
            nextInfo = ev.nextUnfilled()
        }
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
