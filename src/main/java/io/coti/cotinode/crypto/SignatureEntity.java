package io.coti.cotinode.crypto;

public class SignatureEntity {

    private String r;
    private String s;


    public SignatureEntity(String r,String s)
    {
        this.r = r;
        this.s = s;

    }

    public String getR(){ return this.r;}
    public String getS(){ return this.s;}


}
