/*
 * Copyright (C) Sportradar AG. See LICENSE for full license governing this code
 */

package com.sportradar.unifiedodds.sdk.impl;

import com.sportradar.utils.URN;

/**
 * Representation of a broker routing key
 */
public class RoutingKeyInfo {
    /**
     * The complete broker routing key
     */
    private final String fullRoutingKey;

    /**
     * The relating sport identifier
     */
    private final URN sportId;

    /**
     * The relating event identifier
     */
    private final URN eventId;

    /**
     * Indicates if the routing key is a system related routing key (snapshot complete, producer ups,...)
     * (pattern: "-.-.-.#")
     */
    private final boolean systemRoutingKey;


    /**
     * Initializes a new {@link RoutingKeyInfo} instance
     *
     * @param fullRoutingKey - the complete broker routing key
     * @param sportId - the relating sport identifier
     * @param eventId - the relating event identifier
     */
    RoutingKeyInfo(String fullRoutingKey, URN sportId, URN eventId) {
        this.fullRoutingKey = fullRoutingKey;
        this.sportId = sportId;
        this.eventId = eventId;
        this.systemRoutingKey = false;
    }

    /**
     * Initializes a new {@link RoutingKeyInfo} instance
     *
     * @param fullRoutingKey - the full broker routing key
     * @param systemRoutingKey - indication if the current routing key is a system routing key
     */
    RoutingKeyInfo(String fullRoutingKey, boolean systemRoutingKey) {
        this.systemRoutingKey = systemRoutingKey;
        this.fullRoutingKey = fullRoutingKey;
        this.sportId = null;
        this.eventId = null;
    }


    /**
     * Returns the complete routing key of the current instance
     *
     * @return - the complete routing key of the current instance
     */
    public String getFullRoutingKey() {
        return fullRoutingKey;
    }

    /**
     * Returns the relating sport {@link URN} identifier
     *
     * @return - the relating sport {@link URN} identifier if available; otherwise null
     */
    public URN getSportId() {
        return sportId;
    }

    /**
     * Returns the relating event {@link URN} identifier
     *
     * @return - the relating event {@link URN} identifier if available; otherwise null
     */
    public URN getEventId() {
        return eventId;
    }

    /**
     * Indicates if the current instance is a system routing key(snapshot complete, producer ups,...)
     *
     * @return - <code>true</code> if the current instance represents a system routing key; otherwise <code>false</code>
     */
    public boolean isSystemRoutingKey() {
        return systemRoutingKey;
    }

    @Override
    public String toString() {
        return "RoutingKeyInfo{" +
                "fullRoutingKey='" + fullRoutingKey + '\'' +
                ", sportId=" + sportId +
                ", eventId=" + eventId +
                '}';
    }
}
