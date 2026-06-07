package dao;

import model.Supplier;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public String generateKode() throws SQLException {
        String sql = "SELECT kode_supplier FROM supplier ORDER BY id_supplier DESC LIMIT 1";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("kode_supplier");
                int num = Integer.parseInt(last.substring(4)) + 1;
                return String.format("SUP-%03d", num);
            }
        }
        return "SUP-001";
    }

    public boolean insert(Supplier s) throws SQLException {
        String sql = "INSERT INTO supplier (kode_supplier, nama_supplier, telepon, alamat, jenis_kain) " +
                     "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, s.getKodeSupplier());
            ps.setString(2, s.getNamaSupplier());
            ps.setString(3, s.getTelepon());
            ps.setString(4, s.getAlamat());
            ps.setString(5, s.getJenisKain());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Supplier s) throws SQLException {
        String sql = "UPDATE supplier SET nama_supplier=?, telepon=?, alamat=?, jenis_kain=? " +
                     "WHERE id_supplier=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, s.getNamaSupplier());
            ps.setString(2, s.getTelepon());
            ps.setString(3, s.getAlamat());
            ps.setString(4, s.getJenisKain());
            ps.setInt(5, s.getIdSupplier());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int idSupplier) throws SQLException {
        String sql = "DELETE FROM supplier WHERE id_supplier=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idSupplier);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Supplier> getAll() throws SQLException {
        return search("");
    }

    public List<Supplier> search(String keyword) throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM supplier WHERE nama_supplier LIKE ? OR kode_supplier LIKE ? " +
                     "ORDER BY id_supplier ASC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Supplier map(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setIdSupplier(rs.getInt("id_supplier"));
        s.setKodeSupplier(rs.getString("kode_supplier"));
        s.setNamaSupplier(rs.getString("nama_supplier"));
        s.setTelepon(rs.getString("telepon"));
        s.setAlamat(rs.getString("alamat"));
        s.setJenisKain(rs.getString("jenis_kain"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
