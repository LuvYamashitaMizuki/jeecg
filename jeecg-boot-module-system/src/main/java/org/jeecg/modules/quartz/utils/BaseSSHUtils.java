package org.jeecg.modules.quartz.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
    public  class BaseSSHUtils {

        static int localPort = 6611;// 本地端口
        static String remoteHost = "127.0.0.1";// 远程MySQL服务器
        static int remotePort = 9877;// 远程MySQL服务端口
        private static List<Map<String, Object>> list = null;
        private static int assinged_port = 0;

        public static void startSSH(String ssh_User, String ssh_Password, String ssh_Host) throws JSchException {
            // SSH连接用户名
            String sshUser = ssh_User;
            // SSH连接密码
            String sshPassword = ssh_Password;
            // SSH服务器
            String sshHost = ssh_Host;
            // SSH访问端口
            int sshPort = 22;
            JSch jsch = new JSch();
            Session session = jsch.getSession(sshUser, sshHost, sshPort);
            session.setPassword(sshPassword);
            // 设置第一次登陆的时候提示，可选值：(ask | yes | no)
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            // 打印SSH服务器版本信息
            System.out.println(session.getServerVersion());
            // 设置SSH本地端口转发,本地转发到远程

            if (BasePortVerify.isPortAvailable(localPort)) {
                assinged_port = session.setPortForwardingL(localPort, remoteHost, remotePort);
            }

            // 删除本地端口的转发
            // session.delPortForwardingL(localPort);
            // 断开SSH链接
            // session.disconnect();
            // 设置SSH远程端口转发,远程转发到本地
            // session.setPortForwardingR(remotePort, remoteHost, localPort);
            System.out.println("localhost:" + assinged_port + " -> " + remoteHost + ":" + remotePort);
            log.info("SSH成功开启");
        }

        /**
         * @param url      数据库映射后链接
         * @param user     用户名
         * @param password 密码
         * @throws Exception
         */
        public static List<Map<String, Object>> runSSH(String url, String user, String password, String sql) throws Exception {
            Connection conn = null;
            Statement st = null;
            ResultSet rs = null;
            list = new ArrayList<Map<String, Object>>();

            try {
                Class.forName("com.mysql.jdbc.Driver");
                // 设置SSH本地端口转发后，访问本地ip+port就可以访问到远程的ip+port
                conn = DriverManager.getConnection(url, user, password);
                st = conn.createStatement();
                rs = st.executeQuery(sql);
                Map<String, Object> dataMap = null;
                while (rs.next()) {
                    //暂时处理,解决方法如下
                    ResultSetMetaData rsMeta = rs.getMetaData();
                    int columnCount = rsMeta.getColumnCount();
                    dataMap = new HashMap<String, Object>();
                    for (int i = 1; i <= columnCount; i++) {

                        dataMap.put(rsMeta.getColumnLabel(i), rs.getObject(i));
                    }
                    list.add(dataMap);
                }
                System.out.println(list.toString());

            } catch (Exception e) {
                throw e;
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (st != null) {
                    st.close();
                    st = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            return list;
        }

        /**
         * 带端口startSSH
         *
         * @param ssh_User
         * @param ssh_Password
         * @param ssh_Host
         * @throws JSchException
         */
        public static void startSSHPastPort(String ssh_User, String ssh_Password, String ssh_Host, int port) throws  JSchException {
            // SSH连接用户名
            String sshUser = ssh_User;
            // SSH连接密码
            String sshPassword = ssh_Password;
            // SSH服务器
            String sshHost = ssh_Host;
            // SSH访问端口
            int sshPort = 22;
            JSch jsch = new JSch();
            Session session = jsch.getSession(sshUser, sshHost, sshPort);
            session.setPassword(sshPassword);
            // 设置第一次登陆的时候提示，可选值：(ask | yes | no)
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            // 打印SSH服务器版本信息
            System.out.println(session.getServerVersion());
            // 设置SSH本地端口转发,本地转发到远程

            if (BasePortVerify.isPortAvailable(port)) {
                assinged_port = session.setPortForwardingL(port, remoteHost, remotePort);
            }

            // 删除本地端口的转发
            // session.delPortForwardingL(localPort);
            // 断开SSH链接
            // session.disconnect();
            // 设置SSH远程端口转发,远程转发到本地
            // session.setPortForwardingR(remotePort, remoteHost, localPort);
            System.out.println("localhost:" + port + " -> " + remoteHost + ":" + remotePort);

        }

        /**
         * @param url      数据库映射后链接
         * @param user     用户名
         * @param password 密码
         * @throws Exception
         */
        public static List<ConcurrentHashMap<String, Object>> runSSHPastPortConcurrent(String url, String user, String password, String sql) throws Exception {
            Connection conn = null;
            Statement st = null;
            ResultSet rs = null;
            List<ConcurrentHashMap<String, Object>> listMap = new ArrayList<ConcurrentHashMap<String, Object>>();

            try {
                Class.forName("com.mysql.jdbc.Driver");
                // 设置SSH本地端口转发后，访问本地ip+port就可以访问到远程的ip+port
                conn = DriverManager.getConnection(url, user, password);
                st = conn.createStatement();
                rs = st.executeQuery(sql);
                ConcurrentHashMap<String, Object> dataMap = null;
                while (rs.next()) {
                    //暂时处理,解决方法如下
                    ResultSetMetaData rsMeta = rs.getMetaData();
                    int columnCount = rsMeta.getColumnCount();
                    dataMap = new ConcurrentHashMap<String, Object>();
                    for (int i = 1; i <= columnCount; i++) {

                        dataMap.put(rsMeta.getColumnLabel(i), rs.getObject(i));
                    }
                    listMap.add(dataMap);
                }
                System.out.println(listMap.toString());

            } catch (Exception e) {
                throw e;
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (st != null) {
                    st.close();
                    st = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            return listMap;
        }


        public static void main(String[] args) throws Exception {
            BaseSSHUtils.startSSH("root", "qiyi123!@#", "172.16.1.103");
            BaseSSHUtils.runSSH("jdbc:mysql://localhost:6611/zentao?autoReconnect=true", "project", "123456", "select * from zt_action limit 10");
        }
    }



