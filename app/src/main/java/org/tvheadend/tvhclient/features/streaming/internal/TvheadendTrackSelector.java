/*
 * Copyright (c) 2016 Kiall Mac Innes <kiall@macinnes.ie>
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

import android.media.tv.TvTrackInfo;

import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;

import timber.log.Timber;


class TvheadendTrackSelector extends DefaultTrackSelector {

    private String mVideoTrackId;
    private String mAudioTrackId;
    private String mSubtitleTrackId;

    TvheadendTrackSelector(TrackSelection.Factory adaptiveVideoTrackSelectionFactory) {
        super(adaptiveVideoTrackSelectionFactory);
    }

    public boolean selectTrack(int type, String trackId) {
        Timber.d("TrackSelector selectTrack: " + type + " / " + trackId);

        switch (type) {
            case TvTrackInfo.TYPE_VIDEO:
                mVideoTrackId = trackId;
                break;
            case TvTrackInfo.TYPE_AUDIO:
                mAudioTrackId = trackId;
                break;
            case TvTrackInfo.TYPE_SUBTITLE:
                mSubtitleTrackId = trackId;
                break;
            default:
                throw new RuntimeException("Invalid track type: " + type);
        }

        invalidate();

        return true;
    }
/*
    @Override
    protected TrackSelection[] selectTracks(
            RendererCapabilities[] rendererCapabilities,
            TrackGroupArray[] rendererTrackGroupArrays,
            int[][][] rendererFormatSupports)
            throws ExoPlaybackException {
        // Apparently, it's very easy to end up choosing multiple audio track renderers, e.g ffmpeg
        // decode and MediaCodec passthrough. When this happens we end up with a Multiple renderer
        // media clocks enabled IllegalStateException exception (see Issue #107).
        TrackSelection[] trackSelections = super.selectTracks(rendererCapabilities, rendererTrackGroupArrays, rendererFormatSupports);

        // If we made multiple audio track selections, keep only one of them.
        // TODO: Make this smarter, if the user prefers English, and we select a passthrough German
        // track and a English mpeg-2 audio track, we really should keep the english one over the
        // German one.
        int selectedAudioRendererIndex = -1;
        for (int trackSelectionIndex = 0; trackSelectionIndex < trackSelections.length; trackSelectionIndex++) {
            final TrackSelection trackSelection = trackSelections[trackSelectionIndex];

            if (trackSelection == null || !MimeTypes.isAudio(trackSelection.getSelectedFormat().sampleMimeType)) {
                // Skip if, a) renderer has no candidate, b) not an audio renderer
                continue;
            }

            if (selectedAudioRendererIndex != -1) {
                // If we already made a selection, discard this extra selection
                trackSelections[trackSelectionIndex] = null;
                Timber.d("Discarding Audio Track Selection");
                continue;
            }

            // Otherwise, flag that we've made our selection
            selectedAudioRendererIndex = trackSelectionIndex;
        }

        return trackSelections;
    }

    @Override
    protected TrackSelection selectVideoTrack(
            RendererCapabilities rendererCapabilities,
            TrackGroupArray groups,
            int[][] formatSupport,
            DefaultTrackSelector.Parameters params,
            TrackSelection.Factory adaptiveTrackSelectionFactory)
            throws ExoPlaybackException {

        Timber.d("TrackSelector selectVideoTrack");

        // If we haven't explicitly chosen a track, defer to the DefaultTrackSelector implementation.
        if (mVideoTrackId == null) {
            return super.selectVideoTrack(rendererCapabilities, groups, formatSupport, params, adaptiveTrackSelectionFactory);
        } else {
            for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
                TrackGroup trackGroup = groups.get(groupIndex);
                int[] trackFormatSupport = formatSupport[groupIndex];

                for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                    if (isSupported(trackFormatSupport[trackIndex], false)) {
                        Format format = trackGroup.getFormat(trackIndex);

                        if (mVideoTrackId.equals(format.id)) {
                            return new FixedTrackSelection(trackGroup, trackIndex);
                        }
                    }
                }
            }

            return null;
        }
    }

    @Override
    protected TrackSelection selectAudioTrack(
            TrackGroupArray groups,
            int[][] formatSupport,
            DefaultTrackSelector.Parameters params,
            TrackSelection.Factory adaptiveTrackSelectionFactory) {
        Timber.d("TrackSelector selectAudioTrack");

        // If we haven't explicitly chosen a track, defer to the DefaultTrackSelector implementation.
        if (mAudioTrackId == null) {
            try {
                return super.selectAudioTrack(groups, formatSupport, params, adaptiveTrackSelectionFactory);
            } catch (ExoPlaybackException e) {
                return null;
            }
        } else {
            for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
                TrackGroup trackGroup = groups.get(groupIndex);
                int[] trackFormatSupport = formatSupport[groupIndex];

                for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                    if (isSupported(trackFormatSupport[trackIndex], false)) {
                        Format format = trackGroup.getFormat(trackIndex);

                        if (mAudioTrackId.equals(format.id)) {
                            Timber.d("Matched audio track, create FixedTrackSelection");
                            return new FixedTrackSelection(trackGroup, trackIndex);
                        }
                    }
                }
            }

            return null;
        }
    }

    @Override
    protected TrackSelection selectTextTrack(
            TrackGroupArray groups,
            int[][] formatSupport,
            DefaultTrackSelector.Parameters params) {
        Timber.d("TrackSelector selectTextTrack");

        // If we haven't explicitly chosen a track, defer to the DefaultTrackSelector implementation.
        if (mSubtitleTrackId == null) {
            try {
                return super.selectTextTrack(groups, formatSupport, params);
            } catch (ExoPlaybackException e) {
                return null;
            }
        } else {
            for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
                TrackGroup trackGroup = groups.get(groupIndex);
                int[] trackFormatSupport = formatSupport[groupIndex];

                for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                    if (isSupported(trackFormatSupport[trackIndex], false)) {
                        Format format = trackGroup.getFormat(trackIndex);

                        if (mSubtitleTrackId.equals(format.id)) {
                            return new FixedTrackSelection(trackGroup, trackIndex);
                        }
                    }
                }
            }

            return null;
        }
    }
    */
}
