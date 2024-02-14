package fi.metropolia.bibeks.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("MissingPermission")
class BLEViewModel: ViewModel() {

    companion object GattAttributes {
        const val SCAN_PERIOD: Long = 5000
    }

    var mBluetoothGatt: BluetoothGatt? = null
    val mGattCallback = GattClientCallback()

    val scanResults = MutableLiveData<List<ScanResult>>(null)
    val fScanning = MutableLiveData<Boolean>(false)
    private val mResults = java.util.HashMap<String, ScanResult>()
    fun scanDevices(scanner: BluetoothLeScanner, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            fScanning.postValue(true)
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()

            scanner.startScan(null, settings, leScanCallback)
            delay(GattAttributes.SCAN_PERIOD)
            scanner.stopScan(leScanCallback)
            scanResults.postValue(mResults.values.toList())
            Log.d("DBG", "scan results: ${scanResults.value?.size}")

            fScanning.postValue(false)
            getHeartRateBleDevice(context)
        }
    }

    private fun getHeartRateBleDevice(context: Context){
        val bleResults = scanResults.value?.find { it -> it.device.name == "Heart Rate" }
        val device: BluetoothDevice? = bleResults?.device
        Log.d("DBG", "ble selected device ${bleResults?.device.toString()}")
        mBluetoothGatt = device?.connectGatt(context,false, mGattCallback)
        Log.d("DBG", "heart rate from call back check in model ${mGattCallback.mBPM.value.toString()}")

    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val deviceAddress = device.address
            mResults[deviceAddress] = result

        }
    }
}