package furhatos.app.calendarbot.nlu


import furhatos.app.calendarbot.Event
import furhatos.app.calendarbot.nlu.*
import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.util.Language
import furhatos.nlu.common.Number


//class BuyFruit(var fruits : FruitList? = null) : Intent()
class Add : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("")
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

class Periods : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Day", "Week", "Month", "Year")
    }
}

class DayContext (
        var count : Number? = Number(1),
        var period : Periods? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @period", "next @period", "tomorrow")
    }
}

class TimeOfTheDay : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Morning", "Afternoon", "Evening", "Midday", "Noon" , "Dawn", "Dusk")
        // The last 4 seem redundant for this project
    }
}

