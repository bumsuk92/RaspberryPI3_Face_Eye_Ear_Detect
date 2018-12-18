package multi.yoramg.myapplication;

import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Client implements IClient{
    private Socket socket; //소켓
    private InputStream inputStream; // input Stream
    private OutputStream outputStream; //output Stream
    private static Client client;

    private Client() {
        connectToServer();
    }

    public static Client getClient() {
        if (client == null) {
            client = new Client();
        }
        return client;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void sendMessage(String data) {

    }

    @Override
    public void connectToServer() {
        Thread worker = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Log.i(this.getClass().getName(), "socket 연결 시도.");
                    //소켓을 생성하고 입출력 스트립을 소켓에 연결한다.
                    socket = new Socket("70.12.113.141", 6001); //소켓생성
                    Log.i(this.getClass().getName(), "socket 연결 시도 성공.");
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();

                    String startMessage = "#;;S;;&";
                    byte[] byteArray = startMessage.getBytes();
                    try{
                        outputStream.write(byteArray);
                    }catch(IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    connectToServer();
                    e.printStackTrace();
                }
            }
        });
        worker.start();
        try{
            worker.join();
        }catch(InterruptedException e) {}
    }

    public void stop() {
        sendMessage("#;;E;;&");

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveMessage() {
    }
}
