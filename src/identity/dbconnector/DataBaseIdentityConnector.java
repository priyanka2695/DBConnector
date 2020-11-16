package identity.dbconnector;

import identity.dbconnector.dao.UserProfileDao;
import identity.dbconnector.dao.UserProfileDaoImpl;
import identity.dbconnector.domain.UserProfile;
import identity.dbconnector.util.DataBaseIdentityUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

@ConnectorClass(configurationClass = DataBaseIdentityConfig.class, displayNameKey = "displayName", messageCatalogPaths = "identity.dbconnector.Messages")
public class DataBaseIdentityConnector
		implements PoolableConnector, SearchOp<String>, UpdateOp, DeleteOp, CreateOp, SyncOp, GetApiOp, SchemaOp {

	private DataBaseIdentityConfig config;
	private EntityManagerFactory factory = null;
	private UserProfileDao userProfileDao = null;
	private Context context = null;
	private static Log logger = null;

	@Override
	public void dispose() {
		logger.ok("dispose started");
		if (factory != null) {
			logger.ok("dispose factory");
			factory.close();
		}

		if (context != null) {
			try {
				logger.ok("dispose context");
				context.close();
			} catch (NamingException e) {
				logger.error(e, "dispose context");
			}
		}
		logger.ok("dispose ended");

	}

	@Override
	public Configuration getConfiguration() {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	public void init(Configuration config) {
		logger = Log.getLog(this.getClass());
		logger.ok("Invoking the Init Method Start");
		this.config = (DataBaseIdentityConfig) config;
		initDataSource(this.config);
		factory = Persistence.createEntityManagerFactory(DataBaseIdentityConstants.PERSISTENCE_UNIT);
		userProfileDao = new UserProfileDaoImpl(factory);
		logger.ok("Invoking the Init Method End ");

	}

	private void initDataSource(DataBaseIdentityConfig config) {

		logger.ok(" initDataSource - started");

		if (!DataBaseIdentityUtil.isEmpty(config.getEnvironment())
				&& config.getEnvironment().equalsIgnoreCase(DataBaseIdentityConstants.DATASOURCE_LOCAL)) {
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
			System.setProperty(Context.PROVIDER_URL, "file:///tmp");

			try {
				// Properties prop= new Properties();
				// prop.put(Context.INITIAL_CONTEXT_FACTORY,
				// "com.sun.jndi.fscontext.RefFSContextFactory");
				// prop.put(Context.PROVIDER_URL, "file:///tmp");

				context = new InitialContext();
				logger.ok("INitialize the Local Context Successfully");

				OracleConnectionPoolDataSource ds = new OracleConnectionPoolDataSource();
				ds.setURL(config.getUrl());
				ds.setUser(config.getUserName());
				ds.setPassword(config.getPassword());
				context.rebind("mydatasource", ds);

				logger.ok("Data Source created successfully");

			} catch (NamingException e) {
				logger.error(e, "initializing the data source error");
				throw new ConnectorException(DataBaseIdentityConstants.CONNECTOR_CON_EXP);
			} catch (SQLException e) {

				logger.error(e, "initializing the data source error");
				throw new ConnectorException(DataBaseIdentityConstants.CONNECTOR_CON_EXP);
			}

		}

		logger.ok(" initDataSource - Ended");
	}

	@Override
	public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions operations) {
		logger.ok("create Started");

		Map<String, Object> row = DataBaseIdentityUtil.convertSetToMap(attributes);

		logger.ok("Create Request Attributes : " + attributes);

		if (row != null && row.isEmpty()) {
			throw new ConnectorException(DataBaseIdentityConstants.USER_ATTR_MISSING);
		}

		String userLogin = DataBaseIdentityUtil.getString(row, DataBaseIdentityConstants.USER_LOGIN);
		logger.ok("Create Request Login  : " + userLogin);

		if (DataBaseIdentityUtil.isEmpty(userLogin)) {
			logger.error(
					String.format(DataBaseIdentityConstants.USER_ATTR_REQUIRED, DataBaseIdentityConstants.USER_LOGIN));
			throw new ConnectorException(
					String.format(DataBaseIdentityConstants.USER_ATTR_REQUIRED, DataBaseIdentityConstants.USER_LOGIN));
		}

		UserProfile profile = userProfileDao.findByLogin(userLogin);
		if (profile != null) {
			logger.error(String.format(DataBaseIdentityConstants.USER_ALREADY_EXISTS,
					row.get(DataBaseIdentityConstants.USER_LOGIN).toString()));
			throw new ConnectorException(String.format(DataBaseIdentityConstants.USER_ALREADY_EXISTS,
					row.get(DataBaseIdentityConstants.USER_LOGIN).toString()));
		}

		profile = DataBaseIdentityUtil.convertToUserProfile(row);
		profile.setCreateDate(new Date());
		profile.setPassword(DataBaseIdentityUtil.getString(row, DataBaseIdentityConstants.PASSWORD));
		profile.setStatus(DataBaseIdentityConstants.STATUS_ENABLED);

		logger.ok("User Creation Process Started");
		profile = userProfileDao.createUser(profile);
		logger.ok("User Creation Process Ended");

		Uid uid = new Uid(profile.getId().toString());
		logger.ok("User Created Successfully and Id :" + uid);
		logger.ok("create Ended");

		return uid;
	}

	@Override
	public void delete(ObjectClass objectClass, Uid uid, OperationOptions operations) {

		logger.ok("delete Started");
		logger.ok("delete User Uid " + uid);

		userProfileDao.deleteUser(new Long(uid.getUidValue()));

		logger.ok("delete Ended");
	}

	@Override
	public void checkAlive() {
		userProfileDao.test();
	}

	@Override
	public ConnectorObject getObject(ObjectClass objectClass, Uid uid, OperationOptions operations) {
		logger.ok("getObject Started");
		logger.ok("The Requested getObject uid " + uid);

		UserProfile profile = userProfileDao.findById(new Long(uid.getUidValue()));
		if (profile == null) {
			logger.error(String.format(DataBaseIdentityConstants.USER_NOT_FOUND, "Uid", uid.getUidValue()));
			throw new ConnectorException(
					String.format(DataBaseIdentityConstants.USER_NOT_FOUND, "Uid", uid.getUidValue()));
		}

		logger.ok("getObject Ended");

		return DataBaseIdentityUtil.convertMapToConnectorObject(profile);
	}

	@Override
	public Schema schema() {

		logger.ok("schema Started.");

		Set<AttributeInfo> attributes = new HashSet<AttributeInfo>();
		SchemaBuilder builder = new SchemaBuilder(this.getClass());

		AttributeInfoBuilder userLogin = new AttributeInfoBuilder();
		userLogin.setName(DataBaseIdentityConstants.USER_LOGIN);
		userLogin.setCreateable(true);
		userLogin.setUpdateable(true);
		userLogin.setReadable(true);
		userLogin.setRequired(true);
		userLogin.setMultiValued(false);
		attributes.add(userLogin.build());

		AttributeInfoBuilder password = new AttributeInfoBuilder();
		password.setName(DataBaseIdentityConstants.PASSWORD);
		password.setCreateable(true);
		password.setUpdateable(true);
		password.setReadable(true);
		password.setRequired(true);
		password.setMultiValued(false);
		attributes.add(password.build());

		AttributeInfoBuilder firstName = new AttributeInfoBuilder();
		firstName.setName(DataBaseIdentityConstants.FIRST_NAME);
		firstName.setCreateable(true);
		firstName.setUpdateable(true);
		firstName.setReadable(true);
		firstName.setRequired(true);
		firstName.setMultiValued(false);
		attributes.add(firstName.build());

		AttributeInfoBuilder lastName = new AttributeInfoBuilder();
		lastName.setName(DataBaseIdentityConstants.LAST_NAME);
		lastName.setCreateable(true);
		lastName.setUpdateable(true);
		lastName.setReadable(true);
		lastName.setRequired(true);
		lastName.setMultiValued(false);
		attributes.add(lastName.build());

		AttributeInfoBuilder middleName = new AttributeInfoBuilder();
		middleName.setName(DataBaseIdentityConstants.MIDDLE_NAME);
		middleName.setCreateable(true);
		middleName.setUpdateable(true);
		middleName.setReadable(true);
		middleName.setRequired(true);
		middleName.setMultiValued(false);
		attributes.add(middleName.build());

		builder.defineObjectClass(ObjectClass.ACCOUNT.getDisplayNameKey(), attributes);
		logger.ok("Schema Attributes " + attributes);

		logger.ok("schema Started.");

		return builder.build();

	}

	@Override
	public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions operations) {
		logger.ok("update Started");

		logger.ok("update Operation Request Attributes: " + attributes);

		Map<String, Object> row = DataBaseIdentityUtil.convertSetToMap(attributes);

		logger.ok("update Operation Request Attributes: " + row);
		if (row != null && row.isEmpty()) {
			logger.error(DataBaseIdentityConstants.USER_ATTR_MISSING);
			throw new ConnectorException(DataBaseIdentityConstants.USER_ATTR_MISSING);
		}

		UserProfile profile = userProfileDao.findById(Long.parseLong(uid.getUidValue()));

		if (profile == null) {
			logger.error(String.format(DataBaseIdentityConstants.USER_NOT_FOUND, "Uid", uid.getUidValue()));
			throw new ConnectorException(
					String.format(DataBaseIdentityConstants.USER_NOT_FOUND, "Uid", uid.getUidValue()));
		}

		logger.error("The User " + uid.getUidValue() + " found Successfully ");

		profile.setUpdateDate(new Date());
		if (row.containsKey(DataBaseIdentityConstants.USER_LOGIN)) {
			String userName = DataBaseIdentityUtil.getString(row, DataBaseIdentityConstants.USER_LOGIN);
			if (DataBaseIdentityUtil.isEmpty(userName)) {
				logger.error(String.format(DataBaseIdentityConstants.USER_ATTR_REQUIRED,
						DataBaseIdentityConstants.USER_LOGIN));
				throw new ConnectorException(String.format(DataBaseIdentityConstants.USER_ATTR_REQUIRED,
						DataBaseIdentityConstants.USER_LOGIN));
			}

			profile.setUserLogin(userName);
		}
		if (row.containsKey(DataBaseIdentityConstants.PASSWORD)) {
			profile.setPassword(DataBaseIdentityUtil.getString(row, DataBaseIdentityConstants.PASSWORD));
		}
		if (row.containsKey(DataBaseIdentityConstants.FIRST_NAME)) {
			profile.setFirstName(DataBaseIdentityUtil.getString(row, DataBaseIdentityConstants.FIRST_NAME));
		}
		if (row.containsKey(DataBaseIdentityConstants.LAST_NAME)) {
			profile.setLastName(DataBaseIdentityUtil.getString(row, DataBaseIdentityConstants.LAST_NAME));

		}
		if (row.containsKey(DataBaseIdentityConstants.MIDDLE_NAME)) {
			profile.setMiddleName(DataBaseIdentityUtil.getString(row, DataBaseIdentityConstants.MIDDLE_NAME));
		}

		if (row.containsKey(DataBaseIdentityConstants.ATTR_ENABLE)) {
			String enable = DataBaseIdentityUtil.getString(row, DataBaseIdentityConstants.ATTR_ENABLE);
			if (!DataBaseIdentityUtil.isEmpty(enable)) {

				if (enable.equalsIgnoreCase("true")) {
					enable = DataBaseIdentityConstants.STATUS_ENABLED;
				} else {
					enable = DataBaseIdentityConstants.STATUS_DISABLED;
				}
			} else {
				enable = DataBaseIdentityConstants.STATUS_DISABLED;
			}
			profile.setStatus(enable);
		}
		userProfileDao.updateUser(profile);

		logger.ok("update Ended");
		return uid;
	}

	@Override
	public FilterTranslator<String> createFilterTranslator(ObjectClass objectClass, OperationOptions operations) {
		return new DataBaseIdentityFilter();
	}

	@Override
	public void executeQuery(ObjectClass objectClass, String filter, ResultsHandler handler,
			OperationOptions operations) {

		logger.ok("executeQuery Started");
		logger.ok("executeQuery filter " + filter);

		List<String> returnAttrs = new ArrayList<String>();

		if (operations != null) {
			for (String attr : operations.getAttributesToGet()) {
				logger.ok("executeQuery Attributes " + attr);
				returnAttrs.add(attr);
			}
		}

		List<UserProfile> profiles = userProfileDao.findByUsersCriteria(filter);
		logger.error("executeQuery Result " + profiles);

		if (profiles != null && !profiles.isEmpty()) {
			for (UserProfile userProfile : profiles) {
				ConnectorObject conobj = DataBaseIdentityUtil.convertMapToConnectorObject(userProfile);

				if (returnAttrs.contains(DataBaseIdentityConstants.LAST_UPDATE)) {
					conobj = DataBaseIdentityUtil.convertMapToConnectorObject(userProfile,
							DataBaseIdentityConstants.LAST_UPDATE);
				} else if (returnAttrs.contains(DataBaseIdentityConstants.CREATE_DATE)) {
					conobj = DataBaseIdentityUtil.convertMapToConnectorObject(userProfile,
							DataBaseIdentityConstants.CREATE_DATE);
				} else {
					conobj = DataBaseIdentityUtil.convertMapToConnectorObject(userProfile);
				}

				logger.ok("executeQuery Attributes Objects  " + conobj.getAttributes());

				handler.handle(conobj);

				logger.ok("executeQuery Attributes Objects  After " + conobj.getAttributes());
			}
		}
		logger.ok("executeQuery Ended");
	}

	@Override
	public SyncToken getLatestSyncToken(ObjectClass objectClass) {
		// TODO Auto-generated method stub
		SyncToken token = new SyncToken(DataBaseIdentityUtil.convertDateToString(new Date()));
		return token;
	}

	@Override
	public void sync(ObjectClass objectClass, SyncToken syncToken, SyncResultsHandler handler,
			OperationOptions operations) {

		logger.ok("sync Started");
		logger.ok("syncToken  " + syncToken);

		List<UserProfile> profiles = userProfileDao.getUsersByChange(
				syncToken == null ? null : DataBaseIdentityUtil.convertSringToDate("" + syncToken.getValue()));
		logger.ok("sync Result " + profiles);
		if (profiles != null && !profiles.isEmpty()) {
			for (UserProfile userProfile : profiles) {
				SyncDeltaBuilder builder = new SyncDeltaBuilder();
				builder.setObject(DataBaseIdentityUtil.convertMapToConnectorObject(userProfile));
				builder.setDeltaType(SyncDeltaType.CREATE_OR_UPDATE);
				builder.setUid(new Uid("" + userProfile.getId()));
				builder.setToken(new SyncToken(userProfile.getUpdateDate() != null
						? DataBaseIdentityUtil.convertDateToString(userProfile.getUpdateDate())
						: userProfile.getCreateDate() != null
								? DataBaseIdentityUtil.convertDateToString(userProfile.getCreateDate())
								: DataBaseIdentityUtil.convertDateToString(new Date())));
				handler.handle(builder.build());
			}
		}
	}
}
