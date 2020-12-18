package furhatos.app.calendarbot.flow

import furhatos.app.calendarbot.*
import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.calendarbot.nlu.*
import furhatos.nlu.Response

var ev = EventObject()
var calendar = GoogleCalendar()

val Start : State = state(Interaction) {

    onEntry {
        if (ev.intent != null) {

            println("DATE: " + ev.date)
            println("DAY: " + ev.day)
            println("DAY CONTEXT: " + ev.dayContext)

            println("START TIME: " + ev.startTime)
            println("END TIME: " + ev.endTime)
            println("TIME CONTEXT: " + ev.timeContext)
            println("DURATION: " + ev.duration)

            println("BOOK STATEMENT: " + ev.bookStatement)
            println("bookStatement ends with s?: " + Tools.formType(ev.bookStatement.toString()))
            println("------------------------------------")

            var nextInfo = ev.nextUnfilled()
            if (nextInfo == Constants.DATE) {
                furhat.ask(random(
                        "Sure, whicDurah date does this " + ev.bookStatement + " concern?",
                        "Okay. Which date?"
                ))
            } else if (nextInfo == Constants.DURATION) {
                furhat.ask(random(
                        "Sure, how long will the event last?",
                        "Okay, how long will the event go on for?"
                ))
            } else if (nextInfo == Constants.START_TIME) {
                furhat.ask(random(
                        "Sure, when will the event begin?",
                        "Okay, when does the event start?"
                ))
            } else if (nextInfo == Constants.NAME) {
                furhat.ask(random(
                        "Sure, what will you name the event?",
                        "Okay, what is the name of the event?"
                ))
            } else if (nextInfo == Constants.LIST_INTENT) {
                call(ListEvents())
                furhat.ask("Which one do you mean specifically?")
            } else if (nextInfo == Constants.DONE) {
                goto(Final)
            }
        } else {
            furhat.ask("", timeout = 60000)
        }
    }

    onResponse<Add> {
        println("** Entered ADD")
        ev.intent = Constants.ADD_INTENT

        call(SetAddParams(it))

        if (ev.timeContext != null && ev.startTime == null) {
            println("LETS BE PROACTIVE")
            /** Be proactive, we may need to jump between states with goto (much more efficient) */
        }

        goto(Restart)
    }

    onResponse<Remove>{
        println("** Entered REMOVE")
        ev.intent = Constants.REMOVE_INTENT

        call(SetRemoveParams(it))

        goto(Restart)
    }

    onResponse<ListEv> {
        println("** Entered LIST")
        call(SetListParams(it))

        if (Tools.formType(ev.bookStatement)) {
            ev.intent = Constants.LIST_INTENT
        } else {
            ev.intent = Constants.GET_INTENT
        }

        goto(Restart)
    }

    onResponse<Date> {
        println("** Entered Date")
        var date = it.intent
        ev.setDate(date.toString(), true)

        goto(Restart)
    }

    onResponse<Time> {
        println("** Entered Time")
        var time = it.intent
        ev.setTime(time.toString(), Constants.START_TIME)

        goto(Restart)
    }

    onResponse<Duration> {
        println("** Entered Duration")
        var duration = it.intent
        ev.setDuration(duration.toString())

        goto(Restart)
    }

    onResponse<Ordinal> {
        println("** Entered Ordinal")
        var ordinal = it.intent
        print("Ordinal catched: " + ordinal.toString())

        goto(Restart)
    }

    onResponse<Name> {
        println("** Entered Name")
        var name = it.intent
        ev.setName(name.toString())

        goto(Restart)
    }
}


val Final = state {
    onEntry {
        if (ev.intent == Constants.ADD_INTENT) {
            furhat.say("Do you want to " + ev.addStatement + " the " + ev.bookStatement + " with the following information")

            var confirmation = ev.day + " at " + Constants.FROM24HOUR.get(ev.startTime)

            var answer = furhat.askYN(confirmation)

            if (answer!!) {
                var success = calendar.insertEvent(ev)
                if (success) {
                    Tools.mapNameToID(ev)
                    furhat.say("Your event has been added to your calendar.")
                } else {
                    println("LETS BE PROACTIVE")
                    /** Handle it accordingly, by suggesting another day (if full, proactive) etc. */
                }
            }
        } else if (ev.intent == Constants.REMOVE_INTENT) {

            var success = calendar.removeEvent(ev)
            if (success) {
                furhat.say("Your " + Tools.interOptions(ev.bookStatement.toLowerCase(), Constants.REMOVE_PLURAL) +
                        " " + ev.day + " at " + Constants.FROM24HOUR.get(ev.startTime) + " has been removed from the calendar.")
            } else {
                println("LETS BE PROACTIVE")
                /** Be Proactive, "Did you mean {closest event} <- this event?" */
            }

        } else if (ev.intent == Constants.LIST_INTENT) {
            call(ListEvents())
        } else {
            call(GetEvent())
        }

        ev = EventObject()
        goto(Start)
    }
}

