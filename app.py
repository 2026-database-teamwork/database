# 회원가입 
# 1. 회원정보를 받아옴(ID, PW, Nickname)
# 2. 회원정보 비밀번호를 해시함수로 암호화해서 DB에 저장
import sqlite3
from flask import Flask, request, jsonify
from flask_cors import CORS  # 🌟 1. CORS 패키지 가져오기
import hashlib

app = Flask(__name__)
CORS(app)  # 🌟 2. 내 Flask 앱에 CORS 허용 세팅하기 (모든 도메인 허용)
def insert_sample_data():
    conn = sqlite3.connect('rental_system.db')
    cursor = conn.cursor()
    cursor.execute("PRAGMA foreign_keys = ON;")

    # 1. Users 데이터 (Role 포함)
    users_data = [
        ('관리자', '11-11-111111', 'hash_pw_1', '서울시 강남구', '010-1111-1111', 'admin@rental.com', 'none', 'none'),
        ('김철수', '22-22-222222', 'hash_pw_2', '경기도 수원시', '010-2222-2222', 'chulsoo@gmail.com', 'prev_data_01', 'SEDAN'),
        ('이영희', '33-33-333333', 'hash_pw_3', '부산시 해운대구', '010-3333-3333', 'young@naver.com', 'prev_data_02', 'SUV')
    ]
    cursor.executemany('''
        INSERT INTO Users (name, license, password, address, phone, email, dataBeforeCarUse, typeBeforeCarUse)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ''', users_data)

    # 2. Company 데이터
    companies_data = [
        ('현대렌터카', '서울시 서초구', '02-123-4567', '정몽구', 'hyundai@rental.com'),
        ('롯데렌터카', '서울시 송파구', '02-987-6543', '신동빈', 'lotte@rental.com'),
        ('SK렌터카', '서울시 종로구', '02-555-5555', '최태원', 'sk@rental.com')
    ]
    cursor.executemany('''
        INSERT INTO Company (companyName, companyAddress, companyPhone, representativeName, representativeEmail)
        VALUES (?, ?, ?, ?, ?)
    ''', companies_data)

    # 3. Car 데이터 (CompanyId 1:현대, 2:롯데, 3:SK 를 참조)
    cars_data = [
        (1, '아반떼',          '123가 4567', 5, 'avante.jpg', '승차감이 좋은 국민 세단', 50000),
        (1, '팰리세이드',       '98나 7654', 7, 'palisade.jpg', '가족 여행에 최적인 대형 SUV', 120000),
        (2, 'K5',              '55다 5555', 5, 'k5.jpg', '세련된 디자인의 중형 세단', 60000),
        (2, '카니발',           '11라 2222', 9, 'carnival.jpg', '단체 이동에 편리한 미니밴', 150000),
        (3, '아이오닉5',        '77마 8888', 5, 'ioniq5.jpg', '조용한 주행감의 최신 전기차', 90000)
    ]
    
    # app.py 46번째 줄 근처 수정
    cursor.executemany('''
    INSERT OR IGNORE INTO Car (companyId, carName, carNumber, numberOfRider, carImage, carDetail, carRentalCost)
    VALUES (?, ?, ?, ?, ?, ?, ?)''', cars_data)

    conn.commit()
    conn.close()
    print("성공적으로 샘플 데이터가 삽입되었습니다.")
    
