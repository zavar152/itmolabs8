package itmo.labs.zavar.client;

import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseEnum;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import itmo.labs.zavar.studygroup.FormOfEducation;
import itmo.labs.zavar.studygroup.StudyGroup;

public class StringTetst {

	private final static String[] nameMapping = new String[] { "id", "name", "coordinates", "creationDate",
			"studentsCount", "expelledStudents", "transferredStudents", "formOfEducation", "groupAdmin" };
	private static ICsvBeanReader beanReader;
	private static Stack<StudyGroup> stack = new Stack<StudyGroup>(); 
	
	public static void main(String[] args) {
		generateStydyGroup("241;tyt;5,3;2021-04-20;23;123;534;FULL_TIME_EDUCATION;null,0,BLACK,BLACK,USA,0,0,0,");

	}

	private static void generateStydyGroup(String s) {
		beanReader = new CsvBeanReader(new StringReader(s), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
			//beanReader.getHeader(true);
		StudyGroup temp;
		try {
			while ((temp = beanReader.read(StudyGroup.class, nameMapping, getReaderProcessors())) != null) {
					stack.push(temp);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			beanReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static CellProcessor[] getReaderProcessors() {
		CellProcessor[] processors = new CellProcessor[] {
				new NotNull(new ParseLong()), 
				new NotNull(),
				new NotNull(new ParseCoordinates()), 
				new NotNull(new ParseDate("yyyy-MM-dd")),
				new NotNull(new ParseLong()), 
				new NotNull(new ParseInt()), 
				new NotNull(new ParseLong()),
				new NotNull(new ParseEnum(FormOfEducation.class)), 
				new Optional(new ParsePerson()) };
		return processors;
	}
	
}
