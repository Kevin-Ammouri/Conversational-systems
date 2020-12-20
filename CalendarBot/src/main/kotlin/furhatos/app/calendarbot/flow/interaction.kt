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
        println("********************** ev.intent: " + ev.intent + " *** STATUS: " + ev.status)
        if (ev.intent != null && ev.status == Constants.PENDING) {

            println("INTENT: " + ev.intent)

            println("DATE: " + ev.date)
            println("DAY: " + ev.day)
            println("DAY CONTEXT: " + ev.dayContext)

            println("START TIME: " + ev.startTime)
            println("END TIME: " + ev.endTime)
            println("TIME CONTEXT: " + ev.timeContext)
            println("DURATION: " + ev.duration)

            println("NAME: " + ev.name)
            println("BOOK STATEMENT: " + ev.bookStatement)
            println("bookStatement ends with s?: " + Tools.formType(ev.bookStatement.toString()))

            var nextInfo = ev.nextUnfilled()
            println("NEXT INFO REQUIRED: " + nextInfo)
            println("------------------------------------")
            if (nextInfo == Constants.DATE) {
                furhat.ask(random(
                        "Sure, which date does this " + ev.bookStatement + " concern?",
                        "Okay. Which date?"
                ), timeout = 60000)
            } else if (nextInfo == Constants.DURATION) {
                furhat.ask(random(
                        "Sure, how long will the " + ev.bookStatement + " last?",
                        "Okay, how long will the " + ev.bookStatement + " go on for?"
                ), timeout = 60000)
            } else if (nextInfo == Constants.START_TIME) {
                furhat.ask(random(
                        "Sure, when will the " + ev.bookStatement + " begin?",
                        "Okay, when does the " + ev.bookStatement + " start?"
                ), timeout = 60000)
            } else if (nextInfo == Constants.NAME) {
                furhat.ask(random(
                        "Sure, what will you name the " + ev.bookStatement + "?",
                        "Okay, what do you want to name the " + ev.bookStatement + "?"
                ), timeout = 60000)
            } else if (nextInfo == Constants.LIST_INTENT) {
                call(listEvents())
                furhat.ask("Which one do you mean specifically?", timeout = 60000)
            } else if (nextInfo == Constants.BE_PROACTIVE) {
                call(beProactive())
            } else if (nextInfo == Constants.DONE) {
                goto(Final)
            }
        } else if (ev.status == Constants.SUCCESS) {
            furhat.ask("Anything else?", timeout = 60000)
        } else {
            furhat.listen(timeout = 60000)
        }
    }


    onResponse<Add> {
        println("** Entered ADD")
        ev.intent = Constants.ADD_INTENT
        ev.status = Constants.PENDING

        println("FURHAT CAUGHT DATE: " + it.intent.date.toString())
        println("FURHAT CAUGHT startTime: " + it.intent.startTime.toString())
        println("FURHAT CAUGHT endTime: " + it.intent.endTime.toString())
        println("FURHAT CAUGHT duration: " + it.intent.duration.toString())
        println("FURHAT CAUGHT daycontext: " + it.intent.dayContext.toString())
        println("FURHAT CAUGHT name: " + it.intent.name.toString())

        call(setAddParams(it))

        reentry()
    }

    onResponse<Remove>{
        println("** Entered REMOVE")
        ev.intent = Constants.REMOVE_INTENT
        ev.status = Constants.PENDING

        call(setRemoveParams(it))

        reentry()
    }

    onResponse<ListEv> {
        println("** Entered LIST")
        call(setListParams(it))
        ev.status = Constants.PENDING
        if (Tools.formType(ev.bookStatement) || ev.bookStatement.equals(Constants.EVERYTHING)) {
            ev.intent = Constants.LIST_INTENT
        } else {
            ev.intent = Constants.GET_INTENT
        }

        reentry()
    }

    onResponse<InfoIntent> {
        println("** Entered Extra Info State")
        var date = it.intent.date
        var time = it.intent.time
        var duration = it.intent.duration
        var name = it.intent.name

        println("FURHAT CAUGHT DATE: " + it.intent.date.toString())
        println("FURHAT CAUGHT time: " + it.intent.time.toString())
        println("FURHAT CAUGHT duration: " + it.intent.duration.toString())
        println("FURHAT CAUGHT name: " + it.intent.name.toString())

        if (date != null) { ev.setDate(date.toString()) }

        if (time != null) { ev.setTime(time.toString(), Constants.START_TIME) }

        if (duration != null) { ev.setDuration(duration.toString()) }

        if (name != null) { ev.setName(name.toString()) }

        ev.status = Constants.PENDING
        reentry()
    }

    onResponse<Ordinal> {
        println("** Entered Ordinal")
        var ordinal = it.intent
        ev.status = Constants.PENDING
        println("Ordinal catched: " + ordinal.toString())

        reentry()
    }

    onResponse<No> {}

    onResponse<Yes> {
        ev = EventObject()
        furhat.say("What would you like to do?")
        reentry()
    }

    onResponse<Greeting> {
        furhat.say(random("Hello, how can I be of assistance today?", "Hi, how can I help you?"))
        reentry()
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
                    ev.status = Constants.FAILED
                    call(beProactive())
                    /** Handle it accordingly, by suggesting another day (if full, proactive) etc. */
                }
            }
        } else if (ev.intent == Constants.REMOVE_INTENT) {

            var success = calendar.removeEvent(ev)
            if (success) {
                furhat.say("Your " + Tools.interOptions(ev.bookStatement.toLowerCase(), Constants.REMOVE_PLURAL) +
                        " " + ev.day + " at " + Constants.FROM24HOUR.get(ev.startTime) + " has been removed from the calendar.")
            } else {
                ev.status = Constants.FAILED
                call(beProactive())
                /** Be Proactive, "Did you mean {closest event} <- this event?" */
            }

        } else if (ev.intent == Constants.LIST_INTENT) {
            call(listEvents())
        } else {
            call(getEvent())
        }

        ev.status = Constants.SUCCESS
        goto(Start)
    }
}

