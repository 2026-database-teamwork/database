package database.assignment.domain.car.repository;

import database.assignment.domain.car.dto.Car;
import database.assignment.domain.car.dto.CarRegisterDto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CarMemoryRepository implements CarRepository{

    private final Map<Long, Car> carDB = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    public CarMemoryRepository(){
        Car a = new Car();
        a.setCarDetail("독일 최고의 초콜릿 코딩 자동차");
        a.setCarImageUrl("/images/car1.png");
        a.setCarNumber("123en0239");
        a.setCarName("Avantte");
        a.setCarId(sequence.incrementAndGet());
        a.setCarRentalCost(203042);
        a.setCompanyId(1L);
        a.setNumberOfRider(4);
        carDB.put(a.getCarId(), a);
    }

    @Override
    public List<Car> findCarsByCompanyId(Long companyId) {
        return carDB.values().stream()
                .filter(c->c.getCompanyId()==companyId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Car> findByCarNumber(String carNumber){
        return carDB.values().stream()
                .filter(c->c.getCarNumber().equals(carNumber))
                .findFirst();
    }

    @Override
    public Optional<Car> findByCarId(Long carId){
        return carDB.values().stream()
                .filter(c->c.getCarId() == carId)
                .findFirst();
    }

    @Override
    public void createCar(CarRegisterDto carRegisterDto){
        // 1. CarRegisterDto의 알맹이를 꺼내서 새로운 CarDto 객체를 생성하고 조립합니다.
        Car carDto = new Car();

        // 🔑 중요: DB가 없으므로 우리가 직접 고유한 ID를 발급해줍니다.
        carDto.setCarId(sequence.incrementAndGet());

        //나머지 필드 복사
        carDto.setCompanyId((long) carRegisterDto.getCompanyId());
        carDto.setCarName(carRegisterDto.getCarName());
        carDto.setCarNumber(carRegisterDto.getCarNumber());
        carDto.setNumberOfRider(carRegisterDto.getNumberOfRider());
        carDto.setCarImageUrl(carRegisterDto.getCarImageUrl());
        carDto.setCarDetail(carRegisterDto.getCarDetail());
        carDto.setCarRentalCost(carRegisterDto.getCarRentalCost());

        carDB.put(carDto.getCarId(),carDto);
    }
}
