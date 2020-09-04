package org.jeecg.modules.tasktime.job;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.quartz.utils.CurrentDayUtils;
import org.jeecg.modules.tasktime.entity.ZtTasktime;
import org.jeecg.modules.tasktime.service.IZtTasktimeService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class workTimeSynchronize implements Job {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private IZtTasktimeService ztTasktimeService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("---------");
        log.info("定时任务 工时信息同步开始！现在时间为："+ DateUtils.getTimestamp());
        //返回今天的星期数，1代表星期一，7代表星期日，0代表异常数据
        int weekOfToday = CurrentDayUtils.getWeekStr(new Date());
        if(weekOfToday != 0){
            ztTaskProcess();
        }
        log.info("--------");
        log.info("发送完成");
    }

    private void ztTaskProcess(){
        //从Redis中读取今天存储的信息
        String userListFromRedis = getRedisString(CurrentDayUtils.getTodaytoYYYYMMDD() + "notEnough");
        //把Redis中存储的数据按人分隔开，存储到字符串数组中
        String[] userListOriginal = userListFromRedis.replace("\n","").split("h");
        //这个信息转化到实体
        List<ZtTasktime> ZtTaskList = new ArrayList<ZtTasktime>();
        //如果读取的数据不为空，要进行处理
        if(!userListFromRedis.isEmpty()){
            log.info("-----------");
            log.info("完成信息处理");
            userProcess(userListOriginal);
        }
    }

    /**
     * 从Redis中根据文件名读取信息
     * @return
     */
    private String getRedisString(String s){
        Object obj = redisUtil.get(s);
        if(null != obj){
            log.info("-----------");
            log.info("读取出Redis中的信息");
            return obj.toString();
        }
        return "";
    }

    /**
     * 把Redis中的信息读出，并存储为UserEntity实体
     * @param strs 从Redis中读出的并且经过人员分解过的字符串数组
     * @return
     */
    private void userProcess(String[] strs){
        String name = null;
        String taskTimeString = null;
        Double taskTime = null;
        ZtTasktime zt = null;
        for(String s : strs){
            name = stringProcess(s,0);
            taskTimeString = stringProcess(s,1);
            taskTime = Double.parseDouble(taskTimeString);

            zt = new ZtTasktime();
            zt.setTasktime(taskTime);
            zt.setName(name);
            zt.setCreateTime(new Date());
            ztTasktimeService.save(zt);
        }
        log.info("-----------");
        log.info("对用户和工时进行处理");

    }

    /**
     *
     * @return
     */
    private String stringProcess(String s,int flag){
        //截取出姓名
        if(flag == 0){
            return s.substring(s.indexOf("姓名:") + 3,s.indexOf("工作时长:"));
        }
        //截取出工作时长
        if(flag == 1){
            return s.substring(s.indexOf("工作时长") + 5);
        }
        return "";
    }
}
