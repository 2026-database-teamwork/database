package database.assignment.domain.rent.controller;


import database.assignment.domain.member.service.MemberService;
import database.assignment.domain.rent.dto.RentalHistoryResponseDto;
import database.assignment.domain.rent.dto.RentalRequestDto;
import database.assignment.domain.rent.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rental")
public class RentalController {
    private final RentalService rentalService;
    private final MemberService memberService;

    public RentalController(RentalService rentalService, MemberService memberService) {
        this.rentalService = rentalService;
        this.memberService = memberService;
    }

    private String getLicense(String username){
        return memberService.getMember(username).getLicense();
    }

    @GetMapping("/history/my")
    public ResponseEntity<List<RentalHistoryResponseDto>> getMyRentals(Authentication authentication){
        String username = authentication.getName();
        return ResponseEntity.ok(rentalService.getMyRentals(getLicense(username)));
    }

    @GetMapping("/history/car/{carId}")
    public ResponseEntity<List<RentalHistoryResponseDto>> getCarRentals(@PathVariable Long carId){
        return ResponseEntity.ok(rentalService.getCarRentals(carId));
    }

    @PostMapping("/rent")
    public ResponseEntity<String> rentalCar(@RequestBody RentalRequestDto rentalRequestDto, Authentication authentication){
        String username = authentication.getName();
        rentalService.rentalCar(rentalRequestDto, getLicense(username));
        return ResponseEntity.ok("예약성공");
    }


}
