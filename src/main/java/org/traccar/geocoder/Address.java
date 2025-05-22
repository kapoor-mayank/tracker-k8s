package org.traccar.geocoder;


public class Address {
    private String postcode;
    private String country;
    private String state;
    private String district;
    private String settlement;
    private String suburb;
    private String street;
    private String house;
    private String formattedAddress;

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }


    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }


    public String getSettlement() {
        return this.settlement;
    }

    public void setSettlement(String settlement) {
        this.settlement = settlement;
    }


    public String getSuburb() {
        return this.suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }


    public String getStreet() {
        return this.street;
    }

    public void setStreet(String street) {
        this.street = street;
    }


    public String getHouse() {
        return this.house;
    }

    public void setHouse(String house) {
        this.house = house;
    }


    public String getFormattedAddress() {
        return this.formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geocoder\Address.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */