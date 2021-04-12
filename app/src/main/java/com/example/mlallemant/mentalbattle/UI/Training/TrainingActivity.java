package com.example.mlallemant.mentalbattle.UI.Training;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.Calculation;
import com.example.mlallemant.mentalbattle.Utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created by m.lallemant on 22/11/2017.
 */

public class TrainingActivity extends AppCompatActivity {


    //UI
    private RelativeLayout rl_parent;
    private Button btn_start;
    private EditText et_result;
    private TextView tv_score;
    private TextView tv_speed;
    private TextView tv_speed_down;
    private TextView tv_speed_up;

    //Utils
    private List<AnimatedCalculation> calculations;
    private long TIME_BETWEEN_EACH_CALCUL = 10000;
    private final long TIME_TO_FALL = 50000;
    private int score = 0;
    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training_activity);

        calculations = new ArrayList<>();

        initUI();
        initListener();
    }

    private void initUI() {
        rl_parent = (RelativeLayout) findViewById(R.id.training_rl_parent);
        btn_start = (Button) findViewById(R.id.training_btn_start);
        et_result = (EditText) findViewById(R.id.training_et_result);
        tv_score = (TextView) findViewById(R.id.training_tv_score);
        tv_speed = (TextView) findViewById(R.id.training_tv_speed);
        tv_speed_down = (TextView) findViewById(R.id.training_speed_down);
        tv_speed_up = (TextView) findViewById(R.id.training_speed_up);

        calculAndDisplaySpeed();
    }

    private void initListener() {

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                btn_start.setVisibility(View.INVISIBLE);
                launchTraining();
            }
        });

        initListenerButton();

        et_result.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                final String value = editable.toString();
                checkResultOk(value);
            }
        });

        tv_speed_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                TIME_BETWEEN_EACH_CALCUL -= 100;
                calculAndDisplaySpeed();
            }
        });

        tv_speed_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                TIME_BETWEEN_EACH_CALCUL += 100;
                calculAndDisplaySpeed();
            }
        });
    }


    private void checkResultOk(final String value) {

        for (final AnimatedCalculation calculation : calculations) {
            if (calculation.getCalculation().getResult().toString().equals(value)) {
                calculation.remove();
                et_result.setText("");
                calculations.remove(calculation);
                score++;
                tv_score.setText("" + score);

                break;
            }
        }
    }


    private void removeAllAnimation() {
        for (final AnimatedCalculation calculation : calculations) {
            calculation.remove();
        }

        calculations.clear();
    }

    private void launchTraining() {

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                generateNewCalcul();
                mHandler.postDelayed(this, TIME_BETWEEN_EACH_CALCUL);
                TIME_BETWEEN_EACH_CALCUL -= 15;
                calculAndDisplaySpeed();
            }
        };
        mHandler.postDelayed(mRunnable, 100);
    }

    private void calculAndDisplaySpeed() {
        final Float speed = 1 / Long.valueOf(TIME_BETWEEN_EACH_CALCUL).floatValue() * 1000;
        final String text = roundFloat(speed, 3) + "/s";
        tv_speed.setText(text);
    }


    public static float roundFloat(final float number, final int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        final float tmp = number * pow;
        return ((float) ((int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp))) / pow;
    }


    private void generateNewCalcul() {
        final RelativeLayout rl = new RelativeLayout(this);
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rl.setLayoutParams(layoutParams);
        final TextView tv = new TextView(this);
        final Calculation calculation = new Calculation();
        tv.setText(calculation.getCalculText());
        tv.setTextSize(getRandomIntBetween(20, 30));
        rl.addView(tv);
        rl_parent.addView(rl, 0);

        final int x = rl_parent.getWidth();
        final int y = rl_parent.getHeight();


        final ObjectAnimator translateXAnimation = ObjectAnimator.ofFloat(rl, "translationX", getRandomIntBetween(0, Double.valueOf(0.8 * x).intValue()), getRandomIntBetween(0, Double.valueOf(0.8 * x).intValue()));
        final ObjectAnimator translateYAnimation = ObjectAnimator.ofFloat(rl, "translationY", -100, y);

        final AnimatorSet set = new AnimatorSet();
        set.setDuration(TIME_TO_FALL);
        set.playTogether(translateXAnimation, translateYAnimation);
        set.start();

        translateYAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator valueAnimator) {
                final float yPosition = (Float) valueAnimator.getAnimatedValue();

                if (Float.valueOf(yPosition).intValue() > (y - 50)) {
                    removeAllAnimation();
                    TIME_BETWEEN_EACH_CALCUL = 10000;
                    mHandler.removeCallbacks(mRunnable);
                    score = 0;
                    btn_start.setVisibility(View.VISIBLE);
                }
            }
        });

        calculations.add(new AnimatedCalculation(rl, calculation, set));

    }


    private int getRandomIntBetween(final int x1, final int x2) {
        final Random r = new Random();
        return r.nextInt(x2 - x1) + x1;
    }

    private class AnimatedCalculation {

        private final RelativeLayout rl;
        private final Calculation calculation;
        private final AnimatorSet animatorSet;

        AnimatedCalculation(final RelativeLayout rl, final Calculation calculation, final AnimatorSet animatorSet) {
            this.rl = rl;
            this.calculation = calculation;
            this.animatorSet = animatorSet;
        }

        public Calculation getCalculation() {
            return this.calculation;
        }

        public void remove() {
            animatorSet.cancel();
            rl.clearAnimation();
            rl_parent.removeView(rl);
        }
    }

    private void makeToast(final String text) {
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_SHORT).show();
    }

    private void initListenerButton() {
        final Button btn_0 = (Button) findViewById(R.id.training_btn_0);
        final Button btn_1 = (Button) findViewById(R.id.training_btn_1);
        final Button btn_2 = (Button) findViewById(R.id.training_btn_2);
        final Button btn_3 = (Button) findViewById(R.id.training_btn_3);
        final Button btn_4 = (Button) findViewById(R.id.training_btn_4);
        final Button btn_5 = (Button) findViewById(R.id.training_btn_5);
        final Button btn_6 = (Button) findViewById(R.id.training_btn_6);
        final Button btn_7 = (Button) findViewById(R.id.training_btn_7);
        final Button btn_8 = (Button) findViewById(R.id.training_btn_8);
        final Button btn_9 = (Button) findViewById(R.id.training_btn_9);
        final Button btn_minus = (Button) findViewById(R.id.training_btn_minus);
        final ImageButton btn_backspace = (ImageButton) findViewById(R.id.training_btn_backspace);

        btn_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String text = et_result.getText().toString() + 0;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String text = et_result.getText().toString() + 1;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String text = et_result.getText().toString() + 2;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String text = et_result.getText().toString() + 3;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String text = et_result.getText().toString() + 4;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String text = et_result.getText().toString() + 5;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String text = et_result.getText().toString() + 6;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String text = et_result.getText().toString() + 7;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String text = et_result.getText().toString() + 8;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String text = et_result.getText().toString() + 9;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (!et_result.getText().toString().contains("-")) {
                    final String text = "-" + et_result.getText().toString();
                    if (checkLengthText(text)) et_result.setText(text);
                }
            }
        });

        btn_backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (et_result.getText().toString().length() > 0) {
                    final String text = removeLastChar(et_result.getText().toString());
                    if (checkLengthText(text)) et_result.setText(text);
                }
            }
        });
    }

    private Boolean checkLengthText(final String text) {
        Boolean success = false;
        if (text.length() <= Utils.MAX_LENGTH_RESULT) success = true;
        return success;
    }

    private String removeLastChar(final String str) {
        return str.substring(0, str.length() - 1);
    }
}
