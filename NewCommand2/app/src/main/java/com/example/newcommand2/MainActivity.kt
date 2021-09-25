package com.example.newcommand2

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.view.animation.TranslateAnimation
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.newcommand2.utils.log


data class Coord(
    val x : Float,
    val y : Float
)
private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class MainActivity : AppCompatActivity() {

    /*******************************************
     * Properties
     *******************************************/

    var INPUT_NAME_BLE_TAG = "itelma"

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var isScanning = false
        set(value) {
            field = value
            //runOnUiThread { scan_button.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private val scanResults = mutableListOf<ScanResult>()
//    private val scanResultAdapter: ScanResultAdapter by lazy {
//        ScanResultAdapter(scanResults) { result ->
//            if (isScanning) {
//                stopBleScan()
//            }
//            with(result.device) {
//                Timber.w("Connecting to $address")
//                ConnectionManager.connect(this, this@MainActivity)
//            }
//        }
//    }

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    lateinit var imgV : ImageView
    var x1 : Float = 0.0F
    var y1 : Float = 0.0F

    //var x2 : Float = 100.0F
    //var y2 : Float = 100.0F
    var demoList = arrayListOf<Coord>(
        Coord(580F,600F),
        Coord(583F,504F),
        Coord(583F,504F),
        Coord(573F,404F),
        Coord(588F,304F),
        Coord(583F,204F),
        Coord(483F,204F),
        Coord(383F,204F),
        Coord(183F,204F),
        Coord(583F,304F),
        Coord(583F,404F),
        Coord(583F,604F),
    )


    lateinit var power_of_signal : TextView
    lateinit var input_ble_tag : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       // imgV = findViewById(R.id.beacon)
        power_of_signal = findViewById(R.id.power_of_signal)
        input_ble_tag = findViewById(R.id.input_ble_tag)
        //startBleScan()
        var fp = FilledProgress(this@MainActivity)
        fp = findViewById(R.id.progress)

        setTitle("Хакатон Цифра. Определитель rssi")
        var a = 0
        var timer = object : CountDownTimer(190000,1000){
            override fun onTick(millisUntilFinished: Long) {

                try {


                    if (demoList.size >= a){

                        //startMotion(demoList[a].x,demoList[a].y)

                        a++
                    }


                }catch (e : Exception){
                    Toast.makeText(applicationContext,"End",Toast.LENGTH_LONG).show()
                }


            }
            override fun onFinish() {
                stopBleScan()
            }
        }.start()

        var timer2 = object : CountDownTimer(920000,300){
            override fun onTick(millisUntilFinished: Long) {
                for (i in 0 until scanResults.size){

                    if (scanResults[i].device.name != null && scanResults[i].device.name.toString().contains(INPUT_NAME_BLE_TAG,true) == true){
                        updPowerSignal(scanResults[i].rssi, fp)
                    }

                }
            }
            override fun onFinish() {
               // stopBleScan()
            }
        }.start()

        input_ble_tag.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkerFineWithInput()
                INPUT_NAME_BLE_TAG = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    // 75 - 4m ; 55 - 1m  ;  35 - 0m

    private fun updPowerSignal(rssi: Int, fp: FilledProgress?) {

        power_of_signal.text = "-${rssi*-1F}dBm \n${((rssi*(-1f))-35f) / 9f }m"
//        if (rssi*-1f<40f){
//            fp?.setNewLevel(1.3f - ((rssi.toFloat() * -1F)/ 92f))
//            return
//        }
        fp?.setNewLevel(1.3f - ((rssi.toFloat() * -1F)/ 92f))

//        when(rssi){
//            in 0..30 -> {fp?.setNewLevel(98f)}
//            in 30..40 -> {fp?.setNewLevel(90f)}
//            in 0..30 -> {fp?.setNewLevel(90f)}
//            in 0..30 -> {fp?.setNewLevel(90f)}
//            in 0..30 -> {fp?.setNewLevel(90f)}
//            in 0..30 -> {fp?.setNewLevel(90f)}
//        }
        //fp?.setNewLevel(150F-rssi.toFloat()*(-1F)*100f / 90f)

    }

    override fun onResume() {
        super.onResume()
        //ConnectionManager.registerListener(connectionEventListener)
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }

    }



    ///BLE

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }

    /*******************************************
     * Private functions
     *******************************************/

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            scanResults.clear()
            //scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        requestPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

//    private fun setupRecyclerView() {
//        scan_results_recycler_view.apply {
//            adapter = scanResultAdapter
//            layoutManager = LinearLayoutManager(
//                this@MainActivity,
//                RecyclerView.VERTICAL,
//                false
//            )
//            isNestedScrollingEnabled = false
//        }
//
//        val animator = scan_results_recycler_view.itemAnimator
//        if (animator is SimpleItemAnimator) {
//            animator.supportsChangeAnimations = false
//        }
//    }

    /*******************************************
     * Callback bodies
     *******************************************/

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                //scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    log("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)

                //scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            log("onScanFailed: code $errorCode")
        }
    }

//    private val connectionEventListener by lazy {
//        ConnectionEventListener().apply {
//            onConnectionSetupComplete = { gatt ->
//                Intent(this@MainActivity, BleOperationsActivity::class.java).also {
//                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, gatt.device)
//                    startActivity(it)
//                }
//                ConnectionManager.unregisterListener(this)
//            }
//            onDisconnect = {
//                runOnUiThread {
//                    alert {
//                        title = "Disconnected"
//                        message = "Disconnected or unable to connect to device."
//                        positiveButton("OK") {}
//                    }.show()
//                }
//            }
//        }
//    }

    /*******************************************
     * Extension functions
     *******************************************/

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    fun startG(view: View) {
        INPUT_NAME_BLE_TAG = input_ble_tag.text.toString()

        checkerFineWithInput()


        startBleScan()
    }

    fun checkerFineWithInput(){
        if (INPUT_NAME_BLE_TAG == null){
            INPUT_NAME_BLE_TAG = "itelma"

        }else if (INPUT_NAME_BLE_TAG == ""){
            INPUT_NAME_BLE_TAG = "itelma"
        }
        Toast.makeText(applicationContext,"ble tag: ${INPUT_NAME_BLE_TAG}",Toast.LENGTH_LONG).show()
    }



}