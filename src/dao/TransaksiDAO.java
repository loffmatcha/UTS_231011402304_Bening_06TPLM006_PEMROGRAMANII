package dao;

import model.DetailTransaksi;
import model.Transaksi;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAO {

    // ── Generate no transaksi ──────────────────────────────────────────────────
    public String generateNoTransaksi() throws SQLException {
        String sql = "SELECT no_transaksi FROM transaksi ORDER BY id_transaksi DESC LIMIT 1";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                // Format: TRX-20260510-001
                String last = rs.getString("no_transaksi");
                String[] parts = last.split("-");
                int num = Integer.parseInt(parts[parts.length - 1]) + 1;
                String dateStr = new java.text.SimpleDateFormat("yyyyMMdd")
                        .format(new java.util.Date());
                return "TRX-" + dateStr + "-" + String.format("%03d", num);
            }
        }
        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd")
                .format(new java.util.Date());
        return "TRX-" + dateStr + "-001";
    }

    // ── Insert transaksi + details (dalam satu transaction) ───────────────────
    public boolean insertTransaksi(Transaksi t, List<DetailTransaksi> details) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            // 1. Insert header
            String sqlHead = "INSERT INTO transaksi (no_transaksi, id_customer, tanggal, " +
                    "platform_jual, total_harga, diskon, ongkir, grand_total, status, catatan) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)";
            int newId;
            try (PreparedStatement ps = conn.prepareStatement(sqlHead, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getNoTransaksi());
                if (t.getIdCustomer() > 0) ps.setInt(2, t.getIdCustomer());
                else ps.setNull(2, Types.INTEGER);
                
                if (t.getTanggal() != null) ps.setTimestamp(3, new java.sql.Timestamp(t.getTanggal().getTime()));
                else ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));

                ps.setString(4, t.getPlatformJual());
                ps.setBigDecimal(5, t.getTotalHarga());
                ps.setBigDecimal(6, t.getDiskon());
                ps.setBigDecimal(7, t.getOngkir());
                ps.setBigDecimal(8, t.getGrandTotal());
                ps.setString(9, t.getStatus() != null ? t.getStatus() : "lunas");
                ps.setString(10, t.getCatatan());
                ps.executeUpdate();
                ResultSet gk = ps.getGeneratedKeys();
                if (!gk.next()) throw new SQLException("Gagal mendapatkan ID transaksi baru.");
                newId = gk.getInt(1);
                t.setIdTransaksi(newId);
            }

            // 2. Insert details + update stok + inventory_log
            for (DetailTransaksi d : details) {
                // Insert detail
                String sqlDet = "INSERT INTO detail_transaksi (id_transaksi, id_barang, " +
                        "jumlah, harga_satuan, subtotal) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlDet)) {
                    ps.setInt(1, newId);
                    ps.setInt(2, d.getIdBarang());
                    ps.setInt(3, d.getJumlah());
                    ps.setBigDecimal(4, d.getHargaSatuan());
                    ps.setBigDecimal(5, d.getSubtotal());
                    ps.executeUpdate();
                }

                // Update stok
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE barang SET stok = stok - ? WHERE id_barang = ?")) {
                    ps.setInt(1, d.getJumlah());
                    ps.setInt(2, d.getIdBarang());
                    ps.executeUpdate();
                }

                // Inventory log
                String sqlLog = "INSERT INTO inventory_log (id_barang, jenis, jumlah, keterangan) " +
                        "VALUES (?, 'keluar', ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlLog)) {
                    ps.setInt(1, d.getIdBarang());
                    ps.setInt(2, d.getJumlah());
                    ps.setString(3, "Penjualan " + t.getNoTransaksi());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ── Get all ────────────────────────────────────────────────────────────────
    public List<Transaksi> getAll() throws SQLException {
        return filter(null, null, null, null);
    }

    // ── Get last 10 ──────────────────────────────────────────────────────────
    public List<Transaksi> getLast10() throws SQLException {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT t.*, c.nama_customer FROM transaksi t " +
                     "LEFT JOIN customer c ON t.id_customer = c.id_customer " +
                     "ORDER BY t.id_transaksi DESC LIMIT 10";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── Filter ────────────────────────────────────────────────────────────────
    public List<Transaksi> filter(String dari, String sampai,
                                   String platform, String namaCustomer) throws SQLException {
        List<Transaksi> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(
            "SELECT t.*, c.nama_customer FROM transaksi t " +
            "LEFT JOIN customer c ON t.id_customer = c.id_customer WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (dari != null && !dari.isEmpty()) {
            sb.append("AND DATE(t.tanggal) >= ? ");
            params.add(dari);
        }
        if (sampai != null && !sampai.isEmpty()) {
            sb.append("AND DATE(t.tanggal) <= ? ");
            params.add(sampai);
        }
        if (platform != null && !platform.isEmpty() && !platform.equals("Semua")) {
            sb.append("AND t.platform_jual = ? ");
            params.add(platform);
        }
        if (namaCustomer != null && !namaCustomer.isEmpty()) {
            sb.append("AND c.nama_customer LIKE ? ");
            params.add("%" + namaCustomer + "%");
        }
        sb.append("ORDER BY t.id_transaksi DESC");

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── Omzet hari ini ────────────────────────────────────────────────────────
    public BigDecimal omzetHariIni() throws SQLException {
        String sql = "SELECT COALESCE(SUM(grand_total), 0) FROM transaksi " +
                     "WHERE DATE(tanggal) = CURDATE()";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getBigDecimal(1);
        }
        return BigDecimal.ZERO;
    }

    // ── Get details of a transaksi ─────────────────────────────────────────────
    public List<DetailTransaksi> getDetails(int idTransaksi) throws SQLException {
        List<DetailTransaksi> list = new ArrayList<>();
        String sql = "SELECT d.*, b.nama_barang, b.kode_barang FROM detail_transaksi d " +
                     "JOIN barang b ON d.id_barang = b.id_barang WHERE d.id_transaksi = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DetailTransaksi d = new DetailTransaksi();
                d.setIdDetail(rs.getInt("id_detail"));
                d.setIdTransaksi(rs.getInt("id_transaksi"));
                d.setIdBarang(rs.getInt("id_barang"));
                d.setNamaBarang(rs.getString("nama_barang"));
                d.setKodeBarang(rs.getString("kode_barang"));
                d.setJumlah(rs.getInt("jumlah"));
                d.setHargaSatuan(rs.getBigDecimal("harga_satuan"));
                d.setSubtotal(rs.getBigDecimal("subtotal"));
                list.add(d);
            }
        }
        return list;
    }

    // ── Tambah stok masuk (inventory_log masuk) ───────────────────────────────
    public boolean tambahStokMasuk(int idBarang, int jumlah, String keterangan) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE barang SET stok = stok + ? WHERE id_barang = ?")) {
                ps.setInt(1, jumlah);
                ps.setInt(2, idBarang);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO inventory_log (id_barang, jenis, jumlah, keterangan) " +
                    "VALUES (?, 'masuk', ?, ?)")) {
                ps.setInt(1, idBarang);
                ps.setInt(2, jumlah);
                ps.setString(3, keterangan);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ── Get inventory log ─────────────────────────────────────────────────────
    public List<Object[]> getInventoryLog(String dari, String sampai) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(
            "SELECT il.*, b.kode_barang, b.nama_barang FROM inventory_log il " +
            "JOIN barang b ON il.id_barang = b.id_barang WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (dari != null && !dari.isEmpty()) {
            sb.append("AND DATE(il.tanggal) >= ? ");
            params.add(dari);
        }
        if (sampai != null && !sampai.isEmpty()) {
            sb.append("AND DATE(il.tanggal) <= ? ");
            params.add(sampai);
        }
        sb.append("ORDER BY il.tanggal DESC");
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("id_log"),
                    rs.getString("kode_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("jenis"),
                    rs.getInt("jumlah"),
                    rs.getString("keterangan"),
                    rs.getTimestamp("tanggal")
                });
            }
        }
        return list;
    }

    // ── Get Laporan Inventory ──────────────────────────────────────────────────
    public List<DetailTransaksi> getLaporanInventory(String dari, String sampai) throws SQLException {
        List<DetailTransaksi> list = new ArrayList<>();
        String sql = "SELECT dt.id_barang, dt.kode_barang, dt.nama_barang, SUM(dt.jumlah) as total_qty " +
                     "FROM detail_transaksi dt " +
                     "JOIN transaksi t ON dt.id_transaksi = t.id_transaksi " +
                     "WHERE DATE(t.tanggal) >= ? AND DATE(t.tanggal) <= ? " +
                     "GROUP BY dt.id_barang";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, dari);
            ps.setString(2, sampai);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                DetailTransaksi dt = new DetailTransaksi();
                dt.setIdBarang(rs.getInt("id_barang"));
                dt.setKodeBarang(rs.getString("kode_barang"));
                dt.setNamaBarang(rs.getString("nama_barang"));
                dt.setJumlah(rs.getInt("total_qty"));
                list.add(dt);
            }
        }
        return list;
    }

    private Transaksi map(ResultSet rs) throws SQLException {
        Transaksi t = new Transaksi();
        t.setIdTransaksi(rs.getInt("id_transaksi"));
        t.setNoTransaksi(rs.getString("no_transaksi"));
        t.setIdCustomer(rs.getInt("id_customer"));
        try { t.setNamaCustomer(rs.getString("nama_customer")); } catch (Exception ignored) {}
        t.setTanggal(rs.getTimestamp("tanggal"));
        t.setPlatformJual(rs.getString("platform_jual"));
        t.setTotalHarga(rs.getBigDecimal("total_harga"));
        t.setDiskon(rs.getBigDecimal("diskon"));
        t.setOngkir(rs.getBigDecimal("ongkir"));
        t.setGrandTotal(rs.getBigDecimal("grand_total"));
        t.setStatus(rs.getString("status"));
        t.setCatatan(rs.getString("catatan"));
        return t;
    }
}
