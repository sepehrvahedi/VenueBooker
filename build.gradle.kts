import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.lognet.springboot.grpc.gradle.ReactiveFeature
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("org.springframework.boot") version "3.2.3"
  id("io.spring.dependency-management") version "1.1.4"
  id("io.github.lognet.grpc-spring-boot") version "5.1.5"
  id("com.google.protobuf") version "0.9.4"

  kotlin("jvm") version "1.9.22"
  kotlin("plugin.spring") version "1.9.22"
  kotlin("kapt") version "1.9.22"

  `jvm-test-suite`
}

group = "army"
version = "0.0.1-SNAPSHOT"

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
  all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
  }
}

repositories {
  mavenCentral()
//  maven {
//    url = uri("<MAVEN REPO URL>")
//  }
}

grpcSpringBoot {
  reactiveFeature = ReactiveFeature.REACTOR
}

dependencies {
  api("org.springframework.boot:spring-boot-starter-validation")
  api("org.springframework.boot:spring-boot-starter-actuator")
  api("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
  api("org.springframework.boot:spring-boot-starter-webflux")
  api("io.projectreactor.kotlin:reactor-kotlin-extensions")
  api("org.jetbrains.kotlin:kotlin-reflect")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  api("org.springframework.boot:spring-boot-starter-aop")
  api("com.github.ben-manes.caffeine:caffeine")
  api("com.google.code.gson:gson")
  api("io.projectreactor:reactor-core-micrometer")
  api("io.grpc:grpc-protobuf:1.61.1")
  api("io.grpc:grpc-stub:1.61.1")
  api("io.grpc:grpc-netty-shaded:1.61.1")
  api("javax.annotation:javax.annotation-api:1.3.2")
  api("org.springframework.security:spring-security-crypto")
  api("ch.qos.logback:logback-classic:1.4.11")
  api("ch.qos.logback:logback-core:1.4.11")
  api("org.springframework.security:spring-security-core")
  api("org.springframework.boot:spring-boot-starter-security")

  api("io.micrometer:context-propagation:1.1.1")
  api("io.jsonwebtoken:jjwt-api:0.12.5")
  api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
  api("io.jsonwebtoken:jjwt-jackson:0.12.5")
  api("org.redisson:redisson-spring-boot-starter:3.33.0")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
  api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

  api("com.appmattus.fixture:fixture:1.2.0")
  api("com.google.protobuf:protobuf-java-util:3.24.0")


  runtimeOnly("io.micrometer:micrometer-registry-prometheus")
  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

  kapt("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.24.0"
  }
  plugins {
    create("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:1.61.1"
    }
  }
  generateProtoTasks {
    all().forEach {
      it.plugins {
        create("grpc")
      }
    }
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "21"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}


tasks.getByName<Jar>("jar") {
  enabled = true
  archiveFileName.set("booker-jar.jar")
}

tasks.getByName<BootJar>("bootJar") {
  enabled = true
  archiveFileName.set("booker.jar")
}

