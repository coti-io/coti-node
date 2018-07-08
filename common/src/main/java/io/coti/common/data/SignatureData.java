package io.coti.common.data;

public class SignatureData {

    private String r;
    private String s;


    public SignatureData(){

    }

    public SignatureData(String r, String s)
    {
        this.r = r;
        this.s = s;

    }

    public String getR(){ return this.r;}
    public String getS(){ return this.s;}


}
