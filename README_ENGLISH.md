# [中文文档](https://github.com/YolandaQingniu/qnscalesdk)

# Yolanda Android SDK

## Quick start
### Confusion configuration(proguard-rules)
+ -keep class com.qingniu.scale.model.BleScaleData{*;}

### Specific operation document
[Specific integration documentation](https://yolandaqingniu.gitee.io/sdk-doc/)

### Mini Program Entry
[Mini Program Access Document](https://mp.weixin.qq.com/wxopen/plugindevdoc?appid=wx2a4ca48ed5e96748&token=1470542861&lang=zh_CN)

### AndroidX adaptation
If you are adapting to Androix, [please access the SDK adapted to Androidx](https://github.com/YolandaQingniu/qnscalesdkX)

### Android Studio Online dependence
* In the root directory of your project **build.gradle**Add**jitpack**support
   ```
   allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
   ```
* Add dependency in **build.gradle** in the root directory of your module
	```
	<!--The version number here, 2.3.1 can be specified as any release version-->
	<!--If you want to always use the latest version, you can replace 2.3.1 master-SNAPSHOT -->
	dependencies {
	        ...
	        compile 'com.github.YolandaQingniu:qnscalesdk:2.3.1'
	}
	```
	
### Android Studio  Local dependency
* Download the latest [jar and so library](https://github.com/YolandaQingniu/qnscalesdk/releases/download/2.3.1/qnsdk-2.3.1-Android.zip), import the downloaded `jar and so library`
* Create a libs folder under app moudle, and put the so library and jar package into the libs folder.
*  Add configuration to the gradle file of app moudle
```
 sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
```    
 
* Apply for Bluetooth permissions, location permissions, and network permissions in the manifest file (not required for offline SDK)
    ```
   <!--Bluetooth permission-->
   <uses-permission android:name="android.permission.BLUETOOTH" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
   <!-- Dynamic application is required for 6.0 and later-->
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <!--Used to store logs-->
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   <!--If it is an online SDK, network permissions are required-->
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.WAKE_LOCK" />
    ```
* The components in the SDK need to be registered in **AndroidManifest.xml**：


   ```
   <service android:name="com.qingniu.qnble.scanner.BleScanService" android:permission="android.permission.BIND_JOB_SERVICE"/>
   <service android:name="com.qingniu.scale.measure.ble.ScaleBleService" android:permission="android.permission.BIND_JOB_SERVICE"/>
   <service android:name="com.qingniu.scale.measure.broadcast.ScaleBroadcastService" android:permission="android.permission.BIND_JOB_SERVICE"/>
   <service android:name="com.qingniu.scale.wsp.ble.ScaleWspBleService" android:permission="android.permission.BIND_JOB_SERVICE" />
   <service android:name="com.qingniu.scale.measure.broadcast.ScaleFoodBroadcastService" android:permission="android.permission.BIND_JOB_SERVICE" />
    ```
* The resources of the v4 package are used in the SDK, and the resources of the v4 package need to be introduced in the developer project

## Precautions
- The targetSdkVersion is 23 and above, you need to obtain the positioning permission before scanning the device, you need to apply for it by the developer
- To use the Bluetooth function on some mobile phones, you need to turn on the GPS to scan the device. The SDK will output the log that GPS is not turned on, but will not call back errors. Developers can limit themselves
- If your project is multi-process, it is recommended to limit the SDK initialization to the main process

## common problem
For specific usage, please refer to [API document](https://yolandaqingniu.github.io/) and `Demo`. The following are some common problems.

1. Prompt initialization appid error
    + Check whether the initialization file and the used appid match
    + Check whether the imported SDK is the latest
2. The scan device call is successful, but there has been no device callback and no error callback
    + Check whether the scanned device has been connected by others
    + Some mobile phones need to turn on GPS to scan the device, please check whether the mobile phone GPS is turned on
3. Connecting to the device has been unsuccessful or disconnected soon after success
    + Check if the device is connected by someone else
    + Check whether the currently connected device has been paired in the system Bluetooth, if it is already paired, you need to cancel the pairing
    + Some mobile phones need to be scanned before they can connect successfully. Scan the device before connecting
4. The number of indicators obtained is different from the number of indicators for business negotiations
    + First check the device with the problem and whether the name displayed during scanning is correct
    + No matter whether the heart rate indicator is opened or not, the SDK will issue the heart rate indicator
5. Data or device monitoring callback, callback multiple times at the same time
    + First determine whether, set up multiple monitoring. When the monitor is not used, it must be set to null
    + Determine whether the measurement is wearing shoes, this may lead to a situation where multiple measurements are completed in a short time
6. SDK returns no location permission error
    + Check if both **ACCESS_COARSE_LOCATION** and **ACCESS_FINE_LOCATION** have been applied for, and both permissions have been verified in the SDK
    + Whether to compile version 26 and above, if yes, both permissions need to be applied separately (new features of 8.0)
7. The SDK returns the wrong file, confirm that the file location is normal, and confirm that the file is used in the demo without abnormality
    + Check whether the so library is added
    +Check whether the packaged apk file contains the so library
8. Measure Native method error after SDK integration
    + Check if there is a problem with the integration settings
    + Local integration cannot be mixed with online integration, only one method can be selected
    
**`Tip`**: Encountered a problem that cannot be located, I hope the developer can provide the log as soon as possible so that we can find the problem as soon as possible
