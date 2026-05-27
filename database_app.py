# oracle 연결
import oracledb
from flask import Flask, request, jsonify
from flask_cors import CORS  # 1. CORS 패키지 가져오기
import hashlib

app = Flask(__name__)
CORS(app)  # 2. 내 Flask 앱에 CORS 허용 세팅하기 (모든 도메인 허용)
def get_oracledb_connect():
    return oracledb.connect(
        user="C##campingcar_data",
        password="1234",
        dsn = "localhost:1521/free"
    )

         

# 회원가입 API
@app.route('/api/auth/join', methods=['POST'])
def api_register():
    data = request.get_json(silent=True) or {}
    name_receive = data.get('name')    
    license_receive = data.get('license')
    pw_receive = data.get('password') 
    address_receive = data.get('address')
    phone_receive = data.get('phone')
    email_receive = data.get('email')
    
    if not all([name_receive, license_receive, pw_receive, address_receive, phone_receive, email_receive]):
        return jsonify({"message": "회원 정보를 모두 입력해주세요."}), 400
    
    pw_hash = hashlib.sha256(pw_receive.encode('utf-8')).hexdigest()
    
    conn = None
    try:
        conn = get_oracledb_connect()
        cursor = conn.cursor()
        
        # 1. 유저 삽입
        cursor.execute("""
            INSERT INTO USERS (name, license, password, address, phone, email) 
            VALUES (:1, :2, :3, :4, :5, :6)
        """, (name_receive, license_receive, pw_hash, address_receive, phone_receive, email_receive))
        
        # 2. 웰컴 쿠폰 발급 (COUPONS 테이블에 coupon_id=1이 반드시 인서트 되어있어야 함)
        cursor.execute("""
            INSERT INTO USERCOUPONS (user_coupon_id, license, coupon_id, status, issued_at, used_at)
            VALUES (user_coupons_seq.NEXTVAL, :1, 1, 'unused', SYSDATE, NULL)           
        """, (license_receive,))
        
        conn.commit()
        cursor.close()
        return jsonify({"message": "회원가입이 완료되었습니다. 웰컴 쿠폰이 발급되었습니다."}), 200

    except oracledb.IntegrityError as e:
        if conn: conn.rollback()
        print(f"중복 에러 발생: {e}")
        return jsonify({"message": "이미 존재하는 면허번호(아이디) 또는 정보입니다."}), 400

    except oracledb.DatabaseError as e:
        # 🌟 여기에 잡힌다면 십중팔구 COUPONS 테이블에 1번 데이터가 없어서 나는 외래키 에러입니다.
        if conn: conn.rollback()
        print(f"❌ 데이터베이스 상세 에러 로그: {e}")
        return jsonify({"message": "데이터베이스 등록 중 오류가 발생했습니다.", "error": str(e)}), 500

    finally:
        if conn:
            conn.close()
# 로그인 API
@app.route('/api/auth/login', methods=['POST'])
def api_login():
    name_receive = request.json.get('name')
    pw_receive = request.json.get('password')

    if not name_receive or not pw_receive:
        return jsonify({"result": "fail", "message": "아이디와 비밀번호를 모두 입력해주세요."}), 400

    pw_hash = hashlib.sha256(pw_receive.encode('utf-8')).hexdigest()

    conn = None
    try:
        conn = get_oracledb_connect()
        cursor = conn.cursor()

        cursor.execute("SELECT password FROM USERS WHERE name = :1", (name_receive,))
        user = cursor.fetchone()
        cursor.close()

        if user and user[0] == pw_hash:
            return jsonify({"result" : "success", "message": "로그인 성공"}), 200
        else:
            return jsonify({"result": "fail", "message": "아이디 또는 비밀번호가 틀렸습니다."}), 401
    except oracledb.DatabaseError as e:
        return jsonify({"message" : "로그인 처리 중 오류 발생", "error": str(e)}), 500
    finally:
        if conn:
            conn.close()
            
