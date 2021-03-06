/*
 * Copyright (c) 2017 Kiall Mac Innes <kiall@macinnes.ie>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tvheadend.tvhclient.features.streaming.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.ArrayList;

import timber.log.Timber;

class TvheadendRenderersFactory extends DefaultRenderersFactory {

    TvheadendRenderersFactory(Context context) {
        super(context, null, EXTENSION_RENDERER_MODE_PREFER, DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
    }

    /**
     * Builds video renderers for use by the player.
     *
     * @param context                   The {@link Context} associated with the player.
     * @param drmSessionManager         An optional {@link DrmSessionManager}. May be null if the player
     *                                  will not be used for DRM protected playbacks.
     * @param allowedVideoJoiningTimeMs The maximum duration in milliseconds for which video
     *                                  renderers can attempt to seamlessly join an ongoing playback.
     * @param eventHandler              A handler associated with the main thread's looper.
     * @param eventListener             An event listener.
     * @param extensionRendererMode     The extension renderer mode.
     * @param out                       An array to which the built renderers should be appended.
     */
    protected void buildVideoRenderers(Context context,
                                       DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                       long allowedVideoJoiningTimeMs,
                                       Handler eventHandler,
                                       VideoRendererEventListener eventListener,
                                       @ExtensionRendererMode int extensionRendererMode,
                                       ArrayList<Renderer> out) {

        Timber.d("Adding MediaCodecVideoRenderer");
        out.add(new MediaCodecVideoRenderer(
                context,
                MediaCodecSelector.DEFAULT,
                allowedVideoJoiningTimeMs,
                drmSessionManager,
                false,
                eventHandler,
                eventListener,
                MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY));
    }

    /**
     * Builds audio renderers for use by the player.
     *
     * @param context               The {@link Context} associated with the player.
     * @param drmSessionManager     An optional {@link DrmSessionManager}. May be null if the player
     *                              will not be used for DRM protected playbacks.
     * @param audioProcessors       An array of {@link AudioProcessor}s that will process PCM audio
     *                              buffers before output. May be empty.
     * @param eventHandler          A handler to use when invoking event listeners and outputs.
     * @param eventListener         An event listener.
     * @param extensionRendererMode The extension renderer mode.
     * @param out                   An array to which the built renderers should be appended.
     */
    protected void buildAudioRenderers(Context context,
                                       DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                       AudioProcessor[] audioProcessors,
                                       Handler eventHandler,
                                       AudioRendererEventListener eventListener,
                                       @ExtensionRendererMode int extensionRendererMode,
                                       ArrayList<Renderer> out) {

        AudioCapabilities audioCapabilities = AudioCapabilities.getCapabilities(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        final boolean enablePassthroughDecoder = sharedPreferences.getBoolean(
                "audio_passthrough_decoder_enabled",
                false);

        // Native Audio Decoders
        Timber.d("Adding MediaCodecAudioRenderer");
        MediaCodecSelector mediaCodecSelector = buildMediaCodecSelector(enablePassthroughDecoder);
        out.add(new MediaCodecAudioRenderer(mediaCodecSelector, drmSessionManager,
                true, eventHandler, eventListener, audioCapabilities));

/*
        // FFMpeg Audio Decoder
        final boolean enableFfmpegAudioRenderer = sharedPreferences.getBoolean(
                "ffmpeg_audio_enabled",
                true
        );

        if (enableFfmpegAudioRenderer) {
            Timber.d("Adding FfmpegAudioRenderer");
            out.add(new FfmpegAudioRenderer(eventHandler, eventListener, audioProcessors));
        }
        */
    }

    /**
     * Builds a MediaCodecSelector that can explicitly disable audio passthrough
     *
     * @param enablePassthroughDecoder True if audio passthrough shall be enabled, disabled otherwise
     * @return The MediaCodecSelector
     */
    private MediaCodecSelector buildMediaCodecSelector(final boolean enablePassthroughDecoder) {
        return new MediaCodecSelector() {
            @Override
            public MediaCodecInfo getDecoderInfo(String mimeType, boolean requiresSecureDecoder) throws MediaCodecUtil.DecoderQueryException {
                return MediaCodecUtil.getDecoderInfo(mimeType, requiresSecureDecoder);
            }

            @Override
            public MediaCodecInfo getPassthroughDecoderInfo() {
                if (enablePassthroughDecoder) {
                    return MediaCodecUtil.getPassthroughDecoderInfo();
                }
                return null;
            }
        };
    }
}
