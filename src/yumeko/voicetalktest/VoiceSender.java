package yumeko.voicetalktest;

import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;

public class VoiceSender {

    public interface Messager{
        public void postMessage(String message);
        public void postLog(String log);
    }

    public static final String TAG = "VoiceSender";
    public static final String TAG2 = "VoiceSenderTest";

    public static final int SEND_FRAME_COUNT_ONE_TIME = 10;
    public static final int BLOCK_SIZE[] = {12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };


    private boolean isRecording;

    private Messager        mMessager;
	private MediaRecorder   mMediaRecoder;
    private Connection      mConnection;

    private LocalSocket mLocalSocketSender;
    private LocalSocket mLocalSocketReceiver;
    private LocalServerSocket mLocalServerSocket;

    public void setMessager(Messager messager){
        mMessager = messager;
    }

    public boolean mediaRecoderInitialize(){



        if (mMediaRecoder != null) {
            if (isRecording) {
                mMediaRecoder.stop();
                isRecording = false;
            }
            mMediaRecoder.stop();
            mMediaRecoder.reset();
            mMediaRecoder.release();
        }
        mMediaRecoder = new MediaRecorder();
        mMediaRecoder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecoder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        final int mono = 1;
        mMediaRecoder.setAudioChannels(mono);
        mMediaRecoder.setAudioSamplingRate(8000);
        mMediaRecoder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecoder.setOutputFile(mLocalSocketSender.getFileDescriptor());

        try {
            mMediaRecoder.prepare();
            mMediaRecoder.start();
            isRecording = true;
            return true;
        } catch (Exception e) {
            releaseMediaRecorder();
            return false;
        }

    }

    private boolean localSocketInitialize(){

        try {
            releaseLocalSocket();

            String serverName = "armAudioServer";
            final int bufSize = 1024;

            mLocalServerSocket = new LocalServerSocket(serverName);

            mLocalSocketReceiver = new LocalSocket();
            mLocalSocketReceiver.connect(new LocalSocketAddress(serverName));
            mLocalSocketReceiver.setReceiveBufferSize(bufSize);
            mLocalSocketReceiver.setSendBufferSize(bufSize);

            mLocalSocketSender = mLocalServerSocket.accept();
            mLocalSocketSender.setReceiveBufferSize(bufSize);
            mLocalSocketSender.setSendBufferSize(bufSize);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void releaseMediaRecorder(){

        if(mMediaRecoder!=null){
            if (isRecording) {
                mMediaRecoder.stop();
                isRecording = false;
            }
            mMediaRecoder.reset();
            mMediaRecoder.release();
        }
        isRecording = false;
    }

    private void releaseLocalSocket(){
        if(mLocalSocketReceiver!=null){
            try {
                mLocalSocketReceiver.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mLocalSocketReceiver=null;
        }
        if(mLocalSocketSender!=null){
            try {
                mLocalSocketSender.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mLocalSocketSender=null;
        }
        if(mLocalServerSocket!=null){
            try{
                mLocalServerSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            mLocalServerSocket=null;
        }
    }

    private void releaseConnection(){
        mConnection.close();
        mConnection = null;
    }

    private int parseAmrDataFrame(DataInputStream dataInput,byte[] sendBuffer,int offset) throws IOException {
        dataInput.read(sendBuffer , offset , 1);
        int blockIndex = (int) (sendBuffer[offset] >> 3 )& 0x0F;
        int frameLength = BLOCK_SIZE[blockIndex];

        readData(sendBuffer, offset + 1, frameLength, dataInput);
        offset += frameLength +1;
        return offset;
    }

    private void skipAmrHead(DataInputStream dataInput){
        final byte[] AMR_HEAD = new byte[] { 0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A };
        int result = -1;
        int state = 0;
        try {
            while (-1 != (result = dataInput.readByte())) {
                if (AMR_HEAD[0] == result) {
                    state = (0 == state) ? 1 : 0;
                } else if (AMR_HEAD[1] == result) {
                    state = (1 == state) ? 2 : 0;
                } else if (AMR_HEAD[2] == result) {
                    state = (2 == state) ? 3 : 0;
                } else if (AMR_HEAD[3] == result) {
                    state = (3 == state) ? 4 : 0;
                } else if (AMR_HEAD[4] == result) {
                    state = (4 == state) ? 5 : 0;
                } else if (AMR_HEAD[5] == result) {
                    state = (5 == state) ? 6 : 0;
                }

                if (6 == state) {
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "skip amr head , read dat error");
        }
    }

    private void readData(byte[] buffer, int offset, int length, DataInputStream dataInput){
        int numOfRead = -1;
        while(true){
            try{
                numOfRead = dataInput.read(buffer , offset ,length);
                if(numOfRead == -1){
                    Log.d(TAG,"no incoming data");
                    mMessager.postLog("no incoming data");
                    Thread.sleep(100);
                } else {
                    offset += numOfRead;
                    length -= numOfRead;
                    if (length <= 0 ){
                        break;
                    }
                }
            }catch(Exception e){
                Log.e(TAG,"read date error");
                mMessager.postLog("read date error");
                break;
            }
        }
    }

    private void udpSend(byte[] buffer,int dataLength) throws IOException {
        byte[] newBuffer = new byte[dataLength];
        System.arraycopy(buffer,0,newBuffer,0 ,dataLength);
        //mConnection.send(newBuffer);
        testSend(newBuffer);
    }

    private void testSend(byte[] buffer){
        for(int i=0;i<buffer.length;i++){
            mMessager.postLog(i+","+buffer[i]);
        }
    }

    private void sendAmrAudio() throws IOException {
        DataInputStream dataInput = new DataInputStream(mLocalSocketReceiver.getInputStream());
        mConnection = Connection.connect(Config.SERVER_IP,Config.SERVER_PORT);

        skipAmrHead(dataInput);

        byte[] sendBuffer = new byte[1024];
        while(isRecording){
            int offset = 0;
            for(int index=0;index < SEND_FRAME_COUNT_ONE_TIME;++index){
                if(!isRecording){
                    break;
                }

                offset =parseAmrDataFrame(dataInput,sendBuffer,offset);
            }
            try{
                udpSend(sendBuffer,offset);
            }catch (IOException e){
                Log.e(TAG,"udp send error");
            }


        }
        releaseConnection();
        releaseLocalSocket();
        releaseMediaRecorder();
    }

    public void talk(){
        if(isRecording){
            isRecording = false;
        }else{
            if(localSocketInitialize()&&mediaRecoderInitialize()){
                mMessager.postMessage("voice sender ready");
            }else{
                mMessager.postMessage("voice sender initialize error");
            }
            new SendThread().start();
        }
    }

    class SendThread extends Thread{

        @Override
        public void run(){
            try {
                sendAmrAudio();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
