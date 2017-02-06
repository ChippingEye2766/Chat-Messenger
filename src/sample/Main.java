package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.concurrent.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main extends Application {
    ServerSocket server;
    Socket connection;
    Service<Void> background;
    ObjectOutputStream output;
    ObjectInputStream input;
    ProgressIndicator progressIndicator;
    TextField userText;
    TextArea chatwindow;
    int x=1;
    VBox vbox;
    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Messanger");
         vbox=new VBox(20);
         userText=new TextField();
        userText.setOnAction(e->{
            sendMessage(userText.getText());
            userText.setText("");
        });
        userText.setDisable(true);
        userText.setEditable(false);
         progressIndicator=new ProgressIndicator(-1);
    //    progressIndicator.progressProperty().bind(background.progressProperty());
        HBox hBox=new HBox(userText,progressIndicator);
        hBox.setSpacing(30);
        userText.setMinSize(300,50);
         chatwindow=new TextArea();
        chatwindow.setMinSize(300,500);
        chatwindow.setMaxSize(300,500);
       // chatwindow.setDisable(true);
       // chatwindow.setEditable(false);
        vbox.setPadding(new Insets(10,10,10,10));
        background=new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        messanger();
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
        primaryStage.setResizable(false);
    }
    public void messanger(){
        try{
           server=new ServerSocket(3128,100);
            while(true)
            {
                try{
                    waiting();
                    setupStream();
                    whilechatting();
                }catch (EOFException eofexception)
                {
                    showMessage("\n Server has ended");
                }
                finally {
                    closecrap();
                }
            }


       }catch(IOException ioexception)
       {
           ioexception.printStackTrace();
       }
    }

    private void setupStream() throws  IOException{
        output=new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input=new ObjectInputStream(connection.getInputStream());
        showMessage("\n Streams are now connected");
    }
    private void whilechatting() throws IOException {
    String message="you are now connect";
     //   sendMessage(message);
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
        }while (!message.equals("CLIENT-END"));
    }

    private void sendMessage(String message) {

        try{
             output.writeObject("SERVER-"+message);
            output.flush();
            showMessage("\nSERVER-"+message);
        }catch (IOException io)
        {
             io.printStackTrace();
            chatwindow.appendText("\n messange not send");

        }
    }

    private void closecrap() throws IOException {
        showMessage("\n Closing connection");
        output.close();
        input.close();
        connection.close();
    }

    private void waiting() throws IOException{
        showMessage("\nWaiting for someone to connect");
        connection=server.accept();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                userText.setEditable(true);
                userText.setDisable(false);
                progressIndicator.setProgress(1);

            }
        });
        showMessage("\n now connected to "+connection.getInetAddress().getHostName());
    }

    private void showMessage(String s) {
       Platform.runLater(new Runnable() {
           @Override
           public void run() {
               chatwindow.appendText(s);
           }
       });

    }


    public static void main(String[] args) {
        launch(args);
    }
}
