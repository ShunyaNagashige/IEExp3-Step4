/*
 * サーバ側のバックグラウンドスレッドが実行するTaskクラス
 */
package ieexp3.id190441091;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ieexp3.library.CaoAPI;

public class BasicTask4ServerTask implements Runnable {
	// 可変長の配列が欲しいのでArrayListを使用
	private static ArrayList<Thread> threadList;
	private final static int holeNum = 3;
	private Socket socket;
	private String[] holes;
	volatile private boolean threadStatus;

	public BasicTask4ServerTask(Socket socket) {
		this.socket = socket;

		if (threadList == null) {
			threadList = new ArrayList<Thread>();
		}

		// Thread.currentThread():現在実行中のスレッドオブジェクトの参照を返す
		threadList.add(Thread.currentThread());
	}

	@Override
	public void run() {
		String message = null;
		
		try {
			// ソケットの入出力ストリームを取得し，データ入力ストリームを連結
			DataInputStream socketDIS = new DataInputStream(socket.getInputStream());

			threadStatus = false;

			//クライアントからのメッセージを受け取る
			message = socketDIS.readUTF();

			//connectコマンドならロボットへの接続を行う
			if (message.startsWith("connect")) {
				StringTokenizer st = new StringTokenizer(message, " ");
				st.nextToken();

				holes = new String[holeNum];

				for (int i = 0; i < holeNum; i++) {
					holes[i] = st.nextToken();
				}

				connect();

				threadStatus = true;
				BasicTask4ClientController.operation = "no operation";
				System.out.println("Connected completely.");
			}

			while (threadStatus) {
				// クライアントからのメッセージを受け取る
				message = socketDIS.readUTF();

				// disconnectコマンドならロボットおよびクライアントとの通信を切断
				if (message.startsWith("disconnect")) {
					disconnect();
					socket.close();
					threadList.remove(Thread.currentThread());
					threadStatus = false;
				}

				// runコマンドならロボットを制御する
				if (message.startsWith("run")) {
					process();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private void connect() throws Exception {
		// CAOエンジンの初期化
		CaoAPI.init("TestWorkspace");
		System.out.println("CAO engine is initialized.");

		// コントローラに接続
		CaoAPI.connect("RC8", "VE026A");
		System.out.println("Controller and Robot are connected.");

		// 自動モードに設定
		CaoAPI.setControllerMode(CaoAPI.CONTROLLER_MODE_AUTO);
		System.out.println("Operation mode is set to Auto mode.");

		// モータを起動
		CaoAPI.turnOnMotor();
		System.out.println("Motor is turned on.");

		// ロボットの外部速度/加速度/減速度を設定
		float speed = 50.0f, accel = 25.0f, decel = 25.0f;
		CaoAPI.setExtSpeed(speed, accel, decel);
		System.out.println(
				"External speed/acceleration/deceleration is set to " + speed + "/" + accel + "/" + decel + ".");
	}

	/**
	 * ロボットを制御するコマンドを記述する。
	 * 
	 * @throws Exception ロボットの制御に失敗した場合
	 */
	private void process() throws Exception {
		// ロボット操作

		// TakeArm Keep = 0
		CaoAPI.takeArm(0L, 0L);

		// Speed 100
		CaoAPI.speed(-1L, 100.0f);

		// Approach P, P5, @0 50
		// Move L, @0 P5, S = 50
		// DriveA (7, F1)
		// Depart L, @P 50
		CaoAPI.approach(1L, "P" + holes[0], "@0 50", "");
		CaoAPI.move(2L, "@0 P" + holes[0], "S = 50");
		CaoAPI.driveAEx("(7, -45)", "");
		CaoAPI.depart(2L, "@P 50", "");

		// Approach P, P5, @0 50
		// Move L, @0 P5, S = 50
		// DriveA (7, F1)
		// Depart L, @P 50
		CaoAPI.approach(1L, "P" + holes[1], "@0 50", "");
		CaoAPI.move(2L, "@0 P" + holes[1], "S = 50");
		CaoAPI.driveAEx("(7, 25)", "");
		CaoAPI.depart(2L, "@P 50", "");

		// Approach P, P5, @0 50
		// Move L, @0 P5, S = 50
		// DriveA (7, F1)
		// Depart L, @P 50
		CaoAPI.approach(1L, "P" + holes[1], "@0 50", "");
		CaoAPI.move(2L, "@0 P" + holes[1], "S = 50");
		CaoAPI.driveAEx("(7, -45)", "");
		CaoAPI.depart(2L, "@P 50", "");

		// Approach P, P5, @0 50
		// Move L, @0 P5, S = 50
		// DriveA (7, F1)
		// Depart L, @P 50
		CaoAPI.approach(1L, "P" + holes[2], "@0 50", "");
		CaoAPI.move(2L, "@0 P" + holes[2], "S = 50");
		CaoAPI.driveAEx("(7, 25)", "");
		CaoAPI.depart(2L, "@P 50", "");

		CaoAPI.giveArm();
	}

	private void disconnect() throws Exception {
		// モータを停止
		CaoAPI.turnOffMotor();
		System.out.println("Moter is turned off.");

		// コントローラから切断
		CaoAPI.disconnect();
		System.out.println("Controller and Robot is disconnected.");
	}

}
