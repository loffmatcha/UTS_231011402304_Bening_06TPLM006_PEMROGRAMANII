package dao;

import model.Customer;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public String generateKode() throws SQLException {
        String sql = "SELECT kode_customer FROM customer ORDER BY id_customer DESC LIMIT 1";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("kode_customer");
                int num = Integer.parseInt(last.substring(4)) + 1;
                return String.format("CST-%03d", num);
            }
        }
        return "CST-001";
    }

    public boolean insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customer (kode_customer, nama_customer, telepon, alamat, platform) " +
                     "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getKodeCustomer());
            ps.setString(2, c.getNamaCustomer());
            ps.setString(3, c.getTelepon());
            ps.setString(4, c.getAlamat());
            ps.setString(5, c.getPlatform());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Customer c) throws SQLException {
        String sql = "UPDATE customer SET nama_customer=?, telepon=?, alamat=?, platform=? " +
                     "WHERE id_customer=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getNamaCustomer());
            ps.setString(2, c.getTelepon());
            ps.setString(3, c.getAlamat());
            ps.setString(4, c.getPlatform());
            ps.setInt(5, c.getIdCustomer());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int idCustomer) throws SQLException {
        String sql = "DELETE FROM customer WHERE id_customer=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idCustomer);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Customer> getAll() throws SQLException {
        return search("");
    }

    public List<Customer> search(String keyword) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customer WHERE nama_customer LIKE ? OR telepon LIKE ? " +
                     "OR platform LIKE ? ORDER BY id_customer ASC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public int countTotal() throws SQLException {
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM customer")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setIdCustomer(rs.getInt("id_customer"));
        c.setKodeCustomer(rs.getString("kode_customer"));
        c.setNamaCustomer(rs.getString("nama_customer"));
        c.setTelepon(rs.getString("telepon"));
        c.setAlamat(rs.getString("alamat"));
        c.setPlatform(rs.getString("platform"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }
}
