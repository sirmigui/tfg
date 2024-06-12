package tfg;

import java.sql.*;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author migui
 */
public class VentanaEquipos extends javax.swing.JDialog {

    private final ArrayList<String> nombresCards;
    private final CardLayout card;

    private final DefaultComboBoxModel<Equipo> modeloCombos;
    private final VentanaPrincipal vp;

    private File fileImgEscudo;
    private int índice;

    private VisualizadorImágenes vi;

    public VentanaEquipos(int índiceInicial, VentanaPrincipal vp) {
        initComponents();

        this.vi = null;
        this.vp = vp;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                vp.closeVentanaEquipos();
            }
        });

        índice = índiceInicial;
        nombresCards = vp.getNombresCards();

        modeloCombos = new DefaultComboBoxModel<>();
        comboModificar.setModel(modeloCombos);
        comboEliminar.setModel(modeloCombos);

        card = (CardLayout) panelPrincipal.getLayout();
        cambiarPanel();
        actualizarEstadoCombosEquipos();
    }

    private void actualizarEstadoCombosEquipos() {
        modeloCombos.removeAllElements();
        modeloCombos.addAll(vp.getEquipos());
    }

    private File abrirFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        int resultado = fileChooser.showOpenDialog(this);
        return resultado != JFileChooser.CANCEL_OPTION ? fileChooser.getSelectedFile() : null;
    }

    private void obtenerValorNombres(boolean sumar) {
        if (sumar && índice == nombresCards.size() - 1 || !sumar && índice == 0) {
            return;
        }

        índice += sumar ? 1 : -1;
        cambiarPanel();
    }

    private void cambiarPanel() {
        String nombre = nombresCards.get(índice);
        card.show(panelPrincipal, nombre);
        lblAcción.setText((nombre.concat(" equipos")).toUpperCase());
    }

    private String getImg() {
        if (this.fileImgEscudo == null) {
            JOptionPane.showMessageDialog(this, "El archivo debe ser un webp.", "Archivo inválido", JOptionPane.WARNING_MESSAGE);
            return "";
        }
        return this.fileImgEscudo.getName();
    }

    private void insertarEquipo(Equipo e) {
        try {
            PreparedStatement ps = vp.getConn().prepareStatement("insert into equipos (nombre, ciudad, url_escudo, dia_fundacion) values (?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getCiudad());
            ps.setString(3, "equipos/".concat(getImg()));
            ps.setTimestamp(4, new java.sql.Timestamp(e.getFechaFundado().getTime()));

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();

            while (rs.next()) {
                e.setId(rs.getInt(1));
            }

        }
        catch (SQLException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }

        FTPUploader ftp = new FTPUploader(fileImgEscudo, vp.getUsuarioFTP(), vp.getPassFTP(), false, vp.getIp());
        ftp.subirFichero();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panelSuperior = new javax.swing.JPanel();
        btnAnterior = new javax.swing.JButton();
        btnSiguiente = new javax.swing.JButton();
        lblAcción = new javax.swing.JLabel();
        panelPrincipal = new javax.swing.JPanel();
        panelAñadir = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtNombreAñadir = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtCiudadAñadir = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        btnAbrirAñadir = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        btnAñadir = new javax.swing.JButton();
        dateFechaFundadoAñadir = new com.toedter.calendar.JDateChooser();
        panelModificar = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        txtNombreModificar = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtCiudadModificar = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        btnAbrirModificar = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        comboModificar = new javax.swing.JComboBox<>();
        btnModificar = new javax.swing.JButton();
        dateFechaFundadoModificar = new com.toedter.calendar.JDateChooser();
        panelEliminar = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        comboEliminar = new javax.swing.JComboBox<>();
        btnEliminar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Gestión equipos");

        panelSuperior.setLayout(new java.awt.GridBagLayout());

        btnAnterior.setText("◀");
        btnAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnteriorActionPerformed(evt);
            }
        });
        panelSuperior.add(btnAnterior, new java.awt.GridBagConstraints());

        btnSiguiente.setText("▶");
        btnSiguiente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSiguienteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        panelSuperior.add(btnSiguiente, gridBagConstraints);

        lblAcción.setText("AÑADIR EQUIPOS");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelSuperior.add(lblAcción, gridBagConstraints);

        getContentPane().add(panelSuperior, java.awt.BorderLayout.PAGE_START);

        panelPrincipal.setLayout(new java.awt.CardLayout());

        panelAñadir.setBackground(new java.awt.Color(204, 255, 204));
        panelAñadir.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Nombre:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(txtNombreAñadir, gridBagConstraints);

        jLabel3.setText("Ciudad:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(txtCiudadAñadir, gridBagConstraints);

        jLabel4.setText("Escudo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(jLabel4, gridBagConstraints);

        btnAbrirAñadir.setText("Abrir...");
        btnAbrirAñadir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirAñadirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(btnAbrirAñadir, gridBagConstraints);

        jLabel5.setText("Fecha fundado:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(jLabel5, gridBagConstraints);

        btnAñadir.setText("Añadir equipo");
        btnAñadir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAñadirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(btnAñadir, gridBagConstraints);

        dateFechaFundadoAñadir.setDateFormatString("dd/MM/yyyy HH:mm");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(dateFechaFundadoAñadir, gridBagConstraints);

        panelPrincipal.add(panelAñadir, "añadir");

        panelModificar.setBackground(new java.awt.Color(153, 204, 255));
        panelModificar.setLayout(new java.awt.GridBagLayout());

        jLabel10.setText("Nombre:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel10, gridBagConstraints);

        txtNombreModificar.setEditable(false);
        txtNombreModificar.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(txtNombreModificar, gridBagConstraints);

        jLabel11.setText("Ciudad:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel11, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(txtCiudadModificar, gridBagConstraints);

        jLabel12.setText("Escudo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel12, gridBagConstraints);

        btnAbrirModificar.setText("Abrir...");
        btnAbrirModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirModificarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(btnAbrirModificar, gridBagConstraints);

        jLabel13.setText("Fecha fundado:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel13, gridBagConstraints);

        jLabel1.setText("Equipos:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel1, gridBagConstraints);

        comboModificar.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboModificarItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(comboModificar, gridBagConstraints);

        btnModificar.setText("Modificar equipo");
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(btnModificar, gridBagConstraints);

        dateFechaFundadoModificar.setDateFormatString("dd/MM/yyyy HH:mm");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(dateFechaFundadoModificar, gridBagConstraints);

        panelPrincipal.add(panelModificar, "modificar");

        panelEliminar.setBackground(new java.awt.Color(255, 204, 204));

        jLabel6.setText("Equipos:");
        panelEliminar.add(jLabel6);

        panelEliminar.add(comboEliminar);

        btnEliminar.setText("Eliminar equipo");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });
        panelEliminar.add(btnEliminar);

        panelPrincipal.add(panelEliminar, "eliminar");

        getContentPane().add(panelPrincipal, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnteriorActionPerformed
        obtenerValorNombres(false);
    }//GEN-LAST:event_btnAnteriorActionPerformed

    private void btnSiguienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSiguienteActionPerformed
        obtenerValorNombres(true);
    }//GEN-LAST:event_btnSiguienteActionPerformed

    private void btnAñadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAñadirActionPerformed
        if (fileImgEscudo == null || !fileImgEscudo.getAbsolutePath().endsWith(".webp")) {
            JOptionPane.showMessageDialog(this, "El archivo debe ser un webp.", "Archivo inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (dateFechaFundadoAñadir.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Error en la fecha.", "Fecha inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = txtNombreAñadir.getText();
        String ciudad = txtCiudadAñadir.getText();

        if (nombre == null || nombre.isEmpty() || nombre.length() > 50) {
            JOptionPane.showMessageDialog(this, "Error en el nombre.", "Nombre inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ciudad == null || ciudad.isEmpty() || ciudad.length() > 40) {
            JOptionPane.showMessageDialog(this, "Error en la ciudad.", "Ciudad inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Equipo e = new Equipo(nombre, ciudad, getImg(), dateFechaFundadoAñadir.getDate());

        this.insertarEquipo(e);
        vp.addEquipo(e);
        actualizarEstadoCombosEquipos();
    }//GEN-LAST:event_btnAñadirActionPerformed

    private void btnAbrirModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirModificarActionPerformed
        fileImgEscudo = abrirFileChooser();

        if (vi == null || !vi.isShowing()) {
            vi = new VisualizadorImágenes(fileImgEscudo);
        }

    }//GEN-LAST:event_btnAbrirModificarActionPerformed

    private void btnAbrirAñadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirAñadirActionPerformed
        fileImgEscudo = abrirFileChooser();

        if (vi == null || !vi.isShowing()) {
            vi = new VisualizadorImágenes(fileImgEscudo);
        }
    }//GEN-LAST:event_btnAbrirAñadirActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        Equipo e = (Equipo) comboModificar.getSelectedItem();
        int índiceEquipo = vp.getEquipos().indexOf(e);

        if (fileImgEscudo == null || !fileImgEscudo.getAbsolutePath().endsWith(".webp")) {
            JOptionPane.showMessageDialog(this, "El archivo debe ser un webp.", "Archivo inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date fechaFundado = dateFechaFundadoModificar.getDate();

        if (fechaFundado == null) {
            JOptionPane.showMessageDialog(this, "Error en la fecha.", "Fecha inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ciudad = txtCiudadModificar.getText();

        if (ciudad.isEmpty() || ciudad.length() > 40) {
            JOptionPane.showMessageDialog(this, "Error en la ciudad.", "Ciudad inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Equipo nuevoEquipo = new Equipo(e.getNombre(), ciudad, fileImgEscudo.getAbsolutePath(), fechaFundado);

        try {
            PreparedStatement ps = vp.getConn().prepareStatement("update equipos set ciudad = ?, url_escudo = ?, dia_fundacion = ? where id = ?;");
            ps.setString(1, ciudad);
            ps.setString(2, "equipos/".concat(getImg()));
            ps.setDate(3, new java.sql.Date(fechaFundado.getTime()));
            ps.setLong(4, e.getId());
            ps.executeUpdate();

        }
        catch (SQLException ex) {
            Logger.getLogger(VentanaEquipos.class.getName()).log(Level.SEVERE, null, ex);
        }

        vp.setEquipo(índiceEquipo, nuevoEquipo);
        actualizarEstadoCombosEquipos();
        FTPUploader ftp = new FTPUploader(fileImgEscudo, vp.getUsuarioFTP(), vp.getPassFTP(), false, vp.getIp());
        ftp.subirFichero();
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        int respuesta = JOptionPane.showConfirmDialog(this, "Si se borra este equipo, se borrarán todos los partidos asociados a él.", "¿Desea borrar el equipo?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (respuesta == JOptionPane.NO_OPTION) {
            return;
        }

        Equipo e = (Equipo) comboEliminar.getSelectedItem();

        if (e == null) {
            return;
        }

        if (vp.getVentanaJugadores() != null) {
            vp.getVentanaJugadores().getJugadores().removeIf((j) -> j.getIdEquipo() == e.getId());
            vp.getVentanaJugadores().actualizarEstadoComboJugadores();
            vp.getVentanaJugadores().actualizarEstadoComboEquipos();
        }

        modeloCombos.removeElement(e);
        vp.removeEquipo(e);

        java.sql.Connection conn = vp.getConn();
        ArrayList<Partido> partidosBorrados = new ArrayList<>(), partidos = vp.getPartidos();

        try {
            for (Partido partido : partidos) {
                if (!partido.getLocal().equals(e) && !partido.getVisitante().equals(e)) {
                    continue;
                }

                PreparedStatement ps1 = conn.prepareStatement("delete from partidos where id_equipo_local = ? and id_equipo_visitante = ? and fecha_jugado = ?;");
                partidosBorrados.add(partido);
                ps1.setLong(1, partido.getLocal().getId());
                ps1.setLong(2, partido.getVisitante().getId());
                ps1.setTimestamp(3, new java.sql.Timestamp(partido.getFechaJugado().getTime()));
                ps1.executeUpdate();
            }

            PreparedStatement ps2 = conn.prepareStatement("delete from jugadores where id_equipo = ?; ");
            ps2.setLong(1, e.getId());
            ps2.executeUpdate();

            PreparedStatement ps3 = conn.prepareStatement("delete from equipos where id = ?;");
            ps3.setLong(1, e.getId());
            ps3.executeUpdate();

        }
        catch (SQLException ex) {
            Logger.getLogger(VentanaEquipos.class.getName()).log(Level.SEVERE, null, ex);
        }

        vp.getPartidos().removeAll(partidosBorrados);
        vp.getEquipos().remove(e);
        vp.actualizarEstadoCombosPartidos(null);

    }//GEN-LAST:event_btnEliminarActionPerformed

    private void comboModificarItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboModificarItemStateChanged
        Equipo e = (Equipo) comboModificar.getSelectedItem();

        if (e == null) {
            txtNombreModificar.setText(null);
            txtCiudadModificar.setText(null);
            dateFechaFundadoModificar.setDate(null);
            return;
        }

        txtNombreModificar.setText(e.getNombre());
        txtCiudadModificar.setText(e.getCiudad());
        dateFechaFundadoModificar.setDate(e.getFechaFundado());
    }//GEN-LAST:event_comboModificarItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbrirAñadir;
    private javax.swing.JButton btnAbrirModificar;
    private javax.swing.JButton btnAnterior;
    private javax.swing.JButton btnAñadir;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnSiguiente;
    private javax.swing.JComboBox<Equipo> comboEliminar;
    private javax.swing.JComboBox<Equipo> comboModificar;
    private com.toedter.calendar.JDateChooser dateFechaFundadoAñadir;
    private com.toedter.calendar.JDateChooser dateFechaFundadoModificar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel lblAcción;
    private javax.swing.JPanel panelAñadir;
    private javax.swing.JPanel panelEliminar;
    private javax.swing.JPanel panelModificar;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JTextField txtCiudadAñadir;
    private javax.swing.JTextField txtCiudadModificar;
    private javax.swing.JTextField txtNombreAñadir;
    private javax.swing.JTextField txtNombreModificar;
    // End of variables declaration//GEN-END:variables
}
