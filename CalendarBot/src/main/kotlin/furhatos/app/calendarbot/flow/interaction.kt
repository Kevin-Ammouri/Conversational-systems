package furhatos.app.calendarbot.flow

import furhatos.app.calendarbot.Event
import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.calendarbot.nlu.*
import furhatos.nlu.Intent

val Start : State = state(Interaction) {

    onEntry {
        furhat.ask("")
    }

    onResponse<Add>{
        val date = it.intent.dateWrapper?.date
        val time = it.intent.dateWrapper?.time
        var ev = Event()
        ev.setDate(date?.toText())
        ev.setStartTime(time?.toText())
        furhat.say("${ev.date}, ${ev.startTime}, ${ev.nextUnfilled()}")

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
