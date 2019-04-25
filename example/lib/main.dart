import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:aidcore_plugin/aidcore_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  TextEditingController _printerName = new TextEditingController();
  TextEditingController _messagePrint = new TextEditingController();
  String errorMessage = '';

  @override
  void initState() {
    super.initState();
    _printerName.text = "Virtual Bluetooth Printer";
    _messagePrint.text = "Example print from android to printer bluetooth - 1";
    // initPlatformState();
  }

  void print() async{
    if(_printerName.text == null){
      setState(() {
        errorMessage = 'Please input field 1';
      });
    } else {
      await(AidcorePlugin.aidcorePrint(printerName:_printerName.text,data:_messagePrint.text));
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Aidcore Plugin Example'),
        ),
        body: new Container(
          child: new Column(
            children: <Widget>[
              new TextField(
                controller: _printerName,
              ),
              new TextField(
                controller: _messagePrint,
              ),
              new RaisedButton(
                child: new Text('PRINT TEST'),
                onPressed: () => print(),
              )
            ],
          ),
        )
      ),
    );
  }
}
