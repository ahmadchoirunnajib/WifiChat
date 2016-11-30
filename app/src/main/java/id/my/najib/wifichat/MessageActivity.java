package id.my.najib.wifichat;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/*
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;
import android.view.ContextMenu;
import android.widget.TextView;
import android.widget.AdapterView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import android.provider.Settings.Secure;
*/








/**
 * @author (C) Dushk
 */
@SuppressWarnings("ALL")
public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

    static final int SocketServerPORT = 8080;
    private static final int SHARE_PICTURE = 2;
    private static final int REQUEST_PATH = 1;
    private static final int TIMEOUT = 3000;
    private static final int MAXFILELEN = 65000;

    final int portNum = 3238;
    InetAddress ip = null;
    NetworkInterface networkInterface = null;
    ServerSocket serverSocket;
    String curFileName;
    EditText edittext;
    private ArrayList<String> recQue;
    private String[] values;
    private ListView listView;
    private ArrayAdapter adapter;
    private MulticastSocket socket;
    private InetAddress group;
    private MulticastSocket fileSocket;
    private InetAddress fileGroup;
    private String username;

    NotificationManager NM;
    Notification notify;
    PendingIntent pending;
    int offset;

    //SharedPreferences sharedpreferences;
    //SharedPreferences.Editor editor;

    //public static final String MyPREFERENCES = "usernameLokal" ;

    //String usernameLokal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(username);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //TextView userNm = (TextView) findViewById(R.id.usrName);
        //userNm.setText(username);

        /*
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        editor.putString("usernameLokal", username);
        editor.commit();

        usernameLokal = sharedpreferences.getString("usernameLokal","");
        */





        listView = (ListView) findViewById(R.id.listView);
        registerForContextMenu(listView);

        recQue = new ArrayList<>();

        try {
            if (socket == null) {


                Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (enumNetworkInterfaces.hasMoreElements()) {

                    networkInterface = enumNetworkInterfaces.nextElement();
                    Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress.nextElement();

                        if (inetAddress.isSiteLocalAddress()) {
                            ip = inetAddress;
                            break;
                        }
                    }
                    if (ip != null) {
                        break;
                    }
                }
                socket = new MulticastSocket(portNum);
                socket.setInterface(ip);
                socket.setBroadcast(true);

                group = InetAddress.getByName("224.0.0.1");
                socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);

                fileSocket = new MulticastSocket(portNum+1);
                fileSocket.setInterface(ip);
                fileSocket.setBroadcast(true);

                fileGroup = InetAddress.getByName("224.0.0.2");
                fileSocket.joinGroup(new InetSocketAddress(fileGroup, (portNum+1)), networkInterface);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        receiverMessage recvMsgThread = new receiverMessage(recQue);
        recvMsgThread.execute((Void) null);
        Button send = (Button) findViewById(R.id.buttonSend);
        send.setOnClickListener(this);
        //Button shareButton = (Button) findViewById(R.id.buttonshare);
       // shareButton.setOnClickListener(this);

        //Button selectButton = (Button) findViewById(R.id.buttonSelect);
        //selectButton.setOnClickListener(this);
        //edittext = (EditText) findViewById(R.id.editText);

        //---------------------------------------------------------server
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        try {
            socket = new MulticastSocket(portNum);
            socket.setInterface(ip);
            socket.setBroadcast(true);

            group = InetAddress.getByName("224.0.0.1");
            socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSend:
                EditText text = (EditText) findViewById(R.id.msgEditText);
                String textMsg = text.getText().toString();
                if (textMsg.equals("")) {
                    //Toast.makeText(getBaseContext(), "")
                } else {
                    text.setText("");
                    sendMessage sendMessage = new sendMessage(textMsg);
                    sendMessage.execute((Void) null);
                    break;
                }

        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {

                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // See which child activity is calling us back.
        if (requestCode == REQUEST_PATH) {
            if (resultCode == RESULT_OK) {
                curFileName = data.getStringExtra("GetPath");
                curFileName += data.getStringExtra("GetFileName");
                edittext.setText(curFileName);
            }
        }
    }


    private class receiverMessage extends AsyncTask<Void, Void, Boolean> {
        ArrayList<String> msgList;

        receiverMessage(ArrayList<String> msgList) {
            recQue = msgList;
            this.msgList = msgList;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Thread newThread = new Thread() {

                public void run() {
                    while (true) {
                        byte[] recvPkt = new byte[1024];
                        DatagramPacket recv = new DatagramPacket(recvPkt, offset, recvPkt.length);
                        try {
                            socket.receive(recv);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        final String medd = new String(recvPkt, 0, recv.getLength());
                        recQue.add(medd);
                        updateListView(medd);
                    }
                }
            };
            newThread.start();
            return null;
        }
    }

    private class sendMessage extends AsyncTask<Void, Void, Boolean> {

        String textMsg;

        sendMessage(String message) {

            textMsg = username + " : " + message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            byte[] data = textMsg.getBytes();
            offset = 0;
            DatagramPacket packet = new DatagramPacket(data, offset, data.length, group, portNum);

            try {
                socket.send(packet);
                return true;
            } catch (IOException e) {
                return false;
            }

        }
    }



    private void updateListView(final String message) {

        values = new String[recQue.size()];
        for (int x = 0; x < recQue.size(); x++) {
            values[x] = recQue.get(x);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                /*
                if(message.contains(username)) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getBaseContext())
                                    .setSmallIcon(R.drawable.button_send)
                                    .setContentTitle("Pesan Baru")
                                    .setContentText(message);
                    // Creates an explicit intent for an Activity in your app
                    Intent resultIntent = new Intent(getBaseContext(), MessageActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getBaseContext());
                    // Adds the back stack for the Intent (but not the Intent itself)
                    stackBuilder.addParentStack(MessageActivity.class);
                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    // mId allows you to update the notification later on.
                    mNotificationManager.notify(0, mBuilder.build());

                } else {
                    Toast.makeText(getBaseContext(), message + " : " + recQue.size(), Toast.LENGTH_SHORT);
                }
                */

                Vibrator v = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(500);


                adapter = new ArrayAdapter<>(MessageActivity.this, R.layout.list_white_text, R.id.list_content, values);
                listView.setAdapter(adapter);
                listView.setSelection(adapter.getCount() - 1);
            }
        });
        Log.d("WifiChat", "Send : " + message);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_copy) {
            try {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                StringBuilder sb = new StringBuilder();
                for(String str: recQue)
                    sb.append(str+"\n");
                String copy = sb.toString();
                ClipData clip = ClipData.newPlainText("Chats History", copy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getBaseContext(), "Success copy chats history", Toast.LENGTH_SHORT);
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Failed to copy chats history", Toast.LENGTH_SHORT);
            }

        }

        return super.onOptionsItemSelected(item);
    }

}
