package com.sky.service.impl;


import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //获取日期信息
        List<LocalDate> dateList = new ArrayList();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //获取营业额信息
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //传入map搜索当天的营业额信息
            HashMap map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("begin", beginTime);
            map.put("end", endTime);
            Double amount = orderMapper.getAmountTotal(map);
            amount = amount == null ? 0.0 : amount;
            turnoverList.add(amount);
        }
        //封装到vo返回
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户数据统计
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //获取日期信息
        List<LocalDate> dateList = new ArrayList();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            HashMap map = new HashMap();
            //获取当天总用户
            map.put("end", endTime);
            Integer totalUser = userMapper.countUserByMap(map);
            //获取当天新增用户
            map.put("begin", beginTime);
            Integer newUser = userMapper.countUserByMap(map);


            //加入list
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO OrderStatistics(LocalDate begin, LocalDate end) {
        //获取日期信息
        List<LocalDate> dateList = new ArrayList();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //获取当天订单和有效订单
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> vaildOrderList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //获取完成订单
            Integer or1 = getIntegerStatics(beginTime, endTime, null);
            //获取有效订单
            Integer or2 = getIntegerStatics(beginTime, endTime, Orders.COMPLETED);

            //加入对应list
            orderCountList.add(or1);
            vaildOrderList.add(or2);
        }

        //获取当天订单总数和有效订单总数 利用stream流和lambada表达式
        Integer orderTotal = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderTotal = vaildOrderList.stream().reduce(Integer::sum).get();

        //计算完成率
        Double rate = orderTotal == 0 ? 0.0 : validOrderTotal.doubleValue() / orderTotal;
        //封装到vo并且返回
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(vaildOrderList, ","))
                .totalOrderCount(orderTotal)
                .validOrderCount(validOrderTotal)
                .orderCompletionRate(rate)
                .build();
    }

    public Integer getIntegerStatics(LocalDateTime begin, LocalDateTime end, Integer status) {
        HashMap map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        Integer statics = orderMapper.countByMap(map);
        return statics;
    }

    /**
     * 销量统计
     *
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO salesStatistics(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> list = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> nameList = list.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = list.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());


        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    /**
     * 导出运营数据Excel报表
     *
     * @param response
     */
    public void export(HttpServletResponse response) {
        //获取时间信息
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        //获取近30天的BusinessDataVO
        BusinessDataVO vo = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        try {
            //读取template下的模板文件
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //获取sheet页
            XSSFSheet sheet = excel.getSheetAt(0);
            //填充时间信息
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin.toString() + "---" + end.toString() + "                           ");
            //填充概览数据信息(近30天）
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(vo.getTurnover());
            row.getCell(4).setCellValue(vo.getOrderCompletionRate());
            row.getCell(6).setCellValue(vo.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(vo.getValidOrderCount());
            row.getCell(4).setCellValue(vo.getUnitPrice());

            //填充明细数据信息
            for (int i = 0; i < 30; i++) {
                vo = workspaceService.getBusinessData(LocalDateTime.of(begin.plusDays(i), LocalTime.MIN),
                        LocalDateTime.of(begin.plusDays(i), LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(begin.plusDays(i).toString());
                row.getCell(2).setCellValue(vo.getTurnover());
                row.getCell(3).setCellValue(vo.getValidOrderCount());
                row.getCell(4).setCellValue(vo.getOrderCompletionRate());
                row.getCell(5).setCellValue(vo.getUnitPrice());
                row.getCell(6).setCellValue(vo.getNewUsers());
            }
            //将资源存入磁盘
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            //关闭对应资源
            outputStream.close();
            excel.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
