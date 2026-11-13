package ni.edu.uam.ejercicioc3.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.ejercicioc3.dao.EquipoDAO;
import ni.edu.uam.ejercicioc3.dao.TipoEquipoDAO;
import ni.edu.uam.ejercicioc3.model.Equipo;
import ni.edu.uam.ejercicioc3.model.TipoEquipo;

import java.util.List;
import java.util.Optional;

public class LaboratorioController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtMarca;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<TipoEquipo> cmbTipo;
    @FXML private ComboBox<String> cmbEstado;

    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnBuscar;
    @FXML private Button btnQuitarFiltros;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<TipoEquipo> cmbFiltroTipo;

    @FXML private TableView<Equipo> tblEquipos;
    @FXML private TableColumn<Equipo, Long> colId;
    @FXML private TableColumn<Equipo, String> colCodigo;
    @FXML private TableColumn<Equipo, String> colNombre;
    @FXML private TableColumn<Equipo, String> colMarca;
    @FXML private TableColumn<Equipo, String> colEstado;
    @FXML private TableColumn<Equipo, Integer> colCantidad;
    @FXML private TableColumn<Equipo, String> colTipo;

    private final EquipoDAO equipoDAO = new EquipoDAO();
    private final TipoEquipoDAO tipoEquipoDAO = new TipoEquipoDAO();

    private Long idSeleccionado = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idEquipo"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        colTipo.setCellValueFactory(data -> {
            TipoEquipo tipo = data.getValue().getTipoEquipo();
            return new SimpleStringProperty(tipo != null ? tipo.getNombre() : "Sin tipo");
        });

        cmbEstado.setItems(FXCollections.observableArrayList(Equipo.ESTADOS));

        tblEquipos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                seleccionarEquipo(nuevo);
            } else {
                idSeleccionado = null;
                actualizarBotones();
            }
        });

        cargarTipos();
        refrescarTabla();
        actualizarBotones();
    }

    private void cargarTipos() {
        try {
            List<TipoEquipo> tipos = tipoEquipoDAO.listar();
            cmbTipo.setItems(FXCollections.observableArrayList(tipos));
            cmbFiltroTipo.setItems(FXCollections.observableArrayList(tipos));
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudieron cargar los tipos: " + mensajeCausa(ex));
        }
    }

    private void refrescarTabla() {
        try {
            List<Equipo> equipos = equipoDAO.buscar(txtBuscar.getText(), cmbFiltroTipo.getValue());
            tblEquipos.setItems(FXCollections.observableArrayList(equipos));
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo cargar la tabla: " + mensajeCausa(ex));
        }
    }

    private void seleccionarEquipo(Equipo equipo) {
        idSeleccionado = equipo.getIdEquipo();
        txtCodigo.setText(equipo.getCodigo());
        txtNombre.setText(equipo.getNombre());
        txtMarca.setText(equipo.getMarca() != null ? equipo.getMarca() : "");
        txtCantidad.setText(String.valueOf(equipo.getCantidad()));
        cmbEstado.setValue(equipo.getEstado());

        cmbTipo.getItems().stream()
                .filter(t -> t.getIdTipo().equals(equipo.getTipoEquipo().getIdTipo()))
                .findFirst()
                .ifPresent(cmbTipo::setValue);

        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;
        btnActualizar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);
    }

    private Equipo leerFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String marca = txtMarca.getText().trim();
        String textoCantidad = txtCantidad.getText().trim();
        TipoEquipo tipo = cmbTipo.getValue();
        String estado = cmbEstado.getValue();

        if (codigo.isEmpty()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }
        if (codigo.length() > 30) {
            throw new IllegalArgumentException("El código no puede superar 30 caracteres.");
        }
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (nombre.length() > 120) {
            throw new IllegalArgumentException("El nombre no puede superar 120 caracteres.");
        }
        if (marca.length() > 80) {
            throw new IllegalArgumentException("La marca no puede superar 80 caracteres.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Seleccione un tipo de equipo.");
        }
        if (estado == null || !Equipo.ESTADOS.contains(estado)) {
            throw new IllegalArgumentException("Seleccione un estado válido.");
        }
        if (textoCantidad.isEmpty()) {
            throw new IllegalArgumentException("La cantidad es obligatoria.");
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(textoCantidad);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero.");
        }
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }

        return new Equipo(codigo, nombre, marca.isEmpty() ? null : marca, estado, cantidad, tipo);
    }

    @FXML
    private void guardar() {
        try {
            Equipo equipo = leerFormulario();
            if (equipoDAO.existeCodigo(equipo.getCodigo(), null)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Ya existe un equipo con el código " + equipo.getCodigo() + ".");
                return;
            }
            equipoDAO.guardar(equipo);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Equipo guardado correctamente.");
            refrescarTabla();
            limpiar();
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo guardar el equipo: " + mensajeCausa(ex));
        }
    }

    @FXML
    private void actualizar() {
        if (idSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione un equipo de la tabla.");
            return;
        }
        try {
            Equipo equipo = leerFormulario();
            equipo.setIdEquipo(idSeleccionado);

            if (equipoDAO.existeCodigo(equipo.getCodigo(), idSeleccionado)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Ya existe otro equipo con el código " + equipo.getCodigo() + ".");
                return;
            }

            if (equipoDAO.actualizar(equipo)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Equipo actualizado correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "El equipo ya no existe en la base de datos.");
            }
            refrescarTabla();
            limpiar();
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar el equipo: " + mensajeCausa(ex));
        }
    }

    @FXML
    private void eliminar() {
        if (idSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione un equipo de la tabla.");
            return;
        }
        try {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText("Eliminar equipo");
            confirmacion.setContentText("¿Está seguro que desea eliminar el equipo seleccionado?");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                return;
            }

            if (equipoDAO.eliminar(idSeleccionado)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Equipo eliminado correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "El equipo ya no existe en la base de datos.");
            }
            refrescarTabla();
            limpiar();
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar el equipo: " + mensajeCausa(ex));
        }
    }

    @FXML
    private void limpiar() {
        tblEquipos.getSelectionModel().clearSelection();
        idSeleccionado = null;
        txtCodigo.clear();
        txtNombre.clear();
        txtMarca.clear();
        txtCantidad.clear();
        cmbTipo.setValue(null);
        cmbEstado.setValue(null);
        actualizarBotones();
    }
    //Consultas
    @FXML
    private void buscar() {
        refrescarTabla();
    }

    @FXML
    private void quitarFiltros() {
        txtBuscar.clear();
        cmbFiltroTipo.setValue(null);
        refrescarTabla();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private String mensajeCausa(Throwable ex) {
        Throwable causa = ex;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa.getMessage() != null ? causa.getMessage() : ex.toString();
    }
}
