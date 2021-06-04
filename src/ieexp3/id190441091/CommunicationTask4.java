/*
 * GUIクライアントのバックグラウンドスレッドが実行するTaskクラス
 */
package ieexp3.id190441091;

import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import ieexp3.library.CaoAPI;
import javafx.concurrent.Task;

public class CommunicationTask4 extends Task<Void> {
	private String[] holes;
	volatile private boolean isLoop;
	private String addr;
	private int port;

	public CommunicationTask4(String[] holes, String addr, int port) throws Exception{
		for (String hole : holes) {
			if (Integer.parseInt(hole) <= 0 || Integer.parseInt(hole) > 6)
				throw new IllegalArgumentException("1から6の数字を入力してください。");
		}
		
		if(addr.equals("")) {
			throw new IllegalArgumentException("IPアドレスが空です");
		}
		
		if(port==0) {
			throw new IllegalArgumentException("ポート番号が空です");
		}
		
		this.holes = holes;
		this.port = port;
		this.addr = addr;
	}
	
	public CommunicationTask4() throws Exception{}

	@Override
	protected Void call() throws Exception {
		Socket socket = null;
		
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(addr, port));

			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

			isLoop = false;

			String cmd=null;
			
			if (BasicTask4ClientController.operation == "connect") {
				cmd = createCmd(BasicTask4ClientController.operation);

				// データの送信
				dos.writeUTF(cmd);
				dos.flush();
				
				isLoop = true;
				BasicTask4ClientController.operation = "no operation";
				System.out.println("Connected completely.");
				updateMessage("State:Connected.");
			}

			while (isLoop) {
				switch (BasicTask4ClientController.operation) {
				case "run":
					System.out.println("Start!");
					updateMessage("State:Running.");
					cmd = createCmd(BasicTask4ClientController.operation);

					// データの送信
					dos.writeUTF(cmd);
					dos.flush();

					BasicTask4ClientController.operation = "no operation";
					updateMessage("State:Run.");
					break;

				case "disconnect":
					cmd = createCmd(BasicTask4ClientController.operation);

					// データの送信
					dos.writeUTF(cmd);
					dos.flush();

					isLoop = false;
					System.out.println("Disconnected completely.");
					BasicTask4ClientController.operation = "no operation";
					updateMessage("State:Disonnected.");
					break;

				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Service restart.");
			
			if(socket!=null) {
				try{
					socket.close();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	//コマンドの生成
	private String createCmd(String operation) {
		String cmd = operation;

		if (operation == "connect") {
			for (String hole : this.holes) {
				cmd += " ";
				cmd += hole;
			}
		}

		return cmd;
	}
}