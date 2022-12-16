package src.networking.clientserver.GUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JTextArea;

public class ClientHandler implements Runnable {
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientname;
    

    public ClientHandler(Socket socket) {

        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientname = bufferedReader.readLine();
            
            clientHandlers.add(this);
            message_transfer("Server: " + clientname + " has entered the chat!\n");
        } catch (IOException e) {
            closeEverything(this.socket, bufferedReader, bufferedWriter);
        }

    }

    @Override
    public void run() {
        String receiveMessage;
        while (socket.isConnected()) {
            try {
                receiveMessage = bufferedReader.readLine();
                message_transfer(receiveMessage);
            } catch (IOException e) {
                closeEverything(this.socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void message_transfer(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (!clientHandler.clientname.equals(this.clientname)) {
                try {
                    clientHandler.bufferedWriter.write(message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } catch (IOException e) {
                    closeEverything(this.socket, bufferedReader, bufferedWriter);
                }
            }
        }
    }

    public void removeHandlers() {
        clientHandlers.remove(this);
        message_transfer("Server: " + clientname + " has left the chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeHandlers();
        try {

            if (socket != null) {
                socket.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
