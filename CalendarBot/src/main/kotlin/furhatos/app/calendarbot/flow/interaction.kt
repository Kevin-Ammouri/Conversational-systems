package furhatos.app.calendarbot.flow

import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.calendarbot.nlu.*



val Start : State = state(Interaction) {

    onEntry {
        furhat.ask("")
    }

    onResponse<Add>{
        val date = it.intent.date
        furhat.say("${date?.text}")
        goto(AddEvent)
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
    onEntry {


    }

}

val RemoveEvent = state(Interaction) {

}

val ListEvent = state(Interaction) {

}

val EditEvent = state(Interaction) {

}

val Greet = state(Interaction) {

}
