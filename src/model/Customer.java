package model;

import java.sql.Timestamp;

public class Customer {
    private int idCustomer;
    private String kodeCustomer;
    private String namaCustomer;
    private String telepon;
    private String alamat;
    private String platform;
    private Timestamp createdAt;

    public Customer() {}

    public Customer(int idCustomer, String kodeCustomer, String namaCustomer,
                    String telepon, String alamat, String platform) {
        this.idCustomer   = idCustomer;
        this.kodeCustomer = kodeCustomer;
        this.namaCustomer = namaCustomer;
        this.telepon      = telepon;
        this.alamat       = alamat;
        this.platform     = platform;
    }

    public int    getIdCustomer()             { return idCustomer; }
    public void   setIdCustomer(int v)        { this.idCustomer = v; }
    public String getKodeCustomer()           { return kodeCustomer; }
    public void   setKodeCustomer(String v)   { this.kodeCustomer = v; }
    public String getNamaCustomer()           { return namaCustomer; }
    public void   setNamaCustomer(String v)   { this.namaCustomer = v; }
    public String getTelepon()                { return telepon; }
    public void   setTelepon(String v)        { this.telepon = v; }
    public String getAlamat()                 { return alamat; }
    public void   setAlamat(String v)         { this.alamat = v; }
    public String getPlatform()               { return platform; }
    public void   setPlatform(String v)       { this.platform = v; }
    public Timestamp getCreatedAt()           { return createdAt; }
    public void   setCreatedAt(Timestamp v)   { this.createdAt = v; }

    @Override
    public String toString() { return namaCustomer; }
}
