package multi.yoramg.mainserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFCM {
//	public static void main(String[] args) throws Exception {
//		String token = "fxOYyI52GDI:APA91bHVSUnzrAf491Kn9_ESdDZRvWtKDBZk8dOgPuFVW9o0Xd0hgFPdINI6imlGr5gWKtww1GXGdLdYd4eCnOUV98GUcqugCdFHrg1hInbTxX1Y5jo8qq_asQolG3JgsGH14BJjqy7t";
//		sendPush("한주야! 무너지지말자", "통신 박살내자", token);
//	}
	
	public void sendPush(String msgTitle, String msgContent, String userToken) throws Exception {

		final String apiKey = "AAAAxF2NN6o:APA91bH2c-_CKYWowF8SM6zeeXQX8WItYrcjMeotl7lUdDSie5Iar4q5FHwSht6DJGc-lTDxHaoohiyE3B6NMF6OX9GFE_C8LPumMpeMZRb7yGYFBDSAUFG2rroH1E5Htty_XWBxrGVe";
		URL url = new URL("https://fcm.googleapis.com/fcm/send");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 길 뚫기
		conn.setDoOutput(true);
		conn.setRequestMethod("POST"); //post쓴다
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "key=" + apiKey);

		conn.setDoOutput(true);

		// 이렇게 보내면 주제를 ALL로 지정해놓은 모든 사람들한테 알림을 날려준다.
//		String input = "{\"notification\" : {\"title\" : \"여기다 제목 넣기 \", \"body\" : \"여기다 내용 넣기\"}, \"to\":\"/topics/ALL\"}";

		// 이걸로 보내면 특정 토큰을 가지고있는 어플에만 알림을 날려준다 위에 둘중에 한개 골라서 날려주자
		String input = "{\"notification\" : {\"title\" : \""+msgTitle+"\", \"body\" : \""+msgContent+"\"}, \"to\":\""+userToken+"\"}";

		OutputStream os = conn.getOutputStream();

		// 서버에서 날려서 한글 깨지는 사람은 아래처럼 UTF-8로 인코딩해서 날려주자
		os.write(input.getBytes("UTF-8"));
		os.flush();
		os.close();

		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + input);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		// print result
		System.out.println(response.toString());
	}

}
