# 빌드 스테이지 (누락된 부분 추가!)
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew ./

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

# 런타임 스테이지 (Selenium용 Chrome 포함)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Chrome 및 의존성 설치
RUN apk update && apk add --no-cache \
    chromium \
    nss \
    freetype \
    freetype-dev \
    harfbuzz \
    ca-certificates \
    ttf-freefont \
    && rm -rf /var/cache/apk/*

# ChromeDriver 설치 (고정 버전 사용 - 안정적)
RUN CHROMEDRIVER_VERSION=120.0.6099.109 && \
    wget -q --continue -P /tmp "https://storage.googleapis.com/chrome-for-testing-public/${CHROMEDRIVER_VERSION}/linux64/chromedriver-linux64.zip" && \
    unzip /tmp/chromedriver-linux64.zip -d /usr/local/bin/ && \
    mv /usr/local/bin/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver && \
    chmod +x /usr/local/bin/chromedriver && \
    rm -rf /tmp/*

# 빌드된 JAR 파일 복사 (이제 builder 참조 가능)
COPY --from=builder /app/build/libs/*.jar app.jar

# Selenium 환경변수 설정
ENV CHROME_BIN=/usr/bin/chromium-browser \
    CHROMEDRIVER_PATH=/usr/local/bin/chromedriver \
    DISPLAY="" \
    WINDOW_SIZE="1920,1080"

# 포트 노출
EXPOSE 8080

# 환경변수 설정
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Chrome 실행 시 headless 옵션
ENV SELENIUM_ARGS="--headless=new --no-sandbox --disable-dev-shm-usage --disable-gpu --disable-extensions"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dwebdriver.chrome.driver=/usr/local/bin/chromedriver -Dchrome.binary=/usr/bin/chromium-browser -jar app.jar"]
