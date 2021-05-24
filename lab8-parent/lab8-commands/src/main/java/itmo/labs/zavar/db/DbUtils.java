package itmo.labs.zavar.db;

import itmo.labs.zavar.studygroup.StudyGroup;

public class DbUtils {

	public static String getAll() {
		return "SELECT * FROM studygroups;";
	}

	public static String getCount() {
		return "SELECT COUNT(*) FROM studygroups;";
	}

	public static String countGreaterThanTs(long ts) {
		return "SELECT COUNT(transferredstudents) FROM studygroups WHERE transferredstudents > " + ts + ";";
	}
	
	public static String getCreationDate() {
		return "SELECT MIN(creationdate) FROM studygroups;";
	}
	
	public static String getMaxElement() {
		return "SELECT MAX(creationdate) FROM studygroups;";
	}
	
	public static String register() {
		return "INSERT INTO users (name, password) VALUES ( ? , ? )";
	}
	
	public static String getUser() {
		return "SELECT * FROM users WHERE name =  ? ;";
	}
	
	public static String getMinElement() {
		return "SELECT MIN(creationdate) FROM studygroups;";
	}
	
	public static String getOwner(long id) {
		return "SELECT owner FROM studygroups WHERE id = " + id + ";";
	}
	
	public static String clearAll() {
		return "TRUNCATE TABLE studygroups;";
	}
	
	public static String deleteById(long id) {
		return "DELETE FROM studygroups WHERE id = " + id + ";";
	}
	
	public static String deleteBySc(long sc) {
		return "DELETE FROM studygroups WHERE studentsCount = " + sc + ";";
	}
	
	public static String getBySc(long sc) {
		return "SELECT owner, studentsCount FROM studygroups WHERE studentsCount = " + sc + ";";
	}
	
	public static String getById(long id) {
		return "SELECT * FROM studygroups WHERE id = " + id + ";";
	}
	
	public static String averageOfTs() {
		return "SELECT AVG(transferredStudents) FROM studygroups;";
	}
	
	public static String deleteMainTable() {
		return "DROP TABLE studygroups;";
	}
	
	public static String addElement(StudyGroup group, String owner) {
		if(group.getGroupAdmin() == null) {
			return "INSERT INTO studygroups (id, name, x, y, creationDate, studentsCount, expelledStudents, transferredStudents, formOfEducation, adminName, adminPassportID, adminEyeColor, adminHairColor, adminNationality, adminLocationX, adminLocationY, adminLocationZ, adminLocationName, owner) "
					+ "VALUES ((SELECT nextval('sequence_id')), '" + group.getName() + "', " + group.getCoordinates().getX() + ", " + group.getCoordinates().getY() + ", '" + group.getCreationLocalDate() + "', " + group.getStudentsCount() + ", " + group.getExpelledStudents() + ", " + group.getTransferredStudents() + ", '" + group.getFormOfEducation() + "', "
							+ "null, " + 0 + ", 'BLACK', 'BLACK', 'USA', " + 0 + ", " + 0 + ", " + 0 + ", '', '" + owner + "');";
		}
		else {
			return "INSERT INTO studygroups (id, name, x, y, creationDate, studentsCount, expelledStudents, transferredStudents, formOfEducation, adminName, adminPassportID, adminEyeColor, adminHairColor, adminNationality, adminLocationX, adminLocationY, adminLocationZ, adminLocationName, owner) "
					+ "VALUES ((SELECT nextval('sequence_id')), '" + group.getName() + "', " + group.getCoordinates().getX() + ", " + group.getCoordinates().getY() + ", '" + group.getCreationLocalDate() + "', " + group.getStudentsCount() + ", " + group.getExpelledStudents() + ", " + group.getTransferredStudents() + ", '" + group.getFormOfEducation() + "', "
							+ "'" + group.getGroupAdmin().getName() + "', " + group.getGroupAdmin().getPassportID() + ", '" + group.getGroupAdmin().getEyeColor() + "', '" + group.getGroupAdmin().getHairColor() + "', '" + group.getGroupAdmin().getNationality() + "', " + group.getGroupAdmin().getLocation().getX() + ", " + group.getGroupAdmin().getLocation().getY() + ", " + group.getGroupAdmin().getLocation().getZ() + ", '" + group.getGroupAdmin().getLocation().getName() + "', '" + owner + "');";
		}
	}
	
	public static String createMainTable() {
		return "create table studygroups (\r\n"
				+ "    id BIGINT PRIMARY KEY NOT NULL UNIQUE CHECK ( id > 0 ),\r\n"
				+ "    name text NOT NULL,\r\n"
				+ "    x real NOT NULL CHECK ( x > -573 ),\r\n"
				+ "    y double precision NOT NULL,\r\n"
				+ "    creationDate date NOT NULL,\r\n"
				+ "    studentsCount bigint NOT NULL CHECK ( studentsCount > 0 ),\r\n"
				+ "    expelledStudents int NOT NULL CHECK ( expelledStudents > 0 ),\r\n"
				+ "    transferredStudents bigint NOT NULL CHECK ( transferredStudents > 0 ),\r\n"
				+ "    formOfEducation text NOT NULL CHECK ( formOfEducation IN ('DISTANCE_EDUCATION', 'FULL_TIME_EDUCATION', 'EVENING_CLASSES')),\r\n"
				+ "    adminName text,\r\n"
				+ "    adminPassportID text,\r\n"
				+ "    adminEyeColor text NOT NULL CHECK ( adminEyeColor IN ('GREEN', 'BLACK', 'BLUE', 'WHITE') ),\r\n"
				+ "    adminHairColor text NOT NULL CHECK ( adminHairColor IN ('GREEN', 'BLACK', 'BLUE', 'WHITE') ),\r\n"
				+ "    adminNationality text CHECK ( adminNationality IN ('USA', 'GERMANY', 'INDIA', 'VATICAN', 'ITALY') ),\r\n"
				+ "    adminLocationX real NOT NULL,\r\n"
				+ "    adminLocationY real NOT NULL,\r\n"
				+ "    adminLocationZ double precision NOT NULL,\r\n"
				+ "    adminLocationName text NOT NULL CHECK ( length(adminLocationName) < 348 ),\r\n"
				+ "	   owner TEXT NOT NULL"
				+ ");";
	}
	
	public static String addSequence() {
		return "create sequence sequence_id\r\n"
				+ "MINVALUE 1\r\n"
				+ "START WITH 1\r\n"
				+ "INCREMENT BY 1\r\n"
				+ "CACHE 20;";
	}
}
