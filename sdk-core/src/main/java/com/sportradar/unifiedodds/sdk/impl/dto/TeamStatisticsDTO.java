/*
 * Copyright (C) Sportradar AG. See LICENSE for full license governing this code
 */

package com.sportradar.unifiedodds.sdk.impl.dto;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sportradar.uf.sportsapi.datamodel.SAPITeamStatistics;
import com.sportradar.unifiedodds.sdk.entities.HomeAway;
import com.sportradar.utils.URN;
import java.util.Map;

/**
 * Team statistics data transfer object
 */
@SuppressWarnings({ "AbbreviationAsWordInName", "ParameterNumber", "UnnecessaryParentheses" })
public class TeamStatisticsDTO {

    private final String name;
    private final URN teamId;
    private final HomeAway homeAway;
    private final Integer cards;
    private final Integer yellowCards;
    private final Integer redCards;
    private final Integer yellowRedCards;
    private final Integer cornerKicks;
    private final Integer greenCards;

    TeamStatisticsDTO(SAPITeamStatistics t, Map<HomeAway, String> homeAwayMap) {
        Preconditions.checkNotNull(t);

        name = t.getName();
        teamId = t.getId() != null ? URN.parse(t.getId()) : null;
        homeAway =
            homeAwayMap != null
                ? homeAwayMap
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().equals(t.getId()))
                    .map(Map.Entry::getKey)
                    .findAny()
                    .orElse(null)
                : null;

        SAPITeamStatistics.SAPIStatistics statistics = t.getStatistics();
        yellowRedCards = tryParseInt(statistics.getYellowRedCards());
        yellowCards = tryParseInt(statistics.getYellowCards());
        redCards = tryParseInt(statistics.getRedCards());
        cards = tryParseInt(statistics.getCards());
        cornerKicks = tryParseInt(statistics.getCornerKicks());
        greenCards = null;
    }

    TeamStatisticsDTO(
        String name,
        URN teamId,
        HomeAway homeAway,
        Integer yellowCards,
        Integer redCards,
        Integer yellowRedCards,
        Integer cornerKicks,
        Integer greenCards
    ) {
        Preconditions.checkNotNull(homeAway);

        this.name = name; // not available on the AMQP message
        this.teamId = teamId; // not available on the AMQP message
        this.homeAway = homeAway;
        this.yellowCards = yellowCards;
        this.redCards = redCards;
        this.yellowRedCards = yellowRedCards;
        this.cornerKicks = cornerKicks;
        this.greenCards = greenCards;

        boolean valueExists = false;
        int c = 0;
        if (yellowCards != null) {
            valueExists = true;
            c += yellowCards;
        }
        if (redCards != null) {
            valueExists = true;
            c += redCards;
        }
        if (yellowRedCards != null) {
            valueExists = true;
            c += yellowRedCards;
        }
        if (greenCards != null) {
            valueExists = true;
            c += greenCards;
        }
        this.cards = valueExists ? c : null;
    }

    public String getName() {
        return name;
    }

    public URN getTeamId() {
        return teamId;
    }

    public HomeAway getHomeAway() {
        return homeAway;
    }

    public Integer getCards() {
        return cards;
    }

    public Integer getYellowCards() {
        return yellowCards;
    }

    public Integer getRedCards() {
        return redCards;
    }

    public Integer getYellowRedCards() {
        return yellowRedCards;
    }

    public Integer getCornerKicks() {
        return cornerKicks;
    }

    public Integer getGreenCards() {
        return greenCards;
    }

    private static Integer tryParseInt(String val) {
        if (Strings.isNullOrEmpty(val)) {
            return null;
        }

        try {
            return Integer.valueOf(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return (
            "TeamStatisticsDTO{" +
            "teamId=" +
            teamId +
            ", name='" +
            name +
            '\'' +
            ", homeAway=" +
            homeAway +
            ", cards=" +
            cards +
            ", yellowCards=" +
            yellowCards +
            ", redCards=" +
            redCards +
            ", yellowRedCards=" +
            yellowRedCards +
            ", cornerKicks=" +
            cornerKicks +
            ", greenCards=" +
            greenCards +
            '}'
        );
    }
}
