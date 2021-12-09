package com.nd.zxingcode.manager;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

import java.io.Closeable;

/**
 * @author cwj
 */
public final class BeepManager implements MediaPlayer.OnErrorListener, Closeable {

    private static final float BEEP_VOLUME = 0.90f;
    private static final long VIBRATE_DURATION = 200L;

    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private boolean mIsPlayBeep;
    private boolean mIsVibrate;

    public BeepManager(Context context) {
        this.mContext = context;
        this.mMediaPlayer = null;
        updatePrefs(true, true);
    }

    public synchronized void updatePrefs(boolean playBeep, boolean vibrate) {
        this.mIsPlayBeep = playBeep;
        this.mIsVibrate = vibrate;
//        if (playBeep && mMediaPlayer == null) {
//            // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
//            // so we now play on the music stream.
//            mMediaPlayer = buildMediaPlayer(mContext);
//        }
    }

    public synchronized void playBeepSoundAndVibrate() {
//        if (mIsPlayBeep && mMediaPlayer != null) {
//            mMediaPlayer.start();
//            mMediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
//        }
        if (mIsVibrate) {
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

//    private MediaPlayer buildMediaPlayer(Context context) {
//        MediaPlayer mediaPlayer = new MediaPlayer();
//        try {
//            AssetFileDescriptor file = context.getResources().openRawResourceFd(R.raw.zxingcode_scan_beep);
//            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
//            mediaPlayer.setOnErrorListener(this);
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mediaPlayer.setLooping(false);
//            mediaPlayer.prepare();
//            file.close();
//            return mediaPlayer;
//        } catch (IOException ioe) {
////            Log.w(TAG, ioe);
//            ioe.printStackTrace();
//            mediaPlayer.release();
//            return null;
//        }
//    }

    @Override
    public synchronized boolean onError(MediaPlayer mp, int what, int extra) {
        close();
        return true;
    }

    @Override
    public synchronized void close() {
//        if (mMediaPlayer != null) {
//            mMediaPlayer.release();
//            mMediaPlayer = null;
//        }
    }

}
