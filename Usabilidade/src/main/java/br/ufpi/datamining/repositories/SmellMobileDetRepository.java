package br.ufpi.datamining.repositories;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.com.caelum.vraptor.ioc.Component;
import br.ufpi.datamining.models.SmellMobileDetMining;
import br.ufpi.datamining.models.TaskDataMining;
import br.ufpi.repositories.Repository;

@Component
public class SmellMobileDetRepository extends Repository<SmellMobileDetMining, Long> {

	public SmellMobileDetRepository(EntityManager entityManager) {
		super(entityManager);
	}
	
	@SuppressWarnings("unchecked")
	public List<SmellMobileDetMining> getSmellMobileDet(Long idSmell) {
		Query query = entityManager.createNamedQuery("SmellMobileDetMining.findby");
		query.setParameter("idsmell", idSmell);
		try {
			return (List<SmellMobileDetMining>) query.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public SmellMobileDetMining getSmellMobileDetbyId(Long idSmellDet) {
		Query query = entityManager.createNamedQuery("SmellMobileDetMining.findbyId");
		query.setParameter("id", idSmellDet);
		try {
			return (SmellMobileDetMining) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
}
