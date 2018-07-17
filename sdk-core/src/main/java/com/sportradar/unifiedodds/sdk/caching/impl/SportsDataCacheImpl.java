/*
 * Copyright (C) Sportradar AG. See LICENSE for full license governing this code
 */

package com.sportradar.unifiedodds.sdk.caching.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.sportradar.uf.sportsapi.datamodel.SAPICategory;
import com.sportradar.uf.sportsapi.datamodel.SAPIDrawFixture;
import com.sportradar.uf.sportsapi.datamodel.SAPIFixture;
import com.sportradar.uf.sportsapi.datamodel.SAPILottery;
import com.sportradar.uf.sportsapi.datamodel.SAPIMatchSummaryEndpoint;
import com.sportradar.uf.sportsapi.datamodel.SAPIMatchTimelineEndpoint;
import com.sportradar.uf.sportsapi.datamodel.SAPISport;
import com.sportradar.uf.sportsapi.datamodel.SAPISportEvent;
import com.sportradar.uf.sportsapi.datamodel.SAPIStageSummaryEndpoint;
import com.sportradar.uf.sportsapi.datamodel.SAPITournament;
import com.sportradar.uf.sportsapi.datamodel.SAPITournamentExtended;
import com.sportradar.uf.sportsapi.datamodel.SAPITournamentInfoEndpoint;
import com.sportradar.unifiedodds.sdk.SDKInternalConfiguration;
import com.sportradar.unifiedodds.sdk.caching.CacheItem;
import com.sportradar.unifiedodds.sdk.caching.CategoryCI;
import com.sportradar.unifiedodds.sdk.caching.DataRouter;
import com.sportradar.unifiedodds.sdk.caching.DataRouterListener;
import com.sportradar.unifiedodds.sdk.caching.DataRouterManager;
import com.sportradar.unifiedodds.sdk.caching.SportCI;
import com.sportradar.unifiedodds.sdk.caching.SportsDataCache;
import com.sportradar.unifiedodds.sdk.caching.impl.ci.CacheItemFactory;
import com.sportradar.unifiedodds.sdk.exceptions.internal.CacheItemNotFoundException;
import com.sportradar.unifiedodds.sdk.exceptions.internal.CommunicationException;
import com.sportradar.unifiedodds.sdk.exceptions.internal.IllegalCacheStateException;
import com.sportradar.utils.URN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implements methods used to access various sport events data
 */
public class SportsDataCacheImpl implements SportsDataCache, DataRouterListener {
    private static final Logger logger = LoggerFactory.getLogger(SportsDataCacheImpl.class);

    /**
     * A {@link Cache} instance used to cache fetched sports
     */
    private final Cache<URN, SportCI> sportsCache;

    /**
     * A {@link Cache} instance used to cache fetched categories
     */
    private final Cache<URN, CategoryCI> categoriesCache;

    /**
     * A factory used to build specific sport event cache items
     */
    private final CacheItemFactory cacheItemFactory;

    /**
     * The {@link DataRouterManager} instance used to initiate data requests
     */
    private final DataRouterManager dataRouterManager;


    @Inject
    SportsDataCacheImpl(Cache<URN, SportCI> sportsCache,
                        Cache<URN, CategoryCI> categoriesCache,
                        CacheItemFactory cacheItemFactory,
                        SDKInternalConfiguration configuration,
                        DataRouterManager dataRouterManager) {
        Preconditions.checkNotNull(sportsCache);
        Preconditions.checkNotNull(categoriesCache);
        Preconditions.checkNotNull(cacheItemFactory);
        Preconditions.checkNotNull(configuration);
        Preconditions.checkNotNull(dataRouterManager);

        this.sportsCache = sportsCache;
        this.categoriesCache = categoriesCache;
        this.cacheItemFactory = cacheItemFactory;
        this.dataRouterManager = dataRouterManager;
    }


    /**
     * Returns a {@link List} sports supported by the feed.
     *
     * @param locales a {@link List} of {@link Locale} specifying the languages in which the data is returned
     * @return a {@link List} sports supported by the feed
     */
    @Override
    public List<SportData> getSports(List<Locale> locales) throws IllegalCacheStateException {
        Preconditions.checkNotNull(locales);

        ensureLocalesPreFetched(locales);

        return sportsCache.asMap().entrySet().stream()
                .map(entry -> getSportFromCache(entry.getKey(), locales))
                .collect(Collectors.toList());
    }

