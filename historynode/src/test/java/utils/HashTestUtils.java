package utils;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class HashTestUtils {

    private static final String[] hexaOptions = TestConstants.hexaOptions;
    public static final int SIZE_OF_ADDRESS_HASH_IN_HEX = 128;

    public static Hash generateRandomAddressHash() {
        StringBuilder hexa = new StringBuilder();
        for (int i = 0; i < SIZE_OF_ADDRESS_HASH_IN_HEX; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 16);
            hexa.append(hexaOptions[randomNum]);
        }
        String generatedPublicKey = hexa.toString();

        byte[] crc32ToAdd = getCrc32OfByteArray(DatatypeConverter.parseHexBinary(generatedPublicKey));
        return new Hash(generatedPublicKey + DatatypeConverter.printHexBinary(crc32ToAdd));
    }

    public static List<Hash> generateListOfRandomAddressHashes(int listSize) {
        List<Hash> hashes = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            hashes.add(generateRandomAddressHash());
        }
        return hashes;
    }

    private static byte[] getCrc32OfByteArray(byte[] array) {
        Checksum checksum = new CRC32();

        byte[] addressWithoutPadding = CryptoHelper.removeLeadingZerosFromAddress(array);
        checksum.update(addressWithoutPadding, 0, addressWithoutPadding.length);
        return ByteBuffer.allocate(4).putInt((int) checksum.getValue()).array();
    }

}
