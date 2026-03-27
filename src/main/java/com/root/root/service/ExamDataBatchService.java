package com.root.root.service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.root.root.dto.OpenApiExamResponse;
import com.root.root.entity.ExamData;
import com.root.root.repository.ExamDataRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class ExamDataBatchService {

    private final ExamDataRepository examDataRepository;

    @Value("${openapi.service-key}")
    private String serviceKey;

    public void fetchAndSaveExams() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(factory);

        String url = "http://openapi.q-net.or.kr/api/service/rest/InquiryListNationalQualifcationSVC/getList"
                + "?serviceKey=" + serviceKey + "&_type=xml";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
            headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);


            String xmlData = new String(responseEntity.getBody(), StandardCharsets.UTF_8);

            if (xmlData != null && xmlData.contains("<response>")) {

                xmlData = xmlData.replace("<script/>", "");

                XmlMapper xmlMapper = new XmlMapper();
                xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                OpenApiExamResponse response = xmlMapper.readValue(xmlData, OpenApiExamResponse.class);

                if (response != null && response.getBody() != null) {
                    response.getBody().getItems().forEach(item -> {
                        System.out.println("수집된 종목명: " + item.getJmfldnm());

                        ExamData exam = ExamData.builder()
                                .examName(item.getJmfldnm())
                                .examGroup(item.getSeriesnm())
                                .category(item.getMdobligfldnm())
                                .organization("한국산업인력공단")
                                .description(item.getObligfldnm() + " 분야 자격증")
                                .isActive(true)
                                .build();
                        examDataRepository.save(exam);
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
