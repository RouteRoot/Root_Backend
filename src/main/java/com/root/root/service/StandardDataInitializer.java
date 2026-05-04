package com.root.root.service;

import com.root.root.entity.standard.StandardExam;
import com.root.root.entity.standard.StandardJob;
import com.root.root.entity.standard.StandardJobRequirement;
import com.root.root.repository.StandardExamRepository;
import com.root.root.repository.StandardJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StandardDataInitializer implements CommandLineRunner {
    private final StandardJobRepository standardJobRepository;
    private final StandardExamRepository standardExamRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception{
        if(standardJobRepository.count() > 0 || standardExamRepository.count() > 0){
            return;
        }

        // 정보처리기사 표준 데이터
        StandardExam exam1 = new StandardExam();
        exam1.setCertificationName("정보처리기사");
        exam1.setEligibilityCondition("""
                    [기사 등급 공통 응시자격]
                    1. 산업기사 등급 이상의 자격을 취득한 후 응시하려는 종목이 속하는 동일 및 유사 직무분야에서 1년 이상 실무에 종사한 사람
                    2. 기능사 자격을 취득한 후 응시하려는 종목이 속하는 동일 및 유사 직무분야에서 3년 이상 실무에 종사한 사람
                    3. 응시하려는 종목이 속하는 동일 및 유사 직무분야의 다른 종목의 기사 등급 이상의 자격을 취득한 사람
                    4. 관련학과의 대학졸업자등 또는 그 졸업예정자 (4년제 기준 4학년 1학기 이상)
                    5. 3년제 전문대학 관련학과 졸업자등으로서 졸업 후 응시하려는 종목이 속하는 동일 및 유사 직무분야에서 1년 이상 실무에 종사한 사람
                    6. 2년제 전문대학 관련학과 졸업자등으로서 졸업 후 응시하려는 종목이 속하는 동일 유사 직무분야에서 2년 이상 실무에 종사한 사람
                    7. 동일 및 유사 직무분야의 기사 수준 기술훈련과정 이수자 또는 그 이수예정자
                    8. 동일 및 유사 직무분야의 산업기사 수준 기술훈련과정 이수자로서 이수 후 응시하려는 종목이 속하는 동일 및 유사 직무분야에서 2년 이상 실무에 종사한 사람
                    9. 응시하려는 종목이 속하는 동일 및 유사 직무분야에서 4년 이상 실무에 종사한 사람
                    10. 외국에서 동일한 종목에 해당하는 자격을 취득한 사람
                    """);
        addSyllabus(exam1, "소프트웨어 설계");
        addSyllabus(exam1, "소프트웨어 개발");
        addSyllabus(exam1, "데이터베이스 구축");
        addSyllabus(exam1, "프로그래밍 언어 활용");
        addSyllabus(exam1, "정보 시스템 구축 관리");
        standardExamRepository.save(exam1);

        StandardExam exam2 = new StandardExam();
        exam2.setCertificationName("SQLD");
        exam2.setEligibilityCondition("제한 없음 (학력/경력 무관 누구나 응시 가능)");
        addSyllabus(exam2, "데이터 모델링의 이해");
        addSyllabus(exam2, "SQL 기본 및 활용");
        standardExamRepository.save(exam2);

        // 정보처리산업기사
        StandardExam exam3 = new StandardExam();
        exam3.setCertificationName("정보처리산업기사");
        exam3.setEligibilityCondition("""
        [산업기사 등급 공통 응시자격 - 관련학과는 모든 대학·모든 학과 해당]
        1. 기능사 자격을 취득한 후 응시하려는 종목이 속하는 동일 및 유사 직무분야에서 1년 이상 실무에 종사한 사람
        2. 응시하려는 종목이 속하는 동일 및 유사 직무분야의 다른 종목의 산업기사 등급 이상의 자격을 취득한 사람
        3. 관련학과의 2년제 또는 3년제 전문대학졸업자등 또는 그 졸업예정자
        4. 관련학과의 대학졸업자등 또는 그 졸업예정자
        5. 동일 및 유사 직무분야의 산업기사 수준 기술훈련과정 이수자 또는 그 이수예정자
        6. 응시하려는 종목이 속하는 동일 및 유사 직무분야에서 2년 이상 실무에 종사한 사람
        7. 고용노동부령으로 정하는 기능경기대회 입상자
        8. 외국에서 동일한 종목에 해당하는 자격을 취득한 사람
        """);
        addSyllabus(exam3, "정보시스템 기반 기술");
        addSyllabus(exam3, "프로그래밍 언어 활용");
        addSyllabus(exam3, "데이터베이스 활용");
        standardExamRepository.save(exam3);

        // 빅데이터분석기사
        StandardExam exam4 = new StandardExam();
        exam4.setCertificationName("빅데이터분석기사");
        exam4.setEligibilityCondition("""
        [아래 9가지 중 하나 충족 시 응시 가능 - 관련학과·동일직무분야 제한 없음]
        1. 대학졸업자 또는 졸업예정자 (전공 무관, 4년제 기준)
        2. 3년제 전문대학 졸업자로서 졸업 후 1년 이상 실무에 종사한 사람
        3. 2년제 전문대학 졸업자로서 졸업 후 2년 이상 실무에 종사한 사람
        4. 기사 등급 이상의 자격을 취득한 사람
        5. 기사 수준 기술훈련과정 이수자 또는 이수예정자
        6. 산업기사 자격 취득 후 동일 유사 직무분야에서 1년 이상 실무에 종사한 사람
        7. 산업기사 수준 기술훈련과정 이수 후 2년 이상 실무에 종사한 사람
        8. 기능사 자격 취득 후 3년 이상 실무에 종사한 사람
        9. 동일 및 유사 직무분야에서 4년 이상 실무에 종사한 사람
        """);
        addSyllabus(exam4, "빅데이터 분석 기획");
        addSyllabus(exam4, "빅데이터 탐색");
        addSyllabus(exam4, "빅데이터 모델링");
        addSyllabus(exam4, "빅데이터 결과 해석");
        standardExamRepository.save(exam4);

        // 컴퓨터그래픽기능사
        StandardExam exam5 = new StandardExam();
        exam5.setCertificationName("컴퓨터그래픽기능사");
        exam5.setEligibilityCondition("제한 없음 (학력/경력 무관 누구나 응시 가능)");
        addSyllabus(exam5, "산업 디자인 일반");
        addSyllabus(exam5, "색채 및 도법");
        addSyllabus(exam5, "디자인 재료");
        addSyllabus(exam5, "컴퓨터그래픽스");
        standardExamRepository.save(exam5);

        // 웹디자인개발기능사
        StandardExam exam6 = new StandardExam();
        exam6.setCertificationName("웹디자인개발기능사");
        exam6.setEligibilityCondition("제한 없음 (학력/경력 무관 누구나 응시 가능)");
        addSyllabus(exam6, "웹디자인 구현");
        addSyllabus(exam6, "웹페이지 제작");
        standardExamRepository.save(exam6);

        // SQLP
        StandardExam exam7 = new StandardExam();
        exam7.setCertificationName("SQLP");
        exam7.setEligibilityCondition("""
        [학력/경력 또는 자격 기준 중 하나 충족 시 응시 가능]
        학력/경력 기준:
        1. 4년제 대학 졸업자 또는 졸업예정자로서 데이터베이스 관련 실무경력 1년 이상
        2. 전문대학 졸업자로서 데이터베이스 관련 실무경력 3년 이상
        3. 고등학교 졸업자로서 데이터베이스 관련 실무경력 5년 이상
        자격 기준:
        4. SQLD(SQL 개발자) 자격 취득 후 데이터베이스 관련 실무경력 1년 이상
        5. 정보처리기사 등 IT 관련 기사 자격 취득 후 데이터베이스 관련 실무경력 1년 이상
        """);
        addSyllabus(exam7, "데이터 모델링의 이해");
        addSyllabus(exam7, "SQL 기본 및 활용");
        addSyllabus(exam7, "SQL 고급활용 및 튜닝");
        standardExamRepository.save(exam7);

        // 정보보안기사
        StandardExam exam8 = new StandardExam();
        exam8.setCertificationName("정보보안기사");
        exam8.setEligibilityCondition("""
        [기사 등급 공통 응시자격 - 정보보안 분야는 모든 학과·모든 직무분야 응시 가능]
        1. 관련학과의 대학졸업자등 또는 그 졸업예정자 (전공 무관, 4년제 기준)
        2. 산업기사 등급 이상의 자격을 취득한 후 동일 및 유사 직무분야에서 1년 이상 실무에 종사한 사람
        3. 기능사 자격을 취득한 후 동일 및 유사 직무분야에서 3년 이상 실무에 종사한 사람
        4. 동일 및 유사 직무분야의 다른 종목 기사 등급 이상의 자격을 취득한 사람
        5. 3년제 전문대학 졸업자로서 졸업 후 동일 유사 직무분야에서 1년 이상 실무에 종사한 사람
        6. 2년제 전문대학 졸업자로서 졸업 후 동일 유사 직무분야에서 2년 이상 실무에 종사한 사람
        7. 동일 및 유사 직무분야에서 4년 이상 실무에 종사한 사람
        """);
        addSyllabus(exam8, "시스템 보안");
        addSyllabus(exam8, "네트워크 보안");
        addSyllabus(exam8, "애플리케이션 보안");
        addSyllabus(exam8, "정보보안 일반");
        addSyllabus(exam8, "정보보안 관리 및 법규");
        standardExamRepository.save(exam8);

        // 컴퓨터활용능력 1급
        StandardExam exam9 = new StandardExam();
        exam9.setCertificationName("컴퓨터활용능력 1급");
        exam9.setEligibilityCondition("제한 없음 (학력/경력 무관 누구나 응시 가능)");
        addSyllabus(exam9, "컴퓨터 일반");
        addSyllabus(exam9, "스프레드시트 일반");
        addSyllabus(exam9, "데이터베이스 일반");
        standardExamRepository.save(exam9);

        // 컴퓨터활용능력 2급
        StandardExam exam10 = new StandardExam();
        exam10.setCertificationName("컴퓨터활용능력 2급");
        exam10.setEligibilityCondition("제한 없음 (학력/경력 무관 누구나 응시 가능)");
        addSyllabus(exam10, "컴퓨터 일반");
        addSyllabus(exam10, "스프레드시트 일반");
        standardExamRepository.save(exam10);

        // 네트워크관리사 2급
        StandardExam exam11 = new StandardExam();
        exam11.setCertificationName("네트워크관리사 2급");
        exam11.setEligibilityCondition("제한 없음 (학력/경력 무관 누구나 응시 가능)");
        addSyllabus(exam11, "네트워크 일반");
        addSyllabus(exam11, "TCP/IP");
        addSyllabus(exam11, "NOS");
        addSyllabus(exam11, "네트워크 운용기기");
        standardExamRepository.save(exam11);

        // 네트워크관리사 1급
        StandardExam exam12 = new StandardExam();
        exam12.setCertificationName("네트워크관리사 1급");
        exam12.setEligibilityCondition("""
        [아래 조건 중 하나 충족 시 응시 가능]
        1. 네트워크관리사 2급 자격 취득자
        2. 전기·전자·통신·정보처리 직무분야 국가기술자격 취득자 (산업기사 이상)
        3. IT 관련 사업장에서 5년 이상 종사한 사람
        """);
        addSyllabus(exam12, "네트워크 일반");
        addSyllabus(exam12, "TCP/IP");
        addSyllabus(exam12, "NOS");
        addSyllabus(exam12, "네트워크 운용기기");
        addSyllabus(exam12, "네트워크 보안");
        standardExamRepository.save(exam12);

        StandardJob job1 = new StandardJob();
        job1.setHope("백엔드 개발자");

        addRequirement(job1, "정보처리기사");
        addRequirement(job1, "SQLD");
        standardJobRepository.save(job1);

        // 프론트엔드 개발자
        StandardJob job2 = new StandardJob();
        job2.setHope("프론트엔드 개발자");
        addRequirement(job2, "정보처리기사");
        addRequirement(job2, "정보처리산업기사");
        addRequirement(job2, "웹디자인개발기능사");
        standardJobRepository.save(job2);

        // 데이터 엔지니어/분석가
        StandardJob job3 = new StandardJob();
        job3.setHope("데이터 엔지니어/분석가");
        addRequirement(job3, "정보처리기사");
        addRequirement(job3, "SQLD");
        addRequirement(job3, "빅데이터분석기사");
        standardJobRepository.save(job3);

        // 모바일 앱 개발자
        StandardJob job4 = new StandardJob();
        job4.setHope("모바일 앱 개발자");
        addRequirement(job4, "정보처리기사");
        addRequirement(job4, "정보처리산업기사");
        standardJobRepository.save(job4);

        // 웹 디자이너
        StandardJob job5 = new StandardJob();
        job5.setHope("웹 디자이너");
        addRequirement(job5, "웹디자인개발기능사");
        addRequirement(job5, "컴퓨터그래픽기능사");
        standardJobRepository.save(job5);

        // 보안 전문가
        StandardJob job6 = new StandardJob();
        job6.setHope("보안 전문가");
        addRequirement(job6, "정보보안기사");
        addRequirement(job6, "정보처리기사");
        addRequirement(job6, "네트워크관리사 2급");
        addRequirement(job6, "네트워크관리사 1급");
        standardJobRepository.save(job6);

        // DBA (데이터베이스 관리자)
        StandardJob job7 = new StandardJob();
        job7.setHope("DBA");
        addRequirement(job7, "SQLP");
        addRequirement(job7, "SQLD");
        addRequirement(job7, "빅데이터분석기사");
        addRequirement(job7, "정보처리기사");
        standardJobRepository.save(job7);

        // IT 사무직 / 전산직
        StandardJob job8 = new StandardJob();
        job8.setHope("IT 사무직");
        addRequirement(job8, "컴퓨터활용능력 1급");
        addRequirement(job8, "컴퓨터활용능력 2급");
        addRequirement(job8, "정보처리산업기사");
        standardJobRepository.save(job8);
    }

    private void addRequirement(StandardJob job, String taskName){
        StandardJobRequirement req = new StandardJobRequirement();
        req.setStandardJob(job);
        req.setTaskName(taskName);

        if(job.getRequirements() == null) job.setRequirements(new java.util.ArrayList<>());
        job.getRequirements().add(req);
    }

    private void addSyllabus(StandardExam exam, String subjectName){
        com.root.root.entity.standard.StandardExamSyllabus syllabus = new com.root.root.entity.standard.StandardExamSyllabus();
        syllabus.setStandardExam(exam);
        syllabus.setSubjectName(subjectName);

        if(exam.getSyllabuses() == null){
            exam.setSyllabuses(new java.util.ArrayList<>());
        }
        exam.getSyllabuses().add(syllabus);
    }
}
