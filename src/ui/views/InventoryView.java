package ui.views;

import dao.BarangDAO;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Barang;

import java.util.List;

public class InventoryView {
    private VBox view;
    private final BarangDAO barangDAO = new BarangDAO();

    private TableView<Barang> table;

    public InventoryView() {
        buildView();
        refresh();
    }

    public VBox getView() { return view; }

    private void buildView() {
        view = new VBox(12);
        view.setPadding(new javafx.geometry.Insets(0, 0, 30, 0));
        
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("form-card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        Label formTitle = new Label("Status Stok Barang (Inventory)");
        formTitle.getStyleClass().add("page-title");
        formTitle.setStyle("-fx-font-size: 18px;");

        table = new TableView<>();
        table.getStyleClass().add("table-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Barang, String> colKode = new TableColumn<>("Kode");
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeBarang"));

        TableColumn<Barang, String> colNama = new TableColumn<>("Nama Produk");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));

        TableColumn<Barang, Integer> colStok = new TableColumn<>("Stok Tersedia");
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        
        // Row styling based on stock
        table.setRowFactory(tv -> new TableRow<Barang>() {
            @Override
            protected void updateItem(Barang item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if (item.getStok() == 0) {
                        setStyle("-fx-background-color: #ffc8c8;"); // ROW_DANGER equivalent
                    } else if (item.getStok() < 5) {
                        setStyle("-fx-background-color: #ffffc8;"); // ROW_WARNING equivalent
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        table.getColumns().addAll(colKode, colNama, colStok);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox topBox = new HBox(12);
        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.getStyleClass().add("btn-ghost");
        btnRefresh.setOnAction(e -> refresh());
        topBox.getChildren().addAll(formTitle, new Region(), btnRefresh);
        HBox.setHgrow(topBox.getChildren().get(1), Priority.ALWAYS);

        tableCard.getChildren().addAll(topBox, table);
        view.getChildren().add(tableCard);
    }

    public void refresh() {
        Task<List<Barang>> task = new Task<>() {
            @Override protected List<Barang> call() throws Exception { return barangDAO.getAll(); }
            @Override protected void succeeded() { table.getItems().setAll(getValue()); }
        };
        new Thread(task).start();
    }
}
