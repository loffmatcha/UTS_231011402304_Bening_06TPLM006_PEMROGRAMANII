package ui.views;

import dao.BarangDAO;
import dao.CustomerDAO;
import dao.TransaksiDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Barang;
import model.Customer;
import model.DetailTransaksi;
import model.Transaksi;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransaksiView {
    private VBox view;
    private final BarangDAO barangDAO = new BarangDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    // Header
    private ComboBox<CustomerWrapper> cmbCustomer;
    private ComboBox<String> cmbPlatform;
    private DatePicker dpTanggal;

    // Cart Input
    private ComboBox<BarangWrapper> cmbBarang;
    private TextField txtHarga, txtQty, txtDiskonItem;
    private Button btnTambahCart;

    // Cart Table
    private TableView<DetailTransaksi> tableCart;
    private List<DetailTransaksi> cartList = new ArrayList<>();

    // Summary
    private Label lblSubtotal, lblGrandTotal;
    private TextField txtDiskonGlobal;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;

    public TransaksiView() {
        buildView();
        refreshData();
    }

    public VBox getView() { return view; }

    private void buildView() {
        view = new VBox(12);
        view.setPadding(new javafx.geometry.Insets(0, 0, 30, 0));

        // -- HEADER TRANSAKSI --
        VBox headerCard = new VBox(12);
        headerCard.getStyleClass().add("form-card");
        Label lblHeader = new Label("Header Transaksi");
        lblHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        GridPane gridHeader = new GridPane();
        gridHeader.setMaxWidth(800);
        gridHeader.setHgap(16); gridHeader.setVgap(12);

        cmbCustomer = new ComboBox<>();
        cmbCustomer.setMaxWidth(Double.MAX_VALUE);
        
        cmbPlatform = new ComboBox<>(FXCollections.observableArrayList("Offline", "Shopee", "Tokopedia", "Tiktok", "Lainnya"));
        cmbPlatform.getSelectionModel().selectFirst();
        cmbPlatform.setMaxWidth(Double.MAX_VALUE);
        
        dpTanggal = new DatePicker(LocalDate.now());
        dpTanggal.setMaxWidth(Double.MAX_VALUE);

        gridHeader.add(labeledField("Pilih Customer", cmbCustomer), 0, 0);
        gridHeader.add(labeledField("Platform Penjualan", cmbPlatform), 1, 0);
        gridHeader.add(labeledField("Tanggal Transaksi", dpTanggal), 2, 0);
        
        for(int i=0; i<3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            gridHeader.getColumnConstraints().add(cc);
        }
        headerCard.getChildren().addAll(lblHeader, gridHeader);

        // -- CART INPUT --
        VBox inputCard = new VBox(12);
        inputCard.getStyleClass().add("form-card");
        Label lblInput = new Label("Tambah Produk ke Keranjang");
        lblInput.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        GridPane gridInput = new GridPane();
        gridInput.setMaxWidth(1000);
        gridInput.setHgap(16); gridInput.setVgap(12);

        cmbBarang = new ComboBox<>();
        cmbBarang.setMaxWidth(Double.MAX_VALUE);
        cmbBarang.setOnAction(e -> {
            BarangWrapper bw = cmbBarang.getValue();
            if (bw != null && bw.barang != null) {
                txtHarga.setText(bw.barang.getHargaJual().toPlainString());
            }
        });

        txtHarga = new TextField(); txtHarga.setEditable(false);
        txtQty = new TextField("1");
        txtDiskonItem = new TextField("0");
        
        btnTambahCart = new Button("Tambah ke Keranjang");
        btnTambahCart.getStyleClass().add("btn-primary");
        btnTambahCart.setMaxWidth(Double.MAX_VALUE);
        HBox actionBox = new HBox(btnTambahCart);
        HBox.setHgrow(btnTambahCart, Priority.ALWAYS);
        actionBox.setPadding(new javafx.geometry.Insets(18, 0, 0, 0));
        btnTambahCart.setOnAction(e -> addToCart());

        gridInput.add(labeledField("Pilih Barang (Stok)", cmbBarang), 0, 0);
        gridInput.add(labeledField("Harga Satuan", txtHarga), 1, 0);
        gridInput.add(labeledField("Qty", txtQty), 2, 0);
        gridInput.add(labeledField("Diskon Item (Rp)", txtDiskonItem), 3, 0);
        gridInput.add(actionBox, 4, 0);
        
        ColumnConstraints cc30 = new ColumnConstraints(); cc30.setPercentWidth(30);
        ColumnConstraints cc20 = new ColumnConstraints(); cc20.setPercentWidth(20);
        ColumnConstraints cc15 = new ColumnConstraints(); cc15.setPercentWidth(15);
        gridInput.getColumnConstraints().addAll(cc30, cc20, cc15, cc15, cc20);
        
        inputCard.getChildren().addAll(lblInput, gridInput);

        // -- CART TABLE --
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("form-card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        tableCart = new TableView<>();
        tableCart.getStyleClass().add("table-view");
        VBox.setVgrow(tableCart, Priority.ALWAYS);

        TableColumn<DetailTransaksi, String> colNama = new TableColumn<>("Nama Barang");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));

        TableColumn<DetailTransaksi, String> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(tc -> new SimpleStringProperty(formatRp(tc.getValue().getHargaSatuan())));

        TableColumn<DetailTransaksi, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(new PropertyValueFactory<>("jumlah"));



        TableColumn<DetailTransaksi, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(tc -> new SimpleStringProperty(formatRp(tc.getValue().getSubtotal())));

        TableColumn<DetailTransaksi, Void> colAction = new TableColumn<>("Aksi");
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Hapus");
            {
                btn.getStyleClass().add("btn-danger");
                btn.setOnAction(e -> {
                    DetailTransaksi dt = getTableView().getItems().get(getIndex());
                    cartList.remove(dt);
                    updateCartTable();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btn);
            }
        });

        tableCart.getColumns().addAll(colNama, colHarga, colQty, colSub, colAction);
        tableCart.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // -- SUMMARY --
        HBox summaryBox = new HBox(16);
        summaryBox.setAlignment(Pos.CENTER_RIGHT);

        VBox boxLabels = new VBox(8);
        boxLabels.setAlignment(Pos.CENTER_RIGHT);
        
        lblSubtotal = new Label("Subtotal: Rp 0");
        lblSubtotal.setStyle("-fx-font-weight: bold;");
        
        HBox diskonGlobalBox = new HBox(8);
        diskonGlobalBox.setAlignment(Pos.CENTER_RIGHT);
        diskonGlobalBox.getChildren().addAll(new Label("Diskon Global (Rp): "), txtDiskonGlobal = new TextField("0"));
        txtDiskonGlobal.setMaxWidth(100);
        txtDiskonGlobal.setOnKeyReleased(e -> calculateTotal());
        
        lblGrandTotal = new Label("Grand Total: Rp 0");
        lblGrandTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #c0392b;");
        
        Button btnSimpan = new Button("Simpan Transaksi");
        btnSimpan.getStyleClass().add("btn-primary");
        btnSimpan.setStyle("-fx-font-size: 16px; -fx-padding: 10 24;");
        btnSimpan.setOnAction(e -> simpanTransaksi());

        boxLabels.getChildren().addAll(lblSubtotal, diskonGlobalBox, lblGrandTotal);
        summaryBox.getChildren().addAll(boxLabels, btnSimpan);

        tableCard.getChildren().addAll(tableCart, summaryBox);
        view.getChildren().addAll(headerCard, inputCard, tableCard);
    }

    private VBox labeledField(String labelText, Control field) {
        VBox box = new VBox(4);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 12px;");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private String formatRp(BigDecimal val) {
        if (val == null) return "Rp 0";
        return "Rp " + NumberFormat.getNumberInstance(new Locale("id", "ID")).format(val);
    }

    private void refreshData() {
        Task<Void> task = new Task<>() {
            List<Customer> cList;
            List<Barang> bList;
            @Override protected Void call() throws Exception {
                cList = customerDAO.getAll();
                bList = barangDAO.getAll();
                return null;
            }
            @Override protected void succeeded() {
                cmbCustomer.getItems().clear();
                cmbCustomer.getItems().add(new CustomerWrapper(null, "-- Pilih Customer --"));
                for (Customer c : cList) cmbCustomer.getItems().add(new CustomerWrapper(c, c.getNamaCustomer()));
                cmbCustomer.getSelectionModel().selectFirst();

                cmbBarang.getItems().clear();
                for (Barang b : bList) {
                    cmbBarang.getItems().add(new BarangWrapper(b, b.getKodeBarang() + " - " + b.getNamaBarang() + " (Stok: " + b.getStok() + ")"));
                }
            }
        };
        new Thread(task).start();
    }

    private void addToCart() {
        BarangWrapper bw = cmbBarang.getValue();
        if (bw == null || bw.barang == null) { showAlert("Pilih barang terlebih dahulu!"); return; }
        try {
            int qty = Integer.parseInt(txtQty.getText().trim());
            if (qty <= 0) { showAlert("Qty minimal 1!"); return; }
            if (qty > bw.barang.getStok()) { showAlert("Stok tidak mencukupi!"); return; }
            
            BigDecimal sub = bw.barang.getHargaJual().multiply(new BigDecimal(qty));
            
            if (sub.compareTo(BigDecimal.ZERO) < 0) { showAlert("Diskon melebihi harga!"); return; }

            DetailTransaksi dt = new DetailTransaksi();
            dt.setIdBarang(bw.barang.getIdBarang());
            dt.setKodeBarang(bw.barang.getKodeBarang());
            dt.setNamaBarang(bw.barang.getNamaBarang());
            dt.setJumlah(qty);
            dt.setHargaSatuan(bw.barang.getHargaJual());
            dt.setSubtotal(sub);
            
            cartList.add(dt);
            updateCartTable();
            
            cmbBarang.getSelectionModel().clearSelection();
            txtHarga.clear(); txtQty.setText("1"); txtDiskonItem.setText("0");
        } catch (Exception e) {
            showAlert("Input qty/diskon tidak valid!");
        }
    }

    private void updateCartTable() {
        tableCart.getItems().setAll(cartList);
        calculateTotal();
    }

    private void calculateTotal() {
        subtotal = BigDecimal.ZERO;
        for (DetailTransaksi dt : cartList) subtotal = subtotal.add(dt.getSubtotal());
        
        BigDecimal diskonGlobal = BigDecimal.ZERO;
        try { diskonGlobal = new BigDecimal(txtDiskonGlobal.getText().trim()); } catch (Exception ignored) {}
        
        grandTotal = subtotal.subtract(diskonGlobal);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) grandTotal = BigDecimal.ZERO;
        
        lblSubtotal.setText("Subtotal: " + formatRp(subtotal));
        lblGrandTotal.setText("Grand Total: " + formatRp(grandTotal));
    }

    private void simpanTransaksi() {
        CustomerWrapper cw = cmbCustomer.getValue();
        if (cw == null || cw.customer == null) { showAlert("Pilih customer!"); return; }
        if (cartList.isEmpty()) { showAlert("Keranjang masih kosong!"); return; }
        if (dpTanggal.getValue() == null) { showAlert("Pilih tanggal!"); return; }

        Transaksi t = new Transaksi();
        t.setIdCustomer(cw.customer.getIdCustomer());
        t.setPlatformJual(cmbPlatform.getValue());
        
        Timestamp ts = new Timestamp(Date.from(dpTanggal.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
        t.setTanggal(ts);
        
        t.setTotalHarga(subtotal);
        try { t.setDiskon(new BigDecimal(txtDiskonGlobal.getText().trim())); } 
        catch (Exception e) { t.setDiskon(BigDecimal.ZERO); }
        t.setGrandTotal(grandTotal);

        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                t.setNoTransaksi(transaksiDAO.generateNoTransaksi());
                return transaksiDAO.insertTransaksi(t, cartList);
            }
            @Override protected void succeeded() {
                if (getValue()) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Sukses");
                    a.setHeaderText(null);
                    a.setContentText("Transaksi berhasil disimpan!\nNo. Transaksi: " + t.getNoTransaksi());
                    a.show();
                    
                    cartList.clear();
                    updateCartTable();
                    refreshData();
                } else {
                    showAlert("Gagal menyimpan transaksi!");
                }
            }
        };
        new Thread(task).start();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private static class CustomerWrapper {
        Customer customer;
        String display;
        CustomerWrapper(Customer c, String d) { this.customer = c; this.display = d; }
        @Override public String toString() { return display; }
    }

    private static class BarangWrapper {
        Barang barang;
        String display;
        BarangWrapper(Barang b, String d) { this.barang = b; this.display = d; }
        @Override public String toString() { return display; }
    }
}
