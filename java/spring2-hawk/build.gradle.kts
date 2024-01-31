plugins {
    `java-library`
    `maven-publish`
    `signing`
    id("org.springframework.boot") version "2.7.8"
    id("io.spring.dependency-management") version "1.1.4"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    compileOnly("org.springframework:spring-web:5.3.25")
    compileOnly("org.springframework.security:spring-security-core:5.7.6")
    compileOnly("org.springframework.security:spring-security-web:5.7.6")
    compileOnly("org.apache.tomcat.embed:tomcat-embed-core:9.0.44")

    implementation("commons-io:commons-io:2.15.1")
    api(project(":core"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.bootJar {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set("HAWK Authentication")
                url.set("https://github.com/jc-lab/hawk-authentication")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("jclab")
                        name.set("Joseph Lee")
                        email.set("joseph@jc-lab.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/jc-lab/hawk-authentication.git")
                    developerConnection.set("scm:git:ssh://git@github.com/jc-lab/hawk-authentication.git")
                    url.set("https://github.com/jc-lab/hawk-authentication")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if ("$version".endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = findProperty("ossrhUsername") as String?
                password = findProperty("ossrhPassword") as String?
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

tasks.withType<Sign>().configureEach {
    onlyIf { project.hasProperty("signing.gnupg.keyName") || project.hasProperty("signing.keyId") }
}
