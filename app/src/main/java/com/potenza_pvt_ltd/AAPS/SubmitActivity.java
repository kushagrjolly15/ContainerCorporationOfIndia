package com.potenza_pvt_ltd.AAPS;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.potenza_pvt_ltd.AAPS.function.PocketPos;
import com.potenza_pvt_ltd.AAPS.util.FontDefine;
import com.potenza_pvt_ltd.AAPS.util.Printer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class SubmitActivity extends Activity implements AdapterView.OnItemSelectedListener{
    protected EditText driverno;
    protected EditText vehicleno;
    protected TextView date;
    protected Button submitButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth mRef;
    DatabaseReference reference;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth.AuthStateListener mRefListener;
    String transpoter_name,vehicle_type;
    private String typeofuser;
    private String email_operator;
    TextView myLabel;

    // will enable user to enter any text to be printed

    // android built in classes for bluetooth operations
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    // needed for communication to bluetooth device / network
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    private String transporter;
    private String drvrno,vhclno,final_date;
    private String localTime;
    String header1,header2,header3,header4,header5,footer1,footer2;
    int printcode;
    ArrayAdapter<String> dataAdapter,dataAdapter1;
    Spinner spinner,spinner_transporter;
    private ArrayList<String> code=new ArrayList<>();
    private ArrayList<String> name=new ArrayList<>();
    private String aps;
    ProgressBar pb,pb1,pb2;
    private LinearLayout linear_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        printcode=getIntent().getIntExtra("printcode",printcode);
        aps=preferences.getString("aps",null);
        Log.d("PrintCode", String.valueOf(printcode));
        Button openButton = (Button) findViewById(R.id.open);
        pb=(ProgressBar)findViewById(R.id.progressBar);
        pb1=(ProgressBar)findViewById(R.id.progressBar1);
        pb2=(ProgressBar)findViewById(R.id.progressBar2);
        linear_layout=(LinearLayout)findViewById(R.id.linear_layout);
        myLabel = (TextView) findViewById(R.id.label);
        Button closeButton = (Button) findViewById(R.id.close);
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("User", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("User", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        Query query=reference.child("users").child("Slip_Details");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                SlipDetail post = snapshot.getValue(SlipDetail.class);
                Log.d("fppter", post.getFooter1());
                header1 = post.getHeader1();
                header2 = post.getHeader2();
                header3 = post.getHeader3();
                header4 = post.getHeader4();
                header5 = post.getHeader5();
                footer1 = post.getFooter1();
                footer2 = post.getFooter2();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
        // open bluetooth connection
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    findBT();
                    openBT();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        typeofuser=preferences.getString("typeofuser",null);
        email_operator=preferences.getString("email",null);
        Button logout=(Button)findViewById(R.id.button_logout);
        driverno= (EditText) findViewById(R.id.editText3);
        vehicleno = (EditText) findViewById(R.id.editText4);
        date= (TextView) findViewById(R.id.textView_date1);
        submitButton= (Button) findViewById(R.id.button_submit);
        spinner = (Spinner) findViewById(R.id.spinner2);
        spinner.setOnItemSelectedListener(this);
        Query queryRef = reference.child("users").child("Vehicle_Type");
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("value of", String.valueOf(dataSnapshot.getKey()));
                VehicleTypeDetails post = dataSnapshot.getValue(VehicleTypeDetails.class);
                code.add(post.getVehicle_type());
                dataAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, code);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dataAdapter.notifyDataSetChanged();
                spinner.setAdapter(dataAdapter);
                pb.setVisibility(View.GONE);
                if(pb1.getVisibility()==View.GONE){
                    linear_layout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
        //new getSpinnerData(SubmitActivity.this).execute();
        dataAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, code);
        dataAdapter.notifyDataSetChanged();
        spinner_transporter = (Spinner) findViewById(R.id.spinner3);
        spinner_transporter.setOnItemSelectedListener(this);
        final Query queryRef1 = reference.child("users").child("Transporter_Details").orderByChild("name");
        queryRef1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("value of", String.valueOf(dataSnapshot.getKey()));
                TransporterDetails post = dataSnapshot.getValue(TransporterDetails.class);
                name.add(post.getName());
                Collections.sort(name, String.CASE_INSENSITIVE_ORDER);
                dataAdapter1 = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, name);
                dataAdapter1.notifyDataSetChanged();
                dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_transporter.setAdapter(dataAdapter1);
                pb1.setVisibility(View.GONE);
                if (pb.getVisibility() == View.GONE) {
                    linear_layout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
        //new getSpinnerData1(SubmitActivity.this).execute();
        dataAdapter1 = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, name);
        dataAdapter1.notifyDataSetChanged();
        final Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);
        int dd = c.get(Calendar.DAY_OF_MONTH);
        date.setText(new StringBuilder().append(dd).append("/").append(mm+1).append("/").append(yy));
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
        localTime = sdf.format(calendar.getTime());
        if(localTime.contains("p.m.")){
            Log.d("localtime",localTime);
            localTime=localTime.replace("p.m.","PM");
        }
        else if(localTime.contains("pm")){
            Log.d("localtime",localTime);
            localTime=localTime.replace("pm","PM");
        }
        else if(localTime.contains("am")){
            Log.d("localtime",localTime);
            localTime=localTime.replace("am","AM");
        }
        else if(localTime.contains("a.m.")){
            Log.d("localtime",localTime);
            localTime=localTime.replace("a.m.","AM");
        }
        // Check Authentication
        mRef = FirebaseAuth.getInstance();
        mRefListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("User", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("User", "onAuthStateChanged:signed_out");
                    loadLoginView();
                }
                // ...
            }
        };

        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pb2.setVisibility(View.VISIBLE);
                linear_layout.setVisibility(View.GONE);
                transporter = transpoter_name;
                drvrno = driverno.getText().toString();
                vhclno = vehicleno.getText().toString();
                final_date = date.getText().toString();
                if (!drvrno.isEmpty() && !vhclno.isEmpty()) {
                    Query queryRef3 = reference.child("users").child("Transporter_Details").orderByChild("name").equalTo(transporter);
                    queryRef3.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            int flag = 0;
                            Log.d("value of", String.valueOf(dataSnapshot.getKey()));
                            TransporterDetails post = dataSnapshot.getValue(TransporterDetails.class);
                            String vehicle_nos = post.getVehicle_no();
                            String[] nos = vehicle_nos.split(",");
                            for (int i = 0; i < nos.length; i++) {
                                if (nos[i].contentEquals(vhclno)) {
                                    flag = 1;
                                    Map<String, Object> graceNickname = new HashMap<>();
                                    graceNickname.put("transporter", transporter);
                                    graceNickname.put("dno", drvrno);
                                    graceNickname.put("vno", vhclno);
                                    graceNickname.put("vtype", vehicle_type);
                                    graceNickname.put("toa", localTime);
                                    graceNickname.put("email", email_operator);
                                    graceNickname.put("date", final_date);
                                    graceNickname.put(typeofuser, "true");
                                    graceNickname.put("tod", "");
                                    graceNickname.put("aps", "");
                                    graceNickname.put("pap", 0);
                                    graceNickname.put("cost", "0");
                                    DatabaseReference newpostref = reference.child("users").child("data").push();
                                    newpostref.setValue(graceNickname);
                                    editor.putString("UserID for data", newpostref.getKey());
                                    AlertDialog.Builder builder = new AlertDialog.Builder(SubmitActivity.this);
                                    builder.setMessage("You have successfully uploaded the details!")
                                            .setTitle(R.string.submit_title)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                    driverno.setText("");
                                    vehicleno.setText("");
                                    try {
                                        sendData();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (transporter.contentEquals("N/A")) {
                                Map<String, Object> graceNickname = new HashMap<>();
                                graceNickname.put("transporter", "N/A");
                                graceNickname.put("dno", drvrno);
                                graceNickname.put("vno", vhclno);
                                graceNickname.put("vtype", vehicle_type);
                                graceNickname.put("toa", localTime);
                                graceNickname.put("email", email_operator);
                                graceNickname.put("date", final_date);
                                graceNickname.put(typeofuser, "true");
                                graceNickname.put("tod", "");
                                graceNickname.put("aps", "");
                                graceNickname.put("pap", 0);
                                graceNickname.put("cost", "0");
                                DatabaseReference newpostref = reference.child("users").child("data").push();
                                newpostref.setValue(graceNickname);
                                editor.putString("UserID for data", newpostref.getKey());
                                AlertDialog.Builder builder = new AlertDialog.Builder(SubmitActivity.this);
                                builder.setMessage("You have successfully uploaded the details!")
                                        .setTitle(R.string.submit_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog1 = builder.create();
                                dialog1.show();
                                driverno.setText("");
                                vehicleno.setText("");
                                try {
                                    sendData();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            pb2.setVisibility(View.GONE);
                            linear_layout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {

                        }
                    });
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(SubmitActivity.this);
                    builder.setMessage("Enter the details")
                            .setTitle("Message")
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog1 = builder.create();
                    dialog1.show();
                    pb2.setVisibility(View.GONE);
                    linear_layout.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    void closeBT() throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            myLabel.setText("Bluetooth Closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // this will send text data to be printed by the bluetooth printer
    void sendData() throws IOException{

        try {

            // the text typed by the user
            String titleStr	= "Receipt ";
            StringBuilder contentSb	= new StringBuilder();
            contentSb.append(header1+ "\n");
            contentSb.append(header2+ "\n");
            contentSb.append(header3+ "\n");
            contentSb.append(header4+ "\n");
            contentSb.append(header5+ "\n");
            contentSb.append("\nVehicle Number     :"+ vhclno+ "\n");
            contentSb.append("Transporter Name      : "+transporter + "\n");
            contentSb.append("Vehicle Type :"+vehicle_type + "\n");
            contentSb.append("Driver Number : " +drvrno+ "\n");
            contentSb.append("Date : "+final_date + "\n");
            contentSb.append("Time of Arrival : "+localTime + "\n");
            contentSb.append("Email :" +email_operator+ "\n");
            contentSb.append("\n\n\n\n"+footer1+"\n");
            contentSb.append(footer2+"\n");


            byte[] titleByte	= Printer.printfont(titleStr, FontDefine.FONT_24PX, FontDefine.Align_CENTER,
                    (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

            byte[] content1Byte	= Printer.printfont(contentSb.toString(), FontDefine.FONT_24PX,FontDefine.Align_LEFT,
                   (byte)0x1A, PocketPos.LANGUAGE_ENGLISH);

            byte[] totalByte	= new byte[titleByte.length +content1Byte.length];
            int offset = 0;
            System.arraycopy(titleByte, 0, totalByte, offset, titleByte.length);
            offset += titleByte.length;

            System.arraycopy(content1Byte, 0, totalByte, offset, content1Byte.length);
            offset += content1Byte.length;

            byte[] senddata = PocketPos.FramePack(PocketPos.FRAME_TOF_PRINT, totalByte, 0, totalByte.length);
            Log.d("Send", Arrays.toString(senddata));

            mmOutputStream.write(senddata);

            // tell the user data were sent
            myLabel.setText("Data sent.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null) {
                myLabel.setText("No bluetooth adapter available");
            }

            if(!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0) {
                final ArrayAdapter<String> paired=new ArrayAdapter<String>(SubmitActivity.this,android.R.layout.select_dialog_singlechoice);
                final ArrayList dev= new ArrayList();
                for (BluetoothDevice device : pairedDevices) {
                    paired.add(device.getName());
                    dev.add(device);
                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    if (device.getName().equals("silbt-010")||device.getName().equals("silbt-009")) {
                        mmDevice = device;
                        break;
                    }
                }
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(SubmitActivity.this);
                builderSingle.setTitle("Select One Name:-");

                builderSingle.setNegativeButton(
                        "cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setAdapter(
                        paired,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mmDevice = (BluetoothDevice) dev.get(which);
                            }
                        });
                builderSingle.show();
            }

            myLabel.setText("Bluetooth device found.");

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    // tries to open a connection to the bluetooth printer device
    void openBT() throws IOException {
        try {

            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            myLabel.setText("Bluetooth Opened");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
 * after opening a connection to bluetooth printer device,
 * we have to listen and check if a data were sent to be printed.
 */
    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myLabel.setText(data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadLoginView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner3 = (Spinner) parent;
        if(spinner3.getId() == R.id.spinner2)
        {
            vehicle_type=parent.getItemAtPosition(position).toString();
        }
        else if(spinner3.getId() == R.id.spinner3)
        {
            transpoter_name=parent.getItemAtPosition(position).toString();
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    @Override
    public void onBackPressed()
    {
        Intent i = new Intent(SubmitActivity.this, TypeofOperator.class);
        i.putExtra("aps",aps);
        startActivity(i);
    }

}
