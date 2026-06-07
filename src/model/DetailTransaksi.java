package model;

import java.math.BigDecimal;

public class DetailTransaksi {
    private int idDetail;
    private int idTransaksi;
    private int idBarang;
    private String namaBarang;
    private String kodeBarang;
    private int jumlah;
    private BigDecimal hargaSatuan;
    private BigDecimal subtotal;

    public DetailTransaksi() {}

    public DetailTransaksi(int idBarang, String namaBarang, String kodeBarang,
                           int jumlah, BigDecimal hargaSatuan) {
        this.idBarang    = idBarang;
        this.namaBarang  = namaBarang;
        this.kodeBarang  = kodeBarang;
        this.jumlah      = jumlah;
        this.hargaSatuan = hargaSatuan;
        this.subtotal    = hargaSatuan.multiply(BigDecimal.valueOf(jumlah));
    }

    public int        getIdDetail()              { return idDetail; }
    public void       setIdDetail(int v)         { this.idDetail = v; }
    public int        getIdTransaksi()           { return idTransaksi; }
    public void       setIdTransaksi(int v)      { this.idTransaksi = v; }
    public int        getIdBarang()              { return idBarang; }
    public void       setIdBarang(int v)         { this.idBarang = v; }
    public String     getNamaBarang()            { return namaBarang; }
    public void       setNamaBarang(String v)    { this.namaBarang = v; }
    public String     getKodeBarang()            { return kodeBarang; }
    public void       setKodeBarang(String v)    { this.kodeBarang = v; }
    public int        getJumlah()                { return jumlah; }
    public void       setJumlah(int v)           { this.jumlah = v; }
    public BigDecimal getHargaSatuan()           { return hargaSatuan; }
    public void       setHargaSatuan(BigDecimal v){ this.hargaSatuan = v; }
    public BigDecimal getSubtotal()              { return subtotal; }
    public void       setSubtotal(BigDecimal v)  { this.subtotal = v; }
}
