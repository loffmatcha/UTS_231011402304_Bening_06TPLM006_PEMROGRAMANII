package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaksi {
    private int idTransaksi;
    private String noTransaksi;
    private int idCustomer;
    private String namaCustomer;
    private Timestamp tanggal;
    private String platformJual;
    private BigDecimal totalHarga;
    private BigDecimal diskon;
    private BigDecimal ongkir;
    private BigDecimal grandTotal;
    private String status;
    private String catatan;

    public Transaksi() {}

    public int         getIdTransaksi()              { return idTransaksi; }
    public void        setIdTransaksi(int v)         { this.idTransaksi = v; }
    public String      getNoTransaksi()              { return noTransaksi; }
    public void        setNoTransaksi(String v)      { this.noTransaksi = v; }
    public int         getIdCustomer()               { return idCustomer; }
    public void        setIdCustomer(int v)          { this.idCustomer = v; }
    public String      getNamaCustomer()             { return namaCustomer; }
    public void        setNamaCustomer(String v)     { this.namaCustomer = v; }
    public Timestamp   getTanggal()                  { return tanggal; }
    public void        setTanggal(Timestamp v)       { this.tanggal = v; }
    public String      getPlatformJual()             { return platformJual; }
    public void        setPlatformJual(String v)     { this.platformJual = v; }
    public BigDecimal  getTotalHarga()               { return totalHarga; }
    public void        setTotalHarga(BigDecimal v)   { this.totalHarga = v; }
    public BigDecimal  getDiskon()                   { return diskon; }
    public void        setDiskon(BigDecimal v)       { this.diskon = v; }
    public BigDecimal  getOngkir()                   { return ongkir; }
    public void        setOngkir(BigDecimal v)       { this.ongkir = v; }
    public BigDecimal  getGrandTotal()               { return grandTotal; }
    public void        setGrandTotal(BigDecimal v)   { this.grandTotal = v; }
    public String      getStatus()                   { return status; }
    public void        setStatus(String v)           { this.status = v; }
    public String      getCatatan()                  { return catatan; }
    public void        setCatatan(String v)          { this.catatan = v; }
}
