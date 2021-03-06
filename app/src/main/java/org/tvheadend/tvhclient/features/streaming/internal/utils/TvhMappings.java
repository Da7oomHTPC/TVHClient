package org.tvheadend.tvhclient.features.streaming.internal.utils;

import timber.log.Timber;

public class TvhMappings {

    private TvhMappings() {
        throw new IllegalAccessError("Utility class");
    }

    private static final int[] mSampleRates = new int[]{
            96000, 88200, 64000, 48000,
            44100, 32000, 24000, 22050,
            16000, 12000, 11025, 8000,
            7350, 0, 0, 0
    };

    public static int sriToRate(int sri) {
        return mSampleRates[sri & 0xf];
    }

    public static int androidSpeedToTvhSpeed(float speed) {
        // Translate the speed value from what Android uses, to what TVHeadend expects.
        // TVHeadend expects: 0=pause, 100=1x fwd, -100=1x backward)
        int translatedSpeed;
        switch((int) speed) {
            case 1: // Normal Playback
                translatedSpeed = 100;
                break;
            case -2: // 2x Rewind
                translatedSpeed = -200;
                break;
            case -4: // 3x Rewind
                translatedSpeed = -300;
                break;
            case -12: // 4x Rewind
                translatedSpeed = -400;
                break;
            case -48: // 5x Rewind
                translatedSpeed = -500;
                break;
            case 2: // 2x Fast forward
                translatedSpeed = 200;
                break;
            case 8: // 3x Fast forward
                translatedSpeed = 300;
                break;
            case 32: // 4x Fast forward
                translatedSpeed = 400;
                break;
            case 128: // 5x Fast forward
                translatedSpeed = 500;
                break;
            default:
                throw new IllegalArgumentException("Unknown speed: " + speed);
        }
        Timber.d("Translated android speed " + speed + " to TVH speed " + translatedSpeed);
        return translatedSpeed;
    }
}
