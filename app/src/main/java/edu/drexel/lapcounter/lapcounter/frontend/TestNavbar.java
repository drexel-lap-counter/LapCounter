package edu.drexel.lapcounter.lapcounter.frontend;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.OnItemSelectedListener;

public class TestNavbar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_navbar);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new OnItemSelectedListener(this));
    }
}
