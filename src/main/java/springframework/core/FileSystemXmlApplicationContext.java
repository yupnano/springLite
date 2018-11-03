
package springframework.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author PanHuizhi [Phz50@163.com]
 *
 */
public class FileSystemXmlApplicationContext implements ApplicationContext{

    // 存放配置文件信息   
    private static Map<String, Bean> beansMap;

    /**
     * 读取配置文件  
     */
    public FileSystemXmlApplicationContext(String fileName) {
        if (null == beansMap)
            beansMap = XmlBeanDefinitionReader.readXmlFile(fileName);
    }

    /**
     * 根据Bean Id 生成 Bean  
     *
     * @param beanId
     * @return
     */
    public Object getBean(String beanId) {
        Bean bean = (Bean) beansMap.get(beanId);
        Object obj = BeanProcesser.newInstance(bean.getType());
        setProperty(bean, obj);
        return obj;
    }

    // 依赖注入   
    private void setProperty(Bean bean, Object obj) {

        Map<String, Object> propertiesMap = bean.getProperties();

        Iterator<String> keysIterator = propertiesMap.keySet().iterator();
        while (keysIterator.hasNext()) {
            String property = keysIterator.next();
            Object value = propertiesMap.get(property);

            // 处理 value   
            if (value instanceof String) {
                BeanProcesser.setProperty(obj, property, (String) value);
            }

            // 处理 ref   
            if (value instanceof String[]) {
                String[] strsValue = (String[]) value;
                if (strsValue.length == 1) {
                    String beanId = ((String[]) value)[0];
                    BeanProcesser.setProperty(obj, property, getBean(beanId));
                }
            }

            // 处理 list   
            if (value instanceof List) {
                Iterator<?> valuesIterator = ((List<?>) value).iterator();
                List<Object> valuesList = new ArrayList<Object>();
                while (valuesIterator.hasNext()) {
                    Object valueList = (Object) valuesIterator.next();
                    if (valueList instanceof String[]) {
                        valuesList.add(getBean(((String[]) valueList)[0]));
                    }
                }
                BeanProcesser.setProperty(obj, property, valuesList);
            }

            // 处理 map   
            if (value instanceof Map) {
                Iterator<?> entryIterator = ((Map<?, ?>) value).entrySet()
                        .iterator();
                Map<String, Object> map = new HashMap<String, Object>();
                while (entryIterator.hasNext()) {
                    Entry<?, ?> entryMap = (Entry<?, ?>) entryIterator.next();
                    if (entryMap.getValue() instanceof String[]) {
                        map.put((String) entryMap.getKey(),
                                getBean(((String[]) entryMap.getValue())[0]));
                    }
                }
                BeanProcesser.setProperty(obj, property, map);
            }

        }
    }

}