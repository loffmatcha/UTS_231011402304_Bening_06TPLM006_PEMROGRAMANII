package ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.util.Duration;
import ui.views.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainController {
    private BorderPane root;
    private VBox sidebar;
    private BorderPane contentWrapper;
    private HBox lastActiveBtn;
    
    private DashboardView dashboardView;

    private Label lblPageTitle;
    private Label lblDateTime;

    public MainController() {
        root = new BorderPane();
        root.getStyleClass().add("root");
        
        buildSidebar();
        buildMainArea();
        
        root.setLeft(sidebar);
        
        startClock();
        
        Platform.runLater(() -> navigateTo("Dashboard"));
    }

    public BorderPane getRoot() {
        return root;
    }

    private void buildSidebar() {
        sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        VBox brand = new VBox(2);
        Label brandName = new Label("TheIsbe");
        brandName.getStyleClass().add("brand-title");
        Label tagline = new Label("Fashion Management");
        tagline.getStyleClass().add("brand-tagline");
        brand.getChildren().addAll(brandName, tagline);
        
        Separator sep = new Separator();
        sep.getStyleClass().add("sidebar-divider");

        sidebar.getChildren().addAll(brand, sep);

        String[][] menus = {
            {"🏠", "Dashboard"},
            {"👗", "Barang"},
            {"👤", "Customer"},
            {"🏭", "Supplier"},
            {"🛍️", "Transaksi"},
            {"📦", "Inventory"},
            {"📊", "Laporan Transaksi"},
            {"📋", "Laporan Inventory"}
        };

        for (String[] menu : menus) {
            sidebar.getChildren().add(buildMenuBtn(menu[0], menu[1]));
        }
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);
        
        Label version = new Label("v2.0.0 — JavaFX");
        version.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px; -fx-padding: 12 20;");
        sidebar.getChildren().add(version);
    }

    private HBox buildMenuBtn(String icon, String title) {
        HBox btn = new HBox(12);
        btn.getStyleClass().add("nav-button");
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setCursor(Cursor.HAND);
        
        Label lblIcon = new Label(icon);
        lblIcon.setStyle("-fx-font-size: 16px;");
        Label lblTitle = new Label(title);
        
        btn.getChildren().addAll(lblIcon, lblTitle);
        
        btn.setOnMouseClicked(e -> {
            if (lastActiveBtn != null) {
                lastActiveBtn.getStyleClass().remove("nav-button-active");
            }
            btn.getStyleClass().add("nav-button-active");
            lastActiveBtn = btn;
            navigateTo(title);
        });
        
        // Auto-select dashboard on load
        if (title.equals("Dashboard")) {
            btn.getStyleClass().add("nav-button-active");
            lastActiveBtn = btn;
        }
        
        return btn;
    }

    private void buildMainArea() {
        BorderPane mainArea = new BorderPane();
        
        // Header
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0; -fx-padding: 16 24;");
        header.setAlignment(Pos.CENTER_LEFT);
        
        lblPageTitle = new Label("Dashboard");
        lblPageTitle.getStyleClass().add("page-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        lblDateTime = new Label();
        lblDateTime.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        
        header.getChildren().addAll(lblPageTitle, spacer, lblDateTime);
        
        contentWrapper = new BorderPane();
        contentWrapper.getStyleClass().add("content-area");
        
        mainArea.setTop(header);
        mainArea.setCenter(contentWrapper);
        
        root.setCenter(mainArea);
    }

    private CustomerView customerView;
    private SupplierView supplierView;
    private TransaksiView transaksiView;
    private InventoryView inventoryView;
    private LaporanTransaksiView laporanTransaksiView;
    private LaporanInventoryView laporanInventoryView;

    private void navigateTo(String page) {
        lblPageTitle.setText(page);
        Pane targetView = null;
        
        switch (page) {
            case "Dashboard":
                if (dashboardView == null) dashboardView = new DashboardView();
                else dashboardView.refresh();
                targetView = dashboardView.getView();
                break;
            case "Barang":
                BarangView barangView = new BarangView();
                targetView = barangView.getView();
                break;
            case "Customer":
                if (customerView == null) customerView = new CustomerView();
                else customerView.refresh();
                targetView = customerView.getView();
                break;
            case "Supplier":
                if (supplierView == null) supplierView = new SupplierView();
                else supplierView.refresh();
                targetView = supplierView.getView();
                break;
            case "Transaksi":
                // Always create new to reset cart state
                transaksiView = new TransaksiView();
                targetView = transaksiView.getView();
                break;
            case "Inventory":
                if (inventoryView == null) inventoryView = new InventoryView();
                else inventoryView.refresh();
                targetView = inventoryView.getView();
                break;
            case "Laporan Transaksi":
                // Always create new to refresh date filtering UI
                laporanTransaksiView = new LaporanTransaksiView();
                targetView = laporanTransaksiView.getView();
                break;
            case "Laporan Inventory":
                laporanInventoryView = new LaporanInventoryView();
                targetView = laporanInventoryView.getView();
                break;
            default:
                VBox placeholder = new VBox(new Label(page + " (Segera Hadir)"));
                placeholder.setAlignment(Pos.CENTER);
                targetView = placeholder;
        }
        
        if (targetView != null) {
            contentWrapper.getChildren().clear();
            contentWrapper.setCenter(targetView);
            
            FadeTransition ft = new FadeTransition(Duration.millis(150), targetView);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            lblDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy  |  HH:mm:ss", new Locale("id", "ID")).format(new Date()));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
}
