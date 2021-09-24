package org.deabee.android.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AppSettings extends RealmObject {
    @PrimaryKey
    private long id;
    public String IP_ADDRESS;
    public Integer PORT;
    public AppSettings(){

    }
    public AppSettings(String IP_ADDRESS, Integer PORT){
        this.IP_ADDRESS = IP_ADDRESS;
        this.PORT = PORT;
    }

    public String getIP_ADDRESS(){
        return IP_ADDRESS;
    }

    public void setIP_ADDRESS(String IP_ADDRESS) {
        this.IP_ADDRESS = IP_ADDRESS;
    }

    public Integer getPORT() {
        return PORT;
    }

    public void setPORT(Integer PORT) {
        this.PORT = PORT;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
