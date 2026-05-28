package database.assignment.domain.repair.service;

import database.assignment.domain.repair.dto.CarRepairInfo;
import database.assignment.domain.repair.repository.RepairRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepairService {
    private final RepairRepository repairRepository;

    public RepairService(RepairRepository repairRepository) {
        this.repairRepository = repairRepository;
    }

    public List<CarRepairInfo> getRepairHistory(Long carId){
        return repairRepository.findRepairHistory(carId);
    }
}
