package org.jeecg.modules.quartz.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
    public class BasePortVerify {
        /**
         * 查看端口是否被占用
         * @param port
         * @return
         */
        @SuppressWarnings("unused")
        public static boolean isPortAvailable(int port) {
            Socket s = new Socket();
            boolean flag=false;
            String f="";
            List<String> ipList=new ArrayList<String>();
            try {
                Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                        .getNetworkInterfaces();
                while (netInterfaces.hasMoreElements()) {
                    NetworkInterface nif = netInterfaces.nextElement();
                    Enumeration<InetAddress> iparray = nif.getInetAddresses();
                    while (iparray.hasMoreElements()) {
                        ipList.add(iparray.nextElement().getHostAddress());
                    }
                }

                for (int i = 0; i < ipList.size(); i++) {
                    flag=bindPort(ipList.get(i), port);
                    if(!flag){
                        break;
                    }
                }

                if(flag){//未被占用
                    return true;
                }else{//已被占用
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * 查看是否能绑定ip端口
         * @param host
         * @param port
         * @return
         * @throws Exception
         */
        @SuppressWarnings("finally")
        private static Boolean bindPort(String host, int port) throws Exception {
            boolean flag=false;
            try {
                Socket s = new Socket();
                s.bind(new InetSocketAddress(host, port));
                s.close();
                flag=true;
            } catch (Exception e) {
                flag=false;
                //e.printStackTrace();
            } finally{
                return flag;
            }
        }

        public static void main(String[] args) {
            boolean flag=false;
            flag=isPortAvailable(6611);
            if(flag){
                System.out.println("未被占用");
            }else{
                System.out.println("已占有");
            }
        }
    }


