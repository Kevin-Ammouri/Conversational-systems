package furhatos.app.calendarbot.nlu

import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.common.Date
import furhatos.util.Language
import furhatos.nlu.common.Number
import furhatos.nlu.common.Time

class Add(var date : Date? = null,
          var startTime : Time? = null,
          var endTime : Time? = null,
          var duration : Duration? = null,
          var dayContext : DayContext? = null,
          var name : Name? = null,
          var addStatement: AddStatement? = null,
          var bookStatement : BookingStatement? = null): Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
                "@addStatement @bookStatement",
                "@addStatement @bookStatement @startTime @endTime")
    }
}

class Remove(var removeStatement: RemoveStatement? = null,
             var bookStatement: BookingStatement? = null,
             var date : Date? = null,
             var startTime : Time? = null,
             var dayContext : DayContext? = null,
             var name : Name? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@removeStatement @bookStatement")
    }
}

class ListEv(var listStatement: ListStatement? = null,
             var bookStatement: BookingStatement? = null,
             var startDate : Date? = null,
             var endDate : Date? = null,
             var startTime : Time? = null,
             var dayContext : DayContext? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
                "@listStatement @bookStatement",
                "@listStatement @bookStatement @startDate @endDate")
    }
}

class ListStatement : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("List", "Show", "Name", "Specify", "Tell", "Say")
    }
}

class RemoveStatement : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Remove", "Delete", "Cancel", "Erase")
    }
}

class AddStatement : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Schedule", "Book")
    }
}

class BookingStatement : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Meeting", "Event", "Appointment", "Everything")
    }
}

class Duration (
        var number : Number? = Number(1),
        var time : Times? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@number @time")
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

class Periods : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Day", "Week", "Month", "Year")
    }

}