/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.*;

/**
 *
 * @author migui
 */
public class FTPUploader {

    private static final int PUERTO = 21;

    private final String usuario, password;
    private final File fichero;
    private final boolean esJugador;
    private final String ip;

    public FTPUploader(File fichero, String usuario, String password, boolean esJugador, String ip) {
        this.fichero = fichero;
        this.usuario = usuario;
        this.password = password;
        this.esJugador = esJugador;
        this.ip = ip;

        try {
            if (!this.fichero.exists()) {
                fichero.createNewFile();
            }
        } catch (IOException ex) {
            Logger.getLogger(FTPUploader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void subirFichero() {
        FTPClient ftp = new FTPClient();

        try {
            ftp.connect(ip, PUERTO);
            ftp.login(usuario, password);
            ftp.enterLocalPassiveMode();
            String ruta = esJugador ? "jugadores/" : "equipos/";
            boolean done = ftp.storeFile(ruta.concat(fichero.getName()), new FileInputStream(fichero));

            if (done) {
                System.out.println("se ha subido correctamente");
            }

            ftp.logout();
            ftp.disconnect();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (IllegalStateException ex) {
            Logger.getLogger(FTPUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
