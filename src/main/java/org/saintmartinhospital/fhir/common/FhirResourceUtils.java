package org.saintmartinhospital.fhir.common;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.hl7.fhir.r4.model.IdType;

public class FhirResourceUtils {


    private static final TimeZone TIME_ZONE = Calendar.getInstance().getTimeZone();
    private static final String CALENDAR_FORMAT = "yyyy-MM-dd";	
	
	public static String getString( StringParam primitive ) {
		return primitive == null? null: primitive.getValue();
	}
	
	public static Date getDate( DateParam primitive ) {
		return primitive == null? null: primitive.getValue();
	}
	
	public static Long getLong( IdType id ) {
		return id == null? null: id.getIdPartAsLong();
	}
	
	public static Integer getInteger( IdType id ) {
		Long num = getLong( id );
		return num == null? null: num.intValue();
	}
	
	public static Calendar getCalendar( DateParam primitive ) {
		Calendar calendar = null;
		if( primitive != null ) {
			calendar = Calendar.getInstance();
			calendar.setTime( primitive.getValue() );
		}
		return calendar;
	}
	
    public static SimpleDateFormat getCalendarFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat( CALENDAR_FORMAT );
        sdf.setLenient(false);
        sdf.setTimeZone( TIME_ZONE );
        return sdf;
    }	
	
	public static String format( Calendar calendar ) {
        String str = null;
        
        if( calendar != null ) {
            SimpleDateFormat sdf = getCalendarFormat();
            try {
                str = sdf.format( calendar.getTime() );
            } catch( IllegalArgumentException e ) {
                throw new IllegalArgumentException( String.format( "El formato de la fecha [%s] no es válido", sdf.toPattern() ), e );
            }
        }
        
        return str;		
	}
	
	private FhirResourceUtils() {
	}	
	
}
