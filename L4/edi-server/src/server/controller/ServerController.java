package server.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import server.util.CryptographyService;
import server.util.DatabaseService;
import server.util.EDIValidator;
import server.util.Util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.ResourceBundle;



public class ServerController implements Initializable {

    @FXML private TextArea log;

    private String encryptedInfoMessage;

    private String decryptedInfoMessage;

    private String encryptedEDIMessageCipher;

    private String encryptedEDIMessageKey;

    private byte[] encryptedEDIMessage;

    private String decryptedEDIMessage;

    private int generateHeaderNumber() {
        Integer previousLastNumber = Integer.valueOf(DatabaseService.getLastNumber());
        Integer newLastNumber = previousLastNumber + 1;

        /* After generation I need also to save generated number in database */
        DatabaseService.setNewLastNumber(newLastNumber);

        return  newLastNumber;
    }

    @FXML
    @SuppressWarnings("unused")
    private void clearLog() {
        log.setText("");
    }

    public void initialize(URL location, ResourceBundle resources) {

        /*
         * TODO
         * - Make possible to click 2x times on button proceed transaction
         * - Anyway something wrong with threads, even with Thread.sleep()
         */

        DatabaseService.createConnection();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                DatabaseService.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));

        setUpHeaderSocket();
        setUpKeysSocket();
        setUpDecryptionMessageSocket();
        setUpMessageSocket();
    }

    /** Thread to accept Encrypted EDI message */
    private void setUpMessageSocket() {
        new Thread(() -> {

            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ServerSocket serverSocket = null;

            try {
                serverSocket = new ServerSocket(Util.DECRYPTION_MESSAGE_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            log.appendText("Waiting for new decryption message... \n");

            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    log.appendText("\nNew decryption message accepted \n");

                    InputStream inputStream = socket.getInputStream();
                    DataInputStream dataInputStream = new DataInputStream(inputStream);

                    int length = dataInputStream.readInt();
                    encryptedEDIMessage = new byte[length];

                    dataInputStream.readFully(encryptedEDIMessage, 0, encryptedEDIMessage.length);

                    decryptMessage();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private void decryptMessage() {
        byte[] decodedKey = Base64.getDecoder().decode(encryptedEDIMessageKey);
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, encryptedEDIMessageCipher);

        byte[] decrypted = null;

        try {
            final Cipher cipher = Cipher.getInstance(encryptedEDIMessageCipher);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            decrypted = cipher.doFinal(encryptedEDIMessage);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        decryptedEDIMessage = validateDecryptedEDIMessage(new String(decrypted)) ? new String(decrypted) : "Message not valid";
        log.appendText("\n\nDecrypted EDI message: " + decryptedEDIMessage);
    }

    private boolean validateDecryptedEDIMessage(String inputText) {
        String[] blocks = inputText.split("'");
        Integer expectedNumberOfBlocks = Integer.parseInt(blocks[blocks.length - 1].split("[+]")[1]);

        return expectedNumberOfBlocks == blocks.length;
    }

    /** Thread to send new header number */
    private void setUpHeaderSocket() {
        new Thread(() -> {

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(Util.HEADER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.appendText("Waiting for header number request... \n");

            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    log.appendText("New header number request accepted \n");

                    OutputStream outputStream = socket.getOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                    dataOutputStream.writeInt(generateHeaderNumber());

                    socket.close();
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

    /** Thread to send public RSA key */
    private void setUpKeysSocket() {
        new Thread(() -> {

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(Util.PUBLIC_RSA_KEY_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.appendText("Waiting for RSA public key request... \n");

            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    log.appendText("New RSA public key request accepted \n");

                    if ( CryptographyService.lastKey == null ) {
                        CryptographyService.generateRSAKeys();
                    }

                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println(Base64.getEncoder().encodeToString(CryptographyService.lastKey.getPublic().getEncoded()));

                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /** Thread to accept decryption info message */
    private void setUpDecryptionMessageSocket() {
        new Thread(() -> {

            /* Maybe JavaFX components are not thread safe, that is why I got Thread Exceptions without these lines of code */
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(Util.DECTYPRION_INFO_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.appendText("Waiting for new decryption info messages... \n");

            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    log.appendText("New decryption info message accepted \n");

                    InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    String line = null;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ( (line = bufferedReader.readLine()) != null ) {
                        stringBuilder.append(line);
                    }

                    encryptedInfoMessage = stringBuilder.toString();
                    log.appendText("\n\nEncrypted info message: " + encryptedInfoMessage);

                    decryptInfoMessage();

                    /* If InfoMessageValid then I will say client that he should send me message */
                    if ( EDIValidator.validate(decryptedInfoMessage) ) {
                        log.appendText("\nDecryptionInfoMessage is valid");
                        parseDecryptedInfoMessage();
                        sendDecryptionInfoValidationResponse();
                    } else {
                        log.appendText("\nDecryptionInfoMessage not valid");
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void parseDecryptedInfoMessage() {
        String[] blocks = decryptedInfoMessage.split("'");

        encryptedEDIMessageCipher = blocks[4].split("[+]")[1];
        encryptedEDIMessageKey = blocks[5].split("[+]")[1];
    }

    private void sendDecryptionInfoValidationResponse() {
        new Thread(() -> {

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                InetAddress ip = InetAddress.getByName(Util.HOST);
                Socket socket = new Socket(ip, Util.DECRYPTION_INFO_VALIDATION_PORT);

                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                /* 1 means message is valid, 0 means not valid*/
                dataOutputStream.writeInt(1);

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private void decryptInfoMessage() {
        byte[] decrypted = null;
        try {
            final Cipher cipher = Cipher.getInstance("RSA");

            cipher.init(Cipher.DECRYPT_MODE, CryptographyService.lastKey.getPrivate());

            decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedInfoMessage));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        decryptedInfoMessage = new String(decrypted);
        log.appendText("\n\nDecrypted info message: " + decryptedInfoMessage);
    }

}
