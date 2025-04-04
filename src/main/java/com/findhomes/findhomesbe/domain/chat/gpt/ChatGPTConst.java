package com.findhomes.findhomesbe.domain.chat.gpt;

import com.findhomes.findhomesbe.domain.condition.domain.HouseDirection;

public abstract class ChatGPTConst {
    public static final String ROLE1 = "system";
    public static final String ROLE2 = "user";
    public static final String COMPLETE_CONTENT = "You are a machine that returns responses according to a predetermined format. Return the result in the specified format without any extra text.";
    public static final String CHAT_CONTENT = "You are a helpful assistant specialized in understanding user preferences for housing searches.";
    public static final double USER_CONDITION_TEMPERATURE = 0.9;
    public static final double CHAT_TEMPERATURE = 0.9;
    public static final double COMPLETE_TEMPERATURE = 0.2;


    public static final String EXTRACT_KEYWORD_COMMAND =
            "위의 대화에서 사용자가 원하는 조건을 키워드로 뽑아줘. ','를 기준으로 나열해줘.\n" +
                    "연관된 정보는 반드시 하나의 키워드로 묶어 표현해줘. \n" +
                    "예를 들어, '장소'와 '거리'처럼 같은 대상을 설명하는 조건은 \"헬스장(걸어서 5분 거리)\"와 같이 묶어줘. 또, 같은 카테고리나 관련된 조건들도 하나로 묶어 \"학군이 좋은 곳(학원이 많은 곳)\"으로 표현해줘. \n" +
                    "결과는 \"키워드1,키워드2,키워드3...\" 이런 식으로. 다른 말은 하지마.";

