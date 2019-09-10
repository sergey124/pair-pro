package org.vors.pairbot.service;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;


@Component
public class TimeService {
    public Date beginningOfDateMinusDaysFrom(Date from, int minusDays) {
        return java.sql.Date.valueOf(from.toInstant().minus(minusDays, ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toLocalDate());
    }

    public Date datePlusHours(Date date, int hours) {
        return Date.from(date.toInstant().plus(hours, ChronoUnit.HOURS));
    }

    public Date datePlusDays(Date date, int days) {
        return Date.from(date.toInstant().plus(days, ChronoUnit.DAYS));
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

    private Date dateForUserZone(LocalDateTime ldt) {
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("GMT+3"));
        return Date.from(zdt.toInstant());
    }


}
