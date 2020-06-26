package lucene;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class Controller {

    // JAVAFX
    @FXML
    private StackPane stkPane;
    @FXML
    private TabPane tbpVentanas;
    @FXML
    private Tab tabIndexacion, tabBusqueda;
    @FXML
    private GridPane grdIndexacion, grdBusqueda;
    @FXML
    private TextField tfdDirectorioIndexacion, tfdArchivoIndexar, tfdDirectorioIndice,
                      tfdConsulta;
    @FXML
    private Button btnIndexar, btnBuscar,btnVerMas;
    @FXML
    private RadioButton rdbActualizar;
    @FXML
    private Label lblEstadoIndexacion;
    @FXML
    private ComboBox<String> cbxCampos;
    @FXML
    private ListView<?> lvwEscalafon;

    // LUCENE
    Lector lector = new Lector();
    Indexador indexador = new Indexador();
    Buscador buscador = new Buscador();
    Alert alerta = new Alert(Alert.AlertType.WARNING,"");
    ArrayList<String> archivos;


    public void llenarComboBox(){
        if (cbxCampos.getItems() != null)
            cbxCampos.getItems().addAll("titulo","texto","encab","ref");
    }
    public boolean validarCamposIndexacion(){
        return !tfdArchivoIndexar.getText().equals("")
                && !tfdDirectorioIndexacion.getText().equals("");
    }

    public void indexar(ActionEvent actionEvent) {
        if (validarCamposIndexacion()){
            try {
                archivos = lector.obtenerDocumentos(tfdArchivoIndexar.getText());
            }
            catch (IOException e){
                alerta.setTitle("ERROR");
                alerta.setHeaderText("Hubo un error al leer la coleccion de documentos");
                alerta.show();
            }
            try {
                lblEstadoIndexacion.setText("Indexando ...");
                long inicio = System.currentTimeMillis();
                indexador.configurarIndexador(tfdDirectorioIndexacion.getText(),rdbActualizar.isSelected());
                int cont = 0;
                for (String html : archivos) {
                    indexador.indexarContenidos(html);
                    cont++;
                }
                // Se cierra la escritura
                indexador.writer.close();
                long fin = System.currentTimeMillis();
                double tiempo = (double) ((fin - inicio)/1000);

                lblEstadoIndexacion.setText(cont + " documentos indexados de la coleccion "
                                            + tfdArchivoIndexar.getText() + " en " + tiempo
                                            + " segundos");
            }
            catch (IOException e){
                alerta.setTitle("ERROR");
                alerta.setHeaderText("Hubo un error durante la indexacion");
                alerta.show();
            }
        }
        else {
            alerta.setTitle("ERROR");
            alerta.setHeaderText("Alguno de los campos esta vacio");
            alerta.show();
        }


    }

    public void buscar(ActionEvent actionEvent){
        buscador.buscarDocumento(tfdDirectorioIndice.getText(),
                                cbxCampos.getSelectionModel().getSelectedItem(),
                                tfdConsulta.getText(),
                                20);
    }

}
