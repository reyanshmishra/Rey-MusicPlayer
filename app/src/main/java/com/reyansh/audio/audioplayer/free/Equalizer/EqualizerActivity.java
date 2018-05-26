package com.reyansh.audio.audioplayer.free.Equalizer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Paint;
import android.media.audiofx.PresetReverb;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.Database.DataBaseHelper;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Utils.Logger;
import com.reyansh.audio.audioplayer.free.Utils.MusicUtils;
import com.reyansh.audio.audioplayer.free.Utils.PreferencesHelper;
import com.reyansh.audio.audioplayer.free.Utils.TypefaceHelper;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.triggertrap.seekarc.SeekArc;

import java.util.ArrayList;

/**
 * Created by Reyansh on 23/04/2016.
 * <p>
 * Equalizer doesn't work some of the HTC devices I think.
 */


public class EqualizerActivity extends AppCompatActivity {

    /**
     * Application class.
     */
    private Common mApp;
    private Context mContext;


    private SwitchCompat mToggleEqualizerButton;

    /**
     * SeekBars and associated textviews
     */

    private VerticalSeekBar e50HzSeekBar;
    private TextView e50HzTextView;

    private VerticalSeekBar e130HzSeekBar;
    private TextView e130HzTextView;

    private VerticalSeekBar e320HzSeekBar;
    private TextView e320HzTextView;

    private VerticalSeekBar e800HzSeekBar;
    private TextView e800HzTextView;

    private VerticalSeekBar e2kHzSeekBar;
    private TextView e2kHzTextView;

    private VerticalSeekBar e5kHzSeekBar;
    private TextView e5kHzTextView;

    private VerticalSeekBar e12_5kHzSeekBar;
    private TextView e12_5HzTextView;

    /**
     * Save Preset.
     */
    private Button mSavePreset;
    private Button mLoadPresetButton;


    /**
     * SeekArcs for Virtualizer and BassBoost.
     */
    private SeekArc mVirtualizerSeekArc;
    private SeekArc mBassBoostSeekArc;
    private Spinner mReverbSpinner;


    /**
     * Volumes Buttons and SeekBar.
     */
    private ImageButton mVolumeMute;
    private ImageButton mVolumeHigh;
    private SeekBar mVolumeSeekBar;


    /**
     * Controllers
     */
    private ImageButton mImageButtonVirtualizer;
    private ImageButton mImageButtonBassBoost;

    // Temp variables that hold audio fx settings.
    private int fiftyHertzLevel = 16;
    private int oneThirtyHertzLevel = 16;
    private int threeTwentyHertzLevel = 16;
    private int eightHundredHertzLevel = 16;
    private int twoKilohertzLevel = 16;
    private int fiveKilohertzLevel = 16;
    private int twelvePointFiveKilohertzLevel = 16;
    private int volumeLevel = 100;

    // Temp variables that hold audio fx settings.
    private int virtualizerLevel;
    private int bassBoostLevel;
    private int reverbSetting;

    /**
     * Toolbar.
     */
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mApp = (Common) mContext.getApplicationContext();
        setContentView(R.layout.layout_equalizer);


