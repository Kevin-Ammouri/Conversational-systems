package furhatos.app.calendarbot.flow

import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.calendarbot.nlu.*

val Start : State = state(Interaction) {

    onResponse<Add>{
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

}

val RemoveEvent = state(Interaction) {

}

val ListEvent = state(Interaction) {

}

val EditEvent = state(Interaction) {

}

val Greet = state(Interaction) {

}
