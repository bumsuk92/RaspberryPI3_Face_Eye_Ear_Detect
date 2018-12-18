package multi.yoramg.mainserver;

import java.net.ServerSocket;
public class MainServer {
	 public static void main(String[] args) {        
	        System.out.println("-SERVER 시작");
	        
	        try {
	            ServerSocket mainServerSocket = null;
	            mainServerSocket = new ServerSocket(6001);
	            //mainServerSocket.bind(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), 4040));
	            
	            ConnectThread connectThread = new ConnectThread(mainServerSocket);
	            connectThread.start();
	            
	        } catch (Exception e) {}
//	        System.out.println("-SERVER 종료");
	    }
}
