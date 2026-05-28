package database.assignment.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserCoupon {
    private Long userCouponId;
    private String license;
    private Long couponId;
    private String status;
    private LocalDateTime issuedAt; //발급일
    private LocalDateTime usedAt;
}
