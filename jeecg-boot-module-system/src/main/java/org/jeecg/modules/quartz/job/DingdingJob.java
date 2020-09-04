package org.jeecg.modules.quartz.job;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.quartz.entity.TaskEntity;
import org.jeecg.modules.quartz.entity.UserEntity;
import org.jeecg.modules.quartz.entity.WorktimeEntity;
import org.jeecg.modules.quartz.utils.BaseSSHUtils;
import org.jeecg.modules.quartz.utils.CurrentDayUtils;
import org.jeecg.modules.quartz.utils.HolidayUtils;
import org.jeecg.modules.quartz.utils.JavaBeanUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

//这个是log日志的快速注解，相当于private  final Logger logger = LoggerFactory.getLogger(当前类名.class)
@Slf4j
public class DingdingJob implements Job {
    @Resource
    private RedisUtil redisUtil;

    //SSH的HOST
    private static String sshHost = "172.16.1.103";
    //SSH的User
    private static String sshUser = "root";
    //SSH的password
    private static String sshPassword = "qiyi123!@#";

    //Des目标数据库用户名
    private static String desUser = "project";
    //Des目标数据库密码
    private static String desPassword = "123456";

    //需要自己更改url
    private String dingUrl = "https://oapi.dingtalk.com/robot/send?access_token=d7e8d55ce42ae1e909c1cfb95e3963ae77cd25054bab035933705203eaac7ac3";
    // private String dingKey = "SEC40c9dbec073ea489eee005cfe847b0ad9851d1d20902e00957e99bc337745d3d";
    private static String sshUrl = "jdbc:mysql://localhost:6611/zentao?autoReconnect=true&zeroDateTimeBehavior=convertToNull";


    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info(String.format(" 钉钉定时任务  时间:" + DateUtils.getTimestamp()));
        log.error(String.format("测试"));
        //1.日期处理，调用api返回的信息判断
        //2.读取人员信息，进行部门过滤，进行白名单过滤
        //3.读取工时信息，对应第二步的人员，把消耗的工时信息加和
        //4.把小于8小时的人员信息写入redis
        //5.对第四步中的数据，再进行请假人员的过滤
        //6.把过滤后的数据发送出

        //返回今天的星期数，1代表星期一，7代表星期日，0代表异常数据
        int weekOfToday = CurrentDayUtils.getWeekStr(new Date());
        //返回今天是否是工作日，0代表工作日，1代表周末，2代表节假日
        Integer ifTodayWeekday = HolidayUtils.HolidayGet(CurrentDayUtils.getTodaytoYYYYMMDD());

