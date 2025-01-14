package com.sportradar.unifiedodds.sdk.impl.entities;

import com.sportradar.uf.sportsapi.datamodel.SAPIPeriodStatus;
import com.sportradar.uf.sportsapi.datamodel.SAPIStageResult;
import com.sportradar.unifiedodds.sdk.entities.CompetitorResult;

@SuppressWarnings({ "UnnecessaryParentheses" })
public class CompetitorResultImpl implements CompetitorResult {

    private final String type;
    private final String value;
    private final String specifiers;

    public CompetitorResultImpl(SAPIStageResult.SAPICompetitor.SAPIResult result) {
        this.type = result.getType();
        this.value = result.getValue();
        this.specifiers = result.getSpecifiers();
    }

    public CompetitorResultImpl(SAPIPeriodStatus.SAPICompetitor.SAPIResult result) {
        this.type = result.getType();
        this.value = result.getValue();
        this.specifiers = result.getSpecifiers();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getSpecifiers() {
        return specifiers;
    }

    @Override
    public String toString() {
        return (
            "CompetitorResultImpl{" +
            "type='" +
            type +
            '\'' +
            ", value='" +
            value +
            '\'' +
            ", specifiers='" +
            specifiers +
            '\'' +
            '}'
        );
    }
}
