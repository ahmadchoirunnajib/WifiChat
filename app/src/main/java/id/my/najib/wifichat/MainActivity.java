package id.my.najib.wifichat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;

import android.provider.Settings.Secure;

import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends AppCompatActivity {

    private String username;

    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
    String judul;
    String urlaplikasi;

    SwitchCompat switchWifi;
    TextView statuswifi;
    WifiManager wifi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                //getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error")
                    .setMessage("Make sure your Wifi is turn ON or there is WiFi conncetion!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }


        statuswifi = (TextView) findViewById(R.id.statuswifi);
        wifi = (WifiManager)getSystemService(getBaseContext().WIFI_SERVICE);
        switchWifi = (SwitchCompat) findViewById(R.id.switchWifi);


        if (wifi.isWifiEnabled()){
            switchWifi.setChecked(true);
            statuswifi.setText("Status WiFi : ON");
        } else {
            switchWifi.setChecked(false);
            statuswifi.setText("Status WiFi : OFF");
        }
        switchWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifi.startScan();
                    wifi.setWifiEnabled(true);
                    statuswifi.setText("Status WiFi : ON");
                } else {
                    wifi.setWifiEnabled(false);
                    statuswifi.setText("Status WiFi : OFF");
                }
            }
        });

        final EditText editTextuserName = (EditText) findViewById(R.id.userName);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("WiFi Chat");
        setSupportActionBar(toolbar);


        //Button btnStartChat = (Button) findViewById(R.id.startChat);
        FancyButton btnStartChat;
        btnStartChat = (FancyButton) findViewById(R.id.startChat);
        /*
        btnStartChat.setText("Login with Facebook");
        btnStartChat.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btnStartChat.setFocusBackgroundColor(getResources().getColor(R.color.colorAccent));
        btnStartChat.setIconResource(R.drawable.ic_send);
        btnStartChat.setIconPosition(FancyButton.POSITION_LEFT);
        btnStartChat.setTextSize(17);
        btnStartChat.setRadius(5);
        */
        btnStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = editTextuserName.getText().toString();
                if (username.equals("")) {

                } else {
                    Intent intent = new Intent(getBaseContext(), MessageActivity.class);
                    intent.putExtra("username", String.valueOf(editTextuserName.getText()));
                    //idhplokal = Secure.getString(getBaseContext().getContentResolver(),
                    //        Secure.ANDROID_ID);
                    //Toast.makeText(getBaseContext(), idhplokal, Toast.LENGTH_SHORT);
                    //Toast toast = Toast.makeText(getBaseContext(), idhplokal, Toast.LENGTH_SHORT);
                    //toast.show();
                    startActivity(intent);
                }

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                shareIntentMethod();

            }
        });



    }

    private void shareIntentMethod() {
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        urlaplikasi = "http://play.google.com/store/apps/details?id=" + getBaseContext().getPackageName();
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, judul);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Chat with your friend over WiFi? " +
                "Download WiFi Chat NOW, No internet needed, you can chat with your friend Offline" +
                " using WiFi connection! \n" + urlaplikasi +"\n#WiFiChat");
        startActivity(Intent.createChooser(shareIntent, "Share with"));

    }

    private void showAlert(String title, String text) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("How to Use")
                    .setMessage("Turn on WiFi, connect to same Access Point with your other devices")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
