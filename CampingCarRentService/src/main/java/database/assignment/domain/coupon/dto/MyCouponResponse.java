package database.assignment.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyCouponResponse {
    private Long userCouponId;
    private String name;
    private String discountType;
    private Integer discountValue;
    private Integer minOrderAmount;
    private Integer maxDiscountAmount;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime endDate; //쿠폰 종료 기한
}
