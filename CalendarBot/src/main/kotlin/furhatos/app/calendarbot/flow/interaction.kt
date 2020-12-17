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

        val startTime = it.intent.startTime
        val date = it.intent.date
        val duration = it.intent.duration
        val endTime = it.intent.endTime
        val name = it.intent.name
        val dayContext = it.intent.dayContext
        val bookStatement = it.intent.bookStatement

        ev.bookStatement = Tools.interOptions(bookStatement.toString(), Constants.REMOVE_PLURAL)

        if (dayContext != null) {
            if (date == null) {
                var new_date = dayContext.toString()
                ev.setDate(new_date, true)
            } else {
                var new_date = date.toString() + " " + dayContext.toString()
                ev.setDate(new_date, true)
            }
        } else if (date != null) {
            ev.setDate(date.toText(), true)
        } else if (startTime != null) {
            ev.setDate("today", true)
        }

        if (duration != null) { ev.setDuration(duration.toText()) }

        if (endTime != null) { ev.setTime(endTime.toText(), Constants.END_TIME) }

        if (name != null) { ev.setName(name.toText()) }

        if (startTime != null) {
            var timeContext = ev.setTime(startTime.toText(), Constants.START_TIME)
            if (timeContext) {
                /** Be proactive, we may need to jump between states with goto (much more efficient) */
            }
        }

        var nextInfo = ev.nextUnfilled()
        while (nextInfo != Constants.DONE) {
            if (nextInfo == Constants.DATE) {
                var date = furhat.askFor<Date>(random("" +
                        "Sure, which date does this event concern?", "Okay. Which date?"
                ))
                ev.setDate(date?.toText(), true)
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
                var name = furhat.askFor<Name>(random(
                        "Sure, what will you name the event?", "Okay, what is the name of the event?"
                ))
                System.out.println("CAUGHT NAME: " + name.toString())
                ev.setName(name?.toText())
            }
            System.out.println(ev.toString())
            nextInfo = ev.nextUnfilled()
        }
        ev.createID()

        var success = calendar.insertEvent(ev)
        if (success) {
            Tools.mapNameToID(ev)
            furhat.say("Your event has been added to your calendar.")
        } else {
            /** Handle it accordingly, by suggesting another day (if full, proactive) etc. */
        }
        goto(Restart)
    }

    onResponse<Remove>{
        var ev = EventObject()
        ev.intent = Constants.REMOVE_INTENT
        val startTime = it.intent.startTime
        val date = it.intent.date
        val name = it.intent.name
        val dayContext = it.intent.dayContext
        val bookStatement = it.intent.bookStatement

        System.out.println("INTENT: " + Constants.REMOVE_INTENT)
        System.out.println("DATE: " + date)
        System.out.println("START TIME: " + startTime)
        System.out.println("DAY CONTEXT: " + dayContext)
        System.out.println("Name: " + name)
        System.out.println("------------------------------------")

        // TODO: fix almost the same intro as list and add

        if (name != null) { /** This can only be valid once name is fixed, as well as the methods in Tools */
            var id = Tools.getIdFromName(name.toString())
            ev.id = id
        } else {
            if (date != null) {
                ev.setDate(date.toText(), true)
            } else {
                var date = furhat.askFor<Date>(random("" +
                        "Sure, which date does this event concern?", "Okay, on which date?"
                ))
                ev.setDate(date?.toText(), true)
            }

            /** goto(ListEv) ->
             *          if only one event --> List it and ask to be removed
             *          if multiple --> Ask if you meant one (if so which one) or all of them
             *          if the above is implemented the code below can be removed
             */

            if (startTime != null) { /** Handle that startTime can be equal to TIMES_OF_THE_DAY*/
                ev.setTime(startTime.toText(), Constants.START_TIME)
            } else {
                var startTime = furhat.askFor<Time>(random(
                        "Sure, when will the event begin?", "Okay, when does the event start?"
                ))
                ev.setTime(startTime?.toText(), Constants.START_TIME)
            }
            ev.createID()
        }


        var success = calendar.removeEvent(ev)
        if (success) {
            furhat.say("Your " + Tools.interOptions(bookStatement.toString().toLowerCase(), Constants.REMOVE_PLURAL) +
                    " " + ev.day + " at " + Constants.FROM24HOUR.get(ev.startTime) + " has been removed from the calendar.")
        } else {
            /** Be Proactive, "Did you mean {closest event} <- this event?" */
        }

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

        // TODO: Example "List all meetings in two weeks"
        if (startDate == null && startTime == null) {
            furhat.say("Specify a day or date")
            goto(Restart)
        }

        /** Code snippet below: Assuming startTime != null because of the above*/
        if (dayContext != null) {
            if (startDate == null) {
                var new_date = dayContext.toString()
                ev.setDate(new_date, true)
            } else {
                var new_date = startDate.toString() + " " + dayContext.toString()
                ev.setDate(new_date, true)
            }
        } else if (startDate != null) {
            ev.setDate(startDate.toText(), true)
        } else {
            ev.setDate("today", true)
        }

        // For multiple meetings in multiple days
        if (endDate != null) {
            ev.setDate(endDate.toString(), false)
        }

        var listedEvents = false

        if (Tools.formType(bookStatement.toString())) {
            ev.intent = Constants.LIST_INTENT
            if (endDate == null) {
                if (startTime == null) {
                    var events = calendar.listEvents(ev)
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
                } else if (Tools.interOptions(startTime.toString(), Constants.TIME_OR_CONTEXT) == Constants.YES) {
                    ev.setTimeContext(startTime.toString())
                    var time_bounds = Constants.TimeOfDay.get(ev.timeContext)
                    ev.startTime = time_bounds?.get(0)
                    ev.endTime = time_bounds?.get(1)
                    var events = calendar.listEvents(ev)
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
            } else {}
        }

        if (!listedEvents) {
            // Handles a single event
            ev.intent = Constants.GET_INTENT
            /** Redundant I think
            if (ev.date == null) {
                var date = furhat.askFor<Date>(random(
                        "Sure, which date does this event concern?", "Okay. Which date?"
                ))
                ev.setDate(date?.toText(), true)
            }
            */

            if (startTime != null) {
                ev.setTime(startTime.toText(), Constants.START_TIME)
            } else {
                var startTime = furhat.askFor<Time>(random(
                        "Sure, at what time?"
                ))
                ev.setTime(startTime?.toText(), Constants.START_TIME)
            }
            ev.createID()
            var event = calendar.getEvent(ev)
            if (event == null || event.size <= 0) {
                furhat.say("There is no event " + ev.day + " " + startTime?.toText())
                goto(Restart)
            }
            var event_info = event.get(0)
            furhat.say("The event " + ev.day + " " + startTime?.toText() + " is called " +
                    event_info.get(Constants.NAME) + " and the " + event_info.get(Constants.BOOK_STATEMENT) +
                    " will last for " + Constants.TO24HOUR.get(event_info.get(Constants.DURATION)))
        }



        goto(Restart)
    }
}

val Restart = state(Interaction) {
    onEntry {
        goto(Start)
    }
}