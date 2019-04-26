import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/services.dart';
import 'package:meta/meta.dart';
import 'package:flutter/material.dart';


class AidcorePlugin {
  static const MethodChannel _channel = const MethodChannel('aidcore_plugin');
  GlobalKey _globalKey = new GlobalKey();
  bool inside = false;
  Uint8List imageMemory;
  
  ///     PRINT TO BLUETOOTH
  static Future<String> init({ @required String printerName }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'printerName': printerName
    };
    
    String result = await _channel.invokeMethod('Init',params);
    return result;
  }

  static Future<String> setAlign({ @required String align }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'align': align
    };
    
    String result = await _channel.invokeMethod('SetAlign',params);
    return result;
  }

  static Future<String> printText({ @required String text }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'text': text
    };
    
    String result = await _channel.invokeMethod('PrintText',params);
    return result;
  }

  static Future<String> addNewLines({ @required int count }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'count': count
    };
    
    String result = await _channel.invokeMethod('AddNewLines',params);
    return result;
  }

  static Future<String> setLineSpacing({ @required int line }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'line': line
    };
    
    String result = await _channel.invokeMethod('SetLineSpacing',params);
    return result;
  }

  static Future<String> setBold({ @required bool bold }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'bold': bold
    };
    
    String result = await _channel.invokeMethod('SetBold',params);
    return result;
  }

  static Future<String> printImage({ @required Uint8List  bitmap }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'bitmap': bitmap
    };
    
    String result = await _channel.invokeMethod('PrintImage',params);
    return result;
  }

  static Future<String> printBitmap({ @required String printerName, @required Uint8List  bitmap }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'printerName': printerName,
      'bitmapImage': bitmap
    };
    
    String result = await _channel.invokeMethod('PrintBitmap',params);
    return result;
  }

  static Future<String> examplePrint({ @required String printerName }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'printerName': printerName,
    };
    
    String result = await _channel.invokeMethod('ExamplePrint',params);
    return result;
  }

  static Future<String> intentPrint({ @required String printerName, String text }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'printerName': printerName,
      'text': text
    };
    
    String result = await _channel.invokeMethod('IntentPrint',params);
    return result;
  }

  static Future<String> finish() async {
    String result = await _channel.invokeMethod('Finish');
    return result;
  }

  ///     WEBVIEW PRINT
  static Future<dynamic> pdfView(String text){
    assert(text != null && text.isNotEmpty);
    return _channel.invokeMethod('PdfView',text);
  }
}
