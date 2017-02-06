package sample;/**
 * Created by Nishant Sharma on 8/12/2016.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class client extends Application {
    private TextField usertext;
    private TextArea chatwindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message="";
    private String serverIP;
    TextField userText;
    private Socket connection;
    ProgressIndicator progressIndicator;
    Service<Void> background;
    VBox vbox;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        serverIP="172.16.22.182";
        primaryStage.setTitle("Client");
        vbox=new VBox(20);
         userText=new TextField();
        userText.setOnAction(e->{
            sendMessage(userText.getText());
            userText.setText("");
        });
        userText.setDisable(true);
        userText.setEditable(false);
        progressIndicator=new ProgressIndicator(-1);
       // progressIndicator.progressProperty().bind(background.progressProperty());*/
        HBox hBox=new HBox(userText);
        hBox.setSpacing(30);
        userText.setMinSize(300,50);
        chatwindow=new TextArea();
        chatwindow.setMinSize(300,500);
        chatwindow.setMaxSize(300,500);
        // chatwindow.setEditable(false);
        vbox.setPadding(new Insets(10,10,10,10));
        background=new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                    connecting_to_server();
                        settingUpStreams();
                        whileChating();
                        return null;
                    }
                };
            }
        };
        Scene scene=new Scene(vbox, 400, 600);
        scene.getStylesheets().add("/sample/top.css");
        vbox.getChildren().addAll(hBox,chatwindow);
        background.restart();
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void connecting_to_server() throws IOException {
        showMessage("Attempting Connection...\n");
        connection=new Socket(InetAddress.getByName(serverIP),3128);
        showMessage("connected to:"+connection.getInetAddress().getHostName());

    }

    private void whileChating() throws IOException {
        String message="you are now connect";
      //  sendMessage(message);
        //abletotype(true);
        do{
            try
            {
                message=(String)input.readObject();
                showMessage("\n"+message);
            }
            catch (ClassNotFoundException classNotFound)
            {
                showMessage("\n wtf that user has send");
            }
        }while (!message.equals("SERVER-END"));
    }

    private  void settingUpStreams() throws IOException {
        output=new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input=new ObjectInputStream(connection.getInputStream());
        showMessage("\n Streams are now connected");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                userText.setEditable(true);
                userText.setDisable(false);
                progressIndicator.setProgress(1);

            }
        });
    }

    private void showMessage(String s) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                chatwindow.appendText(s);
            }
        });
    }

    private void sendMessage(String message) {
        try{
            output.writeObject("CLIENT-"+message);
            output.flush();
            showMessage("\nCLIENT-"+message);
        }catch (IOException io)
        {
            io.printStackTrace();
            chatwindow.appendText("\n message not send");

        }
    }
}