def init_db():
    # 1. 데이터베이스 연결 (파일 이름: rental_system.db)
    conn = sqlite3.connect('rental_system.db')
    cursor = conn.cursor()

    # 외래키 제약 조건을 활성화 (SQLite는 기본적으로 꺼져 있는 경우가 많습니다)
    cursor.execute("PRAGMA foreign_keys = ON;")

    # 2. Users 테이블 생성 (사용자 정보)
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Users (
        id                  INTEGER PRIMARY KEY AUTOINCREMENT,
        name                TEXT UNIQUE,  -- 중복 가입 방지를 위해 UNIQUE 권장
        license             TEXT,
        password            TEXT,
        address             TEXT,
        phone               TEXT,
        email               TEXT,
        dataBeforeCarUse    TEXT,
        typeBeforeCarUse    TEXT
    )
    ''')

    # 3. Company 테이블 생성 (회사 정보)
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Company (
        companyId           INTEGER PRIMARY KEY AUTOINCREMENT,
        companyName         TEXT NOT NULL,
        companyAddress      TEXT,
        companyPhone        TEXT,
        representativeName  TEXT,
        representativeEmail TEXT
    )
    ''')

    # 4. Car 테이블 생성 (차량 정보)
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Car (
        carId           INTEGER PRIMARY KEY AUTOINCREMENT,
        companyId       INTEGER,
        carName         TEXT NOT NULL,
        carNumber       TEXT UNIQUE,
        numberOfRider   INTEGER,
        carImage        TEXT,
        carDetail       TEXT,
        carRentalCost   INTEGER,
        FOREIGN KEY (companyId) REFERENCES Company(companyId)
    )
    ''')

    # 5. 변경사항 저장 및 연결 종료
    conn.commit()
    conn.close()
    print("모든 테이블이 성공적으로 생성되었습니다.")

# 회원가입 API
@app.route('/api/auth/register', methods=['POST'])
def api_register():
    name_receive = request.json.get('name')    
    license_receive = request.json.get('license')
    pw_receive = request.json.get('password') 
    address_receive = request.json.get('address')
    phone_receive = request.json.get('phone')
    email_receive = request.json.get('email')
    
    if not all([name_receive, license_receive, pw_receive, address_receive, phone_receive, email_receive]):
        # 🌟 return 형식을 다른 API들과 똑같이 jsonify 구조로 맞춰주어 팅기는 버그를 막았습니다.
        return jsonify({"message": "회원 정보를 모두 입력해주세요."}), 400
    
    pw_hash = hashlib.sha256(pw_receive.encode('utf-8')).hexdigest()
    
    try:
        conn = sqlite3.connect("rental_system.db")
        cursor = conn.cursor()
        cursor.execute("INSERT INTO Users (name,license,password,address,phone,email) VALUES (?, ?, ?, ?, ?, ?)", 
        (name_receive,  license_receive, pw_hash, address_receive, phone_receive, email_receive))
        conn.commit()
        conn.close()
        return jsonify({"message": "회원가입이 완료되었습니다."}), 200
    except sqlite3.IntegrityError:
        return jsonify({"message": "이미 존재하는 아이디입니다."}), 400

# 로그인 API
@app.route('/api/auth/login', methods=['POST'])
def api_login():
    name_receive = request.json.get('name')
    pw_receive = request.json.get('password')

    if not name_receive or not pw_receive:
        return jsonify({"result": "fail", "message": "아이디와 비밀번호를 모두 입력해주세요."}), 400
    print(name_receive)
    pw_hash = hashlib.sha256(pw_receive.encode('utf-8')).hexdigest()

    conn = sqlite3.connect("rental_system.db") 
    cursor = conn.cursor()

    cursor.execute("SELECT password FROM Users WHERE name = ?", (name_receive,))
    user = cursor.fetchone()
    conn.close()

    if user and user[0] == pw_hash:
        return jsonify({"result" : "success", "message": "로그인 성공"}), 200
    else:
        return jsonify({"result": "fail", "message": "아이디 또는 비밀번호가 틀렸습니다."}), 401

# 2~3단계. 지역별 대여회사 조회 통로
@app.route('/api/company/<region>', methods=['GET'])
def get_companies_by_region(region):

    conn = sqlite3.connect("rental_system.db")
    cursor = conn.cursor()
    # :1을 ?로 변경하고, % 와일드카드는 파라미터 쪽에 붙여서 넘깁니다.
    cursor.execute(
    "SELECT companyId, companyName,companyAddress, companyPhone, representativeName, representativeEmail FROM Company WHERE companyAddress LIKE ?", 
    ('%' + region + '%',))
    companies = cursor.fetchall()
    cursor.close()
    conn.close()
    
    result = [{"companyId": c[0], "companyName": c[1],"companyAddress": c[2],"companyPhone": c[3],"representativeName": c[4],"representativeEmail": c[5]} for c in companies]
    return jsonify(result), 200

# 4~5단계. 특정 대여회사의 캠핑카 및 정비정보 조회 통로
@app.route('/api/company/<int:companyId>/cars', methods=['GET'])
def get_cars(companyId):
    print(companyId, "sldkfj")
    conn = sqlite3.connect("rental_system.db")
    cursor = conn.cursor()
    cursor.execute("""
        SELECT carId, companyId ,carName, carNumber, numberOfRider, carImage, carDetail, carRentalCost 
        FROM Car WHERE companyId = :1
    """, (companyId,))
    cars = cursor.fetchall()
    cursor.close()
    conn.close()
    
    result = [{
        "carId": car[0], "companyId" : car[1],  "carName" : car[2],  "carNumber" : car[3],  "numberOfRider" : car[4],  "carImage" : car[5],
        "carDetail" : car[6], "carRentalCost" : car[7]   
    } for car in cars]
    return jsonify(result), 200

if __name__ == '__main__':
    init_db()
    app.run(debug=True, port=8080)