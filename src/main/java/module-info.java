module fr.walliang.astronomy.acquisitiondelay {
    requires javafx.controls;
    requires org.apache.logging.log4j;
    // Requis explicitement pour que jlink inclue l'implémentation dans l'image
    requires org.apache.logging.log4j.core;

    // JavaFX instancie Gui (extends Application) par réflexion
    opens fr.walliang.astronomy.acquisitiondelay.ui to javafx.graphics;
}
