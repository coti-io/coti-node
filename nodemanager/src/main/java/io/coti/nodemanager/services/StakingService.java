package io.coti.nodemanager.services;

import io.coti.basenode.data.FeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.crypto.StakingNodeCrypto;
import io.coti.nodemanager.data.StakingNodeData;
import io.coti.nodemanager.http.GetStakersListResponce;
import io.coti.nodemanager.http.SetNodeStakeAdminRequest;
import io.coti.nodemanager.http.SetNodeStakeRequest;
import io.coti.nodemanager.http.SetNodeStakeResponse;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.http.data.StakingNodeDataList;
import io.coti.nodemanager.model.StakingNodes;
import io.coti.nodemanager.services.data.NodeWeighted;
import io.coti.nodemanager.services.interfaces.IStakingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static io.coti.basenode.data.NetworkType.TestNet;
import static io.coti.basenode.data.NodeType.FullNode;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.nodemanager.http.HttpStringConstants.NODE_STAKE_SET_AUTHENTICATION_ERROR;

@Service
@Slf4j
public class StakingService implements IStakingService {

    @Autowired
    private StakingNodes stakingNodes;
    @Autowired
    private StakingNodeCrypto stakingNodeCrypto;

    @Value("${staking.fee.maximum.standard: 100.0}")
    private BigDecimal maximumFee;
    @Value("${staking.fee.minimum.standard: 0.0}")
    private BigDecimal minimumFee;
    @Value("${staking.fee.rate.standard: 0.1}")
    private BigDecimal standardFee;
    @Value("${staking.fee.maximum.check: true}")
    private boolean checkMaximum;
    @Value("${staking.public.key:}")
    private String stakeSignerPublicKey;
    @Value("${staking.stakers.only: false}")
    private boolean stakersOnly;
    @Value("${staking.randomize.without.stake: false}")
    private boolean randomizeWithoutStake;

    @Override
    public ResponseEntity<IResponse> setNodeStake(SetNodeStakeAdminRequest setNodeStakeAdminRequest) {
        try {
            StakingNodeData stakingNodeData = new StakingNodeData(setNodeStakeAdminRequest.getNodeHash(), setNodeStakeAdminRequest.getStake());
            stakingNodes.put(stakingNodeData);
            log.info("New node stake data set through admin interface: {} ", setNodeStakeAdminRequest);
            return ResponseEntity.status(HttpStatus.OK).
                    body(new SetNodeStakeResponse(setNodeStakeAdminRequest.getNodeHash(), setNodeStakeAdminRequest.getStake()));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    @Override
    public ResponseEntity<IResponse> setNodeStake(SetNodeStakeRequest setNodeStakeRequest) {

        if (!checkIfSignerInList(setNodeStakeRequest.getSignerHash()) || !stakingNodeCrypto.verifySignature(setNodeStakeRequest)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(NODE_STAKE_SET_AUTHENTICATION_ERROR, STATUS_ERROR));
        }

        SetNodeStakeAdminRequest setNodeStakeAdminRequest = new SetNodeStakeAdminRequest(setNodeStakeRequest.getNodeHash(), setNodeStakeRequest.getStake());
        return setNodeStake(setNodeStakeAdminRequest);
    }

    @Override
    public ResponseEntity<IResponse> getStakersList() {
        try {
            List<StakingNodeDataList> stakers = new ArrayList<>();
            stakingNodes.forEach(N -> stakers.add(new StakingNodeDataList(N.getNodeHash(), N.getStake())));
            return ResponseEntity.status(HttpStatus.OK).
                    body(new GetStakersListResponce(stakers));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    @Override
    public ResponseEntity<String> distributionCheck(FeeData request) {
        Map<Hash, NetworkNodeData> stakers = new HashMap<>();
        stakingNodes.forEach(N -> {
            stakers.put(N.getHash(), new NetworkNodeData(FullNode, "no", "no", N.getHash(), TestNet));
        });

        for (NetworkNodeData stake : stakers.values()) {
            stake.setFeeData(new FeeData(request.getFeePercentage(), request.getMinimumFee(), request.getMaximumFee()));
        }

        Hash selectedNode = selectNode(stakers);

        return ResponseEntity.status(HttpStatus.OK).body(selectedNode.toHexString());
    }

    @Override
    public Hash selectNode(Map<Hash, NetworkNodeData> nodeMap) {
        double totalWeight = 0.0;
        int numberNodes = 0;
// it can be done another way, first create list from the map, shuffle, calculate accumulated weights for all positions, after use binary search, but the gain is unclear
        List<NodeWeighted> nodesListWeighted = new ArrayList<>();
        if (!randomizeWithoutStake) {
            for (NetworkNodeData node : nodeMap.values()) {
                StakingNodeData stakingNodeData = stakingNodes.getByHash(node.getNodeHash());
                if ((stakingNodeData != null) && (stakingNodeData.getStake() != null) && (stakingNodeData.getStake().doubleValue() > 0)) {
                    double weight = stakingNodeData.getStake().doubleValue() *
                            probabilityFromFee(node.getFeeData()) *
                            probabilityFromTS(node.getTrustScore());

                    if (weight > 0) {
                        NodeWeighted nodeWeighted = new NodeWeighted(node.getNodeHash(), weight);
                        nodesListWeighted.add(nodeWeighted);
                        totalWeight = totalWeight + weight;
                        numberNodes++;
                    }
                }
            }
        } else {
            for (NetworkNodeData node : nodeMap.values()) {
                double weight = probabilityFromFee(node.getFeeData()) *
                        probabilityFromTS(node.getTrustScore());

                if (weight > 0) {
                    NodeWeighted nodeWeighted = new NodeWeighted(node.getNodeHash(), weight);
                    nodesListWeighted.add(nodeWeighted);
                    totalWeight = totalWeight + weight;
                    numberNodes++;
                }
            }
        }
        Collections.shuffle(nodesListWeighted);
        double randomValue = Math.random();
        double randomWeight = totalWeight * randomValue;

        if (randomValue <= 0.5) {
            double accumWeight = 0.0;
            for (int i = 0; i < numberNodes; i++) {
                accumWeight = accumWeight + nodesListWeighted.get(i).getWeight();
                if (accumWeight >= randomWeight) {
                    return nodesListWeighted.get(i).getNodeHash();
                }
            }
        } else {
            double accumWeight = totalWeight;
            for (int i = numberNodes - 1; i >= 0; i--) {
                accumWeight = accumWeight - nodesListWeighted.get(i).getWeight();
                if (accumWeight <= randomWeight) {
                    return nodesListWeighted.get(i).getNodeHash();
                }
            }
        }
        return null;
    }

    @Override
    public boolean filterFullNodes(SingleNodeDetailsForWallet singleNodeDetailsForWallet) {
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
        double f = feeData.getFeePercentage().doubleValue() / 100.0;
        if (f == 0) {
            return 0;
        }
        double f0 = standardFee.doubleValue() / 100.0;
        if (f >= f0 * 6.0 / 4.0) {
            return 0;
        }

        double p = 1.0 / f0 - (16.0 / 9.0 / (Math.pow(f0, 3))) * Math.pow((f - 3.0 / 4.0 * f0), 2);
        return p;
    }

    private double probabilityFromTS(Double trustScore) {
        return 1.0;
    }

    private boolean checkIfSignerInList(Hash signerHash) {
        return signerHash.toString().equals(stakeSignerPublicKey);
    }
}
