/*
 * Copyright (C) Sportradar AG. See LICENSE for full license governing this code
 */

package com.sportradar.unifiedodds.sdk.caching;

import com.sportradar.unifiedodds.sdk.caching.ci.*;
import com.sportradar.utils.URN;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Defines methods used to access competitor information
 */
@SuppressWarnings({ "AbbreviationAsWordInName" })
public interface CompetitorCI extends CacheItem {
    /**
     * Returns the {@link Map} containing translated country names
     *
     * @param locales a {@link List} specifying the required languages
     * @return the {@link Map} containing translated country names
     */
    Map<Locale, String> getCountryNames(List<Locale> locales);

    /**
     * Returns the {@link Map} containing translated competitor abbreviations
     *
     * @param locales a {@link List} specifying the required languages
     * @return the {@link Map} containing translated competitor abbreviations
     */
    Map<Locale, String> getAbbreviations(List<Locale> locales);

    /**
     * Returns a value indicating whether represented competitor is virtual
     *
     * @return - a value indicating whether represented competitor is virtual
     */
    boolean isVirtual();

    /**
     * Returns a {@link String} representation of a country code
     *
     * @return - a {@link String} representation of a country code
     */
    String getCountryCode();

    /**
     * Returns the reference ids associated with the current instance
     *
     * @return - the reference ids associated with the current instance
     */
    ReferenceIdCI getReferenceId();

    /**
     * Returns a {@link List} of associated player ids
     *
     * @param locales the locales in which the players data should be pre-fetched
     * @return {@link List} of associated player ids
     */
    List<URN> getAssociatedPlayerIds(List<Locale> locales);

    /**
     * Returns a {@link List} of known competitor jerseys
     *
     * @return {@link List} of known competitor jerseys
     */
    List<JerseyCI> getJerseys();

    /**
     * Returns the associated competitor manager
     *
     * @param locales the locales in which the data should be translated
     * @return the associated competitor manager
     */
    ManagerCI getManager(List<Locale> locales);

    /**
     * Return the associated competitor home venue
     *
     * @param locales the locales in which the data should be translated
     * @return the associated competitor home venue
     */
    VenueCI getVenue(List<Locale> locales);

    /**
     * Get the gender of the player
     * @return the gender
     */
    String getGender();

    /**
     * Get the age group of the player
     * @return the age group
     */
    String getAgeGroup();

    /**
     * Returns race driver of the competitor
     *
     * @return the race driver of the competitor if available; otherwise null
     */
    RaceDriverProfileCI getRaceDriver();

    /**
     * Last time (if any) competitor profile was fetched
     * @return Last time (if any) competitor profile was fetched
     */
    Date getLastTimeCompetitorProfileIsFetched();

    /**
     * The list of locales used to fetch competitor profiles
     * @return the list of locales used to fetch competitor profiles
     */
    List<Locale> getCultureCompetitorProfileFetched();

    /**
     * Returns state/province of the country
     *
     * @return state
     */
    String getState();

    /**
     * Returns id of the associated sport
     * @return sport id
     */
    URN getSportId();

    /**
     * Returns id of the associated category
     * @return category id
     */
    URN getCategoryId();

    /**
     * Return the short name
     * @return the short name
     */
    String getShortName();
}
