package furhatos.app.calendarbot.flow

import furhatos.app.calendarbot.*
import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.calendarbot.nlu.*
import furhatos.nlu.Response
import java.util.HashMap

var ev = EventObject()
var calendar = GoogleCalendar()

val Start : State = state(Interaction) {
    onEntry {
        println("** STATUS = " + ev.status + " **")
        if (ev.intent != null && ev.status == Constants.PENDING) {

            Tools.debugPrint(ev)

            var nextInfo = ev.nextUnfilled()
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
                val events = call(listEvents()) as List<HashMap<String, String>>
                if (events.size > 1)
                    furhat.ask("Which one do you mean specifically?", timeout = 60000)
                else {
                    ev.startTime = events.get(0).get(Constants.START_TIME)
                    reentry()
                }
            } else if (nextInfo == Constants.BE_PROACTIVE) {
                call(beProactive())
                reentry()
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
        println(it.intent.bookStatement)
        if (it.intent.bookStatement == null || Tools.formType(ev.bookStatement) || ev.bookStatement == Constants.EVERYTHING) {
            ev.intent = Constants.LIST_INTENT
        } else {
            ev.intent = Constants.GET_INTENT
        }

        reentry()
    }

    onResponse<InfoIntent> {
        println("** Entered Extra Info State")
        var date = it.intent.date
        var startTime = it.intent.startTime
        var endTime = it.intent.endTime
        var duration = it.intent.duration
        var name = it.intent.name

        if (date != null) { ev.setDate(date.toString()) }

        if (startTime != null) { ev.setTime(startTime.toString(), Constants.START_TIME) }

        if (endTime != null) { ev.setTime(endTime.toString(), Constants.END_TIME) }

        if (duration != null) { ev.setDuration(duration.toString()) }

        if (name != null) { ev.setName(name.toString()) }

        ev.status = Constants.PENDING
        reentry()
    }

    onResponse<No> {
        println("** Entered NO")
    }

    onResponse<Yes> {
        println("** Entered YES")
        ev = EventObject()
        furhat.say("What would you like to do?")
        reentry()
    }

    onResponse<Clear> {
        println("** Entered CLEAR")
        ev = EventObject()
        furhat.say("Okay")
        reentry()
    }

    onResponse<Greeting> {
        println("** Entered GREETING")
        furhat.say(random("Hello, how can I be of assistance today?", "Hi, how can I help you?"))
        reentry()
    }
}


val Final = state {
    onEntry {
        if (ev.intent == Constants.ADD_INTENT) {
            furhat.say("Do you want to " + ev.addStatement + " the " + ev.bookStatement + " with the following information")

            var confirmation = ev.day + " at " + Constants.FROM24HOUR.get(ev.startTime) + " with name " + ev.name

            var answer = furhat.askYN(confirmation, timeout=60000)

            if (answer!!) {
                var success = calendar.insertEvent(ev)
                if (success) {
                    Tools.mapNameToID(ev)
                    furhat.say("Your event has been added to your calendar.")
                } else {
                    ev.status = Constants.FAILED
                    call(beProactive())
                    reentry()
                }
            }
        } else if (ev.intent == Constants.REMOVE_INTENT) {

            var answer = furhat.askYN("Are you sure you want to remove the " +
                    Tools.interOptions(ev.bookStatement.toLowerCase(), Constants.REMOVE_PLURAL) + " at " +
                    Constants.FROM24HOUR.get(ev.startTime) + " " + ev.day + "?", timeout=60000)

            if (answer!!) {
                var success = calendar.removeEvent(ev)
                if (success) {
                    furhat.say("Your " + Tools.interOptions(ev.bookStatement.toLowerCase(), Constants.REMOVE_PLURAL) +
                            " " + ev.day + " at " + Constants.FROM24HOUR.get(ev.startTime) + " has been removed from the calendar.")
                } else {
                    ev.status = Constants.FAILED
                    call(beProactive())
                    reentry()
                }
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

        if (ev.intent == Constants.ADD_INTENT && ev.status == Constants.PENDING) {
            var suggestTimes = Tools.findAvailableTime(ev, 7, calendar)
            call(suggestTime(suggestTimes))
            terminate()
        } else if (ev.intent == Constants.ADD_INTENT && ev.status == Constants.FAILED) {
            var suggestTimes = Tools.findAvailableTime(ev, 7, calendar)
            for (at in suggestTimes) {
                var time = ev.startTime
                ev.startTime = null
                ev.endTime = null
                var events = calendar.listEvents(ev)
                for (event in events) {
                    if (time == event.get(Constants.START_TIME)) {
                        furhat.say("Looks like you already have " + Tools.interOptions(ev.bookStatement, Constants.PRONOUNCE) + " "
                                + Tools.interOptions(ev.bookStatement.toLowerCase(), Constants.REMOVE_PLURAL)
                                + " booked " + " at " + Constants.FROM24HOUR.get(time) + " " + ev.day + " called " + event.get(Constants.NAME))
                    }
                }
                ev.timeContext = Tools.getTimeContext(time)
                suggestTimes = Tools.findAvailableTime(ev, 2, calendar)
                call(suggestTime(suggestTimes))
                terminate()
            }
        } else if (ev.intent == Constants.REMOVE_INTENT && ev.status == Constants.FAILED) {
            ev.startTime = null
            ev.endTime = null
            var events = calendar.listEvents(ev)
            if (events == null || events.size <= 0) {
                ev = EventObject()
                goto(Start)
                terminate()
            }
            for (event in events) {
                var answer = furhat.askYN("Did you mean the " + Tools.interOptions(event.get(Constants.BOOK_STATEMENT)?.toLowerCase(), Constants.REMOVE_PLURAL)
                        + " starting at " + Constants.FROM24HOUR.get(event.get(Constants.START_TIME)) + " and ending at " +
                        Constants.FROM24HOUR.get(event.get(Constants.END_TIME)) + " with name " + event.get(Constants.NAME), timeout=60000)

                if (answer!!) {
                    ev.startTime = event.get(Constants.START_TIME)
                    ev.date = event.get(Constants.DATE)
                    terminate()
                }
            }
        }
        terminate()
    }
}

fun suggestTime(suggestTimes : ArrayList<AvailableTimes>?) = state {
    onEntry {
        var firstTime = true
        for (at in suggestTimes!!) {
            if (at.startTimes.isEmpty()) {
                furhat.say("Looks like your schedule is full " + ev.day + " in the " + ev.timeContext)
            }
            firstTime = true
            for (time in at.startTimes) {
                var answer : Boolean?
                if (firstTime) {
                    answer = furhat.askYN("Does " + Constants.FROM24HOUR.get(time) + " " + at.day + " work for you?", timeout=60000)
                    firstTime = false
                } else {
                    answer = furhat.askYN("How about at " + Constants.FROM24HOUR.get(time) + "?", timeout=60000)
                }

                if (answer!!) {
                    if (ev.date != at.date) {
                        ev.date = at.date
                        ev.day = at.day
                    }
                    ev.setTime(time, Constants.START_TIME)
                    terminate()
                }
            }
        }
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
            if (events.size == 1 && ev.intent == Constants.REMOVE_INTENT)  { terminate(events) }
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
            if (events.size == 1 && ev.intent == Constants.REMOVE_INTENT) { terminate(events) }
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
        } else if ((date != null && date.toString() != "null") && !(dayContext != null && dayContext.toString() != "null")) {
            ev.setDate(date.toString())
        } else if ((date != null && date.toString() != "null") || (dayContext != null && dayContext.toString() != "null")) {
            var new_date = date.toString() + " in " + dayContext
            ev.setDate(new_date)
        }

        if (startTime != null) { ev.setTime(startTime.toString(), Constants.START_TIME) }

        if (endTime != null) { ev.setTime(endTime?.toText(), Constants.END_TIME) }

        if (duration != null) { ev.setDuration(duration?.toText()) }

        if (bookStatement == null) {
            ev.bookStatement = Tools.interOptions(Constants.EVENT, Constants.REMOVE_PLURAL)
        } else {
            ev.bookStatement = Tools.interOptions(bookStatement.toString(), Constants.REMOVE_PLURAL)
        }
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
        } else if ((date != null && date.toString() != "null") && !(dayContext != null && dayContext.toString() != "null")) {
            ev.setDate(date.toString())
        } else if ((date != null && date.toString() != "null") || (dayContext != null && dayContext.toString() != "null")) {
            var new_date = date.toString() + " in " + dayContext
            ev.setDate(new_date)
        }

        if (startTime != null) { ev.setTime(startTime.toString(), Constants.START_TIME) }

        if (bookStatement == null) {
            ev.bookStatement = Tools.interOptions(Constants.EVENT, Constants.REMOVE_PLURAL)
        } else {
            ev.bookStatement = Tools.interOptions(bookStatement.toString(), Constants.REMOVE_PLURAL)
        }
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
        } else if ((date != null && date.toString() != "null") && !(dayContext != null && dayContext.toString() != "null")) {
            ev.setDate(date.toString())
        } else if ((date != null && date.toString() != "null") || (dayContext != null && dayContext.toString() != "null")) {
            var new_date = date.toString() + " in " + dayContext
            ev.setDate(new_date)
        }

        if (startTime != null) { ev.setTime(startTime.toString(), Constants.START_TIME) }

        if (bookStatement == null) {
            ev.bookStatement = Constants.EVENT + "s"
        } else {
            ev.bookStatement = bookStatement.toString()
        }
        ev.listStatement = Tools.interOptions(listStatement.toString(), Constants.REMOVE_PLURAL)
        terminate()
    }
}