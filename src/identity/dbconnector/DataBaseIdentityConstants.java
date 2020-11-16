package identity.dbconnector;

public interface DataBaseIdentityConstants {

	public String PERSISTENCE_UNIT = "databaseconnector";
	public String USER_NOT_FOUND = "The User %s %s Not found.";
	public String USER_LOGIN = "userLogin";
	public String PASSWORD = "password";
	public String FIRST_NAME = "firstName";
	public String LAST_NAME = "lastName";
	public String MIDDLE_NAME = "middleName";
	public String STATUS = "Status";
	public String USER_ATTR_MISSING = "The User required attributes are missing in the request.";
	public String USER_ATTR_REQUIRED = "The %s attribute is missing.";
	public String USER_ALREADY_EXISTS = "The User %s already exists.";
	public String DATASOURCE_LOCAL = "local";
	public String CONFIG_PARAM = "The config parameter %s is empty";
	public String STATUS_ENABLED = "Enabled";
	public String STATUS_DISABLED = "Disabled";
	public String CONNECTOR_CON_EXP = "The Connector SQL Connection Error Exception";
	public String ATTR_ENABLE = "__ENABLE__";
	public String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
	public String DB_DATE_FORMAT = "YYYY/MM/DD HH24:MI:SS";
	public String LAST_UPDATE = "updateDate";
	public String CREATE_DATE = "createDate";

}
