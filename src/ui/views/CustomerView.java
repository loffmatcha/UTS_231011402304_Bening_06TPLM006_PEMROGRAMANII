package ui.views;

import dao.CustomerDAO;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Customer;

import java.util.List;

public class CustomerView {
    private VBox view;
    private final CustomerDAO customerDAO = new CustomerDAO();

    private TextField txtNama, txtKontak, txtSearch, txtAlamat;
    private TableView<Customer> table;
    private int selectedId = -1;

    public CustomerView() {
        buildView();
        refresh();
    }

    public VBox getView() { return view; }

    private void buildView() {
        view = new VBox(12);
        view.setPadding(new javafx.geometry.Insets(0, 0, 30, 0));
        
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("form-card");

        Label formTitle = new Label("Form Data Customer");
        formTitle.getStyleClass().add("page-title");
        formTitle.setStyle("-fx-font-size: 18px;");

        GridPane grid = new GridPane();
        grid.setMaxWidth(1000);
        grid.setHgap(16); grid.setVgap(12);

        txtNama = new TextField();
        txtKontak = new TextField();
        txtAlamat = new TextField();

        grid.add(labeledField("Nama Customer", txtNama), 0, 0);
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
        txtSearch.setPromptText("🔍 Cari pelanggan...");
        txtSearch.setMaxWidth(300);
        txtSearch.setOnKeyReleased(e -> doSearch());

        table = new TableView<>();
        table.getStyleClass().add("table-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Customer, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaCustomer"));
        TableColumn<Customer, String> colKontak = new TableColumn<>("Kontak");
        colKontak.setCellValueFactory(new PropertyValueFactory<>("telepon"));
        TableColumn<Customer, String> colAlamat = new TableColumn<>("Alamat");
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        TableColumn<Customer, String> colTgl = new TableColumn<>("Tanggal Daftar");
        colTgl.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        table.getColumns().addAll(colNama, colKontak, colAlamat, colTgl);
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
        Task<List<Customer>> task = new Task<>() {
            @Override protected List<Customer> call() throws Exception { return customerDAO.getAll(); }
            @Override protected void succeeded() { table.getItems().setAll(getValue()); }
        };
        new Thread(task).start();
    }

    private void doSearch() {
        String kw = txtSearch.getText();
        Task<List<Customer>> task = new Task<>() {
            @Override protected List<Customer> call() throws Exception { return customerDAO.search(kw); }
            @Override protected void succeeded() { table.getItems().setAll(getValue()); }
        };
        new Thread(task).start();
    }

    private void fillForm(Customer c) {
        selectedId = c.getIdCustomer();
        txtNama.setText(c.getNamaCustomer());
        txtKontak.setText(c.getTelepon());
        txtAlamat.setText(c.getAlamat());
    }

    private void clearForm() {
        selectedId = -1;
        txtNama.clear(); txtKontak.clear(); txtAlamat.clear();
    }

    private void doTambah() {
        if (txtNama.getText().trim().isEmpty()) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                Customer c = new Customer();
                c.setNamaCustomer(txtNama.getText());
                c.setTelepon(txtKontak.getText());
                c.setAlamat(txtAlamat.getText());
                return customerDAO.insert(c);
            }
            @Override protected void succeeded() { clearForm(); refresh(); }
        };
        new Thread(task).start();
    }

    private void doEdit() {
        if (selectedId < 0 || txtNama.getText().trim().isEmpty()) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                Customer c = new Customer();
                c.setIdCustomer(selectedId);
                c.setNamaCustomer(txtNama.getText());
                c.setTelepon(txtKontak.getText());
                c.setAlamat(txtAlamat.getText());
                return customerDAO.update(c);
            }
            @Override protected void succeeded() { clearForm(); refresh(); }
        };
        new Thread(task).start();
    }

    private void doHapus() {
        if (selectedId < 0) return;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception { return customerDAO.delete(selectedId); }
            @Override protected void succeeded() { clearForm(); refresh(); }
        };
        new Thread(task).start();
    }
}
