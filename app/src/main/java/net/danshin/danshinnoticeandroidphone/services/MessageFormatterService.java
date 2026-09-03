package net.danshin.danshinnoticeandroidphone.services;

import android.content.Context;

import net.danshin.danshinnoticeandroidphone.R;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.Message;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.Age;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.BirthEvent;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.BirthWouldEvent;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.Date;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.DeathEvent;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.Interval;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.Person;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.UpcomingResponse;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class MessageFormatterService {
    private static final String TEMPLATE = "%s\n\n%s";
    private final Context context;

    public MessageFormatterService(Context context) {
        this.context = context;
    }

    public Message getMessage(UpcomingResponse response) {
        String title = context.getString(R.string.gen_dates_upcoming_title);
        String content = buildMessageContent(response);
        if (content == null) {
            return new Message(
                    String.format(TEMPLATE, title, context.getString(R.string.gen_dates_upcoming_no_events)),
                    true
            );
        } else {
            return new Message(String.format(TEMPLATE, title, content), false);
        }
    }

    private @Nullable String buildMessageContent(UpcomingResponse response) {
        StringBuilder content = new StringBuilder();

        if (response.isEmpty()) {
            return null;
        }

        boolean hasPastEvents = hasEvents(response.beforeBirth(), response.beforeBirthWould(), response.beforeDeath());
        if (hasPastEvents) {
            addTitle(content, context.getString(R.string.gen_dates_upcoming_past));
            addEvents(content, response.beforeBirth(), context.getString(R.string.gen_dates_upcoming_birthday), false, false);
            addEvents(content, response.beforeBirthWould(), context.getString(R.string.gen_dates_upcoming_birthday), true, false);
            addEvents(content, response.beforeDeath(), context.getString(R.string.gen_dates_upcoming_memory), false, false);
        }

        boolean hasTodayEvents = hasEvents(response.todayBirth(), response.todayBirthWould(), response.todayDeath());
        if (hasTodayEvents) {
            addTitle(content, context.getString(R.string.gen_dates_upcoming_today));
            addEvents(content, response.todayBirth(), context.getString(R.string.gen_dates_upcoming_birthday), false, false);
            addEvents(content, response.todayBirthWould(), context.getString(R.string.gen_dates_upcoming_birthday), true, false);
            addEvents(content, response.todayDeath(), context.getString(R.string.gen_dates_upcoming_memory), false, false);
        }

        boolean hasFutureEvents = hasEvents(response.afterBirth(), response.afterBirthWould(), response.afterDeath());
        if (hasFutureEvents) {
            addTitle(content, context.getString(R.string.gen_dates_upcoming_future));
            addEvents(content, response.afterBirth(), context.getString(R.string.gen_dates_upcoming_birthday), false, true);
            addEvents(content, response.afterBirthWould(), context.getString(R.string.gen_dates_upcoming_birthday), true, true);
            addEvents(content, response.afterDeath(), context.getString(R.string.gen_dates_upcoming_memory), false, true);
        }

        return content.toString();
    }

    private boolean hasEvents(List<?>... eventLists) {
        for (List<?> list : eventLists) {
            if (list != null && !list.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void addTitle(StringBuilder content, String title) {
        content.append(title).append(":\n");
    }

    private void addEvents(StringBuilder content, List<?> events, String eventTypeText, boolean isWould, boolean isFuture) {
        if (events == null || events.isEmpty()) {
            return;
        }
        for (Object event : events) {
            content.append("  • ").append(formatEvent(event, eventTypeText, isWould, isFuture)).append("\n");
        }
        content.append("\n");
    }

    private String formatEvent(Object event, String eventType, boolean isWould, boolean isFuture) {
        if (event instanceof BirthEvent birth) {
            return formatBirthEvent(birth, eventType, isWould, isFuture);
        } else if (event instanceof BirthWouldEvent birthWould) {
            return formatBirthWouldEvent(birthWould, eventType, isWould);
        } else if (event instanceof DeathEvent death) {
            return formatDeathEvent(death);
        }
        return "";
    }

    private String formatBirthEvent(BirthEvent event, String eventType, boolean isWould, boolean isFuture) {
        Date date = event.date();
        Person person = event.person();
        Age age = event.age();

        String dateStr = formatDate(date);
        String personStr = formatPerson(person);
        String ageStr = formatAge(age, isWould, false, isFuture);

        return String.format("%s %s %s (%s)", dateStr, eventType, personStr, ageStr);
    }

    private String formatBirthWouldEvent(BirthWouldEvent event, String eventType, boolean isWould) {
        Date date = event.date();
        Person person = event.person();
        Age age = event.age();

        String dateStr = formatDate(date);
        String personStr = formatPerson(person);
        String ageStr = formatAge(age, isWould, false, false);

        return String.format("%s %s %s (%s)", dateStr, eventType, personStr, ageStr);
    }

    private String formatDeathEvent(DeathEvent event) {
        Date date = event.date();
        Person person = event.person();
        Age age = event.age();
        Interval interval = event.interval();

        String dateStr = formatDate(date);
        String personStr = formatPerson(person);
        String ageStr = formatAge(age, false, true, false);
        String intervalStr = formatInterval(interval);

        String memoryText = context.getString(R.string.gen_dates_upcoming_memory);
        String agoText = context.getString(R.string.gen_dates_upcoming_ago);
        String ageText = context.getString(R.string.gen_dates_upcoming_age);

        return String.format("%s %s %s (%s %s %s %s)",
                dateStr, memoryText, personStr, intervalStr, agoText, ageText, ageStr);
    }

    private String formatDate(Date date) {
        if (date == null) return context.getString(R.string.gen_dates_upcoming_date_unknown);
        return String.format("%s-%s-%s", date.year(), date.month(), date.day());
    }

    private String formatPerson(Person person) {
        if (person == null) return context.getString(R.string.gen_dates_upcoming_person_unknown);
        return formatPersonFullName(person);
    }

    private String formatPersonFullName(Person person) {
        StringBuilder sb = new StringBuilder();
        if (person.surname() != null) sb.append(person.surname()).append(" ");
        if (person.name() != null) sb.append(person.name()).append(" ");
        if (person.patronymic() != null) sb.append(person.patronymic());
        return sb.toString().trim();
    }

    private String formatAge(Age age, boolean isWould, boolean isDeath, boolean isFuture) {
        if (age == null) return context.getString(R.string.gen_dates_upcoming_age_unknown);

        int years = age.y();
        String yearsStr = formatYears(years);

        if (isDeath) {
            return yearsStr;
        } else if (isWould) {
            return String.format(context.getString(R.string.gen_dates_upcoming_age_would), years);
        } else if (isFuture) {
            return String.format(context.getString(R.string.gen_dates_upcoming_age_future), yearsStr);
        } else {
            return String.format(context.getString(R.string.gen_dates_upcoming_age_turned), yearsStr);
        }
    }

    private String formatInterval(Interval interval) {
        if (interval == null)
            return context.getString(R.string.gen_dates_upcoming_interval_unknown);

        int years = interval.y();
        int months = interval.m();
        int days = interval.d();

        if (years > 0) {
            return formatYears(years);
        } else if (months > 0) {
            return formatMonths(months);
        } else if (days > 0) {
            return formatDays(days);
        }

        return "";
    }

    private String formatYears(int years) {
        return context.getResources().getQuantityString(R.plurals.gen_dates_upcoming_year, years, years);
    }

    private String formatMonths(int months) {
        return context.getResources().getQuantityString(R.plurals.gen_dates_upcoming_month, months, months);
    }

    private String formatDays(int days) {
        return context.getResources().getQuantityString(R.plurals.gen_dates_upcoming_day, days, days);
    }
}
