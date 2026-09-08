#!/bin/bash
# 에러 발생 시 스크립트 실행을 즉시 중단
set -e

cd ~/inventory

set -a
source ~/infra/common.env
set +a
NEW_TAG="${IMAGE_TAG:-latest}"
LAST_GOOD_FILE=".last-good-tag"
OLD_TAG=$(cat "$LAST_GOOD_FILE" 2>/dev/null || echo "latest")

SERVICES=("inventory-1" "inventory-2")
PORTS=("10411" "10412")

EUREKA_URL="http://127.0.0.1:10402/eureka/apps/INVENTORY"
INSTANCE_COUNT="${#SERVICES[@]}"

# 유레카에 UP 인 인스턴스가 몇 개인지 확인
count_up() {
  curl -sf -H "Accept: application/json" "$EUREKA_URL" 2>/dev/null \
    | grep -o '"status":"UP"' | wc -l | tr -d ' '
}

wait_until_all_up() {
  for attempt in {1..40}; do
    if [ "$(count_up)" -ge "$INSTANCE_COUNT" ]; then
      echo "유레카 등록 확인 (UP=${INSTANCE_COUNT})"
      return 0
    fi

    sleep 3
  done

  echo "유레카 등록 타임아웃"
  return 1
}

# 지정한 태그로 전체 인스턴스 롤링 배포, 실패하면 false 반환
deploy_tag() {
  local tag="$1"
  export IMAGE_TAG="$tag"
  docker compose -f compose.yaml pull

  for i in "${!SERVICES[@]}"; do
    SERVICE="${SERVICES[$i]}"
    PORT="${PORTS[$i]}"

    # 유레카 및 라우터에서 해당 인스턴스 제외
    echo "${SERVICE} 유레카 상태 변경 (OUT_OF_SERVICE)"
    curl -sf -X POST -H "Content-Type: application/json" \
      -d '{"status": "OUT_OF_SERVICE"}' \
      "http://127.0.0.1:${PORT}/actuator/serviceregistry"

    echo "${SERVICE} 유레카 갱신 대기 (65초)"
    sleep 65;

    echo "${SERVICE} 재배포 (tag=${tag})"
    docker compose -f compose.yaml up -d --force-recreate "$SERVICE"

    for attempt in {1..30}; do
      if curl -sf "http://127.0.0.1:${PORT}/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
        echo "${SERVICE} 배포 성공"
        break;
      fi

      if [ "$attempt" -eq 30 ]; then
        echo "${SERVICE} 배포 실패 (타임아웃, tag=${tag})"
        return 1;
      fi

      sleep 2;
    done

    wait_until_all_up || return 1

    # 마지막 인스턴스 뒤에는 내릴 대상이 없으므로 기다리지 않음.
    if [ "$i" -lt "$((INSTANCE_COUNT - 1))" ]; then
      echo "게이트웨이 반영 대기 (40초)"
      sleep 40;
    fi
  done

  return 0
}

if deploy_tag "$NEW_TAG"; then
  echo "$NEW_TAG" > "$LAST_GOOD_FILE"
  docker image prune -f
else
  echo "새 버전(${NEW_TAG}) 배포 실패, 이전 버전(${OLD_TAG})으로 롤백"
  deploy_tag "$OLD_TAG" || echo "롤백도 실패함, 수동 확인 필요"
  docker image prune -f
  exit 1
fi
