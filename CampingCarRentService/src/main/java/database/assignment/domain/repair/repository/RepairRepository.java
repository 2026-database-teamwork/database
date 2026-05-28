package database.assignment.domain.repair.repository;

import database.assignment.domain.repair.dto.CarRepairInfo;

import java.util.List;

public interface RepairRepository {
    //차의 정비 내역 가져오기
    public List<CarRepairInfo> findRepairHistory(Long carId);
}
