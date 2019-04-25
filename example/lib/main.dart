import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:aidcore_plugin/aidcore_plugin.dart';
import 'package:aidcore_plugin_example/toast.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  BlinkingToast toast = new BlinkingToast();
  final navigatorKey = GlobalKey<NavigatorState>();
  GlobalKey _globalKey = new GlobalKey();

  bool inside = false;
  Uint8List imageInMemory;

  TextEditingController _printerName = new TextEditingController();
  TextEditingController _messagePrint = new TextEditingController();

  @override
  void initState() {
    super.initState();
    _printerName.text = "Virtual Bluetooth Printer";
    _messagePrint.text = "Example print from android to printer bluetooth - 1";
  }

  Future<Uint8List> _capturePng() async {
    try {
      print('inside');
      inside = true;
      RenderRepaintBoundary boundary =
          _globalKey.currentContext.findRenderObject();
      ui.Image image = await boundary.toImage(pixelRatio: 3.0);
      ByteData byteData =
          await image.toByteData(format: ui.ImageByteFormat.png);
      Uint8List pngBytes = byteData.buffer.asUint8List();
//      String bs64 = base64Encode(pngBytes);
//      print(pngBytes);
//      print(bs64);
      print('png done');
      setState(() {
        imageInMemory = pngBytes;
        inside = false;
      });
      return pngBytes;
    } catch (e) {
      print(e);
    }
  }

  void examplePrint() async{
    await(AidcorePlugin.init(printerName:_printerName.text));
    await(AidcorePlugin.setAlign(align:"center"));
    await(AidcorePlugin.setBold(bold:true));
    await(AidcorePlugin.printText(text:"GWK Cultural Park"));
    await(AidcorePlugin.setBold(bold:false));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Selamat Datang / Welcome"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"www.gwkbali.com"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"------------------------------"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Order Number : 1234567890"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Pos ID : POSID_001"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Cashier : Sempak Superman"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Printed : 19 Masehi Tahun 200 Cahaya"));
    await(AidcorePlugin.addNewLines(count:2));
    await(AidcorePlugin.printText(text:"------------------------------"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"2X  ABC170001 [P] ADULT FORG  250,0000"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"------------------------------"));
    await(AidcorePlugin.addNewLines(count:2));
    await(AidcorePlugin.printText(text:"Total Item    1   Total Qty   2"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Sub Total     (Rp.)       250,0000"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Total         (Rp.)       250,0000"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Payment       (Rp.)       300,0000"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Change        (Rp.)       50,0000"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"------------------------------"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"###### Payment #####"));
    await(AidcorePlugin.addNewLines(count:2));
    await(AidcorePlugin.printText(text:"Payment Type    : Single Payment"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Payment Method  : Cash"));
    await(AidcorePlugin.addNewLines(count:3));
    await(AidcorePlugin.printText(text:"DISINI PAKE BARCODE"));
    await(AidcorePlugin.addNewLines(count:3));
    await(AidcorePlugin.printText(text:"------------------------------"));
    await(AidcorePlugin.addNewLines(count:1));
    await(AidcorePlugin.printText(text:"Kata-kata cantik bahasa indonesia dan inggris"));
    await(AidcorePlugin.finish());
  }

  void printImage() async {
    await (AidcorePlugin.printImage(bitmap:imageInMemory));
  }

  void webPrint() async {
    String html = "<h1>Test H1</h1><br /><h2>Test H2</h2>";
    final String result = await(AidcorePlugin.pdfView(html));
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      navigatorKey: navigatorKey,
      home: new RepaintBoundary(
        key: _globalKey,
        child: Scaffold(
          appBar: AppBar(
            title: const Text('Aidcore Plugin Example'),
          ),
          body: new Container(
            child: new ListView(
              children: <Widget>[
                new TextField(
                  controller: _printerName,
                ),
                new TextField(
                  controller: _messagePrint,
                ),
                new RaisedButton(
                  child: new Text('EXAMPLE PRINT'),
                  onPressed: () => examplePrint()
                ),
                new RaisedButton(
                  color: Colors.blue,
                  child: new Text('WEB PRINT PDF', style: TextStyle(color:Colors.white),),
                  onPressed: () => webPrint(),
                ),
                new RaisedButton(
                  color: Colors.green,
                  child: new Text('PRINT IMAGE', style: TextStyle(color:Colors.white),),
                  onPressed: () => printImage(),
                ),
                new RaisedButton(
                  color: Colors.red,
                  child: Text('CAPTURE IMAGE'),
                  onPressed: _capturePng,
                ),
                inside ? CircularProgressIndicator()
                :
                imageInMemory != null
                    ? Container(
                        child: Image.memory(imageInMemory),
                        margin: EdgeInsets.all(10))
                    : Container(),
              ],
            ),
          )
        ),
      )
    );
  }
}
