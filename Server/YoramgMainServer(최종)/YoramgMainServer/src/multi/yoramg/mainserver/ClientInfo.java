package multi.yoramg.mainserver;

import java.net.Socket;

public class ClientInfo {
	Socket mobileSocket;
    Socket deviceSocket;
    String id;				
    String serial;
        
    // info 생성자
    ClientInfo(Socket socket, String str, String division) {
    	// 모바일 정보 생성시
    	if ( division.equals("#") ) {
    		this.mobileSocket = socket;
    		this.id = str;
    	}
    	// 스마트아기침대 정보 생성시 필요한가? 디바이스가 무적권 먼저 들어왔있도록 한다면 파트너 디바이스 info찾아서 빈칸 채워주면 될것같음
    	else if ( division.equals("$") ) {
    		this.deviceSocket = socket;
    		this.serial = str;
    	}
    	else {
    		System.out.println("ClientInfo 생성 잘못됨");
    	}
    }
    
    public void setMobileSocket(Socket mobileSocket) {this.mobileSocket = mobileSocket;}
    public void setId(String id) {this.id = id;}
    
    public void setDeviceSocket(Socket deviceSocket) {this.deviceSocket = deviceSocket;}
    public void setSerial(String serial) {this.serial = serial;}
    
    public Socket getMobileSocket() {return mobileSocket;}
    public String getId() {return id;}
    public Socket getDeviceSocket() {return deviceSocket;}
	public String getSerial() {return serial;}
}
