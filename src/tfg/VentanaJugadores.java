package tfg;

import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author migui
 */
public class VentanaJugadores extends javax.swing.JDialog {

    private final ArrayList<String> nombresCards;
    private final CardLayout card;

    private final VentanaPrincipal vp;
    private final DefaultComboBoxModel<Equipo> modeloComboEquipos;
    private final DefaultComboBoxModel<Jugador> modeloComboJugadores;

    private final DefaultComboBoxModel<String> modeloComboElementos, modeloComboPosiciones, modeloComboCursos;

    private final ArrayList<Jugador> jugadores;

    private VisualizadorImágenes vi;
    private File imgJugador;
    private int índice;

    public VentanaJugadores(int índiceInicial, VentanaPrincipal vp) {
        initComponents();
        this.jugadores = new ArrayList<>();

        this.vp = vp;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                vp.closeVentanaJugadores();
            }
        });

        this.modeloComboEquipos = new DefaultComboBoxModel<>();

        this.comboEquipoAñadir.setModel(modeloComboEquipos);
        this.comboEquipoModificar.setModel(modeloComboEquipos);

        this.modeloComboJugadores = new DefaultComboBoxModel<>();
        this.comboJugadoresModificar.setModel(modeloComboJugadores);
        this.comboJugadoresEliminar.setModel(modeloComboJugadores);

        this.modeloComboElementos = new DefaultComboBoxModel<>();
        this.modeloComboPosiciones = new DefaultComboBoxModel<>();
        this.modeloComboCursos = new DefaultComboBoxModel<>();

        modeloComboElementos.addAll(Arrays.asList(new String[]{
            "Montaña", "Fuego", "Bosque", "Aire"
        }));
        modeloComboPosiciones.addAll(Arrays.asList(new String[]{
            "DL", "MD", "DF", "PR"
        }));
        modeloComboCursos.addAll(Arrays.asList(new String[]{
            "Primero", "Segundo", "Tercero"
        }));

        comboElementoAñadir.setModel(modeloComboElementos);
        comboElementoModificar.setModel(modeloComboElementos);

        comboPosiciónAñadir.setModel(modeloComboPosiciones);
        comboPosiciónModificar.setModel(modeloComboPosiciones);

        comboCursoAñadir.setModel(modeloComboCursos);
        comboCursoModificar.setModel(modeloComboCursos);

        this.índice = índiceInicial;
        this.nombresCards = vp.getNombresCards();

        this.card = (CardLayout) this.panelPrincipal.getLayout();

        cambiarPanel();
        actualizarEstadoComboEquipos();
        obtenerDatos();
    }

    private File abrirFileChooser() {
        final JFileChooser fileChooser = new JFileChooser();
        final int resultado = fileChooser.showOpenDialog(this);
        return resultado != JFileChooser.CANCEL_OPTION ? fileChooser.getSelectedFile() : null;
    }

    private void obtenerDatos() {
        try {
            Connection conn = vp.getConn();
            PreparedStatement ps = conn.prepareStatement("select * from jugadores order by nombre_completo asc;");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                long id;
                String nombre, posición, elemento, curso, urlImagen;
                int dorsal;
                long idEquipo;

                id = rs.getLong("id");

                nombre = rs.getString("nombre_completo");
                posición = rs.getString("posicion");
                elemento = rs.getString("elemento");
                curso = rs.getString("curso");
                urlImagen = rs.getString("url_imagen");
                dorsal = rs.getByte("dorsal");
                idEquipo = rs.getLong("id_equipo");

                jugadores.add(new Jugador(id, nombre, elemento, posición, curso, urlImagen, dorsal, idEquipo));
            }

        } catch (SQLException ex) {
            Logger.getLogger(VentanaJugadores.class.getName()).log(Level.SEVERE, null, ex);
        }

        actualizarEstadoComboJugadores();
    }

    protected void actualizarEstadoComboJugadores() {
        modeloComboJugadores.removeAllElements();
        modeloComboJugadores.addAll(jugadores);
    }

    protected ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    private void insertarJugador(Jugador j) {
        try {
            PreparedStatement ps = vp.getConn().prepareStatement("insert jugadores (nombre_completo, posicion, elemento, curso, url_imagen, dorsal, id_equipo) values (?, ?, ?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, j.getNombre());
            ps.setString(2, j.getPosición());
            ps.setString(3, j.getElemento());
            ps.setString(4, j.getCurso());
            ps.setString(5, "jugadores/".concat(getImg()));
            ps.setInt(6, j.getDorsal());
            ps.setLong(7, j.getIdEquipo());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            while (rs.next()) {
                j.setId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }

        FTPUploader ftp = new FTPUploader(imgJugador, vp.getUsuarioFTP(), vp.getPassFTP(), true, vp.getIp());
        ftp.subirFichero();
    }

    private void addJugador(Jugador j) {
        if (j == null) {
            JOptionPane.showMessageDialog(this, "El jugador es nulo.", "Jugador nulo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (jugadores.contains(j)) {
            JOptionPane.showMessageDialog(this, "Ese jugador ya existe.", "Jugador duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        jugadores.add(j);
        modeloComboJugadores.removeAllElements();
        modeloComboJugadores.addAll(jugadores);
    }

    protected void removeJugador(Jugador j) {
        if (j == null) {
            JOptionPane.showMessageDialog(this, "El jugador es nulo.", "Jugador nulo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!jugadores.contains(j)) {
            JOptionPane.showMessageDialog(this, "Ese jugador no existe.", "Jugador no existe", JOptionPane.WARNING_MESSAGE);
            return;
        }

        jugadores.remove(j);
        modeloComboJugadores.removeAllElements();
        modeloComboJugadores.addAll(jugadores);
    }

    protected void actualizarEstadoComboEquipos() {
        modeloComboEquipos.removeAllElements();
        modeloComboEquipos.addAll(vp.getEquipos());
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
        lblAcción.setText((nombre.concat(" jugadores")).toUpperCase());
    }

    private String getImg() {
        if (this.imgJugador == null) {
            JOptionPane.showMessageDialog(this, "El archivo debe ser un webp.", "Archivo inválido", JOptionPane.WARNING_MESSAGE);
            return "";
        }
        return this.imgJugador.getName();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
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
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnAñadir = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        comboPosiciónAñadir = new javax.swing.JComboBox<>();
        comboCursoAñadir = new javax.swing.JComboBox<>();
        comboEquipoAñadir = new javax.swing.JComboBox<>();
        spinnerDorsalAñadir = new javax.swing.JSpinner();
        btnAbrirAñadir = new javax.swing.JButton();
        comboElementoAñadir = new javax.swing.JComboBox<>();
        panelModificar = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        txtNombreModificar = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        btnModificar = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        comboPosiciónModificar = new javax.swing.JComboBox<>();
        comboCursoModificar = new javax.swing.JComboBox<>();
        comboEquipoModificar = new javax.swing.JComboBox<>();
        spinnerDorsalModificar = new javax.swing.JSpinner();
        btnAbrirModificar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        comboJugadoresModificar = new javax.swing.JComboBox<>();
        comboElementoModificar = new javax.swing.JComboBox<>();
        panelEliminar = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        comboJugadoresEliminar = new javax.swing.JComboBox<>();
        btnEliminar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Gestión jugadores ");

        panelSuperior.setLayout(new java.awt.GridBagLayout());

        btnAnterior.setText("◀");
        btnAnterior.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnAnteriorActionPerformed(evt);
            }
        });
        panelSuperior.add(btnAnterior, new java.awt.GridBagConstraints());

        btnSiguiente.setText("▶");
        btnSiguiente.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnSiguienteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        panelSuperior.add(btnSiguiente, gridBagConstraints);

        lblAcción.setText("AÑADIR JUGADORES");
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

        txtNombreAñadir.setText("Mark Evans");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(txtNombreAñadir, gridBagConstraints);

        jLabel3.setText("Elemento:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(jLabel3, gridBagConstraints);

        jLabel4.setText("Posición:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Curso:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(jLabel5, gridBagConstraints);

        btnAñadir.setText("Añadir jugador");
        btnAñadir.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnAñadirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(btnAñadir, gridBagConstraints);

        jLabel7.setText("Dorsal:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(jLabel7, gridBagConstraints);

        jLabel8.setText("Equipo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(jLabel8, gridBagConstraints);

        jLabel9.setText("Imagen:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(jLabel9, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(comboPosiciónAñadir, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(comboCursoAñadir, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(comboEquipoAñadir, gridBagConstraints);

        spinnerDorsalAñadir.setValue(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(spinnerDorsalAñadir, gridBagConstraints);

        btnAbrirAñadir.setText("Abrir...");
        btnAbrirAñadir.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnAbrirAñadirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(btnAbrirAñadir, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelAñadir.add(comboElementoAñadir, gridBagConstraints);

        panelPrincipal.add(panelAñadir, "añadir");

        panelModificar.setBackground(new java.awt.Color(153, 204, 255));
        panelModificar.setLayout(new java.awt.GridBagLayout());

        jLabel14.setText("Nombre:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel14, gridBagConstraints);

        txtNombreModificar.setEditable(false);
        txtNombreModificar.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(txtNombreModificar, gridBagConstraints);

        jLabel15.setText("Elemento:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel15, gridBagConstraints);

        jLabel16.setText("Posición:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel16, gridBagConstraints);

        jLabel17.setText("Curso:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel17, gridBagConstraints);

        btnModificar.setText("Modificar jugador");
        btnModificar.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnModificarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(btnModificar, gridBagConstraints);

        jLabel18.setText("Dorsal:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel18, gridBagConstraints);

        jLabel19.setText("Equipo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel19, gridBagConstraints);

        jLabel20.setText("Imagen:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel20, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(comboPosiciónModificar, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(comboCursoModificar, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(comboEquipoModificar, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(spinnerDorsalModificar, gridBagConstraints);

        btnAbrirModificar.setText("Abrir...");
        btnAbrirModificar.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnAbrirModificarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(btnAbrirModificar, gridBagConstraints);

        jLabel1.setText("Jugadores:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(jLabel1, gridBagConstraints);

        comboJugadoresModificar.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                comboJugadoresModificarItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(comboJugadoresModificar, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        panelModificar.add(comboElementoModificar, gridBagConstraints);

        panelPrincipal.add(panelModificar, "modificar");

        panelEliminar.setBackground(new java.awt.Color(255, 204, 204));

        jLabel6.setText("Jugadores:");
        panelEliminar.add(jLabel6);

        panelEliminar.add(comboJugadoresEliminar);

        btnEliminar.setText("Eliminar jugador");
        btnEliminar.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
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
        actualizarEstadoComboEquipos();
    }//GEN-LAST:event_btnAnteriorActionPerformed

    private void btnSiguienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSiguienteActionPerformed
        obtenerValorNombres(true);
        actualizarEstadoComboEquipos();
    }//GEN-LAST:event_btnSiguienteActionPerformed

    private void btnAñadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAñadirActionPerformed
        String nombre = txtNombreAñadir.getText();
        String elemento = comboElementoAñadir.getSelectedItem().toString();
        String posición = comboPosiciónAñadir.getSelectedItem().toString();
        String curso = comboCursoAñadir.getSelectedItem().toString();
        int dorsal = Integer.parseInt(spinnerDorsalAñadir.getValue().toString());
        Equipo e = (Equipo) comboEquipoAñadir.getSelectedItem();
        long id = 0;

        for (Jugador j : jugadores) {
            if (j.getNombre().equals(nombre)) {
                JOptionPane.showMessageDialog(this, "Ya hay un jugador con ese nombre.", "Nombre inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (nombre == null || nombre.isBlank() || nombre.length() > 75) {
            JOptionPane.showMessageDialog(this, "El nombre del jugador es inválido.", "Nombre inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (elemento == null) {
            JOptionPane.showMessageDialog(this, "Debes seleccionar un elemento.", "Elemento inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (posición == null) {
            JOptionPane.showMessageDialog(this, "Debes seleccionar una posición.", "Posición inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (curso == null) {
            JOptionPane.showMessageDialog(this, "Debes seleccionar un curso.", "Curso inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (dorsal <= 0 || dorsal > 127) {
            JOptionPane.showMessageDialog(this, "El dorsal del jugador es inválido.", "Dorsal inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (e == null) {
            JOptionPane.showMessageDialog(this, "El equipo del jugador es inválido.", "Equipo inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (imgJugador == null || !imgJugador.getAbsolutePath().endsWith(".webp")) {
            JOptionPane.showMessageDialog(this, "Imagen no válida: debe ser webp.", "Imagen inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Jugador j = new Jugador(nombre, elemento, posición, curso, getImg(), dorsal, e.getId());

        this.insertarJugador(j);
        addJugador(j);

    }//GEN-LAST:event_btnAñadirActionPerformed

    private void btnAbrirAñadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirAñadirActionPerformed
        imgJugador = abrirFileChooser();

        if (vi == null || !vi.isShowing()) {
            vi = new VisualizadorImágenes(imgJugador);
        }
    }//GEN-LAST:event_btnAbrirAñadirActionPerformed

    private void comboJugadoresModificarItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_comboJugadoresModificarItemStateChanged
    {//GEN-HEADEREND:event_comboJugadoresModificarItemStateChanged
        Jugador j = (Jugador) modeloComboJugadores.getSelectedItem();

        if (j == null) {
            txtNombreModificar.setText(null);
            comboElementoModificar.setSelectedItem(null);
            comboPosiciónModificar.setSelectedItem(null);
            comboCursoModificar.setSelectedItem(null);
            comboEquipoModificar.setSelectedItem(null);
            spinnerDorsalModificar.setValue(1);
            return;
        }

        txtNombreModificar.setText(j.getNombre());
        comboElementoModificar.setSelectedItem(j.getElemento());
        comboPosiciónModificar.setSelectedItem(j.getPosición());
        comboCursoModificar.setSelectedItem(j.getCurso());
        spinnerDorsalModificar.setValue(j.getDorsal());

        for (Equipo e : vp.getEquipos()) {
            if (e.getId() == j.getIdEquipo()) {
                comboEquipoModificar.setSelectedItem(e);
                break;
            }
        }
    }//GEN-LAST:event_comboJugadoresModificarItemStateChanged

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnEliminarActionPerformed
    {//GEN-HEADEREND:event_btnEliminarActionPerformed
        Jugador j = (Jugador) modeloComboJugadores.getSelectedItem();

        if (j == null) {
            return;
        }

        if (modeloComboJugadores.getSize() > 0) {
            modeloComboJugadores.setSelectedItem(modeloComboJugadores.getElementAt(modeloComboJugadores.getSize() - 1));
        }

        try {
            PreparedStatement ps;
            ps = vp.getConn().prepareStatement("delete from jugadores where id = ?;");
            ps.setLong(1, j.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(VentanaJugadores.class.getName()).log(Level.SEVERE, null, ex);
        }

        removeJugador(j);
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnModificarActionPerformed
    {//GEN-HEADEREND:event_btnModificarActionPerformed
        Jugador j = (Jugador) modeloComboJugadores.getSelectedItem();
        int índiceJugador = jugadores.indexOf(j);

        if (j == null) {
            JOptionPane.showMessageDialog(this, "No hay ningún jugador seleccionado.", "Sin jugador", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String elemento = (String) comboElementoModificar.getSelectedItem();
        String posición = (String) comboPosiciónModificar.getSelectedItem();
        String curso = (String) comboCursoModificar.getSelectedItem();
        int dorsal = Integer.parseInt(spinnerDorsalModificar.getValue().toString());
        Equipo e = (Equipo) comboEquipoModificar.getSelectedItem();

        {
            if (elemento == null) {
                JOptionPane.showMessageDialog(this, "Debes seleccionar un elemento.", "Elemento inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (posición == null) {
                JOptionPane.showMessageDialog(this, "Debes seleccionar una posición.", "Posición inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (curso == null) {
                JOptionPane.showMessageDialog(this, "Debes seleccionar un curso.", "Curso inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (dorsal <= 0 || dorsal > 127) {
                JOptionPane.showMessageDialog(this, "El dorsal del jugador es inválido.", "Dorsal inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (e == null) {
                JOptionPane.showMessageDialog(this, "El equipo del jugador es inválido.", "Equipo inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (imgJugador == null || !imgJugador.getAbsolutePath().endsWith(".webp")) {
                JOptionPane.showMessageDialog(this, "Imagen no válida: debe ser webp.", "Imagen inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        j.setCurso(curso);
        j.setElemento(elemento);
        j.setPosición(posición);
        j.setDorsal(dorsal);
        j.setIdEquipo(e.getId());

        jugadores.set(índiceJugador, j);
        actualizarEstadoComboJugadores();

        try {
            PreparedStatement ps = vp.getConn().prepareStatement("update jugadores set curso = ?, elemento = ?, posicion = ?, dorsal = ?, id_equipo = ?, url_imagen = ? where id = ?;");
            ps.setString(1, curso);
            ps.setString(2, elemento);
            ps.setString(3, posición);
            ps.setInt(4, dorsal);
            ps.setLong(5, e.getId());
            ps.setString(6, "jugadores/".concat(getImg()));
            ps.setLong(7, j.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(VentanaJugadores.class.getName()).log(Level.SEVERE, null, ex);
        }

        FTPUploader ftp = new FTPUploader(imgJugador, vp.getUsuarioFTP(), vp.getPassFTP(), true, vp.getIp());
        ftp.subirFichero();

    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnAbrirModificarActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnAbrirModificarActionPerformed
    {//GEN-HEADEREND:event_btnAbrirModificarActionPerformed
        imgJugador = abrirFileChooser();

        if (vi == null || !vi.isShowing()) {
            vi = new VisualizadorImágenes(imgJugador);
        }
    }//GEN-LAST:event_btnAbrirModificarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbrirAñadir;
    private javax.swing.JButton btnAbrirModificar;
    private javax.swing.JButton btnAnterior;
    private javax.swing.JButton btnAñadir;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnSiguiente;
    private javax.swing.JComboBox<String> comboCursoAñadir;
    private javax.swing.JComboBox<String> comboCursoModificar;
    private javax.swing.JComboBox<String> comboElementoAñadir;
    private javax.swing.JComboBox<String> comboElementoModificar;
    private javax.swing.JComboBox<Equipo> comboEquipoAñadir;
    private javax.swing.JComboBox<Equipo> comboEquipoModificar;
    private javax.swing.JComboBox<Jugador> comboJugadoresEliminar;
    private javax.swing.JComboBox<Jugador> comboJugadoresModificar;
    private javax.swing.JComboBox<String> comboPosiciónAñadir;
    private javax.swing.JComboBox<String> comboPosiciónModificar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel lblAcción;
    private javax.swing.JPanel panelAñadir;
    private javax.swing.JPanel panelEliminar;
    private javax.swing.JPanel panelModificar;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JSpinner spinnerDorsalAñadir;
    private javax.swing.JSpinner spinnerDorsalModificar;
    private javax.swing.JTextField txtNombreAñadir;
    private javax.swing.JTextField txtNombreModificar;
    // End of variables declaration//GEN-END:variables
}
