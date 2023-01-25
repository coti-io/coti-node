package io.coti.nodemanager.services;

import io.coti.basenode.data.FeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.data.StakingNodeData;
import io.coti.nodemanager.data.WeightedNode;
import io.coti.nodemanager.http.GetStakerListResponse;
import io.coti.nodemanager.http.SetNodeStakeRequest;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.http.data.StakingNodeResponseData;
import io.coti.nodemanager.services.interfaces.IStakingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.nodemanager.http.HttpStringConstants.NODE_STAKE_SET_AUTHENTICATION_ERROR;
import static io.coti.nodemanager.http.HttpStringConstants.STAKING_NODE_ADDED;
import static io.coti.nodemanager.services.NodeServiceManager.stakingNodeCrypto;
import static io.coti.nodemanager.services.NodeServiceManager.stakingNodes;

@Service
@Slf4j
public class StakingService implements IStakingService {

    @Value("${staking.fee.maximum.standard: 100.0}")
    private BigDecimal maximumFee;
    @Value("${staking.fee.minimum.standard: 0.0}")
    private BigDecimal minimumFee;
    @Value("${staking.fee.rate.standard: 0.1}")
    private BigDecimal standardFee;
    @Value("${staking.fee.maximum.check: true}")
    private boolean checkMaximum;
    @Value("${staking.public.keys:}")
    private String[] stakeSignerPublicKeyArray;
    @Value("${staking.stakers.only: false}")
    private boolean stakersOnly;
    private static final double PROBABILITY_FROM_TS = 1.0;

    @Override
    public ResponseEntity<IResponse> setNodeStake(StakingNodeData stakingNodeData) {
        try {
            stakingNodes.put(stakingNodeData);
            log.info("New node stake data set through admin interface: {} ", stakingNodeData);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(String.format(STAKING_NODE_ADDED, stakingNodeData.getNodeHash())));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    @Override
    public ResponseEntity<IResponse> setNodeStake(SetNodeStakeRequest setNodeStakeRequest) {

        if (!checkIfSignerInList(setNodeStakeRequest.getSignerHash()) || !stakingNodeCrypto.verifySignature(setNodeStakeRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(NODE_STAKE_SET_AUTHENTICATION_ERROR, STATUS_ERROR));
        }

        return setNodeStake(new StakingNodeData(setNodeStakeRequest.getNodeHash(), setNodeStakeRequest.getStake()));
    }

    @Override
    public ResponseEntity<IResponse> getStakerList() {
        try {
            List<StakingNodeResponseData> stakers = new ArrayList<>();
            stakingNodes.forEach(stakingNodeData -> stakers.add(new StakingNodeResponseData(stakingNodeData)));
            return ResponseEntity.status(HttpStatus.OK).body(new GetStakerListResponse(stakers));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    @Override
    public NetworkNodeData selectStakedNode(Map<Hash, NetworkNodeData> nodeMap) {
        double totalWeight = 0.0;
        int numberNodes = 0;
        List<WeightedNode> weightedNodeList = new ArrayList<>();

        for (NetworkNodeData node : nodeMap.values()) {
            StakingNodeData stakingNodeData = stakingNodes.getByHash(node.getNodeHash());
            if (stakingNodeData != null && stakingNodeData.getStake() != null && stakingNodeData.getStake().doubleValue() > 0) {
                double weight = stakingNodeData.getStake().doubleValue() * probabilityFromFee(node.getFeeData()) * PROBABILITY_FROM_TS;
                if (weight > 0) {
                    WeightedNode weightedNode = new WeightedNode(node, weight);
                    weightedNodeList.add(weightedNode);
                    totalWeight += weight;
                    numberNodes++;
                }
            }
        }

        Collections.shuffle(weightedNodeList);
        double randomValue = Math.random();
        double randomWeight = totalWeight * randomValue;
        NetworkNodeData selectedStakedNode;

        if (randomValue <= 0.5) {
            selectedStakedNode = getSelectedStakedNodeFromLowerRandom(numberNodes, weightedNodeList, randomWeight);
        } else {
            selectedStakedNode = getSelectedStakedNodeFromHigherRandom(totalWeight, numberNodes, weightedNodeList, randomWeight);
        }
        return selectedStakedNode;
    }

    private NetworkNodeData getSelectedStakedNodeFromHigherRandom(double totalWeight, int numberNodes, List<WeightedNode> weightedNodeList, double randomWeight) {
        double accumWeight = totalWeight;
        for (int i = numberNodes - 1; i >= 0; i--) {
            accumWeight -= weightedNodeList.get(i).getWeight();
            if (accumWeight <= randomWeight) {
                return weightedNodeList.get(i).getNode();
            }
        }
        return null;
    }

    private NetworkNodeData getSelectedStakedNodeFromLowerRandom(int numberNodes, List<WeightedNode> weightedNodeList, double randomWeight) {
        double accumWeight = 0.0;
        for (int i = 0; i < numberNodes; i++) {
            accumWeight += weightedNodeList.get(i).getWeight();
            if (accumWeight >= randomWeight) {
                return weightedNodeList.get(i).getNode();
            }
        }
        return null;
    }

    @Override
    public boolean filterFullNode(SingleNodeDetailsForWallet singleNodeDetailsForWallet) {
        if (!stakersOnly) {
            return true;
        }
        StakingNodeData stakingNodeData = stakingNodes.getByHash(singleNodeDetailsForWallet.getNodeHash());
        return (stakingNodeData != null) && (stakingNodeData.getStake().doubleValue() != 0.0);
    }

    private double probabilityFromFee(FeeData feeData) {
        if (checkMaximum) {
            if (feeData.getMaximumFee() == null) {
                return 0;
            }
            if (feeData.getMaximumFee().doubleValue() != maximumFee.doubleValue()) {
                return 0;
            }
        }
        if (feeData.getFeePercentage() == null) {
            return 0;
        }
        double fee = feeData.getFeePercentage().doubleValue() / 100.0;
        if (fee == 0) {
            return 0;
        }
        double recommendedFee = standardFee.doubleValue() / 100.0;
        if (fee >= recommendedFee * 6.0 / 4.0) {
            return 0;
        }

        return 1.0 / recommendedFee - (16.0 / 9.0 / (Math.pow(recommendedFee, 3))) * Math.pow((fee - 3.0 / 4.0 * recommendedFee), 2);

    }

    private boolean checkIfSignerInList(Hash signerHash) {
        return Arrays.asList(stakeSignerPublicKeyArray).contains(signerHash.toString());
    }
}
