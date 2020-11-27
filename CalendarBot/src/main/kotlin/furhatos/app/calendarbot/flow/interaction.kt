package furhatos.app.calendarbot.flow

import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.calendarbot.nlu.*

val Start : State = state(Interaction) {

    onEntry {
        furhat.ask("Hi there. Do you like robots?")
    }

    onResponse<Yes>{
        furhat.say("I like humans.")
    }

    onResponse<No>{
        furhat.say("That's sad.")
    }
}
