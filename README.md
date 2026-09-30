# OkHttp API Test Framework

An API test framework written in Java. It sends HTTP requests with **OkHttp**, checks responses with **JUnit 5**, and reads JSON with **Jackson** and **JsonPath**. The environment (test / qa / staging) is chosen from properties files.

The example tests call [JSONPlaceholder](https://jsonplaceholder.typicode.com), a free public API for testing.

---

## 1. Prerequisites

| Tool | Version | Check with |
|---|---|---|
| JDK | 21 (LTS), e.g. [Eclipse Temurin](https://adoptium.net) | `java -version` |
| Maven | 3.9.x | `mvn -v` |
| IDE | Antigravity / VS Code, IntelliJ IDEA or Eclipse | – |

### Install JDK 21 and Maven (macOS)

**Option A: Homebrew**
```bash
brew install --cask temurin@21
brew install maven
```

**Option B: manual install (no admin rights needed)**
1. Download the JDK 21 `.tar.gz` for your CPU (Apple Silicon = `aarch64`, Intel = `x64`) from https://adoptium.net and extract it into `~/Library/Java/JavaVirtualMachines/`.
2. Download the Maven `apache-maven-3.9.x-bin.tar.gz` from https://maven.apache.org/download.cgi and extract it into `~/tools/`.
3. Add this to `~/.zshrc` (change the Maven folder name to match your version):
   ```bash
   # Java + Maven
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   export MAVEN_HOME="$HOME/tools/apache-maven-3.9.x"
   export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
   ```
4. Open a new terminal and check that `java -version` and `mvn -v` both work.

---

## 2. IDE setup (Antigravity / VS Code)

1. Install the **Extension Pack for Java** (`vscjava.vscode-java-pack`). It includes Language Support for Java (Red Hat), Maven, Debugger and Test Runner.
2. Point the IDE at JDK 21. Press Cmd+Shift+P, choose **Preferences: Open User Settings (JSON)** and add the lines below, using your own JDK and Maven paths:
   ```json
   "java.jdt.ls.java.home": "/path/to/jdk-21/Contents/Home",
   "java.configuration.runtimes": [
       { "name": "JavaSE-21", "path": "/path/to/jdk-21/Contents/Home", "default": true }
   ],
   "maven.executable.path": "/path/to/maven/bin/mvn",
   "java.configuration.updateBuildConfiguration": "automatic"
   ```
3. Quit the IDE completely (Cmd+Q) and reopen it.
4. Choose **File → Open Folder…** and select this project folder, the one that contains `pom.xml`. Click **Trust** when asked.
5. Wait for the status bar to show **Java: Ready**. The project is then imported as a Maven project and the dependencies are downloaded.

**IntelliJ / Eclipse:** import the folder as a **Maven project** and set the project SDK to JDK 21.

---

## 3. Project structure

```
OkHttpProject
├── pom.xml                          # Maven build file: dependencies and plugins
├── README.md
└── src
    └── test
        ├── java
        │   ├── Config.java          # Loads the environment properties file
        │   └── GetRequestTest.java  # Example GET API tests
        └── resources
            ├── test.env.properties     # default environment
            ├── qa.env.properties
            └── staging.env.properties
```

The tests and their config live under `src/test`. `src/main` is not used.

---

## 4. Libraries used

All of these are declared in `pom.xml`. Maven downloads them automatically.

| Library | Maven artifact | Version | Used for |
|---|---|---|---|
| **OkHttp** | `com.squareup.okhttp3:okhttp` | 4.12.0 | HTTP client: builds and sends requests (`OkHttpClient`, `Request`, `Response`) |
| **JUnit 5 (Jupiter)** | `org.junit.jupiter:junit-jupiter` | 5.11.4 | Test framework: `@Test` and assertions such as `assertEquals` |
| **Jackson Databind** | `com.fasterxml.jackson.core:jackson-databind` | 2.18.2 | JSON parsing as a tree (`ObjectMapper`, `JsonNode`); can also map JSON to Java objects (POJOs) |
| **Jayway JsonPath** | `com.jayway.jsonpath:json-path` | 2.9.0 | JSON parsing with dot paths such as `address.geo.lat` (`JsonPath`, `DocumentContext`) |
| Maven Surefire Plugin | `org.apache.maven.plugins:maven-surefire-plugin` | 3.5.2 | Runs the JUnit tests during `mvn test` |

The code is compiled for **Java 21** (`maven.compiler.release=21` in `pom.xml`).

---

## 5. Running the tests

### From the terminal
```bash
mvn test                               # all tests, default "test" environment
mvn test -Dtest=GetRequestTest         # one test class
mvn test -Dtest=GetRequestTest#getPostById   # one test method
mvn test -Denv=qa                      # all tests against QA
```
A successful run ends with `Tests run: N, Failures: 0` and `BUILD SUCCESS`.

### From the IDE
- Open a test file and click **▶ Run Test** above a test method or the class.
- Or open the **Testing** panel (flask icon in the left bar) and run tests from there.

---

## 6. Environment configuration

Base URLs are **not hard-coded** in the tests. Each environment has its own properties file in `src/test/resources`:

```properties
# test.env.properties
base.url=https://jsonplaceholder.typicode.com
```

`Config.java` chooses the file from the `env` system property:

| Command | File loaded |
|---|---|
| `mvn test` | `test.env.properties` (default) |
| `mvn test -Denv=qa` | `qa.env.properties` |
| `mvn test -Denv=staging` | `staging.env.properties` |

The console prints which file was used, e.g. `Loaded environment: qa (qa.env.properties)`. If the file doesn't exist (for example, a typo in `-Denv`), the tests stop with `<env>.env.properties not found on classpath`. This means they never run against the wrong environment by mistake.

**Using config in a test:**
```java
Config.baseUrl()          // value of base.url
Config.get("any.key")     // any other key from the properties file
```

**Adding a new environment:** create `src/test/resources/<name>.env.properties` with a `base.url`, then run `mvn test -Denv=<name>`. No code change is needed.

**Choosing the environment for IDE runs:** add this to the IDE settings JSON:
```json
"java.test.config": { "vmArgs": ["-Denv=qa"] }
```

---

## 7. CI pipeline (GitHub Actions)

The workflow file is `.github/workflows/api-tests.yml`. It runs `mvn test` on JDK 21 (Ubuntu). Maven dependencies are cached between runs.

| Trigger | Environment |
|---|---|
| Push to `main` / `master` | `test` |
| Pull request | `test` |
| Manual run: **Actions** tab → **API Tests** → **Run workflow** | choose `test`, `qa` or `staging` |

After each run, download the **surefire-reports-&lt;env&gt;** artifact from the run's summary page to see the detailed test results. The reports are uploaded even when tests fail.

---

## 8. JSON parsing: two ways

`GetRequestTest.getUserWithNestedJson()` reads the same nested response in both ways.

Sample response from `/users/1`:
```json
{
  "id": 1,
  "address": { "city": "Gwenborough", "geo": { "lat": "-37.3159", "lng": "81.1496" } },
  "company": { "name": "Romaguera-Crona" }
}
```

### Method 1: Jackson (`JsonNode`)
Goes down the tree one level at a time.
```java
ObjectMapper mapper = new ObjectMapper();
JsonNode node = mapper.readTree(body);

String lat = node.get("address").get("geo").get("lat").asText();
int id     = node.get("id").asInt();
```

### Method 2: JsonPath (`DocumentContext`)
Goes straight to a nested value with a dot path.
```java
DocumentContext json = JsonPath.parse(body);   // parse once

String lat  = json.read("address.geo.lat");    // read many times
String name = json.read("company.name");
```
`DocumentContext` holds the parsed JSON document, so it isn't re-parsed for every `read()`. The one-line `JsonPath.read(body, "path")` re-parses on each call.

**Useful JsonPath patterns:**
| Path | Meaning |
|---|---|
| `address.geo.lat` | nested field |
| `[0].name` | first item of a top-level array |
| `items[2].price` | third item of an array field |
| `items.length()` | size of an array |
| `$..email` | every `email` field at any depth |

**Which one to use?**
- **JsonPath:** quick value checks in assertions. Shorter and easier to read.
- **Jackson:** when you need to map JSON to Java classes (POJOs) or build JSON request bodies.

---

## 9. Writing a new test

Add a method to an existing test class, or create a new class under `src/test/java`:

```java
@Test
void getCommentsForPost() throws IOException {
    Request request = new Request.Builder()
            .url(Config.baseUrl() + "/posts/1/comments")
            .get()
            .build();

    try (Response response = client.newCall(request).execute()) {   // try-with-resources closes the response
        String body = response.body().string();                     // body can only be read once

        assertEquals(200, response.code());

        DocumentContext json = JsonPath.parse(body);
        assertEquals(1, (int) json.read("[0].postId"));
    }
}
```

**Conventions:**
- Always build URLs from `Config.baseUrl()`. Never hard-code a host.
- Wrap `execute()` in `try (...)` so the connection is released.
- Read `response.body().string()` **once** and store it in a variable.
- Give test methods clear names that describe the behaviour, e.g. `getUserWithNestedJson`.
