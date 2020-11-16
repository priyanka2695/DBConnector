package identity.dbconnector.util;

import identity.dbconnector.DataBaseIdentityConstants;
import identity.dbconnector.domain.UserProfile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;

public class DataBaseIdentityUtil {

	public static ConnectorObject convertMapToConnectorObject(UserProfile userProfile) {
		ConnectorObjectBuilder userObjBuilder = new ConnectorObjectBuilder();
		String status = userProfile.getStatus();
		if (!isEmpty(status) && status.equalsIgnoreCase(DataBaseIdentityConstants.STATUS_ENABLED)) {
			userObjBuilder.addAttribute(DataBaseIdentityConstants.STATUS, DataBaseIdentityConstants.STATUS_ENABLED);

		} else {
			userObjBuilder.addAttribute(DataBaseIdentityConstants.STATUS, DataBaseIdentityConstants.STATUS_DISABLED);
		}

		userObjBuilder.addAttribute(DataBaseIdentityConstants.FIRST_NAME, userProfile.getFirstName());
		userObjBuilder.addAttribute(DataBaseIdentityConstants.LAST_NAME, userProfile.getLastName());
		userObjBuilder.addAttribute(DataBaseIdentityConstants.MIDDLE_NAME, userProfile.getMiddleName());
		userObjBuilder.addAttribute(DataBaseIdentityConstants.USER_LOGIN, userProfile.getUserLogin());
		userObjBuilder.setUid(Long.toString(userProfile.getId()));
		userObjBuilder.setName(Long.toString(userProfile.getId()));

		ConnectorObject conobj = userObjBuilder.build();
		return conobj;
	}

	public static ConnectorObject convertMapToConnectorObject(UserProfile userProfile, String updateFieldName) {
		ConnectorObjectBuilder userObjBuilder = new ConnectorObjectBuilder();
		String status = userProfile.getStatus();
		if (!isEmpty(status) && status.equalsIgnoreCase(DataBaseIdentityConstants.STATUS_ENABLED)) {
			userObjBuilder.addAttribute(DataBaseIdentityConstants.STATUS, DataBaseIdentityConstants.STATUS_ENABLED);

		} else {
			userObjBuilder.addAttribute(DataBaseIdentityConstants.STATUS, DataBaseIdentityConstants.STATUS_DISABLED);
		}

		userObjBuilder.addAttribute(DataBaseIdentityConstants.FIRST_NAME, userProfile.getFirstName());
		userObjBuilder.addAttribute(DataBaseIdentityConstants.LAST_NAME, userProfile.getLastName());
		userObjBuilder.addAttribute(DataBaseIdentityConstants.MIDDLE_NAME, userProfile.getMiddleName());
		userObjBuilder.addAttribute(DataBaseIdentityConstants.USER_LOGIN, userProfile.getUserLogin());
		userObjBuilder.setUid(Long.toString(userProfile.getId()));
		userObjBuilder.setName(Long.toString(userProfile.getId()));

		if (updateFieldName != null) {
			if (updateFieldName.equalsIgnoreCase("updateDate")) {
				if (userProfile.getUpdateDate() != null) {
					userObjBuilder.addAttribute(updateFieldName, userProfile.getUpdateDate().getTime());
				}
			}
			if (updateFieldName.equalsIgnoreCase("createDate")) {
				if (userProfile.getCreateDate() != null) {
					userObjBuilder.addAttribute(updateFieldName, userProfile.getCreateDate().getTime());
				}
			}
		}
		ConnectorObject conobj = userObjBuilder.build();
		return conobj;
	}

	public static boolean isEmpty(Object data) {
		boolean result = data == null;

		if (!result) {
			if (data instanceof String) {
				String strData = (String) data;
				result = strData.trim().isEmpty();
			}

		}
		return result;
	}

	public static Map<String, Object> convertSetToMap(Set<Attribute> attributes) {
		Map<String, Object> row = null;
		if (attributes != null && !attributes.isEmpty()) {
			row = new HashMap<String, Object>();
			for (Attribute attribute : attributes) {
				List<Object> value = attribute.getValue();

				if (attribute.getName().equalsIgnoreCase("__CURRENT_ATTRIBUTES__")) {
				}
				/*
				 * else if(attribute.getName().equalsIgnoreCase("__ENABLE__")) {
				 * addValueToMap(row, value, attribute); String
				 * enableValue=row.get("__ENABLE__").toString();
				 * if(enableValue.equalsIgnoreCase("true")) { row.put("status", "active"); }
				 * else { row.put("status", "inactive"); } row.remove("__ENABLE__"); }
				 */
				else {
					addValueToMap(row, value, attribute);
				}

			}
		}

		return row;
	}

	public static UserProfile convertToUserProfile(Map<String, Object> row) {
		UserProfile profile = new UserProfile();
		profile.setUserLogin(getString(row, DataBaseIdentityConstants.USER_LOGIN));
		profile.setFirstName(getString(row, DataBaseIdentityConstants.FIRST_NAME));
		profile.setLastName(getString(row, DataBaseIdentityConstants.LAST_NAME));
		profile.setMiddleName(getString(row, DataBaseIdentityConstants.MIDDLE_NAME));
		return profile;

	}

	public static void addValueToMap(Map<String, Object> row, List<Object> value, Attribute attribute) {
		if (isMultiValued(attribute.getValue())) {
			row.put(attribute.getName(), attribute.getValue());
		} else {
			if (isEmpty(value)) {
				row.put(attribute.getName(), "");
			} else {
				row.put(attribute.getName(), value.get(0));
			}
		}

	}

	public static boolean isMultiValued(List<Object> data) {
		boolean result = false;
		if (data != null && !data.isEmpty()) {
			result = data.size() > 1;
		}
		return result;
	}

	public static String getString(Map<String, Object> row, String key) {
		String result = "";
		if (row.containsKey(key)) {
			Object data = row.get(key);
			if (data instanceof String) {
				result = (String) data;
			}
		}
		return result;
	}

	public static String getString(Object data) {
		return data != null ? data.toString() : "";
	}

	public static String convertDateToString(Date date) {
		System.out.println("Requested Date " + date);
		String result = "";
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		result = format.format(date);
		return result;
	}

	public static Date convertSringToDate(String date) {
		System.out.println("Conversion Requested Date " + date);
		Date result = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			result = format.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}