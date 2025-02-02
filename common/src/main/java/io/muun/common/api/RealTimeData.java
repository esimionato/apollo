package io.muun.common.api;

import io.muun.common.MuunFeatureJson;
import io.muun.common.Supports;
import io.muun.common.utils.Since;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealTimeData {

    @NotNull
    public FeeWindowJson feeWindow;

    @NotNull
    public ExchangeRateWindow exchangeRateWindow;

    @Since(
            apolloVersion = Supports.BlockchainHeight.APOLLO,
            falconVersion = Supports.BlockchainHeight.FALCON
    )
    public int currentBlockchainHeight;

    @Since(
            apolloVersion = Supports.IncomingSwaps.APOLLO,
            falconVersion = Supports.IncomingSwaps.FALCON
    )
    public List<ForwardingPolicyJson> forwardingPolicies;

    @NotNull
    public MinFeeRateIncrementToBumpJson minFeeRateIncrementToBumpJson;

    @NotNull
    public double minFeeRateInWeightUnits;

    @NotNull
    public List<MuunFeatureJson> features;

    /**
     * Json constructor.
     */
    public RealTimeData() {
    }

    /**
     * Houston constructor.
     */
    public RealTimeData(
            FeeWindowJson feeWindow,
            ExchangeRateWindow exchangeRateWindow,
            int currentBlockchainHeight,
            List<ForwardingPolicyJson> forwardingPolicies,
            MinFeeRateIncrementToBumpJson minFeeRateIncrementToBumpJson,
            double minFeeRateInWeightUnits,
            List<MuunFeatureJson> features
    ) {
        this.feeWindow = feeWindow;
        this.exchangeRateWindow = exchangeRateWindow;
        this.currentBlockchainHeight = currentBlockchainHeight;
        this.forwardingPolicies = forwardingPolicies;
        this.minFeeRateIncrementToBumpJson = minFeeRateIncrementToBumpJson;
        this.minFeeRateInWeightUnits = minFeeRateInWeightUnits;
        this.features = features;
    }
}
