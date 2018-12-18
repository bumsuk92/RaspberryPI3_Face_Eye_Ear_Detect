package multi.yoramg.mainserver;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
// ���̵� ��� ��ĭ�϶� ���н�Ű�°� �ʿ�
// ����̽� ���� ���� �α��� �ϸ� ����ó��
class ConnectThread extends Thread {
	private static final int LOGIN_ID = 0;
	private static final int LOGIN_PASSWORD = 1;
	private static final int LOGIN_TOKEN = 2;
	
	private static final int RES_ID = 0;
	private static final int RES_NAME = 1;
	private static final int RES_BIRTHDAY = 2;
	private static final int RES_SERIAL = 3;
	private static final int RES_PASSWORD = 4;
	private static final int RES_FCMTOKEN = 5;
	
	private static final int DB_USER_DEVICE_INFO_SERIAL = 0;
	private static final int DB_USER_DEVICE_INFO_PASSWORD = 1;
	private static final int DB_USER_DEVICE_INFO_NAME = 2;
	private static final int DB_USER_DEVICE_INFO_BIRTHDAY = 3;
	private static final int DB_USER_DEVICE_INFO_DEVICESTATE = 4;
	
	private static final int HEAD = 0;
	private static final int NAME = 1;
	private static final int COMMAND = 2;
	private static final int DATA = 3;
	private static final int TAIL = 4;
    ServerSocket mainServerSocket = null;
    Socket serverSocket = new Socket();
    List<ClientInfo> list = new ArrayList<ClientInfo>();
    
    ConnectThread(ServerSocket mainServerSocket) {
        this.mainServerSocket = mainServerSocket;
    }
    
    public void disconnect(Socket socket) {
    	try {
    		socket.close();
    	} catch (Exception e) {}
    }
    
 // �α��� �Ҷ� serial�� info ã�Ƽ� ����ϼ���, id �����ϱ�
    public boolean setLoginInfo(String serial_, String id_, Socket mobileSocket_) {
    	boolean flag = false;
    	for (int i = 0; i < list.size(); i++) {
    		if ( list.get(i).serial.equals(serial_) ) {
            	list.get(i).setId(id_);
            	list.get(i).setMobileSocket(mobileSocket_);
            	flag = true;
            }
    	}
    	return flag;
    }
    
