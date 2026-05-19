package com.catering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catering.entity.Report;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {
    @Select("SELECT * FROM report WHERE status = #{status} ORDER BY create_time DESC")
    List<Report> selectByStatus(@Param("status") String status);

    @Select("SELECT * FROM report WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Report> selectByUserId(@Param("userId") Long userId);

    @Update("UPDATE report SET status = #{status}, handler_id = #{handlerId}, handle_result = #{handleResult}, handle_time = NOW() WHERE report_id = #{reportId}")
    void handleReport(@Param("reportId") Long reportId, @Param("status") String status, 
                      @Param("handlerId") Long handlerId, @Param("handleResult") String handleResult);
}