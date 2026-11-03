# HabitTap release rules (NFR-9).
#
# Most of the stack needs nothing: Compose, Koin and DataStore ship their own rules, and
# Room generates plain classes. The entries below cover the three places where something
# is referenced by name rather than by a call site, which R8 cannot see.

# --- kotlinx.serialization -------------------------------------------------------------
# Navigation 3 keys are @Serializable; the generated serializers are looked up reflectively.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.taptap.navigation.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.taptap.navigation.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.taptap.navigation.**$$serializer { *; }

# --- Room ---------------------------------------------------------------------------
# Entities are instantiated by the generated DAO implementation.
-keep class com.taptap.data.db.CounterEntity { *; }
-keep class com.taptap.data.db.CounterEventEntity { *; }
-keep class com.taptap.data.db.UrgeCount { *; }
# Enums are stored and read back by name, so their names must survive obfuscation.
-keepclassmembers enum com.taptap.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public java.lang.String name();
}

# --- WorkManager ----------------------------------------------------------------------
# Workers are constructed by name from the WorkManager database.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
