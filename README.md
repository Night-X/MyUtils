# MyUtils
一些有用的工具类和服务类。
okhttp的封装，下载服务。

Version:
--------

   v1.0.0:

    Gradle依赖: compile 'cn.knight:tools:1.0.0'

ProGuard
--------

If you are using ProGuard you might need to add the following options:

```
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase