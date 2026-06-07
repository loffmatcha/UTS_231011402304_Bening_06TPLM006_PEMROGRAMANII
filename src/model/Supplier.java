package model;

import java.sql.Timestamp;

public class Supplier {
    private int idSupplier;
    private String kodeSupplier;
    private String namaSupplier;
    private String telepon;
    private String alamat;
    private String jenisKain;
    private Timestamp createdAt;

    public Supplier() {}

    public Supplier(int idSupplier, String kodeSupplier, String namaSupplier,
                    String telepon, String alamat, String jenisKain) {
        this.idSupplier   = idSupplier;
        this.kodeSupplier = kodeSupplier;
        this.namaSupplier = namaSupplier;
        this.telepon      = telepon;
        this.alamat       = alamat;
        this.jenisKain    = jenisKain;
    }

    public int    getIdSupplier()           { return idSupplier; }
    public void   setIdSupplier(int v)      { this.idSupplier = v; }
    public String getKodeSupplier()         { return kodeSupplier; }
    public void   setKodeSupplier(String v) { this.kodeSupplier = v; }
    public String getNamaSupplier()         { return namaSupplier; }
    public void   setNamaSupplier(String v) { this.namaSupplier = v; }
    public String getTelepon()              { return telepon; }
    public void   setTelepon(String v)      { this.telepon = v; }
    public String getAlamat()               { return alamat; }
    public void   setAlamat(String v)       { this.alamat = v; }
    public String getJenisKain()            { return jenisKain; }
    public void   setJenisKain(String v)    { this.jenisKain = v; }
    public Timestamp getCreatedAt()         { return createdAt; }
    public void   setCreatedAt(Timestamp v) { this.createdAt = v; }

    @Override
    public String toString() { return namaSupplier; }
}
