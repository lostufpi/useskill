package br.ufpi.datamining.repositories;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.com.caelum.vraptor.ioc.Component;
import br.ufpi.datamining.models.ActionDataMining;
import br.ufpi.datamining.models.auxiliar.CountActionsAux;
import br.ufpi.datamining.models.auxiliar.FieldSearch;
import br.ufpi.datamining.models.auxiliar.OrderSearch;
import br.ufpi.datamining.models.enums.ActionTypeDataMiningEnum;
import br.ufpi.datamining.utils.EntityManagerUtil;
import br.ufpi.repositories.Repository;

@Component
public class ActionDataMiningRepository extends Repository<ActionDataMining, Long> {

	public ActionDataMiningRepository(EntityManager entityManager) {
		super(EntityManagerUtil.getEntityManager());
		//super(entityManager);
	}

	public ActionDataMining getUniqueTest(Long id) {
		Query query = entityManager.createNamedQuery("ActionDataMining.findById");
		query.setParameter("id", id);
		try {
			return (ActionDataMining) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ActionDataMining> getActions(List<FieldSearch> fields, Set<ActionTypeDataMiningEnum> disregardActions, OrderSearch order, Long limit){
		if(fields != null && fields.size() > 0){
			String squery = "SELECT a FROM ActionDataMining a WHERE ";
			int count = 0;
			for(FieldSearch f : fields){
				count++;
				squery += " " + f.getField() + " " + f.getComparator().getDesc() + " :" + f.getAlias() + " ";
				if(fields.size() > count){
					squery += " AND ";
				}
			}
			if(disregardActions != null && disregardActions.size() > 0){
				count = 0;
				squery += " AND sActionType NOT IN (";
				for(ActionTypeDataMiningEnum da : disregardActions){
					count++;
					squery += "'" + da.getAction() + "' ";
					if(disregardActions.size() > count){
						squery += ",";
					}
				}
				squery += ") ";
			}
			//ordenar aqui pode causar problemas, pois ao buscar eventos mais recentes, traz null pointer
			//squery += " ORDER BY a.sTime ASC";
			if (order != null) {
				squery += " ORDER BY a." + order.getField();
				squery += order.isAscending() ? " ASC " : " DESC ";
			}
			
			if (limit != null) {
				squery += " LIMIT " + limit;
			}
			
			System.out.println(squery);
			
			Query query = entityManager.createQuery(squery);
			for(FieldSearch f : fields){
				query.setParameter(f.getAlias(), f.getValue());
			}
			
			try {
				query.setMaxResults(800000);
				return query.getResultList();
			} catch (NoResultException e) {}
		}
		return null;
	}
	
	/*MILL*/
	@SuppressWarnings("unchecked")
	public List<ActionDataMining> getActionsOrder(List<FieldSearch> fields, Set<ActionTypeDataMiningEnum> disregardActions, List<OrderSearch> order, Long limit){
		if(fields != null && fields.size() > 0){
			String squery = "SELECT a FROM ActionDataMining a WHERE ";
			int count = 0;
			for(FieldSearch f : fields){
				count++;
				squery += " " + f.getField() + " " + f.getComparator().getDesc() + " :" + f.getAlias() + " ";
				if(fields.size() > count){
					squery += " AND ";
				}
			}
			if(disregardActions != null && disregardActions.size() > 0){
				count = 0;
				squery += " AND sActionType NOT IN (";
				for(ActionTypeDataMiningEnum da : disregardActions){
					count++;
					squery += "'" + da.getAction() + "' ";
					if(disregardActions.size() > count){
						squery += ",";
					}
				}
				squery += ") ";
			}
			//ordenar aqui pode causar problemas, pois ao buscar eventos mais recentes, traz null pointer
			//squery += " ORDER BY a.sTime ASC";
			count = 0;
			if (order != null) {
				squery += " ORDER BY " ;
				for(OrderSearch f : order){
					count++;
					squery += f.getField() + " "; 
					squery += f.isAscending() ? " ASC " : " DESC ";	
					if(fields.size() > count){
						squery += " , ";
					}
				}							
			}			
			if (limit != null) {
				squery += " LIMIT " + limit;
			}			
			//System.out.println(squery);
			
			Query query = entityManager.createQuery(squery);
			for(FieldSearch f : fields){
				query.setParameter(f.getAlias(), f.getValue());
			}
			
			try {
				query.setMaxResults(800000);
				return query.getResultList();
			} catch (NoResultException e) {}
		}
		return null;
	}	
	/*FIN MILL*/
	
	@SuppressWarnings("unchecked")
	public List<CountActionsAux> getCountActionsByRestrictions(FieldSearch fieldGroup, List<FieldSearch> fieldsSearch, OrderSearch order){
		List<CountActionsAux> list = new LinkedList<CountActionsAux>();
		String squery = "SELECT distinct(" + fieldGroup.getField() + "), count(a) from ActionDataMining a ";
		
		if (fieldsSearch != null && fieldsSearch.size() > 0) {
			squery += " WHERE "; 
			int count = 0;
			for(FieldSearch f : fieldsSearch){
				count++;
				squery += " " + f.getField() + " " + f.getComparator().getDesc() + " :" + f.getAlias() + " ";
				if(fieldsSearch.size() > count){
					squery += " AND ";
				}
			}
		}
		
		squery += " GROUP BY " + fieldGroup.getField();
		
		if (order != null) {
			squery += " ORDER BY a." + order.getField();
			squery += order.isAscending() ? " ASC " : " DESC ";
		} else {
			squery += " ORDER BY 2 DESC";
		}
		
		System.out.println(squery);
		
		Query query = entityManager.createQuery(squery);
		for(FieldSearch f : fieldsSearch){
			query.setParameter(f.getAlias(), f.getValue());
		}
		
		List<Object[]> resultList = query.getResultList();
		
		for (Object[] o : resultList) {
			list.add(new CountActionsAux((String) o[0], null, (Long) o[1]));
		}
		
		try {
			return list;
		} catch (NoResultException e) {}
		return null;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<ActionDataMining> getUserActions(String username, Long initialId, Set<ActionTypeDataMiningEnum> disregardActions) {
		String sql = "SELECT a FROM ActionDataMining a WHERE a.sTime >= :initialId AND sUsername = :username";
		if(disregardActions.size() > 0){
			int count = 0;
			sql += " AND sActionType NOT IN (";
			for(ActionTypeDataMiningEnum da : disregardActions){
				count++;
				sql += "'" + da.getAction() + "' ";
				if(disregardActions.size() > count){
					sql += ",";
				}
			}
			sql += ") ";
		}
		sql += " ORDER BY a.sTime ASC";
		Query query = entityManager.createQuery(sql);
		query.setParameter("initialId", initialId);
		query.setParameter("username", username);
		
		try {
			return query.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ActionDataMining> getUserActions(String username, Long initialId, Long finalId, Set<ActionTypeDataMiningEnum> disregardActions) {
		String sql = "SELECT a FROM ActionDataMining a WHERE a.sTime >= :initialId AND a.sTime <= :finalId AND sUsername = :username";
		if(disregardActions.size() > 0){
			int count = 0;
			sql += " AND sActionType NOT IN (";
			for(ActionTypeDataMiningEnum da : disregardActions){
				count++;
				sql += "'" + da.getAction() + "' ";
				if(disregardActions.size() > count){
					sql += ",";
				}
			}
			sql += ") ";
		}
		sql += " ORDER BY a.sTime ASC";
		Query query = entityManager.createQuery(sql);
		query.setParameter("initialId", initialId);
		query.setParameter("username", username);
		query.setParameter("finalId", finalId);
		
		try {
			return query.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}

}
