package multi.yoramg.mainserver;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
// 아이디 비번 빈칸일때 실패시키는것 필요
// 디바이스 접속 전에 로그인 하면 실패처리
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
    
 // 로그인 할때 serial로 info 찾아서 모바일소켓, id 세팅하기
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
	            	System.out.println("ConnectThread 시작");
	                serverSocket = mainServerSocket.accept();
	                System.out.println("-Client 접속시도중");
	                
	                InputStream inputStream = serverSocket.getInputStream();
	                byte[] byteArray = new byte[256];
	                int size = inputStream.read(byteArray);
	                
	                if (size == -1) { disconnect(serverSocket);}
	                
	                String message = new String(byteArray, 0, size, "UTF-8");
	                System.out.println("전송받은 메세지 : " + message);
	                String[] messageArray = message.split(";");
	                
	                // 전송받은 메세지 ; 으로 스플릿
	                System.out.print("메세지 스플릿 후 : ");
	                for ( String wo : messageArray ) {
	                	System.out.print(wo + ", ");
	                }
	                System.out.println("");
	                
	                // 스아침이 들어왔을 때
	                if ( messageArray[HEAD].equals("$") && messageArray[COMMAND].equals("S")) {
	                	System.out.println("미확인 디바이스의 연결 시도 발생");
	                	
	                	//sendMessage(serverSocket, "$;server;ZZ;;&");
	                	                	
	                	// serial check
	                	DBTable dbTable = new DBTable();
	                	boolean check = dbTable.serialCheck(messageArray[NAME]);
	                	if ( check ) {
	                		System.out.println("serial(" + messageArray[NAME] + ") 이 연결 되었다.");
	                		list.add(new ClientInfo(serverSocket, messageArray[NAME], messageArray[HEAD]));
	                		
	                		if ( messageArray[DATA].equals("on") ) {
	                			dbTable.setDevicestate(dbTable.getId(messageArray[NAME]), "on");
	                		}
	                		else if ( messageArray[DATA].equals("off") ) {
	                			dbTable.setDevicestate(dbTable.getId(messageArray[NAME]), "off");
	                		}
	                		// 클라이언트 인포 출력
	                		for (int i = 0; i < list.size(); i++) {
	                    		System.out.println(list.get(i).serial + "의 클라이언트 인포 : " + list.get(i).id + ", " + list.get(i).serial + ", " + list.get(i).mobileSocket + ", " + list.get(i).deviceSocket);
	                    	}
	                		ClientThread clientThread = new ClientThread(serverSocket, list);
	                		clientThread.start();
	                	} 
	                	else {
	                		System.out.println("스아침 도킹 fail");
	                	}
	                }
	                
	                
	                // #;;S;;& (모바일)이 들어왔을 때
	                else if ( messageArray[HEAD].equals("#") && messageArray[COMMAND].equals("S")) {
	                	System.out.println("식별되지 않은 모바일이 대기중");
	                	
	                	while (true) {
	                		System.out.println("mobileMessage 대기 중 ");
		                    //sendMessage(serverSocket, "mobileMessage 주셈");
		                    
		                    // 소켓 읽기
		                    size = inputStream.read(byteArray);
		                    
		                    System.out.println("mobileMessage 데이터 받음");
		                    
		                    // 내용물없으면 빠꾸
		                    if (size == -1) { disconnect(serverSocket);}
		                    
		                    String mobileMessage = new String(byteArray, 0, size, "UTF-8");
		                    System.out.println("mobileMessage 메세지 찢기 전 : " + mobileMessage);
		                    String[] mobileMessageArray = mobileMessage.split(";");
		                    
		                    
		                    
		                    // mobileMessageArray 커맨드가 50(로그인) 일 경우
		                    // mobileMessageArray[HEAD] == #, mobileMessageArray[COMMAND} == 50
		                    if ( mobileMessageArray[HEAD].equals("#") && mobileMessageArray[COMMAND].equals("50") ) {
		                    	
		                    	// 데이터를 @으로 스플릿한다.  
		                    	String[] loginDataArray = mobileMessageArray[DATA].split("@");	// [0] id [1] passwd [2] FCM token
		                    	
		                    	// loginDataArray 크기 확인
		                    	System.out.println(loginDataArray.length);
		                    	
		                    	// 크기가 3이 아니라면 { id, passwd, token } 형식에 안맞으므로 빠꾸
		                    	if (loginDataArray.length != 3) {
		                    		System.out.println("login fail { id, passwd, token } 형식을 갖추지 못함, #;server;52;;&");
		                    		sendMessage(serverSocket, "#;server;52;;&");
		                    		
		                    		// 다시 mobileMessage받으러 ㄱ
		                    		continue;
		                    	}
		                    	
		                    	DBTable dbTable = new DBTable();
		                    	
		                    	// 모바일에서 넘어 온 id, passwd가 db에 저장된 값과 일치하는지 확인 하는 메소드
		                    	boolean check = dbTable.loginCheck(loginDataArray[LOGIN_ID], loginDataArray[LOGIN_PASSWORD]);
		                    	
		                    	 /* 
		                    	 일치한다면 로그인 성공 한것,  해당 유저의 침대가 서버에 접속 되어 있는지 확인
		                    	 접속 되어 있다면 작업쓰레드 생성하고, 침대 clientinfo의 모바일 소켓, id 부분 세팅
		                    	 접속 되어 있지 않다면 상황에 대한 피드백 mobile로 전송 한 후 다시 mobileMessage받는 곳으로 빠꾸
		                    	 */
		                    	
		                    	if ( check ) {
		                    		boolean flag = setLoginInfo(dbTable.getUser_Device_Info(loginDataArray[LOGIN_ID], 
		                    				DB_USER_DEVICE_INFO_SERIAL), loginDataArray[LOGIN_ID], serverSocket);
		                    		
		                    		//setLoginInfo 결과값이 false라면 침대가 네트워크와 연결 되어 있지 않다는 뜻임
		                    		System.out.println("setLoginInfo 결과값 : " + 
		                    		setLoginInfo(dbTable.getUser_Device_Info(loginDataArray[LOGIN_ID], DB_USER_DEVICE_INFO_SERIAL), 
		                    				loginDataArray[LOGIN_ID], serverSocket));
		                    		
		                    		// 침대가 서버와 연결되어 있지 않을 때, 모바일 접근 막는 부분
		                    		// 모바일에게 피드백(command : 53) 전송하고 다시 mobileMesaage받는 곳으로 빠꾸
		                    		if ( !flag ) {
		                    			System.out.println("login fail device network off    #;server;53;;&");
			                    		sendMessage(serverSocket, "#;server;53;;&");
		                    			continue;
		                    		}
		                    		//////////////////////////////////////////////////////////
		                    		
		                    		System.out.println("login success");
		                    		
		                    		// DB에 새로 받아온 토큰으로 업데이트
		                    		dbTable.setFcmToken(loginDataArray[LOGIN_ID], loginDataArray[LOGIN_TOKEN]);
		                    		
		                    		// 작업 쓰레드 생성	                    		
		                    		ClientThread clientThread = new ClientThread(serverSocket, list);
		                    		clientThread.start();
		                    		
		                    		// 모바일에 로그인 성공 알림 ( 애기이름, 생일, 디바이스 상태값, serial 같이 전송 )
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
		                    		
		                    		// 수정 된 클라이언트 인포 출력
		                    		for (int i = 0; i < list.size(); i++) {
		                        		System.out.println(list.get(i).id + "의 클라이언트 인포 : " + list.get(i).id + ", " + list.get(i).serial + ", " + list.get(i).mobileSocket + ", " + list.get(i).deviceSocket);
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
		                   // id 중복 체크
		                    else if ( mobileMessageArray[HEAD].equals("#") && mobileMessageArray[COMMAND].equals("60") && mobileMessageArray[DATA] != null ) {
		                    	DBTable dbTable = new DBTable();
		                    	String id = mobileMessageArray[DATA];
		                    	
		                    	boolean flag = dbTable.checkId(id);
		                    	// 중복 되지 않으면 true, 중복 되면 false
		                    	if ( flag ) {
		                    		System.out.println("아이디 사용 가능");
		                    		
//		                    		for ( int i = 0; i < 3000; i++) {
//		                    			
//		                    		}
		                    		sendMessage(serverSocket, "#;server;61;;&");
		                    		//sendMessage(serverSocket, "#;server;61;;&");
		                    	}
		                    	else {
		                    		sendMessage(serverSocket, "#;server;62;;&");
		                    		//sendMessage(serverSocket, "#;server;62;;&");
		                    		//System.out.println("아이디 사용 불가능");
		                    	}
		                    	
		                    }
		                    
		                    
	                    // 회원 가입
		                    else if ( mobileMessageArray[HEAD].equals("#") && mobileMessageArray[COMMAND].equals("70") && mobileMessageArray[DATA] != null ) {
		                    			        
		                    	// 회원가입 파싱
			                    String[] resUserDataArray =  mobileMessageArray[DATA].split("@");
			                    
			                    // 크기가 6이 아니라면 { Id, 이름, 생년월일, serial, password, FCMtoken } 형식에 안맞으므로 빠꾸
		                    	if (resUserDataArray.length != 5) {
		                    		System.out.println("resister user fail { Id, 이름, 생년월일, serial, password } 형식을 갖추지 못함");
		                    		sendMessage(serverSocket, "#;server;72;;&");
		                    		// 다시 mobileMessage받으러 ㄱ
		                    		continue;
		                    	}
			                    DBTable dbTable = new DBTable();
			                    // db에 회원 정보 저장
			                    
			                    boolean flag = dbTable.resisterUser(resUserDataArray[RES_ID], resUserDataArray[RES_PASSWORD], 
			                    		resUserDataArray[RES_NAME], resUserDataArray[RES_BIRTHDAY], resUserDataArray[RES_SERIAL]);
			                    System.out.println("회원가입 flag : " + flag);
			                    if (flag) {
			                    	System.out.println("회원가입 성공");
			                    	sendMessage(serverSocket, "#;server;71;;&");
			                    }
			                    else {
			                    	System.out.println("회원가입 실패");
			                    	sendMessage(serverSocket, "#;server;72;;&");
			                    }
			                   
	                    	}
		                    // #;;S;;&, $;;S;;& 도 아닌 경우, 쫓아낸다
	                		else {
	                			disconnect(serverSocket);
	                			System.out.println("쫓아냈다");
	                			// connectThread 싸이클 시작으로 가야함
	                			// 안간다면 여기 continue 추가 해보자
	                			
	                		}
		                    System.out.println("ConnectThread 싸이클 끝");
	                	}
	                }
	            }
	        }catch (Exception e) { disconnect(serverSocket); }
	        continue;
	    }
    }
}
