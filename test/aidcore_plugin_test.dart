import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:aidcore_plugin/aidcore_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('aidcore_plugin');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await AidcorePlugin.platformVersion, '42');
  });
}