    public void sendMessage(Socket socket, String message_) {
    	try {
    		byte[] byteArray = message_.getBytes("UTF-8");
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(byteArray);
    	} catch (Exception e) {}
    }
    @Override
    public void run() {
    	while ( true ) {
	        try {
	        	while (true) {
	            	System.out.println("ConnectThread ����");
	                serverSocket = mainServerSocket.accept();
	                System.out.println("-Client ���ӽõ���");
	                
	                InputStream inputStream = serverSocket.getInputStream();
	                byte[] byteArray = new byte[256];
	                int size = inputStream.read(byteArray);
	                
	                if (size == -1) { disconnect(serverSocket);}
	                
	                String message = new String(byteArray, 0, size, "UTF-8");
	                System.out.println("���۹��� �޼��� : " + message);
	                String[] messageArray = message.split(";");
	                
	                // ���۹��� �޼��� ; ���� ���ø�
	                System.out.print("�޼��� ���ø� �� : ");
	                for ( String wo : messageArray ) {
	                	System.out.print(wo + ", ");
	                }
	                System.out.println("");
	                
	                // ����ħ�� ������ ��
	                if ( messageArray[HEAD].equals("$") && messageArray[COMMAND].equals("S")) {
	                	System.out.println("��Ȯ�� ����̽��� ���� �õ� �߻�");
	                	
	                	//sendMessage(serverSocket, "$;server;ZZ;;&");
	                	                	
	                	// serial check
	                	DBTable dbTable = new DBTable();
	                	boolean check = dbTable.serialCheck(messageArray[NAME]);
	                	if ( check ) {
	                		System.out.println("serial(" + messageArray[NAME] + ") �� ���� �Ǿ���.");
	                		list.add(new ClientInfo(serverSocket, messageArray[NAME], messageArray[HEAD]));
	                		
	                		if ( messageArray[DATA].equals("on") ) {
	                			dbTable.setDevicestate(dbTable.getId(messageArray[NAME]), "on");
	                		}
	                		else if ( messageArray[DATA].equals("off") ) {
	                			dbTable.setDevicestate(dbTable.getId(messageArray[NAME]), "off");
	                		}
	                		// Ŭ���̾�Ʈ ���� ���
	                		for (int i = 0; i < list.size(); i++) {
	                    		System.out.println(list.get(i).serial + "�� Ŭ���̾�Ʈ ���� : " + list.get(i).id + ", " + list.get(i).serial + ", " + list.get(i).mobileSocket + ", " + list.get(i).deviceSocket);
	                    	}
	                		ClientThread clientThread = new ClientThread(serverSocket, list);
	                		clientThread.start();
	                	} 
	                	else {
	                		System.out.println("����ħ ��ŷ fail");
	                	}
	                }
	                
	                
	                // #;;S;;& (�����)�� ������ ��
	                else if ( messageArray[HEAD].equals("#") && messageArray[COMMAND].equals("S")) {
	                	System.out.println("�ĺ����� ���� ������� �����");
	                	
	                	while (true) {
	                		System.out.println("mobileMessage ��� �� ");
		                    //sendMessage(serverSocket, "mobileMessage �ּ�");
		                    
		                    // ���� �б�
		                    size = inputStream.read(byteArray);
		                    
		                    System.out.println("mobileMessage ������ ����");
		                    
		                    // ���빰������ ����
		                    if (size == -1) { disconnect(serverSocket);}
		                    
		                    String mobileMessage = new String(byteArray, 0, size, "UTF-8");
		                    System.out.println("mobileMessage �޼��� ���� �� : " + mobileMessage);
		                    String[] mobileMessageArray = mobileMessage.split(";");
		                    
		                    
		                    
		                    // mobileMessageArray Ŀ�ǵ尡 50(�α���) �� ���
		                    // mobileMessageArray[HEAD] == #, mobileMessageArray[COMMAND} == 50
		                    if ( mobileMessageArray[HEAD].equals("#") && mobileMessageArray[COMMAND].equals("50") ) {
		                    	
		                    	// �����͸� @���� ���ø��Ѵ�.  
		                    	String[] loginDataArray = mobileMessageArray[DATA].split("@");	// [0] id [1] passwd [2] FCM token
		                    	
		                    	// loginDataArray ũ�� Ȯ��
		                    	System.out.println(loginDataArray.length);
		                    	
		                    	// ũ�Ⱑ 3�� �ƴ϶�� { id, passwd, token } ���Ŀ� �ȸ����Ƿ� ����
		                    	if (loginDataArray.length != 3) {
		                    		System.out.println("login fail { id, passwd, token } ������ ������ ����, #;server;52;;&");
		                    		sendMessage(serverSocket, "#;server;52;;&");
		                    		
		                    		// �ٽ� mobileMessage������ ��
		                    		continue;
		                    	}
		                    	
		                    	DBTable dbTable = new DBTable();
		                    	
		                    	// ����Ͽ��� �Ѿ� �� id, passwd�� db�� ����� ���� ��ġ�ϴ��� Ȯ�� �ϴ� �޼ҵ�
		                    	boolean check = dbTable.loginCheck(loginDataArray[LOGIN_ID], loginDataArray[LOGIN_PASSWORD]);
		                    	
		                    	 /* 
		                    	 ��ġ�Ѵٸ� �α��� ���� �Ѱ�,  �ش� ������ ħ�밡 ������ ���� �Ǿ� �ִ��� Ȯ��
		                    	 ���� �Ǿ� �ִٸ� �۾������� �����ϰ�, ħ�� clientinfo�� ����� ����, id �κ� ����
		                    	 ���� �Ǿ� ���� �ʴٸ� ��Ȳ�� ���� �ǵ�� mobile�� ���� �� �� �ٽ� mobileMessage�޴� ������ ����
		                    	 */
		                    	
		                    	if ( check ) {
		                    		boolean flag = setLoginInfo(dbTable.getUser_Device_Info(loginDataArray[LOGIN_ID], 
		                    				DB_USER_DEVICE_INFO_SERIAL), loginDataArray[LOGIN_ID], serverSocket);
		                    		
		                    		//setLoginInfo ������� false��� ħ�밡 ��Ʈ��ũ�� ���� �Ǿ� ���� �ʴٴ� ����
		                    		System.out.println("setLoginInfo ����� : " + 
		                    		setLoginInfo(dbTable.getUser_Device_Info(loginDataArray[LOGIN_ID], DB_USER_DEVICE_INFO_SERIAL), 
		                    				loginDataArray[LOGIN_ID], serverSocket));
		                    		
		                    		// ħ�밡 ������ ����Ǿ� ���� ���� ��, ����� ���� ���� �κ�
		                    		// ����Ͽ��� �ǵ��(command : 53) �����ϰ� �ٽ� mobileMesaage�޴� ������ ����
		                    		if ( !flag ) {
		                    			System.out.println("login fail device network off    #;server;53;;&");
			                    		sendMessage(serverSocket, "#;server;53;;&");
		                    			continue;
		                    		}
		                    		//////////////////////////////////////////////////////////
		                    		
		                    		System.out.println("login success");
		                    		
		                    		// DB�� ���� �޾ƿ� ��ū���� ������Ʈ
		                    		dbTable.setFcmToken(loginDataArray[LOGIN_ID], loginDataArray[LOGIN_TOKEN]);
		                    		
		                    		// �۾� ������ ����	                    		
		                    		ClientThread clientThread = new ClientThread(serverSocket, list);
		                    		clientThread.start();
		                    		
		                    		// ����Ͽ� �α��� ���� �˸� ( �ֱ��̸�, ����, ����̽� ���°�, serial ���� ���� )
		                    		String successMessage = "#;server;51;";
		                    		successMessage += dbTable.getUser_Device_Info(loginDataArray[LOGIN_ID], DB_USER_DEVICE_INFO_NAME);
		                    		successMessage += "@";
		                    		successMessage += dbTable.getUser_Device_Info(loginDataArray[LOGIN_ID], DB_USER_DEVICE_INFO_BIRTHDAY);
		                    		successMessage += "@";
		                    		successMessage += dbTable.getUser_Device_Info(loginDataArray[LOGIN_ID], DB_USER_DEVICE_INFO_DEVICESTATE);
		                    		successMessage += "@";
		                    		successMessage += dbTable.getUser_Device_Info(loginDataArray[LOGIN_ID], DB_USER_DEVICE_INFO_SERIAL);
		                    		successMessage += ";&";
		                    		for (int i = 0; i < 3000; i++) {
		                    			
		                    		}
		                    		sendMessage(serverSocket, successMessage);
		                    		
		                    		// ���� �� Ŭ���̾�Ʈ ���� ���
		                    		for (int i = 0; i < list.size(); i++) {
		                        		System.out.println(list.get(i).id + "�� Ŭ���̾�Ʈ ���� : " + list.get(i).id + ", " + list.get(i).serial + ", " + list.get(i).mobileSocket + ", " + list.get(i).deviceSocket);
		                        	}
		                    		break;
		                    	}
		                    	else {
		                    		System.out.println("login fail");
		                    		String echoMessage = "#;server;52;;&";
		        	            	byte[] failbyteArray = echoMessage.getBytes("UTF-8");
		        	            	OutputStream outputStream = serverSocket.getOutputStream();
		        	                outputStream.write(failbyteArray);
		                    	}
		                    	
		                    }
		                   // id �ߺ� üũ
		                    else if ( mobileMessageArray[HEAD].equals("#") && mobileMessageArray[COMMAND].equals("60") && mobileMessageArray[DATA] != null ) {
		                    	DBTable dbTable = new DBTable();
		                    	String id = mobileMessageArray[DATA];
		                    	
		                    	boolean flag = dbTable.checkId(id);
		                    	// �ߺ� ���� ������ true, �ߺ� �Ǹ� false
		                    	if ( flag ) {
		                    		System.out.println("���̵� ��� ����");
		                    		
//		                    		for ( int i = 0; i < 3000; i++) {
//		                    			
//		                    		}
		                    		sendMessage(serverSocket, "#;server;61;;&");
		                    		//sendMessage(serverSocket, "#;server;61;;&");
		                    	}
		                    	else {
		                    		sendMessage(serverSocket, "#;server;62;;&");
		                    		//sendMessage(serverSocket, "#;server;62;;&");
		                    		//System.out.println("���̵� ��� �Ұ���");
		                    	}
		                    	
		                    }
		                    
		                    
	                    // ȸ�� ����
		                    else if ( mobileMessageArray[HEAD].equals("#") && mobileMessageArray[COMMAND].equals("70") && mobileMessageArray[DATA] != null ) {
		                    			        
		                    	// ȸ������ �Ľ�
			                    String[] resUserDataArray =  mobileMessageArray[DATA].split("@");
			                    
			                    // ũ�Ⱑ 6�� �ƴ϶�� { Id, �̸�, �������, serial, password, FCMtoken } ���Ŀ� �ȸ����Ƿ� ����
		                    	if (resUserDataArray.length != 5) {
		                    		System.out.println("resister user fail { Id, �̸�, �������, serial, password } ������ ������ ����");
		                    		sendMessage(serverSocket, "#;server;72;;&");
		                    		// �ٽ� mobileMessage������ ��
		                    		continue;
		                    	}
			                    DBTable dbTable = new DBTable();
			                    // db�� ȸ�� ���� ����
			                    
			                    boolean flag = dbTable.resisterUser(resUserDataArray[RES_ID], resUserDataArray[RES_PASSWORD], 
			                    		resUserDataArray[RES_NAME], resUserDataArray[RES_BIRTHDAY], resUserDataArray[RES_SERIAL]);
			                    System.out.println("ȸ������ flag : " + flag);
			                    if (flag) {
			                    	System.out.println("ȸ������ ����");
			                    	sendMessage(serverSocket, "#;server;71;;&");
			                    }
			                    else {
			                    	System.out.println("ȸ������ ����");
			                    	sendMessage(serverSocket, "#;server;72;;&");
			                    }
			                   
	                    	}
		                    // #;;S;;&, $;;S;;& �� �ƴ� ���, �ѾƳ���
	                		else {
	                			disconnect(serverSocket);
	                			System.out.println("�ѾƳ´�");
	                			// connectThread ����Ŭ �������� ������
	                			// �Ȱ��ٸ� ���� continue �߰� �غ���
	                			
	                		}
		                    System.out.println("ConnectThread ����Ŭ ��");
	                	}
	                }
	            }
	        }catch (Exception e) { disconnect(serverSocket); }
	        continue;
	    }
    }
}
