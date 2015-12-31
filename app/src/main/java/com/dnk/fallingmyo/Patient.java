package com.dnk.fallingmyo;

/**
 * Created by JeffreyHao-Chan on 2015-03-14.
 */
public class Patient {

    private String name;
    private String accountID;
    private String macAddress;
    private String email;
    private String roomNumber;
    private boolean Emergency;
    private Assistance assistanceRequired;
    private boolean disable;

    public Patient() {

    }

    public Patient(String name, String accountID, String macAddress, String email, String roomNumber, boolean Emergency,
                   Assistance assistanceRequired, boolean disable) {
        this.name = name;
        this.accountID = accountID;
        this.macAddress = macAddress;
        this.email = email;
        this.roomNumber = roomNumber;
        this.Emergency = Emergency;
        this.assistanceRequired = assistanceRequired;
        this.disable = disable;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getAccountID() {
        return this.accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }


    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomNumber() {
        return this.roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public boolean getEmergency() {
        return this.Emergency;
    }

    public void setEmergency(boolean emergency) {
        this.Emergency = emergency;
    }

    public Assistance getAssistanceRequired() {
        return this.assistanceRequired;
    }

    public void setAssistanceRequired(Assistance assistanceRequired) {
        this.assistanceRequired = assistanceRequired;
    }

    public boolean getDisable() {
        return this.disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }
}