# 지역별 대여회사 조회 통로
@app.route('/api/company/<region>', methods=['GET'])
def get_companies_by_region(region):
    conn = get_oracledb_connect()
    cursor = conn.cursor()
    # :1을 ?로 변경하고, % 와일드카드는 파라미터 쪽에 붙여서 넘깁니다.
    cursor.execute(
    "SELECT companyId, companyName,companyAddress, companyPhone, representativeName, representativeEmail FROM COMPANY WHERE companyAddress LIKE :1", 
    ('%' + region + '%',))
    
    
    # cursor.execute("SELECT companyId, companyName, companyAddress, companyPhone, representativeName, representativeEmail FROM COMPANY")
    
    companies = cursor.fetchall()
    cursor.close()
    conn.close()
    
    result = [{"companyId": c[0], "companyName": c[1],"companyAddress": c[2],"companyPhone": c[3],"representativeName": c[4],"representativeEmail": c[5]} for c in companies]
    return jsonify(result), 200

# 4~5단계. 특정 대여회사의 캠핑카 및 정비정보 조회 통로
@app.route('/api/company/<int:companyId>/cars', methods=['GET'])
def get_cars(companyId):
    conn = get_oracledb_connect()
    cursor = conn.cursor()
    cursor.execute("""
        SELECT carId, companyId ,carName, carNumber, numberOfRider, carImage, carDetail, carRentalCost 
        FROM CAR WHERE companyId = :1
    """, (companyId,))
    # cursor.execute("""
    #     SELECT carId, companyId ,carName, carNumber, numberOfRider, carImage, carDetail, carRentalCost 
    #     FROM CAR """)
    cars = cursor.fetchall()
    cursor.close()
    conn.close()
    
    result = [{
        "carId": car[0], "companyId" : car[1],  "carName" : car[2],  "carNumber" : car[3],  "numberOfRider" : car[4],  "carImage" : car[5],
        "carDetail" : car[6], "carRentalCost" : car[7]   
    } for car in cars]
    print(result)
    return jsonify(result), 200

# 예약이력확인
@app.route('/api/rental/history/my', methods=['GET'])
def api_renthistory():
    # carName, license, companyname, start, end, totalcost
    name_receive = request.args.get('username')
    print(name_receive)
    
    try:
        conn = get_oracledb_connect()
        cursor = conn.cursor()
        
        cursor.execute("""
            SELECT c.carName, r.license, cp.companyName, r.startDateTime, r.endDateTime, r.totalCost
            FROM RENTAL r
            JOIN Car c ON r.carId = c.carId
            JOIN Company cp ON r.companyId = cp.companyId
            JOIN USERS u ON r.license = u.license
            WHERE u.name = :1
            ORDER BY r.rentalId DESC
        """, (name_receive,))
        history_data = cursor.fetchall()
        cursor.close()
        conn.close()
        print(history_data)
    
        result = [{
            "carName": row[0],
            "license": row[1],
            "companyName": row[2],
            "startDateTime": row[3].strftime('%Y-%m-%dT%H:%M:%S') if row[3] else None,
            "endDateTime": row[4].strftime('%Y-%m-%dT%H:%M:%S') if row[4] else None,
            "totalCost": int(row[5]) if row[5] else 0
        } for row in history_data]
        
        return jsonify(result), 200

    except oracledb.DatabaseError as e:
        return jsonify({"message": "예약 내역 조회 중 오류가 발생했습니다", "error": str(e)}), 500
    
# 조회하고 있는 차의 예약 내역
@app.route('/api/rental/history/car/<int:carId>', methods=['GET'])
def api_carhistory(carId):
    try:
        conn = get_oracledb_connect()
        cursor = conn.cursor()
        
        cursor.execute("""
            SELECT startDateTime, endDateTime FROM RENTAL WHERE carId = :1            
        """, (carId,))
        car_history = cursor.fetchall()
        cursor.close()
        conn.close()

        result = [{"startDateTime": row[0], "endDateTime": row[1]} for row in car_history]
        return jsonify(result), 200
        
    except oracledb.DatabaseError as e:
        return jsonify({"message": "차량 예약 조회 실패", "error": str(e)}), 500
    