    /**
     * Returns a {@link SportData} instance representing the sport associated with the provided {@link URN} identifier
     *
     * @param sportId a {@link URN} specifying the id of the sport
     * @param locales a {@link List} of {@link Locale} specifying the languages in which the data is returned
     * @return a {@link SportData} containing information about the requested sport
     */
    @Override
    public SportData getSport(URN sportId, List<Locale> locales) throws IllegalCacheStateException, CacheItemNotFoundException {
        Preconditions.checkNotNull(sportId);
        Preconditions.checkNotNull(locales);

        ensureLocalesPreFetched(locales);

        return Optional.ofNullable(getSportFromCache(sportId, locales))
                .orElseThrow(() -> new CacheItemNotFoundException("Sport CI with id[" + sportId + "] could not be found"));
    }

    /**
     * Returns the associated category data
     *
     * @param categoryId the identifier of the category
     * @param locales the locales in which to provide the data
     * @return the category data of the category associated with the provided identifier
     * @throws IllegalCacheStateException if the cache load failed
     * @throws CacheItemNotFoundException if the cache item could not be found - category does not exists in the cache/api
     */
    @Override
    public CategoryCI getCategory(URN categoryId, List<Locale> locales) throws IllegalCacheStateException, CacheItemNotFoundException {
        Preconditions.checkNotNull(categoryId);
        Preconditions.checkNotNull(locales);

        ensureLocalesPreFetched(locales);

        return Optional.ofNullable(categoriesCache.getIfPresent(categoryId))
                .orElseThrow(() -> new CacheItemNotFoundException("Category CI with id[" + categoryId + "], could not be found"));
    }

    @Override
    public void onSportEventFetched(URN id, SAPISportEvent data, Locale dataLocale) {
        Preconditions.checkNotNull(data);

        onTournamentReceived(data.getTournament(), dataLocale);
    }

    @Override
    public void onTournamentFetched(URN id, SAPITournament data, Locale locale) {
        Preconditions.checkNotNull(data);

        onTournamentReceived(id, data, locale);
    }

    @Override
    public void onTournamentExtendedFetched(URN id, SAPITournamentExtended data, Locale dataLocale) {
        Preconditions.checkNotNull(data);

        onTournamentReceived(id, data, dataLocale);
    }

    @Override
    public void onTournamentInfoEndpointFetched(URN requestedId, URN tournamentId, URN seasonId, SAPITournamentInfoEndpoint data, Locale dataLocale, CacheItem requester) {
        Preconditions.checkNotNull(data);

        onTournamentReceived(tournamentId, data.getTournament(), dataLocale);
    }

    @Override
    public void onStageSummaryEndpointFetched(URN id, SAPIStageSummaryEndpoint data, Locale dataLocale, CacheItem requester) {
        Preconditions.checkNotNull(data);

        if (data.getSportEvent() != null) {
            onTournamentReceived(data.getSportEvent().getTournament(), dataLocale);
        }
    }

    @Override
    public void onMatchSummaryEndpointFetched(URN id, SAPIMatchSummaryEndpoint data, Locale dataLocale, CacheItem requester) {
        Preconditions.checkNotNull(data);

        if (data.getSportEvent() != null) {
            onTournamentReceived(data.getSportEvent().getTournament(), dataLocale);
        }
    }

    @Override
    public void onFixtureFetched(URN id, SAPIFixture data, Locale dataLocale, CacheItem requester) {
        Preconditions.checkNotNull(data);

        onTournamentReceived(data.getTournament(), dataLocale);
    }

    @Override
    public void onSportFetched(URN sportId, SAPISport sport, Locale dataLocale) {
        Preconditions.checkNotNull(sport);
        Preconditions.checkNotNull(dataLocale);

        SportCI ifPresentSport = sportsCache.getIfPresent(sportId);
        if (ifPresentSport == null) {
            sportsCache.put(sportId, cacheItemFactory.buildSportCI(sportId, sport, null, dataLocale));
        } else {
            ifPresentSport.merge(sport, dataLocale);
        }
    }

    @Override
    public void onMatchTimelineFetched(URN id, SAPIMatchTimelineEndpoint data, Locale dataLocale, CacheItem requester) {
        Preconditions.checkNotNull(data);

        if (data.getSportEvent() != null) {
            onTournamentReceived(data.getSportEvent().getTournament(), dataLocale);
        }
    }

    @Override
    public void onLotteryFetched(URN id, SAPILottery data, Locale locale, CacheItem requester) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(locale);

