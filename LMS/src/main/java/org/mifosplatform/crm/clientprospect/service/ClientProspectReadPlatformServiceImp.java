package org.mifosplatform.crm.clientprospect.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.mifosplatform.crm.clientprospect.data.ClientProspectData;
import org.mifosplatform.crm.clientprospect.data.ProspectDetailAssignedToData;
import org.mifosplatform.crm.clientprospect.data.ProspectDetailData;
import org.mifosplatform.crm.clientprospect.data.ProspectProductData;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientProspectReadPlatformServiceImp implements
		ClientProspectReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<ClientProspectData> paginationHelper = new PaginationHelper<ClientProspectData>();
	private final String SQLSEARCHPARAMETER = "%' OR";
	private String extraCriteria = "";

	@Autowired
	public ClientProspectReadPlatformServiceImp(
			final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {

		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.context = context;
	}

	public Collection<ClientProspectData> retriveClientProspect() {
		
		context.authenticatedUser();
		final ClientProspectMapper rowMapper = new ClientProspectMapper();
		final String sql = "select " + rowMapper.query();
		return jdbcTemplate.query(sql, rowMapper);
	}

	public Page<ClientProspectData> retriveClientProspect(final SearchSqlQuery searchClientProspect, 
			final Long userId, final boolean flag) {

		final ClientProspectMapperForNewClient rowMapper = new ClientProspectMapperForNewClient();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ").append(rowMapper.query())
				.append(" where p.is_deleted = 'N' | 'Y' ");
		
		if(flag){
			sqlBuilder.append(" and p.createdby_id = " + userId);
		}
				

		String sqlSearch = searchClientProspect.getSqlSearch();
			
		if (sqlSearch != null) {
			sqlSearch = sqlSearch.trim();
			extraCriteria = " and (p.mobile_no like '%"
					+ sqlSearch
					+ SQLSEARCHPARAMETER
					+ " p.emailId_id like '%"
					+ sqlSearch
					+ SQLSEARCHPARAMETER
					+ " p.status like '%"
					+ sqlSearch
					+ SQLSEARCHPARAMETER
					+ " p.address like '%"
					+ sqlSearch
					+ SQLSEARCHPARAMETER
					+ " concat(ifnull(p.first_name, ''), if(p.first_name > '',' ', '') , ifnull(p.last_name, '')) like '%"
					+ sqlSearch + "%') ";
		}
		sqlBuilder.append(extraCriteria);

		if (searchClientProspect.isLimited()) {
			sqlBuilder.append(" limit ")
					.append(searchClientProspect.getLimit());
		}

		if (searchClientProspect.isOffset()) {
			sqlBuilder.append(" offset ").append(
					searchClientProspect.getOffset());
		}

		final String sqlCountRows = "SELECT FOUND_ROWS()";
		return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows,
				sqlBuilder.toString(), new Object[] {}, rowMapper);

	}

	@Override
	public ProspectDetailData retriveClientProspect(Long clientProspectId) {
		return new ProspectDetailData();
	}

	@Override
	public List<ProspectDetailAssignedToData> retrieveUsers() {
		context.authenticatedUser();

		UserMapper mapper = new UserMapper();

		String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	@Override
	public List<ProspectDetailData> retriveProspectDetailHistory(
			final Long prospectdetailid, final Long userId) {
		
		context.authenticatedUser();
		HistoryMapper mapper = new HistoryMapper();
		String sql = "select " + mapper.query() + " where p.id = d.prospect_id and p.createdby_id=? and d.prospect_id=? order by d.id desc";
		return jdbcTemplate.query(sql, mapper, new Object[] { userId, prospectdetailid });
	}

	private static final class HistoryMapper implements RowMapper<ProspectDetailData> {
		@Override
		public ProspectDetailData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			
			Long id = rs.getLong("id");
			Long prospectId = rs.getLong("prospectId");
			String callStatus = rs.getString("callStatus");
			Date nextTime = rs.getTimestamp("nextTime");
			String notes = rs.getString("notes");
			String assignedTo = rs.getString("assignedTo");
			return new ProspectDetailData(id, prospectId, callStatus,
					DateFormat.getDateTimeInstance().format(nextTime), notes, assignedTo);
		}

		public String query() {
			return "d.id as id, d.prospect_id as prospectId, d.next_time as nextTime, d.notes as notes, cv.code_value as callStatus, au.username as assignedTo from b_prospect p, b_prospect_detail d left outer join m_code_value cv on d.call_status=cv.id left outer join m_appuser au on au.id=d.assigned_to";
		}
	}

	private static final class UserMapper implements
			RowMapper<ProspectDetailAssignedToData> {

		public String schema() {
			return "u.id as id,u.username as assignedTo from m_appuser u where u.is_deleted=0";

		}

		@Override
		public ProspectDetailAssignedToData mapRow(ResultSet rs, int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String username = rs.getString("assignedTo");
			return new ProspectDetailAssignedToData(id, username);

		}

	}

	public class ClientProspectMapper implements RowMapper<ClientProspectData> {

		@Override
		public ClientProspectData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			
			Long id = rs.getLong("id");
			String firstName = rs.getString("firstName");
			String middleName = rs.getString("middleName");
			String lastName = rs.getString("lastName");
			String mobileNumber = rs.getString("mobileNumber");
			String emailId = rs.getString("emailId");
			String sourceOfPublicity = rs.getString("sourceOfPublicity");
			String preferredLoanProduct = rs.getString("preferredLoanProduct");
			Date preferredCallingTime = rs.getDate("preferredCallingTime");
			String address = rs.getString("address");
			String status = rs.getString("status");
			String tin = rs.getString("tin");
			String isDeleted = rs.getString("isDeleted");
			String note = rs.getString("note");
			String location = rs.getString("location");
			
			return new ClientProspectData(id, firstName, middleName, lastName, mobileNumber, emailId, 
					sourceOfPublicity, preferredCallingTime, address, tin, preferredLoanProduct, status, isDeleted, note,location);
		}

		public String query() {
			// String sql =
			// "p.id as id, p.prospect_type as prospectType, p.first_name as firstName, p.middle_name as middleName, p.last_name as lastName, p.home_phone_number as homePhoneNumber, p.work_phone_number as workPhoneNumber, p.mobile_number as mobileNumber, p.emailId as emailId, p.source_of_publicity as sourceOfPublicity, p.preferred_plan as preferredPlan, p.preferred_calling_time as preferredCallingTime, p.address as address, p.street_area as streetArea, p.city_district as cityDistrict, p.state as state, p.country as country, p.status as status, p.status_remark as statusRemark, p.is_deleted as isDeleted, (select notes FROM b_prospect_detail pd where pd.prospect_id =p.id and pd.id=(select max(id) from b_prospect_detail where b_prospect_detail.prospect_id = p.id)) as note";
			String sql = "p.id as id, p.first_name as firstName, p.middle_name as middleName, p.last_name as lastName, "
					+ "p.mobile_no as mobileNumber, p.email_id as emailId, p.source_of_publicity as sourceOfPublicity, "
					+ "p.preferred_loan_product as preferredLoanProduct, p.preferred_calling_time as preferredCallingTime, "
					+ "p.address as address, p.status as status, p.tin as tin, p.is_deleted as isDeleted, p.note as note," 
					+ " p.location as location "
					+ " from m_prospect p ";

			return sql;
		}
	}
			
	public class ClientProspectMapperForNewClient implements
			RowMapper<ClientProspectData> {

		@Override
		public ClientProspectData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			Long id = rs.getLong("id");
			String firstName = rs.getString("firstName");
			String middleName = rs.getString("middleName");
			String lastName = rs.getString("lastName");
			String mobileNumber = rs.getString("mobileNumber");
			String emailId = rs.getString("emailId");
			String sourceOfPublicity = rs.getString("sourceOfPublicity");
			String preferredLoanProduct = rs.getString("preferredLoanProduct");
			Date preferredCallingTime = rs.getDate("preferredCallingTime");
			String note = rs.getString("note");
			String location = rs.getString("location");
			String address = rs.getString("address");
			String status = rs.getString("status");
			String tin = rs.getString("tin");
			String isDeleted = rs.getString("isDeleted");
			
			return new ClientProspectData(id, firstName, middleName, lastName, mobileNumber, emailId, 
					sourceOfPublicity, preferredCallingTime, address, tin, preferredLoanProduct, status, isDeleted, note,location);
		}

		public String query() {
			
			String sql = "SQL_CALC_FOUND_ROWS p.id as id, "
					+ "p.first_name as firstName, p.middle_name as middleName, p.last_name as lastName, "
					+ "p.mobile_no as mobileNumber, p.email_id as emailId, p.source_of_publicity as sourceOfPublicity, "
					+ "p.preferred_loan_product as preferredLoanProduct, p.preferred_calling_time as preferredCallingTime, "
					+ "p.address as address, p.status as status, p.tin as tin, p.note as note,p.location as location, "
					+ "p.is_deleted as isDeleted from m_prospect p ";
			return sql;
		}
	}

	@Override
	public Collection<ProspectProductData> retriveProducts() {
		context.authenticatedUser();

		final String sql = "select s.id as id, s.name as productName, s.description as productDescription from m_product_loan s";

		final RowMapper<ProspectProductData> rm = new PeriodMapper();

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	private static final class PeriodMapper implements
			RowMapper<ProspectProductData> {

		@Override
		public ProspectProductData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String productName = rs.getString("productName");
			String productDescription = rs.getString("productDescription");
			
			return new ProspectProductData(id, productName, productDescription);

		}

	}

	@Override
	public ClientProspectData retriveSingleClient(Long id, Long userId) {

		context.authenticatedUser();
		final EditClientProspectMapper rowMapper = new EditClientProspectMapper();
		final String sql = "select " + rowMapper.query()
				+ " from m_prospect p where id=?";
		return jdbcTemplate.queryForObject(sql, rowMapper, new Object[] { id });
	}

	public class EditClientProspectMapper implements
			RowMapper<ClientProspectData> {

		@Override
		public ClientProspectData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			
			Long id = rs.getLong("id");
			String firstName = rs.getString("firstName");
			String middleName = rs.getString("middleName");
			String lastName = rs.getString("lastName");
			String mobileNumber = rs.getString("mobileNumber");
			String emailId = rs.getString("emailId");
			String sourceOfPublicity = rs.getString("sourceOfPublicity");
			String preferredLoanProduct = rs.getString("preferredLoanProduct");
			Date preferredCallingTime = rs.getTimestamp("preferredCallingTime");
			String note = rs.getString("note");
			String location = rs.getString("location");
			String address = rs.getString("address");
			
			String status = rs.getString("status");
			String tin = rs.getString("tin");
			
			String isDeleted = rs.getString("isDeleted");
			
			
			return new ClientProspectData(id, firstName, middleName, lastName, mobileNumber, emailId, 
					sourceOfPublicity, preferredCallingTime, address, tin, preferredLoanProduct, status, isDeleted, note,location);
		}

		public String query() {
			
			return	"p.id as id,p.first_name as firstName, p.middle_name as middleName, p.last_name as lastName, " +
					"p.mobile_no as mobileNumber, p.email_id as emailId, p.source_of_publicity as sourceOfPublicity, " +
					"p.preferred_loan_product as preferredLoanProduct, p.preferred_calling_time as preferredCallingTime, " +
					"p.address as address,p.status as status , p.tin as tin, p.note as note,p.location as location, " +
					"p.is_deleted as isDeleted";
		}
	}
					
}
