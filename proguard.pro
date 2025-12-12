# === INPUT & OUTPUT ===
-injars app.jar
-outjars app-obf.jar

# === JDK 25 standard modules ===
-libraryjars ${java.home}/jmods/java.base.jmod

# === KEEP ENTRY POINT ===
-keep public class Main {
    public static void main(java.lang.String[]);
}

# === REMOVE UNUSED CODE ===
-dontoptimize
-dontpreverify
-dontnote
-dontwarn
-overloadaggressively

# === ENABLE OBFUSCATION USING DEFAULT NAMES ===
-repackageclasses ''
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,LineNumberTable,EnclosingMethod
