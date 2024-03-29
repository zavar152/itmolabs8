package itmo.labs.zavar.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.commands.base.InputParser;
import itmo.labs.zavar.db.DbUtils;
import itmo.labs.zavar.exception.CommandArgumentException;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.exception.CommandRunningException;
import itmo.labs.zavar.exception.CommandSQLException;
import itmo.labs.zavar.studygroup.Color;
import itmo.labs.zavar.studygroup.Coordinates;
import itmo.labs.zavar.studygroup.Country;
import itmo.labs.zavar.studygroup.FormOfEducation;
import itmo.labs.zavar.studygroup.Location;
import itmo.labs.zavar.studygroup.Person;
import itmo.labs.zavar.studygroup.StudyGroup;

/**
 * Adds a new element to the collection if its value is less than that of the
 * smallest element in this collection. Requires {ELEMENT}.
 * 
 * @author Zavar
 * @version 1.1
 */
public class AddIfMinCommand extends Command {

	private AddIfMinCommand() {
		super("add_if_min", "{ELEMENT}");
	}

	@Override
	public boolean isNeedInput() {
		return true;
	}

	@Override
	public String[] getInputOrder(int type) {
		if (type == 8) {
			return new String[] { "name", "x", "y", "studentsCount", "expelledStudents", "transferredStudents",
					"formOfEducation", "answer" };
		} else {
			return new String[] { "name", "x", "y", "studentsCount", "expelledStudents", "transferredStudents",
					"formOfEducation", "answer", "adminName", "adminPassportID", "adminEyeColor", "adminHairColor",
					"adminCountry", "adminLocation", "adminX", "adminY", "adminZ" };
		}
	}

	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream)
			throws CommandException {
		if (args instanceof String[] && args.length > 0 && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT))) {
			throw new CommandArgumentException("This command doesn't require any arguments!\n" + getUsage());
		} else {
			PrintStream pr = new PrintStream(outStream);
			Scanner in = new Scanner(inStream);
			
			try {
				String name = "";
				Coordinates coordinates = null;
				Long studentsCount = null;
				int expelledStudents = 0;
				long transferredStudents = 0;
				FormOfEducation formOfEducation = null;
				Person groupAdmin = null;

				String admName = "";
				String passportID = "";
				Color eyeColor = null;
				Color hairColor = null;
				Country nationality = null;
				Location location;
				String nameStr = "";
				float x1 = 0f;
				Float y1 = 0f;
				Long z= 0l;
				
				if (type.equals(ExecutionType.CLIENT) | type.equals(ExecutionType.SCRIPT) | type.equals(ExecutionType.INTERNAL_CLIENT)) {
					pr.println("Enter name:");
					name = InputParser.parseString(outStream, in, "Name", Integer.MIN_VALUE, Integer.MAX_VALUE,
							false, false);

					pr.println("Enter X coordinate:");
					Double x = InputParser.parseDouble(outStream, in, "X", -573.0d, Double.MAX_VALUE, false, false);
					pr.println("Enter Y coordinate:");
					Float y = InputParser.parseFloat(outStream, in, "Y", Float.MIN_VALUE, Float.MAX_VALUE, false,
							false);
					coordinates = new Coordinates(x, y);

					pr.println("Enter students count:");
					studentsCount = InputParser.parseLong(outStream, in, "Students count", 0l, Long.MAX_VALUE, false,
							false);

					pr.println("Enter expelled students count:");
					expelledStudents = InputParser.parseInteger(outStream, in, "Expelled students", 0,
							Integer.MAX_VALUE, false, true);

					pr.println("Enter transferred students count:");
					transferredStudents = InputParser.parseLong(outStream, in, "Transferred students", 0l,
							Long.MAX_VALUE, false, true);

					pr.println("Enter form of education, values - " + Arrays.toString(FormOfEducation.values()));
					formOfEducation = FormOfEducation
							.valueOf(InputParser.parseEnum(outStream, in, FormOfEducation.class, false));

					pr.println("Does the group have an admin? [YES]");
					String answ = InputParser.parseString(outStream, in, "Answer", Integer.MIN_VALUE, Integer.MAX_VALUE,
							false, true);
					
					if (answ.equals("YES")) {
						pr.println("Enter name:");
						admName = InputParser.parseString(outStream, in, "Name", Integer.MIN_VALUE,
								Integer.MAX_VALUE, false, false);

						pr.println("Enter passport ID:");
						passportID = InputParser.parseString(outStream, in, "Passport ID", Integer.MIN_VALUE,
								Integer.MAX_VALUE, true, false);

						pr.println("Enter eye color, values - " + Arrays.toString(Color.values()));
						eyeColor = Color.valueOf(InputParser.parseEnum(outStream, in, Color.class, false));

						pr.println("Enter hair color, values - " + Arrays.toString(Color.values()));
						hairColor = Color.valueOf(InputParser.parseEnum(outStream, in, Color.class, false));

						pr.println("Enter country, values - " + Arrays.toString(Country.values()));
						String an = InputParser.parseEnum(outStream, in, Country.class, true);
						if (an != null) {
							nationality = Country.valueOf(an);
						}

						pr.println("Enter name location:");
						nameStr = InputParser.parseString(outStream, in, "Location name", Integer.MIN_VALUE, 348,
								true, false);

						pr.println("Enter X:");
						x1 = InputParser.parseFloat(outStream, in, "X", Float.MIN_VALUE, Float.MAX_VALUE, false,
								true);

						pr.println("Enter Y:");
						y1 = InputParser.parseFloat(outStream, in, "Y", Float.MIN_VALUE, Float.MAX_VALUE, false,
								false);

						pr.println("Enter Z:");
						z = InputParser.parseLong(outStream, in, "Z", Long.MIN_VALUE, Long.MAX_VALUE, false,
								false);
						location = new Location(x1, y1, z, nameStr);
						groupAdmin = new Person(admName, passportID, eyeColor, hairColor, nationality, location);
					}
				}

					StudyGroup temp1 = null;
					if (type.equals(ExecutionType.CLIENT) | type.equals(ExecutionType.SCRIPT) | type.equals(ExecutionType.INTERNAL_CLIENT)) {
						temp1 = new StudyGroup(name, coordinates, studentsCount, expelledStudents, transferredStudents, formOfEducation, groupAdmin);
						super.args = new Object[] {temp1};
					} else if (type.equals(ExecutionType.SERVER)) {
						temp1 = (StudyGroup) args[0];
					}

					if(type.equals(ExecutionType.INTERNAL_CLIENT)) {
						args = new String[] {"internal"};
					}
					
					if (type.equals(ExecutionType.SERVER) | type.equals(ExecutionType.SCRIPT) | type.equals(ExecutionType.INTERNAL_CLIENT)) {
						Connection con = null;
						PreparedStatement stmt;

						try {
							con = env.getDbManager().getConnection();
						} catch (SQLException e2) {
							throw new CommandSQLException("Failed to connect to database!");
						}
						
						stmt = con.prepareStatement(DbUtils.getMinElement());
						ResultSet rs = stmt.executeQuery();
						rs.next();

						if (temp1.getCreationLocalDate().compareTo(LocalDate.parse(rs.getString(1))) < 0) {

							stmt = con.prepareStatement(DbUtils.addElement(temp1, env.getUser((String) args[args.length-1])));

							if (stmt.executeUpdate() == 0) {
								pr.println("Element didn't add!");
							} else {
								pr.println("Element added!");
							}

						} else {
							pr.println("Element greater than min element in collection!");
						}
						con.close();
					}
				} catch (InputMismatchException e) {
					throw new CommandRunningException("Input closed!");
				} catch (Exception e) {
					throw new CommandRunningException("Parsing error!");
				}
		}
	}

	/**
	 * Uses for commands registration.
	 * 
	 * @param commandsMap Commands' map.
	 */
	public static void register(HashMap<String, Command> commandsMap) {
		AddIfMinCommand command = new AddIfMinCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command added element if its creation date is less than min value in collection!";
	}
}