        if (weekOfToday != 0) {
            BaseSSHUtils.startSSH(sshUser,sshPassword,sshHost);
            //从数据库中读出所有用户的数据，zt_user表是目标表
            String userSql = "select * from zt_user";
            //要把用户信息进行存储，用反射找到需要存储的内容
            List<Map<String, Object>> listUserFromDatabase = BaseSSHUtils.runSSH(sshUrl, desUser, desPassword, userSql);
            //把数据库中的数据进行类型转换后存储的地方
            List<UserEntity> listUserEntityOriginal = new ArrayList<UserEntity>();
            userProcess(listUserFromDatabase,listUserEntityOriginal);
            List<Integer> whiteListDept = new ArrayList<Integer>(new HashSet<Integer>());
            //部门白名单建立
            addWhitelistDept(whiteListDept);
            //用户白名单建立
            List<String> whiteList = new ArrayList<String>();
            addWhitelist(whiteList);
            //进行用户和白名单的过滤
            List<UserEntity> listUserEntityhasSifted = new ArrayList<UserEntity>();
            listUserEntityhasSifted = deptSift(listUserEntityOriginal, whiteListDept, whiteList);
            //过滤后人员名单拼接,以'','',格式
            StringBuffer userListStr = null;
            for(UserEntity ue : listUserEntityhasSifted){
                if (null != userListStr) {
                    userListStr.append("," + "'" + ue.getAccount() + "'");
                }
                else {
                    userListStr = new StringBuffer();
                    userListStr.append("'" + ue.getAccount() + "'");
                }
            }

            //工时查询
            String ztTaskSQL = "select * from zt_taskestimate zt where zt.account in (" + userListStr + ")\n" +
            					"and date>= '" + CurrentDayUtils.getBeforeDay() + "'\n" +
            					"and date<'" + CurrentDayUtils.getAfterDay() + "'";
            //在数据库中查找工时信息
            List<Map<String, Object>> listTaskFromDatabase = BaseSSHUtils.runSSH(sshUrl, desUser, desPassword, ztTaskSQL);
            //把想要的数据存储起来
            List<TaskEntity> listTaskEntityOriginal = new ArrayList<TaskEntity>();
            //对数据库信息和工时信息进行处理
            taskProcess(listTaskFromDatabase,listTaskEntityOriginal);
            //存储请假信息 表名需要变化

//            String WorktimeSQL = "select * from zt_leave where zt.account in (" + userListStr + ")\n"                   + "and date>= '" + CurrentDayUtils.getBeforeDay() + "'\n"
//                    + "and date<'" + CurrentDayUtils.getAfterDay() + "'";
//            List<Map<String, Object>> worktimeFromDatabase = BaseSSHUtils.runSSH(sshUrl, desUser, desPassword,WorktimeSQL);
////            //把想要的数据存储起来
//            List<WorktimeEntity> worktimeEntityOriginal = new ArrayList<WorktimeEntity>();
//            worktimeProcess(worktimeFromDatabase,worktimeEntityOriginal);
//            //计算工时，把工时信息存储到这个User中
//            calculateWorktime(listUserEntityhasSifted,worktimeEntityOriginal);
            calculateTasktime(listUserEntityhasSifted, listTaskEntityOriginal);
            //两个字符串变量分别存储工时不足以及工时充足的人员名单
            StringBuffer notEnough = new StringBuffer("");
            StringBuffer isEnough = new StringBuffer("");
            //对工时充足和工时不足的名单进行拼接
            processTasktime(listUserEntityhasSifted, notEnough, isEnough);
            //把工时充足和工时不足的信息发送到Redis中
            sendToRedis(CurrentDayUtils.getTodaytoYYYYMMDD(), notEnough, isEnough);

            //对工作日进行发送操作，但不涉及调休，
            if (ifTodayWeekday == 0) {
                sendToDingding(weekOfToday,notEnough);
            }
            //对url和key进行处理，发送出信息。
            //测试
            log.info("------------");
            log.info("------------");
            log.info("发送成功");


        }

    }

    /**
     * 从数据库中读出的用户信息
     *
     * @param list1 把UserEntity类中的信息读取出来
     * @param list2
     * @throws Exception
     */
    private void userProcess(List<Map<String, Object>> list1, List<UserEntity> list2) throws Exception {
        List<Object> listObj = new ArrayList<Object>();
        for (Map<String, Object> map : list1) {
            listObj.add(JavaBeanUtils.map2JavaBean(UserEntity.class, map));
        }
        UserEntity ue = null;
        for (Object obj : listObj) {
            ue = (UserEntity) obj;
            list2.add(ue);
        }
        log.info("------------");
        log.info("用户信息反射完成。");
    }

    /**
     * 从数据库中读出任务信息
     *
     * @param list1 把TaskEntity类中的信息读取出来
     * @param list2
     * @throws Exception
     */
    private void taskProcess(List<Map<String, Object>> list1, List<TaskEntity> list2) throws Exception {
        List<Object> listObj = new ArrayList<Object>();
        for (Map<String, Object> map : list1) {
            listObj.add(JavaBeanUtils.map2JavaBean(TaskEntity.class, map));
        }
        TaskEntity te = null;
        for (Object obj : listObj) {
            te = (TaskEntity) obj;
            list2.add(te);
        }
        log.info("------------");
        log.info("任务信息反射完成。");
    }

    /**
     * 部门白名单建立，需要对照部门的编号进行add操作
     *
     * @param whiteListDept
     */
    private void addWhitelistDept(List<Integer> whiteListDept) {
        whiteListDept.add(0);
        whiteListDept.add(10);
        whiteListDept.add(11);
        whiteListDept.add(12);
        whiteListDept.add(13);
        whiteListDept.add(14);
        whiteListDept.add(15);
        whiteListDept.add(16);
        whiteListDept.add(32);
        whiteListDept.add(33);
        whiteListDept.add(34);
        whiteListDept.add(35);
        log.info("------------");
        log.info("部门白名单建立完成。");
    }

    /**
     * 对用户白名单进行操作,维护白名单在append后添加类似语句",添加人姓名"
     */
    private void addWhitelist(List<String> whiteList) {
        StringBuffer stringbufferwhitelist = new StringBuffer();
        stringbufferwhitelist.append("李仑");
        stringbufferwhitelist.append(",李嘉");
        stringbufferwhitelist.append(",李玉洁");
        stringbufferwhitelist.append("");
        String[] vips = stringbufferwhitelist.toString().split(",");
        for (String whitelistinProcess : vips) {
            whiteList.add(whitelistinProcess);
        }
        log.info("------------");
        log.info("用户白名单建立完成。");
    }

    /**
     * 进行部门以及白名单的处理
     *
     * @param list1
     * @param listDept
     * @return
     */
    private List<UserEntity> deptSift(List<UserEntity> list1, List<Integer> listDept, List<String> whiteList) {
        List<UserEntity> listInProcess = new ArrayList<>();
        if (list1 != null) {
            for (UserEntity user : list1) {
                //对已删除的人员进行过滤
                boolean ifdeleted = Integer.valueOf(user.getDeleted()) == 1;
                //对部门白名单进行过滤
                boolean deptSift = listDept.contains(user.getDept());
                //对用户白名单进行过滤
                boolean nameSift = whiteList.contains(user.getRealname());
                if (!ifdeleted && !deptSift && !nameSift) {
                    user.setConsumed(0.0);
                    listInProcess.add(user);
                }
            }
            log.info("------------");
            log.info("白名单过滤完成。");
            return listInProcess;

        } else {
            return null;
        }
    }

    /**
     * 对照用户名单，把工时信息累加到用户名单中
     *
     * @param list1
     * @param list2
     */
    private void calculateTasktime(List<UserEntity> list1, List<TaskEntity> list2) {
        for (UserEntity ue : list1) {
            for (TaskEntity te : list2) {
                if (ue.getAccount().equalsIgnoreCase(te.getAccount())) {
                    ue.setConsumed(ue.getConsumed() + te.getConsumed());
                }
            }
        }
        log.info("------------");
        log.info("工时信息累加。");
    }

    /**
     * 对每天应有的工时进行处理。
     */
    private void worktimeProcess(List<Map<String,Object>> list1,List<WorktimeEntity> list2) throws Exception {
        List<Object> listObj = new ArrayList<Object>();
        for (Map<String, Object> map : list1) {
            listObj.add(JavaBeanUtils.map2JavaBean(WorktimeEntity.class, map));
        }
        WorktimeEntity wte = null;
        for (Object obj : listObj) {
            wte = (WorktimeEntity) obj;
            list2.add(wte);
        }
        log.info("------------");
        log.info("工时信息统计完成。");
    }

    /**
     * 计算每个人应有的工时
     * @param list1
     * @param list2
     */
    private void calculateWorktime(List<UserEntity> list1,List<WorktimeEntity> list2){
        for (UserEntity ue : list1) {
            for (WorktimeEntity wte : list2) {
                if (ue.getAccount().equalsIgnoreCase(wte.getAccount())) {
                    ue.setWorktime(8 - wte.getLength());
                }
            }
        }
    }

    /**
     * 将工时充足和工时不足的用户进行字符串拼接
     *
     * @param list1
     */
    private void processTasktime(List<UserEntity> list1, StringBuffer notEnough, StringBuffer isEnough) {
        for (UserEntity ue : list1) {
            //异常数据均设置为0
            if (ue.getConsumed() <= 0) {
                ue.setConsumed(0.0);
            }
            //工时小于8
            if (ue.getConsumed() < 8 - ue.getWorktime()) {
                processMessage(notEnough, ue);
            }
            //工时大于等于8
            else {
                processMessage(isEnough, ue);
            }
        }
    }

    /**
     * 把信息进行拼接
     *
     * @param message
     * @param ue
     * @return
     */
    private StringBuffer processMessage(StringBuffer message, UserEntity ue) {
        message.append("\n");
        message.append("姓名:" + ue.getRealname()).append("\n");
        message.append("工作时长:" + formatToNumber(new BigDecimal(ue.getConsumed()))).append("h\n\n");
//        message.append("工作饱和度:" + formatToNumber(new BigDecimal(ue.getConsumed() / 8.0 * 100)) + "%").append("\n\n");
        return message;
    }

    /**
     * 格式化输出一位小数
     *
     * @param obj
     * @return
     */
    private static String formatToNumber(BigDecimal obj) {
        DecimalFormat df = new DecimalFormat("#.0");
        if (obj.compareTo(BigDecimal.ZERO) == 0) {
            return "0.0";
        } else if (obj.compareTo(BigDecimal.ZERO) > 0 && obj.compareTo(new BigDecimal(1)) < 0) {
            return "0" + df.format(obj).toString();
        } else {
            return df.format(obj).toString();
        }
    }

    /**
     * 把信息发送到Redis中
     *
     * @param todayString
     * @param notEnough
     * @param isEnough
     */
    private void sendToRedis(String todayString, StringBuffer notEnough, StringBuffer isEnough) {
        redisUtil.set(todayString + "notEnough", notEnough);
        redisUtil.set(todayString + "isEnough", isEnough);
        log.info("------------");
        log.info("信息发送到Redis中");
    }

    /**
     * 对每天是否发送进行判断，特别是周一周六周日，暂时未完成
     * @param week
     */
    private void sendToDingding(int week,StringBuffer notEnough) throws ApiException {
        if(week != 6 && week != 7){
            dingSetProcess(dingUrl,notEnough);
            log.info("------------");
            log.info("钉钉信息已输出，今天不为周末");
        }
        else {
            log.info("------------");
            log.info("钉钉信息不输出，今天是周末");
        }
    }

