package com.hty.baseframe.jproxy.util;

import com.hty.baseframe.common.bean.DomElement;
import com.hty.baseframe.common.util.StringUtil;
import com.hty.baseframe.common.util.XmlParser;
import com.hty.baseframe.jproxy.bean.LocalService;
import com.hty.baseframe.jproxy.bean.RegistryCenter;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.common.BeanProvider;
import com.hty.baseframe.jproxy.common.RegistryFactory;
import com.hty.baseframe.jproxy.common.ServiceFactory;
import com.hty.baseframe.jproxy.common.SysProperties;
import com.hty.baseframe.jproxy.exception.IllegalConfigurationException;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.List;

/**
 * 配置加载工具类
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class ConfigLoader {

    public void load(String conf) throws Exception {
        XmlParser xmlParser = XmlParser.getInstance();
        Document doc;
        InputStream ips = this.getClass().getClassLoader().getResourceAsStream(conf);
        doc = xmlParser.getDocument(ips);
        List<DomElement> eles = xmlParser.parse(doc);
        if (null != eles)
            for (int i = 0; i < eles.size(); i++) {
                if (i > 0) {
                    throw new IllegalStateException("Configuration file error:\n\tRoot document 'baseframe:jproxy' must be unique.");
                }
                if ("baseframe:jproxy".equals(eles.get(i).getName())) {
                    parseProperties(eles.get(i).getElements(), conf);
                    parseLocalService(eles.get(i).getElements());
                    parseRemoteService(eles.get(i).getElements());
                    parseRegisterCenter(eles.get(i).getElements());
                }
            }
    }

    /**
     * 解析属性
     */
    private void parseProperties(List<DomElement> eles, String conf)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (null != eles)
            for (DomElement ele : eles) {
                if ("properties".equals(ele.getName())) {
                    List<DomElement> peles = ele.getElements();
                    if (null != peles)
                        for (DomElement pele : peles) {
                            if ("property".equals(pele.getName())) {
                                String name = StringUtil.trim(pele.getAttribute("name"));
                                String value = StringUtil.trim(pele.getAttribute("value"));
                                if ("bean_provider".equals(name)) {
                                    try {
                                        Object beanProvider = Class.forName(value).newInstance();
                                        if (beanProvider instanceof BeanProvider) {
                                            ServiceFactory.setBeanProvider((BeanProvider) beanProvider);
                                        } else {
                                            throw new IllegalConfigurationException("Property 'bean_provider' is not instance of class " + BeanProvider.class.getName() + " at file '" + conf + "'.");
                                        }
                                    } catch (ClassNotFoundException e) {
                                        throw new ClassNotFoundException("Can not Instance BeanProvider " + value);
                                    } catch (IllegalConfigurationException e) {
                                        throw e;
                                    }
                                }
                                SysProperties.addProperty(name, value);
                            }
                        }
                }
            }
    }

    /**
     * 解析本地服务
     */
    private void parseLocalService(List<DomElement> eles) throws ClassNotFoundException {
        if (null != eles)
            for (DomElement ele : eles) {
                if ("service:local".equals(ele.getName())) {
                    List<DomElement> peles = ele.getElements();
                    if (null != peles)
                        for (DomElement pele : peles) {
                            if ("lservice".equals(pele.getName())) {
                                String token = StringUtil.trim(pele.getAttribute("token"));
                                String clazzstr = StringUtil.trim(pele.getAttribute("class"));
                                String version = StringUtil.trim(pele.getAttribute("version"));
                                String center = StringUtil.trim(pele.getAttribute("center"));
                                if (StringUtil.isEmpty(version)) {
                                    version = null;
                                }
                                if (StringUtil.isEmpty(center)) {
                                    center = null;
                                }
                                if (StringUtil.isEmpty(clazzstr)) {
                                    throw new IllegalConfigurationException("local service's attribute 'class' can not be null!");
                                }
                                Class<?> clazz = Class.forName(clazzstr);
                                LocalService ls = new LocalService(clazz, center);
                                List<DomElement> heles = pele.getElements();
                                if (null != heles)
                                    for (DomElement hele : heles) {
                                        if ("conditions".equals(hele.getName())) {
                                            List<DomElement> wheles = hele.getElements();
                                            if (null != wheles)
                                                for (DomElement whele : wheles) {
                                                    if ("condition".equals(whele.getName())) {
                                                        String name = StringUtil.trim(whele.getAttribute("name"));
                                                        String value = StringUtil.trim(whele.getAttribute("value"));
                                                        ls.addCondition(name, value);
                                                    }
                                                }
                                        }
                                    }
                                ServiceFactory.addLocalService(ls);
                            }
                        }
                }
            }
    }

    /**
     * 解析远程服务
     */
    private void parseRemoteService(List<DomElement> eles) throws ClassNotFoundException {
        if (null != eles)
            for (DomElement ele : eles) {
                if ("service:remote".equals(ele.getName())) {
                    List<DomElement> peles = ele.getElements();
                    if (null != peles)
                        for (DomElement pele : peles) {
                            if ("rservice".equals(pele.getName())) {
                                String clazzName = StringUtil.trim(pele.getAttribute("class"));
                                String host = StringUtil.trim(pele.getAttribute("host"));
                                String port = StringUtil.trim(pele.getAttribute("port"));
                                String center = StringUtil.trim(pele.getAttribute("center"));
                                if (StringUtil.isEmpty(center)) {
                                    center = null;
                                }

                                if (StringUtil.isEmpty(clazzName)) {
                                    throw new IllegalConfigurationException("remote service's attribute 'clazz' can not be null!");
                                }
                                if (null == center) {
                                    if (StringUtil.isEmpty(host)) {
                                        throw new IllegalConfigurationException("remote service's attribute 'host' can not be null!");
                                    }
                                    if (StringUtil.isEmpty(port)) {
                                        throw new IllegalConfigurationException("remote service's attribute 'port' can not be null!");
                                    }
                                }
                                Class<?> clazz = Class.forName(clazzName);
                                RemoteService rs = new RemoteService(clazz, host, port, center);

                                List<DomElement> heles = pele.getElements();
                                if (null != heles)
                                    for (DomElement hele : heles) {
                                        if ("conditions".equals(hele.getName())) {
                                            List<DomElement> wheles = hele.getElements();
                                            if (null != wheles)
                                                for (DomElement whele : wheles) {
                                                    if ("condition".equals(whele.getName())) {
                                                        String name = StringUtil.trim(whele.getAttribute("name"));
                                                        String value = StringUtil.trim(whele.getAttribute("value"));
                                                        rs.addCondition(name, value);
                                                    }
                                                }
                                        }
                                    }
                                ServiceFactory.addRemoteService(rs);
                            }
                        }
                }
            }
    }

    /**
     * 解析注册中心
     */
    private void parseRegisterCenter(List<DomElement> eles) {
        if (null != eles)
            for (DomElement ele : eles) {
                if ("registry".equals(ele.getName())) {
                    List<DomElement> peles = ele.getElements();
                    if (null != peles)
                        for (DomElement pele : peles) {
                            if ("center".equals(pele.getName())) {
                                String id = StringUtil.trim(pele.getAttribute("id"));
                                String host = StringUtil.trim(pele.getAttribute("host"));
                                String _port = StringUtil.trim(pele.getAttribute("port"));
                                if (StringUtil.isEmpty(id)) {
                                    throw new IllegalConfigurationException("registry center's attribute 'id' can not be null!");
                                }
                                if (StringUtil.isEmpty(host)) {
                                    throw new IllegalConfigurationException("registry center's attribute 'host' can not be null!");
                                }
                                if (StringUtil.isEmpty(_port)) {
                                    throw new IllegalConfigurationException("registry center's attribute 'port' can not be null!");
                                }
                                int port = 0;
                                try {
                                    port = Integer.valueOf(_port);
                                } catch (NumberFormatException e) {
                                    throw new IllegalConfigurationException("registry center's attribute 'port' must be a positive integer.");
                                }
                                RegistryCenter center = new RegistryCenter(id, host, port);
                                RegistryFactory.getInstance().addRegistryCenter(center);
                            }
                        }
                }
            }
    }

}
