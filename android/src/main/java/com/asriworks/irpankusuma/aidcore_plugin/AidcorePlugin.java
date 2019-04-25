package com.asriworks.irpankusuma.aidcore_plugin;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

// https://medium.com/@thejohnoke/bluetooth-printing-with-android-e79c64044fc9
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.webkit.WebView;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.Locale;
import java.util.Objects;


/** AidcorePlugin */
public class AidcorePlugin implements MethodCallHandler {
  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "aidcore_plugin");
    channel.setMethodCallHandler(new AidcorePlugin());
  }

  /**
   * Bluetooth Print
   */
  BluetoothAdapter bluetoothAdapter;
  BluetoothSocket socket;
  BluetoothDevice bluetoothDevice;
  OutputStream outputStream;
  InputStream inputStream;
  Thread workerThread;
  byte[] readBuffer;
  int readBufferPosition;
  volatile boolean stopWorker;
  String value = "";
  
  private double fontSize = 17;
  private double printFontSize = 26;
  final String SAVED_SIZE = "saved_size";

  WebView webView;
  
  @Override
  public void onMethodCall(MethodCall call, Result result) {
    
    switch (call.method){
      default:
      {
        Log.d(logTag,"0. not implemented");
        result.notImplemented();
        break;
      }
      case "AidcorePrint":
      {
        // Input
        Log.d(logTag,"1. method : AidcorePrint java");
        String data = call.argument("data");
        String printerName = call.argument("printerName");
        IntentPrint(data,printerName);
        result.success(value);
      }
    }
  }


  /**
   * 
   * @param txtvalue
   * @param printerName
   * Print to Blutooth printer
   * https://medium.com/@thejohnoke/bluetooth-printing-with-android-e79c64044fc9
   */
  public void IntentPrint(String txtvalue, String printerName){
    byte[] buffer = txtvalue.getBytes();
    byte[] PrintHeader = { (byte) 0xAA,0x55,2,0 };
    PrintHeader[3]=(byte) buffer.length;
    InitPrinter(printerName);
    
    if(PrintHeader.length>128){
      value+="\nValue is more than 128 size\n";
    } else {
      try {
        outputStream.write(txtvalue.getBytes());
        outputStream.close();
        socket.close();
      } catch(Exception ex){
        value+=ex.toString()+ "\n" +"Excep IntentPrint \n";
      }
    }
  }

  public void InitPrinter(String name){
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    try {
      if(!bluetoothAdapter.isEnabled())
      {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        // startActivityForResult(enableBluetooth, 0);
      }

      Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
      if(pairedDevices.size() > 0)
      {
        for(BluetoothDevice device : pairedDevices){
          if(device.getName().equals(name))
          {
            bluetoothDevice = device;
            break;
          }
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
        socket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
        bluetoothAdapter.cancelDiscovery();
        socket.connect();
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        beginListenForData();
      } else {
        value+="No Devices Found";
      }
    } catch(Exception ex){
      value+=ex.toString()+ "\n" +" InitPrinter \n";
    }
  }

  void beginListenForData(){
    Log.d(logTag,"7 Begin");
    try {
      final Handler handler = new Handler();
      // this is the ASCII code for a newline character
      final byte delimiter = 10;

      stopWorker = false;
      readBufferPosition = 0;
      readBuffer = new byte[1024];

      workerThread = new Thread(new Runnable(){
        public void run() {
          while (!Thread.currentThread().isInterrupted() && !stopWorker){
            try {
              int bytesAvailable = inputStream.available();
              if (bytesAvailable > 0) {
                byte[] packetBytes = new byte[bytesAvailable];
                inputStream.read(packetBytes);
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
                        Log.d(logTag,data);
                      }
                    });
                  } else {
                    readBuffer[readBufferPosition++] = b;
                  }
                }
              }
            } catch(Exception ex){
              stopWorker = true;
            }
          }  
        }
      });

      workerThread.start();
    } catch(Exception ex){
      ex.printStackTrace();
    }
  }
}
