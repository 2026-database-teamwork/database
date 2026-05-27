import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { initializeDb, executeQuery } from './db.js';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(express.json());

// Logger middleware
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

// Database state tracker
let dbConnected = false;

// Case-insensitive case-robust row value extractor
const getVal = (row, key) => {
  if (!row) return undefined;
  const upper = key.toUpperCase();
  const lower = key.toLowerCase();
  if (row[key] !== undefined) return row[key];
  if (row[upper] !== undefined) return row[upper];
  if (row[lower] !== undefined) return row[lower];
  return undefined;
};

// In-memory data store for fallback/demo mode when Oracle DB is disconnected
const mockData = {
  users: [
    { license: '12-34-56', name: 'test', password: 'password', address: 'Seoul', phone: '010-1234-5678', email: 'test@test.com' }
  ],
  companies: [
    { companyId: 1, companyName: '서울 캠핑 스타', companyAddress: '서울특별시 강남구 테헤란로 123', companyPhone: '02-123-4567', representativeName: '김서울', representativeEmail: 'seoul_star@camping.com' },
    { companyId: 2, companyName: '메트로 캠핑 서울', companyAddress: '서울특별시 마포구 월드컵북로 45', companyPhone: '02-987-6543', representativeName: '이서울', representativeEmail: 'metro_seoul@camping.com' },
    { companyId: 3, companyName: '대전 한빛 캠핑카', companyAddress: '대전광역시 유성구 대학로 99', companyPhone: '042-555-1234', representativeName: '박대전', representativeEmail: 'hanbit_dj@camping.com' },
    { companyId: 4, companyName: '제주 바람 캠핑카', companyAddress: '제주특별자치도 제주시 공항로 1', companyPhone: '064-777-8888', representativeName: '최제주', representativeEmail: 'wind_jeju@camping.com' },
    { companyId: 5, companyName: '경기 숲속 캠핑', companyAddress: '경기도 수원시 팔달구 효원로 200', companyPhone: '031-222-3333', representativeName: '정경기', representativeEmail: 'forest_gg@camping.com' },
    { companyId: 6, companyName: '부산 바다 캠핑', companyAddress: '부산광역시 해운대구 해운대해변로 292', companyPhone: '051-888-9999', representativeName: '강부산', representativeEmail: 'sea_busan@camping.com' }
  ],
  cars: [
    { carId: 1, companyId: 1, carName: '포레스트 프리미엄', carNumber: '12가 3456', numberOfRider: 4, carDetail: '최신형 프리미엄 캠핑카로 넓은 실내 공간과 최고급 빌트인 가구, 풀옵션 주방 시스템 및 개별 샤워실을 완비하고 있어 4인 가족 여행에 최적화된 모델입니다.', carRentalCost: 250000, carImage: 'https://images.unsplash.com/photo-1513313778780-9ae4807465f0?auto=format&fit=crop&q=80&w=600' },
    { carId: 2, companyId: 1, carName: '컴팩트 마실', carNumber: '34러 7890', numberOfRider: 2, carDetail: '연인들을 위한 아담하고 아늑한 2인승 캠핑카입니다. 실용적인 레이아웃과 감성적인 무드등이 탑재되어 조용하고 낭만적인 차박 여행에 안성맞춤입니다.', carRentalCost: 150000, carImage: 'https://images.unsplash.com/photo-1523987355523-c7b5b0dd90a7?auto=format&fit=crop&q=80&w=600' },
    { carId: 3, companyId: 2, carName: '어반 오디세이', carNumber: '56배 1357', numberOfRider: 3, carDetail: '도심 탈출을 꿈꾸는 직장인들을 위한 스타일리시한 캠핑카. 탁월한 연비와 주행 편의성을 갖추고 있으며 루프탑 텐트가 기본 장착되어 있습니다.', carRentalCost: 180000, carImage: 'https://images.unsplash.com/photo-1496302661278-537aa01e2db5?auto=format&fit=crop&q=80&w=600' },
    { carId: 4, companyId: 3, carName: '대전 한빛 1호', carNumber: '78고 2468', numberOfRider: 5, carDetail: '5인 탑승 및 취침이 가능한 대형 모터홈입니다. 넓은 리빙룸 영역과 태양광 충전 시스템이 내장되어 있어 노지 캠핑 시 전력 걱정 없이 편리한 캠핑이 가능합니다.', carRentalCost: 280000, carImage: 'https://images.unsplash.com/photo-1527689368864-3a821dbccc34?auto=format&fit=crop&q=80&w=600' },
    { carId: 5, companyId: 4, carName: '제주 바람 스테이', carNumber: '90너 3579', numberOfRider: 4, carDetail: '제주도의 푸른 바다와 산바다를 만끽하기에 완벽한 오프로드 주행 가능 캠핑카입니다. 야외 테라스용 사이드 어닝과 감성 가득한 나무 인테리어가 매력적입니다.', carRentalCost: 220000, carImage: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?auto=format&fit=crop&q=80&w=600' },
    { carId: 6, companyId: 5, carName: '네이처 스위트', carNumber: '12오 9876', numberOfRider: 4, carDetail: '자연 친화적인 우드 톤의 럭셔리 모터홈입니다. 넓은 수납공간과 안락한 침대 매트리스가 구비되어 있어 장기 캠핑 여행을 준비하시는 가족단위 고객에게 강추합니다.', carRentalCost: 260000, carImage: 'https://images.unsplash.com/photo-1534088568595-a066f410bcda?auto=format&fit=crop&q=80&w=600' },
    { carId: 7, companyId: 6, carName: '블루 오션', carNumber: '34조 5432', numberOfRider: 2, carDetail: '바다가 보이는 언덕 위 차박에 완벽하게 튜닝된 미니밴 캠핑카입니다. 차량 후면 전체가 통창 느낌으로 개방되어 멋진 오션뷰를 침대에서 감상할 수 있습니다.', carRentalCost: 160000, carImage: 'https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&q=80&w=600' }
  ],
  coupons: [
    { couponId: 1, couponName: '가입 환영 10% 할인쿠폰', couponCode: 'WELCOME10', discountType: 'percent', discountValue: 10, minOrderAmount: 0 },
    { couponId: 2, couponName: '주말 특별 ₩30,000 할인쿠폰', couponCode: 'WEEKEND30K', discountType: 'amount', discountValue: 30000, minOrderAmount: 100000 },
    { couponId: 3, couponName: '첫 렌트 보너스 ₩10,000 할인쿠폰', couponCode: 'FIRST10K', discountType: 'amount', discountValue: 10000, minOrderAmount: 0 },
    { couponId: 4, couponName: '무제한 5% 할인쿠폰', couponCode: 'ALWAYS5', discountType: 'percent', discountValue: 5, minOrderAmount: 0 }
  ],
  userCoupons: [], // schema: { userCouponId, username, couponId, isUsed, issuedAt }
  rentals: [] // schema: { rentalId, carId, license, companyId, startDateTime, endDateTime, totalCost }
};

// Initialize starter coupons for 'test' user in mock data
mockData.userCoupons = mockData.coupons.map((c, i) => ({
  userCouponId: i + 1,
  username: 'test',
  couponId: c.couponId,
  isUsed: false,
  issuedAt: new Date().toISOString()
}));

// Helper to get license from username
async function getLicenseByUsername(username) {
  if (dbConnected) {
    try {
      const sql = 'SELECT license FROM USERS WHERE name = :username';
      const result = await executeQuery(sql, { username });
      if (result.rows.length > 0) {
        return getVal(result.rows[0], 'license');
      }
    } catch (err) {
      console.error('Error in getLicenseByUsername:', err.message);
    }
    return null;
  } else {
    const user = mockData.users.find(u => u.name === username);
    return user ? user.license : null;
  }
}

// Helper to auto-issue coupons if a user has none (Works for both DB and Mock modes)
async function ensureUserHasCoupons(username) {
  const license = await getLicenseByUsername(username);
  if (!license) return;

  if (dbConnected) {
    try {
      const checkSql = 'SELECT COUNT(*) AS CNT FROM USER_COUPONS WHERE username = :username';
      const checkResult = await executeQuery(checkSql, { username });
      const row = checkResult.rows[0];
      const count = row ? (row.CNT !== undefined ? row.CNT : (row.cnt !== undefined ? row.cnt : 0)) : 0;

      if (Number(count) === 0) {
        console.log(`Auto-issuing database coupons for user: ${username}`);
        const couponsResult = await executeQuery('SELECT coupon_id FROM COUPON', []);
        for (const r of couponsResult.rows) {
          const couponId = r.COUPON_ID || r.coupon_id;
          await executeQuery(
            'INSERT INTO USER_COUPONS (username, coupon_id, is_used, issued_at) VALUES (:username, :couponId, 0, SYSDATE)',
            { username, couponId }
          );
        }
      }
    } catch (err) {
      console.error('Error auto-issuing coupons in DB:', err.message);
    }
  } else {
    // Mock mode auto-issue
    const userCoupons = mockData.userCoupons.filter(uc => uc.username === username);
    if (userCoupons.length === 0) {
      console.log(`Auto-issuing mock coupons for user: ${username}`);
      mockData.coupons.forEach((c) => {
        mockData.userCoupons.push({
          userCouponId: mockData.userCoupons.length + 1,
          username: username,
          couponId: c.couponId,
          isUsed: false,
          issuedAt: new Date().toISOString()
        });
      });
    }
  }
}

// 1. Authentication Endpoints
app.post('/api/auth/login', async (req, res) => {
  const { name, password } = req.body;
  if (!name || !password) {
    return res.status(400).json({ result: 'fail', message: '아이디와 비밀번호를 입력해주세요.' });
  }

  if (dbConnected) {
    try {
      const sql = 'SELECT name, password, license FROM USERS WHERE name = :name';
      const result = await executeQuery(sql, { name });
      
      if (result.rows.length > 0) {
        const user = result.rows[0];
        const dbPassword = getVal(user, 'password');
        if (dbPassword === password) {
          await ensureUserHasCoupons(name);
          return res.json({ result: 'success', message: '로그인 성공' });
        }
      }
      return res.status(401).json({ result: 'fail', message: '아이디 또는 비밀번호가 틀렸습니다.' });
    } catch (err) {
      console.error('Login error:', err);
      return res.status(500).json({ result: 'fail', message: '로그인 처리 중 서버 에러가 발생했습니다.' });
    }
  } else {
    // Fallback Mock Mode
    const user = mockData.users.find(u => u.name === name && u.password === password);
    if (user) {
      await ensureUserHasCoupons(name);
      return res.json({ result: 'success', message: '로그인 성공' });
    }
    return res.status(401).json({ result: 'fail', message: '아이디 또는 비밀번호가 틀렸습니다.' });
  }
});

app.post('/api/auth/join', async (req, res) => {
  const { name, license, password, address, phone, email } = req.body;
  if (!name || !password) {
    return res.status(400).json({ result: 'fail', message: '필수 가입 정보(이름, 비밀번호)가 누락되었습니다.' });
  }

  if (dbConnected) {
    try {
      const checkSql = 'SELECT name FROM USERS WHERE name = :name';
      const checkResult = await executeQuery(checkSql, { name });
      if (checkResult.rows.length > 0) {
        return res.status(400).json({ result: 'fail', message: '이미 존재하는 사용자 이름입니다.' });
      }

      const insertSql = `
        INSERT INTO USERS (license, name, password, address, phone, email)
        VALUES (:license, :name, :password, :address, :phone, :email)
      `;
      await executeQuery(insertSql, { license, name, password, address, phone, email });
      await ensureUserHasCoupons(name);

      return res.status(201).json({ result: 'success', message: '회원가입 완료' });
    } catch (err) {
      console.error('Registration error:', err);
      return res.status(500).json({ result: 'fail', message: '회원가입 처리 중 서버 에러가 발생했습니다.' });
    }
  } else {
    // Fallback Mock Mode
    const exists = mockData.users.some(u => u.name === name);
    if (exists) {
      return res.status(400).json({ result: 'fail', message: '이미 존재하는 사용자 이름입니다.' });
    }
    mockData.users.push({ license, name, password, address, phone, email });
    await ensureUserHasCoupons(name);
    return res.status(201).json({ result: 'success', message: '회원가입 완료' });
  }
});

// 2. Company & Vehicle Endpoints
app.get('/api/company/:region', async (req, res) => {
  const { region } = req.params;
  if (dbConnected) {
    try {
      const sql = `
        SELECT 
          companyId AS "companyId", 
          companyName AS "companyName", 
          companyAddress AS "companyAddress", 
          companyPhone AS "companyPhone", 
          representativeEmail AS "representativeEmail", 
          representativeName AS "representativeName"
        FROM COMPANY 
        WHERE companyAddress LIKE '%' || :region || '%'
      `;
      const result = await executeQuery(sql, { region });
      return res.json(result.rows);
    } catch (err) {
      console.error('Fetch companies error:', err);
      return res.status(500).json({ message: '회사 목록 조회 중 에러가 발생했습니다.' });
    }
  } else {
    // Fallback Mock Mode
    const filtered = mockData.companies.filter(c => c.companyAddress.includes(region));
    return res.json(filtered);
  }
});

app.get('/api/company/:companyId/cars', async (req, res) => {
  const { companyId } = req.params;
  if (dbConnected) {
    try {
      const sql = `
        SELECT 
          carId AS "carId", 
          companyId AS "companyId", 
          carName AS "carName", 
          carNumber AS "carNumber", 
          numberOfRider AS "numberOfRider", 
          carDetail AS "carDetail", 
          carRentalCost AS "carRentalCost", 
          carImage AS "carImageUrl" 
        FROM CAR 
        WHERE companyId = :companyId
      `;
      const result = await executeQuery(sql, { companyId: Number(companyId) });
      return res.json(result.rows);
    } catch (err) {
      console.error('Fetch cars error:', err);
      return res.status(500).json({ message: '차량 목록 조회 중 에러가 발생했습니다.' });
    }
  } else {
    // Fallback Mock Mode
    const filtered = mockData.cars.filter(c => c.companyId === Number(companyId));
    return res.json(filtered.map(c => ({
      carId: c.carId,
      companyId: c.companyId,
      carName: c.carName,
      carNumber: c.carNumber,
      numberOfRider: c.numberOfRider,
      carDetail: c.carDetail,
      carRentalCost: c.carRentalCost,
      carImageUrl: c.carImage
    })));
  }
});

// 3. Coupon Endpoints
app.get('/api/coupons/my', async (req, res) => {
  const { username } = req.query;
  if (!username) {
    return res.status(400).json({ message: '사용자 이름이 누락되었습니다.' });
  }

  // Ensure coupons are issued first
  await ensureUserHasCoupons(username);

  if (dbConnected) {
    try {
      const sql = `
        SELECT 
          uc.user_coupon_id AS "userCouponId",
          c.coupon_name AS "couponName",
          c.coupon_code AS "couponCode",
          c.discount_type AS "discountType",
          c.discount_value AS "discountValue",
          c.min_order_amount AS "minOrderAmount",
          uc.is_used AS "isUsed"
        FROM USER_COUPONS uc
        JOIN COUPON c ON uc.coupon_id = c.coupon_id
        WHERE uc.username = :username AND uc.is_used = 0
      `;
      const result = await executeQuery(sql, { username });
      
      const coupons = result.rows.map(row => ({
        userCouponId: Number(getVal(row, 'userCouponId')),
        couponName: getVal(row, 'couponName'),
        couponCode: getVal(row, 'couponCode'),
        discountType: getVal(row, 'discountType'),
        discountValue: Number(getVal(row, 'discountValue')),
        minOrderAmount: Number(getVal(row, 'minOrderAmount') || 0),
        isUsed: getVal(row, 'isUsed') === 1
      }));
      
      return res.json(coupons);
    } catch (err) {
      console.error('Fetch coupons error:', err);
      return res.status(500).json({ message: '쿠폰 목록 조회 중 에러가 발생했습니다.' });
    }
  } else {
    // Fallback Mock Mode
    const userCoupons = mockData.userCoupons.filter(uc => uc.username === username && !uc.isUsed);
    const result = userCoupons.map(uc => {
      const coupon = mockData.coupons.find(c => c.couponId === uc.couponId);
      if (!coupon) return null;
      return {
        userCouponId: uc.userCouponId,
        couponName: coupon.couponName,
        couponCode: coupon.couponCode,
        discountType: coupon.discountType,
        discountValue: Number(coupon.discountValue),
        minOrderAmount: Number(coupon.minOrderAmount || 0),
        isUsed: uc.isUsed
      };
    }).filter(Boolean);
    return res.json(result);
  }
});

// 4. Rental/Booking Endpoints
app.post('/api/rental/rent', async (req, res) => {
  const { carId, companyId, username, startDateTime, endDateTime, totalCost, userCouponId } = req.body;
  if (!carId || !companyId || !username || !startDateTime || !endDateTime || totalCost === undefined) {
    return res.status(400).json({ message: '필수 예약 정보가 누락되었습니다.' });
  }

  if (dbConnected) {
    try {
      const insertSql = `
        INSERT INTO RENTAL (carId, companyId, username, startDateTime, endDateTime, totalCost, user_coupon_id)
        VALUES (:carId, :companyId, :username, 
                TO_DATE(:startDateTime, 'YYYY-MM-DD"T"HH24:MI:SS'), 
                TO_DATE(:endDateTime, 'YYYY-MM-DD"T"HH24:MI:SS'), 
                :totalCost, :userCouponId)
      `;
      
      await executeQuery(insertSql, {
        carId: Number(carId),
        companyId: Number(companyId),
        username,
        startDateTime,
        endDateTime,
        totalCost: Number(totalCost),
        userCouponId: userCouponId ? Number(userCouponId) : null
      });

      if (userCouponId) {
        const updateCouponSql = `
          UPDATE USER_COUPONS 
          SET is_used = 1, used_at = SYSDATE
          WHERE user_coupon_id = :userCouponId
        `;
        await executeQuery(updateCouponSql, { userCouponId: Number(userCouponId) });
      }

      return res.status(201).json({ message: '예약이 성공적으로 완료되었습니다.' });
    } catch (err) {
      console.error('Create rental error:', err);
      return res.status(500).json({ message: '예약 처리 중 에러가 발생했습니다.' });
    }
  } else {
    // Fallback Mock Mode
    const newRental = {
      rentalId: mockData.rentals.length + 1,
      carId: Number(carId),
      companyId: Number(companyId),
      username,
      startDateTime,
      endDateTime,
      totalCost: Number(totalCost),
      userCouponId: userCouponId ? Number(userCouponId) : null
    };
    mockData.rentals.push(newRental);

    if (userCouponId) {
      const uc = mockData.userCoupons.find(uc => uc.userCouponId === Number(userCouponId));
      if (uc) {
        uc.isUsed = true;
      }
    }
    return res.status(201).json({ message: '예약이 성공적으로 완료되었습니다.' });
  }
});

app.get('/api/rental/history/my', async (req, res) => {
  const { username } = req.query;
  if (!username) {
    return res.status(400).json({ message: '사용자 이름이 누락되었습니다.' });
  }

  if (dbConnected) {
    try {
      const sql = `
        SELECT 
          r.rentalId AS "rentalId",
          c.carName AS "carName",
          comp.companyName AS "companyName",
          c.carNumber AS "carNumber",
          TO_CHAR(r.startDateTime, 'YYYY-MM-DD"T"HH24:MI:SS') AS "startDateTime",
          TO_CHAR(r.endDateTime, 'YYYY-MM-DD"T"HH24:MI:SS') AS "endDateTime",
          r.totalCost AS "totalCost"
        FROM RENTAL r
        JOIN CAR c ON r.carId = c.carId
        JOIN COMPANY comp ON r.companyId = comp.companyId
        WHERE r.username = :username
        ORDER BY r.rentalId DESC
      `;
      const result = await executeQuery(sql, { username });
      
      const history = result.rows.map(row => ({
        rentalId: getVal(row, 'rentalId'),
        carName: getVal(row, 'carName'),
        companyName: getVal(row, 'companyName'),
        license: getVal(row, 'license'),
        startDateTime: getVal(row, 'startDateTime'),
        endDateTime: getVal(row, 'endDateTime'),
        totalCost: getVal(row, 'totalCost')
      }));

      return res.json(history);
    } catch (err) {
      console.error('Fetch rental history error:', err);
      return res.status(500).json({ message: '예약 내역 조회 중 에러가 발생했습니다.' });
    }
  } else {
    // Fallback Mock Mode
    const userRentals = mockData.rentals.filter(r => r.username === username);
    const history = userRentals.map(r => {
      const car = mockData.cars.find(c => c.carId === r.carId);
      const company = mockData.companies.find(comp => comp.companyId === r.companyId);
      return {
        rentalId: r.rentalId,
        carName: car ? car.carName : '알 수 없는 차량',
        companyName: company ? company.companyName : '알 수 없는 업체',
        license: car ? car.carNumber : '임시 번호',
        startDateTime: r.startDateTime,
        endDateTime: r.endDateTime,
        totalCost: r.totalCost
      };
    });
    return res.json(history.reverse());
  }
});

app.get('/api/rental/history/car/:carId', async (req, res) => {
  const { carId } = req.params;
  if (dbConnected) {
    try {
      const sql = `
        SELECT 
          TO_CHAR(start_date_time, 'YYYY-MM-DD"T"HH24:MI:SS') AS "startDateTime",
          TO_CHAR(end_date_time, 'YYYY-MM-DD"T"HH24:MI:SS') AS "endDateTime"
        FROM RENTAL
        WHERE car_id = :carId
      `;
      const result = await executeQuery(sql, { carId: Number(carId) });

      const bookedRanges = result.rows.map(row => ({
        startDateTime: getVal(row, 'startDateTime'),
        endDateTime: getVal(row, 'endDateTime')
      }));

      return res.json(bookedRanges);
    } catch (err) {
      console.error('Fetch car booking ranges error:', err);
      return res.status(500).json({ message: '차량 예약 일정 조회 중 에러가 발생했습니다.' });
    }
  } else {
    // Fallback Mock Mode
    const carRentals = mockData.rentals.filter(r => r.carId === Number(carId));
    const bookedRanges = carRentals.map(r => ({
      startDateTime: r.startDateTime,
      endDateTime: r.endDateTime
    }));
    return res.json(bookedRanges);
  }
});

// Start Express Server and attempt DB initialization
app.listen(PORT, async () => {
  console.log(`================================================================`);
  console.log(` Camping Car Rental Server running on http://localhost:${PORT}`);
  console.log(`================================================================`);
  try {
    await initializeDb();
    dbConnected = true;
  } catch (dbErr) {
    console.warn('\n⚠️  WARNING: Running server in local OFFLINE/DEMO fallback mode.');
    console.log('To use Oracle Database features, please configure "server/.env" correctly.');
    console.log('Ensure you execute "server/schema.sql" in your Oracle client first.');
    console.log('================================================================\n');
  }
});
