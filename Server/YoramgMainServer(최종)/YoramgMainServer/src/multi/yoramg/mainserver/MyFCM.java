package multi.yoramg.mainserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFCM {
//	public static void main(String[] args) throws Exception {
//		String token = "fxOYyI52GDI:APA91bHVSUnzrAf491Kn9_ESdDZRvWtKDBZk8dOgPuFVW9o0Xd0hgFPdINI6imlGr5gWKtww1GXGdLdYd4eCnOUV98GUcqugCdFHrg1hInbTxX1Y5jo8qq_asQolG3JgsGH14BJjqy7t";
//		sendPush("���־�! ������������", "��� �ڻ쳻��", token);
//	}
	
	public void sendPush(String msgTitle, String msgContent, String userToken) throws Exception {

		final String apiKey = "AAAAxF2NN6o:APA91bH2c-_CKYWowF8SM6zeeXQX8WItYrcjMeotl7lUdDSie5Iar4q5FHwSht6DJGc-lTDxHaoohiyE3B6NMF6OX9GFE_C8LPumMpeMZRb7yGYFBDSAUFG2rroH1E5Htty_XWBxrGVe";
		URL url = new URL("https://fcm.googleapis.com/fcm/send");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // �� �ձ�
		conn.setDoOutput(true);
		conn.setRequestMethod("POST"); //post����
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "key=" + apiKey);

		conn.setDoOutput(true);

		// �̷��� ������ ������ ALL�� �����س��� ��� ��������� �˸��� �����ش�.
//		String input = "{\"notification\" : {\"title\" : \"����� ���� �ֱ� \", \"body\" : \"����� ���� �ֱ�\"}, \"to\":\"/topics/ALL\"}";

		// �̰ɷ� ������ Ư�� ��ū�� �������ִ� ���ÿ��� �˸��� �����ش� ���� ���߿� �Ѱ� ��� ��������
		String input = "{\"notification\" : {\"title\" : \""+msgTitle+"\", \"body\" : \""+msgContent+"\"}, \"to\":\""+userToken+"\"}";

		OutputStream os = conn.getOutputStream();

		// �������� ������ �ѱ� ������ ����� �Ʒ�ó�� UTF-8�� ���ڵ��ؼ� ��������
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
