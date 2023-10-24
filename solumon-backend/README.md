# Solumon_Backend
솔루몬(Solumon)은 solution과 solomon을 합친 이름으로, 익명으로 고민을 나누고 해소할 수 있는 **고민 해결 플랫폼**입니다. 

사람들은 크고 작은 결정을 어려워하고 조언을 필요로 합니다. \
하지만 중요한 고민은 자신의 비밀이 알려질까봐 이야기하기 꺼려지고 작은 고민은 조언을 듣기엔 하찮다고 여겨서 쉽사리 이야기 할 수 없습니다. \
솔루몬은 고민이 있지만 혼자서 해결할 수 없을 때 다수의 의견을 듣고 해결할 수 있도록 기획되었습니다. 

익명의 커뮤니티에 자신의 고민글을 게시하고 **투표와 실시간 채팅**을 통해 다양한 의견과 조언을 얻을 수 있도록 합니다. \
그리고 **관심 주제 태그에 대한 추천 게시글**을 기반으로 평소에 관심있었던 주제의 고민들과 유사한 고민들을 먼저 확인할 수 있고, 내가 잘 알고 있는 분야의 다른 사람들의 고민에 도움을 줄 수 있습니다.

## Team Devcisive
developers와 decisive를 합친 이름으로 결단력 있는 개발자들이 되고자 하는 마음을 담았습니다.

## Architecture
![architecture.png](docs%2Farchitecture.png)

## Tech Stack
<div align=center> 
  <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> 
  <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> 
  <img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> 
  <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> 
  <img src="https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> 
  <img src="https://img.shields.io/badge/elasticsearch-005571?style=for-the-badge&logo=Elasticsearch&logoColor=white">
  <img src="https://img.shields.io/badge/amazonaws-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white">
  <img src="https://img.shields.io/badge/amazons3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">
  <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/amazonec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white">
  <img src="https://img.shields.io/badge/apachekafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white">
  <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
</div>

## 프로젝트 기능
### 유저 기능

1. 회원 가입 및 로그인
  - 사이트 자체 회원 가입 및 로그인과 카카오 로그인을 지원합니다.
2. 관심 주제 설정
  - 첫 로그인 시 관심 주제 태그를 최대 5개 선택할 수 있습니다.
  - 관심 주제 태그는 언제든 수정 가능합니다.
3. 문제 회원 채팅 금지 기능
  - 특정 회원이 5회 이상 신고될 경우 밴(ban) 상태 처리되어 채팅이 불가능합니다.
  - 밴은 일주일이 경과될 경우 자동 해제됩니다.
  - 누적 3회 밴 상태 될 시 채팅이 영구 정지됩니다.


### 고민 게시글 관련 기능

1. 관심 주제 기반 열람
  - 한 시간마다 spring batch를 통해 관심 주제 기반 추천 글이 업데이트 됩니다.
  - content-based 추천 알고리즘(코사인 유사도)을 활용합니다.
2. 채팅을 통한 게시글 관련 논의
  - 게시글마다 spring websocket과 STOMP, kafka를 통한 실시간 채팅을 지원합니다.
  - 내 게시글에 채팅이 달릴 시 알림이 발생합니다.
3. 투표 참여
  - 내 게시글의 투표 선택지와 투표 마감 시간을 자유롭게 설정할 수 있습니다.
  - 내 게시글인 경우 게시글 상세페이지로 이동 시 현재까지 진행된 투표 결과를 확인할 수 있습니다.
  - 내 게시글이 아닌 경우
    - 투표 마감이 되지 않은 경우 
    - 투표를 하지 않은 상태라면 투표 선택지를 선택할 수 있습니다.
  - 투표를 한 상태라면 현재까지 진행된 투표 결과를 확인할 수 있습니다.
  - 투표 마감이 된 경우 투표를 하지 않은 회원도 투표 결과 창을 확인할 수 있습니다.
  - 내 게시글의 투표 마감 시 알림이 발생합니다.
4. 검색 기능
  - 태그 검색, 제목 및 본문 검색을 통해 찾고싶은 단어가 포함되어있는 게시글을 검색할 수 있습니다.
  - elastic search을 기반으로 한글 형태소 분석기인 nori analyzer를 이용하여 어근을 검색합니다.


## ERD
![erd.png](docs%2Ferd.png)


## 팀원
- 김가현: https://github.com/ofgongmu
- 유도경: https://github.com/DokyungYou
- 천세경: https://github.com/GyeongSe99
- 황가연: https://github.com/Gayeon-H