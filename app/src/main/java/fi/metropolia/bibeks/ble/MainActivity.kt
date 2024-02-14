package fi.metropolia.bibeks.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fi.metropolia.bibeks.ble.ui.theme.BLETheme
@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : ComponentActivity() {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var takePermissions: ActivityResultLauncher<Array<String>>
    private lateinit var takeResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var bluetoothViewModel: BLEViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothViewModel = BLEViewModel()
        takePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
            { it ->
                it.entries.forEach{
                    Log.d("DBG list permission", "${it.key} = ${it.value}")

                    if (!it.value) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        takeResultLauncher.launch(enableBtIntent)
                    }
                }
                if (it[Manifest.permission.BLUETOOTH_ADMIN] == true
                    && it[Manifest.permission.ACCESS_FINE_LOCATION] == true){

                    bluetoothAdapter.bluetoothLeScanner.let { scan ->
                        bluetoothViewModel.scanDevices(
                            scan,
                            this
                        )
                    }
                } else {
                    Toast.makeText(applicationContext, "Not all permissions are granted", Toast.LENGTH_SHORT).show()
                }
            }
        takeResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d("DBG result callback ok", " ${result.resultCode}")
            } else {
                Log.d("DBG result callback NOT OK", " ${result.resultCode}")
            }
        }

        setContent {
            BLETheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        StartScan()
                        ShowDevices(bluetoothViewModel)
                    }

                }
            }
        }
    }

    @Composable
    fun StartScan() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(getString(R.string.app_name), fontSize = 30.sp)
            Spacer(modifier = Modifier.padding(10.dp))

            OutlinedButton(onClick = {
                takePermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ))
            }) {
                Text("Start scan")
            }

        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    fun ShowDevices(model: BLEViewModel) {
        val value: List<ScanResult>? by model.scanResults.observeAsState(null)
        val fScanning: Boolean by model.fScanning.observeAsState(false)

        val bpm: Int by model.mGattCallback.mBPM.observeAsState(0)

        Column (modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (fScanning) "Scanning" else "Not scanning")
            Text("Device connected: ${if (model.mBluetoothGatt !=null) model.mBluetoothGatt?.device?.name else "press scan button"}")

            HeartRateInfoContainer(bpm)
            Divider(thickness = 1.dp)
            Text(
                if (value?.size ==0) "no devices" else if (value == null) "" +
                        "" else "found ${value?.size}",
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.padding(10.dp))

            value?.forEach {
                //Text("${it.device.name} ${it.device.type} ${it.device.address}")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Name: ${it.device.name ?: "(No name)"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Mac address: ${it.device.address}",
                                modifier = Modifier
                                    .weight(0.5f)
                            )
                            Text(
                                text = "RSSI: ${it.rssi} dBm",
                                modifier = Modifier
                                    .weight(0.5f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HeartRateInfoContainer(bpm: Int) {
        Text("Heart rate: $bpm BPM", fontSize = 25.sp)
    }

}


