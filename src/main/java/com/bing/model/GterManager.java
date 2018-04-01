package com.bing.model;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * This is a singleton
 * @author Bing Zhenchao
 *
 */
public enum GterManager {
	INSTANCE;
	private SessionFactory factory;
	private GterManager(){
		Configuration configuration = new Configuration().configure();
		StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
        factory = configuration.buildSessionFactory(builder.build());
	}
	public Integer addGterOffer(Gteroffer offer){
		Integer id = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try{
	         tx = session.beginTransaction();
	         id = (Integer)session.save(offer);
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	         session.close(); 
	      }
	      return id;
	}
	public Integer addGterperson(Gterperson person){
		Integer id = null;
		Session session = factory.openSession();
		Transaction tx = null;
		try{
	         tx = session.beginTransaction();
	         id = (Integer)session.save(person);
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	         session.close(); 
	      }
	      return id;
	}
}
