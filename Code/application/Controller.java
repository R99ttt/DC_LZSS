package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import LZSS.LZSSEncoderDecoder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

/**
 * Assignment Final
 * Submitted by: 
 * Student 1. Itamar Abir 	ID# 
 * Student 2. Rami Abo Rabia	ID# 
 */


public class Controller implements Initializable {
	@FXML
	TextField outputFile;

	@FXML
	TextField inputFile;

	//private LZSSEncoderDecoder LZSS;
	//private LZSSEncoderDecoder decoder;


	@FXML
	private Button startCompress;

	@FXML
	private ChoiceBox<Integer> SearchWindowSize;
	private Integer[] SearchWindowSizeVal = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 262144};

	@FXML
	private ChoiceBox<Integer> LookAhead;
	private Integer[] LookAheadVal = SearchWindowSizeVal;


	@FXML
	private ChoiceBox<Integer> MinMatchSize;
	private Integer[] MinMatchSizeVal = {2, 4, 6, 8, 10, 12, 14, 16, 18, 20};

	@FXML
	private TextField decompressOutputFile;

	@FXML
	private TextField decompressInputFile;

	@FXML
	private Button decompressLocation;

	@FXML
	private Button startDecompress;


	@FXML
	public void location(ActionEvent e) throws IOException {		

		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

		FileChooser fc = new FileChooser();
		fc.setTitle("Select a file to be Encoded");

		File selectedFile = fc.showOpenDialog(stage);

		if (selectedFile != null) { 

			inputFile.setText(selectedFile.getPath()); // Set the text of the inputFile TextField
			outputFile.setText(selectedFile.getParent());

		}

	}


	@FXML
	public void compress(ActionEvent e) throws IOException {

		int selectedSearchWindowSize = SearchWindowSize.getValue();
		int selectedLookAhead = LookAhead.getValue();
		int selectedMinMatchSize = MinMatchSize.getValue();

		LZSSEncoderDecoder LZSS = new LZSSEncoderDecoder(selectedSearchWindowSize, selectedLookAhead, selectedMinMatchSize);

		String inputPath = inputFile.getText();
		String outputPath = outputFile.getText();

		// Build the path for the compressed file
		String compressedFileName = "encoded.txt";
		String compressedFilePath = outputPath + File.separator + compressedFileName;

		String[] input_names = new String[] { inputPath };
		String[] output_names = new String[] { compressedFilePath };

		LZSS.Compress(input_names, output_names);

		displayAlert("Compression completed. File is ready.");

	}

	private void displayAlert(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Compression Complete");
		alert.setHeaderText(null);
		alert.setContentText(message);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.setAlwaysOnTop(true); // This will keep the alert dialog on top

		alert.showAndWait();
	}




	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		SearchWindowSize.getItems().addAll(SearchWindowSizeVal);
		MinMatchSize.getItems().addAll(MinMatchSizeVal);
		LookAhead.getItems().addAll(LookAheadVal);

	}
	@FXML
	public void decompressLocation(ActionEvent e) throws IOException {

		Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

		FileChooser fc = new FileChooser();
		fc.setTitle("Select a file to be Decoded");

		File selectedFile = fc.showOpenDialog(stage);

		if (selectedFile != null) { 

			decompressInputFile.setText(selectedFile.getPath()); // Set the text of the inputFile TextField			
			decompressOutputFile.setText(selectedFile.getParent());

		}
	}

	@FXML
	public void decompress(ActionEvent e) throws IOException, ClassNotFoundException {
		String inputPath = decompressInputFile.getText();
		String outputPath = decompressOutputFile.getText();
		
		//default values we dont care what it gets because it will change since its reading from the file
		LZSSEncoderDecoder decoder = new LZSSEncoderDecoder(64, 32, 4); 

		
		// Build the path for the decompressed file
		String decompressedFileName = "decoded.txt";
		String decompressedFilePath = outputPath + File.separator + decompressedFileName;

		String[] input_names = new String[] { inputPath };
		String[] output_names = new String[] { decompressedFilePath };

		decoder.Decompress(input_names, output_names);

		displayAlert("Decompression completed. File is ready.");
	}






}
