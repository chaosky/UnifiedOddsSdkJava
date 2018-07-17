/*
 * Copyright (C) Sportradar AG. See LICENSE for full license governing this code
 */

package com.sportradar.unifiedodds.sdk.caching.ci;

import com.google.common.base.Preconditions;
import com.sportradar.uf.sportsapi.datamodel.SAPISportEventConditions;

import java.util.Locale;

/**
 * A sport event conditions representation used by caching components
 */
public class SportEventConditionsCI {
    /**
     * A {@link String} specifying the attendance of the associated sport event
     */
    private String attendance;

    /**
     * The mode of the event
     */
    private String eventMode;

    /**
     * The {@link RefereeCI} instance representing the referee presiding over the associated sport event
     */
    private RefereeCI referee;

    /**
     * The {@link WeatherInfoCI} instance representing the expected weather on the associated sport event
     */
    private WeatherInfoCI weatherInfo;

    /**
     * Initializes a new instance of the {@link SportEventConditionsCI} class
     *
     * @param seConditions - {@link SAPISportEventConditions} containing information about the competitor
     * @param locale - {@link Locale} specifying the language of the <i>seConditions</i>
     */
    public SportEventConditionsCI(SAPISportEventConditions seConditions, Locale locale) {
        Preconditions.checkNotNull(seConditions);
        Preconditions.checkNotNull(locale);

        merge(seConditions, locale);
    }

    /**
     * Merges the information from the provided {@link SAPISportEventConditions} into the current instance
     *
     * @param seConditions - {@link SAPISportEventConditions} containing information about the competitor
     * @param locale - {@link Locale} specifying the language of the <i>seConditions</i>
     */
    public void merge(SAPISportEventConditions seConditions, Locale locale) {
        Preconditions.checkNotNull(seConditions);
        Preconditions.checkNotNull(locale);

        attendance = seConditions.getAttendance();
        eventMode = seConditions.getMatchMode();

        if (seConditions.getReferee() != null) {
            if (referee == null) {
                referee = new RefereeCI(seConditions.getReferee(), locale);
            } else {
                referee.merge(seConditions.getReferee(), locale);
            }
        }

        if (seConditions.getWeatherInfo() != null) {
            weatherInfo = new WeatherInfoCI(seConditions.getWeatherInfo());
        }
    }

    /**
     * Returns a {@link String} specifying the attendance of the associated sport event
     *
     * @return - a {@link String} specifying the attendance of the associated sport event
     */
    public String getAttendance() {
        return attendance;
    }

    /**
     * Returns the mode of the event
     *
     * @return - the mode of the event
     */
    public String getEventMode() {
        return eventMode;
    }

    /**
     * Returns the {@link RefereeCI} instance representing the referee presiding over the associated sport event
     *
     * @return - the {@link RefereeCI} instance representing the referee presiding over the associated sport event
     */
    public RefereeCI getReferee() {
        return referee;
    }

    /**
     * Returns the {@link WeatherInfoCI} instance representing the expected weather on the associated sport event
     *
     * @return - the {@link WeatherInfoCI} instance representing the expected weather on the associated sport event
     */
    public WeatherInfoCI getWeatherInfo() {
        return weatherInfo;
    }
}
