// Top-level build file: declares plugin versions once for every module.
plugins {
    id("com.android.application") version "9.5.0" apply false
    // AGP 9 compiles Kotlin itself; declaring KGP here only pins the Kotlin version.
    id("org.jetbrains.kotlin.android") version "2.4.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}
