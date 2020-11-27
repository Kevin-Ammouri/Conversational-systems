package furhatos.app.calendarbot

import furhatos.app.calendarbot.flow.*
import furhatos.skills.Skill
import furhatos.flow.kotlin.*

class CalendarbotSkill : Skill() {
    override fun start() {
        Flow().run(Idle)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}
