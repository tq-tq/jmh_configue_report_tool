// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

plugins {
  id("java")
  id("org.jetbrains.intellij") version "1.13.2"
}

group = "com.intellij.sdk"
version = "2.0.7"

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.openjdk.jmh", "jmh-core", version = "1.36")
  implementation("org.openjdk.jmh", "jmh-generator-annprocess", version = "1.36")
  implementation("com.alibaba","fastjson", version = "1.2.67_noneautotype2")
  implementation("commons-io","commons-io", version = "2.7")
  implementation("org.jfree","jfreechart", version = "1.0.19")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
  version.set("2022.1.4")
  plugins.set(listOf("com.intellij.java"))
}

tasks {
  buildSearchableOptions {
    enabled = false
  }

  patchPluginXml {
    version.set("${project.version}")
    sinceBuild.set("221")
    untilBuild.set("223.*")
  }

}
