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
	
	// ����Ʈ �Ʊ�ħ�� -> ���� 
	private static final String SLEEPSTART = "10";
	private static final String SLEEPSTOP = "11";
	private static final String CCTVDATA = "31";
	private static final String AUTOSLEEPINDUCESTATEON = "02";
	private static final String AUTOSLEEPINDUCESTATEOFF = "03";
	private static final String EMERGENCY = "40";
	private static final String AUTOSLEEPINDUCESTATEON_CRYING = "A0";
	private static final String AUTOSLEEPINDUCESTATEOFF_CRYING = "A1";
	
	// ���� - > ����Ʈ �Ʊ�ħ��
	private static final String ASKCCTV = "30";
	private static final String STOPCCTV = "32";
	private static final String ASKAUTOSLEEPINDUCEON = "00";
	private static final String ASKAUTOSLEEPINDUCEOFF = "01";
	
	// ����� -> ����
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
	
	// ���� -> �����
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
    // ���ڿ� ������ ����
    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }
    // ���� �޼ҵ�
    public void sendMessage (Socket socket, String message_) {
    	try {
    		byte[] byteArray = message_.getBytes("UTF-8");
        	OutputStream outputStream = socket.getOutputStream();
            outputStream.write(byteArray);
    	} catch(Exception e) {}
    }
    
    // ����̽� �������� ����� ���� ã��
    public Socket searchMobileSocket(Socket socket) {
    	Socket mobileSocket = null;
    	for (int i = 0; i < list.size(); i++) {
            if (list.get(i).deviceSocket == socket) {
            	mobileSocket = list.get(i).mobileSocket;
            }
    	}
    	return mobileSocket;
    }
    // ����� �������� ����̽� ���� ã��
    public Socket searchDeviceSocket(Socket socket) {
    	Socket deviceSocket = null;
    	for (int i = 0; i < list.size(); i++) {
            if (list.get(i).mobileSocket == socket) {
            	deviceSocket = list.get(i).deviceSocket;
            }
    	}
    	return deviceSocket;
    }
    
    // client���� mobile ����
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
    // ������ ���� �޼ҵ�
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
                	// ���� �� �ð��� ���� ���� ���۽ð� ���� ���� 1�� ������ ��� �̾����� �������� ���
                	
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
                			System.out.println("�ڵ��α����� ��ū�� ����");
                		}
                		
                		dbTable.setFcmToken(loginDataArray[0], loginDataArray[2]);
	                case SLEEPSTART :
	                	System.out.println("��� �ð� DB�� ����");
	                	idx = dbTable.getIdx();
	                	idx += 1;
	                	dbTable.resisterSleepData("SLEEPSTART", messageArray[1], idx);
	                	dbTable.setIdx();
	                	break;
	                case SLEEPSTOP :
	                	System.out.println("�� �� �ð� DB�� ����");
	                	idx = dbTable.getIdx();
	                	idx += 1;
	                	dbTable.resisterSleepData("SLEEPSTOP", messageArray[1], idx);
	                	dbTable.setIdx();
	                	break;
	                	
	                case ASKAUTOSLEEPINDUCEON :
	                	// ����̽����� ���� ���� ��� on ��û ����
	                	sendMessage(searchDeviceSocket(serverSocket), "$;server;00;;&");
	                	break;
	                case ASKAUTOSLEEPINDUCEOFF :
	                	// ����̽����� ���� ���� ��� off ��û ����
	                	sendMessage(searchDeviceSocket(serverSocket), "$;server;01;;&");
	                	break;
	                	
	                case AUTOSLEEPINDUCESTATEON :	
	                	// ����Ͽ��� ���� ���� ��� on ���¶�� �˸�
	                	sendMessage(searchMobileSocket(serverSocket), "#;server;02;;&");
	                	dbTable.setDevicestate(dbTable.getId(messageArray[NAME]), "on");
	                	break;
	                case AUTOSLEEPINDUCESTATEOFF :	
	                	// ����Ͽ��� ���� ���� ��� off ���¶�� �˸�
	                	sendMessage(searchMobileSocket(serverSocket), "#;server;03;;&");
	                	dbTable.setDevicestate(dbTable.getId(messageArray[NAME]), "off");
	                	break;
	                
	                case MODIFYUSER :
	                	String[] modUser = messageArray[DATA].split("@");
	                	if ( modUser.length != 4 ) {
	                		System.out.println(" {NEW�̸�, NEW�������, NEWserial, NEWpassword} ���Ŀ� �ȸ���");
	                		sendMessage(serverSocket, "#;server;92;;&");
	                		break;
	                	}							// id				
	                	boolean flag = dbTable.modifyUser(messageArray[NAME], modUser[MOD_PASSWORD], modUser[MOD_NAME], modUser[MOD_BIRTHDAY], modUser[MOD_SERIAL]);
	                	
	                	if ( flag ) {
	                		System.out.println("ȸ������ ���� ����");
	                		sendMessage(serverSocket, "#;server;81;;&");
	                	}
	                	else {
	                		System.out.println("ȸ������ ���� ����");
	                		sendMessage(serverSocket, "#;server;82;;&");
	                	}
	                	break;
	                case DELETEUSER :
	                	// ���� ���� ����
	                	String deleteUser = messageArray[DATA];
	                	
	                	// dbTable.checkId(deleteUser)���� false��� DB�� �ִ� id�� �����Ϸ��ϴ°Ŵ� ����
	                	if ( !dbTable.checkId(deleteUser) ) {
	                		//���� 
	                		if ( dbTable.deleteUser(messageArray[NAME], 
	                				dbTable.getUser_Device_Info(messageArray[NAME], DB_USER_DEVICE_INFO_SERIAL)) ) {
	                			// ������ ���� ������
	                			System.out.println("���� ����");
		                		sendMessage(serverSocket, "#;server;91;;&");
		                		break;
	                		}
	                		else {
	                			System.out.println("���� ���� deleteUser�޼ҵ� ����?");
		                		sendMessage(serverSocket, "#;server;92;;&");
	                		}
	                	}
	                	else {
	                		// ���� ���̵� �����Ϸ� �ϴ� ����
	                		System.out.println("���� ���̵� �����Ϸ���, ���� ����");
	                		sendMessage(serverSocket, "#;server;92;;&");
	                	}
	                	break;
	                	
	                case ASKSLEEPDATA :
	                	// db���� ��Ī�Ǵ� �ø��� �� �����´�
	                	// �޾ƿ� data ���� YYYYMMDDHHmmss(���� ���ķ�) ���ϴ°� Ȯ��
	                	// sleep_data table���� YYYYMMDDHHmmss ������ ������ ���ƿ�
	                	// ���ƿ� ������ [data]�� ��Ƽ� ����Ͽ��� ����
	                	//String serial = dbTable.getSerial(messageArray[NAME]);
	                	
