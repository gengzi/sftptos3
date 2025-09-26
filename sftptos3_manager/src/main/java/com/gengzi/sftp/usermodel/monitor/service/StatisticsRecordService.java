package com.gengzi.sftp.usermodel.monitor.service;

import com.gengzi.sftp.usermodel.response.StatisticsRecordResponse;
import com.gengzi.sftp.usermodel.response.StatisticsTrafficResponse;

import java.util.List;

public interface StatisticsRecordService {

    StatisticsRecordResponse statisticsNow();

    List<StatisticsTrafficResponse> statisticsTraffic(String timeType);

}
