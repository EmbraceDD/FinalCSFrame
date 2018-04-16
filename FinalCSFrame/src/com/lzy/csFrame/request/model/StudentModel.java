package com.lzy.csFrame.request.model;

public class StudentModel {
   private String id;
   private String name;
   private String sex;
   private String score;
   
   public StudentModel() {
    }

	public StudentModel(String id, String name, String sex, String score) {
		this.id = id;
		this.name = name;
		this.sex = sex;
		this.score = score;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "id=" + id + ", name=" + name + ", sex=" + sex + ", score=" + score;
	}
	   
   
   
}
