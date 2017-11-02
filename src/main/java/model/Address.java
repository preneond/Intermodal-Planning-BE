package model;

public class Address {
    public double latitude;
    public double longitude;
    public String address;

    public Address() {
    }

    public Address(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Address(String address) {
        this.address = address;
    }
}
