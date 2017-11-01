package test.beans;

import java.io.Serializable;

public class TestUser implements Serializable{
	private static final long serialVersionUID = -2201177266311075246L;
	
	private long id;
	private String username;
	public TestUser() {
	}
	public TestUser(long id, String username) {
		this.id = id;
		this.username = username;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String toString() {
		return "{id:'"+ id +"', username:'"+ username +"'}";
	}
}
