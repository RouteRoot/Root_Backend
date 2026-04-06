package com.root.root.dto;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.*;

import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "response")
public class OpenApiExamResponse {

    private Header header;
    private Body body;

    @Data
    public static class Header {

        private String resultCode;
        private String resultMsg;
    }

    @Data
    public static class Body {
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        private List<ExamItem> items;
        private Integer numOfRows;
    private Integer pageNo;
    private Integer totalCount;
    }

    @Data
    public static class ExamItem {

        private String jmcd;        // 종목코드
        private String jmfldnm;     // 종목명 (예: 정보처리기사)
        private String seriesnm;    // 계열명 (예: 기사)
        private String mdobligfldnm; // 중직무분야명 (예: 정보기술)
        private String obligfldnm;   // 대직무분야명 (예: 정보통신)
        private String qualgbnm;    // 자격구분명 (예: 국가기술자격)
    }
}
