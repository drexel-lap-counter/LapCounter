package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class PoolSizeActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this, R.id.navigation_settings);
    private static final String TAG = DeviceSelectActivity.class.getSimpleName();
    //Default Values
    public static final int defPoolSize = 25;
    public static final String defPoolUnits = "Yards";

    //Keys used for loading/save pool size and units
    public static final String poolSizePreferences = "pool_size_pref";
    public static final String poolSizeKey = "pool_size_key";
    public static final String poolUnitsKey = "pool_size_units";

    private EditText custom_pool_text;
    private int poolSize;
    private String poolUnits;
    private SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pool_size);

        getSupportActionBar().setTitle(R.string.pool_size_title);
        mNavBar.init();

        custom_pool_text = findViewById(R.id.pool_size_custom_text);
        custom_pool_text.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                tryGetCustomPoolSize(s.toString());
            }
        });

        pref = getSharedPreferences(poolSizePreferences, Context.MODE_PRIVATE);
        loadPoolData();
    }

    //Load the saved pool data and setup our two radio groups
    private void loadPoolData()
    {
        poolSize = pref.getInt(poolSizeKey,defPoolSize);
        poolUnits = pref.getString(poolUnitsKey,defPoolUnits);
        Log.i(TAG,String.format("poolSize:%d, poolUnits:%s",poolSize,poolUnits));

        //Pool Size RadioGroup Setup
        if(poolSize == 25)
        {
            ((RadioButton) findViewById(R.id.pool_size_25)).setChecked(true);
        }
        else if(poolSize == 50)
        {
            ((RadioButton) findViewById(R.id.pool_size_50)).setChecked(true);
        }
        else//Custom
        {
            ((RadioButton) findViewById(R.id.pool_size_custom)).setChecked(true);
            custom_pool_text.setText(String.valueOf(poolSize));
            custom_pool_text.setEnabled(true);
        }

        //Pool Size Units RadioGroup Setup
        if(poolUnits.equals("Yards"))
        {
            ((RadioButton) findViewById(R.id.pool_units_yards)).setChecked(true);
        }
        else
        {
            ((RadioButton) findViewById(R.id.pool_units_meters)).setChecked(true);
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected  void onPause()
    {
        super.onPause();
    }

    protected  void onResume()
    {
        super.onResume();
        loadPoolData();
    }

    public void onPoolSizeRadioButtonClicked(View view)
    {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId())
        {
            case R.id.pool_size_25:
                if(checked)
                {
                    //Coming from Custom
                    if(custom_pool_text.isEnabled())
                    {
                        custom_pool_text.setEnabled(false);
                    }
                    poolSize = 25;
                    break;
                }
            case R.id.pool_size_custom:
                if(checked)
                {
                    custom_pool_text.setEnabled(true);
                    tryGetCustomPoolSize(custom_pool_text.getText().toString());
                    break;
                }
            default:        //pool size 50
                if(checked)
                {
                    //Coming from Custom
                    if(custom_pool_text.isEnabled())
                    {
                        custom_pool_text.setEnabled(false);
                    }
                    poolSize = 50;
                    break;
                }
        }
    }

    public void onPoolUnitsRadioButtonClicked(View view)
    {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId())
        {
            case R.id.pool_units_yards:
                if(checked)
                {
                    poolUnits = "Yards";
                    break;
                }
            default:    // units meters
                if(checked)
                {
                    poolUnits = "Meters";
                    break;
                }
        }
    }

    public void onConfirmButtonClicked(View view)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(poolSizeKey,poolSize);
        editor.putString(poolUnitsKey,poolUnits);
        editor.apply();
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void tryGetCustomPoolSize(String s)
    {
        int val = 0;
        try
        {
            val = Integer.parseInt(s);
        }
        catch(Exception e)
        {
            Log.i("EXCEPTION",e.toString());
            return;
        }
        poolSize = val;
    }

    // Methods for TESTING
    public int getPoolSize() {
        return poolSize;
    }
    public void setCustom_pool_text(EditText custom_pool_text){
        this.custom_pool_text = custom_pool_text;
    }
    public String getPoolUnits(){
        return poolUnits;
    }
}
