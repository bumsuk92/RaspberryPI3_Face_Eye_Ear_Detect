package multi.yoramg.myapplication;
/**
 * 20181122 / 작성자 : 배한주
 * 서버와의 소켓연결 기능을 구현하기 위한 인터페이스
 */
public interface IClient {
    public void sendMessage(String data);
    public void connectToServer();
    public void receiveMessage();
}
