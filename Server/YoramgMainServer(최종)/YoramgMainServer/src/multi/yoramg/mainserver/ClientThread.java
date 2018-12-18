package multi.yoramg.mainserver;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ClientThread extends Thread {
	private static final int DB_USER_DEVICE_INFO_SERIAL = 0;
	private static final int DB_USER_DEVICE_INFO_PASSWORD = 1;
	private static final int DB_USER_DEVICE_INFO_NAME = 2;
	private static final int DB_USER_DEVICE_INFO_BIRTHDAY = 3;
	private static final int DB_USER_DEVICE_INFO_DEVICESTATE = 4;
	
	private static final int MOD_NAME = 0;
	private static final int MOD_BIRTHDAY = 1;
	private static final int MOD_SERIAL = 2;
	private static final int MOD_PASSWORD = 3;
	
	
	private static final int HEAD = 0;
	private static final int NAME = 1;
	private static final int COMMAND = 2;
	private static final int DATA = 3;
	private static final int TAIL = 4;
	
	//Command
	private static final String NETWORKSTART = "S";
	private static final String NETWORKEND = "E";
	
	// 스마트 아기침대 -> 서버 
	private static final String SLEEPSTART = "10";
	private static final String SLEEPSTOP = "11";
	private static final String CCTVDATA = "31";
	private static final String AUTOSLEEPINDUCESTATEON = "02";
	private static final String AUTOSLEEPINDUCESTATEOFF = "03";
	private static final String EMERGENCY = "40";
	private static final String AUTOSLEEPINDUCESTATEON_CRYING = "A0";
	private static final String AUTOSLEEPINDUCESTATEOFF_CRYING = "A1";
	
	// 서버 - > 스마트 아기침대
	private static final String ASKCCTV = "30";
	private static final String STOPCCTV = "32";
	private static final String ASKAUTOSLEEPINDUCEON = "00";
	private static final String ASKAUTOSLEEPINDUCEOFF = "01";
	
	// 모바일 -> 서버
	//private static final String ASKCCTV = "30";
	//private static final String STOPCCTV = "32";
//	private static final String ASKAUTOSLEEPINDUCEON = "00";
//	private static final String ASKAUTOSLEEPINDUCEOFF = "01";
	private static final String ASKLOGIN = "50";
	private static final String CHECKID = "60";
	private static final String REGISTERUSER = "70";
	private static final String MODIFYUSER = "80";
	private static final String DELETEUSER = "90";
	private static final String ASKSLEEPDATA = "20";
	private static final String STATEON_CRYING_OK = "A2";
	private static final String STATEOFF_CRYING_OK = "A3";
	
	// 서버 -> 모바일
	private static final String LOGINSUCCESS = "51";
	private static final String LOGINFAIL = "52";
	private static final String USABLEID = "61";
	private static final String UNUSABLEID = "62";
	private static final String REGISTERUSERSUCCESS = "71";
	private static final String REGISTERUSERFAIL = "72";
	private static final String MODIFYUSERSUCCESS = "81";
	private static final String MODIFYUSERFAIL = "82";
	private static final String DELETEUSERSUCCESS = "91";
	private static final String DELETEUSERFAIL = "91";
	private static final String SENDSLEEPDATA = "21";
//	private static final String AUTOSLEEPINDUCESTATEON = "02";
//	private static final String AUTOSLEEPINDUCESTATEOFF = "03";
	
	private static final int DB_USERDEVICEINFO_SERIAL = 0;
	private static final int DB_USERDEVICEINFO_PASSWORD = 1;
	private static final int DB_USERDEVICEINFO_NAME = 2;
	private static final int DB_USERDEVICEINFO_BIRTHDAY = 3;
	private static final int DB_SLINKINFO_DEVICESTATE = 4;
	
	String die = "";
	Socket serverSocket;
    List<ClientInfo> list = new ArrayList<ClientInfo>();
 
    ClientThread(Socket serverSocket, List list) {
        this.serverSocket = serverSocket;
        this.list = list;
    }
    // 문자열 마지막 삭제
    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }
    // 전송 메소드
    public void sendMessage (Socket socket, String message_) {
    	try {
    		byte[] byteArray = message_.getBytes("UTF-8");
        	OutputStream outputStream = socket.getOutputStream();
            outputStream.write(byteArray);
    	} catch(Exception e) {}
    }
    
    // 디바이스 소켓으로 모바일 소켓 찾기
    public Socket searchMobileSocket(Socket socket) {
    	Socket mobileSocket = null;
    	for (int i = 0; i < list.size(); i++) {
            if (list.get(i).deviceSocket == socket) {
            	mobileSocket = list.get(i).mobileSocket;
            }
    	}
    	return mobileSocket;
    }
    // 모바일 소켓으로 디바이스 소켓 찾기
    public Socket searchDeviceSocket(Socket socket) {
    	Socket deviceSocket = null;
    	for (int i = 0; i < list.size(); i++) {
            if (list.get(i).mobileSocket == socket) {
            	deviceSocket = list.get(i).deviceSocket;
            }
    	}
    	return deviceSocket;
    }
    
    // client인포 mobile 수정
    public boolean setLoginInfo(String id_, String newId, Socket mobileSocket_) {
    	boolean flag = false;
    	for (int i = 0; i < list.size(); i++) {
    		if ( list.get(i).id.equals(id_) ) {
            	list.get(i).setId(newId);
            	list.get(i).setMobileSocket(mobileSocket_);
            	flag = true;
            }
    	}
    	return flag;
    }
    // 데이터 찢는 메소드
    public String[] getData(String data) {
    	String[] dataArray = data.split("@");
    	return dataArray;
    }
    
    @Override
    public void run() {
    	 try {
    		 while (true) {
            	DBTable dbTable = new DBTable();
            	int idx;
                InputStream inputStream = serverSocket.getInputStream();
                
                byte[] byteArray = new byte[256];
                int size = inputStream.read(byteArray);
                
                if (size == -1) {break;}
                
                String message = new String(byteArray, 0, size, "UTF-8");
                System.out.println("message : " + message);
                String[] messageArray = message.split(";");
                                                
                switch (messageArray[COMMAND]) {
                	// 수면 끝 시간과 다음 수면 시작시간 사이 텀이 1분 이하일 경우 이어지는 수면으로 계산
                	
                	case "50" :
                		String loginDataArray[] = messageArray[DATA].split("@");
                		String successMessage = "#;server;51;";
                		successMessage += dbTable.getUser_Device_Info(loginDataArray[0], DB_USER_DEVICE_INFO_NAME);
                		successMessage += "@";
                		successMessage += dbTable.getUser_Device_Info(loginDataArray[0], DB_USER_DEVICE_INFO_BIRTHDAY);
                		successMessage += "@";
                		successMessage += dbTable.getUser_Device_Info(loginDataArray[0], DB_USER_DEVICE_INFO_DEVICESTATE);
                		successMessage += "@";
                		successMessage += dbTable.getUser_Device_Info(loginDataArray[0], DB_USER_DEVICE_INFO_SERIAL);
                		successMessage += ";&";
                		sendMessage(serverSocket, successMessage);
                		if (loginDataArray[2] == null) {
                			System.out.println("자동로그인중 토큰이 널임");
                		}
                		
                		dbTable.setFcmToken(loginDataArray[0], loginDataArray[2]);
	                case SLEEPSTART :
	                	System.out.println("잠든 시간 DB에 저장");
	                	idx = dbTable.getIdx();
	                	idx += 1;
	                	dbTable.resisterSleepData("SLEEPSTART", messageArray[1], idx);
	                	dbTable.setIdx();
	                	break;
	                case SLEEPSTOP :
	                	System.out.println("잠 깬 시간 DB에 저장");
	                	idx = dbTable.getIdx();
	                	idx += 1;
	                	dbTable.resisterSleepData("SLEEPSTOP", messageArray[1], idx);
	                	dbTable.setIdx();
	                	break;
	                	
	                case ASKAUTOSLEEPINDUCEON :
	                	// 디바이스에게 수면 유도 기능 on 요청 전달
	                	sendMessage(searchDeviceSocket(serverSocket), "$;server;00;;&");
	                	break;
	                case ASKAUTOSLEEPINDUCEOFF :
	                	// 디바이스에게 수면 유도 기능 off 요청 전달
	                	sendMessage(searchDeviceSocket(serverSocket), "$;server;01;;&");
	                	break;
	                	
	                case AUTOSLEEPINDUCESTATEON :	
	                	// 모바일에게 수면 유도 기능 on 상태라고 알림
	                	sendMessage(searchMobileSocket(serverSocket), "#;server;02;;&");
	                	dbTable.setDevicestate(dbTable.getId(messageArray[NAME]), "on");
	                	break;
	                case AUTOSLEEPINDUCESTATEOFF :	
	                	// 모바일에게 수면 유도 기능 off 상태라고 알림
	                	sendMessage(searchMobileSocket(serverSocket), "#;server;03;;&");
	                	dbTable.setDevicestate(dbTable.getId(messageArray[NAME]), "off");
	                	break;
	                
	                case MODIFYUSER :
	                	String[] modUser = messageArray[DATA].split("@");
	                	if ( modUser.length != 4 ) {
	                		System.out.println(" {NEW이름, NEW생년월일, NEWserial, NEWpassword} 형식에 안맞음");
	                		sendMessage(serverSocket, "#;server;92;;&");
	                		break;
	                	}							// id				
	                	boolean flag = dbTable.modifyUser(messageArray[NAME], modUser[MOD_PASSWORD], modUser[MOD_NAME], modUser[MOD_BIRTHDAY], modUser[MOD_SERIAL]);
	                	
	                	if ( flag ) {
	                		System.out.println("회원정보 수정 성공");
	                		sendMessage(serverSocket, "#;server;81;;&");
	                	}
	                	else {
	                		System.out.println("회원정보 수정 실패");
	                		sendMessage(serverSocket, "#;server;82;;&");
	                	}
	                	break;
	                case DELETEUSER :
	                	// 유저 정보 삭제
	                	String deleteUser = messageArray[DATA];
	                	
	                	// dbTable.checkId(deleteUser)값이 false라면 DB에 있는 id를 삭제하려하는거니 정상
	                	if ( !dbTable.checkId(deleteUser) ) {
	                		//삭제 
	                		if ( dbTable.deleteUser(messageArray[NAME], 
	                				dbTable.getUser_Device_Info(messageArray[NAME], DB_USER_DEVICE_INFO_SERIAL)) ) {
	                			// 삭제가 성공 했으면
	                			System.out.println("삭제 성공");
		                		sendMessage(serverSocket, "#;server;91;;&");
		                		break;
	                		}
	                		else {
	                			System.out.println("삭제 실패 deleteUser메소드 고장?");
		                		sendMessage(serverSocket, "#;server;92;;&");
	                		}
	                	}
	                	else {
	                		// 없는 아이디를 삭제하려 하니 실패
	                		System.out.println("없는 아이디를 삭제하려함, 삭제 실패");
	                		sendMessage(serverSocket, "#;server;92;;&");
	                	}
	                	break;
	                	
	                case ASKSLEEPDATA :
	                	// db에서 매칭되는 시리얼 값 가져온다
	                	// 받아온 data 에서 YYYYMMDDHHmmss(언제 이후로) 원하는가 확인
	                	// sleep_data table에서 YYYYMMDDHHmmss 이후의 데이터 빨아옴
	                	// 빨아온 데이터 [data]에 담아서 모바일에게 전달
	                	//String serial = dbTable.getSerial(messageArray[NAME]);
	                	
//	                	String[] dataArray = getData(messageArray[DATA]); // [data] 찢어서 넣기 [0] = date [1] = time
//	                	String sendData = dbTable.readSleepData(dbTable.getLink_info(messageArray[NAME], DB_USERDEVICEINFO_SERIAL), dataArray[0], dataArray[1]);
	                	String msg = "#;server;21;";
	                	System.out.println(dbTable.getUser_Device_Info(messageArray[NAME], DB_USERDEVICEINFO_SERIAL));
	                	String sleepdata = dbTable.readSleepData(dbTable.getUser_Device_Info(messageArray[NAME], DB_USERDEVICEINFO_SERIAL), "date", "time");
	                	if ( !sleepdata.equals(null) ) {
	                		System.out.println("문자열 마지막 지움");
	                		sleepdata = removeLastChar(sleepdata);// 데이터가 없을땐 마지막문자 지우면 안됨
	                	}
	                	//System.out.println(msg);
	                	System.out.println(msg);
	                	System.out.println(sleepdata);
	                	String aaa = msg + sleepdata + ";&";
	                	
	                	System.out.println(aaa);
	                	//System.out.println(dbTable.readSleepData(dbTable.getLink_info(messageArray[NAME], DB_USERDEVICEINFO_SERIAL), "date", "time"));
	                	sendMessage(serverSocket, aaa);
	                	
	                	break;
		            case EMERGENCY :
	                	MyFCM myFCM = new MyFCM();
	                	String token = dbTable.getFcmToken(dbTable.getId(messageArray[NAME]));
	                	myFCM.sendPush("질식 위험!", "질식 위험!\t아기 상태를 확인하세요", token);
	                	System.out.println("아기 질식 위험!!!!!!!!!!!!!!!!");
	                	break;
	                
	                	// 수면 유도를 지속적으로 했지만 아기가 계속 움
		            case AUTOSLEEPINDUCESTATEON_CRYING :
		            	MyFCM myFCM1 = new MyFCM();
	                	String token1 = dbTable.getFcmToken(dbTable.getId(messageArray[NAME]));
	                	myFCM1.sendPush("아기가 울고 있어요!", "아기가 울고 있어요!\t아기 상태를 확인하세요", token1);
	                	System.out.println("수면 유도 하고있는데도 아기가 계속 운다");
	                	break;
		            
		            case AUTOSLEEPINDUCESTATEOFF_CRYING :
		            	MyFCM myFCM2 = new MyFCM();
	                	String token2 = dbTable.getFcmToken(dbTable.getId(messageArray[NAME]));
	                	myFCM2.sendPush("아기가 울고 있어요!", "아기가 울고 있어요!\t수면 유도 기능을 켜시거나, 아기 상태를 확인하세요", token2);
	                	System.out.println("수면 유도 꺼져있는데 아기가 계속 운다");
	                	break;
	                	
		            case STATEON_CRYING_OK :
		            	sendMessage(searchMobileSocket(serverSocket), "$;server;A2;;&");
		            	break;
		            	
		            case STATEOFF_CRYING_OK :
		            	sendMessage(searchMobileSocket(serverSocket), "$;server;A3;;&");
		            	break;
		            	
		            // 로그아웃
		            case "55" :
		            	sendMessage(serverSocket, "#;server;56;;&");
		            	setLoginInfo(messageArray[NAME], "", null);
		            	dbTable.setFcmToken(messageArray[NAME], "blank!");
		            	// 토큰 값 바꾸는것 필요
		            	
		            case "E" :
		            	if (messageArray[HEAD].equals("#")) {
		            		// 모바일 네트워크 종료시 
		            		// 로그아웃 처리하고
		            		setLoginInfo(messageArray[NAME], "", null);
		            		// token 비워야함
		            		dbTable.setFcmToken(messageArray[NAME], "blank!");
		            	}
		            	else if (messageArray[HEAD].equals("$")) {
		            		// 디바이스 네트워크 종료시
		            		for (int i = 0; i < list.size();   ) {
		                        if(serverSocket == list.get(i).deviceSocket) {
		                        	System.out.println("디바이스 종료");
		                        	try {
		                        		//serverSocket.close();
		                        	} catch (Exception e1) {}
		                        	list.remove(i);
		                        }
		            		}
		            	}
		            	
		            	
	                default :
	                	for (int i = 0; i < list.size(); i++) {
	                		//sendMessage(serverSocket, "잘못된 메세지");
	                    }
	                	break;
                }
            } 
        } catch (Exception e) {
            for (int i = 0; i < list.size();   ) {
                if(serverSocket == list.get(i).deviceSocket) {
                	System.out.println("디바이스 예외발생");
                	try {
                		serverSocket.close();
                	} catch (Exception e1) {}
                	list.remove(i);
                }
                else if(serverSocket == list.get(i).mobileSocket) {
                	System.out.println("모바일 예외발생");
                	try {
                		serverSocket.close();
                		break;
                	} catch (Exception e1) {}
                	
                }
            }
        } 
    	finally {
    		for (int i = 0; i < list.size();   ) {
                if(serverSocket == list.get(i).deviceSocket) {
                	System.out.println("디바이스 종료");
                	try {
                		//serverSocket.close();
                	} catch (Exception e1) {}
                	list.remove(i);
                }
                else if(serverSocket == list.get(i).mobileSocket) {
                	System.out.println("모바일 종료");
                	try {
                		break;
                	} catch (Exception e1) {}
                }
            }
		}
    	 
    }
}