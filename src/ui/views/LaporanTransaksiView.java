package ui.views;

import dao.TransaksiDAO;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Transaksi;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LaporanTransaksiView {
    private VBox view;
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    private DatePicker dpStart, dpEnd;
    private TableView<Transaksi> table;
    private Label lblTotalOmzet;

    public LaporanTransaksiView() {
        buildView();
    }

    public VBox getView() { return view; }

    private void buildView() {
        view = new VBox(12);
        view.setPadding(new javafx.geometry.Insets(0, 0, 30, 0));
        
        VBox filterCard = new VBox(12);
        filterCard.getStyleClass().add("form-card");

        Label formTitle = new Label("Laporan Transaksi");
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
            alert.setContentText("Fitur cetak Laporan Transaksi akan segera tersedia!");
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

        TableColumn<Transaksi, String> colNo = new TableColumn<>("No. Transaksi");
        colNo.setCellValueFactory(new PropertyValueFactory<>("noTransaksi"));
        
        TableColumn<Transaksi, String> colCust = new TableColumn<>("Customer");
        colCust.setCellValueFactory(new PropertyValueFactory<>("namaCustomer"));
        
        TableColumn<Transaksi, Timestamp> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(new PropertyValueFactory<>("tanggal"));

        TableColumn<Transaksi, BigDecimal> colTotal = new TableColumn<>("Grand Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
        colTotal.setCellFactory(tc -> new TableCell<Transaksi, BigDecimal>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText("Rp " + NumberFormat.getNumberInstance(new Locale("id", "ID")).format(item));
            }
        });

        table.getColumns().addAll(colNo, colCust, colTgl, colTotal);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox summaryBox = new HBox();
        summaryBox.setAlignment(Pos.CENTER_RIGHT);
        lblTotalOmzet = new Label("Total Omzet: Rp 0");
        lblTotalOmzet.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        summaryBox.getChildren().add(lblTotalOmzet);

        tableCard.getChildren().addAll(table, summaryBox);
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

        Task<List<Transaksi>> task = new Task<>() {
            @Override protected List<Transaksi> call() throws Exception {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String d1 = sdf.format(start);
                String d2 = sdf.format(end);
                return transaksiDAO.filter(d1, d2, null, null);
            }
            @Override protected void succeeded() {
                List<Transaksi> list = getValue();
                table.getItems().setAll(list);
                
                BigDecimal sum = BigDecimal.ZERO;
                for (Transaksi t : list) sum = sum.add(t.getGrandTotal());
                lblTotalOmzet.setText("Total Omzet: Rp " + NumberFormat.getNumberInstance(new Locale("id", "ID")).format(sum));
            }
        };
        new Thread(task).start();
    }
}