    public static final String HOUSE_CONDITION_DETAIL_REQUEST_COMMAND =
            "사용자가 챗봇과 나눈 대화를 기반으로 활용 가능 데이터에서 관련있는 데이터를 골라주고, 관련있는 데이터의 상세 조건을 같이 써줘.\n" +
                    "[각 데이터의 상세 조건 결과 형식]\n" +
                    "관리비는 정수로, 복층과 분리형은 true아니면 false, 층수는 정수로, 크기는 제곱미터 단위로 표현(평수일 경우 제곱미터로 변환)해줘." +
                    "그리고 방_수와 화장실수_는 정수로, 방향은 " + HouseDirection.getAllData() + " 중에 하나로 해주고, 완공일은 날짜를 8자리 숫자로 표현해줘.\n" +
                    "데이터와 데이터의 상세 조건은 '-'로 연결하고, 데이터들은 ','로 연결해줘.\n" +
                    "예시: 키워드@관리비-3, 키워드@복층-true\n" +
                    "모든 조건을 선택하는 것이 아니라 사용자가 요청한 것과 직접적으로 관련이 있는 데이터만 골라줘. " +
                    "반환 형식대로 반환해주면돼. 형식 외에 다른 말은 절대로 쓰지마 절대로.\n" +
                    "관련있는게 없으면 빈칸으로 해줘. 요청한 조건만 신경쓰면돼. 직접 요청한게 아닌데 너가 필요하지 않을까 마음대로 판단하지 마.";
    public static final String HOUSE_OPTION_DETAIL_REQUEST_COMMAND =
            "사용자가 챗봇과 나눈 대화를 기반으로 활용 가능 데이터에서 관련있는 데이터를 골라줘.\n" +
                    "사용자의 요청과 관련이 없는 데이터는 절대로 고르지 마.\n" +
                    "모든 데이터는 ',' 쉼표를 기준으로 구분해줘.\n" +
                    "쉼표와 데이터 이름 외에는 절대로 아무것도 반환하지마.\n" +
                    "예시: 키워드@화재경보기,키워드@신발장,키워드@옷장 \n" +
                    "직접 관련 있는 것만 골라. 사용자 요청이랑 관련 없는데 너 맘대로 추가하면 절대 안돼. 관련있는게 없으면 빈칸으로 해줘. 요청한 조건만 신경쓰면돼. 직접 요청한게 아닌데 너가 필요하지 않을까 마음대로 판단하지 마.";
    public static final String FACILITY_CATEGORY_DETAIL_REQUEST_COMMAND =
            "사용자가 챗봇과 나눈 대화를 기반으로 활용 가능 데이터에서 관련있는 시설 데이터를 골라줘. " +
                    "그리고 사용자의 요청과 어느 정도 관련있는지와 중요도를 가중치로 1에서 10의 숫자로 표현해줘.\n" +
                    "예를 들어 '음식점_버거킹-7'은 사용자가 버거킹과 가까운 집을 원할 경우, 음식점 중에서 버거킹의 중요도가 3임을 의미해.\n" +
                    "'음식점_버거킹'과 같은 형식에서 '음식점'은 큰 범주를, '버거킹'은 그 범주 안의 특정 단어를 의미해.\n" +
                    "이처럼 포함관계에 속하는 데이터는 '_'로 표시해. 특 정 카테고리 전부일 경우 특정 이름이 아니라 all로 표시해.\n" +
                    "사용자의 요청과 관련이 있는 시설 데이터 만을 쉼표로 구분해서 써주고, 관련도를 가중치로 표시해줘. 그 외에는 절대로 아무것도 반환하지마.\n" +
                    "관련있는게 없으면 빈칸으로 해줘. 요청한 조건만 신경쓰면돼. 직접 요청한게 아닌데 너가 필요하지 않을까 마음대로 판단하지 마.\n" +
                    "그리고 사용자가 요청한 시설 중에 해당하는 것이 없을 경우 적절한 데이터에 포함 관계로 써주면 돼. " +
                    "예를 들어서 피자면 음식점으로 연결 해주면돼." +
                    "예시: 키워드@음식점_버거킹-7, 키워드@피시방_all-2, 키워드@미용실_all-1, 키워드@병원_이비인후과-4, 키워드@병원_소아과-3\n" +
                    "'시설범주_상세 요청 단어' 쌍은 중복되면 안돼. 예를 들어서 '병원_all'이 있으면 '병원_all'이 또 나오면 절대 안돼. '병원_소아과'와 '병원_이비인후과'는 다르기 때문에 같이 나올 수 있어.\n" +
                    "하나의 키워드에 ";
    public static final String PUBLIC_DATA_DETAIL_REQUEST_COMMAND =
            "사용자가 챗봇과 나눈 대화를 기반으로 활용 가능 공공 데이터에서 관련있는 공공 데이터를 골라줘. " +
                    "그리고 사용자의 요청과 어느 정도 관련있는지와 중요도를 가중치로 1에서 10의 숫자로 표현해줘.\n" +
                    "데이터는 아래 예시의 형식처럼 데이터와 가중치는 '-'로 연결하고 ','로 구분해서 나열해줘.\n" +
                    "예시: 키워드@범죄율-7, 키워드@화재율-2\n" +
                    "사용자의 요청과 관련있는 공공 데이터만 골라주고, 관련이 없는 것은 절대로 쓰지마. " +
                    "관련있는게 없으면 빈칸으로 해줘. 요청한 조건만 신경쓰면돼. 직접 요청한게 아닌데 너가 필요하지 않을까 마음대로 판단하지 마. 그리고 요청 형식 외의 다른 말은 절대 아무것도 하지마.";
    public static final String USER_LOCATION_DETAIL_REQUEST_COMMAND =
            "사용자가 가까웠으면 하는 특정 지점의 좌표입니다. 위도와 경도는 '+'로 구분되고 (37.1239+127.9128)과 같이 표현됩니다. 다음과 같은 양식을 지켜야합니다. 예를 들어 '네이버 본사와 강남역이랑 가까웠으면 좋겠다'와 같은 조건을 받으면 네이버본사_(37.359512+127.105220)-2, 강남역_(37.497940+127.027620)-2 와 같이 나타냅니다.\n" +
                    "응답 형식을 반드시 지켜주고, 그 외에는 절대 아무 것도 언급하지마. 그리고 사용자가 요청한 것만 써줘.\n" +
                    "따로 특정 지점이나 시설을 언급한게 없으면 그냥 아무것도 쓰지마\n" +
                    "특히 너가 좌표를 모르는 시설이면 추가하지마. 좌표를 진짜로 원래 아는 시설이나 지역만 추가해줘.";
}
