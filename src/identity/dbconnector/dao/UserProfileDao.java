package identity.dbconnector.dao;

import java.util.Date;
import java.util.List;

import identity.dbconnector.domain.UserProfile;

public interface UserProfileDao {
	public UserProfile findByLogin(String loginId);

	public UserProfile findById(long id);

	public UserProfile createUser(UserProfile userProfile);

	public UserProfile updateUser(UserProfile userProfile);

	public UserProfile deleteUser(long id);

	public List<UserProfile> getAllUser();

	public List<UserProfile> getUsersByChange(Date date);

	public List<UserProfile> findByUsersCriteria(String criteria);

	public void test();

}
