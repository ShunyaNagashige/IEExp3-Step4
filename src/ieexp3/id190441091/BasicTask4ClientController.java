/*
 * GUIクライアントのControllerクラス
 */
package ieexp3.id190441091;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class BasicTask4ClientController {
	@FXML
	private Button buttonConnect;
	@FXML
	private Button buttonDisconnect;
	@FXML
	private Button buttonRun;
	@FXML
	private TextField textFieldAddress;
	@FXML
	private TextField textFieldPort;
	@FXML
	private TextField textField1;
	@FXML
	private TextField textField2;
	@FXML
	private TextField textField3;
	@FXML
	private Label labelState;

	private CommunicationTask4 task;
	private Thread thread;

	/*
	 * ロボットが実行する操作を表す。(connect,run,disconnectのどれか)
	 * (この変数はヒープ領域にある？というのも，スレッド間で共有できるメモリはヒープ領域のはずだから。)
	 */
	public static String operation;

	/* ホールの番号を表す */
	private final int holeNum = 3;
	
	private String[] holes;

	@FXML
	protected void handleButtonConnectAction(ActionEvent event) {
		try {
			//入力されたホールの番号を，受け取る
			holes = new String[holeNum];
			TextField[] textFields = { textField1, textField2, textField3 };

			for (int i = 0; i < holeNum; i++) {
				holes[i] = textFields[i].getText();
			}

			task = new CommunicationTask4(holes,textFieldAddress.getText(),Integer.parseInt(textFieldPort.getText()));

			buttonConnect.disableProperty().bind(task.runningProperty());
			buttonRun.disableProperty().bind(task.runningProperty().not());
			buttonDisconnect.disableProperty().bind(task.runningProperty().not());
			labelState.textProperty().bind(task.messageProperty());
			
			operation = "connect";
			thread=new Thread(task);
			thread.setDaemon(true);
			thread.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	protected void handleButtonRunAction(ActionEvent event) {
		try {
			/*
			 * バックグラウンドスレッドを再度実行できるようにする。 
			 * ※Taskクラスは再利用できない1回限りのオブジェクトを定義するため，
			 * 　一度バックグラウンドスレッドを実行して終了した後，
			 * 　バックグラウンドタスクインスタンスが存在しても再度実行できない。
			 */
			if(task!=null)
				task=null;
			task=new CommunicationTask4();
			operation = "run";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	protected void handleButtonDisconnectAction(ActionEvent event) {
		try {
			/*
			 * handleButtonRunActionを参照
			 */
			if(task!=null)
				task=null;
			task=new CommunicationTask4();
			operation = "disconnect";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
