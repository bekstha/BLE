package fi.metropolia.bibeks.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.*

@SuppressLint("MissingPermission")

class GattClientCallback (): BluetoothGattCallback() {
    val mBPM = MutableLiveData<Int>(0)

    val HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D)
    val HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37)
    val CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902)

    private fun convertFromInteger(i: Int): UUID {
        val MSB = 0x0000000000001000L
        val LSB = -0x7fffff7fa064cb05L
        val value = (i and -0x1).toLong()
        return UUID(MSB or (value shl 32), LSB)
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        if (status == BluetoothGatt.GATT_FAILURE){
            Log.d("BLE DBG", "GATT connection failure")
            return
        } else if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d("BLE DBG", "GATT connection success")
        }

        if (newState == BluetoothProfile.STATE_CONNECTED){
            Log.d("BLE DBG", "connected GATT service")
            gatt?.discoverServices()
        }
        else if (newState == BluetoothProfile.STATE_DISCONNECTED){
            Log.d("BLE DBG", "disconnected GATT service")
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (status != BluetoothGatt.GATT_SUCCESS){
            return
        }
        Log.d("BLE DBG", "onServiceDiscovered")

        gatt?.services?.forEach { gattService ->
            Log.d("BLE DBG", " gattServices ${gattService.uuid}")
            if (gattService.uuid == HEART_RATE_SERVICE_UUID){
                Log.d("BLE DBG", "Found heart rate service")
            }
            for (gattCharacteristic in gattService.characteristics){
                Log.d("BLE DBG", "Characteristic ${gattCharacteristic.uuid}")
            }
            val characteristic = gatt.getService(HEART_RATE_SERVICE_UUID)?.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
            if (gatt.setCharacteristicNotification(characteristic, true)){
                val descriptor = characteristic?.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                val writing = gatt.writeDescriptor(descriptor)
            }
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        Log.d("BLE DBG", "onDescriptorWrite()")
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("NullSafeMutableLiveData")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        Log.d("BLE DBG", "characteristic data received")
        val bpm = characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1)
        mBPM.postValue(bpm)
        Log.d( "BLE DBG" , "BPM livedata: ${mBPM.value} " )

    }
}