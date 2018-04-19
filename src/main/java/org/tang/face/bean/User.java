package org.tang.face.bean;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author Ram Alapure
 * @since 05-04-2017
 */

@Entity
@Table(name="face_user")
public class User {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private long id;
	
	private String userName;
	
	private int recogniseCode;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getRecogniseCode() {
		return recogniseCode;
	}

	public void setRecogniseCode(int recogniseCode) {
		this.recogniseCode = recogniseCode;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", userName='" + userName + '\'' +
				", recogniseCode=" + recogniseCode +
				'}';
	}
}
