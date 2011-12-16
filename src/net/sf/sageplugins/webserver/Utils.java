/*
 * Created on 27-Apr-2005
 *
 * 
 *
 */
package net.sf.sageplugins.webserver;


import java.io.PrintWriter;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Niel
 *
 * 
 *
 */
public abstract class Utils {
	
	static void PrintDateTimeWidget(PrintWriter out, long time_millis, String name_prefix){
		out.println("<table class=\"DateTimeEdit\">");
		out.println("<tr><td>Year</td><td>/</td><td>Month</td><td>/</td><td>Date</td><td>-</td><td>Hr(24h)</td><td>:</td><td>Min</td></tr>");
		out.println("<tr>");
	    GregorianCalendar cal=new GregorianCalendar();
	    cal.setTimeInMillis(time_millis);

	    out.println("<td><input type=\"text\" onchange=\"RemoveNonNumbers(this)\" size=\"4\" name=\""+name_prefix+"_yr\" value=\""+Integer.toString(cal.get(Calendar.YEAR))+"\"></td>");
	    out.println("<td>/</td>");
	    out.println("<td><select name=\""+name_prefix+"_mth\">");
		DateFormatSymbols dsyms=new DateFormatSymbols();
		String[] months=dsyms.getMonths();
		for(int i=GregorianCalendar.JANUARY;i<=GregorianCalendar.DECEMBER;i++){
			out.print("    <option value=\""+
					Integer.toString(i)+"\"");
			if ( i==cal.get(Calendar.MONTH) )
				out.print(" selected=\"selected\"");
			out.println(">"+months[i]+"</option>");
		}
	    out.println("</select></td>");
	    out.println("<td>/</td>");
	    out.println("<td><input type=\"text\" onchange=\"RemoveNonNumbers(this)\" size=\"2\" name=\""+name_prefix+"_dd\" value=\""+Integer.toString(cal.get(Calendar.DAY_OF_MONTH))+"\"></td>");
	    out.println("<td>-</td>");
	    out.println("<td><input type=\"text\" onchange=\"RemoveNonNumbers(this)\" size=\"2\" name=\""+name_prefix+"_hh\" value=\""+Integer.toString(cal.get(Calendar.HOUR_OF_DAY))+"\"></td>");
	    out.println("<td>:</td>");
	    out.println("<td><input type=\"text\" onchange=\"RemoveNonNumbers(this)\" size=\"2\" name=\""+name_prefix+"_mm\" value=\""+Integer.toString(cal.get(Calendar.MINUTE))+"\"></td>");
	    out.println("</tr></table>");
	    
	}
	static long ParseDateTimeWidget(String name_prefix, HttpServletRequest req)
	throws NumberFormatException
	{
		GregorianCalendar cal=new GregorianCalendar(
				getIntParam(req,name_prefix+"_yr","Year",Integer.MIN_VALUE,Integer.MAX_VALUE),
				getIntParam(req,name_prefix+"_mth","Month",0,11),
				getIntParam(req,name_prefix+"_dd","Day",1,31),
				getIntParam(req,name_prefix+"_hh","Hour",0,23),
				getIntParam(req,name_prefix+"_mm","Minute",0,59));
		return cal.getTimeInMillis();		
	}
	static int getIntParam(HttpServletRequest req,String paramname,String parammsg, int lowrange,int highrange)
	throws NumberFormatException {
		String p=null;
		int rv=Integer.MIN_VALUE;
		if ( (p=req.getParameter(paramname))!=null){
			try {
				rv=Integer.parseInt(p);
			} catch (Exception e) {
				throw new NumberFormatException("invalid integer "+p+" for "+parammsg);
			}
			if ( rv < lowrange || rv > highrange) {
				throw new NumberFormatException("Integer out of range: "+p+" for "+parammsg);
			}
		} else {
			throw new NumberFormatException("Missing parameter "+parammsg);
		}
		return rv;
	}
	
}
