package com.jakey.live.params;

/**
 * Created by jakey on 2017/7/25.
 */

public class AudioParams {

    // 采样率
    private int sampleRateInHz = 44100;
    // 声道个数
    private int channel = 1;

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public void setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}
