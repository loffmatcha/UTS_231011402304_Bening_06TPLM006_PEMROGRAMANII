package ui.views;

import dao.BarangDAO;
import dao.SupplierDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Barang;
import model.Supplier;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BarangView {
    private VBox view;
    private final BarangDAO barangDAO = new BarangDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    // Form fields
    private TextField txtKode, txtNama, txtWarna, txtStok, txtHargaBeli, txtHargaJual, txtSearch;
    private ComboBox<String> cmbKategori, cmbUkuran;
    private ComboBox<SupplierWrapper> cmbSupplier;

    // Table
    private TableView<Barang> table;

    // State
    private int selectedId = -1;

    public BarangView() {
        buildView();
        refresh();
    }

    public VBox getView() {
        return view;
    }

    private void buildView() {
        view = new VBox(12);
        view.setPadding(new javafx.geometry.Insets(0, 0, 30, 0));
        
        // --- FORM AREA ---
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("form-card");

        Label formTitle = new Label("Form Data Produk");
        formTitle.getStyleClass().add("page-title");
        formTitle.setStyle("-fx-font-size: 18px;");

        GridPane grid = new GridPane();
        grid.setMaxWidth(1000);
        grid.setHgap(16);
        grid.setVgap(12);

        txtKode = new TextField(); txtKode.setEditable(false);
        txtNama = new TextField();
        txtWarna = new TextField();
        txtStok = new TextField();
        txtHargaBeli = new TextField();
        txtHargaJual = new TextField();

        cmbKategori = new ComboBox<>(FXCollections.observableArrayList("Outer", "Cardigan", "Blazer", "Kimono", "Lainnya"));
        cmbKategori.setMaxWidth(Double.MAX_VALUE);
        
        cmbUkuran = new ComboBox<>(FXCollections.observableArrayList("S", "M", "L", "XL", "All Size"));
        cmbUkuran.setMaxWidth(Double.MAX_VALUE);
        
        cmbSupplier = new ComboBox<>();
        cmbSupplier.setMaxWidth(Double.MAX_VALUE);

        grid.add(labeledField("Kode Barang", txtKode), 0, 0);
        grid.add(labeledField("Nama Barang", txtNama), 1, 0);
        grid.add(labeledField("Kategori", cmbKategori), 2, 0);
        grid.add(labeledField("Ukuran", cmbUkuran), 3, 0);
        
        grid.add(labeledField("Warna", txtWarna), 0, 1);
        grid.add(labeledField("Harga Beli", txtHargaBeli), 1, 1);
        grid.add(labeledField("Harga Jual", txtHargaJual), 2, 1);
        grid.add(labeledField("Stok Awal", txtStok), 3, 1);
        
        grid.add(labeledField("Supplier", cmbSupplier), 0, 2, 2, 1);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            grid.getColumnConstraints().add(cc);
        }

        // Action Buttons
        HBox actionBox = new HBox(12);
        actionBox.setPadding(new javafx.geometry.Insets(18, 0, 0, 0));
        Button btnTambah = new Button("+ Tambah");
        btnTambah.getStyleClass().add("btn-primary");
        btnTambah.setOnAction(e -> doTambah());

        Button btnEdit = new Button("✏ Edit");
        btnEdit.getStyleClass().add("btn-secondary");
        btnEdit.setOnAction(e -> doEdit());

        Button btnHapus = new Button("🗑 Hapus");
        btnHapus.getStyleClass().add("btn-danger");
        btnHapus.setOnAction(e -> doHapus());

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-ghost");
        btnRefresh.setOnAction(e -> { clearForm(); refresh(); });

        actionBox.getChildren().addAll(btnTambah, btnEdit, btnHapus, btnRefresh);
        grid.add(actionBox, 2, 2, 2, 1);

        formCard.getChildren().addAll(formTitle, grid);

        // --- TABLE AREA ---
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("form-card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Cari nama atau kode produk...");
        txtSearch.setMaxWidth(300);
        txtSearch.setOnKeyReleased(e -> doSearch());

        table = new TableView<>();
        table.getStyleClass().add("table-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Barang, String> colKode = new TableColumn<>("Kode");
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeBarang"));

        TableColumn<Barang, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));

        TableColumn<Barang, String> colKat = new TableColumn<>("Kategori");
        colKat.setCellValueFactory(new PropertyValueFactory<>("kategori"));

        TableColumn<Barang, String> colUkuran = new TableColumn<>("Ukuran");
        colUkuran.setCellValueFactory(new PropertyValueFactory<>("ukuran"));

        TableColumn<Barang, String> colWarna = new TableColumn<>("Warna");
        colWarna.setCellValueFactory(new PropertyValueFactory<>("warna"));

        TableColumn<Barang, Integer> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));

        TableColumn<Barang, BigDecimal> colHarga = new TableColumn<>("Harga Jual");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaJual"));
        colHarga.setCellFactory(tc -> new TableCell<Barang, BigDecimal>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText("Rp " + NumberFormat.getNumberInstance(new Locale("id", "ID")).format(item));
            }
        });

        table.getColumns().addAll(colKode, colNama, colKat, colUkuran, colWarna, colStok, colHarga);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) fillForm(newSel);
        });

        tableCard.getChildren().addAll(txtSearch, table);
        view.getChildren().addAll(formCard, tableCard);
    }

    private VBox labeledField(String labelText, Control field) {
        VBox box = new VBox(4);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 12px;");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    public void refresh() {
        Task<Void> task = new Task<>() {
            private List<Barang> bList;
            private List<Supplier> sList;
            private String newKode;

            @Override protected Void call() throws Exception {
                bList = barangDAO.getAll();
                sList = supplierDAO.getAll();
                newKode = barangDAO.generateKode();
                return null;
            }

            @Override protected void succeeded() {
                table.getItems().setAll(bList);
                cmbSupplier.getItems().clear();
                cmbSupplier.getItems().add(new SupplierWrapper(null, "-- Tanpa Supplier --"));
                for (Supplier s : sList) {
                    cmbSupplier.getItems().add(new SupplierWrapper(s, s.getNamaSupplier()));
                }
                cmbSupplier.getSelectionModel().selectFirst();
                txtKode.setText(newKode);
            }
        };
        new Thread(task).start();
    }

    private void doSearch() {
        String kw = txtSearch.getText();
        Task<List<Barang>> task = new Task<>() {
            @Override protected List<Barang> call() throws Exception {
                return barangDAO.search(kw);
            }
            @Override protected void succeeded() {
                table.getItems().setAll(getValue());
            }
        };
        new Thread(task).start();
    }

    private void fillForm(Barang b) {
        selectedId = b.getIdBarang();
        txtKode.setText(b.getKodeBarang());
        txtNama.setText(b.getNamaBarang());
        cmbKategori.setValue(b.getKategori());
        cmbUkuran.setValue(b.getUkuran());
        txtWarna.setText(b.getWarna());
        txtStok.setText(String.valueOf(b.getStok()));
        txtHargaBeli.setText(b.getHargaBeli() != null ? b.getHargaBeli().toPlainString() : "");
        txtHargaJual.setText(b.getHargaJual() != null ? b.getHargaJual().toPlainString() : "");
        
        cmbSupplier.getSelectionModel().selectFirst();
        if (b.getIdSupplier() > 0) {
            for (SupplierWrapper sw : cmbSupplier.getItems()) {
                if (sw.supplier != null && sw.supplier.getIdSupplier() == b.getIdSupplier()) {
                    cmbSupplier.getSelectionModel().select(sw);
                    break;
                }
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtNama.clear();
        cmbKategori.getSelectionModel().clearSelection();
        cmbUkuran.getSelectionModel().clearSelection();
        txtWarna.clear();
        txtStok.clear();
        txtHargaBeli.clear();
        txtHargaJual.clear();
        cmbSupplier.getSelectionModel().selectFirst();
    }

    private boolean validateForm() {
        if (txtNama.getText().trim().isEmpty()) {
            showAlert("Validasi", "Nama barang tidak boleh kosong.");
            return false;
        }
        try { Integer.parseInt(txtStok.getText().trim()); } 
        catch (NumberFormatException e) { showAlert("Validasi", "Stok harus berupa angka."); return false; }
        
        try { new BigDecimal(txtHargaBeli.getText().trim()); } 
        catch (Exception e) { showAlert("Validasi", "Harga beli tidak valid."); return false; }
        
        try { new BigDecimal(txtHargaJual.getText().trim()); } 
        catch (Exception e) { showAlert("Validasi", "Harga jual tidak valid."); return false; }
        
        return true;
    }

    private Barang formToBarang() {
        Barang b = new Barang();
        b.setNamaBarang(txtNama.getText().trim());
        b.setKategori(cmbKategori.getValue());
        b.setUkuran(cmbUkuran.getValue());
        b.setWarna(txtWarna.getText().trim());
        b.setStok(Integer.parseInt(txtStok.getText().trim()));
        b.setHargaBeli(new BigDecimal(txtHargaBeli.getText().trim()));
        b.setHargaJual(new BigDecimal(txtHargaJual.getText().trim()));
        
        SupplierWrapper sw = cmbSupplier.getValue();
        if (sw != null && sw.supplier != null) {
            b.setIdSupplier(sw.supplier.getIdSupplier());
        }
        return b;
    }

    private void doTambah() {
        if (!validateForm()) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                Barang b = formToBarang();
                b.setKodeBarang(barangDAO.generateKode());
                return barangDAO.insert(b);
            }
            @Override protected void succeeded() {
                if (getValue()) {
                    clearForm(); refresh();
                    showAlert("Sukses", "Produk berhasil ditambahkan! ✅");
                }
            }
        };
        new Thread(task).start();
    }

    private void doEdit() {
        if (selectedId < 0) { showAlert("Peringatan", "Pilih produk yang ingin diedit."); return; }
        if (!validateForm()) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                Barang b = formToBarang();
                b.setIdBarang(selectedId);
                b.setKodeBarang(txtKode.getText());
                return barangDAO.update(b);
            }
            @Override protected void succeeded() {
                if (getValue()) {
                    clearForm(); refresh();
                    showAlert("Sukses", "Produk berhasil diupdate! ✅");
                }
            }
        };
        new Thread(task).start();
    }

    private void doHapus() {
        if (selectedId < 0) { showAlert("Peringatan", "Pilih produk yang ingin dihapus."); return; }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.setContentText("Yakin ingin menghapus produk ini?");
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                Task<Boolean> task = new Task<>() {
                    @Override protected Boolean call() throws Exception {
                        return barangDAO.delete(selectedId);
                    }
                    @Override protected void succeeded() {
                        if (getValue()) {
                            clearForm(); refresh();
                        } else {
                            showAlert("Gagal", "Gagal menghapus produk. Pastikan tidak ada transaksi terkait.");
                        }
                    }
                };
                new Thread(task).start();
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    // Helper class untuk ComboBox Supplier
    private static class SupplierWrapper {
        Supplier supplier;
        String displayText;
        SupplierWrapper(Supplier s, String d) { this.supplier = s; this.displayText = d; }
        @Override public String toString() { return displayText; }
    }
}
