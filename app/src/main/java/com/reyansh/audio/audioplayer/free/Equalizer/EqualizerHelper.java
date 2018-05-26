package com.reyansh.audio.audioplayer.free.Equalizer;

import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;

public class EqualizerHelper {


    private Equalizer mEqualizer;
    private Virtualizer mVirtualizer;
    private BassBoost mBassBoost;
    private PresetReverb mPresetReverb;
    private boolean mIsEqualizerSupported = true;

    private int m50HzLevel = 16;
    private int m130HzLevel = 16;
    private int m320HzLevel = 16;
    private int m800HzLevel = 16;
    private int m2kHzLevel = 16;
    private int m5kHzLevel = 16;
    private int m12kHzLevel = 16;

    private short mVirtualizerLevel = 0;
    private short mBassBoostLevel = 0;
    private short mReverbSetting = 0;

    public EqualizerHelper(int audioSessionId1, boolean equalizerEnabled) throws RuntimeException {

        mEqualizer = new Equalizer(0, audioSessionId1);
        mEqualizer.setEnabled(equalizerEnabled);

        mVirtualizer = new Virtualizer(0, audioSessionId1);
        mVirtualizer.setEnabled(equalizerEnabled);

        mBassBoost = new BassBoost(0, audioSessionId1);
        mBassBoost.setEnabled(equalizerEnabled);

        mPresetReverb = new PresetReverb(0, audioSessionId1);
        mPresetReverb.setEnabled(equalizerEnabled);
    }


    public Equalizer getEqualizer() {
        return mEqualizer;
    }

    public void setEqualizer(Equalizer equalizer) {
        mEqualizer = equalizer;
    }

    public BassBoost getBassBoost() {
        return mBassBoost;
    }

    public void setBassBoost(BassBoost bassBoost) {
        mBassBoost = bassBoost;
    }

    public Virtualizer getVirtualizer() {
        return mVirtualizer;
    }

    public void setVirtualizer(Virtualizer virtualizer) {
        mVirtualizer = virtualizer;
    }

    public PresetReverb getPresetReverb() {
        return mPresetReverb;
    }

    public void setPresetReverb(PresetReverb presetReverb) {
        mPresetReverb = presetReverb;
    }

    public int get50HzLevel() {
        return m50HzLevel;
    }

    public void set50HzLevel(int l50HzLevel) {
        m50HzLevel = l50HzLevel;
    }

    public int get130HzLevel() {
        return m130HzLevel;
    }

    public void set130HzLevel(int l130HzLevel) {
        m130HzLevel = l130HzLevel;
    }

    public int get320HzLevel() {
        return m320HzLevel;
    }

    public void set320HzLevel(int l320HzLevel) {
        m320HzLevel = l320HzLevel;
    }

    public int get800HzLevel() {
        return m800HzLevel;
    }

    public void set800HzLevel(int l800HzLevel) {
        m800HzLevel = l800HzLevel;
    }

    public int get2kHzLevel() {
        return m2kHzLevel;
    }

    public void set2kHzLevel(int l2kHzLevel) {
        m2kHzLevel = l2kHzLevel;
    }

    public int get5kHzLevel() {
        return m5kHzLevel;
    }

    public void set5kHzLevel(int l5kHzLevel) {
        m5kHzLevel = l5kHzLevel;
    }

    public int get12kHzLevel() {
        return m12kHzLevel;
    }

    public void set12kHzLevel(int l12kHzLevel) {
        m12kHzLevel = l12kHzLevel;
    }

    public short getVirtualizerLevel() {
        return mVirtualizerLevel;
    }

    public void setVirtualizerLevel(short virtualizerLevel) {
        mVirtualizerLevel = virtualizerLevel;
    }

    public short getBassBoostLevel() {
        return mBassBoostLevel;
    }

    public void setBassBoostLevel(short bassBoostLevel) {
        mBassBoostLevel = bassBoostLevel;
    }

    public short getReverbSetting() {
        return mReverbSetting;
    }

    public void setReverbSetting(short reverbSetting) {
        mReverbSetting = reverbSetting;
    }

    public boolean isEqualizerSupported() {
        return mIsEqualizerSupported;
    }

    public void setIsEqualizerSupported(boolean isSupported) {
        mIsEqualizerSupported = isSupported;
    }


    public void releaseEQObjects() throws Exception {
        mEqualizer.release();
        mVirtualizer.release();
        mBassBoost.release();
        mPresetReverb.release();
        mEqualizer = null;
        mVirtualizer = null;
        mBassBoost = null;
        mPresetReverb = null;
    }

    public PresetReverb getReverb() {
        return mPresetReverb;
    }
}
