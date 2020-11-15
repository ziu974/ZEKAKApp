package com.example.zekak;

public class Statistics {

    //Server: AWS, (class 이름: Statistics.java)
    // '모두먹음'카테고리 관리하기 위해(sqlite용량한계,장기적관점),
    //
    // [과정]
    // 시작:'모두먹음'버튼이 눌려졌을 떄 그 클래스에서 Statistics 객체 써서 진행
    // TODO:영양성분 api 사용해서 모두먹음 처리된 아이템의 영양성분 추출(이름으로 검색) -->
    // TODO:(파일형태??에 저장) -->
    // TODO:그 파일을 aws 서버에 업로드 (성공하면 로컬 ItemsDB에서 delete()진행)-->
    // <aws쪽구현>서버 상에서 람다코드 돌려서 자신의 데베에 저장 -->
    // <aws쪽구현> 서버 데베 column: id/탄/단/지/트랜 등 -->
    // TODO: local: 통계 탭 누를 시 서버에서 이 자료 다 긁어와서 띄워줌 //[끝!]
}
