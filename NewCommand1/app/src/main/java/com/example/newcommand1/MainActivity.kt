package com.example.newcommand1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
import android.view.View
import android.widget.ImageView
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.newcommand1.utils.log


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
private fun initNavigation() {
        navController = Navigation.findNavController(this, R.id.main_screen_container_fragment)
        val navGraph = navController!!.navInflater.inflate(R.navigation.nav_main_screen)
        //navController!!.setGraph(R.navigation.)

        if (!PreferenceMaestro.isPremiumStatus){
            when((0..10).random()){
                0,1 -> {
                    navGraph.startDestination = R.id.purchaseFragment;
                }
                else ->{
                    navGraph.startDestination = R.id.mainFragmentOfApp;
                }
            }


        }

        navController!!.graph = navGraph;

        bottomNavView.setupWithNavController(navController!!)
    }

    private fun firstLaunch() {
        // will set low animation preferences if is old device. I think old device is API with version under 10 API
        // API 28 = Android 9
        if (Build.VERSION.SDK_INT <= 28){
            PreferenceMaestro.isLightMode = true
        }

        if (PreferenceMaestro.isFirstStart) {

            val intent = Intent(this, AddSolarStationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        //.initBottomNav()

    }

    private fun manageBottomNavBar() {
        val firstFragment = MainScreenFragment()
        val secFragment = ToolsManagerFragment()
        bottomNavView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.MainScreenForecast -> {
                    navController?.navigate(R.id.mainFragmentOfApp)
                    //findNavController(R.id.main_screen_container_fragment).navigate(MainScreenFragmentDirections.actionMainFragmentToToolsManagerFragment())

                }
                R.id.ToolsScreen -> {
                    navController?.navigate(R.id.tlMng)
                    //findNavController(R.id.main_screen_container_fragment).navigate(ToolsManagerFragmentDirections.actionToolsManagerFragmentToMainFragment())

                }

                R.id.PurchaseScreen -> {
                    navController?.navigate(R.id.purchaseFragment)
                    //findNavController(R.id.main_screen_container_fragment).navigate(ToolsManagerFragmentDirections.actionToolsManagerFragmentToMainFragment())

                }
            }
            true
        }
    }

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    lateinit var imgV : ImageView
    var x1 : Float = 0.0F
    var y1 : Float = 0.0F

    //var x2 : Float = 100.0F
    //var y2 : Float = 100.0F
    var demoList = arrayListOf<Coord>(
        Coord(570F,604F),
        Coord(590F,609F),
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgV = findViewById(R.id.beacon)
        power_of_signal = findViewById(R.id.power_of_signal)
        startBleScan()
        var timer2 = object : CountDownTimer(1920000,300){
            override fun onTick(millisUntilFinished: Long) {
                //startMotion((0..300).random()*1.0F,(0..500).random()*1.0F)
                for (i in 0 until scanResults.size){
                    if (scanResults[i].device.name != null && scanResults[i].device.name.toString().contains(BLEDEV,true) == true){
                        updPowerSignal(scanResults[i].rssi)
                    }

                }



            }
            override fun onFinish() {
                // stopBleScan()
            }
        }.start()

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = BluetoothChatService(this, mHandler)
    }
    var BLEDEV = "track"
    // 5 dBm = 1m
    var RSSI1 = 0 // 75 - 4m ; 55 - 1m  ;  35 - 0m

    fun startAllTimers(){
        var a = 0
        var timer = object : CountDownTimer(120000,1000){
            override fun onTick(millisUntilFinished: Long) {
                //startMotion((0..300).random()*1.0F,(0..500).random()*1.0F)
                for (i in 0 until scanResults.size){

                    if (scanResults[i].device.name != null && scanResults[i].device.name.toString().contains(BLEDEV,true) == true){
                        updPowerSignal(scanResults[i].rssi)
                        RSSI1 = scanResults[i].rssi
                    }

                }
                try {


                    if (demoList.size >= a){

                        startMotion(demoList[a].x,demoList[a].y)

                        a++
                    }


                }catch (e : Exception){
                    cancel()
                    //Toast.makeText(applicationContext,"End",Toast.LENGTH_LONG).show()
                }


            }
            override fun onFinish() {
                stopBleScan()
            }
        }.start()


    }

    private fun updPowerSignal(rssi: Int) {
        power_of_signal.text = "${rssi}dBm"

    }

    override fun onResume() {
        super.onResume()
        //ConnectionManager.registerListener(connectionEventListener)
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    fun startMotion(x2: Float, y2 : Float){

        val animation1 = TranslateAnimation(x1, x2+(0..12).random(), y1, y2+(0..12).random())
        animation1.duration = 4000
        animation1.fillAfter = true
        imgV.startAnimation(animation1)
        x1 = x2
        y1 = y2

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

        startAllTimers()
    }

     /**********
     *  Get Data From Another Locator
     *
     */

     /**
      *
     * The Handler that gets information back from the BluetoothChatService
      *
     */
    private val mHandler = @SuppressLint("HandlerLeak")
    private var mChatService: BluetoothChatService? = null

    private fun connectDevice(deviceData: BluetoothDevice) {


        // Attempt to connect to the device
        mChatService?.connect(device, true)

    }


}