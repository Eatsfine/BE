package com.eatsfine.eatsfine.domain.payment.dto.response;

import java.time.OffsetDateTime;

public record TossPaymentResponse(
                String paymentKey,
                String type,
                String orderId,
                String orderName,
                String mId,
                String currency,
                String method,
                Integer totalAmount,
                Integer balanceAmount,
                String status,
                OffsetDateTime requestedAt,
                OffsetDateTime approvedAt,
                Boolean useEscrow,
                String lastTransactionKey,
                Integer suppliedAmount,
                Integer vat,
                EasyPay easyPay,
                Receipt receipt) {

        public record EasyPay(
                        String provider,
                        Integer amount,
                        Integer discountAmount) {
        }

        public record Receipt(
                        String url) {
        }
}
