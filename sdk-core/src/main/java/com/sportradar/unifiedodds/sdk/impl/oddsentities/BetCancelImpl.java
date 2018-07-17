/*
 * Copyright (C) Sportradar AG. See LICENSE for full license governing this code
 */

package com.sportradar.unifiedodds.sdk.impl.oddsentities;

import com.google.common.base.Preconditions;
import com.sportradar.uf.datamodel.UFBetCancel;
import com.sportradar.unifiedodds.sdk.entities.SportEvent;
import com.sportradar.unifiedodds.sdk.impl.oddsentities.markets.MarketFactory;
import com.sportradar.unifiedodds.sdk.oddsentities.BetCancel;
import com.sportradar.unifiedodds.sdk.oddsentities.MarketCancel;
import com.sportradar.unifiedodds.sdk.oddsentities.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created on 22/06/2017.
 * // TODO @eti: Javadoc
 */
class BetCancelImpl<T extends SportEvent> extends EventMessageImpl<T> implements BetCancel<T> {
    private final static Logger logger = LoggerFactory.getLogger(BetCancelImpl.class);
    private final Date startTime;
    private final Date endTime;
    private final String supercededBy;
    private final List<MarketCancel> affectedMarkets;

    BetCancelImpl(T sportEvent, UFBetCancel message, Producer producer, byte[] rawMessage, MarketFactory factory) {
        super(sportEvent, rawMessage, producer, message.getTimestamp(), message.getRequestId());
        Preconditions.checkNotNull(factory);

        startTime = message.getStartTime() == null ? null : new Date(message.getStartTime());
        endTime = message.getEndTime() == null ? null : new Date(message.getEndTime());
        supercededBy = message.getSupercededBy();

        if (message.getMarket() != null) {
            affectedMarkets = message.getMarket().stream()
                    .map(m -> factory.buildMarketCancel(sportEvent, m, message.getProduct()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } else {
            logger.warn("Processing BetCancel with empty market list");
            affectedMarkets = Collections.emptyList();
        }
    }

    /**
     * @return the list of markets that are affected
     */
    @Override
    public List<MarketCancel> getMarkets() {
        return affectedMarkets;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public String getSupercededBy() {
        return supercededBy;
    }
}
