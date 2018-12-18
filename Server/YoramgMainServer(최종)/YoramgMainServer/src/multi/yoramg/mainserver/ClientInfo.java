package multi.yoramg.mainserver;

import java.net.Socket;

public class ClientInfo {
	Socket mobileSocket;
    Socket deviceSocket;
    String id;				
    String serial;
        
    // info ������
    ClientInfo(Socket socket, String str, String division) {
    	// ����� ���� ������
    	if ( division.equals("#") ) {
    		this.mobileSocket = socket;
    		this.id = str;
    	}
    	// ����Ʈ�Ʊ�ħ�� ���� ������ �ʿ��Ѱ�? ����̽��� ������ ���� �����ֵ��� �Ѵٸ� ��Ʈ�� ����̽� infoã�Ƽ� ��ĭ ä���ָ� �ɰͰ���
    	else if ( division.equals("$") ) {
    		this.deviceSocket = socket;
    		this.serial = str;
    	}
    	else {
    		System.out.println("ClientInfo ���� �߸���");
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
