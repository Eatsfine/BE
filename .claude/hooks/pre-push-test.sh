#!/bin/bash
# Claude Code PreToolUse 훅: git push 전 테스트 자동 실행
# stdin으로 Bash 도구의 입력 JSON을 받아 push 명령인지 확인 후 테스트를 실행합니다.

INPUT=$(cat)

# Bash 도구 입력에서 실행 명령어 추출
BASH_CMD=$(python3 -c "
import sys, json
try:
    data = json.loads(sys.stdin.read())
    print(data.get('command', ''))
except Exception:
    print('')
" <<< "$INPUT")

# git push 명령이 아니면 즉시 통과
if ! echo "$BASH_CMD" | grep -qE '(^|&&|\|\|)\s*git push'; then
    exit 0
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  git push 감지 — 테스트를 먼저 실행합니다"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

PROJECT_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$PROJECT_ROOT"

if ./gradlew test 2>&1; then
    echo ""
    echo "✅ 테스트 통과 — push를 진행합니다."
    exit 0
else
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  ❌ 테스트 실패 — push가 차단되었습니다"
    echo "  실패한 테스트를 수정한 후 다시 시도하세요."
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    exit 2
fi
