package multi.yoramg.mainserver;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {
	private String dbName;
	private String userId;
	private String password;
	private String url;
	
	public Connection con = null;
	
	public DBConnect() {}
	public DBConnect( String dbName, String userId, String password ) {
		this.dbName = dbName;
		this.userId = userId;
		this.password = password;
		url = "jdbc:mysql://localhost:3306/"
				+ this.dbName
				+ "?serverTimezone=UTC&"
				+ "useSSL=false&"
				+ "allowPublicKeyRetrieval=true";
		connectDB();
	}
	
	public void connectDB() {
		try {
			Class.forName( "com.mysql.cj.jdbc.Driver" );
			try  {
				con = DriverManager.getConnection( url, userId, password );  
			} catch ( SQLException e ) {
				e.printStackTrace();
			}
		} catch ( ClassNotFoundException e ) {
			System.out.println( "not found JDBC Driver" );		
		} 		
	}
	
	public void closeDB() {
		try {
			con.close();
		} catch ( SQLException e ) {}
	}
	
}
