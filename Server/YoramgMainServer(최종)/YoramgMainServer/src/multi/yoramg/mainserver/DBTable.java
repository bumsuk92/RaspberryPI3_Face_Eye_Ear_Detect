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
			pstmt.setString(2, blank);	// 비번
			pstmt.setString(3, blank);	// 이름
			pstmt.setString(4, blank);	// 생일
			pstmt.setString(5, blank);	// 기계상태
			pstmt.setString(6, blank);	// 토큰
			pstmt.setString(7, serial_);	// 시리얼로 찾아라
			int r = pstmt.executeUpdate();
			pstmt.close();
			flag = true;
		} catch ( SQLException e ) {}
		finally {
			dbConnect.closeDB();
		}
		
		return flag;
	}
	// user_device_info 에서 get해오는 메소드들 하나로 합칠 수 있을 듯
	// sql에 ? 넣고 매개변수로 인덱스 하나 받아서 조건문으로 ?를 채우면 될듯
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
		
		// 시리얼 변경이 아니라면
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
		// 시리얼 변경일 경우
		else {
			// 기존 아이디와 시리얼로 유저정보 삭제부터 함
			boolean deleteFlag = deleteUser(id_, serial);
			// 새로운 정보를 새시리얼에 등록
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
			// 받아온 id로 where 쿼리문을 통해 password를 변수에 받는다
			// password가 널이면 아이디가 없는거니까 false
			// password가 널이 아니면 password_ 랑 비교해서 둘이 같으면 true 아니면false
			rs.next();
			password = rs.getString("password");
			//System.out.println("입력 받은 " + id_ + "비번 : " + password_ + "\tdb에 있는 비번 : " + password);
			
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
		System.out.println("로그인 체크 리턴되는 값 : " + check);
		return check;
		
	}
	
	public String readSleepData(String serial_, String data_, String time_) {
		DBConnect dbConnect = new DBConnect( "test_yoramg", "root", "a123" ); 
		// serial_ 과 일지하는 자료들은 전부 뽑아낸 뒤
		// data값이 data_값보다 큰 값들을 뽑아낸 뒤
		// time값이 time_값보다 큰 값들을 뽑는다.
		// state, date, time 을 state#date#time@state#date#time 형태로 리턴해준다
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
