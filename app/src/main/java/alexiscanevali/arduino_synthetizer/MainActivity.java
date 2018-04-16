package alexiscanevali.arduino_synthetizer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ScrollView bluetoothDevices;
    private Set<BluetoothDevice> devices;
    private TextView txtIndication;
    private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        * Bluetooth setup
        * */
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        txtIndication = (TextView) findViewById(R.id.txt_bluetooth_info);
        bluetoothDevices = (ScrollView) findViewById(R.id.bluetoothSelector);

        String BTstatus;
        if (bluetooth != null) {
            if (bluetooth.isEnabled()) {
                BTstatus = "Select a device in the list below";
                devices = bluetooth.getBondedDevices();
                if (devices.size() == 0) {
                    TextView noDevicePaired = new TextView(this);
                    noDevicePaired.setText("No device paired");
                    bluetoothDevices.addView(noDevicePaired);
                }else{
                    Button currentButton;
                    int i=0;
                    LinearLayout buttonsLayout = new LinearLayout(this);
                    buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    buttonsLayout.setOrientation(LinearLayout.VERTICAL);
                    for(BluetoothDevice bd: devices){
                        currentButton = new Button(this);
                        currentButton.setText(bd.getName()+" ("+bd.getAddress()+")");
                        currentButton.setId(i);
                        currentButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        currentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int elementNumber = view.getId();
                                launchNextActivity(elementNumber);
                            }
                        });
                        buttonsLayout.addView(currentButton);
                        i++;
                    }
                    bluetoothDevices.addView(buttonsLayout);
                }
            } else {
                BTstatus = "Bluetooth not enabled";
                requestUserToEnableBluetooth();
            }
        } else {
            BTstatus = "There is no bluetooth dongle on this device";
        }
        txtIndication.setText(BTstatus);
    }

    public void updateUI(){
        bluetoothDevices.removeAllViewsInLayout();

        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        String BTstatus;
        if (bluetooth != null) {
            if (bluetooth.isEnabled()) {
                BTstatus = "Select a device in the list below";
                devices = bluetooth.getBondedDevices();
                if (devices.size() == 0) {
                    TextView noDevicePaired = new TextView(this);
                    noDevicePaired.setText("No device paired");
                    bluetoothDevices.addView(noDevicePaired);
                }else{
                    Button currentButton;
                    int i=0;
                    LinearLayout buttonsLayout = new LinearLayout(this);
                    buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    buttonsLayout.setOrientation(LinearLayout.VERTICAL);
                    for(BluetoothDevice bd: devices){
                        currentButton = new Button(this);
                        currentButton.setText(bd.getName()+" ("+bd.getAddress()+")");
                        currentButton.setId(i);
                        currentButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        currentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int elementNumber = view.getId();
                                launchNextActivity(elementNumber);
                            }
                        });
                        buttonsLayout.addView(currentButton);
                        i++;
                    }
                    bluetoothDevices.addView(buttonsLayout);
                }
            } else {
                BTstatus = "Bluetooth not enabled";
                requestUserToEnableBluetooth();
            }
        } else {
            BTstatus = "There is no bluetooth dongle on this device";
        }
        txtIndication.setText(BTstatus);
    }

    private void launchNextActivity(int buttonID){
        bluetoothDevices.removeAllViewsInLayout();
        TextView loadingMessage = new TextView(this);
        loadingMessage.setText("Please wait, connecting to device and loading app content...");
        loadingMessage.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,ScrollView.LayoutParams.WRAP_CONTENT));
        bluetoothDevices.addView(loadingMessage);
        String address = "ERROR";

        int i=0;
        for(BluetoothDevice bd: devices){
            if(i==buttonID){
                address=bd.getAddress();
            }
            i++;
        }

        Intent intent = new Intent(this, SynthetizerCommands.class);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
        startActivity(intent);
    }

    private void requestUserToEnableBluetooth(){
        Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_ENABLE_BLUETOOTH)
            return;
        if (resultCode == RESULT_OK) {
            // User activated bluetooth
            updateUI();
        } else {
            // User hasn't activated bluetooth
            requestUserToEnableBluetooth();
        }
    }
}
