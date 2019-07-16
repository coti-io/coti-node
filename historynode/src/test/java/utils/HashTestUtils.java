package utils;

import io.coti.basenode.data.Hash;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class HashTestUtils {

    private static String[] hexaOptions = TestConstants.hexaOptions;
    private static int SIZE_OF_HASH = TestConstants.SIZE_OF_HASH;
    public static final int SIZE_OF_ADDRESS_HASH_IN_HEX = 128;

    public static Hash generateRandomAddressHash(){
        StringBuilder hexa = new StringBuilder();
        for (int i = 0; i < SIZE_OF_ADDRESS_HASH_IN_HEX; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 16);
            hexa.append(hexaOptions[randomNum]);
        }
        String generatedPublicKey = hexa.toString();

        byte[] crc32ToAdd = getCrc32OfByteArray(DatatypeConverter.parseHexBinary(generatedPublicKey));
        return new Hash(generatedPublicKey + DatatypeConverter.printHexBinary(crc32ToAdd));
    }

    public static List<Hash> generateListOfRandomAddressHashes(int listSize){
        List<Hash> hashes = new ArrayList<>();
        for (int i = 0 ; i <= listSize ; i++){
            hashes.add(generateRandomAddressHash());
        }
        return hashes;
    }

    public static Hash generateRandomHash() {
        return generateRandomHash(SIZE_OF_HASH);
    }

    public static Hash generateRandomHash(int lengthOfHash) {
        StringBuilder hexa = new StringBuilder();
        for (int i = 0; i < lengthOfHash; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa.append(hexaOptions[randomNum]);
        }
        return new Hash(hexa.toString());
    }

    public static List<Hash> generateListOfRandomHashes(int listSize){
        List<Hash> hashes = new ArrayList<>();
        for (int i = 0 ; i <= listSize ; i++){
            hashes.add(generateRandomHash());
        }
        return hashes;
    }

    public static Set<Hash> generateSetOfRandomHashes(int listSize){
        Set<Hash> hashes = new HashSet<>();
        for (int i = 0 ; i <= listSize ; i++){
            hashes.add(generateRandomHash());
        }
        return hashes;
    }

    private static byte[] removeLeadingZerosFromAddress(byte[] addressBytesWithoutChecksum) {
        byte[] xPart = Arrays.copyOfRange(addressBytesWithoutChecksum, 0, addressBytesWithoutChecksum.length / 2);
        byte[] yPart = Arrays.copyOfRange(addressBytesWithoutChecksum, addressBytesWithoutChecksum.length / 2, addressBytesWithoutChecksum.length);

        byte[] xPointPart = new byte[0];
        byte[] yPointPart = new byte[0];

        for (int i = 0; i < xPart.length; i++) {
            if (xPart[i] != 0) {
                xPointPart = Arrays.copyOfRange(xPart, i, xPart.length);
                break;
            }
        }

        for (int i = 0; i < yPart.length; i++) {
            if (yPart[i] != 0) {
                yPointPart = Arrays.copyOfRange(yPart, i, yPart.length);
                break;
            }
        }

        ByteBuffer addressBuffer = ByteBuffer.allocate(xPointPart.length + yPointPart.length);
        addressBuffer.put(xPointPart);
        addressBuffer.put(yPointPart);
        return addressBuffer.array();
    }

    private static byte[] getCrc32OfByteArray(byte[] array) {
        Checksum checksum = new CRC32();

        byte[] addressWithoutPadding = removeLeadingZerosFromAddress(array);
        checksum.update(addressWithoutPadding, 0, addressWithoutPadding.length);
        byte[] checksumValue = ByteBuffer.allocate(4).putInt((int) checksum.getValue()).array();
        return checksumValue;
    }

}