fun ListEvents() = state {
    onEntry {
        if (ev.startTime == null) {
            var events = calendar.listEvents(ev)
            if (events == null || events.size <= 0) {
                furhat.say("You do not have anything coming up " + ev.day)
                goto(Restart)
            }
            furhat.say(ev.day + " you have")

            for (event in events) {
                furhat.say(
                        Tools.interOptions(event.get(Constants.BOOK_STATEMENT), Constants.PRONOUNCE) + " " +
                                event.get(Constants.BOOK_STATEMENT)?.toLowerCase() + " at " +
                                Constants.FROM24HOUR.get(event.get(Constants.START_TIME)) + " called " + event.get(Constants.NAME)
                )
            }
        } else if (ev.timeContext != null) {
            var time_bounds = Constants.TimeOfDay.get(ev.timeContext)
            ev.startTime = time_bounds?.get(0)
            ev.endTime = time_bounds?.get(1)
            var events = calendar.listEvents(ev)
            if (events == null || events.size <= 0) {
                furhat.say("You do not have anything coming up " + ev.day + " in the " + ev.timeContext)
                goto(Restart)
            }
            furhat.say(ev.day + " in the " + ev.timeContext + " you have")
            for (event in events) {
                furhat.say(
                        Tools.interOptions(event.get(Constants.BOOK_STATEMENT), Constants.PRONOUNCE) + " " +
                                event.get(Constants.BOOK_STATEMENT)?.toLowerCase() + " at " +
                                Constants.FROM24HOUR.get(event.get(Constants.START_TIME)) + " called " + event.get(Constants.NAME)
                )
            }
        }

        terminate()
    }
}

fun GetEvent() = state {
    onEntry {
        var event = calendar.getEvent(ev)
        if (event == null || event.size <= 0) {
            furhat.say("There is no event " + ev.day + " at " + Constants.FROM24HOUR.get(ev.startTime))
            goto(Restart)
        }
        var event_info = event.get(0)
        furhat.say("The event " + ev.day + " at " + Constants.FROM24HOUR.get(ev.startTime)
                + " is called " + event_info.get(Constants.NAME) + " and the " + event_info.get(Constants.BOOK_STATEMENT) +
                " will last for " + Constants.TO24HOUR.get(event_info.get(Constants.DURATION)))

        terminate()
    }
}

fun SetAddParams(it : Response<out Add>) = state {
    onEntry {

        var startTime = it.intent.startTime
        var date = it.intent.date
        var duration = it.intent.duration
        var endTime = it.intent.endTime
        var name = it.intent.name
        var dayContext = it.intent.dayContext
        var bookStatement = it.intent.bookStatement
        var addStatement = it.intent.addStatement

        if (date.toString().contains(Constants.TODAY)) {
            ev.setDate(Constants.TODAY, true)
        } else if (date != null || dayContext != null) {
            var new_date = date.toString() + " " + dayContext
            ev.setDate(new_date, true)
        }

        if (startTime != null) { ev.setTime(startTime.toString(), Constants.START_TIME) }

        if (endTime != null) { ev.setTime(endTime?.toText(), Constants.END_TIME) }

        if (duration != null) { ev.setDuration(duration?.toText()) }

        ev.bookStatement = Tools.interOptions(bookStatement.toString(), Constants.REMOVE_PLURAL)
        ev.addStatement = Tools.interOptions(addStatement.toString(), Constants.REMOVE_PLURAL)

        if (name != null) { ev.setName(name?.toText()) }
        terminate()
    }
}

fun SetRemoveParams(it : Response<out Remove>) = state {
    onEntry {
        var startTime = it.intent.startTime
        var date = it.intent.date
        var name = it.intent.name
        var dayContext = it.intent.dayContext
        var bookStatement = it.intent.bookStatement
        var removeStatement = it.intent.removeStatement

        if (date.toString().contains(Constants.TODAY)) {
            ev.setDate(Constants.TODAY, true)
        } else if (date != null || dayContext != null) {
            var new_date = date.toString() + " " + dayContext
            ev.setDate(new_date, true)
        }

        if (startTime != null) { ev.setTime(startTime.toString(), Constants.START_TIME) }

        ev.bookStatement = Tools.interOptions(bookStatement.toString(), Constants.REMOVE_PLURAL)
        ev.removeStatement = Tools.interOptions(removeStatement.toString(), Constants.REMOVE_PLURAL)

        if (name != null) { ev.setName(name?.toText()) }
        terminate()
    }
}

fun SetListParams(it : Response<out ListEv>) = state {
    onEntry {
        var startTime = it.intent.startTime
        var date = it.intent.date
        var dayContext = it.intent.dayContext
        var bookStatement = it.intent.bookStatement
        var listStatement = it.intent.listStatement

        if (date.toString().contains(Constants.TODAY)) {
            ev.setDate(Constants.TODAY, true)
        } else if (date != null || dayContext != null) {
            var new_date = date.toString() + " " + dayContext
            ev.setDate(new_date, true)
        }

        if (startTime != null) { ev.setTime(startTime.toString(), Constants.START_TIME) }

        ev.bookStatement = bookStatement.toString()
        ev.listStatement = Tools.interOptions(listStatement.toString(), Constants.REMOVE_PLURAL)
        terminate()
    }
}


val Restart = state(Interaction) {
    onEntry {
        goto(Start)

    }
}