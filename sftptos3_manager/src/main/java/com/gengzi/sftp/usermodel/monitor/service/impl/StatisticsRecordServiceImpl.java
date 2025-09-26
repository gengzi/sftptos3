package com.gengzi.sftp.usermodel.monitor.service.impl;

import com.gengzi.sftp.usermodel.dao.audit.StatisticsRecord;
import com.gengzi.sftp.usermodel.dao.audit.StatisticsRecordRepository;
import com.gengzi.sftp.usermodel.monitor.service.StatisticsRecordService;
import com.gengzi.sftp.usermodel.response.StatisticsRecordResponse;
import com.gengzi.sftp.usermodel.response.StatisticsTrafficResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsRecordServiceImpl implements StatisticsRecordService {


    @Autowired
    private StatisticsRecordRepository statisticsRecordRepository;

    @Override
    public StatisticsRecordResponse statisticsNow() {
        // 获取今天的日期（不包含时间）
        LocalDate today = LocalDate.now();
        // 组合今天的日期和 00:00:00 时间
        LocalDateTime todayStart = today.atStartOfDay();
        List<StatisticsRecord> byCreateTimeBetween = statisticsRecordRepository.findByCreateTimeBetween(todayStart, LocalDateTime.now());
        List<StatisticsRecordResponse> statisticsRecordResponses = byCreateTimeBetween.stream().map(statisticsRecord -> {
            StatisticsRecordResponse response = new StatisticsRecordResponse();
            response.setAuthCountVal(statisticsRecord.getAuthCountVal());
            response.setAuthSuccessVal(statisticsRecord.getAuthSuccessVal());
            response.setAuthFailureVal(statisticsRecord.getAuthFailureVal());
            response.setDownloadCountVal(statisticsRecord.getDownloadCountVal());
            response.setDownloadSuccessVal(statisticsRecord.getDownloadSuccessVal());
            response.setDownloadFailureVal(statisticsRecord.getDownloadFailureVal());
            response.setUploadCountVal(statisticsRecord.getUploadCountVal());
            response.setUploadSuccessVal(statisticsRecord.getUploadSuccessVal());
            response.setUploadFailureVal(statisticsRecord.getUploadFailureVal());
            response.setDelCountVal(statisticsRecord.getDelCountVal());
            response.setDelSuccessVal(statisticsRecord.getDelSuccessVal());
            response.setDelFailureVal(statisticsRecord.getDelFailureVal());
            return response;
        }).collect(Collectors.toList());
        StatisticsRecordResponse response = new StatisticsRecordResponse();
        response.setAuthCountVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getAuthCountVal).sum());
        response.setAuthSuccessVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getAuthSuccessVal).sum());
        response.setAuthFailureVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getAuthFailureVal).sum());
        response.setDownloadCountVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getDownloadCountVal).sum());
        response.setDownloadSuccessVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getDownloadSuccessVal).sum());
        response.setDownloadFailureVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getDownloadFailureVal).sum());
        response.setUploadCountVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getUploadCountVal).sum());
        response.setUploadSuccessVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getUploadSuccessVal).sum());
        response.setUploadFailureVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getUploadFailureVal).sum());
        response.setDelCountVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getDelCountVal).sum());
        response.setDelSuccessVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getDelSuccessVal).sum());
        response.setDelFailureVal(statisticsRecordResponses.stream().mapToLong(StatisticsRecordResponse::getDelFailureVal).sum());
        return response;
    }

    @Override
    public List<StatisticsTrafficResponse> statisticsTraffic(String timeType) {
        ArrayList<StatisticsTrafficResponse> statisticsTrafficResponses = new ArrayList<>();
        switch (timeType) {
            case "24":
                List<StatisticsRecord> statisticsRecordByCreateTimeBetweenHour = statisticsRecordRepository.
                        findStatisticsRecordByCreateTimeBetween(LocalDate.now().atStartOfDay(),
                                LocalDateTime.of(LocalDate.now(), LocalTime.MAX));

                for (int hour = 0; hour < 24; hour++) {
                    String hourStr = String.format("%02d", hour); // 格式化小时为"00"~"23"
                    String timeLabel = hourStr + "时";
                    StatisticsTrafficResponse hourResponse = new StatisticsTrafficResponse();
                    hourResponse.setTimeLabel(timeLabel);
                    int finalHour = hour;
                    long uploadSizeSum = statisticsRecordByCreateTimeBetweenHour.stream()
                            .filter(statisticsRecord -> statisticsRecord.getCreateTime().getHour() == finalHour)
                            .mapToLong(statisticsRecord -> Long.parseLong("".equals(statisticsRecord.getUploadSize()) ? "0" : statisticsRecord.getUploadSize()))
                            .sum();
                    long downloadSizeSum = statisticsRecordByCreateTimeBetweenHour.stream()
                            .filter(statisticsRecord -> statisticsRecord.getCreateTime().getHour() == finalHour)
                            .mapToLong(statisticsRecord -> Long.parseLong("".equals(statisticsRecord.getDownloadSize()) ? "0" : statisticsRecord.getDownloadSize()))
                            .sum();
                    hourResponse.setUploadSize(String.valueOf(uploadSizeSum));
                    hourResponse.setDownloadSize(String.valueOf(downloadSizeSum));
                    statisticsTrafficResponses.add(hourResponse);
                }
                break;
            case "7":
                List<StatisticsRecord> statisticsRecordByCreateTimeBetweenDay = statisticsRecordRepository.
                        findStatisticsRecordByCreateTimeBetween(LocalDate.now().minusDays(7).atStartOfDay(),
                                LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
                for (int day = 7; day > 0; day--) {
                    LocalDateTime localDateTime = LocalDate.now().minusDays(day).atStartOfDay();
                    String timeLabel = localDateTime.format(DateTimeFormatter.ofPattern("MM-dd"));
                    StatisticsTrafficResponse dayResponse = new StatisticsTrafficResponse();
                    dayResponse.setTimeLabel(timeLabel);
                    int finalHour = day;
                    long uploadSizeSum = statisticsRecordByCreateTimeBetweenDay.stream()
                            .filter(statisticsRecord -> statisticsRecord.getCreateTime().getDayOfYear() == localDateTime.getDayOfYear())
                            .mapToLong(statisticsRecord -> Long.parseLong(statisticsRecord.getUploadSize()))
                            .sum();
                    long downloadSizeSum = statisticsRecordByCreateTimeBetweenDay.stream()
                            .filter(statisticsRecord -> statisticsRecord.getCreateTime().getDayOfYear() == localDateTime.getDayOfYear())
                            .mapToLong(statisticsRecord -> Long.parseLong(statisticsRecord.getDownloadSize()))
                            .sum();
                    dayResponse.setUploadSize(String.valueOf(uploadSizeSum));
                    dayResponse.setDownloadSize(String.valueOf(downloadSizeSum));
                    statisticsTrafficResponses.add(dayResponse);
                }
                break;
            default:
                break;
        }
        return statisticsTrafficResponses;
    }


}
