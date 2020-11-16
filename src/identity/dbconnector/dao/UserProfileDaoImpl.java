package identity.dbconnector.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import identity.dbconnector.DataBaseIdentityConstants;
import identity.dbconnector.domain.UserProfile;

public class UserProfileDaoImpl implements UserProfileDao {

	private EntityManagerFactory factory;
	private Log logger = Log.getLog(this.getClass());

	public UserProfileDaoImpl(EntityManagerFactory factory) {
		this.factory = factory;
	}

	@Override
	public UserProfile findByLogin(String loginId) {
		String operation = "findByLogin - ";
		logger.ok(operation + " Started");

		EntityManager em = null;
		UserProfile profile = null;
		try {
			em = factory.createEntityManager();
			Query query = em.createQuery("Select T FROM UserProfile T WHERE T.userLogin = :userLogin");
			logger.ok(operation + " SQL" + query);

			query.setParameter("userLogin", loginId);

			List<UserProfile> profiles = query.getResultList();
			logger.ok(operation + " Result " + profiles);
			if (profiles != null && !profiles.isEmpty()) {
				profile = profiles.get(0);
			}
		} catch (Exception e) {
			logger.error(e, operation);
			throw new ConnectorException(e);
		} finally {
			if (em != null) {
				em.close();
			}
		}

		logger.ok(operation + " Ended");

		return profile;
	}

	@Override
	public UserProfile findById(long id) {
		String operation = "findById - ";
		logger.ok(operation + " Started");

		logger.ok(operation + " Requested Search ID " + id);

		EntityManager em = null;
		UserProfile profile = null;
		try {
			em = factory.createEntityManager();
			profile = em.find(UserProfile.class, id);
			logger.ok(operation + " Requested Search Result " + profile);
		} catch (Exception e) {
			logger.error(e, operation);
			throw new ConnectorException(e);
		} finally {
			if (em != null) {
				em.close();
			}
		}
		logger.ok(operation + " Ended");
		return profile;
	}

	@Override
	public UserProfile createUser(UserProfile userProfile) {
		// TODO Auto-generated method stub
		String operation = "createUser - ";
		logger.ok(operation + " Started");

		EntityManager em = null;
		try {
			em = factory.createEntityManager();
			em.getTransaction().begin();
			em.persist(userProfile);
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e, operation);
			if (em != null) {
				em.getTransaction().rollback();
			}
			throw new ConnectorException(e);
		} finally {
			if (em != null) {
				em.close();
			}
		}

		logger.ok(operation + " Ended");
		return userProfile;
	}

	@Override
	public UserProfile updateUser(UserProfile userProfile) {

		String operation = "updateUser - ";
		logger.ok(operation + " Started");

		EntityManager em = null;
		UserProfile profile = null;
		try {
			em = factory.createEntityManager();
			em.getTransaction().begin();
			profile = em.merge(userProfile);
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e, operation);

			if (em != null) {
				em.getTransaction().rollback();
			}
			throw new ConnectorException(e);
		} finally {
			if (em != null) {
				em.close();
			}
		}

		logger.ok(operation + " Ended");

		return profile;
	}

	@Override
	public UserProfile deleteUser(long id) {
		EntityManager em = null;
		UserProfile profile = null;
		String operation = "deleteUser - ";
		logger.ok(operation + " Started");
		try {
			em = factory.createEntityManager();
			em.getTransaction().begin();
			profile = em.getReference(UserProfile.class, id);
			em.remove(profile);
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e, operation);
			if (em != null) {
				em.getTransaction().rollback();
			}
			throw new ConnectorException(e);
		} finally {
			if (em != null) {
				em.close();
			}
		}
		logger.ok(operation + " Ended");
		return null;
	}

	@Override
	public List<UserProfile> getAllUser() {
		EntityManager em = null;
		List<UserProfile> profiles = null;
		String operation = "getAllUser - ";
		logger.ok(operation + " Started");

		try {
			em = factory.createEntityManager();
			Query query = em.createQuery("Select T FROM UserProfile T ");
			logger.ok(operation + " SQl " + query);
			profiles = query.getResultList();
		} catch (Exception e) {
			logger.error(e, operation);
			throw new ConnectorException(e);
		} finally {
			if (em != null) {
				em.close();
			}
		}

		logger.ok(operation + " Ended ");
		return profiles;
	}

	@Override
	public List<UserProfile> getUsersByChange(Date date) {
		String operation = "getUsersByChange - ";
		logger.ok(operation + " Started");

		EntityManager em = null;
		List<UserProfile> profiles = null;
		try {
			em = factory.createEntityManager();
			Query query = em.createQuery(
					"Select T FROM UserProfile T WHERE T.updateDate >= :changeDate OR T.createDate>= :createDate");
			query.setParameter("changeDate", date, TemporalType.DATE);
			query.setParameter("createDate", date, TemporalType.DATE);

			logger.ok(operation + " Sql " + query);

			profiles = query.getResultList();
		} catch (Exception e) {
			logger.error(e, operation);
			throw new ConnectorException(e);
		} finally {
			if (em != null) {
				em.close();
			}
		}
		logger.ok(operation + " Ended");
		return profiles;
	}

	@Override
	public void test() {
		if (!factory.isOpen()) {
			factory = Persistence.createEntityManagerFactory(DataBaseIdentityConstants.PERSISTENCE_UNIT);
			if (!factory.isOpen()) {
				throw new ConnectorException("The Data Base Connection Error");
			}
		}

	}

	@Override
	public List<UserProfile> findByUsersCriteria(String criteria) {

		String operation = "findByUsersCriteria - ";

		logger.ok(operation + " Started");

		EntityManager em = null;
		List<UserProfile> profiles = null;
		try {
			em = factory.createEntityManager();
			String sql = "Select T FROM UserProfile T ";

			if (!criteria.equalsIgnoreCase("*")) {
				sql = sql + " WHERE " + criteria;
			}
			logger.ok(operation + " sql " + sql);

			Query query = em.createQuery(sql);
			profiles = query.getResultList();

		} catch (Exception e) {
			logger.error(e, operation);

			throw new ConnectorException(e);
		} finally {
			if (em != null) {
				em.close();
			}
		}

		logger.ok(operation + " Ended");
		return profiles;

	}

}
