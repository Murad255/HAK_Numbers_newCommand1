package com.example.newcommand1

class Communication {

    object : Handler() {
        override fun handleMessage(msg: Message) {

            when (msg.what) {

                Constants.MESSAGE_STATE_CHANGE -> {

                    when (msg.arg1) {

                        BluetoothChatService.STATE_CONNECTED -> {

                            status.text = getString(R.string.connected_to) + " "+ mConnectedDeviceName
                            connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_connected))
                            Snackbar.make(findViewById(R.id.mainScreen),"Connected to " + mConnectedDeviceName,Snackbar.LENGTH_SHORT).show()
                            //mConversationArrayAdapter.clear()
                            connected = true
                        }

                        BluetoothChatService.STATE_CONNECTING -> {
                            status.text = getString(R.string.connecting)
                            connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_connecting))
                            connected = false
                        }

                        BluetoothChatService.STATE_LISTEN, BluetoothChatService.STATE_NONE -> {
                            status.text = getString(R.string.not_connected)
                            connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_red))
                            Snackbar.make(findViewById(R.id.mainScreen),getString(R.string.not_connected),Snackbar.LENGTH_SHORT).show()
                            connected = false
                        }
                    }
                }

                Constants.MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
                    val writeMessage = String(writeBuf)
                    //Toast.makeText(this@MainActivity,"Me: $writeMessage",Toast.LENGTH_SHORT).show()
                    //mConversationArrayAdapter.add("Me:  " + writeMessage)
                    val milliSecondsTime = System.currentTimeMillis()
                    chatFragment.communicate(com.webianks.bluechat.Message(writeMessage,milliSecondsTime,Constants.MESSAGE_TYPE_SENT))

                }
                Constants.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                    val milliSecondsTime = System.currentTimeMillis()
                    //Toast.makeText(this@MainActivity,"$mConnectedDeviceName : $readMessage",Toast.LENGTH_SHORT).show()
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage)
                    chatFragment.communicate(com.webianks.bluechat.Message(readMessage,milliSecondsTime,Constants.MESSAGE_TYPE_RECEIVED))
                }
                Constants.MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
                    mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
                    status.text = getString(R.string.connected_to) + " " +mConnectedDeviceName
                    connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_connected))
                    Snackbar.make(findViewById(R.id.mainScreen),"Connected to " + mConnectedDeviceName,Snackbar.LENGTH_SHORT).show()
                    connected = true
                    showChatFragment()
                }
                Constants.MESSAGE_TOAST -> {
                    status.text = getString(R.string.not_connected)
                    connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_red))
                    Snackbar.make(findViewById(R.id.mainScreen),msg.data.getString(Constants.TOAST),Snackbar.LENGTH_SHORT).show()
                    connected = false
                }
            }
        }
    }


    private fun sendMessage(message: String) {

        // Check that we're actually connected before trying anything
        if (mChatService?.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return
        }

        // Check that there's actually something to send
        if (message.isNotEmpty()) {
            // Get the message bytes and tell the BluetoothChatService to write
            val send = message.toByteArray()
            mChatService?.write(send)

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0)
            //mOutEditText.setText(mOutStringBuffer)
        }
    }

    private fun showChatFragment() {

        if(!isFinishing) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            chatFragment = ChatFragment.newInstance()
            chatFragment.setCommunicationListener(this)
            fragmentTransaction.replace(R.id.mainScreen, chatFragment, "ChatFragment")
            fragmentTransaction.addToBackStack("ChatFragment")
            fragmentTransaction.commit()
        }
    }

    override fun onCommunication(message: String) {
        sendMessage(message)
    }
}