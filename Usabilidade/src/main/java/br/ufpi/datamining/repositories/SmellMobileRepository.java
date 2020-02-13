package br.ufpi.datamining.repositories;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.com.caelum.vraptor.ioc.Component;
import br.ufpi.datamining.models.SmellMobileDetMining;
import br.ufpi.datamining.models.SmellMobileMining;
import br.ufpi.repositories.Repository;

@Component
public class SmellMobileRepository extends Repository<SmellMobileMining, Long> {

	public SmellMobileRepository(EntityManager entityManager) {
		super(entityManager);
	}
	
	@SuppressWarnings("unchecked")
	public List<SmellMobileMining> getSmellMobile() {
		Query query = entityManager.createNamedQuery("SmellMobileMining.findAll");
		try {
			return (List<SmellMobileMining>) query.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public SmellMobileMining getSmellMobilebyId(Long idSmell) {
		Query query = entityManager.createNamedQuery("SmellMobileMining.findById");
		query.setParameter("idsmell", idSmell);
		try {
			return (SmellMobileMining) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}	
	
}
