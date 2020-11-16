package identity.dbconnector;

import identity.dbconnector.util.DataBaseIdentityUtil;

import java.util.Date;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanFilter;

public class DataBaseIdentityFilter extends AbstractFilterTranslator<String> {
	private Log logger = Log.getLog(this.getClass());

	public String createEqualsExpression(EqualsFilter filter, boolean not) {
		String operation = "createEqualsExpression - ";

		logger.ok(operation + " Started");

		logger.ok(operation + " filter " + filter);

		String query = null;
		if (not) {
			return query;
		}

		Attribute attr = filter.getAttribute();
		if (attr == null || attr.getValue() == null || (attr.getValue() != null && attr.getValue().isEmpty())) {
			return query;
		}

		String name = filter.getName();
		Object val = attr.getValue().get(0);
		if (DataBaseIdentityUtil.isEmpty(val)) {
			throw new ConnectorException("The filter value can not be empty");
		}
		String strValue = DataBaseIdentityUtil.getString(val);
		query = "T." + name + "='" + val + "'";
		logger.ok(operation + " Final Filter " + query);
		logger.ok(operation + " Ended");

		return query;
	}

	@Override
	// TODO Auto-generated method stub

	protected String createContainsExpression(ContainsFilter filter, boolean not) {
		String operation = "createContainsExpression - ";
		logger.ok(operation + " Started");
		String query = null;
		if (not) {
			return query;
		}

		Attribute attr = filter.getAttribute();
		if (attr == null || attr.getValue() == null || (attr.getValue() != null && attr.getValue().isEmpty())) {
			return query;
		}

		String name = filter.getName();
		Object val = attr.getValue().get(0);
		if (DataBaseIdentityUtil.isEmpty(val)) {
			throw new ConnectorException("The filter value can not be empty");
		}
		String strValue = DataBaseIdentityUtil.getString(val);
		if (strValue.equalsIgnoreCase("*")) {
			query = "*";
		} else {
			String tempVal = "";
			if (strValue.startsWith("*")) {
				tempVal = strValue.substring(1) + "%";
			}
			if (strValue.endsWith("*")) {
				if (!DataBaseIdentityUtil.isEmpty(tempVal)) {
					tempVal = strValue;
				} else {
					tempVal = strValue.substring(0, strValue.length() - 1) + "%";
				}
			} else {
				tempVal = strValue;
			}
			query = "T." + name + " LIKE '" + tempVal + "'";
		}
		logger.ok(operation + " Final Filter " + query);
		logger.ok(operation + " Ended ");
		return query;
	}

	@Override
	protected String createGreaterThanExpression(GreaterThanFilter filter, boolean not) {
		String operation = "createGreaterThanOrEqualExpression - ";
		logger.ok(operation + " Started");
		String query = null;
		if (not) {
			return query;
		}

		Attribute attr = filter.getAttribute();
		if (attr == null || attr.getValue() == null || (attr.getValue() != null && attr.getValue().isEmpty())) {
			return query;
		}

		String name = filter.getName();
		Object val = attr.getValue().get(0);
		if (DataBaseIdentityUtil.isEmpty(val)) {
			throw new ConnectorException("The filter value can not be empty");
		}
		String strValue = DataBaseIdentityUtil.getString(val);

		if (name.equalsIgnoreCase("updateDate")) {
			query = "T." + name + " > FUNC('TO_DATE','"
					+ DataBaseIdentityUtil.convertDateToString(new Date(new Long(strValue).longValue())) + "','"
					+ DataBaseIdentityConstants.DB_DATE_FORMAT + "')";
		} else {
			query = "T." + name + " > '" + strValue + "'";
		}

		logger.ok(operation + " Final Filter " + query);
		logger.ok(operation + " Ended ");
		return query;
	}

	@Override
	protected String createAndExpression(String leftExpression, String rightExpression) {
		return leftExpression + " AND " + rightExpression;

	}

}
