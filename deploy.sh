#!/bin/bash
# =============================
# AWS Lightsail 배포 스크립트
# =============================
LIGHTSAIL_IP="여기에_서버_IP"         # 예: 13.125.xxx.xxx
LIGHTSAIL_USER="ubuntu"
KEY_PATH="여기에_PEM_키_경로"          # 예: ~/.ssh/lightsail-key.pem  또는  C:/Users/SBS/.ssh/lightsail-key.pem
JAR_NAME="board-0.0.1-SNAPSHOT.jar"
REMOTE_DIR="/home/ubuntu/app"

# =============================
# 1. 로컬 빌드
# =============================
echo "[1/3] JAR 빌드 중..."
mvn clean package -DskipTests -q

if [ $? -ne 0 ]; then
  echo "빌드 실패! 배포를 중단합니다."
  exit 1
fi

echo "      빌드 완료 → target/$JAR_NAME"

# =============================
# 2. 서버에 JAR 전송
# =============================
echo "[2/3] 서버에 JAR 전송 중..."
ssh -i $KEY_PATH $LIGHTSAIL_USER@$LIGHTSAIL_IP "mkdir -p $REMOTE_DIR"
scp -i $KEY_PATH target/$JAR_NAME $LIGHTSAIL_USER@$LIGHTSAIL_IP:$REMOTE_DIR/app.jar

if [ $? -ne 0 ]; then
  echo "전송 실패! 서버 IP와 키 경로를 확인하세요."
  exit 1
fi

echo "      전송 완료"

# =============================
# 3. 앱 재시작
# =============================
echo "[3/3] 앱 재시작 중..."
ssh -i $KEY_PATH $LIGHTSAIL_USER@$LIGHTSAIL_IP "
  # 기존 프로세스 종료
  pkill -f 'java -jar' 2>/dev/null || true
  sleep 2

  # 백그라운드로 실행 (로그는 app.log에 저장)
  nohup java -jar $REMOTE_DIR/app.jar \
    --spring.profiles.active=prod \
    > $REMOTE_DIR/app.log 2>&1 &

  echo \"PID: \$!\"
"

echo ""
echo "배포 완료! http://$LIGHTSAIL_IP:8080"
echo "로그 확인: ssh -i $KEY_PATH $LIGHTSAIL_USER@$LIGHTSAIL_IP 'tail -f ~/app/app.log'"
