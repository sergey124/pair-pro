package org.vors.pairbot.service;

import org.springframework.stereotype.Component;
import org.vors.pairbot.model.UserInfo;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;


@Component
public class TimeService {
    static final int MIN_DAYS_BETWEEN_DECLINED = 1;

    public Date beginningOfDateMinusDaysFrom(Date from, int minusDays) {
        return java.sql.Date.valueOf(from.toInstant().minus(minusDays, ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toLocalDate());
    }

    public Date datePlusHours(Date date, int hours) {
        return datePlus(date, hours, ChronoUnit.HOURS);
    }

    public Date datePlusDays(Date date, int days) {
        return datePlus(date, days, ChronoUnit.DAYS);
    }

    public Date dateMinusDays(Date date, int days) {
        return datePlus(date, -days, ChronoUnit.DAYS);
    }

    private Date datePlus(Date date, int amount, ChronoUnit timeUnit) {
        return Date.from(date.toInstant().plus(amount, timeUnit));
    }

    public Date chooseSessionDate() {
        LocalDateTime ldt = LocalDate.now().atTime(16, 0);

        ldt = ldt.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY));

        Date sessionDate = dateForUserZone(ldt);

//        Date sessionDate = Timestamp.valueOf(ldt);

        if (sessionDate.before(new Date())) {
            ldt.plus(1, ChronoUnit.WEEKS);
            sessionDate = dateForUserZone(ldt);
        }

        return sessionDate;
    }

    public Date nowPlusDuration(Duration duration) {
        Date now = new Date();

        return Date.from(now.toInstant().plus(duration));
    }

    private Date dateForUserZone(LocalDateTime ldt) {
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("GMT+3"));
        return Date.from(zdt.toInstant());
    }


    public Date lastDeclineThreshold() {
        return dateMinusDays(new Date(), MIN_DAYS_BETWEEN_DECLINED);
    }

    public Date nextDateToCreateEvent(UserInfo user) {
        return datePlusDays(user.getLastDeclineDate(), MIN_DAYS_BETWEEN_DECLINED);
    }


}
