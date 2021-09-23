package com.example.voicerecorder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.agrawalsuneet.squareloaderspack.loaders.WaveLoader;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener {

    private NavController navController;

    private ImageButton listButton;
    private ImageButton recordButton;
    private TextView filenameText;

    private boolean isRecording = false;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 10;

    private MediaRecorder mediaRecorder;
    private String recordFile;

    private Chronometer timer;
    private WaveLoader waveLoader;


    public RecordFragment() {
        // Required empty public constructor
    }

    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = container.getContext();
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        listButton = view.findViewById(R.id.btn_record_list);
        recordButton = view.findViewById(R.id.btn_record);
        timer = view.findViewById(R.id.timer_record);
        filenameText = view.findViewById(R.id.record_filename);
        waveLoader = view.findViewById(R.id.audioRecordView);

        listButton.setOnClickListener(this);
        recordButton.setOnClickListener(this);

        stopAnim();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_record_list:
                if (isRecording) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                            isRecording = false;
                        }
                    });
                    alertDialog.setNegativeButton("No", null);
                    alertDialog.setTitle("Audio is still recording");
                    alertDialog.setMessage("Are you sure, you want to stop the recording?");
                    alertDialog.create().show();
                } else {
                    navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                }
                break;

            case R.id.btn_record:
                if (isRecording) {
                    //Stop recording
                    stopRecording();

                    recordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_button, null));
                    isRecording = false;
                } else {
                    //Start Recording
                    if (checkPermissions()) {
                        startRecording();

                        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_button, null));
                        isRecording = true;
                    }
                }
                break;

        }
    }

    private void startRecording() {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        SimpleDateFormat formatter = new SimpleDateFormat("yy_MM_DD_hh_mm_ss", Locale.getDefault());
        Date now = new Date();

        recordFile = "Recording_" + formatter.format(now) + ".3gp";
        filenameText.setText("File name: " + recordFile);

        initRecorder();
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();

        startAnim();
    }

    private void initRecorder() {
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioSamplingRate(48000);
        mediaRecorder.setAudioEncodingBitRate(48000);
    }

    private void stopRecording() {
        timer.stop();
        Toast.makeText(getActivity(), "Recording stopped and saved", Toast.LENGTH_SHORT).show();

        mediaRecorder.stop();
        mediaRecorder.release();

        mediaRecorder = null;

        stopAnim();
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    private void stopAnim() {
        Animation animation1 =
                AnimationUtils.loadAnimation(context, R.anim.width_out);
        waveLoader.startAnimation(animation1);
    }

    private void startAnim() {
        Animation animation2 =
                AnimationUtils.loadAnimation(context, R.anim.width_in);
        waveLoader.startAnimation(animation2);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRecording) {
            stopRecording();
        }
    }
}
