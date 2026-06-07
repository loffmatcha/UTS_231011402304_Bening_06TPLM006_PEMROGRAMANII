package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Barang {
    private int idBarang;
    private String kodeBarang;
    private String namaBarang;
    private String kategori;
    private String ukuran;
    private String warna;
    private BigDecimal hargaBeli;
    private BigDecimal hargaJual;
    private int stok;
    private int idSupplier;
    private String namaSupplier;
    private Timestamp createdAt;

    public Barang() {}

    public Barang(int idBarang, String kodeBarang, String namaBarang,
                  String kategori, String ukuran, String warna,
                  BigDecimal hargaBeli, BigDecimal hargaJual,
                  int stok, int idSupplier) {
        this.idBarang    = idBarang;
        this.kodeBarang  = kodeBarang;
        this.namaBarang  = namaBarang;
        this.kategori    = kategori;
        this.ukuran      = ukuran;
        this.warna       = warna;
        this.hargaBeli   = hargaBeli;
        this.hargaJual   = hargaJual;
        this.stok        = stok;
        this.idSupplier  = idSupplier;
    }

    // Getters & Setters
    public int getIdBarang()              { return idBarang; }
    public void setIdBarang(int v)        { this.idBarang = v; }
    public String getKodeBarang()         { return kodeBarang; }
    public void setKodeBarang(String v)   { this.kodeBarang = v; }
    public String getNamaBarang()         { return namaBarang; }
    public void setNamaBarang(String v)   { this.namaBarang = v; }
    public String getKategori()           { return kategori; }
    public void setKategori(String v)     { this.kategori = v; }
    public String getUkuran()             { return ukuran; }
    public void setUkuran(String v)       { this.ukuran = v; }
    public String getWarna()              { return warna; }
    public void setWarna(String v)        { this.warna = v; }
    public BigDecimal getHargaBeli()      { return hargaBeli; }
    public void setHargaBeli(BigDecimal v){ this.hargaBeli = v; }
    public BigDecimal getHargaJual()      { return hargaJual; }
    public void setHargaJual(BigDecimal v){ this.hargaJual = v; }
    public int getStok()                  { return stok; }
    public void setStok(int v)            { this.stok = v; }
    public int getIdSupplier()            { return idSupplier; }
    public void setIdSupplier(int v)      { this.idSupplier = v; }
    public String getNamaSupplier()       { return namaSupplier; }
    public void setNamaSupplier(String v) { this.namaSupplier = v; }
    public Timestamp getCreatedAt()       { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }

    @Override
    public String toString() { return namaBarang; }
}
