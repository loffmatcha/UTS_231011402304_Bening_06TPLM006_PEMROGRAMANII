package main;

public class Launcher {
    public static void main(String[] args) {
        // Trik ini digunakan agar JavaFX bisa berjalan via Classpath 
        // tanpa memerlukan konfigurasi --module-path di VM Options.
        MainApp.main(args);
    }
}
