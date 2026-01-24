package com.eatsfine.eatsfine.domain.storetable.validator;

import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.exception.StoreTableException;
import com.eatsfine.eatsfine.domain.storetable.exception.status.StoreTableErrorStatus;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;

import java.util.List;

public class StoreTableValidator {

    private StoreTableValidator() {
        // 인스턴스화 방지
    }

    // 좌석 범위 검증 (최소 좌석 수가 최대 좌석 수보다 클 수 없음)
    public static void validateSeatRange(int minSeatCount, int maxSeatCount) {
        if (minSeatCount > maxSeatCount) {
            throw new StoreTableException(StoreTableErrorStatus._TABLE_INVALID_SEAT_RANGE);
        }
    }

    // 테이블 전체(시작점 + 크기)가 그리드 범위 내에 있는지 검증
    public static void validateGridBounds(int gridX, int gridY, int widthSpan, int heightSpan, TableLayout layout) {
        // 테이블의 끝점 계산 (0-based이므로 -1)
        int endX = gridX + widthSpan - 1;
        int endY = gridY + heightSpan - 1;

        // 시작점이 음수이거나, 끝점이 그리드 범위를 벗어나면 예외
        if (gridX < 0 || gridY < 0 || endX >= layout.getCols() || endY >= layout.getLows()) {
            throw new StoreTableException(StoreTableErrorStatus._TABLE_POSITION_OUT_OF_BOUNDS);
        }
    }

    // 새로 추가할 테이블이 기존 테이블과 겹치지 않는지 확인
    public static void validateNoOverlap(int gridX, int gridY, int widthSpan, int heightSpan,
                                          List<StoreTable> existingTables) {
        for (StoreTable existing : existingTables) {
            if (isOverlapping(gridX, gridY, widthSpan, heightSpan, existing)) {
                throw new StoreTableException(StoreTableErrorStatus._TABLE_POSITION_OVERLAPS);
            }
        }
    }

    // 직사각형 겹침 판정 알고리즘
    private static boolean isOverlapping(int newX, int newY, int newWidth, int newHeight, StoreTable existing) {
        // 새 테이블의 범위
        int newX2 = newX + newWidth - 1;
        int newY2 = newY + newHeight - 1;

        // 기존 테이블의 범위
        int existX1 = existing.getGridX();
        int existY1 = existing.getGridY();
        int existX2 = existing.getGridX() + existing.getWidthSpan() - 1;
        int existY2 = existing.getGridY() + existing.getHeightSpan() - 1;

        // 겹치는 조건: x축도 겹치고 y축도 겹침
        boolean xOverlap = (newX <= existX2) && (newX2 >= existX1);
        boolean yOverlap = (newY <= existY2) && (newY2 >= existY1);

        return xOverlap && yOverlap;
    }
}
