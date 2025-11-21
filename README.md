# 가계부 앱 (Finance Tracker)

## 프로젝트 소개
대학교 과제로 제작한 가계부 앱입니다. 토스 앱의 알림을 자동으로 감지하여 거래 내역을 쉽게 등록할 수 있는 기능을 제공합니다.

## 주요 기능

### 1. 달력 기반 가계부
- 월별 달력 뷰로 수입/지출 확인
- 날짜별로 거래 내역 표시 (+/- 표시)
- 각 날짜를 클릭하면 상세 내역 확인 가능

### 2. 거래 내역 관리
- 수입/지출 추가, 수정, 삭제
- 카테고리별 분류 (식비, 교통, 쇼핑, 여가, 공과금, 급여, 기타)
- 날짜 및 시간 설정
- 상세 설명 추가

### 3. 토스 알림 연동 (핵심 기능)
- 토스 앱의 거래 알림 자동 감지
- 감지된 거래에 대해 등록 여부를 묻는 알림 표시
- 알림 클릭 시 거래 등록 화면으로 바로 이동
- 금액, 지출처, 시간 자동 입력 (수정 가능)

### 4. 월별 통계
- 총 수입 금액
- 총 지출 금액
- 잔액 계산

## 기술 스택
- **언어**: Java
- **빌드 도구**: Gradle
- **데이터베이스**: Room Database
- **UI**: Material Design Components
- **달력**: Material CalendarView
- **아키텍처**: MVVM Pattern

## 설치 및 실행

### 필요 조건
- Android Studio Arctic Fox 이상
- Android SDK 24 (Android 7.0) 이상
- JDK 8 이상

### 빌드 방법
1. Android Studio에서 프로젝트 열기
2. Gradle Sync 실행
3. 앱 실행 (Shift + F10)

### 알림 권한 설정
토스 알림 감지 기능을 사용하려면 다음 권한이 필요합니다:

1. **알림 접근 권한**
   - 설정 → 알림 → 알림 접근 → 가계부 앱 활성화

2. **일반 알림 권한**
   - 앱 실행 시 알림 권한 허용

## 사용 방법

### 거래 수동 등록
1. 메인 화면 우측 하단의 플러스(+) 버튼 클릭
2. 수입/지출 선택
3. 금액, 카테고리, 설명 입력
4. 날짜 및 시간 선택
5. 저장 버튼 클릭

### 토스 알림 자동 등록
1. 토스 앱에서 거래 발생 (결제, 입금 등)
2. 가계부 앱에서 등록 알림 확인
3. 알림 클릭하여 거래 등록 화면으로 이동
4. 자동 입력된 정보 확인 및 수정
5. 저장 버튼 클릭

### 거래 내역 확인
1. 메인 화면에서 달력의 날짜 클릭
2. 해당 날짜의 거래 내역 확인
3. 일별 수입/지출 합계 확인

## 프로젝트 구조
```
app/src/main/java/com/example/financetracker/
├── database/           # Room 데이터베이스 관련
│   ├── AppDatabase.java
│   ├── TransactionDao.java
│   └── TransactionRepository.java
├── model/              # 데이터 모델
│   └── Transaction.java
├── service/            # 백그라운드 서비스
│   └── TossNotificationListenerService.java
├── ui/                 # UI 관련 (Activity, ViewModel, Adapter)
│   ├── MainActivity.java
│   ├── MainViewModel.java
│   ├── DayDetailActivity.java
│   ├── DayDetailViewModel.java
│   ├── AddTransactionActivity.java
│   ├── AddTransactionViewModel.java
│   └── TransactionAdapter.java
└── utils/              # 유틸리티 클래스
    ├── DateUtils.java
    └── TossNotificationParser.java
```

## 데이터베이스 스키마

### Transaction Table
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | INTEGER | 기본 키 (자동 증가) |
| type | TEXT | 거래 유형 (INCOME/EXPENSE) |
| amount | INTEGER | 금액 |
| category | TEXT | 카테고리 |
| description | TEXT | 상세 설명 |
| date | TEXT | 날짜 (yyyy-MM-dd) |
| time | TEXT | 시간 (HH:mm) |
| isFromNotification | BOOLEAN | 알림에서 생성 여부 |

## 주의사항
- 토스 앱이 설치되어 있어야 알림 연동 기능이 작동합니다
- 알림 접근 권한은 Android 설정에서 수동으로 허용해야 합니다
- 정산 거래의 경우 실제 지출 금액을 수동으로 수정하여 등록하세요

## 라이선스
이 프로젝트는 대학교 과제용으로 제작되었습니다.

## 개발자
대학생 과제 프로젝트

## 버전 정보
- Version: 1.0
- Last Updated: 2024
