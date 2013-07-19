package yumeko.voicetalktest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements VoiceSender.Messager{

	private static int numAudioSource = MediaRecorder.AudioSource.MIC;
	private static int numSampleRateInHz = 44100;
	private static int numChannelConfigIn = AudioFormat.CHANNEL_IN_MONO;
	private static int numChannelConfigOut = AudioFormat.CHANNEL_OUT_MONO;
	private static int numAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

	private int numBufferSizeInBytes;
	private AudioRecord mAudioRecode;
	private boolean isRecording;
	
	private VoiceSender mVoiceSender;

    private EditText mServerIPTextbox;
    private TextView mLogTextbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        mServerIPTextbox = (EditText) findViewById(R.id.ip_and_port);
        mLogTextbox      = (TextView) findViewById(R.id.amr_data_log);
        mVoiceSender = new VoiceSender();
        mVoiceSender.setMessager(this);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void talk(View v){
        mVoiceSender.talk();
	}

    public void connect(View v){
        try {
            Connection connection = Connection.connect(Config.SERVER_IP,Config.SERVER_PORT);
            postMessage("udpsocket ready,target server ip - " + Config.SERVER_IP + ":" + Config.SERVER_PORT);
        } catch (SocketException e) {
            postLog(e.toString());
            postMessage("socket exception");
        } catch (UnknownHostException e) {
            postLog(e.toString());
            postMessage("unknownHost exception");
        }
    }

    @Override
    public void postMessage(String message) {
        mServerIPTextbox.setText(message);
    }

    @Override
    public void postLog(String log) {
        if(log.endsWith("\n")){
            mLogTextbox.append(log);
        }else{
            mLogTextbox.append(log+"\n");
        }
    }
}
