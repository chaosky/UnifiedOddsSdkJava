package com.sportradar.unifiedodds.sdk.cfg;

import com.google.common.base.Preconditions;
import com.sportradar.unifiedodds.sdk.SDKConfigurationPropertiesReader;
import com.sportradar.unifiedodds.sdk.SDKConfigurationReader;
import com.sportradar.unifiedodds.sdk.SDKConfigurationYamlReader;

import java.util.concurrent.TimeUnit;

/**
 * A base implementation of the {@link RecoveryConfigurationBuilder}
 */
abstract class RecoveryConfigurationBuilderImpl<T> extends ConfigurationBuilderBaseImpl<T> implements RecoveryConfigurationBuilder<T> {
    private final static int MIN_INACTIVITY_SECONDS = 20;
    private final static int MAX_INACTIVITY_SECONDS = 180;
    private final static int MIN_RECOVERY_EXECUTION_MINUTES = 15;
    private final static int MAX_RECOVERY_EXECUTION_MINUTES = 60 * 6;

    int maxInactivitySeconds = MIN_INACTIVITY_SECONDS;
    int maxRecoveryExecutionTimeMinutes = MAX_RECOVERY_EXECUTION_MINUTES;

    RecoveryConfigurationBuilderImpl(SDKConfigurationPropertiesReader sdkConfigurationPropertiesReader, SDKConfigurationYamlReader sdkConfigurationYamlReader) {
        super(sdkConfigurationPropertiesReader, sdkConfigurationYamlReader);
    }

    @Override
    public T loadConfigFromSdkProperties() {
        loadConfigFrom(sdkConfigurationPropertiesReader);

        return super.loadConfigFromSdkProperties();
    }

    @Override
    public T loadConfigFromApplicationYml() {
        loadConfigFrom(sdkConfigurationYamlReader);

        return super.loadConfigFromApplicationYml();
    }

    /**
     * Sets the max time window between two consecutive alive messages before the associated producer is marked as down(min 20s - max 180s)
     *
     * @param inactivitySeconds the max time window between two consequtive alive messages
     * @return a {@link RecoveryConfigurationBuilder} derived instance used to set general configuration properties
     */
    @Override
    @SuppressWarnings("unchecked")
    public T setMaxInactivitySeconds(int inactivitySeconds) {
        Preconditions.checkArgument(inactivitySeconds >= MIN_INACTIVITY_SECONDS, "Inactivity seconds value must be more than " + MIN_INACTIVITY_SECONDS);
        Preconditions.checkArgument(inactivitySeconds <= MAX_INACTIVITY_SECONDS, "Inactivity seconds value must be less than " + MAX_INACTIVITY_SECONDS);

        this.maxInactivitySeconds = inactivitySeconds;
        return (T) this;
    }

    /**
     * Sets the maximum time in seconds in which recovery must be completed (minimum 15 minutes - max 6 hours)
     *
     * @param value the {@link TimeUnit} value
     * @param timeUnit the used {@link TimeUnit}
     * @return a {@link RecoveryConfigurationBuilder} derived instance used to set general configuration properties
     */
    @Override
    @SuppressWarnings("unchecked")
    public T setMaxRecoveryExecutionTime(int value, TimeUnit timeUnit) {
        Preconditions.checkNotNull(timeUnit, "The time unit can not be null");

        long executionMinutes = TimeUnit.MINUTES.convert(value, timeUnit);

        Preconditions.checkArgument(executionMinutes >= MIN_RECOVERY_EXECUTION_MINUTES, "Recovery execution minutes must be more than " + MIN_RECOVERY_EXECUTION_MINUTES);
        Preconditions.checkArgument(executionMinutes <= MAX_RECOVERY_EXECUTION_MINUTES, "Recovery execution minutes must be less than " + MAX_RECOVERY_EXECUTION_MINUTES);

        maxRecoveryExecutionTimeMinutes = Math.toIntExact(executionMinutes);
        return (T) this;
    }

    /**
     * Loads the properties that are relevant to the builder from the provided {@link SDKConfigurationReader}
     *
     * @param sdkConfigurationReader the reader from which the properties should be red
     */
    private void loadConfigFrom(SDKConfigurationReader sdkConfigurationReader) {
        Preconditions.checkNotNull(sdkConfigurationReader);

        sdkConfigurationPropertiesReader.readMaxRecoveryTime().ifPresent(v -> setMaxRecoveryExecutionTime(v, TimeUnit.MINUTES));
        sdkConfigurationPropertiesReader.readMaxInactivitySeconds().ifPresent(this::setMaxInactivitySeconds);
    }
}
