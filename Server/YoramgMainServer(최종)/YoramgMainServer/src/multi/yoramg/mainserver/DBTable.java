package multi.yoramg.mainserver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DBTable {
	DBTable() {}
	
	public boolean deleteUser (String id_, String serial_) {
		boolean flag = false;
		String blank = "blank!";
		
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		String sql = "UPDATE user_device_info SET id=?, password=?, "
				+ "name=?, birthday=?, devicestate=?, fcm_token=?"
				+ " where serial=?;";
		PreparedStatement pstmt = null;
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			pstmt.setString(1, blank); // id
			pstmt.setString(2, blank);	// ���
			pstmt.setString(3, blank);	// �̸�
			pstmt.setString(4, blank);	// ����
			pstmt.setString(5, blank);	// ������
			pstmt.setString(6, blank);	// ��ū
			pstmt.setString(7, serial_);	// �ø���� ã�ƶ�
			int r = pstmt.executeUpdate();
			pstmt.close();
			flag = true;
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		
		return flag;
	}
	// user_device_info ���� get�ؿ��� �޼ҵ�� �ϳ��� ��ĥ �� ���� ��
	// sql�� ? �ְ� �Ű������� �ε��� �ϳ� �޾Ƽ� ���ǹ����� ?�� ä��� �ɵ�
	public boolean checkId (String id_) {
		boolean flag = true;
		
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		String id = "";
		String sql = "SELECT id FROM user_device_info;";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			rs = pstmt.executeQuery();
			while ( rs.next() ) {
				id = rs.getString("id");
				if ( id.equals(id_) ) {
					flag = false;
					break;
				}
			}
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		return flag;
	}
	
	
	public boolean modifyUser(String id_, String newPassword_,
			String newName_, String newBirthday_, String newSerial_) {
		
		String serial = getUser_Device_Info(id_, 0);
		boolean flag = false;
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		PreparedStatement pstmt = null;
		
		// �ø��� ������ �ƴ϶��
		if ( serial.equals(newSerial_)) {
			String sql = "UPDATE user_device_info SET password=?, name=?, birthday=?"
					+ " where id=?;";
			try {
				pstmt = dbConnect.con.prepareStatement( sql );
				pstmt.setString(1, newPassword_);
				pstmt.setString(2, newName_);
				pstmt.setString(3, newBirthday_);
				pstmt.setString(4, id_);
				int r = pstmt.executeUpdate();
				pstmt.close();
				flag = true;
			} catch ( SQLException e ) {}
			finally {
				dbConnect.closeDB();
			}
		}
		// �ø��� ������ ���
		else {
			// ���� ���̵�� �ø���� �������� �������� ��
			boolean deleteFlag = deleteUser(id_, serial);
			// ���ο� ������ ���ø��� ���
			boolean resisterFlag = resisterUser(id_, newPassword_, newName_, newBirthday_, newSerial_);
			if ( deleteFlag && resisterFlag) {
				flag = true;
			}
		}
		return flag;
	}
	
	public boolean resisterUser(String id_, String password_,
			String name_, String birthday_, String serial_) {
		boolean flag = false;
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		String sql = "UPDATE user_device_info SET id=?, password=?, name=?, birthday=?, fcm_token=?"
				+ " where serial=?;";
		PreparedStatement pstmt = null;
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			pstmt.setString(1, id_);
			pstmt.setString(2, password_);
			pstmt.setString(3, name_);
			pstmt.setString(4, birthday_);
			pstmt.setString(5, "blank");
			pstmt.setString(6, serial_);
			int r = pstmt.executeUpdate();
			pstmt.close();
			flag = true;
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		return flag;
	}
	
	public String getFcmToken (String id_) {
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		String fcmToken = "";
		String sql = "SELECT fcm_token FROM user_device_info where id='" + id_ + "';";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			rs = pstmt.executeQuery();
			if ( rs.next() ) {
				fcmToken = rs.getString("fcm_token");
			}
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		return fcmToken;
	}
	public void setFcmToken (String id_, String fcmToken_) {
		String oldFcmToken = getFcmToken(id_);
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		
		String sql = "UPDATE user_device_info SET fcm_Token=? where id=?;";
		PreparedStatement pstmt = null;
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			pstmt.setString(1, fcmToken_);
			pstmt.setString(2, id_);
			int r = pstmt.executeUpdate();
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		String newFcmToken = getFcmToken(id_);
		System.out.println("oldFcmToken : " + oldFcmToken + " " + "newFcmToken : " + newFcmToken);
	}
	
	public String getUser_Device_Info(String id_, int idx) {
		String what = "";
		String sql = null;
		String result = "";
		switch (idx) {
			case 0 :
				what = "serial";
				sql = "SELECT serial FROM user_device_info where id='" + id_ + "';";
				break;
			case 1 :
				what = "password";
				sql = "SELECT password FROM user_device_info where id='" + id_ + "';";
				break;
			case 2 :
				what = "name";
				sql = "SELECT name FROM user_device_info where id='" + id_ + "';";
				break;
			case 3 :
				what = "birthday";
				sql = "SELECT birthday FROM user_device_info where id='" + id_ + "';";
				break;
			case 4 :
				what = "devicestate";
				sql = "SELECT devicestate FROM user_device_info where id='" + id_ + "';";
				break;
		}
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		String birthday = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			rs = pstmt.executeQuery();
			
			if ( what.equals("serial")) {
				if ( rs.next() ) {
					result = rs.getString("serial");
				}
			}
			else if ( what.equals("password")) {
				if ( rs.next() ) {
					result = rs.getString("password");
				}
			}
			else if ( what.equals("name")) {
				if ( rs.next() ) {
					result = rs.getString("name");
				}
			}
			else if ( what.equals("birthday")) {
				if ( rs.next() ) {
					result = rs.getString("birthday");
				}
			}
			else if ( what.equals("devicestate")) {
				if ( rs.next() ) {
					result = rs.getString("devicestate");
				}
			}
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		return result;
	}

	public String getId(String serial_) {
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		String id = "";
		String sql = "SELECT id FROM user_device_info where serial='" + serial_ + "';";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			rs = pstmt.executeQuery();
			if ( rs.next() ) {
				id = rs.getString("id");
			}
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		return id;
	}
	
	
	public boolean serialCheck(String serial_) {
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		boolean check = false;
		String serial;
		String sql = "SELECT * FROM user_device_info";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
						
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			rs = pstmt.executeQuery();

			while ( rs.next() ) {
				serial = rs.getString("serial");
				if ( serial.equals(serial_) ) {
					check = true;
					break;
				}
			}
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		return check;
	}
	
	public int getIdx () {
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		int idx = 0;
		String sql = "SELECT * FROM idx";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
						
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			rs = pstmt.executeQuery();
			if ( rs.next() ) {
				idx = rs.getInt("idx");
			}
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		return idx;
	}
	public void setIdx () {
		int oldIdx = getIdx();
		int idx = oldIdx + 1;
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		
		String sql = "UPDATE idx SET idx=? where idx=?;";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
						
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			pstmt.setInt(1, idx);
			pstmt.setInt(2, oldIdx);
			int r = pstmt.executeUpdate();
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		int newIdx = getIdx();
		System.out.println("oldIdx : " + oldIdx + " " + "newIdx : " + newIdx);
	}
	
	public String getDevicestate (String id_) {
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		String deviceState = "";
		String sql = "SELECT devicestate FROM user_device_info where id='" + id_ + "';";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			rs = pstmt.executeQuery();
			if ( rs.next() ) {
				deviceState = rs.getString("devicestate");
			}
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		return deviceState;
	}
	public void setDevicestate (String id_, String state_) {
		String oldDevicestate = getDevicestate(id_);
		String devicestate = state_;
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		
		String sql = "UPDATE user_device_info SET devicestate=? where id=?;";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			pstmt.setString(1, devicestate);
			pstmt.setString(2, id_);
			int r = pstmt.executeUpdate();
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		String newDevicestate = getDevicestate(id_);
		System.out.println("oldDevicestate : " + oldDevicestate + " " + "newDevicestate : " + newDevicestate);
	}
	 
	public boolean loginCheck(String id_, String password_ ) {
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		boolean check = false;
		System.out.println("id : " + id_ + "\tpasswd : " + password_);
		String password = "";
		String sql = "SELECT password FROM user_device_info where id='" + id_ + "';";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
						
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			rs = pstmt.executeQuery();
			// �޾ƿ� id�� where �������� ���� password�� ������ �޴´�
			// password�� ���̸� ���̵� ���°Ŵϱ� false
			// password�� ���� �ƴϸ� password_ �� ���ؼ� ���� ������ true �ƴϸ�false
			rs.next();
			password = rs.getString("password");
			//System.out.println("�Է� ���� " + id_ + "��� : " + password_ + "\tdb�� �ִ� ��� : " + password);
			
			if ( password != null && password.equals(password_) ) {
				check = true;
			}
			else {
				pstmt.close();
			}
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		System.out.println("�α��� üũ ���ϵǴ� �� : " + check);
		return check;
		
	}
	
	public String readSleepData(String serial_, String data_, String time_) {
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		// serial_ �� �����ϴ� �ڷ���� ���� �̾Ƴ� ��
		// data���� data_������ ū ������ �̾Ƴ� ��
		// time���� time_������ ū ������ �̴´�.
		// state, date, time �� state#date#time@state#date#time ���·� �������ش�
		String sql = "SELECT state, date, time FROM sleep_data where serial='" + serial_ + "';";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sleepdata = "";
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			rs = pstmt.executeQuery();
			//sleepdata = rs.getString("@");
			
			while ( rs.next() ) {
				
				sleepdata += rs.getString("state");
				sleepdata += "#";
				sleepdata += rs.getString("date");
				sleepdata += "#";
				sleepdata += rs.getString("time");
				sleepdata += "@";
				
			}
			
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		return sleepdata;
	}
	
	public void resisterSleepData (String message, String serial_, int idx_) {
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		int idx = idx_;
		String serial = serial_;
		String state = null;
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		System.out.println(serial);
		if ( message.equals("SLEEPSTART") ) {
			state = "start";
		}
		else if ( message.equals("SLEEPSTOP") ) {
			state = "stop";
		}
		String sql = "insert into sleep_data ( idx, serial, state, date, time )"
				+ "values ( ?, ?, ?, ?, ? )";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = dbConnect.con.prepareStatement( sql );
			pstmt.setInt(1, idx);
			pstmt.setString( 2,  serial );
			pstmt.setString( 3,  state );
			pstmt.setString( 4,  date );
			pstmt.setString( 5,  time );
			pstmt.executeUpdate();
			pstmt.close();
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}		
	}
}