fun beProactive() = state {
    onEntry {
        println("******** LETS BE PROACTIVE ***********")
        var suggestTimes = Tools.findAvailableTime(ev, 2, calendar)

        Tools.printList(suggestTimes)

        if (ev.intent == Constants.ADD_INTENT && ev.status == Constants.PENDING) { // Only Time Context and duration given

        } else if (ev.intent == Constants.ADD_INTENT && ev.status == Constants.FAILED) { // Failed ADD

        } else if (ev.intent == Constants.REMOVE_INTENT && ev.status == Constants.FAILED) { //Failed Remove

        }
        terminate()
    }
}

fun listEvents() = state {
    onEntry {
        if (ev.timeContext == null) {
            var events = calendar.listEvents(ev)
            if (events == null || events.size <= 0) {
                furhat.say("You do not have anything coming up " + ev.day)
                ev.status = Constants.SUCCESS
                goto(Start)
            }
            furhat.say(ev.day + " you have")

            for (event in events) {
                furhat.say(
                        Tools.interOptions(event.get(Constants.BOOK_STATEMENT), Constants.PRONOUNCE) + " " +
                                event.get(Constants.BOOK_STATEMENT)?.toLowerCase() + " at " +
                                Constants.FROM24HOUR.get(event.get(Constants.START_TIME)) + " called " + event.get(Constants.NAME)
                )
            }
        } else {
            var time_bounds = Constants.TimeOfDay.get(ev.timeContext)
            ev.startTime = time_bounds?.get(0)
            ev.endTime = time_bounds?.get(1)
            var events = calendar.listEvents(ev)
            if (events == null || events.size <= 0) {
                furhat.say("You do not have anything coming up " + ev.day + " in the " + ev.timeContext)
                ev.status = Constants.SUCCESS
                goto(Start)
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

fun getEvent() = state {
    onEntry {
        var event = calendar.getEvent(ev)
        if (event == null || event.size <= 0) {
            furhat.say("There is no event " + ev.day + " at " + Constants.FROM24HOUR.get(ev.startTime))
            ev.status = Constants.SUCCESS
            goto(Start)
        }
        var event_info = event.get(0)
        furhat.say("The event " + ev.day + " at " + Constants.FROM24HOUR.get(ev.startTime)
                + " is called " + event_info.get(Constants.NAME) + " and the " + event_info.get(Constants.BOOK_STATEMENT) +
                " will last for " + Constants.TO24HOUR.get(event_info.get(Constants.DURATION)))

        terminate()
    }
}

fun setAddParams(it : Response<out Add>) = state {
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
            ev.setDate(Constants.TODAY)
        } else if ((date != null && date.toString() != "null") || (dayContext != null && dayContext.toString() != "null")) {
            var new_date = date.toString() + " " + dayContext
            ev.setDate(new_date)
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

fun setRemoveParams(it : Response<out Remove>) = state {
    onEntry {
        var startTime = it.intent.startTime
        var date = it.intent.date
        var name = it.intent.name
        var dayContext = it.intent.dayContext
        var bookStatement = it.intent.bookStatement
        var removeStatement = it.intent.removeStatement

        if (date.toString().contains(Constants.TODAY)) {
            ev.setDate(Constants.TODAY)
        } else if (date != null || dayContext != null) {
            var new_date = date.toString() + " " + dayContext
            ev.setDate(new_date)
        }

        if (startTime != null) { ev.setTime(startTime.toString(), Constants.START_TIME) }

        ev.bookStatement = Tools.interOptions(bookStatement.toString(), Constants.REMOVE_PLURAL)
        ev.removeStatement = Tools.interOptions(removeStatement.toString(), Constants.REMOVE_PLURAL)

        if (name != null) { ev.setName(name?.toText()) }
        terminate()
    }
}

fun setListParams(it : Response<out ListEv>) = state {
    onEntry {
        var startTime = it.intent.startTime
        var date = it.intent.date
        var dayContext = it.intent.dayContext
        var bookStatement = it.intent.bookStatement
        var listStatement = it.intent.listStatement

        if (date.toString().contains(Constants.TODAY)) {
            ev.setDate(Constants.TODAY)
        } else if (date != null || dayContext != null) {
            var new_date = date.toString() + " " + dayContext
            ev.setDate(new_date)
        }

        if (startTime != null) { ev.setTime(startTime.toString(), Constants.START_TIME) }

        ev.bookStatement = bookStatement.toString()
        ev.listStatement = Tools.interOptions(listStatement.toString(), Constants.REMOVE_PLURAL)
        terminate()
    }
}