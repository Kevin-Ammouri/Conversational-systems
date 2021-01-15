package furhatos.app.calendarbot.nlu

import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.common.Date
import furhatos.util.Language
import furhatos.nlu.common.Number
import furhatos.nlu.common.PersonName
import furhatos.nlu.common.Time

class Add(var date : Date? = null,
          var startTime : Time? = null,
          var endTime : Time? = null,
          var duration : Duration? = null,
          var name : PersonName? = null,
          var dayContext : DayContext? = null,
          var addStatement: AddStatement? = null,
          var bookStatement : BookingStatement? = null): Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
                "@addStatement",
                "@addStatement @bookStatement",
                "@addStatement @bookStatement @date",
                "@addStatement @bookStatement @duration",
                "@addStatement @bookStatement @startTime @endTime")
    }
}

class Remove(var removeStatement: RemoveStatement? = null,
             var bookStatement: BookingStatement? = null,
             var date : Date? = null,
             var startTime : Time? = null,
             var name : PersonName? = null,
             var dayContext : DayContext? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
                "@removeStatement",
                "@removeStatement @bookStatement",
                "@removeStatement @bookStatement @date",
                "@removeStatement @bookStatement @startTime",
                "@removeStatement @bookStatement @name",
                "@removeStatement @bookStatement from my schedule")
    }
}

class ListEv(var listStatement: ListStatement? = null,
             var bookStatement: BookingStatement? = null,
             var date : Date? = null,
             var startTime : Time? = null,
             var dayContext : DayContext? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
                "@listStatement",
                "@listStatement @bookStatement",
                "@listStatement @bookStatement @date",
                "@listStatement @bookStatement @startTime",
                "How does my schedule look on @date ?")
    }
}

class InfoIntent(
        var date : Date? = null,
        var startTime : Time? = null,
        var duration : Duration? = null,
        var name : PersonName? = null,
        var endTime : Time? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@date", "@duration", "@name", "@startTime @endTime", "it is scheduled at @startTime",
                "until @endTime", "it will last @endTime", "I meant @startTime @endTime")
    }
}

class Clear : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Clear everything", "scratch what i said", "never mind", "forget what i said", "start over")
    }
}

class ListStatement : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("List", "Show", "Specify", "Tell", "Is there", "Coming Up", "Do I")
    }
}

class RemoveStatement : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Remove", "Delete", "Cancel", "Erase")
    }
}

class AddStatement : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Schedule", "Book", "Put", "Add", "Insert")
    }
}

class BookingStatement : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Meeting", "Event", "Appointment", "Deadline")
    }
}

class Duration (
        var number : Number? = Number(1),
        var time : Times? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@number @time", "@number and a half @time")
    }

    override fun toText(): String {
        return generate("$number $time")
    }
}

class Times : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Second", "Seconds", "Minute", "Minutes", "Hour", "Hours", "-hour")
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

class Periods : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Day", "Week", "Month", "Year")
    }

}