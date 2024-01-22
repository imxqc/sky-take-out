package com.sky.controller.admin;

import com.sky.dto.GoodsSalesDTO;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "数据统计相关接口")
public class ReportController {
    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计相关接口
     *
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计相关接口")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ) {
        log.info("营业额数据统计,{},{}", begin, end);
        return Result.success(reportService.turnoverStatistics(begin, end));
    }

    /**
     * 用户统计相关接口
     *
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计相关接口")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ) {
        log.info("用户数据统计,{},{}", begin, end);
        return Result.success(reportService.userStatistics(begin, end));
    }

    /**
     * 订单统计相关接口
     *
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计相关接口")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ) {
        log.info("订单数据统计,{},{}", begin, end);
        return Result.success(reportService.OrderStatistics(begin, end));
    }

    /**
     * 销量统计相关接口
     *
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    @ApiOperation("销量统计相关接口")
    public Result<SalesTop10ReportVO> sales(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ) {
        log.info("销量数据统计,{},{}", begin, end);
        return Result.success(reportService.salesStatistics(begin, end));
    }

    /**
     * 导出运营数据Excel报表
     * @param response 获取输出流
     */
    @GetMapping("/export")
    @ApiOperation("导出excel")
    public void exportExcel(HttpServletResponse response) {
        reportService.export(response);
    }
}