        onLotteryReceived(id, data, locale);
    }

    @Override
    public void onDrawFixtureFetched(URN id, SAPIDrawFixture data, Locale locale, CacheItem requester) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(locale);

        onLotteryReceived(data.getLottery(), locale);
    }

    private void onLotteryReceived(SAPILottery lottery, Locale dataLocale) {
        Preconditions.checkNotNull(dataLocale);

        if (lottery == null) {
            return;
        }

        onSportAndCategoryReceived(URN.parse(lottery.getId()), lottery, lottery.getSport(), lottery.getCategory(), dataLocale);
    }

    private void onLotteryReceived(URN lotteryId, SAPILottery lottery, Locale dataLocale) {
        Preconditions.checkNotNull(lotteryId);
        Preconditions.checkNotNull(lottery);
        Preconditions.checkNotNull(dataLocale);

        onSportAndCategoryReceived(lotteryId, lottery, lottery.getSport(), lottery.getCategory(), dataLocale);
    }

    private void onTournamentReceived(SAPITournament tournament, Locale dataLocale) {
        Preconditions.checkNotNull(dataLocale);

        if (tournament == null) {
            return;
        }

        onTournamentReceived(URN.parse(tournament.getId()), tournament, dataLocale);
    }

    private void onTournamentReceived(URN tournamentId, SAPITournament tournament, Locale dataLocale) {
        Preconditions.checkNotNull(tournamentId);
        Preconditions.checkNotNull(tournament);
        Preconditions.checkNotNull(dataLocale);

        onSportAndCategoryReceived(tournamentId, tournament, tournament.getSport(), tournament.getCategory(), dataLocale);
    }

    private void onSportAndCategoryReceived(URN tournamentId, Object sourceApiObject, SAPISport sport, SAPICategory category, Locale dataLocale) {
        Preconditions.checkNotNull(tournamentId);
        Preconditions.checkNotNull(sourceApiObject);
        Preconditions.checkNotNull(sport);
        Preconditions.checkNotNull(category);
        Preconditions.checkNotNull(dataLocale);

        URN sportId = URN.parse(sport.getId());
        URN categoryId = URN.parse(category.getId());

        CategoryCI ifPresentCategory = categoriesCache.getIfPresent(categoryId);
        if (ifPresentCategory == null) {
            categoriesCache.put(categoryId, cacheItemFactory.buildCategoryCI(categoryId, category, Collections.singletonList(tournamentId), sportId, dataLocale));
        } else {
            ifPresentCategory.merge(sourceApiObject, dataLocale);
        }

        SportCI ifPresentSport = sportsCache.getIfPresent(sportId);
        if (ifPresentSport == null) {
            sportsCache.put(sportId, cacheItemFactory.buildSportCI(sportId, sport, Collections.singletonList(categoryId), dataLocale));
        } else {
            ifPresentSport.merge(sourceApiObject, dataLocale);
        }
    }

    /**
     * Ensures that the sports data was already pre-fetched by the {@link DataRouter}
     *
     * @param locales the needed locales
     * @throws IllegalCacheStateException if an error occurs while fetching the data translations
     */
    private void ensureLocalesPreFetched(List<Locale> locales) throws IllegalCacheStateException {
        for (Locale locale : locales) {
            try {
                dataRouterManager.requestAllTournamentsForAllSportsEndpoint(locale);
                dataRouterManager.requestAllSportsEndpoint(locale);
            } catch (CommunicationException e) {
                throw new IllegalCacheStateException("An error occurred while fetching all sports endpoint", e);
            }
            try {
                dataRouterManager.requestAllLotteriesEndpoint(locale);
            } catch (CommunicationException e) {
                logger.warn("Lotteries endpoint request failed while ensuring cache integrity", e);
            }
        }
    }

    /**
     * Returns a {@link SportData} representing the sport specified by <code>sportId</code> in the
     * languages specified by <code>locales</code>, or a null reference if the specified sport does not exist
     *
     * @param sportId a {@link URN } specifying the id of the sport to get
     * @param locales a {@link  List} specifying the languages to which the sport must be translated
     * @return a {@link SportData} representing the sport or null if the request failed
     */
    private SportData getSportFromCache(URN sportId, List<Locale> locales) {
        SportCI sportCI = sportsCache.getIfPresent(sportId);
        if (sportCI == null) {
            return null;
        }

        List<CategoryData> cachedCategories = new ArrayList<>();
        for (URN catURN : sportCI.getCategoryIds()) {
            CategoryCI categoryCI = categoriesCache.getIfPresent(catURN);
            if (categoryCI == null) {
                return null;
            }

            cachedCategories.add(new CategoryData(
                    categoryCI.getId(),
                    categoryCI.getNames(locales).entrySet().stream().
                            filter(lsEntry -> locales.contains(lsEntry.getKey())).
                            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                    categoryCI.getTournamentIds(),
                    categoryCI.getCountryCode()));
        }

        return new SportData(
                sportCI.getId(),
                sportCI.getNames(locales).entrySet().stream().
                        filter(lsEntry -> locales.contains(lsEntry.getKey())).
                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                cachedCategories);
    }
}
