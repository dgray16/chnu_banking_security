package client.controller;

import client.entity.EDIMessage;
import client.entity.EncryptedEDIMessage;
import client.util.EDIMessageHeaders;
import client.util.CryptographyService;
import client.util.Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML private ComboBox<Label> documentTypeComboBox;

    @FXML private TextField summary;

    @FXML private TextArea purpose;


    @FXML private TextField sendersName;

    @FXML private TextField sendersBankAccount;

    @FXML private TextField sendersBankName;

    @FXML private TextField sendersBankCode;


    @FXML private TextField recipientsName;

    @FXML private TextField recipientsBankAccount;

    @FXML private TextField recipientsBankName;

    @FXML private TextField recipientsBankCode;

    private Alert alert;

    private int headerNumber;

    private String publicRSAKey;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* Setup document type ComboBox */
        documentTypeComboBox.getItems().add(new Label(EDIMessageHeaders.DocumentType.PAYMENT.getFullName()));
    }

    @FXML
    @SuppressWarnings("unused")
    private void proceedTransaction() {
        if ( !checkEmptyFields() ) {
            EDIMessage plainTextMessage = EDIMessage.create(
                    generateHeader(),
                    LocalDateTime.now(),
                    Duration.ofHours(24),
                    getDocumentType(),
                    sendersName.getText(),
                    sendersBankName.getText(),
                    sendersBankCode.getText(),
                    sendersBankAccount.getText(),
                    recipientsName.getText(),
                    recipientsBankName.getText(),
                    recipientsBankCode.getText(),
                    recipientsBankAccount.getText(),
                    purpose.getText(),
                    Currency.getInstance("UAH"),
                    new BigDecimal(summary.getText()).setScale(3, RoundingMode.HALF_UP)
                    );

            EncryptedEDIMessage encryptedEDIMessage = EncryptedEDIMessage.create(
                    plainTextMessage, CryptographyService.generateSecureRandomPassword(), generateHeader());

            getPublicRSAKey();

            sendEncryptionInfoMessage(encryptedEDIMessage.getInfoMessage());

            getValidationResponseFromServer(encryptedEDIMessage);

        } else {
            sendAlert("Something is empty");
        }
    }

    private void getPublicRSAKey() {
        new Thread(() -> {
            try {
                InetAddress ip = InetAddress.getByName(Util.HOST);
                Socket socket = new Socket(ip, Util.PUBLIC_RSA_KEY_PORT);

                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line = null;
                StringBuilder stringBuilder = new StringBuilder();

                while ( (line = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(line);
                }

                publicRSAKey = stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getValidationResponseFromServer(EncryptedEDIMessage encryptedEDIMessage) {
        new Thread(() -> {

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(Util.DECRYPTION_INFO_VALIDATION_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                while (true) {
                    Socket socket = serverSocket.accept();

                    InputStream inputStream = socket.getInputStream();
                    DataInputStream dataInputStream = new DataInputStream(inputStream);

                    Integer statusCode = dataInputStream.readInt();

                    if ( statusCode == 1 ) {
                        sendMessage(encryptedEDIMessage);
                    } else {
                        sendAlert("Your data is invalid");
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private void sendEncryptionInfoMessage(EncryptedEDIMessage.EncryptionInfoMessage infoMessage) {
        new Thread(() -> {

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                InetAddress ip = InetAddress.getByName(Util.HOST);
                Socket socket = new Socket(ip, Util.DECRYPTION_INFO_PORT);

                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

                printWriter.println(CryptographyService.RSA(infoMessage.toString(), publicRSAKey));

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMessage(EncryptedEDIMessage encryptedEDIMessage) {
        new Thread(() -> {
            try {
                Thread.sleep(350);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                InetAddress ip = InetAddress.getByName(Util.HOST);
                Socket socket = new Socket(ip, Util.DECRYPTION_MESSAGE_PORT);

                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                dataOutputStream.writeInt(encryptedEDIMessage.getEncrypted().length);
                dataOutputStream.write(encryptedEDIMessage.getEncrypted());

            } catch (IOException e) {
                e.printStackTrace();
            }


        }).start();
    }

    /**
     * It gets new header number from internal DB from server.
     * I use it 2 times per transaction, one for message header, another for infoMessage header.
     */
    private int generateHeader() {
        try {
            InetAddress ip = InetAddress.getByName(Util.HOST);
            Socket socket = new Socket(ip, Util.HEADER_PORT);

            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            headerNumber = dataInputStream.readInt();
            socket.close();

        } catch (IOException e) {
            Platform.runLater(() -> sendAlert("Server offline"));
            e.printStackTrace();
        }

        return headerNumber;
    }

    private EDIMessageHeaders.DocumentType getDocumentType() {
        return documentTypeComboBox.getItems().get(0).getText().equals(EDIMessageHeaders.DocumentType.PAYMENT.getFullName())
                ? EDIMessageHeaders.DocumentType.PAYMENT
                : EDIMessageHeaders.DocumentType.INVOICE;
    }

    /**
     * Checks if Strings are empty or not.
     *
     * @return true if something is empty, false if everything is good
     */
    private boolean checkEmptyFields() {
        return documentTypeComboBox.getSelectionModel().getSelectedItem() == null || summary.getText().isEmpty() ||
                purpose.getText().isEmpty() || sendersName.getText().isEmpty() ||
                sendersBankAccount.getText().isEmpty() || sendersBankName.getText().isEmpty() ||
                sendersBankCode.getText().isEmpty() ||recipientsName.getText().isEmpty() ||
                recipientsBankAccount.getText().isEmpty() || recipientsBankName.getText().isEmpty() ||
                recipientsBankCode.getText().isEmpty();

    }

    private void sendAlert(String message) {
        if ( alert == null ) {
            alert = new Alert(Alert.AlertType.ERROR);
        }

        alert.setContentText(message);
        alert.showAndWait();
    }

}
