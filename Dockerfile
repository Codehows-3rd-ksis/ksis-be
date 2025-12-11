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

# ChromeDriver 설치 (ENV 제거하고 RUN으로 실행)
RUN CHROME_VERSION=$(apk info -r chromium | grep Version | cut -d' ' -f3 | cut -d'-' -f1) && \
    CHROMEDRIVER_VERSION=120.0.6099.109 && \
    wget -q --continue -P /tmp "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/${CHROMEDRIVER_VERSION}/linux64/chromedriver-linux64.zip" && \
    unzip /tmp/chromedriver-linux64.zip -d /usr/local/bin/ && \
    mv /usr/local/bin/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver && \
    chmod +x /usr/local/bin/chromedriver && \
    rm -rf /tmp/*

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# Selenium 환경변수 설정 (단순 name=value만)
ENV CHROME_BIN=/usr/bin/chromium-browser \
    CHROMEDRIVER_PATH=/usr/local/bin/chromedriver \
    DISPLAY="" \
    WINDOW_SIZE="1920,1080"

# 포트 노출
EXPOSE 8080

# 환경변수 설정
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Chrome 실행 시 headless 옵션 기본 제공 (별도 ENV)
ENV SELENIUM_ARGS="--headless=new --no-sandbox --disable-dev-shm-usage --disable-gpu --disable-extensions"

# 실행 (수정된 ENTRYPOINT)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dwebdriver.chrome.driver=/usr/local/bin/chromedriver -Dchrome.binary=/usr/bin/chromium-browser -jar app.jar"]
