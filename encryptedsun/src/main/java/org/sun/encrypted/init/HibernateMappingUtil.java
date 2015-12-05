package org.sun.encrypted.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.hibernate.util.StringHelper;
import org.sun.encrypted.pojo.HibernateProperty;

/**
 * Spring初始化时记录Hibernate所有映射关系
 * 
 * @author Administrator
 * 
 */
public class HibernateMappingUtil {

	private String packageName = "com/jieyou/sun/po";

	private static HashMap<String, List<HibernateProperty>> MAPPING_MAP = new HashMap<String, List<HibernateProperty>>();

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public static List<HibernateProperty> getPropertyList(String tableName) {
		return MAPPING_MAP.get(tableName);
	}

	@SuppressWarnings("unchecked")
	public void loadMapping() {

		String packagePath = "src/" + packageName;
		File filelist = new File(packagePath);
		List<String> list = Arrays.asList(filelist.list());

		for (int i = 0, len = list.size(); i < len; i++) {
			File xmlFile = null;
			if (list.get(i).endsWith(".xml")) {
				xmlFile = new File(packagePath + "/" + list.get(i));
				FileInputStream inputStream = null;
				SAXReader reader = new SAXReader();

				try {

					reader.setFeature(
							"http://apache.org/xml/features/nonvalidating/load-external-dtd",
							false);

					inputStream = new FileInputStream(xmlFile);
					Document document = reader.read(inputStream);

					Element rootElement = document.getRootElement();
					List<Node> classNode = rootElement.elements("class");
					Element classElement = (Element) classNode.get(0);
					String className = StringHelper.unqualify(classElement
							.attribute("name").getText());

					List<HibernateProperty> propertyList = new ArrayList<HibernateProperty>();

					List<Node> idNode = classElement.elements("id");
					Element idElement = (Element) idNode.get(0);
					propertyList.add(new HibernateProperty(idElement.attribute(
							"name").getText(), idElement.attribute("type")
							.getText()));

					List<Node> propertyNode = classElement.elements("property");
					for (int j = 0, len1 = propertyNode.size(); j < len1; j++) {
						Element propertyElement = (Element) propertyNode.get(j);
						propertyList.add(new HibernateProperty(propertyElement
								.attribute("name").getText(), propertyElement
								.attribute("type").getText()));
					}

					MAPPING_MAP.put(className, propertyList);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (IOException e) {
					}
				}
			}

		}
	}

	public void printMap() {
		Iterator<Entry<String, List<HibernateProperty>>> iterator = MAPPING_MAP
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, List<HibernateProperty>> entry = iterator.next();
			System.out.println(entry.getKey());
			List<HibernateProperty> propertyList = entry.getValue();
			for (int i = 0, len = propertyList.size(); i < len; i++) {
				System.out.print(propertyList.get(i).getPropetyName() + " "
						+ propertyList.get(i).getDataType()+ " ");
			}
			System.out.println();
		}
	}

}