//	                	String[] dataArray = getData(messageArray[DATA]); // [data] ��� �ֱ� [0] = date [1] = time
//	                	String sendData = dbTable.readSleepData(dbTable.getLink_info(messageArray[NAME], DB_USERDEVICEINFO_SERIAL), dataArray[0], dataArray[1]);
	                	String msg = "#;server;21;";
	                	System.out.println(dbTable.getUser_Device_Info(messageArray[NAME], DB_USERDEVICEINFO_SERIAL));
	                	String sleepdata = dbTable.readSleepData(dbTable.getUser_Device_Info(messageArray[NAME], DB_USERDEVICEINFO_SERIAL), "date", "time");
	                	if ( !sleepdata.equals(null) ) {
	                		System.out.println("���ڿ� ������ ����");
	                		sleepdata = removeLastChar(sleepdata);// �����Ͱ� ������ ���������� ����� �ȵ�
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
	                	myFCM.sendPush("���� ����!", "���� ����!\t�Ʊ� ���¸� Ȯ���ϼ���", token);
	                	System.out.println("�Ʊ� ���� ����!!!!!!!!!!!!!!!!");
	                	break;
	                
	                	// ���� ������ ���������� ������ �ƱⰡ ��� ��
		            case AUTOSLEEPINDUCESTATEON_CRYING :
		            	MyFCM myFCM1 = new MyFCM();
	                	String token1 = dbTable.getFcmToken(dbTable.getId(messageArray[NAME]));
	                	myFCM1.sendPush("�ƱⰡ ��� �־��!", "�ƱⰡ ��� �־��!\t�Ʊ� ���¸� Ȯ���ϼ���", token1);
	                	System.out.println("���� ���� �ϰ��ִµ��� �ƱⰡ ��� ���");
	                	break;
		            
		            case AUTOSLEEPINDUCESTATEOFF_CRYING :
		            	MyFCM myFCM2 = new MyFCM();
	                	String token2 = dbTable.getFcmToken(dbTable.getId(messageArray[NAME]));
	                	myFCM2.sendPush("�ƱⰡ ��� �־��!", "�ƱⰡ ��� �־��!\t���� ���� ����� �ѽðų�, �Ʊ� ���¸� Ȯ���ϼ���", token2);
	                	System.out.println("���� ���� �����ִµ� �ƱⰡ ��� ���");
	                	break;
	                	
		            case STATEON_CRYING_OK :
		            	sendMessage(searchMobileSocket(serverSocket), "$;server;A2;;&");
		            	break;
		            	
		            case STATEOFF_CRYING_OK :
		            	sendMessage(searchMobileSocket(serverSocket), "$;server;A3;;&");
		            	break;
		            	
		            // �α׾ƿ�
		            case "55" :
		            	sendMessage(serverSocket, "#;server;56;;&");
		            	setLoginInfo(messageArray[NAME], "", null);
		            	dbTable.setFcmToken(messageArray[NAME], "blank!");
		            	// ��ū �� �ٲٴ°� �ʿ�
		            	
		            case "E" :
		            	if (messageArray[HEAD].equals("#")) {
		            		// ����� ��Ʈ��ũ ����� 
		            		// �α׾ƿ� ó���ϰ�
		            		setLoginInfo(messageArray[NAME], "", null);
		            		// token �������
		            		dbTable.setFcmToken(messageArray[NAME], "blank!");
		            	}
		            	else if (messageArray[HEAD].equals("$")) {
		            		// ����̽� ��Ʈ��ũ �����
		            		for (int i = 0; i < list.size();   ) {
		                        if(serverSocket == list.get(i).deviceSocket) {
		                        	System.out.println("����̽� ����");
		                        	try {
		                        		//serverSocket.close();
		                        	} catch (Exception e1) {}
		                        	list.remove(i);
		                        }
		            		}
		            	}
		            	
		            	
	                default :
	                	for (int i = 0; i < list.size(); i++) {
	                		//sendMessage(serverSocket, "�߸��� �޼���");
	                    }
	                	break;
                }
            } 
        } catch (Exception e) {
            for (int i = 0; i < list.size();   ) {
                if(serverSocket == list.get(i).deviceSocket) {
                	System.out.println("����̽� ���ܹ߻�");
                	try {
                		serverSocket.close();
                	} catch (Exception e1) {}
                	list.remove(i);
                }
                else if(serverSocket == list.get(i).mobileSocket) {
                	System.out.println("����� ���ܹ߻�");
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
                	System.out.println("����̽� ����");
                	try {
                		//serverSocket.close();
                	} catch (Exception e1) {}
                	list.remove(i);
                }
                else if(serverSocket == list.get(i).mobileSocket) {
                	System.out.println("����� ����");
                	try {
                		break;
                	} catch (Exception e1) {}
                }
            }
		}
    	 
    }
}