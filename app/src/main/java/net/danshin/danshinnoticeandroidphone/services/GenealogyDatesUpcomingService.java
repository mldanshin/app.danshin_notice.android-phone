package net.danshin.danshinnoticeandroidphone.services;

import net.danshin.danshinnoticeandroidphone.NetworkUtils;
import net.danshin.danshinnoticeandroidphone.config.BuildConfig;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.DateType;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.PersonDate;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.SettingsResponse;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.BirthEvent;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.BirthWouldEvent;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.DeathEvent;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.Person;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.UpcomingResponse;

import org.json.JSONException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class GenealogyDatesUpcomingService {
    public UpcomingResponse getGenealogyData(String token, LocalDate date, SettingsResponse setting) throws JSONException, IOException {
        UpcomingResponse response = fetchGenealogyData(token, date, setting.dayBefore(), setting.dayAfter());

        if (setting.dates() == null) {
            return null;
        }

        List<BirthEvent> filteredBeforeBirth = filterEvents(response.beforeBirth(), setting.dates(), DateType.BIRTH);
        List<BirthWouldEvent> filteredBeforeBirthWould = filterEvents(response.beforeBirthWould(), setting.dates(), DateType.BIRTH);
        List<DeathEvent> filteredBeforeDeath = filterEvents(response.beforeDeath(), setting.dates(), DateType.DEATH);
        List<BirthEvent> filteredTodayBirth = filterEvents(response.todayBirth(), setting.dates(), DateType.BIRTH);
        List<BirthWouldEvent> filteredTodayBirthWould = filterEvents(response.todayBirthWould(), setting.dates(), DateType.BIRTH);
        List<DeathEvent> filteredTodayDeath = filterEvents(response.todayDeath(), setting.dates(), DateType.DEATH);
        List<BirthEvent> filteredAfterBirth = filterEvents(response.afterBirth(), setting.dates(), DateType.BIRTH);
        List<BirthWouldEvent> filteredAfterBirthWould = filterEvents(response.afterBirthWould(), setting.dates(), DateType.BIRTH);
        List<DeathEvent> filteredAfterDeath = filterEvents(response.afterDeath(), setting.dates(), DateType.DEATH);

        boolean isEmpty = filteredBeforeBirth.isEmpty() &&
                filteredBeforeBirthWould.isEmpty() &&
                filteredBeforeDeath.isEmpty() &&
                filteredTodayBirth.isEmpty() &&
                filteredTodayBirthWould.isEmpty() &&
                filteredTodayDeath.isEmpty() &&
                filteredAfterBirth.isEmpty() &&
                filteredAfterBirthWould.isEmpty() &&
                filteredAfterDeath.isEmpty();

        return new UpcomingResponse(
                filteredBeforeBirth,
                filteredBeforeBirthWould,
                filteredBeforeDeath,
                filteredTodayBirth,
                filteredTodayBirthWould,
                filteredTodayDeath,
                filteredAfterBirth,
                filteredAfterBirthWould,
                filteredAfterDeath,
                isEmpty
        );
    }

    private UpcomingResponse fetchGenealogyData(String token, LocalDate date, int daysBefore, int daysAfter) throws JSONException, IOException {
        try {
            String urlWithParams = String.format(
                    Locale.US,
                    "%s?date=%s&before_day=%d&after_day=%d",
                    BuildConfig.getGenealogyDateUpcomingUrl(),
                    date.toString(),
                    daysBefore,
                    daysAfter
            );

            final CountDownLatch latch = new CountDownLatch(1);
            final String[] resultBody = {null};
            final Exception[] error = {null};

            NetworkUtils.getJson(urlWithParams, token, new NetworkUtils.Callback() {
                @Override
                public void onSuccess(int code, String body) {
                    if (code == 200) {
                        resultBody[0] = body;
                    } else {
                        error[0] = new IOException("Server returned code: " + code);
                    }
                    latch.countDown();
                }

                @Override
                public void onError(Exception e) {
                    error[0] = e;
                    latch.countDown();
                }
            });

            latch.await();

            if (error[0] != null) {
                throw new IOException(error[0]);
            }
            if (resultBody[0] == null) {
                throw new IOException("Empty response body");
            }
            return UpcomingResponse.fromJson(resultBody[0]);

        } catch (JSONException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Failed to execute network task", e);
        }
    }

    private <T> List<T> filterEvents(List<T> events, List<PersonDate> personDates, DateType dateType) {
        if (events == null || events.isEmpty() || personDates == null || personDates.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> personIds = new ArrayList<>();
        for (PersonDate pd : personDates) {
            if (pd.dateType() == dateType) {
                personIds.add(pd.personId());
            }
        }

        if (personIds.isEmpty()) {
            return new ArrayList<>();
        }

        return events.stream()
                .filter(event -> {
                    Person person = null;
                    if (event instanceof BirthEvent birth) {
                        person = birth.person();
                    } else if (event instanceof BirthWouldEvent birthWould) {
                        person = birthWould.person();
                    } else if (event instanceof DeathEvent death) {
                        person = death.person();
                    }
                    return person != null && personIds.contains(person.id());
                })
                .collect(Collectors.toList());
    }
}
