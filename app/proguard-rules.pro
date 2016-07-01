# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\xoracle\Desktop\Workspace\Softwares\android-sdk_r24.1.2-windows\android-sdk-windows/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-dontshrink
-ignorewarnings

-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keepclassmembers class * {public *;}

-keepattributes Signature
-keepattributes *Annotation*

#-libraryjars ../kanke_new_library/dlnaphone/libs/teleal-common-1.0.14.jar
#-libraryjars ../kanke_new_library/dlnaphone/libs/cling-core-1.0.5.jar
#-libraryjars ../kanke_new_library/dlnaphone/libs/cling-support-1.0.5.jar
#-libraryjars libs/hdp1.jar


#-libraryjars libs/mta-mid-sdk-2.20.jar
#-libraryjars libs/mta-stats-sdk-2.0.4.jar
# 以下包不进行过滤
-keep class org.** { *; }
#jpush
-dontwarn cn.jpush.**
-keep class cn.jpush.**{*;}
#baidu
-keep class com.baidu.**{*;}
#Qcloud和mat
-keep class com.tencent.upload.network.base.ConnectionImpl
-keep class com.tencent.upload.network.base.ConnectionImpl {*;}
-keep class * extends com.qq.taf.jce.JceStruct { *; }
#加密
-keep class Decoder.**{*;}

#ShareSDK
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}
-keep class com.mob.**{*;}
-dontwarn com.mob.**
-dontwarn cn.sharesdk.**
-dontwarn **.R$*

#async-http
-keep class com.loopj.**{*;}

#convenient
-keep class com.bigkoo.**{*;}

#greenDao
-keep class de.greenrobot.dao.** {*;}
#保持greenDao的方法不被混淆
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
#用来保持生成的表名不被混淆
#public static java.lang.String TABLENAME;
}
-keep class **$Properties

#Gosn
-keep class com.google.**{*;}

#httpclient
-keep class cz.msebera.**{*;}

#library
-keep class com.daimajia.**{*;}
-keep class com.nineoldandroids.**{*;}
#okhttp
-keep class com.squareup.**{*;}
-keep class okio.**{*;}
#PhotoView
-keep class uk.co.**{*;}
#imageLoader
-keep class com.nostra13.**{*;}


-keep public class com.dym.film.R$*{
public static final int *;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#记录生成的日志数据,gradle build时在本项目根目录输出
#apk 包内所有 class 的内部结构
-dump class_files.txt
#未混淆的类和成员
-printseeds seeds.txt
#列出从 apk 中删除的代码
-printusage unused.txt
#混淆前后的映射
-printmapping mapping.txt
