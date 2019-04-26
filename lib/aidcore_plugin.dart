import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/services.dart';
import 'package:meta/meta.dart';
import 'package:flutter/material.dart';
import 'dart:io';


class AidcorePlugin {
  static const MethodChannel _channel = const MethodChannel('aidcore_plugin');
  
  ///     PRINT TO BLUETOOTH
  static Future<String> init({ @required String printerName }) async {
    final Map<String, dynamic> params = <String,dynamic>{ 'printerName': printerName };
    return await _channel.invokeMethod('Init',params);
  }

  static Future<String> setAlign({ @required String align }) async {
    final Map<String, dynamic> params = <String,dynamic>{ 'align': align };
    return await _channel.invokeMethod('SetAlign',params);
  }

  static Future<String> printText({ @required String text }) async {
    final Map<String, dynamic> params = <String,dynamic>{ 'text': text };
    return await _channel.invokeMethod('PrintText',params);
  }

  static Future<String> addNewLines({ @required int count }) async {
    final Map<String, dynamic> params = <String,dynamic>{ 'count': count };
    return await _channel.invokeMethod('AddNewLines',params);
  }

  static Future<String> setLineSpacing({ @required int line }) async {
    final Map<String, dynamic> params = <String,dynamic>{ 'line': line };
    String result = await _channel.invokeMethod('SetLineSpacing',params);
    return result;
  }

  static Future<String> setBold({ @required bool bold }) async {
    final Map<String, dynamic> params = <String,dynamic>{ 'bold': bold };
    return await _channel.invokeMethod('SetBold',params);
  }

  static Future<String> printImage({ @required Uint8List  imgBytes }) async {
    final Map<String, dynamic> params = <String,dynamic>{ 'bytes': imgBytes };
    return await _channel.invokeMethod('PrintImage',params);
  }

  static Future<String> finish() async {
    return await _channel.invokeMethod('Finish');
  }

  ///     WEBVIEW PRINT
  static Future<dynamic> pdfView(String text){
    assert(text != null && text.isNotEmpty);
    return _channel.invokeMethod('PdfView',text);
  }
}
