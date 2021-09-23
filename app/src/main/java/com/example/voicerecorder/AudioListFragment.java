package com.example.voicerecorder;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick{

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private RecyclerView audioList;
    private File[] allFiles;

    private AudioListAdapter audioListAdapter;

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    private File fileToPlay;

    //UI elements
    private ImageButton playBtn;
    private ImageButton rewindBtn;
    private ImageButton forwardBtn;
    private TextView playerStatus;
    private TextView playerFilename;

    private SeekBar playerSeekBar;
    private Handler seekBarHandler;
    private Runnable updateSeekBar;

    public AudioListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerSheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        audioList = view.findViewById(R.id.lv_audio);

        playBtn = view.findViewById(R.id.player_btn_play);
        rewindBtn = view.findViewById(R.id.player_btn_rewind);
        forwardBtn = view.findViewById(R.id.player_btn_forward);
        playerStatus = view.findViewById(R.id.player_tv_status);
        playerFilename = view.findViewById(R.id.player_tv_audio_title);
        playerSeekBar = view.findViewById(R.id.player_seekBar);

        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();

        audioListAdapter = new AudioListAdapter(allFiles, this);

        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(audioListAdapter);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pauseAudio();
                } else {
                    if (fileToPlay != null) {
                        resumeAudio();
                    }
                }
            }
        });

        rewindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileToPlay != null) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 500);
                    updateRunnable();
                    seekBarHandler.postDelayed(updateSeekBar, 0);
                }
            }
        });

        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileToPlay != null) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 500);
                    updateRunnable();
                    seekBarHandler.postDelayed(updateSeekBar, 0);
                }
            }
        });

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (fileToPlay != null) {
                    pauseAudio();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fileToPlay != null) {
                    int progress = seekBar.getProgress();
                    mediaPlayer.seekTo(progress);
                    if (isPlaying) {
                        resumeAudio();
                    }
                }
            }
        });

    }

    @Override
    public void onClickListener(File file, int position) {
        fileToPlay = file;
        if (isPlaying) {
            stopAudio();
            playAudio(fileToPlay);
        } else {
            playAudio(fileToPlay);
        }
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_play, null));
        isPlaying = false;
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    private void resumeAudio() {
        mediaPlayer.start();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_pause, null));
        playerStatus.setText("Playing");
        isPlaying = true;
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar,0);
    }

    private void stopAudio() {
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_play, null));
        playerStatus.setText("Stopped");
        isPlaying = false;
        mediaPlayer.stop();
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    private void playAudio(File fileToPlay) {
        mediaPlayer = new MediaPlayer();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_pause, null));
        playerFilename.setText(fileToPlay.getName());
        playerStatus.setText("Playing");

        isPlaying = true;


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                pauseAudio();
                playerStatus.setText("Finished");
            }
        });


        playerSeekBar.setMax(mediaPlayer.getDuration());
        seekBarHandler = new Handler();
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar, 0);
    }

    private void updateRunnable() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                playerSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                seekBarHandler.postDelayed(this, 0);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isPlaying) {
            stopAudio();
        }
    }

}
