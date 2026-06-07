package ui.views;

import dao.TransaksiDAO;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.DetailTransaksi;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class LaporanInventoryView {
    private VBox view;
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    private DatePicker dpStart, dpEnd;
    private TableView<DetailTransaksi> table;

    public LaporanInventoryView() {
        buildView();
    }

    public VBox getView() { return view; }

    private void buildView() {
        view = new VBox(12);
        view.setPadding(new javafx.geometry.Insets(0, 0, 30, 0));
        
        VBox filterCard = new VBox(12);
        filterCard.getStyleClass().add("form-card");

        Label formTitle = new Label("Laporan Produk Terjual (Inventory)");
        formTitle.getStyleClass().add("page-title");
        formTitle.setStyle("-fx-font-size: 18px;");

        HBox filterBox = new HBox(12);
        filterBox.setAlignment(Pos.BOTTOM_LEFT);
        
        dpStart = new DatePicker(LocalDate.now().minusDays(7));
        dpEnd = new DatePicker(LocalDate.now());

        Button btnFilter = new Button("Cari");
        btnFilter.getStyleClass().add("btn-primary");
        btnFilter.setOnAction(e -> doFilter());
        
        Button btnPrint = new Button("🖨 Cetak");
        btnPrint.getStyleClass().add("btn-secondary");
        btnPrint.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setHeaderText(null);
            alert.setContentText("Fitur cetak Laporan Inventory akan segera tersedia!");
            alert.show();
        });

        filterBox.getChildren().addAll(
            labeledField("Dari Tanggal", dpStart), 
            labeledField("Sampai Tanggal", dpEnd), 
            btnFilter, btnPrint
        );
        
        filterCard.getChildren().addAll(formTitle, filterBox);

        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("form-card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        table = new TableView<>();
        table.getStyleClass().add("table-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<DetailTransaksi, String> colKode = new TableColumn<>("Kode Barang");
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeBarang"));
        
        TableColumn<DetailTransaksi, String> colNama = new TableColumn<>("Nama Barang");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        
        TableColumn<DetailTransaksi, Integer> colQty = new TableColumn<>("Total Terjual (Qty)");
        colQty.setCellValueFactory(new PropertyValueFactory<>("jumlah"));

        table.getColumns().addAll(colKode, colNama, colQty);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableCard.getChildren().addAll(table);
        view.getChildren().addAll(filterCard, tableCard);
        
        doFilter();
    }

    private VBox labeledField(String labelText, Control field) {
        VBox box = new VBox(4);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 12px;");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private void doFilter() {
        if (dpStart.getValue() == null || dpEnd.getValue() == null) return;
        Date start = Date.from(dpStart.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(dpEnd.getValue().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        Task<List<DetailTransaksi>> task = new Task<>() {
            @Override protected List<DetailTransaksi> call() throws Exception {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return transaksiDAO.getLaporanInventory(sdf.format(start), sdf.format(end));
            }
            @Override protected void succeeded() {
                table.getItems().setAll(getValue());
            }
        };
        new Thread(task).start();
    }
}