# 예약하기
@app.route('/api/rental/rent', methods=['POST'])
def api_rent():
    car_id = request.json.get('carId')
    company_id = request.json.get('companyId')
    start_date_time = request.json.get('startDateTime')
    end_date_time = request.json.get('endDateTime')
    total_cost = request.json.get('totalCost')
    user_name = request.json.get('username')
    coupon_id = request.json.get('couponId')
        
    if not all([car_id, company_id, user_name, start_date_time, end_date_time, total_cost]):
        return jsonify({"message":"예약에 필요한 모든 정보가 누락되었습니다."}), 400
    
    conn = None
    try:
        conn = get_oracledb_connect()
        cursor = conn.cursor()
        
        # 1. 유저 라이센스 조회
        cursor.execute("SELECT license FROM USERS WHERE name = :1", (user_name,))
        user_row = cursor.fetchone()
        
        if not user_row:
            return jsonify({"message": "유효하지 않은 유저 세션입니다."}), 400
        
        detected_license = user_row[0]
        
        # 2. 차량 원본 대여료 조회 (금액 검증용)
        cursor.execute("SELECT carRentalCost FROM CAR WHERE carId = :1", (int(car_id),))
        car_row = cursor.fetchone()
        if not car_row:
            return jsonify({"message":"존재하지 않는 차량입니다."}), 400
        origin_cost = car_row[0]
        
        # 3. 쿠폰 할인율/할인금액 조회 및 적용
        discount = 0
        if coupon_id and str(coupon_id).strip():
            # 유저가 가진 미사용 쿠폰의 할인 금액을 가져옴
            cursor.execute("""
                SELECT c.discount_value 
                FROM USERCOUPONS uc
                JOIN COUPONS c ON uc.coupon_id = c.coupon_id
                WHERE uc.user_coupon_id = :1 AND uc.license = :2 AND uc.status = 'unused'
            """, (int(coupon_id), detected_license))
            coupon_row = cursor.fetchone()
            
            if coupon_row:
                discount = coupon_row[0]
            else:
                return jsonify({"message": "사용할 수 없거나 유효하지 않은 쿠폰입니다."}), 400
            
        cursor.execute("""
           INSERT INTO RENTAL (carId, license, companyId, startDateTime, endDateTime, totalCost)
           VALUES (:1, :2, :3, TO_DATE(:4, 'YYYY-MM-DD"T"HH24:MI:SS'), TO_DATE(:5, 'YYYY-MM-DD"T"HH24:MI:SS'), :6)            
        """, (int(car_id), detected_license, int(company_id), start_date_time, end_date_time, int(total_cost)))
        
        if coupon_id and str(coupon_id).strip():
            cursor.execute("""
                UPDATE USERCOUPONS 
                SET status = 'used', usedDate = SYSDATE 
                WHERE license = :1 AND coupon_id = :2 AND status = 'unused'
            """, (detected_license, int(coupon_id)))
        
        conn.commit()
        return jsonify({"message":"예약이 성공적으로 완료되었습니다."}), 200
    
    except Exception as e:
        if conn:
            conn.rollback()
        return jsonify({"message": "서버 내부 에러 발생", "error": str(e)}), 500
        
    finally:
        if 'cursor' in locals() and cursor:
            cursor.close()
        if conn:
            conn.close()
            
@app.route('/api/coupons/my', methods=['GET'])
def get_my_coupons():
    user_name = request.args.get('username')
    user_license = request.args.get('license')
    
    if not user_license and not user_name:
        return jsonify({"message": "회원정보(면허번호)가 없습니다"}), 400
    
    conn = None
    try:
        conn = get_oracledb_connect()
        cursor = conn.cursor()
        
        if not user_license and user_name:
            cursor.execute("SELECT license FROM USERS WHERE name = :1", (user_name.strip(),))
            user_row = cursor.fetchone()
            
            if not user_row:
                cursor.close()
                return jsonify({"message": "존재하지 않는 이름입니다."}), 400
                
            user_license = user_row[0]

        # 🌟 2. 알아낸 license와 status='unused' 조건으로 쿠폰함 최종 조회
        cursor.execute("""
            SELECT uc.user_coupon_id, c.COUPONNAME, c.discount_value, c.end_date
            FROM USERCOUPONS uc
            JOIN COUPONS c ON uc.coupon_id = c.coupon_id
            WHERE uc.license = :1
                AND uc.status = 'unused'
                AND c.end_date >= TRUNC(SYSDATE)
            ORDER BY c.end_date ASC
        """, (user_license.strip(),))
        
        coupon_list = cursor.fetchall()
        cursor.close()
        
        result = [{
            "user_Coupon_id": row[0],
            "couponName": row[1],
            "discountValue": row[2],
            "endDate": row[3].strftime('%Y-%m-%d') if row[3] else None
        } for row in coupon_list]
        
        return jsonify(result), 200
    
    except oracledb.DatabaseError as e:
        return jsonify({"message":"쿠폰 목록 조회 중 오류가 발생했습니다.", "error":str(e)}), 500
    finally:
        if conn: conn.close()
 
if __name__ == '__main__':
    app.run(debug=True, port=8080)