package yumeko.voicetalktest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

public class MainActivity extends Activity {

	private static int numAudioSource = MediaRecorder.AudioSource.MIC;
	private static int numSampleRateInHz = 44100;
	private static int numChannelConfigIn = AudioFormat.CHANNEL_IN_MONO;
	private static int numChannelConfigOut = AudioFormat.CHANNEL_OUT_MONO;
	private static int numAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

	private int numBufferSizeInBytes;
	private AudioRecord mAudioRecode;
	private boolean isRecording;
	
	private MediaRecorder mMediaRecorder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void record(View v) {
		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/test.pcm");

		if (file.exists()) {
			file.delete();
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to create "
					+ file.toString());
		}

		try {
			final FileOutputStream fos = new FileOutputStream(file);

			numBufferSizeInBytes = AudioRecord.getMinBufferSize(
					numSampleRateInHz, numChannelConfigIn, numAudioFormat);
			mAudioRecode = new AudioRecord(numAudioSource, numSampleRateInHz,
					numChannelConfigIn, numAudioFormat, numBufferSizeInBytes);

			final byte[] buffer = new byte[numBufferSizeInBytes];

			new Thread() {

				@Override
				public void run() {

					mAudioRecode.startRecording();
					isRecording = true;

					while (isRecording) {
						mAudioRecode.read(buffer, 0, numBufferSizeInBytes);
						try {
							fos.write(buffer);
						} catch (IOException e) {
							e.printStackTrace();
							isRecording = false;
						}
					}
					mAudioRecode.stop();
					mAudioRecode.release();// 释放资源
					mAudioRecode = null;
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void stop(View v) {
		isRecording = false;
	}

	public void play(View v) {
		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/test.pcm");
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		numBufferSizeInBytes = AudioTrack.getMinBufferSize(numSampleRateInHz,
				numChannelConfigOut, numAudioFormat);

		byte[] buffer = new byte[numBufferSizeInBytes];

		AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				numSampleRateInHz, numChannelConfigOut, numAudioFormat,
				numBufferSizeInBytes, AudioTrack.MODE_STREAM);
		// 放音
		audioTrack.play();

		final FileInputStream inFinal = in;
		final byte[] bufferFinal = buffer;
		final AudioTrack audioTrackFinal = audioTrack;
		new Thread() {

			@Override
			public void run() {
				int byteread = 0;

				try {
					while ((byteread = inFinal.read(bufferFinal)) != -1) {
						System.out.write(bufferFinal, 0, byteread);
						System.out.flush();
						audioTrackFinal.write(bufferFinal, 0,
								numBufferSizeInBytes);
					}
				} catch (Exception e) {
					Log.e("AudioTrack", "Playback Failed");
				}

				audioTrackFinal.stop();
				audioTrackFinal.release();
			}

		}.start();
	}
	
	private recordByAmr(){
		mMediaRecorder = new MediaRecorder();

		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		final int mono = 1;
		mMediaRecorder.setAudioChannels(mono);
		mMediaRecorder.setAudioSamplingRate(8000);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mMediaRecorder.setOutputFile(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/test.amr");
		
		new Thread(){
			
		}.start();
	}
	
	public void talk(View v){
		
	}
	
	private void initLocalSocket(){
		
	}
	
	private void prepareAudioRecoder(){
		
	}
	
	private void sendDate(){
		
	}
	
	private void readAmrFrame(){
		
	}
}
