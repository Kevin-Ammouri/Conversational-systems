package furhatos.app.calendarbot.flow

import furhatos.app.calendarbot.Constants
import furhatos.app.calendarbot.EventObject
import furhatos.app.calendarbot.GoogleCalendar
import furhatos.app.calendarbot.Tools
import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.calendarbot.nlu.*

var ev = EventObject()
var calendar = GoogleCalendar()

val Start : State = state(Interaction) {

    onEntry {
        furhat.ask("")
    }

    onResponse<Add>{
        ev.intent = Constants.ADD_INTENT

        val startTime = it.intent.startTime
        val date = it.intent.date
        val duration = it.intent.duration
        val endTime = it.intent.endTime
        val name = it.intent.name
        val dayContext = it.intent.dayContext

        System.out.println("INTENT: " + Constants.ADD_INTENT)
        System.out.println("DATE: " + date)
        System.out.println("START TIME: " + startTime)
        System.out.println("END TIME: " + endTime)
        System.out.println("DURATION: " + duration)
        System.out.println("DAY CONTEXT: " + dayContext)
        System.out.println("Name: " + name)
        System.out.println("------------------------------------")

        if (startTime != null) {
            ev.setTime(startTime.toText(), Constants.START_TIME)
        }

        if (date != null) {
            ev.setDate(date.toText())
        }

        if (duration != null) {
            ev.setDuration(duration.toText())
        }


        if (endTime != null) {
            ev.setTime(endTime.toText(), Constants.END_TIME)
        }


        if (name != null) {
            ev.setName(name.toText())
        }


        var nextInfo = ev.nextUnfilled()
        while (nextInfo != Constants.DONE) {
            System.out.println(ev.toString())
            if (nextInfo == Constants.DATE) {
                var date = furhat.askFor<Date>(random("" +
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
        ev.createID()
        calendar.TakeAction(ev)
        furhat.say("Your event has been added to your calendar.")
        goto(Restart)
    }

    onResponse<Remove>{
        ev.intent = Constants.REMOVE_INTENT
        val startTime = it.intent.startTime
        val date = it.intent.date
        val name = it.intent.name
        val dayContext = it.intent.dayContext

        System.out.println("INTENT: " + Constants.REMOVE_INTENT)
        System.out.println("DATE: " + date)
        System.out.println("START TIME: " + startTime)
        System.out.println("DAY CONTEXT: " + dayContext)
        System.out.println("Name: " + name)
        System.out.println("------------------------------------")

        if (name != null) {
            //Fetch id from stored mapping (HashMap<Name, ID>)
            var id = "" //TODO: TEMPORARY VARIABLE
            ev.id = id
        } else {
            if (date != null) {
                ev.setDate(date.toText())
            } else {
                var date = furhat.askFor<Date>(random("" +
                        "Sure, which date does this event concern?", "Okay. Which date?"
                ))
                ev.setDate(date?.toText())
            }

            if (startTime != null) {
                ev.setTime(startTime.toText(), Constants.START_TIME)
            } else {
                var startTime = furhat.askFor<Time>(random(
                        "Sure, when will the event begin?", "Okay, when does the event start?"
                ))
                ev.setTime(startTime?.toText(), Constants.START_TIME)
            }
            ev.createID()
        }

        var RemoveSuccess = Tools.SendToRemoveAPI(ev.id)
        if (RemoveSuccess) {
            furhat.say { "Your event has been removed from the calendar." }
        } else {
            furhat.say {"Your event as described as not been found"}
        }
        goto(Restart)
    }

    onResponse<ListEv> {
        ev.intent = Constants.LIST_INTENT
        System.out.println("INTENT: " + Constants.LIST_INTENT)

        val startTime = it.intent.startTime
        val startDate = it.intent.startDate
        val endDate = it.intent.endDate
        val dayContext = it.intent.dayContext
        val bookStatement = it.intent.bookStatement

        System.out.println("START DATE: " + startDate)
        System.out.println("END DATE: " + endDate)
        System.out.println("START TIME: " + startTime)
        System.out.println("DAY CONTEXT: " + dayContext)
        System.out.println("BOOK STATEMENT: " + bookStatement)
        System.out.println("bookStatement ends with s?: " + Tools.FormType(bookStatement.toString()))
        System.out.println("------------------------------------")

        goto(Restart)
    }
}

val Restart = state(Interaction) {
    onEntry {
        goto(Start)
    }
}