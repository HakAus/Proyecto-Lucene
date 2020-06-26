package lucene;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class Controller {

    @FXML
    private StackPane stkPane;

    @FXML
    private TabPane tbpVentanas;

    @FXML
    private Tab tabIndexacion;

    @FXML
    private GridPane grdIndexacion;

    @FXML
    private TextField tfdDirectorioIndexacion;

    @FXML
    private TextField tfdArchivoIndexar;

    @FXML
    private Button btnIndexar;

    @FXML
    private RadioButton rdbActualizar;

    @FXML
    private Label lblEstadoIndexacion;

    @FXML
    private Tab tabBusqueda;

    @FXML
    private GridPane grdBusqueda;

    @FXML
    private TextField tfdDirectorioIndice;

    @FXML
    private TextField tfdConsulta;

    @FXML
    private Button btnBuscar;

    @FXML
    private ComboBox<?> cbxCampos;

    @FXML
    private ListView<?> lvwEscalafon;

    @FXML
    private Button btnVerMas;

}
