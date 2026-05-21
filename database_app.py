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
    name_receive = request.json.get('name')    
    license_receive = request.json.get('license')
    pw_receive = request.json.get('password') 
    address_receive = request.json.get('address')
    phone_receive = request.json.get('phone')
    email_receive = request.json.get('email')
    if not all([name_receive, license_receive, pw_receive, address_receive, phone_receive, email_receive]):
        # return 형식을 다른 API들과 똑같이 jsonify 구조로 맞춰주어 팅기는 버그를 막았습니다.
        return jsonify({"message": "회원 정보를 모두 입력해주세요."}), 400
    
    pw_hash = hashlib.sha256(pw_receive.encode('utf-8')).hexdigest()
    
    try:
        conn = get_oracledb_connect()
        cursor = conn.cursor()
        cursor.execute("INSERT INTO USERS (name,license,password,address,phone,email) VALUES (:1,:2,:3,:4,:5,:6)", 
        (name_receive,  license_receive, pw_hash, address_receive, phone_receive, email_receive))
        conn.commit()
        cursor.close()
        return jsonify({"message": "회원가입이 완료되었습니다."}), 200
    except oracledb.IntegrityError:
        return jsonify({"message": "이미 존재하는 아이디입니다."}), 400

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

    conn = get_oracledb_connect()
    cursor = conn.cursor()

    cursor.execute("SELECT password FROM USERS WHERE name = :1", (name_receive,))
    user = cursor.fetchone()
    conn.close()

    if user and user[0] == pw_hash:
        return jsonify({"result" : "success", "message": "로그인 성공"}), 200
    else:
        return jsonify({"result": "fail", "message": "아이디 또는 비밀번호가 틀렸습니다."}), 401

# 2~3단계. 지역별 대여회사 조회 통로
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
    # cursor.execute("""
    #     SELECT carId, companyId ,carName, carNumber, numberOfRider, carImage, carDetail, carRentalCost 
    #     FROM CAR WHERE companyId = :1
    # """, (companyId,))
    
    cursor.execute("""
        SELECT carId, companyId ,carName, carNumber, numberOfRider, carImage, carDetail, carRentalCost 
        FROM CAR """)
    cars = cursor.fetchall()
    cursor.close()
    conn.close()
    
    result = [{
        "carId": car[0], "companyId" : car[1],  "carName" : car[2],  "carNumber" : car[3],  "numberOfRider" : car[4],  "carImage" : car[5],
        "carDetail" : car[6], "carRentalCost" : car[7]   
    } for car in cars]
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
            "startDateTime": row[3],
            "endDateTime": row[4],
            "totalCost": row[5]
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
    
    print(car_id)
    print(user_name)
    if not all([car_id, company_id, user_name, start_date_time, end_date_time, total_cost]):
        return jsonify({"message":"예약에 필요한 모든 정보가 누락되었습니다."}), 400
    
    try:
        conn = get_oracledb_connect()
        cursor = conn.cursor()
        
        cursor.execute("SELECT license FROM USERS WHERE name = :1", (user_name,))
        user_row = cursor.fetchone()
        
        if not user_row:
            return jsonify({"message": "유효하지 않은 유저 세션입니다."}), 400
        
        detected_license = user_row[0]
        
        cursor.execute("""
           INSERT INTO RENTAL (carId, license, companyId, startDateTime, endDateTime, totalCost)
           VALUES (:1, :2, :3, :4, :5, :6)            
        """, (car_id, detected_license, company_id, start_date_time, end_date_time, total_cost))
        
        conn.commit()
        cursor.close()
        conn.close()
        
        return jsonify({"message":"예약이 성공적으로 완료되었습니다."}), 200
    
    except oracledb.DatabaseError as e:
        return jsonify({"message": "오라클 DB 예약 저장 실패", "error": str(e)}), 500
    
if __name__ == '__main__':
    app.run(debug=True, port=8080)