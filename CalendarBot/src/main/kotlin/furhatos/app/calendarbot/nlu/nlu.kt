package furhatos.app.calendarbot.nlu


import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.common.Date
import furhatos.util.Language
import furhatos.nlu.common.Number
import furhatos.nlu.common.Time


class Add(var dateWrapper : DateWrapper? = null): Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Schedule a meeting on the @${dateWrapper?.date}", "book an event @${dateWrapper?.date}",
        "Schedule a meeting on the @${dateWrapper?.date} at @${dateWrapper?.time}")
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

class DaysOfTheWeek : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        // TODO: Separate weekdays from weekends
    }
}


class DateWrapper (
        var time : Time? = null,
        var date : Date? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@time @date", "@date @time", "@date", "@time")
    }

    override fun toText(): String {
        return generate("$time, $date")
    }
}

class Duration (
        var number : Number? = Number(1),
        var time : Times? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@number @time", "@time", "@number")
    }
}

class Name (var name : String? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("the name is @name", "name the event @name")
    }

}

class DayContext (
        var count : Number? = Number(1),
        var period : Periods? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @period", "next @period", "tomorrow")
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
        // The last 4 seem redundant for this project
    }
}

