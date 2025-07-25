#!/bin/bash

#########################################
# [1] 배포에 필요한 변수 설정
#########################################
EC2_USER=ubuntu
EC2_HOST=3.39.102.137
EC2_KEY=/c/pem/login-server-key-pair.pem
JAR_FILE=build/libs/login-0.0.1-SNAPSHOT.jar
REMOTE_DIR=/home/ubuntu/app
REMOTE_JAR_NAME=login-0.0.1-SNAPSHOT.jar
DOMAIN_NAME=skc05096.o-r.kr
ENV_FILE=.env

#########################################
# [2] Spring Boot JAR 파일 빌드
#########################################
echo "📦 JAR 빌드 중..."
./gradlew clean build -x test || { echo "❌ JAR 빌드 실패"; exit 1; }
echo "🧹 .env 줄바꿈(LF) 변환 중..."
sed -i 's/\r$//' "$ENV_FILE"
#########################################
# [3] EC2에 JAR + .env 전송
#########################################
echo "🚀 EC2로 파일 전송 중..."
scp -i "$EC2_KEY" "$JAR_FILE" "$ENV_FILE" $EC2_USER@$EC2_HOST:$REMOTE_DIR/ || { echo "❌ 전송 실패"; exit 1; }

#########################################
# [4] EC2 기존 프로세스 종료 및 실행
#########################################
echo "🛑 기존 프로세스 종료 중..."
ssh -i "$EC2_KEY" $EC2_USER@$EC2_HOST "pkill -f $REMOTE_JAR_NAME || true"

echo "🔄 EC2에서 서버 실행 중..."
ssh -i "$EC2_KEY" $EC2_USER@$EC2_HOST <<EOF
  bash -c '
    cd $REMOTE_DIR
    set -a
    . .env
    set +a
    nohup java -jar $REMOTE_JAR_NAME > log.txt 2>&1 &
    echo "✅ 백그라운드 실행 완료 (log.txt 기록)"
  '
EOF

#########################################
# [5] EC2에서 Java 프로세스 확인
#########################################
echo "📋 EC2에서 실행 중인 Java 프로세스:"
ssh -i "$EC2_KEY" $EC2_USER@$EC2_HOST "ps aux | grep java | grep -v grep"
echo "📄 최근 서버 로그 (상위 20줄):"
ssh -i "$EC2_KEY" $EC2_USER@$EC2_HOST "tail -n 20 $REMOTE_DIR/log.txt"
#########################################
# [6] 접속 안내
#########################################
echo "✅ 배포 완료! 접속: https://$DOMAIN_NAME"
