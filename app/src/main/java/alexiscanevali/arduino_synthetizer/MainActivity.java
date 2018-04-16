package alexiscanevali.arduino_synthetizer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
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
                    Button currentButton = new Button(this);
                    int i=0;
                    for(BluetoothDevice bd: devices){
                        currentButton.setText(bd.getName()+" ("+bd.getAddress()+")");
                        currentButton.setId(i);
                        currentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int elementNumber = view.getId();
                                String address = "ERROR";

                                int i=0;
                                for(BluetoothDevice bd: devices){
                                    if(i==elementNumber){
                                        address=bd.getAddress();
                                    }
                                    i++;
                                }
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                                startActivity(intent);
                            }
                        });
                        bluetoothDevices.addView(currentButton);
                        i++;
                    }
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
                    Button currentButton = new Button(this);
                    int i=0;
                    for(BluetoothDevice bd: devices){
                        currentButton.setText(bd.getName()+" ("+bd.getAddress()+")");
                        currentButton.setId(i);
                        currentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int elementNumber = view.getId();
                                String address = "ERROR";

                                int i=0;
                                for(BluetoothDevice bd: devices){
                                    if(i==elementNumber){
                                        address=bd.getAddress();
                                    }
                                    i++;
                                }

                                Intent intent = new Intent(MainActivity.this, SynthetizerCommands.class);
                                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                                startActivity(intent);
                            }
                        });
                        bluetoothDevices.addView(currentButton);
                        i++;
                    }
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
