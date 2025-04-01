import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      title: 'Platform Channel Demo',
      home: HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const _batteryChannel =
      MethodChannel('samples.flutter.dev/battery'); // 메서드 채널 생성
  static const _accelChannel =
      EventChannel('samples.flutter.dev/accelerometer'); // 이벤트 채널 생성

  String _battery = '🔋 배터리 잔량: (버튼 클릭)';
  String _accelData = '📦 센서 수신 중...';

  @override
  void initState() {
    super.initState();

    // 가속도 센서 EventChannel 구독
    _accelChannel.receiveBroadcastStream().listen((event) {
      setState(() {
        _accelData = event.toString();
      });
    }, onError: (error) {
      setState(() {
        _accelData = '센서 오류: $error';
      });
    });
  }

  // 배터리 정보 MethodChannel 요청
  Future<void> _getBatteryLevel() async {
    try {
      final int result =
          await _batteryChannel.invokeMethod('getBatteryLevel'); // 호출 메서드
      setState(() {
        _battery = '🔋 배터리 잔량: $result%';
      });
    } on PlatformException catch (e) {
      setState(() {
        _battery = '⚠️ 오류: ${e.message}';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Method & EventChannel 예제')),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: _getBatteryLevel,
              child: const Text('🔋 배터리 잔량 확인'),
            ),
            const SizedBox(height: 12),
            Text(_battery, style: const TextStyle(fontSize: 18)),
            const Divider(height: 40),
            const Text('📡 가속도 센서 (실시간):'),
            const SizedBox(height: 8),
            Text(_accelData, style: const TextStyle(fontSize: 18)),
          ],
        ),
      ),
    );
  }
}
