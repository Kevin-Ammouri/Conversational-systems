package furhatos.app.calendarbot.flow

import furhatos.app.calendarbot.*
import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.app.calendarbot.nlu.*

var calendar = GoogleCalendar()

val Start : State = state(Interaction) {

    onEntry {
        furhat.ask("", timeout=60000)
        //It may be better to just wait for a statement instead of asking nothing and getting a response
    }

    onResponse<Add>{
        var ev = EventObject()
        ev.intent = Constants.ADD_INTENT
        //We must be able to send it to other states, worst case this doesn't have to be implemented
        val startTime = it.intent.startTime
        val date = it.intent.date
        val duration = it.intent.duration
        val endTime = it.intent.endTime
        val name = it.intent.name
        val dayContext = it.intent.dayContext //Once 2, 3.. weeks/months have been implemented, this will be used.
        val bookStatement = it.intent.bookStatement

        ev.bookStatement = Tools.removePlural(bookStatement.toString())

        //startTime can be afternoon, we need to redefine how we handle startTime.
        if (startTime != null) { ev.setTime(startTime.toText(), Constants.START_TIME) }

        if (date != null) {
            var specific_date = ev.setDate(date.toText())
            if (!specific_date && dayContext != null) {
                val new_date = date.toString() + "" + dayContext.toString()
                ev.setDate(new_date)
            }
        }

        if (duration != null) { ev.setDuration(duration.toText()) }

        if (endTime != null) { ev.setTime(endTime.toText(), Constants.END_TIME) }

        if (name != null) { ev.setName(name.toText()) }

        var nextInfo = ev.nextUnfilled()
        while (nextInfo != Constants.DONE) {
            if (nextInfo == Constants.DATE) {
                var date = furhat.askFor<Date>(random("" +
                        "Sure, which date does this event concern?", "Okay. Which date?"
                ))
                ev.setDate(date?.toText())
            } else if (nextInfo == Constants.DURATION) {
                var duration = furhat.askFor<Duration>(random(
                        "Sure, how long will the event last?", "Okay, how long will the event go on for?"
                ))
                ev.setDuration(duration?.toText())
            } else if (nextInfo == Constants.START_TIME) {
                var startTime = furhat.askFor<Time>(random(
                        "Sure, when will the event begin?", "Okay, when does the event start?"
                ))
                ev.setTime(startTime?.toText(), Constants.START_TIME)
            } else if (nextInfo == Constants.NAME) {
                var name = furhat.askFor<PersonName>(random(
                        "Sure, what will you name the event?", "Okay, what is the name of the event?"
                ))
                System.out.println("CAUGHT NAME: " + name.toString())
                ev.setName(name?.toText())
            }
            System.out.println(ev.toString())
            nextInfo = ev.nextUnfilled()
        }
        ev.createID()
        //Tools.mapNameToID(ev)
        /*
        var success = Tools.GoogleAPICall(ev)
        if (success) {
            furhat.say("Your event has been added to your calendar.")
        } else {
            // TODO: Handle it accordingly, by suggesting another day (if full, proactive) etc.
        }
        */
        goto(Restart)
    }

    onResponse<Remove>{
        var ev = EventObject()
        ev.intent = Constants.REMOVE_INTENT
        val startTime = it.intent.startTime
        val date = it.intent.date
        val name = it.intent.name
        val dayContext = it.intent.dayContext //Once 2, 3.. weeks/months have been implemented, this will be used.

        System.out.println("INTENT: " + Constants.REMOVE_INTENT)
        System.out.println("DATE: " + date)
        System.out.println("START TIME: " + startTime)
        System.out.println("DAY CONTEXT: " + dayContext)
        System.out.println("Name: " + name)
        System.out.println("------------------------------------")


        if (name != null) { // This can only be valid once name is fixed, as well as the methods in Tools
            //Fetch id from stored mapping (HashMap<Name, ID>)
            var id = Tools.getIdFromName(name.toString())
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

        /*
        var success = Tools.SendToRemoveAPI(ev.id)
        if (success) {
            furhat.say { "Your event has been removed from the calendar." }
        } else {
            furhat.say {"Your event as described has not been found"}
        }
        */
        goto(Restart)
    }

    onResponse<ListEv> {
        var ev = EventObject()
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
        System.out.println("bookStatement ends with s?: " + Tools.formType(bookStatement.toString()))
        System.out.println("------------------------------------")

        if (startDate == null) {
            furhat.say("Specify a day or date")
            goto(Restart)
        }

        if (dayContext == null) {
            ev.setDate(startDate.toString())
        } else {
            var new_date = startDate.toString() + " " + dayContext.toString()
            ev.setDate(new_date)
        }

        var listedEvents = false

        // TODO: Handle multiple events on multiple days (add a for loop from start to end date)
        //  and surround the code below for multiple events within a day. This is done through
        //  setting date to whatever the day was 

        // Handles multiple events within a day
        if (Tools.formType(bookStatement.toString())) {
            ev.intent = Constants.LIST_INTENT
            if (endDate == null) {
                if (startTime == null) {
                    System.out.println(ev.date)
                    var events = calendar.ListEvents(ev)
                    System.out.println(events)
                    if (events == null || events.size <= 0) {
                        furhat.say("You do not have anything coming up " + ev.day)
                        goto(Restart)
                    }
                    furhat.say(ev.day + " you have")

                    for(event in events) {
                        furhat.say(
                            Tools.interOptions(event.get(Constants.BOOK_STATEMENT), Constants.PRONOUNCE) + " " +
                                    event.get(Constants.BOOK_STATEMENT)?.toLowerCase() + " at " +
                                    Constants.FROM24HOUR.get(event.get(Constants.START_TIME)) + " called " + event.get(Constants.NAME)
                        )
                    }
                    listedEvents = true
                } else if (Tools.interOptions(startTime.toString(), Constants.TIMEORCONTEXT) == Constants.YES) {
                    ev.setTimeContext(startTime.toString())
                    var time_bounds = Constants.TimeOfDay.get(ev.timeContext)
                    ev.startTime = time_bounds?.get(0)
                    ev.endTime = time_bounds?.get(1)
                    System.out.println("--- Before calling ListEvents (API) ---")
                    System.out.println("time bounds: " + time_bounds.toString())
                    System.out.println("ev.date: " + ev.date)
                    System.out.println("ev.startTime: " + ev.startTime)
                    System.out.println("ev.endTime: " + ev.endTime)
                    var events = calendar.ListEvents(ev)
                    if (events == null || events.size <= 0) {
                        furhat.say("You do not have anything coming up " + ev.day + " in the " + ev.timeContext)
                        goto(Restart)
                    }
                    furhat.say(ev.day + " in the " + ev.timeContext + " you have")
                    for(event in events) {
                        furhat.say(
                                Tools.interOptions(event.get(Constants.BOOK_STATEMENT), Constants.PRONOUNCE) + " " +
                                        event.get(Constants.BOOK_STATEMENT)?.toLowerCase() + " at " +
                                        Constants.FROM24HOUR.get(event.get(Constants.START_TIME)) + " called " + event.get(Constants.NAME)
                        )
                    }
                    listedEvents = true
                }
            }
        }

        if (!listedEvents) {
            // Handles a single event
            ev.intent = Constants.GET_INTENT
            if (startDate != null) {
                ev.setDate(startDate.toText())
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

            // furhat.say procedure..
        }



        goto(Restart)
    }
}

val Restart = state(Interaction) {
    onEntry {
        goto(Start)
    }
}