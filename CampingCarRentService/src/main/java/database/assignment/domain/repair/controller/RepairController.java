package database.assignment.domain.repair.controller;

import database.assignment.domain.repair.dto.CarRepairInfo;
import database.assignment.domain.repair.service.RepairService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repair")
public class RepairController {
    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    @GetMapping("/history/{carId}")
    public ResponseEntity<List<CarRepairInfo>> getReapirHistory(@PathVariable Long carId){
        return ResponseEntity.ok(repairService.getRepairHistory(carId));
    }
}
