package org.vors.pairbot.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.vors.pairbot.constant.BotConstants
import org.vors.pairbot.model.UserInfo
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*


@Component
class TimeService(
        @Value("\${event.decline.interval.min.seconds:86400}")
        var minSecondsBetweenDeclined: Int = -1
) {

    fun beginningOfDateMinusDaysFrom(from: Date, minusDays: Int): Date {
        return java.sql.Date.valueOf(from.toInstant().minus(minusDays.toLong(), ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toLocalDate())
    }

    fun availableDateTreshold(date: Date): Date {
        return beginningOfDateMinusDaysFrom(date, BotConstants.MIN_DAYS_BETWEEN_SESSIONS)
    }

    fun datePlusSeconds(date: Date, amount: Int): Date {
        return datePlus(date, amount, ChronoUnit.SECONDS)
    }

    fun datePlusHours(date: Date, hours: Int): Date {
        return datePlus(date, hours, ChronoUnit.HOURS)
    }

    fun datePlusDays(date: Date, days: Int): Date {
        return datePlus(date, days, ChronoUnit.DAYS)
    }

    fun dateMinusSeconds(date: Date, amount: Int): Date {
        return datePlus(date, -amount, ChronoUnit.SECONDS)
    }

    fun dateMinusDays(date: Date, days: Int): Date {
        return datePlus(date, -days, ChronoUnit.DAYS)
    }

    private fun datePlus(date: Date, amount: Int, timeUnit: ChronoUnit): Date {
        return Date.from(date.toInstant().plus(amount.toLong(), timeUnit))
    }

    fun chooseSessionDate(): Date {
        var targetDate = LocalDate.now()

        while (targetDate.dayOfWeek == DayOfWeek.SATURDAY || targetDate.dayOfWeek == DayOfWeek.SUNDAY) {
            targetDate = targetDate.plusDays(1)
        }
        val ldt = targetDate.atTime(16, 0)

        var sessionDate = dateForUserZone(ldt)

        if (sessionDate.before(Date())) {
            ldt.plus(1, ChronoUnit.DAYS)
            sessionDate = dateForUserZone(ldt)
        }

        return sessionDate
    }

    fun nowPlusDuration(duration: Duration): Date {
        val now = Date()

        return Date.from(now.toInstant().plus(duration))
    }

    private fun dateForUserZone(ldt: LocalDateTime): Date {
        val zdt = ldt.atZone(ZoneId.of("GMT+3"))
        return Date.from(zdt.toInstant())
    }

    private fun dateForUserZone(ldt: LocalDateTime, zone: ZoneId): ZonedDateTime {
        return ldt.atZone(zone)
    }

    fun lastDeclineThreshold(): Date {
        return dateMinusSeconds(Date(), minSecondsBetweenDeclined)
    }

    fun nextDateToCreateEvent(user: UserInfo): Date {
        return datePlusSeconds(user.lastDeclineDate!!, minSecondsBetweenDeclined)
    }


}
