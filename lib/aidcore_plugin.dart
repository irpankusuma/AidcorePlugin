import 'dart:async';
import 'package:flutter/services.dart';
import 'package:meta/meta.dart';

class AidcorePlugin {
  static const MethodChannel _channel = const MethodChannel('aidcore_plugin');
  
  ///     PRINT TO BLUETOOTH
  static Future<String> aidcorePrint({ @required String printerName, String data }) async {
    final Map<String, dynamic> params = <String,dynamic>{
      'printerName': printerName,
      'data': data
    };
    
    String result = await _channel.invokeMethod('AidcorePrint',params);
    return result;
  }
}
