package ui.views;

import dao.SupplierDAO;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Supplier;

import java.util.List;

public class SupplierView {
    private VBox view;
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private TextField txtNama, txtKontak, txtSearch, txtAlamat;
    private TableView<Supplier> table;
    private int selectedId = -1;

    public SupplierView() {
        buildView();
        refresh();
    }

    public VBox getView() { return view; }

    private void buildView() {
        view = new VBox(12);
        view.setPadding(new javafx.geometry.Insets(0, 0, 30, 0));
        
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("form-card");

        Label formTitle = new Label("Form Data Supplier");
        formTitle.getStyleClass().add("page-title");
        formTitle.setStyle("-fx-font-size: 18px;");

        GridPane grid = new GridPane();
        grid.setMaxWidth(1000);
        grid.setHgap(16); grid.setVgap(12);

        txtNama = new TextField();
        txtKontak = new TextField();
        txtAlamat = new TextField();

        grid.add(labeledField("Nama Supplier", txtNama), 0, 0);
        grid.add(labeledField("Kontak / No HP", txtKontak), 1, 0);
        grid.add(labeledField("Alamat Lengkap", txtAlamat), 0, 1);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc, cc);

        HBox actionBox = new HBox(12);
        actionBox.setPadding(new javafx.geometry.Insets(18, 0, 0, 0));
        Button btnTambah = new Button("+ Tambah"); btnTambah.getStyleClass().add("btn-primary");
        Button btnEdit = new Button("✏ Edit"); btnEdit.getStyleClass().add("btn-secondary");
        Button btnHapus = new Button("🗑 Hapus"); btnHapus.getStyleClass().add("btn-danger");
        Button btnRefresh = new Button("↻ Refresh"); btnRefresh.getStyleClass().add("btn-ghost");

        btnTambah.setOnAction(e -> doTambah());
        btnEdit.setOnAction(e -> doEdit());
        btnHapus.setOnAction(e -> doHapus());
        btnRefresh.setOnAction(e -> { clearForm(); refresh(); });

        actionBox.getChildren().addAll(btnTambah, btnEdit, btnHapus, btnRefresh);
        grid.add(actionBox, 1, 1);
        formCard.getChildren().addAll(formTitle, grid);

        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("form-card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Cari supplier...");
        txtSearch.setMaxWidth(300);
        txtSearch.setOnKeyReleased(e -> doSearch());

        table = new TableView<>();
        table.getStyleClass().add("table-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Supplier, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaSupplier"));
        TableColumn<Supplier, String> colKontak = new TableColumn<>("Kontak");
        colKontak.setCellValueFactory(new PropertyValueFactory<>("telepon"));
        TableColumn<Supplier, String> colAlamat = new TableColumn<>("Alamat");
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));

        table.getColumns().addAll(colNama, colKontak, colAlamat);
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
        Task<List<Supplier>> task = new Task<>() {
            @Override protected List<Supplier> call() throws Exception { return supplierDAO.getAll(); }
            @Override protected void succeeded() { table.getItems().setAll(getValue()); }
        };
        new Thread(task).start();
    }

    private void doSearch() {
        String kw = txtSearch.getText();
        Task<List<Supplier>> task = new Task<>() {
            @Override protected List<Supplier> call() throws Exception { return supplierDAO.search(kw); }
            @Override protected void succeeded() { table.getItems().setAll(getValue()); }
        };
        new Thread(task).start();
    }

    private void fillForm(Supplier s) {
        selectedId = s.getIdSupplier();
        txtNama.setText(s.getNamaSupplier());
        txtKontak.setText(s.getTelepon());
        txtAlamat.setText(s.getAlamat());
    }

    private void clearForm() {
        selectedId = -1;
        txtNama.clear(); txtKontak.clear(); txtAlamat.clear();
    }

    private void doTambah() {
        if (txtNama.getText().trim().isEmpty()) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                Supplier s = new Supplier();
                s.setNamaSupplier(txtNama.getText());
                s.setTelepon(txtKontak.getText());
                s.setAlamat(txtAlamat.getText());
                return supplierDAO.insert(s);
            }
            @Override protected void succeeded() { clearForm(); refresh(); }
        };
        new Thread(task).start();
    }

    private void doEdit() {
        if (selectedId < 0 || txtNama.getText().trim().isEmpty()) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                Supplier s = new Supplier();
                s.setIdSupplier(selectedId);
                s.setNamaSupplier(txtNama.getText());
                s.setTelepon(txtKontak.getText());
                s.setAlamat(txtAlamat.getText());
                return supplierDAO.update(s);
            }
            @Override protected void succeeded() { clearForm(); refresh(); }
        };
        new Thread(task).start();
    }

    private void doHapus() {
        if (selectedId < 0) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception { return supplierDAO.delete(selectedId); }
            @Override protected void succeeded() { clearForm(); refresh(); }
        };
        new Thread(task).start();
    }
}
