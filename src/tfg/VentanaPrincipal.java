package tfg;

import java.sql.*;
import java.awt.CardLayout;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 *
 * @author migui
 */
public class VentanaPrincipal extends javax.swing.JFrame {

    private static final int AÑADIR = 0, MODIFICAR = 1, ELIMINAR = 2;

    private final String user, password, schema, ip;
    private final ArrayList<String> formacionesVálidas;

    private final ArrayList<String> nombresCards;
    private final CardLayout card;

    private int índiceCard;

    private final ArrayList<Equipo> equipos;
    private final ArrayList<Partido> partidos;

    private final DefaultComboBoxModel<Equipo> modeloComboEquiposLocal, modeloComboEquiposVisit;

    private final DefaultComboBoxModel<Partido> modeloComboPartidos;

    private VentanaJugadores vj;
    private VentanaEquipos ve;

    private Connection conn;
    private String usuarioFTP, passFTP;

    public VentanaPrincipal(String usuarioFTP, String passFTP) {
        initComponents();

        final Properties p = new Properties();
        try (FileInputStream input = new FileInputStream("bd.properties")) {
            p.load(input);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.user = p.getProperty("bd_username");
        this.password = p.getProperty("bd_password");
        this.ip = p.getProperty("bd_ip");
        this.schema = p.getProperty("bd_schema");

        setVisible(true);
        this.usuarioFTP = usuarioFTP;
        this.passFTP = passFTP;

        try {
            final URL helpUrl = this.getClass().getResource("/ayudas/ayuda.hs");
            final HelpSet hs = new HelpSet(null, helpUrl);
            final HelpBroker browser = hs.createHelpBroker();
            browser.enableHelpOnButton(miAyuda, "index", hs);

        }
        catch (HelpSetException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.índiceCard = 0;

        this.equipos = new ArrayList<>();

        this.partidos = new ArrayList<>();

        this.formacionesVálidas = new ArrayList<>(Arrays.asList(new String[]{
            "5-3-2", "4-5-1", "4-4-1-1", "4-4-2", "4-3-2-1", "4-3-3", "3-5-2", "3-4-3", "2-3-5"
        }));

        this.nombresCards = new ArrayList<>(Arrays.asList(new String[]{
            "añadir", "modificar", "eliminar"
        }));

        this.card = (CardLayout) this.panelPrincipal.getLayout();

        this.modeloComboEquiposLocal = new DefaultComboBoxModel();
        this.modeloComboEquiposVisit = new DefaultComboBoxModel();

        this.comboEquipoLocalAñadir.setModel(this.modeloComboEquiposLocal);
        this.comboEquipoVisitAñadir.setModel(this.modeloComboEquiposVisit);
        this.comboEquipoLocalModificar.setModel(this.modeloComboEquiposLocal);
        this.comboEquipoVisitModificar.setModel(this.modeloComboEquiposVisit);

        this.modeloComboPartidos = new DefaultComboBoxModel();
        this.comboPartidosModificar.setModel(this.modeloComboPartidos);
        this.comboPartidosEliminar.setModel(this.modeloComboPartidos);

        obtenerDatos();
    }

    public String getUsuarioFTP() {
        return usuarioFTP;
    }

    public String getPassFTP() {
        return passFTP;
    }

    public void setUsuarioFTP(String user) {
        this.usuarioFTP = user;
    }

    public void setPassFTP(String pass) {
        this.passFTP = pass;
    }

    public String getIp() {
        return ip;
    }

    private void obtenerDatos() {
        try {
            conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:3306/%s", ip, schema), user, password);
            PreparedStatement ps = conn.prepareStatement("select * from equipos;");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                long id;
                String nombre, ciudad, urlImg;
                Date fechaFundado;

                id = rs.getLong("id");
                nombre = rs.getString("nombre");
                ciudad = rs.getString("ciudad");
                urlImg = rs.getString("url_escudo");
                fechaFundado = rs.getDate("dia_fundacion");

                equipos.add(new Equipo(id, nombre, ciudad, urlImg, fechaFundado));
            }

            ps = conn.prepareStatement("select * from partidos;");
            rs = ps.executeQuery();

            while (rs.next()) {
                Date fechaJugado;
                Equipo local = null, visitante = null;
                int golesLocal, golesVisit;
                String formaciónLocal, formaciónVisit, resumen;
                long idLocal, idVisitante;

                fechaJugado = rs.getTimestamp("fecha_jugado");

                idLocal = rs.getLong("id_equipo_local");
                idVisitante = rs.getLong("id_equipo_visitante");

                golesLocal = rs.getByte("goles_local");
                golesVisit = rs.getByte("goles_visitante");

                formaciónLocal = rs.getString("formacion_local");
                formaciónVisit = rs.getString("formacion_visitante");

                resumen = rs.getString("resumen");

                for (Equipo e : equipos) {
                    local = local == null && e.getId() == idLocal ? e : local;
                    visitante = visitante == null && e.getId() == idVisitante ? e : visitante;
                }

                partidos.add(new Partido(local, visitante, fechaJugado, golesLocal, golesVisit, formaciónLocal, formaciónVisit, resumen));
            }

        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo establecer la conexión con la base de datos", "Error conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        modeloComboEquiposLocal.addAll(equipos);
        modeloComboEquiposVisit.addAll(equipos);
        modeloComboPartidos.addAll(partidos);
    }

    private void insertPartido(Partido p) {
        try {
            PreparedStatement ps = conn.prepareStatement("insert into partidos values (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setLong(1, p.getLocal().getId());
            ps.setLong(2, p.getVisitante().getId());

            ps.setTimestamp(3, new java.sql.Timestamp(p.getFechaJugado().getTime()));

            ps.setInt(4, p.getGolesLocal());
            ps.setInt(5, p.getGolesVisit());

            ps.setString(6, p.getFormaciónLocal());
            ps.setString(7, p.getFormaciónVisit());

            ps.setString(8, p.getResumen());
            ps.executeUpdate();

        }
        catch (SQLException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected VentanaJugadores getVentanaJugadores() {
        return vj;
    }

    protected VentanaEquipos getVentanaEquipos() {
        return ve;
    }

    protected void closeVentanaJugadores() {
        vj.dispose();
        vj = null;
    }

    protected void closeVentanaEquipos() {
        ve.dispose();
        ve = null;
    }

    protected ArrayList<Partido> getPartidos() {
        return this.partidos;
    }

    protected ArrayList<String> getNombresCards() {
        return this.nombresCards;
    }

    protected ArrayList<Equipo> getEquipos() {
        return equipos;
    }

    protected Connection getConn() {
        return conn;
    }

    protected void addEquipo(Equipo e) {
        if (e == null) {
            JOptionPane.showMessageDialog(this, "El equipo es nulo.", "Equipo nulo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (equipos.contains(e)) {
            JOptionPane.showMessageDialog(this, "Ese equipo ya existe.", "Equipo duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        equipos.add(e);

        modeloComboEquiposLocal.addElement(e);
        modeloComboEquiposVisit.addElement(e);
    }

    protected void removeEquipo(Equipo e) {
        if (e == null) {
            JOptionPane.showMessageDialog(this, "El equipo es nulo.", "Equipo nulo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!equipos.contains(e)) {
            JOptionPane.showMessageDialog(this, "Ese equipo no existe.", "Equipo no existe", JOptionPane.WARNING_MESSAGE);
            return;
        }

        equipos.remove(e);

        modeloComboEquiposLocal.removeElement(e);
        modeloComboEquiposVisit.removeElement(e);
    }

    protected void setEquipo(int índice, Equipo e) {
        if (e == null) {
            JOptionPane.showMessageDialog(this, "El equipo es nulo.", "Equipo nulo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        equipos.set(índice, e);

        modeloComboEquiposLocal.removeAllElements();
        modeloComboEquiposVisit.removeAllElements();

        modeloComboEquiposLocal.addAll(equipos);
        modeloComboEquiposVisit.addAll(equipos);
    }

    private boolean horaVálida(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MINUTE) == 0 || c.get(Calendar.MINUTE) == 30;
    }

    private void obtenerNombreCard(boolean sumar) {
        if (sumar && índiceCard == nombresCards.size() - 1 || !sumar && índiceCard == 0) {
            return;
        }

        índiceCard += sumar ? 1 : -1;
        cambiarPanel();
    }

    private void cambiarPanel() {
        String nombre = nombresCards.get(índiceCard);
        card.show(panelPrincipal, nombre);
        lblAcción.setText((nombre.concat(" partidos")).toUpperCase());
    }

    protected void actualizarEstadoCombosPartidos(Partido p) {
        modeloComboPartidos.removeAllElements();
        modeloComboPartidos.addAll(partidos);

        if (partidos.size() >= 1) {
            modeloComboPartidos.setSelectedItem(p == null ? partidos.get(0) : p);
        }
    }

    private boolean algúnEquipoJugóEseDía(Partido partido, Date fechaAJugar, Equipo localAJugar, Equipo visitAJugar) {
        Calendar fechaCalendar = Calendar.getInstance();
        Calendar fechaPartidoCalendar = Calendar.getInstance();

        fechaCalendar.setTime(fechaAJugar);
        fechaPartidoCalendar.setTime(partido.getFechaJugado());

        if (fechaPartidoCalendar.get(Calendar.YEAR) == fechaCalendar.get(Calendar.YEAR)
                && fechaPartidoCalendar.get(Calendar.MONTH) == fechaCalendar.get(Calendar.MONTH)
                && fechaPartidoCalendar.get(Calendar.DATE) == fechaCalendar.get(Calendar.DATE)) {
            if (partido.getLocal().equals(localAJugar) || partido.getVisitante().equals(localAJugar)) {
                JOptionPane.showMessageDialog(this, String.format("%s ya ha jugado ese día.", localAJugar.getNombre()), "Equipo incorrecto", JOptionPane.WARNING_MESSAGE);
                return true;
            }
            if (partido.getLocal().equals(visitAJugar) || partido.getVisitante().equals(visitAJugar)) {
                JOptionPane.showMessageDialog(this, String.format("%s ya ha jugado ese día.", visitAJugar.getNombre()), "Equipo incorrecto", JOptionPane.WARNING_MESSAGE);
                return true;
            }
        }

        return false;
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
        spinnerGolesVisitAñadir = new javax.swing.JSpinner();
        spinnerGolesLocalAñadir = new javax.swing.JSpinner();
        jLabel23 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtResumenAñadir = new javax.swing.JTextArea();
        jLabel19 = new javax.swing.JLabel();
        btnAñadir = new javax.swing.JButton();
        comboEquipoLocalAñadir = new javax.swing.JComboBox<>();
        comboEquipoVisitAñadir = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        txtFormaciónLocalAñadir = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        txtFormaciónVisitAñadir = new javax.swing.JTextField();
        dateFechaJugadoAñadir = new com.toedter.calendar.JDateChooser();
        panelModificar = new javax.swing.JPanel();
        spinnerGolesVisitModificar = new javax.swing.JSpinner();
        spinnerGolesLocalModificar = new javax.swing.JSpinner();
        jLabel33 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtResumenModificar = new javax.swing.JTextArea();
        jLabel34 = new javax.swing.JLabel();
        btnModificar = new javax.swing.JButton();
        comboEquipoLocalModificar = new javax.swing.JComboBox<>();
        comboEquipoVisitModificar = new javax.swing.JComboBox<>();
        jLabel35 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        txtFormaciónLocalModificar = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        txtFormaciónVisitModificar = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        dateFechaJugadoModificar = new com.toedter.calendar.JDateChooser();
        comboPartidosModificar = new javax.swing.JComboBox<>();
        panelEliminar = new javax.swing.JPanel();
        comboPartidosEliminar = new javax.swing.JComboBox<>();
        btnEliminar = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        miAñadir = new javax.swing.JMenuItem();
        miModificar = new javax.swing.JMenuItem();
        miEliminar = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        miAñadirEquipos = new javax.swing.JMenuItem();
        miModificarEquipos = new javax.swing.JMenuItem();
        miEliminarEquipos = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        miAyuda = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Gestión partidos");

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

        lblAcción.setText("AÑADIR PARTIDOS");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelSuperior.add(lblAcción, gridBagConstraints);

        getContentPane().add(panelSuperior, java.awt.BorderLayout.PAGE_START);

        panelPrincipal.setLayout(new java.awt.CardLayout());

        panelAñadir.setBackground(new java.awt.Color(204, 255, 204));
        panelAñadir.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(spinnerGolesVisitAñadir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(spinnerGolesLocalAñadir, gridBagConstraints);

        jLabel23.setText("Resumen:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(jLabel23, gridBagConstraints);

        txtResumenAñadir.setColumns(20);
        txtResumenAñadir.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        txtResumenAñadir.setLineWrap(true);
        txtResumenAñadir.setRows(5);
        jScrollPane1.setViewportView(txtResumenAñadir);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(jScrollPane1, gridBagConstraints);

        jLabel19.setText("Equipo local:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(jLabel19, gridBagConstraints);

        btnAñadir.setText("Añadir partido");
        btnAñadir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAñadirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(btnAñadir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(comboEquipoLocalAñadir, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(comboEquipoVisitAñadir, gridBagConstraints);

        jLabel20.setText("Equipo visit.:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(jLabel20, gridBagConstraints);

        jLabel15.setText("Fecha jugado:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(jLabel15, gridBagConstraints);

        jLabel21.setText("Goles local:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(jLabel21, gridBagConstraints);

        jLabel22.setText("Goles visit.:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(jLabel22, gridBagConstraints);

        jLabel24.setText("Formación local:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(jLabel24, gridBagConstraints);

        txtFormaciónLocalAñadir.setText("4-4-2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(txtFormaciónLocalAñadir, gridBagConstraints);

        jLabel25.setText("Formación visit:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(jLabel25, gridBagConstraints);

        txtFormaciónVisitAñadir.setText("4-3-3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelAñadir.add(txtFormaciónVisitAñadir, gridBagConstraints);

        dateFechaJugadoAñadir.setDate(new Date());
        dateFechaJugadoAñadir.setDateFormatString("dd/MM/yyyy HH:mm");
        dateFechaJugadoAñadir.setInheritsPopupMenu(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(dateFechaJugadoAñadir, gridBagConstraints);

        panelPrincipal.add(panelAñadir, "añadir");
        panelAñadir.getAccessibleContext().setAccessibleName("Añadir");

        panelModificar.setBackground(new java.awt.Color(153, 204, 255));
        panelModificar.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(spinnerGolesVisitModificar, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(spinnerGolesLocalModificar, gridBagConstraints);

        jLabel33.setText("Resumen:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(jLabel33, gridBagConstraints);

        txtResumenModificar.setColumns(20);
        txtResumenModificar.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        txtResumenModificar.setLineWrap(true);
        txtResumenModificar.setRows(5);
        txtResumenModificar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane3.setViewportView(txtResumenModificar);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(jScrollPane3, gridBagConstraints);

        jLabel34.setText("Equipo local:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(jLabel34, gridBagConstraints);

        btnModificar.setText("Modificar partido");
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(btnModificar, gridBagConstraints);

        comboEquipoLocalModificar.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(comboEquipoLocalModificar, gridBagConstraints);

        comboEquipoVisitModificar.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(comboEquipoVisitModificar, gridBagConstraints);

        jLabel35.setText("Equipo visit.:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(jLabel35, gridBagConstraints);

        jLabel17.setText("Fecha jugado:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(jLabel17, gridBagConstraints);

        jLabel36.setText("Goles local:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(jLabel36, gridBagConstraints);

        jLabel37.setText("Goles visit.:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(jLabel37, gridBagConstraints);

        jLabel38.setText("Formación local:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(jLabel38, gridBagConstraints);

        txtFormaciónLocalModificar.setText("4-4-2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(txtFormaciónLocalModificar, gridBagConstraints);

        jLabel39.setText("Formación visit:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(jLabel39, gridBagConstraints);

        txtFormaciónVisitModificar.setText("4-3-3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(txtFormaciónVisitModificar, gridBagConstraints);

        jLabel1.setText("Partidos:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(jLabel1, gridBagConstraints);

        dateFechaJugadoModificar.setDateFormatString("dd/MM/yyyy HH:mm");
        dateFechaJugadoModificar.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(dateFechaJugadoModificar, gridBagConstraints);

        comboPartidosModificar.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboPartidosModificarItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        panelModificar.add(comboPartidosModificar, gridBagConstraints);

        panelPrincipal.add(panelModificar, "modificar");

        panelEliminar.setBackground(new java.awt.Color(255, 204, 204));

        panelEliminar.add(comboPartidosEliminar);

        btnEliminar.setText("Eliminar partido");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });
        panelEliminar.add(btnEliminar);

        panelPrincipal.add(panelEliminar, "eliminar");

        getContentPane().add(panelPrincipal, java.awt.BorderLayout.CENTER);

        jMenu1.setText("Jugadores");

        miAñadir.setText("Añadir");
        miAñadir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAñadirActionPerformed(evt);
            }
        });
        jMenu1.add(miAñadir);

        miModificar.setText("Modificar");
        miModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miModificarActionPerformed(evt);
            }
        });
        jMenu1.add(miModificar);

        miEliminar.setText("Eliminar");
        miEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miEliminarActionPerformed(evt);
            }
        });
        jMenu1.add(miEliminar);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Equipos");

        miAñadirEquipos.setText("Añadir");
        miAñadirEquipos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAñadirEquiposActionPerformed(evt);
            }
        });
        jMenu2.add(miAñadirEquipos);

        miModificarEquipos.setText("Modificar");
        miModificarEquipos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miModificarEquiposActionPerformed(evt);
            }
        });
        jMenu2.add(miModificarEquipos);

        miEliminarEquipos.setText("Eliminar");
        miEliminarEquipos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miEliminarEquiposActionPerformed(evt);
            }
        });
        jMenu2.add(miEliminarEquipos);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Ayuda");

        jMenuItem1.setText("Usuario FTP");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem1);

        miAyuda.setText("Ayuda");
        jMenu3.add(miAyuda);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void btnAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnteriorActionPerformed
        obtenerNombreCard(false);
    }//GEN-LAST:event_btnAnteriorActionPerformed
    private void btnSiguienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSiguienteActionPerformed
        obtenerNombreCard(true);
    }//GEN-LAST:event_btnSiguienteActionPerformed
    private void miAñadirEquiposActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miAñadirEquiposActionPerformed
        if (ve != null) {
            return;
        }

        ve = new VentanaEquipos(AÑADIR, this);
        ve.setVisible(true);
    }//GEN-LAST:event_miAñadirEquiposActionPerformed

    private void miModificarEquiposActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miModificarEquiposActionPerformed
        if (ve != null) {
            return;
        }

        ve = new VentanaEquipos(MODIFICAR, this);
        ve.setVisible(true);
    }//GEN-LAST:event_miModificarEquiposActionPerformed

    private void miEliminarEquiposActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miEliminarEquiposActionPerformed
        if (ve != null) {
            return;
        }

        ve = new VentanaEquipos(ELIMINAR, this);
        ve.setVisible(true);
    }//GEN-LAST:event_miEliminarEquiposActionPerformed

    private void miAñadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miAñadirActionPerformed
        if (vj != null) {
            return;
        }

        vj = new VentanaJugadores(AÑADIR, this);
        vj.setVisible(true);
    }//GEN-LAST:event_miAñadirActionPerformed

    private void miModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miModificarActionPerformed
        if (vj != null) {
            return;
        }

        vj = new VentanaJugadores(MODIFICAR, this);
        vj.setVisible(true);
    }//GEN-LAST:event_miModificarActionPerformed

    private void miEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miEliminarActionPerformed
        if (vj != null) {
            return;
        }

        vj = new VentanaJugadores(ELIMINAR, this);
        vj.setVisible(true);
    }//GEN-LAST:event_miEliminarActionPerformed

    private void btnAñadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAñadirActionPerformed
        if (dateFechaJugadoAñadir.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Los partidos sólo se pueden jugar a en punto ó y media.", "Hora incorrecta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date fechaAJugar = dateFechaJugadoAñadir.getDate();

        String formaciónLocal, formaciónVisit;
        Equipo local, visit;
        int golesLocal, golesVisit;

        formaciónLocal = txtFormaciónLocalAñadir.getText();
        formaciónVisit = txtFormaciónVisitAñadir.getText();

        local = (Equipo) comboEquipoLocalAñadir.getSelectedItem();
        visit = (Equipo) comboEquipoVisitAñadir.getSelectedItem();

        golesLocal = Integer.parseInt(spinnerGolesLocalAñadir.getValue().toString());
        golesVisit = Integer.parseInt(spinnerGolesVisitAñadir.getValue().toString());

        {
            if (!horaVálida(fechaAJugar)) {
                JOptionPane.showMessageDialog(this, "Los partidos sólo se pueden jugar a en punto ó y media.", "Hora incorrecta", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (local == null || visit == null) {
                JOptionPane.showMessageDialog(this, "Debes seleccionar dos equipos.", "Equipos incorrectos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (local.equals(visit)) {
                JOptionPane.showMessageDialog(this, "Un equipo no se puede enfrentar a sí mismo.", "Equipos incorrectos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!formacionesVálidas.contains(formaciónLocal)) {
                JOptionPane.showMessageDialog(this, "La formación local no es válida.", "Formación inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!formacionesVálidas.contains(formaciónVisit)) {
                JOptionPane.showMessageDialog(this, "La formación visit. no es válida.", "Formación inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (golesLocal < 0 || golesLocal > 127) {
                JOptionPane.showMessageDialog(this, "Valor de los goles locales es inválido.", "Goles inválidos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (golesVisit < 0 || golesVisit > 127) {
                JOptionPane.showMessageDialog(this, "Valor de los goles visitantes es inválido.", "Goles inválidos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (txtResumenAñadir.getText().length() > 250) {
                JOptionPane.showMessageDialog(this, "Resumen demasiado largo.", "Resumen inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        for (Partido p : partidos) {
            if (algúnEquipoJugóEseDía(p, fechaAJugar, local, visit)) {
                return;
            }
        }
        Partido p = new Partido(local, visit, dateFechaJugadoAñadir.getDate(), golesLocal, golesVisit, formaciónLocal, formaciónVisit, txtResumenAñadir.getText());
        partidos.add(p);
        insertPartido(p);
        actualizarEstadoCombosPartidos(p);
    }//GEN-LAST:event_btnAñadirActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        Partido p = (Partido) comboPartidosEliminar.getSelectedItem();

        if (p == null) {
            return;
        }

        partidos.remove(p);

        actualizarEstadoCombosPartidos(null);

        try {
            PreparedStatement ps = conn.prepareStatement("delete from partidos where id_equipo_local = ? and id_equipo_visitante = ? and fecha_jugado = ?");
            ps.setLong(1, p.getLocal().getId());
            ps.setLong(2, p.getVisitante().getId());
            ps.setTimestamp(3, new java.sql.Timestamp(p.getFechaJugado().getTime()));

            ps.executeUpdate();
        }
        catch (SQLException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        Partido p = (Partido) modeloComboPartidos.getSelectedItem();

        if (p == null) {
            return;
        }

        int índicePartido = modeloComboPartidos.getIndexOf(p);

        String formaciónLocal, formaciónVisit, resumen;
        int golesLocal, golesVisit;

        formaciónLocal = txtFormaciónLocalModificar.getText();
        formaciónVisit = txtFormaciónVisitModificar.getText();

        golesLocal = Integer.parseInt(spinnerGolesLocalModificar.getValue().toString());
        golesVisit = Integer.parseInt(spinnerGolesVisitModificar.getValue().toString());

        resumen = txtResumenModificar.getText();

        if (!formacionesVálidas.contains(formaciónLocal)) {
            JOptionPane.showMessageDialog(this, "La formación local no es válida.", "Formación inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!formacionesVálidas.contains(formaciónVisit)) {
            JOptionPane.showMessageDialog(this, "La formación visit. no es válida.", "Formación inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (golesLocal < 0 || golesLocal > 127) {
            JOptionPane.showMessageDialog(this, "Valor de los goles locales es inválido.", "Goles inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (golesVisit < 0 || golesVisit > 127) {
            JOptionPane.showMessageDialog(this, "Valor de los goles visitantes es inválido.", "Goles inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (resumen.length() > 250) {
            JOptionPane.showMessageDialog(this, "Resumen demasiado largo.", "Resumen inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Partido pModificar = new Partido(p.getLocal(), p.getVisitante(), p.getFechaJugado(), golesLocal, golesVisit, formaciónLocal, formaciónVisit, resumen);
        partidos.set(índicePartido, pModificar);
        actualizarEstadoCombosPartidos(pModificar);

        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE partidos SET goles_local = ?, goles_visitante = ?, formacion_local = ?, formacion_visitante = ?, resumen = ? WHERE id_equipo_local = ? AND id_equipo_visitante = ? AND fecha_jugado = ?;");
            ps.setInt(1, golesLocal);
            ps.setInt(2, golesVisit);
            ps.setString(3, formaciónLocal);
            ps.setString(4, formaciónVisit);
            ps.setString(5, resumen);

            ps.setLong(6, pModificar.getLocal().getId());
            ps.setLong(7, pModificar.getVisitante().getId());
            ps.setTimestamp(8, new java.sql.Timestamp(p.getFechaJugado().getTime()));

            ps.executeUpdate();

        }
        catch (SQLException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_btnModificarActionPerformed

    private void comboPartidosModificarItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboPartidosModificarItemStateChanged
        Partido p = (Partido) modeloComboPartidos.getSelectedItem();

        if (p == null) {
            comboEquipoLocalModificar.setSelectedItem(null);
            comboEquipoVisitModificar.setSelectedItem(null);
            dateFechaJugadoModificar.setDate(null);

            spinnerGolesLocalModificar.setValue(0);
            spinnerGolesVisitModificar.setValue(0);

            txtFormaciónLocalModificar.setText(null);
            txtFormaciónVisitModificar.setText(null);

            txtResumenModificar.setText(null);
            return;
        }

        comboEquipoLocalModificar.setSelectedItem(p.getLocal());
        comboEquipoVisitModificar.setSelectedItem(p.getVisitante());
        dateFechaJugadoModificar.setDate(p.getFechaJugado());

        spinnerGolesLocalModificar.setValue(p.getGolesLocal());
        spinnerGolesVisitModificar.setValue(p.getGolesVisit());

        txtFormaciónLocalModificar.setText(p.getFormaciónLocal());
        txtFormaciónVisitModificar.setText(p.getFormaciónVisit());

        txtResumenModificar.setText(p.getResumen());
    }//GEN-LAST:event_comboPartidosModificarItemStateChanged

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        new FTPChooser(this).setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    public static void main(String[] args) {
        new VentanaPrincipal("ftp", "abc123.").setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnterior;
    private javax.swing.JButton btnAñadir;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnSiguiente;
    private javax.swing.JComboBox<Equipo> comboEquipoLocalAñadir;
    private javax.swing.JComboBox<Equipo> comboEquipoLocalModificar;
    private javax.swing.JComboBox<Equipo> comboEquipoVisitAñadir;
    private javax.swing.JComboBox<Equipo> comboEquipoVisitModificar;
    private javax.swing.JComboBox<Partido> comboPartidosEliminar;
    private javax.swing.JComboBox<Partido> comboPartidosModificar;
    private com.toedter.calendar.JDateChooser dateFechaJugadoAñadir;
    private com.toedter.calendar.JDateChooser dateFechaJugadoModificar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblAcción;
    private javax.swing.JMenuItem miAyuda;
    private javax.swing.JMenuItem miAñadir;
    private javax.swing.JMenuItem miAñadirEquipos;
    private javax.swing.JMenuItem miEliminar;
    private javax.swing.JMenuItem miEliminarEquipos;
    private javax.swing.JMenuItem miModificar;
    private javax.swing.JMenuItem miModificarEquipos;
    private javax.swing.JPanel panelAñadir;
    private javax.swing.JPanel panelEliminar;
    private javax.swing.JPanel panelModificar;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JSpinner spinnerGolesLocalAñadir;
    private javax.swing.JSpinner spinnerGolesLocalModificar;
    private javax.swing.JSpinner spinnerGolesVisitAñadir;
    private javax.swing.JSpinner spinnerGolesVisitModificar;
    private javax.swing.JTextField txtFormaciónLocalAñadir;
    private javax.swing.JTextField txtFormaciónLocalModificar;
    private javax.swing.JTextField txtFormaciónVisitAñadir;
    private javax.swing.JTextField txtFormaciónVisitModificar;
    private javax.swing.JTextArea txtResumenAñadir;
    private javax.swing.JTextArea txtResumenModificar;
    // End of variables declaration//GEN-END:variables
}
