package itmo.labs.zavar.client.util;

import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

import itmo.labs.zavar.studygroup.Color;
import itmo.labs.zavar.studygroup.Country;
import itmo.labs.zavar.studygroup.Location;
import itmo.labs.zavar.studygroup.Person;

/**
 * Class to parse person object for {@link CellProcessor}.
 * 
 * @author Zavar
 * @version 1.1
 */
public class ParsePerson extends CellProcessorAdaptor implements StringCellProcessor {
	public ParsePerson() {
		super();
	}

	public ParsePerson(final CellProcessor next) {
		super(next);
	}

	@Override
	public <T> T execute(final Object value, final CsvContext context) {
		validateInputNotNull(value, context);

		final Person person;

		if (value instanceof String) {
			try {
				String[] str = ((String) value).split(",");
				if (!str[0].trim().equals("null")) {
					person = new Person(str[0].trim(), str[1].trim(), Color.valueOf(str[2].trim()),
							Color.valueOf(str[3].trim()), Country.valueOf(str[4].trim()),
							new Location(Float.parseFloat(str[5].trim()), Float.parseFloat(str[6].trim()),
									(long) Float.parseFloat(str[7].trim()), str[8].trim()));
				} else {
					person = null;
				}
				
			} catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
				throw new SuperCsvCellProcessorException(String.format("'%s' could not be parsed as an Person", value),
						context, this, e);
			}
		} else {
			final String actualClassName = value.getClass().getName();
			throw new SuperCsvCellProcessorException(
					String.format("the input value should be of type String but is of type %s", actualClassName),
					context, this);
		}

		return next.execute(person, context);
	}

}
