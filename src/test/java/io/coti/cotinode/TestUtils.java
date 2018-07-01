package io.coti.cotinode;


import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TestUtils {

    private static final String[] hexaOptions = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};

    public static String getRandomHexa(){
        String hexa = "";
        for(int i =0 ; i < 20 ; i++){
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa += hexaOptions[randomNum];
        }
        return hexa;
    }

    public static Double getRandomDouble() {
        Random r = new Random();
        return 1 + (100 - 1) * r.nextDouble();
    }

}