        /**
         *Adding toolbar.
         */
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.equalizer);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        MusicUtils.applyFontForToolbarTitle(this);
        /**
         *Initializations.
         */

        e50HzSeekBar = (com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar) findViewById(R.id.slider_1);
        e50HzTextView = (TextView) findViewById(R.id.e50hztxt);
        e50HzSeekBar.setOnSeekBarChangeListener(equalizer50HzListener);
        e50HzTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_CONDENSED));

        e130HzSeekBar = (VerticalSeekBar) findViewById(R.id.slider_2);
        e130HzSeekBar.setOnSeekBarChangeListener(equalizer130HzListener);
        e130HzTextView = (TextView) findViewById(R.id.e130hztxt);
        e130HzTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_CONDENSED));

        e320HzSeekBar = (VerticalSeekBar) findViewById(R.id.slider_3);
        e320HzSeekBar.setOnSeekBarChangeListener(equalizer320HzListener);
        e320HzTextView = (TextView) findViewById(R.id.e320hztxt);
        e320HzTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_CONDENSED));

        e800HzSeekBar = (VerticalSeekBar) findViewById(R.id.slider_4);
        e800HzSeekBar.setOnSeekBarChangeListener(equalizer800HzListener);
        e800HzTextView = (TextView) findViewById(R.id.e800hztxt);
        e800HzTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_CONDENSED));

        e2kHzSeekBar = (VerticalSeekBar) findViewById(R.id.slider_5);
        e2kHzSeekBar.setOnSeekBarChangeListener(equalizer2kHzListener);
        e2kHzTextView = (TextView) findViewById(R.id.e2khztxt);
        e2kHzTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_CONDENSED));

        e5kHzSeekBar = (VerticalSeekBar) findViewById(R.id.slider_6);
        e5kHzSeekBar.setOnSeekBarChangeListener(equalizer5kHzListener);
        e5kHzTextView = (TextView) findViewById(R.id.e5khztxt);
        e5kHzTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_CONDENSED));

        e12_5kHzSeekBar = (VerticalSeekBar) findViewById(R.id.slider_7);
        e12_5kHzSeekBar.setOnSeekBarChangeListener(equalizer12_5kHzListener);
        e12_5HzTextView = (TextView) findViewById(R.id.e12_5khztxt);
        e12_5HzTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_CONDENSED));

        mVirtualizerSeekArc = (SeekArc) findViewById(R.id.seek_arc_virtualizer);
        mVirtualizerSeekArc.setOnSeekArcChangeListener(virtualizerListener);

        TextView virtualizerText = (TextView) findViewById(R.id.virtualizer);
        virtualizerText.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_BOLD));

        TextView bassboostText = (TextView) findViewById(R.id.bass_boost);
        bassboostText.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_BOLD));

        mBassBoostSeekArc = (SeekArc) findViewById(R.id.seek_arc_bass_boost);
        mBassBoostSeekArc.setOnSeekArcChangeListener(bassBoostListener);

        mImageButtonVirtualizer = (ImageButton) findViewById(R.id.image_button_virtualizer);
        mImageButtonBassBoost = (ImageButton) findViewById(R.id.image_button_bass_boost);

        mVolumeMute = (ImageButton) findViewById(R.id.volume_mute);
        mVolumeHigh = (ImageButton) findViewById(R.id.volume_high);

        mReverbSpinner = (Spinner) findViewById(R.id.reverb_spinner);
        mVolumeSeekBar = (SeekBar) findViewById(R.id.volume_seekbar);

        mVolumeSeekBar.setMax(100);
        mVolumeSeekBar.setOnSeekBarChangeListener(onVolumeSeekChange);


        mVolumeMute.setOnClickListener(v -> {
            //Fade out volume here.
        });

        mVolumeHigh.setOnClickListener(v -> {
            //Fade in volume here.
        });

        ArrayList<String> reverbPresets = new ArrayList<>();
        reverbPresets.add("None");
        reverbPresets.add("Large Hall");
        reverbPresets.add("Large Room");
        reverbPresets.add("Medium Hall");
        reverbPresets.add("Medium Room");
        reverbPresets.add("Small Room");
        reverbPresets.add("Plate");


        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_text, reverbPresets);
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        mReverbSpinner.setAdapter(dataAdapter);
        mReverbSpinner.setOnItemSelectedListener(reverbListener);

        mLoadPresetButton = (Button) findViewById(R.id.load_preset_button);
        mLoadPresetButton.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_BOLD));

        mLoadPresetButton.setOnClickListener(v -> buildLoadPresetDialog().show());

        mSavePreset = (Button) findViewById(R.id.savePreset);
        mSavePreset.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_BOLD));

        mSavePreset.setOnClickListener(v -> {
            AlertDialog dialog = buildSavePresetDialog();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.show();

        });

        new AsyncInitSlidersTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.switch_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.myswitch);
        View view = MenuItemCompat.getActionView(menuItem);
        mToggleEqualizerButton = (SwitchCompat) view.findViewById(R.id.switchButton);
        mToggleEqualizerButton.setChecked(PreferencesHelper.getInstance().getBoolean(PreferencesHelper.Key.IS_EQUALIZER_ACTIVE, false));
        mToggleEqualizerButton.setOnCheckedChangeListener(equalizerActive);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    CompoundButton.OnCheckedChangeListener equalizerActive = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mToggleEqualizerButton.isChecked()) {
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.IS_EQUALIZER_ACTIVE, true);
                if (mApp.isServiceRunning()) {
                    mApp.getService().getEqualizerHelper().getBassBoost().setEnabled(true);
                    mApp.getService().getEqualizerHelper().getVirtualizer().setEnabled(true);
                    mApp.getService().getEqualizerHelper().getEqualizer().setEnabled(true);
                }
            } else {
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.IS_EQUALIZER_ACTIVE, false);
                if (mApp.isServiceRunning()) {
                    mApp.getService().getEqualizerHelper().getBassBoost().setEnabled(false);
                    mApp.getService().getEqualizerHelper().getVirtualizer().setEnabled(false);
                    mApp.getService().getEqualizerHelper().getEqualizer().setEnabled(false);
                }
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener equalizer50HzListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
            try {
                short sixtyHertzBand = mApp.getService().getEqualizerHelper().getEqualizer().getBand(50000);
                if (seekBarLevel == 16) {
                    e50HzTextView.setText("0 dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {
                    if (seekBarLevel == 0) {
                        e50HzTextView.setText("-" + "15 dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) (-1500));

                    } else {
                        e50HzTextView.setText("-" + (16 - seekBarLevel) + " dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) -((16 - seekBarLevel) * 100));
                    }
                } else if (seekBarLevel > 16) {
                    e50HzTextView.setText("+" + (seekBarLevel - 16) + " dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) ((seekBarLevel - 16) * 100));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((seekBarLevel == 31 || seekBarLevel == 0) && changedByUser) {
                arg0.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }

            Logger.log("LEVEL " + seekBarLevel);

            fiftyHertzLevel = seekBarLevel;

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {

        }

    };
    /**
     * 130 Hz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer130HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate equalizer band.
                short twoThirtyHertzBand = mApp.getService().getEqualizerHelper().getEqualizer().getBand(130000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel == 16) {
                    e130HzTextView.setText("0 dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {
                    if (seekBarLevel == 0) {
                        e130HzTextView.setText("-" + "15 dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) (-1500));
                    } else {
                        e130HzTextView.setText("-" + (16 - seekBarLevel) + " dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) -((16 - seekBarLevel) * 100));
                    }

                } else if (seekBarLevel > 16) {
                    e130HzTextView.setText("+" + (seekBarLevel - 16) + " dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) ((seekBarLevel - 16) * 100));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((seekBarLevel == 31 || seekBarLevel == 0) && changedByUser) {
                arg0.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            oneThirtyHertzLevel = seekBarLevel;
        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

    };
    /**
     * 320 Hz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer320HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
            try {
                short nineTenHertzBand = mApp.getService().getEqualizerHelper().getEqualizer().getBand(320000);
                if (seekBarLevel == 16) {
                    e320HzTextView.setText("0 dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel == 0) {
                        e320HzTextView.setText("-" + "15 dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) (-1500));
                    } else {
                        e320HzTextView.setText("-" + (16 - seekBarLevel) + " dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) -((16 - seekBarLevel) * 100));
                    }

                } else if (seekBarLevel > 16) {
                    e320HzTextView.setText("+" + (seekBarLevel - 16) + " dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) ((seekBarLevel - 16) * 100));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((seekBarLevel == 31 || seekBarLevel == 0) && changedByUser) {
                arg0.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            threeTwentyHertzLevel = seekBarLevel;
        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {

        }

    };
    /**
     * 800 Hz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer800HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
            try {
                //Get the appropriate equalizer band.
                short threeKiloHertzBand = mApp.getService().getEqualizerHelper().getEqualizer().getBand(800000);
                //Set the gain level text based on the slider position.
                if (seekBarLevel == 16) {
                    e800HzTextView.setText("0 dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {
                    if (seekBarLevel == 0) {
                        e800HzTextView.setText("-" + "15 dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) (-1500));
                    } else {
                        e800HzTextView.setText("-" + (16 - seekBarLevel) + " dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) -((16 - seekBarLevel) * 100));
                    }

                } else if (seekBarLevel > 16) {
                    e800HzTextView.setText("+" + (seekBarLevel - 16) + " dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) ((seekBarLevel - 16) * 100));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((seekBarLevel == 31 || seekBarLevel == 0) && changedByUser) {
                arg0.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            eightHundredHertzLevel = seekBarLevel;
        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {

        }

    };
    /**
     * 2 kHz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer2kHzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
            try {
                short fourteenKiloHertzBand = mApp.getService().getEqualizerHelper().getEqualizer().getBand(2000000);
                if (seekBarLevel == 16) {
                    e2kHzTextView.setText("0 dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel == 0) {
                        e2kHzTextView.setText("-" + "15 dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) (-1500));
                    } else {
                        e2kHzTextView.setText("-" + (16 - seekBarLevel) + " dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) -((16 - seekBarLevel) * 100));
                    }

                } else if (seekBarLevel > 16) {
                    e2kHzTextView.setText("+" + (seekBarLevel - 16) + " dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) ((seekBarLevel - 16) * 100));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((seekBarLevel == 31 || seekBarLevel == 0) && changedByUser) {
                arg0.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            twoKilohertzLevel = seekBarLevel;
        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
        }

    };
    /**
     * 5 kHz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer5kHzListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
            try {
                short fiveKiloHertzBand = mApp.getService().getEqualizerHelper().getEqualizer().getBand(5000000);
                if (seekBarLevel == 16) {
                    e5kHzTextView.setText("0 dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {
                    if (seekBarLevel == 0) {
                        e5kHzTextView.setText("-" + "15 dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) (-1500));
                    } else {
                        e5kHzTextView.setText("-" + (16 - seekBarLevel) + " dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) -((16 - seekBarLevel) * 100));
                    }

                } else if (seekBarLevel > 16) {
                    e5kHzTextView.setText("+" + (seekBarLevel - 16) + " dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) ((seekBarLevel - 16) * 100));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((seekBarLevel == 31 || seekBarLevel == 0) && changedByUser) {
                arg0.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            fiveKilohertzLevel = seekBarLevel;
        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {

        }

    };
    /**
     * 12.5 kHz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer12_5kHzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
            try {
                short twelvePointFiveKiloHertzBand = mApp.getService().getEqualizerHelper().getEqualizer().getBand(9000000);
                if (seekBarLevel == 16) {
                    e12_5HzTextView.setText("0 dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {
                    if (seekBarLevel == 0) {
                        e12_5HzTextView.setText("-" + "15 dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) (-1500));
                    } else {
                        e12_5HzTextView.setText("-" + (16 - seekBarLevel) + " dB");
                        mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) -((16 - seekBarLevel) * 100));
                    }

                } else if (seekBarLevel > 16) {
                    e12_5HzTextView.setText("+" + (seekBarLevel - 16) + " dB");
                    mApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) ((seekBarLevel - 16) * 100));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((seekBarLevel == 31 || seekBarLevel == 0) && changedByUser) {
                arg0.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            twelvePointFiveKilohertzLevel = seekBarLevel;
        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {

        }

    };
    private SeekArc.OnSeekArcChangeListener virtualizerListener = new SeekArc.OnSeekArcChangeListener() {

        @Override
        public void onProgressChanged(SeekArc arg0, int arg1, boolean arg2) {
            virtualizerLevel = (short) arg1;
            if (mApp.isServiceRunning()) {
                mApp.getService().getEqualizerHelper().getVirtualizer().setStrength((short) virtualizerLevel);
            }
            float angle = ((float) arg1 / 1000) * 280;
            mImageButtonVirtualizer.setRotation(angle);

            if ((arg1 == 5 || arg1 == 990) && arg2) {
                arg0.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekArc seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekArc seekBar) {
        }

    };
    private SeekArc.OnSeekArcChangeListener bassBoostListener = new SeekArc.OnSeekArcChangeListener() {
        @Override
        public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
            bassBoostLevel = (short) i;
            if (mApp.isServiceRunning()) {
                mApp.getService().getEqualizerHelper().getBassBoost().setStrength((short) bassBoostLevel);
            }
            float angle = ((float) i / 1000) * 280;
            mImageButtonBassBoost.setRotation(angle);

            if ((i == 5 || i == 990) && b) {
                seekArc.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekArc seekArc) {

        }

        @Override
        public void onStopTrackingTouch(SeekArc seekArc) {

        }


    };
    private AdapterView.OnItemSelectedListener reverbListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
            PreferencesHelper.getInstance().put(PreferencesHelper.Key.LAST_PRESET_NAME, mReverbSpinner.getSelectedItem().toString());
            reverbSetting = index;

            if (mApp.isServiceRunning())
                if (index == 0) {
                    mApp.getService().getEqualizerHelper().getReverb().setPreset(PresetReverb.PRESET_NONE);
                    reverbSetting = 0;
                } else if (index == 1) {
                    mApp.getService().getEqualizerHelper().getReverb().setPreset(PresetReverb.PRESET_LARGEHALL);
                    reverbSetting = 1;
                } else if (index == 2) {
                    mApp.getService().getEqualizerHelper().getReverb().setPreset(PresetReverb.PRESET_LARGEROOM);
                    reverbSetting = 2;
                } else if (index == 3) {
                    mApp.getService().getEqualizerHelper().getReverb().setPreset(PresetReverb.PRESET_MEDIUMHALL);
                    reverbSetting = 3;
                } else if (index == 4) {
                    mApp.getService().getEqualizerHelper().getReverb().setPreset(PresetReverb.PRESET_MEDIUMROOM);
                    reverbSetting = 4;
                } else if (index == 5) {
                    mApp.getService().getEqualizerHelper().getReverb().setPreset(PresetReverb.PRESET_SMALLROOM);
                    reverbSetting = 5;
                } else if (index == 6) {
                    mApp.getService().getEqualizerHelper().getReverb().setPreset(PresetReverb.PRESET_PLATE);
                    reverbSetting = 6;
                } else
                    reverbSetting = 0;
        }

        public void onNothingSelected(AdapterView<?> arg0) {

        }
    };


    /**
     * This is used to control the volume of the currently playing media player
     */

    private SeekBar.OnSeekBarChangeListener onVolumeSeekChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float volume = ((float) progress) / 100f;

            if (mApp.isServiceRunning()) {
                mApp.getService().getMediaPlayer().setVolume(volume, volume);
                volumeLevel = progress;
            } else {
                volumeLevel = progress;
            }
            if ((progress == 100 || progress == 0) && fromUser) {
                seekBar.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        setEQValuesForSong();
    }

    /**
     * Saves the EQ settings to the database for the specified song.
     */
    public void setEQValuesForSong() {
        //Grab the EQ values for the specified song.
        int[] currentEqValues = mApp.getDBAccessHelper().getEQValues();

        //Check if a database entry already exists for this song.
        if (currentEqValues[11] == 0) {
            //Add a new DB entry.
            mApp.getDBAccessHelper().addEQValues(
                    fiftyHertzLevel,
                    oneThirtyHertzLevel,
                    threeTwentyHertzLevel,
                    eightHundredHertzLevel,
                    twoKilohertzLevel,
                    fiveKilohertzLevel,
                    twelvePointFiveKilohertzLevel,
                    virtualizerLevel,
                    bassBoostLevel,
                    reverbSetting,
                    volumeLevel);
        } else {
            //Update the existing entry.
            mApp.getDBAccessHelper().updateSongEQValues(
                    fiftyHertzLevel,
                    oneThirtyHertzLevel,
                    threeTwentyHertzLevel,
                    eightHundredHertzLevel,
                    twoKilohertzLevel,
                    fiveKilohertzLevel,
                    twelvePointFiveKilohertzLevel,
                    virtualizerLevel,
                    bassBoostLevel,
                    reverbSetting,
                    volumeLevel);
        }
    }

    /**
     * Retrieves the saved equalizer settings for the current song
     * and applies them to the UI elements.
     */
    public class AsyncInitSlidersTask extends AsyncTask<Boolean, Boolean, Boolean> {

        int[] eqValues;

        @Override
        protected Boolean doInBackground(Boolean... params) {
            eqValues = mApp.getDBAccessHelper().getEQValues();
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            fiftyHertzLevel = eqValues[0];
            oneThirtyHertzLevel = eqValues[1];
            threeTwentyHertzLevel = eqValues[2];
            eightHundredHertzLevel = eqValues[3];
            twoKilohertzLevel = eqValues[4];
            fiveKilohertzLevel = eqValues[5];
            twelvePointFiveKilohertzLevel = eqValues[6];
            virtualizerLevel = eqValues[7];
            bassBoostLevel = eqValues[8];
            reverbSetting = eqValues[9];
            volumeLevel = eqValues[10];

            //Move the sliders to the equalizer settings.


            e50HzSeekBar.setProgress(fiftyHertzLevel);
            e130HzSeekBar.setProgress(oneThirtyHertzLevel);
            e320HzSeekBar.setProgress(threeTwentyHertzLevel);
            e800HzSeekBar.setProgress(eightHundredHertzLevel);
            e2kHzSeekBar.setProgress(twoKilohertzLevel);
            e5kHzSeekBar.setProgress(fiveKilohertzLevel);
            e12_5kHzSeekBar.setProgress(twelvePointFiveKilohertzLevel);
            mVirtualizerSeekArc.setProgress(virtualizerLevel);
            mBassBoostSeekArc.setProgress(bassBoostLevel);
            mVolumeSeekBar.setProgress(volumeLevel);
            mReverbSpinner.setSelection(reverbSetting, false);


            Logger.log("REVERB " + reverbSetting);

            //50Hz Band.
            if (fiftyHertzLevel == 16) {
                e50HzTextView.setText("0 dB");
            } else if (fiftyHertzLevel < 16) {

                if (fiftyHertzLevel == 0) {
                    e50HzTextView.setText("-" + "15 dB");
                } else {
                    e50HzTextView.setText("-" + (16 - fiftyHertzLevel) + " dB");
                }

            } else if (fiftyHertzLevel > 16) {
                e50HzTextView.setText("+" + (fiftyHertzLevel - 16) + " dB");
            }

            //130Hz Band.
            if (oneThirtyHertzLevel == 16) {
                e130HzTextView.setText("0 dB");
            } else if (oneThirtyHertzLevel < 16) {

                if (oneThirtyHertzLevel == 0) {
                    e130HzTextView.setText("-" + "15 dB");
                } else {
                    e130HzTextView.setText("-" + (16 - oneThirtyHertzLevel) + " dB");
                }

            } else if (oneThirtyHertzLevel > 16) {
                e130HzTextView.setText("+" + (oneThirtyHertzLevel - 16) + " dB");
            }

            //320Hz Band.
            if (threeTwentyHertzLevel == 16) {
                e320HzTextView.setText("0 dB");
            } else if (threeTwentyHertzLevel < 16) {

                if (threeTwentyHertzLevel == 0) {
                    e320HzTextView.setText("-" + "15 dB");
                } else {
                    e320HzTextView.setText("-" + (16 - threeTwentyHertzLevel) + " dB");
                }

            } else if (threeTwentyHertzLevel > 16) {
                e320HzTextView.setText("+" + (threeTwentyHertzLevel - 16) + " dB");
            }

            //800Hz Band.
            if (eightHundredHertzLevel == 16) {
                e800HzTextView.setText("0 dB");
            } else if (eightHundredHertzLevel < 16) {

                if (eightHundredHertzLevel == 0) {
                    e800HzTextView.setText("-" + "15 dB");
                } else {
                    e800HzTextView.setText("-" + (16 - eightHundredHertzLevel) + " dB");
                }

            } else if (eightHundredHertzLevel > 16) {
                e800HzTextView.setText("+" + (eightHundredHertzLevel - 16) + " dB");
            }

            //2kHz Band.
            if (twoKilohertzLevel == 16) {
                e2kHzTextView.setText("0 dB");
            } else if (twoKilohertzLevel < 16) {

                if (twoKilohertzLevel == 0) {
                    e2kHzTextView.setText("-" + "15 dB");
                } else {
                    e2kHzTextView.setText("-" + (16 - twoKilohertzLevel) + " dB");
                }

            } else if (twoKilohertzLevel > 16) {
                e2kHzTextView.setText("+" + (twoKilohertzLevel - 16) + " dB");
            }

            //5kHz Band.
            if (fiveKilohertzLevel == 16) {
                e5kHzTextView.setText("0 dB");
            } else if (fiveKilohertzLevel < 16) {

                if (fiveKilohertzLevel == 0) {
                    e5kHzTextView.setText("-" + "15 dB");
                } else {
                    e5kHzTextView.setText("-" + (16 - fiveKilohertzLevel) + " dB");
                }

            } else if (fiveKilohertzLevel > 16) {
                e5kHzTextView.setText("+" + (fiveKilohertzLevel - 16) + " dB");
            }

            //12.5kHz Band.
            if (twelvePointFiveKilohertzLevel == 16) {
                e12_5HzTextView.setText("0 dB");
            } else if (twelvePointFiveKilohertzLevel < 16) {

                if (twelvePointFiveKilohertzLevel == 0) {
                    e12_5HzTextView.setText("-" + "15 dB");
                } else {
                    e12_5HzTextView.setText("-" + (16 - twelvePointFiveKilohertzLevel) + " dB");
                }

            } else if (twelvePointFiveKilohertzLevel > 16) {
                e12_5HzTextView.setText("+" + (twelvePointFiveKilohertzLevel - 16) + " dB");
            }
        }
    }

    /**
     * Builds the "Save Preset" dialog. Does not call the show() method, so you
     * should do this manually when calling this method.
     *
     * @return A fully built AlertDialog reference.
     */
    private AlertDialog buildSavePresetDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.add_new_equalizer_preset_dialog_layout, null);

        final EditText newPresetNameField = (EditText) dialogView.findViewById(R.id.new_preset_name_text_field);
        newPresetNameField.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_BOOK));
        newPresetNameField.setPaintFlags(newPresetNameField.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);


        //Set the dialog title.
        builder.setTitle(R.string.save_preset);
        builder.setView(dialogView);
        builder.setNegativeButton(R.string.cancel, (dialog, arg1) -> dialog.dismiss());

        builder.setPositiveButton(R.string.done, (dialog, which) -> {

            //Get the preset name from the text field.
            String presetName = newPresetNameField.getText().toString();

            //Add the preset and it's values to the DB.
            mApp.getDBAccessHelper().addNewEQPreset(presetName,
                    fiftyHertzLevel,
                    oneThirtyHertzLevel,
                    threeTwentyHertzLevel,
                    eightHundredHertzLevel,
                    twoKilohertzLevel,
                    fiveKilohertzLevel,
                    twelvePointFiveKilohertzLevel,
                    (short) mVirtualizerSeekArc.getProgress(),
                    (short) mBassBoostSeekArc.getProgress(),
                    (short) mReverbSpinner.getSelectedItemPosition());

            Toast.makeText(mContext, R.string.preset_saved, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        return builder.create();

    }


    /**
     * Builds the "Load Preset" dialog. Does not call the show() method, so this
     * should be done manually after calling this method.
     *
     * @return A fully built AlertDialog reference.
     */
    private AlertDialog buildLoadPresetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Get a cursor with the list of EQ presets.
        final Cursor cursor = mApp.getDBAccessHelper().getAllEQPresets();

        //Set the dialog title.
        builder.setTitle(R.string.load_preset);
        builder.setCursor(cursor, new DialogInterface.OnClickListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cursor.moveToPosition(which);

                //Close the dialog.
                dialog.dismiss();

                //Pass on the equalizer values to the appropriate fragment.
                fiftyHertzLevel = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.EQ_50_HZ));
                oneThirtyHertzLevel = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.EQ_130_HZ));
                threeTwentyHertzLevel = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.EQ_320_HZ));
                eightHundredHertzLevel = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.EQ_800_HZ));
                twoKilohertzLevel = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.EQ_2000_HZ));
                fiveKilohertzLevel = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.EQ_5000_HZ));
                twelvePointFiveKilohertzLevel = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.EQ_12500_HZ));
                virtualizerLevel = cursor.getShort(cursor.getColumnIndex(DataBaseHelper.VIRTUALIZER));
                bassBoostLevel = cursor.getShort(cursor.getColumnIndex(DataBaseHelper.BASS_BOOST));
                reverbSetting = cursor.getShort(cursor.getColumnIndex(DataBaseHelper.REVERB));

                //Save the new equalizer settings to the DB.
                @SuppressWarnings({"rawtypes"})
                AsyncTask task = new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object... arg0) {
                        setEQValuesForSong();
                        return null;
                    }

                    @Override
                    public void onPostExecute(Object result) {
                        super.onPostExecute(result);

                        //Reinitialize the UI elements to apply the new equalizer settings.
                        new AsyncInitSlidersTask().execute();
                    }

                };
                task.execute();
                if (cursor != null)
                    cursor.close();
            }
        }, DataBaseHelper.PRESET_NAME);
        return builder.create();
    }

}
