# OkHttp API Test Framework

An API test framework written in Java. It sends HTTP requests with **OkHttp**, checks responses with **JUnit 5**, and reads JSON with **Jackson** and **JsonPath**. The environment (test / qa / staging) is chosen from properties files.

The example tests call [GoRest](https://gorest.co.in), a free public REST API for testing that **really saves data**. Records you create can be read back, and deleted records are gone. That makes full create → get → delete tests possible.

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

### Get a GoRest access token

GoRest needs a free access token for creating and deleting data (reading works without one).

1. Go to https://gorest.co.in/consumer/login and sign in with GitHub, Google or Microsoft.
2. Copy your access token.
3. Add it to `~/.zshrc` and open a new terminal:
   ```bash
   export GO_REST_API_TOKEN=your-token-here
   ```

The token is **never** stored in the project, so it can't be committed to git by mistake. See section 6 for other ways to pass it.

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
        │   ├── Base.java            # Parent class of all tests: default objects (client, mapper, apiUtils, faker, response, path)
        │   ├── APIUtils.java        # All API request methods (get, post, ...) + response helpers
        │   ├── Config.java          # Loads the environment properties file
        │   ├── TemplateUtil.java    # Renders JSON request bodies from templates
        │   ├── GetRequestTest.java  # GET tests: list of users, one user (both JSON parsing methods)
        │   ├── PostRequestTest.java # POST tests: create a user, create a post for a user
        │   ├── UsersEnd2EndTest.java # E2E: create → get → delete → get 404 for /users
        │   └── PostsEnd2EndTest.java # E2E: create → get → delete → get 404 for /posts
        └── resources
            ├── test.env.properties     # default environment
            ├── qa.env.properties
            ├── staging.env.properties
            └── templates
                ├── createPost.ftl      # JSON body template for POST /posts
                └── createUser.ftl      # JSON body template for POST /users
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
| **Apache FreeMarker** | `org.freemarker:freemarker` | 2.3.35 | Template engine: builds JSON request bodies from `.ftl` template files |
| **Datafaker** | `net.datafaker:datafaker` | 2.7.0 | Generates realistic random test data (names, emails, addresses, ...) |
| Maven Surefire Plugin | `org.apache.maven.plugins:maven-surefire-plugin` | 3.5.2 | Runs the JUnit tests during `mvn test` |

The code is compiled for **Java 21** (`maven.compiler.release=21` in `pom.xml`).

---

## 5. Running the tests

### From the terminal
```bash
mvn test                               # all tests, default "test" environment
mvn test -Dtest=GetRequestTest         # one test class
mvn test -Dtest=GetRequestTest#getUsersList  # one test method
mvn test -Denv=qa                      # all tests against QA
```
A successful run ends with `Tests run: N, Failures: 0` and `BUILD SUCCESS`.

`GO_REST_API_TOKEN` must be set (see section 1). Without it, the output starts with `WARNING: GO_REST_API_TOKEN is not set` and every test that creates data fails with `expected: <201> but was: <401>`.

### From the IDE
- Open a test file and click **▶ Run Test** above a test method or the class.
- Or open the **Testing** panel (flask icon in the left bar) and run tests from there.

---

## 6. Environment configuration

Base URLs are **not hard-coded** in the tests. Each environment has its own properties file in `src/test/resources`:

```properties
# test.env.properties
base.url=https://gorest.co.in/public/v2
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

### API token

`Config.token()` reads the token from, in this order:
1. the `GO_REST_API_TOKEN` environment variable, e.g. `export GO_REST_API_TOKEN=...` in `~/.zshrc`
2. the `api.token` system property, e.g. `mvn test -Dapi.token=...`

The shared `client` in `Base` adds `Authorization: Bearer <token>` to **every** request automatically, so tests and `APIUtils` never deal with it.

**IDE runs:** an IDE started from the Dock doesn't read `~/.zshrc`. Either start it from a terminal, or add the token to the IDE settings JSON (your user settings, not the project):
```json
"java.test.config": { "env": { "GO_REST_API_TOKEN": "your-token-here" } }
```

---

## 7. JSON parsing: two ways

In tests, use `mapper.readTree(apiUtils.getBody(response))` (Jackson) or `apiUtils.getJsonPath(response)` (JsonPath). The examples below show what each one does underneath.

`GetRequestTest.getUserById()` reads the same response in both ways.

Sample response from `/users/{id}`:
```json
{ "id": 8643372, "name": "Jane Doe", "email": "jane.doe.20260930183330473@gmail.com", "gender": "female", "status": "active" }
```

### Method 1: Jackson (`JsonNode`)
Goes down the tree one level at a time.
```java
ObjectMapper mapper = new ObjectMapper();
JsonNode node = mapper.readTree(body);

String email = node.get("email").asText();
int id       = node.get("id").asInt();

// nested JSON: one get() per level, e.g. node.get("address").get("geo").get("lat")
```

### Method 2: JsonPath (`DocumentContext`)
Reads a value with a path. Nested values use dots.
```java
DocumentContext json = JsonPath.parse(body);   // parse once

String email = json.read("email");             // read many times
int count    = json.read("$.length()");        // size of a list response, e.g. GET /users

// nested JSON: one path, e.g. json.read("address.geo.lat")
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

## 8. Request body templates (FreeMarker)

JSON request bodies are **not built in Java code**. Each body lives in a template file under `src/test/resources/templates/`, with placeholders for the values that change.

`templates/createPost.ftl`:
```ftl
{
  "user_id": ${userId?c},
  "title": "${title?json_string}",
  "body": "${body?json_string}"
}
```

In the test, pass the template name and the values as a `Map`. `apiUtils.post(...)` renders the template and sends it:
```java
Map<String, Object> postData = new HashMap<>();
postData.put("userId", userId);
postData.put("title", "OkHttp POST example");
postData.put("body", "Created from PostRequestTest");

response = apiUtils.post("/posts", "createPost.ftl", postData);
```

**Template rules:**
| Value type | Write it as | Why |
|---|---|---|
| String | `"${name?json_string}"` | escapes quotes, backslashes and new lines so the JSON stays valid |
| Number | `${name?c}` | prints `1000`, not `1,000` |
| Boolean | `${name?c}` | prints `true` / `false` |

**Nested JSON bodies:** still use **one flat `Map`**. The nesting lives only in the template, which puts each value in the right place:
```java
data.put("city", "London");
data.put("companyName", "Equal Experts");
```
```ftl
"address": { "city": "${city?json_string}" },
"company": { "name": "${companyName?json_string}" }
```
When two nested objects have a field with the same name (e.g. user `name` and company `name`), give the keys different names in the map, e.g. `name` and `companyName`.

- If a placeholder has no value in the `Map`, `render()` fails straight away. You never send a half-filled body.
- **Adding a new body:** create `templates/<name>.ftl`, then build a `Map` of values and call `apiUtils.post("/path", "<name>.ftl", data)` in your test.

---

## 9. Writing a new test

The framework has two building blocks:

| Class | Role |
|---|---|
| `Base` | Parent class that **every test class extends**. Holds the default objects, so tests never create them. |
| `APIUtils` | All API request methods and response helpers. It builds the URL from `Config.baseUrl()`, sends the request, prints the request and response, and **always closes the real response** so the connection is released. |

API methods return OkHttp's own `okhttp3.Response`. Its body is already held in memory, so you can read it as many times as you need with `apiUtils.getBody(response)`.

**Objects available in every test (from `Base`):**
| Field | Type | Use |
|---|---|---|
| `client` | `OkHttpClient` | shared HTTP client (used by `apiUtils`); adds the `Authorization` header to every request |
| `mapper` | `ObjectMapper` | Jackson: `mapper.readTree(apiUtils.getBody(response))` |
| `faker` | `Faker` | random test data: `faker.name().firstName()`, `faker.internet().emailAddress()` |
| `apiUtils` | `APIUtils` | send requests and read responses |
| `response` | `okhttp3.Response` | the current test's response |
| `path` | `DocumentContext` | JsonPath view of the response: `path = apiUtils.getJsonPath(response)` |

JUnit creates a new test class instance for every `@Test`, so `response` and `path` start empty in each test.

**`APIUtils` methods:**
| Method | Does |
|---|---|
| `apiUtils.get("/path")` | GET request |
| `apiUtils.post("/path", jsonString)` | POST with a raw JSON string |
| `apiUtils.post("/path", "template.ftl", data)` | POST with a body built from a template |
| `apiUtils.delete("/path")` | DELETE request |
| `apiUtils.getBody(response)` | response body as a String (can be called any number of times) |
| `apiUtils.getJsonPath(response)` | JsonPath `DocumentContext` of the body: `.read("email")` |
| `apiUtils.getNewEmail()` | unique email on every call: Faker first.last name + timestamp, e.g. `robin.hamill.20260930183330473@gmail.com` |

**Useful `Response` methods (OkHttp):**
| Method | Returns |
|---|---|
| `response.code()` | HTTP status code, e.g. `200` |
| `response.header("Content-Type")` | any response header |
| `response.isSuccessful()` | `true` for 2xx status codes |

Read the body with `apiUtils.getBody(response)`, not `response.body().string()`. `string()` can only be called once; `getBody()` can be called any number of times.

**Example: a new test class**
```java
public class ActiveUsersTest extends Base {

    @Test
    void getActiveUsers() throws IOException {
        response = apiUtils.get("/users?status=active");

        assertEquals(200, response.code());

        path = apiUtils.getJsonPath(response);
        assertEquals("active", path.read("[0].status"));
    }
}
```

**Conventions:**
- Every test class must `extend Base`. Don't create your own `OkHttpClient`, `ObjectMapper` or `APIUtils`.
- Send requests only through `apiUtils`. Pass the path (e.g. `"/users/1"`); the host always comes from the environment file.
- Put JSON request bodies in `templates/*.ftl`, not in Java strings.
- Use `faker` for values that must be unique or realistic, e.g. `apiUtils.getNewEmail()` for a new email on every run. GoRest rejects duplicate emails.
- **Create your own test data; don't rely on fixed IDs.** GoRest is shared by many people and its data changes all the time, so a test that needs a user creates one first (see `GetRequestTest.getUserById`).
- **Clean up:** delete everything the test created at the end. Delete child records first, e.g. the post before its user.
- GoRest status codes to expect: `200` GET, `201` POST, `204` DELETE (no body), `404` not found, `401` missing or invalid token, `422` validation error (e.g. email already taken).
- For a new HTTP method (PUT, PATCH), add it to `APIUtils` so every test can use it.
- Give test methods clear names that describe the behaviour, e.g. `createGetDeleteUser`.

---

## 10. CI pipeline (GitHub Actions)

The workflow file is `.github/workflows/api-tests.yml`. It runs `mvn test` on JDK 21 (Ubuntu). Maven dependencies are cached between runs.

**One-time setup:** the pipeline reads the GoRest token from a repository secret. In GitHub, go to **Settings → Secrets and variables → Actions → New repository secret**, then add:
- Name: `GO_REST_API_TOKEN`
- Value: your GoRest token

Without it, the tests that create data fail with `401`.

| Trigger | Environment |
|---|---|
| Push to `main` / `master` | `test` |
| Pull request | `test` |
| Manual run: **Actions** tab → **API Tests** → **Run workflow** | choose `test`, `qa` or `staging` |

After each run, download the **surefire-reports-&lt;env&gt;** artifact from the run's summary page to see the detailed test results. The reports are uploaded even when tests fail.
