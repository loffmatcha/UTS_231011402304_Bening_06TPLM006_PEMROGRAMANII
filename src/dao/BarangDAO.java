package dao;

import model.Barang;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BarangDAO {

    // ── Generate next kode ─────────────────────────────────────────────────────
    public String generateKode() throws SQLException {
        String sql = "SELECT kode_barang FROM barang ORDER BY id_barang DESC LIMIT 1";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("kode_barang"); // BRG-005
                int num = Integer.parseInt(last.substring(4)) + 1;
                return String.format("BRG-%03d", num);
            }
        }
        return "BRG-001";
    }

    // ── Insert ─────────────────────────────────────────────────────────────────
    public boolean insert(Barang b) throws SQLException {
        String sql = "INSERT INTO barang (kode_barang, nama_barang, kategori, ukuran, warna, " +
                     "harga_beli, harga_jual, stok, id_supplier) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, b.getKodeBarang());
            ps.setString(2, b.getNamaBarang());
            ps.setString(3, b.getKategori());
            ps.setString(4, b.getUkuran());
            ps.setString(5, b.getWarna());
            ps.setBigDecimal(6, b.getHargaBeli());
            ps.setBigDecimal(7, b.getHargaJual());
            ps.setInt(8, b.getStok());
            if (b.getIdSupplier() > 0)
                ps.setInt(9, b.getIdSupplier());
            else
                ps.setNull(9, Types.INTEGER);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Update ─────────────────────────────────────────────────────────────────
    public boolean update(Barang b) throws SQLException {
        String sql = "UPDATE barang SET nama_barang=?, kategori=?, ukuran=?, warna=?, " +
                     "harga_beli=?, harga_jual=?, stok=?, id_supplier=? WHERE id_barang=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, b.getNamaBarang());
            ps.setString(2, b.getKategori());
            ps.setString(3, b.getUkuran());
            ps.setString(4, b.getWarna());
            ps.setBigDecimal(5, b.getHargaBeli());
            ps.setBigDecimal(6, b.getHargaJual());
            ps.setInt(7, b.getStok());
            if (b.getIdSupplier() > 0)
                ps.setInt(8, b.getIdSupplier());
            else
                ps.setNull(8, Types.INTEGER);
            ps.setInt(9, b.getIdBarang());
            return ps.executeUpdate() > 0;
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────────
    public boolean delete(int idBarang) throws SQLException {
        String sql = "DELETE FROM barang WHERE id_barang=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idBarang);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Get all ────────────────────────────────────────────────────────────────
    public List<Barang> getAll() throws SQLException {
        return search("");
    }

    // ── Search by nama / kode ──────────────────────────────────────────────────
    public List<Barang> search(String keyword) throws SQLException {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT b.*, s.nama_supplier FROM barang b " +
                     "LEFT JOIN supplier s ON b.id_supplier = s.id_supplier " +
                     "WHERE b.nama_barang LIKE ? OR b.kode_barang LIKE ? " +
                     "ORDER BY b.id_barang ASC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── Get by id ─────────────────────────────────────────────────────────────
    public Barang getById(int id) throws SQLException {
        String sql = "SELECT b.*, s.nama_supplier FROM barang b " +
                     "LEFT JOIN supplier s ON b.id_supplier = s.id_supplier " +
                     "WHERE b.id_barang=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    // ── Count total produk ─────────────────────────────────────────────────────
    public int countTotal() throws SQLException {
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM barang")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── Count stok menipis (< 5) ───────────────────────────────────────────────
    public int countStokMenipis() throws SQLException {
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM barang WHERE stok < 5")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── Get stok menipis list ──────────────────────────────────────────────────
    public List<Barang> getStokMenipis() throws SQLException {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT b.*, s.nama_supplier FROM barang b " +
                     "LEFT JOIN supplier s ON b.id_supplier = s.id_supplier " +
                     "WHERE b.stok < 5 ORDER BY b.stok ASC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── Update stok ───────────────────────────────────────────────────────────
    public boolean updateStok(int idBarang, int deltaStok) throws SQLException {
        String sql = "UPDATE barang SET stok = stok + ? WHERE id_barang = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, deltaStok);
            ps.setInt(2, idBarang);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Map ResultSet → Barang ─────────────────────────────────────────────────
    private Barang map(ResultSet rs) throws SQLException {
        Barang b = new Barang();
        b.setIdBarang(rs.getInt("id_barang"));
        b.setKodeBarang(rs.getString("kode_barang"));
        b.setNamaBarang(rs.getString("nama_barang"));
        b.setKategori(rs.getString("kategori"));
        b.setUkuran(rs.getString("ukuran"));
        b.setWarna(rs.getString("warna"));
        b.setHargaBeli(rs.getBigDecimal("harga_beli"));
        b.setHargaJual(rs.getBigDecimal("harga_jual"));
        b.setStok(rs.getInt("stok"));
        b.setIdSupplier(rs.getInt("id_supplier"));
        try { b.setNamaSupplier(rs.getString("nama_supplier")); } catch (Exception ignored) {}
        b.setCreatedAt(rs.getTimestamp("created_at"));
        return b;
    }
}
