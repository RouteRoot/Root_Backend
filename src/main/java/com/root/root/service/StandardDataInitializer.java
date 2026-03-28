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

        StandardJob job1 = new StandardJob();
        job1.setHope("백엔드 개발자");

        addRequirement(job1, "정보처리기사");
        addRequirement(job1, "SQLD");
        standardJobRepository.save(job1);
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
