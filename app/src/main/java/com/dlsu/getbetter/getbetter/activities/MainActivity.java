package com.dlsu.getbetter.getbetter.activities;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.sql.SQLException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private RecyclerView pageList;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DrawerLayout drawerLayout;

    ActionBarDrawerToggle actionBarDrawerToggle;
    SystemSessionManager systemSessionManager;

    private DataAdapter getBetterDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();

        String userNameLabel = user.get(SystemSessionManager.LOGIN_USER_NAME);
        String currentHealthCenter = hc.get(SystemSessionManager.HEALTH_CENTER_NAME);


        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        initializeDatabase();
        String userName = getUserName(userNameLabel);




    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getUserName(String userEmail) {


        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        String username = getBetterDb.getUserName(userEmail);

        getBetterDb.closeDatabase();

        return username;
    }


}
