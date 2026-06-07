package ui.views;

import dao.BarangDAO;
import dao.CustomerDAO;
import dao.TransaksiDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Transaksi;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DashboardView {
    private VBox view;
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final BarangDAO barangDAO = new BarangDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    
    private Label lblOmzet, lblTrx, lblProduk, lblCustomer;
    private TableView<Transaksi> tableTrx;
    
    public DashboardView() {
        buildView();
        refresh();
    }
    
    public VBox getView() {
        return view;
    }
    
    private void buildView() {
        view = new VBox(12);
        view.setPadding(new javafx.geometry.Insets(0, 0, 30, 0));
        
        // Header
        HBox header = new HBox();
        VBox titleBox = new VBox(4);
        Label title = new Label("Ringkasan Hari Ini");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Pantau performa bisnis TheIsbe secara real-time.");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().add(titleBox);
        
        // Cards
        HBox cardsRow = new HBox(16);
        lblOmzet = new Label("Rp 0");
        lblTrx = new Label("0");
        lblProduk = new Label("0");
        lblCustomer = new Label("0");
        
        cardsRow.getChildren().addAll(
            buildCard("Omzet Hari Ini", lblOmzet),
            buildCard("Total Transaksi", lblTrx),
            buildCard("Total Produk", lblProduk),
            buildCard("Total Customer", lblCustomer)
        );
        for(javafx.scene.Node n : cardsRow.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }
        
        // Table
        Label tblTitle = new Label("10 Transaksi Terakhir");
        tblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2D2D2D;");
        
        tableTrx = new TableView<>();
        tableTrx.getStyleClass().add("table-view");
        VBox.setVgrow(tableTrx, Priority.ALWAYS);
        
        TableColumn<Transaksi, String> colNo = new TableColumn<>("No. Transaksi");
        colNo.setCellValueFactory(new PropertyValueFactory<>("noTransaksi"));
        
        TableColumn<Transaksi, String> colCust = new TableColumn<>("Customer");
        colCust.setCellValueFactory(new PropertyValueFactory<>("namaCustomer"));
        
        TableColumn<Transaksi, String> colPlat = new TableColumn<>("Platform");
        colPlat.setCellValueFactory(new PropertyValueFactory<>("platformJual"));
        
        TableColumn<Transaksi, BigDecimal> colTotal = new TableColumn<>("Grand Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
        colTotal.setCellFactory(tc -> new javafx.scene.control.TableCell<Transaksi, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Rp " + NumberFormat.getNumberInstance(new Locale("id", "ID")).format(item));
                }
            }
        });
        
        tableTrx.getColumns().addAll(colNo, colCust, colPlat, colTotal);
        tableTrx.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        view.getChildren().addAll(header, cardsRow, tblTitle, tableTrx);
    }
    
    private HBox buildCard(String title, Label valueLabel) {
        HBox card = new HBox(16);
        card.getStyleClass().add("summary-card");
        card.setAlignment(Pos.CENTER_LEFT);
        
        Region accent = new Region();
        accent.getStyleClass().add("card-accent");
        
        VBox info = new VBox(4);
        valueLabel.getStyleClass().add("card-value");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-label");
        info.getChildren().addAll(valueLabel, titleLabel);
        
        card.getChildren().addAll(accent, info);
        return card;
    }
    
    public void refresh() {
        Task<Void> task = new Task<>() {
            private BigDecimal omzet;
            private int trx, prod, cust;
            private List<Transaksi> recent;
            
            @Override
            protected Void call() throws Exception {
                omzet = transaksiDAO.omzetHariIni();
                trx = transaksiDAO.getAll().size();
                prod = barangDAO.getAll().size();
                cust = customerDAO.getAll().size();
                recent = transaksiDAO.getLast10();
                return null;
            }
            
            @Override
            protected void succeeded() {
                lblOmzet.setText("Rp " + NumberFormat.getNumberInstance(new Locale("id", "ID")).format(omzet));
                lblTrx.setText(String.valueOf(trx));
                lblProduk.setText(String.valueOf(prod));
                lblCustomer.setText(String.valueOf(cust));
                
                tableTrx.getItems().clear();
                tableTrx.getItems().addAll(recent);
            }
        };
        new Thread(task).start();
    }
}
