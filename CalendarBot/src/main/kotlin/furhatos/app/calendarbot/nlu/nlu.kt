package furhatos.app.calendarbot.nlu

import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.common.Date
import furhatos.util.Language
import furhatos.nlu.common.Number
import furhatos.nlu.common.Time

class Add(var data : DataWrapper? = null): Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
                "Schedule a meeting on the @${data?.date}",
                "Schedule a meeting on the @${data?.date} @${data?.startTime} @${data?.duration}",
                "Schedule a meeting in the @${data?.timeOfDay}")
                //"Schedule a meeting on the @${data?.date} at @${data?.startTime} and it will last @${data?.duration}",
                //"Schedule a meeting in the @${data?.timeOfDay}")
    }
}

class Remove : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("")
    }
}

class ListEv : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("")
    }
}

class Edit : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("")
    }
}

class DataWrapper (
        var startTime : Time? = null,
        var date : Date? = null,
        var day : DaysOfTheWeek? = null,
        var duration : Duration? = null,
        var dayContext : DayContext? = null,
        var endTime : Time? = null,
        var timeOfDay : TimeOfTheDay? = null,
        var name : Name? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@date @startTime", "@date", "@startTime", "@date @startTime @duration",
                "@date @startTime @endTime", "@endTime", "@timeOfDay", "@date @duration")
    }
}

class DaysOfTheWeek : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        // TODO: Separate weekdays from weekends
    }
}

class Duration (
        var number : Number? = Number(1),
        var time : Times? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("last for @number @time", "last @number @time", "@number", "@time", "should last @number @time")
    }

    override fun toText(): String {
        return generate("$number $time")
    }
}

class Name (var name : String? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("the name is @name", "name the event @name", "call the event @name")
    }

    override fun toText(): String {
        return generate("$name")
    }

}

class DayContext (
        var count : Number? = Number(1),
        var period : Periods? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @period", "next @period", "tomorrow")
    }

    override fun toText() : String {
        return generate("$count $period")
    }
}


class Month : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December")
    }
}

class Periods : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Day", "Week", "Month", "Year")
    }
}

class Times : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Second", "Seconds", "Minute", "Minutes", "Hour", "Hours")
    }
}

class TimeOfTheDay : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Morning", "Afternoon", "Evening", "Midday", "Noon" , "Dawn", "Dusk")
    }
}

