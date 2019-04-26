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
import android.graphics.Bitmap;

// https://github.com/Rockvole/flutter_html2pdf_viewer
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.print.PrintDocumentAdapter;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;


/** AidcorePlugin */
public class AidcorePlugin implements MethodCallHandler {
  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "aidcore_plugin");
    AidcorePlugin instance = new AidcorePlugin(registrar);
    channel.setMethodCallHandler(instance);
  }

  private final Registrar mRegistrar;
  private AidcorePlugin(Registrar registrar){
    this.mRegistrar = registrar;
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
  String _result;

  public static final int ALIGN_CENTER = 100;
  public static final int ALIGN_RIGHT = 101;
  public static final int ALIGN_LEFT = 102;

  private static final byte[] NEW_LINE = {10};
  private static final byte[] ESC_ALIGN_CENTER = new byte[]{0x1b, 'a', 0x01};
  private static final byte[] ESC_ALIGN_RIGHT = new byte[]{0x1b, 'a', 0x02};
  private static final byte[] ESC_ALIGN_LEFT = new byte[]{0x1b, 'a', 0x00};
  
  
  @Override
  public void onMethodCall(MethodCall call, Result result) {
    
    switch (call.method){
      default:
      {
        result.notImplemented();
        break;
      }
      case "PdfView":
      {
        if (!(call.arguments instanceof String)) {
          result.error("ARGUMENT_ERROR", "String argument expected", null);
          return;
        }
        final String text = (String) call.arguments;
        showReport(text);
        result.success(null);
  
        break;
      }
      case "Init":
      {
        String printerName = call.argument("printerName");
        InitialPrinter(printerName);
        result.success("INITIAL PRINTER");
      }
      case "SetAlign":
      {
        String align = call.argument("align");
        if(align == "center"){
          setAlign(AidcorePlugin.ALIGN_CENTER);
          result.success("CENTER");
        } else if(align == "right"){
          setAlign(AidcorePlugin.ALIGN_RIGHT);
          result.success("RIGHT");
        } else if(align == "left"){
          setAlign(AidcorePlugin.ALIGN_LEFT);
          result.success("LEFT");
        } else {
          setAlign(AidcorePlugin.ALIGN_LEFT);
          result.success("LEFT");
        }
      }
      case "PrintText":
      {
        String text = call.argument("text");
        result.success("PRINT TEXT : "+text);
        printText(text);
      }
      case "AddNewLines":
      {
        int count = call.argument("count");
        result.success("PRINT NEW LINE");
        addNewLines(count);
      }
      case "SetLineSpacing":{
        int line = call.argument("line");
        result.success("PRINT SET LINE SPACING");
        setLineSpacing(line);
      }
      case "SetBold":
      {
        Boolean bold = call.argument("bold");
        result.success("PRINT SET BOLD");
        setBold(bold);
      }
      case "PrintImage":
      {
        Log.d("PRINT IMAGE","PROCESSING");
        Bitmap bitmap = call.argument("bitmap");
        InitialPrinter("Virtual Bluetooth Printer");
        printText("CONTON PRINT IMAGE");
        addNewLines(1);
        printImage(bitmap);
        finish();
        Log.d("PRINT IMAGE","FINISH");
      }
      case "PrintBitmap":
      {
        Log.d("PRINT BITMAP","PRINT BITMAP ------->>>>>>>>>");
        String printerName = call.argument("printerName");
        Log.d("PRINT BITMAP",printerName);
        Bitmap img = call.argument("bitmapImage");
        PrintBitmap(printerName, img);
      }
      case "IntentPrint":
      {
        String printerName = call.argument("printerName");
        String text = call.argument("text");
        IntentPrint(text, printerName);
      }
      case "Finish":
      {
        finish();
      }
    }
  }

  /**
   * @param reportString
   */
  private void showReport(String reportString) {
    WebView webView = new WebView(mRegistrar.context());
    webView.loadDataWithBaseURL(null, reportString, "text/HTML", "UTF-8", null);
    PrintManager printManager = (PrintManager) mRegistrar.activity().getSystemService(Context.PRINT_SERVICE);

    PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

    String jobName = "Pdf View Print Test";

    PrintAttributes attrib = new PrintAttributes.Builder().
            setMediaSize(PrintAttributes.MediaSize.NA_LETTER.asLandscape()).
            setMinMargins(PrintAttributes.Margins.NO_MARGINS).
            build();
    printManager.print(jobName, printAdapter, attrib);
  }



  /**
   * 
   * @param txtvalue
   * @param printerName
   * Print to Blutooth printer
   * https://medium.com/@thejohnoke/bluetooth-printing-with-android-e79c64044fc9
   */
  public void IntentPrint(String txtvalue, String printerName){
    InitialPrinter(printerName);

    byte[] buffer = txtvalue.getBytes();
    byte[] PrintHeader = { (byte) 0xAA,0x55,2,0 };
    PrintHeader[3]=(byte) buffer.length;
    
    if(PrintHeader.length>128){
      Log.d("log","Value more than 128 characters.");
      _result = "Value more than 128 characters";
    } else {
      try {
        outputStream.write(txtvalue.getBytes());
        outputStream.close();
        socket.close();
      } catch(Exception ex){
        Log.d("log",ex.toString());
      }
    }
  }

  /**
   * Print Bitmap
   */
  public void PrintBitmap(String printerName, Bitmap img){
    InitialPrinter(printerName);
    setAlign(AidcorePlugin.ALIGN_CENTER);
    printImage(img);
    finish();
  }

  /**
   * Example Print
   */
  public void ExamplePrint(String printerName){
    Log.d("EXAMPLE PRINT",printerName);
    InitialPrinter(printerName);
    setAlign(AidcorePlugin.ALIGN_CENTER);
    addNewLines(2);
    setBold(true);
    printText("---------------------------------------");
    addNewLines(1);
    printText("---------- KEMERDEKAAN PRINT ----------");
    addNewLines(1);
    printText("----------- 17-Agustus-1945 -----------");
    addNewLines(1);
    printText("---------------------------------------");
    addNewLines(1);
    setBold(false);
    printText("Quantity       : 0");
    addNewLines(1);
    printText("Harga Satuan   : 1,00");
    addNewLines(1);
    printText("Total          : 1,00");
    addNewLines(1);
    setBold(true);
    printText("---------------------------------------");
    addNewLines(1);
    printText("---------- THANKS ----------");
    addNewLines(1);
    printText("----------- 17-Agustus-1945 -----------");
    addNewLines(1);
    printText("---------------------------------------");
    addNewLines(1);
    setBold(false);
  }

  /**
   * 
   * @param text
   * @return
   */

  public void finish(){
    if(socket != null){
      try {
        workerThread.sleep(2000);
        outputStream.close();
        socket.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      socket = null;
    }
  } 

  /**
   * 
   * @param text
   */
  public boolean printText(String text){
    try {
      outputStream.write(encodeNonAscii(text).getBytes());
      workerThread.sleep(80);
      return true;
    } catch (Exception e) {
      Log.d("Log","Print text error",e);
      return false;
    }
  }

  public boolean printUnicode(byte[] data){
    try {
      outputStream.write(data);
      workerThread.sleep(50);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean addNewLine(){
    return printUnicode(NEW_LINE);
  }

  public int addNewLines(int count){
    int success=0;
    for(int i=0;i<count;i++){
      if(addNewLine()) success++;
    }
    return success;
  }

  public boolean printImage(Bitmap bitmap){
    byte[] command = decodeBitmap(bitmap);
    return printUnicode(command);
  }

  public void setAlign(int alignType) {
    byte[] d;
    switch (alignType) {
        case ALIGN_CENTER:
            d = ESC_ALIGN_CENTER;
            break;
        case ALIGN_LEFT:
            d = ESC_ALIGN_LEFT;
            break;
        case ALIGN_RIGHT:
            d = ESC_ALIGN_RIGHT;
            break;
        default:
            d = ESC_ALIGN_LEFT;
            break;
    }

    try {
        outputStream.write(d);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }

  public void setLineSpacing(int lineSpacing) {
    byte[] cmd = new byte[]{0x1B, 0x33, (byte) lineSpacing};
    printUnicode(cmd);
  }

  public void setBold(boolean bold) {
      byte[] cmd = new byte[]{0x1B, 0x45, bold ? (byte) 1 : 0};
      printUnicode(cmd);
  }


  /**
   * Bitmap
   */
  public static byte[] decodeBitmap(Bitmap bmp) {
    int bmpWidth = bmp.getWidth();
    int bmpHeight = bmp.getHeight();

    List<String> list = new ArrayList<>();
    StringBuffer sb;
    int zeroCount = bmpWidth % 8;
    String zeroStr = "";
    if (zeroCount > 0) {
        for (int i = 0; i < (8 - zeroCount); i++) zeroStr = zeroStr + "0";
    }

    for (int i = 0; i < bmpHeight; i++) {
        sb = new StringBuffer();
        for (int j = 0; j < bmpWidth; j++) {
            int color = bmp.getPixel(j, i);
            int r = (color >> 16) & 0xff;
            int g = (color >> 8) & 0xff;
            int b = color & 0xff;
            if (r > 160 && g > 160 && b > 160) sb.append("0");
            else sb.append("1");
        }
        if (zeroCount > 0) sb.append(zeroStr);
        list.add(sb.toString());
    }

    List<String> bmpHexList = binaryListToHexStringList(list);
    String commandHexString = "1D763000";
    String widthHexString = Integer
            .toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8 : (bmpWidth / 8 + 1));
    if (widthHexString.length() > 2) {
        return null;
    } else if (widthHexString.length() == 1) {
        widthHexString = "0" + widthHexString;
    }
    widthHexString = widthHexString + "00";

    String heightHexString = Integer.toHexString(bmpHeight);
    if (heightHexString.length() > 2) {
        return null;
    } else if (heightHexString.length() == 1) {
        heightHexString = "0" + heightHexString;
    }
    heightHexString = heightHexString + "00";

    List<String> commandList = new ArrayList<>();
    commandList.add(commandHexString + widthHexString + heightHexString);
    commandList.addAll(bmpHexList);

    return hexList2Byte(commandList);
  }

  private static List<String> binaryListToHexStringList(List<String> list) {
      List<String> hexList = new ArrayList<>();
      for (String binaryStr : list) {
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < binaryStr.length(); i += 8) {
              String str = binaryStr.substring(i, i + 8);
              String hexString = strToHexString(str);
              sb.append(hexString);
          }
          hexList.add(sb.toString());
      }
      return hexList;
  }

  private static String hexStr = "0123456789ABCDEF";
  private static String[] binaryArray = {"0000", "0001", "0010", "0011",
          "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
          "1100", "1101", "1110", "1111"};

  private static String strToHexString(String binaryStr) {
      String hex = "";
      String f4 = binaryStr.substring(0, 4);
      String b4 = binaryStr.substring(4, 8);
      for (int i = 0; i < binaryArray.length; i++) {
          if (f4.equals(binaryArray[i]))
              hex += hexStr.substring(i, i + 1);
      }
      for (int i = 0; i < binaryArray.length; i++) {
          if (b4.equals(binaryArray[i]))
              hex += hexStr.substring(i, i + 1);
      }

      return hex;
  }

  private static byte[] hexList2Byte(List<String> list) {
      List<byte[]> commandList = new ArrayList<>();
      for (String hexStr : list) commandList.add(hexStringToBytes(hexStr));
      return sysCopy(commandList);
  }

  private static byte[] hexStringToBytes(String hexString) {
      if (hexString == null || hexString.equals("")) return null;
      hexString = hexString.toUpperCase();
      int length = hexString.length() / 2;
      char[] hexChars = hexString.toCharArray();
      byte[] d = new byte[length];
      for (int i = 0; i < length; i++) {
          int pos = i * 2;
          d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
      }
      return d;
  }

  private static byte[] sysCopy(List<byte[]> srcArrays) {
      int len = 0;
      for (byte[] srcArray : srcArrays) {
          len += srcArray.length;
      }
      byte[] destArray = new byte[len];
      int destLen = 0;
      for (byte[] srcArray : srcArrays) {
          System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
          destLen += srcArray.length;
      }

      return destArray;
  }

  private static byte charToByte(char c) {
      return (byte) "0123456789ABCDEF".indexOf(c);
  }

  /**
   * 
   * @param text
   */
  private static String encodeNonAscii(String text) {
    return text.replace('á', 'a')
            .replace('č', 'c')
            .replace('ď', 'd')
            .replace('é', 'e')
            .replace('ě', 'e')
            .replace('í', 'i')
            .replace('ň', 'n')
            .replace('ó', 'o')
            .replace('ř', 'r')
            .replace('š', 's')
            .replace('ť', 't')
            .replace('ú', 'u')
            .replace('ů', 'u')
            .replace('ý', 'y')
            .replace('ž', 'z')
            .replace('Á', 'A')
            .replace('Č', 'C')
            .replace('Ď', 'D')
            .replace('É', 'E')
            .replace('Ě', 'E')
            .replace('Í', 'I')
            .replace('Ň', 'N')
            .replace('Ó', 'O')
            .replace('Ř', 'R')
            .replace('Š', 'S')
            .replace('Ť', 'T')
            .replace('Ú', 'U')
            .replace('Ů', 'U')
            .replace('Ý', 'Y')
            .replace('Ž', 'Z');
}
  

  public void InitialPrinter(String name){
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    try {
      if(bluetoothAdapter == null){
        Log.d("log","No Bluetooth adapter found.");
      } else {
        if(!bluetoothAdapter.isEnabled())
        {
          Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          Log.d("log","Bluetooth request enable.");
        }
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
        Log.d("log","Device Not Found.");
        _result = "Bluetooth device not found.";
      }
    } catch(Exception ex){
      Log.d("log",ex.toString());
    }
  }

  void beginListenForData(){
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
                        Log.d("info",data);
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