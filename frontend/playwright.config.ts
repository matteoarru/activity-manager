import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  reporter: "list",
  use: { baseURL: "http://127.0.0.1:5173", trace: "retain-on-failure", ...devices["Desktop Chrome"], launchOptions: { executablePath: "/usr/bin/google-chrome" } },
  webServer: [
    { command: "npm run dev -- --host 127.0.0.1", cwd: ".", url: "http://127.0.0.1:5173", reuseExistingServer: true, timeout: 120_000 },
    { command: "JAVA_HOME=${JAVA_HOME:-/home/matteo/.jdk/jdk-25.0.2} PATH=${JAVA_HOME:-/home/matteo/.jdk/jdk-25.0.2}/bin:/home/matteo/.maven/maven-3.9.16/bin:$PATH mvn -q -f backend/api/pom.xml org.springframework.boot:spring-boot-maven-plugin:4.1.0:run", cwd: "..", url: "http://127.0.0.1:8080/api/health", reuseExistingServer: true, timeout: 120_000 },
  ],
});
