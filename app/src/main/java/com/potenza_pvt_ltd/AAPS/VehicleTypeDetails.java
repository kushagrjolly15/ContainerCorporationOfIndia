package com.potenza_pvt_ltd.AAPS;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Kushagr_Jolly on 7/10/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleTypeDetails {
    //name and address string
    private String vehicle_type;
    private String key;
    private String code;
    private String fixed_tariff;

    public VehicleTypeDetails() {

            /*Blank default constructor essential for Firebase*/
    }
    public VehicleTypeDetails(String a){

    }

    @JsonProperty("Key")
    public String getKey(){
        return key;
    }
    public void setKey(String key){
        this.key=key;
    }
    @JsonProperty("vehicle_type")
    public String getVehicle_type(){
        return vehicle_type;
    }
    public void setVehicle_type(String key){
        this.vehicle_type=key;
    }
    @JsonProperty("code")
    public String getCode(){
        return code;
    }
    public void setCode(String key){
        this.code=key;
    }
    @JsonProperty("fixed_tariff")
    public String getFixed_tariff(){
        return fixed_tariff;
    }
    public void setFixed_tariff(String key){
        this.fixed_tariff =key;
    }
}