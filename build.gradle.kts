buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    // Puedes eliminar esta línea si ya tienes el classpath arriba
    // id("com.google.gms.google-services") version "4.4.2" apply false
}
