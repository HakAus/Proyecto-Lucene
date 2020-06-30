package lucene;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ControladorGeneral implements Controlador {


    // Variables JAVAFX
    @FXML private StackPane stkPane;
    @FXML private TabPane tbpVentanas;
    @FXML private Tab tabIndexacion, tabBusqueda;
    @FXML private GridPane grdIndexacion, grdBusqueda;
    @FXML public TableView<DocumentoEncontrado> tblEscalafon;
    @FXML public TableColumn<String,DocumentoEncontrado> clmPosicion;
    @FXML public TableColumn<String,DocumentoEncontrado> clmTitulo;
    @FXML public TableColumn<String,DocumentoEncontrado> clmPagina;
    @FXML public TableColumn<String,DocumentoEncontrado> clmPuntaje;
    @FXML private TextField tfdDirectorioIndexacion, tfdArchivoIndexar, tfdDirectorioIndice,
                      tfdConsulta;
    @FXML private Button btnIndexar, btnBuscar,btnVerMas;
    @FXML private RadioButton rdbActualizar;
    @FXML private Label lblEstadoIndexacion, lblDocumentosEncontrados, lblDocumentosRestantes, lblTiempoConsulta;
    @FXML private ComboBox<String> cbxCampos;

    // Variables LUCENE
    Lector lector;
    Indexador indexador;
    Buscador buscador;

    // Otras variables
    Alert alerta = new Alert(Alert.AlertType.WARNING,"");
    ArrayList<Html_Indexado> archivos;

    @Override
    public void prepararVentana() {
        llenarComboBox();
        configurarTabla();
        Analizador analizadores = new Analizador();
        try {
            analizadores.leerStopWords("stop_words");

        }
        catch (IOException e){
            alerta.setTitle("ERROR");
            alerta.setHeaderText("Hubo un problema al cargar el archivo de stop words");
            alerta.setContentText("Revise que el nombre de archivo especificado es correcto.");
            alerta.show();
        }
        lector = new Lector();
        indexador = new Indexador(analizadores);
        buscador = new Buscador(analizadores, lblDocumentosEncontrados, lblTiempoConsulta);
    }

    public void llenarComboBox(){
        if (cbxCampos.getItems().size() == 0)
            cbxCampos.getItems().addAll("titulo","texto","encab","ref");
    }

    public void configurarTabla(){
        clmPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        clmTitulo.setCellValueFactory(new PropertyValueFactory<>("tituloMostrar"));
        clmPagina.setCellValueFactory(new PropertyValueFactory<>("btnPagina"));
        clmPuntaje.setCellValueFactory(new PropertyValueFactory<>("puntaje"));
        tblEscalafon.setPlaceholder(new Label("No hay resultados"));
    }

    public void limpiarTabla(){
        tblEscalafon.getItems().clear();
    }

    public boolean validarCamposIndexacion(){
        return !tfdArchivoIndexar.getText().equals("")
                && !tfdDirectorioIndexacion.getText().equals("");
    }

    public boolean validarCamposBusqueda(){
        return !tfdDirectorioIndice.getText().equals("") &&
                !cbxCampos.getSelectionModel().getSelectedItem().equals("") &&
                !tfdConsulta.getText().equals("");
    }

    public void indexar(ActionEvent actionEvent) {
        if (validarCamposIndexacion()){
            try {
                archivos = lector.obtenerDocumentos(tfdArchivoIndexar.getText());
            }
            catch (IOException e){
                alerta.setTitle("ERROR");
                alerta.setHeaderText("Hubo un error al leer la colección de documentos");
                alerta.show();
            }
            try {
                lblEstadoIndexacion.setText("Indexando ...");
                long inicio = System.currentTimeMillis();
                indexador.configurarIndexador(tfdDirectorioIndexacion.getText(),rdbActualizar.isSelected());
                int contador = 0;
                for (Html_Indexado html : archivos) {
                    indexador.indexarContenidos(html);
                    contador++;
                }
                // Se cierra la escritura
                indexador.writer.close();
                long fin = System.currentTimeMillis();
                double tiempo = (double) ((fin - inicio)/1000);

                lblEstadoIndexacion.setText(contador + " documentos indexados de la coleccion "
                                            + tfdArchivoIndexar.getText() + " en " + tiempo
                                            + " segundos");
            }
            catch (IOException e){
                alerta.setTitle("ERROR");
                alerta.setHeaderText("Hubo un error durante la indexación");
                alerta.show();
            }
        }
        else {
            alerta.setTitle("ALERTA");
            alerta.setHeaderText("Alguno de los campos esta vacio");
            alerta.show();
        }
    }

    public void buscar(ActionEvent actionEvent){
        limpiarTabla();
        if (validarCamposBusqueda()){
            ArrayList<DocumentoEncontrado> resultados = buscador.buscarDocumento(tfdDirectorioIndice.getText(),
                    cbxCampos.getSelectionModel().getSelectedItem(), tfdConsulta.getText(), 20);
            for (DocumentoEncontrado doc : resultados){
                tblEscalafon.getItems().add(doc);
            }
        }
        else {
            alerta.setTitle("ALERTA");
            alerta.setHeaderText("Por favor, asegúrese de llenar todos los campos");
            alerta.show();
        }
    }

    public void testHtml(ActionEvent actionEvent){
        File htmlFile = new File("test");
        try {
            Desktop.getDesktop().browse(htmlFile.toURI());
        }
        catch (IOException e){
            System.out.println("Hubo un problema al cargar la pagina web");
        }
    }

}
