# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 保留所有公开类和方法名，仅用于 debug
#-keepclasseswithmembers class * {
#    public <methods>;
#}


#--------------------------1.实体类---------------------------------
# 与服务端交互时，使用GSON、fastjson等框架解析服务端数据时，所写的JSON对象类不混淆，否则无法将JSON解析成对应的对象；
-keep class sc.windom.sofill.dataClass.** { *; }

#--------------------------2.第三方包-------------------------------

# UltimateBarX 混淆规则
-keep class com.zackratos.ultimatebarx.ultimatebarx.** { *; }
-keep public class * extends androidx.fragment.app.Fragment { *; }

-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# https://bugly.qq.com/docs
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#-------------------------3.与js互相调用的类------------------------


#-------------------------4.反射相关的类和方法----------------------
-keep class sc.windom.sofill.annotations.** { *; }

#-------------------------5.基本不用动区域--------------------------
#指定代码的压缩级别
-optimizationpasses 5

#包明不混合大小写
-dontusemixedcaseclassnames

#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

#混淆时是否记录日志
-verbose

#优化  不优化输入的类文件
-dontoptimize

#预校验
-dontpreverify

# 保留sdk系统自带的一些内容 【例如：-keepattributes *Annotation* 会保留Activity的被@override注释的onCreate、onDestroy方法等】
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# 记录生成的日志数据,gradle build时在本项根目录输出
# apk 包内所有 class 的内部结构
-dump proguard/class_files.txt
# 未混淆的类和成员
-printseeds proguard/seeds.txt
# 列出从 apk 中删除的代码
-printusage proguard/unused.txt
# 混淆前后的映射
-printmapping proguard/mapping.txt


# 避免混淆泛型
-keepattributes Signature
# 抛出异常时保留代码行号,保持源文件以及行号
-keepattributes SourceFile,LineNumberTable

#-----------------------------6.默认保留区-----------------------
## 保持 native 方法不被混淆
#-keepclasseswithmembernames class * {
#   native <methods>;
#}
#
#-keepclassmembers public class * extends android.view.View {
# public <init>(android.content.Context);
# public <init>(android.content.Context, android.util.AttributeSet);
# public <init>(android.content.Context, android.util.AttributeSet, int);
# public void set*(***);
#}
#
##保持 Serializable 不被混淆
#-keepclassmembers class * implements java.io.Serializable {
#   static final long serialVersionUID;
#   private static final java.io.ObjectStreamField[] serialPersistentFields;
#   !static !transient <fields>;
#   !private <fields>;
#   !private <methods>;
#   private void writeObject(java.io.ObjectOutputStream);
#   private void readObject(java.io.ObjectInputStream);
#   java.lang.Object writeReplace();
#   java.lang.Object readResolve();
#}
#
## 保持自定义控件类不被混淆
#-keepclasseswithmembers class * {
# public <init>(android.content.Context,android.util.AttributeSet);
#}
## 保持自定义控件类不被混淆
#-keepclasseswithmembers class * {
# public <init>(android.content.Context,android.util.AttributeSet,int);
#}
## 保持自定义控件类不被混淆
#-keepclassmembers class * extends android.app.Activity {
# public void *(android.view.View);
#}
#
## 保持枚举 enum 类不被混淆
#-keepclassmembers enum * {
#   public static **[] values();
#   public static ** valueOf(java.lang.String);
#}
#
## 保持 Parcelable 不被混淆
#-keep class * implements android.os.Parcelable {
#  public static final android.os.Parcelable$Creator *;
#}
#
## 不混淆R文件中的所有静态字段，我们都知道R文件是通过字段来记录每个资源的id的，字段名要是被混淆了，id也就找不着了。
#-keepclassmembers class **.R$* {
#   public static <fields>;
#}
#
## 保持哪些类不被混淆
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Fragment
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.preference.Preference

# ============忽略警告，否则打包可能会不成功=============
-ignorewarnings

#作者：小余的自习室
#链接：https://juejin.cn/post/7225511164120891453
#来源：稀土掘金
#著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。