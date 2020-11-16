package identity.dbconnector.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="USR_PROF")
public class UserProfile implements Serializable {

 /**
  * 
  */
 private static final long serialVersionUID = 3582501004813497788L;
 
 @Id
 @GeneratedValue(strategy=GenerationType.IDENTITY)
 private Long id;
 
 @Column(name="USR_LOGIN")
 private String userLogin;
 @Column(name="USR_PWD")
 private String password;
 @Column(name="USR_FIRST_NAME")
 private String firstName;
 @Column(name="USR_LAST_NAME")
 private String lastName;
 @Column(name="USR_MIDDLE_NAME")
 private String middleName;
 @Column(name="USR_STATUS")
 private String status;
 @Column(name="USR_CREATE_DATE")
 @Temporal(TemporalType.DATE)
 private Date createDate;
 @Column(name="USR_LASTUPDATE")
 @Temporal(TemporalType.DATE)
 private Date updateDate;
 public Long getId() {
  return id;
 }
 public void setId(Long id) {
  this.id = id;
 }
 public String getUserLogin() {
  return userLogin;
 }
 public void setUserLogin(String userLogin) {
  this.userLogin = userLogin;
 }
 public String getPassword() {
  return password;
 }
 public void setPassword(String password) {
  this.password = password;
 }
 public String getFirstName() {
  return firstName;
 }
 public void setFirstName(String firstName) {
  this.firstName = firstName;
 }
 public String getLastName() {
  return lastName;
 }
 public void setLastName(String lastName) {
  this.lastName = lastName;
 }
 public String getMiddleName() {
  return middleName;
 }
 public void setMiddleName(String middleName) {
  this.middleName = middleName;
 }
 public String getStatus() {
  return status;
 }
 public void setStatus(String status) {
  this.status = status;
 }
 public Date getUpdateDate() {
  return updateDate;
 }
 public void setUpdateDate(Date updateDate) {
  this.updateDate = updateDate;
 }
 public Date getCreateDate() {
  return createDate;
 }
 public void setCreateDate(Date createDate) {
  this.createDate = createDate;
 }

 
 
}
