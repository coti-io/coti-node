package io.coti.basenode.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class ClusterStampNameData implements IEntity {

    private ClusterStampType type;
    private Long versionTime;
    private Long creationTime;
    private Hash hash;

    public ClusterStampNameData(String clusterStampName){
        String delimiter = "_";
        String[] clusterstampDelimitedName = clusterStampName.split(delimiter);
        this.type = clusterstampDelimitedName[1].equals("M") ? ClusterStampType.MAJOR : ClusterStampType.TOKEN;
        this.versionTime = Long.parseLong(clusterstampDelimitedName[2]);
        this.creationTime = Long.parseLong(clusterstampDelimitedName[3].split("\\.")[0]);
        generateAndSetHash();
    }

    public ClusterStampNameData(ClusterStampType type){
        this.type = type;
        this.versionTime = Instant.now().toEpochMilli();
        this.creationTime = versionTime;
        generateAndSetHash();
    }

    public ClusterStampNameData(ClusterStampType type, Long versionTime){
        this.type = type;
        this.versionTime = versionTime;
        this.creationTime = Instant.now().toEpochMilli();
        generateAndSetHash();
    }

    public String getClusterStampFileName(){
        StringBuilder sb = new StringBuilder("Clusterstamp_");
        sb.append(type.getMark()).append("_").append(versionTime.toString());
        if(!versionTime.equals(creationTime)){
            sb.append("_").append(creationTime.toString());
        }
        return sb.append(".csv").toString();
    }

    public ClusterStampNameData(){

    }

    private void generateAndSetHash(){
        byte[] typeInBytes = Integer.toString(this.type.ordinal()).getBytes();
        byte[] versionTimeInBytes = this.versionTime.toString().getBytes();
        byte[] creationTimeInBytes = this.creationTime.toString().getBytes();
        byte[] concatDataFields = ByteBuffer.allocate(typeInBytes.length + versionTimeInBytes.length + creationTimeInBytes.length)
                .put(typeInBytes).put(versionTimeInBytes).put(creationTimeInBytes).array();
        this.hash = CryptoHelper.cryptoHash(concatDataFields);
    }

    public boolean isMajor(){
        return type == ClusterStampType.MAJOR;
    }
}