//    /**
//     * 根据内容进行url的处理
//     * @param secret
//     * @param dingUrl
//     * @return
//     * @throws Exception
//     */
//    private String dingUrlProcess(String secret,String dingUrl) throws Exception {
//        //签名方法
//        Long timeStamp = System.currentTimeMillis();
//
//        String StringToSign = timeStamp + "\n" + secret;
//
//        Mac mac = Mac.getInstance("HmacSHA256");
//        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
//        byte[] signData= mac.doFinal(StringToSign.getBytes("UTF-8"));
//        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
//
//        String url = dingUrl +"&"+ timeStamp.toString()+"&"+ sign;
//        return url;
//    }

    /**
     * 钉钉发送信息处理
     * @param accessToken
     * @param notEnough
     * @throws ApiException
     */
    private void dingSetProcess(String accessToken,StringBuffer notEnough) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(accessToken);
        OapiRobotSendRequest request = new OapiRobotSendRequest();

        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setIsAtAll(true);
        request.setAt(at);

        // 若上一步isAtAll没有设置true，则根据此处设置的手机号来@指定人
        List<String> mobiles = new ArrayList<>();
        mobiles.add("166********");
        at.setAtMobiles(mobiles);

        // 以下是设置各种消息格式的方法
        //sentText(request);
        //sendLink(request);
        if(!notEnough.toString().equalsIgnoreCase("")){
            sendMarkdown(request,notEnough);
            log.info("------------");
            log.info("输出名单有人");
        }else{
            sendMarkdown1(request);
            log.info("------------");
            log.info("输出名单为空");
        }

        //    sendActionCard(request);
        //    sendFeedCard(request);

        OapiRobotSendResponse response = client.execute(request);
        System.out.println(response.getErrmsg());
    }

    /**
     * 配合处理markdown类型的数据
     * @param request
     * @param notEnough1 工时不足人名单
     */
    private void sendMarkdown(OapiRobotSendRequest request,StringBuffer notEnough1) {
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle("工时信息");
        String date = DateUtils.formatDate();
            markdown.setText("今天是"+ date + "\n\n" +
                    "> 早上好！以下为工时不满8小时人员名单，请及时补录工时。\n\n" +
                    "> " + notEnough1 + "\n\n" +
                    "> ![screenshot](https://s1.ax1x.com/2020/08/28/do1jp9.png)\n" +
                    "> ###### 9点30分发布 统计时间为上个工作日 \n");

        request.setMsgtype("markdown");
        request.setMarkdown(markdown);

    }

    private void sendMarkdown1(OapiRobotSendRequest request) {
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle("工时信息");
        String date = DateUtils.formatDate();
             StringBuffer notEnough1 = new StringBuffer();
            markdown.setText("今天是"+ date + "\n\n" +
                    "> 早上好！\n\n" +
                    "> 所有人工时都填写完成。\n\n" +
                    "> ![screenshot](https://s1.ax1x.com/2020/08/28/do1jp9.png)\n" +
                    "> ###### 9点30分发布 统计时间为上个工作日 \n");


        request.setMsgtype("markdown");
        request.setMarkdown(markdown);

    }


    /**
     * 处理text类型的数据
     * @param request
     */
    private void sentText(OapiRobotSendRequest request) {
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent("sdk demo");
        request.setMsgtype("text");
        request.setText(text);

    }

    /**
     * 其他格式的测试数据
     * @param url
     * @throws ApiException
     */
    private void dingMessageProcess(String url) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(url);
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent("测试文本消息");
        request.setText(text);
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setIsAtAll(true);
        request.setAt(at);

        request.setMsgtype("link");
        OapiRobotSendRequest.Link link = new OapiRobotSendRequest.Link();
        link.setMessageUrl("https://www.dingtalk.com/");
        link.setPicUrl("");
        link.setTitle("时代的火车向前开");
        link.setText("这个即将发布的新版本，创始人xx称它为红树林。而在此之前，每当面临重大升级，产品经理们都会取一个应景的代号，这一次，为什么是红树林");
        request.setLink(link);

        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle("杭州天气");
        markdown.setText("#### 杭州天气 @156xxxx8827\n" +
                "> 9度，西北风1级，空气良89，相对温度73%\n\n" +
                "> ![screenshot](https://gw.alicdn.com/tfs/TB1ut3xxbsrBKNjSZFpXXcXhFXa-846-786.png)\n" +
                "> ###### 10点20分发布 [天气](http://www.thinkpage.cn/) \n");
        request.setMarkdown(markdown);
        OapiRobotSendResponse response = client.execute(request);
    }

}


