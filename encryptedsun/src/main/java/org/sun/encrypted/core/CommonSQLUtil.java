package org.sun.encrypted.core;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class CommonSQLUtil {

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public int findMaxId(String tableName) {
		Session session = sessionFactory.openSession();
		int rowID = 0;
		try {
			Query query = session.createQuery("select max(id) from "
					+ tableName);
			if (query.uniqueResult() == null) {
				rowID = 1;
			} else {
				rowID = (Integer) query.uniqueResult() + 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

		return rowID;
	}

}
